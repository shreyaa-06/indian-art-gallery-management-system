package com.artgallery;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.concurrent.Executors;

import com.artgallery.config.AppConfig;
import com.artgallery.config.DatabaseInitializer;
import com.artgallery.controller.ArtistController;
import com.artgallery.controller.ArtworkController;
import com.artgallery.controller.AuthController;
import com.artgallery.controller.CustomerController;
import com.artgallery.controller.DashboardController;
import com.artgallery.controller.ExhibitionController;
import com.artgallery.controller.ReportController;
import com.artgallery.controller.StaticFileHandler;
import com.sun.net.httpserver.HttpServer;

public class Main {
    public static void main(String[] args) throws IOException {
        DatabaseInitializer.initialize();

        int port = AppConfig.getInt("server.port", 8080);
        Path webRoot = Path.of("web").toAbsolutePath();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // API routes
        server.createContext("/api/auth", new AuthController());
        server.createContext("/api/dashboard", new DashboardController());
        server.createContext("/api/artists", new ArtistController());
        server.createContext("/api/artworks", new ArtworkController());
        server.createContext("/api/exhibitions", new ExhibitionController());
        server.createContext("/api/customers", new CustomerController());
        server.createContext("/api/reports", new ReportController());

        // Static files
        server.createContext("/", new StaticFileHandler(webRoot));

        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("ShilpSangraha running at http://localhost:" + port);
        System.out.println("Default login: admin / admin123");
    }
}
