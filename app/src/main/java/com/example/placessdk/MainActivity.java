package com.example.placessdk;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.*;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, FetchAddressTask.OnTaskCompleted {


    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private SearchView searchView;

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private Location mLastLocation;
    private FusedLocationProviderClient mFusedLocationClient;

    public static LatLng MYLOCATION = null;
    private double longitute, latitude;

    private PlaceDetectionClient mPlaceDetectionClient;

    private View mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        searchView = findViewById(R.id.sv_location);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Initialize the FusedLocationClient.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(
                this);

        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);
        mapView = mapFragment.getView();

        getLocation();

        // Initialize Places.
        com.google.android.libraries.places.api.Places.initialize(getApplicationContext(), "AIzaSyDvZwvZIqj8osv9MLIhssIzuUqacU8Bvr0");

        // Create a new Places client instance.
        PlacesClient placesClient = com.google.android.libraries.places.api.Places.createClient(this);

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // TODO: Get info about the selected place.
                Log.i("<>", "Place: " + place.getName() + ", " + place.getId());

                String location = place.getName();
                Log.d("<>", location);

                List<Address> addressList = null;
                Geocoder geocoder = new Geocoder(MainActivity.this);
                try {
                    addressList = geocoder.getFromLocationName(location, 1);
                    if (geocoder.getFromLocationName(location, 1).isEmpty()) {
                        Log.d("<>", "No json object returned, try other search");
                        Toast.makeText(MainActivity.this, "No json object returned, try other search", Toast.LENGTH_SHORT).show();
                    } else {
                        Address address = addressList.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                        Log.d("<>", String.valueOf(latLng));

//                    final LatLng markerLoc = new LatLng(-34, 151);
                        map.clear();
                        map.addMarker(new MarkerOptions().position(latLng).title(location));
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                    }


                } catch (NullPointerException ex) {
                    ex.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                Log.i("<>", "An error occurred: " + status);
            }
        });


//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                Log.d("<>", query);
//                String location = searchView.getQuery().toString();
//                Log.d("<>", location);
//                List<Address> addressList = null;
//
//                if (location != null || !location.equals("")) {
//                    Geocoder geocoder = new Geocoder(MainActivity.this);
//                    try {
//                        addressList = geocoder.getFromLocationName(location, 1);
//                        if (geocoder.getFromLocationName(location, 1).isEmpty()) {
//                            Log.d("<>", "No json object returned, try other search");
//                            Toast.makeText(MainActivity.this, "No json object returned, try other search", Toast.LENGTH_SHORT).show();
//                        } else {
//                            Address address = addressList.get(0);
//                            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
//                            Log.d("<>", String.valueOf(latLng));
//
////                    final LatLng markerLoc = new LatLng(-34, 151);
//                            map.clear();
//                            map.addMarker(new MarkerOptions().position(latLng).title(location));
//                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
//                        }
//
//
//                    } catch (NullPointerException ex) {
//                        ex.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                return false;
//            }
//        });
        mapFragment.getMapAsync(this);
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);

        } else {

            mFusedLocationClient.getLastLocation().addOnSuccessListener(
                    new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {

                                mLastLocation = location;
                                longitute = mLastLocation.getLongitude();
                                latitude = mLastLocation.getLatitude();
                                moveMap(location);
                                Log.d("Location Coordinates", "Lat: " + mLastLocation.getLatitude() + "<<<<<>>>>> Long:" + mLastLocation.getLongitude());
//                                mLocationTextView.setText(
//                                        getString(R.string.location_text,
//                                                mLastLocation.getLatitude(),
//                                                mLastLocation.getLongitude(),
//                                                mLastLocation.getTime()));
                            } else {
//                                mLocationTextView.setText(R.string.no_location);
                            }
                        }

                    }
            );
        }
    }

    private void moveMap(Location location) {
        LatLng latLng = new LatLng(latitude, longitute);
        map.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title("My new location"));

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        map.getUiSettings().setZoomControlsEnabled(true);

        if (mapView != null &&
                mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on left bottom
            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);

            rlp.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
            rlp.addRule(RelativeLayout.ALIGN_END, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            rlp.setMargins(30, 0, 0, 40);
        }

        // Start the reverse geocode AsyncTask
        new FetchAddressTask(MainActivity.this,
                MainActivity.this).execute(location);
