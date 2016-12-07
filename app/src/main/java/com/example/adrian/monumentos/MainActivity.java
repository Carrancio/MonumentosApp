package com.example.adrian.monumentos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
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
 * Esta es la clase principal
 * @author Adrian Munoz Rojo
 * @author Rafael Matamoros Luque
 * @author David Carrancio Aguado
 */
public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private boolean hasPermission;
    private boolean permissionRequestDone = false;

    DrawerLayout drawerLayout;
    NavigationView navigationView;

    private String HTTPS = "https://";
    private String idioma;
    private String WIKI_URL = ".wikipedia.org/w/api.php?action=query&format=json";
    private String TAG = MainActivity.class.getSimpleName();

    private double latitudGPS;
    private double longitudGPS;
    private String jSonCoords;

    private Location location;

    //El radio se debe recoger del usuario
    private int radio = 1000;

    //El número máximo de POI se debe recoger del usuario
    private int maxPOI = 500;


    private GoogleApiClient mGoogleApiClient;

    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    private static final String STATE_RESOLVING_ERROR = "resolving_error";
    private static final int REQUEST_LOCATION = 2;

    private LocationRequest locationRequest;
    private static final int LOCATION_INTERVAL = 1000;


    /**
     * Crea la vista
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hasPermission = hasPermisosUbicacion();

        //Solicitar permisos de ubicación en caso de no tenerlos
        if(!hasPermission){
            if(permissionRequestDone)
                requestPermission();

            //Comprobación de que ya hemos obtenido los permisos antes de continuar
            hasPermission = hasPermisosUbicacion();
        }

        idioma = Locale.getDefault().getLanguage();

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
        //Fragmento de informacion
        final Fragment1 fragment = new Fragment1();
        getFragmentManager().beginTransaction().add(R.id.content_frame, homeFragment).commit();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navview);

        //Hay que asegurarse de que se han obtenido correctamente las coordenadas del GPS
        obtenerCoordenadasGPS();


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
               // boolean fragmentTransaction = false;
               // Fragment fragment = null;
                // Intent IrMapa = new Intent(MainActivity.this,Maps.class);
                Intent i = new Intent(MainActivity.this, Maps.class);

            //menu
                switch (item.getItemId()) {
                    case R.id.menu_inicio:

                        getFragmentManager().beginTransaction().replace(R.id.content_frame, homeFragment).commit();


                        break;

                    case R.id.menu_ayuda:


                      //  fragmentTransaction = true;
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.content_frame, fragment)
                                .commit();

                        break;

                    case R.id.menu_monumentos:

                        if((latitudGPS == 0.0) && (longitudGPS == 0.0)){
                            obtenerCoordenadasGPS();
                            Toast.makeText(MainActivity.this, "No se han encontrado datos de puntos de interés cercanos a su posición", Toast.LENGTH_SHORT).show();
                        }
                        else
                            Toast.makeText(MainActivity.this, "Obteniendo puntos de interés...", Toast.LENGTH_SHORT).show();

                        POIListFragment poiListFragment = new POIListFragment();
                        getFragmentManager()
                                .beginTransaction()
                                .replace(R.id.content_frame, poiListFragment)
                                .addToBackStack(null)
                                .commit();

                        break;

                    case R.id.menu_mapa:

                        if((latitudGPS == 0.0) && (longitudGPS == 0.0)){
                            obtenerCoordenadasGPS();
                            Toast.makeText(MainActivity.this, "No se han encontrado datos de puntos de interés cercanos a su posición", Toast.LENGTH_SHORT).show();
                        }
                        else
                            Toast.makeText(MainActivity.this, "Obteniendo puntos de interés...", Toast.LENGTH_SHORT).show();

                        //pasando parametros a otra actividad
                        i.putExtra("jSonCoords",jSonCoords);
                        i.putExtra("latitudGPS",latitudGPS);
                        i.putExtra("longitudGPS", longitudGPS);

                        startActivity(i);
                        break;


                }
                //se sustituye el contenido
              /*  if (fragmentTransaction) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_frame, fragment)
                            .addToBackStack(null)
                            .commit();

                    item.setChecked(true);
                    getSupportActionBar().setTitle(item.getTitle());
                }*/

                drawerLayout.closeDrawers();

                return true;
            }
        });

        Toolbar appbar = (Toolbar)findViewById(R.id.appbar);
        setSupportActionBar(appbar);

        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     *
     */
    private class GETPOIs extends AsyncTask<Void, Void, Void> {

        /**
         *
         * @param params
         * @return
         */
        @Override
        protected Void doInBackground(Void... params) {

            ArrayList<POI> listaPOIs = new ArrayList<>();
            ArrayList<ArrayList<String>> listaPoi = new ArrayList<>();
            String pageIds = "";

            HttpHandler handler = new HttpHandler();

            //Petición a la API de Wikipedia y almacenamiento de la respuesta
            String urlCoords = HTTPS + idioma + WIKI_URL + "&list=geosearch&gscoord=" +
                    latitudGPS + "%7C" + longitudGPS + "&gsradius=" + radio + "&gslimit=" + maxPOI;

            jSonCoords = handler.makeServiceCall(urlCoords);
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

                for(int j = 0; j < geosearch.length(); j++){
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
                    } catch (JSONException e){
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

            GlobalState globalState = (GlobalState) MainActivity.this.getApplicationContext();
            globalState.setListaPOIs(listaPOIs);

            return null;
        }
    }

    @Override
    protected void onStop() {
        // solo se para si se conecta
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }
        else {
            startLocationUpdates();

            location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if(location != null) {
                latitudGPS = location.getLatitude();
                longitudGPS = location.getLongitude();

                new GETPOIs().execute();
            }
        }
    }

    private void startLocationUpdates(){

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }
        else {
            // Crea la localicacion
            locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(LOCATION_INTERVAL)
                    .setFastestInterval(LOCATION_INTERVAL);

            // Request location updates
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    locationRequest, this);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if(grantResults.length == 1
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

    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(this, "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }
    }

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

    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        MainActivity.ErrorDialogFragment dialogFragment = new MainActivity.ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

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

    /*Comprobación permisos ubicación (GPS) concedidos*/
    private boolean hasPermisosUbicacion(){

        return ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

    }

    /*Solicitar permisos de ubicación al usuario*/
    private void requestPermission(){

        // Check Permissions Now
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION);
    }

    /*Método encargado de comprobar si se han obtenido ya las coordenadas GPS y, en caso contrario, obtenerlas*/
    public void obtenerCoordenadasGPS(){

        Toast.makeText(this, "Obteniendo coordenadas GPS...", Toast.LENGTH_SHORT).show();
        mGoogleApiClient.reconnect();

    }


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }


    @Override
    public void onLocationChanged(Location location) {

        latitudGPS = location.getLatitude();
        longitudGPS = location.getLongitude();

        if((latitudGPS != 0.0) && (longitudGPS != 0.0)){
            new GETPOIs().execute();

            // Disconnecting the client invalidates it.
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }


    public double getLatitudGPS() { return latitudGPS; }

    public double getLongitudGPS() { return longitudGPS; }




    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:

                drawerLayout.openDrawer(GravityCompat.START);
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_lateral, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        return super.onPrepareOptionsMenu(menu);
    }

}
