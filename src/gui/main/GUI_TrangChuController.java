package gui.main;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class GUI_TrangChuController {

    @FXML
    private Label lblXinChao;

    @FXML
    public void initialize() {
        // Hàm này tự chạy khi form Trang Chủ vừa mở lên
        // Mốt mình sẽ truyền dữ liệu đăng nhập qua đây nha
        lblXinChao.setText("Chào mừng đến với hệ thống!"); 
    }
}