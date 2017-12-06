package com.spontune.android.spontune.Data;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.graphics.Bitmap;

@Entity(tableName = "events")
public class Event {
    @PrimaryKey(autoGenerate = true)
    public int ID;
    public String creator;
    //public LatLng location;
    public double lat;
    public double lng;
    public String summary;
    public String description;
    public int startingTime;
    public int endingTime;
    public int category;
    //public Bitmap picture;

    public Event(double lat, double lng, String creator, String summary, String description, int startingTime, int endingTime, int category){
        this.lat = lat;
        this.lng = lng;
        this.creator = creator;
        this.summary = summary;
        this.description = description;
        this.startingTime = startingTime;
        this.endingTime = endingTime;
        this.category = category;
        //this.picture = picture;
    }
}
