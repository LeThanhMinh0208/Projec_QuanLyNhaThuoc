package entity;

public class DonThuoc {
    private String maDonThuoc;
    private String maHoaDon;
    private String tenBacSi;
    private String chanDoan;
    private String hinhAnhDon;
    private String thongTinBenhNhan;

    public DonThuoc() {}

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

    public void setMaDonThuoc(String maDT) { this.maDonThuoc = maDT; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }
    public void setTenBacSi(String tenBacSi) { this.tenBacSi = tenBacSi; }
    public void setChanDoan(String chanDoan) { this.chanDoan = chanDoan; }
    public void setHinhAnhDon(String url) { this.hinhAnhDon = url; }
    public void setThongTinBenhNhan(String info) { this.thongTinBenhNhan = info; }

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