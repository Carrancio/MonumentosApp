package com.example.adrian.monumentos.Fragmentos;

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

import com.example.adrian.monumentos.GlobalState;
import com.example.adrian.monumentos.InfoBubble;
import com.example.adrian.monumentos.MainActivity;
import com.example.adrian.monumentos.POI;

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
 * Esta clase se encarga de, en función de dónde venga la creación de una instancia de la misma, se mostrará,
 * o bien un mapa centrado en la posición del usuario con una serie de POIs alrededor (mostrarMapaUsuario)
 * (en función de los parámetros introducidos por el usuario en HomeFragment), o bien, un mapa centrado en un POI concreto
 * mostrando una ruta desde la ubicación del usuario hasta ese POI, indicando, paso por paso, por dónde debe ir el usuario,
 * además de la distancia y el tiempo estimado de llegada (en coche) al mismo (mostrarMapaPOI).
 *
 * <p>Esta clase forma parte de la aplicación TripApp, desarrollada para la asignatura Sistemas Móviles.</p>
 *
 * @author Adrián Muñoz Rojo
 * @author Rafael Matamoros Luque
 * @author David Carrancio Aguado
 * @see GlobalState
 * @see InfoBubble
 * @see MainActivity
 * @see POI
 * @version 1.0
 */
public class MapFragment extends Fragment implements MapEventsReceiver, Marker.OnMarkerClickListener {

    //Etiqueta utilizada para obtener del Bundle el nombre del POI en cuestión en mostrarMapaPOI
    private final static String POI_NOMBRE = "POI_NOMBRE";

    //Bundle en el que se almacena el origen de la llamada a MapFragment y, en su caso, el nombre del POI en cuestión (mostrarMapaPOI)
    private Bundle params;

    //Variables donde se almacenan las coordenadas del GPS del usuario obtenidas en MainActivity
    private double latitudGPS, longitudGPS;

    //MapView en el que se mostrará toda la información
    private MapView mapView;

    //Variable de la que obtendremos los datos en relación a los POIs tras las consultas a la API de WikiPedia en MainActivity
    private GlobalState globalState;

    //Variable empleada para detectar cuándo la pantalla ha pasado de estar en "portrait" a estar en "landscape"
    private int orientacionPantalla;

    //GeoPoint que almacena el valor devuelto por getMapCenter utilizado para gestionar el mapa en modo "landscape"
    private IGeoPoint centroPantalla;

    //ArrayLists donde se almacenan los marcadores y los nodos de las rutas que se han añadido al MapView
    private final ArrayList<Marker> marcadores = new ArrayList<>();
    private final ArrayList<Marker> nodeMarkers = new ArrayList<>();

    //Variable para determinar si el MapFragment debe ser restaurado de un estado anterior (se ha pulsado el botón Atrás) o no
    private int indiceRestaurar = -1;

    /**
     * El constructor por defecto es reemplazado.
     */
    public MapFragment() {
    }

