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

public class DAO_ThongKeDoanhThu {

    private boolean hasFilter(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim();
        return !normalized.isEmpty() && !"Tất cả".equalsIgnoreCase(normalized) && !"TẤT CẢ".equalsIgnoreCase(normalized);
    }

    /**
     * Lấy tổng doanh thu trong khoảng thời gian
     */
    public double getTongDoanhThu(LocalDate tuNgay, LocalDate denNgay, String loaiBan, String hinhThuc) {
        String sql = "SELECT ISNULL(SUM(ISNULL(ct.soLuong * ct.donGia, 0) * " +
                "CASE WHEN hd.thueVAT > 0 THEN (1 + hd.thueVAT/100) ELSE 1 END), 0) as tongDoanhThu " +
                "FROM HoaDon hd " +
                "LEFT JOIN ChiTietHoaDon ct ON hd.maHoaDon = ct.maHoaDon " +
                "WHERE CAST(hd.ngayLap AS DATE) >= ? AND CAST(hd.ngayLap AS DATE) <= ? ";

        if (hasFilter(loaiBan)) {
            sql += "AND hd.loaiBan = ? ";
        }
        if (hasFilter(hinhThuc)) {
            sql += "AND hd.hinhThucThanhToan = ? ";
        }

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(tuNgay));
            ps.setDate(2, java.sql.Date.valueOf(denNgay));

            int paramIndex = 3;
            if (hasFilter(loaiBan)) {
                ps.setString(paramIndex++, loaiBan);
            }
            if (hasFilter(hinhThuc)) {
                ps.setString(paramIndex++, hinhThuc);
            }

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("tongDoanhThu");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Lấy tổng số đơn hàng
     */
    public int getTongDonHang(LocalDate tuNgay, LocalDate denNgay, String loaiBan, String hinhThuc) {
        String sql = "SELECT COUNT(*) as tongDon FROM HoaDon " +
                "WHERE CAST(ngayLap AS DATE) >= ? AND CAST(ngayLap AS DATE) <= ? ";

        if (hasFilter(loaiBan)) {
            sql += "AND loaiBan = ? ";
        }
        if (hasFilter(hinhThuc)) {
            sql += "AND hinhThucThanhToan = ? ";
        }

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(tuNgay));
            ps.setDate(2, java.sql.Date.valueOf(denNgay));

            int paramIndex = 3;
            if (hasFilter(loaiBan)) {
                ps.setString(paramIndex++, loaiBan);
            }
            if (hasFilter(hinhThuc)) {
                ps.setString(paramIndex++, hinhThuc);
            }

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("tongDon");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Lấy giá trị trung bình mỗi đơn hàng
     */
    public double getGiaTrungBinh(LocalDate tuNgay, LocalDate denNgay, String loaiBan, String hinhThuc) {
        double tongDoanhThu = getTongDoanhThu(tuNgay, denNgay, loaiBan, hinhThuc);
        int tongDonHang = getTongDonHang(tuNgay, denNgay, loaiBan, hinhThuc);

        if (tongDonHang == 0) {
			return 0;
		}
        return tongDoanhThu / tongDonHang;
    }

    /**
     * Lấy số khách hàng trong khoảng thời gian
     */
    public int getSoKhachHang(LocalDate tuNgay, LocalDate denNgay) {
        return getSoKhachHang(tuNgay, denNgay, null, null);
    }

    public int getSoKhachHang(LocalDate tuNgay, LocalDate denNgay, String loaiBan, String hinhThuc) {
        String sql = "SELECT COUNT(DISTINCT maKhachHang) as soKH FROM HoaDon " +
                "WHERE CAST(ngayLap AS DATE) >= ? AND CAST(ngayLap AS DATE) <= ? ";

        if (hasFilter(loaiBan)) {
            sql += "AND loaiBan = ? ";
        }
        if (hasFilter(hinhThuc)) {
            sql += "AND hinhThucThanhToan = ? ";
        }

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(tuNgay));
            ps.setDate(2, java.sql.Date.valueOf(denNgay));

