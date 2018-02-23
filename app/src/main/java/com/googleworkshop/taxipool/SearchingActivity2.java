package com.googleworkshop.taxipool;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class SearchingActivity2 extends NavDrawerActivity {
    protected static MyReceiver receiver;
    protected static final String FORMAT = "%02d:%02d";
    protected static final String FORMAT1 = "%2d:%02d:%02d";
    //protected static int pos = PreferencesActivity.timeSpinner.getSelectedItemPosition();
    protected long numOfSeconds;
    private DatabaseReference database;
    private String requestId, groupId;
    Intent nextIntent;//TODO why is this declared here?
    private boolean isActive;
    private SharedPreferences.Editor editor;
    private CountDownTimer countDownTimer;
    protected Intent i;
    private boolean isInFront;
    protected SharedPreferences lastRequestSharedPref;
    //protected SharedPreferences.Editor lastRequestPrefEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean startedFromNotificationAfterDestroy = false;//TODO Change name
        requestId=getIntent().getStringExtra("requestId");
        if(requestId == null){//activity was started from notification after being destroyed
            startedFromNotificationAfterDestroy = true;
            Long timeLeft = getTimeLeftForRequest();
            if(timeLeft < 0){
                gotoPreferences();
            }
            numOfSeconds = timeLeft;
        }
        database = FirebaseDatabase.getInstance().getReference();

        setContentView(R.layout.searching_screen_layout);
        addDrawer();
        getSupportActionBar().setTitle("Looking for a match");

        isInFront = true;

        final TextView timer = (TextView)findViewById(R.id.timer);
        TextView origin = (TextView)findViewById(R.id.real_origin);
        TextView destination = (TextView)findViewById(R.id.real_dest);
        Button findNewTrip = (Button)findViewById(R.id.find_new_trip);
        findNewTrip.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            gotoPreferences();
        }
    });
    String originName=getIntent().getStringExtra("origin");
        if(originName!=null)
            origin.setText(originName);
        else
                origin.setText("Your location");
        destination.setText(getIntent().getStringExtra("destination"));

        if(!startedFromNotificationAfterDestroy) {
            numOfSeconds = getIntent().getLongExtra("numOfSeconds", 999);
        }
        countDownTimer=  new CountDownTimer(numOfSeconds*1000, 1000) {
            public void onTick(long millisUntilFinished) {
                if(millisUntilFinished >= 60*60*1000) {//over an hour left
                    timer.setText(String.format(FORMAT1,
                            TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                    TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));

                }
                else {
                    timer.setText(String.format(FORMAT,
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                    TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
                }
            }

            public void onFinish() {
                //if(groupId !=null &&!isActive) {//This did not work, do we even need to check anything here?
                if(groupId==null ||!isActive) {
                    //TODO I don't understand why we need isActive
                    //timer.setText("Sorry, We could not find a match");
                    Intent intent = new Intent(SearchingActivity2.this, PreferencesActivity.class);
                    if(groupId !=null) {
                        database.child("groups").child(groupId).child("closed").setValue(true);//Why are we doing this?
                    }
                    NotificationManager mNotificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    try {
                        mNotificationManager.cancelAll();//delete all previously sent notifications
                    }catch (NullPointerException e){
                        Log.i("null pointer", "NullPointerException in cancelAll()");
                    }
                    if(!isInFront) {
                        NotificationUtils.sendNotification("Sorry, We could not find a match",
                                "you are welcome to try again soon", intent, getApplicationContext());
                        finish();
                    }
                    else{
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(SearchingActivity2.this);
                        builder1.setMessage("Sorry, we could not find you a match. Would you like to try again?"); //TODO word it better..
                        builder1.setCancelable(false);

                        builder1.setPositiveButton(
                                "Yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        gotoPreferences();
                                    }
                                });

                        builder1.setNegativeButton(
                                "No",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        finish();
                                    }
                                });

                        AlertDialog alert11 = builder1.create();
                        alert11.show();

                        //startActivity(intent);
                        //finish();
                    }
                }
            }
        };
        countDownTimer.start();
        SharedPreferences sharedPref = this.getSharedPreferences("requestId", Context.MODE_PRIVATE);
        nextIntent=new Intent(this,MatchScreenActivity.class);
        //TODO should I check if null?

        if(requestId!=null){
           editor = sharedPref.edit();
            editor.putString("requestId",requestId);
            if(getIntent().getStringExtra("origin") != null){
                editor.putString("origin", getIntent().getStringExtra("origin"));
                editor.putString("destination", getIntent().getStringExtra("destination"));
            }
            editor.commit();
        }

        setupServiceReceiver();
        if(!startedFromNotificationAfterDestroy) {
            startSearchingService();
        }
    }


    @Override
    public void gotoPreferences(){
        /*
        long serviceStartTime = getIntent().getLongExtra("serviceStartTime", 0);
        if(serviceStartTime == 0) {
            countDownTimer.cancel();
        }*/
        countDownTimer.cancel();
        String groupId=SearchingService.groupId;
        if(groupId !=null &&!isActive) {
            database.child("groups").child(groupId).child("closed").setValue(true);
        }
        editor.putString("requestId",null);
        editor.commit();
        Intent intent;
        intent = new Intent(this, PreferencesActivity.class);
        stopService(new Intent(SearchingActivity2.this, SearchingService.class));
        startActivity(intent);
        finish();
    }

    public void startSearchingService(){
        i = new Intent(this, SearchingService.class);
        Bundle bundle = new Bundle();
        bundle.putLong("numOfSeconds", numOfSeconds);
        bundle.putString("requestId", requestId);
        bundle.putParcelable("receiver", receiver);
        bundle.putLong("serviceStartTime", System.currentTimeMillis());//Am I using this?
        bundle.putString("destination", getIntent().getStringExtra("destination"));
        bundle.putString("origin", getIntent().getStringExtra("origin"));
        i.putExtra("bundle", bundle);
        startService(i);
    }

    // Setup the callback for when data is received from the service
    public void setupServiceReceiver() {
        receiver = new MyReceiver(new Handler());
        // onReceiveResult is called when data is received from the service
        receiver.setReceiver(new MyReceiver.Receiver() {
            @Override
            public void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == 1) {//group found
                    String groupId = resultData.getString("groupId");
                    isActive = resultData.getBoolean("isActive");
                    Request currentRequest = resultData.getParcelable("currentRequest");
                    ClientUtils.saveRequest(currentRequest, getApplicationContext());
                    countDownTimer.cancel();
                    if(isInFront) {
                        startActivity(nextIntent);
                    }
                    finish();
                }
                else{
                    //?
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        //Toast.makeText(this, "Searching activity resumed", Toast.LENGTH_LONG).show();
        isInFront = true;
    }

    public void onDestroy() {
        super.onDestroy();
        //Toast.makeText(this, "Searching activity destroyed", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onPause() {
        super.onPause();
        isInFront = false;
    }

    protected long getTimeLeftForRequest(){//TODO Duplicate of method in LoginActivity. find a way to use the same one twice
        lastRequestSharedPref = getSharedPreferences("lastRequest", 0);

        if(!lastRequestSharedPref.contains("lastRequestTimeStamp") || !lastRequestSharedPref.contains("lastRequestDuration")){
            //no valid last request exists
            return -1;
        }
        long timeStamp = lastRequestSharedPref.getLong("lastRequestTimeStamp", 0);
        long duration = lastRequestSharedPref.getLong("lastRequestDuration", 0);
        long currentTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

        if(currentTime > timeStamp + duration){//last request has expired
            return -1;
        }
        return duration - (currentTime - timeStamp);
    }


}
