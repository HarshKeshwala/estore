-- Seed admin user (password: admin123)
-- BCrypt hash generated for 'admin123'
INSERT INTO users (email, password, first_name, last_name, role, created_at)
VALUES ('admin@estore.com', '$2a$10$YVWUrdhUHFFT6uQ4gz/VRujfvnJgMwU.sKpk7hq1k8.R0l8tDyNcW', 'Admin', 'User', 'ADMIN', CURRENT_TIMESTAMP);
