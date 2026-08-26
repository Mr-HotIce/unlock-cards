package com.mrhotice.unlockcards;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.util.List;

public class CardActivity extends Activity {
    private Card card;
    private boolean revealed;
    private TextView hint;
    private TextView ipa;
    private TextView ruphon;
    private TextView russian;
    private TableLayout petrov;

    public static void show(Context c) {
        Intent i = new Intent(c, CardActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        try {
            c.startActivity(i);
        } catch (Exception e) {
            fireFullScreen(c);
        }
    }

    public static void fireFullScreen(Context c) {
        Intent i = new Intent(c, CardActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(
                c, 7, i, PendingIntent.FLAG_UPDATE_CURRENT | UnlockService.immutable());
        Notification.Builder b;
        if (Build.VERSION.SDK_INT >= 26) {
            b = new Notification.Builder(c, UnlockService.CH_CARD);
        } else {
            b = new Notification.Builder(c);
        }
        Notification n = b.setContentTitle("Unlock Cards")
                .setContentText("Карточка")
                .setSmallIcon(R.drawable.ic_stat)
                .setPriority(Notification.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_ALARM)
                .setFullScreenIntent(pi, true)
                .setAutoCancel(true)
                .build();
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(99, n);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupLockFlags();
        setContentView(R.layout.activity_card);
        hint = findViewById(R.id.hint);
        ipa = findViewById(R.id.ipa);
        ruphon = findViewById(R.id.ruphon);
        russian = findViewById(R.id.russian);
        petrov = findViewById(R.id.petrov);
        findViewById(R.id.cardBody).setOnClickListener(v -> reveal());
        findViewById(R.id.word).setOnClickListener(v -> reveal());
        ((Button) findViewById(R.id.btnClose)).setOnClickListener(v -> finish());
        ((Button) findViewById(R.id.btnNext)).setOnClickListener(v -> {
            loadCard();
        });
        loadCard();
    }

    private void setupLockFlags() {
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            if (km != null) km.requestDismissKeyguard(this, null);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void loadCard() {
        CardStore store = CardStore.get(this);
        boolean verbs = Prefs.verbsOnly(this);
        int size = store.size(verbs);
        card = store.get(Prefs.nextIndex(this, size), verbs);
        Prefs.bumpShown(this);
        revealed = false;
        bind();
    }

    private void bind() {
        if (card == null) {
            ((TextView) findViewById(R.id.word)).setText("нет карточек");
            return;
        }
        ((TextView) findViewById(R.id.word)).setText(card.word);
        String meta = trimJoin(card.pos, card.level, card.kind);
        ((TextView) findViewById(R.id.meta)).setText(meta);
        hint.setVisibility(View.VISIBLE);
        ipa.setVisibility(View.GONE);
        ruphon.setVisibility(View.GONE);
        russian.setVisibility(View.GONE);
        petrov.setVisibility(View.GONE);
        petrov.removeAllViews();
        if (revealed) reveal();
    }

    private void reveal() {
        if (card == null) return;
        revealed = true;
        hint.setVisibility(View.GONE);
        if (!empty(card.ipa)) {
            ipa.setText(card.ipa);
            ipa.setVisibility(View.VISIBLE);
        }
        if (!empty(card.transcription)) {
            ruphon.setText("[" + card.transcription + "]");
            ruphon.setVisibility(View.VISIBLE);
        }
        if (!empty(card.russian)) {
            russian.setText(card.russian);
            russian.setVisibility(View.VISIBLE);
        }
        if (card.verb) {
            fillPetrov();
            petrov.setVisibility(View.VISIBLE);
        }
    }

    private void fillPetrov() {
        petrov.removeAllViews();
        TableRow head = new TableRow(this);
        head.addView(cell("", true, 0));
        head.addView(cell("?", true, 1));
        head.addView(cell("+", true, 2));
        head.addView(cell("−", true, 3));
        petrov.addView(head);
        List<Petrov.Row> rows = Petrov.table(card);
        for (Petrov.Row r : rows) {
            TableRow tr = new TableRow(this);
            tr.addView(cell(r.label, true, 0));
            tr.addView(cell(r.q, false, 1));
            tr.addView(cell(r.a, false, 2));
            tr.addView(cell(r.n, false, 3));
            petrov.addView(tr);
        }
    }

    private TextView cell(String text, boolean bold, int kind) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(kind == 0 ? 11 : 11.5f);
        tv.setPadding(8, 8, 8, 8);
        tv.setGravity(kind == 0 ? Gravity.CENTER : Gravity.START);
        tv.setTextColor(getResources().getColor(R.color.ink));
        if (bold) tv.setTypeface(Typeface.DEFAULT_BOLD);
        int bg = R.drawable.badge_bg;
        if (kind == 1) bg = R.drawable.cell_q;
        else if (kind == 2) bg = R.drawable.cell_a;
        else if (kind == 3) bg = R.drawable.cell_n;
        tv.setBackgroundResource(bg);
        return tv;
    }

    private static String trimJoin(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p == null || p.trim().isEmpty() || "—".equals(p)) continue;
            if (sb.length() > 0) sb.append("  ·  ");
            sb.append(p.trim());
        }
        return sb.toString();
    }

    private static boolean empty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
