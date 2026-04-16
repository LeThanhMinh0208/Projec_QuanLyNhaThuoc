package dao;

import connectDB.ConnectDB;
import entity.LoThuoc;
import entity.Thuoc;
import entity.NhaCungCap;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAO_LoThuoc {
    
    public ArrayList<LoThuoc> getAllLoThuoc() {
        ArrayList<LoThuoc> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            
            // Đã thêm LEFT JOIN NhaCungCap và lấy cột ngayNhapKho
            String sql = "SELECT l.maLoThuoc, l.ngaySanXuat, l.hanSuDung, l.soLuongTon, l.giaNhap, l.viTriKho, l.trangThai, l.ngayNhapKho, l.maNhaCungCap, " +
                         "t.maThuoc, t.tenThuoc, t.hinhAnh, t.donViCoBan, " +
                         "ncc.tenNhaCungCap " +
                         "FROM LoThuoc l " +
                         "JOIN Thuoc t ON l.maThuoc = t.maThuoc " +
                         "LEFT JOIN NhaCungCap ncc ON l.maNhaCungCap = ncc.maNhaCungCap";
            
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Thuoc thuoc = new Thuoc();
                thuoc.setMaThuoc(rs.getString("maThuoc"));
                thuoc.setTenThuoc(rs.getString("tenThuoc"));
                thuoc.setHinhAnh(rs.getString("hinhAnh"));
                thuoc.setDonViCoBan(rs.getString("donViCoBan"));

                LoThuoc lo = new LoThuoc(
                    rs.getString("maLoThuoc"),
                    rs.getDate("ngaySanXuat"),
                    rs.getDate("hanSuDung"),
                    rs.getInt("soLuongTon"),
                    rs.getDouble("giaNhap"),
                    rs.getString("viTriKho"),
                    thuoc,
                    rs.getInt("trangThai")
                );

                // Ánh xạ 2 trường mới từ DB lên Object
                lo.setNgayNhapKho(rs.getDate("ngayNhapKho"));
                
                if (rs.getString("maNhaCungCap") != null) {
                    NhaCungCap ncc = new NhaCungCap();
                    ncc.setMaNhaCungCap(rs.getString("maNhaCungCap"));
                    ncc.setTenNhaCungCap(rs.getString("tenNhaCungCap"));
                    lo.setNhaCungCap(ncc);
                }

                list.add(lo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean themLoThuoc(LoThuoc lo) {
        // Đã cập nhật câu INSERT có thêm ngayNhapKho và maNhaCungCap
        String sql = "INSERT INTO LoThuoc (MaLoThuoc, MaThuoc, NgaySanXuat, HanSuDung, SoLuongTon, GiaNhap, ViTriKho, trangThai, ngayNhapKho, maNhaCungCap) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, 1, ?, ?)";
        
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
             
            stmt.setString(1, lo.getMaLoThuoc());
            stmt.setString(2, lo.getThuoc().getMaThuoc()); 
            stmt.setDate(3, lo.getNgaySanXuat());
            stmt.setDate(4, lo.getHanSuDung());
            stmt.setInt(5, lo.getSoLuongTon());
            stmt.setDouble(6, lo.getGiaNhap());
            stmt.setString(7, lo.getViTriKho());
            
            // Truyền 2 tham số mới vào DB
            stmt.setDate(8, lo.getNgayNhapKho());
            if (lo.getNhaCungCap() != null) {
                stmt.setString(9, lo.getNhaCungCap().getMaNhaCungCap());
            } else {
                stmt.setNull(9, java.sql.Types.VARCHAR);
            }
            
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ Lỗi SQL khi thêm Lô: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<LoThuoc> getLoThuocTheoFEFO(String maThuoc, String viTriKho) {
        List<LoThuoc> list = new ArrayList<>();
        String sql = "SELECT * FROM LoThuoc WHERE maThuoc = ? AND viTriKho = ? AND soLuongTon > 0 AND trangThai = 1 ORDER BY hanSuDung ASC";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maThuoc);
            pst.setString(2, viTriKho);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                LoThuoc lo = new LoThuoc();
                lo.setMaLoThuoc(rs.getString("maLoThuoc"));
                lo.setSoLuongTon(rs.getInt("soLuongTon"));
                lo.setHanSuDung(rs.getDate("hanSuDung"));
                lo.setNgaySanXuat(rs.getDate("ngaySanXuat"));
                lo.setViTriKho(rs.getString("viTriKho"));
                lo.setTrangThai(rs.getInt("trangThai"));
                list.add(lo);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public ArrayList<LoThuoc> getLoThuocBanDuocByMaThuoc(String maThuoc) {
        ArrayList<LoThuoc> ds = new ArrayList<>();
        // Chỉ lấy lô thuộc KHO_BAN_HANG, còn hạn, còn tồn
        String sql = 
                "SELECT * FROM LoThuoc " +
                "WHERE maThuoc = ? " +
                "  AND soLuongTon > 0 " +
                "  AND trangThai = 1 " + 
                "  AND viTriKho = 'KHO_BAN_HANG' " +
                "  AND hanSuDung >= CAST(GETDATE() AS DATE) " +
                "ORDER BY hanSuDung ASC";
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
                lo.setViTriKho(rs.getString("viTriKho"));
                lo.setTrangThai(rs.getInt("trangThai"));

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

    public String getLoFEFO(String maThuoc) {
        String sql = "SELECT TOP 1 maLoThuoc FROM LoThuoc " +
                     "WHERE maThuoc = ? " +
                     "  AND soLuongTon > 0 " +
                     "  AND trangThai = 1 " + 
                     "  AND viTriKho = 'KHO_BAN_HANG' " +
                     "  AND hanSuDung > CAST(GETDATE() AS DATE) " +
                     "ORDER BY hanSuDung ASC";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maThuoc);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getString("maLoThuoc");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<LoThuoc> getLoThuocTraNCC(String maThuoc, String viTriKho) {
        List<LoThuoc> list = new ArrayList<>();
        String sql = "SELECT * FROM LoThuoc WHERE maThuoc = ? AND viTriKho = ? AND soLuongTon > 0 AND trangThai = 1 ORDER BY hanSuDung ASC";
        
        try (Connection con = connectDB.ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            
            pst.setString(1, maThuoc);
            pst.setString(2, viTriKho);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                LoThuoc lo = new LoThuoc();
                lo.setMaLoThuoc(rs.getString("maLoThuoc"));
                lo.setSoLuongTon(rs.getInt("soLuongTon"));
                lo.setHanSuDung(rs.getDate("hanSuDung"));
                lo.setNgaySanXuat(rs.getDate("ngaySanXuat"));
                lo.setViTriKho(rs.getString("viTriKho"));
                lo.setGiaNhap(rs.getDouble("giaNhap")); 
                lo.setTrangThai(rs.getInt("trangThai"));
                
             
                String maNCC = rs.getString("maNhaCungCap");
                if (maNCC != null) {
                    NhaCungCap ncc = new NhaCungCap();
                    ncc.setMaNhaCungCap(maNCC);
                    lo.setNhaCungCap(ncc);
                }
                
                list.add(lo);
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        return list;
    }
    public boolean capNhatTrangThaiLo(String maLoThuoc, int trangThaiMoi) {
        String sql = "UPDATE LoThuoc SET trangThai = ? WHERE maLoThuoc = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, trangThaiMoi);
            ps.setString(2, maLoThuoc);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getSoLuongTon(String maLoThuoc) {
        int soLuong = 0;
        String sql = "SELECT soLuongTon FROM LoThuoc WHERE maLoThuoc = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maLoThuoc);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                soLuong = rs.getInt("soLuongTon");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return soLuong;
    }

    public Object[] getThongTinTheLo(String maLoThuoc) {
        Object[] result = new Object[]{"Không xác định", 0, 0, 0};
        
        // Vì Controller vẫn cần 4 phần tử nên em giữ nguyên structure, 
        // nhưng tenNCC (result[0]) không cần lấy bằng SQL cồng kềnh nữa, Controller sẽ lấy trực tiếp từ Entity.
        String sql = "SELECT " +
            "(SELECT ISNULL(SUM(ctpn.soLuong * dq.tyLeQuyDoi), 0) FROM ChiTietPhieuNhap ctpn JOIN DonViQuyDoi dq ON ctpn.maQuyDoi = dq.maQuyDoi WHERE ctpn.maLoThuoc = ?) AS slNhapBanDau, " +
            "(SELECT ISNULL(SUM(cthd.soLuong * dq.tyLeQuyDoi), 0) FROM ChiTietHoaDon cthd JOIN DonViQuyDoi dq ON cthd.maQuyDoi = dq.maQuyDoi WHERE cthd.maLoThuoc = ?) AS slDaBan, " +
            "(SELECT ISNULL(SUM(soLuong), 0) FROM ChiTietPhieuXuat WHERE maLoThuoc = ? AND loaiPhieu IN (2, 3)) AS slXuatTra";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setString(1, maLoThuoc);
            ps.setString(2, maLoThuoc);
            ps.setString(3, maLoThuoc);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result[1] = rs.getInt("slNhapBanDau");
                result[2] = rs.getInt("slDaBan");
                result[3] = rs.getInt("slXuatTra");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
    public List<LoThuoc> getTatCaLoThuocTraNCC(String maThuoc) {
        List<LoThuoc> list = new ArrayList<>();
        // Đã bỏ điều kiện viTriKho = ?
        String sql = "SELECT * FROM LoThuoc WHERE maThuoc = ? AND soLuongTon > 0 AND trangThai = 1 ORDER BY hanSuDung ASC";
        
        try (Connection con = connectDB.ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            
            pst.setString(1, maThuoc);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                LoThuoc lo = new LoThuoc();
                lo.setMaLoThuoc(rs.getString("maLoThuoc"));
                lo.setSoLuongTon(rs.getInt("soLuongTon"));
                lo.setHanSuDung(rs.getDate("hanSuDung"));
                lo.setNgaySanXuat(rs.getDate("ngaySanXuat"));
                lo.setViTriKho(rs.getString("viTriKho"));
                lo.setGiaNhap(rs.getDouble("giaNhap")); 
                lo.setTrangThai(rs.getInt("trangThai"));
                
                String maNCC = rs.getString("maNhaCungCap");
                if (maNCC != null) {
                    NhaCungCap ncc = new NhaCungCap();
                    ncc.setMaNhaCungCap(maNCC);
                    lo.setNhaCungCap(ncc);
                }
                list.add(lo);
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        return list;
    }
}