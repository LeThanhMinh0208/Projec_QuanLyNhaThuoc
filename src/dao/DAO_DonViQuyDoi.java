package dao;

import connectDB.ConnectDB;
import entity.DonViQuyDoi;
import entity.Thuoc;

import java.sql.*;
import java.util.ArrayList;

public class DAO_DonViQuyDoi {

    /** Lấy tất cả đơn vị quy đổi theo maThuoc (chỉ 4 cột thực tế trong DB, không có giaBan) */
    public ArrayList<DonViQuyDoi> getDonViByMaThuoc(String maThuoc) {
        ArrayList<DonViQuyDoi> list = new ArrayList<>();
        String sql = "SELECT maQuyDoi, maThuoc, tenDonVi, tyLeQuyDoi "
                   + "FROM DonViQuyDoi WHERE maThuoc = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maThuoc);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String maQuyDoi = rs.getString("maQuyDoi");
                String tenDonVi = rs.getString("tenDonVi");
                int tyLeQuyDoi = rs.getInt("tyLeQuyDoi");
                
                list.add(new DonViQuyDoi(maQuyDoi, maThuoc, tenDonVi, tyLeQuyDoi));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Lấy 1 đơn vị theo maQuyDoi */
    public DonViQuyDoi getByMaQuyDoi(String maQuyDoi) {
        String sql = "SELECT * FROM DonViQuyDoi WHERE maQuyDoi = ?";
        try (Connection con = ConnectDB.getConnection(); 
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maQuyDoi);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new DonViQuyDoi(
                        rs.getString("maQuyDoi"),
                        rs.getString("maThuoc"),
                        rs.getString("tenDonVi"),
                        rs.getInt("tyLeQuyDoi")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
 // ========================================================
    // HÀM LẤY ĐƠN VỊ LỚN NHẤT CỦA THUỐC (Dành riêng cho Đặt Hàng)
    // ========================================================
    public DonViQuyDoi getDonViLonNhatCuaThuoc(String maThuoc) {
        DonViQuyDoi dvMax = null;
        String sql = "SELECT TOP 1 * FROM DonViQuyDoi WHERE maThuoc = ? ORDER BY tyLeQuyDoi DESC";
        
        ConnectDB.getInstance();
		try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            
            pst.setString(1, maThuoc);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                dvMax = new DonViQuyDoi();
                dvMax.setMaQuyDoi(rs.getString("maQuyDoi"));
                
                // FIX LỖI GẠCH ĐỎ Ở ĐÂY NÈ SẾP:
                dvMax.setMaThuoc(maThuoc); 
                
                dvMax.setTenDonVi(rs.getString("tenDonVi"));
                dvMax.setTyLeQuyDoi(rs.getInt("tyLeQuyDoi"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dvMax;
    }
}