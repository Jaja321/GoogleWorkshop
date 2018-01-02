package com.googleworkshop.taxipool;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_screen);
        database = FirebaseDatabase.getInstance().getReference();
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);
        groupId=getIntent().getStringExtra("groupId");
        intent = new Intent(MatchScreenActivity.this, RatingActivity.class);

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
        requestRef.orderByChild("groupId").equalTo(groupId).addChildEventListener(new ChildEventListener() {
            //For each request in group:
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                buddyCount++;
                final Request request=dataSnapshot.getValue(Request.class);
                groupUsers.add(dataSnapshot.getValue(User.class));
                database.child("users").child(request.getRequesterId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User requester=dataSnapshot.getValue(User.class);
                        mMap.addMarker(new MarkerOptions().position(request.destLatLng()).title(requester.getName() + "'s Destination").icon(BitmapDescriptorFactory.defaultMarker(hue)));
                        builder.include(request.destLatLng());
                        hue = ((int)hue + 30)%360;
                        fixCamera();
                        //buddyCount++;
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
}
