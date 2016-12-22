package com.example.adrian.monumentos;

import android.support.multidex.MultiDexApplication;

import java.util.ArrayList;

/**
 * Clase que se utiliza para almacenar la información global a toda la aplicación. En este caso, el ArrayList de POIs a mostrar.
 *
 * @author Adrián Muñoz Rojo
 * @author Rafael Matamoros Luque
 * @author David Carrancio Aguado
 * @version 1.0
 */
@SuppressWarnings("ALL")
public class GlobalState extends MultiDexApplication {

    //ArrayList donde se almacenan los POIs obtenidos en MainActivity
    private ArrayList<POI> listaPOIs;

    /**
     * Called to do initial creation of a fragment. This is called after onAttach(Activity) and before onCreateView(LayoutInflater,
     * ViewGroup, Bundle).
     *
     * <p>Además, se inicializa el ArrayList de POIs</p>
     */
    @Override
    public void onCreate() {
        super.onCreate();

        listaPOIs = new ArrayList<>();
    }

    /**
     * @return listaPOIs La lista de POIs almacenados.
     */
    public ArrayList<POI> getListaPOIs() { return listaPOIs; }

    /**
     * @param listaPOIs La lista de POIs a almacenar.
     */
    public void setListaPOIs(ArrayList<POI> listaPOIs) { this.listaPOIs = listaPOIs; }
}