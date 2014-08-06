package ru.mami.schedule.utils;

import android.app.Application;
import android.content.Context;
import android.util.Log;

// static way to get context
public class MobileApplication extends Application  {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        MobileApplication.context = getApplicationContext();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.i(getClass().getSimpleName(), "onTerminate()");
        UpdateServiceManager.getInstance().stopService();
    }

    public static Context getContext() {
        return MobileApplication.context;
    }
}
