package com.googleworkshop.taxipool;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class ChatNotificationService extends FirebaseMessagingService {
    String userId;
    public static Intent intent;


    public ChatNotificationService(){
        intent=null;
        FirebaseAuth mAuth=FirebaseAuth.getInstance();
        userId=mAuth.getCurrentUser().getUid();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
       if(!remoteMessage.getData().get("senderId").equals(userId)){
           //notificationIntent.setPackage(null); // The golden row !!!
           intent.putExtra("chat", true);
           NotificationUtils.sendNotification("You have a new message!" ,"Click to view it",
                   intent, getApplicationContext());
       }
    }
}
