package entity;

import java.time.LocalDateTime;

public class NhatKyHoatDong {
    private int maLog;
    private String maNhanVien;
    private String tenNhanVien; // JOIN từ bảng NhanVien
    private String hanhDong;
    private String doiTuong;
    private String maDoiTuong;
    private String moTa;
    private LocalDateTime thoiGian;

    public NhatKyHoatDong() {}

    public NhatKyHoatDong(int maLog, String maNhanVien, String tenNhanVien, String hanhDong,
                           String doiTuong, String maDoiTuong, String moTa, LocalDateTime thoiGian) {
        this.maLog = maLog;
        this.maNhanVien = maNhanVien;
        this.tenNhanVien = tenNhanVien;
        this.hanhDong = hanhDong;
        this.doiTuong = doiTuong;
        this.maDoiTuong = maDoiTuong;
        this.moTa = moTa;
        this.thoiGian = thoiGian;
    }

    public int getMaLog() { return maLog; }
    public void setMaLog(int maLog) { this.maLog = maLog; }

    public String getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(String maNhanVien) { this.maNhanVien = maNhanVien; }

    public String getTenNhanVien() { return tenNhanVien; }
    public void setTenNhanVien(String tenNhanVien) { this.tenNhanVien = tenNhanVien; }

    public String getHanhDong() { return hanhDong; }
    public void setHanhDong(String hanhDong) { this.hanhDong = hanhDong; }

    public String getDoiTuong() { return doiTuong; }
    public void setDoiTuong(String doiTuong) { this.doiTuong = doiTuong; }

    public String getMaDoiTuong() { return maDoiTuong; }
    public void setMaDoiTuong(String maDoiTuong) { this.maDoiTuong = maDoiTuong; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public LocalDateTime getThoiGian() { return thoiGian; }
    public void setThoiGian(LocalDateTime thoiGian) { this.thoiGian = thoiGian; }
}
