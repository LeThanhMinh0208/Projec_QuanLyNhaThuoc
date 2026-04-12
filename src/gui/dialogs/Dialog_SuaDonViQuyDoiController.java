package gui.dialogs;

import dao.DAO_DonViQuyDoi;
import dao.DAO_Thuoc;
import entity.DonViQuyDoi;
import entity.Thuoc;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ListCell;
import javafx.beans.value.ChangeListener;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import utils.AlertUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class Dialog_SuaDonViQuyDoiController {

    @FXML private Label lblTitle;
    @FXML private Label lblChonThuoc;
    @FXML private TextField txtMaThuoc;
    @FXML private TextField txtTenThuoc;
    @FXML private ComboBox<Thuoc> cbThuoc;
    @FXML private TextField txtDonViBac1;
    @FXML private ComboBox<String> cbDonViBac2;
    @FXML private ComboBox<String> cbDonViBac3;
    @FXML private Spinner<Integer> spTyLeBac2;
    @FXML private Spinner<Integer> spTyLeBac3;
    @FXML private Button btnLuu;
    @FXML private Button btnHuy;

    private final DAO_Thuoc daoThuoc = new DAO_Thuoc();
    private final DAO_DonViQuyDoi daoDonViQuyDoi = new DAO_DonViQuyDoi();

    private Thuoc thuocDangSua;
    private boolean addMode;
    private Runnable onSaved;
    private String originalStateKey = "";

    @FXML
    public void initialize() {
        txtDonViBac1.setDisable(true);

        configureThuocComboDisplay();

        cbDonViBac2.setItems(FXCollections.observableArrayList(goiYDonVi()));
        cbDonViBac3.setItems(FXCollections.observableArrayList(goiYDonVi()));

        spTyLeBac2.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 10000, 10));
        spTyLeBac3.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(3, 100000, 100));
        addDirtyTrackingListeners();

        cbThuoc.getItems().setAll(daoThuoc.getAllThuocTatCa());
        cbThuoc.valueProperty().addListener((obs, oldV, newV) -> {
            if (addMode) {
                if (newV != null) {
                    fillTheoThuocDuocChon(newV);
                    danhDauStateGoc();
                    updateSaveButtonState();
                } else {
                    clearFormForAddMode();
                    updateSaveButtonState();
                }
            }
        });

        updateSaveButtonState();
    }

    public void setAddMode(boolean addMode) {
        this.addMode = addMode;
        if (addMode) {
            lblTitle.setText("THÊM QUY ĐỔI ĐƠN VỊ THUỐC");
            cbThuoc.setDisable(false);
            cbThuoc.setVisible(true);
            cbThuoc.setManaged(true);
            lblChonThuoc.setVisible(true);
            lblChonThuoc.setManaged(true);
            cbThuoc.getSelectionModel().clearSelection();
            clearFormForAddMode();
            danhDauStateGoc();
            updateSaveButtonState();
        } else {
            lblTitle.setText("SỬA QUY ĐỔI ĐƠN VỊ THUỐC");
            cbThuoc.setDisable(true);
            cbThuoc.setVisible(false);
            cbThuoc.setManaged(false);
            lblChonThuoc.setVisible(false);
            lblChonThuoc.setManaged(false);
            updateSaveButtonState();
        }
    }

    public void setDataForEdit(Thuoc thuoc, List<DonViQuyDoi> danhSachDonVi) {
        this.thuocDangSua = thuoc;
        txtMaThuoc.setText(thuoc.getMaThuoc());
        txtTenThuoc.setText(thuoc.getTenThuoc());

        txtDonViBac1.setText(thuoc.getDonViCoBan());
        cbDonViBac2.setValue(null);
        cbDonViBac3.setValue(null);
        spTyLeBac2.getValueFactory().setValue(10);
        spTyLeBac3.getValueFactory().setValue(100);

        ArrayList<DonViQuyDoi> moRong = new ArrayList<>();
        for (DonViQuyDoi dv : danhSachDonVi) {
            if (dv.getTyLeQuyDoi() == 1 && (txtDonViBac1.getText() == null || txtDonViBac1.getText().isBlank())) {
                txtDonViBac1.setText(dv.getTenDonVi());
            }
            if (dv.getTyLeQuyDoi() > 1) {
                moRong.add(dv);
            }
        }
        moRong.sort(Comparator.comparingInt(DonViQuyDoi::getTyLeQuyDoi));

        if (!moRong.isEmpty()) {
            cbDonViBac2.setValue(moRong.get(0).getTenDonVi());
            spTyLeBac2.getValueFactory().setValue(moRong.get(0).getTyLeQuyDoi());
        }
        if (moRong.size() > 1) {
            cbDonViBac3.setValue(moRong.get(1).getTenDonVi());
            spTyLeBac3.getValueFactory().setValue(moRong.get(1).getTyLeQuyDoi());
        }

        danhDauStateGoc();
        updateSaveButtonState();
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    private void fillTheoThuocDuocChon(Thuoc thuoc) {
        this.thuocDangSua = thuoc;
        txtMaThuoc.setText(thuoc.getMaThuoc());
        txtTenThuoc.setText(thuoc.getTenThuoc());

        List<DonViQuyDoi> danhSach = daoDonViQuyDoi.getDonViByMaThuocOrderAsc(thuoc.getMaThuoc());
        setDataForEdit(thuoc, danhSach);
    }

    @FXML
    private void handleLuu() {
        if (thuocDangSua == null) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Thiếu dữ liệu", "Vui lòng chọn thuốc cần cấu hình.");
            return;
        }

        String tenBac1 = normalize(txtDonViBac1.getText());
        String tenBac2 = normalize(cbDonViBac2.getValue());
        String tenBac3 = normalize(cbDonViBac3.getValue());

        int tyLeBac2 = spTyLeBac2.getValue() == null ? 0 : spTyLeBac2.getValue();
        int tyLeBac3 = spTyLeBac3.getValue() == null ? 0 : spTyLeBac3.getValue();

        String loi = validateInput(tenBac1, tenBac2, tenBac3, tyLeBac2, tyLeBac3);
        if (loi != null) {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Dữ liệu không hợp lệ", loi);
            return;
        }

        ArrayList<DonViQuyDoi> dsLuu = new ArrayList<>();
        if (!tenBac2.isBlank()) {
            dsLuu.add(new DonViQuyDoi(null, thuocDangSua.getMaThuoc(), tenBac2, tyLeBac2));
        }
        if (!tenBac3.isBlank()) {
            dsLuu.add(new DonViQuyDoi(null, thuocDangSua.getMaThuoc(), tenBac3, tyLeBac3));
        }

        boolean ok = daoDonViQuyDoi.saveCauHinhDonVi(thuocDangSua.getMaThuoc(), tenBac1, dsLuu);
        if (!ok) {
            String message = daoDonViQuyDoi.getLastError() == null
                    ? "Không thể lưu quy đổi đơn vị."
                    : daoDonViQuyDoi.getLastError();
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", message);
            return;
        }

        AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã lưu cấu hình quy đổi đơn vị.");
        if (onSaved != null) {
            onSaved.run();
        }
        danhDauStateGoc();
        close();
    }

    @FXML
    private void handleHuy() {
        close();
    }

    private String validateInput(String tenBac1, String tenBac2, String tenBac3, int tyLeBac2, int tyLeBac3) {
        if (tenBac1.isBlank()) {
            return "Đơn vị bậc 1 không được để trống.";
        }

        if (!tenBac2.isBlank() && !tenBac3.isBlank() && tyLeBac2 == tyLeBac3) {
            return "Tỷ lệ quy đổi của bậc 2 và bậc 3 không được trùng nhau.";
        }

        if (!tenBac3.isBlank() && tenBac2.isBlank()) {
            return "Vui lòng nhập bậc 2 trước khi nhập bậc 3.";
        }

        if (!tenBac2.isBlank() && tyLeBac2 <= 1) {
            return "Tỷ lệ bậc 2 phải lớn hơn 1.";
        }

        if (!tenBac3.isBlank() && tyLeBac3 <= tyLeBac2) {
            return "Tỷ lệ bậc 3 phải lớn hơn tỷ lệ bậc 2.";
        }

        if (!tenBac2.isBlank() && tenBac1.equalsIgnoreCase(tenBac2)) {
            return "Tên đơn vị bậc 2 không được trùng bậc 1.";
        }

        if (!tenBac3.isBlank() && (tenBac1.equalsIgnoreCase(tenBac3) || tenBac2.equalsIgnoreCase(tenBac3))) {
            return "Tên đơn vị bậc 3 không được trùng các bậc trước.";
        }

        return null;
    }

    private List<String> goiYDonVi() {
        return List.of("Viên", "Vỉ", "Hộp", "Chai", "Lọ", "Tuýp", "Gói", "Ống");
    }

    private void clearFormForAddMode() {
        thuocDangSua = null;
        txtMaThuoc.clear();
        txtTenThuoc.clear();
        txtDonViBac1.clear();
        cbDonViBac2.setValue(null);
        cbDonViBac3.setValue(null);
        spTyLeBac2.getValueFactory().setValue(10);
        spTyLeBac3.getValueFactory().setValue(100);
    }

    private void addDirtyTrackingListeners() {
        ChangeListener<Object> listener = (obs, oldV, newV) -> updateSaveButtonState();
        cbDonViBac2.valueProperty().addListener(listener);
        cbDonViBac3.valueProperty().addListener(listener);
        spTyLeBac2.valueProperty().addListener(listener);
        spTyLeBac3.valueProperty().addListener(listener);
    }

    private void danhDauStateGoc() {
        originalStateKey = buildStateKey();
    }

    private void updateSaveButtonState() {
        if (btnLuu == null) {
            return;
        }

        if (thuocDangSua == null) {
            btnLuu.setDisable(true);
            return;
        }

        String currentState = buildStateKey();
        boolean changed = !Objects.equals(currentState, originalStateKey);
        btnLuu.setDisable(!changed);
    }

    private String buildStateKey() {
        return String.join("|",
                normalize(txtMaThuoc.getText()),
                normalize(txtDonViBac1.getText()),
                normalize(cbDonViBac2.getValue()),
                String.valueOf(spTyLeBac2.getValue()),
                normalize(cbDonViBac3.getValue()),
                String.valueOf(spTyLeBac3.getValue()));
    }

    private void configureThuocComboDisplay() {
        cbThuoc.setConverter(new StringConverter<>() {
            @Override
            public String toString(Thuoc thuoc) {
                if (thuoc == null) {
                    return "";
                }
                return thuoc.getMaThuoc() + " - " + thuoc.getTenThuoc();
            }

            @Override
            public Thuoc fromString(String string) {
                return null;
            }
        });

        cbThuoc.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Thuoc item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getMaThuoc() + " - " + item.getTenThuoc());
            }
        });
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private void close() {
        Stage stage = (Stage) btnLuu.getScene().getWindow();
        stage.close();
    }
}
