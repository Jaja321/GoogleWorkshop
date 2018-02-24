package com.googleworkshop.taxipool;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.CountDownTimer;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class SearchingActivity extends NavDrawerActivity {
    protected static final String FORMAT = "%02d:%02d";
    protected static final String FORMAT1 = "%2d:%02d:%02d";
    protected long numOfSeconds;
    private DatabaseReference database;
    private String requestId, groupId;
    Intent nextIntent;
    private boolean isActive;
    private SharedPreferences.Editor editor;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.searching_screen_layout);
        addDrawer();
        getSupportActionBar().setTitle("Looking for a match");

        final TextView timer = (TextView)findViewById(R.id.timer);
        TextView origin = (TextView)findViewById(R.id.real_origin);
        TextView destination = (TextView)findViewById(R.id.real_dest);
        Button findNewTrip = (Button)findViewById(R.id.find_new_trip);
        findNewTrip.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                                Intent intent = new Intent(SearchingActivity.this, PreferencesActivity.class);
                                                startActivity(intent);
                                           }
                                       });
        origin.setText(getIntent().getStringExtra("origin"));
        destination.setText(getIntent().getStringExtra("destination"));

        numOfSeconds = getIntent().getLongExtra("numOfSeconds", 999);
        countDownTimer=  new CountDownTimer(numOfSeconds*1000, 1000) {
            public void onTick(long millisUntilFinished) {
                //check for a match?
                if(millisUntilFinished >= 60*60*1000) {//over an hour left
                    timer.setText(String.format(FORMAT1,
                            TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                    TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));

                }
                else {
                    timer.setText(String.format(FORMAT,
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                    TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
                }
            }

            public void onFinish() {
                timer.setText("done!");
                if(groupId!=null &&!isActive) {
                    Intent intent = new Intent(SearchingActivity.this, PreferencesActivity.class);
                    database.child("groups").child(groupId).child("closed").setValue(true);
                    NotificationManager mNotificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    try {
                        mNotificationManager.cancelAll();//delete all previously sent notifications
                    }catch (NullPointerException e){
                        Log.i("null pointer", "NullPointerException in cancelAll()");
                    }
                    NotificationUtils.sendNotification("Sorry, We could not find a match",
                            "you are welcome to try again soon", intent, getApplicationContext());
                }
            }
        };
        countDownTimer.start();
        SharedPreferences sharedPref = this.getSharedPreferences("requestId", Context.MODE_PRIVATE);
        nextIntent=new Intent(this,MatchScreenActivity.class);
        requestId=getIntent().getStringExtra("requestId");
        if(requestId!=null){
           editor = sharedPref.edit();
            editor.putString("requestId",requestId);
            editor.commit();
        }
        waitForGroup();
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
                    //Found a group:
                    DatabaseReference isActiveRef=database.child("groups").child(groupId).child("active");
                    isActiveRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(!dataSnapshot.exists())
                                return;
                            isActive=dataSnapshot.getValue(boolean.class);
                            if(isActive) {
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

    @Override
    public void gotoPreferences(){
        countDownTimer.cancel();
        if(groupId!=null &&!isActive)
            database.child("groups").child(groupId).child("closed").setValue(true);
        editor.putString("requestId",null);
        editor.commit();
        Intent intent;
        intent = new Intent(this, PreferencesActivity.class);
        startActivity(intent);
        finish();
    }
}
