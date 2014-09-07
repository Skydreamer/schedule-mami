package ru.mami.schedule.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Start service when device has booted
            UpdateServiceManager.getInstance().startService();
        }
    }

}
