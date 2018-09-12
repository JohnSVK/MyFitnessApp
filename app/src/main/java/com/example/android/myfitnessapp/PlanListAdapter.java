package com.example.android.myfitnessapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.myfitnessapp.entities.MainListItem;
import com.example.android.myfitnessapp.entities.PlanListItem;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by John on 6. 4. 2016.
 */
public class PlanListAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<PlanListItem> mItems;

    private ArrayList<String> mDailyForecastIcons;

    public PlanListAdapter(Context context, ArrayList<PlanListItem> listItems) {
        this.mContext = context;
        this.mItems = listItems;

        mDailyForecastIcons = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (mItems.isEmpty())
            return null;

        if (mDailyForecastIcons != null && !mDailyForecastIcons.isEmpty())
            loadListIcons();

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.plan_list_item, parent, false);

        TextView itemName = (TextView) rowView.findViewById(R.id.plan_list_item_name);
        TextView itemDate = (TextView) rowView.findViewById(R.id.plan_list_item_date);
        TextView itemDuration = (TextView) rowView.findViewById(R.id.plan_list_item_duration);
        FrameLayout itemWeatherLayout = (FrameLayout) rowView.findViewById(R.id.plan_list_item_weather);

        String name = mItems.get(position).getmName();
        String date = String.valueOf(sdf.format(mItems.get(position).getmDate().getTime()));
        String duration = String.valueOf(mItems.get(position).getmDuration()) + " min";
        ImageView weatherIcon = mItems.get(position).getmWeatherImage();

        itemName.setText(name);
        itemDate.setText(date);
        itemDuration.setText(duration);

        if (weatherIcon != null) {
            itemWeatherLayout.addView(weatherIcon);
        }

        return rowView;
    }

    private void loadListIcons() {
        Calendar calendar = Calendar.getInstance();
        String baseUrl = "http://api.openweathermap.org/img/w/";

        //Log.v("DATE", calendar.toString());

        for (PlanListItem item : mItems) {
            Calendar itemDate = item.getmDate();

            //Log.v("DATE", itemDate.toString());

            if (itemDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                    itemDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)) {
                ImageView weatherIcon = new ImageView(mContext);

                for (int i = 0; i < mDailyForecastIcons.size(); i++) {
                    if (itemDate.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH) + i) {
                        Picasso.with(mContext).load(baseUrl + mDailyForecastIcons.get(i) + ".png").into(weatherIcon);
                        item.setmWeatherImage(weatherIcon);
                    }
                }
            }
        }
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public ArrayList<PlanListItem> getmItems() {
        return mItems;
    }

    public void setmItems(ArrayList<PlanListItem> mItems) {
        this.mItems = mItems;
    }

    public ArrayList<String> getmDailyForecastIcons() {
        return mDailyForecastIcons;
    }

    public void setmDailyForecastIcons(ArrayList<String> mDailyForecastIcons) {
        this.mDailyForecastIcons = mDailyForecastIcons;
    }
}