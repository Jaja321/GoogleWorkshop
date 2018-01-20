package com.googleworkshop.taxipool;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


public class ThankYouActivity extends NavDrawerActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thank_you);
        addDrawer();
        getSupportActionBar().setTitle("Come back soon");
        final Button newTripButton = (Button) findViewById(R.id.find_new_trip);
        newTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(ThankYouActivity.this, PreferencesActivity.class);
                startActivity(myIntent);
            }
        });
    }
}
