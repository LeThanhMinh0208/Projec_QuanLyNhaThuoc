package dao;

import connectDB.ConnectDB;
import entity.ChiTietDoiTra;
import entity.ChiTietDoiTraView;
import entity.PhieuDoiTra;
import entity.PhieuDoiTraView;
import utils.DoiTraSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DAO_PhieuDoiTra {
    public String generateMaPhieuDoiTra() {
        String sql = "SELECT MAX(CAST(SUBSTRING(maPhieuDoiTra, 4, LEN(maPhieuDoiTra)) AS INT)) FROM PhieuDoiTra";
        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return String.format("PDT%04d", rs.getInt(1) + 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "PDT0001";
    }

    public List<ChiTietDoiTraView> getChiTietCoTheDoiTra(String maHoaDon) {
        List<ChiTietDoiTraView> list = new ArrayList<>();
        String sql =
                "SELECT ct.maQuyDoi, ct.maLoThuoc, t.tenThuoc, dv.tenDonVi, lo.hanSuDung, " +
                "       SUM(ct.soLuong) AS soLuongDaMua, " +
                "       ISNULL((SELECT SUM(dt.soLuong) " +
                "               FROM ChiTietDoiTra dt " +
                "               JOIN PhieuDoiTra pdt ON pdt.maPhieuDoiTra = dt.maPhieuDoiTra " +
                "               WHERE pdt.maHoaDon = ct.maHoaDon " +
                "                 AND dt.maQuyDoi = ct.maQuyDoi " +
                "                 AND dt.maLoThuoc = ct.maLoThuoc), 0) AS soLuongDaTra, " +
                "       MAX(ct.donGia) * (1 + hd.thueVAT / 100.0) AS donGia " +
                "FROM ChiTietHoaDon ct " +
                "JOIN HoaDon hd ON hd.maHoaDon = ct.maHoaDon " +
                "JOIN DonViQuyDoi dv ON dv.maQuyDoi = ct.maQuyDoi " +
                "JOIN Thuoc t ON t.maThuoc = dv.maThuoc " +
                "JOIN LoThuoc lo ON lo.maLoThuoc = ct.maLoThuoc " +
                "WHERE ct.maHoaDon = ? " +
                "GROUP BY ct.maHoaDon, hd.thueVAT, ct.maQuyDoi, ct.maLoThuoc, t.tenThuoc, dv.tenDonVi, lo.hanSuDung " +
                "ORDER BY t.tenThuoc, lo.hanSuDung";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maHoaDon);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                ChiTietDoiTraView item = new ChiTietDoiTraView();
                item.setMaQuyDoi(rs.getString("maQuyDoi"));
                item.setMaLoThuoc(rs.getString("maLoThuoc"));
                item.setTenThuoc(rs.getString("tenThuoc"));
                item.setTenDonVi(rs.getString("tenDonVi"));
                item.setHanSuDung(rs.getDate("hanSuDung"));
                item.setSoLuongDaMua(rs.getInt("soLuongDaMua"));
                item.setSoLuongDaTra(rs.getInt("soLuongDaTra"));
                item.setDonGia(rs.getDouble("donGia"));
                list.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<PhieuDoiTraView> getAllPhieuDoiTra(String tuKhoa) {
        List<PhieuDoiTraView> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT pdt.maPhieuDoiTra, pdt.ngayDoiTra, pdt.maHoaDon, pdt.hinhThucXuLy, pdt.phiPhat, pdt.lyDo, " +
                "       pdt.ketQuaDoiSanPham, pdt.danhSachThuocDoi, " +
                "       kh.hoTen AS tenKhachHang, nv.hoTen AS tenNhanVien " +
                "FROM PhieuDoiTra pdt " +
                "JOIN HoaDon hd ON hd.maHoaDon = pdt.maHoaDon " +
                "LEFT JOIN KhachHang kh ON kh.maKhachHang = hd.maKhachHang " +
                "JOIN NhanVien nv ON nv.maNhanVien = pdt.maNhanVien " +
                "WHERE 1=1");

        List<Object> params = new ArrayList<>();
        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            sql.append(" AND (pdt.maPhieuDoiTra LIKE ? OR pdt.maHoaDon LIKE ? OR kh.hoTen LIKE ? OR nv.hoTen LIKE ?)");
            String kw = "%" + tuKhoa.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        sql.append(" ORDER BY pdt.ngayDoiTra DESC");

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pst.setObject(i + 1, params.get(i));
            }
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                PhieuDoiTraView item = new PhieuDoiTraView();
                item.setMaPhieuDoiTra(rs.getString("maPhieuDoiTra"));
                item.setNgayDoiTra(rs.getTimestamp("ngayDoiTra"));
                item.setMaHoaDon(rs.getString("maHoaDon"));
                item.setTenKhachHang(rs.getString("tenKhachHang"));
                item.setTenNhanVien(rs.getString("tenNhanVien"));
                item.setHinhThucXuLy(rs.getString("hinhThucXuLy"));
                item.setPhiPhat(rs.getDouble("phiPhat"));
                item.setLyDo(rs.getString("lyDo"));
                item.setKetQuaDoiSanPham(rs.getString("ketQuaDoiSanPham"));
                item.setDanhSachThuocDoi(rs.getString("danhSachThuocDoi"));
                list.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Object[]> getChiTietByMaPhieuDoiTra(String maPhieuDoiTra) {
        List<Object[]> list = new ArrayList<>();
        String sql =
                "SELECT t.tenThuoc, dv.tenDonVi, ct.maLoThuoc, lo.hanSuDung, SUM(ct.soLuong) AS soLuong, " +
                "       MAX(hdct.donGia) AS donGia, SUM(ct.soLuong * hdct.donGia) AS thanhTien " +
                "FROM ChiTietDoiTra ct " +
                "JOIN DonViQuyDoi dv ON dv.maQuyDoi = ct.maQuyDoi " +
                "JOIN Thuoc t ON t.maThuoc = dv.maThuoc " +
                "JOIN LoThuoc lo ON lo.maLoThuoc = ct.maLoThuoc " +
                "JOIN PhieuDoiTra pdt ON pdt.maPhieuDoiTra = ct.maPhieuDoiTra " +
                "JOIN ChiTietHoaDon hdct ON hdct.maHoaDon = pdt.maHoaDon AND hdct.maQuyDoi = ct.maQuyDoi AND hdct.maLoThuoc = ct.maLoThuoc " +
                "WHERE ct.maPhieuDoiTra = ? " +
                "GROUP BY t.tenThuoc, dv.tenDonVi, ct.maLoThuoc, lo.hanSuDung " +
                "ORDER BY t.tenThuoc";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maPhieuDoiTra);
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

    public boolean lapPhieuDoiTra(PhieuDoiTra pdt, List<ChiTietDoiTra> dsChiTiet, List<DoiTraSession.DonViDoiData> dsThuocDoi) {
        if (pdt == null || pdt.getHoaDon() == null || pdt.getNhanVien() == null || dsChiTiet == null || dsChiTiet.isEmpty()) {
            return false;
        }

        List<ChiTietDoiTra> chiTietHopLe = gopChiTiet(dsChiTiet);
        if (chiTietHopLe.isEmpty()) {
            return false;
        }

        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            // Serialize dsThuocDoi to JSON-like string
            String danhSachThuocDoiJson = null;
            if (dsThuocDoi != null && !dsThuocDoi.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (DoiTraSession.DonViDoiData td : dsThuocDoi) {
                    if (sb.length() > 0) sb.append(";");
                    sb.append(td.getMaQuyDoi()).append("|")
                      .append(td.getTenThuoc()).append("|")
                      .append(td.getTenDonVi()).append("|")
                      .append(td.getSoLuong()).append("|")
                      .append(td.getDonGia());
                }
                danhSachThuocDoiJson = sb.toString();
            }
            pdt.setDanhSachThuocDoi(danhSachThuocDoiJson);

            // --- INSERT PhieuDoiTra (với retry cho race condition mã phiếu) ---
            String sqlPhieu =
                    "INSERT INTO PhieuDoiTra(maPhieuDoiTra, maHoaDon, maNhanVien, ngayDoiTra, lyDo, hinhThucXuLy, phiPhat, ketQuaDoiSanPham, danhSachThuocDoi) VALUES(?,?,?,?,?,?,?,?,?)";
            int retry = 0;
            while (retry < 3) {
                try (PreparedStatement pst = con.prepareStatement(sqlPhieu)) {
                    pst.setString(1, pdt.getMaPhieuDoiTra());
                    pst.setString(2, pdt.getHoaDon().getMaHoaDon());
                    pst.setString(3, pdt.getNhanVien().getMaNhanVien());
                    pst.setTimestamp(4, new java.sql.Timestamp(pdt.getNgayDoiTra().getTime()));
                    pst.setString(5, pdt.getLyDo());
                    pst.setString(6, pdt.getHinhThucXuLy().name());
                    pst.setDouble(7, pdt.getPhiPhat());
                    pst.setString(8, pdt.getKetQuaDoiSanPham());
                    pst.setString(9, pdt.getDanhSachThuocDoi());
                    pst.executeUpdate();
                    break;
                } catch (SQLException e) {
                    if ((e.getErrorCode() == 2627 || e.getErrorCode() == 2601) && retry < 2) {
                        pdt.setMaPhieuDoiTra(generateMaPhieuDoiTra());
                        retry++;
                    } else {
                        con.rollback();
                        con.setAutoCommit(true);
                        return false;
                    }
                }
            }

            // --- INSERT ChiTietDoiTra + cộng tồn thuốc trả ---
            String sqlChiTiet = "INSERT INTO ChiTietDoiTra(maPhieuDoiTra, maQuyDoi, maLoThuoc, soLuong, tinhTrang) VALUES(?,?,?,?,?)";
            String sqlCongTon = "UPDATE LoThuoc SET soLuongTon = soLuongTon + ? WHERE maLoThuoc = ?";

            for (ChiTietDoiTra ct : chiTietHopLe) {
                int soLuongConLai = getSoLuongConLaiCoTheDoi(con, pdt.getHoaDon().getMaHoaDon(), ct.getMaQuyDoi(), ct.getMaLoThuoc());
                if (ct.getSoLuong() <= 0 || ct.getSoLuong() > soLuongConLai) {
                    con.rollback();
                    con.setAutoCommit(true);
                    return false;
                }

                try (PreparedStatement pst = con.prepareStatement(sqlChiTiet)) {
                    pst.setString(1, ct.getMaPhieuDoiTra());
                    pst.setString(2, ct.getMaQuyDoi());
                    pst.setString(3, ct.getMaLoThuoc());
                    pst.setInt(4, ct.getSoLuong());
                    pst.setString(5, ct.getTinhTrang());
                    pst.executeUpdate();
                }

                int tyLeQuyDoi = getTyLeQuyDoi(con, ct.getMaQuyDoi());
                try (PreparedStatement pst = con.prepareStatement(sqlCongTon)) {
                    pst.setInt(1, ct.getSoLuong() * tyLeQuyDoi);
                    pst.setString(2, ct.getMaLoThuoc());
                    pst.executeUpdate();
                }
            }

            // --- Trừ tồn kho thuốc đổi cho khách (DOI_SAN_PHAM - Hỗ trợ trừ nhiều lô FEFO) ---
            if (dsThuocDoi != null && !dsThuocDoi.isEmpty()) {
                for (DoiTraSession.DonViDoiData thuocDoi : dsThuocDoi) {
                    int tyLe = getTyLeQuyDoi(con, thuocDoi.getMaQuyDoi());
                    int soLuongPhaiTruCoBan = thuocDoi.getSoLuong() * tyLe;

                    String maThuoc = getMaThuocByMaQuyDoi(con, thuocDoi.getMaQuyDoi());
                    if (maThuoc == null) {
                        con.rollback();
                        con.setAutoCommit(true);
                        return false;
                    }

                    // Lấy tất cả lô FEFO có thể bán lưu vào một list riêng biệt trước
                    String sqlLayLo = "SELECT maLoThuoc, soLuongTon FROM LoThuoc " +
                                     "WHERE maThuoc = ? AND viTriKho = 'KHO_BAN_HANG' " +
                                     "AND soLuongTon > 0 AND trangThai = 1 " +
                                     "AND hanSuDung >= CAST(GETDATE() AS DATE) " +
                                     "ORDER BY hanSuDung ASC";
                    
                    List<String> listMaLo = new ArrayList<>();
                    List<Integer> listTonLo = new ArrayList<>();
                    
                    try (PreparedStatement pstLo = con.prepareStatement(sqlLayLo)) {
                        pstLo.setString(1, maThuoc);
                        try (ResultSet rsLo = pstLo.executeQuery()) {
                            while (rsLo.next()) {
                                listMaLo.add(rsLo.getString("maLoThuoc"));
                                listTonLo.add(rsLo.getInt("soLuongTon"));
                            }
                        }
                    }

                    int soLuongDaTru = 0;
                    for (int i = 0; i < listMaLo.size() && soLuongDaTru < soLuongPhaiTruCoBan; i++) {
                        String maLo = listMaLo.get(i);
                        int tonLo = listTonLo.get(i);
                        int canTru = Math.min(tonLo, soLuongPhaiTruCoBan - soLuongDaTru);
                        
                        // Trừ tồn kho tại lô này
                        String sqlTruTon = "UPDATE LoThuoc SET soLuongTon = soLuongTon - ? WHERE maLoThuoc = ?";
                        try (PreparedStatement pstTru = con.prepareStatement(sqlTruTon)) {
                            pstTru.setInt(1, canTru);
                            pstTru.setString(2, maLo);
                            pstTru.executeUpdate();
                        }
                        soLuongDaTru += canTru;
                    }

                    // Nếu sau khi quét hết các lô mà vẫn chưa trừ đủ số lượng -> rollback
                    if (soLuongDaTru < soLuongPhaiTruCoBan) {
                        con.rollback();
                        con.setAutoCommit(true);
                        return false;
                    }
                }
            }

            con.commit();
            con.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                    con.setAutoCommit(true);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        }
    }

    // Overload giữ tương thích cũ (Dialog chỉ dùng HOAN_TIEN, không truyền dsThuocDoi)
    public boolean lapPhieuDoiTra(PhieuDoiTra pdt, List<ChiTietDoiTra> dsChiTiet) {
        return lapPhieuDoiTra(pdt, dsChiTiet, null);
    }

    private String getMaThuocByMaQuyDoi(Connection con, String maQuyDoi) throws SQLException {
        String sql = "SELECT maThuoc FROM DonViQuyDoi WHERE maQuyDoi = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maQuyDoi);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getString("maThuoc");
            }
        }
        return null;
    }

    private String getLoFEFO(Connection con, String maThuoc) throws SQLException {
        String sql = "SELECT TOP 1 maLoThuoc FROM LoThuoc " +
                     "WHERE maThuoc = ? AND viTriKho = 'KHO_BAN_HANG' " +
                     "AND soLuongTon > 0 AND trangThai = 1 " +
                     "AND hanSuDung >= CAST(GETDATE() AS DATE) " +
                     "ORDER BY hanSuDung ASC";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maThuoc);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getString("maLoThuoc");
            }
        }
        return null;
    }

    private int getSoLuongTon(Connection con, String maLoThuoc) throws SQLException {
        String sql = "SELECT soLuongTon FROM LoThuoc WHERE maLoThuoc = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maLoThuoc);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("soLuongTon");
            }
        }
        return 0;
    }

    private int getSoLuongConLaiCoTheDoi(Connection con, String maHoaDon, String maQuyDoi, String maLoThuoc) throws SQLException {
        String sqlDaMua = "SELECT ISNULL(SUM(soLuong), 0) FROM ChiTietHoaDon WHERE maHoaDon = ? AND maQuyDoi = ? AND maLoThuoc = ?";
        String sqlDaTra =
                "SELECT ISNULL(SUM(dt.soLuong), 0) FROM ChiTietDoiTra dt " +
                "JOIN PhieuDoiTra pdt ON pdt.maPhieuDoiTra = dt.maPhieuDoiTra " +
                "WHERE pdt.maHoaDon = ? AND dt.maQuyDoi = ? AND dt.maLoThuoc = ?";

        int daMua = 0;
        int daTra = 0;

        try (PreparedStatement pst = con.prepareStatement(sqlDaMua)) {
            pst.setString(1, maHoaDon);
            pst.setString(2, maQuyDoi);
            pst.setString(3, maLoThuoc);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                daMua = rs.getInt(1);
            }
        }

        try (PreparedStatement pst = con.prepareStatement(sqlDaTra)) {
            pst.setString(1, maHoaDon);
            pst.setString(2, maQuyDoi);
            pst.setString(3, maLoThuoc);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                daTra = rs.getInt(1);
            }
        }

        return Math.max(0, daMua - daTra);
    }

    private int getTyLeQuyDoi(Connection con, String maQuyDoi) throws SQLException {
        String sql = "SELECT tyLeQuyDoi FROM DonViQuyDoi WHERE maQuyDoi = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maQuyDoi);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return Math.max(1, rs.getInt(1));
            }
        }
        return 1;
    }

    private List<ChiTietDoiTra> gopChiTiet(List<ChiTietDoiTra> dsChiTiet) {
        Map<String, ChiTietDoiTra> map = new LinkedHashMap<>();
        for (ChiTietDoiTra item : dsChiTiet) {
            if (item == null || item.getMaQuyDoi() == null || item.getMaLoThuoc() == null || item.getSoLuong() <= 0) {
                continue;
            }

            String key = item.getMaQuyDoi() + "|" + item.getMaLoThuoc();
            ChiTietDoiTra existing = map.get(key);
            if (existing == null) {
                ChiTietDoiTra copy = new ChiTietDoiTra();
                copy.setMaPhieuDoiTra(item.getMaPhieuDoiTra());
                copy.setMaQuyDoi(item.getMaQuyDoi());
                copy.setMaLoThuoc(item.getMaLoThuoc());
                copy.setSoLuong(item.getSoLuong());
                copy.setTinhTrang(item.getTinhTrang());
                map.put(key, copy);
            } else {
                existing.setSoLuong(existing.getSoLuong() + item.getSoLuong());
                if ((existing.getTinhTrang() == null || existing.getTinhTrang().isBlank())
                        && item.getTinhTrang() != null && !item.getTinhTrang().isBlank()) {
                    existing.setTinhTrang(item.getTinhTrang());
                }
            }
        }
        return new ArrayList<>(map.values());
    }
}
