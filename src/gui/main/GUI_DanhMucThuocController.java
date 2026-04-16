package gui.main;

import dao.DAO_Thuoc;
import entity.Thuoc;
import gui.dialogs.Dialog_SuaThuocController;
import gui.dialogs.Dialog_XoaThuocController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utils.AlertUtils;
import utils.DoiTraSession;
import utils.SceneUtils;
import utils.WindowUtils;

import java.io.IOException;
import java.io.InputStream;

public class GUI_DanhMucThuocController {
    @FXML private TableView<Thuoc> tableThuoc;
    @FXML private Label lblTitle;

    @FXML private TableColumn<Thuoc, String> colMa, colHinhAnh, colTen, colDanhMuc, colHoatChat, colHangSX, colNuocSX, colCongDung, colTrangThai;
    @FXML private TableColumn<Thuoc, Boolean> colKeDon;

    @FXML private TextField txtTimKiem;
    @FXML private Button btnThem, btnSua, btnXoa, btnDoiThuoc;

    private final DAO_Thuoc daoThuoc = new DAO_Thuoc();
    private final ObservableList<Thuoc> masterData = FXCollections.observableArrayList();
    private FilteredList<Thuoc> filteredData;

    @FXML
    public void initialize() {
        setupTable();
        loadData();
        setupExchangeMode();
    }