    /**
     * Called to do initial creation of a fragment. This is called after onAttach(Activity) and before onCreateView(LayoutInflater,
     * ViewGroup, Bundle).
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Obtención de los parámetros pasados a través del Bundle, así como de la información de los POIs obtenidos en MainActivity
        params = getArguments();
        globalState = (GlobalState) getActivity().getApplication();
    }

    /**
     * Método llamado para instanciar el fragmento con su vista asociada (R.layout.map_fragment, en este caso).
     *
     * <p>Además, se encarga de obtener la orientación actual de la pantalla, de especificar algunas opciones necesarias en el
     * mapView que se utiliza, así como de llamar a un método u otro (mostrarMapaUsuario o mostrarMapaPOI) en función de dónde
     * venga la llamada de creación de este fragmento.</p>
     *
     * <p>En caso de que la llamada venga de POIListAdapter (POIs en formato de lista) o de otro MapFragment, este nuevo
     * MapFragment debe mostrar sólo el mapa con la ubicación del usuario, el POI en cuestión y la ruta entre ambos puntos.</p>
     *
     * <p>En cambio, si la llamada viene de cualquier otra parte (MainActivity, etc.), lo que se debe mostrar es un mapa
     * con la ubicación del usuario y todos los POIs cercanos</p>
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     *                  The fragment should not add the view itself, but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState  If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View vista = inflater.inflate(com.example.adrian.monumentos.R.layout.map_fragment, container, false);

        //Bindeo del MapView del layout a la variable de clase
        mapView = (MapView) vista.findViewById(com.example.adrian.monumentos.R.id.map);

        //Obtenemos la orientación actual de la pantalla
        orientacionPantalla = getActivity().getResources().getConfiguration().orientation;

        //Almacenamos las coordenadas del GPS del usuario
        Bundle usuario = ((MainActivity) getActivity()).obtenerArgumentos();
        latitudGPS = usuario.getDouble("latitudGPS");
        longitudGPS = usuario.getDouble("longitudGPS");

        //Configuramos algunas opciones del MapView
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.setTilesScaledToDpi(true);

        //Dependiendo desde donde reciba, se ejecuta un método u otro
        //noinspection ConstantConditions
        if ((params.getString("Origen").equals("POIListAdapter")) || (params.getString("Origen").equals("MapFragment")))
            mostrarMapaPOI();
        else
            mostrarMapaUsuario();

        return vista;
    }

    /**
     * Called when the Fragment is no longer resumed.
     *
     * Además, se encarga de obtener qué InfoWindow está abierta en el momento de abandonar el foco del MapFragment actual,
     * para restaurarlo en el momento en que se pulse el botón "Atrás"
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
     * Called by the system when the device configuration changes while your component is running.
     *
     * <p>Además, se encarga de comprobar qué InfoWindow estaba abierta cuando cambió la configuración (de la pantalla, en este caso),
     * para obtener su posición, y centrar de nuevo la pantalla en ese punto en el momento de efectuar el giro.</p>
     *
     * <p>Por último, a la posición de la cámara se le aplica una pequeña desviación en la longitud si el dispositivo se encuentra
     * en modo "landscape" para evitar que las InfoWindow se queden parcialmente ocultas tras la cabecera del menú de navegación.</p>
     *
     * @param newConfig The new device configuration.
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
     * Llamado cuando se hace tap sobre cualquier ubicación del mapa en la que no se encuentre ningún elemento.
     * Simplemente cierra todas las InfoWindow abiertas en ese momento
     *
     * @param geoPoint Punto sobre el que se aplica el método (no utilizado en nuestra aplicación)
     */
    @Override
    public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
        InfoWindow.closeAllInfoWindowsOn(mapView);
        return true;
    }

    /**
     * Llamado cuando se hacer un "tap largo" sobre el mapa. Como nuestra aplicación no captura este tipo de eventos,
     * simplemente retorna el control.
     *
     * @param geoPoint Punto sobre el que se aplica el método (no utilizado en nuestra aplicación)
     */
    @Override
    public boolean longPressHelper(GeoPoint geoPoint) {
        //No hacer nada. No capturamos este tipo de eventos
        return false;
    }

    /**
     * Called when a marker has been clicked or tapped.
     *
     * <p>Este método se encarga de, primero, cerrar el resto de InfoWindow abiertas. Después actualiza el centro de la
     * pantalla en función de si el dispositivo se encuentra en posición "portrait" o "landscape". Y, por último, abre la
     * InfoWindow de ese marcador en concreto y mueve la cámara (mediante animateTo) a esa nueva posición de la pantalla.</p>
     *
     * @param marker Marcador sobre el que se ha hecho click o tap.
     * @param mapView MapView sobre el que se aplica este método
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

    /*
     * Método que se encarga de mostrar un mapa centrado en la ubicación del POI.
     *
     * Además muestra la ubicación del usuario, así como una ruta desde la ubicación del usuario a la ubicación del POI
     * en concreto, formada, a su vez, por una serie de nodos con instrucciones concretas para llegar al destino.
     */
    private void mostrarMapaPOI() {

        //Obtención de la lista de POIs obtenida en MainActivity y el nombre del POI en cuestión a mostrar
        String nombrePoi = params.getString(POI_NOMBRE);
        ArrayList<POI> listaPOIs = globalState.getListaPOIs();
        POI poi = null;

        //Obtención del objeto POI asociado a ese nombre
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

        //Creación del GeoPoint con la ubicación del POI
        GeoPoint point = new GeoPoint(latitud, longitud);

        //Aplicación del zoom sobre el mapView
        IMapController iMapController = mapView.getController();
        iMapController.setZoom(14);

        //Obtención del nuevo centroPantalla en función de la posición del dispositivo
        centroPantalla = point;
        if (orientacionPantalla == 2)
            //Orientation: Landscape
            centroPantalla = new GeoPoint(centroPantalla.getLatitude() + 0.0015, centroPantalla.getLongitude());

        iMapController.setCenter(centroPantalla);

        //Creación del objeto Marker con la información del POI
        Marker marker = new Marker(mapView);
        marker.setTitle(nombrePoi);
        marker.setSnippet(descripcion);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setRelatedObject(poi);
        marker.setIcon(ContextCompat.getDrawable(getActivity(), com.example.adrian.monumentos.R.drawable.marker_icon_poi));

        InfoBubble infoBubble = new InfoBubble(mapView, this, false);
        marker.setInfoWindow(infoBubble);

        //Ubicación del usuario
        GeoPoint miUbicacion = new GeoPoint(latitudGPS, longitudGPS);

        //Creación del objeto Marker con la información de la ubicación del usuario
        Marker user = new Marker(mapView);
        user.setTitle(getResources().getString(com.example.adrian.monumentos.R.string.ubication));
        user.setPosition(miUbicacion);
        user.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        user.setIcon(ContextCompat.getDrawable(getActivity(), com.example.adrian.monumentos.R.drawable.marker_user_icon));

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this);
        mapView.getOverlays().add(0, mapEventsOverlay);

        marker.setOnMarkerClickListener(this);
        user.setOnMarkerClickListener(this);

        //Creación del ProgressDialog a mostrar durante la obtención de la ruta entre el usuario y el POI
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getResources().getString(com.example.adrian.monumentos.R.string.datos));
        progressDialog.setCanceledOnTouchOutside(false);

        marker.showInfoWindow();

        //Añadimos ambos marcadores a los ArrayList por si fuera necesario recuperar el estado del fragmento más adelante
        marcadores.add(marker);
        marcadores.add(user);

        //Creación de la ruta desde el usuario hasta el POI
        new crearRuta(progressDialog, miUbicacion, point, mapView.getOverlays(), user, marker).execute();
    }

    /*
     * Método que se encarga de mostrar un mapa centrado en la posición del usuario, y las ubicaciones
     * de los POI cercanos en función de los parámetros introducidos por el usuario en HomeFragment.
     */
    private void mostrarMapaUsuario() {

        //Obtención de los POIs
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
            marker.setIcon(ContextCompat.getDrawable(getActivity(), com.example.adrian.monumentos.R.drawable.marker_icon_poi));

            marker.setInfoWindow(infoBubble);

            mapView.getOverlays().add(marker);
            marcadores.add(marker);
        }

        //Creación de un objeto Marker con la ubicación del usuario
        GeoPoint miUbicacion = new GeoPoint(latitudGPS, longitudGPS);

        Marker user = new Marker(mapView);
        user.setTitle(getResources().getString(com.example.adrian.monumentos.R.string.ubication));
        user.setPosition(miUbicacion);
        user.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        user.setIcon(ContextCompat.getDrawable(getActivity(), com.example.adrian.monumentos.R.drawable.marker_user_icon));

        mapView.getOverlays().add(user);

        //Aplicación del zoom sobre el MapView
        IMapController iMapController = mapView.getController();
        iMapController.setZoom(14);

        /*
        * Si el fragmento debe ser restaurado, "indiceRestaurar" será distinto de -1 y tendrá el índice del ArrayList
        * del Marker a restaurar.
        *
        * En caso de que "indiceRestaurar" sea igual a -1, el fragmento no tiene que ser restaurado a un estado anterior, por
        * lo que simplemente se centrará el mapa en la ubicación del usuario y se abrirá su InfoWindow.
        * */
        if (indiceRestaurar == -1) {
            centroPantalla = miUbicacion;
            if (orientacionPantalla == 2)
                //Orientation: Landscape
                centroPantalla = new GeoPoint(centroPantalla.getLatitude() + 0.0015, centroPantalla.getLongitude());

            iMapController.setCenter(centroPantalla);

            user.showInfoWindow();

            mapView.invalidate();
        } else {
            //El fragmento está siendo restaurado de un estado anterior
            Marker marcador = marcadores.get(indiceRestaurar);

            POI restoredPOI = null;

            //Es necesario obtener el objeto POI asociado
            for (POI poiToRestore : listaPOIs) {
                if (poiToRestore.getNombre().equals(marcador.getTitle())) {
                    restoredPOI = poiToRestore;
                }
            }

            assert restoredPOI != null;
            centroPantalla = new GeoPoint(restoredPOI.getLatitud(), restoredPOI.getLongitud());
            if (orientacionPantalla == 2)
                //Orientation: Landscape
                centroPantalla = new GeoPoint(centroPantalla.getLatitude() + 0.0015, centroPantalla.getLongitude());

            iMapController.setCenter(centroPantalla);

            //Es necesario crear un nuevo objeto Marker con la información del POI a restaurar
            Marker restoredMarker = new Marker(mapView);
            restoredMarker.setTitle(restoredPOI.getNombre());
            restoredMarker.setSnippet(restoredPOI.getDescripcion());
            restoredMarker.setPosition(new GeoPoint(restoredPOI.getLatitud(), restoredPOI.getLongitud()));
            restoredMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            restoredMarker.setRelatedObject(restoredPOI);
            restoredMarker.setIcon(ContextCompat.getDrawable(getActivity(), com.example.adrian.monumentos.R.drawable.marker_icon_poi));

            InfoBubble infoBubbleToRestore = new InfoBubble(mapView, this, true);
            restoredMarker.setInfoWindow(infoBubbleToRestore);

            mapView.getOverlays().remove(marcador);
            mapView.getOverlays().add(restoredMarker);

            //Eliminación del anterior Marker del ArrayList e incorporación del nuevo elemento
            marcadores.remove(indiceRestaurar);
            marcadores.add(restoredMarker);

            restoredMarker.showInfoWindow();

            mapView.invalidate();
        }

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this);
        mapView.getOverlays().add(0, mapEventsOverlay);

        //A cada Marker en el ArrayList se le asigna un objeto onMarkerClick
        for (Marker markerPOI : marcadores) {
            markerPOI.setOnMarkerClickListener(this);
        }

        user.setOnMarkerClickListener(this);

        //Por último, se añade el usuario al ArrayList
        marcadores.add(user);
    }

    /*
    * Clase empleada para obtener la ruta entre la ubicación del usuario y el POI en cuestión; así como de los nodos que
    * componen esa ruta.
    */
    private class crearRuta extends AsyncTask<Void, Void, Void> {

        //GeoPoints inicio y fin de la ruta
        private final GeoPoint inicio;
        private final GeoPoint fin;

        private final List<Overlay> overlays;
        private final ProgressDialog progressDialog;

        //Markers que indican el inicio y fin de la ruta
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
            //Mostrar el progressDialog en lo que se obtienen los datos necesarios
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            RoadManager roadManager = new OSRMRoadManager(getActivity());

            ArrayList<GeoPoint> geoPoints = new ArrayList<>();
            geoPoints.add(inicio);
            geoPoints.add(fin);

            //Obtención de la ruta a través de los GeoPoint
            Road road = roadManager.getRoad(geoPoints);

            Polyline polyline = RoadManager.buildRoadOverlay(road);

            polyline.setWidth(10);

            overlays.add(polyline);

            Drawable nodeIcon = ContextCompat.getDrawable(getActivity(), com.example.adrian.monumentos.R.drawable.marker_node);

            String subdescripcion;

            //Para cada nodo en la ruta, crear un nuevo Marker con su información
            for (int i = 1; i < road.mNodes.size() - 1; i++) {
                RoadNode roadNode = road.mNodes.get(i);
                Marker nodeMarker = new Marker(mapView);
                nodeMarker.setPosition(roadNode.mLocation);
                nodeMarker.setIcon(nodeIcon);
                nodeMarker.setTitle("Paso " + i);

                //Traducir las instrucciones y actualizar la distancia y el tiempo desde cada nodo al final de la ruta
                cambiarInstrucciones(roadNode);
                actualizarDatos(road, i);

                nodeMarker.setSnippet(roadNode.mInstructions);

                subdescripcion = Road.getLengthDurationText(getActivity(), roadNode.mLength, roadNode.mDuration);

                nodeMarker.setSubDescription(introducirEspacios(subdescripcion) + " restante hasta tu destino");

                nodeMarker.setImage(seleccionarIcono(roadNode.mManeuverType));

                overlays.add(nodeMarker);
                nodeMarkers.add(nodeMarker);

                //Mismo método "onMarkerClickListener" que la super clase
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

            //Se repite el mismo proceso para el nodo del usuario (inicio) que para el del resto de nodos de la ruta
            RoadNode nodoUsuario = road.mNodes.get(0);
            actualizarDatos(road, 0);

            subdescripcion = Road.getLengthDurationText(getActivity(), nodoUsuario.mLength, nodoUsuario.mDuration);

            user.setSubDescription(introducirEspacios(subdescripcion) + " restante hasta tu destino");

            //Se repite el mismo proceso para el nodo del poi (final) que para el del resto de nodos de la ruta
            RoadNode nodoPoi = road.mNodes.get(road.mNodes.size() - 1);
            actualizarDatos(road, road.mNodes.size() - 1);

            subdescripcion = Road.getLengthDurationText(getActivity(), nodoPoi.mLength, nodoPoi.mDuration);

            poi.setSubDescription(introducirEspacios(subdescripcion) + " restante hasta tu destino");

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //Añadir, por último, los marcadores al MapView y actualizar el mismo
            mapView.getOverlays().add(user);
            mapView.getOverlays().add(poi);

            mapView.invalidate();

            //Cerrar el ProgressDialog una vez se ha terminado
            progressDialog.dismiss();
        }

        /*
        * Dado que la información que proporciona la biblioteca "osmdroid" viene, por defecto, en inglés, y sin posibilidad de
        * traducirla de forma eficiente, este método se encarga de traducir cada una de las posibles instrucciones que pueden
        * aparecer en los nodos y traducirlas al castellano.
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

        /*
        * Dado que la información almacenada en cada NodeMarker en relación a la distancia y tiempo es de un nodo hasta el siguiente,
        * este método se encarga de que en cada NodeMarker aparezca la distancia y tiempo desde ese nodo hasta el final de la ruta.
        * */
        private void actualizarDatos(Road road, int indice) {
            ArrayList<RoadNode> roadNodes = road.mNodes;
            RoadNode roadNode = roadNodes.get(indice);

            for (int i = indice + 1; i < roadNodes.size(); i++) {
                roadNode.mLength += roadNodes.get(i).mLength;
                roadNode.mDuration += roadNodes.get(i).mDuration;
            }
        }

        /*
        * Este método simplemente modifica la frase en la que viene la información de distancia y tiempo en cada NodeMarker para que
        * se vea mejor al añadir algunos espacios.
        * */
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

        /*
        * Método que, en función del atributo "maneuverType" asigna al NodeMarker un icono representando la información que éste
        * proporciona en relación a la dirección que debe tomar el usuario.
        * */
        private Drawable seleccionarIcono(int maneuverType) {

            Drawable icono = ContextCompat.getDrawable(getActivity(), com.example.adrian.monumentos.R.drawable.ic_continue);

            switch (maneuverType) {
                case 3:
                    icono = ContextCompat.getDrawable(getActivity(), com.example.adrian.monumentos.R.drawable.ic_slight_left);
                    break;
                case 4:
                    icono = ContextCompat.getDrawable(getActivity(), com.example.adrian.monumentos.R.drawable.ic_turn_left);
                    break;
                case 5:
                    icono = ContextCompat.getDrawable(getActivity(), com.example.adrian.monumentos.R.drawable.ic_sharp_left);
                    break;
                case 6:
                    icono = ContextCompat.getDrawable(getActivity(), com.example.adrian.monumentos.R.drawable.ic_slight_right);
                    break;
                case 7:
                    icono = ContextCompat.getDrawable(getActivity(), com.example.adrian.monumentos.R.drawable.ic_turn_right);
                    break;
                case 8:
                    icono = ContextCompat.getDrawable(getActivity(), com.example.adrian.monumentos.R.drawable.ic_sharp_right);
                    break;
                case 12:
                    icono = ContextCompat.getDrawable(getActivity(), com.example.adrian.monumentos.R.drawable.ic_u_turn);
                    break;
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                case 32:
                case 33:
                case 34:
                    icono = ContextCompat.getDrawable(getActivity(), com.example.adrian.monumentos.R.drawable.ic_roundabout);
                    break;
            }
            return icono;
        }
    }
}
