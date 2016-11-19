package com.example.adrian.monumentos;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class ErrorDialogFragment extends DialogFragment{

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //Parámetros indicando el tipo de Error a mostrar
        Bundle params = getArguments();
        String tipoError = params.getString("Error");

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        switch (tipoError){
            case "GPS":
                builder = builder.setMessage(getString(R.string.error_activar_GPS_1) +' ' + getString(R.string.error_activar_GPS_2));

                break;

            case "INTERNET":
                builder = builder.setMessage(getString(R.string.error_activar_Internet_1) + ' ' + getString(R.string.error_activar_Internet_2));

                break;
        }
            builder.setPositiveButton(R.string.aceptar_boton, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) { } });

        // Create the AlertDialog object and return it
        return builder.create();
    }
}
