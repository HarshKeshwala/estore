-- Seed admin user (password: admin123)
-- BCrypt hash generated for 'admin123'
INSERT INTO users (email, password, first_name, last_name, role, created_at)
VALUES ('admin@estore.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrVj6EYT6pWj7QNr8JVVJNmEQu', 'Admin', 'User', 'ADMIN', CURRENT_TIMESTAMP);
