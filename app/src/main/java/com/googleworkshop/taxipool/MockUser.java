package com.googleworkshop.taxipool;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Jerafi on 12/2/2017.
 */

public class MockUser {
    LatLng location;
    String name;
    public MockUser(LatLng location,String name){
        this.location=location;
        this.name=name;
    }
}
