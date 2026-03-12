package entity;

public class DonViQuyDoi {
	private String maQuyDoi;
    private String maThuoc;
    private String tenDonVi;
    private int tyLeQuyDoi;
    private double giaBan;
    
	public DonViQuyDoi() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public DonViQuyDoi(String maQuyDoi, String maThuoc, String tenDonVi, int tyLeQuyDoi, double giaBan) {
		super();
		this.maQuyDoi = maQuyDoi;
		this.maThuoc = maThuoc;
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
	public String getMaThuoc() {
		return maThuoc;
	}
	public void setMaThuoc(String maThuoc) {
		this.maThuoc = maThuoc;
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
    
}
