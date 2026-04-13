package entity;

import java.sql.Date;

public class ChiTietDoiTraView {
    private String maQuyDoi;
    private String maLoThuoc;
    private String tenThuoc;
    private String tenDonVi;
    private Date hanSuDung;
    private int soLuongDaMua;
    private int soLuongDaTra;
    private double donGia;

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

    public String getTenThuoc() {
        return tenThuoc;
    }

    public void setTenThuoc(String tenThuoc) {
        this.tenThuoc = tenThuoc;
    }

    public String getTenDonVi() {
        return tenDonVi;
    }

    public void setTenDonVi(String tenDonVi) {
        this.tenDonVi = tenDonVi;
    }

    public Date getHanSuDung() {
        return hanSuDung;
    }

    public void setHanSuDung(Date hanSuDung) {
        this.hanSuDung = hanSuDung;
    }

    public int getSoLuongDaMua() {
        return soLuongDaMua;
    }

    public void setSoLuongDaMua(int soLuongDaMua) {
        this.soLuongDaMua = soLuongDaMua;
    }

    public int getSoLuongDaTra() {
        return soLuongDaTra;
    }

    public void setSoLuongDaTra(int soLuongDaTra) {
        this.soLuongDaTra = soLuongDaTra;
    }

    public double getDonGia() {
        return donGia;
    }

    public void setDonGia(double donGia) {
        this.donGia = donGia;
    }

    public int getSoLuongConLai() {
        return Math.max(0, soLuongDaMua - soLuongDaTra);
    }
}
