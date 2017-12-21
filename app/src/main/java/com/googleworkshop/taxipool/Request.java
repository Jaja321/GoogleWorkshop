package com.googleworkshop.taxipool;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Jerafi on 12/20/2017.
 */

public class Request implements Parcelable{
    private User requester;
    private LatLng src;
    private LatLng dest;
    private int timePrefs;
    private int numOfPassengers;
    private String groupId=null;

    public Request(User requester,LatLng src,LatLng dest,int timePrefs,int numOfPassengers){
        this.requester = requester;
        this.src = src;
        this.dest = dest;
        this.timePrefs = timePrefs;
        this.numOfPassengers = numOfPassengers;
    }

    public User getRequester() {
        return requester;
    }

    public LatLng getSrc() {
        return src;
    }

    public LatLng getDest() {
        return dest;
    }

    public int getTimePrefs() {
        return timePrefs;
    }

    public int getNumOfPassengers() {
        return numOfPassengers;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(requester,i);
        parcel.writeDouble(src.latitude);
        parcel.writeDouble(src.longitude);
        parcel.writeDouble(dest.latitude);
        parcel.writeDouble(dest.longitude);
        parcel.writeInt(timePrefs);
        parcel.writeInt(numOfPassengers);
        parcel.writeString(groupId);
    }
    private Request(Parcel in){
        this.requester = in.readParcelable(User.class.getClassLoader());
        this.src = new LatLng(in.readDouble(),in.readDouble());
        this.dest = new LatLng(in.readDouble(),in.readDouble());
        this.timePrefs = in.readInt();
        this.numOfPassengers = in.readInt();
        this.groupId = in.readString();
    }

    public static final Parcelable.Creator<Request> CREATOR = new Parcelable.Creator<Request>(){

        @Override
        public Request createFromParcel(Parcel parcel) {
            return new Request(parcel);
        }

        @Override
        public Request[] newArray(int size) {
            return new Request[size];
        }
    };
}