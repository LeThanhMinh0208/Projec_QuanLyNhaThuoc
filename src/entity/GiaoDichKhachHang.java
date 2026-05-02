package entity;

import java.sql.Timestamp;

public class GiaoDichKhachHang {
	private String tenKhachHang;
	private String sdtKhachHang;
    private String maHoaDon;
    private Timestamp ngayLap;
    private String tenNhanVien;
    private double tamTinh;
    private double thueVAT;
    private double tongSauVAT;
    private String hinhThucThanhToan;
    private String ghiChu;

    public GiaoDichKhachHang() {}

    // Getters & Setters
    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String v) { this.maHoaDon = v; }
    public Timestamp getNgayLap() { return ngayLap; }
    public void setNgayLap(Timestamp v) { this.ngayLap = v; }
    public String getTenNhanVien() { return tenNhanVien; }
    public void setTenNhanVien(String v) { this.tenNhanVien = v; }
    public double getTamTinh() { return tamTinh; }
    public void setTamTinh(double v) { this.tamTinh = v; }
    public double getThueVAT() { return thueVAT; }
    public void setThueVAT(double v) { this.thueVAT = v; }
    public double getTongSauVAT() { return tongSauVAT; }
    public void setTongSauVAT(double v) { this.tongSauVAT = v; }
    public String getHinhThucThanhToan() { return hinhThucThanhToan; }
    public void setHinhThucThanhToan(String v) { this.hinhThucThanhToan = v; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String v) { this.ghiChu = v; }
    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getSdtKhachHang() {
        return sdtKhachHang;
    }

    public void setSdtKhachHang(String sdtKhachHang) {
        this.sdtKhachHang = sdtKhachHang;
    }
    public String getHinhThucLabel() {
        if (hinhThucThanhToan == null) {
			return "Tiền mặt";
		}
        switch (hinhThucThanhToan) {
            case "CHUYEN_KHOAN": return "Chuyển khoản";
            case "THE": return "Thẻ tín dụng";
            default: return "Tiền mặt";
        }
    }
}