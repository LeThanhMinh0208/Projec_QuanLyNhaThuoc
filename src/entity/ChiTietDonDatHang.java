package entity;

public class ChiTietDonDatHang {
    private DonDatHang donDatHang;
    private Thuoc thuoc;
    private DonViQuyDoi donViQuyDoi;

    private int soLuongDat;
    private int soLuongDaNhan;
    private double donGiaDuKien;

    // Các biến tạm để form Nhập Kho lưu thông tin Lô/Date
    private String maLo;
    private String ngaySanXuatTemp;
    private String hanSuDung;

    public ChiTietDonDatHang() {}

    public double getThanhTien() {
        return soLuongDat * donGiaDuKien;
    }

    // =========================================================================
    // GETTER & SETTER
    // =========================================================================
    public DonDatHang getDonDatHang() { return donDatHang; }
    public void setDonDatHang(DonDatHang donDatHang) { this.donDatHang = donDatHang; }
    public Thuoc getThuoc() { return thuoc; }
    public void setThuoc(Thuoc thuoc) { this.thuoc = thuoc; }
    public DonViQuyDoi getDonViQuyDoi() { return donViQuyDoi; }
    public void setDonViQuyDoi(DonViQuyDoi donViQuyDoi) { this.donViQuyDoi = donViQuyDoi; }
    public int getSoLuongDat() { return soLuongDat; }
    public void setSoLuongDat(int soLuongDat) { this.soLuongDat = soLuongDat; }
    public int getSoLuongDaNhan() { return soLuongDaNhan; }
    public void setSoLuongDaNhan(int soLuongDaNhan) { this.soLuongDaNhan = soLuongDaNhan; }
    public double getDonGiaDuKien() { return donGiaDuKien; }
    public void setDonGiaDuKien(double donGiaDuKien) { this.donGiaDuKien = donGiaDuKien; }
    public String getMaLo() { return maLo; }
    public void setMaLo(String maLo) { this.maLo = maLo; }
    public String getNgaySanXuatTemp() { return ngaySanXuatTemp; }
    public void setNgaySanXuatTemp(String ngaySanXuatTemp) { this.ngaySanXuatTemp = ngaySanXuatTemp; }
    public String getHanSuDung() { return hanSuDung; }
    public void setHanSuDung(String hanSuDung) { this.hanSuDung = hanSuDung; }
}