package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import connectDB.ConnectDB;
import entity.NhanVien;

public class DAO_NhanVien {

    public NhanVien dangNhap(String taiKhoan, String matKhau) {
        String sql = "SELECT * FROM NhanVien WHERE tenDangNhap = ? AND matKhau = ? AND trangThai != 0";
        ConnectDB.getInstance();
		try (Connection con = ConnectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, taiKhoan);
            pst.setString(2, matKhau);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                if (rs.getString("tenDangNhap").equals(taiKhoan) && rs.getString("matKhau").equals(matKhau)) {
                    return new NhanVien(
                        rs.getString("maNhanVien"), rs.getString("tenDangNhap"), rs.getString("matKhau"),
                        rs.getString("hoTen"), rs.getString("chucVu"), rs.getString("caLamViec"),
                        rs.getString("sdt"), rs.getInt("trangThai")
                    );
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public List<NhanVien> getChiNhanVien() {
        List<NhanVien> list = new ArrayList<>();
        String sql = "SELECT * FROM NhanVien WHERE chucVu = N'Nhân Viên' AND trangThai IN (1, 2)";
        ConnectDB.getInstance();
		try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new NhanVien(
                    rs.getString("maNhanVien"), rs.getString("tenDangNhap"), rs.getString("matKhau"),
                    rs.getString("hoTen"), rs.getString("chucVu"), rs.getString("caLamViec"),
                    rs.getString("sdt"), rs.getInt("trangThai")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 🚨 1. KIỂM TRA SĐT KHI THÊM MỚI (Chỉ kiểm tra những người đang hoạt động/khóa)
    public boolean kiemTraSdtTonTai(String sdt) {
        String sql = "SELECT 1 FROM NhanVien WHERE sdt = ? AND trangThai != 0";
        ConnectDB.getInstance();
		try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sdt);
            ResultSet rs = ps.executeQuery();
            return rs.next(); // Trả về true nếu SĐT đã có người xài
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // 🚨 2. KIỂM TRA SĐT KHI CẬP NHẬT (Trừ chính mình ra)
    public boolean kiemTraSdtTonTaiKhiCapNhat(String sdt, String maNVHienTai) {
        String sql = "SELECT 1 FROM NhanVien WHERE sdt = ? AND maNhanVien != ? AND trangThai != 0";
        ConnectDB.getInstance();
		try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sdt);
            ps.setString(2, maNVHienTai);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public String kiemTraNhanVienDaXoa(String hoTen, String sdt) {
        String sql = "SELECT maNhanVien FROM NhanVien WHERE hoTen = ? AND sdt = ? AND trangThai = 0";
        ConnectDB.getInstance();
		try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hoTen);
            ps.setString(2, sdt);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
				return rs.getString("maNhanVien");
			}
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public boolean kiemTraTaiKhoanTonTai(String username) {
        String sql = "SELECT 1 FROM NhanVien WHERE tenDangNhap = ?";
        ConnectDB.getInstance();
		try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean khoiPhucNhanVien(String maNV, String taiKhoanMoi) {
        String sql = "UPDATE NhanVien SET tenDangNhap = ?, matKhau = '123456', trangThai = 1 WHERE maNhanVien = ?";
        ConnectDB.getInstance();
		try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, taiKhoanMoi);
            ps.setString(2, maNV);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean themNhanVien(NhanVien nv) {
        String sql = "INSERT INTO NhanVien (maNhanVien, tenDangNhap, matKhau, hoTen, chucVu, caLamViec, sdt, trangThai) VALUES (?, ?, ?, ?, ?, ?, ?, 1)";
        ConnectDB.getInstance();
		try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nv.getMaNhanVien());
            ps.setString(2, nv.getTenDangNhap());
            ps.setString(3, nv.getMatKhau());
            ps.setString(4, nv.getHoTen());
            ps.setString(5, nv.getChucVu());
            ps.setString(6, nv.getCaLamViec());
            ps.setString(7, nv.getSdt());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean capNhatThongTin(NhanVien nv, boolean isResetPass) {
        String sql = isResetPass ?
            "UPDATE NhanVien SET hoTen = ?, sdt = ?, matKhau = '123456' WHERE maNhanVien = ?" :
            "UPDATE NhanVien SET hoTen = ?, sdt = ? WHERE maNhanVien = ?";
        ConnectDB.getInstance();
		try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nv.getHoTen());
            ps.setString(2, nv.getSdt());
            ps.setString(3, nv.getMaNhanVien());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean xoaMemNhanVien(String maNV) {
        String sql = "UPDATE NhanVien SET trangThai = 0, tenDangNhap = maNhanVien + '_del' WHERE maNhanVien = ?";
        ConnectDB.getInstance();
		try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean capNhatTrangThai(String maNV, int trangThaiMoi) {
        String sql = "UPDATE NhanVien SET trangThai = ? WHERE maNhanVien = ?";
        ConnectDB.getInstance();
		try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, trangThaiMoi);
            ps.setString(2, maNV);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public String phatSinhMaMoi() {
        String sql = "SELECT TOP 1 maNhanVien FROM NhanVien ORDER BY maNhanVien DESC";
        ConnectDB.getInstance();
		try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String maCu = rs.getString(1);
                int so = Integer.parseInt(maCu.substring(2)) + 1;
                return String.format("NV%03d", so);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "NV001";
    }
    public boolean doiMatKhau(String maNV, String matKhauMoi) {
        String sql = "UPDATE NhanVien SET matKhau = ? WHERE maNhanVien = ?";
        ConnectDB.getInstance();
		try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, matKhauMoi);
            ps.setString(2, maNV);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}