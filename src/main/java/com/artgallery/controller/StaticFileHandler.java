package com.artgallery.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class StaticFileHandler implements HttpHandler {
    private final Path webRoot;

    public StaticFileHandler(Path webRoot) {
        this.webRoot = webRoot;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) {
            path = "/pages/login.html";
        }

        Path filePath = webRoot.resolve(path.substring(1)).normalize();
        if (!filePath.startsWith(webRoot) || !Files.exists(filePath) || Files.isDirectory(filePath)) {
            // SPA-style fallback for page routes
            if (path.startsWith("/pages/")) {
                filePath = webRoot.resolve(path.substring(1));
            } else {
                exchange.sendResponseHeaders(404, -1);
                exchange.close();
                return;
            }
        }

        if (!Files.exists(filePath)) {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
            return;
        }

        String contentType = getContentType(filePath.toString());
        byte[] bytes = Files.readAllBytes(filePath);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String getContentType(String filename) {
        if (filename.endsWith(".html")) return "text/html; charset=UTF-8";
        if (filename.endsWith(".css")) return "text/css; charset=UTF-8";
        if (filename.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (filename.endsWith(".svg")) return "image/svg+xml";
        if (filename.endsWith(".png")) return "image/png";
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) return "image/jpeg";
        if (filename.endsWith(".woff2")) return "font/woff2";
        return "application/octet-stream";
    }
}
