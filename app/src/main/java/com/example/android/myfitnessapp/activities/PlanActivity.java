package com.example.android.myfitnessapp.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.myfitnessapp.DBHelper;
import com.example.android.myfitnessapp.JSONParser;
import com.example.android.myfitnessapp.MainListAdapter;
import com.example.android.myfitnessapp.PlanListAdapter;
import com.example.android.myfitnessapp.R;
import com.example.android.myfitnessapp.entities.MainListItem;
import com.example.android.myfitnessapp.entities.PlanListItem;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;

public class PlanActivity extends AppCompatActivity {

    private ArrayList<PlanListItem> mList;
    private PlanListAdapter mListAdapter;
    private DBHelper mDB;
    private ProgressDialog pDialog;

    private double mLat;
    private double mLng;

    private ArrayList<String> mDailyForecastIcons;

    private String mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);

        // local DB init
        mDB = new DBHelper(this);

        SharedPreferences pref = getSharedPreferences(getString(R.string.preferences_session), MODE_PRIVATE);
        mUser = pref.getString(getString(R.string.preferences_session_user), null);

        // list init
        mList = new ArrayList<>();
        mListAdapter = new PlanListAdapter(getApplicationContext(), mList);

        ListView listView = (ListView) findViewById(R.id.plan_listview);
        listView.setAdapter(mListAdapter);

        // toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mToolbar.setTitle(R.string.title_activity_plan);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get coordinates
        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences_user_data), MODE_PRIVATE);
        String locationStr = preferences.getString(getString(R.string.preferences_user_data_location), null);

        if (locationStr != null) {
            mLat = Double.parseDouble(locationStr.split(",")[0]);
            mLng = Double.parseDouble(locationStr.split(",")[1]);
        }
        //Log.e("PLAN LOCATION", mLat + "," + mLng);

        mDailyForecastIcons = new ArrayList<>();

        pDialog = new ProgressDialog(this);

        showDialog(getString(R.string.plan_dialog));
        new FetchWeatherTask().execute();

        //new GetActPlannedActivities().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_plan, menu);
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
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), MainActivity.class);

            startActivity(intent);
            finish();
        } else if (id == R.id.add_plan_activity) {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), AddPlanActivity.class);

            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), MainActivity.class);

        startActivity(intent);
        finish();
    }

    public void showDialog(String msg) {

        pDialog.setMessage(msg);
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();
    }

    private class GetActPlannedActivities extends AsyncTask<Void, Void, ArrayList<PlanListItem>> {

        @Override
        protected ArrayList<PlanListItem> doInBackground(Void... params) {

            return mDB.getActPlannedActivities(mUser);
        }

        @Override
        protected void onPostExecute(ArrayList<PlanListItem> plannedActivities) {
            pDialog.dismiss();

            mList = plannedActivities;

            if (mList != null && !mList.isEmpty()) {
                //loadListIcons(plannedActivities);

                mListAdapter.setmDailyForecastIcons(mDailyForecastIcons);
                mListAdapter.setmItems(mList);
                mListAdapter.notifyDataSetChanged();

                Log.v("PlanActivity", "Entries successfully loaded");

                // kontrola naplanovanych aktivit
                checkPlan(mList);

            } else {
                Log.e("MainActivity", "NULL ENTRIES");
            }
        }
    }

    private class FetchWeatherTask extends AsyncTask<Void, Void, ArrayList<String>> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected ArrayList<String> doInBackground(Void... params) {

            //LinkedHashMap<Calendar, String> daysForecast = new LinkedHashMap<>();
            ArrayList<String> dailyForecastIcons = new ArrayList<>();

            String format = "json";
            String units = "metric";
            int numDays = 14;

            //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");

            final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily";
            final String LAT_PARAM = "lat";
            final String LNG_PARAM = "lon";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";
            final String APIKEY_PARAM = "APPID";

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(LAT_PARAM, String.valueOf(mLat))
                    .appendQueryParameter(LNG_PARAM, String.valueOf(mLng))
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                    .appendQueryParameter(APIKEY_PARAM, "d13b16b1710ce9fe47f9390787336351")
                    .build();

            URL url = null;
            try {
                url = new URL(builtUri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            JSONObject forecastJson = JSONParser.getJSONFromUrl(url, "GET");

            JSONArray listJson = null;

            try {
                if (forecastJson != null) {
                    listJson = forecastJson.getJSONArray("list");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (listJson != null) {
                for (int i = 0; i < listJson.length(); i++) {
                    try {
                        JSONObject dayForecast = listJson.getJSONObject(i);

                        /*long date = dayForecast.getLong("dt");
                        Calendar calendar = new GregorianCalendar();
                        calendar.setTimeInMillis(date);*/

                        String dateIcon = dayForecast.getJSONArray("weather").getJSONObject(0).getString("icon");

                        //daysForecast.put(calendar, dateIcon);
                        dailyForecastIcons.add(dateIcon);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            //Log.v(LOG_TAG, "Forecast JSON String: ");

            return dailyForecastIcons;
        }

        @Override
        protected void onPostExecute(ArrayList<String> dailyForecast) {
            pDialog.dismiss();

            if (dailyForecast != null && !dailyForecast.isEmpty()) {

               mDailyForecastIcons = dailyForecast;

            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.general_connection_error), Toast.LENGTH_LONG).show();

                Log.e("PlanActivity", "NULL weather ENTRIES");
            }

            showDialog(getString(R.string.auth_loaddata_progressdialog));

            new GetActPlannedActivities().execute();
        }
    }

    private class GetPlannedActivities extends AsyncTask<Void, Void, ArrayList<PlanListItem>> {

        @Override
        protected ArrayList<PlanListItem> doInBackground(Void... params) {

            return mDB.getAllPlannedActivities(mUser);
        }

        @Override
        protected void onPostExecute(ArrayList<PlanListItem> plannedActivities) {

            mList = plannedActivities;

            if (mList != null && !mList.isEmpty()) {

                mListAdapter.setmItems(mList);
                mListAdapter.notifyDataSetChanged();

                Log.v("PlanActivity", "Entries successfully loaded");

                // kontrola naplanovanych aktivit
                checkPlan(mList);

            } else {
                Log.e("MainActivity", "NULL ENTRIES");
            }

        }
    }

    private void loadListIcons(ArrayList<PlanListItem> list) {
        Calendar calendar = Calendar.getInstance();
        String baseUrl = "http://api.openweathermap.org/img/w/";

        Log.v("DATE", calendar.toString());

        for(PlanListItem item : list) {
            Calendar itemDate = item.getmDate();

            Log.v("DATE", itemDate.toString());


            if (itemDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                    itemDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)){
                ImageView weatherIcon = new ImageView(this);

                //if (itemDate.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)) {
                    Picasso.with(this).load(baseUrl + mDailyForecastIcons.get(0)+ ".png").into(weatherIcon);
                    item.setmWeatherImage(weatherIcon);
                //}
            }
        }
    }

    private void checkPlan(ArrayList<PlanListItem> list) {
        //final long MILLISECS_PER_DAY = 24 * 60 * 60 * 1000;
        int activitiesCount = 0;

        for (PlanListItem item : list) {
            if (Calendar.getInstance().after(item.getmDate())) {
                activitiesCount++;

                Calendar itemDate = item.getmDate();
                itemDate.set(Calendar.HOUR_OF_DAY, item.getmDate().get(Calendar.HOUR_OF_DAY) + 1);

                if (Calendar.getInstance().after(itemDate)) {

                }

            }
        }
        //Toast.makeText(this, "Počet nesplnených naplánovaných aktivít: " + activitiesCount, Toast.LENGTH_LONG).show();
    }
}
