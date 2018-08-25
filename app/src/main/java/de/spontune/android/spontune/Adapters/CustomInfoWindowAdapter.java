package de.spontune.android.spontune.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import de.spontune.android.spontune.Data.Event;
import de.spontune.android.spontune.R;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Context context;
    private Event event;

    public CustomInfoWindowAdapter(Context ctx){
        context = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity)context).getLayoutInflater().inflate(R.layout.fragment_info, null);

        ImageView categoryImage = view.findViewById(R.id.fragment_image);
        TextView eventTitle = view.findViewById(R.id.fragment_title);
        TextView startingTimeView = view.findViewById(R.id.fragment_time);
        TextView startingDateView = view.findViewById(R.id.fragment_date);
        ImageView startingDateImageView = view.findViewById(R.id.fragment_image_date);

        event = (Event) marker.getTag();
        assert event != null;

        long startingTime = event.getStartingTime();
        long endingTime = event.getEndingTime();
        long nowMillis = Calendar.getInstance().getTimeInMillis();
        Calendar endOfDay = Calendar.getInstance();
        endOfDay.set(Calendar.HOUR_OF_DAY, 23);
        endOfDay.set(Calendar.MINUTE, 59);
        endOfDay.set(Calendar.SECOND, 59);
        long endOfDayMillis = endOfDay.getTimeInMillis();
        if(startingTime < nowMillis){
            String text = convertToHoursAndMinutes(endingTime - nowMillis);
            startingTimeView.setText(context.getResources().getString(R.string.remaining_time_until_end, text));
            startingDateView.setVisibility(View.GONE);
            startingDateImageView.setVisibility(View.GONE);
        }else if(startingTime > nowMillis && startingTime < endOfDayMillis) {
            String text = convertToHoursAndMinutes(startingTime - nowMillis);
            startingTimeView.setText(context.getResources().getString(R.string.remaining_time_until_start, text));
            startingDateView.setVisibility(View.GONE);
            startingDateImageView.setVisibility(View.GONE);
        }

        if(event.getPicture() != null){
            categoryImage.setImageBitmap(event.getPicture());
        }

        eventTitle.setText(event.getSummary());

        return view;
    }

    private String convertToHoursAndMinutes(long millis){
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        return hours + "h " + minutes + "min";
    }

}