package com.gitxpresso.launcherhijack;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import com.jaredrummler.android.device.DeviceName;

public class AccServ extends AccessibilityService {

    static final String TAG = "AccServ";
    static boolean HomePressCanceled = false;
    static HomeWatcher homeWatcher;
    static String lastApp = ""; 
    static String lastClass = "";
    private SettingsMan.SettingStore settings;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        
        CharSequence pkg = event.getPackageName();
        CharSequence cls = event.getClassName();

        lastApp = (pkg != null) ? pkg.toString() : "";
        lastClass = (cls != null) ? cls.toString() : "";

        if (settings == null) {
            settings = SettingsMan.GetSettings();
        }

        if (!settings.ApplicationOpenDetection)
            return;

        if ("com.amazon.firelauncher".equals(lastApp)) {
            HomePress.Perform(getApplicationContext());
        }
    }

    @Override
    public void onInterrupt() {
        Log.v(TAG, "onInterrupt");
    }

    @Override
    public boolean onKeyEvent(KeyEvent event) {
        if (event == null || settings == null) return false;
        
        if (!settings.HardwareDetection)
            return false;

        int keyCode = event.getKeyCode();
        int action = event.getAction();

        switch (keyCode) {
            case KeyEvent.KEYCODE_HOME:
                if (action == KeyEvent.ACTION_UP) {
                    HomePressCanceled = false;
                } else if (action == KeyEvent.ACTION_DOWN && !HomePressCanceled) {
                    HomePress.Perform(getApplicationContext());
                    return true;
                }
                return false;

            case KeyEvent.KEYCODE_MENU:
                if (settings.MenuButtonOverride && action == KeyEvent.ACTION_DOWN) {
                    HomePressCanceled = true;
                }
                return false;
        }
        return false;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        settings = SettingsMan.GetSettings();
        MainActivity.SetContext(getApplicationContext());

        lastClass = "";
        lastApp = "";

        homeWatcher = new HomeWatcher(getApplicationContext());
        homeWatcher.setOnHomePressedListener(new OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                if (settings != null && settings.BroadcastRecieverDetection && !HomePressCanceled) {
                    boolean isRecents = (lastApp.equals("com.android.systemui") && 
                                       lastClass.contains("RecentsActivity"));
                                       
                    if (!settings.RecentAppOverride || !isRecents) {
                        Log.d("New Home", "Home Press Detected. LastApp: " + lastApp);
                        HomePress.Perform(getApplicationContext());
                    }
                }
            }

            @Override
            public void onRecentAppPressed() {}
        });
        
        try {
            homeWatcher.startWatch();
        } catch (Exception e) {
            Log.e(TAG, "Failed to start HomeWatcher: " + e.getMessage());
        }

        Log.v(TAG, "Launcher Hijack Service Started on " + DeviceName.getDeviceName());
        HomePress.Perform(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        if (homeWatcher != null) {
            homeWatcher.stopWatch();
        }
        super.onDestroy();
    }
}