package dao;

import connectDB.ConnectDB;
import entity.DonViQuyDoi;
import entity.Thuoc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
                DonViQuyDoi dv = new DonViQuyDoi();
                dv.setMaQuyDoi(rs.getString("maQuyDoi"));
                
                Thuoc t = new Thuoc();
                t.setMaThuoc(rs.getString("maThuoc"));
                dv.setThuoc(t);
                
                dv.setTenDonVi(rs.getString("tenDonVi"));
                dv.setTyLeQuyDoi(rs.getInt("tyLeQuyDoi"));
                dv.setGiaBan(rs.getDouble("giaBan"));
                
                list.add(dv);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}