-- Art Gallery Management System - Seed Data
-- Run after schema.sql: mysql -u root -p art_gallery_db < database/seed.sql
-- Default admin is created automatically on first startup: admin / admin123
-- Or insert manually after generating a bcrypt hash

USE art_gallery_db;

INSERT INTO artists (name, nationality, birth_year, death_year, email, bio) VALUES
('Raja Ravi Varma', 'Indian', 1848, 1906, NULL, 'One of India’s greatest painters, known for blending European academic techniques with Indian mythology and traditions.'),

('M. F. Husain', 'Indian', 1915, 2011, NULL, 'A leading figure of modern Indian art and founding member of the Bombay Progressive Artists Group.'),

('Jamini Roy', 'Indian', 1887, 1972, NULL, 'Indian painter inspired by Bengal folk traditions and Kalighat painting style.'),

('Amrita Sher-Gil', 'Indian', 1913, 1941, NULL, 'Pioneer of modern Indian art known for portraying everyday Indian life.'),

('S. H. Raza', 'Indian', 1922, 2016, NULL, 'Modern Indian artist famous for abstract works inspired by Indian philosophy and the Bindu symbol.');


INSERT INTO artworks (title, artist_id, category, medium, year_created, dimensions, price, status, description) VALUES

('Shakuntala', 1, 'Painting', 'Oil on Canvas', 1870, '110 x 80 cm', 5000000.00, 'in_exhibition',
'Iconic painting depicting Shakuntala from the Mahabharata.'),

('Mother Teresa', 2, 'Painting', 'Acrylic on Canvas', 1980, '100 x 70 cm', 3000000.00, 'available',
'Artwork representing Husain’s expressive modern painting style.'),

('Three Pujarins', 3, 'Painting', 'Tempera on Cloth', 1937, '90 x 60 cm', 1500000.00, 'available',
'Bengal folk-inspired artwork showing Jamini Roy’s unique style.'),

('Bride’s Toilet', 4, 'Painting', 'Oil on Canvas', 1937, '140 x 90 cm', 4500000.00, 'in_exhibition',
'A famous work reflecting Indian women and social themes.'),

('Bindu', 5, 'Painting', 'Acrylic on Canvas', 1980, '100 x 100 cm', 6000000.00, 'sold',
'Abstract artwork inspired by Indian spirituality and geometry.'),

('Hamsa Damayanti', 1, 'Painting', 'Oil on Canvas', 1899, '120 x 90 cm', 5500000.00, 'available',
'Classic Raja Ravi Varma painting inspired by Indian epics.'),

('Village Scene', 3, 'Painting', 'Natural Pigments', 1940, '80 x 60 cm', 900000.00, 'on_loan',
'Artwork inspired by rural Bengal culture.');

INSERT INTO exhibitions (title, description, location, start_date, end_date, status) VALUES

('Treasures of Indian Art',
'A journey through classical Indian paintings and legendary artists.',
'National Gallery Hall, Bengaluru',
'2026-01-15',
'2026-04-30',
'active'),

('Modern Masters of India',
'Celebrating modern Indian artists and their contribution to global art.',
'Mumbai Art Centre',
'2026-06-01',
'2026-09-30',
'upcoming'),

('Colours of Heritage',
'A showcase of folk, traditional and cultural artworks.',
'Jaipur Heritage Gallery',
'2025-05-01',
'2025-08-30',
'completed');

INSERT INTO exhibition_artworks (exhibition_id, artwork_id, display_order) VALUES
(1, 1, 1),
(1, 6, 2),
(2, 3, 1),
(3, 5, 1);

INSERT INTO customers (name, email, phone, address, visit_date, notes) VALUES

('Aarav Sharma',
 'aarav.sharma@email.com',
 '+91-9876543210',
 'Indiranagar, Bengaluru',
 '2026-03-10',
 'Interested in Indian classical paintings.'),

('Ananya Rao',
 'ananya.rao@email.com',
 '+91-9876543211',
 'Jayanagar, Bengaluru',
 '2026-03-12',
 'Collector of traditional Indian artwork.'),

('Sophia Anderson',
 'sophia.anderson@email.com',
 '+1-415-555-0123',
 'San Francisco, USA',
 '2026-03-15',
 'International collector interested in Indian mythological paintings.'),

('Oliver Bennett',
 'oliver.bennett@email.com',
 '+44-7700-900123',
 'London, United Kingdom',
 '2026-03-18',
 'Collector specializing in South Asian and cultural artworks.');

INSERT INTO activity_log (action, entity_type, entity_id, description, user_id) VALUES
('create', 'artwork', 7, 'Added artwork "Earthen Form II"', NULL),
('create', 'exhibition', 2, 'Created exhibition "Digital Frontiers"', NULL),
('update', 'artwork', 5, 'Updated status of "Steel & Sky" to sold', NULL),
('create', 'customer', 4, 'Registered customer Robert Hayes', NULL),
('assign', 'exhibition', 1, 'Assigned artworks to "Horizons: New Perspectives"', NULL);
