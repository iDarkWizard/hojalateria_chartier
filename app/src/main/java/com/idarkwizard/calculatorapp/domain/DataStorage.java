package com.idarkwizard.calculatorapp.domain;

import android.util.Log;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class DataStorage {

    private static final String TAG = "DataStorage";

    private List<List<String>> rowList;
    private String sheetName;

    public DataStorage() {
        this.rowList = new ArrayList<>();
    }

    public List<List<String>> getRowList() {
        return rowList;
    }

    public String getSheetName() {
        return sheetName;
    }

    public List<List<String>> readSheet(XSSFSheet sheet, FormulaEvaluator formulaEvaluator) {
        int rowsCount = sheet.getPhysicalNumberOfRows();
        Log.d(TAG, "readSheet: Reading sheet: " + sheet.getSheetName() + " total rows: " + rowsCount);
        readSheetData(rowsCount, sheet, formulaEvaluator);
        Log.d(TAG, "readSheet: Sheet read " + sheet.getSheetName());
        return rowList;
    }

    private void readSheetData(int rowsCount, XSSFSheet sheet, FormulaEvaluator formulaEvaluator) {
        sheetName = sheet.getSheetName();
        Log.d(TAG, "readSheetData: Reading sheet: " + sheetName);
        for (int i = 0; i < rowsCount; i++){
            Log.d(TAG, "readSheetData: Reading row: " + i);
            List<String> columnList = new ArrayList<>();
            Row row = sheet.getRow(i);
            int rowCellsCount = row.getPhysicalNumberOfCells();
            Log.d(TAG, "readSheetData: Reading row: " + i + " total columns: " + rowCellsCount);
            for (int j = 0; j < rowCellsCount; j++) {
                Log.d(TAG, "readSheetData: Reading column: " + j + " for row: " + i);
                columnList.add(getCellAsString(row, j, formulaEvaluator));
            }
            rowList.add(columnList);
        }
        Log.d(TAG, "readSheetData: Read finished for sheet: " + sheetName);
    }

    private String getCellAsString(Row row, int c, FormulaEvaluator formulaEvaluator) {
        String value = "";
        try {
            Cell cell = row.getCell(c);
            CellValue cellValue = formulaEvaluator.evaluate(cell);
            switch (cellValue.getCellType()) {
                case Cell.CELL_TYPE_BOOLEAN:
                    value = "" + cellValue.getBooleanValue();
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    double numericValue = cell.getNumericCellValue();
                    if(HSSFDateUtil.isCellDateFormatted(cell)){
                        double date = cellValue.getNumberValue();
                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
                        value = formatter.format(HSSFDateUtil.getJavaDate(date));
                    }
                    value = "" + numericValue;
                    break;
                case Cell.CELL_TYPE_STRING:
                    value = cellValue.getStringValue();
                    break;
                default:
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "getCellAsString: NullPointerException: " + e.getMessage());
        }
        return  value;
    }
}