            int paramIndex = 3;
            if (hasFilter(loaiBan)) {
                ps.setString(paramIndex++, loaiBan);
            }
            if (hasFilter(hinhThuc)) {
                ps.setString(paramIndex++, hinhThuc);
            }

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("soKH");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Lấy doanh thu theo ngày trong tháng
     */
    public List<Map<String, Object>> getDoanhThuTheoNgay(LocalDate tuNgay, LocalDate denNgay) {
        return getDoanhThuTheoNgay(tuNgay, denNgay, null, null);
    }

    public List<Map<String, Object>> getDoanhThuTheoNgay(LocalDate tuNgay, LocalDate denNgay, String loaiBan, String hinhThuc) {
        String sql = "WITH ChiTietSum AS (" +
                "    SELECT maHoaDon, SUM(soLuong * donGia) as tongTien FROM ChiTietHoaDon GROUP BY maHoaDon" +
                ")" +
                "SELECT CAST(hd.ngayLap AS DATE) as ngay, " +
                "ISNULL(SUM(CASE WHEN hd.thueVAT > 0 THEN cts.tongTien * (1 + hd.thueVAT/100) ELSE cts.tongTien END), 0) as doanhThu " +
                "FROM HoaDon hd " +
                "LEFT JOIN ChiTietSum cts ON hd.maHoaDon = cts.maHoaDon " +
                "WHERE CAST(hd.ngayLap AS DATE) >= ? AND CAST(hd.ngayLap AS DATE) <= ? ";

        if (hasFilter(loaiBan)) {
            sql += "AND hd.loaiBan = ? ";
        }
        if (hasFilter(hinhThuc)) {
            sql += "AND hd.hinhThucThanhToan = ? ";
        }

        sql += "GROUP BY CAST(hd.ngayLap AS DATE) " +
                "ORDER BY ngay ASC";

        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(tuNgay));
            ps.setDate(2, java.sql.Date.valueOf(denNgay));

