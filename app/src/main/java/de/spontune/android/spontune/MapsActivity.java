package de.spontune.android.spontune;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.VectorDrawable;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;


import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;

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
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.spontune.android.spontune.Data.Event;

import java.util.ArrayList;
import java.util.Arrays;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPoiClickListener, GoogleMap.OnMarkerClickListener{

    private static final String LOG_TAG = MapsActivity.class.getSimpleName();

    //Google Maps API stuff
    private static GoogleMap mMap;
    private CameraPosition mCameraPosition;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    //Set the seminary room as default location to make it look like it worked when something goes wrong ;)
    private static final LatLng mDefaultLocation = new LatLng(48.1500593,11.5662206);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    //Current location
    private static Location mLastKnownLocation;

    //Keys for storing the location and camera position
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    //Category selection button states
    private boolean mFoodAndDrinkActivated = false;
    private boolean mPartyActivated = false;
    private boolean mMusicActivated = false;
    private boolean mSportsActivated = false;

    //category buttons
    private ImageButton mButtonFoodAndDrink;
    private ImageButton mButtonParty;
    private ImageButton mButtonMusic;
    private ImageButton mButtonSports;

    //Firebase stuff
    private static DatabaseReference mEventsDatabaseReference;
    private static ChildEventListener mChildEventListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private static final int RC_SIGN_IN = 123; //Request code for the Firebase UI sign in

    //Lists of currently selected and unselected events
    private ArrayList<Event> mSelectedEvents = new ArrayList<>();
    private ArrayList<Event> mUnselectedEvents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Set up Firebase
        FirebaseApp.initializeApp(this);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mEventsDatabaseReference = firebaseDatabase.getReference().child("events");

        //Set up category buttons and action bar
        setUpActionBar();
        setUpCategoryButtons();

        //If the status of the app was saved beforehand, retrieve the saved location and camera position
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        Toast.makeText(this, "onCreate() called", Toast.LENGTH_SHORT).show();

        //Get a Fused Location Provider Client
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //Get the map fragment and jump to onMapReady when the map is set up
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this); //calls onMapReady
        setUpAuthStateListener();
    }


    /**
     * Set up the Firebase AuthStateListener for initializing the sign in if the user is currently
     * not logged in.
     */
    private void setUpAuthStateListener(){
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    onSignedInInitialize();
                    Toast.makeText(MapsActivity.this, "Hallo, " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                }else{
                    onSignedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(true)
                                    .setAvailableProviders(
                                            Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }


    /**
     * What to do when the user cancels the sign-in process.
     * As of now, he will be taken back to the main screen and the connection to the Firebase
     * database will be interrupted.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_CANCELED){
                onSignedOutCleanup();
                Toast.makeText(this, "Anmeldung abgebrochen", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Interrupts the connection to the Firebase database if the user is not signed in.
     */
    private void onSignedOutCleanup() {
        mMap.clear();
        if(mChildEventListener != null) {
            mEventsDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }


    /**
     * If the user is signed in, connect to the Firebase database and retrieve events.
     */
    private void onSignedInInitialize() {
        if(mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Event event = dataSnapshot.getValue(Event.class);
                    assert event != null;
                    event.setID(dataSnapshot.getKey());
                    mSelectedEvents.add(event);
                    updateSelectedEvents();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
        }
        mEventsDatabaseReference.addChildEventListener(mChildEventListener);
    }


    /**
     * Sets up the action bar with the list and profile buttons on top of the screen.
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

            final ImageButton buttonList = customView.findViewById(R.id.action_list);
            buttonList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MapsActivity.this, ListActivity.class);
                    startActivity(intent);
                }
            });

            final ImageButton buttonProfile = customView.findViewById(R.id.action_user_profile);
            buttonProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO create user profile activity and intent that activity with this button
                    mToast.show();
                }
            });
        }
    }


    /**
     * Sets up the logic for the category buttons on the bottom of the screen.
     * If all categories are deactivated (un-selected), the system acts like all categories are selected.
     * By default, all categories are deactivated.
     */
    private void setUpCategoryButtons(){
        Toolbar mBottomToolbar = findViewById(R.id.toolbar_bottom);
        mBottomToolbar.setPadding(0,0,0,0);
        mBottomToolbar.setContentInsetsAbsolute(0,0);

        mButtonFoodAndDrink = findViewById(R.id.action_category_food_and_drink);
        mButtonParty = findViewById(R.id.action_category_party);
        mButtonMusic = findViewById(R.id.action_category_music);
        mButtonSports = findViewById(R.id.action_category_sports);
        ImageButton buttonAdd = findViewById(R.id.add_event);

        mButtonFoodAndDrink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFoodAndDrinkActivated = !mFoodAndDrinkActivated;
                if(everyCategoryActivated()) setAllButtonsFalse();
                toggleButtonsGreyed();
                updateSelectedEvents();
            }
        });

        mButtonParty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPartyActivated = !mPartyActivated;
                if(everyCategoryActivated()) setAllButtonsFalse();
                toggleButtonsGreyed();
                updateSelectedEvents();
            }
        });

        mButtonMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMusicActivated = !mMusicActivated;
                if(everyCategoryActivated()) setAllButtonsFalse();
                toggleButtonsGreyed();
                updateSelectedEvents();
            }
        });

        mButtonSports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSportsActivated = ! mSportsActivated;
                if(everyCategoryActivated()) setAllButtonsFalse();
                toggleButtonsGreyed();
                updateSelectedEvents();
            }
        });

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MapsActivity.this, CreateActivity.class);
                i.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
            }
        });
    }


    /**
     * Saves the state of the map when the activity is exited (not halted).
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
     * When the map is ready, get the location permission, update the map UI and set the camera to the current position.
     * @param googleMap Map created by some mysterious forces
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(getLocationPermission()){
            updateLocationUI();
            getDeviceLocation();
        }
        mMap.setOnPoiClickListener(this);
        mMap.setOnMarkerClickListener(this);
    }


    /**
     * Requests the location permission from the user.
     */
    private boolean getLocationPermission(){
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            return true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return false;
        }
    }


    /**
     * Mandatory part of the run time permission request system implemented with Android 6.0 (API level 23).
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
                    setUpAuthStateListener();
                    updateLocationUI();
                    getDeviceLocation();
                }else{
                    //The app is not going to work without location permission, so we'll just kill it for now (HAS to be changed)
                    System.exit(1);
                }
            }
        }
    }


    /**
     * Update the UI of the map (actually, just enable the "My Location"-Button).
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
     * Get the current device location.
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
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d(LOG_TAG, "Current location is null. Using defaults.");
                            Log.e(LOG_TAG, "Exception: " + task.getException());
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    /**
     * Updates the lists of selected and unselected events after the user presses one of the
     * category buttons.
     */
    private void updateSelectedEvents(){
        for(int i = 0; i < mSelectedEvents.size(); i++){
            Event event = mSelectedEvents.get(i);
            int cat = event.getCategory();
            if((cat == 1 && !mFoodAndDrinkActivated
                    || cat == 2 && !mPartyActivated
                    || cat == 3 && !mMusicActivated
                    || cat == 4 && !mSportsActivated)
                    && !noCategoryActivated()){
                mUnselectedEvents.add(event);
                mSelectedEvents.remove(event);
                i--;
            }
        }

        for(int i = 0; i < mUnselectedEvents.size(); i++){
            Event event = mUnselectedEvents.get(i);
            int cat = event.getCategory();
            if(cat == 1 && mFoodAndDrinkActivated
                    || cat == 2 && mPartyActivated
                    || cat == 3 && mMusicActivated
                    || cat == 4 && mSportsActivated
                    || noCategoryActivated()){
                mUnselectedEvents.remove(event);
                mSelectedEvents.add(event);
                i--;
            }
        }
        updateEventMarkers();
    }


    /**
     * Updates the event markers on the map based on selected categories.
     */
    private void updateEventMarkers(){
        mMap.clear();
        for(Event event : mSelectedEvents){
            Resources r = getResources();
            Drawable[] layers = new Drawable[3];
            Bitmap center;
            Bitmap filling;
            Bitmap border = getBitmap(this, R.drawable.event_max_border);
            Bitmap resizedFilling;
            Bitmap resizedBorder;
            LayerDrawable layerDrawable;

            switch (event.getCategory()) {
                case 1:
                    center = getBitmap(this, R.drawable.category_food_and_drink_marker);
                    filling = getBitmap(this, R.drawable.category_food_and_drink_filling);
                    break;
                case 2:
                    center = getBitmap(this, R.drawable.category_party_marker);
                    filling = getBitmap(this, R.drawable.category_party_filling);
                    break;
                case 3:
                    center = getBitmap(this, R.drawable.category_music_marker);
                    filling = getBitmap(this, R.drawable.category_music_filling);
                    break;
                default:
                    center = getBitmap(this, R.drawable.category_sports_marker);
                    filling = getBitmap(this, R.drawable.category_sports_filling);
            }

            int borderSize = (int) Math.sqrt(Math.pow(200, 2) - Math.pow((event.getMaxPersons() - 200), 2));
            int scaldedAttendance = (int) Math.sqrt(Math.pow(borderSize, 2) - Math.pow((event.getCurrentPersons() - borderSize), 2));
            resizedFilling = Bitmap.createScaledBitmap(filling, center.getWidth() + scaldedAttendance, center.getHeight() + scaldedAttendance, true);
            resizedBorder = Bitmap.createScaledBitmap(border, center.getWidth() + borderSize, center.getHeight() + borderSize, true);

            BitmapDrawable gravityCenter = new BitmapDrawable(r, center);
            gravityCenter.setGravity(Gravity.CENTER);
            BitmapDrawable gravityFilling = new BitmapDrawable(r, resizedFilling);
            gravityFilling.setGravity(Gravity.CENTER);
            BitmapDrawable gravityBorder = new BitmapDrawable(r, resizedBorder);
            gravityBorder.setGravity(Gravity.CENTER);

            layers[0] = gravityFilling;
            layers[1] = gravityBorder;
            layers[2] = gravityCenter;

            layerDrawable = new LayerDrawable(layers);
            Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(event.getLat(), event.getLng()))
                    .anchor(0.5f, 0.5f)
                    .icon(bitmapDescriptorFromDrawable(getApplicationContext(), layerDrawable)));
            marker.setTag(event.getID());
        }
    }


    /**
     * Transforms a VectorDrawable into a Bitmap.
     * @param vectorDrawable the SVG to convert
     * @return a Bitmap
     */
    private static Bitmap vectorToBitmap(VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    protected static Bitmap getBitmap(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable instanceof BitmapDrawable) {
            return BitmapFactory.decodeResource(context.getResources(), drawableId);
        } else if (drawable instanceof VectorDrawable) {
            return vectorToBitmap((VectorDrawable) drawable);
        } else {
            throw new IllegalArgumentException("unsupported drawable type");
        }
    }


    /**
     * @param context current context.
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


    protected static BitmapDescriptor bitmapDescriptorFromDrawable(Context context, Drawable drawable){
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(new Canvas(bitmap));

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    /**
     * Greys out category buttons based on which categories are selected.
     */
    private void toggleButtonsGreyed(){
        Drawable foodAndDrinkIcon = this.getResources().getDrawable(R.drawable.category_food_and_drink);
        Drawable partyIcon = this.getResources().getDrawable(R.drawable.category_party);
        Drawable musicIcon = this.getResources().getDrawable(R.drawable.category_music);
        Drawable sportsIcon = this.getResources().getDrawable(R.drawable.category_sports);
        Drawable foodAndDrinkIconDeactivated = this.getResources().getDrawable(R.drawable.category_food_and_drink_deactivated);
        Drawable partyIconDeactivated = this.getResources().getDrawable(R.drawable.category_party_deactivated);
        Drawable musicIconDeactivated = this.getResources().getDrawable(R.drawable.category_music_deactivated);
        Drawable sportsIconDeactivated = this.getResources().getDrawable(R.drawable.category_sports_deactivated);
        if(noCategoryActivated()){
            mButtonFoodAndDrink.setImageDrawable(foodAndDrinkIcon);
            mButtonParty.setImageDrawable(partyIcon);
            mButtonMusic.setImageDrawable(musicIcon);
            mButtonSports.setImageDrawable(sportsIcon);
        }else{
            Drawable newIcon = mFoodAndDrinkActivated ? foodAndDrinkIcon : foodAndDrinkIconDeactivated;
            mButtonFoodAndDrink.setImageDrawable(newIcon);

            newIcon = mPartyActivated ? partyIcon : partyIconDeactivated;
            mButtonParty.setImageDrawable(newIcon);

            newIcon = mMusicActivated ? musicIcon : musicIconDeactivated;
            mButtonMusic.setImageDrawable(newIcon);

            newIcon = mSportsActivated ? sportsIcon : sportsIconDeactivated;
            mButtonSports.setImageDrawable(newIcon);
        }

    }


    /**
     * Handles clicks on (event-)markers on the map.
     * @param marker Clicked marker
     * @return true if click was consumed, else false
     */
    @Override
    public boolean onMarkerClick(Marker marker){
        if(marker.getTag() != null) {
            String clickedEventID = (String) marker.getTag();
            Event clickedEvent = null;
            for(Event event : mSelectedEvents){
                if(event.getID().equals(marker.getTag())){
                    clickedEvent = event;
                }
            }

            if(clickedEvent != null) {
                Intent i = new Intent(this, EventActivity.class);
                Bundle b = new Bundle();
                b.putString("id", clickedEventID);
                b.putString("creator", clickedEvent.getCreator());
                b.putDouble("lat", clickedEvent.getLat());
                b.putDouble("lng", clickedEvent.getLng());
                b.putString("summary", clickedEvent.getSummary());
                b.putString("description", clickedEvent.getDescription());
                b.putLong("startingTime", clickedEvent.getStartingTime());
                b.putLong("endingTime", clickedEvent.getEndingTime());
                b.putInt("category", clickedEvent.getCategory());
                b.putInt("maxPersons", clickedEvent.getMaxPersons());
                b.putInt("currentPersons", clickedEvent.getCurrentPersons());
                b.putString("address", clickedEvent.getAddress());
                i.putExtras(b);
                startActivity(i);
            }

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
     * Checks whether no category is selected.
     * @return true if no category is selected
     */
    private boolean noCategoryActivated(){
        return (!mFoodAndDrinkActivated && !mPartyActivated && !mMusicActivated && !mSportsActivated);
    }


    /**
     * Checks whether all categories are selected.
     * @return true if all categories are selected
     */
    private boolean everyCategoryActivated(){
        return (mFoodAndDrinkActivated && mPartyActivated && mMusicActivated && mSportsActivated);
    }


    /**
     * Un-selects all categories and thus puts category system back in idle mode.
     */
    private void setAllButtonsFalse(){
        mFoodAndDrinkActivated = false;
        mPartyActivated = false;
        mMusicActivated = false;
        mSportsActivated = false;
    }


    @Override
    protected void onPause(){
        super.onPause();
        if(mMap != null) {
            mCameraPosition = mMap.getCameraPosition();
        }
        if(mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        if(mChildEventListener != null) {
            mEventsDatabaseReference.removeEventListener(mChildEventListener);
        }
    }


    @Override
    protected void onResume(){
        super.onResume();
        mSelectedEvents = new ArrayList<>();
        if(mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        }
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

}
