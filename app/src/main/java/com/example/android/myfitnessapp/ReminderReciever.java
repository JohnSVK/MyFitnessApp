package com.example.android.myfitnessapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by John on 18. 5. 2016.
 */
public class ReminderReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String data = (String) intent.getStringExtra("data");

        Intent myIntent = new Intent(context, ReminderService.class);
        myIntent.putExtra("data", data);

        context.startService(myIntent);
    }
}
