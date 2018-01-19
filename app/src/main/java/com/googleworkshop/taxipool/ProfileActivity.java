package com.googleworkshop.taxipool;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends NavDrawerActivity {
    private FirebaseAuth mAuth;
    private static DatabaseReference database= FirebaseDatabase.getInstance().getReference();
    private String userID;

    private TextView userName, rating, report;
    private ImageView profileImg;
    private User user;
    private User currUser;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        addDrawer();


        report = (TextView) findViewById(R.id.report);
        userName = (TextView) findViewById(R.id.user_name);
        rating = (TextView) findViewById(R.id.rating);
        profileImg = (ImageView) findViewById(R.id.profile_img);

        Intent intent = getIntent();
        user = intent.getParcelableExtra("User");
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        database.child("users").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currUser = dataSnapshot.getValue(User.class);
                if (user == null) {
                    user = currUser;
                }
                initProfile(user);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });



    }
    private void initProfile(User user){
        userName.setText(user.getName());
        rating.setText(user.getRating()+"/5.0");
        Glide.with(getApplicationContext()).load(user.getProfilePicture()).into(profileImg);
        if(!currUser.getUserId().equals(user.getUserId())){ //TODO: this does not work
            report.setVisibility(View.VISIBLE);
            report.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ProfileActivity.this);

                    // set title
                    alertDialogBuilder.setTitle("Report");

                    // set dialog message
                    alertDialogBuilder
                            .setMessage("Would you like to report this user?")
                            .setCancelable(false)
                            .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    //TODO: report
                                    dialog.cancel();
                                }
                            })
                            .setNegativeButton("No",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    // if this button is clicked, just close
                                    // the dialog box and do nothing
                                    dialog.cancel();
                                }
                            });

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();
                }
            });


        }
    }

    /*
    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        //Fragment fragment = null;
        Intent intent;
        switch(menuItem.getItemId()) {
            case R.id.nav_my_profile:
                break;//do nothing, already in profile
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
        //menuItem.setChecked(true);
        // Set action bar title
        //setTitle(menuItem.getTitle());
        // Close the navigation drawer
        mDrawer.closeDrawers();
    }*/

    @Override
    public void selectDrawerItem(MenuItem menuItem) {
        if(menuItem.getItemId() != R.id.nav_my_profile){
            super.selectDrawerItem(menuItem);
        }
    }

}