//        Toast.makeText(this, String.valueOf(location.getLatitude())+", "+String.valueOf(location.getLongitude()), Toast.LENGTH_SHORT).show();
//        Toast.makeText(this, String.valueOf(location), Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                // If the permission is granted, get the location,
                // otherwise, show a Toast
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    Toast.makeText(this,
                            R.string.location_permission_denied,
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void getTemp(LatLng point) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + point.latitude + "&lon=" + point.longitude + "&appid=91c0b02fe6caf490726de810d51c39bf&units=metric";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
//                                textView.setText("Response: " + response.toString());
                        Log.d("<>", "Response: " + response.toString());

                        try {
                            Double temperature;
                            Double feelsLike;


                            JSONObject main = response.getJSONObject("main");
                            temperature = main.getDouble("temp");
                            feelsLike = main.getDouble("feels_like");

//                                    Double pressure = main.getDouble("pressure");
//                                    textView.setText("Temp: " + temprature + "\nFeels like: " + feelsLike+ "\nPressure: "+pressure);
//                                    Log.d("<>","Temp: " + temprature + "Feels like: " + feelsLike+ " Pressure: "+pressure);
//                                    JSONArray weather = response.getJSONArray("weather");
//                                    for(int i=0; i<weather.length(); i++){
//                                        JSONObject object = weather.getJSONObject(i);
//                                        String description = object.getString("description");
//                                        textView.setText(textView.getText()+"\nDescription: "+description );
//                                        Log.d("<>","Description: "+description);
//                                    }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error

                    }
                });
        // Access the RequestQueue through your singleton class.
        queue.add(jsonObjectRequest);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        final LatLng markerLoc = new LatLng(-34, 151);
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);


        googleMap.addMarker(new MarkerOptions().position(markerLoc).title("Default Location"));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerLoc, 10));
        map = googleMap;


        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng point) {


                // Instantiate the RequestQueue.
                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + point.latitude + "&lon=" + point.longitude + "&appid=91c0b02fe6caf490726de810d51c39bf&units=metric";

//                map.clear();
//                map.addMarker(new MarkerOptions().position(point).title("Coordinates: " + point.longitude + ", " + point.latitude));
//                Log.d("Coordinates: ", point.longitude + " <> " + point.latitude);


                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                        (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
//                                textView.setText("Response: " + response.toString());
                                Log.d("<>", "Response: " + response.toString());

                                try {
                                    Double temperature;
                                    Double feelsLike;

                                    JSONObject main = response.getJSONObject("main");
                                    temperature = main.getDouble("temp");
                                    feelsLike = main.getDouble("feels_like");

                                    map.clear();
                                    map.addMarker(new MarkerOptions().position(point).title("Temp: " + Math.round(temperature) + "  Feels Like: " + Math.round(feelsLike)));

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // TODO: Handle error

                            }
                        });
                // Access the RequestQueue through your singleton class.
                queue.add(jsonObjectRequest);

//                map.clear();
//                map.addMarker(new MarkerOptions().position(point).title("Coordinates: " + point.longitude + ", " + point.latitude));
//                Log.d("Coordinates: ", point.longitude + " <> " + point.latitude);

                Location location = new Location("");
                location.setLongitude(point.longitude);
                location.setLatitude(point.latitude);

                // Start the reverse geocode AsyncTask
                new FetchAddressTask(MainActivity.this,
                        MainActivity.this).execute(location);

            }
        });
    }

    @Override
    public void onTaskCompleted(String result) {
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
    }
}
