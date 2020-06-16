package com.polover.digipub;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences mSharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isLaunchAtStartupEnabled = mSharedPref.getBoolean("key_startup_launch", true);
        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED) && isLaunchAtStartupEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(new Intent(context, ForegroundService.class)
                        .setAction(Constants.ACTION.STARTFOREGROUND_ACTION));
            } else {
                context.startService(new Intent(context, ForegroundService.class)
                        .setAction(Constants.ACTION.STARTFOREGROUND_ACTION));
            }
        }
    }
}
