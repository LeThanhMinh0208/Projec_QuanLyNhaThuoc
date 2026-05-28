package gui.main;

import java.awt.Desktop;
import java.io.File;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import dao.DAO_ThongKeTonKho;
import dao.DAO_ThongKeHangHoa;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import utils.ThongKeHangHoaExcelExporter;
import utils.ThongKeHangHoaPdfExporter;

public class GUI_ThongKeHangHoaController {

    @FXML private DatePicker dpTuNgay, dpDenNgay;
    @FXML private ComboBox<String> cbKhoangNhanh, cbDanhMuc;

    @FXML private Label lblTongMatHang, lblTongMatHangPercent;
    @FXML private Label lblTongSoLuongBan, lblDoanhThuGoc, lblDoanhThuSauThue;

    // Biểu đồ 1: PieChart - Cơ cấu số lượng tiêu thụ theo danh mục
    @FXML private PieChart chartCoCauSoLuong;

    // Biểu đồ 2: BarChart - Top 10 sản phẩm bán ít nhất
    @FXML private BarChart<String, Number> chartBanItNhat;

    // Biểu đồ 3: BarChart - Top 10 sản phẩm bị đổi/trả nhiều nhất
    @FXML private BarChart<String, Number> chartDoiTra;

    // Bảng 1: Sản phẩm bán chạy
    @FXML private TableView<Map<String, Object>> tableBanChay;
    @FXML private TableColumn<Map<String, Object>, Integer> colSTT1, colSoLuongBan1;
    @FXML private TableColumn<Map<String, Object>, String> colMaThuoc1, colTenThuoc1, colDanhMuc1, colDonVi1;

    // Bảng 2: Sản phẩm doanh thu cao
    @FXML private TableView<Map<String, Object>> tableDoanhThuCao;
    @FXML private TableColumn<Map<String, Object>, Integer> colSTT2;
    @FXML private TableColumn<Map<String, Object>, String> colMaThuoc2, colTenThuoc2, colDanhMuc2, colDonVi2;
    @FXML private TableColumn<Map<String, Object>, Double> colDoanhThu2;

    private final DAO_ThongKeHangHoa daoHangHoa = new DAO_ThongKeHangHoa();
    private final DAO_ThongKeTonKho daoTonKho = new DAO_ThongKeTonKho();
    private final DecimalFormat df = new DecimalFormat("#,##0");
    private boolean suppressAutoReload = false;

    // Thread management để tránh race condition
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private AtomicLong lastRequestId = new AtomicLong(0);
    private boolean isActive = true;

    // Cache dữ liệu cho xuất báo cáo
    private LocalDate currentTuNgay;
    private LocalDate currentDenNgay;
    private String currentDanhMuc;
    private Map<String, Object> currentTongQuan;
    private List<Map<String, Object>> currentCoCauSoLuong;
    private List<Map<String, Object>> currentTopSanPham;
    private List<Map<String, Object>> currentChamLC;
    private List<Map<String, Object>> currentDoiTra;

    @FXML
    public void initialize() {
        LocalDate today = LocalDate.now();
        suppressAutoReload = true;
        dpTuNgay.setValue(YearMonth.now().atDay(1));
        dpDenNgay.setValue(today);

        cbKhoangNhanh.setItems(FXCollections.observableArrayList("Tùy chọn", "Trong ngày", "7 ngày qua", "30 ngày qua", "Tháng này"));
        cbKhoangNhanh.setValue("Tháng này");
        cbDanhMuc.setItems(FXCollections.observableArrayList(daoTonKho.getDanhMucThuoc()));
        cbDanhMuc.setValue("Tất cả");

        setupTables();
        setupAutoReload();

        suppressAutoReload = false;
        setLoadingPlaceholders();
        loadDataThongKe();
    }

    public void stopBackgroundTasks() {
        isActive = false;
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }

    private void setLoadingPlaceholders() {
        if (lblTongMatHang != null)    lblTongMatHang.setText("---");
        if (lblTongMatHangPercent != null) lblTongMatHangPercent.setText("");
        if (lblTongSoLuongBan != null) lblTongSoLuongBan.setText("---");
        if (lblDoanhThuGoc != null)    lblDoanhThuGoc.setText("---");
        if (lblDoanhThuSauThue != null) lblDoanhThuSauThue.setText("---");
    }

