package com.googleworkshop.taxipool;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.goto_login:
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                break;
        }
    }
}
