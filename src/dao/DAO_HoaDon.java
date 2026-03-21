package dao;

import connectDB.ConnectDB;
import entity.HoaDon;
import entity.ChiTietHoaDon;
import java.sql.*;
import java.util.List;

public class DAO_HoaDon {
    public boolean thanhToan(HoaDon hd, List<ChiTietHoaDon> dsCT) {
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false); // Bắt đầu giao dịch

            // 1. Lưu HoaDon
            String sqlHD = "INSERT INTO HoaDon(maHoaDon, maKhachHang, maNhanVien, ngayLap, thueVAT, hinhThucThanhToan, ghiChu) VALUES(?,?,?,?,?,?,?)";
            PreparedStatement pstHD = con.prepareStatement(sqlHD);
            pstHD.setString(1, hd.getMaHoaDon());
            pstHD.setString(2, hd.getKhachHang() != null ? hd.getKhachHang().getMaKhachHang() : null);
            pstHD.setString(3, hd.getNhanVien().getMaNhanVien());
            pstHD.setDate(4, hd.getNgayLap());
            pstHD.setDouble(5, hd.getThueVAT());
            pstHD.setString(6, hd.getHinhThucThanhToan());
            pstHD.setString(7, hd.getGhiChu());
            pstHD.executeUpdate();

            // 2. Lưu ChiTietHoaDon
            String sqlCT = "INSERT INTO ChiTietHoaDon(maHoaDon, maQuyDoi, maLoThuoc, soLuong, donGia) VALUES(?,?,?,?,?)";
            PreparedStatement pstCT = con.prepareStatement(sqlCT);
            for (ChiTietHoaDon ct : dsCT) {
                pstCT.setString(1, hd.getMaHoaDon());
                pstCT.setString(2, ct.getMaQuyDoi());
                pstCT.setString(3, ct.getMaLoThuoc());
                pstCT.setInt(4, ct.getSoLuong());
                pstCT.setDouble(5, ct.getDonGia());
                pstCT.executeUpdate();

                // Cập nhật tồn kho theo lô (giảm soLuongTon trong LoThuoc)
                String sqlUpdateLo = "UPDATE LoThuoc SET soLuongTon = soLuongTon - ? WHERE maLoThuoc = ?";
                try (PreparedStatement pstUpdateLo = con.prepareStatement(sqlUpdateLo)) {
                    pstUpdateLo.setInt(1, ct.getSoLuongTruKho());
                    pstUpdateLo.setString(2, ct.getMaLoThuoc());
                    pstUpdateLo.executeUpdate();
                }
            }

            con.commit();
            con.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            if (con != null)
                try {
                    con.rollback();
                    con.setAutoCommit(true);
                } catch (SQLException ex) {
                }
            e.printStackTrace();
            return false;
        }
    }

    public String generateMaHoaDon() {
        String sql = "SELECT MAX(CAST(SUBSTRING(maHoaDon, 3, LEN(maHoaDon)) AS INT)) FROM HoaDon";
        try (Connection con = ConnectDB.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                int max = rs.getInt(1);
                return String.format("HD%04d", max + 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "HD0001";
    }
}