package de.spontune.android.spontune;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EventActivity extends AppCompatActivity {

    private TextView mEventDescriptionTextView;
    private ImageView categoryImage;
    //TODO apply gradient only if the description is too long
    private View gradientView;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private String eventID;
    private String uid;
    private boolean isParticipating;

    private CircleImageView participantsImageOne;
    private CircleImageView participantsImageTwo;
    private CircleImageView participantsImageThree;

    private Map<String, String> following;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        final Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        setUpActionBar(bundle);

        eventID = bundle.getString("id");
        categoryImage = findViewById(R.id.category_image);
        switch(bundle.getInt("category")){
            case 1:
                loadImage("creative");
                break;
            case 2:
                loadImage("party");
                break;
            case 3:
                loadImage("happening");
                break;
            case 4:
                loadImage("sports");
        }

        DatabaseReference creatorReference = firebaseDatabase.getReference().child("users").child(bundle.getString("creator")).child("username");
        creatorReference.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot){
                String creator = dataSnapshot.getValue(String.class);
                TextView textViewCreator = findViewById(R.id.text_view_creator);
                textViewCreator.setText(getResources().getString(R.string.created_by, creator));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError){

            }
        });

        databaseReference = firebaseDatabase.getReference().child("events").child(eventID).child("participants");
        uid = firebaseAuth.getUid();

        final HashMap<String, String> participants = (HashMap<String, String>) getIntent().getSerializableExtra("participants");
        DatabaseReference userReference = firebaseDatabase.getReference().child("users").child(uid).child("following");
        userReference.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot){
                following = (HashMap<String, String>) dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError){
                //TODO handle database exception
            }
        });

        int numParticipants = participants.size();
        TextView participantsTextView = findViewById(R.id.text_view_participants);
        participantsTextView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(EventActivity.this, ParticipantsActivity.class);
                intent.putExtra("creator", bundle.getString("creator"));
                intent.putExtra("participants", participants);
                intent.putExtra("uid", uid);
                startActivity(intent);
            }
        });
        participantsImageOne = findViewById(R.id.participants_image_one);
        participantsImageTwo = findViewById(R.id.participants_image_two);
        participantsImageThree = findViewById(R.id.participants_image_three);
        Object[] participantsSet = participants.keySet().toArray();
        if(numParticipants == 1){
            participantsTextView.setText(getResources().getString(R.string.participants_one));
            loadUserImage((String) participantsSet[0], 1);
            participantsImageTwo.setVisibility(View.GONE);
            participantsImageThree.setVisibility(View.GONE);
        }else if(numParticipants <= 3){
            participantsTextView.setText(getResources().getString(R.string.participants_more_than_three, numParticipants));
            if(numParticipants == 2){
                participantsImageThree.setVisibility(View.GONE);
                loadUserImage((String) participantsSet[0], 1);
                loadUserImage((String) participantsSet[1], 2);
            }else{
                loadUserImage((String) participantsSet[0], 1);
                loadUserImage((String) participantsSet[1], 2);
                loadUserImage((String) participantsSet[2], 3);
            }
        }else{
            participantsTextView.setText(getResources().getString(R.string.participants_more_than_three, numParticipants));
            loadUserImage((String) participantsSet[0], 1);
            loadUserImage((String) participantsSet[1], 2);
            loadUserImage((String) participantsSet[2], 3);
        }

        final Button joinEventButton = findViewById(R.id.event_join);
        final String creator = bundle.getString("creator");
        if(creator.equals(uid)){
            isParticipating = true;
            joinEventButton.setText(getResources().getString(R.string.delete_event));
        }else if(participants.containsKey(uid)){
            isParticipating = true;
            joinEventButton.setText(getResources().getText(R.string.leave));
        }
        joinEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(creator.equals(uid)){
                    firebaseDatabase.getReference().child("events").child(eventID).removeValue();
                    finish();
                }else if(!isParticipating) {
                    databaseReference.child(uid).setValue(uid);
                    joinEventButton.setText(getResources().getText(R.string.leave));
                    isParticipating = true;
                }else{
                    databaseReference.child(uid).removeValue();
                    joinEventButton.setText(getResources().getText(R.string.join_event));
                    isParticipating = false;
                }
            }
        });

        TextView textViewIconParticipants = findViewById(R.id.text_view_icon_participants);
        int maxPersons = bundle.getInt("maxPersons");
        if(maxPersons != 0){
            String max = getResources().getString(R.string.participants_limit, maxPersons);
            String current = getResources().getString(R.string.num_participants, numParticipants);
            textViewIconParticipants.setText(current + max);
        }else{
            String current = getResources().getString(R.string.num_participants, numParticipants);
            textViewIconParticipants.setText(current);
        }

        ImageButton btnChat = findViewById(R.id.event_chat);
        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isParticipating) {
                    startActivity(new Intent(EventActivity.this, ChatActivity.class).putExtra("id", eventID));
                }else{
                    Toast.makeText(EventActivity.this, "Du musst dem Event beitreten, um chatten zu können", Toast.LENGTH_LONG).show();
                }
            }
        });

        ImageButton btnLocation = findViewById(R.id.event_map);
        btnLocation.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                double lat = bundle.getDouble("lat");
                double lng = bundle.getDouble("lng");
                int category = bundle.getInt("category");
                Intent returnIntent = new Intent();
                returnIntent.putExtra("lat", lat);
                returnIntent.putExtra("lng", lng);
                returnIntent.putExtra("category", category);
                returnIntent.putExtra("id", eventID);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });


        mEventDescriptionTextView = findViewById(R.id.event_description_textview);
        mEventDescriptionTextView.setText(bundle.getString("description"));
        gradientView = findViewById(R.id.gradient);
        final int collapsedMaxLines = 7;
        final int expandedMaxLines = 20;

        mEventDescriptionTextView.post(new Runnable() {
            @Override
            public void run() {
                int lines = mEventDescriptionTextView.getLineCount();
                if(lines > collapsedMaxLines){
                    gradientView.setVisibility(View.VISIBLE);
                    mEventDescriptionTextView.setMaxLines(7);

                    //The description expands when the user clicks on it (or shrinks when it's already expanded)
                    mEventDescriptionTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ObjectAnimator animation = ObjectAnimator.ofInt(mEventDescriptionTextView, "maxLines", mEventDescriptionTextView.getMaxLines() == collapsedMaxLines? expandedMaxLines : collapsedMaxLines);
                            animation.setInterpolator(new DecelerateInterpolator());
                            animation.setDuration(200).start();
                            if(mEventDescriptionTextView.getMaxLines() == collapsedMaxLines) {
                                gradientView.setVisibility(View.GONE);
                            }else{
                                gradientView.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            }
        });

        setUpTimeAndDate(bundle);

    }


    private void setUpActionBar(Bundle bundle){
        assert  getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(bundle.getString("summary"));
    }


    /**
     * Converts the starting and ending times (which are stored in unix milliseconds on the database)
     * to human-readable time formats.
     */
    private void setUpTimeAndDate(Bundle bundle){
        TextView startingTimeTextView = findViewById(R.id.text_view_starting_time);
        TextView endingTimeTextView = findViewById(R.id.text_view_ending_time);
        long startingTime = bundle.getLong("startingTime");
        long endingTime = bundle.getLong("endingTime");

        Calendar endOfDay = Calendar.getInstance();
        endOfDay.set(Calendar.HOUR_OF_DAY, 23);
        endOfDay.set(Calendar.MINUTE, 59);
        endOfDay.set(Calendar.SECOND, 59);
        Calendar endOfTomorrow = Calendar.getInstance();
        endOfTomorrow.add(Calendar.DATE, 1);
        endOfTomorrow.set(Calendar.HOUR_OF_DAY, 23);
        endOfTomorrow.set(Calendar.MINUTE, 59);
        endOfTomorrow.set(Calendar.SECOND, 59);

        Date startingDate = new Date(startingTime);
        String startingDayString = getDayOfWeek(startingDate);
        String startingDateString = DateFormat.getDateFormat(getApplicationContext()).format(startingDate);
        String startingTimeString = DateFormat.getTimeFormat(getApplicationContext()).format(startingDate);

        Date endingDate = new Date(endingTime);
        String endingDayString = getDayOfWeek(endingDate);
        String endingDateString = DateFormat.getDateFormat(getApplicationContext()).format(endingDate);
        String endingTimeString = DateFormat.getTimeFormat(getApplicationContext()).format(endingDate);

        if(startingTime <= endOfDay.getTimeInMillis()){
            String today = getResources().getString(R.string.today);
            startingTimeTextView.setText(getResources().getString(R.string.starting_time, today, startingTimeString));
        }else if(startingTime <= endOfTomorrow.getTimeInMillis()){
            String tomorrow = getResources().getString(R.string.tomorrow);
            startingTimeTextView.setText(getResources().getString(R.string.starting_time, tomorrow, startingTimeString));
        }else{
            startingTimeTextView.setText(getResources().getString(R.string.starting_time, startingDayString + ", " + startingDateString, startingTimeString));
        }

        if(endingTime <= endOfDay.getTimeInMillis()){
            String today = getResources().getString(R.string.today);
            endingTimeTextView.setText(getResources().getString(R.string.ending_time, today, endingTimeString));
        }else if(endingTime <= endOfTomorrow.getTimeInMillis()){
            String tomorrow = getResources().getString(R.string.tomorrow);
            endingTimeTextView.setText(getResources().getString(R.string.ending_time, tomorrow, endingTimeString));
        }else{
            endingTimeTextView.setText(getResources().getString(R.string.ending_time, endingDayString + ", " + endingDateString, endingTimeString));
        }
    }


    private void loadImage(final String categoryName){
        Glide.with(this)
                .load(storageReference.child("categoryImages/" + eventID))
                .error(Glide.with(this).load(storageReference.child("categoryImages/" + categoryName + ".jpg")))
                .into(categoryImage);
    }

    /**
     * try to load the profilePicture from firebase. The image is saved under 'images/[userId]'
     */
    private void loadUserImage(String uid, final int participant){
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.activity_maps_menu_user)
                .error(R.drawable.activity_maps_menu_user);
        switch(participant){
            case 1:
                Glide.with(this).load(storageReference.child("images/" + uid)).apply(options).into(participantsImageOne);
                break;
            case 2:
                Glide.with(this).load(storageReference.child("images/" + uid)).apply(options).into(participantsImageTwo);
                break;
            case 3:
                Glide.with(this).load(storageReference.child("images/" + uid)).apply(options).into(participantsImageThree);
                break;
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finishAfterTransition();
            return true;
        }
        return false;
    }

    private String getDayOfWeek(Date date){
        SimpleDateFormat simpleDateformat = new SimpleDateFormat("EEEE", getResources().getConfiguration().locale); // the day of the week spelled out completely
        return simpleDateformat.format(date);
    }

}
