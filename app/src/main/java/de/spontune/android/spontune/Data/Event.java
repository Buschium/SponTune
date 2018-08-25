package de.spontune.android.spontune.Data;

import android.graphics.Bitmap;

import java.util.HashMap;

public class Event {
    private int category;
    private String creator;
    private String description;
    private long endingTime;
    private String ID;
    private double lat;
    private double lng;
    private int maxPersons;
    private long startingTime;
    private String summary;
    private String address;
    private HashMap<String, String> participants;
    public Bitmap picture;

    public Event(){}

    public Event(double lat, double lng, String creator, String summary, String description, long startingTime,
                 long endingTime, int category, int maxPersons, String address, HashMap<String, String> participants){
        this.lat = lat;
        this.lng = lng;
        this.creator = creator;
        this.summary = summary;
        this.description = description;
        this.startingTime = startingTime;
        this.endingTime = endingTime;
        this.category = category;
        //this.picture = picture;
        this.maxPersons = maxPersons;
        this.address = address;
        this.participants = participants;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getStartingTime() {
        return startingTime;
    }

    public void setStartingTime(long startingTime) {
        this.startingTime = startingTime;
    }

    public long getEndingTime() {
        return endingTime;
    }

    public void setEndingTime(long endingTime) {
        this.endingTime = endingTime;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getMaxPersons() {
        return maxPersons;
    }

    public void setMaxPersons(int maxPersons) {
        this.maxPersons = maxPersons;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public HashMap<String, String> getParticipants() {
        return participants;
    }

    public void setParticipants(HashMap<String, String> participants) {
        this.participants = participants;
    }

    public Bitmap getPicture() {
        return picture;
    }

    public void setPicture(Bitmap picture) {
        this.picture = picture;
    }

    @Override
    public boolean equals(Object event){
        if(event.getClass().equals(Event.class)){
            return ((Event) event).getID().equals(ID);
        }else{
            return false;
        }
    }
}
