package de.spontune.android.spontune;

import android.app.ProgressDialog;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import de.spontune.android.spontune.Data.Event;
import de.spontune.android.spontune.Fragments.CreateMapFragment;
import de.spontune.android.spontune.Fragments.CreateTextFragment;
import de.spontune.android.spontune.Fragments.EventPreviewFragment;
import de.spontune.android.spontune.Fragments.NonSwipeableViewPager;


public class CreateEventActivity extends AppCompatActivity{

    private static FirebaseDatabase mFirebaseDatabase;
    private static FirebaseAuth mFirebaseAuth;
    private static DatabaseReference mDatabaseReference;

    public CreateMapFragment createMapFragment;
    public CreateTextFragment createTextFragment;
    public EventPreviewFragment eventPreviewFragment;

    //Properties for the created event
    private String mEventID;
    private String mUserID;
    private String title;
    private String description;
    private double lat;
    private double lng;

    private NonSwipeableViewPager viewPager;
    private PagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
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

        viewPager = findViewById(R.id.view_pager);
        pagerAdapter = new CreateEventActivity.ScreenSlidePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(0);

        final TextView toolbarTitle = findViewById(R.id.title_text);

        ImageButton btnClose = findViewById(R.id.event_close);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAfterTransition();
            }
        });

        final ImageButton buttonBack = findViewById(R.id.create_back);
        final ImageButton buttonForward = findViewById(R.id.create_forward);
        buttonBack.setVisibility(View.GONE);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(viewPager.getCurrentItem() == 0){
                    finishAfterTransition();
                }else if(viewPager.getCurrentItem() == 1){
                    viewPager.setCurrentItem(0);
                    buttonBack.setVisibility(View.GONE);
                }else{
                    viewPager.setCurrentItem(1);
                    toolbarTitle.setText(getString(R.string.new_event));
                    buttonForward.setImageDrawable(getDrawable(R.drawable.ic_round_arrow_forward));
                }
            }
        });

        buttonForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(viewPager.getCurrentItem() == 0) {
                    viewPager.setCurrentItem(1);
                    buttonBack.setVisibility(View.VISIBLE);
                }else if(viewPager.getCurrentItem() == 1){
                    final EditText inputTitle = createTextFragment.mTitleEdit;
                    final EditText inputDescription = createTextFragment.mDescriptionEdit;
                    Calendar now = GregorianCalendar.getInstance();
                    title = inputTitle.getText().toString();
                    description = inputDescription.getText().toString();
                    long startingTime = getUnixTime(createTextFragment.mStartingCalendar);
                    long endingTime = getUnixTime(createTextFragment.mEndingCalendar);

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
                        createTextFragment.mStartingTimeEdit.getBackground().setColorFilter(getResources().getColor(R.color.error), PorterDuff.Mode.SRC_ATOP);
                        createTextFragment.mStartingDateEdit.getBackground().setColorFilter(getResources().getColor(R.color.error), PorterDuff.Mode.SRC_ATOP);
                    }else if(endingTime <= startingTime){
                        Snackbar.make(view, getResources().getString(R.string.ending_time_before_starting_time), Snackbar.LENGTH_LONG).show();
                        createTextFragment.mEndingTimeEdit.getBackground().setColorFilter(getResources().getColor(R.color.error), PorterDuff.Mode.SRC_ATOP);
                        createTextFragment.mEndingDateEdit.getBackground().setColorFilter(getResources().getColor(R.color.error), PorterDuff.Mode.SRC_ATOP);
                    }else {
                        viewPager.setCurrentItem(2);
                        eventPreviewFragment.mEventDescriptionTextView.setText(description);
                        Date startingDate = new Date(startingTime);
                        String startingDateString = DateFormat.getDateFormat(getApplicationContext()).format(startingDate);
                        String startingTimeString = DateFormat.getTimeFormat(getApplicationContext()).format(startingDate);

                        Date endingDate = new Date(endingTime);
                        String endingDateString = DateFormat.getDateFormat(getApplicationContext()).format(endingDate);
                        String endingTimeString = DateFormat.getTimeFormat(getApplicationContext()).format(endingTime);
                        TextView startingTimeTextView = eventPreviewFragment.mStartingTimeTextView;
                        TextView endingTimeTextView = eventPreviewFragment.mEndingTimeTextView;
                        TextView startingDateTextView = eventPreviewFragment.mStartingDateTextView;
                        TextView endingDateTextView = eventPreviewFragment.mEndingDateTextView;

                        startingTimeTextView.setText(startingTimeString);
                        endingTimeTextView.setText(endingTimeString);

                        Calendar calendar = GregorianCalendar.getInstance();
                        if(calendar.getTimeInMillis() >= startingTime){
                            startingTimeTextView.setText(getResources().getText(R.string.now));
                            startingTimeTextView.setTextColor(getResources().getColor(R.color.green));
                            Animation animBlink = AnimationUtils.loadAnimation(CreateEventActivity.this, R.anim.anim_blink);
                            startingTimeTextView.startAnimation(animBlink);
                        }else if(calendar.get(Calendar.DAY_OF_MONTH) != startingDate.getDate()){
                            startingDateTextView.setText(startingDateString);
                            startingDateTextView.setVisibility(View.VISIBLE);
                        }

                        if(startingDate.getDate() != endingDate.getDate()){
                            endingDateTextView.setText(endingDateString);
                            endingDateTextView.setVisibility(View.VISIBLE);
                        }

                        toolbarTitle.setText(getString(R.string.preview));
                        buttonForward.setImageDrawable(getDrawable(R.drawable.activity_create_accept));
                    }
                }else{
                    final EditText inputTitle = createTextFragment.mTitleEdit;
                    final EditText inputDescription = createTextFragment.mDescriptionEdit;
                    Calendar now = GregorianCalendar.getInstance();
                    title = inputTitle.getText().toString();
                    description = inputDescription.getText().toString();
                    long startingTime = getUnixTime(createTextFragment.mStartingCalendar);
                    long endingTime = getUnixTime(createTextFragment.mEndingCalendar);

                    /*

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
                        createTextFragment.mStartingTimeEdit.getBackground().setColorFilter(getResources().getColor(R.color.error), PorterDuff.Mode.SRC_ATOP);
                        createTextFragment.mStartingDateEdit.getBackground().setColorFilter(getResources().getColor(R.color.error), PorterDuff.Mode.SRC_ATOP);
                    }else if(endingTime <= startingTime){
                        Snackbar.make(view, getResources().getString(R.string.ending_time_before_starting_time), Snackbar.LENGTH_LONG).show();
                        createTextFragment.mEndingTimeEdit.getBackground().setColorFilter(getResources().getColor(R.color.error), PorterDuff.Mode.SRC_ATOP);
                        createTextFragment.mEndingDateEdit.getBackground().setColorFilter(getResources().getColor(R.color.error), PorterDuff.Mode.SRC_ATOP);
                    }else {

                    */
                        AutoCompleteTextView inputAddress = findViewById(R.id.input_address);
                        String address = inputAddress.getText().toString();
                        if (address.equals("")) {
                            address = null;
                        }

                        if(eventPreviewFragment.filePath != null){
                            uploadImage();
                        }

                        HashMap<String, String> participants = new HashMap<>();
                        participants.put(mUserID, mUserID);
                        if(createTextFragment.lat == 0.0 || createTextFragment.lng == 0.0){
                            lat = createMapFragment.lat;
                            lng = createMapFragment.lng;
                        }
                        Event event = new Event(lat, lng, createTextFragment.mUserID, title, description, startingTime, endingTime, createTextFragment.selectedCategory, createTextFragment.maxPersons, createTextFragment.currentPersons, address, participants);
                        mEventID = mDatabaseReference.push().getKey();
                        event.setID(mEventID);
                        mDatabaseReference.child(mEventID).setValue(event);
                        if(eventPreviewFragment.filePath != null){
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
    private void uploadImage() {
        Uri filePath = eventPreviewFragment.filePath;
        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            StorageReference ref = storageReference.child("categoryImages/"+ mEventID);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //progressDialog.dismiss();
                            Toast.makeText(CreateEventActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //progressDialog.dismiss();
                            Toast.makeText(CreateEventActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }


    private long getUnixTime(Calendar calendar){
        return calendar.getTime().getTime();
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        }else if(viewPager.getCurrentItem() == 1){
            viewPager.setCurrentItem(0);
        }else{
            viewPager.setCurrentItem(1);
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
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
        public int getCount() {
            return 3;
        }
    }

}
