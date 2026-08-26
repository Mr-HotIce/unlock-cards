package com.mrhotice.unlockcards;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;

public class UnlockService extends Service {
    public static final String CH_FG = "unlock_cards_fg";
    public static final String CH_CARD = "unlock_cards_card";
    private static final int NID = 42;
    private UnlockReceiver receiver;
    private boolean registered;

    public static void start(Context c) {
        Intent i = new Intent(c, UnlockService.class);
        if (Build.VERSION.SDK_INT >= 26) {
            c.startForegroundService(i);
        } else {
            c.startService(i);
        }
    }

    public static void stop(Context c) {
        c.stopService(new Intent(c, UnlockService.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ensureChannels();
        startAsForeground();
        receiver = new UnlockReceiver();
        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_SCREEN_ON);
        f.addAction(Intent.ACTION_USER_PRESENT);
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(receiver, f, 0x4); // RECEIVER_NOT_EXPORTED
        } else {
            registerReceiver(receiver, f);
        }
        registered = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startAsForeground();
        if (!Prefs.isEnabled(this)) {
            stopSelf();
            return START_NOT_STICKY;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (registered) {
            try { unregisterReceiver(receiver); } catch (Exception ignored) {}
            registered = false;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startAsForeground() {
        Notification n = fgNotification();
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NID, n, 0x40000000); // SPECIAL_USE
        } else if (Build.VERSION.SDK_INT >= 29) {
            startForeground(NID, n, 0);
        } else {
            startForeground(NID, n);
        }
    }

    private void ensureChannels() {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationManager nm = getSystemService(NotificationManager.class);
        NotificationChannel fg = new NotificationChannel(
                CH_FG, getString(R.string.service_channel), NotificationManager.IMPORTANCE_LOW);
        fg.setShowBadge(false);
        nm.createNotificationChannel(fg);
        NotificationChannel card = new NotificationChannel(
                CH_CARD, getString(R.string.card_channel), NotificationManager.IMPORTANCE_HIGH);
        card.setDescription("Полноэкранная карточка после экрана");
        card.enableVibration(false);
        nm.createNotificationChannel(card);
    }

    private Notification fgNotification() {
        Intent open = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(
                this, 1, open, PendingIntent.FLAG_UPDATE_CURRENT | immutable());
        Notification.Builder b;
        if (Build.VERSION.SDK_INT >= 26) {
            b = new Notification.Builder(this, CH_FG);
        } else {
            b = new Notification.Builder(this);
        }
        return b.setContentTitle(getString(R.string.service_title))
                .setContentText(getString(R.string.service_text))
                .setSmallIcon(R.drawable.ic_stat)
                .setContentIntent(pi)
                .setOngoing(true)
                .build();
    }

    static int immutable() {
        return Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0;
    }
}
