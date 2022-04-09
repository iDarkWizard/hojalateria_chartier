package com.idarkwizard.calculatorapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.idarkwizard.calculatorapp.config.LoadData;
import com.idarkwizard.calculatorapp.config.LoadDataV2;
import com.idarkwizard.calculatorapp.domain.DataStorage;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LoadDataActivity extends AppCompatActivity {

    private static final String TAG = "SplashScreen";

    private SharedPreferences sharedPref;

    public void onCreate(Bundle savedInstanceState) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load_data_layout);
        getSupportActionBar().hide();

        View ignoreBtn = findViewById(R.id.ignore_btn);
        View loadBtn = findViewById(R.id.load_btn);

        loadBtn.setOnClickListener(view -> {
            Log.d(TAG, "loadBtn.onClickListener: Starting new activity for result");
            startActivityForResult(new Intent(view.getContext(), LoadDataV2.class), 0);
        });

        ignoreBtn.setOnClickListener(view -> {
            ignoreBtn.setElevation(20);
            startHome(this);
            finish();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data == null)
            return;
        saveCache(sharedPref, data);
        startHome(this);
    }


    private void saveCache(SharedPreferences sharedPref, Intent data){
        int extrasCount = data.getExtras().size();
        SharedPreferences.Editor editor = sharedPref.edit();
        List<String> keys = new ArrayList<>(data.getExtras().keySet());
        for(int i = 0; i < extrasCount; i++) {
            editor.putString(keys.get(i), data.getStringExtra(keys.get(i)));
        }
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        editor.putBoolean("loaded_data_key", true);
        editor.putString("last_loaded", dateFormat.format(calendar.getTime()));
        editor.apply();
    }

    private void unsaveCache(SharedPreferences sharedPref){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("loaded_data_key", false);
        editor.apply();
    }
    @SuppressLint("NewApi")
    private void startHome(Context context) {

        List<DataStorage> dataStorageList = new ArrayList<>();
        URL resource = this.getClass().getClassLoader().getResource("target/classes/PreciosV2.xlsx");
        try {
            LoadData.readExcelData(Paths.get(resource.toURI()).toFile(), dataStorageList);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        LoadData.finishLoad(dataStorageList);
        startActivity(new Intent(context, MainActivity.class));
        finish();
    }
}
