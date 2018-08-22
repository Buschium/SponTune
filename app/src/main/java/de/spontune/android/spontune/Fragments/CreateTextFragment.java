package de.spontune.android.spontune.Fragments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import de.spontune.android.spontune.Input.FetchLocationIntentService;
import de.spontune.android.spontune.Input.PlacesAutoCompleteAdapter;
import de.spontune.android.spontune.R;

public class CreateTextFragment extends Fragment implements DatePickerFragment.PickDateDialogListener, TimePickerFragment.PickTimeDialogListener{

    private static FirebaseDatabase mFirebaseDatabase;
    private static FirebaseAuth mFirebaseAuth;
    private static DatabaseReference mDatabaseReference;
    private PlacesAutoCompleteAdapter mAdapter;
    private static final LatLngBounds BOUNDS_GREATER_MUNICH = new LatLngBounds(new LatLng(48.034211, 11.281276), new LatLng(48.346808, 11.879760));
    private GeoDataClient mGeoDataClient;

    public String mUserID;
    public String title;
    public String description;
    public int maxPersons = 10;
    public int currentPersons = 1;
    public int selectedCategory = 1;
    public double lat;
    public double lng;

    private boolean isStartingTime = true;
    private boolean isStartingDate = true;
    private boolean isMaxPersons = true;

    public EditText mTitleEdit;
    public EditText mDescriptionEdit;
    public EditText mStartingDateEdit;
    public EditText mStartingTimeEdit;
    public EditText mEndingDateEdit;
    public EditText mEndingTimeEdit;
    public EditText mMaxPersons;
    public EditText mCurrentPersons;

    //category buttons
    private ImageButton mButtonFoodAndDrink;
    private ImageButton mButtonParty;
    private ImageButton mButtonMusic;
    private ImageButton mButtonSports;

