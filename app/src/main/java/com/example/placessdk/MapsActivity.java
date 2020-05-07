package com.example.placessdk;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, FetchAddressTask.OnTaskCompleted {

    private GoogleMap mMap;

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private Location mLastLocation;
    private FusedLocationProviderClient mFusedLocationClient;

    public static LatLng MYLOCATION = null;
    private double longitute, latitude;

    private PlaceDetectionClient mPlaceDetectionClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Initialize the FusedLocationClient.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(
                this);

        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);
        getLocation();
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
//                                Log.d("Location Coordinates", "Lat: " + mLastLocation.getLatitude() + "<<<<<>>>>> Long:" + mLastLocation.getLongitude());
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
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title("My new location"));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        mMap.getUiSettings().setZoomControlsEnabled(true);


        // Start the reverse geocode AsyncTask
        new FetchAddressTask(MapsActivity.this,
                MapsActivity.this).execute(location);
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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        final LatLng markerLoc = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(markerLoc).title("My location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLoc, 15));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(point).title("Coordinates: " + point.longitude + ", " + point.latitude));
                Log.d("Coordinates: ", point.longitude + " <> " + point.latitude);


                Location location = new Location("");
                location.setLongitude(point.longitude);
                location.setLatitude(point.latitude);

                // Start the reverse geocode AsyncTask
                new FetchAddressTask(MapsActivity.this,
                        MapsActivity.this).execute(location);


//                Log.d("Address: ",);

            }
        });

    }

    @Override
    public void onTaskCompleted(String result) {
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
    }
}
