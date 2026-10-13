-- =======================================================================
-- V6__scale_realistic_simulation_data.sql
-- NẠP DỮ LIỆU MẪU MÔ PHỎNG QUY MÔ THỰC TẾ (SCALE TEST & LIFECYCLE)
-- Hỗ trợ: Đa đợt (Khóa cũ hoàn thành + Khóa mới đang mở), Đa khoa (CNTT & Cơ khí),
-- Đa giảng viên, Đa sinh viên, Đề tài, Hội đồng phản biện và Điểm số.
-- Hash BCrypt mật khẩu '123456': $2a$10$9icQbws7uEuouiuDWqtKkuDpgjyEUVKAPe.2ig3lTZ.p6lgJz22ey
-- =======================================================================

-- =======================================================================
-- 1. NẠP CÁC ĐỢT ĐĂNG KÝ VÒNG ĐỜI (Registration Periods)
-- =======================================================================
INSERT INTO registration_periods 
(id, name, period_type, lecturer_start_date, lecturer_end_date, student_start_date, student_end_date, review_deadline, defense_date, status)
VALUES
-- Đợt 1: Khóa luận tốt nghiệp HK2 (2024-2025) - ĐÃ HOÀN THÀNH (LỊCH SỬ / ARCHIVED)
(10, 'Khóa luận tốt nghiệp HK2 (2024 - 2025)', 'CAPSTONE', 
 '2025-01-05 08:00:00', '2025-01-25 17:00:00', 
 '2025-02-01 08:00:00', '2025-02-20 17:00:00', 
 '2025-05-15 17:00:00', '2025-05-25 08:00:00', 'COMPLETED'),

-- Đợt 2: Tiểu luận chuyên ngành HK1 (2025-2026) - ĐANG CHẤM PHẢN BIỆN
(11, 'Tiểu luận chuyên ngành HK1 (2025 - 2026)', 'ESSAY', 
 '2025-08-10 08:00:00', '2025-08-30 17:00:00', 
 '2025-09-05 08:00:00', '2025-09-25 17:00:00', 
 '2025-12-10 17:00:00', NULL, 'REVIEWING'),

-- Đợt 3: Khóa luận tốt nghiệp HK1 (2026 - 2027) - ĐANG MỞ CHO SINH VIÊN ĐĂNG KÝ (HIỆN HÀNH)
(12, 'Khóa luận tốt nghiệp HK1 (2026 - 2027)', 'CAPSTONE', 
 '2026-08-15 08:00:00', '2026-09-10 17:00:00', 
 '2026-09-15 08:00:00', '2026-11-30 17:00:00', 
 '2026-12-20 17:00:00', '2026-12-28 08:00:00', 'OPEN_STUDENT'),

-- Đợt 4: Nghiên cứu khoa học Khoa Cơ khí (2026 - 2027) - MỞ CHO GIẢNG VIÊN NỘP ĐỀ TÀI
(13, 'Nghiên cứu khoa học SV Cơ khí (2026 - 2027)', 'RESEARCH', 
 '2026-10-01 08:00:00', '2026-11-15 17:00:00', 
 '2026-11-20 08:00:00', '2026-12-15 17:00:00', 
 '2027-04-10 17:00:00', '2027-04-20 08:00:00', 'OPEN_LECTURER');

-- =======================================================================
-- 2. NẠP GIẢNG VIÊN & TRƯỞNG BỘ MÔN CHO CÁC KHOA (Users)
-- =======================================================================
INSERT INTO users (id, username, password, full_name, email, phone, department_id, is_active) VALUES
-- Khoa CNTT - Bộ môn HTTT
(20, 'head_httt', '$2a$10$9icQbws7uEuouiuDWqtKkuDpgjyEUVKAPe.2ig3lTZ.p6lgJz22ey', 'PGS.TS Nguyễn Thanh HTTT', 'head_httt@ute.edu.vn', '0903000001', 2, TRUE),
(21, 'gv_tam_httt', '$2a$10$9icQbws7uEuouiuDWqtKkuDpgjyEUVKAPe.2ig3lTZ.p6lgJz22ey', 'TS. Vũ Minh Tâm', 'tamvm@ute.edu.vn', '0903000002', 2, TRUE),
(22, 'gv_hang_httt', '$2a$10$9icQbws7uEuouiuDWqtKkuDpgjyEUVKAPe.2ig3lTZ.p6lgJz22ey', 'ThS. Đỗ Thúy Hằng', 'hangdt@ute.edu.vn', '0903000003', 2, TRUE),

