package com.idarkwizard.calculatorapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.idarkwizard.calculatorapp.config.ViewPagerAdapter;
import com.idarkwizard.calculatorapp.service.UtilService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private Map<String, String> data;
    private List<String> titles;
    private View settingsBtn;
    private ListView settings;
    private int titlesCount = 0;

    private void loadData() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        data = (Map<String, String>) sharedPreferences.getAll();
        titles = UtilService.splitAsList(data.get("sheets_names"));
        titlesCount = titles.size();
    }

    @Override
    public void onBackPressed() {
        if(settings.getVisibility() == View.VISIBLE){
            settings.setVisibility(View.GONE);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        loadData();
        settingsBtn = findViewById(R.id.settings_btn);
        settings = findViewById(R.id.settings);
        settings.setElevation(20);
        ArrayList<String> settingsList = new ArrayList<>();
        settingsList.add("Cargar datos.");
        ArrayAdapter<String> settingsAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                settingsList);
        settingsAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        settings.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, settingsList));
        settingsBtn.setOnClickListener(v -> {
            if(settings.getVisibility() == View.GONE)
                settings.setVisibility(View.VISIBLE);
            else
                settings.setVisibility(View.GONE);
        });
        settings.setOnItemClickListener((adapterView, view, i, l) -> {
            Intent intent = new Intent(HomeActivity.this, LoadDataActivity.class);
            startActivity(intent);
            settings.setVisibility(View.GONE);
        });
        TabLayout tabLayout = findViewById(R.id.main_tabs);
        ViewPager2 viewPager2 = findViewById(R.id.main_pager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(this, titlesCount, titles, data);
        viewPager2.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager2,
            new TabLayoutMediator.TabConfigurationStrategy() {
                @Override
                public void onConfigureTab(TabLayout.Tab tab, int position) {
                    tab.setText(titles.get(position));
                }
            }).attach();
    }
}
