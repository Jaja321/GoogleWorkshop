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



public class SearchingService extends Service {
    protected static String groupId = null;
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

        return START_STICKY;
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
                if(request == null){
                    Log.i("SearchingService Error", "final Request request=dataSnapshot.getValue(Request.class); caused a NullPointerException");
                    Toast.makeText(SearchingService.this, "We're sorry, an unexpected error occurred",
                            Toast.LENGTH_SHORT).show();
                    stopSelf();
                    return;
                }
                groupId=request.getGroupId();
                if(groupId!=null){
                    //Found a group

                    //DatabaseReference isActiveRef=database.child("groups").child(groupId).child("active");
                    final DatabaseReference groupRef=database.child("groups").child(groupId);//Changes to isActive will also trigger this
                    //According to FireBase documentation
                    groupRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {

                            if(!dataSnapshot.exists()) {
                                return;
                            }

                            //this happens after the user clicked on "find a new ride" when group is not closed
                            //the group was removed so the value of "isActive" is null
                            //Therefore, the unboxing in Boolean.valueOf() throws an exception
                            try {
                                isActive = dataSnapshot.child("active").getValue(boolean.class);
                            }
                            catch (NullPointerException e){
                                return;
                            }
                            if(isActive) {
                                //the group is still active
                                String body = "We'll let you know if we find more.";
                                String title;
                                try {
                                    int numOfPassengers = dataSnapshot.child("numOfPassengers").getValue(int.class);
                                    numOfPassengers -= request.getNumOfPassengers();
                                    title = String.format("We've found a match of %d people!", numOfPassengers);
                                }
                                catch (NullPointerException e){
                                    Log.i("SearchingService Error", "dataSnapshot.child(\"numOfPassengers\").getValue(int.class) caused a NullPointerException");
                                    Toast.makeText(SearchingService.this, "We're sorry, an unexpected error occured", Toast.LENGTH_LONG).show();
                                    return;
                                }


                                Intent intent = new Intent(getApplicationContext(), MatchScreenActivity.class);
                                intent.putExtra("destLatLng", request.destLatLng());
                                intent.putExtra("currentRequest",request);
                                intent.putExtra("groupId", groupId);
                                intent.putExtra("serviceIntent", serviceIntent);

                                notification = NotificationUtils.getOngoingNotification(title, body, intent, getApplicationContext());

                                startForeground(FOREGROUND_ID, notification);

                                Bundle b = new Bundle();
                                b.putParcelable("destLatLng", request.destLatLng());
                                b.putParcelable("currentRequest", request);
                                b.putString("groupId", groupId);
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
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.putExtra("origin", origin);
        intent.putExtra("destination", destination);
        return NotificationUtils.getOngoingNotification(title, body, intent, getApplicationContext());
    }

    public void finishService(){
        //this is called before service stops

    }

}
