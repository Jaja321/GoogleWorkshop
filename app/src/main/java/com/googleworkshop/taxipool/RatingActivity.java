package com.googleworkshop.taxipool;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * This activity is the screen where the user is asked to rate his travel buddies.
 * Rating is used to find better matches in the future.
 */

public class RatingActivity extends NavDrawerActivity {
    private ArrayList<User> groupUsers;
    private int groupSize;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        groupSize = getIntent().getIntExtra("groupSize", 0);
        groupUsers = (ArrayList<User>) getIntent().getSerializableExtra("groupUsers");
        //load the xml the corresponds to the group size
        if(groupSize == 4){
            setContentView(R.layout.activity_rating3);
            addDrawer();
            if(getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Please rate your group");
            }
            final RatingBar ratingBar1 = (RatingBar) findViewById(R.id.ratingBar1);
            final RatingBar ratingBar2 = (RatingBar) findViewById(R.id.ratingBar2);
            final RatingBar ratingBar3 = (RatingBar) findViewById(R.id.ratingBar3);
            TextView name1 = (TextView)  findViewById(R.id.name1);
            TextView name2 = (TextView)  findViewById(R.id.name2);
            TextView name3 = (TextView)  findViewById(R.id.name3);
            name1.setText(groupUsers.get(0).getName());
            name2.setText(groupUsers.get(1).getName());
            name3.setText(groupUsers.get(2).getName());
            Button submitButton = (Button) findViewById(R.id.submit3);
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ratingBar1.getRating() == 0.0 || ratingBar2.getRating() == 0.0 || ratingBar3.getRating() == 0.0){
                        notRatedDialog();
                    }
                    else{
                        ServerUtils.rateUser(groupUsers.get(0), ratingBar1.getRating());
                        ServerUtils.rateUser(groupUsers.get(1), ratingBar2.getRating());
                        ServerUtils.rateUser(groupUsers.get(2), ratingBar3.getRating());
                        submitRatingDialog();
                    }
                }
            });

        }
        else if(groupSize == 3){
            setContentView(R.layout.activity_rating2);
            addDrawer();
            if(getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Please rate your group");
            }
            final RatingBar ratingBar1 = (RatingBar) findViewById(R.id.ratingBar1);
            final RatingBar ratingBar2 = (RatingBar) findViewById(R.id.ratingBar2);
            TextView name1 = (TextView)  findViewById(R.id.name1);
            TextView name2 = (TextView)  findViewById(R.id.name2);
            name1.setText(groupUsers.get(0).getName());
            name2.setText(groupUsers.get(1).getName());
            Button submitButton = (Button) findViewById(R.id.submit2);
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ratingBar1.getRating() == 0.0 || ratingBar2.getRating() == 0.0){
                        notRatedDialog();
                    }
                    else{
                        ServerUtils.rateUser(groupUsers.get(0), ratingBar1.getRating());
                        ServerUtils.rateUser(groupUsers.get(1), ratingBar2.getRating());
                        submitRatingDialog();
                    }
                }
            });
        }
        else if(groupSize == 2){
            setContentView(R.layout.activity_rating1);
            addDrawer();
            if(getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Please rate your group");
            }
            final RatingBar ratingBar1 = (RatingBar) findViewById(R.id.ratingBar1);
            TextView name1 = (TextView)  findViewById(R.id.name1);
            name1.setText(groupUsers.get(0).getName());
            Button submitButton = (Button) findViewById(R.id.submit1);
            if(submitButton == null){
                finish();
            }
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ratingBar1.getRating() == 0.0){
                        notRatedDialog();
                    }
                    else{
                        ServerUtils.rateUser(groupUsers.get(0), ratingBar1.getRating());
                        submitRatingDialog();
                    }
                }
            });

        }
        else{ //something went wrong
            Intent myIntent = new Intent(RatingActivity.this, PreferencesActivity.class);
            startActivity(myIntent);
        }
    }


    public  void notRatedDialog(){//show the dialog in case the user did not rate all of the group members yet
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RatingActivity.this);

        // set title
        alertDialogBuilder.setTitle("Could not submit");

        // set dialog message
        alertDialogBuilder
                .setMessage("Please rate all of your group members before submitting")
                .setPositiveButton("ok",new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public  void submitRatingDialog(){//show the dialog in case the rating was submitted
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RatingActivity.this);

        // set title
        alertDialogBuilder.setTitle("Thank you");

        // set dialog message

        alertDialogBuilder
                .setMessage("Thank you for rating your group!")
                .setPositiveButton("finish",new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        finish();
                    }
                });


        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

}
