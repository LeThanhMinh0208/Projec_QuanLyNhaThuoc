package entity;

public enum ViTriKho {
    KHO_BAN_HANG("Kho bán hàng"),
    KHO_DU_TRU("Kho dự trữ");

    private final String displayName;

    ViTriKho(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}