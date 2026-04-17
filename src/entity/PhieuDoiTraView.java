package entity;

import java.sql.Timestamp;

public class PhieuDoiTraView {
    private String maPhieuDoiTra;
    private Timestamp ngayDoiTra;
    private String maHoaDon;
    private String tenKhachHang;
    private String tenNhanVien;
    private String hinhThucXuLy;
    private double phiPhat;
    private String lyDo;
    private String ketQuaDoiSanPham;
    private String danhSachThuocDoi;

    public String getMaPhieuDoiTra() {
        return maPhieuDoiTra;
    }

    public void setMaPhieuDoiTra(String maPhieuDoiTra) {
        this.maPhieuDoiTra = maPhieuDoiTra;
    }

    public Timestamp getNgayDoiTra() {
        return ngayDoiTra;
    }

    public void setNgayDoiTra(Timestamp ngayDoiTra) {
        this.ngayDoiTra = ngayDoiTra;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public String getTenKhachHang() {
        return tenKhachHang != null ? tenKhachHang : "Khách lẻ";
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getTenNhanVien() {
        return tenNhanVien;
    }

    public void setTenNhanVien(String tenNhanVien) {
        this.tenNhanVien = tenNhanVien;
    }

    public String getHinhThucXuLy() {
        return hinhThucXuLy;
    }

    public void setHinhThucXuLy(String hinhThucXuLy) {
        this.hinhThucXuLy = hinhThucXuLy;
    }

    public double getPhiPhat() {
        return phiPhat;
    }

    public void setPhiPhat(double phiPhat) {
        this.phiPhat = phiPhat;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }

    public String getKetQuaDoiSanPham() {
        return ketQuaDoiSanPham;
    }

    public void setKetQuaDoiSanPham(String ketQuaDoiSanPham) {
        this.ketQuaDoiSanPham = ketQuaDoiSanPham;
    }

    public String getDanhSachThuocDoi() {
        return danhSachThuocDoi;
    }

    public void setDanhSachThuocDoi(String danhSachThuocDoi) {
        this.danhSachThuocDoi = danhSachThuocDoi;
    }

    public String getHinhThucXuLyLabel() {
        return isDoiSanPham() ? "Đổi sản phẩm" : "Hoàn tiền";
    }

    public boolean isDoiSanPham() {
        return "DOI_SAN_PHAM".equals(hinhThucXuLy);
    }

    public String getMoTaChenhLechDoiSanPham() {
        if (!isDoiSanPham()) {
            return String.format("%,.0f VND", phiPhat);
        }
        if ("BU_TIEN".equals(ketQuaDoiSanPham)) {
            return "Khách bù: " + String.format("%,.0f VND", phiPhat);
        }
        if ("HOAN_TIEN".equals(ketQuaDoiSanPham)) {
            return "Hoàn lại: " + String.format("%,.0f VND", phiPhat);
        }
        return "Không chênh lệch";
    }

    public String getLyDoHienThi() {
        return extractSegment("ly do");
    }

    public String getThongTinThuocTra() {
        return extractSegment("thuoc tra");
    }

    public String getThongTinThuocDoi() {
        return extractSegment("thuoc doi");
    }

    private String extractSegment(String label) {
        if (lyDo == null || lyDo.isBlank()) {
            return "";
        }

        for (String part : lyDo.split("\\|")) {
            String trimmed = part.trim();
            int idx = trimmed.indexOf(':');
            if (idx < 0) {
                if ("ly do".equals(label)) {
                    return trimmed;
                }
                continue;
            }

            String key = normalize(trimmed.substring(0, idx).trim());
            if (key.equals(label)) {
                return trimmed.substring(idx + 1).trim();
            }
        }

        return "ly do".equals(label) ? lyDo.trim() : "";
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase()
                .replace("ý", "y")
                .replace("đ", "d");
    }
}
