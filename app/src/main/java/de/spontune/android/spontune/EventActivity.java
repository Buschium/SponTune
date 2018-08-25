package de.spontune.android.spontune;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
        if(participants.containsKey(uid)){
            isParticipating = true;
            joinEventButton.setText(getResources().getText(R.string.leave));
        }
        joinEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isParticipating) {
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


        mEventDescriptionTextView = findViewById(R.id.event_description_textview);
        mEventDescriptionTextView.setText(bundle.getString("description"));
        gradientView = findViewById(R.id.gradient);
        final int collapsedMaxLines = 7;
        final int expandedMaxLines = 20;
        int lines = mEventDescriptionTextView.getLineCount();

        if(lines > collapsedMaxLines){
            gradientView.setVisibility(View.VISIBLE);

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

        setUpTimeAndDate(bundle);

    }


    private void setUpActionBar(Bundle bundle){
        assert  getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(bundle.getString("summary"));
        DatabaseReference creatorReference = firebaseDatabase.getReference().child("users").child(bundle.getString("creator")).child("username");
        creatorReference.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot){
                getSupportActionBar().setSubtitle(getResources().getString(R.string.created_by, dataSnapshot.getValue(String.class)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError){

            }
        });
    }


    /**
     * Converts the starting and ending times (which are stored in unix milliseconds on the database)
     * to human-readable time formats.
     */
    private void setUpTimeAndDate(Bundle bundle){
        TextView startingTimeTextView = findViewById(R.id.event_metadata_starting_time);
        TextView startingDateTextView = findViewById(R.id.event_metadata_starting_date);
        TextView endingTimeTextView = findViewById(R.id.event_metadata_ending_time);
        TextView endingDateTextView = findViewById(R.id.event_metadata_ending_date);
        long startingTime = bundle.getLong("startingTime");
        long endingTime = bundle.getLong("endingTime");

        Calendar calendar = Calendar.getInstance();

        Date startingDate = new Date(startingTime);
        String startingDateString = DateFormat.getDateFormat(getApplicationContext()).format(startingDate);
        String startingTimeString = DateFormat.getTimeFormat(getApplicationContext()).format(startingDate);

        Date endingDate = new Date(endingTime);
        String endingDateString = DateFormat.getDateFormat(getApplicationContext()).format(endingDate);
        String endingTimeString = DateFormat.getTimeFormat(getApplicationContext()).format(endingTime);

        startingTimeTextView.setText(startingTimeString);
        endingTimeTextView.setText(endingTimeString);

        if(calendar.getTimeInMillis() >= startingTime){
            startingTimeTextView.setText(getResources().getText(R.string.now));
            startingTimeTextView.setTextColor(getResources().getColor(R.color.green));
            Animation animBlink = AnimationUtils.loadAnimation(this, R.anim.anim_blink);
            startingTimeTextView.startAnimation(animBlink);
        }else if(calendar.get(Calendar.DAY_OF_MONTH) != startingDate.getDate()){
            startingDateTextView.setText(startingDateString);
            startingDateTextView.setVisibility(View.VISIBLE);
        }

        if(calendar.get(Calendar.DAY_OF_MONTH) != endingDate.getDate()){
            endingDateTextView.setText(endingDateString);
            endingDateTextView.setVisibility(View.VISIBLE);
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


}
