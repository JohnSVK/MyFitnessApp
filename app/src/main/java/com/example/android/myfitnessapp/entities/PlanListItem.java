package com.example.android.myfitnessapp.entities;

import android.widget.ImageView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by John on 6. 4. 2016.
 */
public class PlanListItem {

    private int dbID;
    private String mName;
    private int mDuration;
    private Calendar mDate;
    private int mWeather;
    private ImageView mWeatherImage;

    public PlanListItem() {
    }

    public PlanListItem(String mName, int mDuration, Calendar mDate, int mWeather, ImageView mWeatherImage) {
        this.mName = mName;
        this.mDuration = mDuration;
        this.mDate = mDate;
        this.mWeather = mWeather;
        this.mWeatherImage = mWeatherImage;
    }

    public int getDbID() {
        return dbID;
    }

    public void setDbID(int dbID) {
        this.dbID = dbID;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public int getmDuration() {
        return mDuration;
    }

    public void setmDuration(int mDuration) {
        this.mDuration = mDuration;
    }

    public Calendar getmDate() {
        return mDate;
    }

    public void setmDate(Calendar mDate) {
        this.mDate = mDate;
    }

    /*
    public void setmDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = null;
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.mDate = date;
    }*/

    public int getmWeather() {
        return mWeather;
    }

    public void setmWeather(int mWeather) {
        this.mWeather = mWeather;
    }

    public ImageView getmWeatherImage() {
        return mWeatherImage;
    }

    public void setmWeatherImage(ImageView mWeatherImage) {
        this.mWeatherImage = mWeatherImage;
    }
}
