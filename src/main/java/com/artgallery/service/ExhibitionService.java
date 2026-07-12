package com.artgallery.service;

import com.artgallery.dao.ActivityDAO;
import com.artgallery.dao.ExhibitionDAO;
import com.artgallery.model.Exhibition;
import com.artgallery.model.PagedResult;
import com.artgallery.model.User;
import com.artgallery.util.ValidationUtil;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ExhibitionService {
    private final ExhibitionDAO exhibitionDAO = new ExhibitionDAO();
    private final ActivityDAO activityDAO = new ActivityDAO();

    private static final String[] VALID_STATUSES = {
            "upcoming", "active", "completed", "cancelled"
    };

    public PagedResult<Exhibition> findAll(String status, int page, int pageSize) throws SQLException {
        return exhibitionDAO.findAll(status, page, pageSize);
    }

    public Exhibition findById(int id) throws SQLException {
        Exhibition ex = exhibitionDAO.findById(id);
        if (ex == null) throw new IllegalArgumentException("Exhibition not found");
        return ex;
    }

    public Exhibition create(Map<String, Object> data, User user) throws SQLException {
        Exhibition ex = mapFromData(data);
        validate(ex);
        int id = exhibitionDAO.create(ex);
        ex.setId(id);
        activityDAO.log("create", "exhibition", id, "Created exhibition \"" + ex.getTitle() + "\"", user.getId());
        return exhibitionDAO.findById(id);
    }

    public Exhibition update(int id, Map<String, Object> data, User user) throws SQLException {
        if (exhibitionDAO.findById(id) == null) throw new IllegalArgumentException("Exhibition not found");
        Exhibition ex = mapFromData(data);
        ex.setId(id);
        validate(ex);
        exhibitionDAO.update(ex);
        activityDAO.log("update", "exhibition", id, "Updated exhibition \"" + ex.getTitle() + "\"", user.getId());
        return exhibitionDAO.findById(id);
    }

    public void delete(int id, User user) throws SQLException {
        Exhibition existing = exhibitionDAO.findById(id);
        if (existing == null) throw new IllegalArgumentException("Exhibition not found");
        if (!exhibitionDAO.delete(id)) throw new SQLException("Failed to delete exhibition");
        activityDAO.log("delete", "exhibition", id, "Deleted exhibition \"" + existing.getTitle() + "\"", user.getId());
    }

    public void assignArtworks(int id, Map<String, Object> data, User user) throws SQLException {
        Exhibition existing = exhibitionDAO.findById(id);
        if (existing == null) throw new IllegalArgumentException("Exhibition not found");

        Object artworkIdsObj = data.get("artworkIds");
        if (artworkIdsObj == null) throw new IllegalArgumentException("Artwork IDs are required");

        Type listType = new TypeToken<List<Integer>>() {}.getType();
        List<Integer> artworkIds;
        if (artworkIdsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Number> nums = (List<Number>) artworkIdsObj;
            artworkIds = nums.stream().map(Number::intValue).toList();
        } else {
            throw new IllegalArgumentException("Invalid artwork IDs format");
        }

        exhibitionDAO.assignArtworks(id, artworkIds);
        activityDAO.log("assign", "exhibition", id,
                "Assigned " + artworkIds.size() + " artworks to \"" + existing.getTitle() + "\"", user.getId());
    }

    private void validate(Exhibition ex) {
        ValidationUtil.requireNonBlank(ex.getTitle(), "Title");
        ValidationUtil.requireNonBlank(ex.getStartDate(), "Start date");
        ValidationUtil.requireNonBlank(ex.getEndDate(), "End date");
        ValidationUtil.requireNonBlank(ex.getStatus(), "Status");
        if (ex.getStartDate().compareTo(ex.getEndDate()) > 0) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        boolean valid = false;
        for (String s : VALID_STATUSES) {
            if (s.equals(ex.getStatus())) valid = true;
        }
        if (!valid) throw new IllegalArgumentException("Invalid status");
    }

    private Exhibition mapFromData(Map<String, Object> data) {
        Exhibition ex = new Exhibition();
        ex.setTitle(ValidationUtil.getString(data, "title"));
        ex.setDescription(ValidationUtil.optionalTrim(ValidationUtil.getString(data, "description")));
        ex.setLocation(ValidationUtil.optionalTrim(ValidationUtil.getString(data, "location")));
        ex.setStartDate(ValidationUtil.getString(data, "startDate"));
        ex.setEndDate(ValidationUtil.getString(data, "endDate"));
        ex.setStatus(ValidationUtil.getString(data, "status"));
        return ex;
    }
}
