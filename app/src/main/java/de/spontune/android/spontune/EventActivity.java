package de.spontune.android.spontune;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;


public class EventActivity extends AppCompatActivity {

    private TextView mEventDescriptionTextView;
    private ImageView categoryImage;
    //TODO apply gradient only if the description is too long
    private View gradientView;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private String eventID;
    private String uid;
    private boolean isParticipating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle bundle = getIntent().getExtras();
        assert bundle != null;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        TextView toolbarTitle = findViewById(R.id.title_text);
        toolbarTitle.setText(bundle.getString("summary"));
        categoryImage = findViewById(R.id.category_image);

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        eventID = bundle.getString("id");
        databaseReference = firebaseDatabase.getReference().child("events").child(eventID).child("participants");
        uid = firebaseAuth.getUid();

        ImageView toolbarImage = findViewById(R.id.avatar_image);
        switch (bundle.getInt("category")){
            case 1:
                toolbarImage.setImageResource(R.drawable.category_creative_light);
                loadImage("creative");
                break;
            case 2:
                toolbarImage.setImageResource(R.drawable.category_party_light);
                loadImage("party");
                break;
            case 3:
                toolbarImage.setImageResource(R.drawable.category_happening_light);
                loadImage("happening");
                break;
            default:
                toolbarImage.setImageResource(R.drawable.category_sports_light);
                loadImage("sports");
                break;
        }

        final HashMap<String, String> participants = (HashMap<String, String>) getIntent().getSerializableExtra("participants");
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

        ImageButton btnClose = findViewById(R.id.event_close);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAfterTransition();
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

        //The description expands when the user clicks on it (or shrinks when it's already expanded)
        mEventDescriptionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int collapsedMaxLines = 7;
                ObjectAnimator animation = ObjectAnimator.ofInt(mEventDescriptionTextView, "maxLines", mEventDescriptionTextView.getMaxLines() == collapsedMaxLines? 20 : collapsedMaxLines);
                animation.setInterpolator(new DecelerateInterpolator());
                animation.setDuration(200).start();
                if(mEventDescriptionTextView.getMaxLines() == collapsedMaxLines) {
                    gradientView.setBackground(null);
                }else{
                    gradientView.setBackground(getDrawable(R.drawable.transparent_to_light_surface_gradient));
                }
            }
        });

        setUpTimeAndDate(bundle);

        if(bundle.getString("address") != null) {
            TextView locationTextView = findViewById(R.id.subtitle_text);
            locationTextView.setText(bundle.getString("address"));
        }

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

        Calendar calendar = GregorianCalendar.getInstance();

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

        if(startingDate.getDate() != endingDate.getDate()){
            endingDateTextView.setText(endingDateString);
            endingDateTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadImage(final String categoryName){
        if(getIntent().getSerializableExtra("picture") != null){
            categoryImage.setImageBitmap((Bitmap) getIntent().getParcelableExtra("picture"));
        }
        try {
            final File localFile = File.createTempFile("categoryImages", "jpg");
            FirebaseStorage.getInstance().getReference().child("categoryImages/" + eventID).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    categoryImage.setImageBitmap(bitmap);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    FirebaseStorage.getInstance().getReference().child("categoryImages/" + categoryName + ".jpg").getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                            categoryImage.setImageBitmap(bitmap);
                        }
                    });
                }
            });
        } catch (IOException e ) {}
    }

}
