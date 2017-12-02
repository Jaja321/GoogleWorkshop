package com.googleworkshop.taxipool;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class RouteOverviewActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_overview);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLngBounds ROUTE;
        mMap = googleMap;
        LatLng rabinSq = new LatLng(32.0795, 34.7802);
        LatLng user = getUserLocation();
        mMap.addMarker(new MarkerOptions().position(user).title("Marker in user location"));
        mMap.addMarker(new MarkerOptions().position(rabinSq).title("Marker in Rabin Square"));

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
}