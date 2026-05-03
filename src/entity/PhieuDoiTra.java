package entity;

import java.util.Date;

public class PhieuDoiTra {
    private String maPhieuDoiTra;
    private Date ngayDoiTra;
    private String lyDo;
    private HinhThucDoiTra hinhThucXuLy; // Đã chuyển sang Enum
    private double phiPhat;
    private String ketQuaDoiSanPham;
    private String danhSachThuocDoi;

    // Sử dụng Object thay vì String để đúng quan hệ trong sơ đồ lớp
    private HoaDon hoaDon;
    private NhanVien nhanVien;
    private KhachHang khachHang;

    public PhieuDoiTra() {
        super();
    }

    public PhieuDoiTra(String maPhieuDoiTra, HoaDon hoaDon, NhanVien nhanVien, KhachHang khachHang, Date ngayDoiTra,
            String lyDo, HinhThucDoiTra hinhThucXuLy, double phiPhat) {
        super();
        this.maPhieuDoiTra = maPhieuDoiTra;
        this.hoaDon = hoaDon;
        this.nhanVien = nhanVien;
        this.khachHang = khachHang;
        this.ngayDoiTra = ngayDoiTra;
        this.lyDo = lyDo;
        this.hinhThucXuLy = hinhThucXuLy;
        this.phiPhat = phiPhat;
    }

    // --- GETTERS AND SETTERS ---
    public String getMaPhieuDoiTra() { return maPhieuDoiTra; }
    public void setMaPhieuDoiTra(String maPhieuDoiTra) { this.maPhieuDoiTra = maPhieuDoiTra; }

    public HoaDon getHoaDon() { return hoaDon; }
    public void setHoaDon(HoaDon hoaDon) { this.hoaDon = hoaDon; }

    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }

    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }

    public Date getNgayDoiTra() { return ngayDoiTra; }
    public void setNgayDoiTra(Date ngayDoiTra) { this.ngayDoiTra = ngayDoiTra; }

    public String getLyDo() { return lyDo; }
    public void setLyDo(String lyDo) { this.lyDo = lyDo; }

    public HinhThucDoiTra getHinhThucXuLy() { return hinhThucXuLy; }
    public void setHinhThucXuLy(HinhThucDoiTra hinhThucXuLy) { this.hinhThucXuLy = hinhThucXuLy; }

    public double getPhiPhat() { return phiPhat; }
    public void setPhiPhat(double phiPhat) { this.phiPhat = phiPhat; }

    public String getKetQuaDoiSanPham() { return ketQuaDoiSanPham; }
    public void setKetQuaDoiSanPham(String ketQuaDoiSanPham) { this.ketQuaDoiSanPham = ketQuaDoiSanPham; }

    public String getDanhSachThuocDoi() { return danhSachThuocDoi; }
    public void setDanhSachThuocDoi(String danhSachThuocDoi) { this.danhSachThuocDoi = danhSachThuocDoi; }
}