package com.gitxpresso.launcherhijack;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class AccServ extends AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getPackageName() != null && event.getPackageName().toString().equals("com.amazon.firelauncher")) {
            HomePress.Perform(this);
        }
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d("LH_Acc", "Accessibility Service Live");
        HomePress.Perform(this);
        ServiceMan.Start(this);
    }

    @Override
    public void onInterrupt() {}
}