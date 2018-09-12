package com.example.android.myfitnessapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by John on 24. 4. 2016.
 */
/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the sections/tabs/pages.
 */
public class MainPagerAdapter extends FragmentPagerAdapter {

    private int mCount;
    private ArrayList<Date> mDates;

    public MainPagerAdapter(FragmentManager fm) {
        super(fm);

        mCount = 0;
        mDates = new ArrayList<>();
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        return MainFragmentPage.newInstance(position, sdf.format(mDates.get(position)));
    }

    @Override
    public int getCount() {
        return mCount;
    }

    public void setmCount(int mCount) {
        this.mCount = mCount;
    }

    public void setmDates(ArrayList<Date> mDates) {
        this.mDates = mDates;
    }

    /*
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return getString(R.string.stats_title_gained);
            case 1:
                return getString(R.string.stats_title_burned);
        }
        return null;
    }*/
}
