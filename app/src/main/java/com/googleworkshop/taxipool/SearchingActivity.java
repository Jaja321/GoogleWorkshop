package com.googleworkshop.taxipool;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class SearchingActivity extends AppCompatActivity {
    protected static final String FORMAT = "%02d:%02d";
    //protected static int pos = PreferencesActivity.timeSpinner.getSelectedItemPosition();
    //protected int numOfSeconds = getIntent().getIntExtra("numOfSeconds", 0);//for now, should come from
    protected int numOfSeconds = 1000;// TODO: change
    private DatabaseReference database;
    private String requestId;
    Intent nextIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.searching_screen_layout);

        ProgressBar progressBar = (ProgressBar)findViewById(R.id.searching_animation);
        final TextView timer = (TextView)findViewById(R.id.timer);

        new CountDownTimer(numOfSeconds*1000, 1000) {

            public void onTick(long millisUntilFinished) {
                //check for a match?
                timer.setText(String.format(FORMAT,
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
            }

            public void onFinish() {
                timer.setText("done!");//for now
            }
        }.start();
        nextIntent=new Intent(this,MatchScreenActivity.class);
        requestId=getIntent().getStringExtra("requestId");
        waitForGroup();
    }

    private void waitForGroup(){
        database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference groupIdRef=database.child("requests").child(requestId).child("groupId");
        groupIdRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String groupId=dataSnapshot.getValue(String.class);
                if(groupId!=null){
                    //Found a group:
                    nextIntent.putExtra("groupId",groupId);
                    startActivity(nextIntent);
                    finish();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
