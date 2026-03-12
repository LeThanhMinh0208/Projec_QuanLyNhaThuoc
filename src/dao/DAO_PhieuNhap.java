package dao;

import connectDB.ConnectDB;
import entity.PhieuNhap;
import entity.ChiTietPhieuNhap;
import java.sql.*;
import java.util.List;

public class DAO_PhieuNhap {
    public boolean nhapHang(PhieuNhap pn, List<ChiTietPhieuNhap> dsCT) {
        Connection con = ConnectDB.getConnection();
        try {
            con.setAutoCommit(false);
            // Lưu PhieuNhap
            String sqlPN = "INSERT INTO PhieuNhap VALUES(?,?,?,?)";
            PreparedStatement pstPN = con.prepareStatement(sqlPN);
            pstPN.setString(1, pn.getMaPhieuNhap());
            pstPN.setDate(2, pn.getNgayNhap());
            pstPN.setString(3, pn.getNhaCungCap().getMaNhaCungCap());
            pstPN.setString(4, pn.getNhanVien().getMaNhanVien());
            pstPN.executeUpdate();

            // Lưu ChiTietPhieuNhap
            String sqlCT = "INSERT INTO ChiTietPhieuNhap VALUES(?,?,?,?)";
            PreparedStatement pstCT = con.prepareStatement(sqlCT);
            for (ChiTietPhieuNhap ct : dsCT) {
                pstCT.setString(1, pn.getMaPhieuNhap());
                pstCT.setString(2, ct.getThuoc().getMaThuoc());
                pstCT.setInt(3, ct.getSoLuong());
                pstCT.setDouble(4, ct.getDonGiaNhap());
                pstCT.executeUpdate();
            }
            con.commit();
            return true;
        } catch (SQLException e) {
            try { con.rollback(); } catch (Exception ex) {}
            return false;
        }
    }
}