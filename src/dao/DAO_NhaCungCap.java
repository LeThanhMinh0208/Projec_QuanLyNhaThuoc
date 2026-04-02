package dao;

import connectDB.ConnectDB;
import entity.NhaCungCap;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAO_NhaCungCap {

	public List<NhaCungCap> getAllNhaCungCap() {
        List<NhaCungCap> list = new ArrayList<>();
        String sql = "SELECT maNhaCungCap, tenNhaCungCap FROM NhaCungCap";
        try (Connection con = ConnectDB.getInstance().getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                NhaCungCap ncc = new NhaCungCap();
                ncc.setMaNhaCungCap(rs.getString("maNhaCungCap"));
                ncc.setTenNhaCungCap(rs.getString("tenNhaCungCap"));
                list.add(ncc);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // Tự động sinh mã NCC mới (ví dụ: NCC001, NCC002, ...)
    public String getMaNhaCungCapMoi() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        String maMoi = "NCC001";  // Mặc định nếu bảng rỗng

        try {
            con = ConnectDB.getConnection();
            String sql = "SELECT maNhaCungCap FROM NhaCungCap ORDER BY maNhaCungCap DESC";
            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);

            if (rs.next()) {
                String maCu = rs.getString("maNhaCungCap");  // Ví dụ: NCC005
                // Lấy phần số: cắt từ vị trí 3 (sau "NCC")
                int soCu = Integer.parseInt(maCu.substring(3));
                int soMoi = soCu + 1;
                maMoi = "NCC" + String.format("%03d", soMoi);  // Định dạng 3 chữ số: NCC006
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

    // Thêm mới nhà cung cấp vào CSDL
    public boolean themNhaCungCap(NhaCungCap ncc) {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = ConnectDB.getConnection();
            String sql = "INSERT INTO NhaCungCap (maNhaCungCap, tenNhaCungCap, sdt, diaChi, congNo) VALUES (?, ?, ?, ?, ?)";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, ncc.getMaNhaCungCap());
            pstmt.setString(2, ncc.getTenNhaCungCap());
            pstmt.setString(3, ncc.getSdt());
            pstmt.setString(4, ncc.getDiaChi());
            pstmt.setDouble(5, ncc.getCongNo());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;  // true nếu insert thành công
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean xoaNhaCungCap(String maNhaCungCap) {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = ConnectDB.getConnection();
            String sql = "DELETE FROM NhaCungCap WHERE maNhaCungCap = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, maNhaCungCap);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public boolean capNhatNhaCungCap(NhaCungCap ncc) {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = ConnectDB.getConnection();
            String sql = "UPDATE NhaCungCap SET tenNhaCungCap = ?, sdt = ?, diaChi = ?, congNo = ? WHERE maNhaCungCap = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, ncc.getTenNhaCungCap());
            pstmt.setString(2, ncc.getSdt());
            pstmt.setString(3, ncc.getDiaChi());
            pstmt.setDouble(4, ncc.getCongNo());
            pstmt.setString(5, ncc.getMaNhaCungCap());

            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean existsByTenNhaCungCap(String ten) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = ConnectDB.getConnection();
            String sql = "SELECT 1 FROM NhaCungCap WHERE LOWER(tenNhaCungCap) = ?";
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
 // HÀM MỚI: Lấy đầy đủ tất cả thông tin để hiển thị cho trang Danh Mục
    public List<NhaCungCap> getAllNhaCungCapFull() {
        List<NhaCungCap> list = new ArrayList<>();
        // Câu lệnh SQL lấy full 5 cột
        String sql = "SELECT maNhaCungCap, tenNhaCungCap, sdt, diaChi, congNo FROM NhaCungCap";
        
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
                list.add(ncc);
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        return list;
    }
 // HÀM MỚI: Cộng dồn tiền vào công nợ hiện tại của Nhà Cung Cấp
    public boolean congCongNoNhaCungCap(String maNCC, double soTienCongThem) {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = ConnectDB.getConnection();
            // Lệnh SQL tự động lấy số cũ cộng số mới an toàn tuyệt đối
            String sql = "UPDATE NhaCungCap SET congNo = congNo + ? WHERE maNhaCungCap = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setDouble(1, soTienCongThem);
            pstmt.setString(2, maNCC);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}