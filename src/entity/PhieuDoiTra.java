package entity;

import java.util.Date;

public class PhieuDoiTra {
	private String maPhieuDoiTra;
    private String maHoaDon;
    private String maNhanVien;
    private String maKhachHang;
    private Date ngayDoiTra;
    private String lyDo;
    private String hinhThucXuLy; // HinhThucDoiTra: HOAN_TIEN | DOI_SAN_PHAM
    private double phiPhat;
    
	public PhieuDoiTra() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public PhieuDoiTra(String maPhieuDoiTra, String maHoaDon, String maNhanVien, String maKhachHang, Date ngayDoiTra,
			String lyDo, String hinhThucXuLy, double phiPhat) {
		super();
		this.maPhieuDoiTra = maPhieuDoiTra;
		this.maHoaDon = maHoaDon;
		this.maNhanVien = maNhanVien;
		this.maKhachHang = maKhachHang;
		this.ngayDoiTra = ngayDoiTra;
		this.lyDo = lyDo;
		this.hinhThucXuLy = hinhThucXuLy;
		this.phiPhat = phiPhat;
	}
	
	public String getMaPhieuDoiTra() {
		return maPhieuDoiTra;
	}
	public void setMaPhieuDoiTra(String maPhieuDoiTra) {
		this.maPhieuDoiTra = maPhieuDoiTra;
	}
	public String getMaHoaDon() {
		return maHoaDon;
	}
	public void setMaHoaDon(String maHoaDon) {
		this.maHoaDon = maHoaDon;
	}
	public String getMaNhanVien() {
		return maNhanVien;
	}
	public void setMaNhanVien(String maNhanVien) {
		this.maNhanVien = maNhanVien;
	}
	public String getMaKhachHang() {
		return maKhachHang;
	}
	public void setMaKhachHang(String maKhachHang) {
		this.maKhachHang = maKhachHang;
	}
	public Date getNgayDoiTra() {
		return ngayDoiTra;
	}
	public void setNgayDoiTra(Date ngayDoiTra) {
		this.ngayDoiTra = ngayDoiTra;
	}
	public String getLyDo() {
		return lyDo;
	}
	public void setLyDo(String lyDo) {
		this.lyDo = lyDo;
	}
	public String getHinhThucXuLy() {
		return hinhThucXuLy;
	}
	public void setHinhThucXuLy(String hinhThucXuLy) {
		this.hinhThucXuLy = hinhThucXuLy;
	}
	public double getPhiPhat() {
		return phiPhat;
	}
	public void setPhiPhat(double phiPhat) {
		this.phiPhat = phiPhat;
	}
    
    
}
