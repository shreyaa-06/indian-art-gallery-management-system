package com.artgallery.controller;

import com.artgallery.model.User;
import com.artgallery.service.ExhibitionService;
import com.artgallery.util.HttpUtil;
import com.artgallery.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

public class ExhibitionController implements HttpHandler {
    private final ExhibitionService exhibitionService = new ExhibitionService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            User user = HttpUtil.requireAuth(exchange);
            if (user == null) return;

            if ("GET".equals(method) && path.equals("/api/exhibitions")) {
                Map<String, String> params = HttpUtil.getQueryParams(exchange);
                int page = HttpUtil.parseIntParam(params, "page", 1);
                int pageSize = HttpUtil.parseIntParam(params, "pageSize", 10);
                String status = params.get("status");
                HttpUtil.sendJson(exchange, 200, exhibitionService.findAll(status, page, pageSize));
            } else if ("GET".equals(method) && path.startsWith("/api/exhibitions/") && !path.endsWith("/artworks")) {
                String idPart = path.substring("/api/exhibitions/".length());
                if (idPart.contains("/")) {
                    HttpUtil.sendError(exchange, 404, "Not found");
                    return;
                }
                int id = Integer.parseInt(idPart);
                HttpUtil.sendJson(exchange, 200, exhibitionService.findById(id));
            } else if ("POST".equals(method) && path.equals("/api/exhibitions")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = JsonUtil.fromJson(HttpUtil.readBody(exchange), Map.class);
                HttpUtil.sendJson(exchange, 201, exhibitionService.create(body, user));
            } else if ("PUT".equals(method) && path.startsWith("/api/exhibitions/") && path.endsWith("/artworks")) {
                String idPart = path.substring("/api/exhibitions/".length(), path.length() - "/artworks".length());
                int id = Integer.parseInt(idPart);
                @SuppressWarnings("unchecked")
                Map<String, Object> body = JsonUtil.fromJson(HttpUtil.readBody(exchange), Map.class);
                exhibitionService.assignArtworks(id, body, user);
                HttpUtil.sendJson(exchange, 200, exhibitionService.findById(id));
            } else if ("PUT".equals(method) && path.startsWith("/api/exhibitions/")) {
                int id = Integer.parseInt(path.substring("/api/exhibitions/".length()));
                @SuppressWarnings("unchecked")
                Map<String, Object> body = JsonUtil.fromJson(HttpUtil.readBody(exchange), Map.class);
                HttpUtil.sendJson(exchange, 200, exhibitionService.update(id, body, user));
            } else if ("DELETE".equals(method) && path.startsWith("/api/exhibitions/")) {
                int id = Integer.parseInt(path.substring("/api/exhibitions/".length()));
                exhibitionService.delete(id, user);
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
