package com.googleworkshop.taxipool;

import android.app.Notification;
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
import android.util.Log;

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

        PendingIntent notificationPendingIntent = PendingIntent.getActivity(context, pendingIntentCount,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        pendingIntentCount++;


        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.ic_taxi_notification)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.ic_taxi_notification))
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

    public static Notification getOngoingNotification(String notificationTitle, String notificationBody, Intent notificationIntent, Context context) {
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

        PendingIntent notificationPendingIntent = PendingIntent.getActivity(context, pendingIntentCount,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        pendingIntentCount++;


        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.ic_taxi_notification)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.ic_taxi_notification))
                .setColor(Color.BLACK)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setContentIntent(notificationPendingIntent);

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        // Dismiss notification once the user touches it.
        //builder.setAutoCancel(true);
        builder.setOngoing(true);

        // Issue the notification
        notificationCount++;
        return builder.build();
    }

    public static void clearAllNotfications(Context context){
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        try {
            mNotificationManager.cancelAll();//delete all previously sent notifications
        }catch (NullPointerException e){
            Log.i("ERROR NotificationUtils", "NullPointerException in cancelAll()");
        }
    }

}
