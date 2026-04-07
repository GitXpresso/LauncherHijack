package com.gitxpresso.launcherhijack;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ServiceMan {
    public static void Start(Context c) {
        Intent i = new Intent(c, HomeButtonService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            c.startForegroundService(i);
        } else {
            c.startService(i);
        }
    }
}