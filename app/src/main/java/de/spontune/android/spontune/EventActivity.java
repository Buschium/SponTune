package de.spontune.android.spontune;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.spontune.android.spontune.R;

import java.util.Calendar;
import java.util.GregorianCalendar;


public class EventActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle bundle = getIntent().getExtras();
        assert bundle != null;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setPadding(0,0,0,0);
        toolbar.setContentInsetsAbsolute(0,0);
        toolbar.setTitle(bundle.getString("summary"));
        setSupportActionBar(toolbar);

        Toolbar bottomToolbar = findViewById(R.id.event_toolbar_bottom);
        bottomToolbar.setPadding(0,0,0,0);
        bottomToolbar.setContentInsetsAbsolute(0,0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView mEventTimeTextView = findViewById(R.id.event_metadata_time);
        long startingTime = bundle.getLong("startingTime");
        long endingTime = bundle.getLong("endingTime");

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTimeInMillis(startingTime);
        int startingHour = calendar.get(Calendar.HOUR_OF_DAY);
        int startingMinute = calendar.get(Calendar.MINUTE);

        calendar.setTimeInMillis(endingTime);
        int endingHour = calendar.get(Calendar.HOUR_OF_DAY);
        int endingMinute = calendar.get(Calendar.MINUTE);

        mEventTimeTextView.setText(getResources().getString(R.string.activity_event_time,
                startingHour,
                startingMinute,
                endingHour,
                endingMinute));

        TextView mEventPeopleTextView = findViewById(R.id.event_metadata_people);
        mEventPeopleTextView.setText(getResources().getString(R.string.activity_event_people,
                bundle.getInt("currentPersons"),
                bundle.getInt("maxPersons")));

        TextView mEventDescriptionTextView = findViewById(R.id.event_description_textview);
        mEventDescriptionTextView.setText(bundle.getString("description"));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is NOT part of this app's task, so create a new task
                    // when navigating up, with a synthesized back stack.
                    TaskStackBuilder.create(this)
                            // Add all of this activity's parents to the back stack
                            .addNextIntentWithParentStack(upIntent)
                            // Navigate up to the closest parent
                            .startActivities();
                } else {
                    // This activity is part of this app's task, so simply
                    // navigate up to the logical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
