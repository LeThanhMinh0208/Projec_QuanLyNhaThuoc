package entity;

public class DonThuoc {
	private String maDonThuoc;
    private String maHoaDon;
    private String tenBacSi;
    private String chanDoan;
    private String hinhAnhDon;
    private String thongTinBenhNhan;
    
	public DonThuoc() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DonThuoc(String maDonThuoc, String maHoaDon, String tenBacSi, String chanDoan, String hinhAnhDon,
			String thongTinBenhNhan) {
		super();
		this.maDonThuoc = maDonThuoc;
		this.maHoaDon = maHoaDon;
		this.tenBacSi = tenBacSi;
		this.chanDoan = chanDoan;
		this.hinhAnhDon = hinhAnhDon;
		this.thongTinBenhNhan = thongTinBenhNhan;
	}
	
	public String getMaDonThuoc() {
		return maDonThuoc;
	}
	public void setMaDonThuoc(String maDonThuoc) {
		this.maDonThuoc = maDonThuoc;
	}
	public String getMaHoaDon() {
		return maHoaDon;
	}
	public void setMaHoaDon(String maHoaDon) {
		this.maHoaDon = maHoaDon;
	}
	public String getTenBacSi() {
		return tenBacSi;
	}
	public void setTenBacSi(String tenBacSi) {
		this.tenBacSi = tenBacSi;
	}
	public String getChanDoan() {
		return chanDoan;
	}
	public void setChanDoan(String chanDoan) {
		this.chanDoan = chanDoan;
	}
	public String getHinhAnhDon() {
		return hinhAnhDon;
	}
	public void setHinhAnhDon(String hinhAnhDon) {
		this.hinhAnhDon = hinhAnhDon;
	}
	public String getThongTinBenhNhan() {
		return thongTinBenhNhan;
	}
	public void setThongTinBenhNhan(String thongTinBenhNhan) {
		this.thongTinBenhNhan = thongTinBenhNhan;
	}
    
}