            int paramIndex = 3;
            if (hasFilter(loaiBan)) {
                ps.setString(paramIndex++, loaiBan);
            }
            if (hasFilter(hinhThuc)) {
                ps.setString(paramIndex++, hinhThuc);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("ngay", rs.getDate("ngay").toLocalDate());
                row.put("doanhThu", rs.getDouble("doanhThu"));
                result.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Lấy cơ cấu doanh thu theo nhóm thuốc
     */
    public List<Map<String, Object>> getCoCAuDoanhThu(LocalDate tuNgay, LocalDate denNgay) {
        return getCoCAuDoanhThu(tuNgay, denNgay, null, null);
    }

    public List<Map<String, Object>> getCoCAuDoanhThu(LocalDate tuNgay, LocalDate denNgay, String loaiBan, String hinhThuc) {
        String sql = "WITH ChiTietSum AS (" +
                "    SELECT maHoaDon, maThuoc, SUM(soLuong * donGia) as tongTien FROM ChiTietHoaDon ct " +
                "    INNER JOIN DonViQuyDoi dv ON ct.maQuyDoi = dv.maQuyDoi GROUP BY maHoaDon, maThuoc" +
                ")" +
                "SELECT ISNULL(dm.tenDanhMuc, 'Không xác định') as nhomThuoc, " +
                "ISNULL(SUM(CASE WHEN hd.thueVAT > 0 THEN cts.tongTien * (1 + hd.thueVAT/100) ELSE cts.tongTien END), 0) as doanhThu " +
                "FROM HoaDon hd " +
                "LEFT JOIN ChiTietSum cts ON hd.maHoaDon = cts.maHoaDon " +
                "LEFT JOIN Thuoc t ON cts.maThuoc = t.maThuoc " +
                "LEFT JOIN DanhMucThuoc dm ON t.maDanhMuc = dm.maDanhMuc " +
                "WHERE CAST(hd.ngayLap AS DATE) >= ? AND CAST(hd.ngayLap AS DATE) <= ? ";

        if (hasFilter(loaiBan)) {
            sql += "AND hd.loaiBan = ? ";
        }
        if (hasFilter(hinhThuc)) {
            sql += "AND hd.hinhThucThanhToan = ? ";
        }

        sql += "GROUP BY dm.tenDanhMuc " +
                "ORDER BY doanhThu DESC";

        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(tuNgay));
            ps.setDate(2, java.sql.Date.valueOf(denNgay));

            int paramIndex = 3;
            if (hasFilter(loaiBan)) {
                ps.setString(paramIndex++, loaiBan);
            }
            if (hasFilter(hinhThuc)) {
                ps.setString(paramIndex++, hinhThuc);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("nhomThuoc", rs.getString("nhomThuoc"));
                row.put("doanhThu", rs.getDouble("doanhThu"));
                result.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Lấy top 10 sản phẩm bán chạy nhất
     */
    public List<Map<String, Object>> getTopSanPham(LocalDate tuNgay, LocalDate denNgay, int top) {
        return getTopSanPham(tuNgay, denNgay, null, null, top);
    }

    public List<Map<String, Object>> getTopSanPham(LocalDate tuNgay, LocalDate denNgay, String loaiBan, String hinhThuc, int top) {
        String sql = "SELECT TOP " + top + " " +
                "Thuoc.tenThuoc, " +
                "ISNULL(SUM(ChiTietHoaDon.soLuong), 0) as soLuong, " +
                "ISNULL(SUM(ChiTietHoaDon.soLuong * ChiTietHoaDon.donGia), 0) as doanhThu " +
                "FROM ChiTietHoaDon " +
                "INNER JOIN HoaDon ON ChiTietHoaDon.maHoaDon = HoaDon.maHoaDon " +
                "INNER JOIN DonViQuyDoi ON ChiTietHoaDon.maQuyDoi = DonViQuyDoi.maQuyDoi " +
                "INNER JOIN Thuoc ON DonViQuyDoi.maThuoc = Thuoc.maThuoc " +
                "WHERE CAST(HoaDon.ngayLap AS DATE) >= ? AND CAST(HoaDon.ngayLap AS DATE) <= ? ";

        if (hasFilter(loaiBan)) {
            sql += "AND HoaDon.loaiBan = ? ";
        }
        if (hasFilter(hinhThuc)) {
            sql += "AND HoaDon.hinhThucThanhToan = ? ";
        }

        sql += "GROUP BY Thuoc.tenThuoc " +
                "ORDER BY soLuong DESC";

        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(tuNgay));
            ps.setDate(2, java.sql.Date.valueOf(denNgay));

            int paramIndex = 3;
            if (hasFilter(loaiBan)) {
                ps.setString(paramIndex++, loaiBan);
            }
            if (hasFilter(hinhThuc)) {
                ps.setString(paramIndex++, hinhThuc);
            }

            ResultSet rs = ps.executeQuery();
            int stt = 1;
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("stt", stt++);
                row.put("tenThuoc", rs.getString("tenThuoc"));
                row.put("soLuong", rs.getInt("soLuong"));
                row.put("doanhThu", rs.getDouble("doanhThu"));
                result.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Lấy top 5 khách hàng mua nhiều nhất
     */
    public List<Map<String, Object>> getTopKhachHang(LocalDate tuNgay, LocalDate denNgay, int top) {
        return getTopKhachHang(tuNgay, denNgay, null, null, top);
    }

    public List<Map<String, Object>> getTopKhachHang(LocalDate tuNgay, LocalDate denNgay, String loaiBan, String hinhThuc, int top) {
        String sql = "WITH ChiTietSum AS (" +
                "    SELECT maHoaDon, SUM(soLuong * donGia) as tongTien FROM ChiTietHoaDon GROUP BY maHoaDon" +
                ")" +
                "SELECT TOP " + top + " " +
                "ISNULL(kh.hoTen, 'Khách lẻ') as tenKhachHang, " +
                "COUNT(hd.maHoaDon) as soDon, " +
                "ISNULL(SUM(CASE WHEN hd.thueVAT > 0 THEN cts.tongTien * (1 + hd.thueVAT/100) ELSE cts.tongTien END), 0) as doanhThu " +
                "FROM HoaDon hd " +
                "LEFT JOIN KhachHang kh ON hd.maKhachHang = kh.maKhachHang " +
                "LEFT JOIN ChiTietSum cts ON hd.maHoaDon = cts.maHoaDon " +
                "WHERE CAST(hd.ngayLap AS DATE) >= ? AND CAST(hd.ngayLap AS DATE) <= ? ";

        if (hasFilter(loaiBan)) {
            sql += "AND hd.loaiBan = ? ";
        }
        if (hasFilter(hinhThuc)) {
            sql += "AND hd.hinhThucThanhToan = ? ";
        }

        sql += "GROUP BY kh.hoTen " +
                "ORDER BY doanhThu DESC";

        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(tuNgay));
            ps.setDate(2, java.sql.Date.valueOf(denNgay));

            int paramIndex = 3;
            if (hasFilter(loaiBan)) {
                ps.setString(paramIndex++, loaiBan);
            }
            if (hasFilter(hinhThuc)) {
                ps.setString(paramIndex++, hinhThuc);
            }

            ResultSet rs = ps.executeQuery();
            int stt = 1;
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("stt", stt++);
                row.put("tenKhachHang", rs.getString("tenKhachHang") != null ? rs.getString("tenKhachHang") : "Khách lẻ");
                row.put("soDon", rs.getInt("soDon"));
                row.put("doanhThu", rs.getDouble("doanhThu"));
                result.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Thống kê số đơn theo hình thức thanh toán
     */
    public List<Map<String, Object>> getThongKeHinhThuc(LocalDate tuNgay, LocalDate denNgay, String loaiBan, String hinhThuc) {
        String sql = "SELECT ISNULL(hd.hinhThucThanhToan, 'KHAC') as hinhThuc, COUNT(*) as soDon " +
                "FROM HoaDon hd " +
                "WHERE CAST(hd.ngayLap AS DATE) >= ? AND CAST(hd.ngayLap AS DATE) <= ? ";

        if (hasFilter(loaiBan)) {
            sql += "AND hd.loaiBan = ? ";
        }
        if (hasFilter(hinhThuc)) {
            sql += "AND hd.hinhThucThanhToan = ? ";
        }

        sql += "GROUP BY hd.hinhThucThanhToan " +
                "ORDER BY soDon DESC";

        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(tuNgay));
            ps.setDate(2, java.sql.Date.valueOf(denNgay));

            int paramIndex = 3;
            if (hasFilter(loaiBan)) {
                ps.setString(paramIndex++, loaiBan);
            }
            if (hasFilter(hinhThuc)) {
                ps.setString(paramIndex++, hinhThuc);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("hinhThuc", rs.getString("hinhThuc"));
                row.put("soDon", rs.getInt("soDon"));
                result.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Lấy sản phẩm "dead" - tồn kho không chuyển động
     */
    public List<Map<String, Object>> getProductDead(int ngayKhongBan) {
        String sql = "SELECT TOP 10 " +
                "t.tenThuoc, " +
                "ISNULL(CONVERT(VARCHAR, t.maThuoc), 'N/A') as nhomThuoc, " +
                "ISNULL(SUM(lt.soLuongTon), 0) as tonKho, " +
                "ISNULL(DATEDIFF(DAY, MAX(hd.ngayLap), CAST(GETDATE() AS DATE)), " + ngayKhongBan + ") as soNgayKhongBan " +
                "FROM Thuoc t " +
                "LEFT JOIN LoThuoc lt ON t.maThuoc = lt.maThuoc AND lt.trangThai = 1 " +
                "LEFT JOIN (SELECT DISTINCT dv.maThuoc, MAX(hd.ngayLap) as ngayLap " +
                "    FROM ChiTietHoaDon ct " +
                "    INNER JOIN HoaDon hd ON ct.maHoaDon = hd.maHoaDon " +
                "    INNER JOIN DonViQuyDoi dv ON ct.maQuyDoi = dv.maQuyDoi " +
                "    GROUP BY dv.maThuoc) hd ON t.maThuoc = hd.maThuoc " +
                "GROUP BY t.maThuoc, t.tenThuoc " +
                "HAVING ISNULL(SUM(lt.soLuongTon), 0) > 0 AND " +
                "ISNULL(DATEDIFF(DAY, MAX(hd.ngayLap), CAST(GETDATE() AS DATE)), " + ngayKhongBan + ") >= ? " +
                "ORDER BY soNgayKhongBan DESC";

        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, ngayKhongBan);

            ResultSet rs = ps.executeQuery();
            int stt = 1;
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("stt", stt++);
                row.put("tenThuoc", rs.getString("tenThuoc"));
                row.put("nhomThuoc", rs.getString("nhomThuoc"));
                row.put("soNgayKhongBan", rs.getInt("soNgayKhongBan"));
                row.put("tonKho", rs.getInt("tonKho"));
                result.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
