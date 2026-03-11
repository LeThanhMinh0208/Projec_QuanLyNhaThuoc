package entity;

import java.util.Objects;

public class NhaCungCap {

    private String maNhaCungCap;
    private String tenNhaCungCap;
    private String sdt;
    private String diaChi;
    private double congNo;

    // Constructor mặc định
    public NhaCungCap() {
        this.congNo = 0;
    }

    // Constructor đầy đủ
    public NhaCungCap(String maNhaCungCap, String tenNhaCungCap, String sdt, String diaChi, double congNo) {
        setMaNhaCungCap(maNhaCungCap);
        setTenNhaCungCap(tenNhaCungCap);
        setSdt(sdt);
        setDiaChi(diaChi);
        setCongNo(congNo);
    }

    // Copy constructor
    public NhaCungCap(NhaCungCap nccKhac) {
        this.maNhaCungCap = nccKhac.maNhaCungCap;
        this.tenNhaCungCap = nccKhac.tenNhaCungCap;
        this.sdt = nccKhac.sdt;
        this.diaChi = nccKhac.diaChi;
        this.congNo = nccKhac.congNo;
    }

    public String getMaNCC() {
        return maNhaCungCap;
    }

    public void setMaNhaCungCap(String maNCC) {
        if (maNCC == null || maNCC.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhà cung cấp không được rỗng");
        }
        this.maNhaCungCap = maNCC;
    }

    public String getTenNCC() {
        return tenNhaCungCap;
    }

    public void setTenNhaCungCap(String tenNCC) {
        if (tenNCC == null || tenNCC.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên nhà cung cấp không được để trống");
        }
        this.tenNhaCungCap = tenNCC;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String soDienThoai) {
        if (soDienThoai == null || !soDienThoai.matches("^0\\d{9}$")) {
            throw new IllegalArgumentException("Số điện thoại không đúng định dạng");
        }
        this.sdt = soDienThoai;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String dc) {
        if (dc == null || dc.trim().isEmpty()) {
            throw new IllegalArgumentException("Địa chỉ không được rỗng");
        }
        this.diaChi = dc;
    }

    public double getCongNo() {
        return congNo;
    }

    public void setCongNo(double no) {
        if (no < 0) {
            throw new IllegalArgumentException("Công nợ không được âm");
        }
        this.congNo = no;
    }

    @Override
    public String toString() {
        return "NhaCungCap{" +
                "maNhaCungCap='" + maNhaCungCap + '\'' +
                ", tenNhaCungCap='" + tenNhaCungCap + '\'' +
                ", sdt='" + sdt + '\'' +
                ", diaChi='" + diaChi + '\'' +
                ", congNo=" + congNo +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(maNhaCungCap);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NhaCungCap)) return false;
        NhaCungCap other = (NhaCungCap) obj;
        return Objects.equals(maNhaCungCap, other.maNhaCungCap);
    }
}
