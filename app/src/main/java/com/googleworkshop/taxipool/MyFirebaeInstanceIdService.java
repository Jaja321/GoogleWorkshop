package com.googleworkshop.taxipool;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaeInstanceIdService extends FirebaseInstanceIdService {
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        ServerUtils.updateToken(refreshedToken);
    }
}
