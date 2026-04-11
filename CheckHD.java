import java.sql.*;
public class CheckHD {
  public static void main(String[] args) throws Exception {
    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    String url = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyNhaThuoc_LongNguyen;encrypt=true;trustServerCertificate=true;";
    try (Connection con = DriverManager.getConnection(url, "sa", "sapassword")) {
      String ma = "HD0016";
      try (PreparedStatement pst = con.prepareStatement("SELECT maHoaDon, ngayLap, maKhachHang, maNhanVien, hinhThucThanhToan FROM HoaDon WHERE maHoaDon = ?")) {
        pst.setString(1, ma);
        try (ResultSet rs = pst.executeQuery()) {
          System.out.println("HOADON:");
          while (rs.next()) {
            System.out.println(rs.getString(1)+" | "+rs.getTimestamp(2)+" | "+rs.getString(3)+" | "+rs.getString(4)+" | "+rs.getString(5));
          }
        }
      }
      try (PreparedStatement pst = con.prepareStatement("SELECT maHoaDon, maBangGia, maQuyDoi, maLoThuoc, soLuong, donGia FROM ChiTietHoaDon WHERE maHoaDon = ?")) {
        pst.setString(1, ma);
        try (ResultSet rs = pst.executeQuery()) {
          System.out.println("CHITIET:");
          int c=0;
          while (rs.next()) {
            c++;
            System.out.println(rs.getString(1)+" | "+rs.getString(2)+" | "+rs.getString(3)+" | "+rs.getString(4)+" | "+rs.getInt(5)+" | "+rs.getDouble(6));
          }
          System.out.println("COUNT="+c);
        }
      }
      try (PreparedStatement pst = con.prepareStatement(
        "SELECT ct.maQuyDoi, ct.maLoThuoc, t.tenThuoc, dv.tenDonVi, lo.hanSuDung, SUM(ct.soLuong) AS soLuongDaMua, " +
        "ISNULL((SELECT SUM(dt.soLuong) FROM ChiTietDoiTra dt JOIN PhieuDoiTra pdt ON pdt.maPhieuDoiTra = dt.maPhieuDoiTra WHERE pdt.maHoaDon = ct.maHoaDon AND dt.maQuyDoi = ct.maQuyDoi AND dt.maLoThuoc = ct.maLoThuoc), 0) AS soLuongDaTra, MAX(ct.donGia) AS donGia " +
        "FROM ChiTietHoaDon ct JOIN DonViQuyDoi dv ON dv.maQuyDoi = ct.maQuyDoi JOIN Thuoc t ON t.maThuoc = dv.maThuoc JOIN LoThuoc lo ON lo.maLoThuoc = ct.maLoThuoc WHERE ct.maHoaDon = ? GROUP BY ct.maHoaDon, ct.maQuyDoi, ct.maLoThuoc, t.tenThuoc, dv.tenDonVi, lo.hanSuDung ORDER BY t.tenThuoc, lo.hanSuDung")) {
        pst.setString(1, ma);
        try (ResultSet rs = pst.executeQuery()) {
          System.out.println("QUERYDOITRA:");
          int c=0;
          while (rs.next()) {
            c++;
            System.out.println(rs.getString("tenThuoc")+" | "+rs.getString("tenDonVi")+" | "+rs.getString("maLoThuoc")+" | mua="+rs.getInt("soLuongDaMua")+" | tra="+rs.getInt("soLuongDaTra"));
          }
          System.out.println("COUNT="+c);
        }
      }
    }
  }
}
