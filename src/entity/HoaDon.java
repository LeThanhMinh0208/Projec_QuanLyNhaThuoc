package entity;
import java.sql.Timestamp;

public class HoaDon {
    private String maHoaDon;
    private Timestamp ngayLap;
    private double thueVAT;
    private String hinhThucThanhToan; // Enum: TIEN_MAT, CHUYEN_KHOAN, THE
    private String ghiChu;
    private NhanVien nhanVien;
    private KhachHang khachHang;
    private String loaiBan; // BAN_LE hoặc BAN_THEO_DON

    public HoaDon() {}

    public HoaDon(String maHoaDon, Timestamp ngayLap, double thueVAT, String hinhThucThanhToan, String ghiChu, NhanVien nhanVien, KhachHang khachHang) {
        this.maHoaDon = maHoaDon;
        this.ngayLap = ngayLap;
        this.thueVAT = thueVAT;
        this.hinhThucThanhToan = hinhThucThanhToan;
        this.ghiChu = ghiChu;
        this.nhanVien = nhanVien;
        this.khachHang = khachHang;
        this.loaiBan = "BAN_LE"; // default
    }

    // Getters and Setters
    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }
    public Timestamp getNgayLap() { return ngayLap; }
    public void setNgayLap(Timestamp ngayLap) { this.ngayLap = ngayLap; }
    public double getThueVAT() { return thueVAT; }
    public void setThueVAT(double thueVAT) { this.thueVAT = thueVAT; }
    public String getHinhThucThanhToan() { return hinhThucThanhToan; }
    public void setHinhThucThanhToan(String hinhThucThanhToan) { this.hinhThucThanhToan = hinhThucThanhToan; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }
    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }
    public String getLoaiBan() { return loaiBan; }
    public void setLoaiBan(String loaiBan) { this.loaiBan = loaiBan; }
}