package com.googleworkshop.taxipool;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.content.Intent;


public class EndTripActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_end);

        final ImageButton gettButton = (ImageButton) findViewById(R.id.order_taxi);
        gettButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchGettIntent = getPackageManager().getLaunchIntentForPackage("com.gettaxi.android");
                startActivity(launchGettIntent);
            }
        });

        final ImageButton pepperButton = (ImageButton) findViewById(R.id.pepper);
        pepperButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchPepperIntent = getPackageManager().getLaunchIntentForPackage("com.pepper.pay");
                startActivity(launchPepperIntent);
            }
        });
    }
}
