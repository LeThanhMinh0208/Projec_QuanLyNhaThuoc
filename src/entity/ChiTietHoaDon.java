package entity;

public class ChiTietHoaDon {
    private HoaDon hoaDon;
    private Thuoc thuoc;
    private int soLuong;
    private double donGia;
    // Bổ sung để bám đúng schema ChiTietHoaDon (maQuyDoi, maLoThuoc)
    private String maQuyDoi;
    private String maLoThuoc;
    
    // Field dùng để trừ kho chính xác (theo đơn vị cơ bản)
    private int soLuongTruKho;

    public ChiTietHoaDon() {}

    public ChiTietHoaDon(HoaDon hoaDon, Thuoc thuoc, int soLuong, double donGia) {
        this.hoaDon = hoaDon;
        this.thuoc = thuoc;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    // Getters and Setters
    public HoaDon getHoaDon() { return hoaDon; }
    public void setHoaDon(HoaDon hoaDon) { this.hoaDon = hoaDon; }
    public Thuoc getThuoc() { return thuoc; }
    public void setThuoc(Thuoc thuoc) { this.thuoc = thuoc; }
    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }

    public String getMaQuyDoi() { return maQuyDoi; }
    public void setMaQuyDoi(String maQuyDoi) { this.maQuyDoi = maQuyDoi; }
    public String getMaLoThuoc() { return maLoThuoc; }
    public void setMaLoThuoc(String maLoThuoc) { this.maLoThuoc = maLoThuoc; }

    public int getSoLuongTruKho() { return soLuongTruKho; }
    public void setSoLuongTruKho(int soLuongTruKho) { this.soLuongTruKho = soLuongTruKho; }
}