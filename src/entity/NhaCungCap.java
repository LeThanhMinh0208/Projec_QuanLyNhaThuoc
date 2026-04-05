package entity;

public class NhaCungCap {
    private String maNhaCungCap;
    private String tenNhaCungCap;
    private String sdt;
    private String diaChi;
    private double congNo;
    private boolean trangThai; // true = hoạt động, false = đã vô hiệu

    public NhaCungCap() {
        this.trangThai = true;
    }

    public NhaCungCap(String maNhaCungCap, String tenNhaCungCap, String sdt, String diaChi, double congNo) {
        this.maNhaCungCap = maNhaCungCap;
        this.tenNhaCungCap = tenNhaCungCap;
        this.sdt = sdt;
        this.diaChi = diaChi;
        this.congNo = congNo;
        this.trangThai = true;
    }

    public NhaCungCap(String maNhaCungCap, String tenNhaCungCap, String sdt, String diaChi, double congNo, boolean trangThai) {
        this.maNhaCungCap = maNhaCungCap;
        this.tenNhaCungCap = tenNhaCungCap;
        this.sdt = sdt;
        this.diaChi = diaChi;
        this.congNo = congNo;
        this.trangThai = trangThai;
    }

    // Getters and Setters
    public String getMaNhaCungCap() { return maNhaCungCap; }
    public void setMaNhaCungCap(String maNhaCungCap) { this.maNhaCungCap = maNhaCungCap; }
    public String getTenNhaCungCap() { return tenNhaCungCap; }
    public void setTenNhaCungCap(String tenNhaCungCap) { this.tenNhaCungCap = tenNhaCungCap; }
    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    public double getCongNo() { return congNo; }
    public void setCongNo(double congNo) { this.congNo = congNo; }
    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }

    @Override
    public String toString() {
        return this.tenNhaCungCap == null ? "" : this.tenNhaCungCap;
    }
}