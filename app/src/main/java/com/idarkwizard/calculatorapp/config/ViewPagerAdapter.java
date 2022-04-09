package com.idarkwizard.calculatorapp.config;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.idarkwizard.calculatorapp.fragments.AbstractFragment;

import java.util.List;
import java.util.Map;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private int itemCount = 0;
    private List<String> titles;
    private Map<String, String> data;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, int itemCount,
                            List<String> titles,
                            Map<String, String> data) {
        super(fragmentActivity);
        this.itemCount = itemCount;
        this.titles = titles;
        this.data = data;
    }

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    public Fragment createFragment(int position) {
        return new AbstractFragment(titles.get(position), data);
    }

    public int getItemCount() {
        return this.itemCount;
    }
}
