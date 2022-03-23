package com.idarkwizard.calculatorapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        View view = findViewById(R.id.calc_btn);
        view.setOnClickListener(d -> {
            startActivity(new Intent(d.getContext(), SteelActivity.class));
        });
    }

}