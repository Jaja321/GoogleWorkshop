package com.googleworkshop.taxipool;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MockServerCommunicationActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Request request = intent.getParcelableExtra("Request");
        /*
        // Creating Mocks
        User MOCKUser1 = new User("MOCK1","Alice Alison",(byte)0,30,null);
        User MOCKUser2 = new User("MOCK2","Bob Bobson",(byte)1,25,null);
        LatLng MOCKdst1 = new LatLng(32.082,34.7944);
        LatLng MOCKdst2 = new LatLng(32.086,34.7911);
        LatLng MOCKsrc1 = new LatLng(request.getSrc().latitude+0.003,request.getSrc().longitude+0.0002);
        LatLng MOCKsrc2 = new LatLng(request.getSrc().latitude+0.00028,request.getSrc().longitude+0.00025);
        Request MOCKRequest1 = new Request(MOCKUser1,MOCKsrc1,MOCKdst1,30,1);
        Request MOCKRequest2 = new Request(MOCKUser2,MOCKsrc2,MOCKdst2,30,1);
        Request[] requests = {request,MOCKRequest1,MOCKRequest2};
        LatLng[] destinations = {request.getDest(),MOCKRequest1.getDest(),MOCKRequest2.getDest()};
        Group group = new Group(new ArrayList<>(Arrays.asList(requests)),new ArrayList<>(Arrays.asList(destinations)));

        Intent next = new Intent(this,MatchScreenActivity.class);
        next.putExtra("Group",group);
        startActivity(next);
        */
    }
}
