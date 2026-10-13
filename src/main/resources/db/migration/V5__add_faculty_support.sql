-- =======================================================================
-- V5__add_faculty_support.sql
-- HỖ TRỢ MỞ RỘNG ĐA KHOA (MULTI-FACULTY SUPPORT)
-- =======================================================================

-- 1. BẢNG KHOA (Faculties)
CREATE TABLE IF NOT EXISTS faculties (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Thêm cột faculty_id vào bảng departments
ALTER TABLE departments 
ADD COLUMN faculty_id BIGINT NULL;

-- 3. Tạo quan hệ khóa ngoại giữa departments và faculties
ALTER TABLE departments
ADD CONSTRAINT fk_departments_faculty 
FOREIGN KEY (faculty_id) REFERENCES faculties (id) ON DELETE SET NULL;

-- 4. Seed dữ liệu mẫu cho các Khoa
INSERT INTO faculties (id, code, name, description) VALUES
(1, 'FIT', 'Khoa Công nghệ Thông tin', 'Đào tạo kỹ sư CNTT, Phần mềm, Hệ thống, Mạng và Trí tuệ nhân tạo'),
(2, 'FME', 'Khoa Cơ khí Chế tạo máy', 'Đào tạo kỹ sư Cơ điện tử, Chế tạo máy, Công nghệ Hàn, Ô tô');

-- 5. Gán các bộ môn hiện có (CNPM, HTTT, MMT) thuộc Khoa CNTT (faculty_id = 1)
UPDATE departments SET faculty_id = 1 WHERE id IN (1, 2, 3);

-- 6. Bổ sung các bộ môn mới thuộc Khoa Cơ khí (faculty_id = 2)
INSERT INTO departments (id, code, name, description, faculty_id) VALUES
(4, 'CK_CDT', 'Cơ điện tử', 'Bộ môn phụ trách Robot, Tự động hóa và Cơ điện tử', 2),
(5, 'CK_CTM', 'Chế tạo máy', 'Bộ môn phụ trách Gia công chính xác, Thiết kế máy công nghiệp', 2);
