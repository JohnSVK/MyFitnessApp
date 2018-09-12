package com.example.android.myfitnessapp;

import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by John on 18. 11. 2015.
 */
public class JSONParser {
    static HttpURLConnection urlConnection = null;
    static JSONObject jsonObject = null;
    static String jsonStr = "";

    public JSONParser() {
        //Log.e("JSONParser", "HALOOOOO");
    }

    public static JSONObject getJSONFromUrl(final URL url, String method) {
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(method);

            if (method.equals("POST")) {

                String query = url.getQuery();
                /*
                String query="email=" + URLEncoder.encode("value1","UTF-8")+
                        "password=" + URLEncoder.encode("value2","UTF-8");*/


                urlConnection.setFixedLengthStreamingMode(query.getBytes().length);
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");


                OutputStream outputStream = new BufferedOutputStream(urlConnection.getOutputStream());
                if (outputStream == null)
                    return null;

                PrintWriter out = new PrintWriter(outputStream);
                out.print(query);

                out.flush();
                out.close();
                outputStream.close();



            } else {
                // upload data
                String query = url.getQuery();

                urlConnection.setRequestProperty("Content-Type",
                        "application/json; charset=utf-8");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);

                OutputStream outputStream = new BufferedOutputStream(urlConnection.getOutputStream());
                if (outputStream == null)
                    return null;

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                if (writer == null)
                    return null;

                writer.write(query);

                writer.flush();
                writer.close();
                outputStream.close();
            }

            int responseCode = urlConnection.getResponseCode();
            if (responseCode >= 400 && responseCode <= 499) {
                Log.e("Bad response", "" + responseCode);
            } else {

                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                if (inputStream == null)
                    return null;

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                if (reader == null)
                    return null;

                StringBuffer buffer = new StringBuffer();

                String line = null;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                jsonStr = buffer.toString();

                reader.close();
                inputStream.close();
            }

        } catch (IOException e) {
            Log.e("JSONParser Buffer Error", "CONNECTION ERROR", e);

            return null;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }

        }

        if (jsonStr == null || jsonStr.isEmpty()) {
            Log.e("JSONParser", "jsonStr is null/empty");
        }
        //Log.e("JSONParser", jsonStr);

        try {
            jsonObject = new JSONObject(jsonStr);
        } catch (JSONException e) {
            Log.e("JSONParser", "Error parsing data");
        }

        return jsonObject;
    }
}
