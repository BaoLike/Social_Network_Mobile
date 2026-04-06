package com.example.social_network.Network;

import android.content.Context;
import android.net.Uri;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.social_network.Config.AppConfig;
import com.example.social_network.Model.SearchUserResult;
import com.example.social_network.Model.UserProfile;
import com.example.social_network.Utils.TokenManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileApiService {

    private static final String GET_PROFILE_PATH = "/profile/info/getProfile";
    private static final String UPDATE_PROFILE_PATH = "/profile/info/updateInfo";

    public interface ProfileCallback {
        void onSuccess(UserProfile profile);

        void onFailure(String message);
    }

    public interface SearchUsersCallback {
        void onSuccess(List<SearchUserResult> users);

        void onFailure(String message);
    }

    public interface SimpleCallback {
        void onSuccess();

        void onFailure(String message);
    }

    public void getMyProfile(Context context, ProfileCallback callback) {
        String token = TokenManager.getAccessToken(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Chưa đăng nhập");
            return;
        }

        String url = AppConfig.BSSE_URL_PROFILE + GET_PROFILE_PATH;
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    if (!"1000".equals(String.valueOf(response.opt("code")))) {
                        callback.onFailure(response.optString("message", "Không tải được hồ sơ"));
                        return;
                    }
                    UserProfile profile = parseProfile(response.optJSONObject("result"));
                    if (profile == null) {
                        callback.onFailure("Dữ liệu hồ sơ không hợp lệ");
                        return;
                    }
                    callback.onSuccess(profile);
                },
                error -> callback.onFailure(parseError(error))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return buildAuthHeadersWithJson(context);
            }
        };

        newQueue(context).add(request);
    }

    public void updateMyProfile(Context context, String firstName, String lastName, String dob, String address,
                               ProfileCallback callback) {
        String token = TokenManager.getAccessToken(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Chưa đăng nhập");
            return;
        }

        String url = AppConfig.BSSE_URL_PROFILE + UPDATE_PROFILE_PATH;
        JSONObject body = new JSONObject();
        try {
            body.put("firstName", firstName != null ? firstName : "");
            body.put("lastName", lastName != null ? lastName : "");
            body.put("dob", dob != null ? dob : "");
            body.put("address", address != null ? address : "");
        } catch (JSONException e) {
            callback.onFailure("Lỗi tạo dữ liệu");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                body,
                response -> {
                    if (!"1000".equals(String.valueOf(response.opt("code")))) {
                        callback.onFailure(response.optString("message", "Cập nhật thất bại"));
                        return;
                    }
                    UserProfile profile = parseProfile(response.optJSONObject("result"));
                    if (profile == null) {
                        callback.onFailure("Phản hồi không hợp lệ");
                        return;
                    }
                    callback.onSuccess(profile);
                },
                error -> callback.onFailure(parseError(error))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return buildAuthHeadersWithJson(context);
            }
        };

        newQueue(context).add(request);
    }

    /**
     * GET /profile/info/search/{keyword} (keyword is URL-encoded path segment).
     */
    public void searchUsers(Context context, String keyword, SearchUsersCallback callback) {
        String token = TokenManager.getAccessToken(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Chưa đăng nhập");
            return;
        }
        String q = keyword == null ? "" : keyword.trim();
        if (q.isEmpty()) {
            callback.onSuccess(Collections.emptyList());
            return;
        }

        String url = AppConfig.BSSE_URL_PROFILE + "/profile/info/search/" + Uri.encode(q, null);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    if (!"1000".equals(String.valueOf(response.opt("code")))) {
                        callback.onFailure(response.optString("message", "Tìm kiếm thất bại"));
                        return;
                    }
                    JSONArray arr = response.optJSONArray("result");
                    if (arr == null) {
                        callback.onSuccess(Collections.emptyList());
                        return;
                    }
                    List<SearchUserResult> list = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        SearchUserResult u = parseSearchUserItem(arr.optJSONObject(i));
                        if (u != null) {
                            list.add(u);
                        }
                    }
                    callback.onSuccess(list);
                },
                error -> callback.onFailure(parseError(error))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return buildAuthHeadersWithJson(context);
            }
        };

        newQueue(context).add(request);
    }

    /**
     * POST /profile/follow/{userId}
     */
    public void followUser(Context context, String userId, SimpleCallback callback) {
        String token = TokenManager.getAccessToken(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Chưa đăng nhập");
            return;
        }
        if (userId == null || userId.trim().isEmpty()) {
            callback.onFailure("Thiếu userId");
            return;
        }
        String url = AppConfig.BSSE_URL_PROFILE + "/profile/follow/" + Uri.encode(userId.trim(), null);
        JSONObject body = new JSONObject();
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    if (!"1000".equals(String.valueOf(response.opt("code")))) {
                        callback.onFailure(response.optString("message", "Theo dõi thất bại"));
                        return;
                    }
                    callback.onSuccess();
                },
                error -> callback.onFailure(parseError(error))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return buildAuthHeadersWithJson(context);
            }
        };

        newQueue(context).add(request);
    }

    private static SearchUserResult parseSearchUserItem(JSONObject item) {
        if (item == null) {
            return null;
        }
        String userId = item.optString("userId", "");
        if (userId.isEmpty()) {
            return null;
        }
        String userName = item.isNull("userName") ? null : item.optString("userName", null);
        String avatar = item.isNull("avatar") ? null : item.optString("avatar", null);
        String firstName = item.optString("firstName", "");
        String lastName = item.optString("lastName", "");
        String gender = item.isNull("gender") ? null : item.optString("gender", null);
        String dob = item.isNull("dob") ? null : item.optString("dob", null);
        String address = item.isNull("address") ? null : item.optString("address", null);
        String phone = item.isNull("phone") ? null : item.optString("phone", null);
        return new SearchUserResult(userId, userName, avatar, firstName, lastName, gender, dob, address, phone);
    }

    private static UserProfile parseProfile(JSONObject o) {
        if (o == null) {
            return null;
        }
        String userId = o.optString("userId", "");
        String userName = o.isNull("userName") ? null : o.optString("userName", null);
        String avatar = o.isNull("avatar") ? null : o.optString("avatar", null);
        String firstName = o.optString("firstName", "");
        String lastName = o.optString("lastName", "");
        String gender = o.isNull("gender") ? null : o.optString("gender", null);
        String dob = o.isNull("dob") ? null : o.optString("dob", null);
        String address = o.isNull("address") ? null : o.optString("address", null);
        String phone = o.isNull("phone") ? null : o.optString("phone", null);
        int follower = o.optInt("follower", 0);
        int followed = o.optInt("followed", 0);
        return new UserProfile(userId, userName, avatar, firstName, lastName, gender, dob, address, phone,
                follower, followed);
    }

    private RequestQueue newQueue(Context context) {
        return Volley.newRequestQueue(context.getApplicationContext());
    }

    private Map<String, String> buildAuthHeadersWithJson(Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        String accessToken = TokenManager.getAccessToken(context);
        if (accessToken != null && !accessToken.isEmpty()) {
            headers.put("Authorization", "Bearer " + accessToken.trim());
        }
        return headers;
    }

    private static String parseError(com.android.volley.VolleyError error) {
        if (error.networkResponse != null && error.networkResponse.data != null) {
            return new String(error.networkResponse.data, StandardCharsets.UTF_8);
        }
        return "Không thể kết nối máy chủ profile";
    }
}
