package com.idarkwizard.calculatorapp.service;

import java.util.Arrays;
import java.util.List;

public class UtilService {

    public static final String STRING_TOKEN = "=AYUDA=";

    public static String parseArrayToString(String[] columnValues) {
        return parseListToString(Arrays.asList(columnValues));
    }

    public static String parseListToString(List<String> columnValues) {
        StringBuilder sb = new StringBuilder();
        for(String cell : columnValues) {
            sb.append(cell).append(STRING_TOKEN);
        }
        String parsedRow = sb.toString();
        if(parsedRow.endsWith(STRING_TOKEN)) {
            int lastTokenIndex = STRING_TOKEN.length();
            parsedRow = parsedRow.substring(0, parsedRow.length() - lastTokenIndex);
        }
        return parsedRow;
    }

    public static String[] splitAsArray(String value) {
        return value.split(STRING_TOKEN);
    }

    public static List<String> splitAsList(String value) {
        return Arrays.asList(value.split(STRING_TOKEN));
    }

}
