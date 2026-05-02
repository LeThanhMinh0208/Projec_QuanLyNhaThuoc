package entity;

public class Thuoc {
    private String maThuoc;
    private String maDanhMuc;
    private String tenThuoc;
    private String hoatChat;
    private String hamLuong;
    private String hangSanXuat;
    private String nuocSanXuat;
    private String congDung;
    private String donViCoBan;
    private String hinhAnh;
    private boolean canKeDon;
    private String trangThai;
    private String trieuChung;
    private String tenDanhMuc;


    public Thuoc() {
    }

    public Thuoc(String maThuoc, String maDanhMuc, String tenThuoc, String hoatChat, String hamLuong,
                 String hangSanXuat, String nuocSanXuat, String congDung, String donViCoBan,
                 String hinhAnh, boolean canKeDon, String trangThai) {
        this.maThuoc = maThuoc;
        this.maDanhMuc = maDanhMuc;
        this.tenThuoc = tenThuoc;
        this.hoatChat = hoatChat;
        this.hamLuong = hamLuong;
        this.hangSanXuat = hangSanXuat;
        this.nuocSanXuat = nuocSanXuat;
        this.congDung = congDung;
        this.donViCoBan = donViCoBan;
        this.hinhAnh = hinhAnh;
        this.canKeDon = canKeDon;
        this.trangThai = trangThai;

    }

    // --- GETTERS & SETTERS ---
    public String getMaThuoc() { return maThuoc; }
    public void setMaThuoc(String maThuoc) { this.maThuoc = maThuoc; }

    public String getMaDanhMuc() { return maDanhMuc; }
    public void setMaDanhMuc(String maDanhMuc) { this.maDanhMuc = maDanhMuc; }

    public String getTenThuoc() { return tenThuoc; }
    public void setTenThuoc(String tenThuoc) { this.tenThuoc = tenThuoc; }

    public String getHoatChat() { return hoatChat; }
    public void setHoatChat(String hoatChat) { this.hoatChat = hoatChat; }

    public String getHamLuong() { return hamLuong; }
    public void setHamLuong(String hamLuong) { this.hamLuong = hamLuong; }

    public String getHangSanXuat() { return hangSanXuat; }
    public void setHangSanXuat(String hangSanXuat) { this.hangSanXuat = hangSanXuat; }

    public String getNuocSanXuat() { return nuocSanXuat; }
    public void setNuocSanXuat(String nuocSanXuat) { this.nuocSanXuat = nuocSanXuat; }

    public String getCongDung() { return congDung; }
    public void setCongDung(String congDung) { this.congDung = congDung; }

    public String getDonViCoBan() { return donViCoBan; }
    public void setDonViCoBan(String donViCoBan) { this.donViCoBan = donViCoBan; }

    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }

    public boolean isCanKeDon() { return canKeDon; }
    public void setCanKeDon(boolean canKeDon) { this.canKeDon = canKeDon; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getTrieuChung() { return trieuChung; }
    public void setTrieuChung(String trieuChung) { this.trieuChung = trieuChung; }

    public String getTenDanhMuc() { return tenDanhMuc; }
    public void setTenDanhMuc(String tenDanhMuc) { this.tenDanhMuc = tenDanhMuc; }
    @Override
    public String toString() {
        return this.tenThuoc == null ? "" : this.tenThuoc;
    }
}