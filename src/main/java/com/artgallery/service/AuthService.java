package com.artgallery.service;

import com.artgallery.dao.UserDAO;
import com.artgallery.model.User;
import com.artgallery.util.PasswordUtil;
import com.artgallery.util.SessionManager;

import java.sql.SQLException;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();

    public User login(String username, String password) throws SQLException {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Username and password are required");
        }
        User user = userDAO.findByUsername(username.trim());
        if (user == null || !PasswordUtil.verify(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        return new User(user.getId(), user.getUsername(), user.getFullName(), user.getRole());
    }

    public String createSession(User user) {
        return SessionManager.createSession(user);
    }

    public void logout(String token) {
        SessionManager.invalidate(token);
    }
}
