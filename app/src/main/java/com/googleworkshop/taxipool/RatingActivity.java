package com.googleworkshop.taxipool;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class RatingActivity extends NavDrawerActivity {
    private ArrayList<User> groupUsers;
    private int groupSize;

    private String groupId;
    private DatabaseReference database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        groupSize = getIntent().getIntExtra("groupSize", 0);
        groupUsers = (ArrayList<User>) getIntent().getSerializableExtra("groupUsers");
        if(groupSize == 4){
            setContentView(R.layout.activity_rating3);
            addDrawer();
            toolbar.setTitle("Please rate your group");
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
                    ServerUtils.rateUser(groupUsers.get(0), ratingBar1.getRating());
                    ServerUtils.rateUser(groupUsers.get(1), ratingBar2.getRating());
                    ServerUtils.rateUser(groupUsers.get(2), ratingBar3.getRating());
                    Intent myIntent = new Intent(RatingActivity.this, ThankYouActivity.class);
                    startActivity(myIntent);
                }
            });

        }
        else if(groupSize == 3){
            setContentView(R.layout.activity_rating2);
            addDrawer();
            toolbar.setTitle("Please rate your group");
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
                    ServerUtils.rateUser(groupUsers.get(0), ratingBar1.getRating());
                    ServerUtils.rateUser(groupUsers.get(1), ratingBar2.getRating());
                    Intent myIntent = new Intent(RatingActivity.this, ThankYouActivity.class);
                    startActivity(myIntent);
                }
            });
        }
        else if(groupSize == 2){
            setContentView(R.layout.activity_rating1);
            addDrawer();
            toolbar.setTitle("Please rate your group");
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
                    double oldRating = groupUsers.get(0).getRating();
                    int numOfStars = ratingBar1.getNumStars();
                    ServerUtils.rateUser(groupUsers.get(0), ratingBar1.getRating());
                    double newRating = groupUsers.get(0).getRating();
                    Intent myIntent = new Intent(RatingActivity.this, ThankYouActivity.class);
                    startActivity(myIntent);
                }
            });

            //submitButton.setOnClickListener(onClick1);
        }
        else{
            //TODO: change, this is for debugging
            Intent myIntent = new Intent(RatingActivity.this, ThankYouActivity.class);
            startActivity(myIntent);
        }

        database = FirebaseDatabase.getInstance().getReference();
        groupId=getIntent().getStringExtra("groupId");
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        //Fragment fragment = null;
        Intent intent;
        switch(menuItem.getItemId()) {
            case R.id.nav_my_profile:
                intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_sign_out:
                //TODO:add sign_out
                break;
            case R.id.nav_preferences:
                intent = new Intent(this, PreferencesActivity.class);
                User user = null;//TODO
                intent.putExtra("User", user);
                startActivity(intent);
                break;
            default:
                //?
        }

        // Highlight the selected item has been done by NavigationView
        //menuItem.setChecked(true);
        // Set action bar title
        //setTitle(menuItem.getTitle());
        // Close the navigation drawer
        mDrawer.closeDrawers();
    }
    //------


}
