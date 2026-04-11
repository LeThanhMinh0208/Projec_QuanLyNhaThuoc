package entity;

import java.sql.Timestamp;

public class HoaDonView {
    private String maHoaDon;
    private Timestamp ngayLap;
    private String tenKhachHang;
    private String sdt;
    private String tenNhanVien;
    private double tamTinh;
    private double thueVAT;
    private double tongSauVAT;
    private String hinhThucThanhToan;
    private String ghiChu;

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public Timestamp getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(Timestamp ngayLap) {
        this.ngayLap = ngayLap;
    }

    public String getTenKhachHang() {
        return tenKhachHang != null ? tenKhachHang : "Khach le";
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getSdt() {
        return sdt != null ? sdt : "--";
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getTenNhanVien() {
        return tenNhanVien;
    }

    public void setTenNhanVien(String tenNhanVien) {
        this.tenNhanVien = tenNhanVien;
    }

    public double getTamTinh() {
        return tamTinh;
    }

    public void setTamTinh(double tamTinh) {
        this.tamTinh = tamTinh;
    }

    public double getThueVAT() {
        return thueVAT;
    }

    public void setThueVAT(double thueVAT) {
        this.thueVAT = thueVAT;
    }

    public double getTongSauVAT() {
        return tongSauVAT;
    }

    public void setTongSauVAT(double tongSauVAT) {
        this.tongSauVAT = tongSauVAT;
    }

    public String getHinhThucThanhToan() {
        return hinhThucThanhToan;
    }

    public void setHinhThucThanhToan(String hinhThucThanhToan) {
        this.hinhThucThanhToan = hinhThucThanhToan;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getHinhThucLabel() {
        if (hinhThucThanhToan == null) {
            return "Tien mat";
        }
        switch (hinhThucThanhToan) {
            case "CHUYEN_KHOAN":
                return "Chuyen khoan";
            case "THE":
                return "The tin dung";
            default:
                return "Tien mat";
        }
    }
}
