package dao;

import connectDB.ConnectDB;
import entity.PhieuXuat;
import entity.ChiTietPhieuXuat;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAO_PhieuXuat {

    private Connection getConn() throws SQLException {
        Connection conn = ConnectDB.getConnection();
        if (conn == null || conn.isClosed()) {
            ConnectDB.getInstance().connect();
            conn = ConnectDB.getConnection();
        }
        return conn;
    }

    public ArrayList<PhieuXuat> getAllPhieuXuat() {
        ArrayList<PhieuXuat> ds = new ArrayList<>();
        String sql = "SELECT p.*, n.hoTen FROM PhieuXuat p JOIN NhanVien n ON p.maNhanVien = n.maNhanVien ORDER BY p.ngayXuat DESC";
        try (Connection conn = getConn();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PhieuXuat px = new PhieuXuat();
                px.setMaPhieuXuat(rs.getString("maPhieuXuat"));
                Timestamp ts = rs.getTimestamp("ngayXuat");
                if (ts != null)
                    px.setNgayXuat(ts.toLocalDateTime());
                px.setMaNhanVien(rs.getString("hoTen"));
                px.setLoaiPhieu(rs.getInt("loaiPhieu"));
                px.setKhoNhan(rs.getString("khoNhan"));
                px.setGhiChu(rs.getString("ghiChu"));

                // FIX: Thêm 2 dòng này để Dialog Xem Chi Tiết nhận diện được NCC
                px.setMaNhaCungCap(rs.getString("maNhaCungCap"));
                px.setTongTien(rs.getDouble("tongTien"));

                ds.add(px);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    // LOGIC ĐỔI HỘ KHẨU (CHUYỂN CẢ CỤC)
    public boolean chuyenKhoNoiBo(PhieuXuat px, List<ChiTietPhieuXuat> listCT, String khoNhan) {
        Connection conn = null;
        try {
            conn = getConn();
            conn.setAutoCommit(false);

            // 1. Lưu Header
            String sqlP = "INSERT INTO PhieuXuat (MaPhieuXuat, NgayXuat, MaNhanVien, LoaiPhieu, KhoNhan, GhiChu) VALUES (?, GETDATE(), ?, 1, ?, ?)";
            PreparedStatement psP = conn.prepareStatement(sqlP);
            psP.setString(1, px.getMaPhieuXuat());
            psP.setString(2, px.getMaNhanVien());
            psP.setString(3, khoNhan);
            psP.setString(4, px.getGhiChu());
            psP.executeUpdate();

            // 2. Lưu Chi Tiết
            String sqlCT = "INSERT INTO ChiTietPhieuXuat (MaPhieuXuat, MaThuoc, SoLo, SoLuong, DonGia, ThanhTien) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement psCT = conn.prepareStatement(sqlCT);

            // 3. Đổi vị trí kho
            String sqlUpdateKho = "UPDATE LoThuoc SET viTriKho = ? WHERE maLoThuoc = ?";
            PreparedStatement psUpdateKho = conn.prepareStatement(sqlUpdateKho);

            for (ChiTietPhieuXuat ct : listCT) {
                psCT.setString(1, px.getMaPhieuXuat());
                psCT.setString(2, ct.getMaThuoc());
                psCT.setString(3, ct.getSoLo());
                psCT.setInt(4, ct.getSoLuong()); // Số lượng này chỉ để hiện lên bảng "Xem"
                psCT.setDouble(5, ct.getDonGia());
                psCT.setDouble(6, ct.getThanhTien());
                psCT.addBatch();

                psUpdateKho.setString(1, khoNhan);
                psUpdateKho.setString(2, ct.getSoLo());
                psUpdateKho.addBatch();
            }

            psCT.executeBatch();
            psUpdateKho.executeBatch();

            conn.commit();
            return true;
        } catch (Exception e) {
            try {
                if (conn != null)
                    conn.rollback();
            } catch (Exception ex) {
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null)
                    conn.setAutoCommit(true);
            } catch (Exception e) {
            }
        }
    }

    // ========================================================
    // HÀM SINH MÃ TỰ ĐỘNG (Đã fix lỗi bỏ qua mã rác cũ)
    // ========================================================
    public String getMaPhieuXuatMoi(String tienTo) {
        // Thêm điều kiện AND LEN(MaPhieuXuat) = 7 để loại bỏ các mã thời gian cũ (VD:
        // CK1775212821968)
        String sql = "SELECT MAX(CAST(SUBSTRING(MaPhieuXuat, 3, LEN(MaPhieuXuat)) AS INT)) " +
                "FROM PhieuXuat WHERE MaPhieuXuat LIKE '" + tienTo + "%' AND LEN(MaPhieuXuat) = 7";
        try (Connection con = ConnectDB.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                int max = rs.getInt(1); // rs.getInt sẽ trả về 0 nếu MAX() bị NULL
                return String.format("%s%05d", tienTo, max + 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tienTo + "00001";
    }

    // ========================================================
    // LOGIC TRẢ NHÀ CUNG CẤP (Loại phiếu = 2) - BẢN FIX CÔNG NỢ
    // ========================================================
    public boolean traNhaCungCap(PhieuXuat px, List<ChiTietPhieuXuat> listCT) {
        Connection conn = null;
        try {
            conn = ConnectDB.getConnection();
            conn.setAutoCommit(false);

            // 1. Lưu Header Phiếu Xuất
            String sqlP = "INSERT INTO PhieuXuat (MaPhieuXuat, NgayXuat, MaNhanVien, LoaiPhieu, MaNhaCungCap, TongTien, GhiChu) VALUES (?, GETDATE(), ?, 2, ?, ?, ?)";
            PreparedStatement psP = conn.prepareStatement(sqlP);
            psP.setString(1, px.getMaPhieuXuat());
            psP.setString(2, px.getMaNhanVien());
            psP.setString(3, px.getMaNhaCungCap());
            psP.setDouble(4, px.getTongTien());
            psP.setString(5, px.getGhiChu());
            psP.executeUpdate();

            // 2. Chi Tiết và Trừ Tồn Kho
            String sqlCT = "INSERT INTO ChiTietPhieuXuat (MaPhieuXuat, MaThuoc, SoLo, SoLuong, DonGia, ThanhTien) VALUES (?, ?, ?, ?, ?, ?)";
            String sqlGiamTon = "UPDATE LoThuoc SET soLuongTon = soLuongTon - ? WHERE maLoThuoc = ?";

            PreparedStatement psCT = conn.prepareStatement(sqlCT);
            PreparedStatement psGiamTon = conn.prepareStatement(sqlGiamTon);

            for (ChiTietPhieuXuat ct : listCT) {
                psCT.setString(1, px.getMaPhieuXuat());
                psCT.setString(2, ct.getMaThuoc());
                psCT.setString(3, ct.getSoLo());
                psCT.setInt(4, ct.getSoLuong());
                psCT.setDouble(5, ct.getDonGia());
                psCT.setDouble(6, ct.getThanhTien());
                psCT.addBatch();

                // Trừ trực tiếp số lượng trong kho
                psGiamTon.setInt(1, ct.getSoLuong());
                psGiamTon.setString(2, ct.getSoLo());
                psGiamTon.addBatch();
            }
            psCT.executeBatch();
            psGiamTon.executeBatch();

            // 3. TRỪ CÔNG NỢ NHÀ CUNG CẤP (Fix lỗi NULL)
            String sqlTruCongNo = "UPDATE NhaCungCap SET congNo = ISNULL(congNo, 0) - ? WHERE maNhaCungCap = ?";
            PreparedStatement psTruCongNo = conn.prepareStatement(sqlTruCongNo);
            psTruCongNo.setDouble(1, px.getTongTien());
            psTruCongNo.setString(2, px.getMaNhaCungCap());
            psTruCongNo.executeUpdate();

            conn.commit();
            return true;
        } catch (Exception e) {
            try {
                if (conn != null)
                    conn.rollback();
            } catch (Exception ex) {
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null)
                    conn.setAutoCommit(true);
            } catch (Exception e) {
            }
        }
    }

    // ========================================================
    // HÀM LẤY CHI TIẾT PHIẾU XUẤT ĐỂ HIỂN THỊ LÊN DIALOG
    // ========================================================
    public List<ChiTietPhieuXuat> getChiTietPhieuXuat(String maPhieu) {
        List<ChiTietPhieuXuat> list = new ArrayList<>();
        // JOIN để lấy Tên thuốc gán tạm vào trường MaThuoc để hiện lên bảng
        String sql = "SELECT ct.*, t.tenThuoc FROM ChiTietPhieuXuat ct " +
                "JOIN Thuoc t ON ct.maThuoc = t.maThuoc " +
                "WHERE ct.maPhieuXuat = ?";
        try (Connection con = ConnectDB.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maPhieu);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                ChiTietPhieuXuat ct = new ChiTietPhieuXuat();
                ct.setMaPhieuXuat(rs.getString("maPhieuXuat"));
                ct.setMaThuoc(rs.getString("tenThuoc")); // Mượn trường này lưu Tên thuốc hiển thị
                ct.setSoLo(rs.getString("soLo"));
                ct.setSoLuong(rs.getInt("soLuong"));
                ct.setDonGia(rs.getDouble("donGia"));
                ct.setThanhTien(rs.getDouble("thanhTien"));
                list.add(ct);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ========================================================
    // LOGIC XUẤT HỦY THUỐC (Loại phiếu = 3)
    // ========================================================
    public boolean xuatHuyThuoc(PhieuXuat px, List<ChiTietPhieuXuat> listCT) {
        Connection conn = null;
        try {
            conn = getConn();
            conn.setAutoCommit(false);

            // 1. Lưu Header Phiếu Xuất (Loại 3, KHÔNG có Kho Nhận, KHÔNG có NCC)
            String sqlP = "INSERT INTO PhieuXuat (MaPhieuXuat, NgayXuat, MaNhanVien, LoaiPhieu, TongTien, GhiChu) VALUES (?, GETDATE(), ?, 3, ?, ?)";
            PreparedStatement psP = conn.prepareStatement(sqlP);
            psP.setString(1, px.getMaPhieuXuat());
            psP.setString(2, px.getMaNhanVien());
            psP.setDouble(3, px.getTongTien()); // Ghi nhận giá trị thiệt hại (tùy chọn)
            psP.setString(4, px.getGhiChu());
            psP.executeUpdate();

            // 2. Chi Tiết và Trừ Tồn Kho
            String sqlCT = "INSERT INTO ChiTietPhieuXuat (MaPhieuXuat, MaThuoc, SoLo, SoLuong, DonGia, ThanhTien) VALUES (?, ?, ?, ?, ?, ?)";
            String sqlGiamTon = "UPDATE LoThuoc SET soLuongTon = soLuongTon - ? WHERE maLoThuoc = ?";

            PreparedStatement psCT = conn.prepareStatement(sqlCT);
            PreparedStatement psGiamTon = conn.prepareStatement(sqlGiamTon);

            for (ChiTietPhieuXuat ct : listCT) {
                psCT.setString(1, px.getMaPhieuXuat());
                psCT.setString(2, ct.getMaThuoc());
                psCT.setString(3, ct.getSoLo());
                psCT.setInt(4, ct.getSoLuong());
                psCT.setDouble(5, ct.getDonGia());
                psCT.setDouble(6, ct.getThanhTien());
                psCT.addBatch();

                // Trừ trực tiếp số lượng trong kho
                psGiamTon.setInt(1, ct.getSoLuong());
                psGiamTon.setString(2, ct.getSoLo());
                psGiamTon.addBatch();
            }
            psCT.executeBatch();
            psGiamTon.executeBatch();

            conn.commit();
            return true;
        } catch (Exception e) {
            try {
                if (conn != null)
                    conn.rollback();
            } catch (Exception ex) {
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null)
                    conn.setAutoCommit(true);
            } catch (Exception e) {
            }
        }
    }
}