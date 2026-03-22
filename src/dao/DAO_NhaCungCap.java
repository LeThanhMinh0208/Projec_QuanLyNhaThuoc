package dao;

import connectDB.ConnectDB;
import entity.NhaCungCap;
import java.sql.*;
import java.util.ArrayList;

public class DAO_NhaCungCap {
    public ArrayList<NhaCungCap> getAllNhaCungCap() {
        ArrayList<NhaCungCap> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM NhaCungCap";
            ResultSet rs = con.createStatement().executeQuery(sql);
            while (rs.next()) {
                ds.add(new NhaCungCap(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getDouble(5)));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }
}