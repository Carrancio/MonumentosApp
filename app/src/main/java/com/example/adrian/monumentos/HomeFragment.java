package com.example.adrian.monumentos;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.MapFragment;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private LocationManager locationManager;

    private Bundle params;
    private Button ubicacion;

    //Constructor por defecto
    public HomeFragment(){}


    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.home_fragment, container, false);

        params = new Bundle();

        ubicacion = (Button) vista.findViewById(R.id.buscarPorUbicacion);

        ubicacion.setOnClickListener(this);

        return vista;
    }

    /**
     * Called when a fragment is first attached to its context.
     * {@link #onCreate(Bundle)} will be called after this.
     *
     * @param context
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

        locationManager = (LocationManager) getActivity().getSystemService( Context.LOCATION_SERVICE );

        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        boolean showDialog = false;

        if (networkInfo != null && networkInfo.isConnected())
            if (gpsEnabled)
                mostrarMapa();
            else{
                showDialog = true;

                params.putString("Error", "GPS");
            }
        else {
            showDialog = true;

            params.putString("Error", "INTERNET");
        }

        if(showDialog){
            DialogFragment errorDialogFragment = new ErrorDialogFragment();
            errorDialogFragment.setArguments(params);

            errorDialogFragment.show(getFragmentManager(), "ErrorDialog");
        }
    }

    private void mostrarMapa(){

        //Si no se han obtenido bien las coordenadas GPS, volver a solicitarlas
        if((((MainActivity) this.getActivity()).getLatitudGPS() == 0.0)
                && (((MainActivity) this.getActivity()).getLongitudGPS() == 0.0)){
            ((MainActivity) this.getActivity()).obtenerCoordenadasGPS();
        }

        params.putDouble("latitud", ((MainActivity) this.getActivity()).getLatitudGPS());
        params.putDouble("longitud", ((MainActivity) this.getActivity()).getLongitudGPS());

        //Creación de un nuevo Fragmento Mapa
        MapFragment mapFragment = new MapFragment();
        mapFragment.setArguments(params);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, mapFragment);
        transaction.addToBackStack("MapFragmentPOI");

        transaction.commit();
    }

}
