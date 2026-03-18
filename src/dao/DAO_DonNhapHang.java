package dao;

import connectDB.ConnectDB;
import entity.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DAO_DonNhapHang {

    public List<DonNhapHang> getAllDonNhapHang() {
        List<DonNhapHang> list = new ArrayList<>();
        String sql = "SELECT d.*, n.TenNhaCungCap, nv.hoTen FROM DonNhapHang d " +
                     "JOIN NhaCungCap n ON d.MaNhaCungCap = n.MaNhaCungCap " +
                     "JOIN NhanVien nv ON d.MaNhanVien = nv.MaNhanVien " +
                     "ORDER BY d.ngayLap DESC";
        try (Connection con = ConnectDB.getInstance().getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                DonNhapHang don = new DonNhapHang();
                don.setMaDonNhap(rs.getString("MaDonNhap"));
                don.setNgayLap(rs.getDate("NgayLap"));
                don.setNgayHenGiao(rs.getDate("NgayHenGiao"));
                don.setTongTienDuTinh(rs.getDouble("TongTienDuTinh"));
                don.setTrangThai(rs.getString("TrangThai"));
                don.setGhiChu(rs.getString("GhiChu"));
                
                NhaCungCap ncc = new NhaCungCap(); 
                ncc.setMaNhaCungCap(rs.getString("MaNhaCungCap")); 
                ncc.setTenNhaCungCap(rs.getString("TenNhaCungCap")); 
                don.setNhaCungCap(ncc);
                
                NhanVien nv = new NhanVien(); 
                nv.setMaNhanVien(rs.getString("MaNhanVien")); 
                nv.setHoTen(rs.getString("hoTen")); 
                don.setNhanVien(nv);
                
                list.add(don);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public String getMaDonMoi() {
        String ma = "DDH001";
        try (Connection con = ConnectDB.getInstance().getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT MAX(maDonNhap) FROM DonNhapHang")) {
            if (rs.next() && rs.getString(1) != null) {
                int so = Integer.parseInt(rs.getString(1).substring(3)) + 1;
                ma = String.format("DDH%03d", so);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return ma;
    }

    public boolean themDonNhapHangVaChiTiet(DonNhapHang don, List<ChiTietDonNhapHang> listCT) {
        Connection con = ConnectDB.getInstance().getConnection();
        PreparedStatement pstDon = null, pstCT = null;
        try {
            con.setAutoCommit(false);
            String sqlDon = "INSERT INTO DonNhapHang (maDonNhap, maNhaCungCap, maNhanVien, ngayLap, ngayHenGiao, tongTienDuTinh, trangThai, ghiChu) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            pstDon = con.prepareStatement(sqlDon);
            pstDon.setString(1, don.getMaDonNhap());
            pstDon.setString(2, don.getNhaCungCap().getMaNhaCungCap());
            pstDon.setString(3, don.getNhanVien().getMaNhanVien());
            pstDon.setDate(4, don.getNgayLap());
            pstDon.setDate(5, don.getNgayHenGiao());
            pstDon.setDouble(6, don.getTongTienDuTinh());
            pstDon.setString(7, don.getTrangThai());
            pstDon.setString(8, don.getGhiChu());

            if (pstDon.executeUpdate() <= 0) { con.rollback(); return false; }

            String sqlCT = "INSERT INTO ChiTietDonNhapHang (maDonNhap, maQuyDoi, soLuongDat, soLuongDaNhan, donGiaDuKien) VALUES (?, ?, ?, ?, ?)";
            pstCT = con.prepareStatement(sqlCT);
            for (ChiTietDonNhapHang ct : listCT) {
                pstCT.setString(1, don.getMaDonNhap());
                pstCT.setString(2, ct.getDonViQuyDoi().getMaQuyDoi());
                pstCT.setInt(3, ct.getSoLuongDat());
                pstCT.setInt(4, 0); 
                pstCT.setDouble(5, ct.getDonGiaDuKien());
                pstCT.addBatch();
            }
            pstCT.executeBatch();
            con.commit();
            return true;
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            e.printStackTrace(); return false;
        } finally {
            try { if (con != null) con.setAutoCommit(true);
                  if (pstDon != null) pstDon.close();
                  if (pstCT != null) pstCT.close(); } catch (SQLException e) {}
        }
    }

    public ArrayList<ChiTietDonNhapHang> getChiTietByMaDon(String maDon) {
        ArrayList<ChiTietDonNhapHang> list = new ArrayList<>();
        
        // Bỏ các cột maLo, ngaySanXuat ra khỏi câu lệnh SELECT
        String sql = "SELECT ct.*, t.maThuoc, t.tenThuoc, dv.tenDonVi, dv.tyLeQuyDoi " +
                     "FROM ChiTietDonNhapHang ct " +
                     "JOIN DonViQuyDoi dv ON ct.maQuyDoi = dv.maQuyDoi " +
                     "JOIN Thuoc t ON dv.maThuoc = t.maThuoc " +
                     "WHERE ct.maDonNhap = ?";
                     
        try (java.sql.Connection con = connectDB.ConnectDB.getInstance().getConnection();
             java.sql.PreparedStatement pst = con.prepareStatement(sql)) {
             
            pst.setString(1, maDon);
            java.sql.ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                ChiTietDonNhapHang ct = new ChiTietDonNhapHang();
                
                entity.Thuoc t = new entity.Thuoc(); 
                t.setMaThuoc(rs.getString("maThuoc")); 
                t.setTenThuoc(rs.getString("tenThuoc")); 
                ct.setThuoc(t);
                
                entity.DonViQuyDoi dv = new entity.DonViQuyDoi(); 
                dv.setMaQuyDoi(rs.getString("maQuyDoi")); 
                dv.setTenDonVi(rs.getString("tenDonVi")); 
                dv.setTyLeQuyDoi(rs.getInt("tyLeQuyDoi")); 
                ct.setDonViQuyDoi(dv);
                
                // CHỈ LẤY NHỮNG GÌ BẢNG CHITIETDON CÓ
                ct.setSoLuongDat(rs.getInt("soLuongDat"));
                ct.setSoLuongDaNhan(rs.getInt("soLuongDaNhan"));
                ct.setDonGiaDuKien(rs.getDouble("donGiaDuKien")); 
                
                // Gán rỗng để sếp TỰ NHẬP TAY trên giao diện
                ct.setMaLo("");
                ct.setNgaySanXuatTemp("");
                ct.setHanSuDung("");
                
                list.add(ct);
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        return list;
    }

    public List<DonNhapHang> getDonHangChoNhap() {
        return getDonHangByTrangThai("IN ('CHO_DUYET', 'DANG_GIAO')");
    }

    public List<DonNhapHang> getDonHangDaNhap() {
        return getDonHangByTrangThai("= 'DA_NHAP_KHO'");
    }

    private List<DonNhapHang> getDonHangByTrangThai(String dieuKien) {
        List<DonNhapHang> list = new ArrayList<>();
        String sql = "SELECT d.*, n.TenNhaCungCap, nv.hoTen FROM DonNhapHang d " +
                     "JOIN NhaCungCap n ON d.MaNhaCungCap = n.MaNhaCungCap " +
                     "JOIN NhanVien nv ON d.MaNhanVien = nv.MaNhanVien " +
                     "WHERE d.TrangThai " + dieuKien + " ORDER BY d.ngayLap DESC";
        try (Connection con = ConnectDB.getInstance().getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                DonNhapHang don = new DonNhapHang();
                don.setMaDonNhap(rs.getString("MaDonNhap"));
                don.setNgayLap(rs.getDate("NgayLap"));
                don.setNgayHenGiao(rs.getDate("NgayHenGiao"));
                don.setTongTienDuTinh(rs.getDouble("TongTienDuTinh"));
                don.setTrangThai(rs.getString("TrangThai"));
                don.setGhiChu(rs.getString("GhiChu"));
                NhaCungCap ncc = new NhaCungCap(); ncc.setMaNhaCungCap(rs.getString("MaNhaCungCap")); ncc.setTenNhaCungCap(rs.getString("TenNhaCungCap")); don.setNhaCungCap(ncc);
                NhanVien nv = new NhanVien(); nv.setMaNhanVien(rs.getString("MaNhanVien")); nv.setHoTen(rs.getString("hoTen")); don.setNhanVien(nv);
                list.add(don);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public DonNhapHang getDonHangByMa(String maDon) {
        List<DonNhapHang> list = getDonHangByTrangThai("= '" + maDon + "'"); // Tái sử dụng hàm trên
        return list.isEmpty() ? null : list.get(0);
    }

    // CẬP NHẬT CẢ GIÁ NHẬP THỰC TẾ
    public boolean capNhatThongTinThucNhap(ChiTietDonNhapHang ct) {
        // Chỉ Update đúng 2 cột có sẵn trong SQL
        String sql = "UPDATE ChiTietDonNhapHang SET soLuongDaNhan = ?, donGiaDuKien = ? WHERE maDonNhap = ? AND maQuyDoi = ?";
        try (java.sql.Connection con = connectDB.ConnectDB.getInstance().getConnection();
             java.sql.PreparedStatement pst = con.prepareStatement(sql)) {
            
            pst.setInt(1, ct.getSoLuongDaNhan());
            pst.setDouble(2, ct.getDonGiaDuKien()); 
            pst.setString(3, ct.getDonNhapHang().getMaDonNhap());
            pst.setString(4, ct.getDonViQuyDoi().getMaQuyDoi());
            
            return pst.executeUpdate() > 0;
        } catch (Exception e) { 
            e.printStackTrace(); 
            return false; 
        }
    }

    public boolean capNhatTrangThaiVaTien(String maDonNhap, double tongTienThucTe) {
        String sql = "UPDATE DonNhapHang SET TrangThai = 'DA_NHAP_KHO', tongTienDuTinh = ? WHERE MaDonNhap = ?";
        try (Connection con = ConnectDB.getInstance().getConnection(); PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setDouble(1, tongTienThucTe);
            stmt.setString(2, maDonNhap);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }
}