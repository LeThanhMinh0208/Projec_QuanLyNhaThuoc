package dao;

import connectDB.ConnectDB;
import entity.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAO_PhieuXuat {

    // 1. LẤY DANH SÁCH PHIẾU XUẤT THEO LOẠI (Dùng để load lên Bảng theo từng Tab)
    public List<PhieuXuat> getPhieuXuatByLoai(String loaiXuat) {
        List<PhieuXuat> list = new ArrayList<>();
        String sql = "SELECT p.*, nv.hoTen, n.tenNhaCungCap FROM PhieuXuat p " +
                     "LEFT JOIN NhanVien nv ON p.maNhanVien = nv.maNhanVien " +
                     "LEFT JOIN NhaCungCap n ON p.maNhaCungCap = n.maNhaCungCap " +
                     "WHERE p.loaiXuat = ? ORDER BY p.ngayXuat DESC";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, loaiXuat);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                PhieuXuat px = new PhieuXuat();
                px.setMaPhieuXuat(rs.getString("maPhieuXuat"));
                px.setNgayXuat(rs.getDate("ngayXuat"));
                px.setLoaiXuat(rs.getString("loaiXuat"));
                px.setKhoNhan(rs.getString("khoNhan"));
                px.setLyDoHuy(rs.getString("lyDoHuy"));
                px.setGhiChu(rs.getString("ghiChu"));
                
                NhanVien nv = new NhanVien();
                nv.setMaNhanVien(rs.getString("maNhanVien"));
                nv.setHoTen(rs.getString("hoTen"));
                px.setNhanVienLap(nv);
                
                if (rs.getString("maNhaCungCap") != null) {
                    NhaCungCap ncc = new NhaCungCap();
                    ncc.setMaNhaCungCap(rs.getString("maNhaCungCap"));
                    ncc.setTenNhaCungCap(rs.getString("tenNhaCungCap"));
                    px.setNhaCungCap(ncc);
                }
                list.add(px);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 2. SINH MÃ PHIẾU XUẤT TỰ ĐỘNG (VD: PX001)
    public String getMaPhieuMoi() {
        String ma = "PX001";
        try (Connection con = ConnectDB.getInstance().getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT MAX(maPhieuXuat) FROM PhieuXuat")) {
            if (rs.next() && rs.getString(1) != null) {
                int so = Integer.parseInt(rs.getString(1).substring(2)) + 1;
                ma = String.format("PX%03d", so);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return ma;
    }
    public boolean luuPhieuXuatVaCapNhatKho(PhieuXuat px, List<ChiTietPhieuXuat> listCT) {
        Connection con = ConnectDB.getInstance().getConnection();
        try {
            con.setAutoCommit(false);
            
            // 1. Lưu PhieuXuat
            String sqlPX = "INSERT INTO PhieuXuat (maPhieuXuat, ngayXuat, loaiXuat, khoNhan, maNhaCungCap, ghiChu) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstPX = con.prepareStatement(sqlPX);
            pstPX.setString(1, px.getMaPhieuXuat());
            pstPX.setDate(2, px.getNgayXuat());
            pstPX.setString(3, px.getLoaiXuat());
            pstPX.setString(4, px.getKhoNhan());
            pstPX.setString(5, px.getNhaCungCap() != null ? px.getNhaCungCap().getMaNhaCungCap() : null);
            pstPX.setString(6, px.getGhiChu());
            pstPX.executeUpdate();

            // 2. Lưu ChiTiet + Trừ kho xuất + Cộng kho nhận
            String sqlCT = "INSERT INTO ChiTietPhieuXuat (maPhieuXuat, maLoThuoc, soLuongXuat) VALUES (?, ?, ?)";
            PreparedStatement pstCT = con.prepareStatement(sqlCT);
            
            String sqlTruKho = "UPDATE LoThuoc SET soLuongTon = soLuongTon - ? WHERE maLoThuoc = ? AND viTriKho = ?";
            PreparedStatement pstTruKho = con.prepareStatement(sqlTruKho);
            
            String sqlCheckKhoNhan = "SELECT * FROM LoThuoc WHERE maLoThuoc = ? AND viTriKho = ?";
            String sqlCongKho = "UPDATE LoThuoc SET soLuongTon = soLuongTon + ? WHERE maLoThuoc = ? AND viTriKho = ?";
            String sqlTaoLoMoi = "INSERT INTO LoThuoc (maLoThuoc, maThuoc, soLuongTon, hanSuDung, viTriKho) SELECT maLoThuoc, maThuoc, ?, hanSuDung, ? FROM LoThuoc WHERE maLoThuoc = ?";

            for (ChiTietPhieuXuat ct : listCT) {
                // Lưu CT
                pstCT.setString(1, px.getMaPhieuXuat());
                pstCT.setString(2, ct.getLoThuoc().getMaLoThuoc());
                pstCT.setInt(3, ct.getSoLuongXuat());
                pstCT.executeUpdate();

                // TRỪ KHO XUẤT
                pstTruKho.setInt(1, ct.getSoLuongXuat());
                pstTruKho.setString(2, ct.getLoThuoc().getMaLoThuoc());
                pstTruKho.setString(3, "KHO_DU_TRU"); // Kho nguồn
                pstTruKho.executeUpdate();

                // CỘNG KHO NHẬN (Chỉ làm nếu là Chuyển Kho)
                if ("CHUYEN_KHO".equals(px.getLoaiXuat())) {
                    PreparedStatement check = con.prepareStatement(sqlCheckKhoNhan);
                    check.setString(1, ct.getLoThuoc().getMaLoThuoc());
                    check.setString(2, px.getKhoNhan());
                    ResultSet rs = check.executeQuery();
                    
                    if (rs.next()) { // Nếu bên kho nhận đã có mã lô này -> Cộng thêm
                        PreparedStatement pstCong = con.prepareStatement(sqlCongKho);
                        pstCong.setInt(1, ct.getSoLuongXuat());
                        pstCong.setString(2, ct.getLoThuoc().getMaLoThuoc());
                        pstCong.setString(3, px.getKhoNhan());
                        pstCong.executeUpdate();
                    } else { // Nếu kho nhận chưa có -> Tạo dòng mới
                        PreparedStatement pstTao = con.prepareStatement(sqlTaoLoMoi);
                        pstTao.setInt(1, ct.getSoLuongXuat());
                        pstTao.setString(2, px.getKhoNhan());
                        pstTao.setString(3, ct.getLoThuoc().getMaLoThuoc());
                        pstTao.executeUpdate();
                    }
                }
            }
            con.commit();
            return true;
        } catch (SQLException e) {
            try { con.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException e) {}
        }
    }
}