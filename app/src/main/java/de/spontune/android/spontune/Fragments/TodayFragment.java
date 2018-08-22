package de.spontune.android.spontune.Fragments;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class TodayFragment extends EventFragment {

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        //Calendar now = GregorianCalendar.getInstance();   //used for determining which events start today
        Calendar endOfDay = GregorianCalendar.getInstance();
        endOfDay.set(Calendar.HOUR_OF_DAY, 23);
        endOfDay.set(Calendar.MINUTE, 59);
        endOfDay.set(Calendar.SECOND, 59);
        Query query = databaseReference.child("events").orderByChild("startingTime").endAt(endOfDay.getTimeInMillis());
        return query;
    }
}
