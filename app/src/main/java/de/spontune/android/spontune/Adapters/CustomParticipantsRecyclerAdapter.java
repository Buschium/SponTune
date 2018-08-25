package de.spontune.android.spontune.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import de.spontune.android.spontune.Data.User;
import de.spontune.android.spontune.R;
import de.spontune.android.spontune.UserActivity;

public class CustomParticipantsRecyclerAdapter extends RecyclerView.Adapter<CustomParticipantsRecyclerAdapter.ParticipantViewHolder>{

    private Context context;
    private List<User> users;
    private Map<String, String> following;
    private FirebaseAuth firebaseAuth;
    private boolean isFollowing;

    public CustomParticipantsRecyclerAdapter(Context context, ArrayList<User> users, HashMap<String, String> following){
        this.context = context;
        this.users = users;
        this.following = following;
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ParticipantViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        return new ParticipantViewHolder(inflater.inflate(R.layout.fragment_participant, viewGroup, false), context);
    }

    @Override
    public void onBindViewHolder(@NonNull final ParticipantViewHolder viewHolder, int position){
        User user = users.get(position);
        final String userID = user.getId();
        viewHolder.participantsMetadata.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(context, UserActivity.class);
                intent.putExtra("uid", userID);
                context.startActivity(intent);
            }
        });

        if(following != null && following.containsKey(userID)){
            viewHolder.buttonFollow.setText(R.string.unfollow);
        }

        if(userID.equals(firebaseAuth.getUid())){
            viewHolder.buttonFollow.setVisibility(View.GONE);
        }else{
            viewHolder.buttonFollow.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    if (following == null) {
                        following = new HashMap<>();
                    }
                    DatabaseReference followingReference = FirebaseDatabase.getInstance().getReference().child("users").child(firebaseAuth.getUid()).child("following");
                    DatabaseReference followersReference = FirebaseDatabase.getInstance().getReference().child("users").child(userID).child("followers");
                    if (!following.containsKey(userID)) {
                        following.put(userID, userID);
                        followingReference.child(userID).setValue(userID);
                        followersReference.child(firebaseAuth.getUid()).setValue(firebaseAuth.getUid());
                        viewHolder.buttonFollow.setText(R.string.unfollow);
                    }else{
                        following.remove(userID);
                        followingReference.child(userID).removeValue();
                        followersReference.child(firebaseAuth.getUid()).removeValue();
                        viewHolder.buttonFollow.setText(R.string.follow);
                    }
                }
            });
        }
        viewHolder.bindToPost(user);
    }

    @Override
    public int getItemCount(){
        return users.size();
    }

    public void addItem(User user){
        if(!users.contains(user)) {
            users.add(user);
            notifyItemInserted(users.size() -1);
        }
    }


    public static class ParticipantViewHolder extends RecyclerView.ViewHolder{

        private View itemView;
        private Context context;
        private CircleImageView userImage;
        private TextView username;
        private TextView realName;
        private Button buttonFollow;
        private View participantsMetadata;

        public ParticipantViewHolder(View itemView, Context context){
            super(itemView);
            this.itemView = itemView;
            this.context = context;
            userImage = itemView.findViewById(R.id.user_image);
            username = itemView.findViewById(R.id.text_view_username);
            realName = itemView.findViewById(R.id.text_view_real_name);
            buttonFollow = itemView.findViewById(R.id.button_follow);
            participantsMetadata = itemView.findViewById(R.id.participants_metadata);
        }

        public void bindToPost(User user){
            loadUserImage(user.getId());
            username.setText(user.getUsername());
        }

        /**
         * try to load the profilePicture from firebase. The image is saved under 'images/[userId]'
         */
        private void loadUserImage(String uid){
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.activity_maps_menu_user)
                    .error(R.drawable.activity_maps_menu_user);
            Glide.with(context)
                    .load(FirebaseStorage.getInstance().getReference().child("images/" + uid))
                    .apply(options)
                    .into(userImage);
        }

    }

}
