package com.spontune.android.spontune;

import android.os.Bundle;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPoiClickListener{

    private final String LOG_TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap;
    private CameraPosition mCameraPosition;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    //Set the seminary room as default location to make it look like it worked when something goes wrong
    private final LatLng mDefaultLocation = new LatLng(48.1500593,11.5662206);
    private static final int DEFAULT_ZOOM = 12;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    //Current location
    private Location mLastKnownLocation;

    //Keys for storing the location and camera position
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        final Toast mToast = Toast.makeText(getApplicationContext(), "Sorry, daran wird noch gebaut", Toast.LENGTH_SHORT);

        ActionBar mActionBar = getSupportActionBar();
        if(mActionBar != null) {
            mActionBar.setDisplayShowHomeEnabled(false);
            mActionBar.setDisplayShowTitleEnabled(false);
            LayoutInflater mLayoutInflater = LayoutInflater.from(this);
            View customView = mLayoutInflater.inflate(R.layout.activity_maps_menu, null);
            mActionBar.setCustomView(customView);
            mActionBar.setDisplayShowCustomEnabled(true);

            Toolbar mParent =(Toolbar) customView.getParent();
            mParent.setPadding(0,0,0,0);
            mParent.setContentInsetsAbsolute(0,0);

            final ImageButton mButtonList = customView.findViewById(R.id.action_list);
            mButtonList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO
                    mToast.show();
                }
            });

            final ImageButton mButtonProfile = customView.findViewById(R.id.action_user_profile);
            mButtonProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO
                    mToast.show();
                }
            });
        }

        //If the status of the app was saved beforehand, retrieve the saved location and camera position
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        //Get a Fused Location Provider Client
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //Get the map fragment and jump to onMapReady when the map is set up
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this); //calls onMapReady
    }


    /**
     * Saves the state of the map when the activity is exited (not halted)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }


    /**
     * When the map is ready, get the location permission, update the map UI and set the camera to the current position
     * @param googleMap Map created by some mysterious forces
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();
        mMap.setOnPoiClickListener(this);
    }


    /**
     * Requests the location permission from the user
     */
    private void getLocationPermission(){
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }


    /**
     * Mandatory part of the run time permission request system implemented with Android 6.0 (API level 23)
     * @param requestCode code of the request sent. Always PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION for now
     * @param permissions permissions requested
     * @param grantResults permissions granted
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            // Only request for now
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is denied, the result arrays are empty
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }else{
                    //The app is not going to work without location permission, so we'll just kill it for now (HAS to be changed)
                    //TODO: replacement for the brute force murder of the app
                    System.exit(0);
                }
            }
        }
        updateLocationUI();
    }


    /**
     * Update the UI of the map (actually, just enable the "My Location"-Button)
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                //This code is not reachable right now as the app closes when the permission is denied
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    /**
     * Get the current device location
     */
    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {

                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d(LOG_TAG, "Current location is null. Using defaults.");
                            Log.e(LOG_TAG, "Exception: " + task.getException());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public void onPoiClick(PointOfInterest poi) {
        Toast.makeText(getApplicationContext(), "Keine Sorge, das kommt auch noch", Toast.LENGTH_LONG).show();
    }

}
