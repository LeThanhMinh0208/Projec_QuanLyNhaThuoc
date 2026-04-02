package dao;

import connectDB.ConnectDB;
import entity.ChiTietDonDatHang;
import entity.DonDatHang;
import entity.DonViQuyDoi;
import entity.NhaCungCap;
import entity.NhanVien;
import entity.Thuoc;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class DAO_DonDatHang {

    // 1. Lấy danh sách tất cả đơn đặt hàng
    public List<DonDatHang> getAllDonDatHang() {
        List<DonDatHang> list = new ArrayList<>();
        String sql = "SELECT d.*, n.tenNhaCungCap, nv.hoTen FROM DonDatHang d " +
                     "JOIN NhaCungCap n ON d.maNhaCungCap = n.maNhaCungCap " +
                     "JOIN NhanVien nv ON d.maNhanVien = nv.maNhanVien " +
                     "ORDER BY d.ngayDat DESC";
        try (Connection con = ConnectDB.getInstance().getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                DonDatHang don = new DonDatHang();
                don.setMaDonDatHang(rs.getString("maDonDatHang"));
                don.setNgayDat(rs.getDate("ngayDat"));
                don.setNgayGiaoDuKien(rs.getDate("ngayGiaoDuKien"));
                don.setTongTienDuTinh(rs.getDouble("tongTienDuTinh"));
                don.setTrangThai(rs.getString("trangThai")); 
                don.setGhiChu(rs.getString("ghiChu"));
                
                NhaCungCap ncc = new NhaCungCap(); 
                ncc.setMaNhaCungCap(rs.getString("maNhaCungCap")); 
                ncc.setTenNhaCungCap(rs.getString("tenNhaCungCap")); 
                don.setNhaCungCap(ncc);
                
                NhanVien nv = new NhanVien(); 
                nv.setMaNhanVien(rs.getString("maNhanVien")); 
                nv.setHoTen(rs.getString("hoTen")); 
                don.setNhanVien(nv);
                
                list.add(don);
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return list;
    }

    // 2. Lấy chi tiết thuốc của 1 Đơn cụ thể
    public List<ChiTietDonDatHang> getChiTietByMaDon(String maDon) {
        List<ChiTietDonDatHang> list = new ArrayList<>();
        String sql = "SELECT ct.*, t.maThuoc, t.tenThuoc, dv.tenDonVi, dv.tyLeQuyDoi " +
                     "FROM ChiTietDonDatHang ct " +
                     "JOIN DonViQuyDoi dv ON ct.maQuyDoi = dv.maQuyDoi " +
                     "JOIN Thuoc t ON dv.maThuoc = t.maThuoc " +
                     "WHERE ct.maDonDatHang = ?";
                     
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
             
            pst.setString(1, maDon);
            ResultSet rs = pst.executeQuery();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            
            while (rs.next()) {
                ChiTietDonDatHang ct = new ChiTietDonDatHang();
                
                Thuoc t = new Thuoc(); 
                t.setMaThuoc(rs.getString("maThuoc")); 
                t.setTenThuoc(rs.getString("tenThuoc")); 
                ct.setThuoc(t);
                
                DonViQuyDoi dv = new DonViQuyDoi(); 
                dv.setMaQuyDoi(rs.getString("maQuyDoi")); 
                dv.setTenDonVi(rs.getString("tenDonVi")); 
                dv.setTyLeQuyDoi(rs.getInt("tyLeQuyDoi")); 
                ct.setDonViQuyDoi(dv);
                
                ct.setSoLuongDat(rs.getInt("soLuongDat"));
                ct.setSoLuongDaNhan(rs.getInt("soLuongDaNhan"));
                ct.setDonGiaDuKien(rs.getDouble("donGiaDuKien")); 
                
                ct.setMaLo(rs.getString("maLo"));
                if(rs.getDate("ngaySanXuat") != null) ct.setNgaySanXuatTemp(sdf.format(rs.getDate("ngaySanXuat")));
                if(rs.getDate("hanSuDung") != null) ct.setHanSuDung(sdf.format(rs.getDate("hanSuDung")));
                
                list.add(ct);
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        return list;
    }

    // 3. Cập nhật trạng thái đơn (Dùng cho Nút Hủy Đơn)
    public boolean updateTrangThaiDonHang(String maDon, String trangThaiMoi) {
        String sql = "UPDATE DonDatHang SET trangThai = ? WHERE maDonDatHang = ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            
            pst.setString(1, trangThaiMoi);
            pst.setString(2, maDon);
            return pst.executeUpdate() > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 4. Sinh mã tự động (DDH001, DDH002...)
    public String getMaDonMoi() {
        String ma = "DDH001";
        // ĐK: LEN(maDonDatHang) = 6 để chỉ lấy DDH001, bỏ qua DDH001.1
        String sql = "SELECT MAX(maDonDatHang) FROM DonDatHang WHERE LEN(maDonDatHang) = 6"; 
        
        try (Connection con = connectDB.ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
             
            if (rs.next() && rs.getString(1) != null) {
                int so = Integer.parseInt(rs.getString(1).substring(3)) + 1;
                ma = String.format("DDH%03d", so);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ma;
    }
    // =========================================================================
    // NGHIỆP VỤ NÂNG CAO: TÁCH ĐƠN TỰ ĐỘNG KHI GIAO THIẾU KÈM NGÀY HẸN
    // =========================================================================
    public boolean xuLyGiaoMotPhanVaTachDon(DonDatHang donCu, List<ChiTietDonDatHang> listChiTietCu, int soNgayHen) {
        Connection con = null;
        try {
            con = ConnectDB.getInstance().getConnection();
            con.setAutoCommit(false); // Bật chế độ Transaction

            // 1. CẬP NHẬT ĐƠN CŨ THÀNH "GIAO_MOT_PHAN"
            String sqlUpdateOld = "UPDATE DonDatHang SET trangThai = 'GIAO_MOT_PHAN' WHERE maDonDatHang = ?";
            PreparedStatement pstUpdateOld = con.prepareStatement(sqlUpdateOld);
            pstUpdateOld.setString(1, donCu.getMaDonDatHang());
            pstUpdateOld.executeUpdate();

            // 2. TÍNH TOÁN XEM CÓ THẬT SỰ BỊ THIẾU HÀNG KHÔNG
            boolean coHangThieu = false;
            double tongTienMoi = 0;
            for (ChiTietDonDatHang ct : listChiTietCu) {
                int slThieu = ct.getSoLuongDat() - ct.getSoLuongDaNhan();
                if (slThieu > 0) {
                    coHangThieu = true;
                    tongTienMoi += slThieu * ct.getDonGiaDuKien(); 
                }
            }

            // Nếu không thiếu hàng (giao đủ 100%) thì chỉ cần commit là xong
            if (!coHangThieu) {
                con.commit();
                return true;
            }

            // 3. NẾU THIẾU -> TẠO ĐƠN ĐẶT HÀNG MỚI (CÓ CỘNG THÊM NGÀY HẸN)
            String maDonMoi = getMaDonMoi(); 
            String sqlInsertNewDon = "INSERT INTO DonDatHang (maDonDatHang, maNhaCungCap, maNhanVien, ngayDat, ngayGiaoDuKien, tongTienDuTinh, trangThai, ghiChu) " +
                                     "VALUES (?, ?, ?, GETDATE(), DATEADD(day, ?, GETDATE()), ?, 'CHO_GIAO', ?)";
            
            PreparedStatement pstInsertNew = con.prepareStatement(sqlInsertNewDon);
            pstInsertNew.setString(1, maDonMoi);
            pstInsertNew.setString(2, donCu.getNhaCungCap().getMaNhaCungCap());
            pstInsertNew.setString(3, donCu.getNhanVien().getMaNhanVien());
            pstInsertNew.setInt(4, soNgayHen); // <--- Chèn số ngày hẹn do UI truyền vào
            pstInsertNew.setDouble(5, tongTienMoi); 
            pstInsertNew.setString(6, "Đơn tách tự động từ đơn giao thiếu: " + donCu.getMaDonDatHang()); 
            pstInsertNew.executeUpdate();

            // 4. TẠO CHI TIẾT ĐƠN HÀNG MỚI
            String sqlInsertNewCT = "INSERT INTO ChiTietDonDatHang (maDonDatHang, maQuyDoi, soLuongDat, soLuongDaNhan, donGiaDuKien) " +
                                    "VALUES (?, ?, ?, 0, ?)";
            PreparedStatement pstInsertCT = con.prepareStatement(sqlInsertNewCT);

            for (ChiTietDonDatHang ct : listChiTietCu) {
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

            // 5. CHỐT LƯU VÀO CSDL
            con.commit();
            return true;

        } catch (Exception e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
 // =========================================================================
    // LƯU ĐƠN ĐẶT HÀNG MỚI (Dùng Transaction)
    // =========================================================================
    public boolean luuDonDatHangMoi(DonDatHang don, List<ChiTietDonDatHang> listChiTiet) {
        Connection con = null;
        try {
            con = ConnectDB.getInstance().getConnection();
            con.setAutoCommit(false); // Bật chế độ Transaction

            // 1. Lưu vào bảng DonDatHang
            String sqlDon = "INSERT INTO DonDatHang (maDonDatHang, maNhaCungCap, maNhanVien, ngayDat, ngayGiaoDuKien, tongTienDuTinh, trangThai, ghiChu) " +
                            "VALUES (?, ?, ?, GETDATE(), ?, ?, ?, ?)";
            try (PreparedStatement pstDon = con.prepareStatement(sqlDon)) {
                pstDon.setString(1, don.getMaDonDatHang());
                pstDon.setString(2, don.getNhaCungCap().getMaNhaCungCap());
                pstDon.setString(3, don.getNhanVien().getMaNhanVien());
                pstDon.setDate(4, don.getNgayGiaoDuKien());
                pstDon.setDouble(5, don.getTongTienDuTinh());
                pstDon.setString(6, don.getTrangThai());
                pstDon.setString(7, don.getGhiChu());
                pstDon.executeUpdate();
            }

            // 2. Lưu vào bảng ChiTietDonDatHang
            String sqlCT = "INSERT INTO ChiTietDonDatHang (maDonDatHang, maQuyDoi, soLuongDat, soLuongDaNhan, donGiaDuKien) " +
                           "VALUES (?, ?, ?, 0, ?)";
            try (PreparedStatement pstCT = con.prepareStatement(sqlCT)) {
                for (ChiTietDonDatHang ct : listChiTiet) {
                    pstCT.setString(1, don.getMaDonDatHang());
                    pstCT.setString(2, ct.getDonViQuyDoi().getMaQuyDoi());
                    pstCT.setInt(3, ct.getSoLuongDat());
                    pstCT.setDouble(4, ct.getDonGiaDuKien());
                    pstCT.addBatch(); // Gom lại chạy 1 lần
                }
                pstCT.executeBatch();
            }

            con.commit(); // Chốt lưu
            return true;

        } catch (Exception e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    public String getMaDonGiaoThieu(String maDonGoc) {
        String baseId = maDonGoc;
        if (maDonGoc.contains(".")) {
            baseId = maDonGoc.substring(0, maDonGoc.indexOf(".")); 
        }
        
        String newId = baseId + ".1"; 
        int maxSuffix = 0;
        
        String sql = "SELECT maDonDatHang FROM DonDatHang WHERE maDonDatHang LIKE ?";
        
        // SỬA CHỖ NÀY: Dùng Connection riêng, tách biệt hoàn toàn để không đá nhau với hàm Cha
        java.sql.Connection con = null;
        java.sql.PreparedStatement pst = null;
        java.sql.ResultSet rs = null;
        
        try {
            // Lấy kết nối mới hoàn toàn
            con = connectDB.ConnectDB.getInstance().getConnection(); 
            pst = con.prepareStatement(sql);
            pst.setString(1, baseId + ".%");
            rs = pst.executeQuery();
            
            while (rs.next()) {
                String id = rs.getString(1); 
                try {
                    int suffix = Integer.parseInt(id.substring(id.indexOf(".") + 1));
                    if (suffix > maxSuffix) {
                        maxSuffix = suffix;
                    }
                } catch (Exception e) {} 
            }
            
            if (maxSuffix > 0) {
                newId = baseId + "." + (maxSuffix + 1);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Chỉ đóng ResultSet và PreparedStatement, TUYỆT ĐỐI KHÔNG ĐÓNG Connection
            // Vì thằng DAO_PhieuNhap vẫn đang dùng nó để làm việc!
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (pst != null) pst.close(); } catch (Exception e) {}
        }
        return newId;
    }
}