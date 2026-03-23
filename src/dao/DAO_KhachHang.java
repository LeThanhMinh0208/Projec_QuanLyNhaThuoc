package dao;

import connectDB.ConnectDB;
import entity.KhachHang;
import java.sql.*;
import java.util.ArrayList;

public class DAO_KhachHang {
    public ArrayList<KhachHang> getAllKhachHang() {
        ArrayList<KhachHang> ds = new ArrayList<>();
        String sql = "SELECT * FROM KhachHang";
        try (Connection con = ConnectDB.getConnection(); 
             Statement statement = con.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                ds.add(new KhachHang(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getInt(5)));
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
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

    public KhachHang getBySdt(String sdt) {
        String sql = "SELECT * FROM KhachHang WHERE sdt = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, sdt);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new KhachHang(
                        rs.getString("maKhachHang"),
                        rs.getString("hoTen"),
                        rs.getString("sdt"),
                        rs.getString("diaChi"),
                        rs.getInt("diemTichLuy")
                );
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean capNhatDiemTichLuy(String maKhachHang, int diemMoi) {
        String sql = "UPDATE KhachHang SET diemTichLuy = ? WHERE maKhachHang = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, diemMoi);
            pst.setString(2, maKhachHang);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public boolean capNhatKhachHang(KhachHang kh) {
        String sql = "UPDATE KhachHang SET hoTen=?, sdt=?, diaChi=?, diemTichLuy=? WHERE maKhachHang=?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, kh.getHoTen());
            pst.setString(2, kh.getSdt());
            pst.setString(3, kh.getDiaChi());
            pst.setInt(4, kh.getDiemTichLuy());
            pst.setString(5, kh.getMaKhachHang());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public boolean xoaKhachHang(String maKhachHang) {
        String sql = "DELETE FROM KhachHang WHERE maKhachHang=?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maKhachHang);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }
}