package com.example.android.myfitnessapp.entities;

import java.util.Calendar;

/**
 * Created by John on 12. 1. 2016.
 */
public class MainListItem {

    private int dbID;
    private String mName;
    private int mSize;
    private int mValue;
    private int mSValue;
    private boolean foodItem;
    private Calendar mDate;

    public MainListItem() {
    }

    public MainListItem(String mName, int mSize, int mValue, int mSValue) {
        this.mName = mName;
        this.mSize = mSize;
        this.mValue = mValue;
        this.mSValue = mSValue;
        this.foodItem = true;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public int getmSize() {
        return mSize;
    }

    public void setmSize(int mSize) {
        this.mSize = mSize;
    }

    public int getmValue() {
        return mValue;
    }

    public void setmValue(int mValue) {
        this.mValue = mValue;
    }

    public int getmSValue() {
        return mSValue;
    }

    public void setmSValue(int mSValue) {
        this.mSValue = mSValue;
    }

    public boolean isFoodItem() {
        return foodItem;
    }

    public void setFoodItem(boolean foodItem) {
        this.foodItem = foodItem;
    }

    public int getDbID() {
        return dbID;
    }

    public void setDbID(int dbID) {
        this.dbID = dbID;
    }

    public Calendar getmDate() {
        return mDate;
    }

    public void setmDate(Calendar mDate) {
        this.mDate = mDate;
    }
}
