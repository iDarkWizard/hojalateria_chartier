package com.idarkwizard.calculatorapp.config;

import android.content.Intent;
import android.util.Log;

import com.idarkwizard.calculatorapp.domain.DataStorage;
import com.idarkwizard.calculatorapp.service.UtilService;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LoadData {

    private static String TAG = "LoadData";

    public static void readExcelData(InputStream inputStream, List<DataStorage> dataStorageList) {
        Log.d(TAG, "readExcelData: ReadingExcelFile.");
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            readSheets(workbook, dataStorageList);
            Log.d(TAG, "readExcelData: Excel file readed.");
        } catch (FileNotFoundException e){
            Log.d(TAG, "readExcelData: FileNotFoundException." + e.getMessage());
        }catch (IOException e) {
            Log.d(TAG, "readExcelData: procrastinator IOException." + e.getMessage());
        }
    }

    private static void readSheets(XSSFWorkbook workbook, List<DataStorage> dataStorageList) {
        Log.d(TAG, "readSheets: Start reading sheets.");
        FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
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

    public static Intent finishLoad(List<DataStorage> dataStorageList) {
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
            intent.putExtra(actualSheetTitles.toLowerCase(Locale.ROOT).replaceAll(" ", "_"), UtilService.parseArrayToString(rowsNames));
            addRowsToIntent(rows, sheetName, rowsNames, intent);
            Log.d(TAG, "finishedLoad: Sheet loaded: " + sheetName);
        }
        intent.putExtra("sheets_names", UtilService.parseArrayToString(sheetNames));
        Log.d(TAG, "finishedLoad: All sheets loaded");
        return intent;
    }

    private static void addRowsToIntent(String[] rows, String sheetName, String[] columnNames, Intent intent) {
        List<String[]> columnList = new ArrayList<>();
        for (int i = 1; i < rows.length; i++) {
            String[] valuesInRow = UtilService.splitAsArray(rows[i]);
            for (int j = 0; j < columnNames.length; j++) {
                String[] column = new String[rows.length - 1];
                if(columnList != null && !columnList.isEmpty() && j <= columnList.size() - 1) {
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
            intent.putExtra(extraName.toLowerCase(Locale.ROOT).replaceAll(" ", "_"), UtilService.parseArrayToString(values));
        }
    }

    private static String[] addRows(List<List<String>> rowList) {
        String[] rows = new String[rowList.size()];
        for (int i = 0; i < rowList.size(); i++) {
            rows[i] = UtilService.parseListToString(rowList.get(i));
        }
        return rows;
    }
}
