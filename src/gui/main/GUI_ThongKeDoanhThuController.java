package gui.main;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dao.DAO_ThongKeDoanhThu;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import utils.ThongKeDoanhThuExcelExporter;
import utils.ThongKeDoanhThuPdfExporter;
import java.awt.Desktop;
import java.io.File;

public class GUI_ThongKeDoanhThuController {

    @FXML private DatePicker dpTuNgay, dpDenNgay;
    @FXML private ComboBox<String> cbLoaiBan, cbHinhThuc;
    @FXML private ComboBox<String> cbKhoangNhanh;

    // KPI Labels
    @FXML private Label lblTongDoanhThu, lblTongDoanhThuPercent;
    @FXML private Label lblTongDonHang, lblTongDonHangPercent;
    @FXML private Label lblGiaTrungBinh, lblGiaTrungBinhPercent;
    @FXML private Label lblSoKhachHang, lblSoKhachHangPercent;
    @FXML private Label lblChartThoiGianTitle;

    // Charts
    @FXML private LineChart<String, Number> chartThoiGian;
    @FXML private CategoryAxis xAxisThoiGian;
    @FXML private NumberAxis yAxisThoiGian;
    @FXML private PieChart chartCoCAu;
    @FXML private BarChart<String, Number> chartTopSanPham;

    // Tables
    @FXML private TableView<Map<String, Object>> tableTopKhachHang;
    @FXML private TableColumn<Map<String, Object>, Integer> colSTT1;
    @FXML private TableColumn<Map<String, Object>, String> colTenKhachHang;
    @FXML private TableColumn<Map<String, Object>, Integer> colSoDon;
    @FXML private TableColumn<Map<String, Object>, Double> colTongDoanhThuKH;

    @FXML private TableView<Map<String, Object>> tableProductDead;
    @FXML private TableColumn<Map<String, Object>, Integer> colSTT2;
    @FXML private TableColumn<Map<String, Object>, String> colTenSanPham;
    @FXML private TableColumn<Map<String, Object>, String> colNhom;
    @FXML private TableColumn<Map<String, Object>, Integer> colSoNgayKhongBan;
    @FXML private TableColumn<Map<String, Object>, Integer> colTonKho;

    private DAO_ThongKeDoanhThu daoThongKe = new DAO_ThongKeDoanhThu();
    private DecimalFormat dfCurrency = new DecimalFormat("#,##0");
    private boolean suppressAutoReload = false;
    private LocalDate lastChangedDateValue;
    private boolean lastChangedWasTuNgay = true;

    // Lưu trữ dữ liệu hiện tại để xuất báo cáo
    private LocalDate currentTuNgay;
    private LocalDate currentDenNgay;
    private String currentLoaiBan;
    private String currentHinhThuc;
    private double currentTongDoanhThu;
    private int currentTongDon;
    private double currentGiaTrungBinh;
    private int currentSoKhachHang;
    private List<Map<String, Object>> currentDataTheoNgay;
    private List<Map<String, Object>> currentDataCoCau;
    private List<Map<String, Object>> currentTopKhachHang;
    private List<Map<String, Object>> currentProductDead;
    private List<Map<String, Object>> currentThongKeHinhThuc;

    @FXML
    public void initialize() {
        LocalDate today = LocalDate.now();
        dpTuNgay.setValue(today);
        dpDenNgay.setValue(today);
        if (lblChartThoiGianTitle != null) {
            lblChartThoiGianTitle.setText("Doanh Thu Theo Thời Gian (Tháng " + today.getMonthValue() + ")");
        }

        // Cấu hình ComboBox
        cbLoaiBan.setItems(FXCollections.observableArrayList("Tất cả", "Bán lẻ", "Bán theo đơn"));
        cbHinhThuc.setItems(FXCollections.observableArrayList("Tất cả", "Tiền mặt", "Chuyển khoản", "Thẻ"));
        cbKhoangNhanh.setItems(FXCollections.observableArrayList("Tùy chọn", "Trong ngày", "7 ngày qua", "30 ngày qua"));
        cbLoaiBan.setValue("Tất cả");
        cbHinhThuc.setValue("Tất cả");
        cbKhoangNhanh.setValue("Tùy chọn");

        setupAutoReload();

        // Cấu hình TableView
        setupTableTopKhachHang();
        setupTableProductDead();

        // Tải dữ liệu lần đầu
        loadDataThongKe();
    }

