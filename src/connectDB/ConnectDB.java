package connectDB;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    private static ConnectDB instance = new ConnectDB();
    private static Connection con;
    
    public static ConnectDB getInstance() {
        return instance;
    }
    
    public void connect() {
    	 try {
             if (con == null || con.isClosed()) {

                 Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); // THÊM DÒNG NÀY

                 String url = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyNhaThuoc_LongNguyen;encrypt=true;trustServerCertificate=true;";
                 String user = "sa";
                 String password = "sapassword";
                 con = DriverManager.getConnection(url, user, password);
             }
            System.out.println("Kết nối CSDL thành công!");
            
        } catch (SQLException e) {
            System.out.println("Lỗi kết nối CSDL: " + e.getMessage());
        } catch (ClassNotFoundException e) { // THÊM CATCH NÀY
            System.out.println("Không tìm thấy driver: " + e.getMessage());
        }
    }
    
    public void disconnect() {
        if (con != null) {
            try { con.close(); } 
            catch (SQLException e) { e.printStackTrace(); }
        }
    }
    
    public static Connection getConnection() {
        try {
            if (con == null || con.isClosed()) {

                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); // THÊM DÒNG NÀY

                String url = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyNhaThuoc_LongNguyen;encrypt=true;trustServerCertificate=true;";
                String user = "sa";
                String password = "sapassword";
                con = DriverManager.getConnection(url, user, password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) { // THÊM CATCH NÀY
            e.printStackTrace();
        }
        return con;
    }
}