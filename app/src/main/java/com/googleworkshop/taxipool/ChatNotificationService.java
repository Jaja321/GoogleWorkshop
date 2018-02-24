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

public class ChatNotificationService extends FirebaseMessagingService {
    String userId;
    public static boolean isChatOpen;


    public ChatNotificationService(){
        FirebaseAuth mAuth=FirebaseAuth.getInstance();
        userId=mAuth.getCurrentUser().getUid();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if(isChatOpen)
            return;
        Intent intent=new Intent(this, MatchScreenActivity.class);
       if(!remoteMessage.getData().get("senderId").equals(userId)){

           NotificationUtils.clearLastNotification(getApplicationContext());
           //notificationIntent.setPackage(null); // The golden row !!!
           intent.putExtra("chat", true);
           intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
           NotificationUtils.sendNotification("You have a new message!" ,"Click to view it",
                   intent, getApplicationContext());
       }
    }
}
