package entity;

import java.sql.Date;

public class LoThuoc {
    private String maLoThuoc;
    private Date ngaySanXuat;
    private Date hanSuDung;
    private int soLuongTon;
    private double giaNhap;
    private String viTriKho; 
    private Thuoc thuoc;

    public LoThuoc() {}

    public LoThuoc(String maLoThuoc, Date ngaySanXuat, Date hanSuDung, int soLuongTon, double giaNhap, String viTriKho, Thuoc thuoc) {
        this.maLoThuoc = maLoThuoc;
        this.ngaySanXuat = ngaySanXuat;
        this.hanSuDung = hanSuDung;
        this.soLuongTon = soLuongTon;
        this.giaNhap = giaNhap;
        this.viTriKho = viTriKho;
        this.thuoc = thuoc;
    }

    // Getters and Setters
    public String getMaLoThuoc() { return maLoThuoc; }
    public void setMaLoThuoc(String maLoThuoc) { this.maLoThuoc = maLoThuoc; }

    public Date getNgaySanXuat() { return ngaySanXuat; }
    public void setNgaySanXuat(Date ngaySanXuat) { this.ngaySanXuat = ngaySanXuat; }

    public Date getHanSuDung() { return hanSuDung; }
    public void setHanSuDung(Date hanSuDung) { this.hanSuDung = hanSuDung; }

    public int getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(int soLuongTon) { this.soLuongTon = soLuongTon; }

    public double getGiaNhap() { return giaNhap; }
    public void setGiaNhap(double giaNhap) { this.giaNhap = giaNhap; }

    public String getViTriKho() { return viTriKho; }
    public void setViTriKho(String viTriKho) { this.viTriKho = viTriKho; }

    public Thuoc getThuoc() { return thuoc; }
    public void setThuoc(Thuoc thuoc) { this.thuoc = thuoc; }
}