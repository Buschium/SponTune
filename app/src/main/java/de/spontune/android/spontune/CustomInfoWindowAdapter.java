package de.spontune.android.spontune;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import de.spontune.android.spontune.Data.Event;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Context context;
    private ImageView categoryImage;

    public CustomInfoWindowAdapter(Context ctx){
        context = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity)context).getLayoutInflater().inflate(R.layout.fragment_info, null);

        categoryImage = view.findViewById(R.id.fragment_image);
        TextView eventTitle = view.findViewById(R.id.fragment_title);
        TextView startingTime = view.findViewById(R.id.fragment_time);
        TextView startingDate = view.findViewById(R.id.fragment_date);
        //CardView itemFragment = view.findViewById(R.id.item_fragment);

        Event event = (Event) marker.getTag();
        switch(event.getCategory()){
            case 1:
                loadImage("creative");
                //itemFragment.setBackgroundColor(context.getResources().getColor(R.color.creative));
                break;
            case 2:
                //loadImage("party");
                //itemFragment.setCardBackgroundColor(context.getResources().getColor(R.color.party));
                break;
            case 3:
                loadImage("happening");
                //itemFragment.setCardBackgroundColor(context.getResources().getColor(R.color.happening));
                break;
            default:
                loadImage("sports");
                //itemFragment.setCardBackgroundColor(context.getResources().getColor(R.color.sports));
                break;
        }
        eventTitle.setText(event.getSummary());
        startingTime.setText(DateFormat.getTimeFormat(context.getApplicationContext()).format(new Date(event.getStartingTime())));
        startingDate.setText(DateFormat.getDateFormat(context.getApplicationContext()).format(new Date(event.getStartingTime())));

        try {
            final File localFile = File.createTempFile("categoryImages", "jpg");
            FirebaseStorage.getInstance().getReference().child("categoryImages/party.jpg").getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    categoryImage.setImageBitmap(bitmap);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    exception.printStackTrace();
                }
            });
        } catch (IOException e ) {}

        return view;
    }

    private void loadImage(String categoryName){
        try {
            final File localFile = File.createTempFile("categoryImages", "jpg");
            FirebaseStorage.getInstance().getReference().child("categoryImages/" + categoryName + ".jpg").getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    categoryImage.setImageBitmap(bitmap);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    exception.printStackTrace();
                }
            });
        } catch (IOException e ) {}
    }
}
