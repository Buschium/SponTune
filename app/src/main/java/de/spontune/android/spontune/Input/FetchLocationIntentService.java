package de.spontune.android.spontune.Input;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.JobIntentService;
import android.os.ResultReceiver;
import android.util.Log;

import com.spontune.android.spontune.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class FetchLocationIntentService extends JobIntentService {

    private static final String TAG = FetchLocationIntentService.class.getSimpleName();
    static final int JOB_ID = 1000;
    private static final int SUCCESS_RESULT = 0;
    private final Handler mHandler = new Handler();
    protected ResultReceiver mReceiver;

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, FetchLocationIntentService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(Intent intent) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String errorMessage = "";

        // Get the location passed to this service through an extra.
        String address = intent.getStringExtra("address");
        mReceiver = intent.getParcelableExtra("resultReceiver");
        List<Address> locations = null;

        try {
            locations = geocoder.getFromLocationName(address,1);
        } catch (IOException ioException) {
            errorMessage = getString(R.string.service_not_available);
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            errorMessage = getString(R.string.invalid_address_used);
            Log.e(TAG, errorMessage + ". Address = " + address, illegalArgumentException);
        }

        // Handle case where no address was found.
        if (locations == null || locations.size() == 0){
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_location_found);
                Log.e(TAG, errorMessage);
            }
        } else {
            Address location = locations.get(0);
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            deliverResultToReceiver(SUCCESS_RESULT, lat, lng);
        }
    }

    private void deliverResultToReceiver(int resultCode, double lat, double lng) {
        Bundle bundle = new Bundle();
        bundle.putDouble("lat", lat);
        bundle.putDouble("lng", lng);
        mReceiver.send(resultCode, bundle);
    }

}
