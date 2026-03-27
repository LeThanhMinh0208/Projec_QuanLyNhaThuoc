package entity;

import java.sql.Timestamp;

/**
 * DTO tổng hợp cho màn hình Danh Sách Hóa Đơn.
 * Không ánh xạ 1:1 với table — là kết quả của query JOIN.
 */
public class HoaDonView {
    private String maHoaDon;
    private Timestamp ngayLap;
    private String tenKhachHang;   // null = Khách lẻ
    private String sdt;
    private String tenNhanVien;
    private double tamTinh;        // SUM(soLuong * donGia)
    private double thueVAT;        // % (ví dụ: 8.0)
    private double tongSauVAT;     // tamTinh * (1 + thueVAT/100)
    private String hinhThucThanhToan;
    private String ghiChu;

    public HoaDonView() {}

    // Getters & Setters
    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }

    public Timestamp getNgayLap() { return ngayLap; }
    public void setNgayLap(Timestamp ngayLap) { this.ngayLap = ngayLap; }

    public String getTenKhachHang() { return tenKhachHang != null ? tenKhachHang : "Khách lẻ"; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }

    public String getSdt() { return sdt != null ? sdt : "—"; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getTenNhanVien() { return tenNhanVien; }
    public void setTenNhanVien(String tenNhanVien) { this.tenNhanVien = tenNhanVien; }

    public double getTamTinh() { return tamTinh; }
    public void setTamTinh(double tamTinh) { this.tamTinh = tamTinh; }

    public double getThueVAT() { return thueVAT; }
    public void setThueVAT(double thueVAT) { this.thueVAT = thueVAT; }

    public double getTongSauVAT() { return tongSauVAT; }
    public void setTongSauVAT(double tongSauVAT) { this.tongSauVAT = tongSauVAT; }

    public String getHinhThucThanhToan() { return hinhThucThanhToan; }
    public void setHinhThucThanhToan(String h) { this.hinhThucThanhToan = h; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    /** Hiển thị hình thức thanh toán thân thiện */
    public String getHinhThucLabel() {
        if (hinhThucThanhToan == null) return "Tiền mặt";
        switch (hinhThucThanhToan) {
            case "CHUYEN_KHOAN": return "Chuyển khoản";
            case "THE":          return "Thẻ tín dụng";
            default:             return "Tiền mặt";
        }
    }
}
