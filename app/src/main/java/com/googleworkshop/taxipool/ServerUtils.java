package com.googleworkshop.taxipool;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Benjamin on 25/12/2017.
 */

public class ServerUtils {
    private static DatabaseReference database= FirebaseDatabase.getInstance().getReference();
    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();

    /*
    Create a new user in the database and return a User object.
     */
    public static User createNewUser(FirebaseUser firebaseUser){
        DatabaseReference userReference=database.child("users").child(firebaseUser.getUid());
        User user=new User(firebaseUser.getUid(),firebaseUser.getDisplayName(),firebaseUser.getPhotoUrl().toString());
        userReference.setValue(user);
        return user;
    }

    public static String addRequest(Request request){
        DatabaseReference requestsRef=database.child("requests");
        String requestId= requestsRef.push().getKey();
        requestsRef.child(requestId).setValue(request);
        return requestId;
    }

    public static void rateUser(User user, int rating){
        int num=user.getNumOfRaters();
        double oldRating=user.getRating();
        double newRating=(oldRating*num+rating)/(num+1);
        DatabaseReference userReference=database.child("users").child(user.getUserId());
        userReference.child("rating").setValue(newRating);
        userReference.child("numOfRaters").setValue(num+1);

    }

    public static LatLng strToLatlng(String str){
        String[] latlong =  str.split(",");
        double latitude = Double.parseDouble(latlong[0]);
        double longitude = Double.parseDouble(latlong[1]);
        return new LatLng(latitude,longitude);
    }

    public static void updateToken(String token){
        FirebaseUser currentUser=mAuth.getCurrentUser();
        DatabaseReference userReference=database.child("users").child(currentUser.getUid());
        userReference.child("messageToken").setValue(token);

    }


}
