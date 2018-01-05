package com.googleworkshop.taxipool;

import android.content.Intent;
import android.content.res.Configuration;
import android.opengl.Visibility;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class MatchScreenActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private LatLng meetingPoint;
    private String groupId;
    private DatabaseReference database;
    LatLngBounds.Builder builder;
    //private float hue=30f;
    private float hue=30;
    private int buddyCount=0;
    private Intent intent;
    private ArrayList<User> groupUsers = new ArrayList<>();
    private TextView[] names;
    private ImageView[] photos;

    //added for navigation drawer
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;
    private User user;
    private User tmpUser;
    private FirebaseAuth mAuth;
    //------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_screen);
        database = FirebaseDatabase.getInstance().getReference();
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);
        groupId=getIntent().getStringExtra("groupId");
        initViews();
        intent = new Intent(MatchScreenActivity.this, RatingActivity.class);


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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        builder = new LatLngBounds.Builder();
        mMap = googleMap;
        handleMeetingPoint(); //Get the group's meeting point and put it on the map
        handleRequests(); //Get the group's requests and deal with them
        intent.putExtra("groupSize", buddyCount);
        intent.putExtra("groupUsers", groupUsers);

    }

    private void initViews(){
        names=new TextView[3];
        photos=new ImageView[3];
        names[0]=(TextView)findViewById(R.id.buddy1_text);
        names[1]=(TextView)findViewById(R.id.buddy2_text);
        names[2]=(TextView)findViewById(R.id.buddy3_text);
        photos[0]=(ImageView)findViewById(R.id.buddy1);
        photos[1]=(ImageView)findViewById(R.id.buddy2);
        photos[2]=(ImageView)findViewById(R.id.buddy3);
    }


    private void fixCamera(){
        LatLngBounds bounds = builder.build();
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,100));
    }

    private void handleMeetingPoint(){
        DatabaseReference meetingPointRef = database.child("groups").child(groupId).child("meetingPoint");
        meetingPointRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                meetingPoint=ServerUtils.strToLatlng(dataSnapshot.getValue(String.class));
                mMap.addMarker(new MarkerOptions().position(meetingPoint).title("Meeting Point").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                builder.include(meetingPoint);
                fixCamera();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }



    private void handleRequests(){
        DatabaseReference requestRef=database.child("requests");
        user = intent.getParcelableExtra("User");
        if(user==null) {
            mAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = mAuth.getCurrentUser();
            database.child("users").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    user = dataSnapshot.getValue(User.class);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
        requestRef.orderByChild("groupId").equalTo(groupId).addChildEventListener(new ChildEventListener() {
            //For each request in group:
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                final Request request=dataSnapshot.getValue(Request.class);
                tmpUser = dataSnapshot.getValue(User.class);
                if(!user.getName().equals(tmpUser.getName())){ //don't add curr user to list
                    groupUsers.add(tmpUser);
                }
                database.child("users").child(request.getRequesterId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User requester=dataSnapshot.getValue(User.class);
                        mMap.addMarker(new MarkerOptions().position(request.destLatLng()).title(requester.getName() + "'s Destination").icon(BitmapDescriptorFactory.defaultMarker(hue)));
                        builder.include(request.destLatLng());
                        hue = ((int)hue + 30)%360;
                        fixCamera();
                        if(buddyCount < 3) {//names and photos are arrays of size 3 so buddyCount == 3 causes crash
                            Log.d("buddyCount",buddyCount+" ");
                            TextView textView=names[buddyCount];
                            ImageView imageView=photos[buddyCount];
                            textView.setText(requester.getName());
                            RequestOptions options = new RequestOptions();
                            options.circleCrop();
                            Glide.with(getApplicationContext()).load(requester.getProfilePicture()).apply(options).into(imageView);
                            textView.setVisibility(View.VISIBLE);
                            imageView.setVisibility(View.VISIBLE);

                        }
                        buddyCount++;
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //TODO maybe: Deal with someone leaving the group.
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

//    private LatLng getUserLocation(){
//        if (PreferencesActivity.originPlace == null){
//            return new LatLng(PreferencesActivity.currLocation.getLatitude(),PreferencesActivity.currLocation.getLongitude());
//        }
//        else{
//            return PreferencesActivity.originPlace.getLatLng();
//        }
//    }

    public void goToChat(View view){
        Intent chatIntent=new Intent(this,ChatActivity.class);
        chatIntent.putExtra("groupId",groupId);
        startActivity(chatIntent);
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
