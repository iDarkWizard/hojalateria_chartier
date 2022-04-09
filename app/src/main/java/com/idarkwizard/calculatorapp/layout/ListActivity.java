package com.idarkwizard.calculatorapp.layout;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.idarkwizard.calculatorapp.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_layout);
        getSupportActionBar().hide();

        View backBtn = findViewById(R.id.list_back_btn);

        ListView mListView = findViewById(R.id.lv);

        String listName = getIntent().getExtras().getString("list_name");
        TextView textView = findViewById(R.id.list_text_view);
        textView.setText(getIntent().getExtras().getString("show_name"));

        List<String> platesTypeList = getIntent().getExtras().getStringArrayList(listName);
        List<String> fixedList = new ArrayList<>();
        platesTypeList.forEach(type -> fixedList.add(type.replace("//", "/")));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fixedList);

        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = (String) adapterView.getItemAtPosition(i);
                Intent intent = new Intent();
                intent.putExtra("selectedPlate", item);
                intent.putExtra("selectedPosition", i);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        backBtn.setOnClickListener(view -> {
            finish();
        });
    }
}
