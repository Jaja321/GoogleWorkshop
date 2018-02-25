package com.googleworkshop.taxipool;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

/**
 * This activity implements the preferences screen, where the user is asked to enter details such as: origin, destination, when would he like to leave etc.
 */
public class PreferencesActivity extends NavDrawerActivity {
    public static int buttonSearch = 0;
    private String dest;
    private Place destPlace = null;
    private String origin;
    private Place originPlace = null;
    protected FusedLocationProviderClient mFusedLocationClient;
    protected SharedPreferences homeSettings;
    protected SharedPreferences.Editor homePrefEditor;
    protected CheckBox homeCBox;
    protected GeoDataClient mGeoDataClient;
    protected Button destButton;
    protected Button originButton;
    public final String HOME_ID = "HOME_ID";
    public final String HOME_SAVED = "HOME_SAVED";
    public final String homePrefs = "UserHomePreferences";
    protected Spinner timeSpinner;
    protected Spinner passengersSpinner;
    private final int ACCESS_FINE_LOCATION_CODE = 17;
    private final int LOCATION_SETTINGS_CODE = 123;
    private int DEST_AUTOCOMPLETE_REQUEST_CODE = 912;
    private int ORIGIN_AUTOCOMPLETE_REQUEST_CODE = 913;
    private User user = null;
    private Request userRequest = null;
    private LatLng srcLatLng;
    private LatLng destLatLng;
    private ProgressBar pBar;
    private String countryISOCode = "IL";// Default, for now
    private AutocompleteFilter allFilter;
    private static DatabaseReference database;
    private FirebaseAuth mAuth;
    //private NavigationView nvDrawer;
    public final String lastRequest = "lastRequest";
    protected SharedPreferences lastRequestSharedPref;
    protected SharedPreferences.Editor lastRequestPrefEditor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        addDrawer();
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Start a new ride");
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mGeoDataClient = Places.getGeoDataClient(this, null);
        homeCBox =  findViewById(R.id.HomeCheckBox);
        homeSettings = getSharedPreferences(homePrefs, 0);
        homePrefEditor = homeSettings.edit();
        destButton = findViewById(R.id.destination);
        originButton = findViewById(R.id.origin);
        pBar = findViewById(R.id.gettingLocationProgress);
        allFilter = new AutocompleteFilter.Builder().setCountry(countryISOCode).build();
        dest = getResources().getString(R.string.destination);
        origin = getResources().getString(R.string.curr_location);
        database= FirebaseDatabase.getInstance().getReference();

