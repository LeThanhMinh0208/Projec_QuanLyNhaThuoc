package connectDB;
<<<<<<< HEAD
=======

>>>>>>> main
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    private static ConnectDB instance = new ConnectDB();
    private static Connection con;
<<<<<<< HEAD
    
    public static ConnectDB getInstance() {
        return instance;
    }
    
    public void connect() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); // THÊM DÒNG NÀY
            String url = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyNhaThuoc_LongNguyen;encrypt=true;trustServerCertificate=true;";
            String user = "sa";
            String password = "sapassword"; 
=======

    public static ConnectDB getInstance() {
        return instance;
    }

    public void connect() {
        try {
            // Thay đổi cấu hình này cho khớp với SQL Server máy bạn
            String url = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyNhaThuoc_LongNguyen;encrypt=true;trustServerCertificate=true;";
            String user = "sa";
            String password = "123456"; 
            
>>>>>>> main
            con = DriverManager.getConnection(url, user, password);
            System.out.println("Kết nối CSDL thành công!");
        } catch (SQLException e) {
            System.out.println("Lỗi kết nối CSDL: " + e.getMessage());
<<<<<<< HEAD
        } catch (ClassNotFoundException e) { // THÊM CATCH NÀY
            System.out.println("Không tìm thấy driver: " + e.getMessage());
        }
    }
    
=======
        }
    }

>>>>>>> main
    public void disconnect() {
        if (con != null) {
            try { con.close(); } 
            catch (SQLException e) { e.printStackTrace(); }
        }
    }
<<<<<<< HEAD
    
    public static Connection getConnection() {
        try {
            if (con == null || con.isClosed()) {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); // THÊM DÒNG NÀY
                String url = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyNhaThuoc_LongNguyen;encrypt=true;trustServerCertificate=true;";
                String user = "sa";
                String password = "sapassword";
=======

    public static Connection getConnection() {
        try {
            if (con == null || con.isClosed()) {
            	String url = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyNhaThuoc_LongNguyen;encrypt=true;trustServerCertificate=true;";
            	String user = "sa";
            	String password = "123456";
>>>>>>> main
                con = DriverManager.getConnection(url, user, password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
<<<<<<< HEAD
        } catch (ClassNotFoundException e) { // THÊM CATCH NÀY
            e.printStackTrace();
=======
>>>>>>> main
        }
        return con;
    }
}