package com.idarkwizard.calculatorapp.service;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalculationService {

    private static final int PLATES_CUT_INDEX = 0;
    private static final int PLATES_FULL_INDEX = 1;
    private static final int PLATES_FOLD_INDEX = 2;
    private static final int PLATES_WEIGHT_INDEX = 3;
    private static final int STEEL_SHINY_INDEX = 0;
    private static final int STEEL_OPAQUE_INDEX = 1;
    private static final String[] PLATES = {"Recorte: %.2f $",
                                            "Entera: %.2f $",
                                            "Plegada: %.2f $",
                                            "Diferencia: %.2f $",
                                            "Corte láser: %.2f $",
                                            "Plegado láser: %.2f $",
                                            "Corte plasma: %.2f $",
                                            "Plgado plasma: %.2f $"};
    private static final String[] STEELS = {"Precio B: %.2f $",
                                            "Precio 0: %.2f $"};
    private static final String TRAY = "Bandeja: %.2f $";
    private static final String TABLE = "Forrar mesa: %.2f $";

    public static List<String> calculate(
            Context context, Double width, Double length, Integer quantity, Integer selected, String thisFragment,
            Map<String, String> data, List<String> dataKeys) {
        return showResults(width, length, quantity, selected, data, dataKeys, thisFragment);
    }


    private static List<String> showResults(Double width, Double length, Integer quantity, Integer selected,
                                            Map<String, String> data,
                                            List<String> dataKeys, String thisFragment) {
        List<Double> results = new ArrayList<>();
        List<Double> itemsToCalculate = new ArrayList<>();
        for (int i = 1; i < dataKeys.size(); i++) {
            String key = dataKeys.get(i);
            thisFragment = thisFragment.toLowerCase(Locale.ROOT)
                    .replaceAll(" ", "_");
            key = thisFragment + "_" + key.toLowerCase(Locale.ROOT)
                    .replaceAll(" ", "_");
            String storedValue = UtilService.splitAsList(data.get(key))
                    .get(selected);
            itemsToCalculate.add(Double.valueOf(storedValue));
        }
        return buildResults(width, length, quantity, selected, itemsToCalculate, thisFragment);
    }

    private static List<String> buildResults(Double width, Double length, Integer quantity, Integer selected,
                                             List<Double> itemsToCalculate, String thisFragment) {
        List<String> results = new ArrayList<>();
        switch (thisFragment) {
            case "chapas":
                results = buildPlates(width, length, quantity, selected, itemsToCalculate);
                break;
            case "acero_inoxidable":
                results =  buildSteels(width, length, itemsToCalculate);
                break;
            case "forrar_mesa":
                results =  buildTable(width, length, quantity, itemsToCalculate);
                break;
            case "bandeja":
                results =  buildTray(width, length, quantity, itemsToCalculate);
                break;
            default:
                results =  buildPlates(length, length, quantity, selected, itemsToCalculate);
                break;
        }
        if (results.isEmpty())
            results.add("No hay resultados que mostrar.");
        return results;
    }

    private static List<String> buildPlates(Double width, Double length, Integer quantity, Integer selected,
                                            List<Double> itemsToCalculate) {
        Double basicResult = width * length * quantity * itemsToCalculate.get(PLATES_WEIGHT_INDEX);
        Double cutResult = basicResult * itemsToCalculate.get(PLATES_CUT_INDEX);
        Double fullResult = basicResult * itemsToCalculate.get(PLATES_FULL_INDEX);
        Double foldResult = basicResult * itemsToCalculate.get(PLATES_FOLD_INDEX);

        Double diffResult = foldResult - fullResult;
        Double laserCutResult = fullResult * 2.5;
        Double laserFoldResult = foldResult * 2.5;
        Double plasmaCutResult = cutResult * 1.6;
        Double plasmaFoldResult = foldResult * 1.6;

        List<String> results = new ArrayList<>();
        results.add(String.format(PLATES[0], cutResult));
        results.add(String.format(PLATES[1], fullResult));
        results.add(String.format(PLATES[2], foldResult));
        results.add(String.format(PLATES[3], diffResult));
        results.add(String.format(PLATES[4], laserCutResult));
        results.add(String.format(PLATES[5], laserFoldResult));
        results.add(String.format(PLATES[6], plasmaCutResult));
        results.add(String.format(PLATES[7], plasmaFoldResult));
        if (foldResult < 0) {
            results.remove(7);
            if (selected > 10)
                results.remove(6);
            results.remove(5);
            if (selected > 10)
                results.remove(4);
            results.remove(3);
            results.remove(2);
        } else if (selected > 10) {
            results.remove(6);
            results.remove(4);
        }
        return results;
    }

    private static List<String> buildSteels(Double width, Double length,
                                            List<Double> itemsToCalculate) {
        Double basicResult = width * length;
        Double shinyResult = basicResult * itemsToCalculate.get(STEEL_SHINY_INDEX);
        Double opaqueResult = basicResult * itemsToCalculate.get(STEEL_OPAQUE_INDEX);

        List<String> results = new ArrayList<>();
        results.add(String.format(STEELS[0], shinyResult));
        results.add(String.format(STEELS[1], opaqueResult));
        if(shinyResult < 0){
            results.remove(0);
            if (opaqueResult < 0)
                results.remove(0);
        } else if (opaqueResult < 0){
            results.remove(1);
        }

        return results;
    }

    private static List<String> buildTable(Double width, Double length, Integer quantity,
                                            List<Double> itemsToCalculate) {
        Double result = width * length * quantity * itemsToCalculate.get(0);

        List<String> results = new ArrayList<>();
        results.add(String.format(TABLE, result));
        return results;
    }

    private static List<String> buildTray(Double width, Double length, Integer quantity,
                                            List<Double> itemsToCalculate) {
        Double result = width * length * quantity * itemsToCalculate.get(0);

        List<String> results = new ArrayList<>();
        results.add(String.format(TRAY, result));
        return results;
    }

}
