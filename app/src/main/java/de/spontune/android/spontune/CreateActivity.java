package de.spontune.android.spontune;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import de.spontune.android.spontune.Data.Event;
import de.spontune.android.spontune.Fragments.DatePickerFragment;
import de.spontune.android.spontune.Input.FetchLocationIntentService;
import de.spontune.android.spontune.Input.PlacesAutoCompleteAdapter;
import de.spontune.android.spontune.Fragments.TimePickerFragment;

import static de.spontune.android.spontune.MapsActivity.bitmapDescriptorFromDrawable;
import static de.spontune.android.spontune.MapsActivity.getBitmap;

public class CreateActivity extends AppCompatActivity implements DatePickerFragment.PickDateDialogListener,
        TimePickerFragment.PickTimeDialogListener, OnMapReadyCallback, GoogleMap.OnMapClickListener{

    private static FirebaseDatabase mFirebaseDatabase;
    private static FirebaseAuth mFirebaseAuth;
    private static DatabaseReference mDatabaseReference;
    private static GoogleMap mMap;
    private PlacesAutoCompleteAdapter mAdapter;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private GeoDataClient mGeoDataClient;

    private static final LatLng mDefaultLocation = new LatLng(48.1500593,11.5662206);
    private static final int DEFAULT_ZOOM = 16;
    private static final LatLngBounds BOUNDS_GREATER_MUNICH = new LatLngBounds(new LatLng(48.034211, 11.281276), new LatLng(48.346808, 11.879760));

    //Properties for the created event
    private String mUserID;
    private String title;
    private String description;
    private int maxPersons = 10;
    private int currentPersons = 1;
    private int selectedCategory = 1;
    private double lat;
    private double lng;

    private boolean isStartingTime = true;
    private boolean isStartingDate = true;
    private boolean isMaxPersons = true;

    private EditText mStartingDateEdit;
    private EditText mStartingTimeEdit;
    private EditText mEndingDateEdit;
    private EditText mEndingTimeEdit;
    private EditText mMaxPersons;
    private EditText mCurrentPersons;

    //category buttons
    private ImageButton mButtonFoodAndDrink;
    private ImageButton mButtonParty;
    private ImageButton mButtonMusic;
    private ImageButton mButtonSports;

    //Calendars for the starting and ending times
    private final Calendar mStartingCalendar = GregorianCalendar.getInstance();
    private final Calendar mEndingCalendar = GregorianCalendar.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mGeoDataClient = Places.getGeoDataClient(this, null);
        setContentView(R.layout.activity_create);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getResources().getString(R.string.new_event));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Set up the FireBase reference and get the UID
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child("events");

        if(mFirebaseAuth.getCurrentUser() != null) {
            mUserID = mFirebaseAuth.getCurrentUser().getUid();
        }else{
            Toast.makeText(this, "Authentifizierungsfehler", Toast.LENGTH_SHORT).show();
            finish();
        }

        AppBarLayout appBar = findViewById(R.id.app_bar);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBar.getLayoutParams();
        AppBarLayout.Behavior behavior = new AppBarLayout.Behavior();
        behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(AppBarLayout appBarLayout) {
                return false;
            }
        });
        params.setBehavior(behavior);

        //Get a Fused Location Provider Client
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //Get the map fragment and jump to onMapReady when the map is set up
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_create);
        mapFragment.getMapAsync(this); //calls onMapReady

        //Set up the input fields and buttons
        setUpStartingButtons();
        setUpEndingButtons();
        setUpVisitorButtons();
        setUpCategoryButtons();
        setUpAcceptButton();
        setUpAddressInput();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);

        //By default, the new event is going to take place at the creator's current position
        getDeviceLocation();
    }


    /**
     * Set up the input fields for the starting time and starting date.
     */
    private void setUpStartingButtons(){
        mStartingDateEdit = findViewById(R.id.starting_date);
        mStartingTimeEdit = findViewById(R.id.starting_time);
        Date startingDate = new Date(mStartingCalendar.getTimeInMillis());

        mStartingDateEdit.setText(DateFormat.getDateFormat(getApplicationContext()).format(startingDate));
        mStartingTimeEdit.setText(DateFormat.getTimeFormat(getApplicationContext()).format(startingDate));

        mStartingDateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showStartingDateDialog();
            }
        });

        mStartingDateEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showStartingDateDialog();
                }
            }
        });

        mStartingTimeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showStartingTimeDialog();
            }
        });

        mStartingTimeEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showStartingTimeDialog();
                }
            }
        });
    }


    /**
     * Set up the input fields for the ending time and ending date.
     */
    private void setUpEndingButtons(){
        mEndingCalendar.add(Calendar.HOUR, 1);
        mEndingCalendar.add(Calendar.MINUTE, 30);
        Date endingDate = new Date(mEndingCalendar.getTimeInMillis());

        mEndingDateEdit = findViewById(R.id.ending_date);
        mEndingTimeEdit = findViewById(R.id.ending_time);
        mEndingDateEdit.setText(DateFormat.getDateFormat(getApplicationContext()).format(endingDate));
        mEndingTimeEdit.setText(DateFormat.getTimeFormat(getApplicationContext()).format(endingDate));

        mEndingDateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEndingDateDialog();
            }
        });

        mEndingDateEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showEndingDateDialog();
                }
            }
        });

        mEndingTimeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEndingTimeDialog();
            }
        });

        mEndingTimeEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showEndingTimeDialog();
                }
            }
        });
    }


    /**
     * Set up the input fields for the maximum number of visitors and the current number of visitors.
     */
    private void setUpVisitorButtons(){
        mMaxPersons = findViewById(R.id.max_persons);
        mCurrentPersons = findViewById(R.id.current_persons);
        mMaxPersons.setText(String.format(Locale.GERMAN, "%d", maxPersons));
        mCurrentPersons.setText(String.format(Locale.GERMAN, "%d", currentPersons));

        mMaxPersons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isMaxPersons = true;
                showNumberPickerDialog();
            }
        });

        mMaxPersons.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    isMaxPersons = true;
                    showNumberPickerDialog();
                }
            }
        });

        mCurrentPersons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isMaxPersons = false;
                showNumberPickerDialog();
            }
        });

        mCurrentPersons.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus) {
                    isMaxPersons = false;
                    showNumberPickerDialog();
                }
            }
        });
    }


    /**
     * Set up the buttons for selecting the category the new event should belong to.
     */
    private void setUpCategoryButtons(){
        mButtonFoodAndDrink = findViewById(R.id.create_category_food_and_drink);
        mButtonParty = findViewById(R.id.create_category_party);
        mButtonMusic = findViewById(R.id.create_category_music);
        mButtonSports = findViewById(R.id.create_category_sports);

        mButtonFoodAndDrink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedCategory = 1;
                toggleButtonsGreyed();
                updateEventMarker();
            }
        });

        mButtonParty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedCategory = 2;
                toggleButtonsGreyed();
                updateEventMarker();
            }
        });

        mButtonMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedCategory = 3;
                toggleButtonsGreyed();
                updateEventMarker();
            }
        });

        mButtonSports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedCategory = 4;
                toggleButtonsGreyed();
                updateEventMarker();
            }
        });
    }


    /**
     * Set up the button for creating the new event, writing it to the database and leaving the create view.
     * It also inspects the input, assuring that all requirements are met.
     */
    private void setUpAcceptButton(){
        ImageButton acceptButton = findViewById(R.id.create_accept);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText inputTitle = findViewById(R.id.input_title);
                final EditText inputDescription = findViewById(R.id.input_description);
                Calendar now = GregorianCalendar.getInstance();
                title = inputTitle.getText().toString();
                description = inputDescription.getText().toString();
                long startingTime = getUnixTime(mStartingCalendar);
                long endingTime = getUnixTime(mEndingCalendar);

                if(title.equals("") || title == null) {
                    Snackbar.make(view, getResources().getString(R.string.no_title_input), Snackbar.LENGTH_LONG).show();
                    inputTitle.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
                    inputTitle.requestFocus();
                    inputTitle.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            if(!charSequence.equals("")){
                                inputTitle.getBackground().setColorFilter(null);
                            }else{
                                inputTitle.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {

                        }
                    });
                }else if(description.equals("") || description == null) {
                    Snackbar.make(view, getResources().getString(R.string.no_description_input), Snackbar.LENGTH_LONG).show();
                    inputDescription.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
                    inputDescription.requestFocus();
                    inputDescription.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            if (!charSequence.equals("")) {
                                inputDescription.getBackground().setColorFilter(null);
                            } else {
                                inputDescription.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {

                        }
                    });
                }else if(startingTime <= now.getTimeInMillis()) {
                    Snackbar.make(view, getResources().getString(R.string.starting_time_over), Snackbar.LENGTH_LONG).show();
                    mStartingTimeEdit.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
                    mStartingDateEdit.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
                }else if(endingTime <= startingTime){
                    Snackbar.make(view, getResources().getString(R.string.ending_time_before_starting_time), Snackbar.LENGTH_LONG).show();
                    mEndingTimeEdit.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
                    mEndingDateEdit.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
                }else {
                    AutoCompleteTextView inputAddress = findViewById(R.id.input_address);
                    String address = inputAddress.getText().toString();
                    if (address.equals("")) {
                        address = null;
                    }

                    Event event = new Event(lat, lng, mUserID, title, description, startingTime, endingTime, selectedCategory, maxPersons, currentPersons, address);
                    String key = mDatabaseReference.push().getKey();
                    mDatabaseReference.child(key).setValue(event);
                    finish();
                }
            }
        });
    }


    /**
     * Set up the text field for the optional address
     */
    private void setUpAddressInput(){
        AutoCompleteTextView autoCompleteTextView = findViewById(R.id.input_address);
        AutocompleteFilter filter = new AutocompleteFilter.Builder().setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS).build();
        mAdapter = new PlacesAutoCompleteAdapter(this, mGeoDataClient, BOUNDS_GREATER_MUNICH, filter);
        autoCompleteTextView.setAdapter(mAdapter);
        autoCompleteTextView.setOnItemClickListener(mAutocompleteClickListener);
    }


    /**
     * Greys out category buttons based on which category is selected
     */
    private void toggleButtonsGreyed(){
        Drawable foodAndDrinkIcon = this.getResources().getDrawable(R.drawable.category_food_and_drink_dark);
        Drawable partyIcon = this.getResources().getDrawable(R.drawable.category_party_dark);
        Drawable musicIcon = this.getResources().getDrawable(R.drawable.category_music_dark);
        Drawable sportsIcon = this.getResources().getDrawable(R.drawable.category_sports_dark);
        Drawable foodAndDrinkIconDeactivated = this.getResources().getDrawable(R.drawable.category_food_and_drink_deactivated);
        Drawable partyIconDeactivated = this.getResources().getDrawable(R.drawable.category_party_deactivated);
        Drawable musicIconDeactivated = this.getResources().getDrawable(R.drawable.category_music_deactivated);
        Drawable sportsIconDeactivated = this.getResources().getDrawable(R.drawable.category_sports_deactivated);

        Drawable newIcon = (selectedCategory == 1) ? foodAndDrinkIcon : foodAndDrinkIconDeactivated;
        mButtonFoodAndDrink.setImageDrawable(newIcon);

        newIcon = (selectedCategory == 2) ? partyIcon : partyIconDeactivated;
        mButtonParty.setImageDrawable(newIcon);

        newIcon = (selectedCategory == 3) ? musicIcon : musicIconDeactivated;
        mButtonMusic.setImageDrawable(newIcon);

        newIcon = (selectedCategory == 4) ? sportsIcon : sportsIconDeactivated;
        mButtonSports.setImageDrawable(newIcon);
    }


    /**
     * Updates the event marker of the new event based on the selected location, category, and values
     * for the maximum number of visitors and the current number of visitors
     */
    private void updateEventMarker(){
        mMap.clear();
        Resources r = getResources();
        Drawable[] layers = new Drawable[3];
        Bitmap center;
        Bitmap filling;
        Bitmap border = getBitmap(this, R.drawable.event_max_border);
        Bitmap resizedFilling;
        Bitmap resizedBorder;
        LayerDrawable layerDrawable;

        switch (selectedCategory) {
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

        int borderSize = (int) Math.sqrt(Math.pow(200, 2) - Math.pow(200 - maxPersons, 2));
        int scaldedAttendance = (int) Math.sqrt(Math.pow(200, 2) - Math.pow(200 - currentPersons, 2));
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
        mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng))
                .anchor(0.5f, 0.5f)
                .icon(bitmapDescriptorFromDrawable(getApplicationContext(), layerDrawable)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), DEFAULT_ZOOM));
    }


    /**
     * Shows the dialog for selecting the starting date
     */
    private void showStartingDateDialog(){
        isStartingDate = true;
        Bundle bundle = new Bundle();
        bundle.putInt("year", mStartingCalendar.get(Calendar.YEAR));
        bundle.putInt("month", mStartingCalendar.get(Calendar.MONTH));
        bundle.putInt("day", mStartingCalendar.get(Calendar.DAY_OF_MONTH));
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.setArguments(bundle);
        newFragment.show(getSupportFragmentManager(), "startingDatePicker");
    }


    /**
     * Shows the dialog for selecting the starting time
     */
    private void showStartingTimeDialog(){
        isStartingTime = true;
        Bundle bundle = new Bundle();
        bundle.putInt("hour", mStartingCalendar.get(Calendar.HOUR_OF_DAY));
        bundle.putInt("minute", mStartingCalendar.get(Calendar.MINUTE));
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.setArguments(bundle);
        newFragment.show(getSupportFragmentManager(), "startingTimePicker");
    }


    /**
     * Shows the dialog for selecting the ending date
     */
    private void showEndingDateDialog(){
        isStartingDate = false;
        Bundle bundle = new Bundle();
        bundle.putInt("year", mEndingCalendar.get(Calendar.YEAR));
        bundle.putInt("month", mEndingCalendar.get(Calendar.MONTH));
        bundle.putInt("day", mEndingCalendar.get(Calendar.DAY_OF_MONTH));
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.setArguments(bundle);
        newFragment.show(getSupportFragmentManager(), "endingDatePicker");
    }


    /**
     * Shows the dialog for selecting the ending time
     */
    private void showEndingTimeDialog(){
        isStartingTime = false;
        Bundle bundle = new Bundle();
        bundle.putInt("hour", mEndingCalendar.get(Calendar.HOUR_OF_DAY));
        bundle.putInt("minute", mEndingCalendar.get(Calendar.MINUTE));
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.setArguments(bundle);
        newFragment.show(getSupportFragmentManager(), "endingTimePicker");
    }


    /**
     * Shows a dialog for selecting either the maximum number of visitors or the current number of visitors
     */
    private void showNumberPickerDialog(){
        View view = getLayoutInflater().inflate(R.layout.number_picker, null);
        final AlertDialog d = new AlertDialog.Builder(this).setView(view).create();

        Button numberPickerCancel = view.findViewById(R.id.number_picker_cancel);
        Button numberPickerSet = view.findViewById(R.id.number_picker_set);
        final NumberPicker np = view.findViewById(R.id.number_picker);
        assert np != null;

        if(!isMaxPersons){
            TextView title = view.findViewById(R.id.number_picker_title);
            TextView text = view.findViewById(R.id.number_picker_text);
            title.setText(R.string.current_persons_title);
            text.setText(R.string.current_persons_text);
        }

        np.setMaxValue(isMaxPersons? 200 : maxPersons - 1);
        np.setMinValue(isMaxPersons? 1 : 0);
        np.setValue(isMaxPersons? maxPersons : currentPersons);
        np.setWrapSelectorWheel(false);

        numberPickerSet.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if(isMaxPersons) {
                    maxPersons = np.getValue();
                    mMaxPersons.setText(String.format(Locale.GERMAN, "%d", maxPersons));
                }else{
                    currentPersons = np.getValue();
                    mCurrentPersons.setText(String.format(Locale.GERMAN, "%d", currentPersons));
                }
                updateEventMarker();
                d.dismiss();
            }
        });
        numberPickerCancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
    }


    /**
     * Gets called when the user has picked a starting/ending date via the respective dialog
     * and updates the respective calendar accordingly
     * @param year selected year
     * @param month selected month
     * @param day selected day
     */
    @Override
    public void onFinishPickDateDialog(int year, int month, int day){
        if(isStartingDate){
            mStartingCalendar.set(Calendar.YEAR, year);
            mStartingCalendar.set(Calendar.MONTH, month);
            mStartingCalendar.set(Calendar.DAY_OF_MONTH, day);
            mStartingDateEdit.setText(DateFormat.getDateFormat(getApplicationContext()).format(new Date(mStartingCalendar.getTimeInMillis())));
            mStartingDateEdit.getBackground().setColorFilter(null);
            mStartingTimeEdit.getBackground().setColorFilter(null);
        }else{
            mEndingCalendar.set(Calendar.YEAR, year);
            mEndingCalendar.set(Calendar.MONTH, month);
            mEndingCalendar.set(Calendar.DAY_OF_MONTH, day);
            mEndingDateEdit.setText(DateFormat.getDateFormat(getApplicationContext()).format(new Date(mEndingCalendar.getTimeInMillis())));
            mEndingDateEdit.getBackground().setColorFilter(null);
            mEndingTimeEdit.getBackground().setColorFilter(null);
        }
    }


    @Override
    public void onFinishPickTimeDialog(int hour, int minute){
        if(isStartingTime){
            mStartingCalendar.set(Calendar.HOUR_OF_DAY, hour);
            mStartingCalendar.set(Calendar.MINUTE, minute);
            mStartingTimeEdit.setText(DateFormat.getTimeFormat(getApplicationContext()).format(new Date(mStartingCalendar.getTimeInMillis())));
            mStartingDateEdit.getBackground().setColorFilter(null);
            mStartingTimeEdit.getBackground().setColorFilter(null);

        }else{
            mEndingCalendar.set(Calendar.HOUR_OF_DAY, hour);
            mEndingCalendar.set(Calendar.MINUTE, minute);
            mEndingTimeEdit.setText(DateFormat.getTimeFormat(getApplicationContext()).format(new Date(mEndingCalendar.getTimeInMillis())));
            mEndingDateEdit.getBackground().setColorFilter(null);
            mEndingTimeEdit.getBackground().setColorFilter(null);
        }
    }


    /**
     * Get the current device location
     */
    private void getDeviceLocation() {
        try {
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {

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


    private long getUnixTime(Calendar calendar){
        return calendar.getTime().getTime();
    }


    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            final AutocompletePrediction item = mAdapter.getItem(position);
            startJobIntentService(item.getFullText(null).toString());
        }
    };


    private void startJobIntentService(String address){
        Intent intent = new Intent(this, FetchLocationIntentService.class);
        LocationResultReceiver receiver = new LocationResultReceiver(new Handler());
        intent.putExtra("resultReceiver", receiver);
        intent.putExtra("address", address);
        FetchLocationIntentService.enqueueWork(this, intent);
    }


    class LocationResultReceiver extends ResultReceiver {

        public LocationResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            double lat = resultData.getDouble("lat");
            double lng = resultData.getDouble("lng");
            CreateActivity.this.lat = lat;
            CreateActivity.this.lng = lng;
            updateEventMarker();
        }
    }
}
