package de.spontune.android.spontune.Data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import de.spontune.android.spontune.R;

public class EventViewHolder extends RecyclerView.ViewHolder {
    public View itemView;
    private TextView titleView;
    private ImageView categoryImageView;
    private TextView startingTimeView;
    private TextView startingDateView;
    private CardView itemFragment;
    private Context context;

    public EventViewHolder(View itemView, Context context) {
        super(itemView);
        this.itemView = itemView;
        this.context = context;
        titleView = itemView.findViewById(R.id.fragment_title);
        categoryImageView = itemView.findViewById(R.id.fragment_image);
        startingTimeView = itemView.findViewById(R.id.fragment_time);
        startingDateView = itemView.findViewById(R.id.fragment_date);
        itemFragment = itemView.findViewById(R.id.item_fragment);
    }

    public void bindToPost(Event event) {
        Date date = new Date(event.getStartingTime());
        titleView.setText(event.getSummary());
        startingTimeView.setText(DateFormat.getTimeFormat(context.getApplicationContext()).format(date));
        startingDateView.setText(DateFormat.getDateFormat(context.getApplicationContext()).format(date));
        switch (event.getCategory()) {
            case 1:
                loadImage("creative", event);
                itemFragment.setCardBackgroundColor(context.getResources().getColor(R.color.creative));
                break;
                case 2:
                    loadImage("party", event);
                    itemFragment.setCardBackgroundColor(context.getResources().getColor(R.color.party));
                    break;
                case 3:
                    loadImage("happening", event);
                    itemFragment.setCardBackgroundColor(context.getResources().getColor(R.color.happening));
                    break;
                default:
                    loadImage("sports", event);
                    itemFragment.setCardBackgroundColor(context.getResources().getColor(R.color.sports));
                    break;
            }
    }


    private void loadImage(final String categoryName, final Event event){
        if(event.getPicture() != null){
            categoryImageView.setImageBitmap(event.getPicture());
        }else{
            try{
                final File localFile = File.createTempFile("categoryImages", "jpg");
                FirebaseStorage.getInstance().getReference().child("categoryImages/" + event.getID()).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                        categoryImageView.setImageBitmap(bitmap);
                        event.setPicture(bitmap);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        FirebaseStorage.getInstance().getReference().child("categoryImages/" + categoryName + ".jpg").getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                categoryImageView.setImageBitmap(bitmap);
                                event.setPicture(bitmap);
                            }
                        });
                    }
                });
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
