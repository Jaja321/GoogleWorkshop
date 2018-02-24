package com.googleworkshop.taxipool;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

/**
 * Created by Gal Ze'evi on 1/3/2018.
 */

public class TaxiPoolService extends IntentService {
    protected String groupId = null;
    protected String requestId = null;
    protected int groupSize = 0;
    protected int numOfSeconds = 0;
    //ResultReceiver rec;
    private DatabaseReference database;
    private int FOREGROUND_ID = 111;//TODO
    private boolean isActive;
    final Handler handler = new Handler();
    final Runnable r = new Runnable() {
        public void run() {
            stopForeground(true);
            finishService();
            stopSelf();
        }
    };
    protected Notification notification;


    // Must create a default constructor
    public TaxiPoolService() {
        // Used to name the worker thread, important only for debugging.
        super("TaxiPoolService");
    }

    @Override
    public void onCreate() {
        super.onCreate(); // if you override onCreate(), make sure to call super().
        // If a Context object is needed, call getApplicationContext() here.
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Get requestId
        requestId = intent.getStringExtra("requestId");
        numOfSeconds = intent.getIntExtra("numOfSeconds", 0);

        startForeground(FOREGROUND_ID,getSearchingNotification());

        waitForGroup();

        handler.postDelayed(r, numOfSeconds*1000);

        //Bundle bundle = new Bundle();
        //bundle.putString("groupId", groupId);
    }


    private void waitForGroup(String requestId){
        //Log.i("In waitForGroup", "In waitForGroup");
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

    private void waitForGroup(){
        database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference groupIdRef=database.child("requests").child(requestId);

        groupIdRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists())
                    return;
                final Request request=dataSnapshot.getValue(Request.class);
                groupId=request.getGroupId();
                if(groupId!=null){
                    //Found a group

                    DatabaseReference isActiveRef=database.child("groups").child(groupId).child("active");
                    isActiveRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if(!dataSnapshot.exists())
                                return;
                            isActive=dataSnapshot.getValue(boolean.class);
                            if(isActive) {

                                String title = "We've found a match!";
                                String body = "Your group has 10 people.";//TODO

                                Intent intent = new Intent(getApplicationContext(), SearchingActivity.class);//TODO
                                intent.putExtra("groupSize", groupSize);

                                notification = NotificationUtils.getOngoingNotification(title, body, intent, getApplicationContext());

                                startForeground(FOREGROUND_ID, notification);

                                /*
                                NotificationManager mNotificationManager =
                                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                try {
                                    mNotificationManager.cancelAll();//delete all previously sent notifications
                                }catch (NullPointerException e){
                                    Log.i("null pointer", "NullPointerException in cancelAll()");
                                }
                                nextIntent.putExtra("groupId", groupId);
                                NotificationUtils.sendNotification("We've found a match!",
                                        "Click to see your travel buddies", nextIntent, getApplicationContext());
                                nextIntent.putExtra("destLatLng", request.destLatLng());
                                nextIntent.putExtra("currentRequest",request);
                                startActivity(nextIntent);
                                finish();
                                */
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public Notification getSearchingNotification(){
        String title = "Looking for a match...";
        String body = "We'll let you know if anything changes";

        Intent intent = new Intent(getApplicationContext(), SearchingServiceActivity.class);//TODO
        intent.putExtra("groupSize", groupSize);

        return NotificationUtils.getOngoingNotification(title, body, intent, getApplicationContext());
    }

    public void finishService(){

    }

}
