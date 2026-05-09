package entity;

import java.sql.Timestamp;

public class ChiTietPhieuKiemKe {
    private String maPhieuKiemKe;
    private String maLoThuoc;
    private int tonKhoSnapshot;
    private int soLuongKiemTra;
    private int chenhLech;
    private String lyDoLech;
    
    // 2 TRƯỜNG MỚI BỔ SUNG:
    private String ghiChu; 
    private Timestamp thoiDiemDem; 
    
    private String trangThaiChiTiet;

    // Constructors
    public ChiTietPhieuKiemKe() {
    }

    public ChiTietPhieuKiemKe(String maPhieuKiemKe, String maLoThuoc, int tonKhoSnapshot, int soLuongKiemTra, 
                              int chenhLech, String lyDoLech, String ghiChu, Timestamp thoiDiemDem, String trangThaiChiTiet) {
        this.maPhieuKiemKe = maPhieuKiemKe;
        this.maLoThuoc = maLoThuoc;
        this.tonKhoSnapshot = tonKhoSnapshot;
        this.soLuongKiemTra = soLuongKiemTra;
        this.chenhLech = chenhLech;
        this.lyDoLech = lyDoLech;
        this.ghiChu = ghiChu;
        this.thoiDiemDem = thoiDiemDem;
        this.trangThaiChiTiet = trangThaiChiTiet;
    }

    // Getters and Setters
    public String getMaPhieuKiemKe() {
        return maPhieuKiemKe;
    }

    public void setMaPhieuKiemKe(String maPhieuKiemKe) {
        this.maPhieuKiemKe = maPhieuKiemKe;
    }

    public String getMaLoThuoc() {
        return maLoThuoc;
    }

    public void setMaLoThuoc(String maLoThuoc) {
        this.maLoThuoc = maLoThuoc;
    }

    public int getTonKhoSnapshot() {
        return tonKhoSnapshot;
    }

    public void setTonKhoSnapshot(int tonKhoSnapshot) {
        this.tonKhoSnapshot = tonKhoSnapshot;
    }

    public int getSoLuongKiemTra() {
        return soLuongKiemTra;
    }

    public void setSoLuongKiemTra(int soLuongKiemTra) {
        this.soLuongKiemTra = soLuongKiemTra;
    }

    public int getChenhLech() {
        return chenhLech;
    }

    public void setChenhLech(int chenhLech) {
        this.chenhLech = chenhLech;
    }

    public String getLyDoLech() {
        return lyDoLech;
    }

    public void setLyDoLech(String lyDoLech) {
        this.lyDoLech = lyDoLech;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public Timestamp getThoiDiemDem() {
        return thoiDiemDem;
    }

    public void setThoiDiemDem(Timestamp thoiDiemDem) {
        this.thoiDiemDem = thoiDiemDem;
    }

    public String getTrangThaiChiTiet() {
        return trangThaiChiTiet;
    }

    public void setTrangThaiChiTiet(String trangThaiChiTiet) {
        this.trangThaiChiTiet = trangThaiChiTiet;
    }
}