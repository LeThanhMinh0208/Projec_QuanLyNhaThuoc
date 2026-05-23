package gui.main;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import dao.DAO_ThongKeHangHoa;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.MapValueFactory;
import utils.ThongKeHangHoaExcelExporter;
import utils.ThongKeHangHoaPdfExporter;

public class GUI_ThongKeHangHoaController implements Initializable {

    @FXML private DatePicker dpTuNgay;
    @FXML private DatePicker dpDenNgay;
    @FXML private ComboBox<String> cbKhoangNhanh;
    @FXML private ComboBox<String> cbDanhMuc;
    @FXML private ComboBox<String> cbTrangThaiTon;

    @FXML private Label lblTongMatHang;
    @FXML private Label lblTongSoLuongTon;
    @FXML private Label lblTongGiaTriTon;
    @FXML private Label lblCanhBaoTon;

    @FXML private LineChart<String, Number> chartBienDong;
    @FXML private PieChart chartCoCauTon;
    @FXML private BarChart<String, Number> chartTopTonKho;

    // Thay đổi từ TableView<Thuoc> sang TableView<Map> để hứng dữ liệu từ DAO
    @FXML private TableView<Map<String, Object>> tableTopTonKho;
    @FXML private TableColumn<Map, Object> colSTTTop, colTenThuocTop, colDanhMucTop, colTonTop, colGiaTriTop;

    @FXML private TableView<Map<String, Object>> tableLoSapHetHan;
    @FXML private TableColumn<Map, Object> colSTTHsd, colTenThuocHsd, colDanhMucHsd, colConLaiHsd, colTonHsd;

    private final DAO_ThongKeHangHoa dao = new DAO_ThongKeHangHoa();
    private final java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initFilters();
        setupTableColumns();
        thongKe();

