package dao;

import connectDB.ConnectDB;
import entity.DonViQuyDoi;
import java.sql.*;
import java.util.ArrayList;

public class DAO_DonViQuyDoi {
    public ArrayList<DonViQuyDoi> getDonViByMaThuoc(String maThuoc) {
        ArrayList<DonViQuyDoi> ds = new ArrayList<>();
        String sql = "SELECT * FROM DonViQuyDoi WHERE maThuoc = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maThuoc);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                // Giả định constructor DonViQuyDoi(ma, ten, tyLe, gia, thuoc)
                // ds.add(new DonViQuyDoi(...));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }
}