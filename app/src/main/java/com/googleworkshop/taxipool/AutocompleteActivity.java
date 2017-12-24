package com.googleworkshop.taxipool;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;



/**
 * Created by Gal Ze'evi on 11/28/2017.
 */

public class AutocompleteActivity extends AppCompatActivity {
    private static final String TAG = PreferencesActivity.class.getSimpleName();
    //public static final String EXTRA_MESSAGE = "com.example.galzeevi.google_workshop_preferences.MESSAGE";//I think this can be deleted
    public static Place selectedPlace = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.autocomplete_layout);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .build();
        autocompleteFragment.setFilter(typeFilter);
   
   //use current location?
   //country code filter
        String countryISOCode = "IL";//default, for now
        /*
        TelephonyManager teleMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        if (teleMgr != null){
            countryISOCode = teleMgr.getSimCountryIso();
        }
        */

        AutocompleteFilter countryFilter = new AutocompleteFilter.Builder()
                .setCountry(countryISOCode)
                .build();

        autocompleteFragment.setFilter(countryFilter);
 
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());//get place details here
                selectedPlace = place;
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    public void writeLocation(View view){
        if(selectedPlace != null) {
//            Intent intent = new Intent(this, PreferencesActivity.class);
            //intent.putExtra(EXTRA_MESSAGE, selectedPlace.getName().toString());
//            startActivity(intent);
            finish();
        }
    }

}
