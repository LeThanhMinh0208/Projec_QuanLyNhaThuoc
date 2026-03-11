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
        // Câu lệnh SQL lấy tất cả các cột
        String sql = "SELECT * FROM Thuoc";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                // Khởi tạo đối tượng Thuoc và set giá trị từ ResultSet
                Thuoc t = new Thuoc(
                    rs.getString("maThuoc"),
                    rs.getString("maDanhMuc"),
                    rs.getString("tenThuoc"),
                    rs.getString("hoatChat"),
                    rs.getString("hamLuong"),
                    rs.getString("hangSanXuat"),
                    rs.getString("nuocSanXuat"),
                    rs.getString("congDungTrieuChung"),
                    rs.getString("donViCoBan"),
                    rs.getString("hinhAnh"),
                    rs.getBoolean("canKeDon"),
                    rs.getString("trangThai")
                );
                
                // Bổ sung cột trieuChung mới (Giả sử bạn đã thêm setter trong Entity)
                t.setTrieuChung(rs.getString("trieuChung"));
                
                list.add(t);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách thuốc: " + e.getMessage());
        }
        return list;
    }

    /**
     * Tìm kiếm thuốc theo mã (Hỗ trợ khi cần tìm nhanh 1 đối tượng)
     */
    public Thuoc getThuocByMa(String ma) {
        String sql = "SELECT * FROM Thuoc WHERE maThuoc = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            
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
                        rs.getString("congDungTrieuChung"),
                        rs.getString("donViCoBan"),
                        rs.getString("hinhAnh"),
                        rs.getBoolean("canKeDon"),
                        rs.getString("trangThai")
                    );
                    t.setTrieuChung(rs.getString("trieuChung"));
                    return t;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}