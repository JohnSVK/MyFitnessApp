package com.example.android.myfitnessapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.myfitnessapp.entities.MainListItem;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by John on 12. 1. 2016.
 */
public class MainListAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<MainListItem> mItems;

    public MainListAdapter(Context context, ArrayList<MainListItem> listItems) {
        this.mContext = context;
        this.mItems = listItems;
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

        if(mItems.isEmpty())
            return null;

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.main_list_item, parent, false);

        TextView itemName = (TextView) rowView.findViewById(R.id.main_list_item_name);
        TextView itemSize = (TextView) rowView.findViewById(R.id.main_list_item_size);
        TextView itemValue = (TextView) rowView.findViewById(R.id.main_list_item_value);
        TextView itemDate = (TextView) rowView.findViewById(R.id.main_list_item_date);
        TextView itemSValue = (TextView) rowView.findViewById(R.id.main_list_item_svalue);

        String itemTypeSizeUnit = "g";
        if(!mItems.get(position).isFoodItem()) {
            itemSValue.setLayoutParams(new LinearLayout.LayoutParams(0, 0));

            itemTypeSizeUnit = "min";
        }

        String name = mItems.get(position).getmName();
        String size = String.valueOf(mItems.get(position).getmSize()) +" "+ itemTypeSizeUnit;
        String value = String.valueOf(mItems.get(position).getmValue()) + " kcal";

        Calendar calendar = mItems.get(position).getmDate();
        /*String date = String.valueOf(calendar.get(Calendar.DATE) + "." + calendar.get(Calendar.MONTH)
                + "." + calendar.get(Calendar.YEAR));*/

        itemName.setText(name);
        itemSize.setText(size);
        itemValue.setText(value);
        //itemDate.setText(date);

        if (mItems.get(position).isFoodItem()) {
            String sValue = String.valueOf(mItems.get(position).getmSValue()) + " " + parent.getContext().getString(R.string.main_list_svalue_unit);

            itemSValue.setText(sValue);
        }

        return rowView;
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public ArrayList<MainListItem> getmItems() {
        return mItems;
    }

    public void setmItems(ArrayList<MainListItem> mItems) {
        this.mItems = mItems;
    }
}
