package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    private static final ConnectDB instance = new ConnectDB();
    
    // Cấu hình thông tin CSDL tập trung tại một nơi để dễ bảo trì
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyNhaThuoc_LongNguyen;encrypt=true;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASSWORD = "sapassword";

    public static ConnectDB getInstance() {
        return instance;
    }

    /**
     * Hàm kiểm tra thử kết nối lúc khởi động ứng dụng
     */
    public void connect() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            try (Connection testConn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                if (testConn != null && !testConn.isClosed()) {
                    System.out.println("Kết nối CSDL thành công!");
                }
            }
        } catch (SQLException e) {
            System.out.println("Lỗi kết nối CSDL: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Không tìm thấy driver: " + e.getMessage());
        }
    }

    /**
     * Không cần thiết phải quản lý đóng connection tĩnh nữa
     */
    public void disconnect() {
        // Hàm này được giữ lại để tránh lỗi biên dịch ở các lớp cũ gọi tới, không cần xử lý gì thêm.
    }

    /**
     * HÀM QUAN TRỌNG: Trả về một đối tượng Connection mới độc lập cho mỗi lần gọi.
     * Giúp giải quyết triệt để lỗi "Socket closed" khi chạy đa luồng (Multi-threading).
     */
    public static Connection getConnection() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Lỗi tạo kết nối mới: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Không tìm thấy Driver SQL Server: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}