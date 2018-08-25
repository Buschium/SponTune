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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.spontune.android.spontune.Adapters.CustomParticipantsRecyclerAdapter;
import de.spontune.android.spontune.Data.User;

public class ParticipantsActivity extends AppCompatActivity{

    private RecyclerView mRecycler;
    private CustomParticipantsRecyclerAdapter customParticipantsRecyclerAdapter;
    private DatabaseReference databaseReference;
    private DatabaseReference userReference;
    private ChildEventListener childEventListener;
    private ValueEventListener valueEventListener;
    private Map<String, String> participants;
    private HashMap<String, String> following;
    private String uid;
    private String creator;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participants);

        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(getResources().getString(R.string.participants));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        participants = (HashMap<String, String>) getIntent().getSerializableExtra("participants");

        uid = getIntent().getStringExtra("uid");
        userReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("following");
        valueEventListener = new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot){
                following = (HashMap<String, String>) dataSnapshot.getValue();
                final ArrayList<User> users = new ArrayList<>();
                mRecycler = findViewById(R.id.participants_list);
                mRecycler.setHasFixedSize(true);
                mRecycler.setLayoutManager(new LinearLayoutManager(ParticipantsActivity.this));
                customParticipantsRecyclerAdapter = new CustomParticipantsRecyclerAdapter(ParticipantsActivity.this, users, following);
                mRecycler.setAdapter(customParticipantsRecyclerAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError){
                //TODO handle database exception
            }
        };

        creator = getIntent().getStringExtra("creator");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        childEventListener = new ChildEventListener(){
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s){
                User user = dataSnapshot.getValue(User.class);
                if(participants.containsKey(user.getId())){
                    if(creator.equals(user.getId())){
                        customParticipantsRecyclerAdapter.addItem(user, 1);
                    }else {
                        customParticipantsRecyclerAdapter.addItem(user);
                    }
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
        };
    }


    @Override
    protected void onPause(){
        super.onPause();
        databaseReference.removeEventListener(childEventListener);
        userReference.removeEventListener(valueEventListener);
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(valueEventListener != null){
            userReference.addListenerForSingleValueEvent(valueEventListener);
        }
        if(childEventListener != null) {
            databaseReference.addChildEventListener(childEventListener);
        }
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
