package entity;

public enum HinhThucDoiTra {
    HOAN_TIEN("Hoàn tiền"),
    DOI_SAN_PHAM("Đổi sản phẩm");

    private String moTa;
    HinhThucDoiTra(String moTa) { this.moTa = moTa; }
    @Override
    public String toString() { return moTa; }
}