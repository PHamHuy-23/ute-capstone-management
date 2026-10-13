package vn.edu.ute.capstone.entity;

public enum EvaluationType {
    ADVISOR("Điểm hướng dẫn (GVHD)"),
    REVIEWER("Điểm phản biện (GVPB)"),
    COUNCIL("Điểm hội đồng");

    private final String displayName;

    EvaluationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
