package dao;

import connectDB.ConnectDB;
import entity.DonViQuyDoi;

import java.sql.*;
import java.util.ArrayList;

public class DAO_DonViQuyDoi {
    public ArrayList<DonViQuyDoi> getDonViByMaThuoc(String maThuoc) {
        ArrayList<DonViQuyDoi> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        String sql = "SELECT * FROM DonViQuyDoi WHERE maThuoc = ?";
        
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maThuoc);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String maQuyDoi = rs.getString("maQuyDoi");
                String maT = rs.getString("maThuoc");
                String tenDonVi = rs.getString("tenDonVi");
                int tyLeQuyDoi = rs.getInt("tyLeQuyDoi");
                
                // FIX LỖI: Bảng CSDL không có cột giaBan, gán mặc định = 0
                double giaBan = 0.0; 
                
                list.add(new DonViQuyDoi(maQuyDoi, maT, tenDonVi, tyLeQuyDoi, giaBan));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

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
                        rs.getInt("tyLeQuyDoi"),
                        0.0 // FIX LỖI TƯƠNG TỰ
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}