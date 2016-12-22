package com.example.adrian.monumentos.Fragmentos;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.adrian.monumentos.MainActivity;
import com.example.adrian.monumentos.Utilidades.SoftKeyboard;

/**
 * Esta clase es el fragmento principal, que se ejecuta en el momento en que el usuario abre la aplicación o pulsa
 * sobre la opción "Inicio" del menú lateral de la misma.
 *
 * <p>El layout que infla está compuesto simplemente por dos cuadros de texto editables (EditText) y un botón
 * que se encarga de llevar al usuario a un MapView con su ubicación y la de los POIs (Point of Interest) cercanos.
 * Previamente a esto se comprueba que de dispone tanto de conexión a Internet como de la ubicación del usuario por
 * medio de las coordenadas GPS de su dispositivo.</p>
 *
 * <p>Esta clase de lo que se encarga es, por un lado, gestionar todos los eventos relacionados con el focus de los EditText
 * y la aparición y desaparición del teclado en pantalla (softkeyboard); y, por otro lado, también se encarga de validar que la
 * información introducida por el usuario es válida (existen unos límites a la hora de introducir valores que la aplicación puede
 * gestionar), mostrando un ErrorDialog en caso contrario, y de enviar esa información introducida por el usuario a la clase MainActivity</p>
 *
 * <p>Esta clase forma parte de la aplicación TripApp, desarrollada para la asignatura Sistemas Móviles.</p>
 *
 * @author Adrián Muñoz Rojo
 * @author Rafael Matamoros Luque
 * @author David Carrancio Aguado
 * @see MainActivity
 * @see SoftKeyboard
 * @see ErrorDialogFragment
 * @version 1.0
 */
public class HomeFragment extends Fragment implements View.OnClickListener {

    //Layout padre que contiene el resto de elementos mostrados en la vista (ImageView, EditText y Button)
    private LinearLayout home_container;

    //Instancia de la clase SoftKeyboard empleada para manejar eventos de aparición y ocultación del teclado en pantalla (softkeyboard)
    private SoftKeyboard softKeyboard;

    //Numero máximo de POIs que el usuario quiere que aparezcan en el MapView
    private EditText nMaxPOIUsuario;

    //Radio de búsqueda respecto a la ubicación del usuario en el que se mostrarán POIs
    private EditText radioUsuario;

    //Tipo de error en caso de que alguno (o ambos) de los valores introducidos por el usuario no sean válidos
    private String tipoError = "";

    /**
     * El constructor por defecto es reemplazado.
     */
    public HomeFragment() {
    }

