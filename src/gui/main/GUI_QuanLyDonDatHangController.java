package gui.main;

import java.text.SimpleDateFormat;
import java.util.List;

import dao.DAO_DonDatHang;
import entity.DonDatHang;
import gui.dialogs.Dialog_ChiTietDonDatHangController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utils.AlertUtils;

public class GUI_QuanLyDonDatHangController {

    @FXML private TableView<DonDatHang> tableDonDat;
    @FXML private TableColumn<DonDatHang, Void> colChiTiet, colThaoTac;

    // Đã FIX: Xóa dấu phẩy thừa ở cuối
    @FXML private TableColumn<DonDatHang, String> colMaDon, colNhaCungCap, colTrangThaiHang;
    @FXML private TableColumn<DonDatHang, java.sql.Date> colNgayLap;
    @FXML private TextField txtTimKiem;

    private DAO_DonDatHang dao = new DAO_DonDatHang();
    private ObservableList<DonDatHang> masterData = FXCollections.observableArrayList();
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    @FXML public void initialize() {
        setupTable();
        loadData();
        setupSearch();
    }

    private void setupTable() {
        colMaDon.setCellValueFactory(new PropertyValueFactory<>("maDonDatHang"));
        colMaDon.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        colNhaCungCap.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNhaCungCap().getTenNhaCungCap()));

        colNgayLap.setCellValueFactory(new PropertyValueFactory<>("ngayDat"));
        colNgayLap.setStyle("-fx-alignment: CENTER;");
        colNgayLap.setCellFactory(column -> new TableCell<DonDatHang, java.sql.Date>() {
            @Override
			protected void updateItem(java.sql.Date item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : sdf.format(item));
            }
        });

        // NÚT XEM
        colChiTiet.setCellFactory(param -> new TableCell<DonDatHang, Void>() {
            private final Button btnXem = new Button("Xem");
            {
                btnXem.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0284c7; -fx-background-radius: 20; -fx-font-weight: bold; -fx-cursor: hand;");
                btnXem.setPrefWidth(70);
            }
            @Override
			protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(btnXem);
                    setAlignment(Pos.CENTER);
                    btnXem.setOnAction(e -> {
                        DonDatHang don = getTableRow().getItem();
                        if (don != null) {
							moDialogChiTiet(don);
						}
                    });
                }
            }
        });

        // CỘT TRẠNG THÁI NHẬP
        colTrangThaiHang.setCellValueFactory(new PropertyValueFactory<>("trangThaiHang"));
        colTrangThaiHang.setCellFactory(col -> createBadgeCell());

     // NÚT NHẬP KHO
        colThaoTac.setCellFactory(param -> new TableCell<DonDatHang, Void>() {
            private final Button btnNhap = new Button("📦 Nhập kho");
            {
                btnNhap.setMinWidth(140);
            }
            @Override
			protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    DonDatHang don = getTableRow().getItem();

                    // Logic kiểm tra mặc định
                    boolean choPhep = don.isChoPhepNhapKho();

                    // ĐOẠN FIX CỦA SẾP: Ép tắt nút nếu đơn đã "Giao Một Phần" (vì phần thiếu đã tách đơn rồi)
                    String trangThai = don.getTrangThaiHang();
                    if (trangThai != null && trangThai.toLowerCase().contains("một phần")) {
                        choPhep = false; // Thu hồi quyền nhập kho
                    }

                    if (choPhep) {
                        btnNhap.setDisable(false);
                        btnNhap.setStyle("-fx-background-color: linear-gradient(to right,#10b981,#059669); -fx-text-fill: white; -fx-background-radius: 20; -fx-font-weight: bold; -fx-cursor: hand;");
                    } else {
                        btnNhap.setDisable(true);
                        btnNhap.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #94a3b8; -fx-background-radius: 20;");
                    }

                    setGraphic(btnNhap);
                    setAlignment(Pos.CENTER);

                    btnNhap.setOnAction(e -> chuyenSangTrangNhapKho(don, e));
                }
            }
        });
    }

    private TableCell<DonDatHang, String> createBadgeCell() {
        return new TableCell<>() {
            @Override
			protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); }
                else {
                    Label lbl = new Label(item);
                    lbl.getStyleClass().add("badge");

                    // Logic màu sắc cho Badge
                    if (item.contains("Chờ Giao") || item.contains("Chưa Nhập") || item.contains("Một Phần")) {
                        lbl.getStyleClass().add("badge-warning");
                    } else if (item.contains("Đã Nhập") || item.contains("Hoàn Thành")) {
                        lbl.getStyleClass().add("badge-success");
                    } else if (item.contains("Đang Xử Lý")) {
                        lbl.getStyleClass().add("badge-primary");
                    } else if (item.contains("Hủy Đơn") || item.contains("DA_HUY")) {
                        lbl.getStyleClass().add("badge-danger");
                    } else {
                        lbl.getStyleClass().add("badge-gray");
                    }

                    HBox box = new HBox(lbl);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        };
    }

    public void loadData() {
List<DonDatHang> list = dao.getAllDonDatHang();

        list.sort((d1, d2) -> d2.getMaDonDatHang().compareTo(d1.getMaDonDatHang()));

        masterData.setAll(list);
        tableDonDat.setItems(masterData);
    }

    private void setupSearch() {
        FilteredList<DonDatHang> filteredData = new FilteredList<>(masterData, p -> true);
        txtTimKiem.textProperty().addListener((obs, oldV, newV) -> {
            filteredData.setPredicate(don -> {
                if (newV == null || newV.isEmpty()) {
					return true;
				}
                String lower = newV.toLowerCase();
                return don.getMaDonDatHang().toLowerCase().contains(lower) ||
                       don.getNhaCungCap().getTenNhaCungCap().toLowerCase().contains(lower);
            });
        });
        SortedList<DonDatHang> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableDonDat.comparatorProperty());
        tableDonDat.setItems(sortedData);
    }

    // Mở Dialog
    private void moDialogChiTiet(DonDatHang don) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_ChiTietDonDatHang.fxml"));
            Parent root = loader.load();

            Dialog_ChiTietDonDatHangController controller = loader.getController();
            controller.setDonDatHang(don);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Chi tiết đơn hàng - " + don.getMaDonDatHang());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            // Lắng nghe sự kiện khi Dialog đóng thì refresh lại bảng chính (để cập nhật trạng thái nếu có Hủy Đơn)
            stage.setOnHidden(e -> {
                tableDonDat.refresh();
            });

            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi hệ thống");
            alert.setHeaderText("Không thể mở cửa sổ chi tiết!");
            alert.setContentText("Chi tiết lỗi: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void chuyenSangTrangNhapKho(DonDatHang don, ActionEvent event) {
        try {
            // 1. Tải trang Nhập Kho
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/main/GUI_NhapKho.fxml"));
            Parent rootNhapKho = loader.load();

            // 2. Lấy controller và gọi hàm "nhập sẵn dữ liệu"
            GUI_NhapKhoController controller = loader.getController();
            controller.chuyenTuDonDatHang(don);

            // 3. MA THUẬT ĐỔI TRANG TỰ ĐỘNG
            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            javafx.scene.Scene scene = source.getScene();

            // Tìm root của cửa sổ và thế chỗ bằng trang Nhập Kho
            if (scene.getRoot() instanceof javafx.scene.layout.BorderPane) {
                javafx.scene.layout.BorderPane mainLayout = (javafx.scene.layout.BorderPane) scene.getRoot();
                mainLayout.setCenter(rootNhapKho); // Nhét vào giữa BorderPane
            }
            else if (scene.getRoot() instanceof javafx.scene.layout.StackPane) {
                javafx.scene.layout.StackPane mainLayout = (javafx.scene.layout.StackPane) scene.getRoot();
                mainLayout.getChildren().clear();
                mainLayout.getChildren().add(rootNhapKho); // Nhét vào StackPane
            } else {
                System.out.println("❌ Không tương thích: Root của sếp không phải BorderPane hay StackPane!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải trang Nhập Kho!");
        }
    }
    @FXML void handleLamMoi(ActionEvent event) {
        loadData();
        txtTimKiem.clear();
    }

    @FXML void handleTaoDonMoi(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_TaoDonDatHang.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Tạo Đơn Đặt Hàng Mới");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            // Lắng nghe khi đóng popup thì reload lại bảng danh sách đơn đặt hàng
            stage.setOnHidden(e -> handleLamMoi(null));

            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở form tạo đơn: " + e.getMessage());
        }
    }
}