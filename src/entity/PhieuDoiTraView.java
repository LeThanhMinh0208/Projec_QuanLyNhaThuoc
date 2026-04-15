package entity;

import java.sql.Timestamp;

public class PhieuDoiTraView {
    private String maPhieuDoiTra;
    private Timestamp ngayDoiTra;
    private String maHoaDon;
    private String tenKhachHang;
    private String tenNhanVien;
    private String hinhThucXuLy;
    private double phiPhat;
    private String lyDo;

    public String getMaPhieuDoiTra() {
        return maPhieuDoiTra;
    }

    public void setMaPhieuDoiTra(String maPhieuDoiTra) {
        this.maPhieuDoiTra = maPhieuDoiTra;
    }

    public Timestamp getNgayDoiTra() {
        return ngayDoiTra;
    }

    public void setNgayDoiTra(Timestamp ngayDoiTra) {
        this.ngayDoiTra = ngayDoiTra;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public String getTenKhachHang() {
<<<<<<< HEAD
        return tenKhachHang != null ? tenKhachHang : "Khach le";
=======
        return tenKhachHang != null ? tenKhachHang : "Khách lẻ";
>>>>>>> 372975594d8f1063277fa68b18264d82aa24f969
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getTenNhanVien() {
        return tenNhanVien;
    }

    public void setTenNhanVien(String tenNhanVien) {
        this.tenNhanVien = tenNhanVien;
    }

    public String getHinhThucXuLy() {
        return hinhThucXuLy;
    }

    public void setHinhThucXuLy(String hinhThucXuLy) {
        this.hinhThucXuLy = hinhThucXuLy;
    }

    public double getPhiPhat() {
        return phiPhat;
    }

    public void setPhiPhat(double phiPhat) {
        this.phiPhat = phiPhat;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }

    public String getHinhThucXuLyLabel() {
        if ("DOI_SAN_PHAM".equals(hinhThucXuLy)) {
<<<<<<< HEAD
            return "Doi san pham";
        }
        return "Hoan tien";
=======
            return "Đổi sản phẩm";
        }
        return "Hoàn tiền";
>>>>>>> 372975594d8f1063277fa68b18264d82aa24f969
    }
}
