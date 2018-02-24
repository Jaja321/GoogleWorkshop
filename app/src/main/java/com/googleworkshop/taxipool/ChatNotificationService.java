package com.googleworkshop.taxipool;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * This service runs in the background and alerts the user if he has received a new message using a notification
 */

public class ChatNotificationService extends FirebaseMessagingService {
    String userId;
    private static int lastNotification;


    public ChatNotificationService(){
        FirebaseAuth mAuth=FirebaseAuth.getInstance();
        userId=mAuth.getCurrentUser().getUid();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Intent intent=new Intent(this, MatchScreenActivity.class);
       if(!remoteMessage.getData().get("senderId").equals(userId)){

           NotificationManager mNotificationManager =
                   (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
           try {
               if(lastNotification!=0)
                   mNotificationManager.cancel(lastNotification);
           }catch (NullPointerException e){
               Log.i("ERROR NotificationUtils", "NullPointerException in cancel()");
           }
           //notificationIntent.setPackage(null); // The golden row !!!
           intent.putExtra("chat", true);
           lastNotification=NotificationUtils.sendNotification("You have a new message!" ,"Click to view it",
                   intent, getApplicationContext());
           Log.d("LastNotification",lastNotification+"");
       }
    }
}
