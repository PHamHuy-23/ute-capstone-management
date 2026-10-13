package vn.edu.ute.capstone.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.ute.capstone.dto.RegistrationPeriodDTO;
import vn.edu.ute.capstone.entity.*;
import vn.edu.ute.capstone.repository.RegistrationPeriodRepository;
import vn.edu.ute.capstone.repository.StudentGroupRepository;
import vn.edu.ute.capstone.repository.TopicRepository;
import vn.edu.ute.capstone.service.EvaluationService;
import vn.edu.ute.capstone.service.ExcelService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelServiceImpl implements ExcelService {

    private final TopicRepository topicRepository;
    private final StudentGroupRepository groupRepository;
    private final EvaluationService evaluationService;
    private final RegistrationPeriodRepository periodRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public ByteArrayInputStream generatePeriodTemplate() {
        String[] headers = {
                "Tên Đợt Đăng Ký (*)",
                "Loại Hình (COURSE / RESEARCH / ESSAY / CAPSTONE) (*)",
                "Bắt Đầu GV Nộp (dd/MM/yyyy HH:mm) (*)",
                "Hạn Chót GV Nộp (dd/MM/yyyy HH:mm) (*)",
                "Bắt Đầu SV Đăng Ký (dd/MM/yyyy HH:mm) (*)",
                "Hạn Chót SV Đăng Ký (dd/MM/yyyy HH:mm) (*)",
                "Hạn GVPB Nộp Điểm (dd/MM/yyyy HH:mm)",
                "Ngày Báo Cáo Hội Đồng (dd/MM/yyyy HH:mm)"
        };

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Mau_Dot_Dang_Ky");

            // Tạo kiểu cho Header (chữ in đậm, viền rõ ràng)
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setBorderBottom(BorderStyle.MEDIUM);
            headerCellStyle.setBorderTop(BorderStyle.MEDIUM);
            headerCellStyle.setBorderRight(BorderStyle.THIN);
            headerCellStyle.setBorderLeft(BorderStyle.THIN);

            // Dòng Header
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerCellStyle);
            }

            // Dòng dữ liệu mẫu 1 (Khóa luận tốt nghiệp)
            Row sampleRow1 = sheet.createRow(1);
            sampleRow1.createCell(0).setCellValue("Khóa luận tốt nghiệp Học kỳ 1 2026-2027");
            sampleRow1.createCell(1).setCellValue("CAPSTONE");
            sampleRow1.createCell(2).setCellValue("01/10/2026 08:00");
            sampleRow1.createCell(3).setCellValue("15/10/2026 17:00");
            sampleRow1.createCell(4).setCellValue("16/10/2026 08:00");
            sampleRow1.createCell(5).setCellValue("25/10/2026 17:00");
            sampleRow1.createCell(6).setCellValue("20/12/2026 17:00");
            sampleRow1.createCell(7).setCellValue("28/12/2026 08:00");

            // Dòng dữ liệu mẫu 2 (Tiểu luận chuyên ngành)
            Row sampleRow2 = sheet.createRow(2);
            sampleRow2.createCell(0).setCellValue("Tiểu luận chuyên ngành Công nghệ phần mềm HK1");
            sampleRow2.createCell(1).setCellValue("ESSAY");
            sampleRow2.createCell(2).setCellValue("05/10/2026 08:00");
            sampleRow2.createCell(3).setCellValue("20/10/2026 17:00");
            sampleRow2.createCell(4).setCellValue("21/10/2026 08:00");
            sampleRow2.createCell(5).setCellValue("30/10/2026 17:00");
            sampleRow2.createCell(6).setCellValue("15/12/2026 17:00");
            sampleRow2.createCell(7).setCellValue(""); // TLCN không cần ngày hội đồng

            // Auto-size các cột để dễ nhìn
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            log.error("Lỗi khi tạo file Excel mẫu: {}", e.getMessage());
            throw new RuntimeException("Không thể tạo file Excel mẫu: " + e.getMessage());
        }
    }

    @Override
    public List<RegistrationPeriodDTO> parsePeriodExcelFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn một file Excel để tải lên!");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new IllegalArgumentException("Định dạng file không hợp lệ! Chỉ chấp nhận file Excel (.xlsx hoặc .xls).");
        }

        List<RegistrationPeriodDTO> periodList = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getLastRowNum() < 1) {
                throw new IllegalArgumentException("File Excel không có dữ liệu để xử lý!");
            }

            // Đọc từ dòng 1 (bỏ qua dòng tiêu đề 0)
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                int displayRow = rowIndex + 1; // Số dòng trên Excel thực tế
                try {
                    String name = getCellValueAsString(row.getCell(0));
                    if (name.isBlank()) {
                        throw new IllegalArgumentException("Tên đợt không được để trống");
                    }

                    String typeStr = getCellValueAsString(row.getCell(1)).toUpperCase().trim();
                    PeriodType periodType;
                    try {
                        periodType = PeriodType.valueOf(typeStr);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Loại hình '" + typeStr + "' không hợp lệ (Phải là COURSE, RESEARCH, ESSAY, hoặc CAPSTONE)");
                    }

                    LocalDateTime lecturerStart = parseDateTime(row.getCell(2), "Thời gian bắt đầu GV");
                    LocalDateTime lecturerEnd = parseDateTime(row.getCell(3), "Hạn chót GV nộp");
                    LocalDateTime studentStart = parseDateTime(row.getCell(4), "Thời gian bắt đầu SV");
                    LocalDateTime studentEnd = parseDateTime(row.getCell(5), "Hạn chót SV đăng ký");

                    LocalDateTime reviewDeadline = parseOptionalDateTime(row.getCell(6));
                    LocalDateTime defenseDate = parseOptionalDateTime(row.getCell(7));

                    RegistrationPeriodDTO dto = RegistrationPeriodDTO.builder()
                            .name(name)
                            .periodType(periodType)
                            .lecturerStartDate(lecturerStart)
                            .lecturerEndDate(lecturerEnd)
                            .studentStartDate(studentStart)
                            .studentEndDate(studentEnd)
                            .reviewDeadline(reviewDeadline)
                            .defenseDate(defenseDate)
                            .status(PeriodStatus.DRAFT) // Mặc định tạo ở trạng thái Bản nháp an toàn
                            .build();

                    periodList.add(dto);
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Lỗi tại Dòng " + displayRow + ": " + ex.getMessage());
                }
            }

            if (periodList.isEmpty()) {
                throw new IllegalArgumentException("Không tìm thấy dòng dữ liệu hợp lệ nào trong file Excel!");
            }

            return periodList;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Lỗi khi đọc file Excel: ", ex);
            throw new IllegalArgumentException("Không thể đọc file Excel tải lên. Vui lòng kiểm tra lại cấu trúc file!");
        }
    }

    private boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK && !getCellValueAsString(cell).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().format(DATE_FORMATTER);
                }
                yield String.valueOf((long) cell.getNumericCellValue());
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private LocalDateTime parseDateTime(Cell cell, String fieldName) {
        if (cell == null) {
            throw new IllegalArgumentException(fieldName + " không được để trống");
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue();
        }
        String val = getCellValueAsString(cell);
        if (val.isBlank()) {
            throw new IllegalArgumentException(fieldName + " không được để trống");
        }
        try {
            return LocalDateTime.parse(val, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(fieldName + " sai định dạng (Yêu cầu định dạng: dd/MM/yyyy HH:mm)");
        }
    }

    private LocalDateTime parseOptionalDateTime(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue();
        }
        String val = getCellValueAsString(cell);
        if (val.isBlank()) return null;
        try {
            return LocalDateTime.parse(val, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Ngày tháng sai định dạng '" + val + "' (Yêu cầu: dd/MM/yyyy HH:mm)");
        }
    }

    @Override
    public ByteArrayInputStream generateTopicTemplate() {
        String[] headers = {
                "Mã Đề Tài (*)",
                "Tên Đề Tài (*)",
                "Mục Tiêu & Mô Tả",
                "Yêu Cầu Tiên Quyết",
                "Mã Bộ Môn (Tùy chọn)",
                "Tài Khoản GV Đồng HD (Tùy chọn)"
        };

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Mau_De_Tai");

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setBorderBottom(BorderStyle.MEDIUM);
            headerCellStyle.setBorderTop(BorderStyle.MEDIUM);
            headerCellStyle.setBorderRight(BorderStyle.THIN);
            headerCellStyle.setBorderLeft(BorderStyle.THIN);

            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerCellStyle);
            }

            // Dữ liệu mẫu
            Object[][] sampleData = {
                    {"DT-AI-01", "Ứng dụng Computer Vision trong nhận diện khuôn mặt", "Xây dựng hệ thống điểm danh tự động", "Python, OpenCV, PyTorch", "SE", "gv_minh"},
                    {"DT-WEB-02", "Hệ thống quản lý kho hàng chuẩn Cloud-Native", "Microservices với Spring Boot và Kubernetes", "Java 21, Spring Boot, Docker", "SE", ""}
            };

            for (int i = 0; i < sampleData.length; i++) {
                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < sampleData[i].length; j++) {
                    row.createCell(j).setCellValue(sampleData[i][j].toString());
                }
            }

            for (int col = 0; col < headers.length; col++) {
                sheet.autoSizeColumn(col);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            log.error("Lỗi khi tạo file Excel mẫu đề tài: ", e);
            throw new RuntimeException("Không thể tạo file Excel mẫu: " + e.getMessage());
        }
    }

    @Override
    public List<vn.edu.ute.capstone.dto.TopicProposalDTO> parseTopicExcelFile(MultipartFile file, Long defaultPeriodId, Long defaultDeptId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng tải lên file Excel không rỗng.");
        }

        List<vn.edu.ute.capstone.dto.TopicProposalDTO> dtos = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowNum = 0;

            for (Row row : sheet) {
                rowNum++;
                if (rowNum == 1) continue; // Bỏ qua Header

                // Bỏ qua dòng trống hoàn toàn
                if (isRowEmpty(row)) continue;

                try {
                    String code = getCellValueAsString(row.getCell(0));
                    String title = getCellValueAsString(row.getCell(1));
                    String description = getCellValueAsString(row.getCell(2));
                    String requirements = getCellValueAsString(row.getCell(3));

                    if (code.isBlank()) {
                        throw new IllegalArgumentException("Mã đề tài không được để trống");
                    }
                    if (title.isBlank()) {
                        throw new IllegalArgumentException("Tên đề tài không được để trống");
                    }

                    vn.edu.ute.capstone.dto.TopicProposalDTO dto = vn.edu.ute.capstone.dto.TopicProposalDTO.builder()
                            .code(code.trim().toUpperCase())
                            .title(title.trim())
                            .description(description)
                            .requirements(requirements)
                            .periodId(defaultPeriodId)
                            .departmentId(defaultDeptId)
                            .build();

                    dtos.add(dto);
                } catch (Exception ex) {
                    errors.add("Dòng " + rowNum + ": " + ex.getMessage());
                }
            }

            if (!errors.isEmpty()) {
                throw new IllegalArgumentException("Lỗi cấu trúc dữ liệu Excel:\n" + String.join("\n", errors));
            }

            if (dtos.isEmpty()) {
                throw new IllegalArgumentException("File Excel không chứa bản ghi đề tài hợp lệ nào.");
            }

            return dtos;
        } catch (IOException e) {
            log.error("Lỗi khi đọc file Excel đề tài: ", e);
            throw new RuntimeException("Không thể đọc định dạng file Excel: " + e.getMessage());
        }
    }

    @Override
    public ByteArrayInputStream exportResultsToExcel(Long periodId) {
        List<Topic> topics = (periodId != null && periodId > 0)
                ? topicRepository.findByPeriodId(periodId)
                : topicRepository.findAll();

        String periodName = "TOAN_BO_CAC_DOT";
        if (periodId != null && periodId > 0) {
            Optional<RegistrationPeriod> periodOpt = periodRepository.findById(periodId);
            if (periodOpt.isPresent()) {
                periodName = periodOpt.get().getName();
            }
        }

        String[] headers = {
                "STT",
                "MÃ ĐỀ TÀI",
                "TÊN ĐỀ TÀI",
                "BỘ MÔN",
                "MÃ NHÓM",
                "DANH SÁCH SINH VIÊN",
                "GV HƯỚNG DẪN",
                "ĐIỂM HƯỚNG DẪN",
                "ĐIỂM PHẢN BIỆN",
                "ĐIỂM HỘI ĐỒNG",
                "ĐIỂM TRUNG BÌNH",
                "XẾP LOẠI"
        };

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Bang_Diem_Tong_Ket");

            // Header Style
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.BLACK.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
            headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerCellStyle.setBorderBottom(BorderStyle.MEDIUM);
            headerCellStyle.setBorderTop(BorderStyle.MEDIUM);
            headerCellStyle.setBorderRight(BorderStyle.THIN);
            headerCellStyle.setBorderLeft(BorderStyle.THIN);

            // Data Row Style
            CellStyle dataCellStyle = workbook.createCellStyle();
            dataCellStyle.setBorderBottom(BorderStyle.THIN);
            dataCellStyle.setBorderTop(BorderStyle.THIN);
            dataCellStyle.setBorderRight(BorderStyle.THIN);
            dataCellStyle.setBorderLeft(BorderStyle.THIN);
            dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Center Data Style
            CellStyle centerCellStyle = workbook.createCellStyle();
            centerCellStyle.cloneStyleFrom(dataCellStyle);
            centerCellStyle.setAlignment(HorizontalAlignment.CENTER);

            // Number/Score Style
            CellStyle scoreCellStyle = workbook.createCellStyle();
            scoreCellStyle.cloneStyleFrom(centerCellStyle);
            Font boldScoreFont = workbook.createFont();
            boldScoreFont.setBold(true);
            scoreCellStyle.setFont(boldScoreFont);
            DataFormat df = workbook.createDataFormat();
            scoreCellStyle.setDataFormat(df.getFormat("0.00"));

            // Dòng tiêu đề
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(25);
            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowIdx = 1;
            for (Topic t : topics) {
                Row row = sheet.createRow(rowIdx);
                row.setHeightInPoints(22);

                // 1. STT
                Cell c0 = row.createCell(0);
                c0.setCellValue(rowIdx);
                c0.setCellStyle(centerCellStyle);

                // 2. Mã đề tài
                Cell c1 = row.createCell(1);
                c1.setCellValue(sanitizeCell(t.getCode()));
                c1.setCellStyle(centerCellStyle);

                // 3. Tên đề tài
                Cell c2 = row.createCell(2);
                c2.setCellValue(sanitizeCell(t.getTitle()));
                c2.setCellStyle(dataCellStyle);

                // 4. Bộ môn
                Cell c3 = row.createCell(3);
                c3.setCellValue(sanitizeCell(t.getDepartment() != null ? t.getDepartment().getName() : ""));
                c3.setCellStyle(dataCellStyle);

                // 5. Mã nhóm & Danh sách sinh viên
                Optional<StudentGroup> groupOpt = groupRepository.findByTopicId(t.getId());
                String groupCode = "";
                List<String> studentInfoList = new ArrayList<>();
                if (groupOpt.isPresent()) {
                    StudentGroup g = groupOpt.get();
                    groupCode = g.getGroupCode();
                    for (GroupMember gm : g.getMembers()) {
                        String sStr = gm.getStudent().getFullName() + " (" + gm.getStudent().getUsername() + ")";
                        if (Boolean.TRUE.equals(gm.getIsLeader())) {
                            sStr += " [Trưởng nhóm]";
                        }
                        studentInfoList.add(sStr);
                    }
                }
                Cell c4 = row.createCell(4);
                c4.setCellValue(groupCode);
                c4.setCellStyle(centerCellStyle);

                Cell c5 = row.createCell(5);
                c5.setCellValue(String.join(", ", studentInfoList));
                c5.setCellStyle(dataCellStyle);

                // 6. GV Hướng dẫn
                List<String> advisorNames = new ArrayList<>();
                for (TopicAdvisor adv : t.getAdvisors()) {
                    String aStr = adv.getLecturer().getFullName();
                    if (Boolean.TRUE.equals(adv.getIsPrimary())) {
                        aStr += " (Chính)";
                    } else {
                        aStr += " (Đồng HD)";
                    }
                    advisorNames.add(aStr);
                }
                Cell c6 = row.createCell(6);
                c6.setCellValue(String.join(", ", advisorNames));
                c6.setCellStyle(dataCellStyle);

                // 7. Điểm chi tiết & Điểm TB
                List<Evaluation> evals = evaluationService.getEvaluationsByTopic(t.getId(), null);
                List<String> advisorScores = new ArrayList<>();
                List<String> reviewerScores = new ArrayList<>();
                List<String> councilScores = new ArrayList<>();

                for (Evaluation ev : evals) {
                    String scoreStr = String.format("%.2f", ev.getScore());
                    if (ev.getEvaluationType() == EvaluationType.ADVISOR) {
                        advisorScores.add(scoreStr);
                    } else if (ev.getEvaluationType() == EvaluationType.REVIEWER) {
                        reviewerScores.add(scoreStr + " (" + ev.getEvaluator().getFullName() + ")");
                    } else if (ev.getEvaluationType() == EvaluationType.COUNCIL) {
                        councilScores.add(scoreStr + " (" + ev.getEvaluator().getFullName() + ")");
                    }
                }

                Cell c7 = row.createCell(7);
                c7.setCellValue(String.join(", ", advisorScores));
                c7.setCellStyle(centerCellStyle);

                Cell c8 = row.createCell(8);
                c8.setCellValue(String.join(", ", reviewerScores));
                c8.setCellStyle(centerCellStyle);

                Cell c9 = row.createCell(9);
                c9.setCellValue(String.join(", ", councilScores));
                c9.setCellStyle(centerCellStyle);

                // 8. Điểm TB & Xếp loại
                Double avg = evaluationService.getAverageScore(t.getId());
                Cell c10 = row.createCell(10);
                Cell c11 = row.createCell(11);
                c11.setCellStyle(centerCellStyle);

                if (avg != null) {
                    c10.setCellValue(avg);
                    c10.setCellStyle(scoreCellStyle);

                    if (avg >= 8.5) {
                        c11.setCellValue("GIỎI");
                    } else if (avg >= 7.0) {
                        c11.setCellValue("KHÁ");
                    } else if (avg >= 5.0) {
                        c11.setCellValue("ĐẠT");
                    } else {
                        c11.setCellValue("KHÔNG ĐẠT");
                    }
                } else {
                    c10.setCellValue("Chưa có");
                    c10.setCellStyle(centerCellStyle);
                    c11.setCellValue("-");
                }

                rowIdx++;
            }

            // Tự động điều chỉnh độ rộng cột
            for (int col = 0; col < headers.length; col++) {
                sheet.autoSizeColumn(col);
                int currentWidth = sheet.getColumnWidth(col);
                sheet.setColumnWidth(col, Math.min(Math.max(currentWidth + 1200, 3000), 18000));
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            log.error("Lỗi khi xuất bảng điểm ra Excel: ", e);
            throw new RuntimeException("Không thể tạo file Excel báo cáo: " + e.getMessage());
        }
    }

    private String sanitizeCell(String val) {
        if (val == null) return "";
        String trimmed = val.trim();
        if (trimmed.startsWith("=") || trimmed.startsWith("+") || trimmed.startsWith("-") || trimmed.startsWith("@")) {
            return "'" + trimmed;
        }
        return trimmed;
    }

}
