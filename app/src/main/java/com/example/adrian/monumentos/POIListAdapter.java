package com.example.adrian.monumentos;

import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
/**
 * Esta clase sirve para obtener datos de cada POI
 * @author Adrian Munoz Rojo
 * @author Rafael Matamoros Luque
 * @author David Carrancio Aguado
 *
 */


class POIListAdapter extends RecyclerView.Adapter<POIListAdapter.ViewHolder> {

    private final ArrayList<POI> poiList;
    private final Context context;
    private final POIListFragment poiListFragment;
    private final GlobalState globalState;

    final static String POI_URL = "POI_URL";
    private final static String POI_NOMBRE = "POI_NOMBRE";

    static class ViewHolder extends RecyclerView.ViewHolder {

        //Elementos de una CardView
        private final TextView tituloPoi, descripcionPoi;
        private final ImageView imagenPoi;
        private final Button botonLeerMas;
        private final Button irAlMapa;

        private ViewHolder(View vista) {
            super(vista);

            tituloPoi = (TextView) vista.findViewById(R.id.titulo_poi);
            descripcionPoi = (TextView) vista.findViewById(R.id.descripcion_poi);
            imagenPoi = (ImageView) vista.findViewById(R.id.imagen_poi);
            botonLeerMas = (Button) vista.findViewById(R.id.boton_leer_mas);
            irAlMapa = (Button) vista.findViewById(R.id.boton_ir_al_mapa);
        }
    }

    POIListAdapter(Context context, POIListFragment poiListFragment){
        this.context = context;
        this.poiListFragment = poiListFragment;
        this.globalState = (GlobalState) context.getApplicationContext();
        this.poiList = globalState.getListaPOIs();
    }


    //Crea una nueva vista (invocado por el LayoutManager)
    @Override
    public POIListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType){

        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.poi_miniatura, parent, false);

        return new ViewHolder(vista);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final String nombrePoi = poiList.get(position).getNombre();
        final String descripcionPoi = poiList.get(position).getDescripcion();
        String urlImagenPoi = poiList.get(position).getUrl_imagen();
        final String enlacePoi = poiList.get(position).getEnlace();

        // Añadimos un evento Listener al botón "Leer más" para redirigir al enlace del POI en la WikiPedia
        if (enlacePoi != null && !"".equals(enlacePoi)) {
            holder.botonLeerMas.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    WikiFragment wikiFragment = new WikiFragment();
                    Bundle params = new Bundle();
                    params.putString(POI_URL, enlacePoi);
                    wikiFragment.setArguments(params);

                    FragmentTransaction transaction = poiListFragment.getFragmentManager().beginTransaction();
                    transaction.replace(R.id.content_frame, wikiFragment);
                    transaction.addToBackStack("WikiFragment");
                    transaction.commit();
                }
            });
        }

        final double latitudPoi = poiList.get(position).getLatitud();
        final double longitudPoi = poiList.get(position).getLongitud();

        // Añadimos otro evento Listener al botón "Ir al mapa" para redirigir al mapa centrado en la ubicación del POI
        if ((latitudPoi != 0.0) && (longitudPoi != 0.0)) {
            holder.irAlMapa.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MapFragment mapFragment = new MapFragment();
                    Bundle params = new Bundle();
                    params.putString("Origen", "POIListAdapter");
                    params.putString(POI_NOMBRE, nombrePoi);
                    mapFragment.setArguments(params);

                    FragmentTransaction transaction = poiListFragment.getFragmentManager().beginTransaction();
                    transaction.replace(R.id.content_frame, mapFragment);
                    transaction.addToBackStack("MapFragment");
                    transaction.commit();
                }
            });
        }
        else {
            Toast.makeText(context, "No se ha podido obtener la ubicación de este punto de interés", Toast.LENGTH_LONG).show();
        }


        if (nombrePoi != null) {
            holder.tituloPoi.setText(nombrePoi);

            if (descripcionPoi != null)
                if ("".equals(descripcionPoi))
                    holder.descripcionPoi.setVisibility(View.GONE);
                else {
                    holder.descripcionPoi.setVisibility(View.VISIBLE);
                    holder.descripcionPoi.setText(descripcionPoi);
                }
        }

        //Dibujar el logo de la App antes de descargar la imagen de WikiPedia
        holder.imagenPoi.setImageResource(R.drawable.ic_app);

        //Descarga de la imagen a partir de la URL
        if (urlImagenPoi != null && !"".equals(urlImagenPoi)) {
            Picasso.with(context).load(urlImagenPoi)
                    .placeholder(R.drawable.ic_app)
                    .fit()
                    .centerCrop()
                    .into(holder.imagenPoi);
        }
    }



    @Override
    public int getItemCount() { return poiList.size(); }
}