-- Khoa CNTT - Bộ môn MMT
(23, 'head_mmt', '$2a$10$9icQbws7uEuouiuDWqtKkuDpgjyEUVKAPe.2ig3lTZ.p6lgJz22ey', 'TS. Phạm Đình MMT', 'head_mmt@ute.edu.vn', '0903000004', 3, TRUE),
(24, 'gv_hai_mmt', '$2a$10$9icQbws7uEuouiuDWqtKkuDpgjyEUVKAPe.2ig3lTZ.p6lgJz22ey', 'ThS. Nguyễn Hoàng Hải', 'hainh@ute.edu.vn', '0903000005', 3, TRUE),

-- Khoa Cơ khí - Trưởng khoa & Giảng viên
(25, 'dean_fme', '$2a$10$9icQbws7uEuouiuDWqtKkuDpgjyEUVKAPe.2ig3lTZ.p6lgJz22ey', 'GS.TS Phan Cơ Khí', 'dean_fme@ute.edu.vn', '0904000001', 4, TRUE),
(26, 'head_cdt', '$2a$10$9icQbws7uEuouiuDWqtKkuDpgjyEUVKAPe.2ig3lTZ.p6lgJz22ey', 'PGS.TS Bùi Cơ Điện Tử', 'head_cdt@ute.edu.vn', '0904000002', 4, TRUE),
(27, 'gv_tuan_cdt', '$2a$10$9icQbws7uEuouiuDWqtKkuDpgjyEUVKAPe.2ig3lTZ.p6lgJz22ey', 'TS. Dương Quốc Tuấn', 'tuandq@ute.edu.vn', '0904000003', 4, TRUE),
(28, 'head_ctm', '$2a$10$9icQbws7uEuouiuDWqtKkuDpgjyEUVKAPe.2ig3lTZ.p6lgJz22ey', 'TS. Ngô Chế Tạo Máy', 'head_ctm@ute.edu.vn', '0904000004', 5, TRUE);

-- Phân quyền cho giảng viên mới
INSERT INTO user_roles (user_id, role_id) VALUES
(20, 2), (20, 3), -- head_httt
(21, 3),          -- gv_tam_httt
(22, 3),          -- gv_hang_httt
(23, 2), (23, 3), -- head_mmt
(24, 3),          -- gv_hai_mmt
(25, 1),          -- dean_fme (Admin Khoa Cơ khí)
(26, 2), (26, 3), -- head_cdt
(27, 3),          -- gv_tuan_cdt
(28, 2), (28, 3); -- head_ctm

