package de.spontune.android.spontune.Fragments;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import de.spontune.android.spontune.R;

import static de.spontune.android.spontune.MapsActivity.vectorToBitmap;

public class CreateMapFragment extends Fragment implements GoogleMap.OnMapClickListener{

    private MapView mMapView;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private static final LatLng mDefaultLocation = new LatLng(48.1500593,11.5662206);
    private static final int DEFAULT_ZOOM = 16;

    public double lat;
    public double lng;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_create_map, container, false);

        mMapView = rootView.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately
        //Get a Fused Location Provider Client
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                mMap.setOnMapClickListener(CreateMapFragment.this);

                //By default, the new event is going to take place at the creator's current position
                getDeviceLocation();
            }
        });

        return rootView;
    }


    /**
     * Get the current device location
     */
    private void getDeviceLocation() {
        try {
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = task.getResult();
                        lat = mLastKnownLocation.getLatitude();
                        lng = mLastKnownLocation.getLongitude();
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                    } else {
                        lat = mDefaultLocation.latitude;
                        lng = mDefaultLocation.longitude;
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                    }
                    updateEventMarker();
                }
            });
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    @Override
    public void onMapClick(LatLng latLng) {
        lat = latLng.latitude;
        lng = latLng.longitude;
        updateEventMarker();
    }


    /**
     * Updates the event marker of the new event based on the selected location, category, and values
     * for the maximum number of visitors and the current number of visitors
     */
    private void updateEventMarker(){
        mMap.clear();
        Drawable markerDrawable = getResources().getDrawable(R.drawable.generic_marker);
        int height = 100;
        int width = 100;
        Bitmap b = vectorToBitmap( (VectorDrawable) markerDrawable);
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng))
                .anchor(0.5f, 1.0f)
                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), DEFAULT_ZOOM));
    }
}
