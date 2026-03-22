package dao;

import connectDB.ConnectDB;
import entity.PhieuDoiTra;
import java.sql.*;

public class DAO_PhieuDoiTra {
    public boolean lapPhieuDoiTra(PhieuDoiTra pdt) {
    	String sql = "INSERT INTO PhieuDoiTra VALUES(?,?,?,?,?,?)";
    	try (Connection con = ConnectDB.getConnection(); 
    	     PreparedStatement pst = con.prepareStatement(sql)) {
    	    
    	    // 1. Mã phiếu (String)
    	    pst.setString(1, pdt.getMaPhieuDoiTra());
    	    
    	    // 2. Ngày đổi trả (Phải convert sang java.sql.Date)
    	    pst.setDate(2, new java.sql.Date(pdt.getNgayDoiTra().getTime()));
    	    
    	    // 3. Lý do (String)
    	    pst.setString(3, pdt.getLyDo());
    	    
    	    // 4. Hình thức xử lý (Enum -> đổi sang String bằng .name())
    	    pst.setString(4, pdt.getHinhThucXuLy().name()); 
    	    
    	    // 5. Phí phạt (Double)
    	    pst.setDouble(5, pdt.getPhiPhat());
    	    
    	    // 6. Mã hóa đơn (Lấy từ đối tượng HoaDon bên trong)
    	    pst.setString(6, pdt.getHoaDon().getMaHoaDon());
    	    
    	    return pst.executeUpdate() > 0;
    	} catch (SQLException e) {
    	    e.printStackTrace();
    	    return false;
    	}
    }
}