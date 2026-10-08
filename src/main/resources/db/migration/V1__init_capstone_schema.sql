-- =======================================================================
-- V1__init_capstone_schema.sql
-- HỆ THỐNG QUẢN LÝ ĐỀ TÀI SINH VIÊN - KHOA CNTT HCMUTE
-- =======================================================================

-- 1. BẢNG BỘ MÔN (Departments)
CREATE TABLE IF NOT EXISTS departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. BẢNG VAI TRÒ (Roles)
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. BẢNG NGƯỜI DÙNG (Users)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE, -- Mã SV hoặc Mã GV
    password VARCHAR(255) NOT NULL,       -- Mật khẩu mã hóa BCrypt
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    department_id BIGINT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_department FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. BẢNG NỐI USER - ROLE (User Roles: Phân quyền n-n)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. BẢNG ĐỢT ĐĂNG KÝ (Registration Periods)
CREATE TABLE IF NOT EXISTS registration_periods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL, -- Tên đợt (VD: KLTN Học kỳ 1 2026-2027)
    period_type ENUM('COURSE', 'RESEARCH', 'ESSAY', 'CAPSTONE') NOT NULL, -- Môn học, NCKH, TLCN, KLTN
    lecturer_start_date DATETIME NOT NULL,
    lecturer_end_date DATETIME NOT NULL,
    student_start_date DATETIME NOT NULL,
    student_end_date DATETIME NOT NULL,
    review_deadline DATETIME, -- Chỉ thiết lập khi là TLCN hoặc KLTN
    defense_date DATETIME,    -- Chỉ thiết lập khi là KLTN
    status ENUM('DRAFT', 'OPEN_LECTURER', 'OPEN_STUDENT', 'IN_PROGRESS', 'REVIEWING', 'DEFENSE', 'COMPLETED') DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. BẢNG ĐỀ TÀI (Topics)
