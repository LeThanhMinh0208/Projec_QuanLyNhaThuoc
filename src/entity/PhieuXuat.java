package entity;

import java.time.LocalDateTime;

public class PhieuXuat {
    private String maPhieuXuat;
    private LocalDateTime ngayXuat;
    private String maNhanVien;
    private int loaiPhieu; // 1: Chuyển kho nội bộ | 2: Trả NCC | 3: Xuất hủy
    private String maNhaCungCap; // Có thể null
    private String khoNhan; // Có thể null
    private double tongTien;
    private String ghiChu;

    public PhieuXuat() {
    }

    public PhieuXuat(String maPhieuXuat, LocalDateTime ngayXuat, String maNhanVien, int loaiPhieu,
            String maNhaCungCap, String khoNhan, double tongTien, String ghiChu) {
        this.maPhieuXuat = maPhieuXuat;
        this.ngayXuat = ngayXuat;
        this.maNhanVien = maNhanVien;
        this.loaiPhieu = loaiPhieu;
        this.maNhaCungCap = maNhaCungCap;
        this.khoNhan = khoNhan;
        this.tongTien = tongTien;
        this.ghiChu = ghiChu;
    }

    // --- Sếp tự Generate Getters và Setters ở đây nhé ---
    public String getMaPhieuXuat() {
        return maPhieuXuat;
    }

    public void setMaPhieuXuat(String maPhieuXuat) {
        this.maPhieuXuat = maPhieuXuat;
    }

    public LocalDateTime getNgayXuat() {
        return ngayXuat;
    }

    public void setNgayXuat(LocalDateTime ngayXuat) {
        this.ngayXuat = ngayXuat;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public int getLoaiPhieu() {
        return loaiPhieu;
    }

    public void setLoaiPhieu(int loaiPhieu) {
        this.loaiPhieu = loaiPhieu;
    }

    public String getMaNhaCungCap() {
        return maNhaCungCap;
    }

    public void setMaNhaCungCap(String maNhaCungCap) {
        this.maNhaCungCap = maNhaCungCap;
    }

    public String getKhoNhan() {
        return khoNhan;
    }

    public void setKhoNhan(String khoNhan) {
        this.khoNhan = khoNhan;
    }

    public double getTongTien() {
        return tongTien;
    }

    public void setTongTien(double tongTien) {
        this.tongTien = tongTien;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}