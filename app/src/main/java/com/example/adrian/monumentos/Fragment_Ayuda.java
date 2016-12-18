package com.example.adrian.monumentos;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Creación del Fragmento
 */

public class Fragment_Ayuda extends Fragment {


    public Fragment_Ayuda() {
        // Required empty public constructor
    }

    /**
     * Metodo onCreate
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    /**
     * Inflamos el fragmento con su layout
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ayuda,container,false);
    }






}