    private void setupTables() {
        // Bảng 1: Bán chạy
        colSTT1.setCellValueFactory(c -> intProp(c.getValue(), "stt"));
        colMaThuoc1.setCellValueFactory(c -> strProp(c.getValue(), "maThuoc"));
        colTenThuoc1.setCellValueFactory(c -> strProp(c.getValue(), "tenThuoc"));
        colDanhMuc1.setCellValueFactory(c -> strProp(c.getValue(), "tenDanhMuc"));
        colDonVi1.setCellValueFactory(c -> strProp(c.getValue(), "donVi"));
        colSoLuongBan1.setCellValueFactory(c -> intProp(c.getValue(), "soLuongBan"));

        // Bảng 2: Doanh thu cao
        colSTT2.setCellValueFactory(c -> intProp(c.getValue(), "stt"));
        colMaThuoc2.setCellValueFactory(c -> strProp(c.getValue(), "maThuoc"));
        colTenThuoc2.setCellValueFactory(c -> strProp(c.getValue(), "tenThuoc"));
        colDanhMuc2.setCellValueFactory(c -> strProp(c.getValue(), "tenDanhMuc"));
        colDonVi2.setCellValueFactory(c -> strProp(c.getValue(), "donVi"));
        colDoanhThu2.setCellValueFactory(c -> doubleProp(c.getValue(), "doanhThu"));
        colDoanhThu2.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : df.format(item) + " đ");
            }
        });
    }

    private void setupAutoReload() {
        dpTuNgay.valueProperty().addListener((obs, old, nv) -> onDateChanged());
        dpDenNgay.valueProperty().addListener((obs, old, nv) -> onDateChanged());
        cbDanhMuc.valueProperty().addListener((obs, old, nv) -> {
            if (!suppressAutoReload) loadDataThongKe();
        });
        cbKhoangNhanh.valueProperty().addListener((obs, old, nv) -> {
            if (!suppressAutoReload) applyQuickRange(nv);
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
        if (quickRange == null || "Tùy chọn".equals(quickRange)) return;
        LocalDate today = LocalDate.now();
        LocalDate fromDate = today;

        switch (quickRange) {
            case "Trong ngày": fromDate = today; break;
            case "7 ngày qua": fromDate = today.minusDays(6); break;
            case "30 ngày qua": fromDate = today.minusDays(29); break;
            case "Tháng này": fromDate = YearMonth.now().atDay(1); break;
        }

        suppressAutoReload = true;
        dpTuNgay.setValue(fromDate);
        dpDenNgay.setValue(today);
        suppressAutoReload = false;
        loadDataThongKe();
    }

    @FXML
    private void loadDataThongKe() {
        LocalDate tuNgay = dpTuNgay.getValue();
        LocalDate denNgay = dpDenNgay.getValue();
        if (tuNgay == null || denNgay == null) return;

        if (tuNgay.isAfter(denNgay)) {
            suppressAutoReload = true;
            dpDenNgay.setValue(tuNgay);
            suppressAutoReload = false;
            denNgay = tuNgay;
        }

        final LocalDate reportTu = tuNgay;
        final LocalDate reportDen = denNgay;
        final String reportDanhMuc = cbDanhMuc.getValue();
        final long requestId = lastRequestId.incrementAndGet();

        executorService.submit(() -> {
            try {
                // Load song song 5 truy vấn
                Map<String, Object> tongQuan      = daoHangHoa.getTongQuan(reportTu, reportDen, reportDanhMuc);
                List<Map<String, Object>> coCauSL = daoHangHoa.getSoLuongTieuThuTheoDanhMuc(reportTu, reportDen, reportDanhMuc);
                List<Map<String, Object>> chamLC   = daoHangHoa.getTopSanPhamBanItNhat(reportTu, reportDen, reportDanhMuc, 10);
                List<Map<String, Object>> doiTra   = daoHangHoa.getTopSanPhamBiDoiTra(reportTu, reportDen, 10);
                List<Map<String, Object>> topSP    = daoHangHoa.getTopSanPhamBanChay(reportTu, reportDen, reportDanhMuc, 10);

                if (requestId != lastRequestId.get() || !isActive) return;

                currentTuNgay      = reportTu;
                currentDenNgay     = reportDen;
                currentDanhMuc     = reportDanhMuc;
                currentTongQuan    = tongQuan;
                currentCoCauSoLuong = coCauSL;
                currentTopSanPham  = topSP;
                currentChamLC      = chamLC;
                currentDoiTra      = doiTra;

                Platform.runLater(() -> {
                    if (requestId != lastRequestId.get() || !isActive) return;

                    // ─── KPI Cards ───
                    lblTongMatHang.setText(df.format(tongQuan.get("tongMatHangDaBan")));
                    lblTongSoLuongBan.setText(df.format(tongQuan.get("tongSoLuongBan")));
                    lblDoanhThuGoc.setText(df.format(tongQuan.get("tongDoanhThuGoc")) + " đ");
                    lblDoanhThuSauThue.setText(df.format(tongQuan.get("tongDoanhThuSauThue")) + " đ");

                    // ─── Biểu đồ 1: PieChart - Cơ cấu số lượng tiêu thụ theo danh mục ───
                    chartCoCauSoLuong.getData().clear();
                    if (coCauSL != null && !coCauSL.isEmpty()) {
                        chartCoCauSoLuong.setVisible(true);
                        for (Map<String, Object> row : coCauSL) {
                            int soLuong = toInt(row.get("soLuong"));
                            if (soLuong > 0) {
                                String tenDanhMuc = String.valueOf(row.get("tenDanhMuc"));
                                chartCoCauSoLuong.getData().add(new PieChart.Data(tenDanhMuc, soLuong));
                            }
                        }
                    } else {
                        chartCoCauSoLuong.setVisible(false);
                    }

                    // ─── Biểu đồ 2: BarChart - Top 10 sản phẩm bán ít nhất ───
                    chartBanItNhat.getData().clear();
                    if (chartBanItNhat.getXAxis() instanceof CategoryAxis) {
                        ((CategoryAxis) chartBanItNhat.getXAxis()).setTickLabelRotation(40);
                    }
                    XYChart.Series<String, Number> seriesBanIt = new XYChart.Series<>();
                    seriesBanIt.setName("Số Lượng Bán");
                    for (Map<String, Object> row : chamLC) {
                        String tenSP = String.valueOf(row.get("tenThuoc"));
                        if (tenSP.length() > 14) tenSP = tenSP.substring(0, 14) + "…";
                        seriesBanIt.getData().add(new XYChart.Data<>(tenSP, toInt(row.get("soLuongBan"))));
                    }
                    chartBanItNhat.getData().add(seriesBanIt);

                    // ─── Biểu đồ 3: BarChart - Top 10 sản phẩm bị đổi/trả nhiều nhất ───
                    chartDoiTra.getData().clear();
                    if (chartDoiTra.getXAxis() instanceof CategoryAxis) {
                        ((CategoryAxis) chartDoiTra.getXAxis()).setTickLabelRotation(40);
                    }
                    XYChart.Series<String, Number> seriesDoiTra = new XYChart.Series<>();
                    seriesDoiTra.setName("Số Lượng Đổi/Trả");
                    if (doiTra != null && !doiTra.isEmpty()) {
                        for (Map<String, Object> row : doiTra) {
                            String tenSP = String.valueOf(row.get("tenThuoc"));
                            if (tenSP.length() > 14) tenSP = tenSP.substring(0, 14) + "…";
                            seriesDoiTra.getData().add(new XYChart.Data<>(tenSP, toInt(row.get("soLuongDoiTra"))));
                        }
                    }
                    chartDoiTra.getData().add(seriesDoiTra);

                    // ─── Bảng chi tiết ───
                    tableBanChay.setItems(FXCollections.observableArrayList(topSP));

                    List<Map<String, Object>> topDoanhThu = new ArrayList<>(topSP);
                    topDoanhThu.sort((a, b) -> Double.compare(toDouble(b.get("doanhThu")), toDouble(a.get("doanhThu"))));
                    tableDoanhThuCao.setItems(FXCollections.observableArrayList(topDoanhThu));
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Lỗi hiển thị", "Không thể hiển thị đầy đủ các khối biểu đồ thống kê hàng hóa."));
            }
        });
    }

    @FXML
    private void handleXoaBoLoc() {
        suppressAutoReload = true;
        dpTuNgay.setValue(YearMonth.now().atDay(1));
        dpDenNgay.setValue(LocalDate.now());
        cbKhoangNhanh.setValue("Tháng này");
        cbDanhMuc.setValue("Tất cả");
        suppressAutoReload = false;
        loadDataThongKe();
    }

    @FXML
    private void handleXuatExcel() {
        try {
            if (currentTongQuan == null) {
                showError("Cảnh báo", "Vui lòng đợi hệ thống tải dữ liệu trước khi xuất file Excel.");
                return;
            }
            String filePath = ThongKeHangHoaExcelExporter.xuatExcel(
                    currentTuNgay, currentDenNgay, currentDanhMuc,
                    currentTongQuan, currentCoCauSoLuong, currentTopSanPham,
                    currentChamLC, currentDoiTra);
            openFile(filePath);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi xuất Excel", "Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    @FXML
    private void handleInBaoCao() {
        try {
            if (currentTongQuan == null) {
                showError("Cảnh báo", "Vui lòng đợi hệ thống tải dữ liệu trước khi trích xuất PDF.");
                return;
            }
            String filePath = ThongKeHangHoaPdfExporter.xuatPDF(
                    currentTuNgay, currentDenNgay, currentDanhMuc,
                    currentTongQuan, currentCoCauSoLuong, currentTopSanPham,
                    currentChamLC, currentDoiTra);
            openFile(filePath);
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

    private int toInt(Object value) { return value instanceof Number ? ((Number) value).intValue() : 0; }
    private double toDouble(Object value) { return value instanceof Number ? ((Number) value).doubleValue() : 0.0; }
    private SimpleObjectProperty<String> strProp(Map<String, Object> row, String key) { return new SimpleObjectProperty<>(row.get(key) == null ? "" : String.valueOf(row.get(key))); }
    private SimpleObjectProperty<Integer> intProp(Map<String, Object> row, String key) { return new SimpleObjectProperty<>(toInt(row.get(key))); }
    private SimpleObjectProperty<Double> doubleProp(Map<String, Object> row, String key) { return new SimpleObjectProperty<>(toDouble(row.get(key))); }

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Thống kê hàng hóa");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}