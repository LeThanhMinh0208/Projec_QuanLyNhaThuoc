package entity;

public class ChiTietPhieuNhap {
    private PhieuNhap phieuNhap;
    private Thuoc thuoc;
    private int soLuong;
    private double donGiaNhap;

    public ChiTietPhieuNhap() {}

    public ChiTietPhieuNhap(PhieuNhap phieuNhap, Thuoc thuoc, int soLuong, double donGiaNhap) {
        this.phieuNhap = phieuNhap;
        this.thuoc = thuoc;
        this.soLuong = soLuong;
        this.donGiaNhap = donGiaNhap;
    }

    // Getters and Setters
    public PhieuNhap getPhieuNhap() { return phieuNhap; }
    public void setPhieuNhap(PhieuNhap phieuNhap) { this.phieuNhap = phieuNhap; }
    public Thuoc getThuoc() { return thuoc; }
    public void setThuoc(Thuoc thuoc) { this.thuoc = thuoc; }
    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
    public double getDonGiaNhap() { return donGiaNhap; }
    public void setDonGiaNhap(double donGiaNhap) { this.donGiaNhap = donGiaNhap; }
}