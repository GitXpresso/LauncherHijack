package com.gitxpresso.launcherhijack;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class HomeButtonService extends Service {
    private BroadcastReceiver homeWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel("LH", "Hijack", NotificationManager.IMPORTANCE_MIN);
            getSystemService(NotificationManager.class).createNotificationChannel(chan);
        }
        startForeground(1, new NotificationCompat.Builder(this, "LH")
                .setContentTitle("Hijack Active")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build());

        homeWatcher = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {
                    String reason = intent.getStringExtra("reason");
                    if ("homekey".equals(reason)) HomePress.Perform(context);
                }
            }
        };
        registerReceiver(homeWatcher, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { return START_STICKY; }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        if (homeWatcher != null) unregisterReceiver(homeWatcher);
        super.onDestroy();
    }
}