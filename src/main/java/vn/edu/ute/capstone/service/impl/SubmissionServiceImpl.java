package vn.edu.ute.capstone.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import vn.edu.ute.capstone.aspect.AuditLog;
import vn.edu.ute.capstone.config.CloudflareR2Config;
import vn.edu.ute.capstone.dto.SubmissionDTO;
import vn.edu.ute.capstone.entity.*;
import vn.edu.ute.capstone.repository.StudentGroupRepository;
import vn.edu.ute.capstone.repository.SubmissionRepository;
import vn.edu.ute.capstone.service.SubmissionService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final StudentGroupRepository groupRepository;
    private final S3Client s3Client;
    private final CloudflareR2Config r2Config;

    private final Path uploadRoot = Paths.get("uploads", "submissions");

    @Override
    @Transactional
    @AuditLog(action = "SUBMIT_REPORT", description = "Nhóm trưởng nộp tài liệu báo cáo tiến độ/hoàn thành")
    public Submission submitReport(SubmissionDTO dto, MultipartFile file, User currentLeader) {
        log.info("Student {} is submitting report for group {}", currentLeader.getUsername(), dto.getGroupId());

        // Guard 1: Nhóm có tồn tại không?
        StudentGroup group = groupRepository.findById(dto.getGroupId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhóm sinh viên tương ứng."));

        // Guard 2: Nhóm đã nhận đề tài chưa?
        if (group.getTopic() == null) {
            throw new IllegalStateException("Nhóm của bạn chưa được phân công hoặc chưa đăng ký đề tài, không thể nộp báo cáo.");
        }

        // Guard 3: Đợt đăng ký có đang ở trạng thái cho phép thực hiện/nộp bài không?
        PeriodStatus status = group.getPeriod().getStatus();
        if (status != PeriodStatus.OPEN_STUDENT && status != PeriodStatus.IN_PROGRESS && status != PeriodStatus.REVIEWING) {
            throw new IllegalStateException("Đợt đăng ký hiện ở trạng thái '" + status.getDisplayName() + "', không tiếp nhận nộp báo cáo.");
        }

        // Guard 4: Bắt buộc người nộp phải là Nhóm trưởng (Group Leader Invariant)
        boolean isLeader = group.getMembers().stream()
                .anyMatch(m -> m.getStudent().getId().equals(currentLeader.getId()) && Boolean.TRUE.equals(m.getIsLeader()));
        if (!isLeader) {
            throw new IllegalStateException("Theo quy định, chỉ duy nhất Nhóm trưởng mới có quyền đại diện nhóm nộp báo cáo.");
        }

        // Guard 5: Kiểm tra file đính kèm
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng tải lên file báo cáo (PDF, DOCX, ZIP, v.v.).");
        }

        // Lưu trữ file an toàn
        String storedFileName = saveFile(file, group.getGroupCode());

        Submission submission = Submission.builder()
                .group(group)
                .submittedBy(currentLeader)
                .title(dto.getTitle().trim())
                .filePath(storedFileName)
                .notes(dto.getNotes())
                .build();

        return submissionRepository.save(submission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Submission> getSubmissionsByGroup(Long groupId, User currentUser) {
        StudentGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhóm sinh viên tương ứng."));

        // Guard kiểm tra quyền xem báo cáo:
        // Phải là thành viên trong nhóm, HOẶC là GVHD của đề tài, HOẶC là Admin/Trưởng bộ môn
        validateAccessToGroupReports(group, currentUser);

        return submissionRepository.findByGroupIdOrderBySubmittedAtDesc(groupId);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource loadReportFile(Long submissionId, User currentUser) {
        Submission submission = findById(submissionId);
        validateAccessToGroupReports(submission.getGroup(), currentUser);

        String fileName = submission.getFilePath();

        // 1. Thử tải từ Cloudflare R2 trước
        try {
            if (r2Config != null && r2Config.getBucketName() != null && !r2Config.getBucketName().isBlank()) {
                log.info("Đang tải file từ Cloudflare R2: bucket={}, key={}", r2Config.getBucketName(), fileName);
                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                        .bucket(r2Config.getBucketName())
                        .key(fileName)
                        .build();

                ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
                byte[] data = objectBytes.asByteArray();
                return new org.springframework.core.io.ByteArrayResource(data) {
                    @Override
                    public String getFilename() {
                        return fileName;
                    }
                };
            }
        } catch (Exception e) {
            log.warn("Không tìm thấy file trên Cloudflare R2 ({}), thử tìm tại ổ đĩa local...", e.getMessage());
        }

        // 2. Fallback tìm tại thư mục uploads/submissions cục bộ
        try {
            Path filePath = this.uploadRoot.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new IllegalStateException("File báo cáo không tồn tại hoặc không thể đọc trên Cloudflare R2 lẫn máy chủ.");
            }
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Đường dẫn file không hợp lệ: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Submission findById(Long submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bản nộp báo cáo với ID: " + submissionId));
    }

    @Override
    @Transactional
    public Submission reviewSubmission(Long submissionId, String feedback, vn.edu.ute.capstone.entity.ReviewStatus status, User currentLecturer) {
        Submission submission = findById(submissionId);
        StudentGroup group = submission.getGroup();

        // Guard: Chỉ GVHD của đề tài mới được phản hồi báo cáo
        boolean isAdmin = currentLecturer.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getName()));
        boolean isAdvisor = false;
        if (group.getTopic() != null) {
            isAdvisor = group.getTopic().getAdvisors().stream()
                    .anyMatch(a -> a.getLecturer().getId().equals(currentLecturer.getId()));
        }

        if (!isAdmin && !isAdvisor) {
            throw new IllegalStateException("Bạn không phải là Giảng viên hướng dẫn của nhóm này để đưa ra phản hồi.");
        }

        if (feedback == null || feedback.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập nội dung nhận xét / góp ý cho nhóm sinh viên.");
        }

        submission.setFeedback(feedback.trim());
        submission.setReviewStatus(status != null ? status : vn.edu.ute.capstone.entity.ReviewStatus.ACCEPTED);
        submission.setReviewedAt(LocalDateTime.now());

        return submissionRepository.save(submission);
    }

    private void validateAccessToGroupReports(StudentGroup group, User currentUser) {
        // Admin hoặc Trưởng khoa toàn quyền
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getName()));
        if (isAdmin) return;

        // Thành viên trong nhóm
        boolean isMember = group.getMembers().stream()
                .anyMatch(m -> m.getStudent().getId().equals(currentUser.getId()));
        if (isMember) return;

        // Giảng viên hướng dẫn của đề tài
        if (group.getTopic() != null) {
            boolean isAdvisor = group.getTopic().getAdvisors().stream()
                    .anyMatch(a -> a.getLecturer().getId().equals(currentUser.getId()));
            if (isAdvisor) return;
        }

        // Trưởng bộ môn phụ trách đề tài
        if (group.getTopic() != null && currentUser.getDepartment() != null
                && currentUser.getRoles().stream().anyMatch(r -> "ROLE_DEPARTMENT_HEAD".equals(r.getName()))
                && group.getTopic().getDepartment().getId().equals(currentUser.getDepartment().getId())) {
            return;
        }

        // Thành viên trong Hội đồng chấm đề tài này (được phép xem đồ án để chấm điểm)
        if (group.getTopic() != null) {
            boolean isCouncilMember = group.getTopic().getCouncils().stream()
                    .anyMatch(c -> c.getMembers().stream()
                            .anyMatch(m -> m.getLecturer().getId().equals(currentUser.getId())));
            if (isCouncilMember) return;
        }

        throw new IllegalStateException("Bạn không có quyền xem hoặc tải file báo cáo của nhóm này.");
    }

    private static final java.util.Set<String> ALLOWED_EXTENSIONS = java.util.Set.of(
            ".pdf", ".docx", ".doc", ".zip", ".rar", ".xlsx", ".xls", ".pptx", ".ppt"
    );

    private String saveFile(MultipartFile file, String groupCode) {
        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Định dạng file không hợp lệ! Hệ thống chỉ chấp nhận các định dạng học thuật an toàn (.pdf, .docx, .doc, .zip, .rar, .xlsx, .pptx).");
        }

        // Tên file độc nhất: GROUPCODE_UUID.ext
        String uniqueName = groupCode + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

        // 1. Tải lên Cloudflare R2 (S3-Compatible)
        boolean r2Success = false;
        try {
            if (r2Config != null && r2Config.getBucketName() != null && !r2Config.getBucketName().isBlank()) {
                log.info("Đang tải file lên Cloudflare R2: bucket={}, key={}", r2Config.getBucketName(), uniqueName);
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(r2Config.getBucketName())
                        .key(uniqueName)
                        .contentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                        .build();

                s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
                log.info("Tải file lên Cloudflare R2 thành công: {}", uniqueName);
                r2Success = true;
            }
        } catch (Exception e) {
            log.error("Lỗi khi tải lên Cloudflare R2, tự động lưu fallback cục bộ: ", e);
        }

        // 2. Luôn đồng bộ 1 bản local backup dự phòng
        try {
            if (!Files.exists(uploadRoot)) {
                Files.createDirectories(uploadRoot);
            }
            Path destination = uploadRoot.resolve(uniqueName);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            if (!r2Success) {
                log.error("Lỗi khi lưu trữ file cục bộ: ", e);
                throw new RuntimeException("Không thể lưu trữ file tải lên: " + e.getMessage());
            }
        }

        return uniqueName;
    }
}
