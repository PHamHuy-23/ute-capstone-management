-- =======================================================================
-- V2__seed_initial_data.sql
-- NẠP DỮ LIỆU BAN ĐẦU: ROLES, DEPARTMENTS, VÀ TÀI KHOẢN MẪU
-- Mật khẩu mặc định cho tất cả tài khoản mẫu là: 123456
-- BCrypt hash của '123456': $2a$10$eACCYoNOHEqgkVE8aI.Pme2Jg8w5W5Nn5vQ5Oln0Z2b0n1y077f5a
-- =======================================================================

-- 1. Nạp Roles
INSERT INTO roles (id, name) VALUES
(1, 'ROLE_ADMIN'),
(2, 'ROLE_DEPARTMENT_HEAD'),
(3, 'ROLE_LECTURER'),
(4, 'ROLE_STUDENT');

-- 2. Nạp Bộ môn Khoa CNTT HCMUTE
INSERT INTO departments (id, code, name, description) VALUES
(1, 'CNPM', 'Công nghệ Phần mềm', 'Bộ môn phụ trách các hướng phát triển phần mềm, kiến trúc hệ thống'),
(2, 'HTTT', 'Hệ thống Thông tin', 'Bộ môn phụ trách phân tích dữ liệu, hệ thống thông tin doanh nghiệp'),
(3, 'MMT', 'Mạng máy tính & An ninh thông tin', 'Bộ môn phụ trách mạng, đám mây, an toàn thông tin');

-- 3. Nạp Tài khoản mẫu (Mật khẩu: 123456)
INSERT INTO users (id, username, password, full_name, email, phone, department_id, is_active) VALUES
-- Trưởng khoa
(1, 'truongkhoa', '$2a$10$eACCYoNOHEqgkVE8aI.Pme2Jg8w5W5Nn5vQ5Oln0Z2b0n1y077f5a', 'PGS.TS Nguyễn Văn Trưởng Khoa', 'dean@ute.edu.vn', '0901000001', 1, TRUE),
-- Trưởng bộ môn CNPM
(2, 'head_cnpm', '$2a$10$eACCYoNOHEqgkVE8aI.Pme2Jg8w5W5Nn5vQ5Oln0Z2b0n1y077f5a', 'TS. Trần Văn Trưởng Bộ Môn', 'head_cnpm@ute.edu.vn', '0901000002', 1, TRUE),
-- Giảng viên hướng dẫn 1 & 2
(3, 'gv_minh', '$2a$10$eACCYoNOHEqgkVE8aI.Pme2Jg8w5W5Nn5vQ5Oln0Z2b0n1y077f5a', 'ThS. Lê Quang Minh', 'minhlq@ute.edu.vn', '0901000003', 1, TRUE),
(4, 'gv_nam', '$2a$10$eACCYoNOHEqgkVE8aI.Pme2Jg8w5W5Nn5vQ5Oln0Z2b0n1y077f5a', 'TS. Hoàng Nam', 'namh@ute.edu.vn', '0901000004', 1, TRUE),
-- Sinh viên (Nhóm mẫu)
(5, 'sv_nghia', '$2a$10$eACCYoNOHEqgkVE8aI.Pme2Jg8w5W5Nn5vQ5Oln0Z2b0n1y077f5a', 'Đặng Đại Nghĩa (SV)', '21110001@student.hcmute.edu.vn', '0902000001', 1, TRUE),
(6, 'sv_anh', '$2a$10$eACCYoNOHEqgkVE8aI.Pme2Jg8w5W5Nn5vQ5Oln0Z2b0n1y077f5a', 'Nguyễn Tuấn Anh (SV)', '21110002@student.hcmute.edu.vn', '0902000002', 1, TRUE),
(7, 'sv_bao', '$2a$10$eACCYoNOHEqgkVE8aI.Pme2Jg8w5W5Nn5vQ5Oln0Z2b0n1y077f5a', 'Lê Quốc Bảo (SV)', '21110003@student.hcmute.edu.vn', '0902000003', 1, TRUE);

-- 4. Gán quyền cho tài khoản
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), -- truongkhoa -> ROLE_ADMIN
(2, 2), -- head_cnpm -> ROLE_DEPARTMENT_HEAD
(2, 3), -- head_cnpm cũng là ROLE_LECTURER
(3, 3), -- gv_minh -> ROLE_LECTURER
(4, 3), -- gv_nam -> ROLE_LECTURER
(5, 4), -- sv_nghia -> ROLE_STUDENT
(6, 4), -- sv_anh -> ROLE_STUDENT
(7, 4); -- sv_bao -> ROLE_STUDENT
