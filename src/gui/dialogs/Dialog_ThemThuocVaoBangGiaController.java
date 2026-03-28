package gui.dialogs;

import dao.DAO_BangGia;
import entity.BangGia;
import entity.ChiTietBangGia;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.stage.Stage;
import utils.AlertUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Dialog_ThemThuocVaoBangGiaController {

    @FXML private TextField txtTimKiem;
    @FXML private TableView<ChiTietBangGia>           tableChonThuoc;
    @FXML private TableColumn<ChiTietBangGia, String> colChonThuoc, colChonDonVi;
    @FXML private TableColumn<ChiTietBangGia, Void>   colChonGia;

    private final DAO_BangGia daoBG = new DAO_BangGia();
    private BangGia bangGia;

    private ObservableList<ChiTietBangGia> masterList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colChonThuoc.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getTenThuoc()));
        colChonDonVi.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getTenDonVi()));

        // Cột giá nhập
        colChonGia.setCellFactory(col -> new TableCell<>() {
            private final TextField tf = new TextField();
            {
                tf.getStyleClass().add("bg-price-input");
                tf.setPromptText("0");
                tf.textProperty().addListener((o, ov, nv) -> {
                    if (!nv.matches("\\d*\\.?\\d*")) tf.setText(ov);
                    if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                        ChiTietBangGia ct = getTableView().getItems().get(getIndex());
                        try {
                            ct.setDonGiaBan(nv.isEmpty() ? BigDecimal.ZERO : new BigDecimal(nv));
                        } catch (Exception ignored) {}
                    }
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                ChiTietBangGia ct = getTableView().getItems().get(getIndex());
                tf.setText(ct.getDonGiaBan() != null && ct.getDonGiaBan().compareTo(BigDecimal.ZERO) != 0
                        ? ct.getDonGiaBan().toPlainString() : "");
                setGraphic(tf);
                setAlignment(Pos.CENTER);
            }
        });

        FilteredList<ChiTietBangGia> filtered = new FilteredList<>(masterList, p -> true);
        txtTimKiem.textProperty().addListener((o, ov, nv) -> {
            String kw = nv == null ? "" : nv.toLowerCase();
            filtered.setPredicate(ct -> kw.isEmpty()
                    || (ct.getTenThuoc() != null && ct.getTenThuoc().toLowerCase().contains(kw)));
        });
        tableChonThuoc.setItems(filtered);
    }

    public void setBangGia(BangGia bg) {
        this.bangGia = bg;
        // Load thuốc chưa có trong bảng giá
        List<ChiTietBangGia> chuaCo = daoBG.getAllThuocVaDonVi();
        // Lọc bỏ các maQuyDoi đã có trong bảng giá
        daoBG.getChiTietByMaBangGia(bg.getMaBangGia()).forEach(existing -> {
            chuaCo.removeIf(ct -> ct.getMaQuyDoi().equals(existing.getMaQuyDoi()));
        });
        masterList.setAll(chuaCo);
    }

    @FXML void handleHuy() {
        ((Stage) tableChonThuoc.getScene().getWindow()).close();
    }

    @FXML void handleLuu() {
        if (bangGia == null) return;

        List<ChiTietBangGia> toAdd = new ArrayList<>();
        for (ChiTietBangGia ct : masterList) {
            if (ct.getDonGiaBan() != null && ct.getDonGiaBan().compareTo(BigDecimal.ZERO) > 0) {
                ct.setMaBangGia(bangGia.getMaBangGia());
                toAdd.add(ct);
            }
        }

        if (toAdd.isEmpty()) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Chưa nhập giá",
                    "Vui lòng nhập giá (> 0) cho ít nhất một đơn vị thuốc.");
            return;
        }

        int success = 0;
        for (ChiTietBangGia ct : toAdd) {
            if (daoBG.themChiTietBangGia(ct)) success++;
        }

        if (success > 0) {
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Thành công",
                    "Đã thêm " + success + " đơn vị thuốc vào bảng giá.");
            ((Stage) tableChonThuoc.getScene().getWindow()).close();
        } else {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm thuốc vào bảng giá.");
        }
    }
}
