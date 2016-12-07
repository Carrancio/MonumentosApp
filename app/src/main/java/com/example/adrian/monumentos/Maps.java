package com.example.adrian.monumentos;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Esta Clase muestra un mapa con las posiciones de los monumentos y nuestra posicion
 * @author Adrian Munoz Rojo
 * @author Rafael Matamoros Luque
 * @author David Carrancio Aguado
 *
 */
public class Maps extends AppCompatActivity implements OnMapReadyCallback {

    private String TAG = Maps.class.getSimpleName();
    private GoogleMap mMap;

    /**
     *
     * @param savedInstanceState
     */
    @Override
       protected void onCreate(Bundle savedInstanceState) {
           super.onCreate(savedInstanceState);
           setContentView(R.layout.maps);

        //Mostramos el mapa en un fragmento

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                   .findFragmentById(R.id.map);

           mapFragment.getMapAsync(this);
       }


    /**
     * Utilizamos este metodo para trabajar con el mapa
     * Podemos añadir marcadores
     * @param googleMap
     */
       @Override
       public void onMapReady(GoogleMap googleMap) {

           mMap= googleMap;

           //Metemos en un string el json que recibe del MainActivity
           String jSonCoords = getIntent().getStringExtra("jSonCoords");

           //Metemos en un double las coordenadas del movil

           double e1 = getIntent().getDoubleExtra("latitudGPS", 0);
           double e2 = getIntent().getDoubleExtra("longitudGPS", 0);

           Log.e(TAG, "JSON: " + jSonCoords);
           Log.e(TAG, "Latitud: " + e1);
           Log.e(TAG, "Longitud: " + e2);

           if (jSonCoords != null) {

               try {
                   JSONObject jsonObj = new JSONObject(jSonCoords);

                   JSONArray geosearch = jsonObj.getJSONArray("geosearch");

                    //añadir marcadores con las coordenadas de cada monumento y su nombre

                   for (int i = 0; i < geosearch.length(); i++) {
                       JSONObject c = geosearch.getJSONObject(i);
                       String lat = c.getString("lat");
                       String lon = c.getString("lon");
                       String title = c.getString("title");
                       double lat1 = Double.parseDouble(lat);
                       double lon1 = Double.parseDouble(lon);

                       LatLng aAñadir = new LatLng(lat1, lon1);
                       mMap.addMarker(new MarkerOptions().position(aAñadir).title(title));
                       mMap.moveCamera(CameraUpdateFactory.newLatLng(aAñadir));
                   }
               } catch (final JSONException e) {
                   runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           Toast.makeText(getApplicationContext(),
                                   "Json parsing error: " + e.getMessage(),
                                   Toast.LENGTH_LONG).show();
                       }
                   });

               }

               mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(e1,e2),13));

               // Centramos el mapa a la posicion del usuario
                   CameraPosition cameraPosition = new CameraPosition.Builder()
                           .target(new LatLng(e1,e2))
                           .zoom(17)
                           .build();
                    //Añadimos marcador a nuestra posicion
                   LatLng miubicacion = new LatLng(e1,e2);
                   mMap.addMarker(new MarkerOptions().position(miubicacion).title("AQUI ESTAS TU").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                   mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
           }
       }
    }



