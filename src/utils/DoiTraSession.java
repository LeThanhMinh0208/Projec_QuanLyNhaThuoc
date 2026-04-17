package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import entity.HinhThucDoiTra;
import entity.HoaDonView;
import entity.Thuoc;

public class DoiTraSession {
    private static HoaDonView hoaDonDangXuLy;
    private static String lyDoTam = "";
    private static String phiPhatTam = "0";
    private static HinhThucDoiTra hinhThucXuLyTam = HinhThucDoiTra.HOAN_TIEN;
    private static final List<ChiTietDoiTraTamData> dsChiTietTam = new ArrayList<>();
    private static final List<DonViDoiData> dsThuocDoi = new ArrayList<>();
    private static Thuoc thuocDoiDaChon;
    private static DonViDoiData donViDoiDaChon;
    private static boolean dangChonThuocDoi;

    private DoiTraSession() {
    }

    public static HoaDonView getHoaDonDangXuLy() {
        return hoaDonDangXuLy;
    }

    public static void setHoaDonDangXuLy(HoaDonView hoaDon) {
        hoaDonDangXuLy = hoaDon;
    }

    public static String getLyDoTam() {
        return lyDoTam;
    }

    public static void setLyDoTam(String lyDo) {
        lyDoTam = lyDo != null ? lyDo : "";
    }

    public static String getPhiPhatTam() {
        return phiPhatTam;
    }

    public static void setPhiPhatTam(String phiPhat) {
        phiPhatTam = (phiPhat == null || phiPhat.isBlank()) ? "0" : phiPhat.trim();
    }

    public static HinhThucDoiTra getHinhThucXuLyTam() {
        return hinhThucXuLyTam != null ? hinhThucXuLyTam : HinhThucDoiTra.HOAN_TIEN;
    }

    public static void setHinhThucXuLyTam(HinhThucDoiTra hinhThuc) {
        hinhThucXuLyTam = hinhThuc != null ? hinhThuc : HinhThucDoiTra.HOAN_TIEN;
    }

    public static List<ChiTietDoiTraTamData> getDsChiTietTam() {
        return Collections.unmodifiableList(dsChiTietTam);
    }

    public static void setDsChiTietTam(List<ChiTietDoiTraTamData> data) {
        dsChiTietTam.clear();
        if (data != null) {
            dsChiTietTam.addAll(data);
        }
    }

    // --- Danh sách thuốc đổi (thay thế single thuocDoiDaChon + donViDoiDaChon) ---
    public static List<DonViDoiData> getDsThuocDoi() {
        return Collections.unmodifiableList(dsThuocDoi);
    }

    public static void addThuocDoi(DonViDoiData item) {
        if (item != null) {
            dsThuocDoi.add(item);
        }
    }

    public static void removeThuocDoi(DonViDoiData item) {
        dsThuocDoi.remove(item);
    }

    public static void clearThuocDoi() {
        dsThuocDoi.clear();
    }

    public static Thuoc getThuocDoiDaChon() {
        return thuocDoiDaChon;
    }

    public static void setThuocDoiDaChon(Thuoc thuoc) {
        thuocDoiDaChon = thuoc;
    }

    public static DonViDoiData getDonViDoiDaChon() {
        return donViDoiDaChon;
    }

    public static void setDonViDoiDaChon(DonViDoiData donVi) {
        donViDoiDaChon = donVi;
    }

    public static boolean isDangChonThuocDoi() {
        return dangChonThuocDoi;
    }

    public static void setDangChonThuocDoi(boolean dangChon) {
        dangChonThuocDoi = dangChon;
    }

    public static void clearDraft() {
        lyDoTam = "";
        phiPhatTam = "0";
        hinhThucXuLyTam = HinhThucDoiTra.HOAN_TIEN;
        dsChiTietTam.clear();
        dsThuocDoi.clear();
        thuocDoiDaChon = null;
        donViDoiDaChon = null;
        dangChonThuocDoi = false;
    }

    public static void clear() {
        hoaDonDangXuLy = null;
        clearDraft();
    }

    public static class ChiTietDoiTraTamData {
        private final String maQuyDoi;
        private final String maLoThuoc;
        private final String tenThuoc;
        private final String tenDonVi;
        private final int soLuongTra;
        private final double thanhTienHoan;
        private final String tinhTrang;

        public ChiTietDoiTraTamData(String maQuyDoi, String maLoThuoc, String tenThuoc,
                String tenDonVi, int soLuongTra, double thanhTienHoan, String tinhTrang) {
            this.maQuyDoi = maQuyDoi;
            this.maLoThuoc = maLoThuoc;
            this.tenThuoc = tenThuoc;
            this.tenDonVi = tenDonVi;
            this.soLuongTra = soLuongTra;
            this.thanhTienHoan = thanhTienHoan;
            this.tinhTrang = tinhTrang;
        }

        public String getMaQuyDoi() {
            return maQuyDoi;
        }

        public String getMaLoThuoc() {
            return maLoThuoc;
        }

        public String getTenThuoc() {
            return tenThuoc;
        }

        public String getTenDonVi() {
            return tenDonVi;
        }

        public int getSoLuongTra() {
            return soLuongTra;
        }

        public double getThanhTienHoan() {
            return thanhTienHoan;
        }

        public String getTinhTrang() {
            return tinhTrang;
        }
    }

    public static class DonViDoiData {
        private final String maQuyDoi;
        private final String tenDonVi;
        private final int soLuong;
        private final double donGia;
        private final String tenThuoc;

        public DonViDoiData(String maQuyDoi, String tenDonVi, int soLuong, double donGia) {
            this(maQuyDoi, tenDonVi, soLuong, donGia, "");
        }

        public DonViDoiData(String maQuyDoi, String tenDonVi, int soLuong, double donGia, String tenThuoc) {
            this.maQuyDoi = maQuyDoi;
            this.tenDonVi = tenDonVi;
            this.soLuong = soLuong;
            this.donGia = donGia;
            this.tenThuoc = tenThuoc != null ? tenThuoc : "";
        }

        public String getMaQuyDoi() {
            return maQuyDoi;
        }

        public String getTenDonVi() {
            return tenDonVi;
        }

        public int getSoLuong() {
            return soLuong;
        }

        public double getDonGia() {
            return donGia;
        }

        public double getThanhTien() {
            return soLuong * donGia;
        }

        public String getTenThuoc() {
            return tenThuoc;
        }
    }
}
