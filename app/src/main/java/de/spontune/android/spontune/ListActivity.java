package de.spontune.android.spontune;


import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import de.spontune.android.spontune.Data.Event;
import de.spontune.android.spontune.Data.EventViewHolder;

public class ListActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        EventPagerAdapter mEventPagerAdapter = new EventPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.viewpager);
        mViewPager.setAdapter(mEventPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }


    /**
     * A fragment for containing the most important information about an event.
     */
    public static class EventFragment extends Fragment {

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

        public Query getQuery(DatabaseReference databaseReference){
            return databaseReference.child("events").limitToFirst(100).orderByChild("startingTime");
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class EventPagerAdapter extends FragmentPagerAdapter {
        private final Fragment[] mFragments = new Fragment[] {
                new EventFragment(),
                new EventFragment(),
                new EventFragment(),
        };

        public EventPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            //return mFragmentList.get(position);
            return new EventFragment();
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.today);
                case 1:
                    return getString(R.string.tomorrow);
                case 2:
                    return getString(R.string.week);
            }
            return null;
        }
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onResume(){
        super.onResume();
    }
}
