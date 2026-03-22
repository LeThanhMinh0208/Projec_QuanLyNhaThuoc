package utils;

import entity.NhanVien;

public class UserSession {
    private static UserSession instance;
    private NhanVien loggedInUser;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setUser(NhanVien user) { this.loggedInUser = user; }
    public NhanVien getUser() { return loggedInUser; }
}