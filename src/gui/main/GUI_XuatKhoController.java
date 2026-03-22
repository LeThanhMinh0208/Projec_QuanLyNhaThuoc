package gui.main;

import dao.DAO_PhieuXuat;
import entity.PhieuXuat;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;

public class GUI_XuatKhoController implements Initializable {

    @FXML private Button btnTabChuyenKho, btnTabTraNCC, btnTabXuatHuy, btnTaoPhieuMoi;
    @FXML private Label lblTieuDeTrang;
    @FXML private DatePicker dpTuNgay, dpDenNgay;
    @FXML private TextField txtTimKiem;

    @FXML private TableView<PhieuXuat> tablePhieuXuat; 
    @FXML private TableColumn<PhieuXuat, Void> colChiTiet;
    @FXML private TableColumn<PhieuXuat, String> colMaPhieu, colNguoiLap, colThongTinDacThu, colGhiChu;
    @FXML private TableColumn<PhieuXuat, java.sql.Date> colNgayXuat;

    private String currentTab = "CHUYEN_KHO"; 
    private DAO_PhieuXuat daoPhieuXuat = new DAO_PhieuXuat();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        switchTab("CHUYEN_KHO"); 
    }

    private void setupTable() {
        colMaPhieu.setCellValueFactory(new PropertyValueFactory<>("maPhieuXuat"));
        
        colNgayXuat.setCellValueFactory(new PropertyValueFactory<>("ngayXuat"));
        colNgayXuat.setCellFactory(column -> new TableCell<>() {
            private final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            @Override protected void updateItem(java.sql.Date item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : format.format(item));
            }
        });

        colNguoiLap.setCellValueFactory(cell -> {
            if (cell.getValue().getNhanVienLap() != null) {
                return new SimpleStringProperty(cell.getValue().getNhanVienLap().getHoTen());
            }
            return new SimpleStringProperty("Không rõ");
        });

        colThongTinDacThu.setCellValueFactory(new PropertyValueFactory<>("thongTinDacThu"));
        
        // Fix Wrap text cho cột Ghi Chú giống bên Nhập Kho
        colGhiChu.setCellValueFactory(new PropertyValueFactory<>("ghiChu"));
        colGhiChu.setCellFactory(column -> {
            return new TableCell<PhieuXuat, String>() {
                private final javafx.scene.text.Text text = new javafx.scene.text.Text();
                {
                    text.wrappingWidthProperty().bind(column.widthProperty().subtract(10));
                }
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        text.setText(item);
                        setGraphic(text);
                    }
                }
            };
        });

        colChiTiet.setCellFactory(param -> new TableCell<>() {
            private final Button btnXem = new Button("Xem");
            {
           
                btnXem.getStyleClass().add("xk-btn-xem"); 
               
                
                btnXem.setOnAction(event -> {
                    PhieuXuat px = getTableView().getItems().get(getIndex());
                    moDialogChiTietPhieu(px);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    HBox box = new HBox(btnXem);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });
    }

    @FXML void handleTabChuyenKho(ActionEvent event) { switchTab("CHUYEN_KHO"); }
    @FXML void handleTabTraNCC(ActionEvent event) { switchTab("TRA_NCC"); }
    @FXML void handleTabXuatHuy(ActionEvent event) { switchTab("XUAT_HUY"); }

    private void switchTab(String tabName) {
        this.currentTab = tabName;
        btnTabChuyenKho.setOpacity(0.5);
        btnTabTraNCC.setOpacity(0.5);
        btnTabXuatHuy.setOpacity(0.5);

        switch (tabName) {
            case "CHUYEN_KHO":
                btnTabChuyenKho.setOpacity(1.0); 
                lblTieuDeTrang.setText("Danh Sách Phiếu Chuyển Kho");
                btnTaoPhieuMoi.setText("➕ LẬP PHIẾU CHUYỂN KHO");
                colThongTinDacThu.setText("Kho Nhận"); 
                break;
            case "TRA_NCC":
                btnTabTraNCC.setOpacity(1.0);
                lblTieuDeTrang.setText("Danh Sách Phiếu Trả Nhà Cung Cấp");
                btnTaoPhieuMoi.setText("➕ LẬP PHIẾU TRẢ HÀNG");
                colThongTinDacThu.setText("Nhà Cung Cấp");
                break;
            case "XUAT_HUY":
                btnTabXuatHuy.setOpacity(1.0);
                lblTieuDeTrang.setText("Danh Sách Phiếu Xuất Hủy Thuốc");
                btnTaoPhieuMoi.setText("➕ LẬP PHIẾU XUẤT HỦY");
                colThongTinDacThu.setText("Lý Do Hủy");
                break;
        }
        loadDataForCurrentTab();
    }

    private void loadDataForCurrentTab() {
        List<PhieuXuat> list = daoPhieuXuat.getPhieuXuatByLoai(currentTab);
        
        FilteredList<PhieuXuat> filteredData = new FilteredList<>(FXCollections.observableArrayList(list), p -> true);
        txtTimKiem.textProperty().addListener((obs, oldV, newV) -> {
            filteredData.setPredicate(px -> {
                if (newV == null || newV.isEmpty()) return true;
                String lower = newV.toLowerCase();
                return px.getMaPhieuXuat().toLowerCase().contains(lower) || 
                       (px.getNhanVienLap() != null && px.getNhanVienLap().getHoTen().toLowerCase().contains(lower));
            });
        });
        tablePhieuXuat.setItems(filteredData);
    }

    // --- LOGIC MỞ POPUP TẠO PHIẾU MỚI (ĐÃ UN-COMMENT) ---
    @FXML
    void moDialogTaoPhieuMoi(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_TaoPhieuXuat.fxml"));
            Parent root = loader.load();

            gui.dialogs.Dialog_TaoPhieuXuatController controller = loader.getController();
            controller.setLoaiPhieu(currentTab); 

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Lập Phiếu Xuất Kho");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadDataForCurrentTab();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Chưa tạo file FXML cho form Lập Phiếu!").show();
        }
    }

    // --- LOGIC MỞ POPUP XEM CHI TIẾT (ĐÃ UN-COMMENT) ---
    private void moDialogChiTietPhieu(PhieuXuat px) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_ChiTietPhieuXuat.fxml"));
            Parent root = loader.load();

            gui.dialogs.Dialog_ChiTietPhieuXuatController controller = loader.getController();
            controller.setDuLieu(px);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Chi Tiết Phiếu Xuất: " + px.getMaPhieuXuat());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Chưa tạo file FXML cho form Chi Tiết!").show();
        }
    }

    @FXML
    void handleLamMoi(ActionEvent event) {
        txtTimKiem.clear();
        dpTuNgay.setValue(null);
        dpDenNgay.setValue(null);
        loadDataForCurrentTab();
    }
}