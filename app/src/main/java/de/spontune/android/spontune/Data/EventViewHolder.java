package de.spontune.android.spontune.Data;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;

import de.spontune.android.spontune.R;

public class EventViewHolder extends RecyclerView.ViewHolder {
    public TextView titleView;
    public ImageView categoryImageView;
    public TextView startingTimeView;
    public TextView startingDateView;
    private Context context;

    public EventViewHolder(View itemView, Context context) {
        super(itemView);
        this.context = context;
        titleView = itemView.findViewById(R.id.fragment_title);
        categoryImageView = itemView.findViewById(R.id.fragment_image);
        startingTimeView = itemView.findViewById(R.id.fragment_time);
        startingDateView = itemView.findViewById(R.id.fragment_date);
    }

    public void bindToPost(Event event) {
        Date date = new Date(event.getStartingTime());
        titleView.setText(event.getSummary());
        startingTimeView.setText(DateFormat.getTimeFormat(context.getApplicationContext()).format(date));
        startingDateView.setText(DateFormat.getDateFormat(context.getApplicationContext()).format(date));
        switch(event.getCategory()){
            case 1:
                categoryImageView.setBackgroundColor(context.getResources().getColor(R.color.foodAndDrink));
                categoryImageView.setImageResource(R.drawable.category_food_and_drink_light);
                break;
            case 2:
                categoryImageView.setBackgroundColor(context.getResources().getColor(R.color.party));
                categoryImageView.setImageResource(R.drawable.category_party_light);
                break;
            case 3:
                categoryImageView.setBackgroundColor(context.getResources().getColor(R.color.music));
                categoryImageView.setImageResource(R.drawable.category_music_light);
                break;
            default:
                categoryImageView.setBackgroundColor(context.getResources().getColor(R.color.sports));
                categoryImageView.setImageResource(R.drawable.category_sports_light);
                break;
        }
    }
}
