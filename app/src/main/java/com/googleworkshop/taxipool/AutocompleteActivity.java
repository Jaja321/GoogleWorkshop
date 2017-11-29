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
    public static final String EXTRA_MESSAGE = "com.example.galzeevi.google_workshop_preferences.MESSAGE";
    public static Place place1 = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.autocomplete_layout);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

    /*
    * The following code example shows setting an AutocompleteFilter on a PlaceAutocompleteFragment to
    * set a filter returning only results with a precise address.
    */
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .build();
        autocompleteFragment.setFilter(typeFilter);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());//get place details here
                place1 = place;
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    public void writeLocation(View view){
        if(place1 != null) {
            Intent intent = new Intent(this, PreferencesActivity.class);
            intent.putExtra(EXTRA_MESSAGE, place1.getName().toString());
            startActivity(intent);
        }
    }

}
