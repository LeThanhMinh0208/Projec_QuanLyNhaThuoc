package entity;
import java.sql.Date;

public class PhieuXuat {
    private String maPhieuXuat;
    private Date ngayXuat;
    private NhanVien nhanVienLap;
    private String loaiXuat;
    private String khoNhan;
    private NhaCungCap nhaCungCap;
    private String lyDoHuy;
    private String ghiChu;

    public PhieuXuat() {}

    // HÀM ẢO: Tự động đổi thông tin cột đặc thù theo loại phiếu để hiển thị lên bảng
    public String getThongTinDacThu() {
        if ("CHUYEN_KHO".equals(loaiXuat)) return khoNhan;
        if ("TRA_NCC".equals(loaiXuat)) return nhaCungCap != null ? nhaCungCap.getTenNhaCungCap() : "";
        if ("XUAT_HUY".equals(loaiXuat)) return lyDoHuy;
        return "";
    }

    // --- GETTER & SETTER ---
    public String getMaPhieuXuat() { return maPhieuXuat; }
    public void setMaPhieuXuat(String maPhieuXuat) { this.maPhieuXuat = maPhieuXuat; }
    public Date getNgayXuat() { return ngayXuat; }
    public void setNgayXuat(Date ngayXuat) { this.ngayXuat = ngayXuat; }
    public NhanVien getNhanVienLap() { return nhanVienLap; }
    public void setNhanVienLap(NhanVien nhanVienLap) { this.nhanVienLap = nhanVienLap; }
    public String getLoaiXuat() { return loaiXuat; }
    public void setLoaiXuat(String loaiXuat) { this.loaiXuat = loaiXuat; }
    public String getKhoNhan() { return khoNhan; }
    public void setKhoNhan(String khoNhan) { this.khoNhan = khoNhan; }
    public NhaCungCap getNhaCungCap() { return nhaCungCap; }
    public void setNhaCungCap(NhaCungCap nhaCungCap) { this.nhaCungCap = nhaCungCap; }
    public String getLyDoHuy() { return lyDoHuy; }
    public void setLyDoHuy(String lyDoHuy) { this.lyDoHuy = lyDoHuy; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}