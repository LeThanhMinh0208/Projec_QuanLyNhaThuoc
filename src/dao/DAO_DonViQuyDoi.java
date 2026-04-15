package dao;

import connectDB.ConnectDB;
import entity.DonViQuyDoi;

import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DAO_DonViQuyDoi {

    private String lastError;

    /** Lấy tất cả đơn vị quy đổi theo maThuoc (chỉ 4 cột thực tế trong DB, không có giaBan) */
    public ArrayList<DonViQuyDoi> getDonViByMaThuoc(String maThuoc) {
        return getDonViByMaThuocOrderAsc(maThuoc);
    }

    public ArrayList<DonViQuyDoi> getDonViByMaThuocOrderAsc(String maThuoc) {
        ArrayList<DonViQuyDoi> list = new ArrayList<>();
        String sql = "SELECT maQuyDoi, maThuoc, tenDonVi, tyLeQuyDoi "
                   + "FROM DonViQuyDoi WHERE maThuoc = ? ORDER BY tyLeQuyDoi ASC, maQuyDoi ASC";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maThuoc);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(mapDonVi(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Lấy 1 đơn vị theo maQuyDoi */
    public DonViQuyDoi getByMaQuyDoi(String maQuyDoi) {
        String sql = "SELECT * FROM DonViQuyDoi WHERE maQuyDoi = ?";
        try (Connection con = ConnectDB.getConnection(); 
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maQuyDoi);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return mapDonVi(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(DonViQuyDoi dv) {
        lastError = null;
        String sql = "INSERT INTO DonViQuyDoi (maQuyDoi, maThuoc, tenDonVi, tyLeQuyDoi) VALUES (?, ?, ?, ?)";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, dv.getMaQuyDoi());
            pst.setString(2, dv.getMaThuoc());
            pst.setString(3, dv.getTenDonVi());
            pst.setInt(4, dv.getTyLeQuyDoi());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            lastError = "Không thể thêm đơn vị quy đổi: " + e.getMessage();
            return false;
        }
    }

    public boolean update(DonViQuyDoi dv) {
        lastError = null;
        String sql = "UPDATE DonViQuyDoi SET tenDonVi = ?, tyLeQuyDoi = ? WHERE maQuyDoi = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, dv.getTenDonVi());
            pst.setInt(2, dv.getTyLeQuyDoi());
            pst.setString(3, dv.getMaQuyDoi());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            lastError = "Không thể cập nhật đơn vị quy đổi: " + e.getMessage();
            return false;
        }
    }

    public boolean deleteByMaQuyDoi(String maQuyDoi) {
        lastError = null;
        String sql = "DELETE FROM DonViQuyDoi WHERE maQuyDoi = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maQuyDoi);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            lastError = "Không thể xóa đơn vị quy đổi do đã phát sinh giao dịch liên quan.";
            return false;
        }
    }

    public boolean deleteDonViMoRongByMaThuoc(String maThuoc) {
        lastError = null;
        String sql = "DELETE FROM DonViQuyDoi WHERE maThuoc = ? AND tyLeQuyDoi > 1";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maThuoc);
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            lastError = "Không thể xóa do một số đơn vị đã được sử dụng trong chứng từ.";
            return false;
        }
    }

    public boolean saveCauHinhDonVi(String maThuoc, String tenDonViCoBan, List<DonViQuyDoi> donViMoRong) {
        lastError = null;
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            String sqlUpdateThuoc = "UPDATE Thuoc SET donViCoBan = ? WHERE maThuoc = ?";
            try (PreparedStatement pst = con.prepareStatement(sqlUpdateThuoc)) {
                pst.setString(1, tenDonViCoBan);
                pst.setString(2, maThuoc);
                pst.executeUpdate();
            }

            ArrayList<DonViQuyDoi> hienCo = new ArrayList<>();
            String sqlGet = "SELECT maQuyDoi, maThuoc, tenDonVi, tyLeQuyDoi FROM DonViQuyDoi WHERE maThuoc = ? ORDER BY tyLeQuyDoi ASC, maQuyDoi ASC";
            try (PreparedStatement pst = con.prepareStatement(sqlGet)) {
                pst.setString(1, maThuoc);
                ResultSet rs = pst.executeQuery();
                while (rs.next()) {
                    hienCo.add(mapDonVi(rs));
                }
            }

            DonViQuyDoi donViBase = null;
            ArrayList<DonViQuyDoi> hienCoMoRong = new ArrayList<>();
            for (DonViQuyDoi dv : hienCo) {
                if (dv.getTyLeQuyDoi() == 1 && donViBase == null) {
                    donViBase = dv;
                } else {
                    hienCoMoRong.add(dv);
                }
            }

            if (donViBase == null) {
                String maMoi = taoMaQuyDoiMoi(con);
                try (PreparedStatement pst = con.prepareStatement(
                        "INSERT INTO DonViQuyDoi (maQuyDoi, maThuoc, tenDonVi, tyLeQuyDoi) VALUES (?, ?, ?, 1)")) {
                    pst.setString(1, maMoi);
                    pst.setString(2, maThuoc);
                    pst.setString(3, tenDonViCoBan);
                    pst.executeUpdate();
                }
            } else {
                try (PreparedStatement pst = con.prepareStatement(
                        "UPDATE DonViQuyDoi SET tenDonVi = ?, tyLeQuyDoi = 1 WHERE maQuyDoi = ?")) {
                    pst.setString(1, tenDonViCoBan);
                    pst.setString(2, donViBase.getMaQuyDoi());
                    pst.executeUpdate();
                }
            }

            List<DonViQuyDoi> mucTieu = new ArrayList<>(donViMoRong == null ? List.of() : donViMoRong);
            mucTieu.sort(Comparator.comparingInt(DonViQuyDoi::getTyLeQuyDoi));

            int soCapNhat = Math.min(hienCoMoRong.size(), mucTieu.size());
            for (int i = 0; i < soCapNhat; i++) {
                DonViQuyDoi cu = hienCoMoRong.get(i);
                DonViQuyDoi moi = mucTieu.get(i);
                try (PreparedStatement pst = con.prepareStatement(
                        "UPDATE DonViQuyDoi SET tenDonVi = ?, tyLeQuyDoi = ? WHERE maQuyDoi = ?")) {
                    pst.setString(1, moi.getTenDonVi());
                    pst.setInt(2, moi.getTyLeQuyDoi());
                    pst.setString(3, cu.getMaQuyDoi());
                    pst.executeUpdate();
                }
            }

            for (int i = soCapNhat; i < mucTieu.size(); i++) {
                DonViQuyDoi dvMoi = mucTieu.get(i);
                String maMoi = taoMaQuyDoiMoi(con);
                try (PreparedStatement pst = con.prepareStatement(
                        "INSERT INTO DonViQuyDoi (maQuyDoi, maThuoc, tenDonVi, tyLeQuyDoi) VALUES (?, ?, ?, ?)")) {
                    pst.setString(1, maMoi);
                    pst.setString(2, maThuoc);
                    pst.setString(3, dvMoi.getTenDonVi());
                    pst.setInt(4, dvMoi.getTyLeQuyDoi());
                    pst.executeUpdate();
                }
            }

            for (int i = soCapNhat; i < hienCoMoRong.size(); i++) {
                DonViQuyDoi dvXoa = hienCoMoRong.get(i);
                try (PreparedStatement pst = con.prepareStatement("DELETE FROM DonViQuyDoi WHERE maQuyDoi = ?")) {
                    pst.setString(1, dvXoa.getMaQuyDoi());
                    pst.executeUpdate();
                }
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            lastError = "Không thể lưu cấu hình quy đổi. Có thể một đơn vị đã phát sinh giao dịch và không thể thay đổi.";
            return false;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getLastError() {
        return lastError;
    }

    private DonViQuyDoi mapDonVi(ResultSet rs) throws SQLException {
        return new DonViQuyDoi(
                rs.getString("maQuyDoi"),
                rs.getString("maThuoc"),
                rs.getString("tenDonVi"),
                rs.getInt("tyLeQuyDoi")
        );
    }

    private String taoMaQuyDoiMoi(Connection con) throws SQLException {
        int max = 0;
        String sql = "SELECT maQuyDoi FROM DonViQuyDoi WHERE maQuyDoi LIKE 'QD%'";
        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                String ma = rs.getString(1);
                if (ma == null) {
                    continue;
                }
                String so = ma.replaceAll("[^0-9]", "");
                if (!so.isEmpty()) {
                    try {
                        max = Math.max(max, Integer.parseInt(so));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return String.format("QD%05d", max + 1);
    }

    // ========================================================
    // HÀM LẤY ĐƠN VỊ LỚN NHẤT CỦA THUỐC (Dành riêng cho Đặt Hàng)
    // ========================================================
    public DonViQuyDoi getDonViLonNhatCuaThuoc(String maThuoc) {
        DonViQuyDoi dvMax = null;
        String sql = "SELECT TOP 1 * FROM DonViQuyDoi WHERE maThuoc = ? ORDER BY tyLeQuyDoi DESC";
        
        ConnectDB.getInstance();
		try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            
            pst.setString(1, maThuoc);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                dvMax = mapDonVi(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dvMax;
    }
}