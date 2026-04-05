package dao;

import connectDB.ConnectDB;
import entity.BangGia;
import entity.ChiTietBangGia;
import entity.Thuoc;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DAO_BangGia {

    // ============================================================
    // LẤY DANH SÁCH BẢNG GIÁ (kèm số lượng thuốc)
    // ============================================================
    public List<BangGia> getAllBangGia() {
        List<BangGia> list = new ArrayList<>();
        // FIX 3G: dem DISTINCT loai thuoc va tong don vi
        String sql = "SELECT bg.maBangGia, bg.tenBangGia, bg.loaiBangGia, " +
                     "       bg.ngayBatDau, bg.ngayKetThuc, bg.moTa, bg.trangThai, " +
                     "       COUNT(DISTINCT dv.maThuoc) AS soLuongThuoc, " +
                     "       COUNT(ctbg.maQuyDoi)       AS soDonVi " +
                     "FROM BangGia bg " +
                     "LEFT JOIN ChiTietBangGia ctbg ON bg.maBangGia = ctbg.maBangGia " +
                     "LEFT JOIN DonViQuyDoi dv ON ctbg.maQuyDoi = dv.maQuyDoi " +
                     "GROUP BY bg.maBangGia, bg.tenBangGia, bg.loaiBangGia, " +
                     "         bg.ngayBatDau, bg.ngayKetThuc, bg.moTa, bg.trangThai " +
                     "ORDER BY bg.ngayBatDau DESC";
        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapBangGia(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ============================================================
    // LẤY BẢNG GIÁ THEO MÃ
    // ============================================================
    public BangGia getBangGiaById(String maBangGia) {
        String sql = "SELECT bg.maBangGia, bg.tenBangGia, bg.loaiBangGia, " +
                     "       bg.ngayBatDau, bg.ngayKetThuc, bg.moTa, bg.trangThai, " +
                     "       COUNT(DISTINCT dv.maThuoc) AS soLuongThuoc, " +
                     "       COUNT(ctbg.maQuyDoi)       AS soDonVi " +
                     "FROM BangGia bg " +
                     "LEFT JOIN ChiTietBangGia ctbg ON bg.maBangGia = ctbg.maBangGia " +
                     "LEFT JOIN DonViQuyDoi dv ON ctbg.maQuyDoi = dv.maQuyDoi " +
                     "WHERE bg.maBangGia = ? " +
                     "GROUP BY bg.maBangGia, bg.tenBangGia, bg.loaiBangGia, " +
                     "         bg.ngayBatDau, bg.ngayKetThuc, bg.moTa, bg.trangThai";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maBangGia);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return mapBangGia(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ============================================================
    // SINH MÃ BẢNG GIÁ (BG0001, BG0002, ...)
    // ============================================================
    public String generateNextMaBangGia() {
        String sql = "SELECT MAX(maBangGia) FROM BangGia WHERE maBangGia LIKE 'BG%'";
        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next() && rs.getString(1) != null) {
                String last = rs.getString(1); // "BG0004"
                int num = Integer.parseInt(last.substring(2)) + 1;
                return String.format("BG%04d", num);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "BG0001";
    }

    // ============================================================
    // TÌM BẢNG GIÁ DEFAULT ĐANG ĐỂ MỞ (chưa có ngày kết thúc)
    // ============================================================
    public BangGia getBangGiaDefaultDangMo() {
        String sql = "SELECT bg.maBangGia, bg.tenBangGia, bg.loaiBangGia, " +
                     "       bg.ngayBatDau, bg.ngayKetThuc, bg.moTa, bg.trangThai, 0 AS soLuongThuoc " +
                     "FROM BangGia bg " +
                     "WHERE bg.loaiBangGia = 'DEFAULT' AND bg.trangThai = 1 AND bg.ngayKetThuc IS NULL";
        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return mapBangGia(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ============================================================
    // TẠO BẢNG GIÁ MỚI (transaction: đóng bảng cũ + insert mới + insert chi tiết)
    // Trả về null nếu thành công, trả về message lỗi nếu thất bại.
    // ============================================================
    public String taoBangGiaMoi(BangGia bg, List<ChiTietBangGia> chiTiet) {
        // --- Validate DEFAULT logic ---
        if ("DEFAULT".equals(bg.getLoaiBangGia())) {
            // FIX 3D: ngay bat dau phai tu ngay mai tro di
            if (!bg.getNgayBatDau().isAfter(LocalDate.now())) {
                return "Ngày bắt đầu phải từ ngày mai trở đi (tối thiểu " + LocalDate.now().plusDays(1) + ")";
            }
            BangGia cuoi = getBangGiaDefaultDangMo();
            if (cuoi != null) {
                if (!bg.getNgayBatDau().isAfter(cuoi.getNgayBatDau())) {
                    return "Ngày bắt đầu phải lớn hơn ngày bắt đầu của bảng giá DEFAULT hiện tại ("
                           + cuoi.getNgayBatDau() + ").";
                }
            }
        }

        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            // 1. Đóng bảng giá DEFAULT cũ (nếu có)
            if ("DEFAULT".equals(bg.getLoaiBangGia())) {
                BangGia cuoi = getBangGiaDefaultDangMoWithCon(con);
                if (cuoi != null) {
                    LocalDate ngayKetThucCu = bg.getNgayBatDau().minusDays(1);
                    String sqlClose = "UPDATE BangGia SET ngayKetThuc = ? WHERE maBangGia = ?";
                    try (PreparedStatement pst = con.prepareStatement(sqlClose)) {
                        pst.setDate(1, Date.valueOf(ngayKetThucCu));
                        pst.setString(2, cuoi.getMaBangGia());
                        pst.executeUpdate();
                    }
                }
            }

            // 2. Chèn bảng giá mới
            String sqlInsert = "INSERT INTO BangGia (maBangGia, tenBangGia, loaiBangGia, ngayBatDau, ngayKetThuc, moTa, trangThai) " +
                               "VALUES (?, ?, ?, ?, ?, ?, 1)";
            try (PreparedStatement pst = con.prepareStatement(sqlInsert)) {
                pst.setString(1, bg.getMaBangGia());
                pst.setString(2, bg.getTenBangGia());
                pst.setString(3, bg.getLoaiBangGia());
                pst.setDate(4, Date.valueOf(bg.getNgayBatDau()));
                pst.setDate(5, bg.getNgayKetThuc() != null ? Date.valueOf(bg.getNgayKetThuc()) : null);
                pst.setString(6, bg.getMoTa());
                pst.executeUpdate();
            }

            // 3. Chèn chi tiết giá
            if (chiTiet != null && !chiTiet.isEmpty()) {
                String sqlDetail = "INSERT INTO ChiTietBangGia (maBangGia, maQuyDoi, donGiaBan) VALUES (?, ?, ?)";
                try (PreparedStatement pst = con.prepareStatement(sqlDetail)) {
                    for (ChiTietBangGia ct : chiTiet) {
                        pst.setString(1, bg.getMaBangGia());
                        pst.setString(2, ct.getMaQuyDoi());
                        pst.setBigDecimal(3, ct.getDonGiaBan());
                        pst.addBatch();
                    }
                    pst.executeBatch();
                }
            }

            con.commit();
            return null; // Thành công
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return "Lỗi cơ sở dữ liệu: " + e.getMessage();
        } finally {
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // ============================================================
    // CẬP NHẬT BẢNG GIÁ (tên, mô tả, ngày kết thúc)
    // ============================================================
    public boolean capNhatBangGia(BangGia bg) {
        String sql = "UPDATE BangGia SET tenBangGia=?, moTa=?, ngayKetThuc=? WHERE maBangGia=?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, bg.getTenBangGia());
            pst.setString(2, bg.getMoTa());
            pst.setDate(3, bg.getNgayKetThuc() != null ? Date.valueOf(bg.getNgayKetThuc()) : null);
            pst.setString(4, bg.getMaBangGia());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ============================================================
    // VÔ HIỆU HÓA BẢNG GIÁ (cập nhật cả ngayKetThuc nếu đang NULL)
    // ============================================================
    public boolean voHieuHoa(String maBangGia) {
        String sql = "UPDATE BangGia SET trangThai=0, "
                   + "ngayKetThuc=ISNULL(ngayKetThuc, CAST(GETDATE() AS DATE)) "
                   + "WHERE maBangGia=?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maBangGia);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ============================================================
    // KIỂM TRA: có bảng DEFAULT khác (active hoặc future) không kể maBangGia này
    // ============================================================
    public boolean coTonTaiBangGiaDefaultKhac(String maBangGiaHienTai) {
        String sql = "SELECT COUNT(*) FROM BangGia "
                   + "WHERE loaiBangGia='DEFAULT' AND trangThai=1 "
                   + "  AND maBangGia <> ? "
                   + "  AND ngayBatDau <= CAST(GETDATE() AS DATE) "
                   + "  AND (ngayKetThuc IS NULL OR ngayKetThuc >= CAST(GETDATE() AS DATE))";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maBangGiaHienTai);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ============================================================
    // LẤY GIÁ VÀ MÃ BẢNG GIÁ CHO ĐƠN VỊ QUY ĐỔI (ưu tiên PROMO > DEFAULT)
    // Trả về Object[]{ BigDecimal donGia, String maBangGia } hoặc null nếu không có
    // ============================================================
    public Object[] getGiaVaMaBangGia(String maQuyDoi) {
        String sql = "SELECT TOP 1 ctbg.donGiaBan, bg.maBangGia "
                   + "FROM ChiTietBangGia ctbg "
                   + "JOIN BangGia bg ON ctbg.maBangGia = bg.maBangGia "
                   + "WHERE ctbg.maQuyDoi = ? "
                   + "  AND bg.trangThai = 1 "
                   + "  AND bg.ngayBatDau <= CAST(GETDATE() AS DATE) "
                   + "  AND (bg.ngayKetThuc IS NULL OR bg.ngayKetThuc >= CAST(GETDATE() AS DATE)) "
                   + "ORDER BY CASE WHEN bg.loaiBangGia = 'PROMO' THEN 0 ELSE 1 END ASC, "
                   + "         bg.ngayBatDau DESC";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maQuyDoi);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new Object[]{ rs.getBigDecimal("donGiaBan"), rs.getString("maBangGia") };
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ============================================================
    // LẤY CHI TIẾT BẢNG GIÁ (kèm tên thuốc, đơn vị)
    // ============================================================
    public List<ChiTietBangGia> getChiTietByMaBangGia(String maBangGia) {
        List<ChiTietBangGia> list = new ArrayList<>();
        String sql = "SELECT ctbg.maBangGia, ctbg.maQuyDoi, ctbg.donGiaBan, " +
                     "       t.maThuoc, t.tenThuoc, dv.tenDonVi " +
                     "FROM ChiTietBangGia ctbg " +
                     "JOIN DonViQuyDoi dv ON ctbg.maQuyDoi = dv.maQuyDoi " +
                     "JOIN Thuoc t ON dv.maThuoc = t.maThuoc " +
                     "WHERE ctbg.maBangGia = ? " +
                     "ORDER BY t.tenThuoc, dv.tenDonVi";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maBangGia);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(new ChiTietBangGia(
                        rs.getString("maBangGia"),
                        rs.getString("maQuyDoi"),
                        rs.getBigDecimal("donGiaBan"),
                        rs.getString("maThuoc"),
                        rs.getString("tenThuoc"),
                        rs.getString("tenDonVi")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ============================================================
    // LẤY THUỐC ĐANG BÁN CHƯA CÓ TRONG BẢNG GIÁ
    // ============================================================
    public List<Thuoc> getThuocChuaCoTrongBangGia(String maBangGia) {
        List<Thuoc> list = new ArrayList<>();
        String sql = "SELECT DISTINCT t.maThuoc, t.tenThuoc " +
                     "FROM Thuoc t " +
                     "JOIN DonViQuyDoi dv ON t.maThuoc = dv.maThuoc " +
                     "WHERE t.trangThai = 'DANG_BAN' " +
                     "  AND t.maThuoc NOT IN (" +
                     "      SELECT dv2.maThuoc FROM ChiTietBangGia ctbg " +
                     "      JOIN DonViQuyDoi dv2 ON ctbg.maQuyDoi = dv2.maQuyDoi " +
                     "      WHERE ctbg.maBangGia = ?) " +
                     "ORDER BY t.tenThuoc";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maBangGia);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Thuoc t = new Thuoc();
                t.setMaThuoc(rs.getString("maThuoc"));
                t.setTenThuoc(rs.getString("tenThuoc"));
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ============================================================
    // THÊM CHI TIẾT GIÁ VÀO BẢNG GIÁ
    // ============================================================
    public boolean themChiTietBangGia(ChiTietBangGia ct) {
        String sql = "INSERT INTO ChiTietBangGia (maBangGia, maQuyDoi, donGiaBan) VALUES (?, ?, ?)";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, ct.getMaBangGia());
            pst.setString(2, ct.getMaQuyDoi());
            pst.setBigDecimal(3, ct.getDonGiaBan());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ============================================================
    // CẬP NHẬT GIÁ MỘT ĐƠN VỊ TRONG BẢNG GIÁ
    // ============================================================
    public boolean capNhatGia(String maBangGia, String maQuyDoi, BigDecimal gia) {
        String sql = "UPDATE ChiTietBangGia SET donGiaBan=? WHERE maBangGia=? AND maQuyDoi=?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setBigDecimal(1, gia);
            pst.setString(2, maBangGia);
            pst.setString(3, maQuyDoi);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ============================================================
    // XÓA CHI TIẾT GIÁ KHỎI BẢNG GIÁ
    // ============================================================
    public boolean xoaChiTietBangGia(String maBangGia, String maQuyDoi) {
        String sql = "DELETE FROM ChiTietBangGia WHERE maBangGia=? AND maQuyDoi=?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maBangGia);
            pst.setString(2, maQuyDoi);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ============================================================
    // LẤY TẤT CẢ THUỐC VÀ ĐƠN VỊ QUY ĐỔI (bỏ qua NGUNG_BAN)
    // ============================================================
    public List<ChiTietBangGia> getAllThuocVaDonVi() {
        List<ChiTietBangGia> list = new ArrayList<>();
        String sql = "SELECT t.maThuoc, t.tenThuoc, dv.maQuyDoi, dv.tenDonVi, dv.tyLeQuyDoi " +
                     "FROM Thuoc t " +
                     "JOIN DonViQuyDoi dv ON t.maThuoc = dv.maThuoc " +
                     "WHERE t.trangThai IN ('DANG_BAN', 'HET_HANG') " +
                     "ORDER BY t.tenThuoc ASC";
        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ChiTietBangGia ct = new ChiTietBangGia();
                ct.setMaThuoc(rs.getString("maThuoc"));
                ct.setTenThuoc(rs.getString("tenThuoc"));
                ct.setMaQuyDoi(rs.getString("maQuyDoi"));
                ct.setTenDonVi(rs.getString("tenDonVi"));
                ct.setDonGiaBan(BigDecimal.ZERO);
                list.add(ct);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ============================================================
    // XÓA BẢNG GIÁ (Xử lý chuỗi ngày kết thúc an toàn)
    // ============================================================
    public boolean xoaBangGia(String maBangGia) {
        BangGia bg = getBangGiaById(maBangGia);
        if (bg == null) return false;

        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            // Nếu xóa bảng DEFAULT
            if ("DEFAULT".equals(bg.getLoaiBangGia())) {
                // BƯỚC 1: Tìm bảng liền TRƯỚC (P)
                LocalDate ngayLienTruoc = bg.getNgayBatDau().minusDays(1);
                String sqlP = "SELECT TOP 1 maBangGia FROM BangGia WHERE loaiBangGia = 'DEFAULT' AND trangThai = 1 AND ngayKetThuc = ?";
                String maP = null;
                try (PreparedStatement pstP = con.prepareStatement(sqlP)) {
                    pstP.setDate(1, Date.valueOf(ngayLienTruoc));
                    try (ResultSet rsP = pstP.executeQuery()) {
                        if (rsP.next()) {
                            maP = rsP.getString(1);
                        }
                    }
                }

                // BƯỚC 2: Tìm bảng liền SAU (N)
                String maN = null;
                if (bg.getNgayKetThuc() != null) {
                    LocalDate ngayLienSau = bg.getNgayKetThuc().plusDays(1);
                    String sqlN = "SELECT TOP 1 maBangGia FROM BangGia WHERE loaiBangGia = 'DEFAULT' AND trangThai = 1 AND ngayBatDau = ?";
                    try (PreparedStatement pstN = con.prepareStatement(sqlN)) {
                        pstN.setDate(1, Date.valueOf(ngayLienSau));
                        try (ResultSet rsN = pstN.executeQuery()) {
                            if (rsN.next()) {
                                maN = rsN.getString(1);
                            }
                        }
                    }
                }

                // BƯỚC 3: Xử lý theo từng trường hợp
                if (maP != null) {
                    if (maN == null || bg.getNgayKetThuc() == null) {
                        // CASE 1 & CASE 3: Cuối chuỗi hoặc duy nhất -> P mở vô hạn (NULL)
                        String updateSql = "UPDATE BangGia SET ngayKetThuc = NULL WHERE maBangGia = ?";
                        try (PreparedStatement pstUpdate = con.prepareStatement(updateSql)) {
                            pstUpdate.setString(1, maP);
                            pstUpdate.executeUpdate();
                        }
                    } else {
                        // CASE 2: Ở giữa chuỗi -> P nối liền khoảng trống tới N
                        String updateSql = "UPDATE BangGia SET ngayKetThuc = ? WHERE maBangGia = ?";
                        try (PreparedStatement pstUpdate = con.prepareStatement(updateSql)) {
                            pstUpdate.setDate(1, Date.valueOf(bg.getNgayKetThuc()));
                            pstUpdate.setString(2, maP);
                            pstUpdate.executeUpdate();
                        }
                    }
                }
            }

            // Xóa chi tiết bảng giá trước
            String delChiTietSql = "DELETE FROM ChiTietBangGia WHERE maBangGia = ?";
            try (PreparedStatement pstDelCT = con.prepareStatement(delChiTietSql)) {
                pstDelCT.setString(1, maBangGia);
                pstDelCT.executeUpdate();
            }

            // Xóa bảng giá chính
            String sql = "DELETE FROM BangGia WHERE maBangGia = ?";
            try (PreparedStatement pstDelBG = con.prepareStatement(sql)) {
                pstDelBG.setString(1, maBangGia);
                pstDelBG.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    // ============================================================
    // HELPER PRIVATE
    // ============================================================
    private BangGia mapBangGia(ResultSet rs) throws SQLException {
        LocalDate ngayKetThuc = rs.getDate("ngayKetThuc") != null
                ? rs.getDate("ngayKetThuc").toLocalDate() : null;
        BangGia bg = new BangGia(
                rs.getString("maBangGia"),
                rs.getString("tenBangGia"),
                rs.getString("loaiBangGia"),
                rs.getDate("ngayBatDau").toLocalDate(),
                ngayKetThuc,
                rs.getString("moTa"),
                rs.getBoolean("trangThai"),
                rs.getInt("soLuongThuoc")
        );
        // Doc soDonVi neu co column nay
        try { bg.setSoDonVi(rs.getInt("soDonVi")); } catch (SQLException ignore) {}
        return bg;
    }

    private BangGia getBangGiaDefaultDangMoWithCon(Connection con) throws SQLException {
        String sql = "SELECT maBangGia, tenBangGia, loaiBangGia, ngayBatDau, ngayKetThuc, moTa, trangThai, 0 AS soLuongThuoc " +
                     "FROM BangGia WHERE loaiBangGia = 'DEFAULT' AND trangThai = 1 AND ngayKetThuc IS NULL";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return mapBangGia(rs);
        }
        return null;
    }

    // ============================================================
    // LAY GIA KE THUA TU BANG DEFAULT DANG HIEU LUC (FIX 3E)
    // ============================================================
    public java.util.Map<String, BigDecimal> getGiaKeThuaTuDefaultHienTai() {
        java.util.Map<String, BigDecimal> map = new java.util.LinkedHashMap<>();
        String sql = "SELECT ctbg.maQuyDoi, ctbg.donGiaBan " +
                     "FROM ChiTietBangGia ctbg " +
                     "JOIN BangGia bg ON ctbg.maBangGia = bg.maBangGia " +
                     "WHERE bg.loaiBangGia = 'DEFAULT' " +
                     "  AND bg.trangThai = 1 " +
                     "  AND bg.ngayBatDau <= CAST(GETDATE() AS DATE) " +
                     "  AND (bg.ngayKetThuc IS NULL OR bg.ngayKetThuc >= CAST(GETDATE() AS DATE)) " +
                     "ORDER BY bg.ngayBatDau DESC";
        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("maQuyDoi"), rs.getBigDecimal("donGiaBan"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    // ============================================================
    // KIEM TRA TRUNG THOI GIAN PROMO (FIX 3F)
    // Tra ve map maQuyDoi -> tenBangTrung
    // ============================================================
    public List<ChiTietBangGia> kiemTraTrungPromo(
            List<String> dsQuyDoi, LocalDate ngayBD, LocalDate ngayKT, String maBangGiaMoi) {
        List<ChiTietBangGia> result = new ArrayList<>();
        if (dsQuyDoi == null || dsQuyDoi.isEmpty()) return result;
        
        // Tạo chuỗi tham số cho IN (?)
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < dsQuyDoi.size(); i++) {
            placeholders.append("?");
            if (i < dsQuyDoi.size() - 1) placeholders.append(",");
        }

        String sql = "SELECT t.tenThuoc, dv.tenDonVi, bg.tenBangGia, bg.ngayBatDau, bg.ngayKetThuc, ctbg.maQuyDoi " +
                     "FROM ChiTietBangGia ctbg " +
                     "JOIN BangGia bg ON ctbg.maBangGia = bg.maBangGia " +
                     "JOIN DonViQuyDoi dv ON ctbg.maQuyDoi = dv.maQuyDoi " +
                     "JOIN Thuoc t ON dv.maThuoc = t.maThuoc " +
                     "WHERE ctbg.maQuyDoi IN (" + placeholders + ") " +
                     "  AND bg.loaiBangGia = 'PROMO' " +
                     "  AND bg.trangThai = 1 " +
                     "  AND bg.maBangGia <> ? " +
                     "  AND bg.ngayBatDau <= ? " +
                     "  AND (bg.ngayKetThuc IS NULL OR bg.ngayKetThuc >= ?)";
        
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            int idx = 1;
            for (String maQD : dsQuyDoi) {
                pst.setString(idx++, maQD);
            }
            pst.setString(idx++, maBangGiaMoi == null ? "" : maBangGiaMoi);
            pst.setDate(idx++, Date.valueOf(ngayKT != null ? ngayKT : LocalDate.now().plusYears(100)));
            pst.setDate(idx++, Date.valueOf(ngayBD));
            
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                ChiTietBangGia ct = new ChiTietBangGia();
                ct.setTenThuoc(rs.getString("tenThuoc"));
                ct.setTenDonVi(rs.getString("tenDonVi"));
                ct.setTenBangGia(rs.getString("tenBangGia"));
                ct.setNgayBatDau(rs.getDate("ngayBatDau").toLocalDate());
                Date kt = rs.getDate("ngayKetThuc");
                if (kt != null) ct.setNgayKetThuc(kt.toLocalDate());
                ct.setMaQuyDoi(rs.getString("maQuyDoi"));
                result.add(ct);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
