package de.spontune.android.spontune.Fragments;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private PickTimeDialogListener pickTimeDialogListener;

    public interface PickTimeDialogListener {
        void onFinishPickTimeDialog(int hour, int minute);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        assert bundle != null;

        int hour = bundle.getInt("hour");
        int minute = bundle.getInt("minute");

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
        pickTimeDialogListener.onFinishPickTimeDialog(hour, minute);
        this.dismiss();
    }

    public void setPickTimeDialogListener(PickTimeDialogListener pickTimeDialogListener) {
        this.pickTimeDialogListener = pickTimeDialogListener;
    }
}
