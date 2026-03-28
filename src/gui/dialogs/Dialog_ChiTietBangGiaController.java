package gui.dialogs;

import dao.DAO_BangGia;
import entity.BangGia;
import entity.ChiTietBangGia;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utils.AlertUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class Dialog_ChiTietBangGiaController {

    @FXML private Label     lblHeaderTitle, lblMa, lblLoai, lblNgayBatDau, lblTrangThai, lblNgayKTHint;
    @FXML private Label     lblCanhBaoActive, lblCanhBaoDefault;
    @FXML private Button    btnThemThuoc, btnXoaBangGia;
    @FXML private TextField txtTen;
    @FXML private DatePicker dpNgayKetThuc;
    @FXML private TextArea  txtMoTa;

    @FXML private TableView<ChiTietBangGia>             tableChiTiet;
    @FXML private TableColumn<ChiTietBangGia, String>   colTenThuoc, colDonVi, colTyLe, colGiaBan;
    @FXML private TableColumn<ChiTietBangGia, Void>     colHanhDong;

    private final DAO_BangGia daoBG = new DAO_BangGia();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private BangGia bangGia;
    private boolean isBangGiaDangHoatDong = false;
    private ObservableList<ChiTietBangGia> listCT = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colTenThuoc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTenThuoc()));
        colDonVi.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTenDonVi()));
        colTyLe.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTyLeQuyDoi() > 0 ? "x" + c.getValue().getTyLeQuyDoi() : "—"));
        colGiaBan.setCellValueFactory(c -> {
            BigDecimal g = c.getValue().getDonGiaBan();
            return new SimpleStringProperty(g != null ? String.format("%,.0f ₫", g.doubleValue()) : "—");
        });

        colHanhDong.setCellFactory(col -> new TableCell<>() {
            private final Button btnSua = new Button("Sửa giá");
            private final Button btnXoa = new Button("Xóa");
            private final javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(6, btnSua, btnXoa);
            {
                btnSua.getStyleClass().add("bg-btn-edit-price");
                btnXoa.getStyleClass().add("bg-btn-delete");
                box.setAlignment(Pos.CENTER);
                btnSua.setOnAction(e -> handleSuaGia(getTableView().getItems().get(getIndex())));
                btnXoa.setOnAction(e -> handleXoa(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                // Disable nút sửa/xóa nếu bảng giá đang hoạt động
                btnSua.setDisable(isBangGiaDangHoatDong);
                btnXoa.setDisable(isBangGiaDangHoatDong);
                
                // Ràng buộc riêng cho DEFAULT
                if (bangGia != null && "DEFAULT".equals(bangGia.getLoaiBangGia())) {
                    btnXoa.setVisible(false);
                    btnXoa.setManaged(false);
                    // Sửa giá: ẩn luôn nếu đang active, hiện nếu chưa active
                    if (isBangGiaDangHoatDong) {
                        btnSua.setVisible(false);
                        btnSua.setManaged(false);
                    } else {
                        btnSua.setVisible(true);
                        btnSua.setManaged(true);
                        btnSua.setDisable(false);
                    }
                }
                
                setGraphic(box);
            }
        });

        tableChiTiet.setItems(listCT);
    }

    /** Gọi từ bên ngoài để nạp dữ liệu vào dialog */
    public void setBangGia(BangGia bg) {
        this.bangGia = bg;
        lblMa.setText(bg.getMaBangGia());
        lblLoai.setText(bg.getLoaiBangGia());
        lblNgayBatDau.setText(bg.getNgayBatDau() != null ? bg.getNgayBatDau().format(FMT) : "—");
        lblTrangThai.setText(bg.getTrangThaiHienThi());
        txtTen.setText(bg.getTenBangGia());
        txtMoTa.setText(bg.getMoTa() != null ? bg.getMoTa() : "");
        dpNgayKetThuc.setValue(bg.getNgayKetThuc());

        boolean isPromo = "PROMO".equals(bg.getLoaiBangGia());
        boolean daKetThuc = "Đã kết thúc".equals(bg.getTrangThaiHienThi());
        dpNgayKetThuc.setDisable(!isPromo || daKetThuc);
        lblNgayKTHint.setText(isPromo
                ? (daKetThuc ? "(Bảng giá đã kết thúc, không sửa được)" : "")
                : "(DEFAULT không có ngày kết thúc)");

        // Xác định bảng giá đang thực sự hoạt động
        LocalDate today = LocalDate.now();
        isBangGiaDangHoatDong = bg.isTrangThai()
                && !bg.getNgayBatDau().isAfter(today)
                && (bg.getNgayKetThuc() == null || !bg.getNgayKetThuc().isBefore(today));

        // Ràng buộc Label
        if ("DEFAULT".equals(bg.getLoaiBangGia())) {
            if (isBangGiaDangHoatDong) {
                lblCanhBaoActive.setVisible(true);
                lblCanhBaoActive.setManaged(true);
                lblCanhBaoDefault.setVisible(false);
                lblCanhBaoDefault.setManaged(false);
            } else {
                lblCanhBaoActive.setVisible(false);
                lblCanhBaoActive.setManaged(false);
                lblCanhBaoDefault.setVisible(true);
                lblCanhBaoDefault.setManaged(true);
            }
            btnThemThuoc.setVisible(false);
            btnThemThuoc.setManaged(false);
        } else {
            // PROMO -> Ẩn cả 2
            lblCanhBaoActive.setVisible(false);
            lblCanhBaoActive.setManaged(false);
            lblCanhBaoDefault.setVisible(false);
            lblCanhBaoDefault.setManaged(false);
            
            btnThemThuoc.setVisible(true);
            btnThemThuoc.setManaged(true);
            btnThemThuoc.setDisable(isBangGiaDangHoatDong);
        }

        // VẤN ĐỀ 3: Nút Xóa bảng áp dụng cho TẤT CẢ TƯƠNG LAI
        if (bg.getNgayBatDau().isAfter(today)) {
            btnXoaBangGia.setVisible(true);
            btnXoaBangGia.setManaged(true);
        } else {
            btnXoaBangGia.setVisible(false);
            btnXoaBangGia.setManaged(false);
        }

        // Khóa textbox nếu active
        if (isBangGiaDangHoatDong) {
            txtTen.setEditable(false);
            txtMoTa.setEditable(false);
        } else {
            txtTen.setEditable(true);
            txtMoTa.setEditable(true);
        }

        loadChiTiet();
    }

    private void loadChiTiet() {
        if (bangGia == null) return;
        listCT.setAll(daoBG.getChiTietByMaBangGia(bangGia.getMaBangGia()));
    }

    @FXML
    private void handleLuu() {
        if (bangGia == null) return;
        String ten = txtTen.getText().trim();
        if (ten.isEmpty()) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Tên bảng giá không được để trống.");
            return;
        }

        if ("PROMO".equals(bangGia.getLoaiBangGia())) {
            LocalDate ngayKT = dpNgayKetThuc.getValue();
            LocalDate today  = LocalDate.now();
            if (ngayKT != null) {
                if (!ngayKT.isAfter(today)) {
                    AlertUtils.showAlert(Alert.AlertType.WARNING, "Ngày không hợp lệ",
                            "Ngày kết thúc phải là ngày trong tương lai.");
                    return;
                }
                if (!ngayKT.isAfter(bangGia.getNgayBatDau())) {
                    AlertUtils.showAlert(Alert.AlertType.WARNING, "Ngày không hợp lệ",
                            "Ngày kết thúc phải sau ngày bắt đầu (" + bangGia.getNgayBatDau().format(FMT) + ").");
                    return;
                }
            }
            bangGia.setNgayKetThuc(ngayKT);
        }

        bangGia.setTenBangGia(ten);
        bangGia.setMoTa(txtMoTa.getText().trim());

        if (daoBG.capNhatBangGia(bangGia)) {
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã lưu thay đổi bảng giá.");
        } else {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lưu thay đổi.");
        }
    }

    @FXML
    private void handleThemThuoc() {
        if (bangGia == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_ThemThuocVaoBangGia.fxml"));
            Parent root = loader.load();
            Dialog_ThemThuocVaoBangGiaController ctrl = loader.getController();
            ctrl.setBangGia(bangGia);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Thêm Thuốc Vào Bảng Giá");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadChiTiet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleSuaGia(ChiTietBangGia ct) {
        TextInputDialog dlg = new TextInputDialog(
                ct.getDonGiaBan() != null ? ct.getDonGiaBan().toPlainString() : "0");
        dlg.setTitle("Sửa giá");
        dlg.setHeaderText(null);
        dlg.setContentText("Giá mới cho " + ct.getTenThuoc() + " - " + ct.getTenDonVi() + " (VNĐ):");
        Optional<String> r = dlg.showAndWait();
        r.ifPresent(val -> {
            try {
                BigDecimal gia = new BigDecimal(val.trim().replace(",", ""));
                if (gia.compareTo(BigDecimal.ZERO) < 0) {
                    AlertUtils.showAlert(Alert.AlertType.WARNING, "Lỗi", "Giá không được âm."); return;
                }
                if (daoBG.capNhatGia(ct.getMaBangGia(), ct.getMaQuyDoi(), gia)) {
                    loadChiTiet();
                } else {
                    AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật giá.");
                }
            } catch (NumberFormatException e) {
                AlertUtils.showAlert(Alert.AlertType.WARNING, "Lỗi nhập liệu", "Giá trị không hợp lệ.");
            }
        });
    }

    private void handleXoa(ChiTietBangGia ct) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText(null);
        confirm.setContentText("Xóa \"" + ct.getTenThuoc() + " - " + ct.getTenDonVi() + "\" khỏi bảng giá?");
        Optional<ButtonType> r = confirm.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.OK) {
            if (daoBG.xoaChiTietBangGia(ct.getMaBangGia(), ct.getMaQuyDoi())) {
                listCT.remove(ct);
            } else {
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa.");
            }
        }
    }

    @FXML
    private void handleDong() {
        ((Stage) tableChiTiet.getScene().getWindow()).close();
    }
    
    @FXML
    private void handleXoaBangGia() {
        if (bangGia == null) return;
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa bảng giá");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn chắc chắn muốn xóa toàn bộ bảng giá " + bangGia.getTenBangGia() + "?\n"
                             + "Hành động này không thể hoàn tác.");
        
        Optional<ButtonType> r = confirm.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.OK) {
            if (daoBG.xoaBangGia(bangGia.getMaBangGia())) {
                AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa bảng giá thành công.");
                ((Stage) tableChiTiet.getScene().getWindow()).close();
            } else {
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa bảng giá.");
            }
        }
    }
}
