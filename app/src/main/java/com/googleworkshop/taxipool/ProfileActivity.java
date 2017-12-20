package com.googleworkshop.taxipool;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth=FirebaseAuth.getInstance();
        FirebaseUser user=mAuth.getCurrentUser(); //get current user

        TextView userName=(TextView)findViewById(R.id.user_name);
        ImageView imageView = (ImageView)findViewById(R.id.profile_img);

        userName.setText(user.getDisplayName()); //Set user name
        Glide.with(getApplicationContext()).load(user.getPhotoUrl()).into(imageView); //Set profile image
    }

}
