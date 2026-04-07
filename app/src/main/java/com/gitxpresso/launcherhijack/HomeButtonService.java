package com.gitxpresso.launcherhijack;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

public class HomeButtonService extends Service {
    private WindowManager wm;
    private View layout;

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        layout = LayoutInflater.from(this).inflate(R.layout.service_layout, null);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                1, 1,
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O ?
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        wm.addView(layout, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (layout != null) wm.removeView(layout);
        ServiceMan.Stop(getApplicationContext());
        ServiceMan.StartSlow(getApplicationContext());
    }
}