package com.example.adrian.monumentos;

import android.support.multidex.MultiDexApplication;
import java.util.ArrayList;

/**
 * Clase que se utilizará para almacenar la información global a toda la Aplicación.
 * En este caso, el ArrayList de POIs a mostrar
 */
public class GlobalState extends MultiDexApplication {

    private ArrayList<POI> listaPOIs;

    @Override
    public void onCreate() {
        super.onCreate();

        listaPOIs = new ArrayList<>();
    }

    public ArrayList<POI> getListaPOIs() { return listaPOIs; }

    public void setListaPOIs(ArrayList<POI> listaPOIs) { this.listaPOIs = listaPOIs; }
}