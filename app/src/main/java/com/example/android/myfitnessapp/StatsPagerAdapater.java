package com.example.android.myfitnessapp;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by John on 9. 2. 2016.
 */
public class StatsPagerAdapater extends PagerAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;

    private LineChartView mLineChartViews[];

    public StatsPagerAdapater(Context context, LineChartView lineChartViews[]) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLineChartViews = lineChartViews;
    }

    @Override
    public int getCount() {
        return mLineChartViews.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        View itemView = mLayoutInflater.inflate(R.layout.stats_pager_item, container, false);

        LineChartView lineChartView = (LineChartView) itemView.findViewById(R.id.stats_pager_chart);
        LineChartView chartView = mLineChartViews[position];
        lineChartView.setLineChartData(chartView.getLineChartData());

        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }

    public void setmLineChartViews(LineChartView[] mLineChartViews) {
        this.mLineChartViews = mLineChartViews;
    }
}
