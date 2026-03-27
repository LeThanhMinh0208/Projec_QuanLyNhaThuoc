package entity;

public class ChiTietPhieuNhap {
    private PhieuNhap phieuNhap;
    private DonViQuyDoi donViQuyDoi;
    private LoThuoc loThuoc;
    private int soLuong;
    private double donGiaNhap;

    public ChiTietPhieuNhap() {}

    // Getters and Setters
    public PhieuNhap getPhieuNhap() { return phieuNhap; }
    public void setPhieuNhap(PhieuNhap phieuNhap) { this.phieuNhap = phieuNhap; }
    public DonViQuyDoi getDonViQuyDoi() { return donViQuyDoi; }
    public void setDonViQuyDoi(DonViQuyDoi donViQuyDoi) { this.donViQuyDoi = donViQuyDoi; }
    public LoThuoc getLoThuoc() { return loThuoc; }
    public void setLoThuoc(LoThuoc loThuoc) { this.loThuoc = loThuoc; }
    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
    public double getDonGiaNhap() { return donGiaNhap; }
    public void setDonGiaNhap(double donGiaNhap) { this.donGiaNhap = donGiaNhap; }
}