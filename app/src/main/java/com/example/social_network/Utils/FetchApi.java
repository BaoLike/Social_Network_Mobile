package com.example.social_network.Utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.social_network.Config.AppConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FetchApi {

    public interface ApiCallback {
        void onSuccess();
        void onFailure(String message);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Register
    // ─────────────────────────────────────────────────────────────────────────

    public void postRegister(String url, Map<String, String> userRegister,
                             Context context, String tag, ApiCallback callback) {
        JSONObject requestBody = new JSONObject();
        try {
            for (Map.Entry<String, String> entry : userRegister.entrySet()) {
                requestBody.put(entry.getKey(), entry.getValue());
            }
        } catch (JSONException e) {
            Log.e(tag, "Build request error: " + e.getMessage());
            callback.onFailure("Lỗi tạo dữ liệu request");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, requestBody,
                response -> {
                    try {
                        String code = response.getString("code");
                        if (code.equals("1000")) {
                            callback.onSuccess();
                        } else {
                            callback.onFailure(response.optString("message", "Đăng ký thất bại"));
                        }
                    } catch (JSONException e) {
                        Log.e(tag, "Parse response error: " + e.getMessage());
                        callback.onFailure("Lỗi xử lý phản hồi từ server");
                    }
                },
                error -> handleVolleyError(error, tag, callback)
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return buildJsonHeaders();
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                20_000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        newQueue(context).add(request);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Login — lưu accessToken vào AppConfig và refreshToken vào SharedPreferences
    // ─────────────────────────────────────────────────────────────────────────

    public void postLogin(String url, Map<String, String> userLogin,
                          Context context, String tag, ApiCallback callback) {
        JSONObject requestBody = new JSONObject();
        try {
            for (Map.Entry<String, String> entry : userLogin.entrySet()) {
                requestBody.put(entry.getKey(), entry.getValue());
            }
        } catch (JSONException e) {
            Log.e(tag, "Build request error: " + e.getMessage());
            callback.onFailure("Lỗi tạo dữ liệu request");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, requestBody,
                response -> {
                    try {
                        String code = response.getString("code");
                        if (code.equals("1000")) {
                            JSONObject result       = response.getJSONObject("result");
                            String     accessToken  = result.getString("accessToken");
                            String     refreshToken = result.getString("refreshToken");

                            // accessToken → AppConfig.ACCESS_TOKEN + SharedPreferences
                            // refreshToken → SharedPreferences
                            TokenManager.saveTokens(context, accessToken, refreshToken);

                            Log.d(tag, "Login success. AccessToken saved.");
                            callback.onSuccess();
                        } else {
                            callback.onFailure(response.optString("message", "Đăng nhập thất bại"));
                        }
                    } catch (JSONException e) {
                        Log.e(tag, "Parse response error: " + e.getMessage());
                        callback.onFailure("Lỗi xử lý phản hồi từ server");
                    }
                },
                error -> handleVolleyError(error, tag, callback)
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return buildJsonHeaders();
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                20_000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        newQueue(context).add(request);
    }

    public void postLogout(Context context, String tag, ApiCallback callback) {
        String refreshToken = TokenManager.getRefreshToken(context);
        if (refreshToken == null) {
            TokenManager.clearTokens(context);
            callback.onSuccess();
            return;
        }

        String url = AppConfig.BASE_URL + "/identity/auth/logout";
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("refreshToken", refreshToken);
        } catch (JSONException e) {
            callback.onFailure("Lỗi tạo dữ liệu request");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, requestBody,
                response -> {
                    try {
                        String code = response.getString("code");
                        TokenManager.clearTokens(context);
                        if (code.equals("1000")) {
                            callback.onSuccess();
                        } else {
                            callback.onFailure(response.optString("message", "Đăng xuất thất bại"));
                        }
                    } catch (JSONException e) {
                        Log.e(tag, "Parse logout response error: " + e.getMessage());
                        TokenManager.clearTokens(context);
                        callback.onSuccess();
                    }
                },
                error -> {
                    TokenManager.clearTokens(context);
                    callback.onSuccess();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return buildAuthHeaders();
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        newQueue(context).add(request);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Refresh Token — lấy accessToken mới khi hết hạn
    // ─────────────────────────────────────────────────────────────────────────

    public void postRefreshToken(Context context, String tag, ApiCallback callback) {
        String refreshToken = TokenManager.getRefreshToken(context);
        if (refreshToken == null) {
            callback.onFailure("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
            return;
        }

        String url = AppConfig.BASE_URL + "/identity/auth/refresh";
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("token", refreshToken);
        } catch (JSONException e) {
            callback.onFailure("Lỗi tạo dữ liệu request");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, requestBody,
                response -> {
                    try {
                        String code = response.getString("code");
                        if (code.equals("1000")) {
                            JSONObject result      = response.getJSONObject("result");
                            String     newAccess   = result.getString("accessToken");

                            // Nếu server cũng trả về refreshToken mới thì cập nhật luôn
                            if (result.has("refreshToken")) {
                                String newRefresh = result.getString("refreshToken");
                                TokenManager.saveTokens(context, newAccess, newRefresh);
                            } else {
                                TokenManager.updateAccessToken(context, newAccess);
                            }

                            Log.d(tag, "Token refreshed successfully.");
                            callback.onSuccess();
                        } else {
                            // refreshToken hết hạn → xóa và yêu cầu đăng nhập lại
                            TokenManager.clearTokens(context);
                            callback.onFailure("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
                        }
                    } catch (JSONException e) {
                        Log.e(tag, "Parse refresh response error: " + e.getMessage());
                        callback.onFailure("Lỗi xử lý phản hồi từ server");
                    }
                },
                error -> {
                    // 401 khi refresh → token hết hạn hẳn, buộc đăng nhập lại
                    if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                        TokenManager.clearTokens(context);
                        callback.onFailure("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
                    } else {
                        handleVolleyError(error, tag, callback);
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return buildJsonHeaders();
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        newQueue(context).add(request);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers dùng chung
    // ─────────────────────────────────────────────────────────────────────────

    private RequestQueue newQueue(Context context) {
        return Volley.newRequestQueue(context);
    }

    private Map<String, String> buildJsonHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        return headers;
    }

    /** Header cho các API cần xác thực (kèm Bearer token). */
    private Map<String, String> buildAuthHeaders() {
        Map<String, String> headers = buildJsonHeaders();
        if (AppConfig.ACCESS_TOKEN != null) {
            headers.put("Authorization", "Bearer " + AppConfig.ACCESS_TOKEN);
        }
        return headers;
    }

    private void handleVolleyError(com.android.volley.VolleyError error,
                                   String tag, ApiCallback callback) {
        if (error.networkResponse != null && error.networkResponse.data != null) {
            try {
                String errorBody = new String(error.networkResponse.data, "UTF-8");
                JSONObject errorJson = new JSONObject(errorBody);
                callback.onFailure(errorJson.optString("message", "Lỗi không xác định"));
            } catch (Exception e) {
                Log.e(tag, "Parse error body failed: " + e.getMessage());
                callback.onFailure("Không thể kết nối đến server. Vui lòng thử lại.");
            }
        } else {
            Log.e(tag, "Network error: " + error.toString());
            callback.onFailure("Không thể kết nối đến server. Vui lòng thử lại.");
        }
    }
}
