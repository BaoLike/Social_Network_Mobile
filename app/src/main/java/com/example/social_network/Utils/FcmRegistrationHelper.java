package com.example.social_network.Utils;

import android.content.Context;
import android.util.Log;

import com.example.social_network.Network.ChatApiService;
import com.example.social_network.Network.NotificationApiService;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Gửi FCM token lên {@code /notification/register} sau khi user đã đăng nhập (có JWT + userId trong sub).
 */
public final class FcmRegistrationHelper {

    private static final String TAG = "FcmRegistration";

    private FcmRegistrationHelper() {
    }

    /**
     * Lấy token FCM hiện tại rồi đăng ký với backend (nếu đã login).
     */
    public static void registerWithCurrentToken(Context context) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w(TAG, "getToken failed", task.getException());
                return;
            }
            String token = task.getResult();
            if (token != null) {
                Log.d(TAG, "FCM token obtained, registering…");
                register(context.getApplicationContext(), token);
            }
        });
    }

    /**
     * Đăng ký token đã có (sau khi {@link FirebaseMessaging#getToken()} thành công).
     */
    public static void register(Context context, String fcmToken) {
        Context app = context.getApplicationContext();
        if (!TokenManager.isLoggedIn(app)) {
            return;
        }
        if (fcmToken == null || fcmToken.isEmpty()) {
            return;
        }
        String userId = new ChatApiService().getCurrentUserId(app);
        if (userId == null || userId.isEmpty()) {
            Log.w(TAG, "No userId from JWT, skip register");
            return;
        }

        new NotificationApiService().registerDevice(app, userId, fcmToken, new NotificationApiService.RegisterCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Device registered for user " + userId);
            }

            @Override
            public void onFailure(String message) {
                Log.w(TAG, "Register failed: " + message);
            }
        });
    }
}
