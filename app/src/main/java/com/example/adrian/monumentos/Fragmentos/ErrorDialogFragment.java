package com.example.adrian.monumentos.Fragmentos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.example.adrian.monumentos.MainActivity;

/**
 * Esta clase consiste en un único metodo que se encarga de recibir un parámetro indicando el tipo de error
 * a mostrar y, en base a eso, construir un objeto de tipo "DialogFragment" informando al usuario del error
 * que se ha producido.
 *
 * <p>Los errores que se muestran mediante esta clase están relacionados con el GPS, conexión a Internet,
 * número máximo de POIs a mostrar y radio de la búsqueda.</p>
 *
 * <p>Esta clase forma parte de la aplicación TripApp, desarrollada para la asignatura Sistemas Móviles.</p>
 *
 * @author Adrián Muñoz Rojo
 * @author Rafael Matamoros Luque
 * @author David Carrancio Aguado
 * @see MainActivity
 * @see HomeFragment
 * @version 1.0
 */
public class ErrorDialogFragment extends DialogFragment{

    /**
     * Este método es llamado después de ejecutar "onCreate(Bundle)" y antes de "onCreateView(LayoutInflater, ViewGroup, Bundle)".
     * Sobreescribe el método original de la clase DialogFragment para recibir un parámetro vía Bundle indicando el tipo de error,
     * así como incorpora una estructura "switch-case" muy simple para definir el mensaje de error a mostrar en función del tipo de
     * error recibido.
     *
     * <p>Por último, simplemente se encarga de comprobar de que, en el caso de que el error esté relacionado con el GPS o la conexión
     * a Internet, si alguno de estos dos no está activado, no avanzar en el NavigationView del menú lateral de la aplicación</p>
     *
     * @param savedInstanceState The last saved instance state of the Fragment, or null if this is a freshly created Fragment.
     * Return a new Dialog instance to be displayed by the Fragment.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //Obtención del tipo de error a mostrar vía Bundle
        Bundle params = getArguments();
        final String tipoError = params.getString("Error");

        //Obtención del Builder que se encargará de crear el diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        assert tipoError != null;

        switch (tipoError){
            case "GPS":
                builder = builder.setMessage(getString(com.example.adrian.monumentos.R.string.error_activar_GPS_1)
                        +' ' + getString(com.example.adrian.monumentos.R.string.error_activar_GPS_2));

                break;

            case "INTERNET":
                builder = builder.setMessage(getString(com.example.adrian.monumentos.R.string.error_activar_Internet_1)
                        + ' ' + getString(com.example.adrian.monumentos.R.string.error_activar_Internet_2));

                break;

            case "MAXPOI":
                builder = builder.setMessage(com.example.adrian.monumentos.R.string.error_maxPOI);

                break;

            case "RADIO":
                builder = builder.setMessage(com.example.adrian.monumentos.R.string.error_radio);

                break;

            case "MAXPOIandRADIO":
                builder = builder.setMessage(com.example.adrian.monumentos.R.string.error_maxPOIandRadio);

                break;
        }

        //Dado que el diálogo es meramente informativo, se añade un único boton, "Aceptar"
            builder.setPositiveButton(com.example.adrian.monumentos.R.string.aceptar_boton, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Si no está activado el GPS y/o la conexión a Internet, nos quedamos donde estamos en el NavigationView
                    if((tipoError.equals("GPS")) || (tipoError.equals("INTERNET")))
                        ((MainActivity) getActivity()).marcarPrevItem();
                }
            });

        //Creación del AlertDialog y retorno del mismo
        return builder.create();
    }
}
