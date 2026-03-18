package entity;

import java.sql.Date;

public class DonNhapHang {
    private String maDonNhap;
    private NhaCungCap nhaCungCap;
    private NhanVien nhanVien;
    private Date ngayLap;
    private Date ngayHenGiao;
    private double tongTienDuTinh;
    private String trangThai;
    private String ghiChu;

    public DonNhapHang() {}

    // Getter và Setter
    public String getMaDonNhap() { return maDonNhap; }
    public void setMaDonNhap(String maDonNhap) { this.maDonNhap = maDonNhap; }

    public NhaCungCap getNhaCungCap() { return nhaCungCap; }
    public void setNhaCungCap(NhaCungCap nhaCungCap) { this.nhaCungCap = nhaCungCap; }

    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }

    public Date getNgayLap() { return ngayLap; }
    public void setNgayLap(Date ngayLap) { this.ngayLap = ngayLap; }

    public Date getNgayHenGiao() { return ngayHenGiao; }
    public void setNgayHenGiao(Date ngayHenGiao) { this.ngayHenGiao = ngayHenGiao; }

    public double getTongTienDuTinh() { return tongTienDuTinh; }
    public void setTongTienDuTinh(double tongTienDuTinh) { this.tongTienDuTinh = tongTienDuTinh; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    @Override
    public String toString() {
        return maDonNhap; 
      
    }
}