package com.example.social_network.Network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.social_network.Config.AppConfig;
import com.example.social_network.Utils.TokenManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Socket.IO client for Netty-socketio (corundumstudio). Đổi tên event / payload cho khớp backend.
 *
 * <p>Mặc định:</p>
 * <ul>
 *   <li>Sau khi {@link Socket#EVENT_CONNECT}, emit {@link #CLIENT_JOIN_EVENT} với body
 *   {@code {"conversationId":"..."}}</li>
 *   <li>Lắng nghe {@link #SERVER_MESSAGE_EVENT} — payload giống REST message (message, me, sender, createdDate)</li>
 * </ul>
 */
public class ChatSocketManager {

    private static final String TAG = "ChatSocket";

    /** Client → server: vào phòng chat (đổi nếu backend dùng tên khác, vd "subscribe"). */
    public static String CLIENT_JOIN_EVENT = "join";

    /** Server → client: tin mới (đổi nếu backend dùng tên khác, vd "chat_message"). */
    public static String SERVER_MESSAGE_EVENT = "message";

    /** Lắng nghe thêm các tên event thường gặp trên Netty-socketio / Spring. */
    private static final String[] SERVER_MESSAGE_ALIASES = {
            "message", "newMessage", "new_message", "chat_message",
            "receiveMessage", "chat", "msg"
    };

    public interface Listener {
        /** Tin nhắn realtime; {@code fromMe} thường bỏ qua ở UI vì đã hiển thị lúc gửi HTTP. */
        void onNewChatMessage(String text, boolean fromMe, String createdDate, String incomingAvatarUrl);

        void onConnected();

        void onDisconnected();

        void onError(String message);
    }

    private final Context appContext;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Socket socket;
    private Listener listener;

    public ChatSocketManager(Context context) {
        this.appContext = context.getApplicationContext();
    }

    /**
     * Kết nối Socket.IO và đăng ký phòng {@code conversationId}.
     */
    public void connect(String conversationId, Listener listener) {
        this.listener = listener;
        disconnect();

        String token = TokenManager.getAccessToken(appContext);
        IO.Options options = new IO.Options();
        options.reconnection = true;
        options.reconnectionDelay = 1_000;
        options.reconnectionDelayMax = 5_000;
        options.timeout = 10_000;
        try {
            if (token != null && !token.isEmpty()) {
                options.query = "token=" + URLEncoder.encode(token, StandardCharsets.UTF_8.name());
            }
        } catch (Exception ignored) {
        }

        try {
            socket = IO.socket(AppConfig.SOCKET_IO_URL, options);
        } catch (URISyntaxException e) {
            dispatchError("Socket URL không hợp lệ: " + e.getMessage());
            return;
        }

        socket.on(Socket.EVENT_CONNECT, onConnect(conversationId));
        socket.on(Socket.EVENT_DISCONNECT, args -> dispatch(() -> {
            if (listener != null) {
                listener.onDisconnected();
            }
        }));
        socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);

        Emitter.Listener onMsg = createServerMessageListener();
        for (String ev : SERVER_MESSAGE_ALIASES) {
            socket.on(ev, onMsg);
        }
        if (!java.util.Arrays.asList(SERVER_MESSAGE_ALIASES).contains(SERVER_MESSAGE_EVENT)) {
            socket.on(SERVER_MESSAGE_EVENT, onMsg);
        }

        socket.connect();
    }

    private Emitter.Listener onConnect(String conversationId) {
        return args -> {
            try {
                JSONObject body = new JSONObject();
                body.put("conversationId", conversationId);
                socket.emit(CLIENT_JOIN_EVENT, body);
                // Một số server chỉ nhận room là chuỗi id:
                socket.emit(CLIENT_JOIN_EVENT, conversationId);
            } catch (JSONException e) {
                Log.e(TAG, "join payload", e);
            }
            dispatch(() -> {
                if (listener != null) {
                    listener.onConnected();
                }
            });
        };
    }

    private final Emitter.Listener onConnectError = args -> {
        String msg = "Lỗi kết nối socket";
        if (args != null && args.length > 0 && args[0] != null) {
            msg = args[0].toString();
        }
        Log.w(TAG, "connect_error: " + msg);
        String finalMsg = msg;
        dispatch(() -> {
            if (listener != null) {
                listener.onError(finalMsg);
            }
        });
    };

    private Emitter.Listener createServerMessageListener() {
        return args -> {
            if (args == null || args.length == 0) {
                return;
            }
            JSONObject item = parseFirstArgAsJson(args[0]);
            item = unwrapMessagePayload(item);
            if (item == null) {
                return;
            }

            String myId = new ChatApiService().getCurrentUserId(appContext);
            JSONObject sender = item.optJSONObject("sender");
            String senderId = sender != null ? sender.optString("userId", "") : "";
            boolean fromMe;
            if (myId != null && !myId.isEmpty() && !senderId.isEmpty()) {
                fromMe = myId.equals(senderId);
            } else {
                fromMe = readMeFlag(item);
            }

            String text = item.optString("message", "");
            if (text.isEmpty()) {
                text = item.optString("content", "");
            }
            String created = item.optString("createdDate", "");
            if (created.isEmpty()) {
                created = item.optString("timestamp", "");
            }
            String incomingAvatar = null;
            if (sender != null && !sender.isNull("avatar")) {
                String av = sender.optString("avatar", "");
                if (!av.isEmpty()) {
                    incomingAvatar = av;
                }
            }

            Log.d(TAG, "socket msg fromMe=" + fromMe + " text=" + text);

            String finalText = text;
            String finalCreated = created;
            String finalAvatar = incomingAvatar;
            boolean finalFromMe = fromMe;
            dispatch(() -> {
                if (listener != null) {
                    listener.onNewChatMessage(finalText, finalFromMe, finalCreated, finalAvatar);
                }
            });
        };
    }

    private static JSONObject parseFirstArgAsJson(Object raw) {
        if (raw instanceof JSONObject) {
            return (JSONObject) raw;
        }
        if (raw instanceof String) {
            try {
                return new JSONObject((String) raw);
            } catch (JSONException e) {
                return null;
            }
        }
        return null;
    }

    /** Một số server bọc payload trong result / data. */
    private static JSONObject unwrapMessagePayload(JSONObject root) {
        if (root == null) {
            return null;
        }
        if (root.has("result") && root.opt("result") instanceof JSONObject) {
            return root.optJSONObject("result");
        }
        if (root.has("data") && root.opt("data") instanceof JSONObject) {
            return root.optJSONObject("data");
        }
        return root;
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

    public void disconnect() {
        if (socket != null) {
            socket.off();
            socket.disconnect();
            socket = null;
        }
        listener = null;
    }

    private void dispatchError(String message) {
        dispatch(() -> {
            if (listener != null) {
                listener.onError(message);
            }
        });
    }

    private void dispatch(Runnable r) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            r.run();
        } else {
            mainHandler.post(r);
        }
    }
}
