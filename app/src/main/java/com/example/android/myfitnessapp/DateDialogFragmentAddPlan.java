package com.example.android.myfitnessapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Created by John on 20. 4. 2016.
 */
public class DateDialogFragmentAddPlan extends DialogFragment {

    public static String TAG = "DateDialogFragment";

    static Context sContext;
    static Calendar sDate;
    static DateDialogFragmentListener sListener;

    private DatePickerDialog.OnDateSetListener dateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {

                    //create new Calendar object for date chosen
                    //this is done simply combine the three args into one
                    Calendar newDate = Calendar.getInstance();
                    newDate.set(year, monthOfYear, dayOfMonth);
                    //call back to the DateDialogFragment listener
                    sListener.dateDialogFragmentDataSet(newDate);

                }
            };

    public static DateDialogFragmentAddPlan newInstance(Context context, int titleResource, Calendar date){
        DateDialogFragmentAddPlan dialog  = new DateDialogFragmentAddPlan();

        sContext = context;
        sDate = date;

        Bundle args = new Bundle();
        args.putInt("title", titleResource);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(sContext, dateSetListener, sDate.get(Calendar.YEAR), sDate.get(Calendar.MONTH), sDate.get(Calendar.DAY_OF_MONTH));
        DatePicker datePicker = datePickerDialog.getDatePicker();

        datePicker.setMinDate(Calendar.getInstance().getTimeInMillis());

        return datePickerDialog;
    }

    public void setDateDialogFragmentListener(DateDialogFragmentListener listener){
        sListener = listener;
    }


}
