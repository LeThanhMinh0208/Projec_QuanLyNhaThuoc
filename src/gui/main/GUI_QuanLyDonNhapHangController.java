package gui.main;

import dao.DAO_DonNhapHang;
import entity.DonNhapHang;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.text.SimpleDateFormat;

public class GUI_QuanLyDonNhapHangController {

    @FXML private TableView<DonNhapHang> tableDonNhap;
    @FXML private TableColumn<DonNhapHang, String> colMaDon, colNhaCungCap, colTrangThai;
    @FXML private TableColumn<DonNhapHang, java.sql.Date> colNgayLap, colNgayHenGiao;
    @FXML private TableColumn<DonNhapHang, Double> colTongTien;
    @FXML private TableColumn<DonNhapHang, Void> colHanhDong, colChiTiet; 
    @FXML private TextField txtTimKiem;

    private ObservableList<DonNhapHang> masterData = FXCollections.observableArrayList();
    private DAO_DonNhapHang daoDonNhap = new DAO_DonNhapHang();
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    @FXML
    public void initialize() {
        setupTable();
        loadDuLieuBang();
        setupSearch();
    }

    private void moDialogChiTietDon(DonNhapHang don) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_ChiTietDonDatHang.fxml"));
            Parent root = loader.load();
            gui.dialogs.Dialog_ChiTietDonDatHangController controller = loader.getController();
            controller.setDetailData(don); 

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Chi tiết đơn đặt hàng: " + don.getMaDonNhap());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Không thể mở bảng chi tiết!").show();
        }
    }

    private void setupTable() {
        colChiTiet.setStyle("-fx-alignment: CENTER;");
        colMaDon.setStyle("-fx-alignment: CENTER;");
        colNhaCungCap.setStyle("-fx-alignment: CENTER-LEFT;");
        colNgayLap.setStyle("-fx-alignment: CENTER;");
        colNgayHenGiao.setStyle("-fx-alignment: CENTER;");
        colTongTien.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 15 0 0;");
        colTrangThai.setStyle("-fx-alignment: CENTER;");
        colHanhDong.setStyle("-fx-alignment: CENTER;");

        colMaDon.setCellValueFactory(new PropertyValueFactory<>("maDonNhap"));
        colNhaCungCap.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNhaCungCap().getTenNhaCungCap()));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        
        colNgayLap.setCellValueFactory(new PropertyValueFactory<>("ngayLap"));
        colNgayLap.setCellFactory(column -> formatNgayCell());
        colNgayHenGiao.setCellValueFactory(new PropertyValueFactory<>("ngayHenGiao"));
        colNgayHenGiao.setCellFactory(column -> formatNgayCell());
        
        colTongTien.setCellValueFactory(new PropertyValueFactory<>("tongTienDuTinh"));
        colTongTien.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : String.format("%,.0f đ", item));
            }
        });

        colChiTiet.setCellFactory(param -> new TableCell<>() {
            private final Button btnXem = new Button("Xem");
            {
                btnXem.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-cursor: hand;");
                btnXem.setOnAction(event -> moDialogChiTietDon(getTableView().getItems().get(getIndex())));
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

        // FIX LOGIC: Cột Thao tác chứa Nút "Nhập Kho" chuyển trang
        colHanhDong.setCellFactory(param -> new TableCell<>() {
            private final Button btnNhapKho = new Button("📦 Nhập Hàng");
            {
                btnNhapKho.setStyle("-fx-background-color: #0ea5e9; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                btnNhapKho.setOnAction(event -> {
                    DonNhapHang don = getTableView().getItems().get(getIndex());
                    chuyenSangTrangNhapKho(don, event); // Truyền dữ liệu sang trang kia
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    DonNhapHang don = getTableRow().getItem();
                    if ("CHO_DUYET".equals(don.getTrangThai()) || "DANG_GIAO".equals(don.getTrangThai())) {
                        HBox pane = new HBox(btnNhapKho);
                        pane.setAlignment(Pos.CENTER);
                        setGraphic(pane);
                    } else {
                        setGraphic(new Label("Đã Nhập Xong"));
                    }
                }
            }
        });
    }

    // --- HÀM CHUYỂN TRANG VÀ BƠM DỮ LIỆU ---
 // --- HÀM CHUYỂN TRANG VÀ BƠM DỮ LIỆU ---
    private void chuyenSangTrangNhapKho(DonNhapHang don, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/main/GUI_NhapKho.fxml"));
            javafx.scene.Parent root = loader.load();

            // Lấy Controller bên kia và gọi hàm setDonDatHang
            GUI_NhapKhoController controller = loader.getController();
            controller.setDonDatHang(don);

            // --- FIX LỖI ÉP GIAO DIỆN TẠI ĐÂY ---
            javafx.scene.Node currentPage = ((javafx.scene.Node) event.getSource()).getScene().lookup(".dmk-main-bg");
            
            if (currentPage != null && currentPage.getParent() instanceof javafx.scene.layout.Pane) {
                javafx.scene.layout.Pane parent = (javafx.scene.layout.Pane) currentPage.getParent();
                
                // 1. Ép cái form mới phải bung to ra hết cỡ (Chống móp méo)
                if (parent instanceof javafx.scene.layout.HBox) {
                    javafx.scene.layout.HBox.setHgrow(root, javafx.scene.layout.Priority.ALWAYS);
                } else if (parent instanceof javafx.scene.layout.VBox) {
                    javafx.scene.layout.VBox.setVgrow(root, javafx.scene.layout.Priority.ALWAYS);
                } else if (parent instanceof javafx.scene.layout.BorderPane) {
                    ((javafx.scene.layout.BorderPane) parent).setCenter(root);
                    return; 
                }

                // 2. Tráo trang mới vào vị trí trang cũ
                int index = parent.getChildren().indexOf(currentPage);
                parent.getChildren().set(index, root);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Lỗi khi mở trang Nhập Kho: " + e.getMessage()).show();
        }
    }
    private TableCell<DonNhapHang, java.sql.Date> formatNgayCell() {
        return new TableCell<>() {
            @Override protected void updateItem(java.sql.Date item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : sdf.format(item));
            }
        };
    }

    @FXML
    void loadDuLieuBang() {
        masterData.setAll(daoDonNhap.getAllDonNhapHang());
        tableDonNhap.setItems(masterData);
    }

    private void setupSearch() {
        FilteredList<DonNhapHang> filteredData = new FilteredList<>(masterData, p -> true);
        txtTimKiem.textProperty().addListener((obs, oldV, newV) -> {
            filteredData.setPredicate(don -> {
                if (newV == null || newV.isEmpty()) return true;
                String lower = newV.toLowerCase();
                return don.getMaDonNhap().toLowerCase().contains(lower) || 
                       don.getNhaCungCap().getTenNhaCungCap().toLowerCase().contains(lower);
            });
        });
        SortedList<DonNhapHang> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableDonNhap.comparatorProperty());
        tableDonNhap.setItems(sortedData);
    }

    @FXML
    void moDialogTaoDonMoi(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_TaoDonNhapHang.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Tạo Đơn Nhập Hàng Mới");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadDuLieuBang();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML void handleRefresh(ActionEvent event) {
        loadDuLieuBang();
        txtTimKiem.clear();
    }
}