package com.example.android.myfitnessapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.android.myfitnessapp.DBHelper;
import com.example.android.myfitnessapp.R;
import com.example.android.myfitnessapp.activities.MainActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;

public class StatActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every loaded fragment in memory. If this becomes too
     * memory intensive, it may be best to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    private DBHelper mDB;

    private Context mContext;

    protected HashMap<Date, Integer> mValuesGained;
    protected HashMap<Date, Integer> mValuesBurned;

    private String mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        mDB = new DBHelper(this);

        mContext = this;

        // Set up the action bar.
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        if (mToolbar != null) {
            mToolbar.setTitle(R.string.title_stats);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mValuesGained = new HashMap<>();
        mValuesBurned = new HashMap<>();

        //new GetGainedDailyEnergyValues().execute();

        SharedPreferences pref = getSharedPreferences(getString(R.string.preferences_session), MODE_PRIVATE);
        mUser = pref.getString(getString(R.string.preferences_session_user), null);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.stats_pager);
        if (mViewPager != null) {
            mViewPager.setAdapter(mSectionsPagerAdapter);
            mViewPager.addOnPageChangeListener(this);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), MainActivity.class);

        startActivity(intent);
        finish();
    }

    @Override
    public void onPageSelected(int position) {
        mViewPager.setCurrentItem(position);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        private static LinkedHashMap<Date, Integer> mDataGained;
        private static LinkedHashMap<Date, Integer> mDataBurned;
        private static LinkedHashMap<Date, Integer> mDataGainedCarbs;
        private static LinkedHashMap<Date, Integer> mDataPlanned;

        private DBHelper mDB;

        private View rootView;
        private LinearLayout layout;
        private int sectionNum;

        private static String mUser;

        public PlaceholderFragment() {

        }

        /**
         * Returns a new instance of this fragment for the given section number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_view_pager_charts, container, false);
            layout = (LinearLayout) rootView;

            SharedPreferences pref = this.getContext().getSharedPreferences(getString(R.string.preferences_session), MODE_PRIVATE);
            mUser = pref.getString(getString(R.string.preferences_session_user), null);

            mDB = new DBHelper(this.getContext());

            mDataGained = new LinkedHashMap<>();
            mDataBurned = new LinkedHashMap<>();
            mDataGainedCarbs = new LinkedHashMap<>();
            mDataPlanned = new LinkedHashMap<>();

            new GetGainedDailyEnergyValues().execute();

            sectionNum = getArguments().getInt(ARG_SECTION_NUMBER);

            return rootView;
        }

        private LineChartData generateLineChartData(LinkedHashMap<Date, Integer> dataValues, String labelY) {

            //Log.v("STAT", ""+dataValues.size());

            if (dataValues.size() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

                int numValues = dataValues.size();
                List<AxisValue> axisValues = new ArrayList<>();

                List<PointValue> values = new ArrayList<PointValue>();

                int i = 0;
                for (Date date : dataValues.keySet()) {
                    values.add(new PointValue(i, dataValues.get(date)));

                    axisValues.add(new AxisValue(i, sdf.format(date).toCharArray()));

                    i++;
                }

                Line line = new Line(values);
                line.setColor(ChartUtils.COLOR_GREEN);

                List<Line> lines = new ArrayList<Line>();
                lines.add(line);

                LineChartData data = new LineChartData(lines);

                Axis axisX = new Axis();
                Axis axisY = new Axis().setHasLines(true);

                axisX.setName(getString(R.string.stats_chart_x_date));
                axisY.setName(labelY);

                axisX.setValues(axisValues);

                axisX.setLineColor(Color.BLACK);
                axisY.setLineColor(Color.BLACK);
                axisX.setTextColor(Color.BLACK);
                axisY.setTextColor(Color.BLACK);

                data.setAxisXBottom(axisX);
                data.setAxisYLeft(axisY);

                return data;
            } else {
                return null;
            }
        }

        private LineChartData generateLineChartDataPlanned(LinkedHashMap<Date, Integer> dataValues, String labelY) {

            //Log.v("STAT", ""+dataValues.size());

            if (dataValues.size() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

                int numValues = dataValues.size();
                List<AxisValue> axisValues = new ArrayList<>();

                List<Line> lines = new ArrayList<Line>();

                List<Date> dates = new ArrayList<>();
                for (Date date : dataValues.keySet()) {
                    dates.add(date);
                }

                int i = 0;
                for (Date date : dataValues.keySet()) {
                    List<PointValue> values = new ArrayList<PointValue>();
                    PointValue point = new PointValue(i, dataValues.get(date));

                    values.add(point);

                    if (i < dataValues.keySet().size() - 1) {
                        PointValue point2 = new PointValue(i+1, dataValues.get(dates.get(i+1)));
                        values.add(point2);
                    }

                    axisValues.add(new AxisValue(i, sdf.format(date).toCharArray()));

                    Line line = new Line(values);

                    if(date.before(new Date())) {
                        line.setPointColor(ChartUtils.COLOR_RED);
                    } else {
                        line.setPointColor(ChartUtils.COLOR_ORANGE);
                    }

                    line.setColor(ChartUtils.COLORS[0]);

                    lines.add(line);

                    i++;
                }

                LineChartData data = new LineChartData(lines);

                Axis axisX = new Axis();
                Axis axisY = new Axis().setHasLines(true);

                axisX.setName(getString(R.string.stats_chart_x_date));
                axisY.setName(labelY);

                axisX.setValues(axisValues);

                axisX.setLineColor(Color.BLACK);
                axisY.setLineColor(Color.BLACK);
                axisX.setTextColor(Color.BLACK);
                axisY.setTextColor(Color.BLACK);

                data.setAxisXBottom(axisX);
                data.setAxisYLeft(axisY);

                return data;
            } else {
                return null;
            }
        }

        private class GetGainedDailyEnergyValues extends AsyncTask<Void, Void, LinkedHashMap<Date, Integer>> {

            @Override
            protected LinkedHashMap<Date, Integer> doInBackground(Void... params) {
                LinkedHashMap<Date, Integer> valuesGained = new LinkedHashMap<>();

                valuesGained = mDB.getGainedDailyEnergyValues(mUser);

                return valuesGained;
            }

            @Override
            protected void onPostExecute(LinkedHashMap<Date, Integer> valuesGained) {
                /*for (Date date : valuesGained.keySet()) {
                    //Log.v("STATS", ""+ date.toString() + " Value: "+ valuesGained.get(date));
                }*/

                mDataGained = valuesGained;

                new GetBurnedDailyEnergyValues().execute();
            }
        }

        private class GetBurnedDailyEnergyValues extends AsyncTask<Void, Void, LinkedHashMap<Date, Integer>> {

            @Override
            protected LinkedHashMap<Date, Integer> doInBackground(Void... params) {
                LinkedHashMap<Date, Integer> valuesBurned = new LinkedHashMap<>();

                valuesBurned = mDB.getBurnedDailyEnergyValues(mUser);

                return valuesBurned;
            }

            @Override
            protected void onPostExecute(LinkedHashMap<Date, Integer> valuesBurned) {
                /*for (Date date : valuesBurned.keySet()) {
                    //Log.v("STATS", ""+ date.toString() + " Value: "+ valuesGained.get(date));
                }*/

                mDataBurned = valuesBurned;

                new GetGainedDailyCarbValues().execute();
            }
        }

        private class GetGainedDailyCarbValues extends AsyncTask<Void, Void, LinkedHashMap<Date, Integer>> {

            @Override
            protected LinkedHashMap<Date, Integer> doInBackground(Void... params) {
                LinkedHashMap<Date, Integer> valuesGainedCarbs = new LinkedHashMap<>();

                valuesGainedCarbs = mDB.getGainedDailyCarbValues(mUser);

                return valuesGainedCarbs;
            }

            @Override
            protected void onPostExecute(LinkedHashMap<Date, Integer> valuesGainedCarbs) {
                /*for (Date date : valuesBurned.keySet()) {
                    //Log.v("STATS", ""+ date.toString() + " Value: "+ valuesGained.get(date));
                }*/

                mDataGainedCarbs = valuesGainedCarbs;

                new GetPlannedDailyValues().execute();
            }
        }

        private class GetPlannedDailyValues extends AsyncTask<Void, Void, LinkedHashMap<Date, Integer>> {

            @Override
            protected LinkedHashMap<Date, Integer> doInBackground(Void... params) {
                LinkedHashMap<Date, Integer> values = new LinkedHashMap<>();

                values = mDB.getPlannedDailyValues(mUser);

                return values;
            }

            @Override
            protected void onPostExecute(LinkedHashMap<Date, Integer> values) {
                /*for (Date date : valuesBurned.keySet()) {
                    //Log.v("STATS", ""+ date.toString() + " Value: "+ valuesGained.get(date));
                }*/

                mDataPlanned = values;

                setupLayout();
            }
        }

        private void setupLayout() {
            switch (sectionNum) {
                case 1:
                    LineChartView lineChartViewGained = new LineChartView(getActivity());

                    if (mDataGained != null)
                        lineChartViewGained.setLineChartData(generateLineChartData(mDataGained, getString(R.string.stats_chart_y_gained)));

                    lineChartViewGained.setZoomType(ZoomType.HORIZONTAL);
                    lineChartViewGained.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);

                    layout.addView(lineChartViewGained);
                    break;
                case 2:
                    LineChartView lineChartViewBurned = new LineChartView(getActivity());

                    if (mDataBurned != null)
                        lineChartViewBurned.setLineChartData(generateLineChartData(mDataBurned, getString(R.string.stats_chart_y_burned)));

                    lineChartViewBurned.setZoomType(ZoomType.HORIZONTAL);
                    lineChartViewBurned.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);

                    layout.addView(lineChartViewBurned);
                    break;
                case 3:
                    LineChartView lineChartViewGainedCarbs = new LineChartView(getActivity());

                    if (mDataGainedCarbs != null)
                        lineChartViewGainedCarbs.setLineChartData(generateLineChartData(mDataGainedCarbs, getString(R.string.stats_chart_y_gained_carbs)));

                    lineChartViewGainedCarbs.setZoomType(ZoomType.HORIZONTAL);
                    lineChartViewGainedCarbs.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);

                    layout.addView(lineChartViewGainedCarbs);
                    break;
                case 4:
                    LineChartView lineChartViewPlanned = new LineChartView(getActivity());

                    if (mDataPlanned != null)
                        lineChartViewPlanned.setLineChartData(generateLineChartDataPlanned(mDataPlanned, getString(R.string.stats_chart_y_planned)));

                    lineChartViewPlanned.setZoomType(ZoomType.HORIZONTAL);
                    lineChartViewPlanned.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);

                    layout.addView(lineChartViewPlanned);
                    break;
            }
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.stats_title_gained);
                case 1:
                    return getString(R.string.stats_title_burned);
                case 2:
                    return getString(R.string.stats_title_gained_sugar);
                case 3:
                    return getString(R.string.stats_title_planned);
            }
            return null;
        }
    }

}

