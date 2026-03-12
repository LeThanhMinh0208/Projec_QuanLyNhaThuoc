package entity;
import java.sql.Date;

public class PhieuNhap {
    private String maPhieuNhap;
    private Date ngayNhap;
    private NhaCungCap nhaCungCap;
    private NhanVien nhanVien;

    public PhieuNhap() {}

    public PhieuNhap(String maPhieuNhap, Date ngayNhap, NhaCungCap nhaCungCap, NhanVien nhanVien) {
        this.maPhieuNhap = maPhieuNhap;
        this.ngayNhap = ngayNhap;
        this.nhaCungCap = nhaCungCap;
        this.nhanVien = nhanVien;
    }

    // Getters and Setters
    public String getMaPhieuNhap() { return maPhieuNhap; }
    public void setMaPhieuNhap(String maPhieuNhap) { this.maPhieuNhap = maPhieuNhap; }
    public Date getNgayNhap() { return ngayNhap; }
    public void setNgayNhap(Date ngayNhap) { this.ngayNhap = ngayNhap; }
    public NhaCungCap getNhaCungCap() { return nhaCungCap; }
    public void setNhaCungCap(NhaCungCap nhaCungCap) { this.nhaCungCap = nhaCungCap; }
    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }
}