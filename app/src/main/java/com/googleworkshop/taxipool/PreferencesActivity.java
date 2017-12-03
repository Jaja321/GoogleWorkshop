package com.googleworkshop.taxipool;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.tasks.OnSuccessListener;

public class PreferencesActivity extends AppCompatActivity {
    public static int buttonSearch = 0;
    public static String dest = "Destination";
    public static Place destPlace = null;
    public static String origin = "Your current location";
    public static Place originPlace = null;
    protected FusedLocationProviderClient mFusedLocationClient;
    protected static Location currLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        //XXX JERAFI ADDED ME FOR LOCATION
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Spinner timeSpinner = (Spinner) findViewById(R.id.time);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> tAdapter = ArrayAdapter.createFromResource(this,
                R.array.hours, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        tAdapter.setDropDownViewResource(R.layout.spinner_item);
        // Apply the adapter to the spinner
        timeSpinner.setAdapter(tAdapter);

        Spinner genderSpinner = (Spinner) findViewById(R.id.gender);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> gAdapter = ArrayAdapter.createFromResource(this,
                R.array.gender, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        gAdapter.setDropDownViewResource(R.layout.spinner_item);
        // Apply the adapter to the spinner
        genderSpinner.setAdapter(gAdapter);

        if(AutocompleteActivity.place1 != null) {
            // Get the Intent that started this activity and extract the string
            Intent intent = getIntent();
            String message = intent.getStringExtra(AutocompleteActivity.EXTRA_MESSAGE);
            Button button;
            // Capture the layout's TextView and set the string as its text
            if(buttonSearch == 1){
                button = findViewById(R.id.destination);
                destPlace = AutocompleteActivity.place1;
                dest = message;
                Button originButton = findViewById(R.id.origin);
                originButton.setText(origin);
            }
            else{
                button = findViewById(R.id.origin);
                originPlace = AutocompleteActivity.place1;
                origin = message;
                Button destButton = findViewById(R.id.destination);
                destButton.setText(dest);
            }
            button.setText(message);
        }
        else{//delete?
            Button destButton = findViewById(R.id.destination);
            destButton.setText(dest);
            Button originButton = findViewById(R.id.origin);
            originButton.setText(origin);
        }

    }

    public void searchDestLocation(View view){
        Intent intent = new Intent(this, AutocompleteActivity.class);
        buttonSearch = 1;
        startActivity(intent);
    }
    public void searchOriginLocation(View view){
        Intent intent = new Intent(this, AutocompleteActivity.class);
        buttonSearch = 0;
        startActivity(intent);
    }
    //XXX JERAFI ADDED TO GO TO THE MAP

    public void goToRoute(View view){
        Intent intent = new Intent(this, MatchScreenActivity.class);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    // Logic to handle location object
                    currLocation = location;
                }
            }
        });
        startActivity(intent);
    }

}
