package entity;

public class ChiTietPhieuXuat {
    private String maPhieuXuat;
    private String maThuoc;
    private String soLo;
    private int soLuong;
    private double donGia;
    private double thanhTien;

    public ChiTietPhieuXuat() {}

    public ChiTietPhieuXuat(String maPhieuXuat, String maThuoc, String soLo, int soLuong, double donGia, double thanhTien) {
        this.maPhieuXuat = maPhieuXuat;
        this.maThuoc = maThuoc;
        this.soLo = soLo;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
    }

    // --- Getters và Setters ---
    public String getMaPhieuXuat() { return maPhieuXuat; }
    public void setMaPhieuXuat(String maPhieuXuat) { this.maPhieuXuat = maPhieuXuat; }
    public String getMaThuoc() { return maThuoc; }
    public void setMaThuoc(String maThuoc) { this.maThuoc = maThuoc; }
    public String getSoLo() { return soLo; }
    public void setSoLo(String soLo) { this.soLo = soLo; }
    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }
    public double getThanhTien() { return thanhTien; }
    public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }
}