package com.artgallery.controller;

import com.artgallery.model.User;
import com.artgallery.service.AuthService;
import com.artgallery.util.HttpUtil;
import com.artgallery.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

public class AuthController implements HttpHandler {
    private final AuthService authService = new AuthService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("POST".equals(method) && path.equals("/api/auth/login")) {
                handleLogin(exchange);
            } else if ("POST".equals(method) && path.equals("/api/auth/logout")) {
                handleLogout(exchange);
            } else if ("GET".equals(method) && path.equals("/api/auth/me")) {
                handleMe(exchange);
            } else {
                HttpUtil.sendError(exchange, 404, "Not found");
            }
        } catch (IllegalArgumentException e) {
            HttpUtil.sendError(exchange, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.sendError(exchange, 500, "Internal server error");
        }
    }

    private void handleLogin(HttpExchange exchange) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> body = JsonUtil.fromJson(HttpUtil.readBody(exchange), Map.class);
        String username = body.get("username") != null ? body.get("username").toString() : "";
        String password = body.get("password") != null ? body.get("password").toString() : "";

        User user = authService.login(username, password);
        String token = authService.createSession(user);
        HttpUtil.setSessionCookie(exchange, token);
        HttpUtil.sendJson(exchange, 200, Map.of(
                "user", Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "fullName", user.getFullName(),
                        "role", user.getRole()
                )
        ));
    }

    private void handleLogout(HttpExchange exchange) throws IOException {
        String token = HttpUtil.getSessionToken(exchange);
        authService.logout(token);
        HttpUtil.clearSessionCookie(exchange);
        HttpUtil.sendJson(exchange, 200, Map.of("message", "Logged out"));
    }

    private void handleMe(HttpExchange exchange) throws IOException {
        User user = HttpUtil.requireAuth(exchange);
        if (user == null) return;
        HttpUtil.sendJson(exchange, 200, Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "fullName", user.getFullName(),
                "role", user.getRole()
        ));
    }
}
