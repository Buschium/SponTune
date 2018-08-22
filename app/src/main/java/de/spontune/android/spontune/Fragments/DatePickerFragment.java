package de.spontune.android.spontune.Fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private PickDateDialogListener pickDateDialogListener;

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
        pickDateDialogListener.onFinishPickDateDialog(year, month, day);
        this.dismiss();
    }

    public void setPickDateDialogListener(PickDateDialogListener pickDateDialogListener){
        this.pickDateDialogListener = pickDateDialogListener;
    }

}
