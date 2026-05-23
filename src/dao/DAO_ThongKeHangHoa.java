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

public class DAO_ThongKeHangHoa {

    private boolean hasFilter(String value) {
        return value != null && !value.trim().isEmpty()
                && !"Tất cả".equalsIgnoreCase(value.trim());
    }

    /** Tổng hợp KPI chính */
    public Map<String, Object> getTongQuan(LocalDate tuNgay, LocalDate denNgay,
                                            String danhMuc) {
        Map<String, Object> result = new HashMap<>();
        StringBuilder sql = new StringBuilder(
            "SELECT " +
            "  COUNT(DISTINCT dv.maThuoc)         AS tongMatHang, " +
            "  ISNULL(SUM(ct.soLuong), 0)          AS tongSoLuongBan, " +
            "  ISNULL(SUM(ct.soLuong * ct.donGia), 0) AS tongDoanhThu, " +
            "  COUNT(DISTINCT hd.maHoaDon)          AS tongDon " +
            "FROM ChiTietHoaDon ct " +
            "JOIN HoaDon hd ON hd.maHoaDon = ct.maHoaDon " +
            "JOIN DonViQuyDoi dv ON dv.maQuyDoi = ct.maQuyDoi " +
            "JOIN Thuoc t ON t.maThuoc = dv.maThuoc " +
            "LEFT JOIN DanhMucThuoc dm ON dm.maDanhMuc = t.maDanhMuc " +
            "WHERE CAST(hd.ngayLap AS DATE) >= ? " +
            "  AND CAST(hd.ngayLap AS DATE) <= ? ");
        if (hasFilter(danhMuc)) sql.append("AND dm.tenDanhMuc = ? ");

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            ps.setDate(1, java.sql.Date.valueOf(tuNgay));
            ps.setDate(2, java.sql.Date.valueOf(denNgay));
            if (hasFilter(danhMuc)) ps.setString(3, danhMuc);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result.put("tongMatHang",    rs.getInt("tongMatHang"));
                result.put("tongSoLuongBan", rs.getInt("tongSoLuongBan"));
                result.put("tongDoanhThu",   rs.getDouble("tongDoanhThu"));
                result.put("tongDon",        rs.getInt("tongDon"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    /** Top N thuốc bán chạy */
    public List<Map<String, Object>> getTopThuocBanChay(LocalDate tuNgay,
            LocalDate denNgay, String danhMuc, int top) {
        List<Map<String, Object>> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT TOP " + top +
            "  t.maThuoc, t.tenThuoc, " +
            "  ISNULL(dm.tenDanhMuc, N'Không xác định') AS tenDanhMuc, " +
            "  SUM(ct.soLuong) AS soLuongBan, " +
            "  SUM(ct.soLuong * ct.donGia) AS doanhThu " +
            "FROM ChiTietHoaDon ct " +
            "JOIN HoaDon hd ON hd.maHoaDon = ct.maHoaDon " +
            "JOIN DonViQuyDoi dv ON dv.maQuyDoi = ct.maQuyDoi " +
            "JOIN Thuoc t ON t.maThuoc = dv.maThuoc " +
            "LEFT JOIN DanhMucThuoc dm ON dm.maDanhMuc = t.maDanhMuc " +
            "WHERE CAST(hd.ngayLap AS DATE) >= ? " +
            "  AND CAST(hd.ngayLap AS DATE) <= ? ");
        if (hasFilter(danhMuc)) sql.append("AND dm.tenDanhMuc = ? ");
        sql.append("GROUP BY t.maThuoc, t.tenThuoc, dm.tenDanhMuc " +
                   "ORDER BY soLuongBan DESC");

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            ps.setDate(1, java.sql.Date.valueOf(tuNgay));
            ps.setDate(2, java.sql.Date.valueOf(denNgay));
            if (hasFilter(danhMuc)) ps.setString(3, danhMuc);
            ResultSet rs = ps.executeQuery();
            int stt = 1;
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("stt",        stt++);
                row.put("maThuoc",    rs.getString("maThuoc"));
                row.put("tenThuoc",   rs.getString("tenThuoc"));
                row.put("tenDanhMuc", rs.getString("tenDanhMuc"));
                row.put("soLuongBan", rs.getInt("soLuongBan"));
                row.put("doanhThu",   rs.getDouble("doanhThu"));
                result.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    /** Doanh thu theo danh mục */
    public List<Map<String, Object>> getDoanhThuTheoDanhMuc(LocalDate tuNgay,
            LocalDate denNgay, String danhMuc) {
        List<Map<String, Object>> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT ISNULL(dm.tenDanhMuc, N'Không xác định') AS tenDanhMuc, " +
            "  SUM(ct.soLuong) AS soLuongBan, " +
            "  SUM(ct.soLuong * ct.donGia) AS doanhThu " +
            "FROM ChiTietHoaDon ct " +
            "JOIN HoaDon hd ON hd.maHoaDon = ct.maHoaDon " +
            "JOIN DonViQuyDoi dv ON dv.maQuyDoi = ct.maQuyDoi " +
            "JOIN Thuoc t ON t.maThuoc = dv.maThuoc " +
            "LEFT JOIN DanhMucThuoc dm ON dm.maDanhMuc = t.maDanhMuc " +
            "WHERE CAST(hd.ngayLap AS DATE) >= ? " +
            "  AND CAST(hd.ngayLap AS DATE) <= ? ");
        if (hasFilter(danhMuc)) sql.append("AND dm.tenDanhMuc = ? ");
        sql.append("GROUP BY dm.tenDanhMuc ORDER BY doanhThu DESC");

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            ps.setDate(1, java.sql.Date.valueOf(tuNgay));
            ps.setDate(2, java.sql.Date.valueOf(denNgay));
            if (hasFilter(danhMuc)) ps.setString(3, danhMuc);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("tenDanhMuc", rs.getString("tenDanhMuc"));
                row.put("soLuongBan", rs.getInt("soLuongBan"));
                row.put("doanhThu",   rs.getDouble("doanhThu"));
                result.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    /** Xu hướng bán theo ngày */
    public List<Map<String, Object>> getXuHuongBanTheoNgay(LocalDate tuNgay,
            LocalDate denNgay, String danhMuc) {
        List<Map<String, Object>> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT CAST(hd.ngayLap AS DATE) AS ngay, " +
            "  SUM(ct.soLuong) AS soLuongBan, " +
            "  SUM(ct.soLuong * ct.donGia) AS doanhThu " +
            "FROM ChiTietHoaDon ct " +
            "JOIN HoaDon hd ON hd.maHoaDon = ct.maHoaDon " +
            "JOIN DonViQuyDoi dv ON dv.maQuyDoi = ct.maQuyDoi " +
            "JOIN Thuoc t ON t.maThuoc = dv.maThuoc " +
            "LEFT JOIN DanhMucThuoc dm ON dm.maDanhMuc = t.maDanhMuc " +
            "WHERE CAST(hd.ngayLap AS DATE) >= ? " +
            "  AND CAST(hd.ngayLap AS DATE) <= ? ");
        if (hasFilter(danhMuc)) sql.append("AND dm.tenDanhMuc = ? ");
        sql.append("GROUP BY CAST(hd.ngayLap AS DATE) ORDER BY ngay ASC");

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            ps.setDate(1, java.sql.Date.valueOf(tuNgay));
            ps.setDate(2, java.sql.Date.valueOf(denNgay));
            if (hasFilter(danhMuc)) ps.setString(3, danhMuc);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("ngay",       rs.getDate("ngay").toLocalDate());
                row.put("soLuongBan", rs.getInt("soLuongBan"));
                row.put("doanhThu",   rs.getDouble("doanhThu"));
                result.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    /** Top thuốc chậm bán (ít nhất trong kỳ) */
    public List<Map<String, Object>> getTopThuocChamBan(LocalDate tuNgay,
            LocalDate denNgay, String danhMuc, int top) {
        List<Map<String, Object>> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT TOP " + top +
            "  t.maThuoc, t.tenThuoc, " +
            "  ISNULL(dm.tenDanhMuc, N'Không xác định') AS tenDanhMuc, " +
            "  ISNULL(SUM(ct.soLuong), 0) AS soLuongBan, " +
            "  ISNULL(SUM(lt.soLuongTon), 0) AS tonKho " +
            "FROM Thuoc t " +
            "LEFT JOIN DanhMucThuoc dm ON dm.maDanhMuc = t.maDanhMuc " +
            "LEFT JOIN LoThuoc lt ON lt.maThuoc = t.maThuoc AND lt.trangThai = 1 " +
            "LEFT JOIN DonViQuyDoi dv ON dv.maThuoc = t.maThuoc " +
            "LEFT JOIN ChiTietHoaDon ct ON ct.maQuyDoi = dv.maQuyDoi " +
            "LEFT JOIN HoaDon hd ON hd.maHoaDon = ct.maHoaDon " +
            "  AND CAST(hd.ngayLap AS DATE) >= ? " +
            "  AND CAST(hd.ngayLap AS DATE) <= ? " +
            "WHERE t.trangThai = 'DANG_BAN' ");
        if (hasFilter(danhMuc)) sql.append("AND dm.tenDanhMuc = ? ");
        sql.append("GROUP BY t.maThuoc, t.tenThuoc, dm.tenDanhMuc " +
                   "HAVING ISNULL(SUM(lt.soLuongTon), 0) > 0 " +
                   "ORDER BY soLuongBan ASC, tonKho DESC");

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            ps.setDate(1, java.sql.Date.valueOf(tuNgay));
            ps.setDate(2, java.sql.Date.valueOf(denNgay));
            if (hasFilter(danhMuc)) ps.setString(3, danhMuc);
            ResultSet rs = ps.executeQuery();
            int stt = 1;
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("stt",        stt++);
                row.put("maThuoc",    rs.getString("maThuoc"));
                row.put("tenThuoc",   rs.getString("tenThuoc"));
                row.put("tenDanhMuc", rs.getString("tenDanhMuc"));
                row.put("soLuongBan", rs.getInt("soLuongBan"));
                row.put("tonKho",     rs.getInt("tonKho"));
                result.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    /** Lấy danh sách danh mục để filter (tái dùng từ DAO_ThongKeTonKho) */
    public List<String> getDanhMucThuoc() {
        List<String> result = new ArrayList<>();
        result.add("Tất cả");
        String sql = "SELECT tenDanhMuc FROM DanhMucThuoc ORDER BY tenDanhMuc";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(rs.getString("tenDanhMuc"));
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }
}