    /**
     * Método llamado para instanciar el fragmento con su vista asociada (R.layout.home_fragment, en este caso).
     *
     * <p>Además, en este método se gestionan los eventos de focus en los EditText, así como los de aparición y desaparición
     * del teclado en pantalla (softkeyboard).</p>
     *
     * <p>Por último, se añade un método "onClickListener" al botón "Buscar alrededor de mi posición".</p>
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

        View vista = inflater.inflate(com.example.adrian.monumentos.R.layout.home_fragment, container, false);

        //Bindeo de las variables de la clase con los elementos del layout
        home_container = (LinearLayout) vista.findViewById(com.example.adrian.monumentos.R.id.home_container);
        nMaxPOIUsuario = (EditText) vista.findViewById(com.example.adrian.monumentos.R.id.maxPOI);
        radioUsuario = (EditText) vista.findViewById(com.example.adrian.monumentos.R.id.radio);

        //Obtención del inputMethodManager para la instanciación de la clase SoftKeyboard
        final InputMethodManager im = (InputMethodManager) getActivity().getApplicationContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        softKeyboard = new SoftKeyboard(home_container, im);

        //Evento SoftKeyboardChanged. Llamado cada vez que el teclado en pantalla desaparece o aparece
        SoftKeyboard.SoftKeyboardChanged softKeyboardChanged =
                new SoftKeyboard.SoftKeyboardChanged() {
            @Override
            public void onSoftKeyboardHide() {
                /* En caso de que el teclado en pantalla desaparezca, lo que queremos es que se pierda el focus en los
                 * EditText, solicitando al Layout padre, que contiene el resto de elementos, que obtenga el focus.
                 *
                 * Este tipo de acciones sólo pueden realizarse en el UI thread principal de la aplicación, motivo por el cual
                 * se emplea el método "runOnUiThread".
                 */
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        home_container.requestFocus();
                    }
                });
            }

            @Override
            public void onSoftKeyboardShow() {
                /* Cuando el teclado en pantalla aparezca no es necesario que se haga ninguna acción
                 * especial, el comportamiento por defecto es el adecuado.
                 */
            }
        };

        //Se añade el Callback a la instancia del SoftKeyboard
        softKeyboard.setSoftKeyboardCallback(softKeyboardChanged);

        //Instanciación del método "onFocusChangeListener" que se asignará a ambos EditText
        View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    //Mostrar el teclado cuando se obtenga el focus del EditText
                    softKeyboard.openSoftKeyboard();
                }
            }
        };

        //Asignación del evento "onFocusChangeListener"
        nMaxPOIUsuario.setOnFocusChangeListener(onFocusChangeListener);
        radioUsuario.setOnFocusChangeListener(onFocusChangeListener);

        /* Definición de un nuevo evento "onFocusChangeListener" que simplemente se encargue de
         * esconder el teclado cuando al Layout padre obtenga el focus, es decir, cuando los EditText
         * pierdan el focus.
         */
        home_container.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    //Ocultar el teclado cuando se obtenga el focus
                    softKeyboard.closeSoftKeyboard();
                }
            }
        });

        //Bindeo del botón encargado de mostrar el mapa y asignación de un evento de tipo "onClickListener"
        Button ubicacion = (Button) vista.findViewById(com.example.adrian.monumentos.R.id.buscarPorUbicacion);
        ubicacion.setOnClickListener(this);

        return vista;
    }

    /**
     * Método que se encarga de recoger los valores de los parámetros introducidos por el usuario (si los hay),
     * de validarlos llamando al método validarDatosEntrada(int, int), de comprobar que antes de devolver el control
     * se dispone tanto de conexión a Internet como de que el GPS esté activado y, finalmente, de enviar esos datos recogidos
     * a MainActivity para que muestre el mapa correspondiente.
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
        } catch (NumberFormatException e) {
            //En caso contrario, asignamos un -1 a la variable
            nMaxPOI = -1;
        }

        try {
            //Almacenamos el valor introducido por el usuario
            radio = Double.valueOf(Double.valueOf(radioUsuario.getText().toString()) * 1000).intValue();
        } catch (NumberFormatException e) {
            //En caso contrario, asignamos un -1 a la variable
            radio = -1;
        }

        if (validarDatosEntrada(nMaxPOI, radio)) {
            //Los datos introducidos son válidos (o no se ha introducido nada)

            //Se comprueba que se disponga de conexión a Internet, así como de que el GPS esté activado
            if (mainActivity.isGPSAndInternetEnabled()) {

                //Se marca la opción "Mapa" en el menú lateral de la aplicación
                mainActivity.getNavigationView().getMenu().getItem(2).setChecked(true);

                //Los valores introducidos (o no) por el usuario son enviados a "MainActivity"
                mainActivity.setInputNMaxPOI(nMaxPOI);
                mainActivity.setInputRadioBusqueda(radio);

                //Se invoca el método encargado de mostrar el mapa
                mainActivity.mostrarInformacion("Mapa");
            }
        } else {
            //Alguno (o ambos) de los parámetros son erróneos. Se procede a mostrar un AlertDialog indicando el tipo de error
            mainActivity.showErrorDialog(tipoError);
        }
    }

    /**
     * Called when the fragment is no longer in use. This is called after onStop() and before onDetach().
     *
     * <p>Además se encarga de parar el hilo encargado de gestionar los eventos del teclado en pantalla de cara
     * a evitar problemas de memoria en la aplicación.</p>
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        softKeyboard.unRegisterSoftKeyboardCallback();
    }



    /**
     * Método encargado de validar que los datos introducidos por el usuario (si es que ha introducido alguno)
     * son válidos.
     * @param maxPOI debe estar entre 1 y 500, no se permite un valor más alto del mismo a la hora de hacer
     * consultas a la API de la WikiPedia, por lo que no se admitirán ningún valor fuera de ese rango.
     * @param radio debe estar entre 10 y 10000 (medida en metros) por el mismo motivo que el anterior parámetro. Este
     * valor es recogido en kilómetros del usuario y pasado a metros antes de ser enviado a este método
     * @return El resultado de la ejecución del mismo es un boolean de valor "true", si y sólo si, todos los parámetros introducidos por
     * el usuario están en el rango correcto (o no se ha introducido ninguno de los dos). En caso de que alguno (o todos)
     * los parámetros introducidos por el usuario no estén en el rango permitido, se devolverá "false" indicando
     * cuál es el error en la variable "tipoError".
     */
    private boolean validarDatosEntrada(int maxPOI, int radio) {

        if (maxPOI != -1) {
            //Se ha introducido un valor para nMaxPOI
            if (radio != -1) {
                //Además, se ha introducido un valor para radio
                if ((maxPOI >= 1) && (maxPOI <= 500)) {
                    //nMaxPOI está en rango valido
                    if ((radio >= 10) && (radio <= 10000)) {
                        //Y radio también está en rango válido
                        return true;
                    } else {
                        tipoError = "RADIO";
                        return false;
                    }
                } else {
                    //nMaxPOI no está en el rango permitido
                    if ((radio >= 10) && (radio <= 10000)) {
                        //Pero radio sí lo está
                        tipoError = "MAXPOI";
                        return false;
                    } else {
                        //Ni nMaxPOI ni rango se encuentran en el rango permitido
                        tipoError = "MAXPOIandRADIO";
                        return false;
                    }
                }
            } else {
                //Pero no se ha introducido un valor para radio
                if ((maxPOI >= 1) && (maxPOI <= 500)) {
                    return true;
                } else {
                    //nMaxPOI no se encuentra en el rango permitido
                    tipoError = "MAXPOI";
                    return false;
                }
            }
        } else {
            //No se ha introducido un valor para nMaxPOI
            if (radio != -1) {
                //Pero sí se ha introducido un valor para radio
                if ((radio >= 10) && (radio <= 10000)) {
                    return true;
                } else {
                    //Radio no se encuentra en el rango permitido
                    tipoError = "RADIO";
                    return false;
                }
            } else {
                //No se ha introducido ningún valor, por tanto, ambos datos son válidos
                return true;
            }
        }
    }
}
