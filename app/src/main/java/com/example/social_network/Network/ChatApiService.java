package com.example.social_network.Network;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.social_network.Config.AppConfig;
import com.example.social_network.Model.ConservationDTO;
import com.example.social_network.Model.ConversationRequestDto;
import com.example.social_network.Model.ConversationResponseDto;
import com.example.social_network.Model.MessageDto;
import com.example.social_network.Model.SendMessageRequestDto;
import com.example.social_network.Utils.TokenManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatApiService implements ChatApiInterface {
    private static final String PROFILE_FOLLOWING_PATH = "/profile/getFollowed";
    private static final String CONVERSATIONS_PATH = "/my-conversations";
    private static final String CONVERSATION_CREATE_PATH = "/chat/conversation/create";
    private static final String MESSAGE_CREATE_PATH = "/chat/messages/create";

    @Override
    public String getCurrentUserId(Context context) {
        String token = TokenManager.getAccessToken(context);
        if (token == null || token.isEmpty()) {
            return null;
        }

        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            return null;
        }

        try {
            byte[] decodedBytes = Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
            String payload = new String(decodedBytes, StandardCharsets.UTF_8);
            JSONObject jsonPayload = new JSONObject(payload);
            return jsonPayload.optString("sub", null);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public void getConservation(Context context, FollowingUsersCallback callback) {
        String url = AppConfig.BASE_URL_CONSERVATION + CONVERSATIONS_PATH;
        Log.e("url request", url);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> callback.onSuccess(parseFollowingUsers(response, getCurrentUserId(context))),
                error -> callback.onFailure(parseError(error))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return buildAuthHeaders(context);
            }
        };

        newQueue(context).add(request);
    }

    @Override
    public void getOrCreateConversation(Context context,
                                        ConversationRequestDto requestDto,
                                        ConversationCallback callback) {
        if (requestDto.getReceiverId() == null || requestDto.getReceiverId().isEmpty()) {
            callback.onFailure("Thiếu người nhận");
            return;
        }
        String url = AppConfig.BSSE_URL_CHAT + CONVERSATION_CREATE_PATH;
        JSONObject body = new JSONObject();
        try {
            body.put("type", "DIRECT");
            JSONArray participantIds = new JSONArray();
            participantIds.put(requestDto.getReceiverId());
            body.put("participantIds", participantIds);
        } catch (JSONException e) {
            callback.onFailure("Invalid request body");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    if (!"1000".equals(String.valueOf(response.opt("code")))) {
                        String msg = response.optString("message", "");
                        if (msg == null || msg.isEmpty()) {
                            msg = "Tạo cuộc trò chuyện thất bại";
                        }
                        callback.onFailure(msg);
                        return;
                    }
                    callback.onSuccess(parseConversationResponse(response.optJSONObject("result")));
                },
                error -> callback.onFailure(parseError(error))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return buildAuthHeaders(context);
            }
        };

        newQueue(context).add(request);
    }

    @Override
    public void getMessages(Context context, String conversationId, MessagesCallback callback) {
        if (conversationId == null || conversationId.isEmpty()) {
            callback.onFailure("Thiếu conversationId");
            return;
        }
        String token = TokenManager.getAccessToken(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Chưa có access token — vui lòng đăng nhập lại");
            return;
        }
        String url = Uri.parse(AppConfig.BSSE_URL_CHAT + "/chat/messages")
                .buildUpon()
                .appendQueryParameter("conversationId", conversationId)
                .build()
                .toString();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    if (!"1000".equals(String.valueOf(response.opt("code")))) {
                        String msg = response.optString("message", "");
                        if (msg == null || msg.isEmpty()) {
                            msg = "Không tải được tin nhắn";
                        }
                        callback.onFailure(msg);
                        return;
                    }
                    callback.onSuccess(parseMessages(extractMessagesResultArray(response)));
                },
                error -> callback.onFailure(parseError(error))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return buildAuthHeaders(context);
            }
        };

        newQueue(context).add(request);
    }

    @Override
    public void postMessage(Context context, SendMessageRequestDto requestDto, SendMessageCallback callback) {
        String token = TokenManager.getAccessToken(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Chưa có access token — vui lòng đăng nhập lại");
            return;
        }
        if (requestDto.getConversationId() == null || requestDto.getConversationId().isEmpty()) {
            callback.onFailure("Thiếu conversationId");
            return;
        }

        String url = AppConfig.BSSE_URL_CHAT + MESSAGE_CREATE_PATH;
        JSONObject body = new JSONObject();
        try {
            body.put("conversationId", requestDto.getConversationId());
            body.put("message", requestDto.getMessage());
        } catch (JSONException e) {
            callback.onFailure("Invalid request body");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    if (!"1000".equals(String.valueOf(response.opt("code")))) {
                        String msg = response.optString("message", "");
                        if (msg == null || msg.isEmpty()) {
                            msg = "Gửi tin nhắn thất bại";
                        }
                        callback.onFailure(msg);
                        return;
                    }
                    MessageDto sent = parseMessageItem(response.optJSONObject("result"));
                    callback.onSuccess(sent);
                },
                error -> callback.onFailure(parseError(error))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return buildAuthHeaders(context);
            }
        };

        newQueue(context).add(request);
    }

    private RequestQueue newQueue(Context context) {
        return Volley.newRequestQueue(context.getApplicationContext());
    }

    private Map<String, String> buildAuthHeaders(Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");

        String accessToken = TokenManager.getAccessToken(context);
        if (accessToken != null && !accessToken.isEmpty()) {
            headers.put("Authorization", "Bearer " + accessToken.trim());
        }
        return headers;
    }

    private List<ConservationDTO> parseFollowingUsers(JSONObject response, String currentUserId) {
        List<ConservationDTO> conservationDTOList = new ArrayList<>();
        if (response == null) {
            return conservationDTOList;
        }

        JSONArray listFollower = response.optJSONArray("result");
        if (listFollower == null) {
            return conservationDTOList;
        }

        for (int i = 0; i < listFollower.length(); i++) {
            JSONObject item = listFollower.optJSONObject(i);
            if (item == null) {
                continue;
            }

            String convDocId = extractConversationDocumentId(item);

            JSONArray participantsResponse = item.optJSONArray("participants");
            JSONObject participant = pickOtherParticipant(participantsResponse, currentUserId);
            if (participant == null) {
                continue;
            }

            String peerUserId = participant.optString("userId", null);
            String conversationAvatar = participant.isNull("avatar")
                    ? null
                    : participant.optString("avatar", null);
            String userName = participant.isNull("userName")
                    ? null
                    : participant.optString("userName", null);
            if (userName == null || userName.isEmpty()) {
                userName = peerUserId;
            }

            if (userName != null && !userName.isEmpty()) {
                conservationDTOList.add(new ConservationDTO(peerUserId, conversationAvatar, userName, convDocId));
            }
        }

        return conservationDTOList;
    }

    /**
     * Prefer the other participant in a DIRECT thread (not the current user).
     */
    private static JSONObject pickOtherParticipant(JSONArray participants, String myUserId) {
        if (participants == null || participants.length() == 0) {
            return null;
        }
        if (myUserId != null && !myUserId.isEmpty()) {
            for (int i = 0; i < participants.length(); i++) {
                JSONObject p = participants.optJSONObject(i);
                if (p == null) {
                    continue;
                }
                String uid = p.optString("userId", "");
                if (!myUserId.equals(uid)) {
                    return p;
                }
            }
        }
        return participants.optJSONObject(0);
    }

    /**
     * Mongo / API conversation id on the parent conversation object.
     */
    private static String extractConversationDocumentId(JSONObject item) {
        if (item == null) {
            return null;
        }
        String[] keys = {"id", "_id", "conversationId"};
        for (String key : keys) {
            if (!item.has(key) || item.isNull(key)) {
                continue;
            }
            Object v = item.opt(key);
            if (v instanceof JSONObject) {
                String oid = ((JSONObject) v).optString("$oid", "");
                if (!oid.isEmpty()) {
                    return oid;
                }
                continue;
            }
            String s = item.optString(key, "");
            if (!s.isEmpty() && !"null".equalsIgnoreCase(s)) {
                return s;
            }
        }
        return null;
    }

    private static JSONArray extractMessagesResultArray(JSONObject response) {
        if (response == null) {
            return null;
        }
        Object raw = response.opt("result");
        if (raw instanceof JSONArray) {
            return (JSONArray) raw;
        }
        if (raw instanceof JSONObject) {
            JSONObject box = (JSONObject) raw;
            JSONArray a = box.optJSONArray("messages");
            if (a != null) {
                return a;
            }
            a = box.optJSONArray("data");
            if (a != null) {
                return a;
            }
            a = box.optJSONArray("content");
            if (a != null) {
                return a;
            }
        }
        return null;
    }

    private static boolean readMeFlag(JSONObject item) {
        if (item == null || !item.has("me") || item.isNull("me")) {
            return false;
        }
        Object v = item.opt("me");
        if (v instanceof Boolean) {
            return (Boolean) v;
        }
        if (v instanceof Number) {
            return ((Number) v).intValue() != 0;
        }
        String s = item.optString("me", "");
        return "true".equalsIgnoreCase(s) || "1".equals(s);
    }

    @Override
    public void getFollowedProfiles(Context context, FollowingUsersCallback callback) {
        String url = AppConfig.BSSE_URL_PROFILE + PROFILE_FOLLOWING_PATH;
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    if (!"1000".equals(String.valueOf(response.opt("code")))) {
                        callback.onFailure(response.optString("message", "Request failed"));
                        return;
                    }
                    callback.onSuccess(parseFollowedProfileUsers(response));
                },
                error -> callback.onFailure(parseError(error))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return buildAuthHeaders(context);
            }
        };

        newQueue(context).add(request);
    }

    private List<ConservationDTO> parseFollowedProfileUsers(JSONObject response) {
        List<ConservationDTO> list = new ArrayList<>();
        if (response == null) {
            return list;
        }

        JSONArray arr = response.optJSONArray("result");
        if (arr == null) {
            return list;
        }

        for (int i = 0; i < arr.length(); i++) {
            JSONObject item = arr.optJSONObject(i);
            if (item == null) {
                continue;
            }

            String userId = item.optString("userId", "");
            String userName = item.isNull("userName") ? null : item.optString("userName", null);
            String firstName = item.optString("firstName", "");
            String lastName = item.optString("lastName", "");
            String display = (userName != null && !userName.isEmpty())
                    ? userName
                    : (firstName + " " + lastName).trim();
            if (display.isEmpty()) {
                display = userId;
            }

            String avatar = item.isNull("avatar") ? null : item.optString("avatar", null);
            list.add(new ConservationDTO(userId, avatar, display));
        }

        return list;
    }

    /**
     * Chat create API returns {@code id} (may be null) and {@code participantsHash};
     * messages API may use either — prefer non-empty {@code id}, else {@code participantsHash}.
     */
    private ConversationResponseDto parseConversationResponse(JSONObject result) {
        if (result == null) {
            return new ConversationResponseDto(null);
        }
        String docId = extractConversationDocumentId(result);
        if (docId != null) {
            return new ConversationResponseDto(docId);
        }
        String participantsHash = result.optString("participantsHash", "");
        if (!participantsHash.isEmpty()) {
            return new ConversationResponseDto(participantsHash);
        }
        return new ConversationResponseDto(null);
    }

    private MessageDto parseMessageItem(JSONObject item) {
        if (item == null) {
            return null;
        }

        String id = item.optString("id", "");
        String conversationId = item.optString("conversationId", "");
        boolean fromMe = readMeFlag(item);
        String content = item.optString("message", "");
        if (content.isEmpty()) {
            content = item.optString("content", "");
        }
        String timestamp = item.optString("createdDate", "");
        if (timestamp.isEmpty()) {
            timestamp = item.optString("timestamp", "");
        }

        String senderId = "";
        String incomingAvatar = null;
        JSONObject sender = item.optJSONObject("sender");
        if (sender != null) {
            senderId = sender.optString("userId", "");
            if (!sender.isNull("avatar")) {
                String av = sender.optString("avatar", "");
                if (!av.isEmpty()) {
                    incomingAvatar = av;
                }
            }
        }

        return new MessageDto(id, conversationId, senderId, content, timestamp, fromMe, incomingAvatar);
    }

    private List<MessageDto> parseMessages(JSONArray arr) {
        List<MessageDto> messages = new ArrayList<>();
        if (arr == null) {
            return messages;
        }
        for (int i = 0; i < arr.length(); i++) {
            MessageDto dto = parseMessageItem(arr.optJSONObject(i));
            if (dto != null) {
                messages.add(dto);
            }
        }
        return messages;
    }

    private String parseError(com.android.volley.VolleyError error) {
        if (error.networkResponse != null && error.networkResponse.data != null) {
            return new String(error.networkResponse.data, StandardCharsets.UTF_8);
        }
        return "Network request failed";
    }
}
