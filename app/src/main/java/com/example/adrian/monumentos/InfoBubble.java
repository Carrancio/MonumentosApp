package com.example.adrian.monumentos;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

/**
 * La clase InfoBubble define la estructura y el contenido que tiene la "burbuja" de información
 * que se muestra cuando, en el mapa, el usuario hace "click" sobre un marcador referente a un POI (Point of Interest).
 *
 * Esta clase utiliza una "third-party library" llamada OSMBonusPack que provee varias funcionalidades para la
 * creación de las "custom info bubble".
 *
 * Enlace al repositorio github de esta biblioteca: https://github.com/MKergall/osmbonuspack
 */
class InfoBubble extends MarkerInfoWindow {

    private POI poi;

    private final static String POI_NOMBRE = "POI_NOMBRE";
    private final static String POI_URL = "POI_URL";

    private boolean mostrarIconoUbicacion;

    InfoBubble(MapView mapView, final MapFragment mapFragment, boolean mostrarIconoUbicacion){
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
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        if(mostrarIconoUbicacion) {
            Button boton_ruta = (Button) mView.findViewById(R.id.boton_ruta);

            boton_ruta.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    view.setBackground(ContextCompat.getDrawable(mView.getContext(), R.drawable.ic_ubication_pressed));

                    MapFragment mapFragmentPOI = new MapFragment();
                    Bundle params = new Bundle();
                    params.putString("Origen", "MapFragment");
                    params.putString(POI_NOMBRE, poi.getNombre());
                    mapFragmentPOI.setArguments(params);

                    mapFragment.getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.content_frame, mapFragmentPOI)
                            .addToBackStack(null)
                            .commit();
                }
            });
        }
    }

    @Override
    public void onOpen(Object item) {
        InfoWindow.closeAllInfoWindowsOn(getMapView());
        super.onOpen(item);

        mView.findViewById(R.id.boton_mas_informacion).setVisibility(View.VISIBLE);

        if(mostrarIconoUbicacion)
            mView.findViewById(R.id.boton_ruta).setVisibility(View.VISIBLE);

        Marker marker = (Marker) item;
        poi = (POI) marker.getRelatedObject();

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
