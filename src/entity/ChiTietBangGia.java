package entity;

import java.math.BigDecimal;

public class ChiTietBangGia {
    private String maBangGia;
    private String maQuyDoi;
    private BigDecimal donGiaBan;
    // Các field join để hiển thị (không lưu DB)
    private String tenThuoc;
    private String tenDonVi;
    private String maThuoc;
    private int tyLeQuyDoi; // display only — join từ DonViQuyDoi

    public ChiTietBangGia() {}

    public ChiTietBangGia(String maBangGia, String maQuyDoi, BigDecimal donGiaBan) {
        this.maBangGia = maBangGia;
        this.maQuyDoi = maQuyDoi;
        this.donGiaBan = donGiaBan;
    }

    public ChiTietBangGia(String maBangGia, String maQuyDoi, BigDecimal donGiaBan,
                           String maThuoc, String tenThuoc, String tenDonVi) {
        this.maBangGia = maBangGia;
        this.maQuyDoi = maQuyDoi;
        this.donGiaBan = donGiaBan;
        this.maThuoc = maThuoc;
        this.tenThuoc = tenThuoc;
        this.tenDonVi = tenDonVi;
    }

    // --- Getters & Setters ---
    public String getMaBangGia() { return maBangGia; }
    public void setMaBangGia(String maBangGia) { this.maBangGia = maBangGia; }

    public String getMaQuyDoi() { return maQuyDoi; }
    public void setMaQuyDoi(String maQuyDoi) { this.maQuyDoi = maQuyDoi; }

    public BigDecimal getDonGiaBan() { return donGiaBan; }
    public void setDonGiaBan(BigDecimal donGiaBan) { this.donGiaBan = donGiaBan; }

    public String getTenThuoc() { return tenThuoc; }
    public void setTenThuoc(String tenThuoc) { this.tenThuoc = tenThuoc; }

    public String getTenDonVi() { return tenDonVi; }
    public void setTenDonVi(String tenDonVi) { this.tenDonVi = tenDonVi; }

    public String getMaThuoc() { return maThuoc; }
    public void setMaThuoc(String maThuoc) { this.maThuoc = maThuoc; }

    public int getTyLeQuyDoi() { return tyLeQuyDoi; }
    public void setTyLeQuyDoi(int tyLeQuyDoi) { this.tyLeQuyDoi = tyLeQuyDoi; }
}
