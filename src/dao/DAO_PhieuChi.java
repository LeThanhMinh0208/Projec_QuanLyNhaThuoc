package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import connectDB.ConnectDB;

public class DAO_PhieuChi {

    // Hàm tự động sinh mã Phiếu Chi (VD: PC001)
    private String getMaPhieuChiMoi(Connection con) throws SQLException {
        String sql = "SELECT TOP 1 maPhieuChi FROM PhieuChi ORDER BY maPhieuChi DESC";
        try (Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                String maCu = rs.getString("maPhieuChi");
                int soCu = Integer.parseInt(maCu.substring(2));
                return "PC" + String.format("%03d", soCu + 1);
            }
        }
        return "PC001";
    }

    // GIAO DỊCH (TRANSACTION): Tạo phiếu chi + Trừ công nợ
    public boolean lapPhieuChi(String maNCC, String maNV, double soTienChi, String hinhThuc, String ghiChu) {
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false); // Bắt đầu Transaction

            // 1. Tạo Phiếu Chi
            String maPhieuChi = getMaPhieuChiMoi(con);
            String sqlInsert = "INSERT INTO PhieuChi (maPhieuChi, maNhaCungCap, maNhanVien, ngayChi, tongTienChi, hinhThucChi, ghiChu) "
                    + "VALUES (?, ?, ?, GETDATE(), ?, ?, ?)";
            try (PreparedStatement pst1 = con.prepareStatement(sqlInsert)) {
                pst1.setString(1, maPhieuChi);
                pst1.setString(2, maNCC);
                pst1.setString(3, maNV);
                pst1.setDouble(4, soTienChi);
                pst1.setString(5, hinhThuc);
                pst1.setString(6, ghiChu);
                pst1.executeUpdate();
            }

            // 2. Trừ Công Nợ của Nhà Cung Cấp
            String sqlUpdate = "UPDATE NhaCungCap SET congNo = congNo - ? WHERE maNhaCungCap = ?";
            try (PreparedStatement pst2 = con.prepareStatement(sqlUpdate)) {
                pst2.setDouble(1, soTienChi);
                pst2.setString(2, maNCC);
                pst2.executeUpdate();
            }

            con.commit(); // Chốt giao dịch thành công!
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } // Lỗi thì hoàn tác
            }
            return false;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public java.util.List<entity.PhieuChi> getAllPhieuChi(String tuKhoa) {
        java.util.List<entity.PhieuChi> list = new java.util.ArrayList<>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = ConnectDB.getConnection();
            String sql = "SELECT pc.*, ncc.tenNhaCungCap, nv.hoTen " +
                    "FROM PhieuChi pc " +
                    "JOIN NhaCungCap ncc ON pc.maNhaCungCap = ncc.maNhaCungCap " +
                    "JOIN NhanVien nv ON pc.maNhanVien = nv.maNhanVien ";

            // Nếu có từ khóa tìm kiếm
            if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
                sql += "WHERE pc.maPhieuChi LIKE ? OR ncc.tenNhaCungCap LIKE ? ";
            }
            sql += "ORDER BY pc.ngayChi DESC";

            pstmt = con.prepareStatement(sql);
            if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
                pstmt.setString(1, "%" + tuKhoa.trim() + "%");
                pstmt.setString(2, "%" + tuKhoa.trim() + "%");
            }

            rs = pstmt.executeQuery();
            while (rs.next()) {
                entity.PhieuChi pc = new entity.PhieuChi();
                pc.setMaPhieuChi(rs.getString("maPhieuChi"));
                pc.setNgayChi(rs.getTimestamp("ngayChi"));
                pc.setTongTienChi(rs.getDouble("tongTienChi"));
                pc.setHinhThucChi(rs.getString("hinhThucChi"));
                pc.setGhiChu(rs.getString("ghiChu"));

                entity.NhaCungCap ncc = new entity.NhaCungCap();
                ncc.setMaNhaCungCap(rs.getString("maNhaCungCap"));
                ncc.setTenNhaCungCap(rs.getString("tenNhaCungCap"));
                pc.setNhaCungCap(ncc);

                entity.NhanVien nv = new entity.NhanVien();
                nv.setMaNhanVien(rs.getString("maNhanVien"));
                nv.setHoTen(rs.getString("hoTen"));
                pc.setNhanVien(nv);

                list.add(pc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}