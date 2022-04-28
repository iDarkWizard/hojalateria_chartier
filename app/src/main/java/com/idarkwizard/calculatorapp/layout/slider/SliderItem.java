package com.idarkwizard.calculatorapp.layout.slider;

public class SliderItem {

    private String selectedResult;

    public SliderItem (String selectedResult) {
        this.selectedResult = selectedResult;
    }

    public String getSelectedResult() {
        return this.selectedResult;
    }

    public void setSelectedResult(String selectedResult) {
        this.selectedResult = selectedResult;
    }
}
