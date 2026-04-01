package com.example.takeit;

import android.content.Context;
import android.content.SharedPreferences;

public class NightModeHelper {

    private static final String PREFS_NAME = "reminder_prefs";
    private static final String KEY_NIGHT_MODE = "night_mode";

    // Night mode colors (ARGB)
    static final int NIGHT_BG      = 0xFF121212;
    static final int NIGHT_SURFACE = 0xFF1E1E1E;
    static final int NIGHT_TEXT    = 0xFFFFFFFF;
    static final int NIGHT_HINT    = 0xFFBBBBBB;
    static final int NIGHT_ACCENT  = 0xFF90CAF9;
    static final int NIGHT_DIVIDER = 0xFF333333;

    // Day mode colors
    static final int DAY_BG      = 0xFFF5F5F5;
    static final int DAY_SURFACE = 0xFFFFFFFF;
    static final int DAY_TEXT    = 0xFF212121;
    static final int DAY_HINT    = 0xFF757575;
    static final int DAY_ACCENT  = 0xFF1976D2;
    static final int DAY_DIVIDER = 0xFFE0E0E0;

    public static boolean isNightMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_NIGHT_MODE, false);
    }

    public static void setNightMode(Context context, boolean night) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
               .edit().putBoolean(KEY_NIGHT_MODE, night).apply();
    }

    public static void toggle(Context context) {
        setNightMode(context, !isNightMode(context));
    }

    public static int bg(Context c)      { return isNightMode(c) ? NIGHT_BG      : DAY_BG; }
    public static int surface(Context c) { return isNightMode(c) ? NIGHT_SURFACE : DAY_SURFACE; }
    public static int text(Context c)    { return isNightMode(c) ? NIGHT_TEXT    : DAY_TEXT; }
    public static int hint(Context c)    { return isNightMode(c) ? NIGHT_HINT    : DAY_HINT; }
    public static int accent(Context c)  { return isNightMode(c) ? NIGHT_ACCENT  : DAY_ACCENT; }
    public static int divider(Context c) { return isNightMode(c) ? NIGHT_DIVIDER : DAY_DIVIDER; }
}
