package com.idarkwizard.calculatorapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.idarkwizard.calculatorapp.config.LoadData;
import com.idarkwizard.calculatorapp.config.LoadDataV2;
import com.idarkwizard.calculatorapp.domain.DataStorage;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LoadDataActivity extends AppCompatActivity {

    public static final int PROGRESS_BAR_TYPE = 0;
    private static final String TAG = "SplashScreen";
    private static final String DATA_URL = "https://github.com/iDarkWizard/hojalateria_chartier/blob/main/app/src" +
            "/main/res/raw/data.xlsx?raw=true";
    private List<DataStorage> dataStorageList;
    private Context context;
    private ProgressDialog pDialog;

    private SharedPreferences sharedPref;

    public void onCreate(Bundle savedInstanceState) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        super.onCreate(savedInstanceState);
        context = this;
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
            startHome();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null)
            return;
        saveCache(sharedPref, data);
        startHome();
    }


    private void saveCache(SharedPreferences sharedPref, Intent data) {
        int extrasCount = data.getExtras()
                .size();
        SharedPreferences.Editor editor = sharedPref.edit();
        List<String> keys = new ArrayList<>(data.getExtras()
                .keySet());
        for (int i = 0; i < extrasCount; i++) {
            editor.putString(keys.get(i), data.getStringExtra(keys.get(i)));
        }
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        editor.putBoolean("loaded_data_key", true);
        editor.putString("last_loaded", dateFormat.format(calendar.getTime()));
        editor.apply();
    }

    private void unsaveCache(SharedPreferences sharedPref) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("loaded_data_key", false);
        editor.apply();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESS_BAR_TYPE: // we set this to 0
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading file. Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(true);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }

    @SuppressLint("NewApi")
    private void startHome() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        dataStorageList = new ArrayList<>();
        new DownloadService().execute(DATA_URL);
    }

    class DownloadService extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(PROGRESS_BAR_TYPE);
        }

        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                checkFilePermissions();
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                int lengthOfFile = connection.getContentLength();
                InputStream input = new BufferedInputStream(url.openStream(),
                        9642);
                OutputStream output = openFileOutput("data.xlsx", MODE_PRIVATE);

                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        protected void onProgressUpdate(String... progress) {
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String file_url) {
            super.onPostExecute(file_url);
            InputStream file = null;
            try {
                file = new FileInputStream("/data/user/0/com.idarkwizard.calculatorapp/files/data.xlsx");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            LoadData.readExcelData(file, dataStorageList);
            Intent loaded = LoadData.finishLoad(dataStorageList);
            saveCache(sharedPref, loaded);
            dismissDialog(PROGRESS_BAR_TYPE);
            startActivity(new Intent(context, MainActivity.class));
            finish();
        }
    }

    @SuppressLint("NewApi")
    private void checkFilePermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int readCheck = this.checkSelfPermission("Manifest.permission.READ_EXTERNAL_STORAGE");
            int writeCheck = this.checkSelfPermission("Manifest.permission.WRITE_EXTERNAL_STORAGE");
            if (readCheck != 0) {
                this.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1001);
            } else{
                Log.d(TAG, "checkBTPermissions:  No need to check permissions. SDK version < LLOLIPOP.");
            }
            if (writeCheck != 0) {
                this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001);
            } else{
                Log.d(TAG, "checkBTPermissions:  No need to check permissions. SDK version < LLOLIPOP.");
            }
        }
    }

}
