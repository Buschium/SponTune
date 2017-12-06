package com.spontune.android.spontune.Data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.graphics.Bitmap;
import com.google.android.gms.maps.model.LatLng;
import java.util.List;

@Dao
public interface EventDAO {

    @Insert
    void insertEvent(Event event);

    @Delete
    void deleteEvent(Event event);

    @Query("SELECT * FROM events")
    List<Event> getAll();

    @Query("SELECT lat FROM events WHERE ID = :number")
    double getLatitude(int number);

    @Query("SELECT lng FROM events WHERE ID = :number")
    double getLongitude(int number);

    @Query("SELECT creator FROM events WHERE ID = :number")
    String getCreator(int number);

    @Query("SELECT summary FROM events WHERE ID = :number")
    String getSummary(int number);

    @Query("SELECT description FROM events WHERE ID = :number")
    String getDescription(int number);

    @Query("SELECT startingTime FROM events WHERE ID = :number")
    int getStartingTime(int number);

    @Query("SELECT endingTime FROM events WHERE ID = :number")
    int getEndingTime(int number);

    @Query("SELECT category FROM events WHERE ID = :number")
    int getCategory(int number);

    /*
    @Query("SELECT picture FROM events WHERE ID = :number")
    Bitmap getPicture(int number);
    */

    @Query("SELECT * FROM events WHERE ID = :number")
    Event getEventByID(int number);
}