CREATE TABLE IF NOT EXISTS topics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    requirements TEXT,
    department_id BIGINT NOT NULL,
    period_id BIGINT NOT NULL,
    status ENUM('PENDING_DEPT', 'APPROVED', 'REJECTED', 'ASSIGNED') DEFAULT 'PENDING_DEPT',
    rejection_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_topics_dept FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE RESTRICT,
    CONSTRAINT fk_topics_period FOREIGN KEY (period_id) REFERENCES registration_periods (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. BẢNG GIẢNG VIÊN HƯỚNG DẪN ĐỀ TÀI (Topic Advisors: 1 hoặc tối đa 2 GVHD)
CREATE TABLE IF NOT EXISTS topic_advisors (
    topic_id BIGINT NOT NULL,
    lecturer_id BIGINT NOT NULL,
    is_primary BOOLEAN DEFAULT TRUE, -- TRUE: GVHD chính, FALSE: Đồng hướng dẫn
    PRIMARY KEY (topic_id, lecturer_id),
    CONSTRAINT fk_ta_topic FOREIGN KEY (topic_id) REFERENCES topics (id) ON DELETE CASCADE,
    CONSTRAINT fk_ta_lecturer FOREIGN KEY (lecturer_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. BẢNG NHÓM SINH VIÊN (Student Groups)
CREATE TABLE IF NOT EXISTS student_groups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_code VARCHAR(50) NOT NULL UNIQUE,
    period_id BIGINT NOT NULL,
    topic_id BIGINT UNIQUE, -- Mỗi nhóm chỉ đăng ký 1 đề tài duy nhất
    status ENUM('REGISTERING', 'ACCEPTED', 'COMPLETED') DEFAULT 'REGISTERING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sg_period FOREIGN KEY (period_id) REFERENCES registration_periods (id) ON DELETE CASCADE,
    CONSTRAINT fk_sg_topic FOREIGN KEY (topic_id) REFERENCES topics (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. BẢNG THÀNH VIÊN NHÓM (Group Members: Tối đa 3 SV, 1 SV chỉ 1 nhóm trong 1 đợt)
CREATE TABLE IF NOT EXISTS group_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    period_id BIGINT NOT NULL, -- Dùng để đảm bảo ràng buộc 1 SV chỉ thuộc 1 nhóm/đợt
    is_leader BOOLEAN DEFAULT FALSE,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_student_period UNIQUE (student_id, period_id), -- Ràng buộc 1 SV duy nhất 1 nhóm trong đợt
    CONSTRAINT fk_gm_group FOREIGN KEY (group_id) REFERENCES student_groups (id) ON DELETE CASCADE,
    CONSTRAINT fk_gm_student FOREIGN KEY (student_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_gm_period FOREIGN KEY (period_id) REFERENCES registration_periods (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. BẢNG BÁO CÁO TIẾN ĐỘ / HOÀN THÀNH (Submissions: Do nhóm trưởng nộp)
CREATE TABLE IF NOT EXISTS submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    submitted_by BIGINT NOT NULL, -- ID sinh viên (bắt buộc phải là nhóm trưởng)
    title VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    notes TEXT,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sub_group FOREIGN KEY (group_id) REFERENCES student_groups (id) ON DELETE CASCADE,
    CONSTRAINT fk_sub_user FOREIGN KEY (submitted_by) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. BẢNG HỘI ĐỒNG PHẢN BIỆN (Councils)
CREATE TABLE IF NOT EXISTS councils (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    period_id BIGINT NOT NULL,
    defense_date DATETIME,
    location VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_councils_period FOREIGN KEY (period_id) REFERENCES registration_periods (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. THÀNH VIÊN HỘI ĐỒNG (Council Members: 3 - 5 GV, gồm Chủ tịch & Thư ký)
CREATE TABLE IF NOT EXISTS council_members (
    council_id BIGINT NOT NULL,
    lecturer_id BIGINT NOT NULL,
    role_in_council ENUM('CHAIRMAN', 'SECRETARY', 'MEMBER') NOT NULL,
    PRIMARY KEY (council_id, lecturer_id),
    CONSTRAINT fk_cm_council FOREIGN KEY (council_id) REFERENCES councils (id) ON DELETE CASCADE,
    CONSTRAINT fk_cm_lecturer FOREIGN KEY (lecturer_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. BẢNG PHÂN CÔNG CHẤM ĐỀ TÀI (Council Topic Assignments)
CREATE TABLE IF NOT EXISTS council_topic_assignments (
    council_id BIGINT NOT NULL,
    topic_id BIGINT NOT NULL,
    PRIMARY KEY (council_id, topic_id),
    CONSTRAINT fk_cta_council FOREIGN KEY (council_id) REFERENCES councils (id) ON DELETE CASCADE,
    CONSTRAINT fk_cta_topic FOREIGN KEY (topic_id) REFERENCES topics (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. BẢNG ĐIỂM SỐ & ĐÁNH GIÁ (Evaluations)
CREATE TABLE IF NOT EXISTS evaluations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    topic_id BIGINT NOT NULL,
    council_id BIGINT,
    evaluator_id BIGINT NOT NULL, -- GV chấm điểm
    score DECIMAL(4, 2) NOT NULL, -- Điểm số (0.00 -> 10.00)
    comments TEXT,
    evaluation_type ENUM('REVIEWER', 'COUNCIL') NOT NULL, -- Điểm phản biện hay điểm hội đồng
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_eval_topic FOREIGN KEY (topic_id) REFERENCES topics (id) ON DELETE CASCADE,
    CONSTRAINT fk_eval_council FOREIGN KEY (council_id) REFERENCES councils (id) ON DELETE SET NULL,
    CONSTRAINT fk_eval_evaluator FOREIGN KEY (evaluator_id) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. BẢNG THÔNG BÁO (Announcements)
CREATE TABLE IF NOT EXISTS announcements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content LONGTEXT NOT NULL,
    author_id BIGINT NOT NULL,
    is_pinned BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ann_author FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
