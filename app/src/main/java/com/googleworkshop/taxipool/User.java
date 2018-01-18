package com.googleworkshop.taxipool;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable{
    private String homeId;
    private String profilePicture;
    private String name;
    private String userId;
    private boolean gender;
    private int age;
    private int numOfRaters;
    private double rating;

    User(){

    }

    User(String userId,String name,String profilePicture){
        this.userId = userId;
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.profilePicture = profilePicture;
        this.rating=0;
        this.numOfRaters=0;
        this.homeId=null;
        this.gender=false;
        this.age=0;
    }

    User(String userId,String name,String profilePicture, boolean gender, int age, int rating, int numOfRaters, String homeId){
        this.userId = userId;
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.profilePicture = profilePicture;
        this.rating=rating;
        this.numOfRaters=numOfRaters;
        this.homeId=homeId;
        this.gender=gender;
        this.age=age;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(homeId);
        parcel.writeString(profilePicture);
        parcel.writeString(name);
        parcel.writeString(userId);
        parcel.writeByte((byte) (gender ? 1 : 0));
        parcel.writeInt(age);
        parcel.writeInt(numOfRaters);
        parcel.writeDouble(rating);
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>(){

        @Override
        public User createFromParcel(Parcel parcel) {
            return new User(parcel);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    private User(Parcel in){
        this.homeId = in.readString();
        this.profilePicture = in.readString();
        this.name = in.readString();
        this.userId = in.readString();
        this.gender = in.readByte() != 0;
        this.age = in.readInt();
        this.numOfRaters = in.readInt();
        this.rating = in.readDouble();
    }

    public String getHomeId() {
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

    String getUserId() {return userId; }

    public boolean getGender() {
        return gender;
    }

    public int getAge() {
        return age;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getNumOfRaters() {
        return numOfRaters;
    }

    public void setNumOfRaters(int numOfRaters) {
        this.numOfRaters = numOfRaters;
    }
    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public void setAge(int age) {
        this.age = age;
    }

}