package de.spontune.android.spontune.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.spontune.android.spontune.Data.Event;
import de.spontune.android.spontune.EventActivity;
import de.spontune.android.spontune.R;

public class CustomFirebaseRecyclerAdapter extends RecyclerView.Adapter<EventViewHolder>{

    private Context mContext;
    private List<Event> mEventList;

    public CustomFirebaseRecyclerAdapter(Context mContext, List<Event> mEventList){
        this.mEventList = mEventList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        return new EventViewHolder(inflater.inflate(R.layout.fragment_list, viewGroup, false), mContext);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder viewHolder, int position) {
        final Event event = mEventList.get(position);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext, EventActivity.class);
                Bundle b = new Bundle();
                b.putString("id", event.getID());
                b.putString("creator", event.getCreator());
                b.putDouble("lat", event.getLat());
                b.putDouble("lng", event.getLng());
                b.putString("summary", event.getSummary());
                b.putString("description", event.getDescription());
                b.putLong("startingTime", event.getStartingTime());
                b.putLong("endingTime", event.getEndingTime());
                b.putInt("category", event.getCategory());
                b.putInt("maxPersons", event.getMaxPersons());
                b.putString("address", event.getAddress());
                i.putExtras(b);
                i.putExtra("participants", event.getParticipants());
                mContext.startActivity(i);
            }
        });
        viewHolder.bindToPost(event);
    }

    @Override
    public int getItemCount(){
        return mEventList.size();
    }

    public void removeItem(Event event){
        if(mEventList.contains(event)) {
            int position = mEventList.indexOf(event);
            mEventList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mEventList.size());
        }
    }

    public void removeItem(int position){
        if(mEventList.get(position) != null){
            mEventList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mEventList.size());
        }
    }

    public void addItem(Event event, int position, int size){
        if(!mEventList.contains(event)) {
            if(position > mEventList.size()) {
                mEventList.add(event);
            }else{
                mEventList.add(position, event);
            }
            notifyItemInserted(position);
        }
    }
}
