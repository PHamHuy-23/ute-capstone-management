-- Thêm giá trị 'ADVISOR' vào enum evaluation_type trong bảng evaluations
ALTER TABLE evaluations 
MODIFY COLUMN evaluation_type ENUM('ADVISOR', 'REVIEWER', 'COUNCIL') NOT NULL;
