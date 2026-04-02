package entity;

public class ChiTietPhieuChi {
    private PhieuChi phieuChi;   // Khóa ngoại kép
    private PhieuNhap phieuNhap; // Khóa ngoại kép
    private double soTienTra;

    // Constructor mặc định
    public ChiTietPhieuChi() {
    }

    // Constructor đầy đủ
    public ChiTietPhieuChi(PhieuChi phieuChi, PhieuNhap phieuNhap, double soTienTra) {
        this.phieuChi = phieuChi;
        this.phieuNhap = phieuNhap;
        this.soTienTra = soTienTra;
    }

    // Getters and Setters
    public PhieuChi getPhieuChi() {
        return phieuChi;
    }

    public void setPhieuChi(PhieuChi phieuChi) {
        this.phieuChi = phieuChi;
    }

    public PhieuNhap getPhieuNhap() {
        return phieuNhap;
    }

    public void setPhieuNhap(PhieuNhap phieuNhap) {
        this.phieuNhap = phieuNhap;
    }

    public double getSoTienTra() {
        return soTienTra;
    }

    public void setSoTienTra(double soTienTra) {
        this.soTienTra = soTienTra;
    }
}