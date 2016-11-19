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

 */
public class Maps extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG ="<<<<<<<<<<<<<<<<<" ;
    private GoogleMap mMap;

    @Override
       protected void onCreate(Bundle savedInstanceState) {
           super.onCreate(savedInstanceState);
           setContentView(R.layout.maps);
        //mapa en un fragmento
           MapFragment mapFragment = (MapFragment) getFragmentManager()
                   .findFragmentById(R.id.map);

           mapFragment.getMapAsync(this);
       }

       @Override
       public void onMapReady(GoogleMap googleMap) {

           mMap= googleMap;

           String jSonCoords = getIntent().getStringExtra("jSonCoords");
           double e1 = getIntent().getDoubleExtra("latitudGPS", 0);
           double e2 = getIntent().getDoubleExtra("longitudGPS", 0);

           Log.e(TAG, ">>>>>>>>>>>>>>>>" + jSonCoords+e1+e2);

           if (jSonCoords != null) {

               try {
                   JSONObject jsonObj = new JSONObject(jSonCoords);

                   JSONArray geosearch = jsonObj.getJSONArray("geosearch");
    //añadir marcadores
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

                   CameraPosition cameraPosition = new CameraPosition.Builder()
                           .target(new LatLng(e1,e2))      // Sets the center of the map to location user
                           .zoom(17)                   // Sets the zoom
                        //.Address location;bearing(90)          // Sets the orientation of the camera to east
                       // .tilt(40)            // Sets the tilt of the camera to 30 degrees
                           .build();                   // Creates a CameraPosition from the builder

                   LatLng miubicacion = new LatLng(e1,e2);
                   mMap.addMarker(new MarkerOptions().position(miubicacion).title("AQUI ESTAS TU").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                   mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
           }
       }
    }



