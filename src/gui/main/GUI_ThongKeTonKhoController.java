package gui.main;

import java.awt.Desktop;
import java.io.File;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import dao.DAO_ThongKeTonKho;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
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
import utils.ThongKeTonKhoExcelExporter;
import utils.ThongKeTonKhoPdfExporter;

public class GUI_ThongKeTonKhoController {

    @FXML private DatePicker dpTuNgay, dpDenNgay;
    @FXML private ComboBox<String> cbKhoangNhanh, cbDanhMuc, cbTrangThaiTon;

    @FXML private Label lblTongMatHang, lblTongMatHangPercent;
    @FXML private Label lblTongSoLuongTon, lblTongSoLuongTonPercent;
    @FXML private Label lblTongGiaTriTon, lblTongGiaTriTonPercent;
    @FXML private Label lblCanhBaoTon, lblCanhBaoTonPercent;
    @FXML private Label lblChartBienDongTitle;

    @FXML private LineChart<String, Number> chartBienDong;
    @FXML private CategoryAxis xAxisBienDong;
    @FXML private NumberAxis yAxisBienDong;
    @FXML private PieChart chartCoCauTon;
    @FXML private BarChart<String, Number> chartTopTonKho;
    
    @FXML private BarChart<String, Number> chartCoCauTonCot;
    @FXML private CategoryAxis xAxisCoCauTonCot;
    @FXML private NumberAxis yAxisCoCauTonCot;

    @FXML private TableView<Map<String, Object>> tableTopTonKho;
    @FXML private TableColumn<Map<String, Object>, Integer> colSTTTop;
    @FXML private TableColumn<Map<String, Object>, String> colMaThuocTop, colTenThuocTop, colDanhMucTop, colDonViTop;
    @FXML private TableColumn<Map<String, Object>, Integer> colTonTop, colSoLoTop;
    @FXML private TableColumn<Map<String, Object>, Double> colGiaTriTop;

    @FXML private TableView<Map<String, Object>> tableLoSapHetHan;
    @FXML private TableColumn<Map<String, Object>, Integer> colSTTHsd;
    @FXML private TableColumn<Map<String, Object>, String> colMaLoHsd, colTenThuocHsd, colDanhMucHsd, colHanDungHsd;
    @FXML private TableColumn<Map<String, Object>, Integer> colConLaiHsd, colTonHsd;

    private final DAO_ThongKeTonKho dao = new DAO_ThongKeTonKho();
    private final DecimalFormat df = new DecimalFormat("#,##0");
    private boolean suppressAutoReload = false;

    // Thread management để tránh race condition
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private AtomicLong lastRequestId = new AtomicLong(0);
    private boolean isActive = true;

    private LocalDate currentTuNgay;
    private LocalDate currentDenNgay;
    private String currentDanhMuc;
    private String currentTrangThaiTon;
    private Map<String, Object> currentTongQuan;
    private List<Map<String, Object>> currentTonKhoTheoDanhMuc;
    private List<Map<String, Object>> currentBienDongTonKho;
    private List<Map<String, Object>> currentTopTonKho;
    private List<Map<String, Object>> currentLoSapHetHan;

    @FXML
    public void initialize() {
        LocalDate today = LocalDate.now();
        suppressAutoReload = true;
        dpTuNgay.setValue(YearMonth.now().atDay(1));
        dpDenNgay.setValue(today);

        cbKhoangNhanh.setItems(FXCollections.observableArrayList("Tùy chọn", "Trong ngày", "7 ngày qua", "30 ngày qua", "Tháng này"));
        cbKhoangNhanh.setValue("Tháng này");
        cbTrangThaiTon.setItems(FXCollections.observableArrayList("Tất cả", "Còn hàng", "Tồn thấp", "Hết hàng"));
        cbTrangThaiTon.setValue("Tất cả");
        cbDanhMuc.setItems(FXCollections.observableArrayList(dao.getDanhMucThuoc()));
        cbDanhMuc.setValue("Tất cả");

        setupTables();
        setupAutoReload();
        suppressAutoReload = false;
        // Đặt placeholder "---" để tránh nháy khi mới load trang
        setLoadingPlaceholders();
        loadDataThongKe();
    }

    /**
     * Dừng tất cả background tasks khi controller bị thay thế.
     * Được gọi từ SceneUtils trước khi load trang mới.
     */
    public void stopBackgroundTasks() {
        isActive = false;
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }

    /**
     * Đặt placeholder "---" cho tất cả KPI labels trước khi dữ liệu được tải.
     * Tránh hiện số 0 mặc định gây nháy giao diện.
     */
    private void setLoadingPlaceholders() {
        if (lblTongMatHang != null)     lblTongMatHang.setText("---");
        if (lblTongMatHangPercent != null) lblTongMatHangPercent.setText("");
        if (lblTongSoLuongTon != null)  lblTongSoLuongTon.setText("---");
        if (lblTongSoLuongTonPercent != null) lblTongSoLuongTonPercent.setText("");
        if (lblTongGiaTriTon != null)   lblTongGiaTriTon.setText("---");
        if (lblTongGiaTriTonPercent != null) lblTongGiaTriTonPercent.setText("");
        if (lblCanhBaoTon != null)      lblCanhBaoTon.setText("---");
        if (lblCanhBaoTonPercent != null) lblCanhBaoTonPercent.setText("");
    }

    private void setupAutoReload() {
        dpTuNgay.valueProperty().addListener((obs, oldValue, newValue) -> onDateChanged());
        dpDenNgay.valueProperty().addListener((obs, oldValue, newValue) -> onDateChanged());
        cbDanhMuc.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!suppressAutoReload) loadDataThongKe();
        });
        cbTrangThaiTon.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!suppressAutoReload) loadDataThongKe();
        });
        cbKhoangNhanh.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!suppressAutoReload) applyQuickRange(newValue);
        });
    }

    private void onDateChanged() {
        if (suppressAutoReload) return;
        suppressAutoReload = true;
        cbKhoangNhanh.setValue("Tùy chọn");
        suppressAutoReload = false;
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
            case "Tháng này":
                fromDate = YearMonth.now().atDay(1);
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

    private void setupTables() {
        colSTTTop.setCellValueFactory(c -> intProp(c.getValue(), "stt"));
        colMaThuocTop.setCellValueFactory(c -> strProp(c.getValue(), "maThuoc"));
        colTenThuocTop.setCellValueFactory(c -> strProp(c.getValue(), "tenThuoc"));
        colDanhMucTop.setCellValueFactory(c -> strProp(c.getValue(), "tenDanhMuc"));
        colTonTop.setCellValueFactory(c -> intProp(c.getValue(), "soLuongTon"));
        colDonViTop.setCellValueFactory(c -> strProp(c.getValue(), "donViCoBan"));
        colSoLoTop.setCellValueFactory(c -> intProp(c.getValue(), "soLo"));
        colGiaTriTop.setCellValueFactory(c -> doubleProp(c.getValue(), "giaTriTon"));
        colGiaTriTop.setCellFactory(column -> moneyCell());
        tableTopTonKho.setRowFactory(tv -> statRow());

        colSTTHsd.setCellValueFactory(c -> intProp(c.getValue(), "stt"));
        colMaLoHsd.setCellValueFactory(c -> strProp(c.getValue(), "maLoThuoc"));
        colTenThuocHsd.setCellValueFactory(c -> strProp(c.getValue(), "tenThuoc"));
        colDanhMucHsd.setCellValueFactory(c -> strProp(c.getValue(), "tenDanhMuc"));
        colHanDungHsd.setCellValueFactory(c -> strProp(c.getValue(), "hanSuDung"));
        colConLaiHsd.setCellValueFactory(c -> intProp(c.getValue(), "soNgayConLai"));
        colTonHsd.setCellValueFactory(c -> intProp(c.getValue(), "soLuongTon"));
        tableLoSapHetHan.setRowFactory(tv -> statRow());
    }

    private SimpleObjectProperty<String> strProp(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return new SimpleObjectProperty<>(value == null ? "" : String.valueOf(value));
    }

    private SimpleObjectProperty<Integer> intProp(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return new SimpleObjectProperty<>(value instanceof Number ? ((Number) value).intValue() : 0);
    }

    private SimpleObjectProperty<Double> doubleProp(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return new SimpleObjectProperty<>(value instanceof Number ? ((Number) value).doubleValue() : 0);
    }

    private TableCell<Map<String, Object>, Double> moneyCell() {
        return new TableCell<Map<String, Object>, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : df.format(item) + " đ");
            }
        };
    }

    private TableRow<Map<String, Object>> statRow() {
        return new TableRow<Map<String, Object>>() {
            @Override
            protected void updateItem(Map<String, Object> item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("stat-table-row");
                if (!empty) getStyleClass().add("stat-table-row");
            }
        };
    }

    @FXML
    private void loadDataThongKe() {
        LocalDate tuNgay = dpTuNgay.getValue();
        LocalDate denNgay = dpDenNgay.getValue();
        if (tuNgay == null || denNgay == null) {
            showError("Thiếu dữ liệu lọc", "Vui lòng chọn đầy đủ từ ngày và đến ngày.");
            return;
        }
        if (tuNgay.isAfter(denNgay)) {
            suppressAutoReload = true;
            dpDenNgay.setValue(tuNgay);
            suppressAutoReload = false;
            denNgay = tuNgay;
        }

        final LocalDate reportTuNgay = tuNgay;
        final LocalDate reportDenNgay = denNgay;
        final String reportDanhMuc = cbDanhMuc.getValue();
        final String reportTrangThaiTon = cbTrangThaiTon.getValue();
        final int nguongTonThap = 20;
        final int soNgayHetHan = 90;

        currentTuNgay = reportTuNgay;
        currentDenNgay = reportDenNgay;
        currentDanhMuc = reportDanhMuc;
        currentTrangThaiTon = reportTrangThaiTon;

        // Tạo ID request để tránh race condition (chỉ request mới nhất mới update UI)
        final long requestId = lastRequestId.incrementAndGet();

        executorService.submit(() -> {
            try {
                Map<String, Object> tongQuan = dao.getTongQuan(reportDanhMuc, nguongTonThap, soNgayHetHan);
                List<Map<String, Object>> tonKhoTheoDanhMuc = dao.getTonKhoTheoDanhMuc(reportDanhMuc);
                List<Map<String, Object>> bienDong = dao.getBienDongTonKho(reportTuNgay, reportDenNgay, reportDanhMuc);
                List<Map<String, Object>> topTonKho = dao.getTopTonKho(reportDanhMuc, reportTrangThaiTon, 10, nguongTonThap);
                List<Map<String, Object>> loSapHetHan = dao.getLoSapHetHan(reportDanhMuc, soNgayHetHan, 10);

                // Kiểm tra còn là request mới nhất không
                if (requestId != lastRequestId.get() || !isActive) {
                    return;
                }

                currentTongQuan = tongQuan;
                currentTonKhoTheoDanhMuc = tonKhoTheoDanhMuc;
                currentBienDongTonKho = bienDong;
                currentTopTonKho = topTonKho;
                currentLoSapHetHan = loSapHetHan;

                Platform.runLater(() -> {
                    if (requestId != lastRequestId.get() || !isActive) return;
                    updateKpis(tongQuan);
                    loadCharts(reportTuNgay, reportDenNgay, tonKhoTheoDanhMuc, bienDong, topTonKho);
                    tableTopTonKho.setItems(FXCollections.observableArrayList(topTonKho));
                    tableLoSapHetHan.setItems(FXCollections.observableArrayList(loSapHetHan));
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Không thể tải dữ liệu", "Đã xảy ra lỗi khi tải thống kê tồn kho."));
            }
        });
    }

    private void updateKpis(Map<String, Object> tongQuan) {
        int matHang = toInt(tongQuan.get("tongMatHang"));
        int soLuong = toInt(tongQuan.get("tongSoLuongTon"));
        double giaTri = toDouble(tongQuan.get("tongGiaTriTon"));
        int tonThap = toInt(tongQuan.get("soMatHangTonThap"));
        int sapHetHan = toInt(tongQuan.get("soLoSapHetHan"));

        lblTongMatHang.setText(df.format(matHang) + " mặt hàng");
        lblTongMatHangPercent.setText(df.format(toInt(tongQuan.get("tongLoConHang"))) + " lô");
        lblTongSoLuongTon.setText(df.format(soLuong));
        lblTongSoLuongTonPercent.setText("ĐVT cơ bản");
        lblTongGiaTriTon.setText(df.format(giaTri) + " đ");
        lblTongGiaTriTonPercent.setText("Theo giá nhập");
        lblCanhBaoTon.setText(df.format(tonThap + sapHetHan) + " cảnh báo");
        lblCanhBaoTonPercent.setText(tonThap + " tồn thấp, " + sapHetHan + " sắp HSD");
    }

    private void loadCharts(LocalDate tuNgay, LocalDate denNgay,
                            List<Map<String, Object>> tonKhoTheoDanhMuc,
                            List<Map<String, Object>> bienDong,
                            List<Map<String, Object>> topTonKho) {
        loadChartCoCau(tonKhoTheoDanhMuc);
        loadChartCoCauCot(tonKhoTheoDanhMuc);
        loadChartBienDong(tuNgay, denNgay, bienDong);
        loadChartTopTonKho(topTonKho);
    }

    private void loadChartCoCau(List<Map<String, Object>> data) {
        chartCoCauTon.getData().clear();
        if (data != null && !data.isEmpty()) {
            chartCoCauTon.setVisible(true);
            for (Map<String, Object> row : data) {
                chartCoCauTon.getData().add(new PieChart.Data(String.valueOf(row.get("tenDanhMuc")), toDouble(row.get("giaTriTon"))));
            }
        } else {
            // Nếu không có dữ liệu thì ẩn PieChart (không hiển thị gì)
            chartCoCauTon.setVisible(false);
        }
    }

    private void loadChartCoCauCot(List<Map<String, Object>> data) {
        chartCoCauTonCot.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Giá Trị Tồn (đ)");
        for (Map<String, Object> row : data) {
            series.getData().add(new XYChart.Data<>(String.valueOf(row.get("tenDanhMuc")), toDouble(row.get("giaTriTon"))));
        }
        chartCoCauTonCot.getData().add(series);
    }

    private void loadChartBienDong(LocalDate tuNgay, LocalDate denNgay, List<Map<String, Object>> data) {
        chartBienDong.getData().clear();
        chartBienDong.setCreateSymbols(true);
        yAxisBienDong.setLabel("Số lượng");
        xAxisBienDong.setLabel(buildRangeLabel(tuNgay, denNgay));
        lblChartBienDongTitle.setText("Biến Động Nhập/Xuất (" + tuNgay.format(DateTimeFormatter.ofPattern("dd/MM")) + " - " + denNgay.format(DateTimeFormatter.ofPattern("dd/MM")) + ")");

        XYChart.Series<String, Number> seriesNhap = new XYChart.Series<>();
        seriesNhap.setName("Nhập");
        XYChart.Series<String, Number> seriesXuat = new XYChart.Series<>();
        seriesXuat.setName("Xuất");
        for (Map<String, Object> row : data) {
            String label = ((LocalDate) row.get("ngay")).format(DateTimeFormatter.ofPattern("dd/MM"));
            seriesNhap.getData().add(new XYChart.Data<>(label, toInt(row.get("soLuongNhap"))));
            seriesXuat.getData().add(new XYChart.Data<>(label, toInt(row.get("soLuongXuat"))));
        }
        chartBienDong.getData().addAll(seriesNhap, seriesXuat);
    }

    private void loadChartTopTonKho(List<Map<String, Object>> data) {
        chartTopTonKho.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Số lượng tồn");
        for (Map<String, Object> row : data) {
            series.getData().add(new XYChart.Data<>(String.valueOf(row.get("tenThuoc")), toInt(row.get("soLuongTon"))));
        }
        chartTopTonKho.getData().add(series);
    }

    private String buildRangeLabel(LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay.equals(denNgay)) return "Ngày " + tuNgay.format(DateTimeFormatter.ofPattern("dd/MM"));
        return "Ngày " + tuNgay.format(DateTimeFormatter.ofPattern("dd/MM")) + " - " + denNgay.format(DateTimeFormatter.ofPattern("dd/MM"));
    }

    @FXML
    private void handleXoaBoLoc() {
        suppressAutoReload = true;
        dpTuNgay.setValue(YearMonth.now().atDay(1));
        dpDenNgay.setValue(LocalDate.now());
        cbKhoangNhanh.setValue("Tháng này");
        cbDanhMuc.setValue("Tất cả");
        cbTrangThaiTon.setValue("Tất cả");
        suppressAutoReload = false;
        loadDataThongKe();
    }

    @FXML
    private void handleXuatExcel() {
        try {
            if (currentTongQuan == null) {
                showError("Không có dữ liệu", "Vui lòng tải dữ liệu trước khi xuất.");
                return;
            }
            String filePath = ThongKeTonKhoExcelExporter.xuatExcel(
                    currentTuNgay, currentDenNgay, currentDanhMuc, currentTrangThaiTon,
                    currentTongQuan, currentTonKhoTheoDanhMuc, currentBienDongTonKho,
                    currentTopTonKho, currentLoSapHetHan);
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
            if (currentTongQuan == null) {
                showError("Không có dữ liệu", "Vui lòng tải dữ liệu trước khi in báo cáo.");
                return;
            }
            String filePath = ThongKeTonKhoPdfExporter.xuatPDF(
                    currentTuNgay, currentDenNgay, currentDanhMuc, currentTrangThaiTon,
                    currentTongQuan, currentTonKhoTheoDanhMuc, currentBienDongTonKho,
                    currentTopTonKho, currentLoSapHetHan);
            openFile(filePath);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("In báo cáo thành công");
            alert.setHeaderText("Thành công");
            alert.setContentText("File báo cáo PDF đã được tạo:\n" + filePath);
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

    private int toInt(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    private double toDouble(Object value) {
        return value instanceof Number ? ((Number) value).doubleValue() : 0;
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Thống kê tồn kho");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