-- =======================================================================
-- 3. NẠP DANH SÁCH SINH VIÊN QUY MÔ (Sinh viên Khóa 2021 & 2022)
-- =======================================================================
INSERT INTO users (id, username, password, full_name, email, phone, department_id, is_active) VALUES
-- Sinh viên Khóa 2021 (Đã hoàn thành tốt nghiệp ở Đợt 10)
(101, '21110101', '$2a$10$9icQbws7uEuouiuDWqtKkuDpgjyEUVKAPe.2ig3lTZ.p6lgJz22ey', 'Võ Hoàng Khang', '21110101@student.hcmute.edu.vn', '0911000101', 1, TRUE),
(102, '21110102', '$2a$10$9icQbws7uEuouiuDWqtKkuDpgjyEUVKAPe.2ig3lTZ.p6lgJz22ey', 'Trần Thị Mỹ Duyên', '21110102@student.hcmute.edu.vn', '0911000102', 1, TRUE),
(103, '21110103', '$2a$10$9icQbws7uEuouiuDWqtKkuDpgjyEUVKAPe.2ig3lTZ.p6lgJz22ey', 'Đinh Tiến Đạt', '21110103@student.hcmute.edu.vn', '0911000103', 2, TRUE),
(104, '21110104', '$2a$10$9icQbws7uEuouiuDWqtKkuDpgjyEUVKAPe.2ig3lTZ.p6lgJz22ey', 'Nguyễn Thị Bích Thảo', '21110104@student.hcmute.edu.vn', '0911000104', 2, TRUE),
(105, '21110105', '$2a$10$9icQbws7uEuouiuDWqtKkuDpgjyEUVKAPe.2ig3lTZ.p6lgJz22ey', 'Lý Quốc Cường', '21110105@student.hcmute.edu.vn', '0911000105', 3, TRUE),

-- Sinh viên Khóa 2022 (Đang làm đồ án Đợt 12 hiện tại)
(110, '22110201', '$2a$10$9icQbws7uEuouiuDWqtKkuDpgjyEUVKAPe.2ig3lTZ.p6lgJz22ey', 'Huỳnh Gia Bảo', '22110201@student.hcmute.edu.vn', '0911000201', 1, TRUE),
(111, '22110202', '$2a$10$9icQbws7uEuouiuDWqtKkuDpgjyEUVKAPe.2ig3lTZ.p6lgJz22ey', 'Phạm Minh Trí', '22110202@student.hcmute.edu.vn', '0911000202', 1, TRUE),
(112, '22110203', '$2a$10$9icQbws7uEuouiuDWqtKkuDpgjyEUVKAPe.2ig3lTZ.p6lgJz22ey', 'Trịnh Cẩm Tú', '22110203@student.hcmute.edu.vn', '0911000203', 1, TRUE),
(113, '22110204', '$2a$10$9icQbws7uEuouiuDWqtKkuDpgjyEUVKAPe.2ig3lTZ.p6lgJz22ey', 'Lê Hữu Phước', '22110204@student.hcmute.edu.vn', '0911000204', 2, TRUE),
(114, '22110205', '$2a$10$9icQbws7uEuouiuDWqtKkuDpgjyEUVKAPe.2ig3lTZ.p6lgJz22ey', 'Bùi Đức Trọng', '22110205@student.hcmute.edu.vn', '0911000205', 3, TRUE),

-- Sinh viên Cơ khí
(120, '22120301', '$2a$10$9icQbws7uEuouiuDWqtKkuDpgjyEUVKAPe.2ig3lTZ.p6lgJz22ey', 'Nguyễn Văn Cơ Khí', '22120301@student.hcmute.edu.vn', '0911000301', 4, TRUE),
(121, '22120302', '$2a$10$9icQbws7uEuouiuDWqtKkuDpgjyEUVKAPe.2ig3lTZ.p6lgJz22ey', 'Trần Đình Hàn', '22120302@student.hcmute.edu.vn', '0911000302', 4, TRUE);

-- Gán ROLE_STUDENT
INSERT INTO user_roles (user_id, role_id) VALUES
(101, 4), (102, 4), (103, 4), (104, 4), (105, 4),
(110, 4), (111, 4), (112, 4), (113, 4), (114, 4),
(120, 4), (121, 4);

