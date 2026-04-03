package dao;

import connectDB.ConnectDB;
import entity.ChiTietDonDatHang;
import entity.DonDatHang;
import entity.NhanVien;
import entity.NhaCungCap;
import entity.PhieuNhap;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAO_PhieuNhap {

    private String getMaPhieuNhapMoi(Connection con) throws SQLException {
        String ma = "PN001";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT MAX(maPhieuNhap) FROM PhieuNhap")) {
            if (rs.next() && rs.getString(1) != null) {
                int so = Integer.parseInt(rs.getString(1).substring(2)) + 1;
                ma = String.format("PN%03d", so);
            }
        }
        return ma;
    }

    public boolean luuPhieuNhapVaCapNhatDon(PhieuNhap phieuNhap, List<ChiTietDonDatHang> listChiTietNhan, DonDatHang donGoc, int soNgayHen) {
        Connection con = null;
        try {
            con = ConnectDB.getInstance().getConnection();
            if (con == null || con.isClosed()) {
                ConnectDB.getInstance().connect(); 
                con = ConnectDB.getInstance().getConnection();
            }
            con.setAutoCommit(false); 

            if (phieuNhap.getMaPhieuNhap() == null || phieuNhap.getMaPhieuNhap().isEmpty()) {
                phieuNhap.setMaPhieuNhap(getMaPhieuNhapMoi(con)); 
            }

            // 1. Lưu Phiếu Nhập
            String sqlInsertPhieuNhap = "INSERT INTO PhieuNhap (maPhieuNhap, maDonDatHang, maNhaCungCap, maNhanVien, ngayNhap) VALUES (?, ?, ?, ?, GETDATE())";
            try (PreparedStatement pstPhieuNhap = con.prepareStatement(sqlInsertPhieuNhap)) {
                pstPhieuNhap.setString(1, phieuNhap.getMaPhieuNhap());
                pstPhieuNhap.setString(2, phieuNhap.getDonDatHang().getMaDonDatHang());
                pstPhieuNhap.setString(3, phieuNhap.getNhaCungCap().getMaNhaCungCap());
                pstPhieuNhap.setString(4, phieuNhap.getNhanVien().getMaNhanVien());
                pstPhieuNhap.executeUpdate();
            }

            // 2. Lưu trực tiếp vào bảng LoThuoc (Cấu trúc gốc)
            String sqlInsertLoThuoc = "IF NOT EXISTS (SELECT 1 FROM LoThuoc WHERE maLoThuoc=?) " + 
                                      "INSERT INTO LoThuoc (maLoThuoc, maThuoc, ngaySanXuat, hanSuDung, soLuongTon, giaNhap, viTriKho) " +
                                      "VALUES (?, ?, ?, ?, ?, ?, 'KHO_DU_TRU') " +
                                      "ELSE " +
                                      "UPDATE LoThuoc SET soLuongTon = soLuongTon + ? WHERE maLoThuoc=?";
                                      
            String sqlInsertChiTietPN = "INSERT INTO ChiTietPhieuNhap (maPhieuNhap, maQuyDoi, maLoThuoc, soLuong, donGiaNhap) VALUES (?, ?, ?, ?, ?)";
            String sqlUpdateChiTietDon = "UPDATE ChiTietDonDatHang SET soLuongDaNhan = soLuongDaNhan + ?, donGiaDuKien = ?, maLo = ?, ngaySanXuat = ?, hanSuDung = ? WHERE maDonDatHang = ? AND maQuyDoi = ?";

            boolean isGiaoThieu = false;
            double tongTienThieu = 0;

            try (PreparedStatement pstLoThuoc = con.prepareStatement(sqlInsertLoThuoc);
                 PreparedStatement pstChiTietPN = con.prepareStatement(sqlInsertChiTietPN);
                 PreparedStatement pstUpdateCTDon = con.prepareStatement(sqlUpdateChiTietDon)) {

                for (ChiTietDonDatHang ct : listChiTietNhan) {
                    if (ct.getSoLuongDaNhan() > 0) {
                        
                        Date ngaySX = Date.valueOf(ChuyenDoiNgay(ct.getNgaySanXuatTemp()));
                        Date hanSD = Date.valueOf(ChuyenDoiNgay(ct.getHanSuDung()));

                        // Bảng LoThuoc: Thêm mới hoặc cộng dồn
                        pstLoThuoc.setString(1, ct.getMaLo()); // Param 1: IF NOT EXISTS
                        pstLoThuoc.setString(2, ct.getMaLo()); // Param 2: Insert
                        pstLoThuoc.setString(3, ct.getThuoc().getMaThuoc()); // Param 3: Insert
                        pstLoThuoc.setDate(4, ngaySX); // Param 4: Insert
                        pstLoThuoc.setDate(5, hanSD); // Param 5: Insert
                        pstLoThuoc.setInt(6, ct.getSoLuongDaNhan()); // Param 6: Insert
                        pstLoThuoc.setDouble(7, ct.getDonGiaDuKien()); // Param 7: Insert
                        pstLoThuoc.setInt(8, ct.getSoLuongDaNhan()); // Param 8: Update
                        pstLoThuoc.setString(9, ct.getMaLo()); // Param 9: Update
                        pstLoThuoc.executeUpdate();

                        // Chi tiết Phiếu Nhập
                        pstChiTietPN.setString(1, phieuNhap.getMaPhieuNhap());
                        pstChiTietPN.setString(2, ct.getDonViQuyDoi().getMaQuyDoi());
                        pstChiTietPN.setString(3, ct.getMaLo());
                        pstChiTietPN.setInt(4, ct.getSoLuongDaNhan());
                        pstChiTietPN.setDouble(5, ct.getDonGiaDuKien());
                        pstChiTietPN.executeUpdate();

                        // Cập nhật Đơn đặt hàng gốc
                        pstUpdateCTDon.setInt(1, ct.getSoLuongDaNhan()); 
                        pstUpdateCTDon.setDouble(2, ct.getDonGiaDuKien()); 
                        pstUpdateCTDon.setString(3, ct.getMaLo());
                        pstUpdateCTDon.setDate(4, ngaySX);
                        pstUpdateCTDon.setDate(5, hanSD);
                        pstUpdateCTDon.setString(6, donGoc.getMaDonDatHang());
                        pstUpdateCTDon.setString(7, ct.getDonViQuyDoi().getMaQuyDoi());
                        pstUpdateCTDon.executeUpdate();
                    }
                    
                    int slThieu = ct.getSoLuongDat() - ct.getSoLuongDaNhan();
                    if (slThieu > 0) {
                        isGiaoThieu = true;
                        tongTienThieu += slThieu * ct.getDonGiaDuKien();
                    }
                }
            }

            // Tách đơn nếu giao thiếu (Giữ nguyên)
            if (isGiaoThieu && soNgayHen > 0) {
                try (PreparedStatement pstUpdateOld = con.prepareStatement("UPDATE DonDatHang SET trangThai = 'GIAO_MOT_PHAN' WHERE maDonDatHang = ?")) {
                    pstUpdateOld.setString(1, donGoc.getMaDonDatHang());
                    pstUpdateOld.executeUpdate();
                }

                dao.DAO_DonDatHang daoDon = new dao.DAO_DonDatHang();
                String maDonMoi = daoDon.getMaDonGiaoThieu(donGoc.getMaDonDatHang());

                String sqlInsertNewDon = "INSERT INTO DonDatHang (maDonDatHang, maNhaCungCap, maNhanVien, ngayDat, ngayGiaoDuKien, tongTienDuTinh, trangThai, ghiChu) VALUES (?, ?, ?, GETDATE(), DATEADD(day, ?, GETDATE()), ?, 'CHO_GIAO', ?)";
                try (PreparedStatement pstInsertNew = con.prepareStatement(sqlInsertNewDon)) {
                    pstInsertNew.setString(1, maDonMoi); 
                    pstInsertNew.setString(2, donGoc.getNhaCungCap().getMaNhaCungCap());
                    pstInsertNew.setString(3, donGoc.getNhanVien().getMaNhanVien());
                    pstInsertNew.setInt(4, soNgayHen); 
                    pstInsertNew.setDouble(5, tongTienThieu);
                    pstInsertNew.setString(6, "Đơn tách tự động từ phần giao thiếu của đơn: " + donGoc.getMaDonDatHang());
                    pstInsertNew.executeUpdate();
                }

                String sqlInsertNewCT = "INSERT INTO ChiTietDonDatHang (maDonDatHang, maQuyDoi, soLuongDat, soLuongDaNhan, donGiaDuKien) VALUES (?, ?, ?, 0, ?)";
                try (PreparedStatement pstInsertCT = con.prepareStatement(sqlInsertNewCT)) {
                    for (ChiTietDonDatHang ct : listChiTietNhan) {
                        int slThieu = ct.getSoLuongDat() - ct.getSoLuongDaNhan();
                        if (slThieu > 0) {
                            pstInsertCT.setString(1, maDonMoi); 
                            pstInsertCT.setString(2, ct.getDonViQuyDoi().getMaQuyDoi());
                            pstInsertCT.setInt(3, slThieu);
                            pstInsertCT.setDouble(4, ct.getDonGiaDuKien());
                            pstInsertCT.addBatch();
                        }
                    }
                    pstInsertCT.executeBatch();
                }
            } else if (!isGiaoThieu) {
                try (PreparedStatement pstTrangThai = con.prepareStatement("UPDATE DonDatHang SET trangThai = 'GIAO_DU' WHERE maDonDatHang = ?")) {
                    pstTrangThai.setString(1, donGoc.getMaDonDatHang());
                    pstTrangThai.executeUpdate();
                }
            }
 
            con.commit(); 
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            try { if (con != null && !con.isClosed()) con.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            try { if (con != null && !con.isClosed()) con.setAutoCommit(true); } catch (SQLException e) {}
        }
    }

    private String ChuyenDoiNgay(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "2099-12-31"; 
        try {
            String[] parts = dateStr.split("/");
            if (parts.length == 3) {
                return parts[2] + "-" + parts[1] + "-" + parts[0];
            }
        } catch (Exception e) { e.printStackTrace(); }
        return dateStr;
    }

    public boolean kiemTraMaLoTonTai(String maLo) {
        boolean tonTai = false;
        String sql = "SELECT maLoThuoc FROM LoThuoc WHERE maLoThuoc = ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maLo);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) tonTai = true; 
        } catch (Exception e) { e.printStackTrace(); }
        return tonTai;
    }

    public List<PhieuNhap> getAllPhieuNhap(String tuKhoa) {
        List<PhieuNhap> ds = new ArrayList<>();
        String sql = "SELECT pn.maPhieuNhap, pn.ngayNhap, ncc.maNhaCungCap, ncc.tenNhaCungCap, nv.maNhanVien, nv.hoTen, " +
                     "COALESCE(SUM(ctpn.soLuong * ctpn.donGiaNhap), 0) AS tongTien " +
                     "FROM PhieuNhap pn " +
                     "JOIN NhaCungCap ncc ON pn.maNhaCungCap = ncc.maNhaCungCap " +
                     "JOIN NhanVien nv ON pn.maNhanVien = nv.maNhanVien " +
                     "LEFT JOIN ChiTietPhieuNhap ctpn ON pn.maPhieuNhap = ctpn.maPhieuNhap " +
                     "WHERE pn.maPhieuNhap LIKE ? OR ncc.tenNhaCungCap LIKE ? " +
                     "GROUP BY pn.maPhieuNhap, pn.ngayNhap, ncc.maNhaCungCap, ncc.tenNhaCungCap, nv.maNhanVien, nv.hoTen " +
                     "ORDER BY pn.ngayNhap DESC";

        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            String searchPattern = "%" + (tuKhoa == null ? "" : tuKhoa) + "%";
            pst.setString(1, searchPattern);
            pst.setString(2, searchPattern);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                PhieuNhap pn = new PhieuNhap();
                pn.setMaPhieuNhap(rs.getString("maPhieuNhap"));
                pn.setNgayNhap(rs.getTimestamp("ngayNhap"));
                
                entity.NhaCungCap ncc = new entity.NhaCungCap();
                ncc.setMaNhaCungCap(rs.getString("maNhaCungCap"));
                ncc.setTenNhaCungCap(rs.getString("tenNhaCungCap"));
                pn.setNhaCungCap(ncc);
                
                entity.NhanVien nv = new entity.NhanVien();
                nv.setMaNhanVien(rs.getString("maNhanVien"));
                nv.setHoTen(rs.getString("hoTen"));
                pn.setNhanVien(nv);
                
                pn.setTongTien(rs.getDouble("tongTien"));
                ds.add(pn);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return ds;
    }
    
}