        // Lắng nghe sự kiện thay đổi bộ lọc để tự động reload dữ liệu
        dpTuNgay.valueProperty().addListener((o, oldV, newV) -> thongKe());
        dpDenNgay.valueProperty().addListener((o, oldV, newV) -> thongKe());
        cbDanhMuc.valueProperty().addListener((o, oldV, newV) -> thongKe());
    }

    private void initFilters() {
        dpTuNgay.setValue(LocalDate.now().minusMonths(1));
        dpDenNgay.setValue(LocalDate.now());
        
        // Load danh mục từ database lên combobox
        cbDanhMuc.setItems(FXCollections.observableArrayList(dao.getDanhMucThuoc()));
        cbDanhMuc.getSelectionModel().selectFirst();

        cbKhoangNhanh.setItems(FXCollections.observableArrayList("Hôm nay", "7 ngày qua", "Tháng này", "Tùy chọn"));
        cbTrangThaiTon.setItems(FXCollections.observableArrayList("Tất cả", "Còn hàng", "Sắp hết hàng"));
    }

    private void setupTableColumns() {
        // Cấu hình bảng thuốc bán chạy sử dụng MapValueFactory
        colSTTTop.setCellValueFactory(new MapValueFactory<>("stt"));
        colTenThuocTop.setCellValueFactory(new MapValueFactory<>("tenThuoc"));
        colDanhMucTop.setCellValueFactory(new MapValueFactory<>("tenDanhMuc"));
        colTonTop.setCellValueFactory(new MapValueFactory<>("soLuongBan"));
        colGiaTriTop.setCellValueFactory(new MapValueFactory<>("doanhThu"));

        // Cấu hình bảng thuốc chậm bán
        colSTTHsd.setCellValueFactory(new MapValueFactory<>("stt"));
        colTenThuocHsd.setCellValueFactory(new MapValueFactory<>("tenThuoc"));
        colDanhMucHsd.setCellValueFactory(new MapValueFactory<>("tenDanhMuc"));
        colConLaiHsd.setCellValueFactory(new MapValueFactory<>("soLuongBan")); // khớp với key sql trả về
        colTonHsd.setCellValueFactory(new MapValueFactory<>("tonKho"));
    }

    private void thongKe() {
        LocalDate tuNgay = dpTuNgay.getValue();
        LocalDate denNgay = dpDenNgay.getValue();
        String danhMuc = cbDanhMuc.getValue();

        if (tuNgay == null || denNgay == null) return;

        // 1. Load KPI tổng quan
        Map<String, Object> tongQuan = dao.getTongQuan(tuNgay, denNgay, danhMuc);
        lblTongMatHang.setText(tongQuan.get("tongMatHang") + " mặt hàng");
        lblTongSoLuongTon.setText(df.format(tongQuan.get("tongSoLuongBan")) + " đơn vị");
        lblTongGiaTriTon.setText(df.format(tongQuan.get("tongDoanhThu")) + " ₫");

        // 2. Load Table Thuốc bán chạy & Chậm bán
        List<Map<String, Object>> topBanChay = dao.getTopThuocBanChay(tuNgay, denNgay, danhMuc, 10);
        tableTopTonKho.setItems(FXCollections.observableArrayList(topBanChay));

        List<Map<String, Object>> chamBan = dao.getTopThuocChamBan(tuNgay, denNgay, danhMuc, 10);
        tableLoSapHetHan.setItems(FXCollections.observableArrayList(chamBan));
        lblCanhBaoTon.setText(chamBan.size() + " mã thuốc");

        // 3. Vẽ biểu đồ xu hướng (LineChart)
        chartBienDong.getData().clear();
        XYChart.Series<String, Number> seriesLine = new XYChart.Series<>();
        seriesLine.setName("Doanh thu");
        List<Map<String, Object>> xuHuong = dao.getXuHuongBanTheoNgay(tuNgay, denNgay, danhMuc);
        for (Map<String, Object> m : xuHuong) {
            seriesLine.getData().add(new XYChart.Data<>(m.get("ngay").toString(), (Number) m.get("doanhThu")));
        }
        chartBienDong.getData().add(seriesLine);

        // 4. Vẽ biểu đồ cơ cấu danh mục (PieChart)
        chartCoCauTon.getData().clear();
        List<Map<String, Object>> coCau = dao.getDoanhThuTheoDanhMuc(tuNgay, denNgay, danhMuc);
        for (Map<String, Object> m : coCau) {
            chartCoCauTon.getData().add(new PieChart.Data((String) m.get("tenDanhMuc"), (Double) m.get("doanhThu")));
        }

        // 5. Vẽ biểu đồ cột Top bán chạy (BarChart)
        chartTopTonKho.getData().clear();
        XYChart.Series<String, Number> seriesBar = new XYChart.Series<>();
        seriesBar.setName("Số lượng đã bán");
        for (Map<String, Object> m : topBanChay) {
            seriesBar.getData().add(new XYChart.Data<>((String) m.get("tenThuoc"), (Number) m.get("soLuongBan")));
        }
        chartTopTonKho.getData().add(seriesBar);
    }

    @FXML
    void handleXoaBoLoc(ActionEvent event) {
        initFilters();
    }

    @FXML
    void handleXuatExcel(ActionEvent event) {
        try {
            String path = ThongKeHangHoaExcelExporter.xuatExcel(
                dpTuNgay.getValue(), dpDenNgay.getValue(), cbDanhMuc.getValue(),
                dao.getTongQuan(dpTuNgay.getValue(), dpDenNgay.getValue(), cbDanhMuc.getValue()),
                dao.getTopThuocBanChay(dpTuNgay.getValue(), dpDenNgay.getValue(), cbDanhMuc.getValue(), 20),
                dao.getDoanhThuTheoDanhMuc(dpTuNgay.getValue(), dpDenNgay.getValue(), cbDanhMuc.getValue()),
                dao.getXuHuongBanTheoNgay(dpTuNgay.getValue(), dpDenNgay.getValue(), cbDanhMuc.getValue()),
                dao.getTopThuocChamBan(dpTuNgay.getValue(), dpDenNgay.getValue(), cbDanhMuc.getValue(), 20)
            );
            new Alert(Alert.AlertType.INFORMATION, "Xuất Excel báo cáo thành công tại: " + path).show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Lỗi xuất file Excel: " + e.getMessage()).show();
        }
    }

    @FXML
    void handleInBaoCao(ActionEvent event) {
        try {
            String path = ThongKeHangHoaPdfExporter.xuatPDF(
                dpTuNgay.getValue(), dpDenNgay.getValue(), cbDanhMuc.getValue(),
                dao.getTongQuan(dpTuNgay.getValue(), dpDenNgay.getValue(), cbDanhMuc.getValue()),
                dao.getTopThuocBanChay(dpTuNgay.getValue(), dpDenNgay.getValue(), cbDanhMuc.getValue(), 20),
                dao.getDoanhThuTheoDanhMuc(dpTuNgay.getValue(), dpDenNgay.getValue(), cbDanhMuc.getValue()),
                dao.getXuHuongBanTheoNgay(dpTuNgay.getValue(), dpDenNgay.getValue(), cbDanhMuc.getValue()),
                dao.getTopThuocChamBan(dpTuNgay.getValue(), dpDenNgay.getValue(), cbDanhMuc.getValue(), 20)
            );
            new Alert(Alert.AlertType.INFORMATION, "In báo cáo PDF thành công tại: " + path).show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Lỗi in file PDF: " + e.getMessage()).show();
        }
    }
}