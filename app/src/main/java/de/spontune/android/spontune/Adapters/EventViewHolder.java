package de.spontune.android.spontune.Adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import de.spontune.android.spontune.Data.Event;
import de.spontune.android.spontune.R;

public class EventViewHolder extends RecyclerView.ViewHolder {
    public View itemView;
    private TextView titleView;
    private ImageView categoryImageView;
    private TextView startingTimeView;
    private ImageView startingDateImageView;
    private TextView startingDateView;
    private CardView itemFragment;
    private Context context;

    public EventViewHolder(View itemView, Context context) {
        super(itemView);
        this.itemView = itemView;
        this.context = context;
        titleView = itemView.findViewById(R.id.fragment_title);
        categoryImageView = itemView.findViewById(R.id.fragment_image);
        startingTimeView = itemView.findViewById(R.id.fragment_time);
        startingDateView = itemView.findViewById(R.id.fragment_date);
        startingDateImageView = itemView.findViewById(R.id.fragment_image_date);
        itemFragment = itemView.findViewById(R.id.item_fragment);
    }

    public void bindToPost(Event event) {
        Date date = new Date(event.getStartingTime());
        long startingTime = event.getStartingTime();
        long endingTime = event.getEndingTime();
        long nowMillis = Calendar.getInstance().getTimeInMillis();
        Calendar endOfDay = Calendar.getInstance();
        endOfDay.set(Calendar.HOUR_OF_DAY, 23);
        endOfDay.set(Calendar.MINUTE, 59);
        endOfDay.set(Calendar.SECOND, 59);
        Calendar startOfTomorrow = Calendar.getInstance();
        startOfTomorrow.add(Calendar.DATE, 1);
        startOfTomorrow.set(Calendar.HOUR_OF_DAY, 0);
        startOfTomorrow.set(Calendar.MINUTE, 0);
        startOfTomorrow.set(Calendar.SECOND, 0);
        Calendar endOfTomorrow = Calendar.getInstance();
        endOfTomorrow.add(Calendar.DATE, 1);
        endOfTomorrow.set(Calendar.HOUR_OF_DAY, 23);
        endOfTomorrow.set(Calendar.MINUTE, 59);
        endOfTomorrow.set(Calendar.SECOND, 59);
        long endOfDayMillis = endOfDay.getTimeInMillis();
        long startOfTomorrowMillis = startOfTomorrow.getTimeInMillis();
        long endOfTomorrowMillis = endOfTomorrow.getTimeInMillis();
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
        }else if(startingTime > startOfTomorrowMillis && startingTime < endOfTomorrowMillis){
            String text = convertToHoursAndMinutes(startingTime - nowMillis);
            startingTimeView.setText(context.getResources().getString(R.string.remaining_time_until_start, text));
            startingDateView.setVisibility(View.GONE);
            startingDateImageView.setVisibility(View.GONE);
        }else{
            startingTimeView.setText(DateFormat.getTimeFormat(context.getApplicationContext()).format(date));
            startingDateView.setText(DateFormat.getDateFormat(context.getApplicationContext()).format(date));
        }
        titleView.setText(event.getSummary());

        switch (event.getCategory()) {
            case 1:
                loadImage("creative", event);
                itemFragment.setCardBackgroundColor(context.getResources().getColor(R.color.creative));
                break;
            case 2:
                loadImage("party", event);
                itemFragment.setCardBackgroundColor(context.getResources().getColor(R.color.party));
                break;
            case 3:
                loadImage("happening", event);
                itemFragment.setCardBackgroundColor(context.getResources().getColor(R.color.happening));
                break;
            default:
                loadImage("sports", event);
                itemFragment.setCardBackgroundColor(context.getResources().getColor(R.color.sports));
                break;
        }
    }

    private void loadImage(final String categoryName, final Event event){
        Glide.with(context)
                .load(FirebaseStorage.getInstance().getReference().child("categoryImages/" + event.getID()))
                .error(Glide.with(context).load(FirebaseStorage.getInstance().getReference().child("categoryImages/" + categoryName + ".jpg")))
                .into(categoryImageView);
    }

    private String convertToHoursAndMinutes(long millis){
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        return hours + "h " + minutes + "min";
    }
}
