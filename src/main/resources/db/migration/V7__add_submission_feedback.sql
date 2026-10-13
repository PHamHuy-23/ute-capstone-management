-- V7__add_submission_feedback.sql
-- Bổ sung trường phản hồi của Giảng viên hướng dẫn cho báo cáo sinh viên

ALTER TABLE submissions
ADD COLUMN feedback TEXT NULL,
ADD COLUMN review_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
ADD COLUMN reviewed_at DATETIME NULL;
