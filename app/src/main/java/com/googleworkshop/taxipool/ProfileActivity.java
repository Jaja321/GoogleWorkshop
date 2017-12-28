package com.googleworkshop.taxipool;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private static DatabaseReference database= FirebaseDatabase.getInstance().getReference();
    private String userID;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final TextView userName = (TextView) findViewById(R.id.user_name);
        final ImageView profileImg = (ImageView) findViewById(R.id.profile_img);
        //mAuth = FirebaseAuth.getInstance();
        //FirebaseUser user = mAuth.getCurrentUser(); //get current user
        //userID = user.getUid(); //get user ID of current user

        Intent intent = getIntent();
        User user=intent.getParcelableExtra("User");

        userName.setText(user.getName());
        Glide.with(getApplicationContext()).load(user.getProfilePicture()).into(profileImg);
        Log.d("userId", "id: "+user.getUserId());
        LatLng l=new LatLng(0,1);
        Request request=new Request(user.getUserId(),user.getName(), l,l,0,0);
        ServerUtils.addRequest(request);
    /*
        DatabaseReference userRef = database.child("users").child(userID); //get a reference to the user in the database
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) { //Always gets called once, and also whenever the user data changes.
                String name = dataSnapshot.child("name").getValue(String.class); //get the value of 'name' field from the database
                userName.setText(name);
                String profilePicture = dataSnapshot.child("profilePicture").getValue(String.class);
                Glide.with(getApplicationContext()).load(profilePicture).into(profileImg); //Set profile image
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}

        });
        */

    }
}
