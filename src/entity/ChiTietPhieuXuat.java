package entity;

public class ChiTietPhieuXuat {
    private PhieuXuat phieuXuat;
    private LoThuoc loThuoc;
    private DonViQuyDoi donViQuyDoi;
    private int soLuongXuat;

    public ChiTietPhieuXuat() {}

    // --- GETTER & SETTER ---
    public PhieuXuat getPhieuXuat() { return phieuXuat; }
    public void setPhieuXuat(PhieuXuat phieuXuat) { this.phieuXuat = phieuXuat; }
    public LoThuoc getLoThuoc() { return loThuoc; }
    public void setLoThuoc(LoThuoc loThuoc) { this.loThuoc = loThuoc; }
    public DonViQuyDoi getDonViQuyDoi() { return donViQuyDoi; }
    public void setDonViQuyDoi(DonViQuyDoi donViQuyDoi) { this.donViQuyDoi = donViQuyDoi; }
    public int getSoLuongXuat() { return soLuongXuat; }
    public void setSoLuongXuat(int soLuongXuat) { this.soLuongXuat = soLuongXuat; }
}