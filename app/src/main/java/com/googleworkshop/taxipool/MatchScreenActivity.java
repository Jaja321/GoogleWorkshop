package com.googleworkshop.taxipool;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
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
import com.google.firebase.database.Query;
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
    LatLngBounds.Builder builder, destBuilder;
    private float hue=30;
    private int buddyCount=0;
    private Intent endTripIntent;
    private ArrayList<User> groupUsers = new ArrayList<>();
    private TextView[] names;
    private ImageView[] photos;
    private View goButton;
    private boolean groupIsClosed;
    private SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    //added for directions
    private DirectionsResult directionsResult;
    private com.google.maps.model.LatLng gMeeting;
    private com.google.maps.model.LatLng gDest;
    private com.google.maps.model.LatLng gWaypoints[] = null;
    private LatLng userDest;
    private com.google.maps.model.LatLng userSrc = null;
    private Marker meetingMarker=null;
    private Polyline meetPolyline = null;
    private Polyline drivePolyline = null;
    private List<LatLng> meetEncodedPolyline = null;
    private List<LatLng> driveEncodedPolyline = null;
    private TextView tTime;
    private TextView tDist;
    private View srcCameraButton, destCameraButton;
    private boolean updated=false;
    private Request currentUserRequest;
    ChildEventListener requestChangeListener;
    Query requestsInGroup;

    private User user;
    private FirebaseAuth mAuth;
    public final static int CHAT_ACTIVITY_CODE = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_screen);
        addDrawer();
        getSupportActionBar().setTitle("Your group");
        database = FirebaseDatabase.getInstance().getReference();
        sharedPreferences=this.getSharedPreferences("requestId", Context.MODE_PRIVATE);
        currentUserRequestId=sharedPreferences.getString("requestId",null);
        currentUserRequest =ClientUtils.getRequest(getApplicationContext());
        editor=sharedPreferences.edit();
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);
        groupId=currentUserRequest.getGroupId();
        initViews();
        endTripIntent = new Intent(MatchScreenActivity.this, EndTripServiceActivity.class);
        //endTripIntent = new Intent(MatchScreenActivity.this, EndTripServiceActivity.class);//TODO check
        endTripIntent.putExtra("groupId", groupId);
        boolean chat=getIntent().getBooleanExtra("chat", false);
        tTime = findViewById(R.id.totalTime);
        tDist = findViewById(R.id.totalDist);
        srcCameraButton=findViewById(R.id.srcButton);
        destCameraButton=findViewById(R.id.destButton);
        if(chat)
            goToChat(null);


    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        builder = new LatLngBounds.Builder();
        destBuilder = new LatLngBounds.Builder();
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


    private void fixCamera(LatLngBounds.Builder boundsBuilder){
        boundsBuilder.include(meetingPoint);
        LatLngBounds bounds = boundsBuilder.build();
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,100));
    }

    public void srcCamera(View view){
        fixCamera(builder);
        drivePolyline.remove();
        meetPolyline = mMap.addPolyline(new PolylineOptions().addAll(meetEncodedPolyline));
        destCameraButton.setVisibility(View.VISIBLE);
        srcCameraButton.setVisibility(View.GONE);
    }

    public void destCamera(View view){
        fixCamera(destBuilder);
        meetPolyline.remove();
        drivePolyline = mMap.addPolyline(new PolylineOptions().addAll(driveEncodedPolyline));
        destCameraButton.setVisibility(View.GONE);
        srcCameraButton.setVisibility(View.VISIBLE);
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
                if(meetingMarker != null){
                    meetingMarker.remove();
                }
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
                    stopService(new Intent(MatchScreenActivity.this, SearchingService.class));
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
        requestsInGroup= requestRef.orderByChild("groupId").equalTo(groupId);
        requestChangeListener= new ChildEventListener() {
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
                        mMap.addMarker(new MarkerOptions().position(request.destLatLng()).title(firstName + "'s Destination").icon(BitmapDescriptorFactory.defaultMarker(hue)));
                        destinations.add(request.destLatLng());
                        destBuilder.include(request.destLatLng());
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
                if(dataSnapshot.getKey().equals(currentUserRequestId)) {
                    return;
                }
                buddyCount--;
                if (buddyCount == 1 && !groupIsClosed) {
                    database.child("groups").child(groupId).child("closed").setValue(true);
                    requestsInGroup.removeEventListener(this);
                    //Too few people, group is closed. Create a new request and go back to searching activity.
                    if(currentUserRequest.timeStamp + currentUserRequest.getTimePrefs()*1000 < System.currentTimeMillis()){
                        //request expired
                        Intent preferencesIntent = new Intent(MatchScreenActivity.this, PreferencesActivity.class);
                        preferencesIntent.putExtra("User", user);
                        startActivity(preferencesIntent);
                        finish();
                    }

                    long timeStamp = currentUserRequest.getTimeStamp();
                    long timePrefs = currentUserRequest.getTimePrefs();
                    long currentTime = System.currentTimeMillis();
                    currentUserRequest.setTimeStamp(currentTime);
                    long timePassed = TimeUnit.MILLISECONDS.toSeconds(currentTime - timeStamp);
                    currentUserRequest.setTimePrefs(timePrefs - timePassed);
                    
                    Intent searchingIntent=new Intent(MatchScreenActivity.this,SearchingActivity2.class);
                    String requestId=ServerUtils.addRequest(currentUserRequest);
                    searchingIntent.putExtra("requestId",requestId);
                    searchingIntent.putExtra("origin", currentUserRequest.origin);
                    searchingIntent.putExtra("destination", currentUserRequest.destination);
                    searchingIntent.putExtra("numOfSeconds", currentUserRequest.getTimePrefs());
                    editor.putString("requestId",requestId);
                    editor.putString("origin", currentUserRequest.origin);
                    editor.putString("destination", currentUserRequest.destination);//do we need both?
                    editor.commit();
                    //searchingIntent.putExtra("request",currentUserRequest);
                    Toast.makeText(MatchScreenActivity.this, "You were left Alone in the group. Searching again...",
                            Toast.LENGTH_SHORT).show();
                    startActivity(searchingIntent);
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
        };
        requestsInGroup.addChildEventListener(requestChangeListener);
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
        //setUserLayout(user, 0);
        for(int i=0;i<buddyCount-1;i++)
            setUserLayout(groupUsers.get(i), i);
        for(int i=buddyCount;i<4;i++) {
            names[i].setVisibility(View.INVISIBLE);
            photos[i].setVisibility(View.INVISIBLE);
        }
    }

    public void goToChat(View view){
        Intent chatIntent=new Intent(this,ChatActivity.class);
        chatIntent.putExtra("groupId",groupId);
        //startActivity(chatIntent);
        startActivityForResult(chatIntent, CHAT_ACTIVITY_CODE);
    }

    public void closeGroup(View view){
        //Show "are you sure?" dialog
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Ready to go? No more users will join the group."); //TODO word it better..
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Go!",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        database.child("groups").child(groupId).child("closed").setValue(true);
                        //chatIntent.putExtra("groupId",groupId);
                        endTripIntent.putExtra("destLatLng", getIntent().getParcelableExtra("destLatLng"));
                        //startActivityForResult(endTripIntent, 13);
                        stopService(new Intent(MatchScreenActivity.this, SearchingService.class));
                        startActivity(endTripIntent);
                        goButton.setVisibility(View.GONE);
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
        fixCamera(builder);
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
        if (drivePolyline != null){
            drivePolyline.remove();
        }
        driveEncodedPolyline = PolyUtil.decode(directionsResult.routes[0].overviewPolyline.getEncodedPath());
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
        if (meetPolyline != null){
            meetPolyline.remove();
        }
        meetingMarker.setSnippet("Time :"+ directionsResult.routes[0].legs[0].duration.humanReadable +
                " Distance :" + directionsResult.routes[0].legs[0].distance.humanReadable);
        meetingMarker.showInfoWindow();
        meetEncodedPolyline = PolyUtil.decode(directionsResult.routes[0].overviewPolyline.getEncodedPath());
        meetPolyline = mMap.addPolyline(new PolylineOptions().addAll(meetEncodedPolyline));
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
                        NotificationManager mNotificationManager =
                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        try {
                            mNotificationManager.cancelAll();//delete all previously sent notifications
                        }catch (NullPointerException e){
                            Log.i("null pointer", "NullPointerException in cancelAll()");
                        }

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
        requestsInGroup.removeEventListener(requestChangeListener);
        final Intent preferencesIntent = new Intent(this, PreferencesActivity.class);
        preferencesIntent.putExtra("User",user);
        //TODO I moved this here b/c if someone already pressed GO and the user clicks "find a new ride" this should still happen
        editor.putString("requestId",null);
        editor.putString("origin",null);
        editor.putString("destination",null);
        editor.commit();
        if(!groupIsClosed) {
            final DatabaseReference groupRef=database.child("groups").child(groupId);
            groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    int numOfUsers=dataSnapshot.child("numOfUsers").getValue(int.class);
                    int numOfPassengers=dataSnapshot.child("numOfPassengers").getValue(int.class);
                    if(numOfUsers<=2){
                        //Delete group:
                        groupRef.removeValue();
                    }else {
                        groupRef.child("numOfUsers").setValue(numOfUsers - 1);
                        groupRef.child("numOfPassengers").setValue(numOfPassengers - currentUserRequest.getNumOfPassengers());
                    }
                    //editor.putString("requestId",null);
                    //editor.putString("origin",null);
                    //editor.putString("destination",null);
                    //editor.commit();
                    database.child("requests").child(currentUserRequestId).setValue(null);

                }
                @Override
                public void onCancelled(DatabaseError firebaseError) {
                }
            });

        }
        stopService(new Intent(MatchScreenActivity.this, SearchingService.class));
        startActivity(preferencesIntent);
        finish();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CHAT_ACTIVITY_CODE || requestCode == PROFILE_ACTIVITY_CODE) {
            if(resultCode == Activity.RESULT_OK){
                boolean newRide=data.getBooleanExtra("newRide", false);
                if(newRide)
                    gotoPreferences();
            }
        }
    }

}
