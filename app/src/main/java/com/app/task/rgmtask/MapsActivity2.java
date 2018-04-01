package com.app.task.rgmtask;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity2 extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    RequestQueue requestQueue;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleApiClient googleApiClient;
    private Location mLastKnownLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    double lat, lng;
    CameraPosition cameraPosition;
    LocationRequest locationRequest;
    private boolean mLocationPermissionGranted = false;
    private EditText searchEditText, nameEditText;
    private Button btnSearch, btnGetCurrentLoc;
    private Button btnAddPOI;
    private StringRequest stringRequest;
    String url = "http://dev.citrans.net:8888/skymeet/poi/add";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        requestQueue = Volley.newRequestQueue(this);

        getLocationPermission();

        // Get the current location of the device and set the position of the map.
        if (mLocationPermissionGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                googleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
                googleApiClient.connect();

                return;
            }
            mMap.setMyLocationEnabled(true);
        }

        btnAddPOI = (Button)findViewById(R.id.add_poi);
        nameEditText = (EditText)findViewById(R.id.name_poi);

        btnAddPOI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = nameEditText.getText().toString();

                if(!name.isEmpty()) {

                    try {
                        JSONObject jsonObject = new JSONObject();
                        JSONArray location = new JSONArray();
                        location.put(lat);
                        location.put(lng);

                        jsonObject.put("id", "5abd46d9dba76d1cfd6b6549d");
                        jsonObject.put("version", 1);
                        jsonObject.put("status", "ACTIVE");
                        jsonObject.put("createdAt", null);
                        jsonObject.put("createdBy", null);
                        jsonObject.put("updatedAt", null);
                        jsonObject.put("updatedBy", null);
                        jsonObject.put("poiId", null);
                        jsonObject.put("title", name);
                        jsonObject.put("description", null);
                        jsonObject.put("imageUrl", null);
                        jsonObject.put("location", (Object)location);


                        final String requestBody = jsonObject.toString();

                        stringRequest = new StringRequest(Request.Method.POST, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        Log.i("VOLLEY", response);
                                        Toast.makeText(MapsActivity2.this,
                                                "POI successfully added", Toast.LENGTH_LONG).show();
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.e("VOLLEY", error.toString());
                                    }
                                }) {
                            @Override
                            public String getBodyContentType() {
                                return "application/json";
                            }

                            @Override
                            public byte[] getBody() {
                                return requestBody.getBytes();
                            }

                            @Override
                            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                                String responseString = "";
                                if (response != null) {
                                    responseString = String.valueOf(response.statusCode);
                                    // can get more details such as response.headers
                                }
                                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                            }

                            /**
                             * Passing some request headers
                             */
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                HashMap<String, String> headers = new HashMap<String, String>();
                                headers.put("Content-Type", "application/json");
                                return headers;
                            }
                        };
                        requestQueue.add(stringRequest);
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            }
        });



    }

    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getDeviceLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (mLocationPermissionGranted) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Task location = fusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Log.v("Maps2", "Success in getting location");
                        Location currentLocation = (Location)task.getResult();
                        lat = currentLocation.getLatitude();
                        lng = currentLocation.getLongitude();
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                15));
                    }
                    else {
                        Log.v("Maps2", "Current location is null");
                    }

                }
            });
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        Log.v("Maps2", "I was called");
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {
                    for(int i = 0; i < grantResults.length; i++) {
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    getDeviceLocation();
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mMap.setMyLocationEnabled(true);
                }
            }
        }

    }


    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setPriority(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
    }

    @Override
    public void onConnectionSuspended(int i) {


    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
