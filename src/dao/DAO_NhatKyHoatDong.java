package dao;

import connectDB.ConnectDB;
import entity.NhatKyHoatDong;
import utils.UserSession;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DAO_NhatKyHoatDong {

    /**
     * Ghi nhật ký hoạt động (dùng nhân viên đang đăng nhập)
     */
    public static boolean ghiLog(String hanhDong, String doiTuong, String maDoiTuong, String moTa) {
        String maNV = null;
        if (UserSession.getInstance().getUser() != null) {
            maNV = UserSession.getInstance().getUser().getMaNhanVien();
        }
        if (maNV == null) return false;
        return ghiLog(maNV, hanhDong, doiTuong, maDoiTuong, moTa);
    }

    /**
     * Ghi nhật ký hoạt động (chỉ định mã nhân viên)
     */
    public static boolean ghiLog(String maNV, String hanhDong, String doiTuong, String maDoiTuong, String moTa) {
        String sql = "INSERT INTO NhatKyHoatDong (maNhanVien, hanhDong, doiTuong, maDoiTuong, moTa) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setString(2, hanhDong);
            ps.setString(3, doiTuong);
            ps.setString(4, maDoiTuong);
            ps.setString(5, moTa);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy toàn bộ nhật ký (mới nhất trước)
     */
    public List<NhatKyHoatDong> getAll() {
        List<NhatKyHoatDong> list = new ArrayList<>();
        String sql = "SELECT nk.*, nv.hoTen AS tenNhanVien FROM NhatKyHoatDong nk "
                   + "JOIN NhanVien nv ON nk.maNhanVien = nv.maNhanVien "
                   + "ORDER BY nk.thoiGian DESC";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Tìm kiếm / lọc nhật ký
     */
    public List<NhatKyHoatDong> timKiem(String keyword, String maNV, LocalDate tuNgay, LocalDate denNgay) {
        List<NhatKyHoatDong> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT nk.*, nv.hoTen AS tenNhanVien FROM NhatKyHoatDong nk "
          + "JOIN NhanVien nv ON nk.maNhanVien = nv.maNhanVien WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (maNV != null && !maNV.isEmpty()) {
            sql.append(" AND nk.maNhanVien = ?");
            params.add(maNV);
        }
        if (tuNgay != null) {
            sql.append(" AND CAST(nk.thoiGian AS DATE) >= ?");
            params.add(Date.valueOf(tuNgay));
        }
        if (denNgay != null) {
            sql.append(" AND CAST(nk.thoiGian AS DATE) <= ?");
            params.add(Date.valueOf(denNgay));
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (nk.hanhDong LIKE ? OR nk.doiTuong LIKE ? OR nk.maDoiTuong LIKE ? OR nk.moTa LIKE ? OR nv.hoTen LIKE ?)");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw); params.add(kw); params.add(kw); params.add(kw); params.add(kw);
        }
        sql.append(" ORDER BY nk.thoiGian DESC");

        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    private NhatKyHoatDong mapRow(ResultSet rs) throws SQLException {
        NhatKyHoatDong nk = new NhatKyHoatDong();
        nk.setMaLog(rs.getInt("maLog"));
        nk.setMaNhanVien(rs.getString("maNhanVien"));
        nk.setTenNhanVien(rs.getString("tenNhanVien"));
        nk.setHanhDong(rs.getString("hanhDong"));
        nk.setDoiTuong(rs.getString("doiTuong"));
        nk.setMaDoiTuong(rs.getString("maDoiTuong"));
        nk.setMoTa(rs.getString("moTa"));
        Timestamp ts = rs.getTimestamp("thoiGian");
        if (ts != null) nk.setThoiGian(ts.toLocalDateTime());
        return nk;
    }
}
