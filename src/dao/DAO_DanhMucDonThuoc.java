package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import connectDB.ConnectDB;
import entity.DonThuoc;

public class DAO_DanhMucDonThuoc {

    private DonThuoc mapRow(ResultSet rs) throws Exception {
        return new DonThuoc(
            rs.getString("maDonThuoc"),
            rs.getString("maHoaDon"),
            rs.getString("tenBacSi"),
            rs.getString("chanDoan"),
            rs.getString("hinhAnhDon"),
            rs.getString("thongTinBenhNhan")
        );
    }
    public ArrayList<DonThuoc> locTheoBacSi(String tenBacSi) {
        ArrayList<DonThuoc> list = new ArrayList<>();
        String sql = "SELECT * FROM DonThuoc WHERE tenBacSi = ?"; // dùng = thay vì LIKE
        Connection con = ConnectDB.getConnection();
        try {
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, tenBacSi);
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
						list.add(mapRow(rs));
					}
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi locTheoBacSi: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
    public String taoMaMoi() throws Exception {
        String sql = "SELECT MAX(maDonThuoc) FROM DonThuoc";
        Connection con = ConnectDB.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getString(1) != null) {
                int so = Integer.parseInt(rs.getString(1).replaceAll("[^0-9]", "")) + 1;
                return String.format("DT%04d", so);
            }
            return "DT0001";
        }
    }

    public ArrayList<DonThuoc> getAll() {
        ArrayList<DonThuoc> list = new ArrayList<>();
        String sql = "SELECT * FROM DonThuoc";
        Connection con = ConnectDB.getConnection();
        try {
            try (PreparedStatement pst = con.prepareStatement(sql);
                 ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
					list.add(mapRow(rs));
				}
            }
        } catch (Exception e) {
            System.err.println("Lỗi getAll: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<DonThuoc> timKiem(String keyword) {
        ArrayList<DonThuoc> list = new ArrayList<>();
        String sql = "SELECT * FROM DonThuoc " +
                     "WHERE maDonThuoc LIKE ? OR tenBacSi LIKE ? OR thongTinBenhNhan LIKE ?";
        Connection con = ConnectDB.getConnection();
        try {
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                String kw = "%" + keyword + "%";
                pst.setString(1, kw);
                pst.setString(2, kw);
                pst.setString(3, kw);
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
						list.add(mapRow(rs));
					}
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi timKiem: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public boolean them(DonThuoc dt) {
        String sql = "INSERT INTO DonThuoc (maDonThuoc, maHoaDon, tenBacSi, chanDoan, hinhAnhDon, thongTinBenhNhan) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        Connection con = ConnectDB.getConnection();
        try {
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, dt.getMaDonThuoc());
                if (dt.getMaHoaDon() == null || dt.getMaHoaDon().isEmpty()) {
                    pst.setNull(2, java.sql.Types.VARCHAR);
                } else {
                    pst.setString(2, dt.getMaHoaDon());
                }
                pst.setString(3, dt.getTenBacSi());
                pst.setString(4, dt.getChanDoan());
                pst.setString(5, dt.getHinhAnhDon());
                pst.setString(6, dt.getThongTinBenhNhan());
                return pst.executeUpdate() > 0;
            }
        } catch (Exception e) {
            System.err.println("Lỗi them: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean sua(DonThuoc dt) {
        String sql = "UPDATE DonThuoc SET tenBacSi=?, chanDoan=?, hinhAnhDon=?, thongTinBenhNhan=? " +
                     "WHERE maDonThuoc=?";
        Connection con = ConnectDB.getConnection();
        try {
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, dt.getTenBacSi());
                pst.setString(2, dt.getChanDoan());
                pst.setString(3, dt.getHinhAnhDon());
                pst.setString(4, dt.getThongTinBenhNhan());
                pst.setString(5, dt.getMaDonThuoc());
                return pst.executeUpdate() > 0;
            }
        } catch (Exception e) {
            System.err.println("Lỗi sua: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean xoa(String maDonThuoc) {
        String sql = "DELETE FROM DonThuoc WHERE maDonThuoc=?";
        Connection con = ConnectDB.getConnection();
        try {
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, maDonThuoc);
                return pst.executeUpdate() > 0;
            }
        } catch (Exception e) {
            System.err.println("Lỗi xoa: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<String> getDanhSachBacSi() {
        ArrayList<String> list = new ArrayList<>();
        list.add("Tất cả bác sĩ");
        String sql = "SELECT DISTINCT tenBacSi FROM DonThuoc WHERE tenBacSi IS NOT NULL ORDER BY tenBacSi";
        Connection con = ConnectDB.getConnection();
        try {
            try (PreparedStatement pst = con.prepareStatement(sql);
                 ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
					list.add(rs.getString("tenBacSi"));
				}
            }
        } catch (Exception e) {
            System.err.println("Lỗi getDanhSachBacSi: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public boolean kiemTraMaHoaDonTonTai(String maHoaDon) {
        String sql = "SELECT COUNT(*) FROM HoaDon WHERE maHoaDon = ?";
        Connection con = ConnectDB.getConnection();
        try {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maHoaDon);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
						return rs.getInt(1) > 0;
					}
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi kiemTraMaHoaDonTonTai: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}