    private void setupTable() {
        colMa.setCellValueFactory(new PropertyValueFactory<>("maThuoc"));
        colTen.setCellValueFactory(new PropertyValueFactory<>("tenThuoc"));
        colDanhMuc.setCellValueFactory(new PropertyValueFactory<>("tenDanhMuc"));
        colHoatChat.setCellValueFactory(new PropertyValueFactory<>("hoatChat"));
        colHangSX.setCellValueFactory(new PropertyValueFactory<>("hangSanXuat"));
        colNuocSX.setCellValueFactory(new PropertyValueFactory<>("nuocSanXuat"));
        colCongDung.setCellValueFactory(new PropertyValueFactory<>("congDung"));

        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        colTrangThai.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                switch (item) {
                    case "DANG_BAN":
                        setText("Dang kinh doanh");
                        break;
                    case "HET_HANG":
                        setText("Het hang");
                        break;
                    default:
                        setText(item);
                        break;
                }
                setStyle("");
            }
        });

        colKeDon.setCellValueFactory(new PropertyValueFactory<>("canKeDon"));
        colKeDon.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : (item ? "Co" : "Khong"));
            }
        });

        colHinhAnh.setCellValueFactory(new PropertyValueFactory<>("hinhAnh"));
        colHinhAnh.setCellFactory(column -> new TableCell<>() {
            private final ImageView iv = new ImageView();
            private final javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(iv);

            {
                iv.setFitWidth(60);
                iv.setFitHeight(60);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
                box.setAlignment(Pos.CENTER);
                box.setPrefHeight(72);
            }

            @Override
            protected void updateItem(String file, boolean empty) {
                super.updateItem(file, empty);
                if (empty || file == null || file.trim().isEmpty()) {
                    iv.setImage(null);
                    setGraphic(empty ? null : box);
                    return;
                }

                try {
                    InputStream is = getClass().getResourceAsStream("/resources/images/images_thuoc/" + file.trim());
                    iv.setImage(is != null ? new Image(is, 60, 60, true, true) : null);
                } catch (Exception e) {
                    iv.setImage(null);
                }
                setGraphic(box);
            }
        });

        tableThuoc.setRowFactory(tv -> {
            TableRow<Thuoc> row = new TableRow<>();

            row.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> {
                if (event.getClickCount() == 1 && !row.isEmpty() && row.isSelected()) {
                    tv.getSelectionModel().clearSelection();
                    tv.getFocusModel().focus(-1);
                    tableThuoc.getParent().requestFocus();
                    event.consume();
                }
            });

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    checkAndOpenDialog("/gui/dialogs/Dialog_SuaThuoc.fxml", "Sua Thuoc");
                }
            });

            return row;
        });
    }

    private void loadData() {
        masterData.setAll(daoThuoc.getAllThuoc());

        filteredData = new FilteredList<>(masterData, p -> true);
        txtTimKiem.textProperty().addListener((o, oldV, newV) -> filterData());

        SortedList<Thuoc> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableThuoc.comparatorProperty());
        tableThuoc.setItems(sortedData);
    }

    private void filterData() {
        String keyword = txtTimKiem.getText() == null ? "" : txtTimKiem.getText().toLowerCase();

        filteredData.setPredicate(t -> {
            if (keyword.isEmpty()) {
                return true;
            }
            if (t.getMaThuoc() != null && t.getMaThuoc().toLowerCase().contains(keyword)) {
                return true;
            }
            if (t.getTenThuoc() != null && t.getTenThuoc().toLowerCase().contains(keyword)) {
                return true;
            }
            if (t.getHoatChat() != null && t.getHoatChat().toLowerCase().contains(keyword)) {
                return true;
            }
            if (t.getHangSanXuat() != null && t.getHangSanXuat().toLowerCase().contains(keyword)) {
                return true;
            }
            if (t.getCongDung() != null && t.getCongDung().toLowerCase().contains(keyword)) {
                return true;
            }

            String keDonString = t.isCanKeDon() ? "co ke don" : "khong ke don";
            return keDonString.contains(keyword);
        });
    }

    @FXML
    void handleThem() {
        openDialog("/gui/dialogs/Dialog_ThemThuoc.fxml", "Them Thuoc Moi", null);
    }

    @FXML
    void handleSua() {
        checkAndOpenDialog("/gui/dialogs/Dialog_SuaThuoc.fxml", "Sua Thuoc");
    }

    @FXML
    void handleXoa() {
        checkAndOpenDialog("/gui/dialogs/Dialog_XoaThuoc.fxml", "Xoa Thuoc");
    }

    @FXML
    void handleDoiThuoc() {
        Thuoc selected = tableThuoc.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Thong bao", "Vui long chon mot thuoc de doi.");
            return;
        }
        DoiTraSession.setThuocDoiDaChon(selected);
        DoiTraSession.setDangChonThuocDoi(false);
        SceneUtils.switchPage("/gui/main/GUI_ChiTietDoiTra.fxml");
    }

    @FXML
    void handleMoQuyDoiDonVi() {
        Thuoc selected = tableThuoc.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Thong bao", "Vui long chon mot loai thuoc trong bang!");
            return;
        }
        moCuaSoDonViQuyDoi(selected);
    }

    @FXML
    void handleRefresh() {
        txtTimKiem.clear();
        tableThuoc.getSelectionModel().clearSelection();
        loadData();
    }

    private void checkAndOpenDialog(String path, String title) {
        if (DoiTraSession.isDangChonThuocDoi()) {
            return;
        }

        Thuoc selected = tableThuoc.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Thong bao", "Vui long chon mot loai thuoc trong bang!");
            return;
        }
        openDialog(path, title, selected);
    }

    private void openDialog(String path, String title, Thuoc data) {
        FXMLLoader loader = WindowUtils.openModal(path, title);
        if (loader == null) {
            return;
        }

        Object ctrl = loader.getController();
        if (ctrl instanceof Dialog_SuaThuocController && data != null) {
            ((Dialog_SuaThuocController) ctrl).setThuocData(data);
        } else if (ctrl instanceof Dialog_XoaThuocController && data != null) {
            ((Dialog_XoaThuocController) ctrl).setThuocData(data);
        }

        Stage stage = (Stage) ((Parent) loader.getRoot()).getScene().getWindow();
        stage.showAndWait();
        loadData();
    }

    private void setupExchangeMode() {
        boolean isExchangeMode = DoiTraSession.isDangChonThuocDoi();

        if (lblTitle != null) {
            lblTitle.setText(isExchangeMode ? "CHON THUOC DOI" : "QUAN LY DANH MUC THUOC");
        }

        setNodeVisible(btnThem, !isExchangeMode);
        setNodeVisible(btnSua, !isExchangeMode);
        setNodeVisible(btnXoa, !isExchangeMode);
        setNodeVisible(btnDoiThuoc, isExchangeMode);

        if (btnDoiThuoc != null) {
            btnDoiThuoc.setDisable(tableThuoc.getSelectionModel().getSelectedItem() == null);
        }

        tableThuoc.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (btnDoiThuoc != null) {
                btnDoiThuoc.setDisable(!isExchangeMode || newVal == null);
            }
        });
    }

    private void setNodeVisible(javafx.scene.Node node, boolean visible) {
        if (node == null) {
            return;
        }
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private void moCuaSoDonViQuyDoi(Thuoc thuoc) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/main/GUI_DonViQuyDoi.fxml"));
            Parent root = loader.load();

            GUI_DonViQuyDoiController controller = loader.getController();
            controller.setThuoc(thuoc);

            Stage stage = new Stage();
            stage.setTitle("Quy doi don vi - " + thuoc.getTenThuoc());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Loi", "Khong the mo trang quy doi don vi.");
        }
    }
}
