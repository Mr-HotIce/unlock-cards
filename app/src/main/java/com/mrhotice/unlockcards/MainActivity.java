package com.mrhotice.unlockcards;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends Activity {
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        status = findViewById(R.id.status);

        Switch en = findViewById(R.id.switchEnabled);
        Switch screen = findViewById(R.id.switchScreenOn);
        Switch unlock = findViewById(R.id.switchUnlock);
        Switch verbs = findViewById(R.id.switchVerbsOnly);
        en.setChecked(Prefs.isEnabled(this));
        screen.setChecked(Prefs.showOnScreenOn(this));
        unlock.setChecked(Prefs.showOnUnlock(this));
        verbs.setChecked(Prefs.verbsOnly(this));

        en.setOnCheckedChangeListener(this::onEnabled);
        screen.setOnCheckedChangeListener((b, v) -> Prefs.setShowOnScreenOn(this, v));
        unlock.setOnCheckedChangeListener((b, v) -> Prefs.setShowOnUnlock(this, v));
        verbs.setOnCheckedChangeListener((b, v) -> Prefs.setVerbsOnly(this, v));

        ((Button) findViewById(R.id.btnPreview)).setOnClickListener(v -> CardActivity.show(this));
        ((Button) findViewById(R.id.btnOverlay)).setOnClickListener(v -> openOverlay());
        ((Button) findViewById(R.id.btnBattery)).setOnClickListener(v -> openBattery());
        ((Button) findViewById(R.id.btnNotify)).setOnClickListener(v -> askNotify());

        if (Prefs.isEnabled(this)) UnlockService.start(this);
        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void onEnabled(CompoundButton b, boolean v) {
        Prefs.setEnabled(this, v);
        if (v) UnlockService.start(this);
        else UnlockService.stop(this);
        refresh();
    }

    private void refresh() {
        CardStore store = CardStore.get(this);
        boolean overlay = Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(this);
        boolean battery = isIgnoringBattery();
        String s = "Карточек: " + store.size(false)
                + "  ·  глаголов: " + store.size(true)
                + "  ·  показано: " + Prefs.shown(this)
                + "\nПоверх окон: " + (overlay ? "да" : "нет")
                + "  ·  батарея: " + (battery ? "без ограничений" : "экономит")
                + "  ·  служба: " + (Prefs.isEnabled(this) ? "вкл" : "выкл");
        status.setText(s);
    }

    private boolean isIgnoringBattery() {
        if (Build.VERSION.SDK_INT < 23) return true;
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        return pm != null && pm.isIgnoringBatteryOptimizations(getPackageName());
    }

    private void openOverlay() {
        if (Build.VERSION.SDK_INT < 23) return;
        Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivity(i);
    }

    private void openBattery() {
        if (Build.VERSION.SDK_INT < 23) return;
        try {
            Intent i = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            i.setData(Uri.parse("package:" + getPackageName()));
            startActivity(i);
        } catch (Exception e) {
            startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
        }
    }

    private void askNotify() {
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 10);
        }
    }
}
