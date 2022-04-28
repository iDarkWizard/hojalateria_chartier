package com.idarkwizard.calculatorapp;

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
import androidx.core.app.ActivityCompat;

import com.idarkwizard.calculatorapp.service.LoadDataService;
import com.idarkwizard.calculatorapp.domain.DataStorage;
import com.idarkwizard.calculatorapp.service.PermissionService;
import com.idarkwizard.calculatorapp.service.UtilService;

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

public class DataNotLoadedActivity extends Activity {

    private static final int PERMISSION_ALL = 0;
    private Handler h;
    private Runnable r;

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

    @Override
    public FileInputStream openFileInput(String name) throws FileNotFoundException {
        return super.openFileInput(name);
    }

    class DownloadService extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            showDialog(PROGRESS_BAR_TYPE);
        }

        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
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
            startActivity(new Intent(context, HomeActivity.class));
            dismissDialog(PROGRESS_BAR_TYPE);
        }
    }
}
