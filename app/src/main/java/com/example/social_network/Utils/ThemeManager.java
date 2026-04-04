package com.example.social_network.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {

    private static final String PREF_NAME  = "theme_prefs";
    private static final String KEY_DARK   = "is_dark_mode";

    /** Áp dụng theme đã lưu — gọi trong Application.onCreate(). */
    public static void applyTheme(Context context) {
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode(context)
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    /** Chuyển đổi giữa dark/light và lưu lại. */
    public static void toggle(Context context) {
        boolean nowDark = !isDarkMode(context);
        prefs(context).edit().putBoolean(KEY_DARK, nowDark).apply();
        AppCompatDelegate.setDefaultNightMode(
                nowDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    public static boolean isDarkMode(Context context) {
        return prefs(context).getBoolean(KEY_DARK, false);
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
}
