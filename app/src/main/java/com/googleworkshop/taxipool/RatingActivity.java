package com.googleworkshop.taxipool;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
<<<<<<< HEAD
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
=======
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
>>>>>>> 33c950a4238a5999d1f08b6e41264162f0c19e41

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


public class RatingActivity extends AppCompatActivity {
<<<<<<< HEAD
    private ArrayList<User> groupUsers;
    private int groupSize;
=======
    private String groupId;
    private DatabaseReference database;
    //added for navigation drawer
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;
    //------
>>>>>>> 33c950a4238a5999d1f08b6e41264162f0c19e41


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
<<<<<<< HEAD
        
        groupSize = getIntent().getIntExtra("groupSize", 0);
        groupUsers = (ArrayList<User>) getIntent().getSerializableExtra("groupUsers");
        //TODO: make sure groupUsers does not incluse curr user
        //TODO: change user names in layout
        if(groupSize == 4){
            setContentView(R.layout.activity_rating3);
            final RatingBar ratingBar1 = (RatingBar) findViewById(R.id.ratingBar1);
            final RatingBar ratingBar2 = (RatingBar) findViewById(R.id.ratingBar2);
            final RatingBar ratingBar3 = (RatingBar) findViewById(R.id.ratingBar3);
            Button submitButton = (Button) findViewById(R.id.submit);
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ServerUtils.rateUser(groupUsers.get(0), ratingBar1.getNumStars());
                    ServerUtils.rateUser(groupUsers.get(1), ratingBar2.getNumStars());
                    ServerUtils.rateUser(groupUsers.get(2), ratingBar3.getNumStars());
                    finish();
                    System.exit(0);
                }
            });

        }
        else if(groupSize == 3){
            setContentView(R.layout.activity_rating3);
            final RatingBar ratingBar1 = (RatingBar) findViewById(R.id.ratingBar1);
            final RatingBar ratingBar2 = (RatingBar) findViewById(R.id.ratingBar2);
            Button submitButton = (Button) findViewById(R.id.submit);
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ServerUtils.rateUser(groupUsers.get(0), ratingBar1.getNumStars());
                    ServerUtils.rateUser(groupUsers.get(1), ratingBar2.getNumStars());
                    finish();
                    System.exit(0);
                }
            });

        }
        else if(groupSize == 2){
            setContentView(R.layout.activity_rating3);
            final RatingBar ratingBar1 = (RatingBar) findViewById(R.id.ratingBar1);
            Button submitButton = (Button) findViewById(R.id.submit);
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ServerUtils.rateUser(groupUsers.get(0), ratingBar1.getNumStars());
                    finish();
                    System.exit(0);
                }
            });

        }
        else{
            finish();
            System.exit(0);
        }
=======
        database = FirebaseDatabase.getInstance().getReference();
        groupId=getIntent().getStringExtra("groupId");

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
>>>>>>> 33c950a4238a5999d1f08b6e41264162f0c19e41
    }

    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */
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
                //TODO:add sign_out
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
