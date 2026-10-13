package vn.edu.ute.capstone.entity;

public enum ReviewStatus {
    PENDING("Chờ phản hồi"),
    ACCEPTED("Đã chấp nhận"),
    REVISE_REQUIRED("Yêu cầu chỉnh sửa");

    private final String displayName;

    ReviewStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
