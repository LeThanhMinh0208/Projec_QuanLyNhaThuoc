package utils;

import java.util.ArrayList;
import java.util.List;

import entity.DonViQuyDoi;
import entity.HinhThucDoiTra;
import entity.HoaDonView;
import entity.Thuoc;

public class DoiTraSession {
    private static HoaDonView hoaDonDangXuLy;
    private static boolean dangChonThuocDoi;
    private static String lyDoTam = "";
    private static String phiPhatTam = "0";
    private static HinhThucDoiTra hinhThucXuLyTam = HinhThucDoiTra.HOAN_TIEN;
    private static Thuoc thuocDoiDaChon;
    private static DonViDoiTamData donViDoiDaChon;
    private static final List<ChiTietDoiTraTamData> dsChiTietTam = new ArrayList<>();

    public static HoaDonView getHoaDonDangXuLy() {
        return hoaDonDangXuLy;
    }

    public static void setHoaDonDangXuLy(HoaDonView hoaDon) {
        hoaDonDangXuLy = hoaDon;
    }

    public static boolean isDangChonThuocDoi() {
        return dangChonThuocDoi;
    }

    public static void setDangChonThuocDoi(boolean dangChonThuocDoi) {
        DoiTraSession.dangChonThuocDoi = dangChonThuocDoi;
    }

    public static String getLyDoTam() {
        return lyDoTam;
    }

    public static void setLyDoTam(String lyDoTam) {
        DoiTraSession.lyDoTam = lyDoTam != null ? lyDoTam : "";
    }

    public static String getPhiPhatTam() {
        return phiPhatTam;
    }

    public static void setPhiPhatTam(String phiPhatTam) {
        DoiTraSession.phiPhatTam = (phiPhatTam == null || phiPhatTam.trim().isEmpty()) ? "0" : phiPhatTam.trim();
    }

    public static HinhThucDoiTra getHinhThucXuLyTam() {
        return hinhThucXuLyTam;
    }

    public static void setHinhThucXuLyTam(HinhThucDoiTra hinhThucXuLyTam) {
        DoiTraSession.hinhThucXuLyTam = hinhThucXuLyTam != null ? hinhThucXuLyTam : HinhThucDoiTra.HOAN_TIEN;
    }

    public static Thuoc getThuocDoiDaChon() {
        return thuocDoiDaChon;
    }

    public static void setThuocDoiDaChon(Thuoc thuocDoiDaChon) {
        DoiTraSession.thuocDoiDaChon = thuocDoiDaChon;
    }

    public static DonViDoiTamData getDonViDoiDaChon() {
        return donViDoiDaChon;
    }

    public static void setDonViDoiDaChon(DonViDoiTamData donViDoiDaChon) {
        DoiTraSession.donViDoiDaChon = donViDoiDaChon;
    }

    public static List<ChiTietDoiTraTamData> getDsChiTietTam() {
        return new ArrayList<>(dsChiTietTam);
    }

    public static void setDsChiTietTam(List<ChiTietDoiTraTamData> data) {
        dsChiTietTam.clear();
        if (data != null) {
            dsChiTietTam.addAll(data);
        }
    }

    public static void clearDraft() {
        dangChonThuocDoi = false;
        lyDoTam = "";
        phiPhatTam = "0";
        hinhThucXuLyTam = HinhThucDoiTra.HOAN_TIEN;
        thuocDoiDaChon = null;
        donViDoiDaChon = null;
        dsChiTietTam.clear();
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

        public ChiTietDoiTraTamData(String maQuyDoi, String maLoThuoc, String tenThuoc,
                                    String tenDonVi, int soLuongTra, double thanhTienHoan) {
            this.maQuyDoi = maQuyDoi;
            this.maLoThuoc = maLoThuoc;
            this.tenThuoc = tenThuoc;
            this.tenDonVi = tenDonVi;
            this.soLuongTra = soLuongTra;
            this.thanhTienHoan = thanhTienHoan;
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
    }

    public static class DonViDoiTamData {
        private final String maQuyDoi;
        private final String tenDonVi;
        private final int soLuong;
        private final double donGia;
        private final String maBangGia;

        public DonViDoiTamData(String maQuyDoi, String tenDonVi, int soLuong, double donGia, String maBangGia) {
            this.maQuyDoi = maQuyDoi;
            this.tenDonVi = tenDonVi;
            this.soLuong = soLuong;
            this.donGia = donGia;
            this.maBangGia = maBangGia;
        }

        public DonViDoiTamData(DonViQuyDoi donVi, int soLuong, double donGia, String maBangGia) {
            this(
                    donVi != null ? donVi.getMaQuyDoi() : null,
                    donVi != null ? donVi.getTenDonVi() : "",
                    soLuong,
                    donGia,
                    maBangGia
            );
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

        public String getMaBangGia() {
            return maBangGia;
        }

        public double getThanhTien() {
            return soLuong * donGia;
        }
    }
}