    //Calendars for the starting and ending times
    public final Calendar mStartingCalendar = GregorianCalendar.getInstance();
    public final Calendar mEndingCalendar = GregorianCalendar.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_create_text, container, false);

        mGeoDataClient = Places.getGeoDataClient(getActivity(), null);

        //Set up the FireBase reference and get the UID
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child("events");

        if(mFirebaseAuth.getCurrentUser() != null) {
            mUserID = mFirebaseAuth.getCurrentUser().getUid();
        }else{
            Toast.makeText(getActivity(), "Authentifizierungsfehler", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        mTitleEdit = rootView.findViewById(R.id.input_title);
        mDescriptionEdit = rootView.findViewById(R.id.input_description);

        setUpStartingButtons(rootView);
        setUpEndingButtons(rootView);
        setUpVisitorButtons(rootView);
        setUpAddressInput(rootView);
        setUpCategoryButtons(rootView);

        return rootView;
    }

    /**
     * Set up the input fields for the starting time and starting date.
     */
    private void setUpStartingButtons(View view){
        mStartingDateEdit = view.findViewById(R.id.starting_date);
        mStartingTimeEdit = view.findViewById(R.id.starting_time);
        mStartingCalendar.add(Calendar.MINUTE, 30);
        Date startingDate = new Date(mStartingCalendar.getTimeInMillis());

        mStartingDateEdit.setText(DateFormat.getDateFormat(getActivity().getApplicationContext()).format(startingDate));
        mStartingTimeEdit.setText(DateFormat.getTimeFormat(getActivity().getApplicationContext()).format(startingDate));

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
    private void setUpEndingButtons(View view){
        mEndingCalendar.add(Calendar.HOUR, 1);
        mEndingCalendar.add(Calendar.MINUTE, 30);
        Date endingDate = new Date(mEndingCalendar.getTimeInMillis());

        mEndingDateEdit = view.findViewById(R.id.ending_date);
        mEndingTimeEdit = view.findViewById(R.id.ending_time);
        mEndingDateEdit.setText(DateFormat.getDateFormat(getActivity().getApplicationContext()).format(endingDate));
        mEndingTimeEdit.setText(DateFormat.getTimeFormat(getActivity().getApplicationContext()).format(endingDate));

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
    private void setUpVisitorButtons(View view){
        mMaxPersons = view.findViewById(R.id.max_persons);
        mCurrentPersons = view.findViewById(R.id.current_persons);
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
     * Shows the dialog for selecting the starting date
     */
    private void showStartingDateDialog(){
        isStartingDate = true;
        Bundle bundle = new Bundle();
        bundle.putInt("year", mStartingCalendar.get(Calendar.YEAR));
        bundle.putInt("month", mStartingCalendar.get(Calendar.MONTH));
        bundle.putInt("day", mStartingCalendar.get(Calendar.DAY_OF_MONTH));
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.setPickDateDialogListener(this);
        newFragment.setArguments(bundle);
        newFragment.show(getActivity().getSupportFragmentManager(), "startingDatePicker");
    }


    /**
     * Shows the dialog for selecting the starting time
     */
    private void showStartingTimeDialog(){
        isStartingTime = true;
        Bundle bundle = new Bundle();
        bundle.putInt("hour", mStartingCalendar.get(Calendar.HOUR_OF_DAY));
        bundle.putInt("minute", mStartingCalendar.get(Calendar.MINUTE));
        TimePickerFragment newFragment = new TimePickerFragment();
        newFragment.setPickTimeDialogListener(this);
        newFragment.setArguments(bundle);
        newFragment.show(getActivity().getSupportFragmentManager(), "startingTimePicker");
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
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.setPickDateDialogListener(this);
        newFragment.setArguments(bundle);
        newFragment.show(getActivity().getSupportFragmentManager(), "endingDatePicker");
    }


    /**
     * Shows the dialog for selecting the ending time
     */
    private void showEndingTimeDialog(){
        isStartingTime = false;
        Bundle bundle = new Bundle();
        bundle.putInt("hour", mEndingCalendar.get(Calendar.HOUR_OF_DAY));
        bundle.putInt("minute", mEndingCalendar.get(Calendar.MINUTE));
        TimePickerFragment newFragment = new TimePickerFragment();
        newFragment.setPickTimeDialogListener(this);
        newFragment.setArguments(bundle);
        newFragment.show(getActivity().getSupportFragmentManager(), "endingTimePicker");
    }


    /**
     * Shows a dialog for selecting either the maximum number of visitors or the current number of visitors
     */
    private void showNumberPickerDialog(){
        View view = getLayoutInflater().inflate(R.layout.number_picker, null);
        final AlertDialog d = new AlertDialog.Builder(getActivity()).setView(view).create();

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
            mStartingDateEdit.setText(DateFormat.getDateFormat(getActivity().getApplicationContext()).format(new Date(mStartingCalendar.getTimeInMillis())));
            mStartingDateEdit.getBackground().setColorFilter(null);
            mStartingTimeEdit.getBackground().setColorFilter(null);
        }else{
            mEndingCalendar.set(Calendar.YEAR, year);
            mEndingCalendar.set(Calendar.MONTH, month);
            mEndingCalendar.set(Calendar.DAY_OF_MONTH, day);
            mEndingDateEdit.setText(DateFormat.getDateFormat(getActivity().getApplicationContext()).format(new Date(mEndingCalendar.getTimeInMillis())));
            mEndingDateEdit.getBackground().setColorFilter(null);
            mEndingTimeEdit.getBackground().setColorFilter(null);
        }
    }


    @Override
    public void onFinishPickTimeDialog(int hour, int minute){
        if(isStartingTime){
            mStartingCalendar.set(Calendar.HOUR_OF_DAY, hour);
            mStartingCalendar.set(Calendar.MINUTE, minute);
            mStartingTimeEdit.setText(DateFormat.getTimeFormat(getActivity().getApplicationContext()).format(new Date(mStartingCalendar.getTimeInMillis())));
            mStartingDateEdit.getBackground().setColorFilter(null);
            mStartingTimeEdit.getBackground().setColorFilter(null);

        }else{
            mEndingCalendar.set(Calendar.HOUR_OF_DAY, hour);
            mEndingCalendar.set(Calendar.MINUTE, minute);
            mEndingTimeEdit.setText(DateFormat.getTimeFormat(getActivity().getApplicationContext()).format(new Date(mEndingCalendar.getTimeInMillis())));
            mEndingDateEdit.getBackground().setColorFilter(null);
            mEndingTimeEdit.getBackground().setColorFilter(null);
        }
    }


    /**
     * Set up the buttons for selecting the category the new event should belong to.
     */
    private void setUpCategoryButtons(View view){
        mButtonFoodAndDrink = view.findViewById(R.id.create_category_creative);
        mButtonParty = view.findViewById(R.id.create_category_party);
        mButtonMusic = view.findViewById(R.id.create_category_happening);
        mButtonSports = view.findViewById(R.id.create_category_sports);

        mButtonFoodAndDrink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedCategory = 1;
                toggleButtonsGreyed();
            }
        });

        mButtonParty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedCategory = 2;
                toggleButtonsGreyed();
            }
        });

        mButtonMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedCategory = 3;
                toggleButtonsGreyed();
            }
        });

        mButtonSports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedCategory = 4;
                toggleButtonsGreyed();
            }
        });
    }


    /**
     * Greys out category buttons based on which category is selected
     */
    private void toggleButtonsGreyed(){
        Drawable creativeIcon = this.getResources().getDrawable(R.drawable.category_creative_light);
        Drawable partyIcon = this.getResources().getDrawable(R.drawable.category_party_light);
        Drawable happeningIcon = this.getResources().getDrawable(R.drawable.category_happening_light);
        Drawable sportsIcon = this.getResources().getDrawable(R.drawable.category_sports_light);
        Drawable foodAndDrinkIconDeactivated = this.getResources().getDrawable(R.drawable.category_creative_deactivated);
        Drawable partyIconDeactivated = this.getResources().getDrawable(R.drawable.category_party_deactivated);
        Drawable musicIconDeactivated = this.getResources().getDrawable(R.drawable.category_happening_deactivated);
        Drawable sportsIconDeactivated = this.getResources().getDrawable(R.drawable.category_sports_deactivated);

        Drawable newIcon = (selectedCategory == 1) ? creativeIcon : foodAndDrinkIconDeactivated;
        mButtonFoodAndDrink.setImageDrawable(newIcon);

        newIcon = (selectedCategory == 2) ? partyIcon : partyIconDeactivated;
        mButtonParty.setImageDrawable(newIcon);

        newIcon = (selectedCategory == 3) ? happeningIcon : musicIconDeactivated;
        mButtonMusic.setImageDrawable(newIcon);

        newIcon = (selectedCategory == 4) ? sportsIcon : sportsIconDeactivated;
        mButtonSports.setImageDrawable(newIcon);
    }


    private long getUnixTime(Calendar calendar){
        return calendar.getTime().getTime();
    }


    /**
     * Set up the text field for the optional address
     */
    private void setUpAddressInput(View view){
        AutoCompleteTextView autoCompleteTextView = view.findViewById(R.id.input_address);
        AutocompleteFilter filter = new AutocompleteFilter.Builder().setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS).build();
        mAdapter = new PlacesAutoCompleteAdapter(getActivity(), mGeoDataClient, BOUNDS_GREATER_MUNICH, filter);
        autoCompleteTextView.setAdapter(mAdapter);
        autoCompleteTextView.setOnItemClickListener(mAutocompleteClickListener);
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            final AutocompletePrediction item = mAdapter.getItem(position);
            startJobIntentService(item.getFullText(null).toString());
        }
    };

    private void startJobIntentService(String address){
        Intent intent = new Intent(getActivity(), FetchLocationIntentService.class);
        CreateTextFragment.LocationResultReceiver receiver = new CreateTextFragment.LocationResultReceiver(new Handler());
        intent.putExtra("resultReceiver", receiver);
        intent.putExtra("address", address);
        FetchLocationIntentService.enqueueWork(getActivity(), intent);
    }

    class LocationResultReceiver extends ResultReceiver {

        public LocationResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            double lat = resultData.getDouble("lat");
            double lng = resultData.getDouble("lng");
            CreateTextFragment.this.lat = lat;
            CreateTextFragment.this.lng = lng;
        }
    }
}
