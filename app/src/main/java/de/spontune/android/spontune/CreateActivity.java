package de.spontune.android.spontune;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.spontune.android.spontune.R;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import de.spontune.android.spontune.Data.Event;
import de.spontune.android.spontune.Input.DatePickerFragment;
import de.spontune.android.spontune.Input.TimePickerFragment;

import static de.spontune.android.spontune.MapsActivity.bitmapDescriptorFromDrawable;
import static de.spontune.android.spontune.MapsActivity.getBitmap;

public class CreateActivity extends AppCompatActivity implements DatePickerFragment.PickDateDialogListener,
        TimePickerFragment.PickTimeDialogListener, OnMapReadyCallback, GoogleMap.OnMapClickListener{

    private static FirebaseDatabase mFirebaseDatabase;
    private static FirebaseAuth mFirebaseAuth;
    private static DatabaseReference mDatabaseReference;
    private static GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private static final LatLng mDefaultLocation = new LatLng(48.1500593,11.5662206);
    private static final int DEFAULT_ZOOM = 16;


    private String mUserID;
    private String title;
    private String description;
    private String startingTime;
    private String startingDate;
    private String endingTime;
    private String endingDate;
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
    private ScrollView mScrollView;

    //category buttons
    private ImageButton mButtonFoodAndDrink;
    private ImageButton mButtonParty;
    private ImageButton mButtonMusic;
    private ImageButton mButtonSports;

    private final Calendar mStartingCalendar = GregorianCalendar.getInstance();
    private final Calendar mEndingCalendar = GregorianCalendar.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Neues Event");
        }
        mScrollView = findViewById(R.id.scroll_view_create);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child("events");

        if(mFirebaseAuth.getCurrentUser() != null) {
            mUserID = mFirebaseAuth.getCurrentUser().getUid();
        }else{
            Toast.makeText(this, "Authentifizierungsfehler", Toast.LENGTH_SHORT).show();
            finish();
        }

        //Get a Fused Location Provider Client
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //Get the map fragment and jump to onMapReady when the map is set up
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_create);
        mapFragment.getMapAsync(this); //calls onMapReady

        setUpStartingButtons();
        setUpEndingButtons();
        setUpVisitorButtons();
        setUpCategoryButtons();
        setUpAcceptButton();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
        getDeviceLocation();
        if(mLastKnownLocation != null) {
            lat = mLastKnownLocation.getLatitude();
            lng = mLastKnownLocation.getLongitude();
        }else{
            lat = mDefaultLocation.latitude;
            lng = mDefaultLocation.longitude;
        }
        updateEventMarker();
    }


    private void setUpStartingButtons(){
        mStartingDateEdit = findViewById(R.id.starting_date);
        mStartingTimeEdit = findViewById(R.id.starting_time);

        mStartingDateEdit.setText(String.format(Locale.GERMAN, "%02d. %02d. %4d",
                mStartingCalendar.get(Calendar.DAY_OF_MONTH),
                mStartingCalendar.get(Calendar.MONTH) + 1,
                mStartingCalendar.get(Calendar.YEAR)));

        mStartingTimeEdit.setText(String.format(Locale.GERMAN, "%02d:%02d Uhr",
                mStartingCalendar.get(Calendar.HOUR_OF_DAY),
                mStartingCalendar.get(Calendar.MINUTE)));

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


    private void setUpEndingButtons(){
        mEndingCalendar.add(Calendar.HOUR, 1);
        mEndingCalendar.add(Calendar.MINUTE, 30);

        mEndingDateEdit = findViewById(R.id.ending_date);
        mEndingTimeEdit = findViewById(R.id.ending_time);

        mEndingDateEdit.setText(String.format(Locale.GERMAN, "%02d. %02d. %4d",
                mEndingCalendar.get(Calendar.DAY_OF_MONTH),
                mEndingCalendar.get(Calendar.MONTH) + 1,
                mEndingCalendar.get(Calendar.YEAR)));

        mEndingTimeEdit.setText(String.format(Locale.GERMAN, "%02d:%02d Uhr",
                mEndingCalendar.get(Calendar.HOUR_OF_DAY),
                mEndingCalendar.get(Calendar.MINUTE)));

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
                isMaxPersons = false;
                showNumberPickerDialog();
            }
        });
    }


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


    private void setUpAcceptButton(){
        ImageButton acceptButton = findViewById(R.id.create_accept);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long startingTime = getUnixTime(mStartingCalendar);
                long endingTime = getUnixTime(mEndingCalendar);

                EditText inputTitle = findViewById(R.id.input_title);
                EditText inputDescription = findViewById(R.id.input_description);
                title = inputTitle.getText().toString();
                description = inputDescription.getText().toString();

                Event event = new Event(lat, lng, mUserID, title, description, startingTime, endingTime, selectedCategory, maxPersons, currentPersons);
                String key = mDatabaseReference.push().getKey();
                event.setID(key);
                mDatabaseReference.child(key).setValue(event);
                finish();
            }
        });
    }


    /**
     * Greys out category buttons based on which category is selected
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

        Drawable newIcon = (selectedCategory == 1) ? foodAndDrinkIcon : foodAndDrinkIconDeactivated;
        mButtonFoodAndDrink.setImageDrawable(newIcon);

        newIcon = (selectedCategory == 2) ? partyIcon : partyIconDeactivated;
        mButtonParty.setImageDrawable(newIcon);

        newIcon = (selectedCategory == 3) ? musicIcon : musicIconDeactivated;
        mButtonMusic.setImageDrawable(newIcon);

        newIcon = (selectedCategory == 4) ? sportsIcon : sportsIconDeactivated;
        mButtonSports.setImageDrawable(newIcon);
    }


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
    }


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


    private void showStartingTimeDialog(){
        isStartingTime = true;
        Bundle bundle = new Bundle();
        bundle.putInt("hour", mStartingCalendar.get(Calendar.HOUR_OF_DAY));
        bundle.putInt("minute", mStartingCalendar.get(Calendar.MINUTE));
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.setArguments(bundle);
        newFragment.show(getSupportFragmentManager(), "startingTimePicker");
    }


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


    private void showEndingTimeDialog(){
        isStartingTime = false;
        Bundle bundle = new Bundle();
        bundle.putInt("hour", mEndingCalendar.get(Calendar.HOUR_OF_DAY));
        bundle.putInt("minute", mEndingCalendar.get(Calendar.MINUTE));
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.setArguments(bundle);
        newFragment.show(getSupportFragmentManager(), "endingTimePicker");
    }

    private void showNumberPickerDialog(){
        View view = getLayoutInflater().inflate(R.layout.number_picker, null);
        final AlertDialog d = new AlertDialog.Builder(this).setView(view).create();

        Button numberPickerCancel = view.findViewById(R.id.number_picker_cancel);
        Button numberPickerSet = view.findViewById(R.id.number_picker_set);
        final NumberPicker np = view.findViewById(R.id.number_picker);
        assert np != null;

        np.setMaxValue(200);
        np.setMinValue(1);
        np.setValue(maxPersons);
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


    @Override
    public void onFinishPickDateDialog(int year, int month, int day){
        String date = String.format(Locale.GERMAN, "%02d. %02d. %4d", day, month + 1, year);
        if(isStartingDate){
            startingDate = date;
            mStartingDateEdit.setText(startingDate);
            mStartingCalendar.set(Calendar.YEAR, year);
            mStartingCalendar.set(Calendar.MONTH, month);
            mStartingCalendar.set(Calendar.DAY_OF_MONTH, day);
        }else{
            endingDate = date;
            mEndingDateEdit.setText(endingDate);
            mEndingCalendar.set(Calendar.YEAR, year);
            mEndingCalendar.set(Calendar.MONTH, month);
            mEndingCalendar.set(Calendar.DAY_OF_MONTH, day);
        }
    }


    @Override
    public void onFinishPickTimeDialog(int hour, int minute){
        String time = String.format(Locale.GERMAN, "%02d:%02d", hour, minute);
        if(isStartingTime){
            startingTime = time;
            mStartingTimeEdit.setText(String.format("%s %s", startingTime, "Uhr"));
            mStartingCalendar.set(Calendar.HOUR_OF_DAY, hour);
            mStartingCalendar.set(Calendar.MINUTE, minute);
        }else{
            endingTime = time;
            mEndingTimeEdit.setText(String.format("%s %s", endingTime, "Uhr"));
            mEndingCalendar.set(Calendar.HOUR_OF_DAY, hour);
            mEndingCalendar.set(Calendar.MINUTE, minute);
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
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                    } else {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                    }
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
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
    }


    private long getUnixTime(Calendar calendar){
        return calendar.getTime().getTime();
    }
}
