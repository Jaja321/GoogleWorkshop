package com.googleworkshop.taxipool;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.android.gms.location.places.Place;

public class PreferencesActivity extends AppCompatActivity {
    public static int buttonSearch = 0;
    public static String dest = "Destination";
    public static Place destPlace = null;
    public static String origin = "Your current location";
    public static Place originPlace = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

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

}
