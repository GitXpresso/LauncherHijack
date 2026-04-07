package com.gitxpresso.launcherhijack;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

public class HomeButtonService extends Service {
    private WindowManager wm;
    private View layout;
    private BroadcastReceiver homeWatcher;
    private static final String TAG = "HomeButtonService";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        
        // Setup the overlay view (required for some system behaviors)
        try {
            layout = LayoutInflater.from(this).inflate(R.layout.service_layout, null);
            int layoutType = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ?
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    1, 1, layoutType,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

            params.gravity = Gravity.TOP | Gravity.LEFT;
            wm.addView(layout, params);
        } catch (Exception e) {
            Log.e(TAG, "Overlay creation failed: " + e.getMessage());
        }

        // Setup the BroadcastReceiver to detect Home button presses
        homeWatcher = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {
                    String reason = intent.getStringExtra("reason");
                    if (reason != null && (reason.equals("homekey") || reason.equals("recentapps"))) {
                        Log.d(TAG, "Home or Recents button pressed. Triggering Hijack...");
                        HomePress.Perform(getApplicationContext());
                    }
                }
            }
        };

        registerReceiver(homeWatcher, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (homeWatcher != null) {
            unregisterReceiver(homeWatcher);
        }
        if (wm != null && layout != null) {
            try {
                wm.removeView(layout);
            } catch (Exception e) {
                Log.e(TAG, "Error removing view: " + e.getMessage());
            }
        }
        
        // Ensure the service restarts
        ServiceMan.Stop(getApplicationContext());
        ServiceMan.StartSlow(getApplicationContext());
    }
}