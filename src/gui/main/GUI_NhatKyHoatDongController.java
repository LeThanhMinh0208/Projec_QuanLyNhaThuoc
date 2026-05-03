package gui.main;

import dao.DAO_NhanVien;
import dao.DAO_NhatKyHoatDong;
import entity.NhanVien;
import entity.NhatKyHoatDong;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class GUI_NhatKyHoatDongController {

    @FXML private ComboBox<String> cboNhanVien;
    @FXML private DatePicker dpTuNgay, dpDenNgay;
    @FXML private TextField txtTimKiem;
    @FXML private TableView<NhatKyHoatDong> tableNhatKy;
    @FXML private TableColumn<NhatKyHoatDong, Integer> colSTT;
    @FXML private TableColumn<NhatKyHoatDong, String> colThoiGian, colNhanVien, colHanhDong, colDoiTuong, colMaDoiTuong, colMoTa;
    @FXML private Label lblTongSo;

    private DAO_NhatKyHoatDong daoNhatKy = new DAO_NhatKyHoatDong();
    private DAO_NhanVien daoNhanVien = new DAO_NhanVien();
    private ObservableList<NhatKyHoatDong> dsNhatKy = FXCollections.observableArrayList();

    // Map mã nhân viên từ ComboBox
    private ObservableList<String> dsMaNV = FXCollections.observableArrayList();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @FXML
    public void initialize() {
        setupComboBox();
        setupTable();
        loadData();
    }

    private void setupComboBox() {
        List<NhanVien> nhanViens = daoNhanVien.getChiNhanVien();
        ObservableList<String> items = FXCollections.observableArrayList();
        items.add("-- Tất Cả --");
        dsMaNV.add("");

        for (NhanVien nv : nhanViens) {
            items.add(nv.getMaNhanVien() + " - " + nv.getHoTen());
            dsMaNV.add(nv.getMaNhanVien());
        }
        cboNhanVien.setItems(items);
        cboNhanVien.getSelectionModel().selectFirst();
    }

    private void setupTable() {
        colSTT.setCellValueFactory(col -> {
            int index = tableNhatKy.getItems().indexOf(col.getValue()) + 1;
            return new SimpleIntegerProperty(index).asObject();
        });

        colThoiGian.setCellValueFactory(col -> {
            if (col.getValue().getThoiGian() != null)
                return new SimpleStringProperty(col.getValue().getThoiGian().format(FMT));
            return new SimpleStringProperty("");
        });

        colNhanVien.setCellValueFactory(col ->
            new SimpleStringProperty(col.getValue().getTenNhanVien() + " (" + col.getValue().getMaNhanVien() + ")"));

        colHanhDong.setCellValueFactory(col ->
            new SimpleStringProperty(dichHanhDong(col.getValue().getHanhDong())));

        colDoiTuong.setCellValueFactory(col ->
            new SimpleStringProperty(col.getValue().getDoiTuong()));

        colMaDoiTuong.setCellValueFactory(col ->
            new SimpleStringProperty(col.getValue().getMaDoiTuong()));

        colMoTa.setCellValueFactory(col ->
            new SimpleStringProperty(col.getValue().getMoTa()));

        // Tô màu hành động
        colHanhDong.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("badge-login", "badge-logout", "badge-create", "badge-update", "badge-delete");
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    NhatKyHoatDong nk = getTableView().getItems().get(getIndex());
                    String hd = nk.getHanhDong();
                    if ("DANG_NHAP".equals(hd)) getStyleClass().add("badge-login");
                    else if ("DANG_XUAT".equals(hd)) getStyleClass().add("badge-logout");
                    else if (hd != null && (hd.startsWith("THEM") || hd.startsWith("TAO"))) getStyleClass().add("badge-create");
                    else if (hd != null && (hd.startsWith("SUA") || hd.startsWith("CAP_NHAT"))) getStyleClass().add("badge-update");
                    else if (hd != null && (hd.startsWith("XOA") || hd.startsWith("KHOA") || hd.startsWith("VO_HIEU"))) getStyleClass().add("badge-delete");
                }
            }
        });

        // Double-click để xem chi tiết
        tableNhatKy.setRowFactory(tv -> {
            TableRow<NhatKyHoatDong> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    moDialogChiTiet(row.getItem());
                }
            });
            return row;
        });
    }

    private void loadData() {
        dsNhatKy.clear();
        dsNhatKy.addAll(daoNhatKy.getAll());
        tableNhatKy.setItems(dsNhatKy);
        lblTongSo.setText("Tổng: " + dsNhatKy.size() + " bản ghi");
    }

    @FXML
    void handleLoc() {
        String maNV = "";
        int selectedIdx = cboNhanVien.getSelectionModel().getSelectedIndex();
        if (selectedIdx > 0 && selectedIdx < dsMaNV.size()) {
            maNV = dsMaNV.get(selectedIdx);
        }

        List<NhatKyHoatDong> results = daoNhatKy.timKiem(
            txtTimKiem.getText(),
            maNV,
            dpTuNgay.getValue(),
            dpDenNgay.getValue()
        );

        dsNhatKy.clear();
        dsNhatKy.addAll(results);
        tableNhatKy.setItems(dsNhatKy);
        lblTongSo.setText("Tổng: " + dsNhatKy.size() + " bản ghi");
    }

    @FXML
    void handleLamMoi() {
        cboNhanVien.getSelectionModel().selectFirst();
        dpTuNgay.setValue(null);
        dpDenNgay.setValue(null);
        txtTimKiem.clear();
        loadData();
    }

    /**
     * Mở dialog chi tiết nhật ký khi double-click
     */
    private void moDialogChiTiet(NhatKyHoatDong nk) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Chi Tiết Nhật Ký Hoạt Động");
        dialog.setResizable(false);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #f0f9ff;");

        // === HEADER ===
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 25, 15, 25));
        header.setStyle("-fx-background-color: linear-gradient(to right, #0c4a6e, #0369a1); -fx-background-radius: 0;");

        String badgeIcon = getBadgeIcon(nk.getHanhDong());
        Label lblIcon = new Label(badgeIcon);
        lblIcon.setStyle("-fx-font-size: 32px; -fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 50; -fx-padding: 10 14; -fx-min-width: 56; -fx-min-height: 56; -fx-alignment: center;");

        VBox headerInfo = new VBox(3);
        Label lblHanhDong = new Label(dichHanhDong(nk.getHanhDong()));
        lblHanhDong.setStyle("-fx-font-size: 22px; -fx-font-weight: 900; -fx-text-fill: white;");
        Label lblThoiGianHeader = new Label(nk.getThoiGian() != null ? nk.getThoiGian().format(FMT) : "N/A");
        lblThoiGianHeader.setStyle("-fx-font-size: 13px; -fx-text-fill: #bae6fd;");
        headerInfo.getChildren().addAll(lblHanhDong, lblThoiGianHeader);

        header.getChildren().addAll(lblIcon, headerInfo);

        // === BODY ===
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(14);
        grid.setPadding(new Insets(25));
        grid.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(14, 165, 233, 0.1), 10, 0, 0, 3);");
        VBox.setMargin(grid, new Insets(15, 20, 10, 20));

        int row = 0;
        addDetailRow(grid, row++, "👤  Nhân viên", nk.getTenNhanVien() + " (" + nk.getMaNhanVien() + ")");
        addDetailRow(grid, row++, "⚡  Hành động", dichHanhDong(nk.getHanhDong()) + " (" + nk.getHanhDong() + ")");
        addDetailRow(grid, row++, "🎯  Đối tượng", nk.getDoiTuong());
        addDetailRow(grid, row++, "🔖  Mã đối tượng", nk.getMaDoiTuong());
        addDetailRow(grid, row++, "🕐  Thời gian", nk.getThoiGian() != null ? nk.getThoiGian().format(FMT) : "N/A");

        // Mô tả - dùng TextArea cho wrap text
        Label lblMoTaLabel = new Label("📝  Mô tả chi tiết");
        lblMoTaLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0369a1;");
        grid.add(lblMoTaLabel, 0, row, 2, 1);

        TextArea txtMoTa = new TextArea(nk.getMoTa() != null ? nk.getMoTa() : "Không có mô tả");
        txtMoTa.setWrapText(true);
        txtMoTa.setEditable(false);
        txtMoTa.setPrefRowCount(6);
        txtMoTa.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 14px; -fx-text-fill: #334155;");
        grid.add(txtMoTa, 0, row + 1, 2, 1);

        // === NÚT ĐÓNG ===
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(0, 20, 20, 20));
        Button btnDong = new Button("Đóng");
        btnDong.setStyle("-fx-background-color: linear-gradient(to right, #0ea5e9, #0284c7); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 10; -fx-cursor: hand; -fx-padding: 10 30;");
        btnDong.setOnAction(e -> dialog.close());
        footer.getChildren().add(btnDong);

        root.getChildren().addAll(header, grid, footer);

        Scene scene = new Scene(root, 520, 560);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label lblLabel = new Label(label);
        lblLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0369a1; -fx-min-width: 160;");
        Label lblValue = new Label(value != null ? value : "N/A");
        lblValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #334155;");
        lblValue.setWrapText(true);
        grid.add(lblLabel, 0, row);
        grid.add(lblValue, 1, row);
    }

    private String getBadgeIcon(String hanhDong) {
        if (hanhDong == null) return "📋";
        if ("DANG_NHAP".equals(hanhDong)) return "🔓";
        if ("DANG_XUAT".equals(hanhDong)) return "🔒";
        if (hanhDong.startsWith("THEM") || hanhDong.startsWith("TAO")) return "➕";
        if (hanhDong.startsWith("SUA") || hanhDong.startsWith("CAP_NHAT")) return "✏️";
        if (hanhDong.startsWith("XOA") || hanhDong.startsWith("KHOA") || hanhDong.startsWith("VO_HIEU")) return "🗑️";
        if ("DOI_MAT_KHAU".equals(hanhDong)) return "🔑";
        if ("DOI_TRA".equals(hanhDong)) return "🔄";
        if ("RESET_MAT_KHAU".equals(hanhDong)) return "🔐";
        if ("MO_KHOA_TAI_KHOAN".equals(hanhDong)) return "🔓";
        return "📋";
    }

    /**
     * Dịch mã hành động sang tiếng Việt
     */
    private String dichHanhDong(String ma) {
        if (ma == null) return "";
        switch (ma) {
            case "DANG_NHAP": return "Đăng nhập";
            case "DANG_XUAT": return "Đăng xuất";
            case "THEM": return "Thêm mới";
            case "SUA": return "Cập nhật";
            case "XOA": return "Xóa";
            case "TAO_HOA_DON": return "Tạo hóa đơn";
            case "TAO_PHIEU_NHAP": return "Tạo phiếu nhập";
            case "TAO_PHIEU_XUAT": return "Tạo phiếu xuất";
            case "TAO_DON_DAT_HANG": return "Tạo đơn đặt hàng";
            case "TAO_BANG_GIA": return "Tạo bảng giá";
            case "CAP_NHAT_BANG_GIA": return "Cập nhật bảng giá";
            case "DOI_TRA": return "Đổi trả";
            case "KHOA_TAI_KHOAN": return "Khóa tài khoản";
            case "MO_KHOA_TAI_KHOAN": return "Mở khóa TK";
            case "DOI_MAT_KHAU": return "Đổi mật khẩu";
            case "CAP_NHAT_QUYEN": return "Phân quyền";
            case "RESET_MAT_KHAU": return "Reset mật khẩu";
            case "VO_HIEU_HOA": return "Vô hiệu hóa";
            default: return ma;
        }
    }
}
