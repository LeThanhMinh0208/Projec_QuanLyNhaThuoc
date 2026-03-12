package gui.dialogs;

import dao.DAO_Thuoc;
import entity.Thuoc;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class Dialog_SuaThuocController {
    @FXML private TextField txtMa, txtTen, txtHoatChat;
    @FXML private ComboBox<String> cbDVT, cbTrangThai;
    @FXML private Button btnHuy;
    private DAO_Thuoc dao = new DAO_Thuoc();

    @FXML public void initialize() {
        cbDVT.getItems().setAll("Viên", "Vỉ", "Hộp", "Chai", "Ống");
        cbTrangThai.getItems().setAll("Đang kinh doanh", "Ngừng kinh doanh");
    }

    public void setThuocData(Thuoc t) {
        txtMa.setText(t.getMaThuoc());
        txtMa.setEditable(false); // Không cho sửa khóa chính
        txtTen.setText(t.getTenThuoc());
        txtHoatChat.setText(t.getHoatChat());
        cbDVT.setValue(t.getDonViCoBan());
        cbTrangThai.setValue(t.getTrangThai());
    }

    @FXML void handleLuu() {
        Thuoc t = new Thuoc();
        t.setMaThuoc(txtMa.getText());
        t.setTenThuoc(txtTen.getText());
        t.setHoatChat(txtHoatChat.getText());
        t.setDonViCoBan(cbDVT.getValue());
        t.setTrangThai(cbTrangThai.getValue());

        if(dao.capNhatThuoc(t)) {
            handleHuy();
        } else {
            new Alert(Alert.AlertType.ERROR, "Lỗi cập nhật CSDL!").show();
        }
    }

    @FXML void handleHuy() { ((Stage) btnHuy.getScene().getWindow()).close(); }
}