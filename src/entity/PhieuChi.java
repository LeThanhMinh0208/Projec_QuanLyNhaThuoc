package entity;

import java.util.Date;

public class PhieuChi {
    private String maPhieuChi;
    private NhaCungCap nhaCungCap; // Khóa ngoại trỏ sang Object NhaCungCap
    private NhanVien nhanVien; // Khóa ngoại trỏ sang Object NhanVien
    private Date ngayChi;
    private double tongTienChi;
    private String hinhThucChi;
    private String ghiChu;

    // Constructor mặc định
    public PhieuChi() {
    }

    // Constructor đầy đủ
    public PhieuChi(String maPhieuChi, NhaCungCap nhaCungCap, NhanVien nhanVien, Date ngayChi, double tongTienChi,
            String hinhThucChi, String ghiChu) {
        this.maPhieuChi = maPhieuChi;
        this.nhaCungCap = nhaCungCap;
        this.nhanVien = nhanVien;
        this.ngayChi = ngayChi;
        this.tongTienChi = tongTienChi;
        this.hinhThucChi = hinhThucChi;
        this.ghiChu = ghiChu;
    }

    // Getters and Setters
    public String getMaPhieuChi() {
        return maPhieuChi;
    }

    public void setMaPhieuChi(String maPhieuChi) {
        this.maPhieuChi = maPhieuChi;
    }

    public NhaCungCap getNhaCungCap() {
        return nhaCungCap;
    }

    public void setNhaCungCap(NhaCungCap nhaCungCap) {
        this.nhaCungCap = nhaCungCap;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public Date getNgayChi() {
        return ngayChi;
    }

    public void setNgayChi(Date ngayChi) {
        this.ngayChi = ngayChi;
    }

    public double getTongTienChi() {
        return tongTienChi;
    }

    public void setTongTienChi(double tongTienChi) {
        this.tongTienChi = tongTienChi;
    }

    public String getHinhThucChi() {
        return hinhThucChi;
    }

    public void setHinhThucChi(String hinhThucChi) {
        this.hinhThucChi = hinhThucChi;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    @Override
    public String toString() {
        return maPhieuChi;
    }
}