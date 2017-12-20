package com.googleworkshop.taxipool;

/**
 * Created by Jerafi on 12/20/2017.
 */

public class User {
    private String homeId;
    private String profilePicture;
    private String name;
    private String userId;
    private char gender;
    private int age;
    private float rating = 5.0f;
    private int numOfRaters = 0;

    public User(String userId,String name,char gender,int age,String profilePicture){
        this.userId = userId;
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.profilePicture = profilePicture;
    }

    public String getHome_id() {
        return homeId;
    }

    public void setHomeId(String homeId) {
        this.homeId = homeId;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public String getName(){
        return name;
    }

    public String getUserId() {
        return userId;
    }

    public char getGender() {
        return gender;
    }

    public int getAge() {
        return age;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getNumOfRaters() {
        return numOfRaters;
    }

    public void setNumOfRaters(int numOfRaters) {
        this.numOfRaters = numOfRaters;
    }
}