    private void setupAutoReload() {
        dpTuNgay.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                lastChangedDateValue = newValue;
                lastChangedWasTuNgay = true;
            }
            onDateFilterChanged();
        });
        dpDenNgay.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                lastChangedDateValue = newValue;
                lastChangedWasTuNgay = false;
            }
            onDateFilterChanged();
        });

        cbLoaiBan.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!suppressAutoReload) {
                loadDataThongKe();
            }
        });

        cbHinhThuc.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!suppressAutoReload) {
                loadDataThongKe();
            }
        });

        cbKhoangNhanh.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!suppressAutoReload) {
                applyQuickRange(newValue);
            }
        });
    }

    private void onDateFilterChanged() {
        if (suppressAutoReload) {
            return;
        }

        // Khi người dùng chỉnh tay ngày, chuyển về chế độ tùy chọn
        if (cbKhoangNhanh != null && !"Tùy chọn".equals(cbKhoangNhanh.getValue())) {
            suppressAutoReload = true;
            cbKhoangNhanh.setValue("Tùy chọn");
            suppressAutoReload = false;
        }

        loadDataThongKe();
    }

    private void applyQuickRange(String quickRange) {
        if (quickRange == null || "Tùy chọn".equals(quickRange)) {
            loadDataThongKe();
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate fromDate;

        switch (quickRange) {
            case "Trong ngày":
                fromDate = today;
                break;
            case "7 ngày qua":
                fromDate = today.minusDays(6);
                break;
            case "30 ngày qua":
                fromDate = today.minusDays(29);
                break;
            default:
                return;
        }

        suppressAutoReload = true;
        dpTuNgay.setValue(fromDate);
        dpDenNgay.setValue(today);
        suppressAutoReload = false;

        loadDataThongKe();
    }

    private void setupTableTopKhachHang() {
        colSTT1.setCellValueFactory(cellData -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> row = cellData.getValue();
            return new javafx.beans.property.SimpleObjectProperty<>((Integer) row.get("stt"));
        });

        colTenKhachHang.setCellValueFactory(cellData -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> row = cellData.getValue();
            return new javafx.beans.property.SimpleObjectProperty<>((String) row.get("tenKhachHang"));
        });

        colSoDon.setCellValueFactory(cellData -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> row = cellData.getValue();
            return new javafx.beans.property.SimpleObjectProperty<>((Integer) row.get("soDon"));
        });

        colTongDoanhThuKH.setCellValueFactory(cellData -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> row = cellData.getValue();
            return new javafx.beans.property.SimpleObjectProperty<>((Double) row.get("doanhThu"));
        });

        // Custom cell factory cho hiển thị tiền tệ
        colTongDoanhThuKH.setCellFactory(column -> new TableCell<Map<String, Object>, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(dfCurrency.format(item) + " ₫");
                }
            }
        });

        tableTopKhachHang.setRowFactory(tv -> new TableRow<Map<String, Object>>() {
            @Override
            protected void updateItem(Map<String, Object> item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("stat-table-row");
                if (!empty) {
                    getStyleClass().add("stat-table-row");
                }
            }
        });
    }

    private void setupTableProductDead() {
        colSTT2.setCellValueFactory(cellData -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> row = cellData.getValue();
            return new javafx.beans.property.SimpleObjectProperty<>((Integer) row.get("stt"));
        });

        colTenSanPham.setCellValueFactory(cellData -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> row = cellData.getValue();
            return new javafx.beans.property.SimpleObjectProperty<>((String) row.get("tenThuoc"));
        });

        colNhom.setCellValueFactory(cellData -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> row = cellData.getValue();
            return new javafx.beans.property.SimpleObjectProperty<>((String) row.get("nhomThuoc"));
        });

        colSoNgayKhongBan.setCellValueFactory(cellData -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> row = cellData.getValue();
            return new javafx.beans.property.SimpleObjectProperty<>((Integer) row.get("soNgayKhongBan"));
        });

        colTonKho.setCellValueFactory(cellData -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> row = cellData.getValue();
            return new javafx.beans.property.SimpleObjectProperty<>((Integer) row.get("tonKho"));
        });

        tableProductDead.setRowFactory(tv -> new TableRow<Map<String, Object>>() {
            @Override
            protected void updateItem(Map<String, Object> item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("stat-table-row");
                if (!empty) {
                    getStyleClass().add("stat-table-row");
                }
            }
        });
    }

    /**
     * Tải tất cả dữ liệu thống kê
     */
    @FXML
    private void loadDataThongKe() {
        LocalDate tuNgay = dpTuNgay.getValue();
        LocalDate denNgay = dpDenNgay.getValue();

        if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) {
            if (lastChangedDateValue != null) {
                tuNgay = lastChangedDateValue;
                denNgay = lastChangedDateValue;
                suppressAutoReload = true;
                dpTuNgay.setValue(lastChangedDateValue);
                dpDenNgay.setValue(lastChangedDateValue);
                suppressAutoReload = false;
            } else if (lastChangedWasTuNgay) {
                denNgay = tuNgay;
            } else {
                tuNgay = denNgay;
            }
        }

        if (tuNgay == null || denNgay == null) {
            showError("Thiếu dữ liệu lọc", "Vui lòng chọn đầy đủ từ ngày và đến ngày.");
            return;
        }
        if (tuNgay.isAfter(denNgay)) {
            showError("Khoảng thời gian không hợp lệ", "Từ ngày không được lớn hơn đến ngày.");
            return;
        }

        String loaiBan = mapLoaiBanToDb(cbLoaiBan.getValue());
        String hinhThuc = mapHinhThucToDb(cbHinhThuc.getValue());

        final LocalDate reportTuNgay = tuNgay;
        final LocalDate reportDenNgay = denNgay;
        final String reportLoaiBan = loaiBan;
        final String reportHinhThuc = hinhThuc;

        // Lưu trữ bộ lọc hiện tại để export
        currentTuNgay = reportTuNgay;
        currentDenNgay = reportDenNgay;
        currentLoaiBan = cbLoaiBan.getValue();
        currentHinhThuc = cbHinhThuc.getValue();

        new Thread(() -> {
            try {
                // Lấy dữ liệu từ DAO
            double tongDoanhThu = daoThongKe.getTongDoanhThu(reportTuNgay, reportDenNgay, reportLoaiBan, reportHinhThuc);
            int tongDon = daoThongKe.getTongDonHang(reportTuNgay, reportDenNgay, reportLoaiBan, reportHinhThuc);
            double giaTrungBinh = daoThongKe.getGiaTrungBinh(reportTuNgay, reportDenNgay, reportLoaiBan, reportHinhThuc);
            int soKH = daoThongKe.getSoKhachHang(reportTuNgay, reportDenNgay, reportLoaiBan, reportHinhThuc);

            List<Map<String, Object>> dataTheoNgay = daoThongKe.getDoanhThuTheoNgay(reportTuNgay, reportDenNgay, reportLoaiBan, reportHinhThuc);
            List<Map<String, Object>> dataCoCau = daoThongKe.getCoCAuDoanhThu(reportTuNgay, reportDenNgay, reportLoaiBan, reportHinhThuc);
            List<Map<String, Object>> dataTopSanPham = daoThongKe.getTopSanPham(reportTuNgay, reportDenNgay, reportLoaiBan, reportHinhThuc, 10);
            List<Map<String, Object>> topKH = daoThongKe.getTopKhachHang(reportTuNgay, reportDenNgay, reportLoaiBan, reportHinhThuc, 5);
                List<Map<String, Object>> productDead = daoThongKe.getProductDead(90);
            List<Map<String, Object>> thongKeHinhThuc = daoThongKe.getThongKeHinhThuc(reportTuNgay, reportDenNgay, reportLoaiBan, reportHinhThuc);

                // Lưu trữ dữ liệu cho export
                currentTongDoanhThu = tongDoanhThu;
                currentTongDon = tongDon;
                currentGiaTrungBinh = giaTrungBinh;
                currentSoKhachHang = soKH;
                currentDataTheoNgay = dataTheoNgay;
                currentDataCoCau = dataCoCau;
                currentTopKhachHang = topKH;
                currentProductDead = productDead;
                currentThongKeHinhThuc = thongKeHinhThuc;

                // Dữ liệu kỳ trước: cùng độ dài khoảng thời gian liền trước kỳ đang xem
                long days = ChronoUnit.DAYS.between(reportTuNgay, reportDenNgay) + 1;
                LocalDate prevDenNgay = reportTuNgay.minusDays(1);
                LocalDate prevTuNgay = prevDenNgay.minusDays(days - 1);

                double prevTongDoanhThu = daoThongKe.getTongDoanhThu(prevTuNgay, prevDenNgay, reportLoaiBan, reportHinhThuc);
                int prevTongDon = daoThongKe.getTongDonHang(prevTuNgay, prevDenNgay, reportLoaiBan, reportHinhThuc);
                double prevGiaTrungBinh = daoThongKe.getGiaTrungBinh(prevTuNgay, prevDenNgay, reportLoaiBan, reportHinhThuc);
                int prevSoKH = daoThongKe.getSoKhachHang(prevTuNgay, prevDenNgay, reportLoaiBan, reportHinhThuc);

                // Tính phần trăm thay đổi
                double percDoanhThu = calculatePercent(tongDoanhThu, prevTongDoanhThu);
                double percDon = calculatePercent(tongDon, prevTongDon);
                double percGia = calculatePercent(giaTrungBinh, prevGiaTrungBinh);
                double percKH = calculatePercent(soKH, prevSoKH);

                // Update UI
                Platform.runLater(() -> {
                    lblTongDoanhThu.setText(dfCurrency.format(tongDoanhThu) + " ₫");
                    lblTongDoanhThuPercent.setText(formatPercent(percDoanhThu));
                    updatePercentStyle(lblTongDoanhThuPercent, percDoanhThu);

                    lblTongDonHang.setText(tongDon + " đơn");
                    lblTongDonHangPercent.setText(formatPercent(percDon));
                    updatePercentStyle(lblTongDonHangPercent, percDon);

                    lblGiaTrungBinh.setText(dfCurrency.format(giaTrungBinh) + " ₫");
                    lblGiaTrungBinhPercent.setText(formatPercent(percGia));
                    updatePercentStyle(lblGiaTrungBinhPercent, percGia);

                    lblSoKhachHang.setText(soKH + " khách");
                    lblSoKhachHangPercent.setText(formatPercent(percKH));
                    updatePercentStyle(lblSoKhachHangPercent, percKH);

                    // Load biểu đồ
                    loadCharts(reportTuNgay, reportDenNgay, dataTheoNgay, dataCoCau, dataTopSanPham);
                    loadTables(topKH, productDead);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Không thể tải dữ liệu", "Đã xảy ra lỗi khi tải trang thống kê doanh thu."));
            }
        }).start();
    }

    private void loadCharts(LocalDate tuNgay,
                            LocalDate denNgay,
                            List<Map<String, Object>> dataTheoNgay,
                            List<Map<String, Object>> dataCoCau,
                            List<Map<String, Object>> dataTopSanPham) {
        loadChartThoiGian(tuNgay, denNgay, dataTheoNgay);
        loadChartCoCAu(dataCoCau);
        loadChartTopSanPham(dataTopSanPham);
    }

    private void loadChartThoiGian(LocalDate tuNgay, LocalDate denNgay, List<Map<String, Object>> data) {
        chartThoiGian.getData().clear();
        chartThoiGian.setCreateSymbols(true);
        yAxisThoiGian.setLabel("Doanh thu (VND)");
        xAxisThoiGian.setLabel(buildRangeLabel(tuNgay, denNgay));

        Map<LocalDate, Double> doanhThuByNgay = new HashMap<>();
        for (Map<String, Object> row : data) {
            LocalDate ngay = (LocalDate) row.get("ngay");
            double doanhThu = ((Number) row.get("doanhThu")).doubleValue();
            doanhThuByNgay.put(ngay, doanhThu);
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh Thu");
        Map<String, Double> doanhThuByLabel = new HashMap<>();

        LocalDate cursor = tuNgay;
        while (!cursor.isAfter(denNgay)) {
            double doanhThu = doanhThuByNgay.getOrDefault(cursor, 0.0);
            String xLabel = cursor.format(DateTimeFormatter.ofPattern("dd/MM"));
            doanhThuByLabel.put(xLabel, doanhThu);
            series.getData().add(new XYChart.Data<>(xLabel, doanhThu / 1_000_000));
            cursor = cursor.plusDays(1);
        }

        chartThoiGian.getData().add(series);
        Platform.runLater(() -> installRevenueTooltips(series, doanhThuByLabel));
    }

    private void installRevenueTooltips(XYChart.Series<String, Number> series, Map<String, Double> doanhThuByLabel) {
        for (XYChart.Data<String, Number> point : series.getData()) {
            String xLabel = point.getXValue();
            Double doanhThu = doanhThuByLabel.get(xLabel);
            if (doanhThu == null) {
                continue;
            }

            Popup popup = createRevenuePopup(xLabel, doanhThu);
            if (point.getNode() != null) {
                attachImmediatePopup(point.getNode(), popup);
            } else {
                point.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        attachImmediatePopup(newNode, popup);
                    }
                });
            }
        }
    }

    private Popup createRevenuePopup(String xLabel, double doanhThu) {
        Label valueLabel = new Label(xLabel + "\n" + dfCurrency.format(doanhThu) + " ₫");
        valueLabel.setStyle("-fx-background-color: rgba(255,255,255,0.98); -fx-border-color: #f97316; -fx-border-width: 1.5; -fx-border-radius: 8; -fx-background-radius: 8; -fx-text-fill: #0f172a; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 6 10 6 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 8, 0, 0, 2);");
        valueLabel.setMouseTransparent(true);

        Popup popup = new Popup();
        popup.setAutoHide(false);
        popup.setHideOnEscape(false);
        popup.getContent().add(new StackPane(valueLabel));
        return popup;
    }

    private void attachImmediatePopup(Node node, Popup popup) {
        node.setOnMouseEntered(event -> {
            Bounds bounds = node.localToScreen(node.getBoundsInLocal());
            if (bounds != null) {
                if (!popup.isShowing()) {
                    popup.show(node, bounds.getMinX() + bounds.getWidth() / 2, bounds.getMinY() - 42);
                }
            }
        });
        node.setOnMouseExited(event -> popup.hide());
    }

    private String buildRangeLabel(LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay.equals(denNgay)) {
            return "Ngày " + tuNgay.format(DateTimeFormatter.ofPattern("dd/MM"));
        }
        return "Ngày " + tuNgay.format(DateTimeFormatter.ofPattern("dd/MM"))
                + " - " + denNgay.format(DateTimeFormatter.ofPattern("dd/MM"));
    }

    private void loadChartCoCAu(List<Map<String, Object>> data) {
        chartCoCAu.getData().clear();

        for (Map<String, Object> row : data) {
            String nhom = (String) row.get("nhomThuoc");
            double doanhThu = ((Number) row.get("doanhThu")).doubleValue();
            chartCoCAu.getData().add(new PieChart.Data(nhom, doanhThu));
        }
    }

    private void loadChartTopSanPham(List<Map<String, Object>> data) {
        chartTopSanPham.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Số Lượng Bán");

        for (Map<String, Object> row : data) {
            String tenThuoc = (String) row.get("tenThuoc");
            int soLuong = ((Number) row.get("soLuong")).intValue();
            series.getData().add(new XYChart.Data<>(tenThuoc, soLuong));
        }

        chartTopSanPham.getData().add(series);
    }

    private void loadTables(List<Map<String, Object>> topKH, List<Map<String, Object>> productDead) {
        tableTopKhachHang.setItems(FXCollections.observableArrayList(topKH));
        tableProductDead.setItems(FXCollections.observableArrayList(productDead));
    }

    @FXML
    private void handleTimKiem() {
        loadDataThongKe();
    }

    @FXML
    private void handleXoaBoLoc() {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = YearMonth.now().atDay(1);

        suppressAutoReload = true;
        dpTuNgay.setValue(firstDayOfMonth);
        dpDenNgay.setValue(today);
        cbLoaiBan.setValue("Tất cả");
        cbHinhThuc.setValue("Tất cả");
        cbKhoangNhanh.setValue("Tùy chọn");
        suppressAutoReload = false;

        loadDataThongKe();
    }

    @FXML
    private void handleXuatExcel() {
        try {
            if (currentTuNgay == null || currentDenNgay == null) {
                showError("Không có dữ liệu", "Vui lòng tải dữ liệu trước khi xuất.");
                return;
            }

            String loaiBanDb = mapLoaiBanToDb(currentLoaiBan);
            String hinhThucDb = mapHinhThucToDb(currentHinhThuc);

            String filePath = ThongKeDoanhThuExcelExporter.xuatExcel(
                    currentTuNgay,
                    currentDenNgay,
                    loaiBanDb,
                    hinhThucDb,
                    currentTongDoanhThu,
                    currentTongDon,
                    currentGiaTrungBinh,
                    currentSoKhachHang,
                    currentDataTheoNgay,
                    currentDataCoCau,
                    currentTopKhachHang,
                    currentProductDead,
                    currentThongKeHinhThuc
            );

            // Mở file
            openFile(filePath);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Xuất Excel thành công");
            alert.setHeaderText("Thành công");
            alert.setContentText("File báo cáo đã được lưu tại:\n" + filePath);
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi xuất Excel", "Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    @FXML
    private void handleInBaoCao() {
        try {
            if (currentTuNgay == null || currentDenNgay == null) {
                showError("Không có dữ liệu", "Vui lòng tải dữ liệu trước khi in báo cáo.");
                return;
            }

            String loaiBanDb = mapLoaiBanToDb(currentLoaiBan);
            String hinhThucDb = mapHinhThucToDb(currentHinhThuc);

            String filePath = ThongKeDoanhThuPdfExporter.xuatPDF(
                    currentTuNgay,
                    currentDenNgay,
                    loaiBanDb,
                    hinhThucDb,
                    currentTongDoanhThu,
                    currentTongDon,
                    currentGiaTrungBinh,
                    currentSoKhachHang,
                    currentDataTheoNgay,
                    currentDataCoCau,
                    currentTopKhachHang,
                    currentProductDead,
                    currentThongKeHinhThuc
            );

            // Mở file PDF
            openFile(filePath);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("In báo cáo thành công");
            alert.setHeaderText("Thành công");
            alert.setContentText("File báo cáo PDF đã được tạo:\n" + filePath + "\n\nFile sẽ được mở để bạn có thể in.");
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi in báo cáo", "Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    private void openFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Tính phần trăm thay đổi so với kỳ trước
     */
    private double calculatePercent(double current, double previous) {
        if (previous == 0) {
			return 0;
		}
        return ((current - previous) / previous) * 100;
    }

    /**
     * Định dạng phần trăm với dấu + hoặc -
     */
    private String formatPercent(double percent) {
        if (percent >= 0) {
            return String.format("+%.1f%%", percent);
        } else {
            return String.format("%.1f%%", percent);
        }
    }

    /**
     * Cập nhật style cho label phần trăm
     */
    private void updatePercentStyle(Label label, double percent) {
        label.getStyleClass().removeAll("kpi-percent-up", "kpi-percent-down");
        if (percent >= 0) {
            label.getStyleClass().add("kpi-percent-up");
        } else {
            label.getStyleClass().add("kpi-percent-down");
        }
    }

    private String mapLoaiBanToDb(String value) {
        if (value == null || "Tất cả".equalsIgnoreCase(value)) {
            return null;
        }
        if ("Bán theo đơn".equalsIgnoreCase(value)) {
            return "BAN_THEO_DON";
        }
        if ("Bán lẻ".equalsIgnoreCase(value)) {
            return "BAN_LE";
        }
        return value;
    }

    private String mapHinhThucToDb(String value) {
        if (value == null || "Tất cả".equalsIgnoreCase(value)) {
            return null;
        }
        if ("Tiền mặt".equalsIgnoreCase(value)) {
            return "TIEN_MAT";
        }
        if ("Chuyển khoản".equalsIgnoreCase(value)) {
            return "CHUYEN_KHOAN";
        }
        if ("Thẻ".equalsIgnoreCase(value) || "Thẻ tín dụng".equalsIgnoreCase(value)) {
            return "THE";
        }
        return value;
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Thống kê doanh thu");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
