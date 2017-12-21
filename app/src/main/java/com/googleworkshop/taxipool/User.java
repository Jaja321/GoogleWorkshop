package com.googleworkshop.taxipool;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jerafi on 12/20/2017.
 */

public class User implements Parcelable{
    private String homeId;
    private String profilePicture;
    private String name;
    private String userId;
    private byte gender;
    private int age;
    private int numOfRaters = 0;
    private float rating = 5.0f;

    public User(String userId,String name,byte gender,int age,String profilePicture){
        this.userId = userId;
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.profilePicture = profilePicture;
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
        parcel.writeByte(gender);
        parcel.writeInt(age);
        parcel.writeInt(numOfRaters);
        parcel.writeFloat(rating);
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
        this.gender = in.readByte();
        this.age = in.readInt();
        this.numOfRaters = in.readInt();
        this.rating = in.readFloat();
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

    public String getUserId() {return name; }

    public byte getGender() {
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