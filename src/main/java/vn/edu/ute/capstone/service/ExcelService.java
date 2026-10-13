package vn.edu.ute.capstone.service;

import org.springframework.web.multipart.MultipartFile;
import vn.edu.ute.capstone.dto.RegistrationPeriodDTO;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface ExcelService {

    /**
     * Tạo file Excel mẫu cho đợt đăng ký kèm hướng dẫn và dữ liệu ví dụ.
     */
    ByteArrayInputStream generatePeriodTemplate();

    /**
     * Đọc và parse dữ liệu file Excel tải lên thành danh sách RegistrationPeriodDTO.
     * Bắt lỗi từng dòng, kiểm tra định dạng và cấu trúc.
     */
    List<RegistrationPeriodDTO> parsePeriodExcelFile(MultipartFile file);

    /**
     * Tạo file Excel mẫu cho Giảng viên nộp hàng loạt đề tài.
     */
    ByteArrayInputStream generateTopicTemplate();

    /**
     * Đọc và parse dữ liệu file Excel nộp đề tài của Giảng viên.
     */
    List<vn.edu.ute.capstone.dto.TopicProposalDTO> parseTopicExcelFile(MultipartFile file, Long defaultPeriodId, Long defaultDeptId);

    /**
     * Xuất Bảng điểm & Báo cáo Tổng hợp kết quả toàn đợt ra file Excel.
     */
    ByteArrayInputStream exportResultsToExcel(Long periodId);

}
