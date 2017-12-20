package com.googleworkshop.taxipool;

import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class SearchingActivity extends AppCompatActivity {
    protected static final String FORMAT = "%02d:%02d";
    protected static int pos = PreferencesActivity.timeSpinner.getSelectedItemPosition();
    protected static int numOfSeconds = getNumOfSeconds(pos);

    //protected static Boolean matchFound = true;//for now

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

    }

    public static int getNumOfSeconds(int pos){
        if(pos == 0) {//"Leave in" selected
            return 0;
        }
        if(pos <= 3 && pos >= 1){//"15 min", "30 min" or "45 min" selected
            return pos * 15 * 60;
        }
        return (pos - 3) * 60 * 60;//"1 hour", "2 hours" or "3 hours" selected
    }
}
