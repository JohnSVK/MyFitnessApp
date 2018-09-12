package com.example.android.myfitnessapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.Time;
import android.util.Log;

import com.example.android.myfitnessapp.entities.MainListItem;
import com.example.android.myfitnessapp.entities.PlanListItem;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;

import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.PointValue;

/**
 * Created by John on 25. 1. 2016.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MyFitnessApp.db";

    public static final String ENTRIES_TABLE_NAME = "entries";
    public static final String ENTRIES_COLUMN_ID = "id";
    public static final String ENTRIES_COLUMN_USER = "user";
    public static final String ENTRIES_COLUMN_NAME = "name";
    public static final String ENTRIES_COLUMN_SIZE = "size";
    public static final String ENTRIES_COLUMN_VALUE = "value";
    public static final String ENTRIES_COLUMN_SVALUE = "svalue";
    public static final String ENTRIES_COLUMN_TYPE = "type";
    public static final String ENTRIES_COLUMN_DATE = "date";

    public static final String PLAN_TABLE_NAME = "planned_activities";
    public static final String PLAN_COLUMN_ID = "id";
    public static final String PLAN_COLUMN_USER = "user";
    public static final String PLAN_COLUMN_NAME = "name";
    public static final String PLAN_COLUMN_DURATION = "duration";
    public static final String PLAN_COLUMN_DATE = "date";
    public static final String PLAN_COLUMN_WEATHER = "weather";
    public static final String PLAN_COLUMN_VALUE = "value";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ENTRIES_TABLE_NAME + " (" +
                ENTRIES_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                ENTRIES_COLUMN_USER + " TEXT, " +
                ENTRIES_COLUMN_NAME + " TEXT, " +
                ENTRIES_COLUMN_SIZE + " INTEGER, " +
                ENTRIES_COLUMN_VALUE + " INTEGER, " +
                ENTRIES_COLUMN_SVALUE + " INTEGER, " +
                ENTRIES_COLUMN_TYPE + " INTEGER, " +
                ENTRIES_COLUMN_DATE + " DATETIME DEFAULT CURRENT_DATE)");

        db.execSQL("CREATE TABLE " + PLAN_TABLE_NAME + " (" +
                PLAN_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                PLAN_COLUMN_USER + " TEXT, " +
                PLAN_COLUMN_NAME + " TEXT, " +
                PLAN_COLUMN_DURATION + " INTEGER, " +
                PLAN_COLUMN_DATE + " DATETIME, " +
                PLAN_COLUMN_WEATHER + " INTEGER, " +
                PLAN_COLUMN_VALUE + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ENTRIES_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PLAN_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertEntry(String user, String name, int size, int value, int sValue, int type) {
        SimpleDateFormat date = new SimpleDateFormat("dd MM EEEE");
        Time time = new Time();
        time.setToNow();

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(ENTRIES_COLUMN_USER, user);
        contentValues.put(ENTRIES_COLUMN_NAME, name);
        contentValues.put(ENTRIES_COLUMN_SIZE, size);
        contentValues.put(ENTRIES_COLUMN_VALUE, value);
        contentValues.put(ENTRIES_COLUMN_SVALUE, sValue);
        contentValues.put(ENTRIES_COLUMN_TYPE, type);
        //contentValues.put(ENTRIES_COLUMN_DATE, date.format(time));

        db.insert(ENTRIES_TABLE_NAME, null, contentValues);

        db.close();

        return true;
    }

    public boolean insertPlannedActivity(String user, String name, int duration, Calendar date, int weather, int value) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(PLAN_COLUMN_USER, user);
        contentValues.put(PLAN_COLUMN_NAME, name);
        contentValues.put(PLAN_COLUMN_DURATION, duration);
        contentValues.put(PLAN_COLUMN_DATE, dateFormat.format(date.getTime()));
        contentValues.put(PLAN_COLUMN_WEATHER, weather);
        contentValues.put(PLAN_COLUMN_VALUE, value);
       // Log.e("DB", ""+ dateFormat.format(date.getTime()));

        db.insert(PLAN_TABLE_NAME, null, contentValues);

        db.close();

        return true;
    }


    public boolean updateEntry(Integer id, String name, int size, int value, int type) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(ENTRIES_COLUMN_NAME, name);
        contentValues.put(ENTRIES_COLUMN_SIZE, size);
        contentValues.put(ENTRIES_COLUMN_VALUE, value);
        contentValues.put(ENTRIES_COLUMN_TYPE, type);

        db.update(ENTRIES_TABLE_NAME, contentValues, ENTRIES_COLUMN_ID + " = ?", new String[]{Integer.toString(id)});

        return true;
    }


    public Integer removeEntry(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(ENTRIES_TABLE_NAME, ENTRIES_COLUMN_ID + " = ?", new String[]{Integer.toString(id)});
    }

    public Integer removePlannedActivity(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(PLAN_TABLE_NAME, PLAN_COLUMN_ID + " = ?", new String[]{Integer.toString(id)});
    }

    public ArrayList<MainListItem> getAllEntries(String user, Calendar date) {
        ArrayList<MainListItem> entries = new ArrayList<>();

        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor result;

        result = db.rawQuery("SELECT * FROM " + ENTRIES_TABLE_NAME +
                " WHERE " + ENTRIES_COLUMN_USER + "= ? AND " +
                " date(" + ENTRIES_COLUMN_DATE + ") BETWEEN" +
                " date('" + sdf.format(date.getTime()) + "')" + " AND " +
                " date('" + sdf.format(date.getTime()) + "')", new String[]{user});

        if (result.moveToFirst()) {
            do {
                MainListItem entry = new MainListItem();
                entry.setmName(result.getString(result.getColumnIndex(ENTRIES_COLUMN_NAME)));
                entry.setmSize(result.getInt(result.getColumnIndex(ENTRIES_COLUMN_SIZE)));
                entry.setmValue(result.getInt(result.getColumnIndex(ENTRIES_COLUMN_VALUE)));
                entry.setFoodItem((result.getInt(result.getColumnIndex(ENTRIES_COLUMN_TYPE)) != 0));
                entry.setDbID(result.getInt(result.getColumnIndex(ENTRIES_COLUMN_ID)));

                if (entry.isFoodItem()) {
                    entry.setmSValue(result.getInt(result.getColumnIndex(ENTRIES_COLUMN_SVALUE)));
                } else {

                }

                entries.add(entry);
            } while (result.moveToNext());
        }

        result.close();
        db.close();

        return entries;
    }

    public ArrayList<PlanListItem> getAllPlannedActivities(String user) {
        ArrayList<PlanListItem> plannedActivities = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor result = db.rawQuery("SELECT * FROM " + PLAN_TABLE_NAME +
                " WHERE " + PLAN_COLUMN_USER + "=" + user, null);

        if (result.moveToFirst()) {
            do {
                PlanListItem plannedActivity = new PlanListItem();
                plannedActivity.setmName(result.getString(result.getColumnIndex(PLAN_COLUMN_NAME)));
                plannedActivity.setmDuration(result.getInt(result.getColumnIndex(PLAN_COLUMN_DURATION)));

                String dateStr = result.getString(result.getColumnIndex(PLAN_COLUMN_DATE));
                //Log.e("DB", ""+dateStr);

                Date date = new Date();
                try {
                    date = sdf.parse(dateStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                plannedActivity.setmDate(calendar);
                plannedActivity.setmWeather(result.getInt(result.getColumnIndex(PLAN_COLUMN_WEATHER)));
                plannedActivity.setDbID(result.getInt(result.getColumnIndex(ENTRIES_COLUMN_ID)));

                plannedActivities.add(plannedActivity);
            } while (result.moveToNext());
        }
        //Log.e("DB", "" + result.getCount());

        result.close();
        db.close();

        return plannedActivities;
    }

    public ArrayList<PlanListItem> getActPlannedActivities(String user) {
        ArrayList<PlanListItem> plannedActivities = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor result = db.rawQuery("SELECT * FROM " + PLAN_TABLE_NAME +
                " WHERE " + PLAN_COLUMN_USER + "= ? AND " +
                " datetime(" + PLAN_COLUMN_DATE + ") > datetime('now')", new String[]{user});

        if (result.moveToFirst()) {
            do {
                PlanListItem plannedActivity = new PlanListItem();
                plannedActivity.setmName(result.getString(result.getColumnIndex(PLAN_COLUMN_NAME)));
                plannedActivity.setmDuration(result.getInt(result.getColumnIndex(PLAN_COLUMN_DURATION)));

                String dateStr = result.getString(result.getColumnIndex(PLAN_COLUMN_DATE));
                //Log.e("DB", ""+dateStr);

                Date date = new Date();
                try {
                    date = sdf.parse(dateStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                plannedActivity.setmDate(calendar);
                plannedActivity.setmWeather(result.getInt(result.getColumnIndex(PLAN_COLUMN_WEATHER)));
                plannedActivity.setDbID(result.getInt(result.getColumnIndex(ENTRIES_COLUMN_ID)));

                plannedActivities.add(plannedActivity);
            } while (result.moveToNext());
        }
        //Log.e("DB", "" + result.getCount());

        result.close();
        db.close();

        return plannedActivities;
    }

    public int getTotalGainedValues(String user, Calendar date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor result = db.rawQuery("SELECT SUM(" + ENTRIES_COLUMN_VALUE + ") FROM " + ENTRIES_TABLE_NAME +
                " WHERE " + ENTRIES_COLUMN_TYPE + "=1" + " AND " +
                ENTRIES_COLUMN_USER + "= ? AND " +
                " date(" + ENTRIES_COLUMN_DATE + ") BETWEEN" +
                " date('" + sdf.format(date.getTime()) + "')" + " AND " +
                " date('" + sdf.format(date.getTime()) + "')", new String[]{user});

        if (result.moveToFirst()) {
            int value =  result.getInt(0);
            result.close();
            db.close();

            return value;
        } else {
            return 0;
        }

    }

    public LinkedHashMap<Date, Integer> getGainedDailyEnergyValues(String user) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        LinkedHashMap<Date, Integer> values = new LinkedHashMap<>();

        Date date = new Date();
        int value;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor result = db.rawQuery("SELECT date(" + ENTRIES_COLUMN_DATE + "), SUM(" + ENTRIES_COLUMN_VALUE + ")" +
                " FROM " + ENTRIES_TABLE_NAME +
                " WHERE " + ENTRIES_COLUMN_TYPE + "=1 AND " +
                ENTRIES_COLUMN_USER + "= ?" +
                " GROUP BY date(" + ENTRIES_COLUMN_DATE + ") ORDER BY date(" + ENTRIES_COLUMN_DATE + ") ASC", new String[]{user});


        if (result.moveToFirst()) {
            do {
                try {
                    date = sdf.parse(result.getString(0));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                value = result.getInt(1);

                values.put(date, value);

            } while (result.moveToNext());

            result.close();
            db.close();

            return values;
        } else {
            return null;
        }

    }

    public int getTotalBurnedValues(String user, Calendar date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor result = db.rawQuery("SELECT SUM(" + ENTRIES_COLUMN_VALUE + ") FROM " + ENTRIES_TABLE_NAME +
                " WHERE " + ENTRIES_COLUMN_TYPE + "=0" + " AND " +
                ENTRIES_COLUMN_USER + "= ? AND " +
                " date(" + ENTRIES_COLUMN_DATE + ") BETWEEN" +
                " date('" + sdf.format(date.getTime()) + "')" + " AND " +
                " date('" + sdf.format(date.getTime()) + "')", new String[]{user});

        if (result.moveToFirst()) {
            int value = result.getInt(0);

            result.close();
            db.close();

            return value;
        } else {
            return 0;
        }
    }

    public LinkedHashMap<Date, Integer> getBurnedDailyEnergyValues(String user) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        LinkedHashMap<Date, Integer> values = new LinkedHashMap<>();

        Date date = new Date();
        int value;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor result = db.rawQuery("SELECT date(" + ENTRIES_COLUMN_DATE + "), SUM(" + ENTRIES_COLUMN_VALUE + ")" +
                " FROM " + ENTRIES_TABLE_NAME +
                " WHERE " + ENTRIES_COLUMN_TYPE + "=0 AND " +
                ENTRIES_COLUMN_USER + "= ?" +
                " GROUP BY date(" + ENTRIES_COLUMN_DATE + ") ORDER BY date(" + ENTRIES_COLUMN_DATE + ") ASC", new String[]{user});

        //Log.v("STATS", ""+ result.getCount());
        if (result.moveToFirst()) {
            do {
                try {
                    date = sdf.parse(result.getString(0));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                value = result.getInt(1);

                values.put(date, value);

            } while (result.moveToNext());

            result.close();
            db.close();

            return values;
        } else {
            return null;
        }

    }

    public int getTotalGainedSValues(String user, Calendar date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor result = db.rawQuery("SELECT SUM(" + ENTRIES_COLUMN_SVALUE + ") FROM " + ENTRIES_TABLE_NAME +
                " WHERE " + ENTRIES_COLUMN_TYPE + "=1" + " AND " +
                ENTRIES_COLUMN_USER + "= ? AND " +
                " date(" + ENTRIES_COLUMN_DATE + ") BETWEEN" +
                " date('" + sdf.format(date.getTime()) + "')" + " AND " +
                " date('" + sdf.format(date.getTime()) + "')", new String[]{user});

        if (result.moveToFirst()) {
            int value = result.getInt(0);

            result.close();
            db.close();

            return value;
        } else {
            return 0;
        }

    }

    public LinkedHashMap<Date, Integer> getGainedDailyCarbValues(String user) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        LinkedHashMap<Date, Integer> values = new LinkedHashMap<>();

        Date date = new Date();
        int value;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor result = db.rawQuery("SELECT date(" + ENTRIES_COLUMN_DATE + "), SUM(" + ENTRIES_COLUMN_SVALUE + ")" +
                " FROM " + ENTRIES_TABLE_NAME +
                " WHERE " + ENTRIES_COLUMN_TYPE + "=1 AND " +
                ENTRIES_COLUMN_USER + "= ?" +
                " GROUP BY date(" + ENTRIES_COLUMN_DATE + ") ORDER BY date(" + ENTRIES_COLUMN_DATE + ") ASC", new String[]{user});


        if (result.moveToFirst()) {
            do {
                try {
                    date = sdf.parse(result.getString(0));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                value = result.getInt(1);

                values.put(date, value);

            } while (result.moveToNext());

            result.close();
            db.close();

            return values;
        } else {
            return null;
        }

    }

    public LinkedHashMap<Date, Integer> getPlannedDailyValues(String user) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        LinkedHashMap<Date, Integer> values = new LinkedHashMap<>();

        Date date = new Date();
        int value;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor result = db.rawQuery("SELECT date(" + PLAN_COLUMN_DATE + "), SUM(" + PLAN_COLUMN_VALUE +")" +
                " FROM " + PLAN_TABLE_NAME +
                " WHERE " + PLAN_COLUMN_USER + "= ?" +
                " GROUP BY date(" + PLAN_COLUMN_DATE + ") ORDER BY date(" + PLAN_COLUMN_DATE + ") ASC", new String[]{user});


        if (result.moveToFirst()) {
            do {
                try {
                    date = sdf.parse(result.getString(0));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                value = result.getInt(1);

                values.put(date, value);

            } while (result.moveToNext());

            result.close();
            db.close();

            return values;
        } else {
            return null;
        }

    }

    public ArrayList<Date> getDatesWithEntries(String user) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        ArrayList<Date> dates = new ArrayList<>();

        Date date = new Date();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor result = db.rawQuery("SELECT date(" + ENTRIES_COLUMN_DATE + ")" +
                " FROM " + ENTRIES_TABLE_NAME +
                " WHERE " + ENTRIES_COLUMN_USER + "= ?" +
                " GROUP BY date(" + ENTRIES_COLUMN_DATE + ") ORDER BY date(" + ENTRIES_COLUMN_DATE + ") ASC", new String[]{user});

        //Log.v("STATS", ""+ result.getCount());
        if (result.moveToFirst()) {
            do {
                try {
                    date = sdf.parse(result.getString(0));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                dates.add(date);

            } while (result.moveToNext());

            result.close();
            db.close();

            return dates;
        } else {
            return null;
        }

    }

    public void removeEntryByValues(String name, String size, String value) {
        int id = -1;

        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + ENTRIES_COLUMN_ID + " FROM " + ENTRIES_TABLE_NAME +
                " WHERE " + ENTRIES_COLUMN_NAME + "='" + name + "'" + " AND " +
                ENTRIES_COLUMN_SIZE + "='" + size + "'" + " AND " +
                ENTRIES_COLUMN_VALUE + "='" + value + "'" + "" +
                " LIMIT 1";
        Log.e("DATABASE ERROR", ""+query);
        Cursor result = db.rawQuery(query, null);

        if (result.moveToFirst()) {
            id = result.getInt(0);
        } else {

        }

        db = this.getWritableDatabase();

        db.delete(ENTRIES_TABLE_NAME, ENTRIES_COLUMN_ID + " = ?", new String[]{Integer.toString(id)});
    }
}
