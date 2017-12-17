package com.googleworkshop.taxipool;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class PreferencesActivity extends AppCompatActivity {
    public static int buttonSearch = 0;
    public static String dest = "Destination";
    public static Place destPlace = null;
    public static String origin = "Your current location";
    public static Place originPlace = null;
    protected FusedLocationProviderClient mFusedLocationClient;
    protected static Location currLocation;
    protected SharedPreferences homeSettings;
    protected SharedPreferences.Editor homePrefEditor;
    protected CheckBox homeCBox;
    protected GeoDataClient mGeoDataClient;
    protected Button destButton;
    protected Button originButton;
    public final String HOME_ID = "HOME_ID";
    public final String HOME_SAVED = "HOME_SAVED";
    public final String homePrefs = "UserHomePreferences";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        //XXX JERAFI ADDED ME FOR LOCATION
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mGeoDataClient = Places.getGeoDataClient(this, null);
        homeCBox =  findViewById(R.id.HomeCheckBox);
        homeSettings = getSharedPreferences(homePrefs, 0);
        homePrefEditor = homeSettings.edit();
        destButton = findViewById(R.id.destination);
        originButton = findViewById(R.id.origin);


        homeCBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                homePrefEditor.putBoolean(HOME_SAVED,compoundButton.isChecked());
                homePrefEditor.apply();
            }
        });
        initializeHome();

        Spinner timeSpinner = (Spinner) findViewById(R.id.time);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> tAdapter = ArrayAdapter.createFromResource(this,
                R.array.relative_time, R.layout.spinner_item);
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
                button = destButton;
                destPlace = AutocompleteActivity.place1;
                dest = message;
                originButton.setText(origin);
            }
            else{
                button = originButton;
                originPlace = AutocompleteActivity.place1;
                origin = message;
//                destButton.setText(dest);
            }
            button.setText(message);
        }
        else{//delete?
            destButton.setText(dest);
            originButton.setText(origin);
        }
        if (destPlace==null){
            homeCBox.setClickable(false);
        }
        else{
            homeCBox.setClickable(true);
            homeCBox.setChecked(homeSettings.getBoolean(HOME_SAVED,false));
            destButton.setText(destPlace.getName());
        }

    }

    private void initializeHome(){
        //JERAFI Using default home location if saved
        if ((destPlace == null) && (homeSettings.getBoolean(HOME_SAVED,false))){
            Task<PlaceBufferResponse> response = mGeoDataClient.getPlaceById(homeSettings.getString(HOME_ID,null));
            response.addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                @Override
                public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                    if (task.isSuccessful()){
                        PlaceBufferResponse homeBuffer = task.getResult();
                        destPlace = homeBuffer.get(0);
                        homeCBox.setChecked(true);
                        homeCBox.setClickable(true);
                        destButton.setText(destPlace.getName());

                    }
                }
            });
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

    public void goToRoute(View view){
        Intent intent = new Intent(this, MatchScreenActivity.class);
        if (destPlace == null){
            return;
        }
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
        if (homeCBox.isChecked()) {
            homePrefEditor.putString(HOME_ID, destPlace.getId());
            homePrefEditor.putBoolean(HOME_SAVED,true);
        }
        else{
            homePrefEditor.putBoolean(HOME_SAVED,false);
        }
        homePrefEditor.apply();
        startActivity(intent);
    }
}
