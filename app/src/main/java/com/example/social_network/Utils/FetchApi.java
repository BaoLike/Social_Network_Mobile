package com.example.social_network.Utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.social_network.Config.AppConfig;
import com.example.social_network.Model.CommentModel;
import com.example.social_network.Model.Post;
import com.example.social_network.Model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FetchApi {

    public interface ApiCallback {
        void onSuccess();
        void onFailure(String message);
    }

    public interface PostsCallback {
        void onSuccess(List<Post> posts);
        void onFailure(String message);
    }

    public void putLikePost(Context context, String postId, String tag, ApiCallback callback) {
        putToggleLike(context, postId, true, tag, callback);
    }

    public void putUnlikePost(Context context, String postId, String tag, ApiCallback callback) {
        putToggleLike(context, postId, false, tag, callback);
    }

    private void putToggleLike(Context context, String postId, boolean like,
                               String tag, ApiCallback callback) {
        if (postId == null || postId.trim().isEmpty()) {
            callback.onFailure("Thiếu postId");
            return;
        }
        String accessToken = TokenManager.getAccessToken(context);
        if (accessToken == null || accessToken.trim().isEmpty()) {
            callback.onFailure("Bạn chưa đăng nhập");
            return;
        }

        String endpoint = like ? "/post/like/" : "/post/unlike/";
        String url = AppConfig.BASE_IP + ":8087" + endpoint + Uri.encode(postId);
        StringRequest request = new StringRequest(
                Request.Method.PUT,
                url,
                response -> callback.onSuccess(),
                error -> handleVolleyError(error, tag, callback)
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = buildJsonHeaders();
                headers.put("Authorization", "Bearer " + accessToken.trim());
                return headers;
            }

            @Override
            public byte[] getBody() {
                return null;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                12_000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        newQueue(context).add(request);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Create Post
    // ─────────────────────────────────────────────────────────────────────────
    public void postCreatePost(Context context, Uri mediaUri, String description,
                               String tag, ApiCallback callback) {
        if (mediaUri == null) {
            callback.onFailure("Vui lòng chọn ảnh/video từ thiết bị");
            return;
        }

        String accessToken = TokenManager.getAccessToken(context);
        if (accessToken == null || accessToken.trim().isEmpty()) {
            callback.onFailure("Bạn chưa đăng nhập");
            return;
        }

        String userId = extractUserIdFromAccessToken(accessToken);
        if (userId == null || userId.trim().isEmpty()) {
            callback.onFailure("Không đọc được userId từ token");
            return;
        }

        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("userId", userId);
            dataJson.put("description", description == null ? "" : description);
        } catch (JSONException e) {
            callback.onFailure("Lỗi tạo dữ liệu bài viết");
            return;
        }

        String postBase = AppConfig.BASE_IP;
        String url = postBase + ":8087/post/create";
        String boundary = "----CreatePostBoundary" + UUID.randomUUID();

        byte[] body;
        try {
            body = buildCreatePostMultipartBody(context, dataJson.toString(), mediaUri, boundary);
        } catch (IOException e) {
            Log.e(tag, "Build create-post body failed: " + e.getMessage());
            callback.onFailure("Không thể đọc file media");
            return;
        }

        Request<JSONObject> request = new Request<JSONObject>(
                Request.Method.POST,
                url,
                error -> handleVolleyError(error, tag, callback)
        ) {
            @Override
            public String getBodyContentType() {
                return "multipart/form-data; boundary=" + boundary;
            }

            @Override
            public byte[] getBody() {
                return body;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + accessToken.trim());
                return headers;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "UTF-8"));
                    return Response.success(new JSONObject(json), HttpHeaderParser.parseCacheHeaders(response));
                } catch (Exception e) {
                    return Response.error(new ParseError(e));
                }
            }

            @Override
            protected void deliverResponse(JSONObject response) {
                String code = String.valueOf(response.opt("code"));
                if ("200".equals(code) || "1000".equals(code)) {
                    callback.onSuccess();
                } else {
                    callback.onFailure(response.optString("message", "Tạo bài viết thất bại"));
                }
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                30_000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        newQueue(context).add(request);
    }

    public void putCommentToPost(Context context, String postId, String comment,
                                 String tag, ApiCallback callback) {
        if (postId == null || postId.trim().isEmpty()) {
            callback.onFailure("Thiếu postId để bình luận");
            return;
        }
        if (comment == null || comment.trim().isEmpty()) {
            callback.onFailure("Nội dung bình luận trống");
            return;
        }

        String accessToken = TokenManager.getAccessToken(context);
        if (accessToken == null || accessToken.trim().isEmpty()) {
            callback.onFailure("Bạn chưa đăng nhập");
            return;
        }

        String userId = extractUserIdFromAccessToken(accessToken);
        if (userId == null || userId.trim().isEmpty()) {
            callback.onFailure("Không đọc được userId từ token");
            return;
        }

        String url = AppConfig.BASE_IP + ":8087/post/comment/" + Uri.encode(postId);
        JSONObject body = new JSONObject();
        try {
            body.put("userId", userId);
            body.put("comment", comment.trim());
        } catch (JSONException e) {
            callback.onFailure("Lỗi tạo dữ liệu comment");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                body,
                response -> {
                    String code = String.valueOf(response.opt("code"));
                    if ("200".equals(code) || "1000".equals(code)) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure(response.optString("message", "Bình luận thất bại"));
                    }
                },
                error -> handleVolleyError(error, tag, callback)
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = buildJsonHeaders();
                headers.put("Authorization", "Bearer " + accessToken.trim());
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                15_000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        newQueue(context).add(request);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Register
    // ─────────────────────────────────────────────────────────────────────────

    public void postRegister(String url, Map<String, String> userRegister, Uri avatarUri,
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

        String boundary = "----RegisterBoundary" + UUID.randomUUID();
        byte[] multipartBody;
        try {
            multipartBody = buildRegisterMultipartBody(context, requestBody.toString(), avatarUri, boundary);
        } catch (IOException e) {
            Log.e(tag, "Build multipart body error: " + e.getMessage());
            callback.onFailure("Không thể đọc ảnh avatar");
            return;
        }

        Request<JSONObject> request = new Request<JSONObject>(
                Request.Method.POST,
                url,
                error -> handleVolleyError(error, tag, callback)
        ) {
            @Override
            public String getBodyContentType() {
                return "multipart/form-data; boundary=" + boundary;
            }

            @Override
            public byte[] getBody() {
                return multipartBody;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "UTF-8"));
                    return Response.success(new JSONObject(json), HttpHeaderParser.parseCacheHeaders(response));
                } catch (Exception e) {
                    return Response.error(new ParseError(e));
                }
            }

            @Override
            protected void deliverResponse(JSONObject response) {
                try {
                    String code = String.valueOf(response.opt("code"));
                    if ("1000".equals(code)) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure(response.optString("message", "Đăng ký thất bại"));
                    }
                } catch (Exception e) {
                    Log.e(tag, "Parse register response error: " + e.getMessage());
                    callback.onFailure("Lỗi xử lý phản hồi từ server");
                }
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                20_000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        newQueue(context).add(request);
    }

    private byte[] buildRegisterMultipartBody(Context context,
                                              String userDataJson,
                                              Uri avatarUri,
                                              String boundary) throws IOException {
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // data: JSON text part
        outputStream.write((twoHyphens + boundary + lineEnd).getBytes(StandardCharsets.UTF_8));
        outputStream.write(("Content-Disposition: form-data; name=\"data\"" + lineEnd).getBytes(StandardCharsets.UTF_8));
        outputStream.write(("Content-Type: application/json; charset=UTF-8" + lineEnd + lineEnd).getBytes(StandardCharsets.UTF_8));
        outputStream.write(userDataJson.getBytes(StandardCharsets.UTF_8));
        outputStream.write(lineEnd.getBytes(StandardCharsets.UTF_8));

        // media: avatar file part
        if (avatarUri != null) {
            String fileName = resolveFileName(context, avatarUri);
            if (fileName == null || fileName.isEmpty()) {
                fileName = "avatar.jpg";
            }
            String mimeType = context.getContentResolver().getType(avatarUri);
            if (mimeType == null || mimeType.isEmpty()) {
                mimeType = "application/octet-stream";
            }

            outputStream.write((twoHyphens + boundary + lineEnd).getBytes(StandardCharsets.UTF_8));
            outputStream.write(("Content-Disposition: form-data; name=\"media\"; filename=\"" + fileName + "\"" + lineEnd)
                    .getBytes(StandardCharsets.UTF_8));
            outputStream.write(("Content-Type: " + mimeType + lineEnd + lineEnd).getBytes(StandardCharsets.UTF_8));

            byte[] fileBytes = readUriBytes(context, avatarUri);
            outputStream.write(fileBytes);
            outputStream.write(lineEnd.getBytes(StandardCharsets.UTF_8));
        }

        outputStream.write((twoHyphens + boundary + twoHyphens + lineEnd).getBytes(StandardCharsets.UTF_8));
        return outputStream.toByteArray();
    }

    private byte[] buildCreatePostMultipartBody(Context context,
                                                String dataJson,
                                                Uri mediaUri,
                                                String boundary) throws IOException {
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // media: file part (required)
        String fileName = resolveFileName(context, mediaUri);
        if (fileName == null || fileName.isEmpty()) {
            fileName = "media.bin";
        }
        String mimeType = context.getContentResolver().getType(mediaUri);
        if (mimeType == null || mimeType.isEmpty()) {
            mimeType = "application/octet-stream";
        }

        outputStream.write((twoHyphens + boundary + lineEnd).getBytes(StandardCharsets.UTF_8));
        outputStream.write(("Content-Disposition: form-data; name=\"media\"; filename=\"" + fileName + "\"" + lineEnd)
                .getBytes(StandardCharsets.UTF_8));
        outputStream.write(("Content-Type: " + mimeType + lineEnd + lineEnd).getBytes(StandardCharsets.UTF_8));
        outputStream.write(readUriBytes(context, mediaUri));
        outputStream.write(lineEnd.getBytes(StandardCharsets.UTF_8));

        // data: JSON text part
        outputStream.write((twoHyphens + boundary + lineEnd).getBytes(StandardCharsets.UTF_8));
        outputStream.write(("Content-Disposition: form-data; name=\"data\"" + lineEnd).getBytes(StandardCharsets.UTF_8));
        outputStream.write(("Content-Type: application/json; charset=UTF-8" + lineEnd + lineEnd).getBytes(StandardCharsets.UTF_8));
        outputStream.write(dataJson.getBytes(StandardCharsets.UTF_8));
        outputStream.write(lineEnd.getBytes(StandardCharsets.UTF_8));

        outputStream.write((twoHyphens + boundary + twoHyphens + lineEnd).getBytes(StandardCharsets.UTF_8));
        return outputStream.toByteArray();
    }

    private byte[] readUriBytes(Context context, Uri uri) throws IOException {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            if (inputStream == null) {
                throw new IOException("Cannot open selected image");
            }
            byte[] data = new byte[4096];
            int nRead;
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            return buffer.toByteArray();
        }
    }

    private String resolveFileName(Context context, Uri uri) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    return cursor.getString(index);
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    private String extractUserIdFromAccessToken(String accessToken) {
        try {
            String[] parts = accessToken.split("\\.");
            if (parts.length < 2) return null;
            byte[] decoded = Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
            JSONObject payload = new JSONObject(new String(decoded, StandardCharsets.UTF_8));
            String sub = payload.optString("sub", null);
            if (sub != null && !sub.trim().isEmpty()) return sub;
            return payload.optString("userId", null);
        } catch (Exception e) {
            return null;
        }
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
    // Posts Feed
    // ─────────────────────────────────────────────────────────────────────────
    public void getPosts(Context context, String tag, PostsCallback callback) {
        String url = AppConfig.BASE_IP + ":8087/post/get-post";
        String currentUserId = extractUserIdFromAccessToken(TokenManager.getAccessToken(context));

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    try {
                        Object parsed = new JSONTokener(response).nextValue();
                        if (parsed instanceof JSONArray) {
                            callback.onSuccess(parsePosts((JSONArray) parsed, currentUserId));
                            return;
                        }

                        if (parsed instanceof JSONObject) {
                            JSONObject json = (JSONObject) parsed;
                            String code = String.valueOf(json.opt("code"));
                            if (!"200".equals(code) && !"1000".equals(code)) {
                                callback.onFailure(json.optString("message",
                                        json.optString("error", "Không tải được danh sách bài viết")));
                                return;
                            }
                            JSONArray result = json.optJSONArray("result");
                            callback.onSuccess(parsePosts(result, currentUserId));
                            return;
                        }

                        callback.onFailure("Phản hồi API không đúng định dạng");
                    } catch (Exception e) {
                        Log.e(tag, "Parse get-post response failed: " + e.getMessage());
                        callback.onFailure("Không thể đọc dữ liệu bài viết");
                    }
                },
                error -> handleVolleyError(error, tag, new ApiCallback() {
                    @Override
                    public void onSuccess() {}

                    @Override
                    public void onFailure(String message) {
                        callback.onFailure(message);
                    }
                })
        ) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                try {
                    // Force UTF-8 because backend may omit charset header.
                    String parsed = new String(response.data, StandardCharsets.UTF_8);
                    return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
                } catch (Exception e) {
                    return Response.error(new ParseError(e));
                }
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = buildAuthHeaders();
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                15_000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

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
                String message = errorJson.optString("message", "");
                if (message.isEmpty()) {
                    message = errorJson.optString("error", "");
                }
                if (message.isEmpty()) {
                    message = errorJson.optString("detail", "");
                }
                if (message.isEmpty()) {
                    message = "Lỗi mạng (" + error.networkResponse.statusCode + ")";
                }
                callback.onFailure(message);
            } catch (Exception e) {
                Log.e(tag, "Parse error body failed: " + e.getMessage());
                int statusCode = error.networkResponse.statusCode;
                callback.onFailure("Request lỗi, mã HTTP: " + statusCode);
            }
        } else {
            Log.e(tag, "Network error: " + error.toString());
            callback.onFailure("Không thể kết nối đến server. Vui lòng thử lại.");
        }
    }

    private List<Post> parsePosts(JSONArray result, String currentUserId) {
        List<Post> posts = new ArrayList<>();
        if (result == null) {
            return posts;
        }

        for (int i = 0; i < result.length(); i++) {
            JSONObject item = result.optJSONObject(i);
            if (item == null) continue;

            String id = item.optString("id", "");
            String userName = item.optString("userName", "user");
            String firstName = item.optString("firstName", "");
            String location = "";
            String avatarUrl = item.optString("avatarUrl", null);
            if (avatarUrl != null && avatarUrl.trim().isEmpty()) {
                avatarUrl = null;
            }
            boolean verified = item.optBoolean("verified", false);
            int avatarColor = colorFromUserName(userName);

            User user = new User(id, userName, location, avatarColor, verified, avatarUrl);

            String caption = item.optString("description", "");
            if (caption == null || caption.trim().isEmpty()) {
                caption = "No caption";
            }

            int likedCount = item.optInt("liked", 0);
            if (likedCount == 0) {
                likedCount = item.optInt("likeCount", 0);
            }
            boolean isLiked = resolveIsLikedByCurrentUser(item, currentUserId);
            String likedBy = firstName != null && !firstName.trim().isEmpty()
                    ? firstName
                    : userName;

            String imageUrl = item.optString("urlMedia", "");
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                imageUrl = null;
            }

            String createdAt = item.optString("createAt", "just now");
            if (createdAt == null || createdAt.trim().isEmpty()) {
                createdAt = "just now";
            }

            List<CommentModel> comments = parseCommentList(item.optJSONArray("commentList"), avatarUrl);

            posts.add(new Post(
                    user,
                    java.util.Arrays.asList(Color.parseColor("#DDDDDD")),
                    likedBy,
                    likedCount,
                    caption,
                    createdAt,
                    isLiked,
                    false,
                    imageUrl,
                    comments
            ));
            posts.get(posts.size() - 1).setPostId(id);
        }
        return posts;
    }

    private List<CommentModel> parseCommentList(JSONArray commentsArray, String fallbackAvatarUrl) {
        List<CommentModel> comments = new ArrayList<>();
        if (commentsArray == null) {
            return comments;
        }

        for (int i = 0; i < commentsArray.length(); i++) {
            JSONObject item = commentsArray.optJSONObject(i);
            if (item == null) continue;

            String username = item.optString("userName", item.optString("username", "user"));
            String content = item.optString("comment", item.optString("content", ""));
            if (content.trim().isEmpty()) {
                continue;
            }
            int likes = item.optInt("liked", item.optInt("likeCount", 0));
            String time = item.optString("createAt", item.optString("timeAgo", "now"));
            String commentAvatarUrl = item.optString("avatarUrl", item.optString("avatar", ""));
            if (commentAvatarUrl.trim().isEmpty()) {
                commentAvatarUrl = fallbackAvatarUrl;
            }

            comments.add(new CommentModel(username, content, time, likes, commentAvatarUrl));
        }
        return comments;
    }

    private boolean resolveIsLikedByCurrentUser(JSONObject item, String currentUserId) {
        if (item == null) {
            return false;
        }

        // New backend field: likedByUser
        if (item.has("likedByUser")) {
            Object v = item.opt("likedByUser");
            if (v instanceof Boolean) {
                return (Boolean) v;
            }
            if (v instanceof Number) {
                return ((Number) v).intValue() != 0;
            }
            String s = item.optString("likedByUser", "");
            if ("true".equalsIgnoreCase(s) || "1".equals(s)) {
                return true;
            }
        }

        if (currentUserId == null || currentUserId.trim().isEmpty()) {
            return false;
        }

        // Common boolean fields from backend contracts
        if (item.has("isLiked") && item.optBoolean("isLiked", false)) return true;
        if (item.has("likedByMe") && item.optBoolean("likedByMe", false)) return true;
        if (item.has("likedByCurrentUser") && item.optBoolean("likedByCurrentUser", false)) return true;

        // Common array fields of user IDs
        String[] likeArrayFields = {"likedUserIds", "likedByUserIds", "likedBy", "likes", "likers"};
        for (String field : likeArrayFields) {
            JSONArray arr = item.optJSONArray(field);
            if (arr == null) continue;
            for (int i = 0; i < arr.length(); i++) {
                Object entry = arr.opt(i);
                if (entry instanceof String) {
                    if (currentUserId.equals(entry)) return true;
                } else if (entry instanceof JSONObject) {
                    JSONObject obj = (JSONObject) entry;
                    String uid = obj.optString("userId", obj.optString("id", ""));
                    if (currentUserId.equals(uid)) return true;
                }
            }
        }

        // String field fallback
        String likedByMeId = item.optString("likedByMeId", "");
        return currentUserId.equals(likedByMeId);
    }

    private int colorFromUserName(String userName) {
        if (userName == null || userName.isEmpty()) {
            return Color.parseColor("#3F51B5");
        }
        int hash = Math.abs(userName.hashCode());
        int r = 80 + (hash % 120);
        int g = 80 + ((hash / 7) % 120);
        int b = 80 + ((hash / 13) % 120);
        return Color.rgb(r, g, b);
    }
}
