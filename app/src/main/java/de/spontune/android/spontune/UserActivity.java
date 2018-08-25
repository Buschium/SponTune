package de.spontune.android.spontune;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.spontune.android.spontune.Data.User;

public class UserActivity extends AppCompatActivity {

    private User user;
    private String uid;
    private boolean imageLoaded;
    private boolean isFollowing;
    private HashMap<String, String> following;
    private HashMap<String, String> followers;
    private Map<String, String> visitorFollowing;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user);
        Slide slide = new Slide();
        slide.setSlideEdge(Gravity.START);
        getWindow().setExitTransition(slide);
        slide.setSlideEdge(Gravity.END);
        getWindow().setEnterTransition(slide);

        firebaseAuth = FirebaseAuth.getInstance();
        assert firebaseAuth != null;

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
        uid = getIntent().getStringExtra("uid");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                TextView tvUsername = findViewById(R.id.tv_username);
                tvUsername.setText(user.getUsername());
                TextView tvDescription = findViewById(R.id.tv_description);
                tvDescription.setText(user.getUserDescription());
                following = user.getFollowing();
                followers = user.getFollowers();
                // if we have the image form the editUserActivity we don't have to load it
                if(!imageLoaded) {
                    loadUserImage(user.getId());
                }
                setUpFollowingAndFollowers();
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

        final Button btnEdit = findViewById(R.id.edit_button);


        if(!uid.equals(FirebaseAuth.getInstance().getUid())){
            final DatabaseReference followingReference = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getUid()).child("following");
            final DatabaseReference followersReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("followers");
            followingReference.addListenerForSingleValueEvent(new ValueEventListener(){
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot){
                    visitorFollowing = (HashMap<String, String>) dataSnapshot.getValue();
                    if(visitorFollowing == null){
                        isFollowing = false;
                        visitorFollowing = new HashMap<>();
                        btnEdit.setText(R.string.follow);
                    }else if(!visitorFollowing.containsKey(uid)){
                        isFollowing = false;
                        btnEdit.setText(R.string.follow);
                    }else{
                        isFollowing = true;
                        btnEdit.setText(R.string.unfollow);
                    }
                    btnEdit.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view){
                            if(!isFollowing) {
                                visitorFollowing.put(uid, uid);
                                followingReference.child(uid).setValue(uid);
                                followersReference.child(firebaseAuth.getUid()).setValue(firebaseAuth.getUid());
                                isFollowing = true;
                                btnEdit.setText(R.string.unfollow);
                            }else{
                                visitorFollowing.remove(uid);
                                followingReference.child(uid).removeValue();
                                followersReference.child(firebaseAuth.getUid()).removeValue();
                                isFollowing = false;
                                btnEdit.setText(R.string.follow);
                            }
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError){
                    //TODO handle database exception
                }
            });


        }

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserActivity.this, EditUserActivity.class).putExtra("justRegistered", false));
            }
        });

    }

    private void setUpFollowingAndFollowers(){
        View followingView = findViewById(R.id.following);
        View followersView = findViewById(R.id.followers);
        TextView followingTextView = findViewById(R.id.following_count);
        TextView followersTextView = findViewById(R.id.followers_count);

        if(following != null){
            String string = "" + following.size();
            followingTextView.setText(string);
            if(following.size() != 0){
                followingView.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        Intent intent = new Intent(UserActivity.this, FollowingFollowersActivity.class);
                        intent.putExtra("title", getResources().getString(R.string.following));
                        intent.putExtra("following", following);
                        startActivity(intent);
                    }
                });
            }
        }else{
            followingTextView.setText("0");
        }

        if(followers != null){
            String string = "" + followers.size();
            followersTextView.setText(string);
            if(followers.size() != 0){
                followersView.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        Intent intent = new Intent(UserActivity.this, FollowingFollowersActivity.class);
                        intent.putExtra("title", getResources().getString(R.string.followers));
                        intent.putExtra("following", followers);
                        startActivity(intent);
                    }
                });
            }
        }else{
            followersTextView.setText("0");
        }

    }

    /**
     * try to load the profilePicture from firebase. The image is saved under 'images/[userId]'
     */
    private void loadUserImage(String uid){
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.activity_maps_menu_user)
                .error(R.drawable.activity_maps_menu_user);
        Glide.with(this)
                .load(FirebaseStorage.getInstance().getReference().child("images/" + uid))
                .apply(options)
                .into((ImageView) findViewById(R.id.profile_image));
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
