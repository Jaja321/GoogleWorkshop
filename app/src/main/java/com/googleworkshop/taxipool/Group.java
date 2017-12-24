package com.googleworkshop.taxipool;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class Group implements Parcelable {
    private List<Request> requests;
    private List<LatLng> destinations;
    private LatLng meetingPoint;
    private String groupId;

    Group(List<Request> requests,List<LatLng> destinations){
        this.requests = requests;
        this.destinations = destinations;
        // Creating groupID using current time and the first user's ID
        groupId = String.valueOf(System.currentTimeMillis()) + requests.get(0).getRequester().getUserId();
        // Creating the group meeting point
        double avgLat = 0d;
        double avgLng = 0d;
        for (Request r: requests){
            avgLat += r.getSrc().latitude;
            avgLng += r.getSrc().longitude;
        }
        meetingPoint = new LatLng(avgLat/requests.size(),avgLng/requests.size());
    }

    private Group(Parcel in) {
        requests = in.createTypedArrayList(Request.CREATOR);
        destinations = in.createTypedArrayList(LatLng.CREATOR);
        meetingPoint = in.readParcelable(LatLng.class.getClassLoader());
        groupId = in.readString();
    }

    public static final Creator<Group> CREATOR = new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

    public List<Request> getRequests() {
        return requests;
    }

    public List<LatLng> getDestinations() {
        return destinations;
    }

    public LatLng getMeetingPoint() {
        return meetingPoint;
    }

    public String getGroupId() {
        return groupId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(requests);
        parcel.writeTypedList(destinations);
        parcel.writeParcelable(meetingPoint, i);
        parcel.writeString(groupId);
    }
}