package dao;

import connectDB.ConnectDB;
import entity.HoaDon;
import entity.ChiTietHoaDon;
import java.sql.*;
import java.util.List;

public class DAO_HoaDon {
    public boolean thanhToan(HoaDon hd, List<ChiTietHoaDon> dsCT) {
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false); // Bắt đầu giao dịch

            // 1. Lưu HoaDon
            String sqlHD = "INSERT INTO HoaDon(maHoaDon, maKhachHang, maNhanVien, ngayLap, thueVAT, hinhThucThanhToan, ghiChu) VALUES(?,?,?,?,?,?,?)";
            PreparedStatement pstHD = con.prepareStatement(sqlHD);
            pstHD.setString(1, hd.getMaHoaDon());
            pstHD.setString(2, hd.getKhachHang() != null ? hd.getKhachHang().getMaKhachHang() : null);
            pstHD.setString(3, hd.getNhanVien().getMaNhanVien());
            pstHD.setDate(4, hd.getNgayLap());
            pstHD.setDouble(5, hd.getThueVAT());
            pstHD.setString(6, hd.getHinhThucThanhToan());
            pstHD.setString(7, hd.getGhiChu());
            pstHD.executeUpdate();

            // 2. Lưu ChiTietHoaDon (đủ 6 cột theo schema mới)
            String sqlCT = "INSERT INTO ChiTietHoaDon(maHoaDon, maBangGia, maQuyDoi, maLoThuoc, soLuong, donGia) VALUES(?,?,?,?,?,?)";
            PreparedStatement pstCT = con.prepareStatement(sqlCT);
            for (ChiTietHoaDon ct : dsCT) {
                pstCT.setString(1, hd.getMaHoaDon());
                pstCT.setString(2, ct.getMaBangGia());
                pstCT.setString(3, ct.getMaQuyDoi());
                pstCT.setString(4, ct.getMaLoThuoc());
                pstCT.setInt(5, ct.getSoLuong());
                pstCT.setDouble(6, ct.getDonGia());
                pstCT.executeUpdate();

                // Cập nhật tồn kho theo lô (dùng soLuongTruKho = số lượng đơn vị cơ bản thực trừ)
                String sqlUpdateLo = "UPDATE LoThuoc SET soLuongTon = soLuongTon - ? WHERE maLoThuoc = ?";
                try (PreparedStatement pstUpdateLo = con.prepareStatement(sqlUpdateLo)) {
                    pstUpdateLo.setInt(1, ct.getSoLuongTruKho());
                    pstUpdateLo.setString(2, ct.getMaLoThuoc());
                    pstUpdateLo.executeUpdate();
                }
            }

