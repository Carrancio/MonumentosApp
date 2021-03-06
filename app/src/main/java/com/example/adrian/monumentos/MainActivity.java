package com.example.adrian.monumentos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.adrian.monumentos.Fragmentos.AboutFragment;
import com.example.adrian.monumentos.Fragmentos.HomeFragment;
import com.example.adrian.monumentos.Fragmentos.MapFragment;
import com.example.adrian.monumentos.Fragmentos.POIListFragment;
import com.example.adrian.monumentos.Utilidades.HttpHandler;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Esta es la clase principal de la aplicación. Se encarga de obtener, en primera instancia, las coordenadas del GPS del usuario,
 * para, una vez se quiera obtener la lista de POIs o el mapa mostrando los mismos, se disparen una serie de consultas a la API de
 * la WikiPedia para obtener esos datos.
 *
 * <p>Se encarga también de gestionar que al usuario se le pidan permisos para utilizar su ubicación, así como de evitar que se
 * utilice la aplicación sin tener una conexión a Internet o sin tener el GPS activado, mostrando un AlertDialog con el error
 * en ese caso.</p>
 *
 * @author Adrián Muñoz Rojo
 * @author Rafael Matamoros Luque
 * @author David Carrancio Aguado
 * @see AboutFragment
 * @see HomeFragment
 * @see MapFragment
 * @see POIListFragment
 * @see HttpHandler
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private boolean permissionRequestDone = false;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private String idioma;

    private final String TAG = MainActivity.class.getSimpleName();

    //Coordenadas GPS del usuario
    private double latitudGPS;
    private double longitudGPS;

    //Radio de búsqueda de POIs (por defecto 1 km)
    private int radio = 1000;

    //Número máximo de búsqueda de POIs (por defecto 30)
    private int maxPOI = 30;

    //Radio de búsqueda introducido por el usuario (por defecto -1)
    private int inputRadioBusqueda = -1;

    //Número máximo de POIs introducido por el usuario (por defecto -1)
    private int inputNMaxPOI = -1;


    private GoogleApiClient mGoogleApiClient;

    /*
     * Request code to use when launching the resolution activity
     */
    private static final int REQUEST_RESOLVE_ERROR = 1001;

    /*
     * Unique tag for the error dialog fragment
     */
    private static final String DIALOG_ERROR = "dialog_error";

    /*
     * Bool to track whether the app is already resolving an error
     */
    private boolean mResolvingError = false;
    private static final String STATE_RESOLVING_ERROR = "resolving_error";

    private static final int REQUEST_LOCATION = 2;
    private static final int LOCATION_INTERVAL = 1000;

    //Dialogo que se muestra mientras se obtienen los datos de los POIs
    private ProgressDialog progressDialog;

    private GlobalState globalState;

    /**
     * Called when the activity is starting
     *
     * @param savedInstanceState  If the activity is being re-initialized after previously
     *                            being shut down then this Bundle contains the data it most recently
     *                            supplied in onSaveInstanceState(Bundle). Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean hasPermission = hasPermisosUbicacion();

        //Solicitar permisos de ubicación en caso de no tenerlos
        if (!hasPermission) {
            if (permissionRequestDone)
                requestPermission();
        }

        //Obtencion del idioma del telefono
        idioma = Locale.getDefault().getLanguage();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.datos));
        progressDialog.setCanceledOnTouchOutside(false);

        mResolvingError = savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);

        // Crea una instancia de  GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        //Conexión con la API de Google Services para obtener coordenadas GPS
        mGoogleApiClient.connect();

        //Fragmento inicial "Home" de la App
        final HomeFragment homeFragment = new HomeFragment();
        getFragmentManager()
                .beginTransaction()
                .add(R.id.content_frame, homeFragment)
                .commit();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navview);

        //Hay que asegurarse de que se han obtenido correctamente las coordenadas del GPS
        obtenerCoordenadasGPS();

        globalState = (GlobalState) MainActivity.this.getApplicationContext();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                Fragment fragment;

                //menu izquierdo
                switch (item.getItemId()) {
                    case R.id.menu_inicio:

                        fragment = getFragmentManager().findFragmentById(R.id.content_frame);

                        if (!(fragment instanceof HomeFragment)) {
                            getFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.content_frame, homeFragment)
                                    .addToBackStack("HomeFragment")
                                    .commit();

                            if(getSupportActionBar() != null) {
                                getSupportActionBar().setTitle(getResources().getString(R.string.menu_inicio));
                            }
                        }

                        break;

                    case R.id.menu_monumentos:

                        //Primero comprobamos que tanto el GPS como la conexión a Internet están activados
                        if (isGPSAndInternetEnabled()) {

                            mostrarInformacion("Monumentos");

                            if(getSupportActionBar() != null) {
                                getSupportActionBar().setTitle(R.string.menu_monumentos);
                            }
                        }

                        break;

                    case R.id.menu_mapa:

                        //Primero comprobamos que tanto el GPS como la conexión a Internet están activados
                        if (isGPSAndInternetEnabled()) {

                            mostrarInformacion("Mapa");

                            if(getSupportActionBar() != null) {
                                getSupportActionBar().setTitle(getResources().getString(R.string.menu_mapa));
                            }
                        }

                        break;

                    case R.id.menu_ayuda:

                        fragment = getFragmentManager().findFragmentById(R.id.content_frame);

                        AboutFragment aboutFragment = new AboutFragment();

                        if (!(fragment instanceof AboutFragment)) {
                            getFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.content_frame, aboutFragment)
                                    .addToBackStack("AboutFragment")
                                    .commit();

                            if(getSupportActionBar() != null) {
                                getSupportActionBar().setTitle(getResources().getString(R.string.menu_sobre_app));
                            }
                        }

                        break;
                }
                drawerLayout.closeDrawers();

                return true;
            }
        });

        //Toolbar
        Toolbar appbar = (Toolbar) findViewById(R.id.appbar);
        setSupportActionBar(appbar);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_menu);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.menu_inicio);
    }

    /*
     * Clase encargada de obtener la información en relación a los POIs.
     *
     * Se encarga de realizar una serie de consultas a la API de la WikiPedia para obtener esta información.
     */
    private class GETPOIs extends AsyncTask<Void, Void, Void> {

        private final ProgressDialog progressDialog;
        private final boolean mostrarMapa;
        private final boolean mostrarMonumentos;
        private final boolean recalcularURL;

        GETPOIs(ProgressDialog progressDialog, boolean mostrarMapa, boolean mostrarMonumentos, boolean recalcularURL) {
            this.progressDialog = progressDialog;
            this.mostrarMapa = mostrarMapa;
            this.mostrarMonumentos = mostrarMonumentos;
            this.recalcularURL = recalcularURL;
        }

        @Override
        protected void onPreExecute() {
            progressDialog.show();
            super.onPreExecute();
        }

        /**
         * OBtencion del json para obtener los POIs
         *
         * @param params Params
         */
        @Override
        protected Void doInBackground(Void... params) {

            if (recalcularURL) {
                ArrayList<POI> listaPOIs = new ArrayList<>();
                ArrayList<ArrayList<String>> listaPoi = new ArrayList<>();
                String pageIds = "";

                HttpHandler handler = new HttpHandler();

                String HTTPS = "https://";
                String WIKI_URL = ".wikipedia.org/w/api.php?action=query&format=json";

                //Petición a la API de Wikipedia y almacenamiento de la respuesta
                String urlCoords = HTTPS + idioma + WIKI_URL + "&list=geosearch&gscoord=" +
                        latitudGPS + "%7C" + longitudGPS + "&gsradius=" + radio + "&gslimit=" + maxPOI;

                String jSonCoords = handler.makeServiceCall(urlCoords);
                String jSonExtract, jSonImage;

                //Eliminiación de la cabecera del primer JSON (28 caracteres)
                jSonCoords = jSonCoords.substring(28);

                try {
                    JSONObject jsonObjectCoords = new JSONObject(jSonCoords);

                    //Array de JSONs
                    JSONArray geosearch = jsonObjectCoords.getJSONArray("geosearch");

                    //Bucle de recorrido del JSONArray con los distintos POIs
                    for (int i = 0; i < geosearch.length(); i++) {
                        JSONObject object = geosearch.getJSONObject(i);

                        ArrayList<String> poi = new ArrayList<>();

                        pageIds += object.getString("pageid") + '|';

                        poi.add(object.getString("pageid"));
                        poi.add(object.getString("title"));
                        poi.add(object.getString("lat"));
                        poi.add(object.getString("lon"));

                        listaPoi.add(poi);
                    }

                    //Extraemos el último carácter añadido
                    pageIds = pageIds.substring(0, pageIds.length() - 1);

                    for (int j = 0; j < geosearch.length(); j++) {
                        //Segunda petición a la API de WikiPedia para obtener el "extract" de un POI
                        String urlExtract = HTTPS + idioma + WIKI_URL + "&prop=extracts&exintro=&explaintext=&pageids=" + listaPoi.get(j).get(0);

                        jSonExtract = handler.makeServiceCall(urlExtract);

                        //Obtención del objeto JSON
                        JSONObject jsonObjectExtract = new JSONObject(jSonExtract);

                        listaPoi.get(j).add(jsonObjectExtract.getJSONObject("query").getJSONObject("pages").getJSONObject(listaPoi.get(j).get(0)).getString("extract"));
                    }

                    //Tercera petición a la API de WikiPedia para extraer la URL de la imagen de un POI
                    String urlImage = HTTPS + idioma + WIKI_URL + "&prop=pageprops|info|pageimages&inprop=url&pilimit=50&pithumbsize=560&pageids=" + pageIds;

                    jSonImage = handler.makeServiceCall(urlImage);

                    //Obtención del objeto JSON
                    JSONObject jsonObjectImage = new JSONObject(jSonImage);

                    //Array de JSons
                    JSONObject images = jsonObjectImage.getJSONObject("query").getJSONObject("pages");


                    //Bucle de recorrido de la lista de POIS para añadir la URL de la imagen y el enlace
                    for (ArrayList<String> nuevoPoi : listaPoi) {

                        try {
                            //Añadir la URL de la imagen al ArrayList
                            nuevoPoi.add(images.getJSONObject(nuevoPoi.get(0)).getJSONObject("thumbnail").getString("source"));
                        } catch (JSONException e) {
                            nuevoPoi.add(null);
                        }

                        //Añadir el enlace al ArrayList
                        nuevoPoi.add(HTTPS + idioma + ".m.wikipedia.org/wiki?curid=" + nuevoPoi.get(0));
                    }

                    //Bucle para crear definitivamente el POI con los datos obtenidos
                    for (ArrayList<String> nuevoPoi : listaPoi) {
                        listaPOIs.add(new POI(nuevoPoi.get(1), nuevoPoi.get(4), Double.parseDouble(nuevoPoi.get(2)), Double.parseDouble(nuevoPoi.get(3)), nuevoPoi.get(5), nuevoPoi.get(6)));
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Un error inesperado ocurrió: " + e.getMessage());
                }
                globalState.setListaPOIs(listaPOIs);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            super.onPostExecute(aVoid);

            if (mostrarMapa)
                mostrarMapFragment();
            else if (mostrarMonumentos)
                mostrarPOIListFragment();
        }
    }

    /**
     * Called when you are no longer visible to the user.
     */
    @Override
    protected void onStop() {
        // solo se para si se conecta
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    /**
     * After calling connect(), this method will be invoked asynchronously when the connect request has successfully completed.
     *
     * @param bundle Bundle of data provided to clients by Google Play services. May be null if no content is provided by the service.
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            startLocationUpdates();

            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (location != null) {
                latitudGPS = location.getLatitude();
                longitudGPS = location.getLongitude();
            }
        }
    }

    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            // Crea la localizacion
            LocationRequest locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(LOCATION_INTERVAL)
                    .setFastestInterval(LOCATION_INTERVAL);

            // Request location updates
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    locationRequest, this);
        }
    }

    /**
     * Callback for the result from requesting permissions.
     *
     * @param requestCode The request code passed in requestPermissions(android.app.Activity, String[], int)
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // We can now safely use the API we requested access to connect the client
                mGoogleApiClient.connect();
            } else {
                //Informar al usuario de la necesidad de utilizar los permisos de ubicación
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("La aplicación necesita acceder a su ubicación para poder ejecutarse")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Pedir permisos de ubicación de nuevo
                                requestPermission();
                            }
                        })
                        .show();
            }
        }
        permissionRequestDone = true;
    }

    /**
     * Called when an activity you launched exits, giving you the requestCode you started it with, the resultCode it returned, and any additional data from it.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult(), allowing you to identify who this result came from.
     * @param resultCode The integer result code returned by the child activity through its setResult().
     * @param data  An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }
    }

    /**
     * Provides callbacks that are called when the client is connected or disconnected from the service.
     *
     * @param i The reason for the disconnection. Defined by constants CAUSE_*.
     */
    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(this, "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Called when there was an error connecting the client to the service.
     *
     * @param connectionResult A ConnectionResult that can be used for resolving the error, and deciding what sort of error occurred.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (!mResolvingError) {
            if (connectionResult.hasResolution()) {
                try {
                    mResolvingError = true;
                    connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
                } catch (IntentSender.SendIntentException e) {
                    // There was an error with the resolution intent. Try again.
                    mGoogleApiClient.connect();
                }
            } else {
                // Show dialog using GooglePlayServicesUtil.getErrorDialog()
                showErrorDialog(connectionResult.getErrorCode());
                mResolvingError = true;
            }
        }
    }

    //Creates a dialog for an error message
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        MainActivity.ErrorDialogFragment dialogFragment = new MainActivity.ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    //Called from ErrorDialogFragment when the dialog is dismissed.
    private void onDialogDismissed() {
        mResolvingError = false;
    }

    //A fragment to display an error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() {
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((MainActivity) getActivity()).onDialogDismissed();
        }
    }

    //Comprobación permisos ubicación (GPS) concedidos
    private boolean hasPermisosUbicacion() {

        return ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

    }

    //Solicitar permisos de ubicación al usuario
    private void requestPermission() {

        // Check Permissions Now
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION);
    }

    //Método encargado de comprobar si se han obtenido ya las coordenadas GPS y, en caso contrario, obtenerlas
    private void obtenerCoordenadasGPS() {
        mGoogleApiClient.reconnect();
    }

    /**
     * Método que obtiene los datos necesarios para MapFragment
     *
     * @return Bundle con la información de la ubicación
     */
    public Bundle obtenerArgumentos() {

        //Almacenamos los datos necesarios para la utilización de HomeFragment
        Bundle params = new Bundle();

        params.putDouble("latitudGPS", latitudGPS);
        params.putDouble("longitudGPS", longitudGPS);

        return params;
    }

    /**
     * Called to retrieve per-instance state from an activity before being killed
     *
     * @param outState Bundle in which to place your saved state.
     * @param outPersistentState Bundle in which to place your saved state.
     */
    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }

    /**
     * Called when the location has changed.
     *
     * @param location The new location, as a Location object.
     */
    @Override
    public void onLocationChanged(Location location) {

        latitudGPS = location.getLatitude();
        longitudGPS = location.getLongitude();

        if ((latitudGPS != 0.0) && (longitudGPS != 0.0)) {
            // Disconnecting the client invalidates it.
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     *
     * @param item The menu item that was selected.
     * @return Return false to allow normal menu processing to proceed, true to consume it here.
     */
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:

                drawerLayout.openDrawer(GravityCompat.START);
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Inflamos el menu lateral
     *
     * @param menu que hemos creado
     * @return true si el menu esta inflado
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_lateral, menu);
        return true;
    }

    /**
     * Prepare the Screen's standard options menu to be displayed
     *
     * @param menu The options menu as last shown or first initialized by onCreateOptionsMenu().
     * @return  true for the menu to be displayed; if you return false it will not be shown.
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Método encargado de comprobar los datos que se han introducido (si se han introducido),
     * verificar que los datos son nuevos a los que se tenían antes, y, si es necesario, mostrarlos
     * o simplemente obtener nuevos datos sin mostrarlos.
     *
     * @param tipoInfo String que indica si se debe mostrar información del Mapa o de Monumentos
     */
    public void mostrarInformacion(String tipoInfo) {

        boolean mostrarMapa, mostrarMonumentos;

        if (tipoInfo.equals("Mapa")) {
            mostrarMapa = true;
            mostrarMonumentos = false;
        } else {
            mostrarMonumentos = true;
            mostrarMapa = false;
        }

        //Comprobamos que se disponen de las coordenadas antes de crear el fragmento
        comprobarObtencionCoordenadas();

        int hayNuevaInformacion = usuarioHaIntroducidoInformacion();

        boolean hayInformacionPrevia = !globalState.getListaPOIs().isEmpty();

        switch (hayNuevaInformacion) {
            case 0:
                //El usuario no ha introducido ningún parámetro
                if (hayInformacionPrevia)
                    if ((maxPOI == 30) && (radio == 1000)) {
                        //La información que hay es la información por defecto
                        //Sólamente hay que mostrar la información que ya teníamos guardada
                        new GETPOIs(progressDialog, mostrarMapa, mostrarMonumentos, false).execute();
                    } else {
                        //La información que hay es distinta de la información por defecto
                        maxPOI = 30;
                        radio = 1000;
                        //Mostramos la información por defecto
                        new GETPOIs(progressDialog, mostrarMapa, mostrarMonumentos, true).execute();
                    }
                else
                    //Es necesario obtener la información y, después, mostrarla
                    new GETPOIs(progressDialog, mostrarMapa, mostrarMonumentos, true).execute();
                break;

            case 1:
                //El usuario sólo ha introducido el parámetro "nMaxPOI"
                if (hayInformacionPrevia)
                    if (inputNMaxPOI == maxPOI)
                        //La información introducida por el usuario es la misma que se tenía previamente
                        new GETPOIs(progressDialog, mostrarMapa, mostrarMonumentos, false).execute();
                    else {
                        //La información introducida por el usuario es nueva
                        maxPOI = inputNMaxPOI;
                        new GETPOIs(progressDialog, mostrarMapa, mostrarMonumentos, true).execute();
                    }
                else {
                    //No se tiene información previa, luego hay que obtener los POIs de 0
                    maxPOI = inputNMaxPOI;
                    new GETPOIs(progressDialog, mostrarMapa, mostrarMonumentos, true).execute();
                }
                break;

            case 2:
                //El usuario sólo ha introducido el parámetro "Radio"
                if (hayInformacionPrevia)
                    if (inputRadioBusqueda == radio)
                        //La información introducida por el usuario es la misma que se tenía previamente
                        new GETPOIs(progressDialog, mostrarMapa, mostrarMonumentos, false).execute();
                    else {
                        //La información introducida por el usuario es nueva
                        radio = inputRadioBusqueda;
                        new GETPOIs(progressDialog, mostrarMapa, mostrarMonumentos, true).execute();
                    }
                else {
                    //No se tiene información previa, luego hay que obtener los POIs de 0
                    radio = inputRadioBusqueda;
                    new GETPOIs(progressDialog, mostrarMapa, mostrarMonumentos, true).execute();
                }
                break;

            case 3:
                //El usuario ha introducido tanto el parámetro "nMaxPOI" como "Radio"
                if (hayInformacionPrevia) {
                    if (inputNMaxPOI == maxPOI) {
                        if (inputRadioBusqueda == radio) {
                            //La información que se tiene es la misma que ha introducido el usuario. Sólo es necesario mostrar, no hay que obtenerla de nuevo
                            new GETPOIs(progressDialog, mostrarMapa, mostrarMonumentos, false).execute();
                        } else {
                            //La información introducida por el usuario es distinta de la que teníamos para el parámetro "Radio"
                            radio = inputRadioBusqueda;
                            new GETPOIs(progressDialog, mostrarMapa, mostrarMonumentos, true).execute();
                        }
                    } else {
                        if (inputRadioBusqueda == radio) {
                            //La información introducida por el usuario es distinta de la que teníamos para el parámetros "nMaxPOI"
                            maxPOI = inputNMaxPOI;
                            new GETPOIs(progressDialog, mostrarMapa, mostrarMonumentos, true).execute();
                        } else {
                            //La información introducida por el usuario es distinta de la que teníamos para ambos parámetros
                            maxPOI = inputNMaxPOI;
                            radio = inputRadioBusqueda;
                            new GETPOIs(progressDialog, mostrarMapa, mostrarMonumentos, true).execute();
                        }
                    }
                } else {
                    //No se tiene información previa, luego hay que obtener los POIs de 0
                    maxPOI = inputNMaxPOI;
                    radio = inputRadioBusqueda;
                    new GETPOIs(progressDialog, mostrarMapa, mostrarMonumentos, true).execute();
                }
                break;
        }
    }

    private int usuarioHaIntroducidoInformacion() {
        if (inputNMaxPOI != -1) {
            //El usuario ha introducido un valor para "nMaxPOI"
            if (inputRadioBusqueda != -1) {
                //El usuario ha introducido un valor para "Radio" y "nMaxPOI"
                return 3;
            } else {
                //Sólo se ha introducido un valor para "nMaxPOI"
                return 1;
            }
        } else {
            //El usuario NO ha introducido un valor para "nMaxPOI"
            if (inputRadioBusqueda != -1) {
                //Sólo se ha introducido un valor para "Radio"
                return 2;
            } else {
                //El usuario no ha introducido nada
                return 0;
            }
        }
    }

    //Metodo empleado para mostrar el Mapa en el fragmento
    private void mostrarMapFragment() {
        //Obtenemos una referencia al fragmento que está activo actualmente
        Fragment fragment = getFragmentManager().findFragmentById(R.id.content_frame);

        MapFragment mapFragment = new MapFragment();

        Bundle params = new Bundle();
        params.putString("Origen", "MainActivity");

        mapFragment.setArguments(params);

        if (!(fragment instanceof MapFragment)) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, mapFragment)
                    .addToBackStack("MapFragment")
                    .commit();

            if(getSupportActionBar() != null) {
                getSupportActionBar().setTitle(getResources().getString(R.string.menu_mapa));
            }
        }
    }

    //Metodo empleado para mostrar la lista de Monumentos en el fragmento
    private void mostrarPOIListFragment() {
        //Obtenemos una referencia al fragmento que está activo actualmente
        Fragment fragment = getFragmentManager().findFragmentById(R.id.content_frame);

        POIListFragment poiListFragment = new POIListFragment();

        if (!(fragment instanceof POIListFragment)) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, poiListFragment)
                    .addToBackStack("POIListFragment")
                    .commit();

            if(getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.menu_monumentos);
            }
        }
    }

    /*
     * Metodo que comprueba si se obtienen las coordenadas GPS
     * Si no las obtiene, se pondrán las de Valladolid por defecto
     *
     */
    private void comprobarObtencionCoordenadas() {
        //Primera comprobación de que se han obtenido las coordenadas del GPS
        if ((latitudGPS == 0.0) && (longitudGPS == 0.0)) {
            obtenerCoordenadasGPS();
        }

        if ((latitudGPS == 0.0) && (longitudGPS == 0.0)) {
            /* Si aún después de un segundo intento, se sigue sin haber podido obtener las coordenadas del GPS, utilizar
             * las coordenadas por defecto (latitudPorDefecto, longitudPorDefecto) antes de crear el fragmento
             */
            double latitudPorDefecto = 41.662826;
            double longitudPorDefecto = -4.705388;

            latitudGPS = latitudPorDefecto;
            longitudGPS = longitudPorDefecto;
        }
    }

    /**
     * Metodo que comprueba si el GPS y Internet están activados
     *
     * @return si alguno no esta activado devuelve un dialogo, si no, no devuelve nada
     */
    public boolean isGPSAndInternetEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        String tipoError = "";

        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        boolean showDialog = false;

        if (networkInfo != null && networkInfo.isConnected()) {
            if (!gpsEnabled) {
                showDialog = true;

                tipoError = "GPS";
            }
        } else {
            showDialog = true;

            tipoError = "INTERNET";
        }

        if (showDialog) {
            showErrorDialog(tipoError);
        }

        return !showDialog;
    }

    /**
     * @return NavigationView
     */
    public NavigationView getNavigationView() {
        return navigationView;
    }

    /**
     * @param inputRadioBusqueda Radio de búsqueda introducido por el usuario
     */
    public void setInputRadioBusqueda(int inputRadioBusqueda) { this.inputRadioBusqueda = inputRadioBusqueda; }

    /**
     * @param inputNMaxPOI Número máximo de POIs introducido por el usuario
     */
    public void setInputNMaxPOI(int inputNMaxPOI) {
        this.inputNMaxPOI = inputNMaxPOI;
    }

    /**
     * @param tipoError String que indica el tipo de error a mostrar en el AlertDialog
     */
    public void showErrorDialog(String tipoError) {

        Bundle params = new Bundle();

        params.putString("Error", tipoError);

        android.app.DialogFragment errorDialogFragment = new com.example.adrian.monumentos.Fragmentos.ErrorDialogFragment();
        errorDialogFragment.setArguments(params);

        errorDialogFragment.show(getFragmentManager(), "ErrorDialog");
    }

    /**
     * Método que se encarga de mostrar en el menú lateral el elemento del que se procede cuando al intentar
     * avanzar por la aplicación, ocurre un error y no se ha podido avanzar al siguiente punto.
     */
    public void marcarPrevItem() {

        String navigationTitle = "";

        if(getSupportActionBar() != null) {
            navigationTitle = (String) getSupportActionBar().getTitle();
        }

        //noinspection ConstantConditions
        if (navigationTitle.equals(getResources().getString(R.string.menu_inicio)))
            navigationView.getMenu().getItem(0).setChecked(true);
        else if (navigationTitle.equals(getResources().getString(R.string.menu_monumentos)))
            navigationView.getMenu().getItem(1).setChecked(true);
        else if (navigationTitle.equals(getResources().getString(R.string.menu_mapa)))
            navigationView.getMenu().getItem(2).setChecked(true);
        else
            navigationView.getMenu().getItem(3).setChecked(true);
    }

    /**
     * Called when the activity has detected the user's press of the back key.
     */
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(navigationView))
            drawerLayout.closeDrawers();
        else {
            super.onBackPressed();

            Fragment fragment = getFragmentManager().findFragmentById(R.id.content_frame);

            if(getSupportActionBar() != null) {
                if (fragment instanceof HomeFragment)
                    getSupportActionBar().setTitle(getResources().getString(R.string.menu_inicio));
                else if (fragment instanceof MapFragment)
                    getSupportActionBar().setTitle(getResources().getString(R.string.menu_mapa));
                else if (fragment instanceof POIListFragment)
                    getSupportActionBar().setTitle(R.string.menu_monumentos);
                else if (fragment instanceof AboutFragment)
                    getSupportActionBar().setTitle(getResources().getString(R.string.menu_sobre_app));
            }

            marcarPrevItem();
        }
    }
}
