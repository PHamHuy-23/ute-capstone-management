-- V8: Bổ sung ràng buộc điều kiện tham gia đợt đăng ký và thông tin học vụ của sinh viên

-- 1. Thêm các tiêu chuẩn điều kiện vào registration_periods
ALTER TABLE registration_periods
ADD COLUMN min_credits INT NULL DEFAULT 0,
ADD COLUMN min_gpa DECIMAL(3,2) NULL DEFAULT 0.00,
ADD COLUMN target_department_id BIGINT NULL,
ADD COLUMN target_cohort VARCHAR(100) NULL,
ADD COLUMN requires_advisor_approval BOOLEAN NOT NULL DEFAULT FALSE,
ADD CONSTRAINT fk_periods_target_dept FOREIGN KEY (target_department_id) REFERENCES departments(id) ON DELETE SET NULL;

-- 2. Thêm thông tin học vụ cho sinh viên vào bảng users
ALTER TABLE users
ADD COLUMN accumulated_credits INT NULL DEFAULT 0,
ADD COLUMN gpa DECIMAL(3,2) NULL DEFAULT 0.00;

-- 3. Cập nhật dữ liệu mẫu thực tế cho các đợt hiện có
-- Đợt 10: Khóa luận tốt nghiệp KLTN24 (Cần tối thiểu 110 tín chỉ, GPA >= 2.0, Khóa K21-K22)
UPDATE registration_periods
SET min_credits = 110, min_gpa = 2.00, target_cohort = '21,22'
WHERE id = 10;

-- Đợt 12: Đợt Khóa luận KLTN26 hiện hành
UPDATE registration_periods
SET min_credits = 110, min_gpa = 2.00, target_cohort = '21,22'
WHERE id = 12;

-- Đợt 13: Đợt NCKH Cơ khí (Yêu cầu GVHD bảo trợ)
UPDATE registration_periods
SET min_credits = 0, min_gpa = 0.00, requires_advisor_approval = TRUE
WHERE id = 13;

-- 4. Cập nhật dữ liệu học vụ giả lập cho các tài khoản sinh viên mẫu
-- Sinh viên năm 4 (Đủ điều kiện làm Khóa luận)
UPDATE users SET accumulated_credits = 125, gpa = 3.25 WHERE username IN ('22110201', '22110202', '22110203', '22120301');
-- Sinh viên năm 3 (Đủ điều kiện NCKH nhưng chưa đủ 110 tín chỉ làm KLTN)
UPDATE users SET accumulated_credits = 85, gpa = 2.80 WHERE username IN ('22110204', '22110205', '22120302');
