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

    //added for navigation drawer
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;
    //------



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
        pBar = findViewById(R.id.gettingLocationProgress);
        allFilter = new AutocompleteFilter.Builder().setCountry(countryISOCode).setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS).build();


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
        timeSpinner.setSelection(0);

        //Jerafi commented out the gender spinner

//        //Spinner genderSpinner = (Spinner) findViewById(R.id.gender);
//        Spinner genderSpinner = findViewById(R.id.gender);
//        // Create an ArrayAdapter using the string array and a default spinner layout
//        ArrayAdapter<CharSequence> gAdapter = ArrayAdapter.createFromResource(this,
//                R.array.gender, R.layout.spinner_item);
//        // Specify the layout to use when the list of choices appears
//        gAdapter.setDropDownViewResource(R.layout.spinner_item);
//        // Apply the adapter to the spinner
//        genderSpinner.setAdapter(gAdapter);
//        genderSpinner.setSelection(0);
//
        passengersSpinner  = findViewById(R.id.num_of_passengers);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> pAdapter = ArrayAdapter.createFromResource(this,
                R.array.num_of_passengers, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        pAdapter.setDropDownViewResource(R.layout.spinner_item);
        // Apply the adapter to the spinner
        passengersSpinner.setAdapter(pAdapter);
        passengersSpinner.setSelection(0);

        //passengersEditText = (EditText)findViewById(R.id.number_of_people);
        //XXX CHANGED TO SPINNER
//        passengersEditText = findViewById(R.id.number_of_people);

        if (destPlace==null){
            homeCBox.setClickable(false);
        }
        else{
            homeCBox.setClickable(true);
            homeCBox.setChecked(homeSettings.getBoolean(HOME_SAVED,false));
            destButton.setText(destPlace.getName());
        }

        //added for navigation drawer
        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = setupDrawerToggle();

        // Tie DrawerLayout events to the ActionBarToggle
        mDrawer.addDrawerListener(drawerToggle);
        // Find our drawer view
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        // Setup drawer view
        setupDrawerContent(nvDrawer);
        //-------


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
                // TODO: Handle the error.
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
        if (checkOriginDest()){
            createRequest();
            return;
        }
    }

    /*
    public void goToSearchingScreen(View view){
        Intent intent = new Intent(this, SearchingActivity.class);
        //TODO delete because already included in request?
        intent.putExtra("numOfSeconds",getNumOfSeconds(timeSpinner.getSelectedItemPosition()));
        startActivity(intent);
    }
    */

    private void createRequest(){
        int nOfSeconds = PreferencesUtils.getNumOfSeconds(timeSpinner.getSelectedItemPosition());
        int nOfPassengers = Integer.parseInt(passengersSpinner.getItemAtPosition(passengersSpinner.getSelectedItemPosition()).toString());
        userRequest = new Request(user.getUserId(), srcLatLng, destLatLng, nOfSeconds, nOfPassengers);
        if (homeCBox.isChecked()) {
            homePrefEditor.putString(HOME_ID, destPlace.getId());
            homePrefEditor.apply();
        }
        String requestId=ServerUtils.addRequest(userRequest);
        Intent intent = new Intent(this, SearchingActivity.class);
        //Intent intent = new Intent(this, SearchingServiceActivity.class);
        intent.putExtra("requestId",requestId);
        intent.putExtra("numOfSeconds",PreferencesUtils.getNumOfSeconds(timeSpinner.getSelectedItemPosition()));//added for searching screen
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
            //            if(currLocation == null){
//                return null;//cannot get origin
//            }
//            srcLatLng = currLocation;
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
            //is this ok?
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                getPermissions();//user has not selected origin in autocomplete and not allowing access to current location
                return;
            }
            fetchUserLocation();
        }
        else{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("GPS signal is disabled :(");
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


    //added for navigation drawer
    private ActionBarDrawerToggle setupDrawerToggle() {
        // NOTE: Make sure you pass in a valid toolbar reference.  ActionBarDrawToggle() does not require it
        // and will not render the hamburger icon without it.
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open,  R.string.drawer_close);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        //Fragment fragment = null;
        switch(menuItem.getItemId()) {
            case R.id.nav_my_profile:
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_sign_out:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                break;
            default:
                //?
        }

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        //setTitle(menuItem.getTitle());
        // Close the navigation drawer
        mDrawer.closeDrawers();
    }
    //------



}
