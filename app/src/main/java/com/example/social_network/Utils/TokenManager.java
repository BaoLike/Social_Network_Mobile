package com.example.social_network.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.social_network.Config.AppConfig;

public class TokenManager {

    private static final String PREF_NAME         = "auth_prefs";
    private static final String KEY_ACCESS_TOKEN  = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";

    /** Lưu cả 2 token sau khi đăng nhập thành công. */
    public static void saveTokens(Context context, String accessToken, String refreshToken) {
        AppConfig.ACCESS_TOKEN = accessToken;
        prefs(context).edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .apply();
    }

    /** Cập nhật accessToken mới (sau khi refresh). */
    public static void updateAccessToken(Context context, String accessToken) {
        AppConfig.ACCESS_TOKEN = accessToken;
        prefs(context).edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .apply();
    }

    /**
     * Trả về accessToken.
     * Ưu tiên lấy từ bộ nhớ (AppConfig), nếu null thì đọc từ SharedPreferences
     * (ví dụ sau khi app khởi động lại).
     */
    public static String getAccessToken(Context context) {
        if (AppConfig.ACCESS_TOKEN != null && !AppConfig.ACCESS_TOKEN.isEmpty()) {
            return AppConfig.ACCESS_TOKEN;
        }
        String token = prefs(context).getString(KEY_ACCESS_TOKEN, null);
        if (token != null && token.isEmpty()) {
            token = null;
        }
        AppConfig.ACCESS_TOKEN = token;
        return token;
    }

    public static String getRefreshToken(Context context) {
        return prefs(context).getString(KEY_REFRESH_TOKEN, null);
    }

    /** Xóa token khi logout. */
    public static void clearTokens(Context context) {
        AppConfig.ACCESS_TOKEN = null;
        prefs(context).edit().clear().apply();
    }

    /** Kiểm tra user đã đăng nhập chưa (dùng cho auto-login check). */
    public static boolean isLoggedIn(Context context) {
        return getAccessToken(context) != null;
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
}
