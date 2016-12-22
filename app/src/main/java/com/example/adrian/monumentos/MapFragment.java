package com.example.adrian.monumentos;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.util.ArrayList;
import java.util.List;

/**
 * Esta clase muestra un mapa con las posiciones de los monumentos y nuestra posicion
 *
 * @author Adrian Munoz Rojo
 * @author Rafael Matamoros Luque
 * @author David Carrancio Aguado
 */
public class MapFragment extends Fragment implements MapEventsReceiver, Marker.OnMarkerClickListener {

    /**
     *
     */
    private final static String POI_NOMBRE = "POI_NOMBRE";

    /**
     *
     */
    private Bundle params;

    /**
     *Coordenadas GPS
     */
    private double latitudGPS, longitudGPS;

    /**
     *
     */
    private MapView mapView;

    /**
     *
     */
    private GlobalState globalState;

    /**
     *
     */
    private int orientacionPantalla;

    /**
     *
     */
    private IGeoPoint centroPantalla;

    /**
     *
     */
    private final ArrayList<Marker> marcadores = new ArrayList<>();

    /**
     *
     */
    private final ArrayList<Marker> nodeMarkers = new ArrayList<>();

    /**
     *
     */
    private int indiceRestaurar = -1;

    /**
     * Constructor por defecto
     */
    public MapFragment() {
    }

