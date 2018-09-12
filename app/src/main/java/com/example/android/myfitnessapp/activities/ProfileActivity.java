package com.example.android.myfitnessapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.android.myfitnessapp.R;
import com.example.android.myfitnessapp.entities.MainListItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private TextView usernameEditxt;
    private TextView emailEditxt;
    private TextView weightEditxt;
    private TextView heightEditxt;
    private TextView locationEditxt;
    private TextView bmiEditxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mToolbar.setTitle(R.string.title_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        usernameEditxt = (TextView) findViewById(R.id.profile_username);
        emailEditxt = (TextView) findViewById(R.id.profile_email);
        weightEditxt = (TextView) findViewById(R.id.profile_weight);
        heightEditxt = (TextView) findViewById(R.id.profile_height);
        //locationEditxt = (TextView) findViewById(R.id.profile_location);
        bmiEditxt = (TextView) findViewById(R.id.profile_bmi);

        if (!getSharedPreferences(getString(R.string.login_free), MODE_PRIVATE)
                .getBoolean(getString(R.string.login_free_access), false)) {
            // set values
            SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences_user_data), MODE_PRIVATE);

            String username = preferences.getString(getString(R.string.preferences_user_data_username), null);
            String email = preferences.getString(getString(R.string.preferences_user_data_email), null);
            int weight = preferences.getInt(getString(R.string.preferences_user_data_weight), 60);
            int height = preferences.getInt(getString(R.string.preferences_user_data_height), 180);
            //String location = preferences.getString(getString(R.string.preferences_user_data_location), null);
            double bmi = weight / Math.pow(((double)height / 100), 2);

            usernameEditxt.setText(username);
            emailEditxt.setText(email);
            weightEditxt.setText(weight + " kg");
            heightEditxt.setText(height + " cm");
            //locationEditxt.setText(location);
            bmiEditxt.setText("" + String.format("%.2f", bmi));
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

}
