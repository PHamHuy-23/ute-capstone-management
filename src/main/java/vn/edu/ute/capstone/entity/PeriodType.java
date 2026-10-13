package vn.edu.ute.capstone.entity;

public enum PeriodType {
    COURSE("Môn học"),
    RESEARCH("Nghiên cứu khoa học"),
    ESSAY("Tiểu luận chuyên ngành"),
    CAPSTONE("Khóa luận tốt nghiệp");

    private final String displayName;

    PeriodType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
