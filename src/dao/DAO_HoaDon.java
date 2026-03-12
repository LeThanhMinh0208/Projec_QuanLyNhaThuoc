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
            String sqlHD = "INSERT INTO HoaDon VALUES(?,?,?,?,?,?,?)";
            PreparedStatement pstHD = con.prepareStatement(sqlHD);
            pstHD.setString(1, hd.getMaHoaDon());
            pstHD.setDate(2, hd.getNgayLap());
            pstHD.setDouble(3, hd.getThueVAT());
            pstHD.setString(4, hd.getHinhThucThanhToan());
            pstHD.setString(5, hd.getGhiChu());
            pstHD.setString(6, hd.getNhanVien().getMaNhanVien());
            pstHD.setString(7, hd.getKhachHang().getMaKhachHang());
            pstHD.executeUpdate();

            // 2. Lưu ChiTietHoaDon
            String sqlCT = "INSERT INTO ChiTietHoaDon VALUES(?,?,?,?)";
            PreparedStatement pstCT = con.prepareStatement(sqlCT);
            for (ChiTietHoaDon ct : dsCT) {
                pstCT.setString(1, hd.getMaHoaDon());
                pstCT.setString(2, ct.getThuoc().getMaThuoc());
                pstCT.setInt(3, ct.getSoLuong());
                pstCT.setDouble(4, ct.getDonGia());
                pstCT.executeUpdate();
                
                // Cập nhật tồn kho (Ví dụ trừ ở bảng Thuoc)
                String sqlUpdateKho = "UPDATE Thuoc SET soLuongTon = soLuongTon - ? WHERE maThuoc = ?";
                // ... thực hiện update kho ...
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null) try { con.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        }
    }
}