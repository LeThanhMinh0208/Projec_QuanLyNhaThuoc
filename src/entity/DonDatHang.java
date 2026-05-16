package entity;

import java.sql.Date;

public class DonDatHang {
    private String maDonDatHang;
    private NhaCungCap nhaCungCap;
    private NhanVien nhanVien;
    private Date ngayDat;
    private Date ngayGiaoDuKien;
    private double tongTienDuTinh;
    private String trangThai;
    private String ghiChu;

    public DonDatHang() {}

    // =========================================================================
    // LOGIC 4 CỘT GIAO DIỆN CHUẨN UX CỦA SẾP
    // =========================================================================

    // 1. Tình Trạng Hàng
    public String getTrangThaiHang() {
        if (trangThai == null) {
			return "Chờ Giao";
		}
        switch (trangThai) {
            case "GIAO_DU": return "Giao Đủ";
            case "GIAO_MOT_PHAN":
            case "DONG_DON_THIEU": return "Giao Một Phần";
            case "DA_HUY": return "Hủy Đơn";
            case "CHO_GIAO":
            default: return "Chờ Giao";
        }
    }

    // 2. Trạng Thái Nhập Kho
    public String getTrangThaiNhap() {
        if (trangThai == null) {
			return "Chưa Nhập Kho";
		}
        switch (trangThai) {
            case "GIAO_DU":
            case "GIAO_MOT_PHAN":
            case "DONG_DON_THIEU": return "Đã Nhập Kho";
            case "CHO_GIAO":
            case "DA_HUY":
            default: return "Chưa Nhập Kho";
        }
    }

    // 3. Tiến Độ Đơn
    public String getTrangThaiHoanThanh() {
        if (trangThai == null) {
			return "Đang Xử Lý";
		}
        switch (trangThai) {
            case "GIAO_DU":
            case "DONG_DON_THIEU":
            case "DA_HUY": return "Hoàn Thành";
            case "CHO_GIAO":
            case "GIAO_MOT_PHAN":
            default: return "Đang Xử Lý";
        }
    }

    // 4. Trạng Thái Nút Bấm Nhập Kho (ON/OFF)
    public boolean isChoPhepNhapKho() {
        // Nút chỉ sáng khi đơn đang chờ xử lý
        return getTrangThaiHoanThanh().equals("Đang Xử Lý");
    }

    // =========================================================================
    // GETTER & SETTER
    // =========================================================================
    public String getMaDonDatHang() { return maDonDatHang; }
    public void setMaDonDatHang(String maDonDatHang) { this.maDonDatHang = maDonDatHang; }
    public NhaCungCap getNhaCungCap() { return nhaCungCap; }
    public void setNhaCungCap(NhaCungCap nhaCungCap) { this.nhaCungCap = nhaCungCap; }
    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }
    public Date getNgayDat() { return ngayDat; }
    public void setNgayDat(Date ngayDat) { this.ngayDat = ngayDat; }
    public Date getNgayGiaoDuKien() { return ngayGiaoDuKien; }
    public void setNgayGiaoDuKien(Date ngayGiaoDuKien) { this.ngayGiaoDuKien = ngayGiaoDuKien; }
    public double getTongTienDuTinh() { return tongTienDuTinh; }
    public void setTongTienDuTinh(double tongTienDuTinh) { this.tongTienDuTinh = tongTienDuTinh; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    @Override public String toString() { return maDonDatHang; }
}