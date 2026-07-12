package com.artgallery.controller;

import com.artgallery.model.User;
import com.artgallery.service.DashboardService;
import com.artgallery.util.HttpUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class DashboardController implements HttpHandler {
    private final DashboardService dashboardService = new DashboardService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            HttpUtil.sendError(exchange, 405, "Method not allowed");
            return;
        }
        try {
            User user = HttpUtil.requireAuth(exchange);
            if (user == null) return;
            HttpUtil.sendJson(exchange, 200, dashboardService.getStats());
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.sendError(exchange, 500, "Internal server error");
        }
    }
}
