package com.artgallery.service;

import com.artgallery.dao.ActivityDAO;
import com.artgallery.dao.ArtistDAO;
import com.artgallery.model.Artist;
import com.artgallery.model.PagedResult;
import com.artgallery.model.User;
import com.artgallery.util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ArtistService {
    private final ArtistDAO artistDAO = new ArtistDAO();
    private final ActivityDAO activityDAO = new ActivityDAO();

    public PagedResult<Artist> findAll(String search, int page, int pageSize) throws SQLException {
        return artistDAO.findAll(search, page, pageSize);
    }

    public List<Artist> findAllSimple() throws SQLException {
        return artistDAO.findAllSimple();
    }

    public Artist findById(int id) throws SQLException {
        Artist artist = artistDAO.findById(id);
        if (artist == null) throw new IllegalArgumentException("Artist not found");
        return artist;
    }

    public Artist create(Map<String, Object> data, User user) throws SQLException {
        Artist artist = mapFromData(data);
        artist.setName(ValidationUtil.requireNonBlank(artist.getName(), "Name"));
        int id = artistDAO.create(artist);
        artist.setId(id);
        activityDAO.log("create", "artist", id, "Added artist \"" + artist.getName() + "\"", user.getId());
        return artist;
    }

    public Artist update(int id, Map<String, Object> data, User user) throws SQLException {
        Artist existing = artistDAO.findById(id);
        if (existing == null) throw new IllegalArgumentException("Artist not found");
        Artist artist = mapFromData(data);
        artist.setId(id);
        artist.setName(ValidationUtil.requireNonBlank(artist.getName(), "Name"));
        artistDAO.update(artist);
        activityDAO.log("update", "artist", id, "Updated artist \"" + artist.getName() + "\"", user.getId());
        return artistDAO.findById(id);
    }

    public void delete(int id, User user) throws SQLException {
        Artist existing = artistDAO.findById(id);
        if (existing == null) throw new IllegalArgumentException("Artist not found");
        if (!artistDAO.delete(id)) throw new SQLException("Failed to delete artist");
        activityDAO.log("delete", "artist", id, "Deleted artist \"" + existing.getName() + "\"", user.getId());
    }

    private Artist mapFromData(Map<String, Object> data) {
        Artist a = new Artist();
        a.setName(ValidationUtil.getString(data, "name"));
        a.setNationality(ValidationUtil.optionalTrim(ValidationUtil.getString(data, "nationality")));
        a.setBirthYear(ValidationUtil.parseOptionalInt(ValidationUtil.getString(data, "birthYear"), "Birth year"));
        a.setDeathYear(ValidationUtil.parseOptionalInt(ValidationUtil.getString(data, "deathYear"), "Death year"));
        a.setEmail(ValidationUtil.optionalTrim(ValidationUtil.getString(data, "email")));
        a.setPhone(ValidationUtil.optionalTrim(ValidationUtil.getString(data, "phone")));
        a.setBio(ValidationUtil.optionalTrim(ValidationUtil.getString(data, "bio")));
        return a;
    }
}
