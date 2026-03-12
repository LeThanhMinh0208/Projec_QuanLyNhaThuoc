package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import connectDB.ConnectDB;
import entity.Thuoc;

public class DAO_Thuoc {

    /**
     * Lấy toàn bộ danh sách thuốc từ cơ sở dữ liệu
     * Bao gồm cột trieuChung mới thêm
     */
	public ArrayList<Thuoc> getAllThuoc() {
	    ArrayList<Thuoc> list = new ArrayList<>();
	    
	    // Sử dụng câu lệnh tường minh: t.* (tất cả cột bảng Thuốc) và d.tenDanhMuc
	    String sql = "SELECT t.*, d.tenDanhMuc AS tenDanhMucHienThi " +
	                 "FROM Thuoc t " +
	                 "LEFT JOIN DanhMucThuoc d ON t.maDanhMuc = d.maDanhMuc";

	    Connection con = ConnectDB.getConnection();
	    try {
	        try (PreparedStatement pst = con.prepareStatement(sql);
	             ResultSet rs = pst.executeQuery()) {
	            
	            while (rs.next()) {
	                Thuoc t = new Thuoc(
	                    rs.getString("maThuoc"),
	                    rs.getString("maDanhMuc"),
	                    rs.getString("tenThuoc"),
	                    rs.getString("hoatChat"),
	                    rs.getString("hamLuong"),
	                    rs.getString("hangSanXuat"),
	                    rs.getString("nuocSanXuat"),
	                    rs.getString("congDung"),
	                    rs.getString("donViCoBan"),
	                    rs.getString("hinhAnh"),
	                    rs.getBoolean("canKeDon"),
	                    rs.getString("trangThai")
	                );
	                
	                // Lấy theo Alias "tenDanhMucHienThi" để chắc chắn không bị lỗi tên cột
	                t.setTenDanhMuc(rs.getString("tenDanhMucHienThi"));
	                
	                // Cột trieuChung (kiểm tra xem trong SQL của bạn có cột này ở bảng Thuoc không)
	                try {
	                    t.setTrieuChung(rs.getString("trieuChung"));
	                } catch (Exception e) {
	                    // Nếu chưa có cột này trong DB thì bỏ qua để không sập chương trình
	                }
	                
	                list.add(t);
	            }
	        }
	    } catch (SQLException e) {
	        System.err.println("Lỗi SQL khi lấy danh sách thuốc: " + e.getMessage());
	    }
	    return list;
	}

    /**
     * Tìm kiếm thuốc theo mã
     */
    public Thuoc getThuocByMa(String ma) {
        String sql = "SELECT * FROM Thuoc WHERE maThuoc = ?";
        Connection con = ConnectDB.getConnection(); // Lấy Connection ra ngoài

        try {
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, ma);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        Thuoc t = new Thuoc(
                            rs.getString("maThuoc"),
                            rs.getString("maDanhMuc"),
                            rs.getString("tenThuoc"),
                            rs.getString("hoatChat"),
                            rs.getString("hamLuong"),
                            rs.getString("hangSanXuat"),
                            rs.getString("nuocSanXuat"),
                            rs.getString("congDung"),
                            rs.getString("donViCoBan"),
                            rs.getString("hinhAnh"),
                            rs.getBoolean("canKeDon"),
                            rs.getString("trangThai")
                        );
                        t.setTrieuChung(rs.getString("trieuChung"));
                        return t;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean capNhatThuoc(Thuoc t) {
        return true; 
    }
    
    public boolean themThuoc(Thuoc t) { return true; }
    public boolean xoaThuoc(String maThuoc) { return true; }
}