package vn.edu.ute.capstone.entity;

public enum PeriodStatus {
    DRAFT("Bản nháp"),
    OPEN_LECTURER("Mở cho GV nộp đề tài"),
    OPEN_STUDENT("Mở cho SV đăng ký"),
    IN_PROGRESS("Đang thực hiện đề tài"),
    REVIEWING("Đang chấm phản biện"),
    DEFENSE("Báo cáo hội đồng"),
    COMPLETED("Hoàn thành đợt");

    private final String displayName;

    PeriodStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
