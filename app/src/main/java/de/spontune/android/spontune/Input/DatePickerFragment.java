package de.spontune.android.spontune.Input;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.app.DatePickerDialog;
import android.widget.DatePicker;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    public interface PickDateDialogListener {
        void onFinishPickDateDialog(int year, int month, int day);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        assert bundle != null;

        int year = bundle.getInt("year");
        int month = bundle.getInt("month");
        int day = bundle.getInt("day");

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        PickDateDialogListener activity = (PickDateDialogListener) getActivity();
        activity.onFinishPickDateDialog(year, month, day);
        this.dismiss();
    }

}
