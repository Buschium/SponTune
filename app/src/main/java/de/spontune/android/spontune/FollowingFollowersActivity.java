package de.spontune.android.spontune;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

import de.spontune.android.spontune.Adapters.CustomParticipantsRecyclerAdapter;
import de.spontune.android.spontune.Data.User;

public class FollowingFollowersActivity extends AppCompatActivity{

    private RecyclerView mRecycler;
    private CustomParticipantsRecyclerAdapter customParticipantsRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participants);

        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(getIntent().getStringExtra("title"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final HashMap<String, String> following = (HashMap<String, String>) getIntent().getSerializableExtra("following");
        final ArrayList<User> users = new ArrayList<>();

        mRecycler = findViewById(R.id.participants_list);
        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        customParticipantsRecyclerAdapter = new CustomParticipantsRecyclerAdapter(this, users, following);
        mRecycler.setAdapter(customParticipantsRecyclerAdapter);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        databaseReference.addChildEventListener(new ChildEventListener(){
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s){
                User user = dataSnapshot.getValue(User.class);
                if(following.containsKey(user.getId())){
                    customParticipantsRecyclerAdapter.addItem(user);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s){

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot){

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s){

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError){
                databaseError.toException().printStackTrace();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finishAfterTransition();
            return true;
        }
        return false;
    }
}