-- =======================================================================
-- 4. NẠP ĐỀ TÀI (Topics) - ĐA DẠNG TRẠNG THÁI & BỘ MÔN
-- =======================================================================
INSERT INTO topics (id, code, title, description, requirements, department_id, period_id, status) VALUES
-- [Đợt 10 - Đã hoàn thành trong quá khứ]
(50, 'KLTN24-CNPM01', 'Xây dựng nền tảng E-Learning hỗ trợ chấm code tự động', 'Mô tả chi tiết hệ thống chấm bài trực tuyến cho sinh viên', 'Yêu cầu: Java, Docker, Microservices', 1, 10, 'ASSIGNED'),
(51, 'KLTN24-HTTT01', 'Ứng dụng Data Warehouse phân tích dữ liệu tuyển sinh HCMUTE', 'Khai phá và xây dựng dashboard BI phân tích xu hướng tuyển sinh', 'Yêu cầu: SQL Server SSIS, PowerBI, Python', 2, 10, 'ASSIGNED'),

-- [Đợt 12 - Đợt hiện hành đang mở cho sinh viên đăng ký]
(52, 'KLTN26-CNPM01', 'Hệ thống Quản lý Bệnh án Điện tử ứng dụng Blockchain', 'Quản lý bảo mật thông tin hồ sơ y tế giữa các bệnh viện', 'Yêu cầu: Smart Contract, Spring Boot, ReactJS', 1, 12, 'APPROVED'),
(53, 'KLTN26-CNPM02', 'Nền tảng Livestream Thương mại điện tử với độ trễ siêu thấp', 'Áp dụng WebRTC và CDN đám mây tối ưu luồng video tương tác', 'Yêu cầu: Go/Java, WebRTC, Redis', 1, 12, 'APPROVED'),
(54, 'KLTN26-HTTT01', 'Xây dựng hệ thống phát hiện gian lận thanh toán bằng Machine Learning', 'Phát hiện bất thường trong giao dịch thẻ tín dụng thời gian thực', 'Yêu cầu: Python, FastAPI, XGBoost, Kafka', 2, 12, 'APPROVED'),
(55, 'KLTN26-MMT01', 'Thiết kế hệ thống phòng thủ mạng Zero-Trust cho trường Đại học', 'Triển khai xác thực đa nhân tố, phân đoạn vi mô hệ thống mạng', 'Yêu cầu: Linux, Cisco, Kubernetes, Snort IDS', 3, 12, 'APPROVED'),
(56, 'KLTN26-CNPM03', 'Ứng dụng Generative AI hỗ trợ lập trình viên viết Unit Test', 'Fine-tune mô hình LLM để tự động sinh mã kiểm thử Java/Python', 'Yêu cầu: Python, LLM APIs, LangChain', 1, 12, 'PENDING_DEPT'),

-- [Đợt 13 - Khoa Cơ khí đang nộp đề tài]
(57, 'NCKH26-CDT01', 'Thiết kế và chế tạo Robot tự hành vận chuyển trong nhà xưởng (AGV)', 'Nghiên cứu chế tạo xe tự hành dẫn đường bằng thị giác máy tính', 'Yêu cầu: ROS, C++, Vi điều khiển STM32', 4, 13, 'APPROVED'),
(58, 'NCKH26-CTM01', 'Tối ưu hóa thông số công nghệ khi phay cao tốc hợp kim Titan', 'Khảo sát độ nhám bề mặt và mòn dụng cụ khi gia công CNC 5 trục', 'Yêu cầu: Mastercam, ANSYS, Thí nghiệm gia công', 5, 13, 'APPROVED');

-- Phân công GVHD cho đề tài
INSERT INTO topic_advisors (topic_id, lecturer_id, is_primary) VALUES
(50, 3, TRUE), (50, 4, FALSE), -- gv_minh & gv_nam
(51, 21, TRUE),                 -- gv_tam_httt
(52, 3, TRUE),                  -- gv_minh
(53, 4, TRUE),                  -- gv_nam
(54, 21, TRUE), (54, 22, FALSE),-- gv_tam_httt & gv_hang_httt
(55, 23, TRUE),                 -- head_mmt
(56, 3, TRUE),                  -- gv_minh
(57, 26, TRUE), (57, 27, FALSE),-- head_cdt & gv_tuan_cdt
(58, 28, TRUE);                 -- head_ctm

