package de.spontune.android.spontune;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.ChangeTransform;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
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
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import de.spontune.android.spontune.Adapters.CustomInfoWindowAdapter;
import de.spontune.android.spontune.Data.Event;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;


@SuppressWarnings("FeatureEnvy")
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPoiClickListener{

    private static final String LOG_TAG = MapsActivity.class.getSimpleName();

    //Google Maps API stuff
    private static GoogleMap mMap;
    private CameraPosition mCameraPosition;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    //Set the seminary room as default location to make it look like it worked when something goes wrong ;)
    private static final LatLng mDefaultLocation = new LatLng(48.1500593,11.5662206);
    private static final int DEFAULT_ZOOM = 12;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    //Current location
    private static Location mLastKnownLocation;

    //Keys for storing the location and camera position
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    //Category selection button states
    private boolean mCreativeActivated = false;
    private boolean mPartyActivated = false;
    private boolean mHappeningActivated = false;
    private boolean mSportsActivated = false;

    private ImageButton mButtonCreative;
    private ImageButton mButtonParty;
    private ImageButton mButtonHappening;
    private ImageButton mButtonSports;
    private ImageButton mButtonAdd;

    //Firebase stuff
    private static DatabaseReference mEventsDatabaseReference;
    private static ChildEventListener mChildEventListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private static final int RC_SIGN_IN = 123; //Request code for the Firebase UI sign in

    //Milliseconds for securing that only events which start today are shown on the map
    private long mNowMillis;
    private long mEndOfDayMillis;

    //Lists of currently selected and unselected events
    private List<Event> mSelectedEvents = new ArrayList<>();
    private List<Event> mUnselectedEvents = new ArrayList<>();

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

        //Set up the Calendar for now and the end of the day and set up the milliseconds
        Calendar now = GregorianCalendar.getInstance();
        Calendar endOfDay = GregorianCalendar.getInstance();
        endOfDay.set(Calendar.HOUR_OF_DAY, 23);
        endOfDay.set(Calendar.MINUTE, 59);
        endOfDay.set(Calendar.SECOND, 59);
        mNowMillis = now.getTimeInMillis();
        mEndOfDayMillis = endOfDay.getTimeInMillis();



        //If the status of the app was saved beforehand, retrieve the saved location and camera position
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        //Get a Fused Location Provider Client for the map
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //Get the map fragment and jump to onMapReady when the map is set up
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this); //calls onMapReady
        setUpAuthStateListener();
    }


    /**
     * Set up the Firebase AuthStateListener for initializing the sign in if the user is currently
     * not logged in.
     * May be used once the user is able to log out.
     */
    private void setUpAuthStateListener(){
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    onSignedInInitialize();
                }else{
                    onSignedOutCleanup();
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
                    final Event event = dataSnapshot.getValue(Event.class);
                    assert event != null;
                    //If the event starts today and hasn't ended yet, it will be displayed on the map
                    //TODO: maybe find a more efficient way that doesn't require to load all the events
                    if(event.getEndingTime() >= mNowMillis && event.getStartingTime() <= mEndOfDayMillis) {
                        event.setID(dataSnapshot.getKey());
                        loadImage(event);
                        mSelectedEvents.add(event);
                        updateSelectedEvents();
                    }
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
     * The layout file for the action bar is activity_maps_menu.
     */
    private void setUpActionBar(){
        ActionBar mActionBar = getSupportActionBar();
        if(mActionBar != null) {
            mActionBar.setDisplayShowHomeEnabled(false); //Gets rid of the "back"-button in the action bar
            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setDisplayShowTitleEnabled(false);
            LayoutInflater mLayoutInflater = LayoutInflater.from(this);
            View customView = mLayoutInflater.inflate(R.layout.activity_maps_menu, null);
            mActionBar.setCustomView(customView,new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mActionBar.setDisplayShowCustomEnabled(true);

            Toolbar mParent = (Toolbar) customView.getParent();
            mParent.setPadding(0,0,0,0);
            mParent.setContentInsetsAbsolute(0,0);

            //Takes the user to the list activity when the "list"-button is pressed
            final ImageButton buttonList = customView.findViewById(R.id.action_list);
            buttonList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MapsActivity.this, ListActivity.class);
                    Slide slide = new Slide();
                    slide.setSlideEdge(Gravity.START);
                    getWindow().setExitTransition(slide);
                    slide.setSlideEdge(Gravity.END);
                    getWindow().setEnterTransition(slide);
                    getWindow().setSharedElementEnterTransition(new ChangeTransform());
                    getWindow().setSharedElementEnterTransition(new ChangeTransform());
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MapsActivity.this, mButtonAdd, "add_event");
                    startActivity(intent, options.toBundle());
                }
            });

            final ImageButton buttonProfile = customView.findViewById(R.id.action_user_profile);
            buttonProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MapsActivity.this, UserActivity.class);
                    Slide slide = new Slide();
                    slide.setSlideEdge(Gravity.END);
                    getWindow().setExitTransition(slide);
                    slide.setSlideEdge(Gravity.START);
                    getWindow().setEnterTransition(slide);
                    intent.putExtra("uid", mFirebaseAuth.getUid());
                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(MapsActivity.this).toBundle());
                }
            });

        }
    }


    /**
     * Sets up the logic for the category buttons on the bottom of the screen.
     * If all categories are deactivated (un-selected), the system acts like all categories are selected (for logic purposes).
     * By default, all categories are deactivated.
     */
    private void setUpCategoryButtons(){

        mButtonCreative = findViewById(R.id.action_category_food_and_drink);
        mButtonParty = findViewById(R.id.action_category_party);
        mButtonHappening = findViewById(R.id.action_category_music);
        mButtonSports = findViewById(R.id.action_category_sports);
        mButtonAdd = findViewById(R.id.add_event);

        mButtonCreative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCreativeActivated = !mCreativeActivated;
                /*If all categories are selected after the button is pressed, all categories have to be set
                 *to false to keep the logic consistent.
                 */
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

        mButtonHappening.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHappeningActivated = !mHappeningActivated;
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

        mButtonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWindow().setEnterTransition(new Slide());
                getWindow().setExitTransition(new Slide());
                Intent intent = new Intent(MapsActivity.this, CreateEventActivity.class);
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(MapsActivity.this).toBundle());
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
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if(marker.getTag() != null) {
                    Event clickedEvent = (Event) marker.getTag();

                    if(clickedEvent != null) {
                        Intent i = new Intent(MapsActivity.this, EventActivity.class);
                        Bundle b = new Bundle();
                        b.putString("id", clickedEvent.getID());
                        b.putString("uid", mFirebaseAuth.getCurrentUser().getUid());
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
                        i.putExtra("participants", clickedEvent.getParticipants());
                        startActivity(i);
                    }
                }
            }
        });
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(this));
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener(){
            @Override
            public boolean onMarkerClick(Marker marker){
                marker.showInfoWindow();
                Projection projection = mMap.getProjection();
                LatLng markerPosition = marker.getPosition();
                Point markerPoint = projection.toScreenLocation(markerPosition);
                Point targetPoint = new Point(markerPoint.x, markerPoint.y - 100);
                LatLng targetPosition = projection.fromScreenLocation(targetPoint);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(targetPosition), 300, null);
                return true;
            }
        });
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
     * Update the UI of the map (actually, just enable the "go to my location"-button, but the method has to be called like this).
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
            if((cat == 1 && !mCreativeActivated
                    || cat == 2 && !mPartyActivated
                    || cat == 3 && !mHappeningActivated
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
            if(cat == 1 && mCreativeActivated
                    || cat == 2 && mPartyActivated
                    || cat == 3 && mHappeningActivated
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
     * Updates the event markers on the map based on selected categories and the list of events in these categories.
     */
    private void updateEventMarkers(){
        mMap.clear();
        for(Event event : mSelectedEvents){
            Drawable markerDrawable;

            switch (event.getCategory()) {
                case 1:
                    markerDrawable = getResources().getDrawable(R.drawable.category_creative_marker);
                    break;
                case 2:
                    markerDrawable = getResources().getDrawable(R.drawable.category_party_marker);
                    break;
                case 3:
                    markerDrawable = getResources().getDrawable(R.drawable.category_happening_marker);
                    break;
                default:
                    markerDrawable = getResources().getDrawable(R.drawable.category_sports_marker);
            }

            int height = 100;
            int width = 100;
            Bitmap b= vectorToBitmap( (VectorDrawable) markerDrawable);
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

            Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(event.getLat(), event.getLng()))
                    .anchor(0.5f, 1.0f)
                    .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
            marker.setTag(event);
        }
    }


    /**
     * Transforms a VectorDrawable into a Bitmap.
     * @param vectorDrawable the SVG to convert
     * @return a Bitmap
     */
    public static Bitmap vectorToBitmap(VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }


    /**
     * Greys out category buttons based on which categories are selected.
     */
    private void toggleButtonsGreyed(){
        Drawable foodAndDrinkIcon = this.getResources().getDrawable(R.drawable.category_creative_light);
        Drawable partyIcon = this.getResources().getDrawable(R.drawable.category_party_light);
        Drawable musicIcon = this.getResources().getDrawable(R.drawable.category_happening_light);
        Drawable sportsIcon = this.getResources().getDrawable(R.drawable.category_sports_light);
        Drawable foodAndDrinkIconDeactivated = this.getResources().getDrawable(R.drawable.category_creative_deactivated);
        Drawable partyIconDeactivated = this.getResources().getDrawable(R.drawable.category_party_deactivated);
        Drawable musicIconDeactivated = this.getResources().getDrawable(R.drawable.category_happening_deactivated);
        Drawable sportsIconDeactivated = this.getResources().getDrawable(R.drawable.category_sports_deactivated);
        if(noCategoryActivated()){
            mButtonCreative.setImageDrawable(foodAndDrinkIcon);
            mButtonParty.setImageDrawable(partyIcon);
            mButtonHappening.setImageDrawable(musicIcon);
            mButtonSports.setImageDrawable(sportsIcon);
        }else{
            Drawable newIcon = mCreativeActivated ? foodAndDrinkIcon : foodAndDrinkIconDeactivated;
            mButtonCreative.setImageDrawable(newIcon);

            newIcon = mPartyActivated ? partyIcon : partyIconDeactivated;
            mButtonParty.setImageDrawable(newIcon);

            newIcon = mHappeningActivated ? musicIcon : musicIconDeactivated;
            mButtonHappening.setImageDrawable(newIcon);

            newIcon = mSportsActivated ? sportsIcon : sportsIconDeactivated;
            mButtonSports.setImageDrawable(newIcon);
        }

    }


    /**
     * Handles clicks on Points of Interest (POIs) on the map like stores, restaurants etc.
     * @param poi Clicked POI
     */
    @Override
    public void onPoiClick(PointOfInterest poi) {
        //TODO handle clicks on POIs
        Toast.makeText(getApplicationContext(), "Keine Sorge, das kommt auch noch", Toast.LENGTH_LONG).show();
    }


    /**
     * Checks whether no category is selected.
     * @return true if no category is selected
     */
    private boolean noCategoryActivated(){
        return (!mCreativeActivated && !mPartyActivated && !mHappeningActivated && !mSportsActivated);
    }


    /**
     * Checks whether all categories are selected.
     * @return true if all categories are selected
     */
    private boolean everyCategoryActivated(){
        return (mCreativeActivated && mPartyActivated && mHappeningActivated && mSportsActivated);
    }


    /**
     * Un-selects all categories and thus puts category system back in idle mode.
     */
    private void setAllButtonsFalse(){
        mCreativeActivated = false;
        mPartyActivated = false;
        mHappeningActivated = false;
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

    private void loadImage(final Event event){
        String categoryName;
        switch(event.getCategory()){
            case 1: categoryName = "creative"; break;
            case 2: categoryName = "party"; break;
            case 3: categoryName = "happening"; break;
            default: categoryName = "sports";
        }
        Glide.with(this)
                .asBitmap()
                .load(FirebaseStorage.getInstance().getReference().child("categoryImages/" + event.getID()))
                .error(Glide.with(this).asBitmap().load(FirebaseStorage.getInstance().getReference().child("categoryImages/" + categoryName + ".jpg")))
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition){
                        event.setPicture(resource);
                    }
                });
    }

}
