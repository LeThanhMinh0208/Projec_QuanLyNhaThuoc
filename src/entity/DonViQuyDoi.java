package entity;

public class DonViQuyDoi {
    private String maQuyDoi;
    private Thuoc thuoc; 
    private String tenDonVi;
    private int tyLeQuyDoi;
    private double giaBan;
    
    public DonViQuyDoi() {
        super();
    }
    
    public DonViQuyDoi(String maQuyDoi, Thuoc thuoc, String tenDonVi, int tyLeQuyDoi, double giaBan) {
        super();
        this.maQuyDoi = maQuyDoi;
        this.thuoc = thuoc;
        this.tenDonVi = tenDonVi;
        this.tyLeQuyDoi = tyLeQuyDoi;
        this.giaBan = giaBan;
    }
    
    public String getMaQuyDoi() {
        return maQuyDoi;
    }
    public void setMaQuyDoi(String maQuyDoi) {
        this.maQuyDoi = maQuyDoi;
    }

    public Thuoc getThuoc() {
        return thuoc;
    }
    public void setThuoc(Thuoc thuoc) {
        this.thuoc = thuoc;
    }

    public String getTenDonVi() {
        return tenDonVi;
    }
    public void setTenDonVi(String tenDonVi) {
        this.tenDonVi = tenDonVi;
    }

    public int getTyLeQuyDoi() {
        return tyLeQuyDoi;
    }
    public void setTyLeQuyDoi(int tyLeQuyDoi) {
        this.tyLeQuyDoi = tyLeQuyDoi;
    }

    public double getGiaBan() {
        return giaBan;
    }
    public void setGiaBan(double giaBan) {
        this.giaBan = giaBan;
    }

    // Hàm này giúp hiển thị chữ "Hộp", "Vỉ", "Viên" thẳng lên ComboBox
    @Override
    public String toString() {
        return tenDonVi; 
    }
}