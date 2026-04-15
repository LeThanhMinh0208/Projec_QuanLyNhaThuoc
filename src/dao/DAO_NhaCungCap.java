package dao;

import connectDB.ConnectDB;
import entity.NhaCungCap;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAO_NhaCungCap {

    // ===================================================================
    // LOAD NHÀ CUNG CẤP (chỉ active, trangThai = 1)
    // ===================================================================

    /**
     * Lấy NCC đang hoạt động, chỉ mã + tên.
     * Dùng cho dropdown chọn NCC khi tạo đơn đặt hàng.
     */
	public List<NhaCungCap> getAllNhaCungCap() {
        List<NhaCungCap> list = new ArrayList<>();
        String sql = "SELECT maNhaCungCap, tenNhaCungCap FROM NhaCungCap WHERE trangThai = 1";
        try (Connection con = ConnectDB.getInstance().getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                NhaCungCap ncc = new NhaCungCap();
                ncc.setMaNhaCungCap(rs.getString("maNhaCungCap"));
                ncc.setTenNhaCungCap(rs.getString("tenNhaCungCap"));
                ncc.setTrangThai(true);
                list.add(ncc);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Lấy full thông tin NCC đang hoạt động.
     * Dùng cho trang Danh Mục NCC.
     */
    public List<NhaCungCap> getAllNhaCungCapFull() {
        List<NhaCungCap> list = new ArrayList<>();
        String sql = "SELECT maNhaCungCap, tenNhaCungCap, sdt, diaChi, congNo, trangThai FROM NhaCungCap WHERE trangThai = 1";
        try (Connection con = ConnectDB.getConnection(); 
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                NhaCungCap ncc = new NhaCungCap();
                ncc.setMaNhaCungCap(rs.getString("maNhaCungCap"));
                ncc.setTenNhaCungCap(rs.getString("tenNhaCungCap"));
                ncc.setSdt(rs.getString("sdt"));
                ncc.setDiaChi(rs.getString("diaChi"));
                ncc.setCongNo(rs.getDouble("congNo"));
                ncc.setTrangThai(rs.getBoolean("trangThai"));
                list.add(ncc);
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        return list;
    }

    // ===================================================================
    // TÌM KIẾM / KIỂM TRA
    // ===================================================================

    public NhaCungCap getNhaCungCapByMa(String maNCC) {
        NhaCungCap ncc = null;
        String sql = "SELECT * FROM NhaCungCap WHERE maNhaCungCap = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maNCC);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    ncc = new NhaCungCap();
                    ncc.setMaNhaCungCap(rs.getString("maNhaCungCap"));
                    ncc.setTenNhaCungCap(rs.getString("tenNhaCungCap"));
                    ncc.setSdt(rs.getString("sdt"));
                    ncc.setDiaChi(rs.getString("diaChi"));
                    ncc.setCongNo(rs.getDouble("congNo"));
                    ncc.setTrangThai(rs.getBoolean("trangThai"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ncc;
    }

    public boolean existsByTenNhaCungCap(String ten) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = ConnectDB.getConnection();
            String sql = "SELECT 1 FROM NhaCungCap WHERE LOWER(tenNhaCungCap) = ? AND trangThai = 1";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, ten.toLowerCase());
            rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // ===================================================================
    // THÊM / CẬP NHẬT (Thêm mới có auto-restore nếu trùng tên+SĐT đã vô hiệu)
    // ===================================================================

    public String getMaNhaCungCapMoi() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        String maMoi = "NCC001";
        try {
            con = ConnectDB.getConnection();
            String sql = "SELECT maNhaCungCap FROM NhaCungCap ORDER BY maNhaCungCap DESC";
            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                String maCu = rs.getString("maNhaCungCap");
                int soCu = Integer.parseInt(maCu.substring(3));
                int soMoi = soCu + 1;
                maMoi = "NCC" + String.format("%03d", soMoi);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return maMoi;
    }

    /**
     * Thêm NCC — Auto-restore nếu tìm thấy bản ghi vô hiệu trùng tên + SĐT.
     * Giữ nguyên công nợ cũ.
     */
    public boolean themNhaCungCap(NhaCungCap ncc) {
        Connection con = ConnectDB.getConnection();

        // Bước 1: Tìm bản ghi đã vô hiệu trùng tên + SĐT
        String sqlCheck = "SELECT maNhaCungCap FROM NhaCungCap " +
                           "WHERE LOWER(tenNhaCungCap) = LOWER(?) AND sdt = ? AND trangThai = 0";
        try (PreparedStatement pstCheck = con.prepareStatement(sqlCheck)) {
            pstCheck.setString(1, ncc.getTenNhaCungCap());
            pstCheck.setString(2, ncc.getSdt());
            ResultSet rs = pstCheck.executeQuery();
            if (rs.next()) {
                // Tìm thấy → RESTORE: cập nhật địa chỉ, giữ công nợ cũ
                String maCu = rs.getString("maNhaCungCap");
                String sqlRestore = "UPDATE NhaCungCap SET trangThai = 1, diaChi = ? WHERE maNhaCungCap = ?";
                try (PreparedStatement pstRestore = con.prepareStatement(sqlRestore)) {
                    pstRestore.setString(1, ncc.getDiaChi());
                    pstRestore.setString(2, maCu);
                    return pstRestore.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Bước 2: INSERT bình thường
        PreparedStatement pstmt = null;
        try {
            String sql = "INSERT INTO NhaCungCap (maNhaCungCap, tenNhaCungCap, sdt, diaChi, congNo, trangThai) VALUES (?, ?, ?, ?, ?, 1)";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, ncc.getMaNhaCungCap());
            pstmt.setString(2, ncc.getTenNhaCungCap());
            pstmt.setString(3, ncc.getSdt());
            pstmt.setString(4, ncc.getDiaChi());
            pstmt.setDouble(5, ncc.getCongNo());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public boolean capNhatNhaCungCap(NhaCungCap ncc) {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = ConnectDB.getConnection();
            String sql = "UPDATE NhaCungCap SET tenNhaCungCap = ?, sdt = ?, diaChi = ? WHERE maNhaCungCap = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, ncc.getTenNhaCungCap());
            pstmt.setString(2, ncc.getSdt());
            pstmt.setString(3, ncc.getDiaChi());
            pstmt.setString(4, ncc.getMaNhaCungCap());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public boolean congCongNoNhaCungCap(String maNCC, double soTienCongThem) {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = ConnectDB.getConnection();
            String sql = "UPDATE NhaCungCap SET congNo = congNo + ? WHERE maNhaCungCap = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setDouble(1, soTienCongThem);
            pstmt.setString(2, maNCC);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // ===================================================================
    // XÓA NHÀ CUNG CẤP — Tự động check → hard hoặc soft delete ngầm
    // ===================================================================

    /**
     * Xóa NCC — backend tự quyết hard/soft delete.
     * User chỉ thấy "Đã xóa thành công."
     */
    public boolean xoaNhaCungCap(String maNhaCungCap) {
        int tongLienQuan = demDuLieuLienQuan(maNhaCungCap);
        if (tongLienQuan <= 0) {
            // Hard delete
            PreparedStatement pstmt = null;
            try {
                Connection con = ConnectDB.getConnection();
                pstmt = con.prepareStatement("DELETE FROM NhaCungCap WHERE maNhaCungCap = ?");
                pstmt.setString(1, maNhaCungCap);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            } finally {
                try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        } else {
            // Soft delete
            String sql = "UPDATE NhaCungCap SET trangThai = 0 WHERE maNhaCungCap = ?";
            try (Connection con = ConnectDB.getConnection();
                 PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, maNhaCungCap);
                return pst.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private int demDuLieuLienQuan(String maNCC) {
        String sql = "SELECT " +
                     "  (SELECT COUNT(*) FROM DonDatHang WHERE maNhaCungCap = ?) + " +
                     "  (SELECT COUNT(*) FROM PhieuNhap   WHERE maNhaCungCap = ?) + " +
                     "  (SELECT COUNT(*) FROM PhieuChi    WHERE maNhaCungCap = ?) " +
                     "AS tongLienQuan";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maNCC);
            pst.setString(2, maNCC);
            pst.setString(3, maNCC);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt("tongLienQuan");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}