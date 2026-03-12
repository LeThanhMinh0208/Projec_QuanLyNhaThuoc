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
        String sql = "UPDATE Thuoc SET maDanhMuc=?, tenThuoc=?, hoatChat=?, hamLuong=?, " +
                     "hangSanXuat=?, nuocSanXuat=?, congDung=?, donViCoBan=?, hinhAnh=?, " +
                     "canKeDon=?, trangThai=?, trieuChung=? WHERE maThuoc=?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            
            pst.setString(1, t.getMaDanhMuc());
            pst.setString(2, t.getTenThuoc());
            pst.setString(3, t.getHoatChat());
            pst.setString(4, t.getHamLuong());
            pst.setString(5, t.getHangSanXuat());
            pst.setString(6, t.getNuocSanXuat());
            pst.setString(7, t.getCongDung());
            pst.setString(8, t.getDonViCoBan());
            pst.setString(9, t.getHinhAnh());
            pst.setBoolean(10, t.isCanKeDon());
            pst.setString(11, t.getTrangThai());
            pst.setString(12, t.getTrieuChung());
            pst.setString(13, t.getMaThuoc()); // Dùng maThuoc để định vị dòng cần sửa

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public String getMaThuocMoi() {
        String maMoi = "TH001";
        String sql = "SELECT MAX(maThuoc) FROM Thuoc";
        Connection con = ConnectDB.getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next() && rs.getString(1) != null) {
                String maHienTai = rs.getString(1);
                int soHienTai = Integer.parseInt(maHienTai.substring(2)); // Cắt chữ "TH"
                maMoi = String.format("TH%03d", soHienTai + 1); // Tăng 1 và định dạng 3 chữ số
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return maMoi;
    }

    // 2. Hàm thêm thuốc vào CSDL
    public boolean themThuoc(Thuoc t) {
        Connection con = ConnectDB.getConnection();
        String sql = "INSERT INTO Thuoc (maThuoc, maDanhMuc, tenThuoc, hoatChat, hamLuong, " +
                     "hangSanXuat, nuocSanXuat, congDung, donViCoBan, hinhAnh, canKeDon, trangThai, trieuChung) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, t.getMaThuoc());
            pst.setString(2, t.getMaDanhMuc()); // Lưu Mã danh mục (ví dụ: DM001)
            pst.setString(3, t.getTenThuoc());
            pst.setString(4, t.getHoatChat());
            pst.setString(5, t.getHamLuong());
            pst.setString(6, t.getHangSanXuat());
            pst.setString(7, t.getNuocSanXuat());
            pst.setString(8, t.getCongDung());
            pst.setString(9, t.getDonViCoBan());
            pst.setString(10, t.getHinhAnh());
            pst.setBoolean(11, t.isCanKeDon());
            pst.setString(12, t.getTrangThai());
            pst.setString(13, t.getTrieuChung());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    public boolean xoaThuoc(String ma) {
        Connection con = ConnectDB.getConnection();
        String sql = "DELETE FROM Thuoc WHERE maThuoc = ?";
        
        
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, ma);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}