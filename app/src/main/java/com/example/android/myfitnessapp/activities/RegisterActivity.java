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
import com.example.android.myfitnessapp.presenters.IRegisterPresenter;
import com.example.android.myfitnessapp.presenters.RegisterPresenter;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

public class RegisterActivity extends AppCompatActivity {

    private EditText mEmailEditTxt;
    private EditText mUsernameEditTxt;
    private EditText mPasswordEditTxt;
    private Button mRegisterBtn;
    private TextView mLoginLink;

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    private static final String SERVER_URL = "http://192.168.1.103:80/myfitnessapp/register.php";

    private static IRegisterPresenter mPresenter;
    private static ProgressDialog pDialog;

    public static final int CONNECTION_ERROR = 0;
    public static final int EMAIL_REQUIRED_FIELD = 1;
    public static final int INVALID_EMAIL = 2;
    public static final int USERNAME_REQUIRED_FIELD = 3;
    public static final int PASSWORD_REQUIRED_FIELD = 4;
    public static final int PASSWORD_TOO_SHORT = 5;
    public static final int EMAIL_TAKEN = 6;
    public static final int USERNAME_TAKEN = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mPresenter = new RegisterPresenter(this);

        mEmailEditTxt = (EditText) findViewById(R.id.register_email);
        mUsernameEditTxt = (EditText) findViewById(R.id.register_username);
        mPasswordEditTxt = (EditText) findViewById(R.id.register_password);
        mRegisterBtn = (Button) findViewById(R.id.register_btn);
        mLoginLink = (TextView) findViewById(R.id.register_existing_account);

        mRegisterBtn.setOnClickListener(new RegisterListener());
        mLoginLink.setOnClickListener(new LoginLinkListener());

        pDialog = new ProgressDialog(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
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

    private class RegisterListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            showDialog();

            String email = mEmailEditTxt.getText().toString();
            String username = mUsernameEditTxt.getText().toString();
            String password = mPasswordEditTxt.getText().toString();

            if (!password.isEmpty()) {
                try {
                    password = SHA256(password);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

            }

            mPresenter.registerUser(email, username, password);

        }
    }

    // HASH funkcia
    public static String SHA256 (String text) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");

        md.update(text.getBytes());
        byte[] digest = md.digest();

        return Base64.encodeToString(digest, Base64.DEFAULT);
    }

    public void unsuccessfulRegister(int errCode) {
        pDialog.dismiss();
        mUsernameEditTxt.setError(null);
        mPasswordEditTxt.setError(null);

        switch(errCode) {
            case CONNECTION_ERROR:
                Toast.makeText(getApplicationContext(), getString(R.string.general_connection_error), Toast.LENGTH_LONG).show();
                break;
            case EMAIL_REQUIRED_FIELD:
                mEmailEditTxt.setError(getString(R.string.login_email_required));
                mEmailEditTxt.requestFocus();
                break;
            case INVALID_EMAIL:
                mEmailEditTxt.setError(getString(R.string.login_email_invalid));
                mEmailEditTxt.requestFocus();
                break;
            case USERNAME_REQUIRED_FIELD:
                mUsernameEditTxt.setError(getString(R.string.register_username_required));
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
            case EMAIL_TAKEN:
                Toast.makeText(getApplicationContext(), getString(R.string.register_email_taken), Toast.LENGTH_LONG).show();
                mEmailEditTxt.requestFocus();
                break;
            case USERNAME_TAKEN:
                Toast.makeText(getApplicationContext(), getString(R.string.register_username_taken), Toast.LENGTH_LONG).show();
                mUsernameEditTxt.requestFocus();
                break;
            default:
                Log.e("LoginActivity", "Wrong Error Code");
        }

    }

    public void successfulRegister(String user) {
        pDialog.dismiss();
        mEmailEditTxt.setError(null);
        mUsernameEditTxt.setError(null);
        mPasswordEditTxt.setError(null);

        /*
        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences_session), MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(getString(R.string.preferences_session_user), user);
        editor.apply();*/

        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.preferences_user_data_email), user);

        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), CreateProfileActivity.class);
        intent.putExtras(bundle);

        startActivity(intent);
        finish();
    }

    public void showDialog() {

        pDialog.setMessage(getString(R.string.auth_login_progressdialog));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();
    }

    private class LoginLinkListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            Intent loginIntent = new Intent();
            loginIntent.setClass(getApplicationContext(), LoginActivity.class);

            startActivity(loginIntent);
        }
    }
}