-- =======================================================================
-- 5. NẠP NHÓM SINH VIÊN & ĐĂNG KÝ ĐỀ TÀI
-- =======================================================================
-- Nhóm đợt 10 (Đã hoàn thành)
INSERT INTO student_groups (id, group_code, period_id, topic_id, status) VALUES
(30, 'GROUP-KLTN24-01', 10, 50, 'COMPLETED'),
(31, 'GROUP-KLTN24-02', 10, 51, 'COMPLETED');

INSERT INTO group_members (group_id, student_id, period_id, is_leader) VALUES
(30, 101, 10, TRUE),
(30, 102, 10, FALSE),
(31, 103, 10, TRUE),
(31, 104, 10, FALSE);

-- Nhóm đợt 12 (Đang đăng ký đợt hiện hành)
INSERT INTO student_groups (id, group_code, period_id, topic_id, status) VALUES
(32, 'GROUP-KLTN26-01', 12, NULL, 'REGISTERING');

INSERT INTO group_members (group_id, student_id, period_id, is_leader) VALUES
(32, 110, 12, TRUE),
(32, 111, 12, FALSE);

-- =======================================================================
-- 6. NẠP HỘI ĐỒNG PHẢN BIỆN & KẾT QUẢ ĐIỂM SỐ LỊCH SỬ (Đợt 10)
-- =======================================================================
INSERT INTO councils (id, name, period_id, defense_date, location) VALUES
(101, 'Hội đồng Khóa luận CNPM 01 (HK2 2024-2025)', 10, '2025-05-25 08:30:00', 'Phòng A1-402'),
(102, 'Hội đồng Khóa luận HTTT 01 (HK2 2024-2025)', 10, '2025-05-25 13:30:00', 'Phòng B2-301');

INSERT INTO council_members (council_id, lecturer_id, role_in_council) VALUES
(101, 2, 'CHAIRMAN'),  -- head_cnpm (Chủ tịch)
(101, 4, 'SECRETARY'), -- gv_nam (Thư ký)
(101, 21, 'MEMBER'),   -- gv_tam_httt (Ủy viên)
(102, 20, 'CHAIRMAN'), -- head_httt (Chủ tịch)
(102, 22, 'SECRETARY'),-- gv_hang_httt (Thư ký)
(102, 3, 'MEMBER');    -- gv_minh (Ủy viên)

INSERT INTO council_topic_assignments (council_id, topic_id) VALUES
(101, 50),
(102, 51);

-- Nạp điểm số hoàn chỉnh cho Đề tài 50 (Xuất sắc: 9.15)
INSERT INTO evaluations (topic_id, council_id, evaluator_id, score, comments, evaluation_type) VALUES
(50, NULL, 3, 9.20, 'Sinh viên hoàn thành khối lượng rất lớn, ứng dụng chạy ổn định trên Docker.', 'ADVISOR'),
(50, NULL, 23, 8.80, 'Kiến trúc vi dịch vụ rõ ràng, cần hoàn thiện thêm tài liệu API.', 'REVIEWER'),
(50, 101, 2, 9.50, 'Phần mềm đáp ứng tốt yêu cầu thực tế của nhà trường. Bảo vệ tự tin.', 'COUNCIL'),
(50, 101, 4, 9.10, 'Hệ thống demo mượt mà, trả lời câu hỏi phản biện thuyết phục.', 'COUNCIL');

-- Nạp điểm số cho Đề tài 51 (Khá giỏi: 8.50)
INSERT INTO evaluations (topic_id, council_id, evaluator_id, score, comments, evaluation_type) VALUES
(51, NULL, 21, 8.50, 'Thu thập và làm sạch dữ liệu tốt.', 'ADVISOR'),
(51, NULL, 4, 8.00, 'Mô hình hóa dữ liệu đúng chuẩn Kimball.', 'REVIEWER'),
(51, 102, 20, 9.00, 'Dashboard BI trực quan, phân tích sâu.', 'COUNCIL');
