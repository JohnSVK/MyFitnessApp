package com.example.android.myfitnessapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.myfitnessapp.JSONParser;
import com.example.android.myfitnessapp.R;
import com.example.android.myfitnessapp.presenters.ILoginPresenter;
import com.example.android.myfitnessapp.presenters.LoginPresenter;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private EditText mUsernameEditTxt;
    private EditText mPasswordEditTxt;
    private String mUser;
    private String username;

    private ILoginPresenter mPresenter;
    private ProgressDialog pDialog;

    public static final int CONNECTION_ERROR = 0;
    public static final int WRONG_DATA = 1;
    public static final int EMAIL_REQUIRED_FIELD = 2;
    public static final int INVALID_EMAIL = 3;
    public static final int PASSWORD_REQUIRED_FIELD = 4;
    public static final int PASSWORD_TOO_SHORT = 5;

    public static final int DATA_NULL = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mPresenter = new LoginPresenter(this);

        mUsernameEditTxt = (EditText) findViewById(R.id.login_username);
        mPasswordEditTxt = (EditText) findViewById(R.id.login_password);
        Button mLoginBtn = (Button) findViewById(R.id.login_btn);
        TextView mRegisterLink = (TextView) findViewById(R.id.login_no_account);
        //TextView mFreeLink = (TextView) findViewById(R.id.login_free);


        if (mLoginBtn != null) {
            mLoginBtn.setOnClickListener(new LoginListener());
        }
        if (mRegisterLink != null) {
            mRegisterLink.setOnClickListener(new RegisterLinkListener());
        }/*
        if (mFreeLink != null) {
            mFreeLink.setOnClickListener(new FreeLinkListener());
        }*/

        pDialog = new ProgressDialog(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
        }

        return super.onOptionsItemSelected(item);
    }

    private class LoginListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            showDialog(getString(R.string.auth_login_progressdialog));

            String username = mUsernameEditTxt.getText().toString();
            String password = mPasswordEditTxt.getText().toString();

            try {
                password = SHA256(password);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            mPresenter.loginUser(username, password);
        }
    }

    // HASH funkcia
    public static String SHA256 (String text) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");

        md.update(text.getBytes());
        byte[] digest = md.digest();

        return Base64.encodeToString(digest, Base64.DEFAULT);
    }

    public void unsuccessfulLogin(int errCode) {
        pDialog.dismiss();
        mUsernameEditTxt.setError(null);
        mPasswordEditTxt.setError(null);

        switch(errCode) {
            case CONNECTION_ERROR:
                Toast.makeText(getApplicationContext(), getString(R.string.general_connection_error), Toast.LENGTH_LONG).show();
                break;
            case WRONG_DATA:
                Toast.makeText(getApplicationContext(), getString(R.string.login_wrong_data), Toast.LENGTH_LONG).show();
                break;
            case EMAIL_REQUIRED_FIELD:
                mUsernameEditTxt.setError(getString(R.string.login_email_required));
                mUsernameEditTxt.requestFocus();
                break;
            case INVALID_EMAIL:
                mUsernameEditTxt.setError(getString(R.string.login_email_invalid));
                mUsernameEditTxt.requestFocus();
                break;
            case PASSWORD_REQUIRED_FIELD:
                mPasswordEditTxt.setError(getString(R.string.login_password_required));
                mPasswordEditTxt.requestFocus();
                break;
            case PASSWORD_TOO_SHORT:
                mPasswordEditTxt.setError(getString(R.string.login_password_short));
                mPasswordEditTxt.requestFocus();
                break;
            default:
                Log.e("LoginActivity", "Wrong Error Code");
        }

    }

    public void successfulLogin(String user) {
        mUser = user;

        pDialog.dismiss();
        mUsernameEditTxt.setError(null);
        mPasswordEditTxt.setError(null);

        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences_session), MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(getString(R.string.preferences_session_user), user.split("@")[0]);
        editor.apply();

        // ziskanie dat z databazy
        showDialog(getString(R.string.auth_loaddata_progressdialog));

        Log.v("LoginActivity", "USER LOGGED");
        mPresenter.getUserData();
    }

    public void unsuccessfulDataLoad(int errCode) {
        pDialog.dismiss();

        switch(errCode) {
            case CONNECTION_ERROR:
                Toast.makeText(getApplicationContext(), getString(R.string.general_connection_error), Toast.LENGTH_LONG).show();
                break;
            case DATA_NULL:
                Toast.makeText(getApplicationContext(), getString(R.string.login_userdata_null), Toast.LENGTH_LONG).show();

                Bundle bundle = new Bundle();
                bundle.putString(getString(R.string.preferences_user_data_email), mUser);

                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), CreateProfileActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
                finish();
                break;
            default:
                Log.e("LoginActivity", "Wrong Error Code");
        }

    }

    public void successfulDataLoad(HashMap<String, Object> data) {
        pDialog.dismiss();

        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences_user_data), MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(getString(R.string.preferences_user_data_email), (String) data.get(getString(R.string.preferences_user_data_email)));
        editor.putString(getString(R.string.preferences_user_data_username), (String) data.get(getString(R.string.preferences_user_data_username)));

        editor.putInt(getString(R.string.preferences_user_data_weight), (int) data.get(getString(R.string.preferences_user_data_weight)));
        editor.putInt(getString(R.string.preferences_user_data_height), (int) data.get(getString(R.string.preferences_user_data_height_param)));
        editor.putString(getString(R.string.preferences_user_data_location), (String) data.get(getString(R.string.preferences_user_data_location_lat)) + "," + (String) data.get(getString(R.string.preferences_user_data_location_lon)));
        //editor.putString(getString(R.string.preferences_user_data_location), (String) data.get(getString(R.string.preferences_user_data_location)));
        //editor.putString(getString(R.string.preferences_user_data_location_lat), (String) data.get(getString(R.string.preferences_user_data_location_lat)));
        //editor.putString(getString(R.string.preferences_user_data_location_lon), (String) data.get(getString(R.string.preferences_user_data_location_lon)));
        editor.apply();

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


    private class RegisterLinkListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            Intent registerIntent = new Intent();
            registerIntent.setClass(getApplicationContext(), RegisterActivity.class);

            startActivity(registerIntent);
        }
    }

    private class FreeLinkListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            SharedPreferences preferences = getSharedPreferences(getString(R.string.login_free), MODE_PRIVATE);

            SharedPreferences.Editor editor = preferences.edit();

            editor.putBoolean(getString(R.string.login_free_access), true);
            editor.apply();

            Bundle bundle = new Bundle();
            bundle.putBoolean(getString(R.string.login_free), true);

            Intent mainIntent = new Intent();
            mainIntent.setClass(getApplicationContext(), MainActivity.class);
            mainIntent.putExtras(bundle);

            startActivity(mainIntent);
            finish();
        }
    }
}
