package com.sachithkn.stonegame;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);
        scheduleSplashScreen();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void scheduleSplashScreen() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 1000);
    }
}
