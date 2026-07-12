package com.artgallery.service;

import com.artgallery.dao.*;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardService {
    private final ArtworkDAO artworkDAO = new ArtworkDAO();
    private final ArtistDAO artistDAO = new ArtistDAO();
    private final ExhibitionDAO exhibitionDAO = new ExhibitionDAO();
    private final ActivityDAO activityDAO = new ActivityDAO();

    public Map<String, Object> getStats() throws SQLException {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalArtworks", artworkDAO.count());
        stats.put("totalArtists", artistDAO.count());
        stats.put("activeExhibitions", exhibitionDAO.countActive());
        stats.put("recentActivity", activityDAO.findRecent(10));
        return stats;
    }
}
