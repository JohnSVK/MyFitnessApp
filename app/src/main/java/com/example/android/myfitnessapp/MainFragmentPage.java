package com.example.android.myfitnessapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.myfitnessapp.activities.DetailActivity;
import com.example.android.myfitnessapp.entities.MainListItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by John on 24. 4. 2016.
 */
/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragmentPage extends Fragment {
    /**
     * The fragment argument representing the section number for this fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_SECTION_DATESTR = "section_datestr";

    private DBHelper mDB;

    private LinearLayout layout;

    private int sectionNum;
    private String sectionDateStr;

    private Calendar mDate = new GregorianCalendar();

    private ArrayList<MainListItem> mList;
    private MainListAdapter mListAdapter;

    private Context mContext;

    private String mUser;

    public MainFragmentPage() {
    }

    /**
     * Returns a new instance of this fragment for the given section number.
     */
    public static MainFragmentPage newInstance(int sectionNumber, String sectionDate) {
        MainFragmentPage fragment = new MainFragmentPage();

        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putString(ARG_SECTION_DATESTR, sectionDate);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.main_fragment_page, container, false);
        layout = (LinearLayout) rootView;

        //init context
        mContext = this.getContext();

        SharedPreferences pref = mContext.getSharedPreferences(getString(R.string.preferences_session), Context.MODE_PRIVATE);
        mUser = pref.getString(getString(R.string.preferences_session_user), null);

        // init databazy
        mDB = new DBHelper(this.getContext());

        // List inicializacia
        mList = new ArrayList<>();
        mListAdapter = new MainListAdapter(this.getContext(), mList);

        ListView listView = (ListView) layout.findViewById(R.id.main_fragment_listview);
        listView.setAdapter(mListAdapter);
        listView.setClickable(true);
        listView.setOnItemClickListener(new ListItemClickListener());


        sectionNum = getArguments().getInt(ARG_SECTION_NUMBER);
        sectionDateStr = getArguments().getString(ARG_SECTION_DATESTR);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = new Date();
        try {
             date = sdf.parse(sectionDateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        new GetUserEntries().execute(calendar);


        return rootView;
    }


    private class ListItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            TextView nameView = (TextView) view.findViewById(R.id.main_list_item_name);
            String name = nameView.getText().toString();
            TextView sizeView = (TextView) view.findViewById(R.id.main_list_item_size);
            String size = sizeView.getText().toString();
            TextView valueView = (TextView) view.findViewById(R.id.main_list_item_value);
            String value = valueView.getText().toString();

            int idToRemove = mList.get(position).getDbID();

            Bundle bundle = new Bundle();
            bundle.putInt("id", idToRemove);
            bundle.putString("name", name);
            bundle.putString("size", size);
            bundle.putString("value", value);


            Intent detailIntent = new Intent();
            detailIntent.setClass(mContext, DetailActivity.class);
            detailIntent.putExtras(bundle);

            startActivity(detailIntent);
            //mContext.finish();
        }
    }

    private class GetUserEntries extends AsyncTask<Calendar, Void, ArrayList<MainListItem>> {

        @Override
        protected ArrayList<MainListItem> doInBackground(Calendar... params) {
            ArrayList<MainListItem> entries = new ArrayList<>();

            //if (mDB != null)
            entries = mDB.getAllEntries(mUser, params[0]);

            return entries;
        }

        @Override
        protected void onPostExecute(ArrayList<MainListItem> entries) {

            mList = entries;

            if (mList != null && !mList.isEmpty()) {
                mListAdapter.setmItems(mList);
                mListAdapter.notifyDataSetChanged();

                Log.v("MainActivity", "Entries successfully loaded");
            } else {
                mListAdapter.setmItems(new ArrayList<MainListItem>());
                mListAdapter.notifyDataSetChanged();

                Log.e("MainActivity", "NULL ENTRIES");
            }

        }
    }
}
