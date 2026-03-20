-- BCrypt hash for '1234'
UPDATE tables SET password_hash = '$2a$10$slYQmyNdgTY18LGvgxPwHOSQKeIsa6TVestdr8pXIYx1zSIAXCbmS' WHERE store_id = 1;
UPDATE store_admins SET password_hash = '$2a$10$slYQmyNdgTY18LGvgxPwHOSQKeIsa6TVestdr8pXIYx1zSIAXCbmS' WHERE store_id = 1;
SELECT 'tables: ' || COUNT(*) FROM tables WHERE password_hash = '$2a$10$slYQmyNdgTY18LGvgxPwHOSQKeIsa6TVestdr8pXIYx1zSIAXCbmS';
