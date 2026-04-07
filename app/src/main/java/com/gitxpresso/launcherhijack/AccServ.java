package com.gitxpresso.launcherhijack;

import android.accessibilityservice.AccessibilityService;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.util.Log;

public class AccServ extends AccessibilityService {
    private SettingsMan.SettingStore settings;
    private static final String TAG = "AccServ";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;

        if (settings == null) {
            settings = SettingsMan.GetSettings();
        }

        if (settings != null && settings.ApplicationOpenDetection) {
            CharSequence pkg = event.getPackageName();
            // Detect when the system launcher is about to appear
            if (pkg != null && pkg.toString().equals("com.amazon.firelauncher")) {
                Log.d(TAG, "System launcher detected via Accessibility. Hijacking...");
                HomePress.Perform(getApplicationContext());
            }
        }
    }

    @Override
    public boolean onKeyEvent(KeyEvent event) {
        if (settings == null) {
            settings = SettingsMan.GetSettings();
        }

        if (settings != null && settings.HardwareDetection) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_HOME) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    HomePress.Perform(getApplicationContext());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i(TAG, "Accessibility Service Connected");
        settings = SettingsMan.GetSettings();
        
        // Initial hijack on service connection
        HomePress.Perform(getApplicationContext());
    }

    @Override
    public void onInterrupt() {
        Log.i(TAG, "Accessibility Service Interrupted");
    }
}