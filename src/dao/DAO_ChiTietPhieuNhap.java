package dao;

import connectDB.ConnectDB;
import entity.ChiTietPhieuNhap;
import entity.Thuoc;
import java.sql.*;
import java.util.ArrayList;

public class DAO_ChiTietPhieuNhap {

    public ArrayList<ChiTietPhieuNhap> getChiTietByMaPhieu(String maPhieu) {
        ArrayList<ChiTietPhieuNhap> ds = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        String sql = "SELECT ct.*, t.tenThuoc FROM ChiTietPhieuNhap ct " +
                     "JOIN Thuoc t ON ct.maThuoc = t.maThuoc " +
                     "WHERE ct.maPhieuNhap = ?";
        
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maPhieu);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                Thuoc t = new Thuoc();
                t.setMaThuoc(rs.getString("maThuoc"));
                t.setTenThuoc(rs.getString("tenThuoc"));
                
                ChiTietPhieuNhap ct = new ChiTietPhieuNhap();
                ct.setThuoc(t);
                ct.setSoLuong(rs.getInt("soLuong"));
                ct.setDonGiaNhap(rs.getDouble("donGiaNhap"));
                
                ds.add(ct);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }
}