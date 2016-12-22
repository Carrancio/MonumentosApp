package com.example.adrian.monumentos;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.adrian.monumentos.Fragmentos.MapFragment;
import com.example.adrian.monumentos.Fragmentos.WikiFragment;
import com.squareup.picasso.Picasso;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

/**
 * La clase InfoBubble define la estructura y el contenido que tiene la "burbuja" de información
 * que se muestra cuando, en el mapa, el usuario hace "click" sobre un marcador referente a un POI.
 *
 * @author Adrián Muñoz Rojo
 * @author Rafael Matamoros Luque
 * @author David Carrancio Aguado
 * @see MapFragment
 * @see WikiFragment
 * @version 1.0
 */
public class InfoBubble extends MarkerInfoWindow {

    //Variables empleadas para pasar los datos en un objeto Bundle
    private final static String POI_NOMBRE = "POI_NOMBRE";
    private final static String POI_URL = "POI_URL";

    //El objeto POI en cuestión
    private POI poi;

    /*
    * Esta variable indica si se debe mostrar el icono de la ruta en la burbuja o no.
    * En función de si la burbuja se abre en un MapFragment con un solo POI o en un MapFragment
    * que muestre todos los POIs junto con la ubicación del usuario
    */
    private final boolean mostrarIconoUbicacion;

    /**
     * Método que se encarga de crear la burbuja y mostrar dos botones. Uno que crearía otro MapFragment con la ubicación
     * del usuario, la del POI en concreto y la ruta entre ambos, y otro que crearía un WikiFragment y te llevaría a la URL
     * del enlace a la WikiPedia del POI.
     *
     * @param mapView MapView sobre el que se va a aplicar
     * @param mapFragment Fragmento en el que se encuentra el Marker que ha sido clickado
     * @param mostrarIconoUbicacion Determina si se debe mostrar el icono para mostrar las rutas o no en esa burbuja
     */
    public InfoBubble(MapView mapView, final MapFragment mapFragment, boolean mostrarIconoUbicacion) {
        super(R.layout.info_bubble, mapView);

        this.mostrarIconoUbicacion = mostrarIconoUbicacion;

        Button boton_mas_info = (Button) mView.findViewById(R.id.boton_mas_informacion);

        boton_mas_info.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (poi.getEnlace() != null) {

                    WikiFragment wikiFragment = new WikiFragment();
                    Bundle params = new Bundle();
                    params.putString(POI_URL, poi.getEnlace());
                    wikiFragment.setArguments(params);

                    mapFragment.getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.content_frame, wikiFragment)
                            .addToBackStack("MapFragment")
                            .commit();
                }
            }
        });

        if (mostrarIconoUbicacion) {
            Button boton_ruta = (Button) mView.findViewById(R.id.boton_ruta);

            boton_ruta.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    MapFragment mapFragmentPOI = new MapFragment();
                    Bundle params = new Bundle();
                    params.putString("Origen", "MapFragment");
                    params.putString(POI_NOMBRE, poi.getNombre());
                    mapFragmentPOI.setArguments(params);

                    mapFragment.getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.content_frame, mapFragmentPOI)
                            .addToBackStack("MapFragment")
                            .commit();
                }
            });
        }
    }

    /**
     * Este método se encarga de, primero, cerrar el resto de InfoWindow que pueda haber abiertas. Después,
     * activa la visibilidad de los botones que sean necesarios, y, por último, intenta incorporar la imagen del POI
     * que se tiene guardada, o en su ausencia, pone la imagen de la aplicación por defecto.
     *
     * @param item Item asociado al InfoBubble. En este caso el Marker al ser clickado
     */
    @Override
    public void onOpen(Object item) {
        super.onOpen(item);

        InfoWindow.closeAllInfoWindowsOn(getMapView());

        Marker marker = (Marker) item;
        poi = (POI) marker.getRelatedObject();

        mView.findViewById(R.id.boton_mas_informacion).setVisibility(View.VISIBLE);

        if (mostrarIconoUbicacion)
            mView.findViewById(R.id.boton_ruta).setVisibility(View.VISIBLE);

        //Dibujar el logo de la App antes de descargar la imagen de WikiPedia
        ((ImageView) mView.findViewById(R.id.bubble_image)).setImageResource(R.drawable.ic_app);

        //Descarga de la imagen a partir de la URL
        if (poi.getUrl_imagen() != null) {
            Picasso.with(mView.getContext()).load(poi.getUrl_imagen())
                    .placeholder(R.drawable.ic_app)
                    .fit()
                    .centerCrop()
                    .into(((ImageView) mView.findViewById(R.id.bubble_image)));
        }
        mView.findViewById(R.id.bubble_image).setVisibility(View.VISIBLE);
    }
}