package com.googleworkshop.taxipool;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class MatchScreenActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_screen);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLngBounds ROUTE;
        mMap = googleMap;
        LatLng rabinSq = new LatLng(32.0795, 34.7802);
        LatLng mock1 = new LatLng(32.082,34.7944);
        LatLng mock2 = new LatLng(32.086,34.7911);
        LatLng user = getUserLocation();
        mMap.addMarker(new MarkerOptions().position(user).title("Meeting point").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        mMap.addMarker(new MarkerOptions().position(mock1).title("Mock1 destination").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        mMap.addMarker(new MarkerOptions().position(mock2).title("Mock2 destination").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(rabinSq).title("User destination").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));

        ROUTE = new LatLngBounds(rabinSq,user);
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(ROUTE,100));

    }
    private LatLng getUserLocation(){
        if (PreferencesActivity.originPlace == null){
            return new LatLng(PreferencesActivity.currLocation.getLatitude(),PreferencesActivity.currLocation.getLongitude());
        }
        else{
            return PreferencesActivity.originPlace.getLatLng();
        }
    }

    public void goToChat(View view){
        startActivity(new Intent(this,ChatActivity.class));
    }
}