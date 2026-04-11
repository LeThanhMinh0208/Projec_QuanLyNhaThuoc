import java.sql.*;
public class CheckPDT {
  public static void main(String[] args) throws Exception {
    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    String url = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyNhaThuoc_LongNguyen;encrypt=true;trustServerCertificate=true;";
    try (Connection con = DriverManager.getConnection(url, "sa", "sapassword")) {
      try (PreparedStatement pst = con.prepareStatement(
        "SELECT pdt.maPhieuDoiTra, pdt.ngayDoiTra, pdt.lyDo, pdt.hinhThucXuLy, pdt.phiPhat, dt.maQuyDoi, dt.maLoThuoc, dt.soLuong, dt.tinhTrang " +
        "FROM PhieuDoiTra pdt JOIN ChiTietDoiTra dt ON dt.maPhieuDoiTra = pdt.maPhieuDoiTra WHERE pdt.maHoaDon = ?")) {
        pst.setString(1, "HD0016");
        try (ResultSet rs = pst.executeQuery()) {
          while (rs.next()) {
            System.out.println(rs.getString(1)+" | "+rs.getTimestamp(2)+" | "+rs.getString(3)+" | "+rs.getString(4)+" | "+rs.getDouble(5)+" | "+rs.getString(6)+" | "+rs.getString(7)+" | "+rs.getInt(8)+" | "+rs.getString(9));
          }
        }
      }
    }
  }
}
