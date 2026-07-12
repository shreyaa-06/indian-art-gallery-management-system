package com.artgallery.service;

import com.artgallery.dao.ActivityDAO;
import com.artgallery.dao.ArtworkDAO;
import com.artgallery.model.Artwork;
import com.artgallery.model.PagedResult;
import com.artgallery.model.User;
import com.artgallery.util.ValidationUtil;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;

public class ArtworkService {
    private final ArtworkDAO artworkDAO = new ArtworkDAO();
    private final ActivityDAO activityDAO = new ActivityDAO();

    private static final String[] VALID_CATEGORIES = {
            "Painting", "Sculpture", "Photography", "Digital", "Mixed Media", "Installation", "Other"
    };
    private static final String[] VALID_STATUSES = {
            "available", "on_loan", "sold", "in_exhibition"
    };

    public PagedResult<Artwork> findAll(String search, String category, int page, int pageSize) throws SQLException {
        return artworkDAO.findAll(search, category, page, pageSize);
    }

    public Artwork findById(int id) throws SQLException {
        Artwork artwork = artworkDAO.findById(id);
        if (artwork == null) throw new IllegalArgumentException("Artwork not found");
        return artwork;
    }

    public Artwork create(Map<String, Object> data, User user) throws SQLException {
        Artwork artwork = mapFromData(data);
        validate(artwork);
        int id = artworkDAO.create(artwork);
        artwork.setId(id);
        activityDAO.log("create", "artwork", id, "Added artwork \"" + artwork.getTitle() + "\"", user.getId());
        return artworkDAO.findById(id);
    }

    public Artwork update(int id, Map<String, Object> data, User user) throws SQLException {
        if (artworkDAO.findById(id) == null) throw new IllegalArgumentException("Artwork not found");
        Artwork artwork = mapFromData(data);
        artwork.setId(id);
        validate(artwork);
        artworkDAO.update(artwork);
        activityDAO.log("update", "artwork", id, "Updated artwork \"" + artwork.getTitle() + "\"", user.getId());
        return artworkDAO.findById(id);
    }

    public void delete(int id, User user) throws SQLException {
        Artwork existing = artworkDAO.findById(id);
        if (existing == null) throw new IllegalArgumentException("Artwork not found");
        if (!artworkDAO.delete(id)) throw new SQLException("Failed to delete artwork");
        activityDAO.log("delete", "artwork", id, "Deleted artwork \"" + existing.getTitle() + "\"", user.getId());
    }

    private void validate(Artwork artwork) {
        ValidationUtil.requireNonBlank(artwork.getTitle(), "Title");
        ValidationUtil.requirePositive(artwork.getArtistId(), "Artist");
        ValidationUtil.requireNonBlank(artwork.getCategory(), "Category");
        ValidationUtil.requireNonBlank(artwork.getStatus(), "Status");
        if (!isValid(artwork.getCategory(), VALID_CATEGORIES)) {
            throw new IllegalArgumentException("Invalid category");
        }
        if (!isValid(artwork.getStatus(), VALID_STATUSES)) {
            throw new IllegalArgumentException("Invalid status");
        }
    }

    private boolean isValid(String value, String[] allowed) {
        for (String a : allowed) {
            if (a.equals(value)) return true;
        }
        return false;
    }

    private Artwork mapFromData(Map<String, Object> data) {
        Artwork a = new Artwork();
        a.setTitle(ValidationUtil.getString(data, "title"));
        String artistId = ValidationUtil.getString(data, "artistId");
        if (artistId != null && !artistId.isBlank()) {
            a.setArtistId(Integer.parseInt(artistId.trim()));
        }
        a.setCategory(ValidationUtil.getString(data, "category"));
        a.setMedium(ValidationUtil.optionalTrim(ValidationUtil.getString(data, "medium")));
        a.setYearCreated(ValidationUtil.parseOptionalInt(ValidationUtil.getString(data, "yearCreated"), "Year"));
        a.setDimensions(ValidationUtil.optionalTrim(ValidationUtil.getString(data, "dimensions")));
        String price = ValidationUtil.getString(data, "price");
        if (price != null && !price.isBlank()) {
            a.setPrice(new BigDecimal(price.trim()));
        }
        a.setStatus(ValidationUtil.getString(data, "status"));
        a.setImageUrl(ValidationUtil.optionalTrim(ValidationUtil.getString(data, "imageUrl")));
        a.setDescription(ValidationUtil.optionalTrim(ValidationUtil.getString(data, "description")));
        return a;
    }
}
