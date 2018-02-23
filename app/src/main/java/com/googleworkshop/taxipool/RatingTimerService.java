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

public class RatingTimerService extends IntentService {
    private static final String CHANNEL_ID = "channel_02";//TODO change ID?
    private final String TAG = "taxipool.geofence";
    private ArrayList<User> groupUsers;
    private int groupSize;
    final Handler handler = new Handler();
    final Runnable r = new Runnable() {
        public void run() {
            sendNotification();
        }
    };

    public RatingTimerService() {
        super("taxipool.ratingTimerService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        groupSize = intent.getIntExtra("groupSize", 0);
        groupUsers = (ArrayList<User>) intent.getSerializableExtra("groupUsers");
        handler.postDelayed(r, 3600000);//one hour
        //handler.postDelayed(r, 10000);//one hour
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