        //get current user
        if (user == null){
            user = getIntent().getParcelableExtra("User");
        }
        if(user==null){
            mAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = mAuth.getCurrentUser();
            database.child("users").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    user=dataSnapshot.getValue(User.class);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
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

        timeSpinner = findViewById(R.id.time);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> tAdapter = ArrayAdapter.createFromResource(this,
                R.array.relative_time, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        tAdapter.setDropDownViewResource(R.layout.spinner_item);
        // Apply the adapter to the spinner
        timeSpinner.setAdapter(tAdapter);
        timeSpinner.setSelection(0);

        passengersSpinner  = findViewById(R.id.num_of_passengers);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> pAdapter = ArrayAdapter.createFromResource(this,
                R.array.num_of_passengers, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        pAdapter.setDropDownViewResource(R.layout.spinner_item);
        // Apply the adapter to the spinner
        passengersSpinner.setAdapter(pAdapter);
        passengersSpinner.setSelection(0);

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
        if (requestCode == DEST_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                destPlace = PlaceAutocomplete.getPlace(this, data);
                Log.i("Hey:", "Place: " + destPlace.getName());
                destButton.setText(destPlace.getName().toString());
                homeCBox.setChecked(false);
                homeCBox.setClickable(true);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.i("Hey:", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
            return;
        }
        if (requestCode == ORIGIN_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                originPlace = PlaceAutocomplete.getPlace(this, data);
                Log.i("Hey:", "Place: " + originPlace.getName());
                originButton.setText(originPlace.getName().toString());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i("Hey:", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
            return;
        }
        if (requestCode == LOCATION_SETTINGS_CODE || resultCode == LOCATION_SETTINGS_CODE){
            return;
        }
        if(AutocompleteActivity.selectedPlace != null) {//a place has been selected in autocomplete
            String selectedPlaceName = AutocompleteActivity.selectedPlace.getName().toString();
            Button button;
            if(buttonSearch == 1){//the user clicked on destination before going to autocomplete screen
                button = destButton;
                destPlace = AutocompleteActivity.selectedPlace;//update destination
                dest = selectedPlaceName;//update destination button message
                homeCBox.setChecked(false);
                homeCBox.setClickable(true);
            }
            else{//the user clicked on origin before going to autocomplete screen
                button = originButton;
                originPlace = AutocompleteActivity.selectedPlace;
                origin = selectedPlaceName;
            }
            button.setText(selectedPlaceName);
        }
        else{//delete?
            destButton.setText(dest);
            originButton.setText(origin);
        }
    }

    private void initializeHome(){
        //Using default home location if saved
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
        if (checkOriginDest()){
            createRequest();
            return;
        }
    }


    private void createRequest(){
        long nOfSeconds = PreferencesUtils.getNumOfSeconds(timeSpinner.getSelectedItemPosition());
        writeNumOfSeconds(nOfSeconds);
        int nOfPassengers = Integer.parseInt(passengersSpinner.getItemAtPosition(passengersSpinner.getSelectedItemPosition()).toString());
        userRequest = new Request(user.getUserId(), srcLatLng, destLatLng, nOfSeconds, nOfPassengers);
        if (homeCBox.isChecked()) {
            homePrefEditor.putString(HOME_ID, destPlace.getId());
            homePrefEditor.apply();
        }
        if(originPlace!=null)
            userRequest.origin = originPlace.getName().toString();
        else
            userRequest.origin=null;
        userRequest.destination = destPlace.getName().toString();
        userRequest.timeStamp = System.currentTimeMillis();

        String requestId=ServerUtils.addRequest(userRequest);
        Intent intent = new Intent(this, SearchingActivity2.class);
        intent.putExtra("requestId",requestId);
        intent.putExtra("numOfSeconds",nOfSeconds);//added for searching screen
        intent.putExtra("destLatLng", destLatLng);//Added for Geofencing
        intent.putExtra("destination", destPlace.getName().toString());
        intent.putExtra("origin", userRequest.origin);

        stopService(new Intent(this, SearchingService.class));

        startActivity(intent);
        finish();
    }

    private boolean checkOriginDest(){
        if(destPlace != null){//user chose destination in autocomplete or previously saved one
            destLatLng = destPlace.getLatLng();
        }
        else{
            return false;
        }
        if(originPlace != null){//user chose origin in autocomplete
            srcLatLng = originPlace.getLatLng();
        }
        else{//try and get user current location
            getUserLocation();
            return false;
        }
        return true;
    }

    private void fetchUserLocation(){
        pBar.setVisibility(View.VISIBLE);
        try{mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    // Logic to handle location object
                    pBar.setVisibility(View.INVISIBLE);
                    srcLatLng = new LatLng(location.getLatitude(),location.getLongitude());
                    createRequest();
                    return;
                }
            }
        });}
        catch (SecurityException e){
            // Happens when user didn't allow location on run-time.
            // We will never get here
        }
    }

    private void getUserLocation(){
        if (PreferencesUtils.isLocationEnabled(this)){
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                getPermissions();//user has not selected origin in autocomplete and not allowing access to current location
                return;
            }
            fetchUserLocation();
        }
        else{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Location is disabled. Please enable location or select an origin.");
            dialog.setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    PreferencesActivity.this.startActivityForResult(myIntent,LOCATION_SETTINGS_CODE);
                    //get gps
                }
            });
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    srcLatLng = null;
                }
            });
            dialog.show();
            return;
        }

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
                    fetchUserLocation();
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

    public void placeAutoComplete(View view){
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setFilter(allFilter).build(this);
            if (view.getId() == R.id.destination){
                startActivityForResult(intent, DEST_AUTOCOMPLETE_REQUEST_CODE);
            }
            else{
                startActivityForResult(intent, ORIGIN_AUTOCOMPLETE_REQUEST_CODE);
            }

        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    public void writeNumOfSeconds(long nOfSeconds){
        lastRequestSharedPref = getSharedPreferences(lastRequest, 0);
        lastRequestPrefEditor = lastRequestSharedPref.edit();

        lastRequestPrefEditor.putLong("lastRequestTimeStamp", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
        lastRequestPrefEditor.putLong("lastRequestDuration", nOfSeconds);

        lastRequestPrefEditor.commit();
    }



    @Override
    public void selectDrawerItem(MenuItem menuItem) {
        if(menuItem.getItemId() != R.id.nav_preferences){
            super.selectDrawerItem(menuItem);
        }
    }
}
