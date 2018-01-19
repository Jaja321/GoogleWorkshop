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
        //handler.postDelayed(r, 3600000);//one hour
        handler.postDelayed(r, 10000);//one hour
    }

    private void sendNotification(String notificationDetails) {
        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);//TODO use same channel for two services?

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }

        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(getApplicationContext(), RatingActivity.class);

        notificationIntent.putExtra("groupSize", groupSize);
        notificationIntent.putExtra("groupUsers", groupUsers);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(RatingActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.ic_launcher_background)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_launcher_background))
                .setColor(Color.BLACK)
                .setContentTitle(notificationDetails)
                .setContentText("Rating helps us find you a better match")
                .setContentIntent(notificationPendingIntent);

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
        Log.i("hey", "hey");
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
