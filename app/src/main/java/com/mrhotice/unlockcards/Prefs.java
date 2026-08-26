package com.mrhotice.unlockcards;

import android.content.Context;
import android.content.SharedPreferences;

public final class Prefs {
    private Prefs() {}

    private static SharedPreferences p(Context c) {
        return c.getSharedPreferences("unlock_cards", Context.MODE_PRIVATE);
    }

    public static boolean isEnabled(Context c) {
        return p(c).getBoolean("enabled", true);
    }

    public static void setEnabled(Context c, boolean v) {
        p(c).edit().putBoolean("enabled", v).apply();
    }

    public static boolean showOnScreenOn(Context c) {
        return p(c).getBoolean("screen_on", true);
    }

    public static void setShowOnScreenOn(Context c, boolean v) {
        p(c).edit().putBoolean("screen_on", v).apply();
    }

    public static boolean showOnUnlock(Context c) {
        return p(c).getBoolean("unlock", true);
    }

    public static void setShowOnUnlock(Context c, boolean v) {
        p(c).edit().putBoolean("unlock", v).apply();
    }

    public static boolean verbsOnly(Context c) {
        return p(c).getBoolean("verbs_only", false);
    }

    public static void setVerbsOnly(Context c, boolean v) {
        p(c).edit().putBoolean("verbs_only", v).apply();
    }

    public static int nextIndex(Context c, int size) {
        if (size <= 0) return 0;
        int i = p(c).getInt("idx", 0);
        p(c).edit().putInt("idx", (i + 1) % size).apply();
        return i % size;
    }

    public static void bumpShown(Context c) {
        p(c).edit().putInt("shown", shown(c) + 1).apply();
    }

    public static int shown(Context c) {
        return p(c).getInt("shown", 0);
    }
}
