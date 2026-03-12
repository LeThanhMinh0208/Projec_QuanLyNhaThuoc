package gui.dialogs;

import dao.DAO_Thuoc;
import entity.Thuoc;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class Dialog_ThemThuocController {
    @FXML private TextField txtMa, txtTen, txtHoatChat;
    @FXML private ComboBox<String> cbDVT, cbTrangThai;
    @FXML private Button btnHuy;

    private DAO_Thuoc dao = new DAO_Thuoc();

    @FXML public void initialize() {
        cbDVT.getItems().setAll("Viên", "Vỉ", "Hộp", "Chai", "Ống");
        cbDVT.getSelectionModel().selectFirst();
        cbTrangThai.getItems().setAll("Đang kinh doanh", "Ngừng kinh doanh");
        cbTrangThai.getSelectionModel().selectFirst();
    }

    @FXML void handleLuu() {
        if(txtTen.getText().isEmpty() || txtMa.getText().isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Mã và Tên thuốc không được rỗng!").show();
            return;
        }
        Thuoc t = new Thuoc();
        t.setMaThuoc(txtMa.getText());
        t.setTenThuoc(txtTen.getText());
        t.setHoatChat(txtHoatChat.getText());
        t.setDonViCoBan(cbDVT.getValue());
        t.setTrangThai(cbTrangThai.getValue());
        
        // dao.themThuoc(t) - Giả sử DAO của bạn có hàm này
        // if(dao.themThuoc(t)) { handleHuy(); } else { báo lỗi }
        handleHuy(); // Tạm thời đóng cửa sổ khi chạy thử nghiệm
    }

    @FXML void handleHuy() { ((Stage) btnHuy.getScene().getWindow()).close(); }
}