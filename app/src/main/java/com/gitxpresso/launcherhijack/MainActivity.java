package com.gitxpresso.launcherhijack; // Corrected package

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.PowerManager;

import java.util.List;

public class MainActivity extends AppCompatActivity
{
    private static Context context;
    private ListView mListAppInfo;
    private MenuItem launcher, sysApps;
    private int prevSelectedIndex = 0;
    public final static int REQUEST_CODE = 5466;

    public static void SetContext(Context c) { if (context == null) context = c; }
    public static Context GetContext() { return context; }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        sysApps = menu.getItem(0);
        launcher = menu.getItem(1);
        launcher.setChecked(true);
        sysApps.setChecked(true);
        UpdateList();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.launcher:
                launcher.setChecked(!launcher.isChecked());
                if (launcher.isChecked()) sysApps.setChecked(true);
                UpdateList();
                break;
            case R.id.sysApps:
                sysApps.setChecked(!sysApps.isChecked());
                UpdateList();
                break;
            case R.id.help:
                OpenHelp();
                break;
            case R.id.donate:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/BaronKiko/LauncherHijack/blob/master/README.md#donations")));
                break;
            case R.id.settings:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void OpenHelp() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/BaronKiko/LauncherHijack/blob/master/HELP.md")));
    }

    private void UpdateList() {
        boolean sys = sysApps.isChecked();
        boolean l = launcher.isChecked();
        List<ResolveInfo> appInfo = Utilities.getInstalledApplication(this, l, sys);
        mListAppInfo = findViewById(R.id.lvApps);
        AppAdapter adapter = new AppAdapter(this, appInfo, getApplicationContext().getPackageManager());
        mListAppInfo.setAdapter(adapter);

        SharedPreferences settings = getSharedPreferences("LauncherHijack", MODE_PRIVATE);
        String selectedPackage = settings.getString("ChosenLauncher", "com.teslacoilsw.launcher");
        for (int i = 0; i < appInfo.size(); i++) {
            if (appInfo.get(i).activityInfo.packageName.equals(selectedPackage)) {
                prevSelectedIndex = i;
                mListAppInfo.setSelection(i);
                mListAppInfo.setItemChecked(i, true);
                break;
            }
        }
    }

    public boolean checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                try { startActivityForResult(intent, REQUEST_CODE); }
                catch(Exception e) { Toast.makeText(this, "Manual permission required", Toast.LENGTH_SHORT).show(); }
                return false;
            }
        }
        return true;
    }

    public static boolean isAccessibilityEnabled(Context context, String id) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> runningServices = am.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);
        for (AccessibilityServiceInfo service : runningServices) {
            if (id.equals(service.getId())) return true;
        }
        return false;
    }

    private void checkBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName())) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                try { startActivity(intent); } catch (Exception e) { Log.e("Main", e.getMessage()); }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SetContext(getApplicationContext());
        super.onCreate(savedInstanceState);

        if (getIntent() != null && getIntent().getBooleanExtra("fromBoot", false)) {
            finish();
            return;
        }

        setContentView(R.layout.activity_main); // Fixed R reference

        if (!isAccessibilityEnabled(context, getPackageName() + "/.AccServ")) {
            new AlertDialog.Builder(this)
                    .setTitle("Service Disabled")
                    .setMessage("Please enable Accessibility Service for Hijack to work.")
                    .setPositiveButton("Settings", (dialog, which) -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)))
                    .show();
        }

        mListAppInfo = findViewById(R.id.lvApps);
        mListAppInfo.setOnItemClickListener((parent, view, pos, id) -> {
            ResolveInfo appInfo = (ResolveInfo) parent.getAdapter().getItem(pos);
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Set Launcher")
                    .setMessage("Use " + appInfo.loadLabel(getPackageManager()) + "?")
                    .setPositiveButton("OK", (dialog, which) -> {
                        prevSelectedIndex = pos;
                        getSharedPreferences("LauncherHijack", MODE_PRIVATE).edit()
                            .putString("ChosenLauncher", appInfo.activityInfo.applicationInfo.packageName)
                            .apply();
                    }).show();
        });

        checkBatteryOptimizations();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) ServiceMan.Start(this);
        }
    }
}