package de.spontune.android.spontune;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;


public class EventActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle bundle = getIntent().getExtras();
        assert bundle != null;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(bundle.getString("summary"));
        ImageView categoryImage = findViewById(R.id.category_image);

        ImageView toolbarImage = findViewById(R.id.toolbar_image);
        switch (bundle.getInt("category")){
            case 1:
                toolbarImage.setBackgroundColor(getResources().getColor(R.color.foodAndDrink));
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

        final Toolbar topToolbar = findViewById(R.id.toolbar_top);
        topToolbar.setTitle("");
        setSupportActionBar(topToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AppBarLayout appBarLayout = findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    setSupportActionBar(toolbar);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    isShow = true;
                } else if(isShow) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    setSupportActionBar(topToolbar);
                    isShow = false;
                }
            }
        });

        Toolbar bottomToolbar = findViewById(R.id.event_toolbar_bottom);
        bottomToolbar.setPadding(0,0,0,0);
        bottomToolbar.setContentInsetsAbsolute(0,0);

        setUpTimeAndDate(bundle);

        TextView maxPersonsTextView = findViewById(R.id.event_metadata_max_persons);
        maxPersonsTextView.setText(String.format(Locale.getDefault(), "%d", bundle.getInt("maxPersons")));

        TextView currentPersonsTextView = findViewById(R.id.event_metadata_current_persons);
        currentPersonsTextView.setText(String.format(Locale.getDefault(), "%d", bundle.getInt("currentPersons")));

        if(bundle.getString("address") != null) {
            ImageView locationImageView = findViewById(R.id.location_image_view);
            locationImageView.setVisibility(View.VISIBLE);
            TextView locationTextView = findViewById(R.id.location_text_view);
            locationTextView.setText(bundle.getString("address"));
            locationTextView.setVisibility(View.VISIBLE);
        }

        TextView mEventDescriptionTextView = findViewById(R.id.event_description_textview);
        mEventDescriptionTextView.setText(bundle.getString("description"));
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

        if(calendar.get(Calendar.DAY_OF_MONTH) != startingDate.getDate() || startingDate.getDate() != endingDate.getDate()){
            findViewById(R.id.image_view_starting_date).setVisibility(View.VISIBLE);
            findViewById(R.id.image_view_ending_date).setVisibility(View.VISIBLE);
            startingDateTextView.setText(startingDateString);
            startingDateTextView.setVisibility(View.VISIBLE);
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
