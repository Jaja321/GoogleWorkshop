package com.googleworkshop.taxipool;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
    private String dest = "Destination";
    private Place destPlace = null;
    private String origin = "Your current location";
    private Place originPlace = null;
    protected FusedLocationProviderClient mFusedLocationClient;
    private LatLng currLocation;
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
    private final int ACCESS_FINE_LOCATION_CODE = 17;
    private User user = null;

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

        // TODO find a better way to get user..
        if (user == null){
            user = getIntent().getParcelableExtra("User");
        }

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

        if (destPlace==null){
            homeCBox.setClickable(false);
        }
        else{
            homeCBox.setClickable(true);
            homeCBox.setChecked(homeSettings.getBoolean(HOME_SAVED,false));
            destButton.setText(destPlace.getName());
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(AutocompleteActivity.selectedPlace != null) {//a place has been selected in autocomplete
            //Intent intent = getIntent();
            //String selectedPlaceName = intent.getStringExtra(AutocompleteActivity.EXTRA_MESSAGE);
            String selectedPlaceName = AutocompleteActivity.selectedPlace.getName().toString();
            Button button;
            if(buttonSearch == 1){//the user clicked on destination before going to autocomplete screen
                button = destButton;
                destPlace = AutocompleteActivity.selectedPlace;//update destination
                dest = selectedPlaceName;//update destination button message
                homeCBox.setChecked(false);
                homeCBox.setClickable(true);
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
        startActivityForResult(intent,buttonSearch);
    }
    public void searchOriginLocation(View view){//the user clicks on origin before moving to autocomplete screen
        Intent intent = new Intent(this, AutocompleteActivity.class);
        buttonSearch = 0;
        startActivityForResult(intent,buttonSearch);
    }

    public void goToRoute(View view){
        Log.d("debugtag","start of gotoRoute");
        Intent intent = new Intent(this, SearchingActivity.class);
        Log.d("debugtag","before createRequest");
        Request request = createRequest();
        Log.d("debugtag","after createRequest");
        if (request == null){ // could not set dest or src
            Log.d("debugtag","request == null");
            return;
        }
        /*
        if (homeCBox.isChecked()) {
            homePrefEditor.putString(HOME_ID, destPlace.getId());
            homePrefEditor.apply();
        }
        */
        Log.d("debugtag","before adding request");
        String requestId=ServerUtils.addRequest(request);

        intent.putExtra("requestId",requestId);
        Log.d("debugtag","end of gotoRoute");
        startActivity(intent);
    }


    public void goToSearchingScreen(View view){
        Intent intent = new Intent(this, SearchingActivity.class);
        //TODO delete because already included in request?
        intent.putExtra("numOfSeconds",timeSpinner.getSelectedItemPosition());
        startActivity(intent);
    }

    private static int getNumOfSeconds(int pos){// move to auxiliary class?
        if(pos == 0) {//"Leave in" selected
            return 0;
        }
        if(pos <= 3 && pos >= 1){//"15 min", "30 min" or "45 min" selected
            return pos * 15 * 60;
        }
        return (pos - 3) * 60 * 60;//"1 hour", "2 hours" or "3 hours" selected
    }

    @Nullable
    private Request createRequest(){
        LatLng srcLatLng;
        LatLng destLatLng;
        if(destPlace != null){//user chose destination in autocomplete or previously saved one
            destLatLng = destPlace.getLatLng();
        }
        else{
            return null;
        }
        if(originPlace != null){//user chose origin in autocomplete
            srcLatLng = originPlace.getLatLng();
        }
        else{//try and get user current location
            getUserLocation();
            if(currLocation == null){
                return null;//cannot get origin
            }
            srcLatLng = currLocation;
        }
        // TODO how does this int help us?
        int nOfSeconds = getNumOfSeconds(timeSpinner.getSelectedItemPosition());
        // TODO use edittext.getselected?
        int nOfPassengers = Integer.parseInt(passengersEditText.getText().toString());
        return new Request(user.getUserId(),user.getName(), srcLatLng, destLatLng, nOfSeconds,nOfPassengers);
    }

    private void getUserLocation(){
        //is this ok?
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            getPermissions();
            return;//user has not selected origin in autocomplete and not allowing access to current location
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    // Logic to handle location object
                    currLocation = new LatLng(location.getLatitude(),location.getLongitude());
                }
            }
        });

    }

    public void getPermissions(){
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},ACCESS_FINE_LOCATION_CODE);
    }
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.i("I am here","This is good..");
        switch (requestCode) {
            case ACCESS_FINE_LOCATION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

}
