package com.idarkwizard.calculatorapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

public class SplashScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isDataLoadedDefaultValue = getResources().getBoolean(R.bool.loaded_data_key);
        boolean isDataLoaded = sharedPref.getBoolean("loaded_data_key", isDataLoadedDefaultValue);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Intent intent;
                if (isDataLoaded){
                    intent = new Intent(SplashScreen.this, MainActivity.class);
                } else {
                    intent = new Intent(SplashScreen.this, LoadDataActivity.class);
                }
                startActivityForResult(intent, 1);
                finish();
            }
        };

        Timer timer = new Timer();
        timer.schedule(timerTask, 2000);
    }
}