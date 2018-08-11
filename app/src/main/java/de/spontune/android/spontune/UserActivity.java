package de.spontune.android.spontune;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

import de.spontune.android.spontune.Data.User;

public class UserActivity extends AppCompatActivity {

    private User user;
    private boolean imageLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user);

        // if we changed the image in the @EditUserActivity.java we have to load it via the intent due to lag from the Server
        String path = getIntent().getStringExtra("image");
        if(path!=null){
            Uri filePath = Uri.parse(path);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                ((ImageView) findViewById(R.id.profile_image)).setImageBitmap(bitmap);
                imageLoaded = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                TextView tvUsername = findViewById(R.id.tv_username);
                tvUsername.setText(user.getUsername());
                TextView tvDescription = findViewById(R.id.tv_description);
                tvDescription.setText(user.getUserDescription());
                // if we have the image form the editUserActivity we don't have to load it
                if(!imageLoaded) {
                    loadImage();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ActionBar mActionBar = getSupportActionBar();
        if(mActionBar != null) {
            LayoutInflater mLayoutInflater = LayoutInflater.from(this);
            View customView = mLayoutInflater.inflate(R.layout.activity_user_menu, null);
            mActionBar.setCustomView(customView, new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mActionBar.setDisplayShowCustomEnabled(true);

            Toolbar mParent = (Toolbar) customView.getParent();
            mParent.setPadding(0, 0, 0, 0);
            mParent.setContentInsetsAbsolute(0, 0);
        }

        Button btnEdit = findViewById(R.id.edit_button);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserActivity.this, EditUserActivity.class).putExtra("justRegistered", false));
            }
        });
    }

    /**
     * try to load the profilePicture from firebase. The image is saved under 'images/[userId]'
     */
    private void loadImage(){
        try {
            final File localFile = File.createTempFile("images", "jpg");
            FirebaseStorage.getInstance().getReference().child("images/"+ user.getId()).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    ((ImageView) findViewById(R.id.profile_image)).setImageBitmap(bitmap);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                }
            });
        } catch (IOException e ) {}
    }
}
