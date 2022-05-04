package com.idarkwizard.calculatorapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.idarkwizard.calculatorapp.config.CustomArrayAdapter;
import com.idarkwizard.calculatorapp.domain.DataStorage;
import com.idarkwizard.calculatorapp.service.DownloadService;
import com.idarkwizard.calculatorapp.service.UtilService;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LoadDataActivity extends AppCompatActivity {

    private static final String TAG = "LoadData";
    File file;
    View directoryBackBtn;
    ArrayList<String> pathHistory;
    TextView actualDirectory;
    String lastDirectory;
    int count = 0;
    ListView lvInternalStorage;
    private List<DataStorage> dataStorageList;
    private List<String> pathList, fileNameList;
    private File[] listFile;

    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: new Activity created");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load_data_list_layout);
        getSupportActionBar().hide();
        lvInternalStorage = findViewById(R.id.files_list);
        directoryBackBtn = findViewById(R.id.files_list_back_btn);
        actualDirectory = findViewById(R.id.actual_directory);
        dataStorageList = new ArrayList<>();
        Log.d(TAG, "onCreate: ready to import data");
        checkFilePermissions();
        showStorage();

        directoryBackBtn.setOnClickListener(view -> {
            Log.d(TAG, "directoryBackBtn.onClickListener: Going back");
            if (count == 0) {
                Log.d(TAG, "directoryBackBtn: You have reached the root directory.");
                finish();
                return;
            }
            pathHistory.remove(count);
            count--;
            checkInternalStorage();
            Log.d(TAG, "directoryBackBtn: " + pathHistory.get(count));
        });

        lvInternalStorage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                lastDirectory = pathHistory.get(count);
                if (adapterView.getItemAtPosition(i)
                        .toString()
                        .endsWith(".xlsx")) {
                    Log.d(TAG, "lvInternalStorage: Selected a file for upload: " + lastDirectory);
                    readExcelData(lastDirectory + "/" + adapterView.getItemAtPosition(i)
                            .toString());
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(view.getContext());
                    DownloadService.unsaveCache(sharedPreferences);
                    finishLoad(dataStorageList, sharedPreferences, view.getContext());
                    startActivity(new Intent(view.getContext(), HomeActivity.class));
                } else {
                    String newPath = pathHistory.get(count) + (String) adapterView.getItemAtPosition(i);
                    count++;
                    pathHistory.add(count, newPath);
                    checkInternalStorage();
                    Log.d(TAG, "lvInternalStorage: " + pathHistory.get(count));
                }
            }
        });
    }

    private void showStorage() {
        Log.d(TAG, "startLoad: Loading storage");
        count = 0;
        pathHistory = new ArrayList<>();
        pathHistory.add(count, System.getenv("EXTERNAL_STORAGE"));
        Log.d(TAG, "storageBtn: " + pathHistory.get(count));
        checkInternalStorage();
    }

    public static void finishLoad(List<DataStorage> dataStorageList, SharedPreferences sharedPreferences,
                               Context context) {
        Log.d(TAG, "finishedLoad: Finalizing load. Preparing return.");
        Intent intent = new Intent();
        String[] sheetNames = new String[dataStorageList.size()];
        Log.d(TAG, "finishedLoad: Loading : " + dataStorageList.size() + "sheets.");
        for (int i = 0; i < dataStorageList.size(); i++) {
            DataStorage dataStorage = dataStorageList.get(i);
            String sheetName = dataStorage.getSheetName();
            Log.d(TAG, "finishedLoad: Loading sheet: " + sheetName);
            String[] rows = addRows(dataStorage.getRowList());
            String[] rowsNames = UtilService.splitAsArray(rows[0]);
            sheetNames[i] = sheetName;
            String actualSheetTitles = sheetName + "_names";
            // Ex: chapas_titles: ['nombre', 'corte', 'plegado']
            intent.putExtra(actualSheetTitles.toLowerCase(Locale.ROOT)
                    .replaceAll(" ", "_"), UtilService.parseArrayToString(rowsNames));
            addRowsToIntent(rows, sheetName, rowsNames, intent);
            Log.d(TAG, "finishedLoad: Sheet loaded: " + sheetName);
        }
        intent.putExtra("sheets_names", UtilService.parseArrayToString(sheetNames));
        Log.d(TAG, "finishedLoad: All sheets loaded");
        LoadDataActivity.saveCache(sharedPreferences, intent);
    }

    private static void addRowsToIntent(String[] rows, String sheetName, String[] columnNames, Intent intent) {
        List<String[]> columnList = new ArrayList<>();
        for (int i = 1; i < rows.length; i++) {
            String[] valuesInRow = UtilService.splitAsArray(rows[i]);
            for (int j = 0; j < columnNames.length; j++) {
                String[] column = new String[rows.length - 1];
                if (columnList != null && !columnList.isEmpty() && j <= columnList.size() - 1) {
                    column = columnList.get(j);
                    column[i - 1] = valuesInRow[j];
                } else {
                    column[i - 1] = valuesInRow[j];
                    columnList.add(j, column);
                }
            }
        }
        for (int i = 0; i < columnNames.length; i++) {
            String[] values = columnList.get(i);
            String extraName = sheetName + "_" + columnNames[i];
            // Ex: chapas_nombre: 'N22;N20;N18;N16'
            intent.putExtra(extraName.toLowerCase(Locale.ROOT)
                    .replaceAll(" ", "_"), UtilService.parseArrayToString(values));
        }
    }

    private static String[] addRows(List<List<String>> rowList) {
        String[] rows = new String[rowList.size()];
        for (int i = 0; i < rowList.size(); i++) {
            rows[i] = UtilService.parseListToString(rowList.get(i));
        }
        return rows;
    }

    public void readExcelData(String filePath) {
        Log.d(TAG, "readExcelData: ReadingExcelFile.");
        File inputFile = new File(filePath);
        try {
            InputStream inputStream = new FileInputStream(inputFile);
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            readSheets(workbook);
            Log.d(TAG, "readExcelData: Excel file readed.");
        } catch (FileNotFoundException e) {
            Log.d(TAG, "readExcelData: FileNotFoundException." + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "readExcelData: procrastinator IOException." + e.getMessage());
        }
    }

    private void readSheets(XSSFWorkbook workbook) {
        Log.d(TAG, "readSheets: Start reading sheets.");
        FormulaEvaluator formulaEvaluator = workbook.getCreationHelper()
                .createFormulaEvaluator();
        int sheetsCount = workbook.getNumberOfSheets();
        for (int i = 0; i < sheetsCount; i++) {
            DataStorage dataStorage = new DataStorage();
            XSSFSheet actualSheet = workbook.getSheetAt(i);
            Log.d(TAG, "readSheets: Sheet: " + i + " named: " + actualSheet.getSheetName());
            dataStorage.readSheet(actualSheet, formulaEvaluator);
            dataStorageList.add(dataStorage);
            Log.d(TAG, "readSheets: Sheet finished and saved: " + actualSheet.getSheetName());
        }
        Log.d(TAG, "readSheets: All sheets read");
    }

    private void checkInternalStorage() {
        Log.d(TAG, "checkInternalStorage: Started.");
        try {
            if (!Environment.getExternalStorageState()
                    .equals(Environment.MEDIA_MOUNTED)) {
                toastMessage("No SD card found.");
            } else {
                file = new File(pathHistory.get(count));
                Log.d(TAG, "checkInternalStorage: directory path: " + pathHistory.get(count));
            }
            if (file.getName()
                    .equals("sdcard"))
                actualDirectory.setText("/internal");
            else
                actualDirectory.setText("/" + file.getName());
            listFile = file.listFiles();

            fileNameList = new ArrayList<>();
            pathList = new ArrayList<>();

            Map<String, String> pathMap = new HashMap<>();
            Map<String, String> filesMap = new HashMap<>();

            for (int i = 0; i < listFile.length; i++) {
                if (listFile[i].isDirectory()) {
                    pathMap.put(listFile[i].getName(), listFile[i].getAbsolutePath());
                    pathList.add("/" + listFile[i].getName());
                } else if (listFile[i].getName()
                        .endsWith(".xlsx")) {
                    filesMap.put(listFile[i].getName(), listFile[i].getAbsolutePath());
                    fileNameList.add(listFile[i].getName());
                }
            }

            Collections.sort(pathList);
            Collections.sort(fileNameList);
            pathList.addAll(fileNameList);
            pathMap.putAll(filesMap);


            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.single_row, R.id.text_view, pathList);
            CustomArrayAdapter customArrayAdapter = new CustomArrayAdapter(this, pathList);
            lvInternalStorage.setAdapter(customArrayAdapter);
            TextView ssd = findViewById(R.id.text_view);
            ssd.setSelected(true);
        } catch (NullPointerException e) {
            Log.e(TAG, "checkInternalStorage: NullPointerException" + e.getMessage());
        }
    }

    @SuppressLint("NewApi")
    private void checkFilePermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionChecl = this.checkSelfPermission("Manifest.permission.READ_EXTERNAL_STORAGE");
            if (permissionChecl != 0) {
                this.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1001);
            } else {
                Log.d(TAG, "checkBTPermissions:  No need to check permissions. SDK version < LLOLIPOP.");
            }
        }
    }

    public static void saveCache(SharedPreferences sharedPref, Intent data) {
        int extrasCount = data.getExtras()
                .size();
        SharedPreferences.Editor editor = sharedPref.edit();
        List<String> keys = new ArrayList<>(data.getExtras()
                .keySet());
        for (int i = 0; i < extrasCount; i++) {
            editor.putString(keys.get(i), data.getStringExtra(keys.get(i)));
        }
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy'T'HH:mm");
        editor.putBoolean("loaded_data_key", true);
        editor.putString("last_loaded", dateFormat.format(calendar.getTime()));
        editor.apply();
    }

    /**
     * Customizable toast
     *
     * @param message
     */
    private void toastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT)
                .show();
    }
}
