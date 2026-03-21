package dao;

import connectDB.ConnectDB;
import entity.LoThuoc;
import entity.ViTriKho;
import entity.Thuoc;
import java.sql.*;
import java.util.ArrayList;

public class DAO_LoThuoc {
    public boolean themLoThuoc(LoThuoc lo) {
        String sql = "INSERT INTO LoThuoc VALUES(?,?,?,?,?,?,?)";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, lo.getMaLoThuoc());
            pst.setDate(2, lo.getNgaySanXuat());
            pst.setDate(3, lo.getHanSuDung());
            pst.setInt(4, lo.getSoLuongTon());
            pst.setDouble(5, lo.getGiaNhap());
            pst.setString(6, lo.getViTriKho().name()); 
            pst.setString(7, lo.getThuoc().getMaThuoc());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    /**
     * Lấy các lô còn tồn, chưa hết hạn, ưu tiên kho bán hàng, sắp xếp theo hạn sử dụng tăng dần (FEFO).
     */
    public ArrayList<LoThuoc> getLoThuocBanDuocByMaThuoc(String maThuoc) {
        ArrayList<LoThuoc> ds = new ArrayList<>();
        String sql =
                "SELECT * FROM LoThuoc " +
                "WHERE maThuoc = ? " +
                "  AND soLuongTon > 0 " +
                "  AND hanSuDung >= CAST(GETDATE() AS DATE) " +
                "ORDER BY CASE WHEN viTriKho = 'KHO_BAN_HANG' THEN 0 ELSE 1 END, hanSuDung ASC";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maThuoc);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                LoThuoc lo = new LoThuoc();
                lo.setMaLoThuoc(rs.getString("maLoThuoc"));
                lo.setNgaySanXuat(rs.getDate("ngaySanXuat"));
                lo.setHanSuDung(rs.getDate("hanSuDung"));
                lo.setSoLuongTon(rs.getInt("soLuongTon"));
                lo.setGiaNhap(rs.getDouble("giaNhap"));
                String viTri = rs.getString("viTriKho");
                lo.setViTriKho(viTri != null ? ViTriKho.valueOf(viTri) : null);

                Thuoc t = new Thuoc();
                t.setMaThuoc(rs.getString("maThuoc"));
                lo.setThuoc(t);

                ds.add(lo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }
}