package com.example.android.myfitnessapp.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.myfitnessapp.DBHelper;
import com.example.android.myfitnessapp.R;
import com.example.android.myfitnessapp.entities.MainListItem;
import com.google.android.gms.plus.PlusShare;

import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private DBHelper mDB;
    private Integer idToRemove;
    private String name;
    private String size;
    private String value;
    private boolean isFoodItem;

    private Toolbar mToolbar;

    private static final int PLUS_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mDB = new DBHelper(this);
        Bundle bundle = getIntent().getExtras();

        idToRemove = bundle.getInt("id");
        name = bundle.getString("name");
        size = bundle.getString("size");
        value = bundle.getString("value");
        isFoodItem = bundle.getBoolean("is_food_item");

        TextView headerView = (TextView) findViewById(R.id.detail_header);
        headerView.setText(name);
        TextView sizeView = (TextView) findViewById(R.id.detail_size);
        sizeView.setText(size);
        TextView valueView = (TextView) findViewById(R.id.detail_energy);
        valueView.setText(value);

        Button deleteBtn = (Button) findViewById(R.id.detail_btn_delete);
        deleteBtn.setOnClickListener(new DeleteClickListener());

        // toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mToolbar.setTitle(R.string.title_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_detail_share) {

            plusShare(isFoodItem, size);

        } else if (id == android.R.id.home) {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), MainActivity.class);

            startActivity(intent);
            finish();
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

    private void plusShare(boolean isFoodItem, String size) {
        String text = "";

        if (isFoodItem) {
            text = "Práve som prijal " + size + " kalórií";
        } else {
            text = "Práve som spálil " + size + " kalórií";
        }

        // Launch the Google+ share dialog with attribution to your app.
        Intent shareIntent = new PlusShare.Builder(this)
                .setType("text/plain")
                .setText(text)
                        //.setContentUrl(Uri.parse("https://developers.google.com/+/"))
                .getIntent();

        startActivityForResult(shareIntent, PLUS_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PLUS_REQUEST_CODE) {
            // Google Plus Result
            Log.v("ADDENTRY", "Successfully posted on Google Plus");

            Intent mainIntent = new Intent();
            mainIntent.setClass(getApplicationContext(), MainActivity.class);

            startActivity(mainIntent);
            finish();
        }
    }

    private class DeleteClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // vymazanie z databazy
            new RemoveUserEntry().execute();
        }
    }

    private class RemoveUserEntry extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            //Log.e("DATABASE REMOVE ID", ""+idToRemove);
            mDB.removeEntry(idToRemove);

            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            Intent mainIntent = new Intent();
            mainIntent.setClass(getApplicationContext(), MainActivity.class);

            startActivity(mainIntent);
            finish();
        }
    }
}
