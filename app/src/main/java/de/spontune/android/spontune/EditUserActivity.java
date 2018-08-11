package de.spontune.android.spontune;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.spontune.android.spontune.Data.User;

public class EditUserActivity extends AppCompatActivity {

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_user);

        final boolean justRegistered = getIntent().getExtras().getBoolean("justRegistered");
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        final EditText etUsername = findViewById(R.id.et_username);
        final EditText etDescription = findViewById(R.id.et_description);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                etUsername.setText(user.getUsername());
                etDescription.setText(user.getUserDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Button btnSave = findViewById(R.id.save_button);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user.setUsername(etUsername.getText().toString());
                user.setUserDescription(etDescription.getText().toString());
                databaseReference.setValue(user);
                if(justRegistered) {
                    startActivity(new Intent(EditUserActivity.this, MapsActivity.class));
                }else{
                    startActivity(new Intent(EditUserActivity.this, UserActivity.class));
                }
            }
        });

    }
}
