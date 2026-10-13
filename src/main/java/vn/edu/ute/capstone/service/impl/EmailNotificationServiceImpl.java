package vn.edu.ute.capstone.service.impl;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import vn.edu.ute.capstone.service.EmailNotificationService;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private final JavaMailSender mailSender;

    @Override
    @Async
    public void sendSimpleEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("hcmute.capstone@student.hcmute.edu.vn");
            message.setTo(to);
            message.setSubject("[HCMUTE CAPSTONE] " + subject);
            message.setText(content);
            mailSender.send(message);
            log.info("[EMAIL SENT] Successfully sent email to '{}' with subject: '{}'", to, subject);
        } catch (Exception e) {
            log.warn("[EMAIL FAILED] Could not send email to '{}': {}. (Running with mock fallback)", to, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setFrom("hcmute.capstone@student.hcmute.edu.vn");
            helper.setTo(to);
            helper.setSubject("[HCMUTE CAPSTONE] " + subject);
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
            log.info("[HTML EMAIL SENT] Successfully sent HTML email to '{}'", to);
        } catch (Exception e) {
            log.warn("[HTML EMAIL FAILED] Could not send HTML email to '{}': {}. (Running with mock fallback)", to, e.getMessage());
        }
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;");
    }

    @Override
    public void notifyTopicStatusChange(String lecturerEmail, String topicTitle, boolean isApproved, String reason) {
        String statusText = isApproved ? "ĐÃ ĐƯỢC PHÊ DUYỆT" : "BỊ TỪ CHỐI";
        String subject = "Thông báo kết quả xét duyệt đề tài: " + escapeHtml(topicTitle);

        StringBuilder html = new StringBuilder();
        html.append("<div style='font-family: Arial, sans-serif; padding: 20px; border: 2px solid #000;'>");
        html.append("<h2 style='color: #000; text-transform: uppercase;'>HCMUTE CAPSTONE - THÔNG BÁO XÉT DUYỆT</h2>");
        html.append("<p>Kính gửi Quý Thầy/Cô,</p>");
        html.append("<p>Đề tài: <strong>").append(escapeHtml(topicTitle)).append("</strong></p>");
        html.append("<p>Trạng thái: <strong style='color: ").append(isApproved ? "#16a34a" : "#dc2626").append(";'>").append(statusText).append("</strong></p>");
        if (!isApproved && reason != null) {
            html.append("<p><strong>Lý do từ chối / Yêu cầu chỉnh sửa:</strong> ").append(escapeHtml(reason)).append("</p>");
        }
        html.append("<hr style='border: 1px solid #000;'/>");
        html.append("<p style='font-size: 12px; color: #666;'>Đây là email tự động từ Hệ thống Quản lý Đề tài Sinh viên - Khoa CNTT HCMUTE.</p>");
        html.append("</div>");

        sendHtmlEmail(lecturerEmail, subject, html.toString());
    }

    @Override
    public void notifyNewSubmission(String advisorEmail, String groupCode, String topicTitle, String submissionTitle) {
        String subject = "Nhóm " + escapeHtml(groupCode) + " đã nộp báo cáo: " + escapeHtml(submissionTitle);

        StringBuilder html = new StringBuilder();
        html.append("<div style='font-family: Arial, sans-serif; padding: 20px; border: 2px solid #000;'>");
        html.append("<h2 style='color: #000; text-transform: uppercase;'>HCMUTE CAPSTONE - BÁO CÁO MỚI</h2>");
        html.append("<p>Kính gửi Giảng viên hướng dẫn,</p>");
        html.append("<p>Nhóm <strong>").append(escapeHtml(groupCode)).append("</strong> vừa tải lên tài liệu báo cáo mới cho đề tài:</p>");
        html.append("<p><strong>").append(escapeHtml(topicTitle)).append("</strong></p>");
        html.append("<p>Tiêu đề báo cáo: <strong>").append(escapeHtml(submissionTitle)).append("</strong></p>");
        html.append("<p>Vui lòng đăng nhập hệ thống để tải xuống và xem xét tiến độ của nhóm.</p>");
        html.append("<hr style='border: 1px solid #000;'/>");
        html.append("<p style='font-size: 12px; color: #666;'>Khoa CNTT - Đại học Sư phạm Kỹ thuật TP.HCM</p>");
        html.append("</div>");

        sendHtmlEmail(advisorEmail, subject, html.toString());
    }

    @Override
    public void notifyScorePublished(String studentEmail, String studentName, String topicTitle, Double score) {
        String subject = "Công bố điểm đánh giá đề tài: " + escapeHtml(topicTitle);

        StringBuilder html = new StringBuilder();
        html.append("<div style='font-family: Arial, sans-serif; padding: 20px; border: 2px solid #000;'>");
        html.append("<h2 style='color: #000; text-transform: uppercase;'>HCMUTE CAPSTONE - KẾT QUẢ ĐÁNH GIÁ</h2>");
        html.append("<p>Chào bạn <strong>").append(escapeHtml(studentName)).append("</strong>,</p>");
        html.append("<p>Đề tài: <strong>").append(escapeHtml(topicTitle)).append("</strong></p>");
        html.append("<p>Điểm tổng kết chính thức: <strong style='font-size: 18px;'>").append(String.format("%.2f", score)).append(" / 10.0</strong></p>");
        html.append("<p>Chúc mừng bạn đã hoàn thành đợt bảo vệ đồ án/khóa luận!</p>");
        html.append("<hr style='border: 1px solid #000;'/>");
        html.append("<p style='font-size: 12px; color: #666;'>Khoa CNTT - Đại học Sư phạm Kỹ thuật TP.HCM</p>");
        html.append("</div>");

        sendHtmlEmail(studentEmail, subject, html.toString());
    }
}
