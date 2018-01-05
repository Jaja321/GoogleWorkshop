package com.googleworkshop.taxipool;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Gal Ze'evi on 1/3/2018.
 */

public class TaxiPoolService extends IntentService {
    protected String groupId = null;
    // Must create a default constructor
    public TaxiPoolService() {
        // Used to name the worker thread, important only for debugging.
        super("test-service");
    }

    @Override
    public void onCreate() {
        super.onCreate(); // if you override onCreate(), make sure to call super().
        // If a Context object is needed, call getApplicationContext() here.
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Extract the receiver passed into the service
        ResultReceiver rec = intent.getParcelableExtra("receiver");
        //Get requestId
        String requestId = intent.getStringExtra("requestId");
        waitForGroup(requestId);
        // Create Bundle to send message to activity
        Bundle bundle = new Bundle();
        //passing info to activity
        int resultCode = (groupId == null)? 0 : 1;//resultCode == 1 iff groupId != null
        bundle.putString("groupId", groupId);
        rec.send(resultCode, bundle);
    }

    private void waitForGroup(String requestId){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference groupIdRef=database.child("requests").child(requestId).child("groupId");
        groupIdRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                groupId=dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
