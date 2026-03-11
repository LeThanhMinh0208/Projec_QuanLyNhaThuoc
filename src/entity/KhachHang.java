package entity;

import java.util.Objects;

public class KhachHang {

    private String maKhachHang;
    private String hoTen;
    private String sdt;
    private String diaChi;
    private int diemTichLuy;

    // Constructor mặc định
    public KhachHang() {
        this.diemTichLuy = 0;
    }

    // Constructor đầy đủ
    public KhachHang(String maKH, String hoTen, String sdt, String diaChi, int diem) {
        setMaKhachHang(maKH);
        setHoTen(hoTen);
        setSdt(sdt);
        setDiaChi(diaChi);
        setDiemTichLuy(diem);
    }

    // Copy constructor
    public KhachHang(KhachHang khKhac) {
        this.maKhachHang = khKhac.maKhachHang;
        this.hoTen = khKhac.hoTen;
        this.sdt = khKhac.sdt;
        this.diaChi = khKhac.diaChi;
        this.diemTichLuy = khKhac.diemTichLuy;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKH) {
        if (maKH == null || !maKH.matches("^KH\\d{3}$")) {
            throw new IllegalArgumentException("Mã khách hàng phải theo dạng KH001");
        }
        this.maKhachHang = maKH;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String HT) {
        if (HT == null || HT.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên không được để trống");
        }
        this.hoTen = HT;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String SDT) {
        if (SDT == null || !SDT.matches("^0\\d{9}$")) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ");
        }
        this.sdt = SDT;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public int getDiemTichLuy() {
        return diemTichLuy;
    }

    public void setDiemTichLuy(int diem) {
        if (diem < 0) {
            throw new IllegalArgumentException("Điểm tích lũy không được âm");
        }
        this.diemTichLuy = diem;
    }

    // Cộng điểm sau mỗi hóa đơn
    public void capNhatDiem(int diemMoi) {
        if (diemMoi < 0) {
            throw new IllegalArgumentException("Điểm cộng thêm không hợp lệ");
        }
        this.diemTichLuy += diemMoi;
    }

    @Override
    public String toString() {
        return "KhachHang{" +
                "maKhachHang='" + maKhachHang + '\'' +
                ", hoTen='" + hoTen + '\'' +
                ", sdt='" + sdt + '\'' +
                ", diaChi='" + diaChi + '\'' +
                ", diemTichLuy=" + diemTichLuy +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(maKhachHang);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof KhachHang)) return false;
        KhachHang other = (KhachHang) obj;
        return Objects.equals(maKhachHang, other.maKhachHang);
    }
}
