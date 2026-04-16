package dao;

import connectDB.ConnectDB;
import entity.DonThuoc;

import java.sql.*;
import java.util.ArrayList;

public class DAO_DonThuoc {

    public ArrayList<DonThuoc> getAll() {
        ArrayList<DonThuoc> ds = new ArrayList<>();
        String sql = "SELECT * FROM DonThuoc";
        try (Connection con = ConnectDB.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ds.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public ArrayList<DonThuoc> getByKeyword(String keyword) {
        ArrayList<DonThuoc> ds = new ArrayList<>();
        String sql = "SELECT * FROM DonThuoc WHERE maDonThuoc LIKE ? OR thongTinBenhNhan LIKE ? OR tenBacSi LIKE ?";
        try (Connection con = ConnectDB.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            String kw = "%" + keyword + "%";
            pst.setString(1, kw);
            pst.setString(2, kw);
            pst.setString(3, kw);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                ds.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public boolean themDonThuoc(DonThuoc dt) {
        String sql = "INSERT INTO DonThuoc(maDonThuoc, maHoaDon, tenBacSi, chanDoan, hinhAnhDon, thongTinBenhNhan) VALUES(?,?,?,?,?,?)";
        try (Connection con = ConnectDB.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, dt.getMaDonThuoc());
            pst.setString(2, dt.getMaHoaDon());
            pst.setString(3, dt.getTenBacSi());
            pst.setString(4, dt.getChanDoan());
            pst.setString(5, dt.getHinhAnhDon());
            pst.setString(6, dt.getThongTinBenhNhan());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean suaDonThuoc(DonThuoc dt) {
        String sql = "UPDATE DonThuoc SET tenBacSi = ?, chanDoan = ?, hinhAnhDon = ?, thongTinBenhNhan = ? WHERE maDonThuoc = ?";
        try (Connection con = ConnectDB.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, dt.getTenBacSi());
            pst.setString(2, dt.getChanDoan());
            pst.setString(3, dt.getHinhAnhDon());
            pst.setString(4, dt.getThongTinBenhNhan());
            pst.setString(5, dt.getMaDonThuoc());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean xoaDonThuoc(String maDonThuoc) {
        String sql = "DELETE FROM DonThuoc WHERE maDonThuoc = ?";
        try (Connection con = ConnectDB.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maDonThuoc);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private DonThuoc map(ResultSet rs) throws SQLException {
        DonThuoc dt = new DonThuoc();
        dt.setMaDonThuoc(rs.getString("maDonThuoc"));
        dt.setMaHoaDon(rs.getString("maHoaDon"));
        dt.setTenBacSi(rs.getString("tenBacSi"));
        dt.setChanDoan(rs.getString("chanDoan"));
        dt.setHinhAnhDon(rs.getString("hinhAnhDon"));
        dt.setThongTinBenhNhan(rs.getString("thongTinBenhNhan"));
        return dt;
    }

    public int getMaxMaDonThuoc() {
        String sql = "SELECT MAX(CAST(SUBSTRING(maDonThuoc, 3, LEN(maDonThuoc)) AS INT)) FROM DonThuoc WHERE maDonThuoc LIKE 'DT%'";
        try (Connection con = ConnectDB.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Lấy đơn thuốc theo mã hóa đơn — dùng cho xuất PDF.
     */
    public DonThuoc getByMaHoaDon(String maHoaDon) {
        String sql = "SELECT * FROM DonThuoc WHERE maHoaDon = ?";
        try (Connection con = ConnectDB.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maHoaDon);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return map(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
