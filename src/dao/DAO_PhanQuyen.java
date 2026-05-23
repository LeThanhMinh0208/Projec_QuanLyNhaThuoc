package dao;

import connectDB.ConnectDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DAO_PhanQuyen {

    /**
     * Lấy danh sách mã quyền của nhân viên
     */
    public List<String> getQuyenByNhanVien(String maNV) {
        List<String> dsQuyen = new ArrayList<>();
        String sql = "SELECT maQuyen FROM PhanQuyen WHERE maNhanVien = ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                dsQuyen.add(rs.getString("maQuyen"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return dsQuyen;
    }

    /**
     * Cập nhật toàn bộ quyền cho nhân viên (xóa cũ + insert mới)
     */
    public boolean capNhatQuyen(String maNV, List<String> dsQuyen) {
        String sqlXoa = "DELETE FROM PhanQuyen WHERE maNhanVien = ?";
        String sqlThem = "INSERT INTO PhanQuyen (maNhanVien, maQuyen) VALUES (?, ?)";
        try (Connection con = ConnectDB.getInstance().getConnection()) {
            con.setAutoCommit(false);
            try {
                // Xóa quyền cũ
                try (PreparedStatement psXoa = con.prepareStatement(sqlXoa)) {
                    psXoa.setString(1, maNV);
                    psXoa.executeUpdate();
                }
                // Thêm quyền mới
                try (PreparedStatement psThem = con.prepareStatement(sqlThem)) {
                    for (String maQuyen : dsQuyen) {
                        psThem.setString(1, maNV);
                        psThem.setString(2, maQuyen);
                        psThem.addBatch();
                    }
                    psThem.executeBatch();
                }
                con.commit();
                return true;
            } catch (Exception e) {
                con.rollback();
                e.printStackTrace();
                return false;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa toàn bộ quyền của nhân viên
     */
    public boolean xoaQuyenByNhanVien(String maNV) {
        String sql = "DELETE FROM PhanQuyen WHERE maNhanVien = ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            return ps.executeUpdate() >= 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
