package com.googleworkshop.taxipool;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.net.Uri;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class MatchScreenActivity extends NavDrawerActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private LatLng meetingPoint;
    private LatLng furthest = null;
    private float furthestDistance;
    private ArrayList<LatLng> destinations=new ArrayList<>();
    private float distance[] = new float[1];
    private String groupId;
    private DatabaseReference database;
    LatLngBounds.Builder builder;
    private float hue=30;
    private int buddyCount=0;
    private Intent endTripIntent;
    private ArrayList<User> groupUsers = new ArrayList<>();
    private TextView[] names;
    private ImageView[] photos;
    private View goButton;

    //added for directions
    private DirectionsResult directionsResult;
    private com.google.maps.model.LatLng gMeeting;
    private com.google.maps.model.LatLng gDest;
    private com.google.maps.model.LatLng gWaypoints[] = null;
    private LatLng userDest;
    private com.google.maps.model.LatLng userSrc = null;
    private Marker meetingMarker;
    private Polyline currPolyline = null;
    private TextView tTime;
    private TextView tDist;

    //added for navigation drawer
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
        endTripIntent = new Intent(MatchScreenActivity.this, EndTripServiceActivity.class);
        //endTripIntent = new Intent(MatchScreenActivity.this, EndTripServiceActivity.class);//TODO check
        endTripIntent.putExtra("groupId", groupId);
        tTime = findViewById(R.id.totalTime);
        tDist = findViewById(R.id.totalDist);

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
                meetingMarker = mMap.addMarker(new MarkerOptions().position(meetingPoint).title("Meeting Point").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                if (userSrc != null){
                    showDirectionsOnMap();
                }
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
//                        Not adding the markers because we only display directions to meeting point
//                        mMap.addMarker(new MarkerOptions().position(request.destLatLng()).title(requester.getName() + "'s Destination").icon(BitmapDescriptorFactory.defaultMarker(hue)));
                        destinations.add(request.destLatLng());
                        setFurthest(request.destLatLng());
                        if (destinations.contains(furthest)){
                            destinations.remove(destinations.indexOf(furthest));
                        }
                        getRouteInfo();


                        builder.include(request.destLatLng());
                        hue = ((int)hue + 30)%360;
                        fixCamera();
                        if(!user.getUserId().equals(requester.getUserId())){ //don't add curr user to list
                            groupUsers.add(requester);
                            userDest = request.destLatLng();
                            userSrc = new com.google.maps.model.LatLng(request.srcLatLng().latitude,request.srcLatLng().longitude);
                            mMap.addMarker(new MarkerOptions().position(request.destLatLng()).title("Your location").icon(BitmapDescriptorFactory.defaultMarker(hue)));
                            showDirectionsOnMap();
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
    //------

    public void openNavigation(View view){
        Uri navIntentUri = Uri.parse("google.navigation:q="+meetingPoint.latitude+","+meetingPoint.longitude+"&mode=w");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, navIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    private void setFurthest(LatLng dest){
        Location.distanceBetween(meetingPoint.latitude,meetingPoint.longitude,dest.latitude,dest.longitude,distance);
        if (furthest == null || distance[0]>furthestDistance){
            if (furthest != null){
                destinations.add(furthest);
            }
            furthest = dest;
            furthestDistance = distance[0];
        }

    }

    private void getRouteInfo(){
        gMeeting = new com.google.maps.model.LatLng(meetingPoint.latitude,meetingPoint.longitude);
        gDest = new com.google.maps.model.LatLng(furthest.latitude,furthest.longitude);
        gWaypoints = new com.google.maps.model.LatLng[destinations.size()];
        for (int i=0;i<destinations.size();i++){
            gWaypoints[i] = new com.google.maps.model.LatLng(destinations.get(i).latitude,destinations.get(i).longitude);
        }
        try{
            directionsResult = DirectionsApi.newRequest(getGeoContext()).mode(TravelMode.DRIVING).origin(gMeeting).destination(gDest)
                    .waypoints(gWaypoints).optimizeWaypoints(true).await();
        }
        catch(Exception e){
            Log.i("Drive dir error:",e.getMessage());
            return;
        }
        long routeDist = 0;
        long routeTime = 0;
        for (DirectionsLeg leg : directionsResult.routes[0].legs){
            routeTime += leg.distance.inMeters;
            routeTime += leg.duration.inSeconds;
            if (leg.endLocation.equals(userDest)){
                break;
            }
        }
        tDist.setText("Total distance: "+String.format("%.1d",routeDist/(long)1000)+ " km");
        tDist.setText("Estimated time: "+String.format("%d",TimeUnit.SECONDS.toMinutes(routeTime))+ " mins");
    }

    private GeoApiContext getGeoContext() {
        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext.setQueryRateLimit(3).setApiKey(getString(R.string.google_api_key)).setConnectTimeout(1, TimeUnit.SECONDS)
                .setReadTimeout(1, TimeUnit.SECONDS).setWriteTimeout(1, TimeUnit.SECONDS);
    }

    private void showDirectionsOnMap(){
        gDest = new com.google.maps.model.LatLng(meetingPoint.latitude,meetingPoint.longitude);
        try {
            directionsResult = DirectionsApi.newRequest(getGeoContext()).mode(TravelMode.WALKING).origin(userSrc).destination(gDest).await();
            Log.d("WHAT",directionsResult.routes[0].legs[0].steps[0].htmlInstructions);
        }
        catch(Exception e){
            Log.i("Not working..",e.getMessage());
            return;
        }
        if (currPolyline != null){
            currPolyline.remove();
        }
        meetingMarker.setSnippet("Time :"+ directionsResult.routes[0].legs[0].duration.humanReadable +
                " Distance :" + directionsResult.routes[0].legs[0].distance.humanReadable);
        meetingMarker.showInfoWindow();
        List<LatLng> decodedPath = PolyUtil.decode(directionsResult.routes[0].overviewPolyline.getEncodedPath());
        currPolyline = mMap.addPolyline(new PolylineOptions().addAll(decodedPath));
    }
}
