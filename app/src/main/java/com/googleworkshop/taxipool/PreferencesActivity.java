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
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
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
    protected EditText passengersEditText;
    public final String HOME_ID = "HOME_ID";
    public final String HOME_SAVED = "HOME_SAVED";
    public final String homePrefs = "UserHomePreferences";
    protected Spinner timeSpinner;

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

        originButton.setText(origin);
        destButton.setText(dest);

        homeCBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                homePrefEditor.putBoolean(HOME_SAVED,compoundButton.isChecked());
                homePrefEditor.apply();
            }
        });
        initializeHome();

        //Spinner timeSpinner = (Spinner) findViewById(R.id.time);
        //timeSpinner = (Spinner) findViewById(R.id.time);
        timeSpinner = findViewById(R.id.time);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> tAdapter = ArrayAdapter.createFromResource(this,
                R.array.relative_time, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        tAdapter.setDropDownViewResource(R.layout.spinner_item);
        // Apply the adapter to the spinner
        timeSpinner.setAdapter(tAdapter);

        //Spinner genderSpinner = (Spinner) findViewById(R.id.gender);
        Spinner genderSpinner = findViewById(R.id.gender);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> gAdapter = ArrayAdapter.createFromResource(this,
                R.array.gender, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        gAdapter.setDropDownViewResource(R.layout.spinner_item);
        // Apply the adapter to the spinner
        genderSpinner.setAdapter(gAdapter);

        //passengersEditText = (EditText)findViewById(R.id.number_of_people);
        passengersEditText = findViewById(R.id.number_of_people);


        if(AutocompleteActivity.selectedPlace != null) {//a place has been selected in autocomplete
            //Intent intent = getIntent();
            //String selectedPlaceName = intent.getStringExtra(AutocompleteActivity.EXTRA_MESSAGE);
            String selectedPlaceName = AutocompleteActivity.selectedPlace.getName().toString();
            Button button;
            if(buttonSearch == 1){//the user clicked on destination before going to autocomplete screen
                button = destButton;
                destPlace = AutocompleteActivity.selectedPlace;//update destination
                dest = selectedPlaceName;//update destination button message
                //originButton.setText(origin);//so that origin will not go back to showing "your current location"
            }
            else{//the user clicked on origin before going to autocomplete screen
                button = originButton;
                originPlace = AutocompleteActivity.selectedPlace;
                origin = selectedPlaceName;
//                destButton.setText(dest);
            }
            button.setText(selectedPlaceName);
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
            String placeId=homeSettings.getString(HOME_ID,null);
            if(placeId!=null) {
                Task<PlaceBufferResponse> response = mGeoDataClient.getPlaceById(placeId);
                response.addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                        if (task.isSuccessful()) {
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
    }

    public void searchDestLocation(View view){//the user clicks on destination before moving to autocomplete screen
        Intent intent = new Intent(this, AutocompleteActivity.class);
        buttonSearch = 1;
        startActivity(intent);
    }
    public void searchOriginLocation(View view){//the user clicks on origin before moving to autocomplete screen
        Intent intent = new Intent(this, AutocompleteActivity.class);
        buttonSearch = 0;
        startActivity(intent);
    }

    public void goToRoute(View view){

        Intent intent = new Intent(this, MatchScreenActivity.class);
        if (destPlace == null){//user has not specified a destination
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
        Log.d("bla","bla");

        startActivity(intent);
    }


    public void goToSearchingScreen(View view){
        Intent intent = new Intent(this, SearchingActivity.class);
        intent.putExtra("numOfSeconds",timeSpinner.getSelectedItemPosition());
        startActivity(intent);
    }

    public static int getNumOfSeconds(int pos){// move to auxiliary class?
        if(pos == 0) {//"Leave in" selected
            return 0;
        }
        if(pos <= 3 && pos >= 1){//"15 min", "30 min" or "45 min" selected
            return pos * 15 * 60;
        }
        return (pos - 3) * 60 * 60;//"1 hour", "2 hours" or "3 hours" selected
    }

    public Request createRequest(){
        LatLng srcLatLng;
        LatLng destLatLng;
        if(destPlace != null){//user chose destination in autocomplete
            destLatLng = destPlace.getLatLng();
        }
        else{
            return null;
        }
        if(originPlace != null){//user chose destination in autocomplete
            srcLatLng = originPlace.getLatLng();
        }
        else{//try and get user current location
            //is this ok?
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                return null;//user has not selected origin in autocomplete and not allowing access to current location
            }
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        currLocation = location;//is it ok to use currLocation?
                    }
                }
            });
            if(currLocation == null){
                return null;//cannot get origin
            }
            srcLatLng = new LatLng(currLocation.getLatitude(), currLocation.getLongitude());
        }
        int nOfSeconds = getNumOfSeconds(timeSpinner.getSelectedItemPosition());
        int nOfPassengers = Integer.parseInt(passengersEditText.getText().toString());
        User user = null;//?
        return new Request(user, srcLatLng, destLatLng, nOfSeconds,nOfPassengers);
    }

}
