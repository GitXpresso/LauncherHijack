package com.gitxpresso.launcherhijack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BroadcastReceiverOnBootComplete extends BroadcastReceiver {
    
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (action == null) return;

        Log.d(TAG, "Received broadcast: " + action);

        // Check for both standard and HTC/Xiaomi quickboot actions
        if (action.equals(Intent.ACTION_BOOT_COMPLETED) || 
            action.equals("android.intent.action.QUICKBOOT_POWERON")) {
            
            Log.i(TAG, "Boot completed detected. Starting Hijack Service...");
            
            // Start the background service management
            ServiceMan.Start(context);
        }
        
        // If an app was removed, we refresh the state
        if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
            Log.i(TAG, "Package removed. Refreshing service.");
            ServiceMan.Stop(context);
            ServiceMan.Start(context);
        }
    }
}