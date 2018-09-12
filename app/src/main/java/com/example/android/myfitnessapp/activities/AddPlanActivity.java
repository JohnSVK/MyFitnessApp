package com.example.android.myfitnessapp.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.android.myfitnessapp.DBHelper;
import com.example.android.myfitnessapp.DateDialogFragment;
import com.example.android.myfitnessapp.DateDialogFragmentAddPlan;
import com.example.android.myfitnessapp.DateDialogFragmentListener;
import com.example.android.myfitnessapp.MainListAdapter;
import com.example.android.myfitnessapp.PlanListAdapter;
import com.example.android.myfitnessapp.R;
import com.example.android.myfitnessapp.ReminderReciever;
import com.example.android.myfitnessapp.ReminderService;
import com.example.android.myfitnessapp.TimeDialogFragment;
import com.example.android.myfitnessapp.TimeDialogFragmentListener;
import com.example.android.myfitnessapp.entities.MainListItem;
import com.example.android.myfitnessapp.entities.PlanListItem;
import com.github.channguyen.rsv.RangeSliderView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class AddPlanActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private ArrayList<PlanListItem> mList;
    private PlanListAdapter mListAdapter;
    private DBHelper mDB;

    private PlanListItem entryToAdd;

    private static ProgressDialog pDialog;

    private Spinner typeView;
    private TextView dateView;
    private TextView timeView;
    private TextView durationView;

    private String mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_plan);

        mDB = new DBHelper(this);

        // toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setTitle(R.string.title_activity_add_plan);

        typeView = (Spinner) findViewById(R.id.add_plan_activity_type);
        dateView = (TextView) findViewById(R.id.add_plan_activity_date);
        timeView = (TextView) findViewById(R.id.add_plan_activity_time);
        durationView = (TextView) findViewById(R.id.add_plan_activity_duration_size);

        RangeSliderView slider = (RangeSliderView) findViewById(R.id.add_plan_activity_slider);
        if (slider != null) {
            slider.setOnSlideListener(new SliderListener());
            slider.setInitialIndex(1);
            slider.setRangeCount(10);
        }

        mList = new ArrayList<>();
        mListAdapter = new PlanListAdapter(this, mList);
        entryToAdd = new PlanListItem();

        int entryDefaultDuration = 30;
        entryToAdd.setmDuration(entryDefaultDuration);

        String sliderText = entryDefaultDuration + " min";
        if (durationView != null) {
            durationView.setText(sliderText);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        entryToAdd.setmDate(Calendar.getInstance());

        dateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //vytvorenie DateDialogFragment
                DateDialogFragmentAddPlan ddf = DateDialogFragmentAddPlan.newInstance(v.getContext(), R.string.date_dialog_title_add_plan, Calendar.getInstance());

                //nastavenie listenera DateDialogFragmentListener
                ddf.setDateDialogFragmentListener(new DateDialogFragmentListener() {
                    @Override
                    public void dateDialogFragmentDataSet(Calendar date) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

                        Calendar dateToAdd = entryToAdd.getmDate();
                        dateToAdd.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH));
                        dateToAdd.set(Calendar.MONTH, date.get(Calendar.MONTH));
                        dateToAdd.set(Calendar.YEAR, date.get(Calendar.YEAR));
                        entryToAdd.setmDate(dateToAdd);

                        dateView.setText(sdf.format(date.getTime()));
                    }
                });

                ddf.show(getFragmentManager(), "date picker dialog fragment");
            }
        });

        timeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimeDialogFragment tdf = TimeDialogFragment.newInstance(v.getContext(), R.string.date_dialog_title, Calendar.getInstance());

                //nastavenie listenera DateDialogFragmentListener
                tdf.setTimeDialogFragmentListener(new TimeDialogFragmentListener() {
                    @Override
                    public void timeDialogFragmentDataSet(Calendar date) {
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

                        Calendar dateToAdd = entryToAdd.getmDate();
                        dateToAdd.set(Calendar.HOUR_OF_DAY, date.get(Calendar.HOUR_OF_DAY));
                        dateToAdd.set(Calendar.MINUTE, date.get(Calendar.MINUTE));
                        entryToAdd.setmDate(dateToAdd);

                        timeView.setText(sdf.format(date.getTime()));
                    }
                });

                tdf.show(getFragmentManager(), "time picker dialog fragment");
            }
        });

        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences_session), MODE_PRIVATE);
        mUser = preferences.getString(getString(R.string.preferences_session_user), null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_plan, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_add_plan_confirm) {
            String typeStr = (String) typeView.getSelectedItem();
            entryToAdd.setmName(typeStr);

            updateEntryListData();

            new AddEntry().execute(entryToAdd);

        } else if (id == android.R.id.home) {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), PlanActivity.class);

            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), PlanActivity.class);

        startActivity(intent);
        finish();
    }

    private class AddEntry extends AsyncTask<PlanListItem, Void, Void> {

        // pridanie novej polozky do databazy
        @Override
        protected Void doInBackground(PlanListItem... params) {
            if (params[0] == null)
                return null;

            String name = params[0].getmName();

            /*Calendar date = new GregorianCalendar();
            date.setTime(params[0].getmDate());*/
            Calendar date = params[0].getmDate();
            //date.set(Calendar.HOUR_OF_DAY, date.get(Calendar.HOUR_OF_DAY)-1);

            int duration = params[0].getmDuration();

            int type = 0;
            if (name.equals(getString(R.string.add_entry_exercise_walking))) {
                type = 0;
            } else if (name.equals(getString(R.string.add_entry_exercise_running))) {
                type = 1;
            } else if (name.equals(getString(R.string.add_entry_exercise_cycling))) {
                type = 2;
            }

            int value = getExerciseCalValue(type);

            //Log.e("CAL VALUE", ""+value);

            mDB.insertPlannedActivity(mUser, name, duration, date, 0, value);

            return null;
        }

        @Override
        protected void onPostExecute(Void params) {

            // notification set
            Intent myIntent = new Intent(AddPlanActivity.this, ReminderReciever.class);
            myIntent.putExtra("data", entryToAdd.getmName() + " " + entryToAdd.getmDuration() + " min");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(AddPlanActivity.this, 0, myIntent, 0);
            AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC, entryToAdd.getmDate().getTimeInMillis(), pendingIntent);

            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), PlanActivity.class);

            startActivity(intent);
            finish();
        }
    }

    private int getExerciseCalValue(int type) {
        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences_user_data), MODE_PRIVATE);

        int weight = preferences.getInt(getString(R.string.preferences_user_data_weight), 1);
        int duration = entryToAdd.getmDuration();

        double koeficient = 0;

        switch (type) {
            case 0: {
                koeficient = 0.07;

                break;
            }
            case 1: {
                koeficient = 0.22;

                break;
            }
            case 2: {
                koeficient = 0.14;

                break;
            }
        }

        return (int) (weight * duration * koeficient);
    }

    private void updateEntryListData() {

        //entryToAdd.setmDate(Calendar.getInstance());

        mList.clear();
        mList.add(entryToAdd);

        // aktualizovanie adaptera
        mListAdapter.setmItems(mList);
        mListAdapter.notifyDataSetChanged();
    }

    private class SliderListener implements RangeSliderView.OnSlideListener {

        @Override
        public void onSlide(int index) {
            int k = 15;
            int size = (index + 1) * k;

            entryToAdd.setmDuration(size);

            durationView.setText(size + " min");

            updateEntryListData();
        }
    }
}
