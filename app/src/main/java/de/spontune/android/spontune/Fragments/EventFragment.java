package de.spontune.android.spontune.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

import de.spontune.android.spontune.Data.Event;
import de.spontune.android.spontune.R;


public abstract class EventFragment extends android.support.v4.app.Fragment {

    private DatabaseReference mDatabase;
    private CustomFirebaseRecyclerAdapter mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;
    private FirebaseAuth firebaseAuth;

    private List<Event> eventList;

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
        firebaseAuth = FirebaseAuth.getInstance();

        eventList = new ArrayList<>();
        ArrayList<Event> eventListNew = new ArrayList<>();
        mAdapter = new CustomFirebaseRecyclerAdapter(getActivity(), eventListNew);
        mRecycler.setAdapter(mAdapter);

        postsQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Event event = dataSnapshot.getValue(Event.class);
                eventList.add(event);
                mAdapter.addItem(event, eventList.indexOf(event), eventList.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public abstract Query getQuery(DatabaseReference databaseReference);

    public void onCategorySelected(boolean creativeActivated, boolean partyActivated, boolean happeningActivated, boolean sportsActivated){
        boolean allCategoriesDeactivated = !creativeActivated && !partyActivated && !happeningActivated && !sportsActivated;
        for(Event event : eventList){
            int category = event.getCategory();
            if(category == 1 && creativeActivated || category == 2 && partyActivated || category == 3 && happeningActivated || category == 4 && sportsActivated || allCategoriesDeactivated){
                mAdapter.addItem(event, eventList.indexOf(event), eventList.size() - 1);
            }else{
                mAdapter.removeItem(event);
            }
        }
        mRecycler.smoothScrollToPosition(0);
    }
}