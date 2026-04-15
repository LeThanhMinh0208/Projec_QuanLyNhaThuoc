package dao;

import connectDB.ConnectDB;
import entity.KhachHang;
import java.sql.*;
import java.util.ArrayList;

public class DAO_KhachHang {

    // ===================================================================
    // LOAD KHÁCH HÀNG (chỉ active, trangThai = 1)
    // ===================================================================

    public ArrayList<KhachHang> getAllKhachHang() {
        ArrayList<KhachHang> ds = new ArrayList<>();
        String sql = "SELECT * FROM KhachHang WHERE trangThai = 1";
        try (Connection con = ConnectDB.getConnection(); 
             Statement statement = con.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                ds.add(new KhachHang(
                    rs.getString("maKhachHang"),
                    rs.getString("hoTen"),
                    rs.getString("sdt"),
                    rs.getString("diaChi"),
                    rs.getInt("diemTichLuy"),
                    rs.getBoolean("trangThai")
                ));
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return ds;
    }

    // ===================================================================
    // THÊM / CẬP NHẬT (Thêm mới có auto-restore nếu trùng SĐT đã vô hiệu)
    // ===================================================================

    /**
     * Thêm khách hàng — Auto-restore nếu tìm thấy bản ghi vô hiệu trùng SĐT.
     * Giữ nguyên điểm tích lũy cũ.
     */
    public boolean themKhachHang(KhachHang kh) {
        Connection con = ConnectDB.getConnection();

        // Bước 1: Tìm bản ghi đã vô hiệu trùng SĐT
        String sqlCheck = "SELECT maKhachHang FROM KhachHang WHERE sdt = ? AND trangThai = 0";
        try (PreparedStatement pstCheck = con.prepareStatement(sqlCheck)) {
            pstCheck.setString(1, kh.getSdt());
            ResultSet rs = pstCheck.executeQuery();
            if (rs.next()) {
                // Tìm thấy → RESTORE: cập nhật tên/địa chỉ, giữ điểm cũ
                String maCu = rs.getString("maKhachHang");
                String sqlRestore = "UPDATE KhachHang SET trangThai = 1, hoTen = ?, diaChi = ? WHERE maKhachHang = ?";
                try (PreparedStatement pstRestore = con.prepareStatement(sqlRestore)) {
                    pstRestore.setString(1, kh.getHoTen());
                    pstRestore.setString(2, kh.getDiaChi());
                    pstRestore.setString(3, maCu);
                    return pstRestore.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Bước 2: Không tìm thấy trùng → INSERT bình thường
        String sql = "INSERT INTO KhachHang (maKhachHang, hoTen, sdt, diaChi, diemTichLuy, trangThai) VALUES(?,?,?,?,?,1)";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, kh.getMaKhachHang());
            pst.setString(2, kh.getHoTen());
            pst.setString(3, kh.getSdt());
            pst.setString(4, kh.getDiaChi());
            pst.setInt(5, kh.getDiemTichLuy());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    /**
     * Tìm khách hàng theo SĐT — chỉ tìm KH đang hoạt động.
     */
    public KhachHang getBySdt(String sdt) {
        String sql = "SELECT * FROM KhachHang WHERE sdt = ? AND trangThai = 1";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, sdt);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new KhachHang(
                        rs.getString("maKhachHang"),
                        rs.getString("hoTen"),
                        rs.getString("sdt"),
                        rs.getString("diaChi"),
                        rs.getInt("diemTichLuy"),
                        rs.getBoolean("trangThai")
                );
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean capNhatDiemTichLuy(String maKhachHang, int diemMoi) {
        String sql = "UPDATE KhachHang SET diemTichLuy = ? WHERE maKhachHang = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, diemMoi);
            pst.setString(2, maKhachHang);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public boolean capNhatKhachHang(KhachHang kh) {
        String sql = "UPDATE KhachHang SET hoTen=?, sdt=?, diaChi=?, diemTichLuy=? WHERE maKhachHang=?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, kh.getHoTen());
            pst.setString(2, kh.getSdt());
            pst.setString(3, kh.getDiaChi());
            pst.setInt(4, kh.getDiemTichLuy());
            pst.setString(5, kh.getMaKhachHang());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    // ===================================================================
    // XÓA KHÁCH HÀNG — Tự động check → hard hoặc soft delete ngầm
    // ===================================================================

    /**
     * Xóa khách hàng — backend tự quyết hard/soft delete.
     * User chỉ thấy "Đã xóa thành công."
     */
    public boolean xoaKhachHang(String maKhachHang) {
        int soHoaDon = demHoaDonLienQuan(maKhachHang);
        if (soHoaDon <= 0) {
            // Hard delete
            String sql = "DELETE FROM KhachHang WHERE maKhachHang = ?";
            try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, maKhachHang);
                return pst.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            // Soft delete
            String sql = "UPDATE KhachHang SET trangThai = 0 WHERE maKhachHang = ?";
            try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, maKhachHang);
                return pst.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private int demHoaDonLienQuan(String maKhachHang) {
        String sql = "SELECT COUNT(*) FROM HoaDon WHERE maKhachHang = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maKhachHang);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}