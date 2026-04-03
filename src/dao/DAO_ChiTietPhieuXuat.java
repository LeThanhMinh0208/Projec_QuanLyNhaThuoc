package dao;

import connectDB.ConnectDB;
import entity.ChiTietPhieuXuat;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAO_ChiTietPhieuXuat {
    public List<ChiTietPhieuXuat> getChiTietByMaPhieu(String maPhieu) {
        List<ChiTietPhieuXuat> list = new ArrayList<>();
        // Query Join để lấy tên thuốc
        String sql = "SELECT ct.*, t.tenThuoc FROM ChiTietPhieuXuat ct " +
                     "JOIN Thuoc t ON ct.maThuoc = t.maThuoc WHERE ct.maPhieuXuat = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhieu);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ChiTietPhieuXuat ct = new ChiTietPhieuXuat();
                ct.setMaThuoc(rs.getString("tenThuoc")); // Hiện tên thuốc
                ct.setSoLo(rs.getString("soLo"));
                ct.setSoLuong(rs.getInt("soLuong"));
                list.add(ct);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}