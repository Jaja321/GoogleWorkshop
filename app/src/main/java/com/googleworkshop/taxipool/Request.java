package com.googleworkshop.taxipool;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class Request implements Parcelable{
    private String requesterId;
    private String src;
    private String dest;
    private long timePrefs;
    private int numOfPassengers;
    private String groupId;
    public Request(){}

    Request(String requesterId,LatLng src,LatLng dest,long timePrefs,int numOfPassengers){
        this.requesterId = requesterId;
        this.src = latlngToStr(src);
        this.dest = latlngToStr(dest);
        this.timePrefs = timePrefs;
        this.numOfPassengers = numOfPassengers;
        groupId=null;
    }

    Request(String requesterId, String src,String dest,long timePrefs,int numOfPassengers, String groupId){
        this.requesterId = requesterId;
        this.src = src;
        this.dest = dest;
        this.timePrefs = timePrefs;
        this.numOfPassengers = numOfPassengers;
        this.groupId=groupId;
    }

    public LatLng srcLatLng(){
        return ServerUtils.strToLatlng(src);
    }
    public LatLng destLatLng(){
        return ServerUtils.strToLatlng(dest);
    }

    public String getRequesterId() {
        return requesterId;
    }

    public String getSrc() { return src;
    }

    public String getDest() {
        return dest;
    }

    public long getTimePrefs() {
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
        parcel.writeString(requesterId);
        parcel.writeString(src);
        parcel.writeString(dest);
        parcel.writeLong(timePrefs);
        parcel.writeInt(numOfPassengers);
        parcel.writeString(groupId);
    }
    private Request(Parcel in){
        this.requesterId = in.readString();
        this.src = in.readString();
        this.dest = in.readString();
        this.timePrefs = in.readLong();
        this.numOfPassengers = in.readInt();
        this.groupId = in.readString();
    }

    static final Parcelable.Creator<Request> CREATOR = new Parcelable.Creator<Request>(){

        @Override
        public Request createFromParcel(Parcel parcel) {
            return new Request(parcel);
        }

        @Override
        public Request[] newArray(int size) {
            return new Request[size];
        }
    };

    private String latlngToStr(LatLng latLng){
        return latLng.latitude+","+ latLng.longitude;
    }
}