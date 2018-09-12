package com.example.android.myfitnessapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.util.Calendar;

/**
 * Created by John on 29. 4. 2016.
 */
public class TimeDialogFragment extends DialogFragment{

    public static String TAG = "DateDialogFragment";

    static Context sContext;
    static Calendar sDate;
    static TimeDialogFragmentListener sListener;

    private TimePickerDialog.OnTimeSetListener timeSetListener =
            new TimePickerDialog.OnTimeSetListener() {

                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                    //create new Calendar object for date chosen
                    //this is done simply combine the three args into one
                    Calendar newDate = Calendar.getInstance();
                    newDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    newDate.set(Calendar.MINUTE, minute);
                    //call back to the DateDialogFragment listener
                    sListener.timeDialogFragmentDataSet(newDate);

                }
            };

    public static TimeDialogFragment newInstance(Context context, int titleResource, Calendar date){
        TimeDialogFragment dialog  = new TimeDialogFragment();

        sContext = context;
        sDate = date;

        Bundle args = new Bundle();
        args.putInt("title", titleResource);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new TimePickerDialog(sContext, timeSetListener, sDate.get(Calendar.HOUR_OF_DAY), sDate.get(Calendar.MINUTE), true);
    }

    public void setTimeDialogFragmentListener(TimeDialogFragmentListener listener){
        sListener = listener;
    }

}
