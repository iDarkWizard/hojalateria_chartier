package com.idarkwizard.calculatorapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    public void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        boolean isDataLoadedDefaultValue = getResources().getBoolean(R.bool.loaded_data_key);
        boolean isDataLoaded = sharedPref.getBoolean("loaded_data_key", isDataLoadedDefaultValue);
        if(isDataLoaded){
            saveCache(sharedPref);
            startHome(getBaseContext());
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        getSupportActionBar().hide();

        View ignoreBtn = findViewById(R.id.ignore_btn);
        View loadBtn = findViewById(R.id.load_btn);

        loadBtn.setOnClickListener(view -> {
            saveCache(sharedPref);
            startHome(getApplicationContext());
            finish();
        });

        ignoreBtn.setOnClickListener(view -> {
            startHome(view.getContext());
            finish();
        });
    }

    private void saveCache(SharedPreferences sharedPref){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("loaded_data_key", true);
        editor.apply();
    }

    private void unsaveCache(SharedPreferences sharedPref){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("loaded_data_key", false);
        editor.apply();
    }

    private void startHome(Context context) {
        startActivity(new Intent(context, MainActivity.class));
        finish();
    }
}
