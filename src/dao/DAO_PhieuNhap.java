package dao;

import connectDB.ConnectDB;
import entity.PhieuNhap;
import entity.NhaCungCap;
import entity.NhanVien;
import java.sql.*;
import java.util.ArrayList;

public class DAO_PhieuNhap {
    public ArrayList<PhieuNhap> getAllPhieuNhap() {
        ArrayList<PhieuNhap> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            
          
            String sql = "SELECT p.*, ncc.tenNhaCungCap, nv.hoTen, " +
                         "(SELECT SUM(soLuong * donGiaNhap) FROM ChiTietPhieuNhap WHERE maPhieuNhap = p.maPhieuNhap) as TongTien " +
                         "FROM PhieuNhap p " +
                         "JOIN NhaCungCap ncc ON p.maNhaCungCap = ncc.maNhaCungCap " +
                         "JOIN NhanVien nv ON p.maNhanVien = nv.maNhanVien";
            
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                // Khởi tạo đối tượng phụ
                NhaCungCap ncc = new NhaCungCap();
                ncc.setMaNhaCungCap(rs.getString("maNhaCungCap"));
                ncc.setTenNhaCungCap(rs.getString("tenNhaCungCap"));
                
                NhanVien nv = new NhanVien();
                nv.setMaNhanVien(rs.getString("maNhanVien"));
                nv.setHoTen(rs.getString("hoTen"));
                
                // Khởi tạo PhieuNhap
                PhieuNhap pn = new PhieuNhap();
                pn.setMaPhieuNhap(rs.getString("maPhieuNhap"));
                pn.setNgayNhap(rs.getDate("ngayNhap"));
                pn.setNhaCungCap(ncc);
                pn.setNhanVien(nv);
                
                // Lưu tạm tổng tiền vào một thuộc tính phụ hoặc xử lý hiển thị
                // Giả sử trong Entity PhieuNhap bạn có field tongTien (double)
                // pn.setTongTien(rs.getDouble("TongTien")); 
                
                // Vì DB bạn không có cột TrangThai, ta giả lập logic hoặc mặc định
                // pn.setTrangThai("Đã thanh toán"); 

                list.add(pn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    public boolean capNhatTrangThai(String maPhieu, String trangThaiMoi) {
        String sql = "UPDATE PhieuNhap SET trangThai = ? WHERE maPhieuNhap = ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, trangThaiMoi);
            pst.setString(2, maPhieu);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean xoaPhieuNhap(String maPhieuNhap) {
        Connection con = ConnectDB.getInstance().getConnection();
        PreparedStatement pstChiTiet = null;
        PreparedStatement pstPhieu = null;

        try {
            // 1. Tắt chế độ AutoCommit để bắt đầu Transaction
            con.setAutoCommit(false);

            // 2. Xóa các dòng con trong bảng ChiTietPhieuNhap trước (Ràng buộc khóa ngoại)
            String sqlChiTiet = "DELETE FROM ChiTietPhieuNhap WHERE maPhieuNhap = ?";
            pstChiTiet = con.prepareStatement(sqlChiTiet);
            pstChiTiet.setString(1, maPhieuNhap);
            pstChiTiet.executeUpdate();

            // 3. Xóa dòng cha trong bảng PhieuNhap
            String sqlPhieu = "DELETE FROM PhieuNhap WHERE maPhieuNhap = ?";
            pstPhieu = con.prepareStatement(sqlPhieu);
            pstPhieu.setString(1, maPhieuNhap);
            int rowsAffected = pstPhieu.executeUpdate();

            // 4. Nếu mọi thứ ổn, xác nhận thay đổi vào DB
            con.commit();
            return rowsAffected > 0;

        } catch (SQLException e) {
            // 5. Nếu có bất kỳ lỗi nào, khôi phục lại dữ liệu ban đầu
            try {
                if (con != null) con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            // 6. Luôn mở lại AutoCommit và đóng Statement
            try {
                if (con != null) con.setAutoCommit(true);
                if (pstChiTiet != null) pstChiTiet.close();
                if (pstPhieu != null) pstPhieu.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}