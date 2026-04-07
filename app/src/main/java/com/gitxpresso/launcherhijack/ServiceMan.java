package com.gitxpresso.launcherhijack;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.util.List;

public class ServiceMan {
    private static Intent mServiceIntent;

    public static void Start(Context c) {
        mServiceIntent = new Intent(c, HomeButtonService.class);
        if (!isMyServiceRunning(HomeButtonService.class, c)) {
            c.startService(mServiceIntent);
        }
    }

    public static void Stop(Context c) {
        if (mServiceIntent != null) {
            c.stopService(mServiceIntent);
        }
    }

    public static void StartSlow(final Context c) {
        new android.os.Handler().postDelayed(() -> Start(c), 1000);
    }

    private static boolean isMyServiceRunning(Class<?> serviceClass, Context c) {
        ActivityManager manager = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}