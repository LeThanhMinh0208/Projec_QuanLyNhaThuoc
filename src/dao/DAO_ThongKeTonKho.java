package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import connectDB.ConnectDB;

public class DAO_ThongKeTonKho {

    private static final String ALL = "Tất cả";

    private boolean hasFilter(String value) {
        return value != null && !value.trim().isEmpty() && !ALL.equalsIgnoreCase(value.trim());
    }

    public List<String> getDanhMucThuoc() {
        List<String> result = new ArrayList<>();
        result.add(ALL);

        String sql = "SELECT tenDanhMuc FROM DanhMucThuoc ORDER BY tenDanhMuc";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(rs.getString("tenDanhMuc"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Map<String, Object> getTongQuan(String danhMuc, int nguongTonThap, int soNgayHetHan) {
        String sql = "SELECT " +
                "COUNT(DISTINCT t.maThuoc) AS tongMatHang, " +
                "COUNT(DISTINCT CASE WHEN l.soLuongTon > 0 THEN l.maLoThuoc END) AS tongLoConHang, " +
                "ISNULL(SUM(CASE WHEN l.trangThai = 1 THEN l.soLuongTon ELSE 0 END), 0) AS tongSoLuongTon, " +
                "ISNULL(SUM(CASE WHEN l.trangThai = 1 THEN l.soLuongTon * ISNULL(l.giaNhap, 0) ELSE 0 END), 0) AS tongGiaTriTon, " +
                "COUNT(DISTINCT CASE WHEN ISNULL(stock.tongTon, 0) <= ? THEN t.maThuoc END) AS soMatHangTonThap, " +
                "COUNT(DISTINCT CASE WHEN l.trangThai = 1 AND l.soLuongTon > 0 AND l.hanSuDung >= CAST(GETDATE() AS DATE) " +
                "AND l.hanSuDung <= DATEADD(day, ?, CAST(GETDATE() AS DATE)) THEN l.maLoThuoc END) AS soLoSapHetHan " +
                "FROM Thuoc t " +
                "LEFT JOIN DanhMucThuoc dm ON t.maDanhMuc = dm.maDanhMuc " +
                "LEFT JOIN LoThuoc l ON t.maThuoc = l.maThuoc " +
                "LEFT JOIN (SELECT maThuoc, SUM(CASE WHEN trangThai = 1 THEN soLuongTon ELSE 0 END) AS tongTon FROM LoThuoc GROUP BY maThuoc) stock " +
                "ON t.maThuoc = stock.maThuoc " +
                "WHERE 1 = 1 ";

        if (hasFilter(danhMuc)) {
            sql += "AND dm.tenDanhMuc = ? ";
        }

        Map<String, Object> result = new HashMap<>();
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, nguongTonThap);
            ps.setInt(2, soNgayHetHan);
            if (hasFilter(danhMuc)) {
                ps.setString(3, danhMuc);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    result.put("tongMatHang", rs.getInt("tongMatHang"));
                    result.put("tongLoConHang", rs.getInt("tongLoConHang"));
                    result.put("tongSoLuongTon", rs.getInt("tongSoLuongTon"));
                    result.put("tongGiaTriTon", rs.getDouble("tongGiaTriTon"));
                    result.put("soMatHangTonThap", rs.getInt("soMatHangTonThap"));
                    result.put("soLoSapHetHan", rs.getInt("soLoSapHetHan"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Map<String, Object>> getTonKhoTheoDanhMuc(String danhMuc) {
        String sql = "SELECT ISNULL(dm.tenDanhMuc, N'Không xác định') AS tenDanhMuc, " +
                "ISNULL(SUM(CASE WHEN l.trangThai = 1 THEN l.soLuongTon ELSE 0 END), 0) AS soLuongTon, " +
                "ISNULL(SUM(CASE WHEN l.trangThai = 1 THEN l.soLuongTon * ISNULL(l.giaNhap, 0) ELSE 0 END), 0) AS giaTriTon " +
                "FROM Thuoc t " +
                "LEFT JOIN DanhMucThuoc dm ON t.maDanhMuc = dm.maDanhMuc " +
                "LEFT JOIN LoThuoc l ON t.maThuoc = l.maThuoc " +
                "WHERE 1 = 1 ";
        if (hasFilter(danhMuc)) {
            sql += "AND dm.tenDanhMuc = ? ";
        }
        sql += "GROUP BY dm.tenDanhMuc HAVING ISNULL(SUM(CASE WHEN l.trangThai = 1 THEN l.soLuongTon ELSE 0 END), 0) > 0 " +
                "ORDER BY giaTriTon DESC";

        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (hasFilter(danhMuc)) {
                ps.setString(1, danhMuc);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("tenDanhMuc", rs.getString("tenDanhMuc"));
                    row.put("soLuongTon", rs.getInt("soLuongTon"));
                    row.put("giaTriTon", rs.getDouble("giaTriTon"));
                    result.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Map<String, Object>> getBienDongTonKho(LocalDate tuNgay, LocalDate denNgay, String danhMuc) {
        String sql = "WITH Ngay AS ( " +
                "SELECT CAST(? AS DATE) AS ngay UNION ALL SELECT DATEADD(day, 1, ngay) FROM Ngay WHERE ngay < CAST(? AS DATE) " +
                "), Nhap AS ( " +
                "SELECT CAST(pn.ngayNhap AS DATE) AS ngay, SUM(ct.soLuong * dv.tyLeQuyDoi) AS soLuongNhap " +
                "FROM ChiTietPhieuNhap ct JOIN PhieuNhap pn ON ct.maPhieuNhap = pn.maPhieuNhap " +
                "JOIN DonViQuyDoi dv ON ct.maQuyDoi = dv.maQuyDoi JOIN Thuoc t ON dv.maThuoc = t.maThuoc " +
                "LEFT JOIN DanhMucThuoc dm ON t.maDanhMuc = dm.maDanhMuc " +
                "WHERE CAST(pn.ngayNhap AS DATE) BETWEEN ? AND ? ";
        if (hasFilter(danhMuc)) {
            sql += "AND dm.tenDanhMuc = ? ";
        }
        sql += "GROUP BY CAST(pn.ngayNhap AS DATE) " +
                "), XuatBan AS ( " +
                "SELECT CAST(hd.ngayLap AS DATE) AS ngay, SUM(ct.soLuong * dv.tyLeQuyDoi) AS soLuongXuat " +
                "FROM ChiTietHoaDon ct JOIN HoaDon hd ON ct.maHoaDon = hd.maHoaDon " +
                "JOIN DonViQuyDoi dv ON ct.maQuyDoi = dv.maQuyDoi JOIN Thuoc t ON dv.maThuoc = t.maThuoc " +
                "LEFT JOIN DanhMucThuoc dm ON t.maDanhMuc = dm.maDanhMuc " +
                "WHERE CAST(hd.ngayLap AS DATE) BETWEEN ? AND ? ";
        if (hasFilter(danhMuc)) {
            sql += "AND dm.tenDanhMuc = ? ";
        }
        sql += "GROUP BY CAST(hd.ngayLap AS DATE) " +
                "), XuatKho AS ( " +
                "SELECT CAST(px.ngayXuat AS DATE) AS ngay, SUM(ct.soLuong) AS soLuongXuat " +
                "FROM ChiTietPhieuXuat ct JOIN PhieuXuat px ON ct.maPhieuXuat = px.maPhieuXuat " +
                "JOIN LoThuoc l ON ct.maLoThuoc = l.maLoThuoc JOIN Thuoc t ON l.maThuoc = t.maThuoc " +
                "LEFT JOIN DanhMucThuoc dm ON t.maDanhMuc = dm.maDanhMuc " +
                "WHERE CAST(px.ngayXuat AS DATE) BETWEEN ? AND ? ";
        if (hasFilter(danhMuc)) {
            sql += "AND dm.tenDanhMuc = ? ";
        }
        sql += "GROUP BY CAST(px.ngayXuat AS DATE) " +
                ") SELECT n.ngay, ISNULL(nh.soLuongNhap, 0) AS soLuongNhap, " +
                "ISNULL(xb.soLuongXuat, 0) + ISNULL(xk.soLuongXuat, 0) AS soLuongXuat " +
                "FROM Ngay n LEFT JOIN Nhap nh ON n.ngay = nh.ngay " +
                "LEFT JOIN XuatBan xb ON n.ngay = xb.ngay " +
                "LEFT JOIN XuatKho xk ON n.ngay = xk.ngay " +
                "ORDER BY n.ngay OPTION (MAXRECURSION 0)";

        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            int i = 1;
            ps.setDate(i++, java.sql.Date.valueOf(tuNgay));
            ps.setDate(i++, java.sql.Date.valueOf(denNgay));
            ps.setDate(i++, java.sql.Date.valueOf(tuNgay));
            ps.setDate(i++, java.sql.Date.valueOf(denNgay));
            if (hasFilter(danhMuc)) ps.setString(i++, danhMuc);
            ps.setDate(i++, java.sql.Date.valueOf(tuNgay));
            ps.setDate(i++, java.sql.Date.valueOf(denNgay));
            if (hasFilter(danhMuc)) ps.setString(i++, danhMuc);
            ps.setDate(i++, java.sql.Date.valueOf(tuNgay));
            ps.setDate(i++, java.sql.Date.valueOf(denNgay));
            if (hasFilter(danhMuc)) ps.setString(i, danhMuc);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("ngay", rs.getDate("ngay").toLocalDate());
                    row.put("soLuongNhap", rs.getInt("soLuongNhap"));
                    row.put("soLuongXuat", rs.getInt("soLuongXuat"));
                    result.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Map<String, Object>> getTopTonKho(String danhMuc, String trangThaiTon, int top, int nguongTonThap) {
        String sql = "SELECT TOP " + top + " t.maThuoc, t.tenThuoc, ISNULL(dm.tenDanhMuc, N'Không xác định') AS tenDanhMuc, " +
                "t.donViCoBan, ISNULL(SUM(CASE WHEN l.trangThai = 1 THEN l.soLuongTon ELSE 0 END), 0) AS soLuongTon, " +
                "ISNULL(SUM(CASE WHEN l.trangThai = 1 THEN l.soLuongTon * ISNULL(l.giaNhap, 0) ELSE 0 END), 0) AS giaTriTon, " +
                "COUNT(CASE WHEN l.trangThai = 1 AND l.soLuongTon > 0 THEN l.maLoThuoc END) AS soLo " +
                "FROM Thuoc t LEFT JOIN DanhMucThuoc dm ON t.maDanhMuc = dm.maDanhMuc " +
                "LEFT JOIN LoThuoc l ON t.maThuoc = l.maThuoc WHERE 1 = 1 ";
        if (hasFilter(danhMuc)) {
            sql += "AND dm.tenDanhMuc = ? ";
        }
        sql += "GROUP BY t.maThuoc, t.tenThuoc, dm.tenDanhMuc, t.donViCoBan ";

        if ("Tồn thấp".equalsIgnoreCase(trangThaiTon)) {
            sql += "HAVING ISNULL(SUM(CASE WHEN l.trangThai = 1 THEN l.soLuongTon ELSE 0 END), 0) <= " + nguongTonThap + " ";
        } else if ("Còn hàng".equalsIgnoreCase(trangThaiTon)) {
            sql += "HAVING ISNULL(SUM(CASE WHEN l.trangThai = 1 THEN l.soLuongTon ELSE 0 END), 0) > 0 ";
        } else if ("Hết hàng".equalsIgnoreCase(trangThaiTon)) {
            sql += "HAVING ISNULL(SUM(CASE WHEN l.trangThai = 1 THEN l.soLuongTon ELSE 0 END), 0) = 0 ";
        }

        sql += "ORDER BY soLuongTon DESC, giaTriTon DESC";

        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (hasFilter(danhMuc)) {
                ps.setString(1, danhMuc);
            }
            try (ResultSet rs = ps.executeQuery()) {
                int stt = 1;
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("stt", stt++);
                    row.put("maThuoc", rs.getString("maThuoc"));
                    row.put("tenThuoc", rs.getString("tenThuoc"));
                    row.put("tenDanhMuc", rs.getString("tenDanhMuc"));
                    row.put("donViCoBan", rs.getString("donViCoBan"));
                    row.put("soLuongTon", rs.getInt("soLuongTon"));
                    row.put("giaTriTon", rs.getDouble("giaTriTon"));
                    row.put("soLo", rs.getInt("soLo"));
                    result.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Map<String, Object>> getLoSapHetHan(String danhMuc, int soNgayHetHan, int top) {
        String sql = "SELECT TOP " + top + " l.maLoThuoc, t.tenThuoc, ISNULL(dm.tenDanhMuc, N'Không xác định') AS tenDanhMuc, " +
                "l.hanSuDung, l.soLuongTon, l.giaNhap, DATEDIFF(day, CAST(GETDATE() AS DATE), l.hanSuDung) AS soNgayConLai " +
                "FROM LoThuoc l JOIN Thuoc t ON l.maThuoc = t.maThuoc " +
                "LEFT JOIN DanhMucThuoc dm ON t.maDanhMuc = dm.maDanhMuc " +
                "WHERE l.trangThai = 1 AND l.soLuongTon > 0 AND l.hanSuDung >= CAST(GETDATE() AS DATE) " +
                "AND l.hanSuDung <= DATEADD(day, ?, CAST(GETDATE() AS DATE)) ";
        if (hasFilter(danhMuc)) {
            sql += "AND dm.tenDanhMuc = ? ";
        }
        sql += "ORDER BY l.hanSuDung ASC, l.soLuongTon DESC";

        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, soNgayHetHan);
            if (hasFilter(danhMuc)) {
                ps.setString(2, danhMuc);
            }
            try (ResultSet rs = ps.executeQuery()) {
                int stt = 1;
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("stt", stt++);
                    row.put("maLoThuoc", rs.getString("maLoThuoc"));
                    row.put("tenThuoc", rs.getString("tenThuoc"));
                    row.put("tenDanhMuc", rs.getString("tenDanhMuc"));
                    row.put("hanSuDung", rs.getDate("hanSuDung").toLocalDate());
                    row.put("soLuongTon", rs.getInt("soLuongTon"));
                    row.put("giaNhap", rs.getDouble("giaNhap"));
                    row.put("soNgayConLai", rs.getInt("soNgayConLai"));
                    result.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
