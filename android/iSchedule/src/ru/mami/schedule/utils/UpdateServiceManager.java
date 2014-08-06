package ru.mami.schedule.utils;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;

import ru.mami.schedule.R;

public class UpdateServiceManager {
    private static UpdateServiceManager instance;
    private UpdateServiceManagerState currentState;

    private PendingIntent updateServiceIntent;
    private AlarmManager alarmManager;

    Context context = MobileApplication.getContext();
    SharedPreferences sharedPreference = PreferenceManager.getDefaultSharedPreferences(context);
    private int delay = Integer.parseInt(sharedPreference.getString(context.getString(R.string.pref_update_delay_id), "10"));

    private UpdateServiceManager() {

    }

    public static UpdateServiceManager getInstance() {
        if (instance == null)
            instance = new UpdateServiceManager();
        return instance;
    }

    public void updateDelay(int value) {
        Log.i(getClass().getSimpleName(), "Set up new delay time: " + value);
        delay = value;
        stopService();
        startService();
    }

    public void startService() {
        if (currentState == UpdateServiceManagerState.RUN)
            return;

        Log.i(getClass().getSimpleName(), "Start service");
        currentState = UpdateServiceManagerState.RUN;
        Calendar calendar = Calendar.getInstance();
        Intent serviceIntent = new Intent(context, UpdateService.class);
        updateServiceIntent = PendingIntent.getService(context, 0, serviceIntent,
                0);

        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), delay * 1000, updateServiceIntent);
    }

    public void stopService() {
        if (currentState == UpdateServiceManagerState.WAIT)
            return;

        Log.i(getClass().getSimpleName(), "Stop services");
        currentState = UpdateServiceManagerState.WAIT;
        updateServiceIntent.cancel();
        alarmManager.cancel(updateServiceIntent);
    }
}


enum UpdateServiceManagerState {
    RUN,
    WAIT
}
