package com.googleworkshop.taxipool;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by Gal Ze'evi on 1/15/2018.
 */

/**
 * This service is launched if we could not add a Geofence in End Trip Activity.
 * It sends a notification asking the user to rate his group after a set amount of time
 */

public class RatingTimerService extends IntentService {
    private ArrayList<User> groupUsers;
    private int groupSize;
    final Handler handler = new Handler();
    final Runnable r = new Runnable() {
        public void run() {
            sendNotification();
        }
    };//This will run when the set time has passed

    public RatingTimerService() {
        super("taxipool.ratingTimerService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        try {
            groupSize = intent.getIntExtra("groupSize", 0);
            groupUsers = (ArrayList<User>) intent.getSerializableExtra("groupUsers");
            handler.postDelayed(r, 3600000);//one hour
        }catch (NullPointerException e){
            Log.i("TimerService Error","groupSize = intent.getIntExtra(\"groupSize\", 0) caused a NullPointerException");
            stopSelf();
        }
    }

    public void sendNotification(){
        String title = "Please rate your travel buddies";
        String body = "Rating helps us find you a better match";

        Intent ratingIntent = new Intent(getApplicationContext(), RatingActivity.class);
        ratingIntent.putExtra("groupSize", groupSize);
        ratingIntent.putExtra("groupUsers", groupUsers);

        NotificationUtils.sendNotification(title, body, ratingIntent, getApplicationContext());
    }


}
