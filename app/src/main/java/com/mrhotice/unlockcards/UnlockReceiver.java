package com.mrhotice.unlockcards;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UnlockReceiver extends BroadcastReceiver {
    private static long lastShown;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Prefs.isEnabled(context)) return;
        String action = intent.getAction();
        boolean screenOn = Intent.ACTION_SCREEN_ON.equals(action);
        boolean unlock = Intent.ACTION_USER_PRESENT.equals(action);
        if (screenOn && !Prefs.showOnScreenOn(context)) return;
        if (unlock && !Prefs.showOnUnlock(context)) return;
        if (!screenOn && !unlock) return;
        long now = System.currentTimeMillis();
        if (now - lastShown < 2500) return;
        lastShown = now;
        CardActivity.show(context);
    }
}
