package vn.edu.ute.capstone.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.ute.capstone.dto.RegistrationPeriodDTO;
import vn.edu.ute.capstone.entity.PeriodStatus;
import vn.edu.ute.capstone.entity.PeriodType;
import vn.edu.ute.capstone.entity.RegistrationPeriod;
import vn.edu.ute.capstone.repository.RegistrationPeriodRepository;
import vn.edu.ute.capstone.repository.TopicRepository;
import vn.edu.ute.capstone.service.RegistrationPeriodService;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RegistrationPeriodServiceImpl implements RegistrationPeriodService {

    private final RegistrationPeriodRepository periodRepository;
    private final TopicRepository topicRepository;

    // Định nghĩa bảng chuyển trạng thái hợp lệ (State Machine Transitions)
    // Mỗi trạng thái chỉ được chuyển sang các trạng thái kế tiếp theo đúng quy trình
    private static final Map<PeriodStatus, Set<PeriodStatus>> VALID_TRANSITIONS = Map.of(
            PeriodStatus.DRAFT, Set.of(PeriodStatus.OPEN_LECTURER),
            PeriodStatus.OPEN_LECTURER, Set.of(PeriodStatus.OPEN_STUDENT, PeriodStatus.DRAFT),
            PeriodStatus.OPEN_STUDENT, Set.of(PeriodStatus.IN_PROGRESS, PeriodStatus.OPEN_LECTURER),
            PeriodStatus.IN_PROGRESS, Set.of(PeriodStatus.REVIEWING),
            PeriodStatus.REVIEWING, Set.of(PeriodStatus.DEFENSE, PeriodStatus.COMPLETED),
            PeriodStatus.DEFENSE, Set.of(PeriodStatus.COMPLETED),
            PeriodStatus.COMPLETED, Set.of() // Một khi đã COMPLETED thì đóng băng, không cho chuyển tiếp
    );

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationPeriod> getAllPeriods() {
        return periodRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public RegistrationPeriod getPeriodById(Long id) {
        return periodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đợt đăng ký với ID: " + id));
    }

    @Override
    @Transactional
    public RegistrationPeriod createPeriod(RegistrationPeriodDTO dto) {
        validatePeriodDates(dto);

        // Chặn trùng lặp: Kiểm tra xem có đợt nào cùng loại đang mở không
        validateNoConflictingActivePeriod(dto.getPeriodType(), null);

        RegistrationPeriod period = RegistrationPeriod.builder()
                .name(dto.getName().trim())
                .periodType(dto.getPeriodType())
                .lecturerStartDate(dto.getLecturerStartDate())
                .lecturerEndDate(dto.getLecturerEndDate())
                .studentStartDate(dto.getStudentStartDate())
                .studentEndDate(dto.getStudentEndDate())
                .reviewDeadline(dto.getReviewDeadline())
                .defenseDate(dto.getDefenseDate())
                .status(dto.getStatus() != null ? dto.getStatus() : PeriodStatus.DRAFT)
                .build();

        return periodRepository.save(period);
    }

    @Override
    @Transactional
    public RegistrationPeriod updatePeriod(Long id, RegistrationPeriodDTO dto) {
        RegistrationPeriod period = getPeriodById(id);

        // THỰC TẾ 1: Đợt đã COMPLETED thì đóng băng hoàn toàn, không cho sửa
        if (period.getStatus() == PeriodStatus.COMPLETED) {
            throw new IllegalStateException("Đợt đăng ký đã hoàn thành và đóng băng kết quả, không thể chỉnh sửa!");
        }

        // THỰC TẾ 2: Nếu đã có đề tài thuộc đợt này mà cố tình đổi loại đợt (ví dụ KLTN sang Môn học)
        if (period.getPeriodType() != dto.getPeriodType()) {
            boolean hasTopics = topicRepository.existsByPeriodId(id);
            if (hasTopics) {
                throw new IllegalStateException("Đợt này đã có giảng viên nộp đề tài, không được phép thay đổi loại hình đề tài!");
            }
        }

        validatePeriodDates(dto);
        validateNoConflictingActivePeriod(dto.getPeriodType(), id);

        period.setName(dto.getName().trim());
        period.setPeriodType(dto.getPeriodType());
        period.setLecturerStartDate(dto.getLecturerStartDate());
        period.setLecturerEndDate(dto.getLecturerEndDate());
        period.setStudentStartDate(dto.getStudentStartDate());
        period.setStudentEndDate(dto.getStudentEndDate());
        period.setReviewDeadline(dto.getReviewDeadline());
        period.setDefenseDate(dto.getDefenseDate());

        return periodRepository.save(period);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, PeriodStatus newStatus) {
        RegistrationPeriod period = getPeriodById(id);
        PeriodStatus currentStatus = period.getStatus();

        if (currentStatus == newStatus) {
            return;
        }

        // THỰC TẾ 3: Chặn State Skipping (nhảy cóc trạng thái bất hợp lệ)
        Set<PeriodStatus> allowedNextStates = VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowedNextStates.contains(newStatus)) {
            throw new IllegalStateException("Không thể chuyển trạng thái từ '" 
                    + currentStatus.getDisplayName() + "' sang '" + newStatus.getDisplayName() 
                    + "'. Vui lòng tuân thủ đúng quy trình!");
        }

        // THỰC TẾ 4: Muốn mở cho sinh viên (OPEN_STUDENT) thì phải có ít nhất 1 đề tài trong hệ thống
        if (newStatus == PeriodStatus.OPEN_STUDENT) {
            long topicCount = topicRepository.countByPeriodId(id);
            if (topicCount == 0) {
                throw new IllegalStateException("Không thể mở đợt cho sinh viên đăng ký khi chưa có đề tài nào được nộp trong đợt này!");
            }
        }

        period.setStatus(newStatus);
        periodRepository.save(period);
    }

    @Override
    @Transactional
    public void deletePeriod(Long id) {
        RegistrationPeriod period = getPeriodById(id);

        // THỰC TẾ 5: An toàn xóa (Safe Deletion Guard)
        // Tuyệt đối không xóa đợt nếu đã có đề tài nộp lên (tránh crash DB hoặc mất dữ liệu đồ án)
        boolean hasTopics = topicRepository.existsByPeriodId(id);
        if (hasTopics) {
            throw new IllegalStateException("Không thể xóa đợt đăng ký này vì đã có đề tài liên kết! Hãy chuyển trạng thái sang Đóng hoặc Hủy.");
        }

        periodRepository.delete(period);
    }

    private void validatePeriodDates(RegistrationPeriodDTO dto) {
        if (dto.getLecturerEndDate().isBefore(dto.getLecturerStartDate())) {
            throw new IllegalArgumentException("Hạn kết thúc nộp đề tài của GV phải sau ngày bắt đầu!");
        }

        if (dto.getStudentEndDate().isBefore(dto.getStudentStartDate())) {
            throw new IllegalArgumentException("Hạn kết thúc đăng ký của SV phải sau ngày bắt đầu!");
        }

        // Sinh viên không thể đăng ký trước khi giảng viên bắt đầu nộp đề tài
        if (dto.getStudentStartDate().isBefore(dto.getLecturerStartDate())) {
            throw new IllegalArgumentException("Thời gian SV đăng ký không thể diễn ra trước thời gian GV nộp đề tài!");
        }

        // Ràng buộc đề bài: Tiểu luận chuyên ngành (ESSAY) hoặc Khóa luận tốt nghiệp (CAPSTONE) cần có hạn nộp điểm GVPB
        if ((dto.getPeriodType() == PeriodType.ESSAY || dto.getPeriodType() == PeriodType.CAPSTONE) && dto.getReviewDeadline() == null) {
            throw new IllegalArgumentException("Đợt " + dto.getPeriodType().getDisplayName() + " bắt buộc phải thiết lập hạn chót nộp điểm phản biện!");
        }

        // Ràng buộc đề bài: Khóa luận tốt nghiệp (CAPSTONE) bắt buộc phải có ngày báo cáo hội đồng
        if (dto.getPeriodType() == PeriodType.CAPSTONE && dto.getDefenseDate() == null) {
            throw new IllegalArgumentException("Khóa luận tốt nghiệp bắt buộc phải thiết lập ngày báo cáo hội đồng!");
        }
    }

    private void validateNoConflictingActivePeriod(PeriodType type, Long currentPeriodId) {
        // Kiểm tra xem có đợt nào cùng loại đang ở trạng thái OPEN không
        List<RegistrationPeriod> activePeriods = periodRepository.findByPeriodType(type).stream()
                .filter(p -> p.getStatus() == PeriodStatus.OPEN_LECTURER || p.getStatus() == PeriodStatus.OPEN_STUDENT)
                .filter(p -> currentPeriodId == null || !p.getId().equals(currentPeriodId))
                .toList();

        if (!activePeriods.isEmpty()) {
            throw new IllegalArgumentException("Đang có một đợt " + type.getDisplayName() 
                    + " khác đang mở hoạt động ('" + activePeriods.get(0).getName() + "'). Vui lòng đóng đợt cũ trước!");
        }
    }

}
