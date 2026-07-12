-- Art Gallery Management System - Database Schema
-- Run: mysql -u root -p < database/schema.sql

CREATE DATABASE IF NOT EXISTS art_gallery_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE art_gallery_db;

-- Users (gallery staff authentication)
CREATE TABLE IF NOT EXISTS users (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name   VARCHAR(100) NOT NULL,
    role        ENUM('admin', 'curator', 'staff') NOT NULL DEFAULT 'staff',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Artists
CREATE TABLE IF NOT EXISTS artists (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    nationality VARCHAR(80),
    birth_year  INT,
    death_year  INT,
    email       VARCHAR(120),
    phone       VARCHAR(30),
    bio         TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_artist_name (name)
);

-- Artworks
CREATE TABLE IF NOT EXISTS artworks (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    artist_id   INT NOT NULL,
    category    ENUM('Painting', 'Sculpture', 'Photography', 'Digital', 'Mixed Media', 'Installation', 'Other') NOT NULL DEFAULT 'Painting',
    medium      VARCHAR(150),
    year_created INT,
    dimensions  VARCHAR(100),
    price       DECIMAL(12, 2),
    status      ENUM('available', 'on_loan', 'sold', 'in_exhibition') NOT NULL DEFAULT 'available',
    image_url   VARCHAR(500),
    description TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (artist_id) REFERENCES artists(id) ON DELETE RESTRICT,
    INDEX idx_artwork_title (title),
    INDEX idx_artwork_category (category),
    INDEX idx_artwork_status (status)
);

-- Exhibitions
CREATE TABLE IF NOT EXISTS exhibitions (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    location    VARCHAR(150),
    start_date  DATE NOT NULL,
    end_date    DATE NOT NULL,
    status      ENUM('upcoming', 'active', 'completed', 'cancelled') NOT NULL DEFAULT 'upcoming',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_exhibition_status (status),
    INDEX idx_exhibition_dates (start_date, end_date)
);

-- Exhibition-Artwork junction table
CREATE TABLE IF NOT EXISTS exhibition_artworks (
    exhibition_id INT NOT NULL,
    artwork_id    INT NOT NULL,
    display_order INT DEFAULT 0,
    added_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (exhibition_id, artwork_id),
    FOREIGN KEY (exhibition_id) REFERENCES exhibitions(id) ON DELETE CASCADE,
    FOREIGN KEY (artwork_id) REFERENCES artworks(id) ON DELETE CASCADE
);

-- Customers / Visitors
CREATE TABLE IF NOT EXISTS customers (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    email       VARCHAR(120),
    phone       VARCHAR(30),
    address     VARCHAR(255),
    visit_date  DATE,
    notes       TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_customer_name (name),
    INDEX idx_customer_email (email)
);

-- Activity log for dashboard
CREATE TABLE IF NOT EXISTS activity_log (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    action      VARCHAR(50)  NOT NULL,
    entity_type VARCHAR(50)  NOT NULL,
    entity_id   INT,
    description VARCHAR(500) NOT NULL,
    user_id     INT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_activity_created (created_at DESC)
);
