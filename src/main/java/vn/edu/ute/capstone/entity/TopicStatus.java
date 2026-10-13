package vn.edu.ute.capstone.entity;

public enum TopicStatus {
    PENDING_DEPT("Chờ bộ môn duyệt"),
    APPROVED("Đã phê duyệt"),
    REJECTED("Bị từ chối"),
    ASSIGNED("Đã có nhóm nhận");

    private final String displayName;

    TopicStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
