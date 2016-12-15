package com.example.adrian.monumentos;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.util.ArrayList;

/**
 * Esta clase muestra un mapa con las posiciones de los monumentos y nuestra posicion
 * @author Adrian Munoz Rojo
 * @author Rafael Matamoros Luque
 * @author David Carrancio Aguado
 *
 */
public class MapFragment extends Fragment implements MapEventsReceiver, Marker.OnMarkerClickListener{

    private final static String POI_NOMBRE = "POI_NOMBRE";
    private Bundle params;
    private double latitudGPS, longitudGPS;

    private MapView mapView;

    private GlobalState globalState;

    /**
     * Constructor por defecto
     */

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
        globalState = (GlobalState) getActivity().getApplication();
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

        mapView = (MapView) vista.findViewById(R.id.map);

        //Almacenamos las coordenadas del GPS del usuario obteniendo los datos a través del Bundle desde MainActivity
        Bundle usuario = ((MainActivity) getActivity()).obtenerArgumentos();
        latitudGPS = usuario.getDouble("latitudGPS");
        longitudGPS = usuario.getDouble("longitudGPS");

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.setTilesScaledToDpi(true);

        if(params.getString("Origen").equals("POIListAdapter"))
            mostrarMapaPOI();
        else
            mostrarMapaUsuario();

        return vista;
    }

    /*
    * Método que se encarga de mostrar un mapa centrado en la ubicación del POI.
    * Además muestra la ubicación del usuario
    * */
    private void mostrarMapaPOI(){

        String nombrePoi = params.getString(POI_NOMBRE);
        ArrayList<POI> listaPOIs = globalState.getListaPOIs();
        POI poi = null;

        for(POI p : listaPOIs){
            if(nombrePoi.equals(p.getNombre())) {
                poi = p;
                break;
            }
        }

        double latitud = poi.getLatitud();
        double longitud = poi.getLongitud();
        String descripcion = poi.getDescripcion();

        GeoPoint point = new GeoPoint(latitud, longitud);

        IMapController iMapController = mapView.getController();
        iMapController.setZoom(14);
        iMapController.setCenter(point);

//añadimos marcadores
        Marker marker = new Marker(mapView);
        marker.setTitle(nombrePoi);
        marker.setSnippet(descripcion);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setRelatedObject(poi);
        marker.setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.marker_icon_poi));

        InfoBubble infoBubble =  new InfoBubble(mapView, this);
        marker.setInfoWindow(infoBubble);

        mapView.getOverlays().add(marker);

        //Ubicación del usuario
        GeoPoint miUbicacion = new GeoPoint(latitudGPS ,longitudGPS);

        Marker user = new Marker(mapView);
        user.setTitle("ESTA ES TU UBICACIÓN");
        user.setPosition(miUbicacion);
        user.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        user.setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.marker_user_icon));

        mapView.getOverlays().add(user);

        marker.showInfoWindow();

        mapView.invalidate();

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this);
        mapView.getOverlays().add(0, mapEventsOverlay);

        user.setOnMarkerClickListener(this);
    }

    /*
     * Método que se encarga de mostrar un mapa centrado en la posición del usuario, y las ubicaciones
     * de los POI cercanos
     * */
    private void mostrarMapaUsuario() {

        ArrayList<POI> listaPOIs = globalState.getListaPOIs();

        InfoBubble infoBubble =  new InfoBubble(mapView, this);

        //Añadir marcadores al mapa con la información de cada POI
        for (POI poi : listaPOIs) {
            GeoPoint geoPoint = new GeoPoint(poi.getLatitud(), poi.getLongitud());

            Marker marker = new Marker(mapView);
            marker.setTitle(poi.getNombre());
            marker.setSnippet(poi.getDescripcion());
            marker.setPosition(geoPoint);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setRelatedObject(poi);
            marker.setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.marker_icon_poi));

            marker.setInfoWindow(infoBubble);

            mapView.getOverlays().add(marker);
        }

        //Ubicación del usuario
        GeoPoint miUbicacion = new GeoPoint(latitudGPS ,longitudGPS);

        Marker user = new Marker(mapView);
        user.setTitle("ESTA ES TU UBICACIÓN");
        user.setPosition(miUbicacion);
        user.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        user.setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.marker_user_icon));

        mapView.getOverlays().add(user);

        IMapController iMapController = mapView.getController();
        iMapController.setZoom(14);
        iMapController.setCenter(miUbicacion);

        user.showInfoWindow();

        mapView.invalidate();

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this);
        mapView.getOverlays().add(0, mapEventsOverlay);

        user.setOnMarkerClickListener(this);
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
    public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
        InfoWindow.closeAllInfoWindowsOn(mapView);
        return true;
    }

    @Override
    public boolean longPressHelper(GeoPoint geoPoint) {
        //No hacer nada. No capturamos este tipo de eventos
        return false;
    }

    @Override
    public boolean onMarkerClick(Marker marker, MapView mapView) {
        InfoWindow.closeAllInfoWindowsOn(mapView);

        //Mostrar infoWindow del Marker y centrar en su posición
        marker.showInfoWindow();
        mapView.getController().animateTo(marker.getPosition());

        return true;
    }
}
