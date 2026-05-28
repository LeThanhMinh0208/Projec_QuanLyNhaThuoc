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
    private static final String ALL = "Tất cả";

    private boolean hasFilter(String value) {
        return value != null && !value.trim().isEmpty() && !ALL.equalsIgnoreCase(value.trim());
    }

    // 1. Lấy dữ liệu KPI Tổng Quan
    public Map<String, Object> getTongQuan(LocalDate tuNgay, LocalDate denNgay, String danhMuc) {
        String sql = "SELECT " +
                "COUNT(DISTINCT ct.maQuyDoi) AS tongMatHangDaBan, " +
                "ISNULL(SUM(ct.soLuong), 0) AS tongSoLuongBan, " +
                "ISNULL(SUM(ct.soLuong * ct.donGia), 0) AS tongDoanhThuGoc, " +
                "ISNULL(SUM(ct.soLuong * ct.donGia * (1 + hd.thueVAT/100.0)), 0) AS tongDoanhThuSauThue " +
                "FROM ChiTietHoaDon ct " +
                "JOIN HoaDon hd ON ct.maHoaDon = hd.maHoaDon " +
                "JOIN DonViQuyDoi dv ON ct.maQuyDoi = dv.maQuyDoi " +
                "JOIN Thuoc t ON dv.maThuoc = t.maThuoc " +
                "LEFT JOIN DanhMucThuoc dm ON t.maDanhMuc = dm.maDanhMuc " +
                "WHERE CAST(hd.ngayLap AS DATE) BETWEEN ? AND ? ";
        if (hasFilter(danhMuc)) {
            sql += "AND dm.tenDanhMuc = ? ";
        }

        Map<String, Object> result = new HashMap<>();
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(tuNgay));
            ps.setDate(2, java.sql.Date.valueOf(denNgay));
            if (hasFilter(danhMuc)) ps.setString(3, danhMuc);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    result.put("tongMatHangDaBan", rs.getInt("tongMatHangDaBan"));
                    result.put("tongSoLuongBan", rs.getInt("tongSoLuongBan"));
                    result.put("tongDoanhThuGoc", rs.getDouble("tongDoanhThuGoc"));
                    result.put("tongDoanhThuSauThue", rs.getDouble("tongDoanhThuSauThue"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // 2. Cơ cấu SỐ LƯỢNG tiêu thụ theo nhóm danh mục thuốc (PieChart mới)
    public List<Map<String, Object>> getSoLuongTieuThuTheoDanhMuc(LocalDate tuNgay, LocalDate denNgay, String danhMuc) {
        String sql = "SELECT ISNULL(dm.tenDanhMuc, N'Không xác định') AS tenDanhMuc, " +
                "ISNULL(SUM(ct.soLuong), 0) AS soLuong " +
                "FROM ChiTietHoaDon ct " +
                "JOIN HoaDon hd ON ct.maHoaDon = hd.maHoaDon " +
                "JOIN DonViQuyDoi dv ON ct.maQuyDoi = dv.maQuyDoi " +
                "JOIN Thuoc t ON dv.maThuoc = t.maThuoc " +
                "LEFT JOIN DanhMucThuoc dm ON t.maDanhMuc = dm.maDanhMuc " +
                "WHERE CAST(hd.ngayLap AS DATE) BETWEEN ? AND ? ";
        if (hasFilter(danhMuc)) {
            sql += "AND dm.tenDanhMuc = ? ";
        }
        sql += "GROUP BY dm.tenDanhMuc ORDER BY soLuong DESC";

        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(tuNgay));
            ps.setDate(2, java.sql.Date.valueOf(denNgay));
            if (hasFilter(danhMuc)) ps.setString(3, danhMuc);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("tenDanhMuc", rs.getString("tenDanhMuc"));
                    row.put("soLuong", rs.getInt("soLuong"));
                    result.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // 3. Top 10 sản phẩm BÁN ÍT NHẤT trong kỳ (dùng đúng bảng ChiTietHoaDon)
    public List<Map<String, Object>> getTopSanPhamBanItNhat(LocalDate tuNgay, LocalDate denNgay, String danhMuc, int top) {
        String sql = "SELECT TOP " + top + " t.maThuoc, t.tenThuoc, " +
                "ISNULL(dm.tenDanhMuc, N'Không xác định') AS tenDanhMuc, " +
                "dv.tenDonVi, SUM(ct.soLuong) AS soLuongBan " +
                "FROM ChiTietHoaDon ct " +
                "JOIN HoaDon hd ON ct.maHoaDon = hd.maHoaDon " +
                "JOIN DonViQuyDoi dv ON ct.maQuyDoi = dv.maQuyDoi " +
                "JOIN Thuoc t ON dv.maThuoc = t.maThuoc " +
                "LEFT JOIN DanhMucThuoc dm ON t.maDanhMuc = dm.maDanhMuc " +
                "WHERE CAST(hd.ngayLap AS DATE) BETWEEN ? AND ? ";
        if (hasFilter(danhMuc)) {
            sql += "AND dm.tenDanhMuc = ? ";
        }
        sql += "GROUP BY t.maThuoc, t.tenThuoc, dm.tenDanhMuc, dv.tenDonVi " +
               "ORDER BY soLuongBan ASC";

        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(tuNgay));
            ps.setDate(2, java.sql.Date.valueOf(denNgay));
            if (hasFilter(danhMuc)) ps.setString(3, danhMuc);

            try (ResultSet rs = ps.executeQuery()) {
                int stt = 1;
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("stt", stt++);
                    row.put("maThuoc", rs.getString("maThuoc"));
                    row.put("tenThuoc", rs.getString("tenThuoc"));
                    row.put("tenDanhMuc", rs.getString("tenDanhMuc"));
                    row.put("donVi", rs.getString("tenDonVi"));
                    row.put("soLuongBan", rs.getInt("soLuongBan"));
                    result.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // 4. Top 10 sản phẩm BỊ ĐỔI/TRẢ NHIỀU NHẤT trong kỳ
    public List<Map<String, Object>> getTopSanPhamBiDoiTra(LocalDate tuNgay, LocalDate denNgay, int top) {
        String sql = "SELECT TOP " + top + " t.maThuoc, t.tenThuoc, " +
                "ISNULL(dm.tenDanhMuc, N'Không xác định') AS tenDanhMuc, " +
                "dv.tenDonVi, " +
                "SUM(ct.soLuong) AS soLuongDoiTra, " +
                "COUNT(DISTINCT pdt.maPhieuDoiTra) AS soPhieu " +
                "FROM ChiTietDoiTra ct " +
                "JOIN PhieuDoiTra pdt ON pdt.maPhieuDoiTra = ct.maPhieuDoiTra " +
                "JOIN DonViQuyDoi dv ON dv.maQuyDoi = ct.maQuyDoi " +
                "JOIN Thuoc t ON t.maThuoc = dv.maThuoc " +
                "LEFT JOIN DanhMucThuoc dm ON dm.maDanhMuc = t.maDanhMuc " +
                "WHERE CAST(pdt.ngayDoiTra AS DATE) BETWEEN ? AND ? " +
                "GROUP BY t.maThuoc, t.tenThuoc, dm.tenDanhMuc, dv.tenDonVi " +
                "ORDER BY soLuongDoiTra DESC, soPhieu DESC";

        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(tuNgay));
            ps.setDate(2, java.sql.Date.valueOf(denNgay));

            try (ResultSet rs = ps.executeQuery()) {
                int stt = 1;
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("stt", stt++);
                    row.put("maThuoc", rs.getString("maThuoc"));
                    row.put("tenThuoc", rs.getString("tenThuoc"));
                    row.put("tenDanhMuc", rs.getString("tenDanhMuc"));
                    row.put("donVi", rs.getString("tenDonVi"));
                    row.put("soLuongDoiTra", rs.getInt("soLuongDoiTra"));
                    row.put("soPhieu", rs.getInt("soPhieu"));
                    result.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // 5. Top sản phẩm bán chạy nhất - giữ lại cho 2 bảng TableView chi tiết
    public List<Map<String, Object>> getTopSanPhamBanChay(LocalDate tuNgay, LocalDate denNgay, String danhMuc, int top) {
        String sql = "SELECT TOP " + top + " t.maThuoc, t.tenThuoc, ISNULL(dm.tenDanhMuc, N'Không xác định') AS tenDanhMuc, " +
                "dv.tenDonVi, SUM(ct.soLuong) AS soLuongBan, " +
                "SUM(ct.soLuong * ct.donGia * (1 + hd.thueVAT/100.0)) AS doanhThu " +
                "FROM ChiTietHoaDon ct " +
                "JOIN HoaDon hd ON ct.maHoaDon = hd.maHoaDon " +
                "JOIN DonViQuyDoi dv ON ct.maQuyDoi = dv.maQuyDoi " +
                "JOIN Thuoc t ON dv.maThuoc = t.maThuoc " +
                "LEFT JOIN DanhMucThuoc dm ON t.maDanhMuc = dm.maDanhMuc " +
                "WHERE CAST(hd.ngayLap AS DATE) BETWEEN ? AND ? ";
        if (hasFilter(danhMuc)) {
            sql += "AND dm.tenDanhMuc = ? ";
        }
        sql += "GROUP BY t.maThuoc, t.tenThuoc, dm.tenDanhMuc, dv.tenDonVi " +
                "ORDER BY soLuongBan DESC, doanhThu DESC";

        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(tuNgay));
            ps.setDate(2, java.sql.Date.valueOf(denNgay));
            if (hasFilter(danhMuc)) ps.setString(3, danhMuc);

            try (ResultSet rs = ps.executeQuery()) {
                int stt = 1;
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("stt", stt++);
                    row.put("maThuoc", rs.getString("maThuoc"));
                    row.put("tenThuoc", rs.getString("tenThuoc"));
                    row.put("tenDanhMuc", rs.getString("tenDanhMuc"));
                    row.put("donVi", rs.getString("tenDonVi"));
                    row.put("soLuongBan", rs.getInt("soLuongBan"));
                    row.put("doanhThu", rs.getDouble("doanhThu"));
                    result.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}