package com.example.android.myfitnessapp.activities;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.myfitnessapp.JSONParser;
import com.example.android.myfitnessapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

public class CreateProfileActivity extends Activity {

    private String mUser;

    private int mWeight;
    private int mHeight;

    private String mLocation;
    private double lat;
    private double lng;

    private TextView weightView;
    private TextView heightView;

    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        pDialog = new ProgressDialog(this);

        mUser = getIntent().getStringExtra(getString(R.string.preferences_user_data_email));

        // default values
        mWeight = 60;
        mHeight = 180;

        weightView = (TextView) findViewById(R.id.create_profile_weight);
        weightView.setText(mWeight + " kg");

        weightView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWeightPicker();
            }
        });

        heightView = (TextView) findViewById(R.id.create_profile_height);
        heightView.setText(mHeight + " cm");

        heightView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHeightPicker();
            }
        });

        //final SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences_user_data), MODE_PRIVATE);
        //mWeight = preferences.getInt(getString(R.string.preferences_user_data_weight), 60);
        //mHeight = preferences.getInt(getString(R.string.preferences_user_data_height), 180);
        //String mLocation = preferences.getString(getString(R.string.preferences_user_data_location), null);


        // save button listener
        Button saveBtn = (Button) findViewById(R.id.create_profile_save_btn);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(v.getContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(v.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                lat = location.getLatitude();
                lng = location.getLongitude();

                mLocation = lat + "," + lng;

                Log.e("LOCATION", "LAT: " + lat + " LNG: " + lng);

                if (mUser == null) {
                    SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences_user_data), MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();

                    editor.putInt(getString(R.string.preferences_user_data_weight), mWeight);
                    editor.putInt(getString(R.string.preferences_user_data_height), mHeight);
                    editor.putString(getString(R.string.preferences_user_data_location), lat + "," + lng);
                    //editor.putFloat(getString(R.string.preferences_user_data_location_lat), (float) lat);
                    //editor.putFloat(getString(R.string.preferences_user_data_location_lng), (float) lng);
                    editor.apply();

                    Intent authIntent = new Intent();
                    authIntent.setClass(getApplicationContext(), MainActivity.class);
                    startActivity(authIntent);
                    finish();
                } else {
                    // upload data to server
                    new SendUserData().execute();
                }
            }
        });
    }

    public void showWeightPicker() {

        final Dialog d = new Dialog(this);
        d.setTitle(getText(R.string.create_profile_weight));
        d.setContentView(R.layout.dialog_number_picker);

        Button b1 = (Button) d.findViewById(R.id.button1);
        Button b2 = (Button) d.findViewById(R.id.button2);

        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker);
        np.setMaxValue(200);
        np.setMinValue(30);
        np.setValue(mWeight);
        np.setWrapSelectorWheel(false);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWeight = np.getValue();

                weightView.setText(String.valueOf(np.getValue()) + " kg");
                d.dismiss();
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
    }

    public void showHeightPicker() {

        final Dialog d = new Dialog(this);
        d.setTitle(getString(R.string.create_profile_height));
        d.setContentView(R.layout.dialog_number_picker);

        Button b1 = (Button) d.findViewById(R.id.button1);
        Button b2 = (Button) d.findViewById(R.id.button2);

        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker);
        np.setMaxValue(250);
        np.setMinValue(130);
        np.setValue(mHeight);
        np.setWrapSelectorWheel(false);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHeight = np.getValue();

                heightView.setText(String.valueOf(np.getValue()) + " cm");
                d.dismiss();
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
    }

    private class SendUserData extends AsyncTask<Void, Void, Boolean> {

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";

        // localhost url
        //private static final String SERVER_URL = "http://192.168.1.103:80/myfitnessapp/login.php/?";
        // remote server url
        private static final String SERVER_URL = "https://myapp-johnsvk.rhcloud.com/senduserdata.php/?";

        @Override
        protected void onPreExecute() {
            pDialog.setMessage(getString(R.string.auth_register_progressdialog));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            Uri builtUri = null;
            builtUri = Uri.parse(SERVER_URL).buildUpon()
                    .appendQueryParameter(getString(R.string.preferences_user_data_email), mUser)
                    .appendQueryParameter(getString(R.string.preferences_user_data_weight), String.valueOf(mWeight))
                    .appendQueryParameter(getString(R.string.preferences_user_data_height_param), String.valueOf(mHeight))
                    .appendQueryParameter(getString(R.string.preferences_user_data_location_lat), String.valueOf(lat))
                    .appendQueryParameter(getString(R.string.preferences_user_data_location_lon), String.valueOf(lng))
                    .build();
            try {
                URL url = new URL(builtUri.toString());
                Log.v("URL", url.toString());

                JSONObject json = JSONParser.getJSONFromUrl(url, "POST");

                if (json == null) {
                    Log.e("LoginActivity", "json object is null");

                    return null;
                }

                if (json.getInt(TAG_SUCCESS) == 1) {
                    return true;
                } else if (json.getInt(TAG_SUCCESS) == 0) {
                    return false;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            pDialog.dismiss();

            if (success == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.general_connection_error), Toast.LENGTH_LONG).show();
            } else if (!success) {
                Toast.makeText(getApplicationContext(), getString(R.string.general_database_error), Toast.LENGTH_LONG).show();
            } else {
                Intent authIntent = new Intent();
                authIntent.setClass(getApplicationContext(), LoginActivity.class);
                startActivity(authIntent);
                finish();
            }
        }
    }
}
