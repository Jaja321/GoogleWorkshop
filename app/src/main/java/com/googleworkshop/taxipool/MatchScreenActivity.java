package com.googleworkshop.taxipool;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
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


public class MatchScreenActivity extends NavDrawerActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private LatLng meetingPoint;
    private String groupId;
    private DatabaseReference database;
    LatLngBounds.Builder builder;
    //private float hue=30f;
    private float hue=30;
    private int buddyCount=0;
    private Intent endTripIntent;
    private ArrayList<User> groupUsers = new ArrayList<>();
    private TextView[] names;
    private ImageView[] photos;
    private View goButton;

    private User user;
    private User tmpUser;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_screen);
        addDrawer();
        database = FirebaseDatabase.getInstance().getReference();
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);
        groupId=getIntent().getStringExtra("groupId");
        initViews();
        endTripIntent = new Intent(MatchScreenActivity.this, EndTripActivity.class);
        //endTripIntent = new Intent(MatchScreenActivity.this, EndTripServiceActivity.class);//TODO check
        endTripIntent.putExtra("groupId", groupId);

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        builder = new LatLngBounds.Builder();
        mMap = googleMap;
        handleMeetingPoint(); //Get the group's meeting point and put it on the map
        handleRequests(); //Get the group's requests and deal with them
        endTripIntent.putExtra("groupSize", buddyCount);
        endTripIntent.putExtra("groupUsers", groupUsers);


    }

    private void initViews(){
        goButton = findViewById(R.id.goButton);
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
        DatabaseReference groupRef=database.child("groups").child(groupId);
        DatabaseReference meetingPointRef = groupRef.child("meetingPoint");
        meetingPointRef.addValueEventListener(new ValueEventListener() {
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
        DatabaseReference groupClosedRef=groupRef.child("closed");
        groupClosedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean closed=dataSnapshot.getValue(boolean.class);
                if(closed){
                    goButton.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }



    private void handleRequests(){
        DatabaseReference requestRef=database.child("requests");
        user = endTripIntent.getParcelableExtra("User");
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
                database.child("users").child(request.getRequesterId()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final User requester=dataSnapshot.getValue(User.class);
                        String firstName=requester.getName().split(" ")[0];
                        mMap.addMarker(new MarkerOptions().position(request.destLatLng()).title(requester.getName() + "'s Destination").icon(BitmapDescriptorFactory.defaultMarker(hue)));
                        builder.include(request.destLatLng());
                        hue = ((int)hue + 30)%360;
                        fixCamera();
                        if(!user.getUserId().equals(requester.getUserId())){ //don't add curr user to list
                            groupUsers.add(requester);
                        }
                        if(buddyCount < 3) {//names and photos are arrays of size 3 so buddyCount == 3 causes crash
                            Log.d("buddyCount",buddyCount+" ");
                            TextView textView=names[buddyCount];
                            ImageView imageView=photos[buddyCount];
                            textView.setText(firstName);
                            RequestOptions options = new RequestOptions();
                            options.circleCrop();
                            Glide.with(getApplicationContext()).load(requester.getProfilePicture()).apply(options).into(imageView);
                            textView.setVisibility(View.VISIBLE);
                            imageView.setVisibility(View.VISIBLE);
                            imageView.setClickable(true);
                            imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent profileIntent=new Intent(MatchScreenActivity.this,ProfileActivity.class);
                                    profileIntent.putExtra("User",requester);
                                    startActivity(profileIntent);
                                }
                            });

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

    public void closeGroup(View view){
        //Show "are you sure?" dialog
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Ready to go? People won't be able to join the group anymore."); //TODO word it better..
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Go!",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        database.child("groups").child(groupId).child("closed").setValue(true);
                        //chatIntent.putExtra("groupId",groupId);
                        endTripIntent.putExtra("destLatLng", getIntent().getParcelableExtra("destLatLng"));
                        startActivity(endTripIntent);
                        goButton.setVisibility(View.INVISIBLE);
                    }
                });

        builder1.setNegativeButton(
                "Not yet",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();

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
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                break;
            case R.id.nav_preferences:
                intent = new Intent(this, PreferencesActivity.class);
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
    }
    //------

}
