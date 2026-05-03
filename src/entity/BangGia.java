package entity;

import java.time.LocalDate;

public class BangGia {
    private String maBangGia;
    private String tenBangGia;
    private String loaiBangGia;   // 'DEFAULT' hoặc 'PROMO'
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc; // null = chưa kết thúc
    private String moTa;
    private boolean trangThai;     // true = đang hoạt động
    private int soLuongThuoc;      // computed từ COUNT DISTINCT mà thuốc, không lưu DB
    private int soDonVi;           // computed từ COUNT mã quy đổi, không lưu DB

    public BangGia() {}

    public BangGia(String maBangGia, String tenBangGia, String loaiBangGia,
                   LocalDate ngayBatDau, LocalDate ngayKetThuc, String moTa,
                   boolean trangThai, int soLuongThuoc) {
        this.maBangGia = maBangGia;
        this.tenBangGia = tenBangGia;
        this.loaiBangGia = loaiBangGia;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.moTa = moTa;
        this.trangThai = trangThai;
        this.soLuongThuoc = soLuongThuoc;
    }

    // --- Getters & Setters ---
    public String getMaBangGia() { return maBangGia; }
    public void setMaBangGia(String maBangGia) { this.maBangGia = maBangGia; }

    public String getTenBangGia() { return tenBangGia; }
    public void setTenBangGia(String tenBangGia) { this.tenBangGia = tenBangGia; }

    public String getLoaiBangGia() { return loaiBangGia; }
    public void setLoaiBangGia(String loaiBangGia) { this.loaiBangGia = loaiBangGia; }

    public LocalDate getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(LocalDate ngayBatDau) { this.ngayBatDau = ngayBatDau; }

    public LocalDate getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(LocalDate ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }

    public int getSoLuongThuoc() { return soLuongThuoc; }
    public void setSoLuongThuoc(int soLuongThuoc) { this.soLuongThuoc = soLuongThuoc; }

    public int getSoDonVi() { return soDonVi; }
    public void setSoDonVi(int soDonVi) { this.soDonVi = soDonVi; }

    /**
     * Tính trạng thái hiển thị:
     * - "Đang hiệu lực": trangThai=1 AND ngayBatDau <= hôm nay AND (ngayKetThuc NULL OR ngayKetThuc >= hôm nay)
     * - "Chưa hiệu lực": ngayBatDau > hôm nay
     * - "Đã kết thúc": trangThai=0 OR ngayKetThuc < hôm nay
     */
    public String getTrangThaiHienThi() {
        LocalDate now = LocalDate.now();
        if (!trangThai) {
			return "Đã kết thúc";
		}
        if (ngayBatDau.isAfter(now)) {
			return "Chưa hiệu lực";
		}
        if (ngayKetThuc != null && ngayKetThuc.isBefore(now)) {
			return "Đã kết thúc";
		}
        return "Đang hiệu lực";
    }

    @Override
    public String toString() {
        return maBangGia + " - " + tenBangGia;
    }
}
