package com.googleworkshop.taxipool;

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
import android.view.MenuItem;
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
    //protected static int pos = PreferencesActivity.timeSpinner.getSelectedItemPosition();
    protected int numOfSeconds;
    private DatabaseReference database;
    private String requestId;
    Intent nextIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.searching_screen_layout);
        addDrawer();

        ProgressBar progressBar = (ProgressBar)findViewById(R.id.searching_animation);
        final TextView timer = (TextView)findViewById(R.id.timer);
        //TODO CHANGE DEFAULT
        numOfSeconds = getIntent().getIntExtra("numOfSeconds", 999);

         new CountDownTimer(numOfSeconds*1000, 1000) {

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
                timer.setText("done!");//for now
                NotificationUtils.sendNotification("Sorry, We could not find a match",
                        "you are welcome to try again soon", nextIntent, getApplicationContext());
            }
        }.start();
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        nextIntent=new Intent(this,MatchScreenActivity.class);
        nextIntent.putExtra("destLatLng", getIntent().getParcelableExtra("destLatLng"));//TODO should I check if null?
        requestId=getIntent().getStringExtra("requestId");
        if(requestId!=null){
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("requestId",requestId);
            editor.commit();
        }else{
            requestId = sharedPref.getString("requestId",null);
        }
        waitForGroup();
    }

    private void waitForGroup(){
        database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference groupIdRef=database.child("requests").child(requestId).child("groupId");

        groupIdRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String groupId=dataSnapshot.getValue(String.class);
                if(groupId!=null){
                    //Found a group:
                    /*
                    nextIntent.putExtra("groupId", groupId);
                    startActivity(nextIntent);
                    finish();
                    */

                    //TODO this sends a notification that we've found a match
                    //TODO I didn't know where to put it
                    //nextIntent.putExtra("groupId", groupId);
                    //NotificationUtils.sendNotification("We've found a match!",
                    //       "Click to see your travel buddies", nextIntent, getApplicationContext());

                    DatabaseReference isActiveRef=database.child("groups").child(groupId).child("active");
                    isActiveRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            boolean isActive=dataSnapshot.getValue(boolean.class);
                            if(isActive) {
                                nextIntent.putExtra("groupId", groupId);
                                NotificationUtils.sendNotification("We've found a match!",
                                        "Click to see your travel buddies", nextIntent, getApplicationContext());
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

    /*
     public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        //Fragment fragment = null;
        Intent intent;
        switch(menuItem.getItemId()) {
            case R.id.nav_my_profile:
                intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_sign_out:
                //TODO: add sign_out
                break;
            case R.id.nav_preferences:
                intent = new Intent(this, PreferencesActivity.class);
                User user = null;//TODO
                intent.putExtra("User", user);
                startActivity(intent);
                break;
            default:
                //?
        }

        // Highlight the selected item has been done by NavigationView
        //menuItem.setChecked(true);
        // Set action bar title
        //setTitle(menuItem.getTitle());
        // Close the navigation drawer
        mDrawer.closeDrawers();
    }*/


}
