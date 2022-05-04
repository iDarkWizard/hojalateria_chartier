package com.idarkwizard.calculatorapp.service;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.idarkwizard.calculatorapp.HomeActivity;
import com.idarkwizard.calculatorapp.LoadDataActivity;
import com.idarkwizard.calculatorapp.domain.DataStorage;
import com.idarkwizard.calculatorapp.service.LoadDataService;
import com.idarkwizard.calculatorapp.service.PermissionService;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloadService extends Activity {

    public static final int PROGRESS_BAR_TYPE = 0;
    private static final String TAG = "DownloadService";
    private static final String DATA_URL = "https://github.com/iDarkWizard/hojalateria_chartier/blob/main/app/src" +
            "/main/res/raw/data.xlsx?raw=true";
    private List<DataStorage> dataStorageList;
    private Context context;
    private ProgressDialog pDialog;

    private SharedPreferences sharedPref;

    public DownloadService (Context context) {
        this.context = context;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this.context);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void startDownload() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        dataStorageList = new ArrayList<>();
        onCreateDialog(PROGRESS_BAR_TYPE);
        new Download().execute(DATA_URL);

    }

    public static void unsaveCache(SharedPreferences sharedPref) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear().apply();
        editor.putBoolean("loaded_data_key", false);
        editor.apply();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESS_BAR_TYPE: // we set this to 0
                pDialog = new ProgressDialog(context);
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

    @Override
    public FileInputStream openFileInput(String name) throws FileNotFoundException {
        return super.openFileInput(name);
    }

    class Download extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                int lengthOfFile = connection.getContentLength();
                InputStream input = new BufferedInputStream(url.openStream(),
                        lengthOfFile);
                OutputStream output = context.openFileOutput("data.xlsx", MODE_PRIVATE);

                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));
                    Thread.sleep(100);
                    output.write(data, 0, count);
                    Log.i(TAG, "doInBackground: Progress: " + (total * 100) / lengthOfFile + "%");
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

        protected void onPostExecute(String file_url) {
            super.onPostExecute(file_url);
            InputStream file = null;
            try {
                file = new FileInputStream("/data/user/0/com.idarkwizard.calculatorapp/files/data.xlsx");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            LoadDataService.readExcelData(file, dataStorageList);
            LoadDataActivity.finishLoad(dataStorageList, sharedPref, context);
            context.startActivity(new Intent(context, HomeActivity.class));
            finish();
        }
    }
}
