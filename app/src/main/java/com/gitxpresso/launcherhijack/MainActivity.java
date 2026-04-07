package com.gitxpresso.launcherhijack;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.accessibility.AccessibilityManager;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static Context context;
    private ListView mListAppInfo;
    private MenuItem launcher, sysApps;

    public static void SetContext(Context c) { context = c; }
    public static Context GetContext() { return context; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SetContext(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();
        UpdateList();

        mListAppInfo = findViewById(R.id.lvApps);
        mListAppInfo.setOnItemClickListener((parent, view, pos, id) -> {
            final ResolveInfo appInfo = (ResolveInfo) parent.getAdapter().getItem(pos);
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Set Launcher")
                    .setMessage("Use " + appInfo.loadLabel(getPackageManager()) + "?")
                    .setPositiveButton("OK", (dialog, which) -> {
                        SharedPreferences settings = getSharedPreferences("LauncherHijack", MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("ChosenLauncher", appInfo.activityInfo.packageName);
                        editor.putString("ChosenLauncherName", appInfo.activityInfo.name);
                        editor.apply();
                        Toast.makeText(MainActivity.this, "Launcher Set", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void checkPermissions() {
        if (!isAccessibilityEnabled()) {
            new AlertDialog.Builder(this)
                .setTitle("Accessibility Required")
                .setMessage("Please enable Launcher Hijack in Accessibility settings.")
                .setPositiveButton("Settings", (d, w) -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)))
                .show();
        }

        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminName = new ComponentName(this, AdminReceiver.class);
        if (!dpm.isAdminActive(adminName)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Required to keep the service persistent.");
            startActivity(intent);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName())) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }

    private void UpdateList() {
        boolean sys = (sysApps == null) || sysApps.isChecked();
        boolean l = (launcher == null) || launcher.isChecked();
        List<ResolveInfo> appInfo = Utilities.getInstalledApplication(this, l, sys);
        AppAdapter adapter = new AppAdapter(this, appInfo, getPackageManager());
        mListAppInfo = findViewById(R.id.lvApps);
        mListAppInfo.setAdapter(adapter);
    }

    private boolean isAccessibilityEnabled() {
        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        String id = getPackageName() + "/.AccServ";
        for (AccessibilityServiceInfo service : am.getEnabledAccessibilityServiceList(-1)) {
            if (id.equals(service.getId())) return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        sysApps = menu.findItem(R.id.sysApps);
        launcher = menu.findItem(R.id.launcher);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        item.setChecked(!item.isChecked());
        UpdateList();
        return true;
    }
}