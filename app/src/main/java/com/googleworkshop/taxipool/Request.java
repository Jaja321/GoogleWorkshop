package com.googleworkshop.taxipool;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class Request implements Parcelable{
    private String requesterId;
    private String src;//This is a LatLng representing the source flattened to String
    private String dest;//This is a LatLng representing the destination flattened to String
    //The reason for not saving the LatLng itself is to allow request to be Parcelable and so we can easily save it with SharedPreferences
    private long timePrefs;
    private int numOfPassengers;
    private String groupId;
    protected String origin;//This is a String with the name of the source
    protected String destination;//This a String with the name of the destination
    protected String requestId;//This is a String used to identify the request on the server and is returned by addRequest()
    protected long timeStamp;//A timestamp of when the request was created, in Milliseconds

    public Request(){}

    Request(String requesterId,LatLng src,LatLng dest,long timePrefs,int numOfPassengers){
        this.requesterId = requesterId;
        this.src = latlngToStr(src);
        this.dest = latlngToStr(dest);
        this.timePrefs = timePrefs;
        this.numOfPassengers = numOfPassengers;
        groupId=null;
        this.timeStamp = System.currentTimeMillis();
    }

    Request(String requesterId, String src,String dest,long timePrefs,int numOfPassengers, String groupId){
        this.requesterId = requesterId;
        this.src = src;
        this.dest = dest;
        this.timePrefs = timePrefs;
        this.numOfPassengers = numOfPassengers;
        this.groupId=groupId;
        this.timeStamp = System.currentTimeMillis();
    }

    Request(String requesterId, String src,String dest,long timePrefs,int numOfPassengers, String groupId, String origin, String destination, String requestId, long timeStamp){
        this(requesterId, src, dest, timePrefs, numOfPassengers, groupId);
        this.origin = origin;
        this.destination = destination;
        this.requestId = requestId;
        this.timeStamp = timeStamp;
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

    public String getSrc() { return src; }

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

    public String getOrigin() {return origin;}

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getRequestId(){return this.requestId;}

    public void setRequestId(String requestId){this.requestId = requestId;}

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setTimePrefs(long timePrefs){this.timePrefs = timePrefs;}

    //TODO These should be updated
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
        parcel.writeString(origin);
        parcel.writeString(destination);
        parcel.writeString(requestId);
        parcel.writeLong(timeStamp);
    }
    private Request(Parcel in){
        this.requesterId = in.readString();
        this.src = in.readString();
        this.dest = in.readString();
        this.timePrefs = in.readLong();
        this.numOfPassengers = in.readInt();
        this.groupId = in.readString();
        this.origin = in.readString();
        this.destination = in.readString();
        this.requestId = in.readString();
        this.timeStamp = in.readLong();
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

    private String latlngToStr(LatLng latLng){
        return latLng.latitude+","+ latLng.longitude;
    }
}