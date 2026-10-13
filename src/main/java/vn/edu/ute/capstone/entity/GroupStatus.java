package vn.edu.ute.capstone.entity;

public enum GroupStatus {
    REGISTERING("Đang lập nhóm & Đăng ký"),
    ACCEPTED("Đã chấp thuận đề tài"),
    COMPLETED("Hoàn thành đề tài");

    private final String displayName;

    GroupStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
