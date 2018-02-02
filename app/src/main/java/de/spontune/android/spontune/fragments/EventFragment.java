package de.spontune.android.spontune.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import de.spontune.android.spontune.Data.Event;
import de.spontune.android.spontune.Data.EventViewHolder;
import de.spontune.android.spontune.EventActivity;
import de.spontune.android.spontune.R;


public abstract class EventFragment extends android.support.v4.app.Fragment {

    private DatabaseReference mDatabase;
    private FirebaseRecyclerAdapter<Event, EventViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;

    public EventFragment() {}

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_recycler, container, false);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mRecycler = rootView.findViewById(R.id.events_list);
        mRecycler.setHasFixedSize(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mManager = new LinearLayoutManager(getActivity());
        mRecycler.setLayoutManager(mManager);

        Query postsQuery = getQuery(mDatabase);

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Event>().setQuery(postsQuery, Event.class).build();

        mAdapter = new FirebaseRecyclerAdapter<Event, EventViewHolder>(options) {

            @Override
            public EventViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new EventViewHolder(inflater.inflate(R.layout.fragment_list, viewGroup, false), viewGroup.getContext());
            }

            @Override
            protected void onBindViewHolder(EventViewHolder viewHolder, int position, final Event event) {
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getActivity(), EventActivity.class);
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
                        b.putInt("currentPersons", event.getCurrentPersons());
                        b.putString("address", event.getAddress());
                        i.putExtras(b);
                        startActivityForResult(i, 1);
                    }
                });
                viewHolder.bindToPost(event);
            }
        };
        mRecycler.setAdapter(mAdapter);
    }


    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    public abstract Query getQuery(DatabaseReference databaseReference);
}