            con.commit();
            con.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            if (con != null)
                try {
                    con.rollback();
                    con.setAutoCommit(true);
                } catch (SQLException ex) {
                }
            e.printStackTrace();
            return false;
        }
    }

    public String generateMaHoaDon() {
        String sql = "SELECT MAX(CAST(SUBSTRING(maHoaDon, 3, LEN(maHoaDon)) AS INT)) FROM HoaDon";
        try (Connection con = ConnectDB.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                int max = rs.getInt(1);
                return String.format("HD%04d", max + 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "HD0001";
    }

    /**
     * Lấy danh sách hóa đơn tổng hợp với filter.
     * @param tuNgay   null = không lọc
     * @param denNgay  null = không lọc
     * @param hinhThuc null = tất cả ("TIEN_MAT", "CHUYEN_KHOAN", "THE")
     * @param keyword  null/blank = không lọc theo từ khóa
     */
    public List<entity.HoaDonView> getDanhSach(
            java.time.LocalDate tuNgay,
            java.time.LocalDate denNgay,
            String hinhThuc,
            String keyword) {

        List<entity.HoaDonView> list = new java.util.ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT hd.maHoaDon, hd.ngayLap, kh.hoTen AS tenKhachHang, kh.sdt, " +
            "       nv.hoTen AS tenNhanVien, hd.thueVAT, hd.hinhThucThanhToan, hd.ghiChu, " +
            "       SUM(ct.soLuong * ct.donGia) AS tamTinh, " +
            "       SUM(ct.soLuong * ct.donGia) * (1 + hd.thueVAT/100.0) AS tongSauVAT " +
            "FROM HoaDon hd " +
            "LEFT JOIN KhachHang kh ON hd.maKhachHang = kh.maKhachHang " +
            "JOIN NhanVien nv ON hd.maNhanVien = nv.maNhanVien " +
            "JOIN ChiTietHoaDon ct ON hd.maHoaDon = ct.maHoaDon " +
            "WHERE 1=1"
        );
        java.util.List<Object> params = new java.util.ArrayList<>();

        if (tuNgay != null) {
            sql.append(" AND CAST(hd.ngayLap AS DATE) >= ?");
            params.add(java.sql.Date.valueOf(tuNgay));
        }
        if (denNgay != null) {
            sql.append(" AND CAST(hd.ngayLap AS DATE) <= ?");
            params.add(java.sql.Date.valueOf(denNgay));
        }
        if (hinhThuc != null && !hinhThuc.isBlank()) {
            sql.append(" AND hd.hinhThucThanhToan = ?");
            params.add(hinhThuc);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (hd.maHoaDon LIKE ? OR kh.hoTen LIKE ? OR kh.sdt LIKE ?)");
            String kw = "%" + keyword + "%";
            params.add(kw); params.add(kw); params.add(kw);
        }
        sql.append(" GROUP BY hd.maHoaDon, hd.ngayLap, kh.hoTen, kh.sdt, nv.hoTen, hd.thueVAT, hd.hinhThucThanhToan, hd.ghiChu");
        sql.append(" ORDER BY hd.ngayLap DESC");

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pst.setObject(i + 1, params.get(i));
            }
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                entity.HoaDonView v = new entity.HoaDonView();
                v.setMaHoaDon(rs.getString("maHoaDon"));
                v.setNgayLap(rs.getTimestamp("ngayLap"));
                v.setTenKhachHang(rs.getString("tenKhachHang"));
                v.setSdt(rs.getString("sdt"));
                v.setTenNhanVien(rs.getString("tenNhanVien"));
                v.setThueVAT(rs.getDouble("thueVAT"));
                v.setHinhThucThanhToan(rs.getString("hinhThucThanhToan"));
                v.setGhiChu(rs.getString("ghiChu"));
                v.setTamTinh(rs.getDouble("tamTinh"));
                v.setTongSauVAT(rs.getDouble("tongSauVAT"));
                list.add(v);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy chi tiết sản phẩm của 1 hóa đơn (dùng trong dialog chi tiết).
     * Trả về mảng Object[] mỗi dòng: [tenThuoc, tenDonVi, maLoThuoc, hanSuDung, soLuong, donGia, thanhTien]
     */
    public List<Object[]> getChiTietByMaHoaDon(String maHoaDon) {
        List<Object[]> list = new java.util.ArrayList<>();
        String sql = "SELECT t.tenThuoc, dv.tenDonVi, lo.maLoThuoc, lo.hanSuDung, " +
                     "       ct.soLuong, ct.donGia, (ct.soLuong * ct.donGia) AS thanhTien " +
                     "FROM ChiTietHoaDon ct " +
                     "JOIN DonViQuyDoi dv ON ct.maQuyDoi = dv.maQuyDoi " +
                     "JOIN Thuoc t ON dv.maThuoc = t.maThuoc " +
                     "JOIN LoThuoc lo ON ct.maLoThuoc = lo.maLoThuoc " +
                     "WHERE ct.maHoaDon = ? " +
                     "ORDER BY t.tenThuoc";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maHoaDon);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("tenThuoc"),
                    rs.getString("tenDonVi"),
                    rs.getString("maLoThuoc"),
                    rs.getDate("hanSuDung"),
                    rs.getInt("soLuong"),
                    rs.getDouble("donGia"),
                    rs.getDouble("thanhTien")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}