package com.googleworkshop.taxipool;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


public class RatingActivity extends AppCompatActivity {
    private ArrayList<User> groupUsers;
    private int groupSize;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        groupSize = getIntent().getIntExtra("groupSize", 0);
        groupUsers = (ArrayList<User>) getIntent().getSerializableExtra("groupUsers");
        //TODO: make sure groupUsers does not incluse curr user
        //TODO: change user names in layout
        if(groupSize == 4){
            setContentView(R.layout.activity_rating3);
            final RatingBar ratingBar1 = (RatingBar) findViewById(R.id.ratingBar1);
            final RatingBar ratingBar2 = (RatingBar) findViewById(R.id.ratingBar2);
            final RatingBar ratingBar3 = (RatingBar) findViewById(R.id.ratingBar3);
            Button submitButton = (Button) findViewById(R.id.submit);
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ServerUtils.rateUser(groupUsers.get(0), ratingBar1.getNumStars());
                    ServerUtils.rateUser(groupUsers.get(1), ratingBar2.getNumStars());
                    ServerUtils.rateUser(groupUsers.get(2), ratingBar3.getNumStars());
                    finish();
                    System.exit(0);
                }
            });

        }
        else if(groupSize == 3){
            setContentView(R.layout.activity_rating3);
            final RatingBar ratingBar1 = (RatingBar) findViewById(R.id.ratingBar1);
            final RatingBar ratingBar2 = (RatingBar) findViewById(R.id.ratingBar2);
            Button submitButton = (Button) findViewById(R.id.submit);
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ServerUtils.rateUser(groupUsers.get(0), ratingBar1.getNumStars());
                    ServerUtils.rateUser(groupUsers.get(1), ratingBar2.getNumStars());
                    finish();
                    System.exit(0);
                }
            });

        }
        else if(groupSize == 2){
            setContentView(R.layout.activity_rating3);
            final RatingBar ratingBar1 = (RatingBar) findViewById(R.id.ratingBar1);
            Button submitButton = (Button) findViewById(R.id.submit);
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ServerUtils.rateUser(groupUsers.get(0), ratingBar1.getNumStars());
                    finish();
                    System.exit(0);
                }
            });

        }
        else{
            finish();
            System.exit(0);
        }
    }
}
