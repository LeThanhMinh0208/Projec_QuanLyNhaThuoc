package entity;

import java.sql.Timestamp;

public class PhieuKiemKe {
    private String maPhieuKiemKe;
    private Timestamp ngayTao;
    private NhanVien nhanVienTao;
    private NhanVien nhanVienDuyet;
    private Timestamp ngayDuyet;
    private String trangThai;
    private String ghiChu;

    // Constructors
    public PhieuKiemKe() {
    }

    public PhieuKiemKe(String maPhieuKiemKe, Timestamp ngayTao, NhanVien nhanVienTao, NhanVien nhanVienDuyet, Timestamp ngayDuyet, String trangThai, String ghiChu) {
        this.maPhieuKiemKe = maPhieuKiemKe;
        this.ngayTao = ngayTao;
        this.nhanVienTao = nhanVienTao;
        this.nhanVienDuyet = nhanVienDuyet;
        this.ngayDuyet = ngayDuyet;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
    }

    // Getters and Setters
    public String getMaPhieuKiemKe() {
        return maPhieuKiemKe;
    }

    public void setMaPhieuKiemKe(String maPhieuKiemKe) {
        this.maPhieuKiemKe = maPhieuKiemKe;
    }

    public Timestamp getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(Timestamp ngayTao) {
        this.ngayTao = ngayTao;
    }

    public NhanVien getNhanVienTao() {
        return nhanVienTao;
    }

    public void setNhanVienTao(NhanVien nhanVienTao) {
        this.nhanVienTao = nhanVienTao;
    }

    public NhanVien getNhanVienDuyet() {
        return nhanVienDuyet;
    }

    public void setNhanVienDuyet(NhanVien nhanVienDuyet) {
        this.nhanVienDuyet = nhanVienDuyet;
    }

    public Timestamp getNgayDuyet() {
        return ngayDuyet;
    }

    public void setNgayDuyet(Timestamp ngayDuyet) {
        this.ngayDuyet = ngayDuyet;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}