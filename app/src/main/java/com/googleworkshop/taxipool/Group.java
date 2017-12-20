package com.googleworkshop.taxipool;


import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Jerafi on 12/20/2017.
 */

public class Group {
    private List<Request> requests;
    private List<LatLng> destinations;
    private LatLng meetingPoint;
    private String groupId;
    public Group(List<Request> requests,List<LatLng> destinations){
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
}
