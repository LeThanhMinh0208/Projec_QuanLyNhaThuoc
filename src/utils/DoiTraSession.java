package utils;

import entity.HoaDonView;

public class DoiTraSession {
    private static HoaDonView hoaDonDangXuLy;

    public static HoaDonView getHoaDonDangXuLy() {
        return hoaDonDangXuLy;
    }

    public static void setHoaDonDangXuLy(HoaDonView hoaDon) {
        hoaDonDangXuLy = hoaDon;
    }

    public static void clear() {
        hoaDonDangXuLy = null;
    }
}
