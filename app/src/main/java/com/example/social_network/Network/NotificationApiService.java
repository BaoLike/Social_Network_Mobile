package com.example.social_network.Network;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.social_network.Config.AppConfig;
import com.example.social_network.Utils.TokenManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * POST /notification/register — lưu FCM token theo user (backend notification service).
 */
public class NotificationApiService {

    private static final String TAG = "NotificationApi";
    private static final String REGISTER_PATH = "/notification/register";

    public interface RegisterCallback {
        void onSuccess();

        void onFailure(String message);
    }

    public void registerDevice(Context context, String userId, String tokenDevice, RegisterCallback callback) {
        Context app = context.getApplicationContext();
        if (userId == null || userId.isEmpty()) {
            callback.onFailure("Thiếu userId");
            return;
        }
        if (tokenDevice == null || tokenDevice.isEmpty()) {
            callback.onFailure("Thiếu token thiết bị");
            return;
        }

        String url = AppConfig.BASE_URL_NOTIFICATION + REGISTER_PATH;
        JSONObject body = new JSONObject();
        try {
            body.put("userId", userId);
            body.put("tokenDevice", tokenDevice);
        } catch (JSONException e) {
            callback.onFailure("Lỗi tạo body");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    Log.d(TAG, "register ok: " + response.toString());
                    callback.onSuccess();
                },
                error -> callback.onFailure(parseError(error))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                String accessToken = TokenManager.getAccessToken(app);
                if (accessToken != null && !accessToken.isEmpty()) {
                    headers.put("Authorization", "Bearer " + accessToken.trim());
                }
                return headers;
            }
        };

        newQueue(app).add(request);
    }

    private RequestQueue newQueue(Context context) {
        return Volley.newRequestQueue(context.getApplicationContext());
    }

    private static String parseError(com.android.volley.VolleyError error) {
        if (error.networkResponse != null && error.networkResponse.data != null) {
            return new String(error.networkResponse.data, StandardCharsets.UTF_8);
        }
        return "Không thể đăng ký thông báo đẩy";
    }
}
