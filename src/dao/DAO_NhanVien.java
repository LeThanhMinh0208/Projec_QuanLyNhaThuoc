package dao;

import connectDB.ConnectDB;
import entity.NhanVien;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DAO_NhanVien {
    
    public NhanVien dangNhap(String taiKhoan, String matKhau) {
        Connection con = ConnectDB.getInstance().getConnection();
        String sql = "SELECT * FROM NhanVien WHERE tenDangNhap = ? AND matKhau = ?";
        
        try {
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, taiKhoan);
            pst.setString(2, matKhau);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                String tenDN_DB = rs.getString("tenDangNhap");
                String pass_DB = rs.getString("matKhau");
                
                if (tenDN_DB.equals(taiKhoan) && pass_DB.equals(matKhau)) {
                    
                    String maNV = rs.getString("maNhanVien");
                    String hoTen = rs.getString("hoTen");
                    String chucVu = rs.getString("chucVu");
                    String caLam = rs.getString("caLamViec");
                    String sdt = rs.getString("sdt");
                    
                    return new NhanVien(maNV, tenDN_DB, pass_DB, hoTen, chucVu, caLam, sdt);
                } 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
}