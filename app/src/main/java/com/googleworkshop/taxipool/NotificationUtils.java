package com.googleworkshop.taxipool;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

/**
 * Created by Gal Ze'evi on 1/16/2018.
 */

public class NotificationUtils {
    private static final String CHANNEL_ID = "channel_01";
    private static int notificationCount = 10;
    private static int pendingIntentCount = 1;
    private static NotificationChannel mChannel = null;

    public static void sendNotification(String notificationTitle, String notificationBody, Intent notificationIntent, Context context) {
        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //CharSequence name = getString(R.string.app_name);
            if(mChannel == null){
                CharSequence name = "TaxiPoolNotificationChannel";
                // Create the channel for the notification
                mChannel =
                        new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

                // Set the Notification Channel for the Notification Manager.
                mNotificationManager.createNotificationChannel(mChannel);
            }
        }

        //Intent notificationIntent = new Intent(getApplicationContext(), RatingActivity.class);

        /*
        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Add the main Activity to the task stack as the parent.
        //ComponentName componentName = notificationIntent.getComponent();
        stackBuilder.addParentStack(RatingActivity.class);//TODO check

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);//TODO check
         */

        PendingIntent notificationPendingIntent = PendingIntent.getActivity(context, pendingIntentCount,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        pendingIntentCount++;


        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.ic_launcher_background)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.ic_launcher_background))
                .setColor(Color.BLACK)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setContentIntent(notificationPendingIntent);

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Issue the notification
        mNotificationManager.notify(notificationCount, builder.build());
        notificationCount++;
    }

}
