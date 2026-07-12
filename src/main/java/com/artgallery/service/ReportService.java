package com.artgallery.service;

import com.artgallery.dao.ReportDAO;

import java.sql.SQLException;
import java.util.Map;

public class ReportService {
    private final ReportDAO reportDAO = new ReportDAO();

    public Map<String, Object> getSummary() throws SQLException {
        return reportDAO.getSummary();
    }
}
