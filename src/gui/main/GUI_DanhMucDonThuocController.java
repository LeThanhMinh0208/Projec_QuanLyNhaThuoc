package gui.main;

import dao.DAO_DanhMucDonThuoc;
import dao.DAO_HoaDon;
import entity.DonThuoc;
import entity.HoaDonView;
import gui.dialogs.Dialog_DonThuocController;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class GUI_DanhMucDonThuocController implements Initializable {

    @FXML private TableView<DonThuoc>            tableThuoc;
    @FXML private TableColumn<DonThuoc, String> colMaDon;
    @FXML private TableColumn<DonThuoc, String> colMaHoaDon;
    @FXML private TableColumn<DonThuoc, String> colBacSi;
    @FXML private TableColumn<DonThuoc, String> colChanDoan;
    @FXML private TableColumn<DonThuoc, String> colBenhNhan;
    
    @FXML private TableColumn<DonThuoc, DonThuoc> colChiTietHoaDon; 
    @FXML private TableColumn<DonThuoc, DonThuoc> colHinhAnh;
    @FXML private TableColumn<DonThuoc, DonThuoc> colTaiLap; // 🚨 CỘT TÁI LẬP 🚨
    @FXML private TableColumn<DonThuoc, DonThuoc> colXoa;
    
    @FXML private TextField                     txtTimKiem;
    @FXML private ComboBox<String>              cbLocDanhMuc;

    private final DAO_DanhMucDonThuoc dao = new DAO_DanhMucDonThuoc();
    private final DAO_HoaDon daoHoaDon = new DAO_HoaDon(); 
    
    private final ObservableList<DonThuoc> masterData = FXCollections.observableArrayList();
    private FilteredList<DonThuoc> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colMaDon.setCellValueFactory(new PropertyValueFactory<>("maDonThuoc"));
        colBacSi.setCellValueFactory(new PropertyValueFactory<>("tenBacSi"));
        colChanDoan.setCellValueFactory(new PropertyValueFactory<>("chanDoan"));
        colBenhNhan.setCellValueFactory(new PropertyValueFactory<>("thongTinBenhNhan"));
        colMaHoaDon.setCellValueFactory(new PropertyValueFactory<>("maHoaDon"));

        filteredData = new FilteredList<>(masterData, p -> true);
        SortedList<DonThuoc> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableThuoc.comparatorProperty());
        tableThuoc.setItems(sortedData);

        // NÚT CHI TIẾT
        colChiTietHoaDon.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue()));
        colChiTietHoaDon.setCellFactory(param -> new TableCell<>() {
            private final Button btnChiTiet = new Button("📄 Chi Tiết");
            {
                btnChiTiet.getStyleClass().addAll("btn-action-table", "btn-soft-blue");
                btnChiTiet.setOnAction(e -> {
                    DonThuoc dt = getTableView().getItems().get(getIndex());
                    if (dt.getMaHoaDon() != null && !dt.getMaHoaDon().trim().isEmpty()) {
                        try {
                            HoaDonView hdView = daoHoaDon.getHoaDonViewByMa(dt.getMaHoaDon());
                            if (hdView != null) moDialogChiTiet(hdView);
                            else showWarn("Lỗi: Không tìm thấy chi tiết hóa đơn!");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        showWarn("Đơn thuốc này chưa được thanh toán!");
                    }
                });
            }
            @Override
            protected void updateItem(DonThuoc dt, boolean empty) {
                super.updateItem(dt, empty);
                setGraphic(empty || dt == null ? null : btnChiTiet);
            }
        });

        // NÚT XEM ẢNH
        colHinhAnh.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue()));
        colHinhAnh.setCellFactory(param -> new TableCell<>() {
            private final Button btnXem = new Button("👁 Xem");
            {
                btnXem.getStyleClass().addAll("btn-action-table", "btn-soft-teal"); 
                btnXem.setOnAction(e -> hienThiHinhAnh(getTableView().getItems().get(getIndex()).getHinhAnhDon()));
            }
            @Override
            protected void updateItem(DonThuoc dt, boolean empty) {
                super.updateItem(dt, empty);
                setGraphic(empty || dt == null ? null : btnXem);
            }
        });

        // =======================================================
        // 🚨 NÚT TÁI LẬP (MỚI THÊM LẠI, CHƯA CÓ LOGIC) 🚨
        // =======================================================
        colTaiLap.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue()));
        colTaiLap.setCellFactory(param -> new TableCell<>() {
            private final Button btnTaiLap = new Button("♻ Tái Lập");
            {
                btnTaiLap.getStyleClass().addAll("btn-action-table", "btn-soft-amber");
                btnTaiLap.setOnAction(e -> {
                    DonThuoc dt = getTableView().getItems().get(getIndex());
                    System.out.println("Đang chờ sếp set logic tái lập cho mã: " + dt.getMaDonThuoc());
                });
            }
            @Override
            protected void updateItem(DonThuoc dt, boolean empty) {
                super.updateItem(dt, empty);
                setGraphic(empty || dt == null ? null : btnTaiLap);
            }
        });

        // NÚT XÓA MỀM
        colXoa.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue()));
        colXoa.setCellFactory(param -> new TableCell<>() {
            private final Button btnXoa = new Button("✖ Xóa");
            {
                btnXoa.getStyleClass().addAll("btn-action-table", "btn-soft-red");
                btnXoa.setOnAction(e -> {
                    DonThuoc dt = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Bạn có chắc muốn xóa đơn thuốc: " + dt.getMaDonThuoc() + "?",
                        ButtonType.YES, ButtonType.NO);
                    confirm.showAndWait().ifPresent(btn -> {
                        if (btn == ButtonType.YES) {
                            if (dao.xoa(dt.getMaDonThuoc())) {
                                taiDuLieu(); 
                                new Alert(Alert.AlertType.INFORMATION, "Đã xóa đơn thuốc!", ButtonType.OK).showAndWait();
                            } else showWarn("Xóa thất bại!");
                        }
                    });
                });
            }
            @Override
            protected void updateItem(DonThuoc dt, boolean empty) {
                super.updateItem(dt, empty);
                setGraphic(empty || dt == null ? null : btnXoa);
            }
        });

        cbLocDanhMuc.setOnAction(e -> locDuLieu());
        txtTimKiem.textProperty().addListener((obs, oldVal, newVal) -> locDuLieu());

        tableThuoc.setRowFactory(tv -> {
            TableRow<DonThuoc> row = new TableRow<>();
            row.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> {
                if (event.getClickCount() == 1 && (!row.isEmpty()) && row.isSelected()) {
                    tv.getSelectionModel().clearSelection();
                    tv.getFocusModel().focus(-1);
                    tableThuoc.getParent().requestFocus();
                    event.consume();
                }
            });
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    moDialogSua(row.getItem());
                }
            });
            return row;
        });

        taiDuLieu();
    }

    private void locDuLieu() {
        if (filteredData == null) return;
        String selectedBacSi = cbLocDanhMuc.getValue();
        String keyword = txtTimKiem.getText() == null ? "" : txtTimKiem.getText().trim().toLowerCase();

        filteredData.setPredicate(dt -> {
            boolean matchBacSi = true;
            if (selectedBacSi != null && !selectedBacSi.equals("Tất cả bác sĩ") && !selectedBacSi.trim().isEmpty()) {
                matchBacSi = dt.getTenBacSi() != null && dt.getTenBacSi().equals(selectedBacSi);
            }

            boolean matchKeyword = true;
            if (!keyword.isEmpty()) {
                String maDon = dt.getMaDonThuoc() == null ? "" : dt.getMaDonThuoc().toLowerCase();
                String maHD = dt.getMaHoaDon() == null ? "" : dt.getMaHoaDon().toLowerCase();
                String benhNhan = dt.getThongTinBenhNhan() == null ? "" : dt.getThongTinBenhNhan().toLowerCase();
                String chanDoan = dt.getChanDoan() == null ? "" : dt.getChanDoan().toLowerCase();

                matchKeyword = maDon.contains(keyword) 
                            || maHD.contains(keyword)
                            || benhNhan.contains(keyword)
                            || chanDoan.contains(keyword);
            }

            return matchBacSi && matchKeyword;
        });
    }

    private void taiDuLieu() {
        String bacSiDangChon = cbLocDanhMuc.getValue();
        cbLocDanhMuc.setOnAction(null); 

        List<String> listBacSi = dao.getDanhSachBacSi();
        if (!listBacSi.contains("Tất cả bác sĩ")) {
            listBacSi.add(0, "Tất cả bác sĩ");
        }
        cbLocDanhMuc.setItems(FXCollections.observableArrayList(listBacSi));

        if (bacSiDangChon != null && cbLocDanhMuc.getItems().contains(bacSiDangChon)) {
            cbLocDanhMuc.setValue(bacSiDangChon);
        } else {
            cbLocDanhMuc.getSelectionModel().selectFirst();
        }
        
        cbLocDanhMuc.setOnAction(e -> locDuLieu()); 
        masterData.setAll(dao.getAll());
        locDuLieu();
    }

    private void moDialogSua(DonThuoc donThuocSua) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_DonThuoc.fxml"));
            Parent root = loader.load();
            Dialog_DonThuocController ctrl = loader.getController();
            ctrl.setOnSuccess(this::taiDuLieu); 
            
            if (donThuocSua != null) ctrl.setDonThuocSua(donThuocSua);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Cập Nhật Đơn Thuốc");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void moDialogChiTiet(HoaDonView hd) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_ChiTietHoaDon.fxml"));
            Parent root = loader.load();
            gui.dialogs.Dialog_ChiTietHoaDonController ctrl = loader.getController();
            ctrl.setHoaDon(hd);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Chi Tiết Hóa Đơn — " + hd.getMaHoaDon());
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showWarn("Không thể mở form chi tiết hóa đơn!");
        }
    }

    private void hienThiHinhAnh(String tenFileAnh) {
        if (tenFileAnh == null || tenFileAnh.trim().isEmpty() || tenFileAnh.equals("url_hinh_anh")) {
            showWarn("Đơn thuốc này chưa được cập nhật hình ảnh chụp thực tế!");
            return;
        }

        try {
            Image img = null;
            String projectPath = System.getProperty("user.dir");
            File file = new File(projectPath + "/src/resources/images/images_donthuoc/" + tenFileAnh);

            if (file.exists()) {
                img = new Image(file.toURI().toString(), false);
            } else {
                var stream = getClass().getResourceAsStream("/resources/images/images_donthuoc/" + tenFileAnh);
                if (stream != null) img = new Image(stream);
            }

            if (img == null || img.isError()) {
                showWarn("Không tìm thấy file ảnh: " + tenFileAnh + "\nĐã thử tìm tại: " + file.getAbsolutePath());
                return; 
            }

            Stage stage = new Stage();
            stage.setTitle("Hình Ảnh Đơn Thuốc: " + tenFileAnh);
            
            ImageView imageView = new ImageView(img);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);

            StackPane imageContainer = new StackPane(imageView);
            imageContainer.setAlignment(Pos.CENTER);
            imageContainer.setStyle("-fx-background-color: #f0f9ff; -fx-padding: 10;");

            double windowWidth = Math.min(img.getWidth() + 40, 1200); 
            double windowHeight = Math.min(img.getHeight() + 60, 800);

            ScrollPane scrollPane = new ScrollPane(imageContainer);
            scrollPane.setPannable(true);
            scrollPane.setStyle("-fx-background-color: transparent;");

            Scene scene = new Scene(scrollPane, windowWidth, windowHeight);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showWarn("Lỗi hệ thống khi mở trình xem ảnh: " + e.getMessage());
        }
    }

    private void showWarn(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }
}