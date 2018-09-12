package com.example.android.myfitnessapp.presenters;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by John on 27. 3. 2016.
 */
public class LoginPresenter implements ILoginPresenter {

    private LoginActivity mView;

    private String mUser;

    public LoginPresenter(LoginActivity view) {
        this.mView = view;
    }

    @Override
    public void loginUser(String email, String password) {

        mUser = email;

        if (email.isEmpty()) {
            mView.unsuccessfulLogin(LoginActivity.EMAIL_REQUIRED_FIELD);
        } else if (!email.contains("@")) {
            mView.unsuccessfulLogin(LoginActivity.INVALID_EMAIL);
        } else if (password.isEmpty()) {
            mView.unsuccessfulLogin(LoginActivity.PASSWORD_REQUIRED_FIELD);
        } else if(password.length() < 4) {
            mView.unsuccessfulLogin(LoginActivity.PASSWORD_TOO_SHORT);
        } else {
            new LoginUser().execute(email, password);
        }
    }

    private class LoginUser extends AsyncTask<String, Void, String> {

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";

        // localhost url
        //private static final String SERVER_URL = "http://192.168.1.103:80/myfitnessapp/login.php/?";
        // remote server url
        private static final String SERVER_URL = "https://myapp-johnsvk.rhcloud.com/login.php/?";

        private static final String LOGIN_FAILED = "failed";

        @Override
        protected String doInBackground(String... params) {

            Uri builtUri = null;
            try {
                builtUri = Uri.parse(SERVER_URL).buildUpon()
                        .appendQueryParameter("email", URLEncoder.encode(params[0], "UTF-8"))
                        .appendQueryParameter("password", URLEncoder.encode(params[1], "UTF-8"))
                        .build();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try {
                URL url = new URL(builtUri.toString());
                Log.v("URL", url.toString());

                JSONObject json = JSONParser.getJSONFromUrl(url, "POST");

                if (json == null) {
                    Log.e("LoginActivity", "json object is null");

                    return null;
                }

                if (json.getInt(TAG_SUCCESS) == 1) {
                    return json.getString(TAG_MESSAGE);
                } else if (json.getInt(TAG_SUCCESS) == 0) {
                    return LOGIN_FAILED;
                }

            } catch (MalformedURLException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String jsonStr) {

            if (jsonStr == null) {
                mView.unsuccessfulLogin(LoginActivity.CONNECTION_ERROR);
            } else if (jsonStr.equals(LOGIN_FAILED)) {
                mView.unsuccessfulLogin(LoginActivity.WRONG_DATA);
            } else {
                mView.successfulLogin(mUser);
            }
        }
    }

    @Override
    public void getUserData() {

        new GetUserData().execute();
    }

    private class GetUserData extends AsyncTask<Void, Void, HashMap<String, Object>> {

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";
        private static final String TAG_DATA = "data";

        private static final String DATA_EMAIL = "email";
        private static final String DATA_USERNAME = "username";
        private static final String DATA_WEIGHT = "weight";
        private static final String DATA_HEIGHT = "height";
        private static final String DATA_LOCATION = "location";
        private static final String DATA_LOCATION_LAT = "location_lat";
        private static final String DATA_LOCATION_LON = "location_lon";

        // localhost url
        //private static final String SERVER_URL = "http://192.168.1.103:80/myfitnessapp/login.php/?";
        // remote server url
        private static final String SERVER_URL = "https://myapp-johnsvk.rhcloud.com/getuserdata.php/?";

        @Override
        protected HashMap<String, Object> doInBackground(Void... params) {

            Uri builtUri = Uri.parse(SERVER_URL).buildUpon()
                    .appendQueryParameter("email", mUser)
                    .build();

            try {
                URL url = new URL(builtUri.toString());

                JSONObject json = JSONParser.getJSONFromUrl(url, "POST");

                if (json == null) {
                    Log.e("LoginActivity", "json object is null");

                    return null;
                }

                Log.e("JSON DATA", "user: "+mUser + " " +json.getInt(TAG_SUCCESS));
                if (json.getInt(TAG_SUCCESS) == 1) {
                    HashMap<String, Object> data = new HashMap<>();

                    data.put(DATA_EMAIL, json.getJSONObject(TAG_DATA).getString(DATA_EMAIL));
                    data.put(DATA_USERNAME, json.getJSONObject(TAG_DATA).getString(DATA_USERNAME));
                    data.put(DATA_WEIGHT, json.getJSONObject(TAG_DATA).getInt(DATA_WEIGHT));
                    data.put(DATA_HEIGHT, json.getJSONObject(TAG_DATA).getInt(DATA_HEIGHT));
                    data.put(DATA_LOCATION_LAT, json.getJSONObject(TAG_DATA).getString(DATA_LOCATION_LAT));
                    data.put(DATA_LOCATION_LON, json.getJSONObject(TAG_DATA).getString(DATA_LOCATION_LON));

                    return data;
                } else if (json.getInt(TAG_SUCCESS) == 0) {
                    return null;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(HashMap<String, Object> data) {

            if (data == null) {
                mView.unsuccessfulDataLoad(LoginActivity.DATA_NULL);
            } else {
                mView.successfulDataLoad(data);
            }
        }
    }
}
