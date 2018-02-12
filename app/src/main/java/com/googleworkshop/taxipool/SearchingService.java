package com.googleworkshop.taxipool;

import android.app.IntentService;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Gal Ze'evi on 1/3/2018.
 */

public class SearchingService extends Service {
    protected String groupId = null;
    protected String requestId = null;
    protected int groupSize = 0;
    protected long numOfSeconds = 0;
    protected ResultReceiver rec;
    protected String origin = null;
    protected String destination = null;
    private DatabaseReference database;
    private int FOREGROUND_ID = 111;//TODO
    private boolean isActive;
    final Handler handler = new Handler();
    final Runnable r = new Runnable() {
        public void run() {
            stopForeground(true);
            //finishService();
            //startActivity(new Intent(SearchingService.this, PreferencesActivity.class));
            stopSelf();
        }
    };
    protected Notification notification;
    protected Intent serviceIntent;


    // Must create a default constructor
    public SearchingService() {
        // Used to name the worker thread, important only for debugging.
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate(); // if you override onCreate(), make sure to call super().
        // If a Context object is needed, call getApplicationContext() here.
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        //for debugging
        //Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
        Log.d("SearchingService", "onDestroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        //Get requestId
        serviceIntent = intent;
        Bundle b = intent.getBundleExtra("bundle");
        requestId = b.getString("requestId");
        numOfSeconds = b.getLong("numOfSeconds", 0);
        rec = b.getParcelable("receiver");
        origin = b.getString("origin");
        destination = b.getString("destination");

        startForeground(FOREGROUND_ID,getSearchingNotification());

        waitForGroup();

        handler.postDelayed(r, numOfSeconds*1000);//service will stop itself when time's up

        return START_STICKY;//TODO ??
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
        final DatabaseReference groupIdRef=database.child("requests").child(requestId);

        groupIdRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists())
                    return;
                final Request request=dataSnapshot.getValue(Request.class);
                groupId=request.getGroupId();
                if(groupId!=null){
                    //Found a group

                    //DatabaseReference isActiveRef=database.child("groups").child(groupId).child("active");
                    final DatabaseReference groupRef=database.child("groups").child(groupId);
                    groupRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {

                            if(!dataSnapshot.exists()) {
                                return;//TODO try again?
                            }
                            DatabaseReference isActiveRef = groupRef.child("active");
                            isActive=dataSnapshot.child("active").getValue(boolean.class);
                            if(isActive) {
                                //the group is still active
                                String title = "We've found a match!";
                                int numOfPassengers=dataSnapshot.child("numOfPassengers").getValue(int.class);

                                String body = String.format("We've found a group of %d people", numOfPassengers);
                                //String body = "Your group has 10 people.";//TODO

                                Intent intent = new Intent(getApplicationContext(), MatchScreenActivity.class);
                                //intent.putExtra("groupSize", groupSize);
                                intent.putExtra("destLatLng", request.destLatLng());
                                intent.putExtra("currentRequest",request);
                                intent.putExtra("groupId", groupId);
                                intent.putExtra("serviceIntent", serviceIntent);

                                notification = NotificationUtils.getOngoingNotification(title, body, intent, getApplicationContext());

                                startForeground(FOREGROUND_ID, notification);

                                Bundle b = new Bundle();
                                b.putParcelable("destLatLng", request.destLatLng());
                                b.putParcelable("currentRequest", request);
                                b.putString("groupId", groupId);//TODO Isn't this already in request?
                                b.putBoolean("isActive", isActive);

                                rec.send(1, b);
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

        Intent intent = new Intent(getApplicationContext(), SearchingActivity2.class);
        //Intent intent = new Intent(getApplicationContext(), PreferencesActivity.class);//TODO this is for now
        //TODO I can't figure out how to get back to the same SearchingActivity2 instance that called the service
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.putExtra("origin", origin);
        intent.putExtra("destination", destination);
        //intent.putExtra("numOfSeconds", numOfSeconds);
        //intent.putExtra("serviceStartTime", System.currentTimeMillis());
        //intent.putExtra("rec", rec);

        return NotificationUtils.getOngoingNotification(title, body, intent, getApplicationContext());
    }

    public void finishService(){
        //TODO this is called before service stops
        //TODO
        //NotificationUtils.sendNotification("hello", "hello", new Intent(getApplicationContext(), SearchingServiceActivity.class), getApplicationContext());

    }

}
