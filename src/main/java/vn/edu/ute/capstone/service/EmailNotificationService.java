package vn.edu.ute.capstone.service;

public interface EmailNotificationService {

    /**
     * Gửi email văn bản thông thường
     */
    void sendSimpleEmail(String to, String subject, String content);

    /**
     * Gửi email thông báo HTML có định dạng chuyên nghiệp
     */
    void sendHtmlEmail(String to, String subject, String htmlContent);

    /**
     * Gửi thông báo khi đề tài được phê duyệt / từ chối
     */
    void notifyTopicStatusChange(String lecturerEmail, String topicTitle, boolean isApproved, String reason);

    /**
     * Gửi thông báo cho GVHD khi nhóm sinh viên nộp báo cáo
     */
    void notifyNewSubmission(String advisorEmail, String groupCode, String topicTitle, String submissionTitle);

    /**
     * Gửi thông báo công bố điểm cho sinh viên
     */
    void notifyScorePublished(String studentEmail, String studentName, String topicTitle, Double score);

}
