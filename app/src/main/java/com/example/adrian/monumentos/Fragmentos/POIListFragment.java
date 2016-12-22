package com.example.adrian.monumentos.Fragmentos;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.adrian.monumentos.POIListAdapter;

/**
 * Esta clase se encarga simplemente de bindear el layout en el que se mostrará la lista de POIs con el objeto RecyclerView,
 * así como la creación del Adapter necesario para poder implementar esta lista.
 *
 * @author Adrián Muñoz Rojo
 * @author Rafael Matamoros Luque
 * @author David Carrancio Aguado
 * @see POIListAdapter
 * @version 1.0
 */
public class POIListFragment extends Fragment {

    /**
     * El constructor por defecto es reemplazado.
     */
    public POIListFragment() {
    }

    /**
     * Método llamado para instanciar el fragmento con su vista asociada (R.layout.poi_list_fragment, en este caso)
     *
     * <p>Además se encarga de crear el objeto RecyclerView.Adapter para gestionar el propio RecyclerView</p>
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     *                  The fragment should not add the view itself, but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState  If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View vista = inflater.inflate(com.example.adrian.monumentos.R.layout.poi_list_fragment, container, false);


        RecyclerView recyclerView = (RecyclerView) vista.findViewById(com.example.adrian.monumentos.R.id.recycler_view);

        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        RecyclerView.Adapter adapter = new POIListAdapter(getActivity(), this);
        recyclerView.setAdapter(adapter);

        return vista;
    }
}