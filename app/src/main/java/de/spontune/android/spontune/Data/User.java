package de.spontune.android.spontune.Data;

import android.graphics.Bitmap;

import java.util.HashMap;

public class User {
    private String id;
    private String username;
    private String userDescription;
    private Bitmap profilePicture;
    private HashMap<String, String> followers;
    private HashMap<String, String> following;

    public User(){}

    public User(String id, String username){
        this.username = username;
        this.id = id;
    }

    public User(String id, String username, String userDescription, Bitmap profilePicture){
        this.id = id;
        this.username = username;
        this.userDescription = userDescription;
        this.profilePicture = profilePicture;
    }

    public String getId(){
        return id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername(){
        return username;
    }

    public void setUserDescription(String userDescription) {
        this.userDescription = userDescription;
    }

    public String getUserDescription(){
        return userDescription;
    }

    public void setProfilePicture(Bitmap profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Bitmap getProfilePicture() {
        return profilePicture;
    }

    public HashMap<String, String> getFollowers(){
        return followers;
    }

    public void setFollowers(HashMap<String, String> followers){
        this.followers = followers;
    }

    public HashMap<String, String> getFollowing(){
        return following;
    }

    public void setFollowing(HashMap<String, String> following){
        this.following = following;
    }
}
