package com.example.adrian.monumentos.Fragmentos;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Esta clase consiste simplemente en un constructor (por defecto) requerido para la creación del Fragmento
 * y el método que se encarga de "inflar" el fragmento con su correspondiente layout.
 *
 * <p>El fragmento es inflado y mostrado cuando el usuario pulsa la opción "Sobre TripApp" del menú lateral de la aplicación.</p>
 *
 * <p>Esta clase forma parte de la aplicación TripApp, desarrollada para la asignatura Sistemas Móviles.</p>
 *
 * @author Adrián Muñoz Rojo
 * @author Rafael Matamoros Luque
 * @author David Carrancio Aguado
 * @version 1.0
 */
public class AboutFragment extends Fragment {

    /**
     * El constructor por defecto es reemplazado.
     */
    public AboutFragment() {
    }

    /**
     * Método llamado para instanciar el fragmento con su vista asociada (R.layout.about_fragment, en este caso)
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     *                  The fragment should not add the view itself, but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState  If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return  inflater.inflate(com.example.adrian.monumentos.R.layout.about_fragment,container,false);
    }
}
