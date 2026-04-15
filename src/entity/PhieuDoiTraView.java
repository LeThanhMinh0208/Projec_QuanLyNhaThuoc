package entity;

import java.sql.Timestamp;

public class PhieuDoiTraView {
    private static final String THUOC_TRA_PREFIX = " | Thuốc trả: ";
    private static final String THUOC_DOI_PREFIX = " | Thuốc đổi: ";
    private static final String KET_QUA_DOI_PREFIX = " | Kết quả đổi: ";
    private String maPhieuDoiTra;
    private Timestamp ngayDoiTra;
    private String maHoaDon;
    private String tenKhachHang;
    private String tenNhanVien;
    private String hinhThucXuLy;
    private double phiPhat;
    private String lyDo;

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

    public String getHinhThucXuLyLabel() {
        if ("DOI_SAN_PHAM".equals(hinhThucXuLy)) {
            return "Đổi sản phẩm";
        }
        return "Hoàn tiền";
    }

    public boolean isDoiSanPham() {
        return "DOI_SAN_PHAM".equals(hinhThucXuLy);
    }

    public double getSoTienChenhLech() {
        return Math.abs(phiPhat);
    }

    public String getMoTaChenhLechDoiSanPham() {
        if (!isDoiSanPham()) {
            return String.format("%,.0f VND", phiPhat);
        }

        String ketQuaDoi = getKetQuaDoiSanPham();
        if ("BU_TIEN".equals(ketQuaDoi)) {
            return "Bù thêm: " + String.format("%,.0f VND", Math.abs(phiPhat));
        }
        if ("HOAN_TIEN".equals(ketQuaDoi)) {
            return "Hoàn lại: " + String.format("%,.0f VND", Math.abs(phiPhat));
        }
        if ("KHONG_CHENH_LECH".equals(ketQuaDoi)) {
            return "Không chênh lệch";
        }

        if (phiPhat > 0) {
            return "Bù thêm: " + String.format("%,.0f VND", phiPhat);
        }
        if (phiPhat < 0) {
            return "Hoàn lại: " + String.format("%,.0f VND", Math.abs(phiPhat));
        }
        return "Không chênh lệch";
    }

    public String getLyDoHienThi() {
        if (lyDo == null || lyDo.isBlank()) {
            return "";
        }

        int idxThuocTra = lyDo.indexOf(THUOC_TRA_PREFIX);
        int idxThuocDoi = lyDo.indexOf(THUOC_DOI_PREFIX);
        int idxKetQua = lyDo.indexOf(KET_QUA_DOI_PREFIX);
        int idx = timViTriMetadataDauTien(idxThuocTra, idxThuocDoi, idxKetQua);
        if (idx >= 0) {
            return lyDo.substring(0, idx).trim();
        }
        return lyDo;
    }

    public String getThongTinThuocTra() {
        return extractMetadataValue(THUOC_TRA_PREFIX, THUOC_DOI_PREFIX, KET_QUA_DOI_PREFIX);
    }

    public String getThongTinThuocDoi() {
        return extractMetadataValue(THUOC_DOI_PREFIX, KET_QUA_DOI_PREFIX);
    }

    public String getKetQuaDoiSanPham() {
        if (lyDo == null || lyDo.isBlank()) {
            return "";
        }

        int idx = lyDo.indexOf(KET_QUA_DOI_PREFIX);
        if (idx >= 0) {
            return lyDo.substring(idx + KET_QUA_DOI_PREFIX.length()).trim();
        }
        return "";
    }

    private int timViTriMetadataDauTien(int... indexes) {
        int min = -1;
        for (int idx : indexes) {
            if (idx >= 0 && (min < 0 || idx < min)) {
                min = idx;
            }
        }
        return min;
    }

    private String extractMetadataValue(String prefix, String... nextPrefixes) {
        if (lyDo == null || lyDo.isBlank()) {
            return "";
        }

        int idx = lyDo.indexOf(prefix);
        if (idx < 0) {
            return "";
        }

        int start = idx + prefix.length();
        int end = -1;
        for (String nextPrefix : nextPrefixes) {
            int nextIdx = lyDo.indexOf(nextPrefix, start);
            if (nextIdx >= 0 && (end < 0 || nextIdx < end)) {
                end = nextIdx;
            }
        }

        return (end >= 0 ? lyDo.substring(start, end) : lyDo.substring(start)).trim();
    }
}
