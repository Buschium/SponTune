package com.spontune.android.spontune;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.Image;
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
import android.widget.LinearLayout;
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

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.spontune.android.spontune.Data.Event;
import com.spontune.android.spontune.Data.DatabaseInitializer;
import com.spontune.android.spontune.Data.AppDatabase;
import com.spontune.android.spontune.Data.EventDAO;

import org.w3c.dom.Text;

import java.util.List;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPoiClickListener, GoogleMap.OnMarkerClickListener{

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

    //Database for storing events
    private static AppDatabase mAppDatabase;

    //Category selection button states
    private boolean mFoodAndDrinkActivated = false;
    private boolean mPartyActivated = false;
    private boolean mMusicActivated = false;
    private boolean mSportsActivated = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mAppDatabase = AppDatabase.getAppDatabase(this);

        //Set up category buttons and action bar
        setUpActionBar();
        setUpCategoryButtons();

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
     * Sets up the action bar with the list and profile buttons on top of the screen
     */
    private void setUpActionBar(){
        //Toast to show when the user tries to use a function that hasn't been implemented yet
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
                    //TODO create event list activity and intent that activity with this button
                    mToast.show();
                }
            });

            final ImageButton mButtonProfile = customView.findViewById(R.id.action_user_profile);
            mButtonProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO create user profile activity and intent that activity with this button
                    mToast.show();
                }
            });
        }
    }


    /**
     * Sets up the logic for the category buttons on the bottom of the screen
     * If all categories are deactivated (un-selected), the system acts like all categories are selected
     * By default, all categories are deactivated
     */
    private void setUpCategoryButtons(){
        Toolbar mBottomToolbar = findViewById(R.id.toolbar_bottom);
        mBottomToolbar.setPadding(0,0,0,0);
        mBottomToolbar.setContentInsetsAbsolute(0,0);

        final ImageButton mButtonFoodAndDrink = findViewById(R.id.action_category_food_and_drink);
        final ImageButton mButtonParty = findViewById(R.id.action_category_party);
        final ImageButton mButtonMusic = findViewById(R.id.action_category_music);
        final ImageButton mButtonSports = findViewById(R.id.action_category_sports);

        mButtonFoodAndDrink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFoodAndDrinkActivated = !mFoodAndDrinkActivated;
                if(everyCategoryActivated()) setAllButtonsFalse();
                toggleButtonsGreyed(mButtonFoodAndDrink, mButtonParty, mButtonMusic, mButtonSports);
                updateEventMarkers();
            }
        });

        mButtonParty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPartyActivated = !mPartyActivated;
                if(everyCategoryActivated()) setAllButtonsFalse();
                toggleButtonsGreyed(mButtonFoodAndDrink, mButtonParty, mButtonMusic, mButtonSports);
                updateEventMarkers();
            }
        });

        mButtonMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMusicActivated = !mMusicActivated;
                if(everyCategoryActivated()) setAllButtonsFalse();
                toggleButtonsGreyed(mButtonFoodAndDrink, mButtonParty, mButtonMusic, mButtonSports);
                updateEventMarkers();
            }
        });

        mButtonSports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSportsActivated = ! mSportsActivated;
                if(everyCategoryActivated()) setAllButtonsFalse();
                toggleButtonsGreyed(mButtonFoodAndDrink, mButtonParty, mButtonMusic, mButtonSports);
                updateEventMarkers();
            }
        });
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
        mMap.setOnMarkerClickListener(this);

        //Used to initialize the test database for now
        DatabaseInitializer.populateAsync(mAppDatabase);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int rows = mAppDatabase.eventDao().getAll().size();
        if(rows > 0){
            String msg = "Successfully initialized " + rows + " rows";
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            updateEventMarkers();
        }
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


    /**
     * Updates the event markers on the map based on selected categories and fetches new events from the database
     */
    private void updateEventMarkers(){
        EventDAO eventDAO = mAppDatabase.eventDao();
        List<Event> mEventsList = eventDAO.getAll();
        mMap.clear();
        for(Event event : mEventsList){
            if((event.category == 1 && mFoodAndDrinkActivated)
                    || (event.category == 2 && mPartyActivated)
                    || (event.category == 3 && mMusicActivated)
                    || (event.category == 4 && mSportsActivated)
                    || noCategoryActivated()) {
                int mResourceID;
                switch (event.category) {
                    case 1:
                        mResourceID = R.drawable.category_food_and_drink;
                        break;
                    case 2:
                        mResourceID = R.drawable.category_party;
                        break;
                    case 3:
                        mResourceID = R.drawable.category_music;
                        break;
                    default:
                        mResourceID = R.drawable.category_sports;
                }
                Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(event.lat, event.lng)).icon(bitmapDescriptorFromVector(getApplicationContext(), mResourceID)));
                marker.setTag(event.ID);
            }
        }
    }


    /**
     * @param context current context
     * @param vectorResId resource ID of the vector graphic
     * @return BitmapDescriptor from a locally saved vector graphic
     */
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    /**
     * @param drawable Drawable that should be converted to grey
     * @return A grey scaled image
     */
    private static Drawable convertDrawableToGrayScale(Drawable drawable) {
        if (drawable == null) return null;
        Drawable res = drawable.mutate();
        res.setColorFilter(Color.rgb(0xd3, 0xd3, 0xd3), PorterDuff.Mode.SCREEN);
        return res;
    }


    /**
     * Greys out category buttons based on which categories are selected
     * @param mFoodAndDrinkButton Button for category "Food and Drink"
     * @param mPartyButton Button for category "Party"
     * @param mMusicButton Button for category "Music"
     * @param mSportsButton Button for category "Sports"
     */
    private void toggleButtonsGreyed(ImageButton mFoodAndDrinkButton, ImageButton mPartyButton, ImageButton mMusicButton, ImageButton mSportsButton){
        Drawable foodAndDrinkIcon = this.getResources().getDrawable(R.drawable.category_food_and_drink);
        Drawable partyIcon = this.getResources().getDrawable(R.drawable.category_party);
        Drawable musicIcon = this.getResources().getDrawable(R.drawable.category_music);
        Drawable sportsIcon = this.getResources().getDrawable(R.drawable.category_sports);

        if(noCategoryActivated()){
            mFoodAndDrinkButton.setImageDrawable(foodAndDrinkIcon);
            mPartyButton.setImageDrawable(partyIcon);
            mMusicButton.setImageDrawable(musicIcon);
            mSportsButton.setImageDrawable(sportsIcon);
        }else{
            Drawable newIcon = mFoodAndDrinkActivated ? foodAndDrinkIcon : convertDrawableToGrayScale(foodAndDrinkIcon);
            mFoodAndDrinkButton.setImageDrawable(newIcon);

            newIcon = mPartyActivated ? partyIcon : convertDrawableToGrayScale(partyIcon);
            mPartyButton.setImageDrawable(newIcon);

            newIcon = mMusicActivated ? musicIcon : convertDrawableToGrayScale(musicIcon);
            mMusicButton.setImageDrawable(newIcon);

            newIcon = mSportsActivated ? sportsIcon : convertDrawableToGrayScale(sportsIcon);
            mSportsButton.setImageDrawable(newIcon);
        }

    }


    @Override
    public boolean onMarkerClick(Marker marker){
        if(marker.getTag() != null) {
            EventDAO eventDAO = mAppDatabase.eventDao();
            int clickedEventID = (Integer) marker.getTag();
            Event clickedEvent = eventDAO.getEventByID(clickedEventID);
            final LinearLayout overlay = findViewById(R.id.overlay);
            overlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    overlay.setVisibility(View.GONE);
                }
            });

            TextView overlayTitle = findViewById(R.id.overlay_title);
            overlayTitle.setText(clickedEvent.summary);

            TextView overlayDescription = findViewById(R.id.overlay_description);
            overlayDescription.setText(clickedEvent.description);

            overlay.setVisibility(View.VISIBLE);

            return true;
        }
        return false;
    }

    /**
     * Handles clicks on Points of Interest (POIs) on the map like stores, restaurants etc.
     * @param poi Clicked POI
     */
    @Override
    public void onPoiClick(PointOfInterest poi) {
        //TODO
        Toast.makeText(getApplicationContext(), "Keine Sorge, das kommt auch noch", Toast.LENGTH_LONG).show();
    }


    /**
     * Checks whether no category is selected
     * @return true if no category is selected
     */
    private boolean noCategoryActivated(){
        return (!mFoodAndDrinkActivated && !mPartyActivated && !mMusicActivated && !mSportsActivated);
    }


    /**
     * Checks whether all categories are selected
     * @return true if all categories are selected
     */
    private boolean everyCategoryActivated(){
        return (mFoodAndDrinkActivated && mPartyActivated && mMusicActivated && mSportsActivated);
    }


    /**
     * Un-selects all categories and thus puts category system back in idle mode
     */
    private void setAllButtonsFalse(){
        mFoodAndDrinkActivated = false;
        mPartyActivated = false;
        mMusicActivated = false;
        mSportsActivated = false;
    }


    /**
     * Deletes the example database when the app is halted (not exited)
     */
    @Override
    protected void onDestroy() {
        for(Event event : mAppDatabase.eventDao().getAll()){
            mAppDatabase.eventDao().deleteEvent(event);
        }
        AppDatabase.destroyInstance();
        super.onDestroy();
    }

}
