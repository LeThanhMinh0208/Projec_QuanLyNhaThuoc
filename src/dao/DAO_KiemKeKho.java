package dao;

import connectDB.ConnectDB;
import entity.LoThuoc;
import entity.PhieuKiemKe;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAO_KiemKeKho {

    public List<Object[]> getLoThuocKhaDungKemDanhMuc() {
        List<Object[]> list = new ArrayList<>();
        // 🚨 ĐÃ THÁO XÍCH: Không còn AND l.soLuongTon > 0
        String sql = "SELECT l.*, t.tenThuoc, dm.tenDanhMuc FROM LoThuoc l JOIN Thuoc t ON l.maThuoc = t.maThuoc JOIN DanhMucThuoc dm ON t.maDanhMuc = dm.maDanhMuc WHERE (l.isLockedForAudit = 0 OR l.isLockedForAudit IS NULL)";
        try (Connection con = ConnectDB.getInstance().getConnection(); PreparedStatement pst = con.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                LoThuoc lo = new LoThuoc(); 
                lo.setMaLoThuoc(rs.getString("maLoThuoc")); 
                lo.setSoLuongTon(rs.getInt("soLuongTon"));
                entity.Thuoc t = new entity.Thuoc(); 
                t.setMaThuoc(rs.getString("maThuoc")); 
                t.setTenThuoc(rs.getString("tenThuoc"));
                lo.setThuoc(t); 
                list.add(new Object[]{lo, rs.getString("tenDanhMuc")});
            }
        } catch (Exception e) { e.printStackTrace(); } 
        return list;
    }

    public List<String> getDanhSachMaLoCoBienDong() {
        List<String> list = new ArrayList<>();
        // 🚨 ĐÃ THÁO XÍCH: Không còn AND l.soLuongTon > 0
        String sql = "SELECT DISTINCT l.maLoThuoc FROM LoThuoc l WHERE (l.isLockedForAudit = 0 OR l.isLockedForAudit IS NULL) " +
            "AND (l.maLoThuoc IN (SELECT maLoThuoc FROM ChiTietHoaDon JOIN HoaDon ON ChiTietHoaDon.maHoaDon = HoaDon.maHoaDon WHERE ngayLap > ISNULL((SELECT MAX(ngayDuyet) FROM PhieuKiemKe pk JOIN ChiTietPhieuKiemKe cpk ON pk.maPhieuKiemKe = cpk.maPhieuKiemKe WHERE cpk.maLoThuoc = l.maLoThuoc AND pk.trangThai = 'DA_HOAN_THANH'), '2000-01-01')) " +
            "OR l.maLoThuoc IN (SELECT maLoThuoc FROM ChiTietPhieuNhap JOIN PhieuNhap ON ChiTietPhieuNhap.maPhieuNhap = PhieuNhap.maPhieuNhap WHERE ngayNhap > ISNULL((SELECT MAX(ngayDuyet) FROM PhieuKiemKe pk JOIN ChiTietPhieuKiemKe cpk ON pk.maPhieuKiemKe = cpk.maPhieuKiemKe WHERE cpk.maLoThuoc = l.maLoThuoc AND pk.trangThai = 'DA_HOAN_THANH'), '2000-01-01')) " +
            "OR l.maLoThuoc IN (SELECT maLoThuoc FROM ChiTietPhieuXuat JOIN PhieuXuat ON ChiTietPhieuXuat.maPhieuXuat = PhieuXuat.maPhieuXuat WHERE ngayXuat > ISNULL((SELECT MAX(ngayDuyet) FROM PhieuKiemKe pk JOIN ChiTietPhieuKiemKe cpk ON pk.maPhieuKiemKe = cpk.maPhieuKiemKe WHERE cpk.maLoThuoc = l.maLoThuoc AND pk.trangThai = 'DA_HOAN_THANH'), '2000-01-01')))";
        try (Connection con = ConnectDB.getInstance().getConnection(); PreparedStatement pst = con.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
            while (rs.next()) list.add(rs.getString("maLoThuoc"));
        } catch (Exception e) { e.printStackTrace(); } 
        return list;
    }

    public boolean taoPhieuKiemKe(String maPhieu, String maNVTao, List<LoThuoc> listChon) {
        String sqlPhieu = "INSERT INTO PhieuKiemKe (maPhieuKiemKe, maNhanVienTao, trangThai) VALUES (?, ?, 'DANG_KIEM_KE')";
        String sqlCT = "INSERT INTO ChiTietPhieuKiemKe (maPhieuKiemKe, maLoThuoc, tonKhoSnapshot) VALUES (?, ?, ?)";
        String sqlLock = "UPDATE LoThuoc SET isLockedForAudit = 1 WHERE maLoThuoc = ?";
        try (Connection con = ConnectDB.getInstance().getConnection()) {
            con.setAutoCommit(false); 
            try (PreparedStatement pstPhieu = con.prepareStatement(sqlPhieu); PreparedStatement pstCT = con.prepareStatement(sqlCT); PreparedStatement pstLock = con.prepareStatement(sqlLock)) {
                pstPhieu.setString(1, maPhieu); pstPhieu.setString(2, maNVTao); pstPhieu.executeUpdate();
                for (LoThuoc lo : listChon) {
                    pstCT.setString(1, maPhieu); pstCT.setString(2, lo.getMaLoThuoc()); pstCT.setInt(3, lo.getSoLuongTon()); pstCT.addBatch();
                    pstLock.setString(1, lo.getMaLoThuoc()); pstLock.addBatch();
                }
                pstCT.executeBatch(); pstLock.executeBatch(); con.commit(); return true;
            } catch (Exception ex) { con.rollback(); return false; }
        } catch (Exception e) { return false; }
    }

    public boolean chotSoKiemKeDuyet(String maPhieu, String maNVDuyet, List<entity.ChiTietPhieuKiemKe> listChiTietDaKiem) {
        String sqlCheck = "SELECT soLuongTon FROM LoThuoc WHERE maLoThuoc = ?";
        String sqlUpKho = "UPDATE LoThuoc SET soLuongTon = soLuongTon + ?, isLockedForAudit = 0 WHERE maLoThuoc = ?";
        String sqlUpCT = "UPDATE ChiTietPhieuKiemKe SET soLuongKiemTra=?, chenhLech=?, lyDoLech=?, ghiChu=?, trangThaiChiTiet=? WHERE maPhieuKiemKe=? AND maLoThuoc=?";
        String sqlUpPhieu = "UPDATE PhieuKiemKe SET trangThai = 'DA_HOAN_THANH', maNhanVienDuyet = ?, ngayDuyet = GETDATE() WHERE maPhieuKiemKe = ?";

        try (Connection con = ConnectDB.getInstance().getConnection()) {
            con.setAutoCommit(false); 
            try (PreparedStatement pstCheck = con.prepareStatement(sqlCheck); PreparedStatement pstUpKho = con.prepareStatement(sqlUpKho);
                 PreparedStatement pstUpCT = con.prepareStatement(sqlUpCT); PreparedStatement pstUpPhieu = con.prepareStatement(sqlUpPhieu)) {
                for (entity.ChiTietPhieuKiemKe ct : listChiTietDaKiem) {
                    pstCheck.setString(1, ct.getMaLoThuoc()); ResultSet rs = pstCheck.executeQuery();
                    if (rs.next() && (rs.getInt(1) + ct.getChenhLech() < 0)) { con.rollback(); throw new Exception("Tồn âm"); }
                    pstUpKho.setInt(1, ct.getChenhLech()); pstUpKho.setString(2, ct.getMaLoThuoc()); pstUpKho.addBatch();
                    
                    pstUpCT.setInt(1, ct.getSoLuongKiemTra()); pstUpCT.setInt(2, ct.getChenhLech()); 
                    pstUpCT.setString(3, ct.getLyDoLech()); pstUpCT.setString(4, ct.getGhiChu()); 
                    pstUpCT.setString(5, ct.getChenhLech() == 0 ? "KHOP" : (ct.getChenhLech() > 0 ? "THUA" : "THIEU"));
                    pstUpCT.setString(6, maPhieu); pstUpCT.setString(7, ct.getMaLoThuoc()); pstUpCT.addBatch();
                }
                pstUpKho.executeBatch(); pstUpCT.executeBatch();
                pstUpPhieu.setString(1, maNVDuyet); pstUpPhieu.setString(2, maPhieu); pstUpPhieu.executeUpdate();
                con.commit(); return true;
            } catch (Exception ex) { con.rollback(); return false; }
        } catch (Exception e) { return false; }
    }

    public void xuLyPhieuNgamQua48h() {
        String sqlUnlock = "UPDATE LoThuoc SET isLockedForAudit = 0 WHERE maLoThuoc IN (SELECT maLoThuoc FROM ChiTietPhieuKiemKe WHERE maPhieuKiemKe IN (SELECT maPhieuKiemKe FROM PhieuKiemKe WHERE trangThai = 'CHO_DUYET' AND DATEDIFF(hour, ngayTao, GETDATE()) > 48))";
        String sqlCancel = "UPDATE PhieuKiemKe SET trangThai = 'DA_HUY', ghiChu = N'Hủy tự động do quá 48h' WHERE trangThai = 'CHO_DUYET' AND DATEDIFF(hour, ngayTao, GETDATE()) > 48";
        try (Connection con = ConnectDB.getInstance().getConnection(); Statement st = con.createStatement()) {
            con.setAutoCommit(false); st.executeUpdate(sqlUnlock); st.executeUpdate(sqlCancel); con.commit();
        } catch (Exception e) {}
    }

    public List<PhieuKiemKe> getAllPhieuGiaiQuyet(String tuKhoa) {
        xuLyPhieuNgamQua48h(); 
        List<PhieuKiemKe> list = new ArrayList<>();
        String sql = "SELECT p.*, nv.hoTen FROM PhieuKiemKe p JOIN NhanVien nv ON p.maNhanVienTao = nv.maNhanVien WHERE p.trangThai != 'DANG_KIEM_KE' AND (p.maPhieuKiemKe LIKE ? OR nv.hoTen LIKE ?) ORDER BY p.ngayTao DESC";
        try (Connection con = ConnectDB.getInstance().getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            String searchPattern = "%" + (tuKhoa == null ? "" : tuKhoa) + "%";
            pst.setString(1, searchPattern); pst.setString(2, searchPattern); ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                PhieuKiemKe pk = new PhieuKiemKe(); pk.setMaPhieuKiemKe(rs.getString("maPhieuKiemKe")); pk.setNgayTao(rs.getTimestamp("ngayTao")); pk.setTrangThai(rs.getString("trangThai"));
                entity.NhanVien nv = new entity.NhanVien(); nv.setMaNhanVien(rs.getString("maNhanVienTao")); nv.setHoTen(rs.getString("hoTen"));
                pk.setNhanVienTao(nv); list.add(pk);
            }
        } catch (Exception e) { e.printStackTrace(); } return list;
    }
}