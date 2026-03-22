package entity;

public class DonThuoc {
<<<<<<< HEAD
    private String maDonThuoc;
    private String maHoaDon;        // THÊM TRƯỜNG NÀY
=======
	private String maDonThuoc;
    private String maHoaDon;
>>>>>>> main
    private String tenBacSi;
    private String chanDoan;
    private String hinhAnhDon;
    private String thongTinBenhNhan;
<<<<<<< HEAD

    public DonThuoc() {}

    // Constructor 6 tham số
    public DonThuoc(String maDonThuoc, String maHoaDon, String tenBacSi,
                    String chanDoan, String hinhAnhDon, String thongTinBenhNhan) {
        this.maDonThuoc = maDonThuoc;
        this.maHoaDon = maHoaDon;
        this.tenBacSi = tenBacSi;
        this.chanDoan = chanDoan;
        this.hinhAnhDon = hinhAnhDon;
        this.thongTinBenhNhan = thongTinBenhNhan;
    }

    public DonThuoc(DonThuoc dtKhac) {
        this(dtKhac.maDonThuoc, dtKhac.maHoaDon, dtKhac.tenBacSi,
             dtKhac.chanDoan, dtKhac.hinhAnhDon, dtKhac.thongTinBenhNhan);
    }

    // Setters
    public void setMaDonThuoc(String maDT) { this.maDonThuoc = maDT; }

    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }

    public void setTenBacSi(String tenBacSi) {
        if (tenBacSi == null || tenBacSi.isBlank())
            throw new IllegalArgumentException("Tên bác sĩ không được để trống");
        this.tenBacSi = tenBacSi;
    }

    public void setChanDoan(String chanDoan) { this.chanDoan = chanDoan; }

    public void setHinhAnhDon(String url) { this.hinhAnhDon = url; }

    public void setThongTinBenhNhan(String info) {
        if (info == null || info.isBlank())
            throw new IllegalArgumentException("Thông tin bệnh nhân không được để trống");
        this.thongTinBenhNhan = info;
    }

    // Getters
    public String getMaDonThuoc()       { return maDonThuoc; }
    public String getMaHoaDon()         { return maHoaDon; }
    public String getTenBacSi()         { return tenBacSi; }
    public String getChanDoan()         { return chanDoan; }
    public String getHinhAnhDon()       { return hinhAnhDon; }
    public String getThongTinBenhNhan() { return thongTinBenhNhan; }

    @Override
    public String toString() {
        return "DonThuoc{ma='" + maDonThuoc + "', bacSi='" + tenBacSi + "'}";
    }
}
=======
    
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
>>>>>>> main
