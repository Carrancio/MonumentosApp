package com.example.adrian.monumentos;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Esta clase muestra un mapa con las posiciones de los monumentos y nuestra posicion
 * @author Adrian Munoz Rojo
 * @author Rafael Matamoros Luque
 * @author David Carrancio Aguado
 *
 */
public class MapFragment extends Fragment implements OnMapReadyCallback{

    private Bundle params;

    private GoogleMap map;
    private MapView mapView;

    //Constructor por defecto
    public MapFragment(){}

    /**
     * Called to do initial creation of a fragment.  This is called after
     * {@link #onAttach(Context)} and before
     * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}, but is not called if the fragment
     * instance is retained across Activity re-creation (see {@link #setRetainInstance(boolean)}).
     * <p>
     * <p>Note that this can be called while the fragment's activity is
     * still in the process of being created.  As such, you can not rely
     * on things like the activity's content view hierarchy being initialized
     * at this point.  If you want to do work once the activity itself is
     * created, see {@link #onActivityCreated(Bundle)}.
     * <p>
     * <p>If your app's <code>targetSdkVersion</code> is 23 or lower, child fragments
     * being restored from the savedInstanceState are restored after <code>onCreate</code>
     * returns. When targeting N or above and running on an N or newer platform version
     * they are restored by <code>Fragment.onCreate</code>.</p>
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        params = getArguments();
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * This is optional, and non-graphical fragments can return null (which
     * is the default implementation).  This will be called between
     * {@link #onCreate(Bundle)} and {@link #onActivityCreated(Bundle)}.
     * <p>
     * <p>If you return a View from here, you will later be called in
     * {@link #onDestroyView} when the view is being released.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.map_fragment, container, false);

        MapsInitializer.initialize(getActivity());
        mapView = (MapView) vista.findViewById(R.id.map);

        mapView.onCreate(params);

        setUpMapIfNeeded();

        return vista;
    }

    private void setUpMapIfNeeded() {
        if (map == null) {
            mapView.getMapAsync(this);
            if (map != null) {
                if(params.size() == 5){
                    //Los únicos parámetros recibidos se refieren a un POI en concreto
                    setUpMapPOI();
                } else{
                    //Se tiene que mostrar el mapa centrado en la ubicación del usuario junto con los POI cercanos
                    setUpMapUser();
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        if(params.size() == 5){
            //Los únicos parámetros recibidos se refieren a un POI en concreto
            setUpMapPOI();
        } else{
            //Se tiene que mostrar el mapa centrado en la ubicación del usuario junto con los POI cercanos
            setUpMapUser();
        }
    }

    /*
    * Método que se encarga de mostrar un mapa centrado en la ubicación del POI.
    * Además muestra la ubicación del usuario
    * */
    private void setUpMapPOI(){

        String nombre = params.getString("POI_NOMBRE");
        double latitud = params.getDouble("POI_LATITUD");
        double longitud = params.getDouble("POI_LONGITUD");
        String enlace = params.getString("POI_URL");

        Bundle usuario = ((MainActivity) getActivity()).obtenerArgumentos();
        double latitudUsuario = usuario.getDouble("latitudGPS");
        double longitudUsuario = usuario.getDouble("longitudGPS");

        LatLng poi = new LatLng(latitud, longitud);
        map.addMarker(new MarkerOptions().position(poi).title(nombre));
        map.moveCamera(CameraUpdateFactory.newLatLng(poi));

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitud, longitud),13));

        //Ubicación del usuario
        LatLng miUbicacion = new LatLng(latitudUsuario ,longitudUsuario);

        // Centramos el mapa a la posicion del usuario
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(poi)
                .zoom(17)
                .build();

        //Añadimos marcador a nuestra posicion
        map.addMarker(new MarkerOptions().position(miUbicacion).title("AQUI ESTAS TU").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    /*
     * Método que se encarga de mostrar un mapa centrado en la posición del usuario, y las ubicaciones
     * de los POI cercanos
     * */
    private void setUpMapUser() {

        //Metemos en un string el json que recibe del MainActivity
        String jSonCoords = params.getString("jSonCoords");

        //Metemos en un double las coordenadas del movil
        double e1 = params.getDouble("latitudGPS");
        double e2 = params.getDouble("longitudGPS");

        if (jSonCoords != null) {

            try {
                JSONObject jsonObj = new JSONObject(jSonCoords);

                JSONArray geosearch = jsonObj.getJSONArray("geosearch");

                //añadir marcadores con las coordenadas de cada monumento y su nombre

                for (int i = 0; i < geosearch.length(); i++) {
                    JSONObject c = geosearch.getJSONObject(i);
                    String lat = c.getString("lat");
                    String lon = c.getString("lon");
                    String title = c.getString("title");
                    double lat1 = Double.parseDouble(lat);
                    double lon1 = Double.parseDouble(lon);

                    LatLng aAñadir = new LatLng(lat1, lon1);
                    map.addMarker(new MarkerOptions().position(aAñadir).title(title));
                    map.moveCamera(CameraUpdateFactory.newLatLng(aAñadir));
                }
            } catch (final JSONException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity().getApplicationContext(),
                                "Json parsing error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });

            }

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(e1,e2),13));

            // Centramos el mapa a la posicion del usuario
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(e1,e2))
                    .zoom(17)
                    .build();
            //Añadimos marcador a nuestra posicion
            LatLng miUbicacion = new LatLng(e1,e2);
            map.addMarker(new MarkerOptions().position(miUbicacion).title("AQUI ESTAS TU").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    /**
     * Called when a fragment is first attached to its context.
     * {@link #onCreate(Bundle)} will be called after this.
     *
     * @param context Context
     */
    @Override
    public void onAttach(Context context) { super.onAttach(context); }

    /**
     * Called when the fragment is no longer attached to its activity.  This is called after
     * {@link #onDestroy()}, except in the cases where the fragment instance is retained across
     * Activity re-creation (see {@link #setRetainInstance(boolean)}), in which case it is called
     * after {@link #onStop()}.
     */
    @Override
    public void onDetach() { super.onDetach(); }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
