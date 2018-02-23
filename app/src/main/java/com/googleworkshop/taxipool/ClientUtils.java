package com.googleworkshop.taxipool;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.concurrent.TimeUnit;

/**
 * Created by Gal Ze'evi on 2/13/2018.
 */

/**
 * The purpose of this class is to have functions that are used by multiple activities.
 * Right now it contains functions for handling the user's request as it moves from one activity to the next.
 * But it does not have to be limited only to that.
 */
public class ClientUtils {

    public ClientUtils(){}

    /**
     * This function flattens the request and saves it in a shared preferences.
     */
    public static void saveRequest(Request request, Context context){
        if(context == null){
            Log.i("ERROR in ClientUtils","You may not use this function with context == null");
            return;
        }
        if(request == null){
            clearRequest(context);
            return;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("request", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("requesterId", request.getRequesterId());
        editor.putString("src", request.getSrc());
        editor.putString("dest", request.getDest());
        editor.putLong("timePrefs", request.getTimePrefs());
        editor.putInt("numOfPassengers", request.getNumOfPassengers());
        editor.putString("groupId", request.getGroupId());
        editor.putString("origin", request.getOrigin());
        editor.putString("destination", request.getDestination());
        editor.putString("requestId", request.getRequestId());
        editor.putLong("timeStamp", request.getTimeStamp());

        editor.commit();//This writes immediately
    }


    public static void clearRequest(Context context){
        if(context == null){
            Log.i("ERROR in ClientUtils","You may not use this function with context == null");
            return;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("request", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("requesterId", null);
        editor.putString("src", null);
        editor.putString("dest", null);
        editor.putLong("timePrefs", 0);
        editor.putInt("numOfPassengers", 0);
        editor.putString("groupId", null);
        editor.putString("origin", null);
        editor.putString("destination", null);
        editor.putString("requestId", null);
        editor.putLong("timeStamp", 0);

        editor.commit();//This writes immediately
    }


    /**
     * This functions returns the request saved in the sharedPreferences or null.
     * Note: this function does not change the timeStamp and numOfSeconds as to leave it with a simple purpose
     *       a seperate function exists that also updates the request (move the time forward and etc.
     *       Therefore, this function should not be called from outside this class
     */
    public static Request unpackRequest(Context context){
        if(context == null){
            Log.i("ERROR in ClientUtils","You may not use this function with context == null");
            return null;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("request", Context.MODE_PRIVATE);
        Request request = null;
        if(sharedPreferences.getString("requesterId", null) != null) {
            request = new Request(sharedPreferences.getString("requesterId", null),
                    sharedPreferences.getString("src", null),//Are all these necessary?
                    sharedPreferences.getString("dest", null),
                    sharedPreferences.getLong("timePrefs", 0),
                    sharedPreferences.getInt("numOfPassengers", 0),
                    sharedPreferences.getString("groupId", null),
                    sharedPreferences.getString("origin", null),
                    sharedPreferences.getString("destination", null),
                    sharedPreferences.getString("requestId", null),
                    sharedPreferences.getLong("timeStamp", 0));
        }
        return request;
    }

    /**
     * This function returns: timePrefs - (time passed since timeStamp) in seconds
     * and updates request.timeStamp the request
     * Note: this function does not check if the result is negative and will not return -1 if it is.
     * Instead, we leave the negative number to give an estimate of how much time has passed.
     */
    public static void updateRequest(Request request){
        if(request == null){
            return;
        }
        long timeStamp = request.getTimeStamp();
        long timePrefs = request.getTimePrefs();
        long currentTime = System.currentTimeMillis();
        request.setTimeStamp(currentTime);
        long timePassed = TimeUnit.MILLISECONDS.toSeconds(currentTime - timeStamp);
        request.setTimePrefs(timePrefs - timePassed);
    }

    public static Request getRequest(Context context){
        Request request = unpackRequest(context);
        updateRequest(request);
        saveRequest(request, context);
        return request;
    }


}
