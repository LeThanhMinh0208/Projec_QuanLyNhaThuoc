package entity;

public class KhachHang {
    private String maKhachHang;
    private String hoTen;
    private String sdt;
    private String diaChi;
    private int diemTichLuy;
    private boolean trangThai; // true = hoạt động, false = đã vô hiệu

    public KhachHang() {
        this.trangThai = true;
    }

    public KhachHang(String maKhachHang, String hoTen, String sdt, String diaChi, int diemTichLuy) {
        this.maKhachHang = maKhachHang;
        this.hoTen = hoTen;
        this.sdt = sdt;
        this.diaChi = diaChi;
        this.diemTichLuy = diemTichLuy;
        this.trangThai = true;
    }

    public KhachHang(String maKhachHang, String hoTen, String sdt, String diaChi, int diemTichLuy, boolean trangThai) {
        this.maKhachHang = maKhachHang;
        this.hoTen = hoTen;
        this.sdt = sdt;
        this.diaChi = diaChi;
        this.diemTichLuy = diemTichLuy;
        this.trangThai = trangThai;
    }

    // Getters and Setters
    public String getMaKhachHang() { return maKhachHang; }
    public void setMaKhachHang(String maKhachHang) { this.maKhachHang = maKhachHang; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    public int getDiemTichLuy() { return diemTichLuy; }
    public void setDiemTichLuy(int diemTichLuy) { this.diemTichLuy = diemTichLuy; }
    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }
}