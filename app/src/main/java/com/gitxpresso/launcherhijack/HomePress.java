package com.gitxpresso.launcherhijack;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class HomePress {
    private static long lastPress = 0;

    public static void Perform(Context c) {
        if (System.currentTimeMillis() - lastPress < 200) return;
        lastPress = System.currentTimeMillis();

        SharedPreferences settings = c.getSharedPreferences("LauncherHijack", Context.MODE_PRIVATE);
        String pkg = settings.getString("ChosenLauncher", "");
        String cls = settings.getString("ChosenLauncherName", "");

        if (pkg.isEmpty()) return;

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(new ComponentName(pkg, cls));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
                        Intent.FLAG_ACTIVITY_CLEAR_TOP | 
                        Intent.FLAG_ACTIVITY_NO_ANIMATION);
        
        try {
            c.startActivity(intent);
        } catch (Exception ignored) {}
    }
}