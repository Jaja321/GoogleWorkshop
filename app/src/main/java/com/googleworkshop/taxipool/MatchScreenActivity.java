package com.googleworkshop.taxipool;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Toast;

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
    private String groupId,currentUserRequestId;
    private DatabaseReference database;
    LatLngBounds.Builder builder;
    private float hue=30;
    private int buddyCount=0;
    private Intent endTripIntent;
    private ArrayList<User> groupUsers = new ArrayList<>();
    private TextView[] names;
    private ImageView[] photos;
    private View goButton;
    private boolean groupIsClosed;
    private SharedPreferences sharedPreferences;

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
    private boolean updated=false;

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
        sharedPreferences=this.getSharedPreferences("requestId", Context.MODE_PRIVATE);
        currentUserRequestId=sharedPreferences.getString("requestId",null);
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
        names=new TextView[4];
        photos=new ImageView[4];
        names[0]=(TextView)findViewById(R.id.buddy1_text);
        names[1]=(TextView)findViewById(R.id.buddy2_text);
        names[2]=(TextView)findViewById(R.id.buddy3_text);
        names[3]=(TextView)findViewById(R.id.buddy4_text);
        photos[0]=(ImageView)findViewById(R.id.buddy1);
        photos[1]=(ImageView)findViewById(R.id.buddy2);
        photos[2]=(ImageView)findViewById(R.id.buddy3);
        photos[3]=(ImageView)findViewById(R.id.buddy4);
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
                if(!dataSnapshot.exists())
                    return;
                meetingPoint=ServerUtils.strToLatlng(dataSnapshot.getValue(String.class));
                meetingMarker = mMap.addMarker(new MarkerOptions().position(meetingPoint).title("Meeting Point").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                if (updated&&userSrc != null){
                    handleDirections();
                }
                else{
                    updated=true;
                }
                builder.include(meetingPoint);
//                fixCamera();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        DatabaseReference groupClosedRef=groupRef.child("closed");
        groupClosedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists())
                    return;
                groupIsClosed=dataSnapshot.getValue(boolean.class);
                if(groupIsClosed){
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
                        if (destinations.size()>1&&userSrc!=null){
                            if (updated){
                                handleDirections();
                            }
                            else{
                                updated=true;
                            }
                        }

//                        builder.include(request.destLatLng());
                        hue = ((int)hue + 30)%360;
//                        fixCamera();
                        if(!user.getUserId().equals(requester.getUserId())){ //don't add curr user to list
                            groupUsers.add(requester);
                        }
                        else{
                            userDest = request.destLatLng();
                            userSrc = new com.google.maps.model.LatLng(request.srcLatLng().latitude,request.srcLatLng().longitude);
                            mMap.addMarker(new MarkerOptions().position(request.srcLatLng()).title("Your location").icon(BitmapDescriptorFactory.defaultMarker(hue)));
                            builder.include(request.srcLatLng());
//                            fixCamera();
                        }
                        buddyCount++;
                        updateLayout();

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
                if(dataSnapshot.getKey()==currentUserRequestId)
                    return;
                buddyCount--;
                if (buddyCount == 1 && !groupIsClosed) {
                    Toast.makeText(MatchScreenActivity.this, "Someone left the group, please search again.",
                            Toast.LENGTH_SHORT).show();
                    Intent preferencesIntent=new Intent(MatchScreenActivity.this, PreferencesActivity.class);
                    preferencesIntent.putExtra("User",user);
                    database.child("groups").child(groupId).child("closed").setValue(true);
                    SharedPreferences.Editor editor=MatchScreenActivity.this.getSharedPreferences("requestId", Context.MODE_PRIVATE).edit();
                    editor.putString("requestId",null);
                    editor.commit();
                    startActivity(preferencesIntent);
                    finish();
                } else {
                    final Request request = dataSnapshot.getValue(Request.class);
                    String requesterId = request.getRequesterId();
                    removeUserFromList(requesterId);
                    updateLayout();
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void setUserLayout(final User user, int index) {
        TextView textView = names[index];
        ImageView imageView = photos[index];
        textView.setText(user.getName().split(" ")[0]);
        RequestOptions options = new RequestOptions();
        options.circleCrop();
        Glide.with(getApplicationContext()).load(user.getProfilePicture()).apply(options).into(imageView);
        textView.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.VISIBLE);
        imageView.setClickable(true);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(MatchScreenActivity.this, ProfileActivity.class);
                profileIntent.putExtra("User", user);
                startActivity(profileIntent);
            }
        });
    }

    private void removeUserFromList(String userId){
        for(int i=0;i<groupUsers.size();i++){
            if(groupUsers.get(i).getUserId().equals(userId)){
                groupUsers.remove(i);
                return;
            }
        }
    }

    private void updateLayout(){
        setUserLayout(user, 0);
        for(int i=1;i<buddyCount;i++)
            setUserLayout(groupUsers.get(i-1), i);
        for(int i=buddyCount;i<4;i++) {
            names[i].setVisibility(View.INVISIBLE);
            photos[i].setVisibility(View.INVISIBLE);
        }


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

    private void handleDirections(){
        setFurthest();
        if (destinations.contains(furthest)){
            destinations.remove(destinations.indexOf(furthest));
        }
        getRouteInfo();
        destinations.add(furthest);
        showDirectionsOnMap();
        fixCamera();
        updated=false;
    }


    private void setFurthest(){
        Location.distanceBetween(meetingPoint.latitude,meetingPoint.longitude,destinations.get(0).latitude,destinations.get(0).longitude,distance);
        furthestDistance = distance[0];
        furthest = destinations.get(0);
        for (LatLng dest : destinations){
            Location.distanceBetween(meetingPoint.latitude,meetingPoint.longitude,dest.latitude,dest.longitude,distance);
            if (distance[0]>furthestDistance){
                furthest = dest;
                furthestDistance = distance[0];
            }
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
            tDist.setText("Distance: N/A");
            tTime.setText("Duration: N/A");
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
        tDist.setText("Distance: "+directionsResult.routes[0].legs[0].distance.humanReadable);
        tTime.setText("Duration: "+directionsResult.routes[0].legs[0].duration.humanReadable);
//        tDist.setText("Total distance: "+String.format("%.1d",routeDist/(long)1000)+ " km");
//        tTime.setText("Estimated time: "+String.format("%d",TimeUnit.SECONDS.toMinutes(routeTime))+ " mins");
        updated = false;
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
            Log.i("Draw Direction Fail",e.getMessage());
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

    @Override
    public void gotoPreferences(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Leave the group and find a new ride?");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        findANewRide();
                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();

    }

    /*
Delete the current request and go to Preferences screen
 */
    private void findANewRide() {
        final Intent preferencesIntent = new Intent(this, PreferencesActivity.class);
        preferencesIntent.putExtra("User",user);
        if(!groupIsClosed) {
            database.child("requests").child(currentUserRequestId).setValue(null);
            final DatabaseReference groupRef=database.child("groups").child(groupId);
            groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    int numOfUsers=dataSnapshot.child("numOfUsers").getValue(int.class);
                    int numOfPassengers=dataSnapshot.child("numOfPassengers").getValue(int.class);
                    if(numOfUsers==1){
                        //Delete group:
                        groupRef.setValue(null);
                    }else {
                        groupRef.child("numOfUsers").setValue(numOfUsers - 1);
                        groupRef.child("numOfPassengers").setValue(numOfPassengers - 1);
                    }
                    SharedPreferences.Editor editor=sharedPreferences.edit();
                    editor.putString("requestId",null);
                    editor.commit();
                    startActivity(preferencesIntent);
                    finish();
                }
                @Override
                public void onCancelled(DatabaseError firebaseError) {
                }
            });

        }else {
            startActivity(preferencesIntent);
            finish();
        }
    }

}
