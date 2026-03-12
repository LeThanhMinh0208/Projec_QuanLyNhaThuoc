package dao;

import connectDB.ConnectDB;
import entity.LoThuoc;
import entity.ViTriKho;
import entity.Thuoc;
import java.sql.*;
import java.util.ArrayList;

public class DAO_LoThuoc {
    public boolean themLoThuoc(LoThuoc lo) {
        String sql = "INSERT INTO LoThuoc VALUES(?,?,?,?,?,?,?)";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, lo.getMaLoThuoc());
            pst.setDate(2, lo.getNgaySanXuat());
            pst.setDate(3, lo.getHanSuDung());
            pst.setInt(4, lo.getSoLuongTon());
            pst.setDouble(5, lo.getGiaNhap());
            pst.setString(6, lo.getViTriKho().name()); 
            pst.setString(7, lo.getThuoc().getMaThuoc());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }
}