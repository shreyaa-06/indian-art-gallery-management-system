package com.artgallery.util;

import com.artgallery.model.User;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class HttpUtil {
    private HttpUtil() {}

    public static void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = JsonUtil.toJson(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void sendError(HttpExchange exchange, int status, String message) throws IOException {
        sendJson(exchange, status, Map.of("error", message));
    }

    public static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public static Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isBlank()) return params;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                params.put(
                        URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(kv[1], StandardCharsets.UTF_8)
                );
            }
        }
        return params;
    }

    public static Map<String, String> getQueryParams(HttpExchange exchange) {
        String query = exchange.getRequestURI().getQuery();
        return parseQuery(query);
    }

    public static String getSessionToken(HttpExchange exchange) {
        String cookie = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookie == null) return null;
        for (String part : cookie.split(";")) {
            part = part.trim();
            if (part.startsWith("SESSION=")) {
                return part.substring(8);
            }
        }
        return null;
    }

    public static User requireAuth(HttpExchange exchange) throws IOException {
        String token = getSessionToken(exchange);
        User user = SessionManager.getUser(token);
        if (user == null) {
            sendError(exchange, 401, "Unauthorized");
            return null;
        }
        return user;
    }

    public static void setSessionCookie(HttpExchange exchange, String token) {
        exchange.getResponseHeaders().add("Set-Cookie",
                "SESSION=" + token + "; Path=/; HttpOnly; SameSite=Strict");
    }

    public static void clearSessionCookie(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Set-Cookie",
                "SESSION=; Path=/; HttpOnly; Max-Age=0; SameSite=Strict");
    }

    public static int parseIntParam(Map<String, String> params, String key, int defaultVal) {
        String val = params.get(key);
        if (val == null || val.isBlank()) return defaultVal;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    public static String getPathParam(String path, String prefix) {
        if (path.startsWith(prefix)) {
            return path.substring(prefix.length());
        }
        return "";
    }
}
