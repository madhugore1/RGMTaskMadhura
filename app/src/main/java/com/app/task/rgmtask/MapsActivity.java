package com.app.task.rgmtask;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {


    private GoogleMap mMap;
    CameraPosition cameraPosition;
    RequestQueue requestQueue;
    String url = "http://dev.citrans.net:8888/skymeet/poi/list";
    ArrayList<LatLng> latLngArrayList = new ArrayList<LatLng>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            // Parsing json array response
                            // loop through each json object
                            for (int i = 0; i < response.length(); i++) {

                                JSONObject poi = (JSONObject) response.get(i);

                                String status = poi.getString("status");

                                JSONArray locationArray = poi.getJSONArray("location");
                                String lat = locationArray.get(0).toString();
                                String lng = locationArray.get(1).toString();

                                if (status.equals("ACTIVE")) {
//                                    latArrayList.add(lat);
//                                    lngArrayList.add(lng);

                                    createLatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                                }

                                //tempTextView.append(status + " " + lat + " " + lng + "\n");
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
//                            Toast.makeText(getApplicationContext(),
//                                    "Error: " + e.getMessage(),
//                                    Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        requestQueue.add(jsonArrayRequest);


        cameraPosition = new CameraPosition.Builder().target(
                new LatLng(19.218330, 72.978088)).zoom(10).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    public void createLatLng(double lat, double lng) {
        LatLng location = new LatLng(lat, lng);
        MarkerOptions marker = new MarkerOptions().position(location);
        mMap.addMarker(new MarkerOptions()
                            .position(location)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_android_black_24dp)));
    }
}
