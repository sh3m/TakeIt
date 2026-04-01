package com.example.takeit;

import android.content.Context;
import android.content.SharedPreferences;

public class NightModeHelper {

    private static final String PREFS_NAME = "reminder_prefs";
    private static final String KEY_NIGHT_MODE = "night_mode";

    // Dark theme
    static final int DARK_BG       = 0xFF0F1117;
    static final int DARK_SURFACE  = 0xFF1A1D2E;
    static final int DARK_SURFACE2 = 0xFF22263A;
    static final int DARK_TEXT     = 0xFFFFFFFF;
    static final int DARK_HINT     = 0xFF8B8FA8;
    static final int DARK_ACCENT   = 0xFF7C5CBF;
    static final int DARK_DIVIDER  = 0xFF2A2D40;

    // Light theme
    static final int LIGHT_BG       = 0xFFF4F5FA;
    static final int LIGHT_SURFACE  = 0xFFFFFFFF;
    static final int LIGHT_SURFACE2 = 0xFFECEEF6;
    static final int LIGHT_TEXT     = 0xFF1A1D27;
    static final int LIGHT_HINT     = 0xFF8E96A8;
    static final int LIGHT_ACCENT   = 0xFF7C5CBF;
    static final int LIGHT_DIVIDER  = 0xFFE4E6F0;

    public static boolean isNightMode(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_NIGHT_MODE, false);
    }

    public static void setNightMode(Context context, boolean night) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_NIGHT_MODE, night).apply();
    }

    public static void toggle(Context context) {
        setNightMode(context, !isNightMode(context));
    }

    public static int bg(Context c)       { return isNightMode(c) ? DARK_BG       : LIGHT_BG; }
    public static int surface(Context c)  { return isNightMode(c) ? DARK_SURFACE  : LIGHT_SURFACE; }
    public static int surface2(Context c) { return isNightMode(c) ? DARK_SURFACE2 : LIGHT_SURFACE2; }
    public static int text(Context c)     { return isNightMode(c) ? DARK_TEXT     : LIGHT_TEXT; }
    public static int hint(Context c)     { return isNightMode(c) ? DARK_HINT     : LIGHT_HINT; }
    public static int accent(Context c)   { return isNightMode(c) ? DARK_ACCENT   : LIGHT_ACCENT; }
    public static int divider(Context c)  { return isNightMode(c) ? DARK_DIVIDER  : LIGHT_DIVIDER; }
}
