package com.googleworkshop.taxipool;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class User implements Parcelable{
    private String homeId;
    private String profilePicture;
    private String name;
    private String userId;
    private int numOfRaters;
    private double rating;
    private List<String> reportedIDs;
    private boolean blocked;


    User(){

    }


    User(String userId, String name, String profilePicture){
        this.userId = userId;
        this.name = name;
        this.profilePicture = profilePicture;
        this.rating = 0;
        this.numOfRaters = 0;
        this.homeId = null;
        this.blocked = false;
        this.reportedIDs = new ArrayList<>();
    }
/*
    User(String userId, String name, String profilePicture, double rating, int numOfRaters, String homeId){
        this.userId = userId;
        this.name = name;
        this.profilePicture = profilePicture;
        this.rating=rating;
        this.numOfRaters=numOfRaters;
        this.homeId=homeId;
        this.blocked = false;
        this.reportedIDs = new ArrayList<>();
    }
*/
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
        parcel.writeInt(numOfRaters);
        parcel.writeDouble(rating);
        parcel.writeByte((byte) (blocked ? 1 : 0));
        parcel.writeList(reportedIDs);
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
        this.numOfRaters = in.readInt();
        this.rating = in.readDouble();
        this.blocked = in.readByte() != 0;
        this.reportedIDs=in.readArrayList(null);
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

    public String getUserId() {return userId; }


    public double getRating() {
        return rating;
    }
/*
    public void setRating(double rating) {
        this.rating = rating;
    }
    */
    public List<String> getReportedIDs() {
        return reportedIDs;
    }

    public boolean getBlocked() {
        return blocked;
    }
    public int getNumOfRaters() {
        return numOfRaters;
    }

    public void setReportedIDs(List<String> reportedIDs) {
        this.reportedIDs = reportedIDs;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

}