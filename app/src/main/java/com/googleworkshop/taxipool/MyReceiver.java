package com.googleworkshop.taxipool;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

/**
 * Created by Gal Ze'evi on 1/3/2018.
 */

// Defines a generic receiver used to pass data to Activity from a Service
public class MyReceiver extends ResultReceiver {
    private Receiver receiver;

    // Constructor takes a handler
    public MyReceiver(Handler handler) {
        super(handler);
    }

    // Setter for assigning the receiver
    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    // Defines our event interface for communication
    public interface Receiver {
        void onReceiveResult(int resultCode, Bundle resultData);
    }

    // Delegate method which passes the result to the receiver if the receiver has been assigned
    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (receiver != null) {
            receiver.onReceiveResult(resultCode, resultData);
        }
        else{
            Log.i("Service alert","Service is null, unable to receive data");
        }
    }
}