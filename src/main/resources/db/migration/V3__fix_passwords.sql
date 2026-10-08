-- =======================================================================
-- V3__fix_passwords.sql
-- Cập nhật lại hash BCrypt chuẩn xác cho mật khẩu '123456'
-- =======================================================================

UPDATE users 
SET password = '$2a$10$9icQbws7uEuouiuDWqtKkuDpgjyEUVKAPe.2ig3lTZ.p6lgJz22ey';
