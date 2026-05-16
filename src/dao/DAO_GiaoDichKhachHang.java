package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import connectDB.ConnectDB;
import entity.GiaoDichKhachHang;

public class DAO_GiaoDichKhachHang {

    /**
     * Lấy toàn bộ lịch sử hóa đơn của 1 khách hàng, có lọc ngày và hình thức.
     */
    public List<GiaoDichKhachHang> getByKhachHang(
            String maKhachHang,
            LocalDate tuNgay,
            LocalDate denNgay,
            String hinhThuc) {

        List<GiaoDichKhachHang> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
            "SELECT hd.maHoaDon, hd.ngayLap, nv.hoTen AS tenNhanVien, " +
            "       hd.thueVAT, hd.hinhThucThanhToan, hd.ghiChu, " +
            "       SUM(ct.soLuong * ct.donGia) AS tamTinh, " +
            "       SUM(ct.soLuong * ct.donGia) * (1 + hd.thueVAT/100.0) AS tongSauVAT " +
            "FROM HoaDon hd " +
            "LEFT JOIN NhanVien nv ON hd.maNhanVien = nv.maNhanVien " +
            "JOIN ChiTietHoaDon ct ON hd.maHoaDon = ct.maHoaDon " +
            "WHERE hd.maKhachHang = ?"
        );

        List<Object> params = new ArrayList<>();
        params.add(maKhachHang);

        if (tuNgay != null) {
            sql.append(" AND CAST(hd.ngayLap AS DATE) >= ?");
            params.add(Date.valueOf(tuNgay));
        }
        if (denNgay != null) {
            sql.append(" AND CAST(hd.ngayLap AS DATE) <= ?");
            params.add(Date.valueOf(denNgay));
        }
        if (hinhThuc != null && !hinhThuc.isBlank()) {
            sql.append(" AND hd.hinhThucThanhToan = ?");
            params.add(hinhThuc);
        }

        sql.append(" GROUP BY hd.maHoaDon, hd.ngayLap, nv.hoTen, hd.thueVAT, hd.hinhThucThanhToan, hd.ghiChu");
        sql.append(" ORDER BY hd.ngayLap DESC");

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pst.setObject(i + 1, params.get(i));
            }
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                GiaoDichKhachHang gd = new GiaoDichKhachHang();
                gd.setMaHoaDon(rs.getString("maHoaDon"));
                gd.setNgayLap(rs.getTimestamp("ngayLap"));
                gd.setTenNhanVien(rs.getString("tenNhanVien") != null ? rs.getString("tenNhanVien") : "—");
                gd.setThueVAT(rs.getDouble("thueVAT"));
                gd.setHinhThucThanhToan(rs.getString("hinhThucThanhToan"));
                gd.setGhiChu(rs.getString("ghiChu"));
                gd.setTamTinh(rs.getDouble("tamTinh"));
                gd.setTongSauVAT(rs.getDouble("tongSauVAT"));
                list.add(gd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy tổng thống kê: tổng hóa đơn, tổng chi tiêu của khách.
     * Trả về double[]{tongHoaDon, tongChiTieu}
     */
    public double[] getThongKe(String maKhachHang) {
        String sql =
            "SELECT COUNT(DISTINCT hd.maHoaDon) AS tongHD, " +
            "       SUM(ct.soLuong * ct.donGia * (1 + hd.thueVAT/100.0)) AS tongTien " +
            "FROM HoaDon hd " +
            "JOIN ChiTietHoaDon ct ON hd.maHoaDon = ct.maHoaDon " +
            "WHERE hd.maKhachHang = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maKhachHang);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new double[]{rs.getInt("tongHD"), rs.getDouble("tongTien")};
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new double[]{0, 0};
    }
    public List<GiaoDichKhachHang> getAllGiaoDich() {
        List<GiaoDichKhachHang> list = new java.util.ArrayList<>();
        // Thêm JOIN KhachHang và lấy thêm trường hoTen, sdt
        String sql = "SELECT hd.maHoaDon, hd.ngayLap, kh.hoTen AS tenKhachHang, kh.sdt, " +
                     "       nv.hoTen AS tenNhanVien, " +
                     "       hd.thueVAT, hd.hinhThucThanhToan, hd.ghiChu, " +
                     "       SUM(ct.soLuong * ct.donGia) AS tamTinh, " +
                     "       SUM(ct.soLuong * ct.donGia) * (1 + hd.thueVAT/100.0) AS tongSauVAT " +
                     "FROM HoaDon hd " +
                     "LEFT JOIN KhachHang kh ON hd.maKhachHang = kh.maKhachHang " + // Join bảng KH
                     "LEFT JOIN NhanVien nv ON hd.maNhanVien = nv.maNhanVien " + // Thêm Join Nhân Viên
                     "JOIN ChiTietHoaDon ct ON hd.maHoaDon = ct.maHoaDon " +
                     "WHERE hd.maKhachHang IS NOT NULL " +
                     "GROUP BY hd.maHoaDon, hd.ngayLap, kh.hoTen, kh.sdt, nv.hoTen, hd.thueVAT, hd.hinhThucThanhToan, hd.ghiChu " +
                     "ORDER BY hd.ngayLap DESC";

        try (java.sql.Connection con = connectDB.ConnectDB.getConnection();
             java.sql.PreparedStatement pst = con.prepareStatement(sql);
             java.sql.ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                GiaoDichKhachHang gd = new GiaoDichKhachHang();
                gd.setMaHoaDon(rs.getString("maHoaDon"));
                gd.setNgayLap(rs.getTimestamp("ngayLap"));
                // Lưu thông tin khách hàng vào Entity (Đảm bảo class GiaoDichKhachHang có các field này)
                gd.setTenKhachHang(rs.getString("tenKhachHang"));
                gd.setSdtKhachHang(rs.getString("sdt"));
                gd.setTenNhanVien(rs.getString("tenNhanVien") != null ? rs.getString("tenNhanVien") : "—");

                gd.setThueVAT(rs.getDouble("thueVAT"));
                gd.setHinhThucThanhToan(rs.getString("hinhThucThanhToan"));
                gd.setTamTinh(rs.getDouble("tamTinh"));
                gd.setTongSauVAT(rs.getDouble("tongSauVAT"));
                list.add(gd);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}