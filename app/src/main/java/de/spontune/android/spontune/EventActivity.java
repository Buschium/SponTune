package de.spontune.android.spontune;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class EventActivity extends AppCompatActivity {

    private TextView mEventDescriptionTextView;
    private View gradientView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle bundle = getIntent().getExtras();
        assert bundle != null;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        TextView toolbarTitle = findViewById(R.id.title_text);
        toolbarTitle.setText(bundle.getString("summary"));
        ImageView categoryImage = findViewById(R.id.category_image);

        ImageView toolbarImage = findViewById(R.id.avatar_image);
        switch (bundle.getInt("category")){
            case 1:
                //toolbarImage.setBackgroundColor(getResources().getColor(R.color.foodAndDrink));
                toolbarImage.setImageResource(R.drawable.category_food_and_drink_light);
                break;
            case 2:
                toolbarImage.setBackgroundColor(getResources().getColor(R.color.party));
                toolbarImage.setImageResource(R.drawable.category_party_light);
                categoryImage.setImageResource(R.drawable.party_default);
                break;
            case 3:
                toolbarImage.setBackgroundColor(getResources().getColor(R.color.music));
                toolbarImage.setImageResource(R.drawable.category_music_light);
                break;
            default:
                toolbarImage.setBackgroundColor(getResources().getColor(R.color.sports));
                toolbarImage.setImageResource(R.drawable.category_sports_light);
                break;
        }

        /*
        Button joinEventButton = findViewById(R.id.join_event_button);
        joinEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        /*
        if(bundle.getString("uid") != null && bundle.getString("uid").equals(bundle.getString("creator"))){
            joinEventButton.setText(R.string.edit_event);
        }
        */

        ImageButton btnClose = findViewById(R.id.event_close);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAfterTransition();
            }
        });


        mEventDescriptionTextView = findViewById(R.id.event_description_textview);
        mEventDescriptionTextView.setText(bundle.getString("description"));
        gradientView = findViewById(R.id.gradient);
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
}
