package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import connectDB.ConnectDB;
import entity.Thuoc;

public class DAO_Thuoc {

    // ===================================================================
    // LOAD THUỐC
    // ===================================================================

    /**
     * Lấy danh sách thuốc đang hoạt động (DANG_BAN + HET_HANG).
     * Dùng cho danh mục thuốc. NGUNG_BAN ẩn hoàn toàn.
     */
    public ArrayList<Thuoc> getAllThuoc() {
        ArrayList<Thuoc> list = new ArrayList<>();
        String sql = "SELECT t.*, d.tenDanhMuc AS tenDanhMucHienThi " +
                     "FROM Thuoc t " +
                     "LEFT JOIN DanhMucThuoc d ON t.maDanhMuc = d.maDanhMuc " +
                     "WHERE t.trangThai IN ('DANG_BAN', 'HET_HANG')";
        Connection con = ConnectDB.getConnection();
        try {
            try (PreparedStatement pst = con.prepareStatement(sql);
                 ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(mapThuoc(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy danh sách thuốc: " + e.getMessage());
        }
        return list;
    }

    /**
     * Lấy TẤT CẢ thuốc kể cả NGUNG_BAN.
     * Dùng cho form Tạo Đơn Đặt Hàng (nhập hàng từ NCC — cho phép đặt
     * cả thuốc đang ngưng bán để khi kích hoạt lại sẽ có hàng sẵn).
     */
    public ArrayList<Thuoc> getAllThuocTatCa() {
        ArrayList<Thuoc> list = new ArrayList<>();
        String sql = "SELECT t.*, d.tenDanhMuc AS tenDanhMucHienThi " +
                     "FROM Thuoc t " +
                     "LEFT JOIN DanhMucThuoc d ON t.maDanhMuc = d.maDanhMuc";
        Connection con = ConnectDB.getConnection();
        try {
            try (PreparedStatement pst = con.prepareStatement(sql);
                 ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(mapThuoc(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy tất cả thuốc: " + e.getMessage());
        }
        return list;
    }

    /**
     * Lấy chỉ thuốc DANG_BAN.
     * Dùng cho form lập hóa đơn (bán hàng), chọn thuốc kê đơn.
     */
    public ArrayList<Thuoc> getAllThuocDangBan() {
        ArrayList<Thuoc> list = new ArrayList<>();
        String sql = "SELECT t.*, d.tenDanhMuc AS tenDanhMucHienThi " +
                     "FROM Thuoc t " +
                     "LEFT JOIN DanhMucThuoc d ON t.maDanhMuc = d.maDanhMuc " +
                     "WHERE t.trangThai = 'DANG_BAN'";
        Connection con = ConnectDB.getConnection();
        try {
            try (PreparedStatement pst = con.prepareStatement(sql);
                 ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(mapThuoc(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy thuốc đang bán: " + e.getMessage());
        }
        return list;
    }

    /** Helper map ResultSet → Thuoc */
    private Thuoc mapThuoc(ResultSet rs) throws SQLException {
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
        t.setTenDanhMuc(rs.getString("tenDanhMucHienThi"));
        try { t.setTrieuChung(rs.getString("trieuChung")); } catch (Exception e) { }
        return t;
    }

    // ===================================================================
    // TÌM KIẾM
    // ===================================================================

    public Thuoc getThuocByMa(String ma) {
        String sql = "SELECT * FROM Thuoc WHERE maThuoc = ?";
        Connection con = ConnectDB.getConnection();
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
        String maMoi = "T00001"; // 1. Đổi giá trị khởi tạo thành T + 5 số 0
        String sql = "SELECT MAX(maThuoc) FROM Thuoc WHERE maThuoc LIKE 'T%'"; // Thêm điều kiện LIKE 'T%' cho chắc cú
        Connection con = ConnectDB.getConnection();
        
        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
             
            if (rs.next() && rs.getString(1) != null) {
                String maHienTai = rs.getString(1);
                
                // 2. Cắt đúng 1 ký tự "T" ở đầu (index 1)
                int soHienTai = Integer.parseInt(maHienTai.substring(1)); 
                
                // 3. Tăng lên 1 đơn vị và ép format về dạng T + 5 chữ số
                maMoi = String.format("T%05d", soHienTai + 1); 
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return maMoi;
    }

    // 2. Hàm thêm thuốc vào CSDL
    public boolean themThuoc(Thuoc t) {
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false); // BẬT TRANSACTION (Lưu 2 bảng cùng lúc)

            // ========================================================
            // 1. THÊM VÀO BẢNG THUỐC
            // ========================================================
            String sqlThuoc = "INSERT INTO Thuoc (maThuoc, maDanhMuc, tenThuoc, hoatChat, hamLuong, " +
                         "hangSanXuat, nuocSanXuat, congDung, donViCoBan, hinhAnh, canKeDon, trangThai, trieuChung) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstThuoc = con.prepareStatement(sqlThuoc)) {
                pstThuoc.setString(1, t.getMaThuoc());
                pstThuoc.setString(2, t.getMaDanhMuc());
                pstThuoc.setString(3, t.getTenThuoc());
                pstThuoc.setString(4, t.getHoatChat());
                pstThuoc.setString(5, t.getHamLuong());
                pstThuoc.setString(6, t.getHangSanXuat());
                pstThuoc.setString(7, t.getNuocSanXuat());
                pstThuoc.setString(8, t.getCongDung());
                pstThuoc.setString(9, t.getDonViCoBan()); 
                pstThuoc.setString(10, t.getHinhAnh());
                pstThuoc.setBoolean(11, t.isCanKeDon());
                pstThuoc.setString(12, t.getTrangThai());
                pstThuoc.setString(13, t.getTrieuChung());
                pstThuoc.executeUpdate();
            }

            String maQuyDoiMoi = "QD" + t.getMaThuoc().substring(1);

            String sqlDonVi = "INSERT INTO DonViQuyDoi (maQuyDoi, maThuoc, tenDonVi, tyLeQuyDoi) VALUES (?, ?, ?, 1)";
            try (PreparedStatement pstDonVi = con.prepareStatement(sqlDonVi)) {
                pstDonVi.setString(1, maQuyDoiMoi);
                pstDonVi.setString(2, t.getMaThuoc());
                pstDonVi.setString(3, t.getDonViCoBan()); // Lấy đơn vị cơ bản (Viên/Hộp/Chai) làm gốc
                pstDonVi.executeUpdate();
            }

            con.commit(); 
            return true;
            
        } catch (SQLException e) { 
            try { if (con != null) con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace(); 
            return false; 
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
        }
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

    public Thuoc getThuocTheoMaHoacTen(String query) {
        Thuoc thuoc = null;
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT TOP 1 * FROM Thuoc WHERE maThuoc = ? OR tenThuoc LIKE ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, query);
            stmt.setString(2, "%" + query + "%");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                thuoc = new Thuoc();
                thuoc.setMaThuoc(rs.getString("maThuoc"));
                thuoc.setTenThuoc(rs.getString("tenThuoc"));
                thuoc.setDonViCoBan(rs.getString("donViCoBan"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return thuoc;
    }

    public ArrayList<Thuoc> getDanhSachGoiY(String query) {
        ArrayList<Thuoc> list = new ArrayList<>();
        String sql = "SELECT TOP 10 maThuoc, tenThuoc FROM Thuoc WHERE maThuoc LIKE ? OR tenThuoc LIKE ?";
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, "%" + query + "%");
            stmt.setString(2, "%" + query + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Thuoc t = new Thuoc();
                t.setMaThuoc(rs.getString("maThuoc"));
                t.setTenThuoc(rs.getString("tenThuoc"));
                list.add(t);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public boolean existsByTenThuoc(String ten) {
        Connection con = ConnectDB.getConnection();
        String sql = "SELECT 1 FROM Thuoc WHERE LOWER(tenThuoc) = ? AND trangThai IN ('DANG_BAN', 'HET_HANG')";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, ten.toLowerCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ===================================================================
    // THÊM / CẬP NHẬT (Thêm mới có auto-restore nếu trùng NGUNG_BAN)
    // ===================================================================

    public String getMaThuocMoi() {
        String maMoi = "TH001";
        String sql = "SELECT MAX(maThuoc) FROM Thuoc";
        Connection con = ConnectDB.getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next() && rs.getString(1) != null) {
                String maHienTai = rs.getString(1);
                int soHienTai = Integer.parseInt(maHienTai.substring(2));
                maMoi = String.format("TH%03d", soHienTai + 1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return maMoi;
    }

    /**
     * Thêm thuốc mới — Auto-restore nếu tìm thấy bản ghi NGUNG_BAN trùng
     * (cùng tenThuoc + hoatChat + hamLuong).
     * @return true nếu thành công (INSERT mới hoặc RESTORE cũ)
     */
    public boolean themThuoc(Thuoc t) {
        Connection con = ConnectDB.getConnection();

        // Bước 1: Tìm bản ghi NGUNG_BAN trùng (cùng tên + hoạt chất + hàm lượng)
        String sqlCheck = "SELECT maThuoc FROM Thuoc " +
                           "WHERE LOWER(tenThuoc) = LOWER(?) " +
                           "AND LOWER(hoatChat) = LOWER(?) " +
                           "AND LOWER(hamLuong) = LOWER(?) " +
                           "AND trangThai = 'NGUNG_BAN'";
        try (PreparedStatement pstCheck = con.prepareStatement(sqlCheck)) {
            pstCheck.setString(1, t.getTenThuoc());
            pstCheck.setString(2, t.getHoatChat());
            pstCheck.setString(3, t.getHamLuong());
            ResultSet rs = pstCheck.executeQuery();
            if (rs.next()) {
                // Tìm thấy bản ghi cũ → RESTORE thay vì INSERT
                String maCu = rs.getString("maThuoc");
                String sqlRestore = "UPDATE Thuoc SET trangThai = 'DANG_BAN', " +
                                    "maDanhMuc = ?, hangSanXuat = ?, nuocSanXuat = ?, " +
                                    "congDung = ?, trieuChung = ?, hinhAnh = ?, canKeDon = ? " +
                                    "WHERE maThuoc = ?";
                try (PreparedStatement pstRestore = con.prepareStatement(sqlRestore)) {
                    pstRestore.setString(1, t.getMaDanhMuc());
                    pstRestore.setString(2, t.getHangSanXuat());
                    pstRestore.setString(3, t.getNuocSanXuat());
                    pstRestore.setString(4, t.getCongDung());
                    pstRestore.setString(5, t.getTrieuChung());
                    pstRestore.setString(6, t.getHinhAnh());
                    pstRestore.setBoolean(7, t.isCanKeDon());
                    pstRestore.setString(8, maCu);
                    return pstRestore.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Bước 2: Không tìm thấy trùng → INSERT bình thường
        String sql = "INSERT INTO Thuoc (maThuoc, maDanhMuc, tenThuoc, hoatChat, hamLuong, " +
                     "hangSanXuat, nuocSanXuat, congDung, donViCoBan, hinhAnh, canKeDon, trangThai, trieuChung) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, t.getMaThuoc());
            pst.setString(2, t.getMaDanhMuc());
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
            pst.setString(13, t.getMaThuoc());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean capNhatGiaNhapMoi(String maThuoc, double giaMoi) {
        int n = 0;
        String sql = "UPDATE Thuoc SET GiaNhap = ? WHERE MaThuoc = ?";
        try (java.sql.Connection con = connectDB.ConnectDB.getInstance().getConnection();
             java.sql.PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setDouble(1, giaMoi);
            stmt.setString(2, maThuoc);
            n = stmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            System.err.println("❌ Lỗi khi cập nhật giá nhập mới cho thuốc: " + e.getMessage());
        }
        return n > 0;
    }

    public double getGiaNhapGanNhat(String maThuoc) {
        double giaNhap = 0.0;
        String sql = "SELECT TOP 1 giaNhap FROM LoThuoc WHERE maThuoc = ? ORDER BY maLoThuoc DESC";
        try (java.sql.Connection con = connectDB.ConnectDB.getInstance().getConnection();
             java.sql.PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maThuoc);
            java.sql.ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                giaNhap = rs.getDouble("giaNhap");
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy Giá Nhập gần nhất từ Lô Thuốc: " + e.getMessage());
        }
        return giaNhap;
    }

    // ===================================================================
    // XÓA THUỐC — Tự động check → hard hoặc soft delete ngầm
    // ===================================================================

    /**
     * Xóa thuốc — backend tự quyết hard/soft delete.
     * User chỉ thấy "Đã xóa thành công."
     * @return true nếu xóa thành công
     */
    public boolean xoaThuoc(String maThuoc) {
        boolean coDuLieu = kiemTraDuLieuLienQuan(maThuoc);
        if (!coDuLieu) {
            return hardDeleteThuoc(maThuoc);
        } else {
            return softDeleteThuoc(maThuoc);
        }
    }

    /**
     * Kiểm tra thuốc có dữ liệu liên quan trong các bảng chi tiết không.
     */
    private boolean kiemTraDuLieuLienQuan(String maThuoc) {
        String sql = "SELECT COUNT(*) FROM DonViQuyDoi dv " +
                     "WHERE dv.maThuoc = ? AND (" +
                     "  EXISTS (SELECT 1 FROM ChiTietBangGia   WHERE maQuyDoi = dv.maQuyDoi) " +
                     "  OR EXISTS (SELECT 1 FROM ChiTietHoaDon   WHERE maQuyDoi = dv.maQuyDoi) " +
                     "  OR EXISTS (SELECT 1 FROM ChiTietPhieuNhap WHERE maQuyDoi = dv.maQuyDoi) " +
                     "  OR EXISTS (SELECT 1 FROM ChiTietDonDatHang WHERE maQuyDoi = dv.maQuyDoi) " +
                     "  OR EXISTS (SELECT 1 FROM ChiTietDoiTra   WHERE maQuyDoi = dv.maQuyDoi) " +
                     ")";
        Connection con = ConnectDB.getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maThuoc);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true; // An toàn: nếu lỗi thì coi như có dữ liệu
    }

    private boolean hardDeleteThuoc(String maThuoc) {
        Connection con = ConnectDB.getConnection();
        try {
            con.setAutoCommit(false);
            try (PreparedStatement pst = con.prepareStatement("DELETE FROM LoThuoc WHERE maThuoc = ?")) {
                pst.setString(1, maThuoc); pst.executeUpdate();
            }
            try (PreparedStatement pst = con.prepareStatement("DELETE FROM DonViQuyDoi WHERE maThuoc = ?")) {
                pst.setString(1, maThuoc); pst.executeUpdate();
            }
            try (PreparedStatement pst = con.prepareStatement("DELETE FROM Thuoc WHERE maThuoc = ?")) {
                pst.setString(1, maThuoc); pst.executeUpdate();
            }
            con.commit();
            return true;
        } catch (SQLException e) {
            try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private boolean softDeleteThuoc(String maThuoc) {
        String sql = "UPDATE Thuoc SET trangThai = 'NGUNG_BAN' WHERE maThuoc = ?";
        Connection con = ConnectDB.getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maThuoc);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}