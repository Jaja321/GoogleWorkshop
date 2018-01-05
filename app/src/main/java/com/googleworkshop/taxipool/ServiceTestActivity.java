package com.googleworkshop.taxipool;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Gal Ze'evi on 1/3/2018.
 */

public class ServiceTestActivity extends Activity{
    public MyReceiver receiverForTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searching_screen_layout);
        setupServiceReceiver();
        onStartService();
        //launchTestService();
    }

    // Starts the IntentService
    public void onStartService() {
        Intent i = new Intent(this, MyTestService.class);
        i.putExtra("foo", "bar");
        i.putExtra("receiver", receiverForTest);
        startService(i);
    }

    // Setup the callback for when data is received from the service
    public void setupServiceReceiver() {
        receiverForTest = new MyReceiver(new Handler());
        // This is where we specify what happens when data is received from the service
        receiverForTest.setReceiver(new MyReceiver.Receiver() {
            @Override
            public void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == 10) {
                    String resultValue = resultData.getString("resultValue");
                    Toast.makeText(ServiceTestActivity.this, resultValue, Toast.LENGTH_SHORT).show();
                    TextView textView = (TextView)findViewById(R.id.timer);
                    //SystemClock.sleep(20000);
                    textView.setText("Hello");
                }
            }
        });
    }

    public void launchTestService() {
        // Construct our Intent specifying the Service
        Intent i = new Intent(this, MyTestService.class);
        // Add extras to the bundle
        i.putExtra("foo", "bar");
        // Start the service
        startService(i);
    }
}