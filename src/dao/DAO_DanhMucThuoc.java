package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import connectDB.ConnectDB;
import entity.DanhMucThuoc;

public class DAO_DanhMucThuoc {
    
    // Hàm lấy toàn bộ danh mục thuốc từ CSDL
    public ArrayList<DanhMucThuoc> getAllDanhMuc() {
        ArrayList<DanhMucThuoc> list = new ArrayList<>();
        String sql = "SELECT * FROM DanhMucThuoc";
        
        // ĐƯA CONNECTION RA NGOÀI TRY
        Connection con = ConnectDB.getConnection();
        
        try {
            // CHỈ ĐỂ PreparedStatement VÀ ResultSet VÀO TRONG
            try (PreparedStatement pst = con.prepareStatement(sql);
                 ResultSet rs = pst.executeQuery()) {
                 
                while (rs.next()) {
                    DanhMucThuoc dm = new DanhMucThuoc(
                        rs.getString("maDanhMuc"),
                        rs.getString("tenDanhMuc"),
                        rs.getString("moTa")
                    );
                    list.add(dm);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy danh mục: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
}