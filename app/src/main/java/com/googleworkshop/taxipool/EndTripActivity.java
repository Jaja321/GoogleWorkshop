package com.googleworkshop.taxipool;

import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.content.Intent;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class EndTripActivity extends NavDrawerActivity {
    private ArrayList<User> groupUsers;
    private int groupSize;
    private String groupId;
    private DatabaseReference database;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_end);
        addDrawer();
        try {
            getSupportActionBar().setTitle("Enjoy your ride");
        }
        catch(NullPointerException e){//I don't think we need to stop the app b/c of this
            Log.i("endTripActivity error", "null pointer execption in setTitle");
        }

        database = FirebaseDatabase.getInstance().getReference();

        groupSize = getIntent().getIntExtra("groupSize", 0);
        groupUsers = (ArrayList<User>) getIntent().getSerializableExtra("groupUsers");
        groupId=getIntent().getStringExtra("groupId");

        //get group size
        database.child("groups").child(groupId).child("numOfUsers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue(int.class) != null){
                    groupSize = dataSnapshot.getValue(int.class);
                }

            }
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        final ImageButton gettButton = (ImageButton) findViewById(R.id.order_taxi);
        gettButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchGettIntent = getPackageManager().getLaunchIntentForPackage("com.gettaxi.android");
                if( launchGettIntent != null){ //the app is installed on this device, open it
                    startActivity(launchGettIntent);
                }
                else{ //the app is not installed on this device, open its' page on the play store
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
                if( launchPepperIntent != null){ //the app is installed on this device, open it
                    startActivity(launchPepperIntent);
                }
                else{ //the app is not installed on this device, open its' page on the play store
                    Uri playStorePepperPage = Uri.parse("http://play.google.com/store/apps/details?id=com.pepper.pay");
                    Intent playStorePepperIntent = new Intent(Intent.ACTION_VIEW, playStorePepperPage);
                    startActivity(playStorePepperIntent);
                }

            }
        });

    }

}
