package de.spontune.android.spontune.fragments;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class WeekFragment extends EventFragment {

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        Calendar startOfDay = GregorianCalendar.getInstance();
        startOfDay.add(Calendar.DATE, 2);
        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);
        Calendar endOfDay = GregorianCalendar.getInstance();
        endOfDay.add(Calendar.DATE, 7);
        endOfDay.set(Calendar.HOUR_OF_DAY, 23);
        endOfDay.set(Calendar.MINUTE, 59);
        endOfDay.set(Calendar.SECOND, 59);
        return databaseReference.child("events").orderByChild("startingTime").startAt(startOfDay.getTimeInMillis()).endAt(endOfDay.getTimeInMillis());
    }
}
