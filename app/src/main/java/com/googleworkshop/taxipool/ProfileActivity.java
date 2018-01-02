package com.googleworkshop.taxipool;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private static DatabaseReference database= FirebaseDatabase.getInstance().getReference();
    private String userID;

    //added for navigation drawer
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;
    //------


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        //setContentView(R.layout.activity_profile);

        final TextView userName = (TextView) findViewById(R.id.user_name);
        final ImageView profileImg = (ImageView) findViewById(R.id.profile_img);
        //mAuth = FirebaseAuth.getInstance();
        //FirebaseUser user = mAuth.getCurrentUser(); //get current user
        //userID = user.getUid(); //get user ID of current user

        Intent intent = getIntent();
        User user = intent.getParcelableExtra("User");//TODO: user is null

        userName.setText(user.getName());
        Glide.with(getApplicationContext()).load(user.getProfilePicture()).into(profileImg);
        Log.d("userId", "id: "+user.getUserId());
        LatLng l=new LatLng(0,1);
        Request request=new Request(user.getUserId(),user.getName(), l,l,0,0);
        ServerUtils.addRequest(request);
    /*
        DatabaseReference userRef = database.child("users").child(userID); //get a reference to the user in the database
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) { //Always gets called once, and also whenever the user data changes.
                String name = dataSnapshot.child("name").getValue(String.class); //get the value of 'name' field from the database
                userName.setText(name);
                String profilePicture = dataSnapshot.child("profilePicture").getValue(String.class);
                Glide.with(getApplicationContext()).load(profilePicture).into(profileImg); //Set profile image
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}

        });
        */

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
        switch(menuItem.getItemId()) {
            case R.id.nav_my_profile:
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_sign_out:
                //TODO: add sign_out
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
