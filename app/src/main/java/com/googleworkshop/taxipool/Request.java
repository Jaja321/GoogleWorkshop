package com.googleworkshop.taxipool;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Jerafi on 12/20/2017.
 */

public class Request {
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
}
