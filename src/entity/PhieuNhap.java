package entity;

import java.sql.Timestamp;

public class PhieuNhap {
    private String maPhieuNhap;
    private DonDatHang donDatHang; 
    private NhaCungCap nhaCungCap;
    private NhanVien nhanVien;
    private Timestamp ngayNhap;
    
    // Thêm trường phụ để tính tổng tiền trên UI
    private double tongTien;

    public PhieuNhap() {}

    // Getters and Setters
    public String getMaPhieuNhap() { return maPhieuNhap; }
    public void setMaPhieuNhap(String maPhieuNhap) { this.maPhieuNhap = maPhieuNhap; }
    public DonDatHang getDonDatHang() { return donDatHang; }
    public void setDonDatHang(DonDatHang donDatHang) { this.donDatHang = donDatHang; }
    public NhaCungCap getNhaCungCap() { return nhaCungCap; }
    public void setNhaCungCap(NhaCungCap nhaCungCap) { this.nhaCungCap = nhaCungCap; }
    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }
    public Timestamp getNgayNhap() { return ngayNhap; }
    public void setNgayNhap(Timestamp ngayNhap) { this.ngayNhap = ngayNhap; }
    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }
}