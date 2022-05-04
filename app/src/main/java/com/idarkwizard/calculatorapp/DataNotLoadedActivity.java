package com.idarkwizard.calculatorapp;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.idarkwizard.calculatorapp.service.DownloadService;
import com.idarkwizard.calculatorapp.domain.DataStorage;
import com.idarkwizard.calculatorapp.service.PermissionService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataNotLoadedActivity extends Activity {

    private static final int PERMISSION_ALL = 0;
    private Handler h;
    private Runnable r;

    private static final String TAG = "SplashScreen";
    private Context context;

    private SharedPreferences sharedPref;

    public void onCreate(Bundle savedInstanceState) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        super.onCreate(savedInstanceState);
        context = this;

        setContentView(R.layout.load_data_layout);

        View ignoreBtn = findViewById(R.id.ignore_btn);
        View loadBtn = findViewById(R.id.load_btn);

        loadBtn.setOnClickListener(view -> startLoad(view));

        ignoreBtn.setOnClickListener(view -> startIgnore(view));
    }

    private void startLoad(View view) {
        h = new Handler();
        r = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "loadBtn.onClickListener: Starting new activity for result");
                startActivityForResult(new Intent(view.getContext(), LoadDataActivity.class), 0);
            }
        };
        loadPermissions();
    }

    private void startIgnore(View view) {
        h = new Handler();
        r = new Runnable() {
            @Override
            public void run() {
                startHome();
            }
        };
        loadPermissions();
    }

    private void loadPermissions() {
        String[] PERMISSIONS = {
                READ_EXTERNAL_STORAGE,
                WRITE_EXTERNAL_STORAGE
        };

        if(!PermissionService.hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        else
            h.postDelayed(r, 1500);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        int index = 0;
        Map<String, Integer> permissionsMap = new HashMap<String, Integer>();
        for(String permission : permissions) {
            permissionsMap.put(permission, grantResults[index]);
            index++;
        }

        if(permissionsMap.get(READ_EXTERNAL_STORAGE) != 0 || permissionsMap.get(WRITE_EXTERNAL_STORAGE) != 0){
            Toast.makeText(this, "Los permisos de de acceso a archivos son imprescindibles.", Toast.LENGTH_LONG).show();
            finish();
        } else {
            h.postDelayed(r, 1500);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null)
            return;
        startHome();
    }

    private void startHome() {
        DownloadService.unsaveCache(sharedPref);
        DownloadService ds = new DownloadService(context);
        ds.startDownload();
    }

}
