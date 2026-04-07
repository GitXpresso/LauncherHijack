package com.gitxpresso.launcherhijack;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import static android.content.Context.MODE_PRIVATE;

public class HomePress {
    private static long LastActivate = 0;

    public static Intent GetDesiredIntent(Context c) {
        SharedPreferences settings = c.getSharedPreferences("LauncherHijack", MODE_PRIVATE);
        String pkg = settings.getString("ChosenLauncher", "com.teslacoilsw.launcher");
        String cls = settings.getString("ChosenLauncherName", "com.teslacoilsw.launcher.Launcher");

        ComponentName componentName = new ComponentName(pkg, cls);
        Intent i = new Intent(Intent.ACTION_MAIN);

        i.addCategory(Intent.CATEGORY_LAUNCHER);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
                 | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS 
                 | Intent.FLAG_ACTIVITY_CLEAR_TOP 
                 | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        i.setComponent(componentName);

        return i;
    }

    public static void Perform(Context c) {
        long time = System.currentTimeMillis();
        // Prevent accidental rapid-fire launches
        if (time - LastActivate < 250) {
            return;
        }
        LastActivate = time;

        try {
            Intent i = GetDesiredIntent(c);
            c.startActivity(i);
        } catch (Exception e) {
            Log.e("HomePress", "Failed to launch chosen launcher: " + e.getMessage());
        }
    }
}