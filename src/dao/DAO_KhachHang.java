package dao;

import connectDB.ConnectDB;
import entity.KhachHang;
import java.sql.*;
import java.util.ArrayList;

public class DAO_KhachHang {
    public ArrayList<KhachHang> getAllKhachHang() {
        ArrayList<KhachHang> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM KhachHang";
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                ds.add(new KhachHang(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getInt(5)));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    public boolean themKhachHang(KhachHang kh) {
        String sql = "INSERT INTO KhachHang VALUES(?,?,?,?,?)";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, kh.getMaKhachHang());
            pst.setString(2, kh.getHoTen());
            pst.setString(3, kh.getSdt());
            pst.setString(4, kh.getDiaChi());
            pst.setInt(5, kh.getDiemTichLuy());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }
}