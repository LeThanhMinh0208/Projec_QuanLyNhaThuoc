package gui.main;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import dao.DAO_NhaCungCap;
import dao.DAO_PhieuChi;
import entity.NhaCungCap;
import entity.PhieuChi;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class GUI_QuanLyCongNoController {

    // =======================================================
    // CONTROLS TABS
    // =======================================================
    @FXML private ToggleGroup tabGroup;
    @FXML private ToggleButton tabLapPhieu, tabDanhSach;
    @FXML private VBox viewLapPhieu, viewDanhSach;

    // =======================================================
    // CONTROLS VIEW 1 (LẬP PHIẾU - DANH SÁCH NCC)
    // =======================================================
    @FXML private TextField txtTimKiemNCC;
    @FXML private TableView<NhaCungCap> tableCongNoNCC;
    @FXML private TableColumn<NhaCungCap, String> colMaNCC, colTenNCC, colSdtNCC;
    @FXML private TableColumn<NhaCungCap, Double> colCongNo;

    // =======================================================
    // CONTROLS VIEW 2 (DANH MỤC PHIẾU CHI LỊCH SỬ)
    // =======================================================
    @FXML private TextField txtTimKiemPhieuChi;
    @FXML private TableView<PhieuChi> tablePhieuChi;
    @FXML private TableColumn<PhieuChi, Void> colXemChiTiet;
    @FXML private TableColumn<PhieuChi, String> colMaPhieuChi, colNhaCungCapPhieu, colNhanVienChi, colHinhThuc;
    @FXML private TableColumn<PhieuChi, java.sql.Timestamp> colNgayChi;
    @FXML private TableColumn<PhieuChi, Double> colSoTienChi;

    // =======================================================
    // DAO & FORMATTER
    // =======================================================
    private DAO_NhaCungCap daoNCC = new DAO_NhaCungCap();
    private DAO_PhieuChi daoPhieuChi = new DAO_PhieuChi();

    private DecimalFormat df = new DecimalFormat("#,### VNĐ");
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        setupTabs();
        setupTableCongNo();
        setupTablePhieuChi();

        loadDuLieuTuDatabase();
    }

    private void setupTabs() {
        tabGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) { oldT.setSelected(true); return; }
            boolean isLapPhieu = (newT == tabLapPhieu);

            viewLapPhieu.setVisible(isLapPhieu);
            viewLapPhieu.setManaged(isLapPhieu);
            viewDanhSach.setVisible(!isLapPhieu);
            viewDanhSach.setManaged(!isLapPhieu);

            // Tự động tải dữ liệu lịch sử phiếu chi khi chuyển sang tab Danh Sách
            if (!isLapPhieu) {
                loadDanhSachPhieuChi("");
            }
        });
    }

    // =======================================================
    // XỬ LÝ TAB 1: LẬP PHIẾU
    // =======================================================
    private void setupTableCongNo() {
        colMaNCC.setCellValueFactory(new PropertyValueFactory<>("maNhaCungCap"));
        colTenNCC.setCellValueFactory(new PropertyValueFactory<>("tenNhaCungCap"));
        colSdtNCC.setCellValueFactory(new PropertyValueFactory<>("sdt"));
        colSdtNCC.setStyle("-fx-alignment: CENTER;");

        colCongNo.setCellValueFactory(new PropertyValueFactory<>("congNo"));
        colCongNo.setCellFactory(column -> new TableCell<NhaCungCap, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    if (item > 0) {
                        // Trường hợp 1: Mình đang nợ NCC -> Hiện màu Đỏ báo động
                        setText(df.format(item) + " VNĐ");
                        setStyle("-fx-text-fill: #e11d48; -fx-font-weight: bold; -fx-alignment: CENTER-RIGHT;");
                    } else if (item == 0) {
                        // Trường hợp 2: Trắng nợ -> Hiện màu Xanh lá an toàn
                        setText("0 VNĐ");
                        setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-alignment: CENTER-RIGHT;");
                    } else {
                        // Trường hợp 3: Số ÂM (NCC nợ ngược lại mình do trả hàng) -> Hiện màu Xanh dương
                        setText(df.format(item) + " VNĐ");
                        setStyle("-fx-text-fill: #0ea5e9; -fx-font-weight: bold; -fx-alignment: CENTER-RIGHT;");
                    }
                }
            }
        });
    }

    private void loadDuLieuTuDatabase() {
        List<NhaCungCap> listDB = daoNCC.getAllNhaCungCapFull();
        ObservableList<NhaCungCap> observableList = FXCollections.observableArrayList(listDB);
        tableCongNoNCC.setItems(observableList);
    }

    @FXML
    void handleMoFormLapPhieu(ActionEvent event) {
        NhaCungCap selected = tableCongNoNCC.getSelectionModel().getSelectedItem();

        // Đã sử dụng AlertUtils siêu gọn
        if (selected == null) {
            utils.AlertUtils.showAlert(Alert.AlertType.WARNING, "Chưa chọn nhà cung cấp", "Vui lòng chọn một nhà cung cấp từ bảng để lập phiếu chi!");
            return;
        }

        if (selected.getCongNo() <= 0) {
            utils.AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Không có công nợ", "Nhà cung cấp này hiện tại đã thanh toán hết công nợ (0 VNĐ).");
            return;
        }

        openDialog("/gui/dialogs/Dialog_LapPhieuChi.fxml", "Lập Phiếu Chi Nợ", selected);
        loadDuLieuTuDatabase();
    }

    private void openDialog(String path, String title, NhaCungCap data) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(path));
            javafx.scene.Parent root = loader.load();

            Object ctrl = loader.getController();

            // 💡 ĐÃ FIX: Cho phép truyền dữ liệu NCC sang cả 2 Form (Phiếu Chi & Phiếu Thu)
            if (ctrl instanceof gui.dialogs.Dialog_LapPhieuChiController && data != null) {
                ((gui.dialogs.Dialog_LapPhieuChiController) ctrl).setNhaCungCap(data);
            }
            else if (ctrl instanceof gui.dialogs.Dialog_NCCThanhToanController && data != null) {
                ((gui.dialogs.Dialog_NCCThanhToanController) ctrl).setNhaCungCap(data);
            }

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle(title);
            stage.setResizable(false);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            utils.AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Không mở được giao diện: " + e.getMessage());
        }
    }

    // =======================================================
    // XỬ LÝ TAB 2: DANH MỤC PHIẾU CHI LỊCH SỬ
    // =======================================================
    private void setupTablePhieuChi() {
        colMaPhieuChi.setCellValueFactory(new PropertyValueFactory<>("maPhieuChi"));


        colNhaCungCapPhieu.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNhaCungCap().getTenNhaCungCap()));
        colNhanVienChi.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNhanVien().getHoTen()));


        colSoTienChi.setCellValueFactory(new PropertyValueFactory<>("tongTienChi"));
        colSoTienChi.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold; -fx-text-fill: #10b981;");
        colSoTienChi.setCellFactory(col -> new TableCell<PhieuChi, Double>() {
            @Override
			protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : df.format(item));
            }
        });

        colXemChiTiet.setCellFactory(param -> new TableCell<PhieuChi, Void>() {
            private final Button btnXem = new Button("Xem");
            {
                btnXem.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0284c7; -fx-background-radius: 15; -fx-cursor: hand;");
                btnXem.setOnAction(e -> {
                    PhieuChi pc = getTableRow().getItem();
                    if (pc != null) {
						moDialogChiTietPhieuChi(pc);
					}
                });
            }
            @Override
			protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnXem);
                setStyle("-fx-alignment: CENTER;");
            }
        });

        if(txtTimKiemPhieuChi != null) {
            txtTimKiemPhieuChi.textProperty().addListener((obs, oldV, newV) -> loadDanhSachPhieuChi(newV));
        }
    }

    private void loadDanhSachPhieuChi(String tuKhoa) {
        if(tablePhieuChi != null) {
            tablePhieuChi.setItems(FXCollections.observableArrayList(daoPhieuChi.getAllPhieuChi(tuKhoa)));
        }
    }

    private void moDialogChiTietPhieuChi(PhieuChi pc) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_ChiTietPhieuChi.fxml"));
            javafx.scene.Parent root = loader.load();

            gui.dialogs.Dialog_ChiTietPhieuChiController ctrl = loader.getController();
            ctrl.setPhieuChi(pc);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Chi Tiết Phiếu Chi: " + pc.getMaPhieuChi());
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            // Đã sử dụng AlertUtils siêu gọn
            utils.AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Không mở được giao diện: " + e.getMessage());
        }
    }
    @FXML
    void handleMoFormNCCThanhToan(ActionEvent event) {
        NhaCungCap selected = tableCongNoNCC.getSelectionModel().getSelectedItem();

        if (selected == null) {
            utils.AlertUtils.showAlert(Alert.AlertType.WARNING, "Chưa chọn nhà cung cấp", "Vui lòng chọn một nhà cung cấp từ bảng để thu tiền!");
            return;
        }

        // KIỂM TRA QUAN TRỌNG: Chỉ cho phép thu tiền khi NCC đang nợ mình (Công nợ bị ÂM)
        if (selected.getCongNo() >= 0) {
            utils.AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Không thể thu tiền", "Nhà cung cấp này không nợ tiền cửa hàng.\nChức năng này chỉ dùng để thu hồi tiền khi NCC nợ lại do trả hàng (Công nợ hiện số Âm).");
            return;
        }

        openDialog("/gui/dialogs/Dialog_NCCThanhToan.fxml", "Thu Tiền Nhà Cung Cấp", selected);
        loadDuLieuTuDatabase();
    }
}