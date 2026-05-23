package utils;

import entity.NhanVien;
import java.util.ArrayList;
import java.util.List;

public class UserSession {
    private static UserSession instance;
    private NhanVien loggedInUser;
    private List<String> danhSachQuyen = new ArrayList<>();

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setUser(NhanVien user) { this.loggedInUser = user; }
    public NhanVien getUser() { return loggedInUser; }

    public void setDanhSachQuyen(List<String> dsQuyen) { this.danhSachQuyen = dsQuyen; }
    public List<String> getDanhSachQuyen() { return danhSachQuyen; }

    /**
     * Kiểm tra nhân viên đang đăng nhập có quyền cụ thể không.
     * Quản Lý (admin) luôn có toàn quyền.
     */
    public boolean hasPermission(String maQuyen) {
        if (loggedInUser == null) return false;
        // Quản Lý luôn có toàn quyền
        if ("Quản Lý".equals(loggedInUser.getChucVu())) return true;
        return danhSachQuyen.contains(maQuyen);
    }

    /**
     * Xóa session khi đăng xuất
     */
    public void clear() {
        loggedInUser = null;
        danhSachQuyen.clear();
    }
}