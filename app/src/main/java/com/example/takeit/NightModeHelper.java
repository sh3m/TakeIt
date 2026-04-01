package com.example.takeit;

import android.content.Context;
import android.content.SharedPreferences;

public class NightModeHelper {

    private static final String PREFS_NAME = "reminder_prefs";
    private static final String KEY_NIGHT_MODE = "night_mode";

    // Dark theme — mirrors the icon palette (deep navy + teal)
    static final int DARK_BG       = 0xFF07090F;
    static final int DARK_SURFACE  = 0xFF0D1520;
    static final int DARK_SURFACE2 = 0xFF132030;
    static final int DARK_TEXT     = 0xFFE8F5F8;
    static final int DARK_HINT     = 0xFF4A7A8A;
    static final int DARK_ACCENT   = 0xFF00B4CC;
    static final int DARK_DIVIDER  = 0xFF112030;

    // Light theme
    static final int LIGHT_BG       = 0xFFE8F5FA;
    static final int LIGHT_SURFACE  = 0xFFFFFFFF;
    static final int LIGHT_SURFACE2 = 0xFFD0EBF2;
    static final int LIGHT_TEXT     = 0xFF071520;
    static final int LIGHT_HINT     = 0xFF4A7080;
    static final int LIGHT_ACCENT   = 0xFF0097A7;
    static final int LIGHT_DIVIDER  = 0xFFB8DDE8;

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
