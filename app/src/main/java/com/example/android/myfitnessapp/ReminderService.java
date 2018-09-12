package com.example.android.myfitnessapp;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.android.myfitnessapp.activities.AddPlanActivity;
import com.example.android.myfitnessapp.activities.PlanActivity;

import junit.framework.Test;

/**
 * Created by John on 18. 5. 2016.
 */
public class ReminderService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        String data = (String) intent.getStringExtra("data");

        Intent resultIntent = new Intent(this, PlanActivity.class);
        resultIntent.putExtra("completed", true);

        long[] pattern = {0, 300, 0};
        PendingIntent pi = PendingIntent.getActivity(this, 01234, resultIntent, 0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle(data)
                .setContentText(getString(R.string.notification_text))
                .setVibrate(pattern)
                .setAutoCancel(true);

        mBuilder.setContentIntent(pi);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(01234, mBuilder.build());

        return flags;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /*Intent intent = new Intent(this, PlanActivity.class);
        long[] pattern = {0, 300, 0};
        PendingIntent pi = PendingIntent.getActivity(this, 01234, intent, 0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_text))
                .setVibrate(pattern)
                .setAutoCancel(true);

        mBuilder.setContentIntent(pi);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(01234, mBuilder.build());*/
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        Log.e("ReminderService","Alam Services Destroyed");
        super.onDestroy();
    }


}
