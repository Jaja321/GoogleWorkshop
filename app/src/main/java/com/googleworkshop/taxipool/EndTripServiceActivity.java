package com.googleworkshop.taxipool;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Gal Ze'evi on 1/10/2018.
 */

public class EndTripServiceActivity extends NavDrawerActivity {
    private GeofencingClient mGeofencingClient;
    private PendingIntent mGeofencePendingIntent;
    private Geofence mGeofence;
    private ArrayList<Geofence> mGeofenceList = new ArrayList<>();
    private final int ACCESS_FINE_LOCATION_CODE = 17;//TODO I used the same code as PreferencesActivity, is that OK?
    private final int LOCATION_SETTINGS_CODE = 123;
    private ArrayList<User> groupUsers;
    private int groupSize;
    private String groupId;
    private DatabaseReference database;
    private LatLng destLatLng;
    //private boolean tryAgain = true;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_end);
        addDrawer();

        //------------------End Trip-------------------------
        final ImageButton gettButton = (ImageButton) findViewById(R.id.order_taxi);
        gettButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchGettIntent = getPackageManager().getLaunchIntentForPackage("com.gettaxi.android");
                if( launchGettIntent != null){
                    startActivity(launchGettIntent);
                }
                else{
                    Uri playStoreGettPage = Uri.parse("http://play.google.com/store/apps/details?id=com.gettaxi.android");
                    Intent playStoreGettIntent = new Intent(Intent.ACTION_VIEW, playStoreGettPage);
                    startActivity(playStoreGettIntent);
                }
            }
        });

        final ImageButton pepperButton = (ImageButton) findViewById(R.id.pepper);
        pepperButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchPepperIntent = getPackageManager().getLaunchIntentForPackage("com.pepper.pay");
                if( launchPepperIntent != null){
                    startActivity(launchPepperIntent);
                }
                else{
                    Uri playStorePepperPage = Uri.parse("http://play.google.com/store/apps/details?id=com.pepper.pay");
                    Intent playStorePepperIntent = new Intent(Intent.ACTION_VIEW, playStorePepperPage);
                    startActivity(playStorePepperIntent);
                }

            }
        });

        final Button ratingButton = (Button) findViewById(R.id.ratingButton);
        ratingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent( EndTripServiceActivity.this, RatingActivity.class);
                myIntent.putExtra("groupSize", groupSize);
                myIntent.putExtra("groupUsers", groupUsers);
                startActivity(myIntent);
            }
        });
        //---------------------------------------------------

        //------------------Geofencing-----------------------
        destLatLng = getIntent().getParcelableExtra("destLatLng");//TODO make sure is sent from matchScreen

        mGeofence = new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId("myGeofence")

                .setCircularRegion(
                        destLatLng.latitude,
                        destLatLng.longitude,
                        3000//Radius in meters
                )
                //Duration in milliseconds,
                .setExpirationDuration(300000)//50 min
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build();
        mGeofenceList.add(mGeofence);

        mGeofencingClient = LocationServices.getGeofencingClient(this);

        database = FirebaseDatabase.getInstance().getReference();

        groupSize = getIntent().getIntExtra("groupSize", 0);
        groupUsers = (ArrayList<User>) getIntent().getSerializableExtra("groupUsers");
        groupId=getIntent().getStringExtra("groupId");

        database.child("groups").child(groupId).child("numOfUsers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue(int.class) != null){
                    groupSize = dataSnapshot.getValue(int.class);
                    addGeofences();
                }

            }
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        //---------------------------------------------------

    }

    //------------------Geofencing-----------------------
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceRatingIntentService.class);
        intent.putExtra("groupSize", groupSize);
        Bundle groupUsersBundle = new Bundle();
        groupUsersBundle.putParcelableArrayList("groupUsers", groupUsers);
        intent.putExtra("groupUsersBundle", groupUsersBundle);
        //intent.putExtra("groupUsers", groupUsers);

        /*
        for(int i = 0; i < groupSize - 1; i++){
            String id = "User" + Integer.toString(i);
            intent.putExtra(id, groupUsers.get(i));
        }*/

        //intent.putExtra("groupUsers", groupUsers);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(this, 177, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private void checkGPSOn(){
        if (!PreferencesUtils.isLocationEnabled(EndTripServiceActivity.this)){
            AlertDialog.Builder dialog = new AlertDialog.Builder(EndTripServiceActivity.this);
            dialog.setMessage("GPS signal is disabled :(");

            dialog.setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    EndTripServiceActivity.this.startActivityForResult(myIntent,LOCATION_SETTINGS_CODE);
                    //get gps
                }
            });
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    //TODO start timer
                    Intent timerIntent = new Intent(EndTripServiceActivity.this, RatingTimerService.class);
                    timerIntent.putExtra("groupSize", groupSize);
                    timerIntent.putExtra("groupUsers", groupUsers);
                    startService(timerIntent);
                    //finish();
                }
            });
            dialog.show();
            return;
        }

    }

    @SuppressWarnings("MissingPermission")
    private void addGeofences() {
        if (!checkPermissions()) {
            getPermissions();
            Log.i("hello hello", "hello hello");
            //showSnackbar(getString(R.string.insufficient_permissions));
            //return;
        }

        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences added
                        Log.i("Added Geofence", "Added Geofence");
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to add geofences
                        //if(tryAgain) {
                         //   checkGPSOn();
                        //}
                        Intent timerIntent = new Intent(EndTripServiceActivity.this, RatingTimerService.class);
                        timerIntent.putExtra("groupSize", groupSize);
                        timerIntent.putExtra("groupUsers", groupUsers);
                        startService(timerIntent);
                        Log.i("Did not add Geofence", "Did not add Geofence");

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //tryAgain = false;
        if (requestCode == LOCATION_SETTINGS_CODE || resultCode == LOCATION_SETTINGS_CODE){
            addGeofences();
            return;
        }
    }


    private void showSnackbar(final String text) {
        View container = findViewById(android.R.id.content);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    public void getPermissions(){
        ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},ACCESS_FINE_LOCATION_CODE);
    }
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.i("I am here","This is good..");
        switch (requestCode) {
            case ACCESS_FINE_LOCATION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.i("IN HERE", "IN HERE");
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    //TODO handle permission not granted
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    //---------------------------------------------------

    //---------------navigation drawer-------------------
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
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
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
        menuItem.setChecked(true);//TODO Remove?
        // Close the navigation drawer
        mDrawer.closeDrawers();
    }*/
    //---------------------------------------------------



}