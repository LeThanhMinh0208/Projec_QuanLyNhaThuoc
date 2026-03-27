package entity;

public class DonViQuyDoi {
    private String maQuyDoi;
    private String maThuoc;
    private String tenDonVi;
    private int    tyLeQuyDoi;

    public DonViQuyDoi() {}

    public DonViQuyDoi(String maQuyDoi, String maThuoc, String tenDonVi, int tyLeQuyDoi) {
        this.maQuyDoi    = maQuyDoi;
        this.maThuoc     = maThuoc;
        this.tenDonVi    = tenDonVi;
        this.tyLeQuyDoi  = tyLeQuyDoi;
    }

    public String getMaQuyDoi()  { return maQuyDoi; }
    public void   setMaQuyDoi(String v) { this.maQuyDoi = v; }

    public String getMaThuoc()   { return maThuoc; }
    public void   setMaThuoc(String v)  { this.maThuoc = v; }

    public String getTenDonVi()  { return tenDonVi; }
    public void   setTenDonVi(String v) { this.tenDonVi = v; }

    public int    getTyLeQuyDoi()       { return tyLeQuyDoi; }
    public void   setTyLeQuyDoi(int v)  { this.tyLeQuyDoi = v; }

    @Override
    public String toString() {
        return tenDonVi == null ? "" : tenDonVi + " (x" + tyLeQuyDoi + ")";
    }
}