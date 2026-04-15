package dao;

import connectDB.ConnectDB;
import entity.ChiTietDoiTra;
import entity.ChiTietDoiTraView;
import entity.PhieuDoiTra;
import entity.PhieuDoiTraView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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
                "       MAX(ct.donGia) AS donGia " +
                "FROM ChiTietHoaDon ct " +
                "JOIN DonViQuyDoi dv ON dv.maQuyDoi = ct.maQuyDoi " +
                "JOIN Thuoc t ON t.maThuoc = dv.maThuoc " +
                "JOIN LoThuoc lo ON lo.maLoThuoc = ct.maLoThuoc " +
                "WHERE ct.maHoaDon = ? " +
                "GROUP BY ct.maHoaDon, ct.maQuyDoi, ct.maLoThuoc, t.tenThuoc, dv.tenDonVi, lo.hanSuDung " +
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
                "SELECT t.tenThuoc, dv.tenDonVi, ct.maLoThuoc, lo.hanSuDung, ct.soLuong, " +
                "       hdct.donGia, (ct.soLuong * hdct.donGia) AS thanhTien " +
                "FROM ChiTietDoiTra ct " +
                "JOIN DonViQuyDoi dv ON dv.maQuyDoi = ct.maQuyDoi " +
                "JOIN Thuoc t ON t.maThuoc = dv.maThuoc " +
                "JOIN LoThuoc lo ON lo.maLoThuoc = ct.maLoThuoc " +
                "JOIN PhieuDoiTra pdt ON pdt.maPhieuDoiTra = ct.maPhieuDoiTra " +
                "JOIN ChiTietHoaDon hdct ON hdct.maHoaDon = pdt.maHoaDon AND hdct.maQuyDoi = ct.maQuyDoi AND hdct.maLoThuoc = ct.maLoThuoc " +
                "WHERE ct.maPhieuDoiTra = ? " +
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

    public boolean lapPhieuDoiTra(PhieuDoiTra pdt, List<ChiTietDoiTra> dsChiTiet) {
        if (pdt == null || pdt.getHoaDon() == null || pdt.getNhanVien() == null || dsChiTiet == null || dsChiTiet.isEmpty()) {
            return false;
        }

        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            String sqlPhieu =
                    "INSERT INTO PhieuDoiTra(maPhieuDoiTra, maHoaDon, maNhanVien, ngayDoiTra, lyDo, hinhThucXuLy, phiPhat) VALUES(?,?,?,?,?,?,?)";
            try (PreparedStatement pst = con.prepareStatement(sqlPhieu)) {
                pst.setString(1, pdt.getMaPhieuDoiTra());
                pst.setString(2, pdt.getHoaDon().getMaHoaDon());
                pst.setString(3, pdt.getNhanVien().getMaNhanVien());
                pst.setTimestamp(4, new java.sql.Timestamp(pdt.getNgayDoiTra().getTime()));
                pst.setString(5, pdt.getLyDo());
                pst.setString(6, pdt.getHinhThucXuLy().name());
                pst.setDouble(7, pdt.getPhiPhat());
                pst.executeUpdate();
            }

            String sqlChiTiet = "INSERT INTO ChiTietDoiTra(maPhieuDoiTra, maQuyDoi, maLoThuoc, soLuong, tinhTrang) VALUES(?,?,?,?,?)";
            String sqlUpdateLo = "UPDATE LoThuoc SET soLuongTon = soLuongTon + ? WHERE maLoThuoc = ?";

            for (ChiTietDoiTra ct : dsChiTiet) {
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
                try (PreparedStatement pst = con.prepareStatement(sqlUpdateLo)) {
                    pst.setInt(1, ct.getSoLuong() * tyLeQuyDoi);
                    pst.setString(2, ct.getMaLoThuoc());
                    pst.executeUpdate();
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
}
