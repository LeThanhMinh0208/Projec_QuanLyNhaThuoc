package entity;

public class ChiTietDoiTra {
    private String maPhieuDoiTra;
    private String maQuyDoi;
    private String maLoThuoc;
    private int soLuong;
    private String tinhTrang;

    public ChiTietDoiTra() {
    }

    public ChiTietDoiTra(String maPhieuDoiTra, String maQuyDoi, String maLoThuoc, int soLuong, String tinhTrang) {
        this.maPhieuDoiTra = maPhieuDoiTra;
        this.maQuyDoi = maQuyDoi;
        this.maLoThuoc = maLoThuoc;
        this.soLuong = soLuong;
        this.tinhTrang = tinhTrang;
    }

    public String getMaPhieuDoiTra() {
        return maPhieuDoiTra;
    }

    public void setMaPhieuDoiTra(String maPhieuDoiTra) {
        this.maPhieuDoiTra = maPhieuDoiTra;
    }

    public String getMaQuyDoi() {
        return maQuyDoi;
    }

    public void setMaQuyDoi(String maQuyDoi) {
        this.maQuyDoi = maQuyDoi;
    }

    public String getMaLoThuoc() {
        return maLoThuoc;
    }

    public void setMaLoThuoc(String maLoThuoc) {
        this.maLoThuoc = maLoThuoc;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public String getTinhTrang() {
        return tinhTrang;
    }

    public void setTinhTrang(String tinhTrang) {
        this.tinhTrang = tinhTrang;
    }
}
