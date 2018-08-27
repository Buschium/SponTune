package de.spontune.android.spontune;

import android.app.ProgressDialog;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import de.spontune.android.spontune.Adapters.NonSwipeableViewPager;
import de.spontune.android.spontune.Data.Event;
import de.spontune.android.spontune.Fragments.CreateMapFragment;
import de.spontune.android.spontune.Fragments.CreateTextFragment;
import de.spontune.android.spontune.Fragments.EventPreviewFragment;


public class CreateEventActivity extends AppCompatActivity{

    private DatabaseReference mDatabaseReference;

    private CreateMapFragment createMapFragment;
    private CreateTextFragment createTextFragment;
    private EventPreviewFragment eventPreviewFragment;

    //Properties for the created event
    private String mEventID;
    private String mUserID;
    private String title;
    private String description;
    private double lat;
    private double lng;
    private int selectedCategory = 1;
    private boolean now;

    private ImageButton mButtonCreative;
    private ImageButton mButtonParty;
    private ImageButton mButtonHappening;
    private ImageButton mButtonSports;

    private NonSwipeableViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getResources().getString(R.string.location_choose));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Set up the FireBase reference and get the UID
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child("events");

        if (mFirebaseAuth.getCurrentUser() != null) {
            mUserID = mFirebaseAuth.getCurrentUser().getUid();
        }else{
            Toast.makeText(this, "Authentifizierungsfehler", Toast.LENGTH_SHORT).show();
            finish();
        }

        viewPager = findViewById(R.id.view_pager);
        PagerAdapter pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(0);
        setUpCategoryButtons();

        final TextView toolbarTitle = findViewById(R.id.title_text);
        toolbarTitle.setText(getResources().getString(R.string.location_choose));

        ImageButton btnClose = findViewById(R.id.event_close);
        btnClose.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                finishAfterTransition();
            }
        });

        final View categoryButtons = findViewById(R.id.category_buttons);
        final ImageButton buttonBack = findViewById(R.id.create_back);
        final ImageButton buttonForward = findViewById(R.id.create_forward);
        buttonBack.setVisibility(View.GONE);
        buttonBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if (viewPager.getCurrentItem() == 0) {
                    finishAfterTransition();
                }else if (viewPager.getCurrentItem() == 1) {
                    viewPager.setCurrentItem(0);
                    categoryButtons.setVisibility(View.VISIBLE);
                    buttonBack.setVisibility(View.GONE);
                }else{
                    viewPager.setCurrentItem(1);
                    toolbarTitle.setText(getString(R.string.new_event));
                    buttonForward.setImageDrawable(getDrawable(R.drawable.ic_round_arrow_forward));
                }
            }
        });

        buttonForward.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if (viewPager.getCurrentItem() == 0) {
                    viewPager.setCurrentItem(1);
                    categoryButtons.setVisibility(View.GONE);
                    buttonBack.setVisibility(View.VISIBLE);
                }else if (viewPager.getCurrentItem() == 1) {
                    final EditText inputTitle = createTextFragment.mTitleEdit;
                    final EditText inputDescription = createTextFragment.mDescriptionEdit;
                    Calendar nowCalendar = Calendar.getInstance();
                    title = inputTitle.getText().toString();
                    description = inputDescription.getText().toString();
                    long startingTime = getUnixTime(createTextFragment.mStartingCalendar);
                    long endingTime = getUnixTime(createTextFragment.mEndingCalendar);

                    if ((title.isEmpty()) || (title == null)) {
                        Snackbar.make(view, getResources().getString(R.string.no_title_input), Snackbar.LENGTH_LONG).show();
                        inputTitle.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
                        inputTitle.requestFocus();
                        inputTitle.addTextChangedListener(new TextWatcher(){
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){
                                if (charSequence.equals("")) {
                                    inputTitle.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
                                }else{
                                    inputTitle.getBackground().setColorFilter(null);
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable editable){

                            }
                        });
                    }else if ((description.isEmpty()) || (description == null)) {
                        Snackbar.make(view, getResources().getString(R.string.no_description_input), Snackbar.LENGTH_LONG).show();
                        inputDescription.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
                        inputDescription.requestFocus();
                        inputDescription.addTextChangedListener(new TextWatcher(){
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){
                                if (charSequence.equals("")) {
                                    inputDescription.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
                                }else{
                                    inputDescription.getBackground().setColorFilter(null);

                                }
                            }

                            @Override
                            public void afterTextChanged(Editable editable){

                            }
                        });
                    }else if (startingTime <= nowCalendar.getTimeInMillis() && !now) {
                        Snackbar.make(view, getResources().getString(R.string.starting_time_over), Snackbar.LENGTH_LONG).show();
                        createTextFragment.mStartingTimeEdit.getBackground().setColorFilter(getResources().getColor(R.color.error), PorterDuff.Mode.SRC_ATOP);
                        createTextFragment.mStartingDateEdit.getBackground().setColorFilter(getResources().getColor(R.color.error), PorterDuff.Mode.SRC_ATOP);
                    }else if (endingTime <= startingTime) {
                        Snackbar.make(view, getResources().getString(R.string.ending_time_before_starting_time), Snackbar.LENGTH_LONG).show();
                        createTextFragment.mEndingTimeEdit.getBackground().setColorFilter(getResources().getColor(R.color.error), PorterDuff.Mode.SRC_ATOP);
                        createTextFragment.mEndingDateEdit.getBackground().setColorFilter(getResources().getColor(R.color.error), PorterDuff.Mode.SRC_ATOP);
                    }else{
                        eventPreviewFragment.mTitleTextView.setText(createTextFragment.mTitleEdit.getText().toString());

                        if(createTextFragment.maxPersons != 0) {
                            eventPreviewFragment.mParticipantsImageView.setVisibility(View.VISIBLE);
                            eventPreviewFragment.mParticipantsTextView.setVisibility(View.VISIBLE);
                            String max = getResources().getString(R.string.participants_limit_without_parentheses, createTextFragment.maxPersons);
                            eventPreviewFragment.mParticipantsTextView.setText(max);
                        }
                        DatabaseReference creatorReference = FirebaseDatabase.getInstance().getReference().child("users").child(createTextFragment.mUserID).child("username");
                        creatorReference.addListenerForSingleValueEvent(new ValueEventListener(){
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot){
                                String creator = dataSnapshot.getValue(String.class);
                                eventPreviewFragment.mCreatorTextView.setText(getResources().getString(R.string.created_by, creator));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError){

                            }
                        });

                        eventPreviewFragment.mEventDescriptionTextView.setText(description);

                        Calendar endOfDay = Calendar.getInstance();
                        endOfDay.set(Calendar.HOUR_OF_DAY, 23);
                        endOfDay.set(Calendar.MINUTE, 59);
                        endOfDay.set(Calendar.SECOND, 59);
                        Calendar endOfTomorrow = Calendar.getInstance();
                        endOfTomorrow.add(Calendar.DATE, 1);
                        endOfTomorrow.set(Calendar.HOUR_OF_DAY, 23);
                        endOfTomorrow.set(Calendar.MINUTE, 59);
                        endOfTomorrow.set(Calendar.SECOND, 59);

                        Date startingDate;
                        if(now){
                            startingDate = new Date();
                        }else{
                            startingDate = new Date(startingTime);
                        }
                        String startingDayString = getDayOfWeek(startingDate);
                        String startingDateString = DateFormat.getDateFormat(getApplicationContext()).format(startingDate);
                        String startingTimeString = DateFormat.getTimeFormat(getApplicationContext()).format(startingDate);

                        Date endingDate = new Date(endingTime);
                        String endingDayString = getDayOfWeek(endingDate);
                        String endingDateString = DateFormat.getDateFormat(getApplicationContext()).format(endingDate);
                        String endingTimeString = DateFormat.getTimeFormat(getApplicationContext()).format(endingDate);

                        if(startingTime <= endOfDay.getTimeInMillis()){
                            String today = getResources().getString(R.string.today);
                            eventPreviewFragment.mStartingTimeTextView.setText(getResources().getString(R.string.starting_time, today, startingTimeString));
                        }else if(startingTime <= endOfTomorrow.getTimeInMillis()){
                            String tomorrow = getResources().getString(R.string.tomorrow);
                            eventPreviewFragment.mStartingTimeTextView.setText(getResources().getString(R.string.starting_time, tomorrow, startingTimeString));
                        }else{
                            eventPreviewFragment.mStartingTimeTextView.setText(getResources().getString(R.string.starting_time, startingDayString + ", " + startingDateString, startingTimeString));
                        }

                        if(endingTime <= endOfDay.getTimeInMillis()){
                            String today = getResources().getString(R.string.today);
                            eventPreviewFragment.mEndingTimeTextView.setText(getResources().getString(R.string.ending_time, today, endingTimeString));
                        }else if(endingTime <= endOfTomorrow.getTimeInMillis()){
                            String tomorrow = getResources().getString(R.string.tomorrow);
                            eventPreviewFragment.mEndingTimeTextView.setText(getResources().getString(R.string.ending_time, tomorrow, endingTimeString));
                        }else{
                            eventPreviewFragment.mEndingTimeTextView.setText(getResources().getString(R.string.ending_time, endingDayString + ", " + endingDateString, endingTimeString));
                        }

                        toolbarTitle.setText(getString(R.string.preview));
                        buttonForward.setImageDrawable(getDrawable(R.drawable.activity_create_accept));

                        viewPager.setCurrentItem(2);
                    }
                }else{
                    final EditText inputTitle = createTextFragment.mTitleEdit;
                    final EditText inputDescription = createTextFragment.mDescriptionEdit;
                    title = inputTitle.getText().toString();
                    description = inputDescription.getText().toString();
                    long startingTime;
                    if(now){
                        startingTime = (new Date()).getTime();
                    }else{
                        startingTime = getUnixTime(createTextFragment.mStartingCalendar);
                    }
                    long endingTime = getUnixTime(createTextFragment.mEndingCalendar);

                    AutoCompleteTextView inputAddress = findViewById(R.id.input_address);
                    String address = inputAddress.getText().toString();
                    if (address.isEmpty()) {
                        address = null;
                    }

                    if (eventPreviewFragment.filePath != null) {
                        uploadImage();
                    }

                    HashMap<String, String> participants = new HashMap<>();
                    participants.put(mUserID, mUserID);
                    if (createTextFragment.lat == 0.0 || createTextFragment.lng == 0.0) {
                        lat = createMapFragment.lat;
                        lng = createMapFragment.lng;
                    }
                    Event event = new Event(lat, lng, createTextFragment.mUserID, title, description, startingTime, endingTime, selectedCategory, createTextFragment.maxPersons, address, participants);
                    mEventID = mDatabaseReference.push().getKey();
                    event.setID(mEventID);
                    mDatabaseReference.child(mEventID).setValue(event);
                    if (eventPreviewFragment.filePath != null) {
                        uploadImage();
                    }
                    finish();
                }
            }
        });
    }


    /**
     * upload the image to firebase in the folder 'images/[userId]'
     */
    private void uploadImage(){
        Uri filePath = eventPreviewFragment.filePath;
        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            StorageReference ref = storageReference.child("categoryImages/" + mEventID);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>(){
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot){
                            //progressDialog.dismiss();
                            Toast.makeText(CreateEventActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener(){
                        @Override
                        public void onFailure(@NonNull Exception e){
                            //progressDialog.dismiss();
                            Toast.makeText(CreateEventActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>(){
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot){
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }
    }


    /**
     * Set up the buttons for selecting the category the new event should belong to.
     */
    private void setUpCategoryButtons(){
        mButtonCreative = findViewById(R.id.create_category_creative);
        mButtonParty = findViewById(R.id.create_category_party);
        mButtonHappening = findViewById(R.id.create_category_happening);
        mButtonSports = findViewById(R.id.create_category_sports);

        mButtonCreative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedCategory = 1;
                toggleButtonsGreyed();
                createMapFragment.setSelectedCategory(1);
            }
        });

        mButtonParty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedCategory = 2;
                toggleButtonsGreyed();
                createMapFragment.setSelectedCategory(2);
            }
        });

        mButtonHappening.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedCategory = 3;
                toggleButtonsGreyed();
                createMapFragment.setSelectedCategory(3);
            }
        });

        mButtonSports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedCategory = 4;
                toggleButtonsGreyed();
                createMapFragment.setSelectedCategory(4);
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
        mButtonCreative.setImageDrawable(newIcon);

        newIcon = (selectedCategory == 2) ? partyIcon : partyIconDeactivated;
        mButtonParty.setImageDrawable(newIcon);

        newIcon = (selectedCategory == 3) ? happeningIcon : musicIconDeactivated;
        mButtonHappening.setImageDrawable(newIcon);

        newIcon = (selectedCategory == 4) ? sportsIcon : sportsIconDeactivated;
        mButtonSports.setImageDrawable(newIcon);
    }

    private String getDayOfWeek(Date date){
        SimpleDateFormat simpleDateformat = new SimpleDateFormat("EEEE", getResources().getConfiguration().locale); // the day of the week spelled out completely
        return simpleDateformat.format(date);
    }

    private long getUnixTime(Calendar calendar){
        return calendar.getTime().getTime();
    }

    @Override
    public void onBackPressed(){
        if (viewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        }else if (viewPager.getCurrentItem() == 1) {
            viewPager.setCurrentItem(0);
        }else{
            viewPager.setCurrentItem(1);
        }
    }

    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((AppCompatCheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.checkbox_now:
                createTextFragment.onCheckboxClicked(R.id.checkbox_now, checked);
                now = checked;
                break;
            case R.id.checkbox_max:
                createTextFragment.onCheckboxClicked(R.id.checkbox_max, checked);
                break;
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter{

        public ScreenSlidePagerAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int position){
            switch (position) {
                case 0:
                    createMapFragment = new CreateMapFragment();
                    return createMapFragment;
                case 1:
                    createTextFragment = new CreateTextFragment();
                    return createTextFragment;
                default:
                    eventPreviewFragment = new EventPreviewFragment();
                    return eventPreviewFragment;
            }
        }

        @Override
        public int getCount(){
            return 3;
        }
    }

}
