package com.spontune.android.spontune.Data;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class DatabaseInitializer {

    private static final String TAG = DatabaseInitializer.class.getName();

    public static void populateAsync(@NonNull final AppDatabase db) {
        PopulateDbAsync task = new PopulateDbAsync(db);
        task.execute();
    }

    public static void populateSync(@NonNull final AppDatabase db) {
        populateWithTestData(db);
    }

    private static void addEvent(final AppDatabase db, Event event) {
        db.eventDao().insertEvent(event);
    }

    private static void populateWithTestData(AppDatabase db) {

        Event eventOne = new Event(48.220615, 11.572844, "TestWizard", "Grillen im Park", "Grillen im Park", 1500, 2000, 1);
        Event eventTwo = new Event(48.213545, 11.557243, "TestWizard", "Straßenkonzert", "Irgendwas mit Musik", 1900, 2030, 3);
        Event eventThree = new Event(48.199209, 11.555369, "TestWizard", "Fußball im Park", "Fußball im Park", 1300, 1430, 4);

        addEvent(db, eventOne);
        addEvent(db, eventTwo);
        addEvent(db, eventThree);

        List<Event> userList = db.eventDao().getAll();
        Log.d(DatabaseInitializer.TAG, "Rows Count: " + userList.size());
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final AppDatabase mDb;

        PopulateDbAsync(AppDatabase db) {
            mDb = db;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            populateWithTestData(mDb);
            return null;
        }

    }
}