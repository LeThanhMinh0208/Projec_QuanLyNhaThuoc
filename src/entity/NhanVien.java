package entity;

import java.util.Objects;

public class NhanVien {

    private String maNhanVien;
    private String tenDangNhap;
    private String matKhau;
    private String hoTen;
    private String chucVu;
    private String caLamViec;
    private String sdt;

    // Constructor mặc định
    public NhanVien() {
    }

    // Constructor đầy đủ
    public NhanVien(String maNhanVien, String tenDangNhap, String matKhau,
                    String hoTen, String chucVu, String caLamViec, String sdt) {

        setMaNhanVien(maNhanVien);
        setTenDangNhap(tenDangNhap);
        setMatKhau(matKhau);
        setHoTen(hoTen);
        setChucVu(chucVu);
        setCaLamViec(caLamViec);
        setSdt(sdt);
    }

    // Copy constructor
    public NhanVien(NhanVien nvKhac) {
        this.maNhanVien = nvKhac.maNhanVien;
        this.tenDangNhap = nvKhac.tenDangNhap;
        this.matKhau = nvKhac.matKhau;
        this.hoTen = nvKhac.hoTen;
        this.chucVu = nvKhac.chucVu;
        this.caLamViec = nvKhac.caLamViec;
        this.sdt = nvKhac.sdt;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNV) {
        if (maNV == null || !maNV.matches("^NV\\d{3}$")) {
            throw new IllegalArgumentException("Mã nhân viên phải theo dạng NV001");
        }
        this.maNhanVien = maNV;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String TDN) {
        if (TDN == null || TDN.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống");
        }
        this.tenDangNhap = TDN;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String MK) {
        if (MK == null || MK.trim().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }
        this.matKhau = MK;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String HT) {
        if (HT == null || HT.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên không được để trống");
        }
        this.hoTen = HT;
    }

    public String getChucVu() {
        return chucVu;
    }

    public void setChucVu(String CV) {

        String[] danhSach = {"Quản lý", "Nhân viên bán thuốc", "Thu ngân"};

        boolean hopLe = false;

        for (String cv : danhSach) {
            if (cv.equalsIgnoreCase(CV)) {
                hopLe = true;
                break;
            }
        }

        if (!hopLe) {
            throw new IllegalArgumentException("Chức vụ không hợp lệ");
        }

        this.chucVu = CV;
    }

    public String getCaLamViec() {
        return caLamViec;
    }

    public void setCaLamViec(String CA) {

        if (CA == null || CA.trim().isEmpty()) {
            this.caLamViec = "";
            return;
        }

        String[] ca = {"Sáng", "Chiều", "Tối"};

        boolean hopLe = false;

        for (String c : ca) {
            if (c.equalsIgnoreCase(CA)) {
                hopLe = true;
                break;
            }
        }

        if (!hopLe) {
            throw new IllegalArgumentException("Ca làm việc không hợp lệ");
        }

        this.caLamViec = CA;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String SDT) {
        if (SDT == null || !SDT.matches("^0\\d{9}$")) {
            throw new IllegalArgumentException("Số điện thoại không đúng định dạng");
        }
        this.sdt = SDT;
    }

    // Phương thức đăng nhập
    public boolean dangNhap(String matKhauNhap) {
        return this.matKhau.equals(matKhauNhap);
    }

    // Phương thức đăng xuất
    public void dangXuat() {
        System.out.println("Nhân viên đã đăng xuất");
    }

    @Override
    public String toString() {
        return "NhanVien{" +
                "maNhanVien='" + maNhanVien + '\'' +
                ", tenDangNhap='" + tenDangNhap + '\'' +
                ", hoTen='" + hoTen + '\'' +
                ", chucVu='" + chucVu + '\'' +
                ", caLamViec='" + caLamViec + '\'' +
                ", sdt='" + sdt + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(maNhanVien);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NhanVien)) return false;
        NhanVien other = (NhanVien) obj;
        return Objects.equals(maNhanVien, other.maNhanVien);
    }
}
