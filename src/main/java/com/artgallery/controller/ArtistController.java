package com.artgallery.controller;

import com.artgallery.model.User;
import com.artgallery.service.ArtistService;
import com.artgallery.util.HttpUtil;
import com.artgallery.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

public class ArtistController implements HttpHandler {
    private final ArtistService artistService = new ArtistService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            User user = HttpUtil.requireAuth(exchange);
            if (user == null) return;

            if ("GET".equals(method) && path.equals("/api/artists/all")) {
                HttpUtil.sendJson(exchange, 200, artistService.findAllSimple());
            } else if ("GET".equals(method) && path.equals("/api/artists")) {
                Map<String, String> params = HttpUtil.getQueryParams(exchange);
                int page = HttpUtil.parseIntParam(params, "page", 1);
                int pageSize = HttpUtil.parseIntParam(params, "pageSize", 10);
                String search = params.get("search");
                HttpUtil.sendJson(exchange, 200, artistService.findAll(search, page, pageSize));
            } else if ("GET".equals(method) && path.startsWith("/api/artists/")) {
                int id = Integer.parseInt(path.substring("/api/artists/".length()));
                HttpUtil.sendJson(exchange, 200, artistService.findById(id));
            } else if ("POST".equals(method) && path.equals("/api/artists")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = JsonUtil.fromJson(HttpUtil.readBody(exchange), Map.class);
                HttpUtil.sendJson(exchange, 201, artistService.create(body, user));
            } else if ("PUT".equals(method) && path.startsWith("/api/artists/")) {
                int id = Integer.parseInt(path.substring("/api/artists/".length()));
                @SuppressWarnings("unchecked")
                Map<String, Object> body = JsonUtil.fromJson(HttpUtil.readBody(exchange), Map.class);
                HttpUtil.sendJson(exchange, 200, artistService.update(id, body, user));
            } else if ("DELETE".equals(method) && path.startsWith("/api/artists/")) {
                int id = Integer.parseInt(path.substring("/api/artists/".length()));
                artistService.delete(id, user);
                HttpUtil.sendJson(exchange, 200, Map.of("message", "Deleted"));
            } else {
                HttpUtil.sendError(exchange, 404, "Not found");
            }
        } catch (NumberFormatException e) {
            HttpUtil.sendError(exchange, 400, "Invalid ID");
        } catch (IllegalArgumentException e) {
            HttpUtil.sendError(exchange, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.sendError(exchange, 500, "Internal server error");
        }
    }
}
