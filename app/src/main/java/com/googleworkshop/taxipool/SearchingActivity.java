package com.googleworkshop.taxipool;

import android.content.Intent;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class SearchingActivity extends AppCompatActivity {
    protected static final String FORMAT = "%02d:%02d";
    //protected static int pos = PreferencesActivity.timeSpinner.getSelectedItemPosition();
    protected int numOfSeconds;
    private DatabaseReference database;
    private String requestId;
    Intent nextIntent;

    //added for navigation drawer
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;
    //------


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.searching_screen_layout);

        ProgressBar progressBar = (ProgressBar)findViewById(R.id.searching_animation);
        final TextView timer = (TextView)findViewById(R.id.timer);
        //TODO CHANGE DEFAULT
        numOfSeconds = getIntent().getIntExtra("numOfSeconds", 999);

        //added for navigation drawer
        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = setupDrawerToggle();

        // Tie DrawerLayout events to the ActionBarToggle
        mDrawer.addDrawerListener(drawerToggle);
        // Find our drawer view
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        // Setup drawer view
        setupDrawerContent(nvDrawer);
        //-------


        new CountDownTimer(numOfSeconds*1000, 1000) {

            public void onTick(long millisUntilFinished) {
                //check for a match?
                timer.setText(String.format(FORMAT,
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
            }

            public void onFinish() {
                timer.setText("done!");//for now
            }
        }.start();
        nextIntent=new Intent(this,MatchScreenActivity.class);
        requestId=getIntent().getStringExtra("requestId");
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
                    nextIntent.putExtra("groupId", groupId);
                    startActivity(nextIntent);
                    finish();
                    /*
                    DatabaseReference isActiveRef=database.child("groups").child(groupId).child("active");
                    isActiveRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            boolean isActive=dataSnapshot.getValue(boolean.class);
                            if(isActive) {

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                        */
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //added for navigation drawer
    private ActionBarDrawerToggle setupDrawerToggle() {
        // NOTE: Make sure you pass in a valid toolbar reference.  ActionBarDrawToggle() does not require it
        // and will not render the hamburger icon without it.
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open,  R.string.drawer_close);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

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
        menuItem.setChecked(true);
        // Set action bar title
        //setTitle(menuItem.getTitle());
        // Close the navigation drawer
        mDrawer.closeDrawers();
    }
    //------

}
