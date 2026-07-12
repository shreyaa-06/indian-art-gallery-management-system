package com.artgallery.util;

import com.artgallery.model.User;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionManager {
    private static final Map<String, SessionData> sessions = new ConcurrentHashMap<>();

    private SessionManager() {}

    public static String createSession(User user) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, new SessionData(user, System.currentTimeMillis()));
        return token;
    }

    public static User getUser(String token) {
        if (token == null) return null;
        SessionData data = sessions.get(token);
        if (data == null) return null;
        return data.user();
    }

    public static void invalidate(String token) {
        if (token != null) sessions.remove(token);
    }

    public static void cleanup(long maxAgeMs) {
        long now = System.currentTimeMillis();
        sessions.entrySet().removeIf(e -> now - e.getValue().createdAt() > maxAgeMs);
    }

    private record SessionData(User user, long createdAt) {}
}
