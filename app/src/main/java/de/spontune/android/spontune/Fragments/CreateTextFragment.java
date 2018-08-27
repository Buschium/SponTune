package de.spontune.android.spontune.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
    public int maxPersons;
    public double lat;
    public double lng;
    public boolean now;

    private boolean isStartingTime = true;
    private boolean isStartingDate = true;

    private ImageView mStartingTimeIcon;
    private ImageView mStartingDateIcon;

    public EditText mTitleEdit;
    public EditText mDescriptionEdit;
    public EditText mStartingDateEdit;
    public EditText mStartingTimeEdit;
    public EditText mEndingDateEdit;
    public EditText mEndingTimeEdit;
    public EditText mMaxPersons;
    public EditText mCurrentPersons;
    private TextView maxPersonsTextView;

    AppCompatCheckBox checkBoxMaxPersons;
    AppCompatCheckBox checkBoxNow;

    //Calendars for the starting and ending times
    public final Calendar mStartingCalendar = Calendar.getInstance();
    public final Calendar mEndingCalendar = Calendar.getInstance();

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

        return rootView;
    }

    /**
     * Set up the input fields for the starting time and starting date.
     */
    private void setUpStartingButtons(View view){
        mStartingDateEdit = view.findViewById(R.id.starting_date);
        mStartingTimeEdit = view.findViewById(R.id.starting_time);
        mStartingTimeIcon = view.findViewById(R.id.starting_time_image);
        mStartingDateIcon = view.findViewById(R.id.starting_date_image);
        mStartingCalendar.add(Calendar.MINUTE, 30);
        Date startingDate = new Date(mStartingCalendar.getTimeInMillis());

        checkBoxNow = view.findViewById(R.id.checkbox_now);
        checkBoxNow.setChecked(false);

        mStartingDateEdit.setText(DateFormat.getDateFormat(getActivity().getApplicationContext()).format(startingDate));
        mStartingTimeEdit.setText(DateFormat.getTimeFormat(getActivity().getApplicationContext()).format(startingDate));

        mStartingDateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showStartingDateDialog();
            }
        });

        mStartingTimeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showStartingTimeDialog();
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

        mEndingTimeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEndingTimeDialog();
            }
        });
    }


    /**
     * Set up the input fields for the maximum number of visitors and the current number of visitors.
     */
    private void setUpVisitorButtons(View view){
        checkBoxMaxPersons = view.findViewById(R.id.checkbox_max);
        checkBoxMaxPersons.setChecked(false);
        maxPersonsTextView = view.findViewById(R.id.max_persons_text_view);
        mMaxPersons = view.findViewById(R.id.max_persons);
        mMaxPersons.setText(String.format(Locale.GERMAN, "%d", maxPersons));

        mMaxPersons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNumberPickerDialog();
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

        np.setMaxValue(200);
        np.setMinValue(1);
        np.setValue(10);
        np.setWrapSelectorWheel(false);

        numberPickerSet.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                maxPersons = np.getValue();
                mMaxPersons.setText(String.format(Locale.GERMAN, "%d", maxPersons));
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


    public void onCheckboxClicked(int id, boolean checked){
        switch(id){
            case R.id.checkbox_now:
                checkBoxNow.setChecked(checked);
                mStartingTimeEdit.setEnabled(!checked);
                mStartingDateEdit.setEnabled(!checked);
                if(checked){
                    mStartingTimeIcon.setImageResource(R.drawable.ic_round_time_deactivated);
                    mStartingDateIcon.setImageResource(R.drawable.ic_round_date_deactivated);
                }else{
                    mStartingTimeIcon.setImageResource(R.drawable.ic_round_time);
                    mStartingDateIcon.setImageResource(R.drawable.ic_round_date);
                }
                break;
            case R.id.checkbox_max:
                checkBoxMaxPersons.setChecked(checked);
                if(checked){
                    maxPersons = 10;
                    maxPersonsTextView.setVisibility(View.VISIBLE);
                    mMaxPersons.setVisibility(View.VISIBLE);
                    mMaxPersons.setText("" + maxPersons);
                }else{
                    maxPersons = 0;
                    maxPersonsTextView.setVisibility(View.GONE);
                    mMaxPersons.setVisibility(View.GONE);
                }
        }
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
