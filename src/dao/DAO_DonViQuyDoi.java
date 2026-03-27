package dao;

import connectDB.ConnectDB;
import entity.DonViQuyDoi;

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
                list.add(new DonViQuyDoi(
                        rs.getString("maQuyDoi"),
                        rs.getString("maThuoc"),
                        rs.getString("tenDonVi"),
                        rs.getInt("tyLeQuyDoi")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Lấy 1 đơn vị theo maQuyDoi */
    public DonViQuyDoi getByMaQuyDoi(String maQuyDoi) {
        String sql = "SELECT maQuyDoi, maThuoc, tenDonVi, tyLeQuyDoi "
                   + "FROM DonViQuyDoi WHERE maQuyDoi = ?";
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
}