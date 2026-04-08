package com.example.social_network.Config;

public class AppConfig {
    public static final String BASE_URL = "http://10.39.86.195:8888/api/v1";
    public  static  final String BSSE_URL_CHAT = "http://10.39.86.195:8085";
    public  static  final String BSSE_URL_PROFILE = "http://10.39.86.195:8081";
    public static final String BASE_URL_CONSERVATION = "http://10.39.86.195:8085/chat/conversation";

    /** Socket.IO (Netty) — same host as chat/profile, port theo server (vd 8099). */
    public static final String SOCKET_IO_URL = "http://10.39.86.195:8099";

    /** Notification service — đăng ký FCM device token. */
    public static final String BASE_URL_NOTIFICATION = "http://10.39.86.195:8082";

    public static String ACCESS_TOKEN = null;
}
