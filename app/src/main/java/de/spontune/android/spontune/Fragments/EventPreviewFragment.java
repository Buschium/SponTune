package de.spontune.android.spontune.Fragments;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import de.spontune.android.spontune.R;

import static android.app.Activity.RESULT_OK;

public class EventPreviewFragment extends Fragment {

    private ImageButton categoryImage;
    public TextView mEventDescriptionTextView;
    public TextView mStartingTimeTextView;
    public TextView mStartingDateTextView;
    public TextView mEndingTimeTextView;
    public TextView mEndingDateTextView;

    private final int PICK_IMAGE_REQUEST = 71;
    public Uri filePath;

    private View gradientView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_event_preview, container, false);

        categoryImage = rootView.findViewById(R.id.category_image);

        mEventDescriptionTextView = rootView.findViewById(R.id.event_description_textview);
        gradientView = rootView.findViewById(R.id.gradient);

        categoryImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        //The description expands when the user clicks on it (or shrinks when it's already expanded)
        mEventDescriptionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int collapsedMaxLines = 7;
                ObjectAnimator animation = ObjectAnimator.ofInt(mEventDescriptionTextView, "maxLines", mEventDescriptionTextView.getMaxLines() == collapsedMaxLines? 20 : collapsedMaxLines);
                animation.setInterpolator(new DecelerateInterpolator());
                animation.setDuration(200).start();
                if(mEventDescriptionTextView.getMaxLines() == collapsedMaxLines) {
                    gradientView.setBackground(null);
                }else{
                    gradientView.setBackground(getActivity().getDrawable(R.drawable.transparent_to_light_surface_gradient));
                }
            }
        });

        mStartingTimeTextView = rootView.findViewById(R.id.event_metadata_starting_time);
        mStartingDateTextView = rootView.findViewById(R.id.event_metadata_starting_date);
        mEndingTimeTextView = rootView.findViewById(R.id.event_metadata_ending_time);
        mEndingDateTextView = rootView.findViewById(R.id.event_metadata_ending_date);

        return rootView;
    }


    /**
     * standard image chooser for the profile picture
     */
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE_REQUEST);
    }

    /**
     * result for the image chooser
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                categoryImage.setImageBitmap(bitmap);
                categoryImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

}
