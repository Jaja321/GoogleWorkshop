package com.googleworkshop.taxipool;

import android.app.Activity;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gal Ze'evi on 1/10/2018.
 */

public class GeofenceRatingIntentService extends IntentService {
    private static final String CHANNEL_ID = "channel_01";
    private final String TAG = "taxipool.geofence";
    private ArrayList<User> groupUsers;
    private int groupSize;

    public GeofenceRatingIntentService() {
        super("GeofenceRatingIntentService");
    }

    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        groupSize = intent.getIntExtra("groupSize", 0);
        Bundle groupUsersBundle = intent.getBundleExtra("groupUsersBundle");
        groupUsers = groupUsersBundle.getParcelableArrayList("groupUsers");

        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Log.i("User Enter","User Enter");

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(geofenceTransition, triggeringGeofences);

            // Send notification and log the transition details.
            //sendNotification(geofenceTransitionDetails);
            //sendNotification("Please rate your travel buddies");
            sendNotification();
            Log.i(TAG, geofenceTransitionDetails);
        } else {
            // Log the error.
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type,
                    geofenceTransition));
        }
    }

    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);
            default:
                return getString(R.string.unknown_geofence_transition);
        }
    }

    private String getGeofenceTransitionDetails(
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ",  triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
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