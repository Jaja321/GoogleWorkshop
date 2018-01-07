package com.googleworkshop.taxipool;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;

import java.util.concurrent.TimeUnit;

public class SearchingServiceActivity extends AppCompatActivity {
    protected MyReceiver receiver;
    protected static final String FORMAT = "%02d:%02d";
    protected int numOfSeconds;
    private DatabaseReference database;
    private String requestId;
    Intent nextIntent;
    Intent i;

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
                stopService(i);
                timer.setText("done!");//for now
            }
        }.start();

        nextIntent = new Intent(this,MatchScreenActivity.class);
        requestId = getIntent().getStringExtra("requestId");
        setupServiceReceiver();
        onStartService();
    }

    // Starts the IntentService
    public void onStartService() {
        i = new Intent(this, TaxiPoolService.class);
        i.putExtra("requestId", requestId);
        i.putExtra("receiver", receiver);
        //i.putExtra("numOfSeconds", numOfSeconds);
        startService(i);
        //finish();
    }

    // Setup the callback for when data is received from the service
    public void setupServiceReceiver() {
        //Log.i("In setupServiceReceiver", "In setupServiceReceiver");
        receiver = new MyReceiver(new Handler());
        // onReceiveResult is called when data is received from the service
        receiver.setReceiver(new MyReceiver.Receiver() {
            @Override
            public void onReceiveResult(int resultCode, Bundle resultData) {
                //Log.i("In onReceiveResult", "In onReceiveResult");
                if (resultCode == 1) {//group found
                    //Log.i("Group Found", "Group Found");
                    String groupId = resultData.getString("groupId");
                    nextIntent.putExtra("groupId",groupId);
                    startActivity(nextIntent);
                    finish();
                }
                else{//does that necessarily means time ran out? I think so
                    finish();
                }
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
