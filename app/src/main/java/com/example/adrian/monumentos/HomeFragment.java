package com.example.adrian.monumentos;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

/**
 * Esta clase
 * @author Adrian Munoz Rojo
 * @author Rafael Matamoros Luque
 * @author David Carrancio Aguado
 */
public class HomeFragment extends Fragment implements View.OnClickListener {

    LinearLayout home_container;
    SoftKeyboard softKeyboard;

    //Variables introducidas (o no) por el usuario
    EditText nMaxPOIUsuario, radioUsuario;

    /*Almacenamos el tipo de error en el caso de que alguno (o ambos) de los valores
     * introducidos por el usuario no sean válidos
     */
    String tipoError = "";

    //Constructor por defecto
    public HomeFragment(){}


    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.home_fragment, container, false);

        home_container = (LinearLayout) vista.findViewById(R.id.home_container);

        final InputMethodManager im = (InputMethodManager) getActivity().getApplicationContext().getSystemService(Activity.INPUT_METHOD_SERVICE);

        nMaxPOIUsuario = (EditText) vista.findViewById(R.id.maxPOI);
        radioUsuario = (EditText) vista.findViewById(R.id.radio);

        softKeyboard = new SoftKeyboard(home_container, im);

        softKeyboard.setSoftKeyboardCallback(new SoftKeyboard.SoftKeyboardChanged() {
            @Override
            public void onSoftKeyboardHide() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        home_container.requestFocus();
                    }
                });
            }

            @Override
            public void onSoftKeyboardShow() {
                //No hacer nada. El comportamiento por defecto es el adecuado
            }
        });

        nMaxPOIUsuario.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    //Mostrar el teclado cuando se obtenga el focus
                    softKeyboard.openSoftKeyboard();
                }
            }
        });

        radioUsuario.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    //Mostrar el teclado cuando se obtenga el focus
                    softKeyboard.openSoftKeyboard();
                }
            }
        });

        home_container.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    //Ocultar el teclado cuando se obtenga el focus
                    softKeyboard.closeSoftKeyboard();
                }
            }
        });

        Button ubicacion = (Button) vista.findViewById(R.id.buscarPorUbicacion);
        ubicacion.setOnClickListener(this);

        return vista;
    }

    /**
     * Called when a fragment is first attached to its context.
     * {@link #onCreate(Bundle)} will be called after this.
     *
     * @param context Context
     */
    @Override
    public void onAttach(Context context) { super.onAttach(context); }

    /**
     * Called when the fragment is no longer attached to its activity.  This is called after
     * {@link #onDestroy()}, except in the cases where the fragment instance is retained across
     * Activity re-creation (see {@link #setRetainInstance(boolean)}), in which case it is called
     * after {@link #onStop()}.
     */
    @Override
    public void onDetach() { super.onDetach(); }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {

        MainActivity mainActivity = (MainActivity) getActivity();

        int nMaxPOI, radio;

        try {
            //Almacenamos el valor introducido por el usuario
            nMaxPOI = Integer.parseInt(nMaxPOIUsuario.getText().toString());
        } catch (NumberFormatException e){
            //En caso contrario, asignamos un -1 a la variable
            nMaxPOI = -1;
        }

        try {
            //Almacenamos el valor introducido por el usuario
            radio = Double.valueOf(Double.valueOf(radioUsuario.getText().toString()) * 1000).intValue();
        } catch (NumberFormatException e){
            //En caso contrario, asignamos un -1 a la variable
            radio = -1;
        }

        if(validarDatosEntrada(nMaxPOI, radio)) {
            //Los datos introducidos son válidos (o no se ha introducido nada)
            if (mainActivity.isGPSAndInternetEnabled()) {
                mainActivity.getNavigationView().getMenu().getItem(2).setChecked(true);

                //Sólo se asginan nuevos valores si el usuario ha introducido algo
                if(nMaxPOI != -1)
                    mainActivity.setInputNMaxPOI(nMaxPOI);

                if(radio != -1)
                    mainActivity.setInputRadioBusqueda(radio);

                mainActivity.mostrarMapa();
            }
        }
        else{
            mainActivity.showErrorDialog(tipoError);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        softKeyboard.unRegisterSoftKeyboardCallback();
    }

    private boolean validarDatosEntrada(int maxPOI, int radio){

        if(maxPOI != -1){
            //Se ha introducido un valor para nMaxPOI
            if(radio != -1){
                //Además, se ha introducido un valor para radio
                if((maxPOI >= 1) && (maxPOI <= 500)) {
                    //nMaxPOI está en rango valido
                    if((radio >= 10) && (radio <= 10000)){
                        //Y radio también está en rango válido
                        return true;
                    }
                    else{
                        tipoError = "RADIO";
                        return false;
                    }
                }
                else{
                    //nMaxPOI no está en el rango permitido
                    if((radio >= 10) && (radio <= 10000)){
                        //Pero radio sí lo está
                        tipoError = "MAXPOI";
                        return false;
                    }
                    else{
                        //Ni nMaxPOI ni rango se encuentran en el rango permitido
                        tipoError = "MAXPOIandRADIO";
                        return false;
                    }
                }
            }
            else{
                //Pero no se ha introducido un valor para radio
                if((maxPOI >= 1) && (maxPOI <= 500)){
                    return true;
                }
                else{
                    tipoError = "MAXPOI";
                    return false;
                }
            }
        }
        else{
            //No se ha introducido un valor para nMaxPOI
            if(radio != -1){
                //Pero sí se ha introducido un valor para radio
                if((radio >= 10) && (radio <= 10000)){
                    return true;
                }
                else{
                    tipoError = "RADIO";
                    return false;
                }
            }
            else{
                //No se ha introducido ningún valor, por tanto, ambos datos son válidos
                return true;
            }
        }
    }
}
