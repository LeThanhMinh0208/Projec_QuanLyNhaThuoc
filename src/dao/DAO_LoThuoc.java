package dao;

import connectDB.ConnectDB;
import entity.LoThuoc;
import entity.Thuoc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class DAO_LoThuoc {
    public ArrayList<LoThuoc> getAllLoThuoc() {
        ArrayList<LoThuoc> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            
            // Lệnh JOIN chuẩn xác theo DB của bạn
            String sql = "SELECT l.maLoThuoc, l.ngaySanXuat, l.hanSuDung, l.soLuongTon, l.giaNhap, l.viTriKho, " +
                         "t.maThuoc, t.tenThuoc, t.hinhAnh " +
                         "FROM LoThuoc l JOIN Thuoc t ON l.maThuoc = t.maThuoc";
            
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
            	Thuoc thuoc = new Thuoc();
            	thuoc.setMaThuoc(rs.getString("maThuoc"));
            	thuoc.setTenThuoc(rs.getString("tenThuoc"));
            	thuoc.setHinhAnh(rs.getString("hinhAnh"));
                

                LoThuoc lo = new LoThuoc(
                    rs.getString("maLoThuoc"),
                    rs.getDate("ngaySanXuat"),
                    rs.getDate("hanSuDung"),
                    rs.getInt("soLuongTon"),
                    rs.getDouble("giaNhap"),
                    rs.getString("viTriKho"),
                    thuoc
                );
                list.add(lo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    public boolean themLoThuoc(entity.LoThuoc lo) {
      
        String sql = "INSERT INTO LoThuoc (MaLoThuoc, MaThuoc, NgaySanXuat, HanSuDung, SoLuongTon, GiaNhap, ViTriKho) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (java.sql.Connection con = connectDB.ConnectDB.getInstance().getConnection();
             java.sql.PreparedStatement stmt = con.prepareStatement(sql)) {
             
            stmt.setString(1, lo.getMaLoThuoc());
            
      
            stmt.setString(2, lo.getThuoc().getMaThuoc()); 
            
            stmt.setDate(3, lo.getNgaySanXuat());
            stmt.setDate(4, lo.getHanSuDung());
            stmt.setInt(5, lo.getSoLuongTon());
            stmt.setDouble(6, lo.getGiaNhap());
            stmt.setString(7, lo.getViTriKho());
            
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (java.sql.SQLException e) {
            System.err.println("❌ Lỗi SQL khi thêm Lô: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}