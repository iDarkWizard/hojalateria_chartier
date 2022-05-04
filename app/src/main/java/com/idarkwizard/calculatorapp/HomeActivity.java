package com.idarkwizard.calculatorapp;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.idarkwizard.calculatorapp.config.ViewPagerAdapter;
import com.idarkwizard.calculatorapp.service.DownloadService;
import com.idarkwizard.calculatorapp.service.UtilService;

import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

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

        settingsBtn.setOnClickListener((view) -> showSettingsDialog());

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

    private void showSettingsDialog() {
        Log.i(TAG, "showSettingsDialog: Showing settings dialog");
        final Dialog dialog = new Dialog(HomeActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.settings);

        final Window window = dialog.getWindow();
        final WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.RIGHT | Gravity.TOP;
        window.setAttributes(wlp);

        final TextView loadData = dialog.findViewById(R.id.load_data_setting);
        final TextView showVersion = dialog.findViewById(R.id.show_version_setting);
        final TextView unloadData = dialog.findViewById(R.id.unload_data_setting);

        final View loadDataBackground = dialog.findViewById(R.id.load_data_setting_background);
        final View showVersionBackground = dialog.findViewById(R.id.show_version_setting_background);

        loadData.setOnClickListener((view) -> {
            loadDataBackground.setElevation(20);
            loadData.setElevation(20);
            showLoadDataDialog();
            dialog.cancel();
        });

        showVersion.setOnClickListener((view) -> {
            showVersionBackground.setElevation(20);
            showVersion.setElevation(20);
            showVersionDialog();
            dialog.cancel();
        });


        dialog.show();
    }

    private void showLoadDataDialog() {
        Log.i("THIS", "Showing version dialog");
        final Dialog dialog = new Dialog(HomeActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.load_data_setting);

        final TextView fromSd = dialog.findViewById(R.id.from_sd_setting);
        final TextView fromGit = dialog.findViewById(R.id.from_git_setting);

        final View fromSdBackground = dialog.findViewById(R.id.from_sd_setting_background);
        final View fromGitBackground = dialog.findViewById(R.id.from_git_setting_background);

        fromSd.setOnClickListener((view) -> {
            fromSdBackground.setElevation(20);
            fromSd.setElevation(20);
            startActivity(new Intent(this, LoadDataActivity.class));
            dialog.cancel();
        });

        fromGit.setOnClickListener((view) -> {
            fromGitBackground.setElevation(20);
            fromGit.setElevation(20);
            download();
            dialog.cancel();
        });
        dialog.show();
    }

    private void download() {
        DownloadService ds = new DownloadService(this);
        ds.startDownload();
    }

    private void showVersionDialog() {
        Log.i("THIS", "Showing version dialog");
        final Dialog dialog = new Dialog(HomeActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.version);

        final TextView version = dialog.findViewById(R.id.version);
        version.setText(getResources().getString(R.string.api_version));

        dialog.show();
    }
}
