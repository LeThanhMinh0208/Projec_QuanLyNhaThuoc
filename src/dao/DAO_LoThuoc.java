package dao;

import connectDB.ConnectDB;
import entity.LoThuoc;
import entity.Thuoc;

import java.sql.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public java.util.List<entity.LoThuoc> getLoThuocTheoFEFO(String maThuoc, String viTriKho) {
        java.util.List<entity.LoThuoc> list = new java.util.ArrayList<>();
       
        String sql = "SELECT * FROM LoThuoc WHERE maThuoc = ? AND viTriKho = ? AND soLuongTon > 0 ORDER BY hanSuDung ASC";
        try (java.sql.Connection con = connectDB.ConnectDB.getInstance().getConnection();
             java.sql.PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maThuoc);
            pst.setString(2, viTriKho);
            java.sql.ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                entity.LoThuoc lo = new entity.LoThuoc();
                lo.setMaLoThuoc(rs.getString("maLoThuoc"));
                lo.setSoLuongTon(rs.getInt("soLuongTon"));
                lo.setHanSuDung(rs.getDate("hanSuDung"));
                lo.setNgaySanXuat(rs.getDate("ngaySanXuat"));
                lo.setViTriKho(rs.getString("viTriKho"));
                list.add(lo);
            }
        } catch (java.sql.SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Lấy các lô còn tồn, chưa hết hạn, ưu tiên kho bán hàng, sắp xếp theo hạn sử dụng tăng dần (FEFO).
     */
    public ArrayList<LoThuoc> getLoThuocBanDuocByMaThuoc(String maThuoc) {
        ArrayList<LoThuoc> ds = new ArrayList<>();
        String sql =
                "SELECT * FROM LoThuoc " +
                "WHERE maThuoc = ? " +
                "  AND soLuongTon > 0 " +
                "  AND hanSuDung >= CAST(GETDATE() AS DATE) " +
                "ORDER BY CASE WHEN viTriKho = 'KHO_BAN_HANG' THEN 0 ELSE 1 END, hanSuDung ASC";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maThuoc);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                LoThuoc lo = new LoThuoc();
                lo.setMaLoThuoc(rs.getString("maLoThuoc"));
                lo.setNgaySanXuat(rs.getDate("ngaySanXuat"));
                lo.setHanSuDung(rs.getDate("hanSuDung"));
                lo.setSoLuongTon(rs.getInt("soLuongTon"));
                lo.setGiaNhap(rs.getDouble("giaNhap"));
                String viTri = rs.getString("viTriKho");
                lo.setViTriKho(viTri);

                Thuoc t = new Thuoc();
                t.setMaThuoc(rs.getString("maThuoc"));
                lo.setThuoc(t);

                ds.add(lo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    /**
     * FEFO nghiêm ngặt: lấy 1 lô duy nhất phù hợp nhất để bán
     * (KHO_BAN_HANG, còn tồn, chưa hết hạn, hạn gần nhất)
     * Trả về maLoThuoc hoặc null nếu không có lô phù hợp.
     */
    public String getLoFEFO(String maThuoc) {
        String sql = "SELECT TOP 1 maLoThuoc FROM LoThuoc " +
                     "WHERE maThuoc = ? " +
                     "  AND soLuongTon > 0 " +
                     "  AND viTriKho = 'KHO_BAN_HANG' " +
                     "  AND hanSuDung > CAST(GETDATE() AS DATE) " +
                     "ORDER BY hanSuDung ASC";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maThuoc);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getString("maLoThuoc");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}