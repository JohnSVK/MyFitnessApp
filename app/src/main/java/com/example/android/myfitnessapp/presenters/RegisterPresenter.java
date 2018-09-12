package com.example.android.myfitnessapp.presenters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.android.myfitnessapp.JSONParser;
import com.example.android.myfitnessapp.R;
import com.example.android.myfitnessapp.activities.LoginActivity;
import com.example.android.myfitnessapp.activities.MainActivity;
import com.example.android.myfitnessapp.activities.RegisterActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by John on 27. 3. 2016.
 */
public class RegisterPresenter implements IRegisterPresenter {

    private RegisterActivity mView;

    private String mUser;

    public RegisterPresenter(RegisterActivity view) {
        this.mView = view;
    }

    @Override
    public void registerUser(String email, String username, String password) {

        mUser = email;

        if (email.isEmpty()) {
            mView.unsuccessfulRegister(RegisterActivity.EMAIL_REQUIRED_FIELD);
        } else if (!email.contains("@")) {
            mView.unsuccessfulRegister(RegisterActivity.INVALID_EMAIL);
        } else if (username.isEmpty()) {
            mView.unsuccessfulRegister(RegisterActivity.USERNAME_REQUIRED_FIELD);
        } else if (password.isEmpty()) {
            mView.unsuccessfulRegister(RegisterActivity.PASSWORD_REQUIRED_FIELD);
        } else if(password.length() < 4) {
            mView.unsuccessfulRegister(RegisterActivity.PASSWORD_TOO_SHORT);
        } else {
            new RegisterUser().execute(email, username, password);
        }
    }

    private class RegisterUser extends AsyncTask<String, Void, Integer> {

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";

        // localhost url
        //private static final String SERVER_URL = "http://192.168.1.103:80/myfitnessapp/register.php";
        //remote server url
        private static final String SERVER_URL = "https://myapp-johnsvk.rhcloud.com/register.php/?";

        private static final String REGISTER_FAILED = "failed";

        private ProgressDialog pDialog;

        @Override
        protected Integer doInBackground(String... params) {

            Uri builtUri = null;
            try {
                builtUri = Uri.parse(SERVER_URL).buildUpon()
                        .appendQueryParameter("email", URLEncoder.encode(params[0], "UTF-8"))
                        .appendQueryParameter("username", URLEncoder.encode(params[1], "UTF-8"))
                        .appendQueryParameter("password", URLEncoder.encode(params[2], "UTF-8"))
                        .build();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try {
                URL url = new URL(builtUri.toString());
                //Log.e("URL", url.toString());
                JSONObject json = JSONParser.getJSONFromUrl(url, "POST");

                if (json == null) {
                    Log.e("LoginActivity", "json object is null");

                    return null;
                }

                return json.getInt(TAG_SUCCESS);

                /*
                if (json.getInt(TAG_SUCCESS) == 1) {
                    return json.getString(TAG_MESSAGE);
                } else if (json.getInt(TAG_SUCCESS) == 0) {
                    return REGISTER_FAILED;
                } else if (json.getInt(TAG_SUCCESS) == 2) {
                    return REGISTER_FAILED;
                } else if (json.getInt(TAG_SUCCESS) == 3) {
                    return REGISTER_FAILED;
                }*/

            } catch (MalformedURLException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Integer successCode) {

            if (successCode == null || successCode == 0) {
                mView.unsuccessfulRegister(RegisterActivity.CONNECTION_ERROR);
            } else if (successCode == 2) {
                mView.unsuccessfulRegister(RegisterActivity.EMAIL_TAKEN);
            } else if (successCode == 3) {
                mView.unsuccessfulRegister(RegisterActivity.USERNAME_TAKEN);
            } else if (successCode == 1) {
                mView.successfulRegister(mUser);
            }
        }
    }
}
