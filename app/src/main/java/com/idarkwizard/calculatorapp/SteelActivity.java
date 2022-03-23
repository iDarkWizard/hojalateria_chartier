package com.idarkwizard.calculatorapp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SteelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buildLayout();
        getSupportActionBar().hide();

        View view = findViewById(R.id.calc_btn);
        view.setOnClickListener(this::onClick);
    }

    private void buildLayout() {
        TextView textView = findViewById(R.id.title);
        textView.setText(R.string.steel_layout);
    }

    private void onClick(View d) {
        finish();
    }
}