    /**
     * Metodo OnCreate
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        params = getArguments();
        globalState = (GlobalState) getActivity().getApplication();
    }

    /**
     * Metodo para crear la vista
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return vista
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.map_fragment, container, false);

        mapView = (MapView) vista.findViewById(R.id.map);

        //Obtenemos la orientación actual de la pantalla
        orientacionPantalla = getActivity().getResources().getConfiguration().orientation;

        //Almacenamos las coordenadas del GPS del usuario
        Bundle usuario = ((MainActivity) getActivity()).obtenerArgumentos();
        latitudGPS = usuario.getDouble("latitudGPS");
        longitudGPS = usuario.getDouble("longitudGPS");

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.setTilesScaledToDpi(true);

        //Dependiendo desde donde reciba
        //noinspection ConstantConditions
        if ((params.getString("Origen").equals("POIListAdapter")) || (params.getString("Origen").equals("MapFragment")))
            mostrarMapaPOI();
        else
            mostrarMapaUsuario();

        return vista;
    }

    /**
     * Método que se encarga de mostrar un mapa centrado en la ubicación del POI.
     * Además muestra la ubicación del usuario
     */
    private void mostrarMapaPOI() {

        String nombrePoi = params.getString(POI_NOMBRE);
        ArrayList<POI> listaPOIs = globalState.getListaPOIs();
        POI poi = null;

        for (POI p : listaPOIs) {
            assert nombrePoi != null;
            if (nombrePoi.equals(p.getNombre())) {
                poi = p;
                break;
            }
        }

        assert poi != null;
        double latitud = poi.getLatitud();
        double longitud = poi.getLongitud();
        String descripcion = poi.getDescripcion();

        GeoPoint point = new GeoPoint(latitud, longitud);

        IMapController iMapController = mapView.getController();
        iMapController.setZoom(14);

        centroPantalla = point;
        if (orientacionPantalla == 2)
            centroPantalla = new GeoPoint(centroPantalla.getLatitude() + 0.0015, centroPantalla.getLongitude());

        iMapController.setCenter(centroPantalla);

        Marker marker = new Marker(mapView);
        marker.setTitle(nombrePoi);
        marker.setSnippet(descripcion);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setRelatedObject(poi);
        marker.setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.marker_icon_poi));

        InfoBubble infoBubble = new InfoBubble(mapView, this, false);
        marker.setInfoWindow(infoBubble);

        //Ubicación del usuario
        GeoPoint miUbicacion = new GeoPoint(latitudGPS, longitudGPS);

        Marker user = new Marker(mapView);
        user.setTitle(getResources().getString(R.string.ubication));
        user.setPosition(miUbicacion);
        user.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        user.setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.marker_user_icon));

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this);
        mapView.getOverlays().add(0, mapEventsOverlay);

        marker.setOnMarkerClickListener(this);
        user.setOnMarkerClickListener(this);

        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getResources().getString(R.string.datos));

        progressDialog.setCanceledOnTouchOutside(false);

        marker.showInfoWindow();

        marcadores.add(marker);
        marcadores.add(user);

        new crearRuta(progressDialog, miUbicacion, point, mapView.getOverlays(), user, marker).execute();
    }

    /*
     * Método que se encarga de mostrar un mapa centrado en la posición del usuario, y las ubicaciones
     * de los POI cercanos
     */


    private void mostrarMapaUsuario() {

        ArrayList<POI> listaPOIs = globalState.getListaPOIs();

        InfoBubble infoBubble = new InfoBubble(mapView, this, true);

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
            marcadores.add(marker);
        }

        //Ubicación del usuario
        GeoPoint miUbicacion = new GeoPoint(latitudGPS, longitudGPS);

        Marker user = new Marker(mapView);
        user.setTitle(getResources().getString(R.string.ubication));
        user.setPosition(miUbicacion);
        user.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        user.setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.marker_user_icon));

        mapView.getOverlays().add(user);

        IMapController iMapController = mapView.getController();
        iMapController.setZoom(14);

        if (indiceRestaurar == -1) {
            centroPantalla = miUbicacion;
            if (orientacionPantalla == 2)
                centroPantalla = new GeoPoint(centroPantalla.getLatitude() + 0.0015, centroPantalla.getLongitude());

            iMapController.setCenter(centroPantalla);

            user.showInfoWindow();

            mapView.invalidate();
        } else {
            //El fragmento está siendo restaurado de un estado anterior
            Marker marcador = marcadores.get(indiceRestaurar);

            POI restoredPOI = null;

            for (POI poiToRestore : listaPOIs) {
                if (poiToRestore.getNombre().equals(marcador.getTitle())) {
                    restoredPOI = poiToRestore;
                }
            }

            assert restoredPOI != null;
            centroPantalla = new GeoPoint(restoredPOI.getLatitud(), restoredPOI.getLongitud());
            if (orientacionPantalla == 2)
                centroPantalla = new GeoPoint(centroPantalla.getLatitude() + 0.0015, centroPantalla.getLongitude());

            iMapController.setCenter(centroPantalla);

            Marker restoredMarker = new Marker(mapView);
            restoredMarker.setTitle(restoredPOI.getNombre());
            restoredMarker.setSnippet(restoredPOI.getDescripcion());
            restoredMarker.setPosition(new GeoPoint(restoredPOI.getLatitud(), restoredPOI.getLongitud()));
            restoredMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            restoredMarker.setRelatedObject(restoredPOI);
            restoredMarker.setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.marker_icon_poi));

            InfoBubble infoBubbleToRestore = new InfoBubble(mapView, this, true);
            restoredMarker.setInfoWindow(infoBubbleToRestore);

            mapView.getOverlays().remove(marcador);
            mapView.getOverlays().add(restoredMarker);

            marcadores.remove(indiceRestaurar);
            marcadores.add(restoredMarker);

            restoredMarker.showInfoWindow();

            mapView.invalidate();
        }

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this);
        mapView.getOverlays().add(0, mapEventsOverlay);

        for (Marker markerPOI : marcadores) {
            markerPOI.setOnMarkerClickListener(this);
        }

        user.setOnMarkerClickListener(this);

        marcadores.add(user);
    }

    /**
     *
     */
    @Override
    public void onPause() {
        super.onPause();

        boolean valorAsignado = false;

        for (int i = 0; i < marcadores.size(); i++) {
            if (marcadores.get(i).isInfoWindowShown()) {
                indiceRestaurar = i;
                valorAsignado = true;
                break;
            }
        }

        if (!valorAsignado)
            //Ninguna burbuja estaba abierta en el momento de ejecutar onPause. No es necesario restaurar el fragmento
            indiceRestaurar = -1;
    }

    /**
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        boolean isAnyInfoWindowOpen = false;

        int nuevaOrientacion = newConfig.orientation;

        IGeoPoint antiguoCentroPantalla = centroPantalla;

        //Comprobamos qué Marker es el que está en foco
        for (Marker marker : marcadores) {
            if (marker.isInfoWindowShown()) {
                centroPantalla = marker.getPosition();
                isAnyInfoWindowOpen = true;
                break;
            }
        }

        //Si el Marker que estaba abierto era un nodo de la ruta, repetir el proceso para los nodeMarker
        if (antiguoCentroPantalla.equals(centroPantalla))
            for (Marker nodeMarker : nodeMarkers) {
                if (nodeMarker.isInfoWindowShown()) {
                    centroPantalla = nodeMarker.getPosition();
                    isAnyInfoWindowOpen = true;
                    break;
                }
            }

        @SuppressWarnings("ConstantConditions") ViewTreeObserver viewTreeObserver = getView().getViewTreeObserver();

        if (isAnyInfoWindowOpen) {
            //Comprobamos si ha cambiado la orientación de la pantalla
            if (nuevaOrientacion == 2) {
                //Orientation: Landscape
                //Centrar el mapa en el Marker en cuestión
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        getView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        centroPantalla = new GeoPoint(centroPantalla.getLatitude() + 0.0015, centroPantalla.getLongitude());
                        mapView.getController().setCenter(centroPantalla);
                    }
                });
            } else {
                //Orientation: Portrait
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        getView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mapView.getController().setCenter(centroPantalla);
                    }
                });
            }
        } else {
            //Dejar el mapa en la posición de la cámara en la que estaba
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    getView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    centroPantalla = mapView.getMapCenter();
                    mapView.getController().setCenter(centroPantalla);
                }
            });
        }
        orientacionPantalla = nuevaOrientacion;
    }

    /**
     * @param geoPoint
     * @return
     */
    @Override
    public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
        InfoWindow.closeAllInfoWindowsOn(mapView);
        return true;
    }

    /**
     * @param geoPoint
     * @return
     */
    @Override
    public boolean longPressHelper(GeoPoint geoPoint) {
        //No hacer nada. No capturamos este tipo de eventos
        return false;
    }

    /**
     * @param marker
     * @param mapView
     * @return
     */
    @Override
    public boolean onMarkerClick(Marker marker, MapView mapView) {
        InfoWindow.closeAllInfoWindowsOn(mapView);

        centroPantalla = marker.getPosition();

        if (orientacionPantalla == 2) {
            //Orientation: Landscape
            centroPantalla = new GeoPoint(centroPantalla.getLatitude() + 0.0015, centroPantalla.getLongitude());
        }

        //Mostrar infoWindow del Marker y centrar en su posición
        marker.showInfoWindow();
        mapView.getController().animateTo(centroPantalla);

        return true;
    }

    /**
     *
     */
    private class crearRuta extends AsyncTask<Void, Void, Void> {

        private final GeoPoint inicio;
        private final GeoPoint fin;
        private final List<Overlay> overlays;
        private final ProgressDialog progressDialog;
        private final Marker user;
        private final Marker poi;

        crearRuta(ProgressDialog progressDialog, GeoPoint inicio, GeoPoint fin, List<Overlay> overlays, Marker user, Marker poi) {
            this.progressDialog = progressDialog;
            this.inicio = inicio;
            this.fin = fin;
            this.overlays = overlays;
            this.user = user;
            this.poi = poi;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            RoadManager roadManager = new OSRMRoadManager(getActivity());

            ArrayList<GeoPoint> geoPoints = new ArrayList<>();
            geoPoints.add(inicio);
            geoPoints.add(fin);

            Road road = roadManager.getRoad(geoPoints);

            Polyline polyline = RoadManager.buildRoadOverlay(road);

            polyline.setWidth(10);

            overlays.add(polyline);

            Drawable nodeIcon = ContextCompat.getDrawable(getActivity(), R.drawable.marker_node);

            String subdescripcion;

            for (int i = 1; i < road.mNodes.size() - 1; i++) {
                RoadNode roadNode = road.mNodes.get(i);
                Marker nodeMarker = new Marker(mapView);
                nodeMarker.setPosition(roadNode.mLocation);
                nodeMarker.setIcon(nodeIcon);
                nodeMarker.setTitle("Paso " + i);

                cambiarInstrucciones(roadNode);
                actualizarDatos(road, i);

                nodeMarker.setSnippet(roadNode.mInstructions);

                subdescripcion = Road.getLengthDurationText(getActivity(), roadNode.mLength, roadNode.mDuration);

                nodeMarker.setSubDescription(introducirEspacios(subdescripcion) + " restante hasta tu destino");

                nodeMarker.setImage(seleccionarIcono(roadNode.mManeuverType));

                overlays.add(nodeMarker);
                nodeMarkers.add(nodeMarker);

                nodeMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker, MapView mapView) {
                        InfoWindow.closeAllInfoWindowsOn(mapView);

                        centroPantalla = marker.getPosition();

                        if (orientacionPantalla == 2) {
                            //Orientation: Landscape
                            centroPantalla = new GeoPoint(centroPantalla.getLatitude() + 0.0015, centroPantalla.getLongitude());
                        }

                        //Mostrar infoWindow del Marker y centrar en su posición
                        marker.showInfoWindow();
                        mapView.getController().animateTo(centroPantalla);

                        return true;
                    }
                });
            }

            //Nodo inicial
            RoadNode nodoUsuario = road.mNodes.get(0);
            actualizarDatos(road, 0);

            subdescripcion = Road.getLengthDurationText(getActivity(), nodoUsuario.mLength, nodoUsuario.mDuration);

            user.setSubDescription(introducirEspacios(subdescripcion) + " restante hasta tu destino");

            //Nodo final
            RoadNode nodoPoi = road.mNodes.get(road.mNodes.size() - 1);
            actualizarDatos(road, road.mNodes.size() - 1);

            subdescripcion = Road.getLengthDurationText(getActivity(), nodoPoi.mLength, nodoPoi.mDuration);

            poi.setSubDescription(introducirEspacios(subdescripcion) + " restante hasta tu destino");

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            mapView.getOverlays().add(user);
            mapView.getOverlays().add(poi);

            mapView.invalidate();

            progressDialog.dismiss();
        }
    }

    /**
     * @param roadNode
     */
    private void cambiarInstrucciones(RoadNode roadNode) {

        String calle = "";
        boolean añadirCalle = false;

        try {
            if (roadNode.mInstructions.contains(" on ")) {
                calle = roadNode.mInstructions.split(" on ")[1];
                añadirCalle = true;
            }
        } catch (NullPointerException e) {
            //NO hacer nada
        }

        switch (roadNode.mManeuverType) {
            case 0:
                roadNode.mInstructions = "Continúa";
                break;
            case 1:
                roadNode.mInstructions = "Continúa recto";
                break;
            case 2:
                roadNode.mInstructions = "Continúa";
                break;
            case 3:
                roadNode.mInstructions = "Desvíate ligeramente hacia la izquierda";
                break;
            case 4:
                roadNode.mInstructions = "Gira a la izquierda";
                break;
            case 5:
                roadNode.mInstructions = "Desvíate hacia la izquierda";
                break;
            case 6:
                roadNode.mInstructions = "Desvíate ligeramente hacia la derecha";
                break;
            case 7:
                roadNode.mInstructions = "Gira a la derecha";
                break;
            case 8:
                roadNode.mInstructions = "Desvíate hacia la derecha";
                break;
            case 9:
                roadNode.mInstructions = "Mantente a la izquierda";
                break;
            case 10:
                roadNode.mInstructions = "Mantente a la derecha";
                break;
            case 11:
                roadNode.mInstructions = "Continúa recto";
                break;
            case 12:
                roadNode.mInstructions = "Haz un cambio de sentido";
                break;
            case 13:
                roadNode.mInstructions = "Haz un cambio de sentido a la izquierda";
                break;
            case 14:
                roadNode.mInstructions = "Haz un cambio de sentido a la derecha";
                break;
            case 15:
                roadNode.mInstructions = "Toma la salida de la izquierda";
                break;
            case 16:
                roadNode.mInstructions = "Toma la salida de la derecha";
                break;
            case 17:
                roadNode.mInstructions = "Toma el ramal de la izquierda";
                break;
            case 18:
                roadNode.mInstructions = "Toma el ramal de la derecha";
                break;
            case 19:
                roadNode.mInstructions = "Toma el ramal de frente";
                break;
            case 20:
                roadNode.mInstructions = "Incorpórate a la izquierda";
                break;
            case 21:
                roadNode.mInstructions = "Incorpórate a la derecha";
                break;
            case 22:
                roadNode.mInstructions = "Incorpórate de frente";
                break;
            case 23:
                roadNode.mInstructions = "Entrando";
                break;
            case 24:
                roadNode.mInstructions = "Has llegado a tu destino";
                break;
            case 25:
                roadNode.mInstructions = "Tu destino está a la izquierda";
                break;
            case 26:
                roadNode.mInstructions = "Tu destino está a la derecha";
                break;
            case 27:
                roadNode.mInstructions = "Entra en la rotonda y toma la primera salida";
                break;
            case 28:
                roadNode.mInstructions = "Entra en la rotonda y toma la segunda salida";
                break;
            case 29:
                roadNode.mInstructions = "Entra en la rotonda y toma la tercera salida";
                break;
            case 30:
                roadNode.mInstructions = "Entra en la rotonda y toma la cuarta salida";
                break;
            case 31:
                roadNode.mInstructions = "Entra en la rotonda y toma la quinta salida";
                break;
            case 32:
                roadNode.mInstructions = "Entra en la rotonda y toma la sexta salida";
                break;
            case 33:
                roadNode.mInstructions = "Entra en la rotonda y toma la séptima salida";
                break;
            case 34:
                roadNode.mInstructions = "Entra en la rotonda y toma la octava salida";
                break;
            case 35:
                roadNode.mInstructions = "Coge el transporte público";
                break;
            case 36:
                roadNode.mInstructions = "Haz transbordo";
                break;
            case 37:
                roadNode.mInstructions = "Entra en la estación de autobuses o metro";
                break;
            case 38:
                roadNode.mInstructions = "Sal de la estación de autobuses o metro";
                break;
            case 39:
                roadNode.mInstructions = "Mantente en tu actual vehículo";
                break;
        }

        if (añadirCalle) {
            roadNode.mInstructions += " por " + calle;
        }
    }

    /**
     * @param road
     * @param indice
     */
    private void actualizarDatos(Road road, int indice) {
        ArrayList<RoadNode> roadNodes = road.mNodes;
        RoadNode roadNode = roadNodes.get(indice);

        for (int i = indice + 1; i < roadNodes.size(); i++) {
            roadNode.mLength += roadNodes.get(i).mLength;
            roadNode.mDuration += roadNodes.get(i).mDuration;
        }
    }

    /**
     * @param subdescripcion
     * @return
     */
    private String introducirEspacios(String subdescripcion) {
        //Medidas de distancia:
        if (subdescripcion.contains("km"))
            subdescripcion = subdescripcion.replace("km", " km");
        else
            subdescripcion = subdescripcion.replace("m", " m");

        //Medidas de tiempo:
        if (subdescripcion.contains("sec"))
            subdescripcion = subdescripcion.replace("sec", " seg");
        else if (subdescripcion.contains("min"))
            subdescripcion = subdescripcion.replace("min", " min");
        else
            subdescripcion = subdescripcion.replace("h", " h");


        return subdescripcion;
    }

    /**
     * @param maneuverType
     * @return
     */
    private Drawable seleccionarIcono(int maneuverType) {

        Drawable icono = ContextCompat.getDrawable(getActivity(), R.drawable.ic_continue);

        switch (maneuverType) {
            case 3:
                icono = ContextCompat.getDrawable(getActivity(), R.drawable.ic_slight_left);
                break;
            case 4:
                icono = ContextCompat.getDrawable(getActivity(), R.drawable.ic_turn_left);
                break;
            case 5:
                icono = ContextCompat.getDrawable(getActivity(), R.drawable.ic_sharp_left);
                break;
            case 6:
                icono = ContextCompat.getDrawable(getActivity(), R.drawable.ic_slight_right);
                break;
            case 7:
                icono = ContextCompat.getDrawable(getActivity(), R.drawable.ic_turn_right);
                break;
            case 8:
                icono = ContextCompat.getDrawable(getActivity(), R.drawable.ic_sharp_right);
                break;
            case 12:
                icono = ContextCompat.getDrawable(getActivity(), R.drawable.ic_u_turn);
                break;
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
            case 33:
            case 34:
                icono = ContextCompat.getDrawable(getActivity(), R.drawable.ic_roundabout);
                break;
        }
        return icono;
    }
}
