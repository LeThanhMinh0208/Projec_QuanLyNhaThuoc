package gui.main;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class GUI_TrangChuController implements Initializable {

    @FXML private Label lblTenNhanVien;
    @FXML private Label lblChucVu;

    @FXML private TableView<ThuocModel> tblThuoc;
    @FXML private TableColumn<ThuocModel, String>  colMaThuoc;
    @FXML private TableColumn<ThuocModel, String>  colTenThuoc;
    @FXML private TableColumn<ThuocModel, String>  colDangBao;
    @FXML private TableColumn<ThuocModel, Integer> colTonKho;
    @FXML private TableColumn<ThuocModel, String>  colGiaBan;
    @FXML private TableColumn<ThuocModel, String>  colDonVi;

    @FXML private TextField txtSearch;
    @FXML private HBox statsBox;
    @FXML private Label lblSoHoaDon;
    @FXML private Label lblDoanhThu;

    @FXML private ListView<String> listHetHan;
    @FXML private ListView<String> listHetHang;
    @FXML private ListView<String> listGiaoDich;

    private final ObservableList<ThuocModel> dsThuoc = FXCollections.observableArrayList();
    private FilteredList<ThuocModel> dsThuocFilter;

    // ===== ĐỔI PASSWORD ĐÚNG MÁY MÀY =====
    private static final String DB_URL  = "jdbc:sqlserver://localhost:1433;"
            + "databaseName=QuanLyNhaThuoc_LongNguyen;"
            + "encrypt=true;trustServerCertificate=true;";
    private static final String DB_USER = "sa";
    private static final String DB_PASS = "sapassword";

    private static String hoTenNhanVien  = "Nhân viên";
    private static String chucVuNhanVien = "";

    public static void setNhanVienInfo(String hoTen, String chucVu) {
        hoTenNhanVien  = hoTen;
        chucVuNhanVien = chucVu;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lblTenNhanVien.setText(hoTenNhanVien);
        lblChucVu.setText(chucVuNhanVien);
        setupTableColumns();
        new Thread(() -> {
            loadDanhMucThuoc();
            loadCanhBaoHetHan();
            loadCanhBaoHetHang();
            loadGiaoDichGanNhat();
            loadThongKeHomNay();
        }).start();
    }

    private void setupTableColumns() {
        colMaThuoc.setCellValueFactory(new PropertyValueFactory<>("maThuoc"));
        colTenThuoc.setCellValueFactory(new PropertyValueFactory<>("tenThuoc"));
        colDangBao.setCellValueFactory(new PropertyValueFactory<>("hoatChat"));
        colTonKho.setCellValueFactory(new PropertyValueFactory<>("tonKho"));
        colGiaBan.setCellValueFactory(new PropertyValueFactory<>("giaBanFormatted"));
        colDonVi.setCellValueFactory(new PropertyValueFactory<>("donViCoBan"));

        tblThuoc.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(ThuocModel item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty)      setStyle("");
                else if (item.getTonKho() == 0) setStyle("-fx-background-color: #fecaca;");
                else if (item.getTonKho() <= 20) setStyle("-fx-background-color: #fef3c7;");
                else                            setStyle("");
            }
        });

        dsThuocFilter = new FilteredList<>(dsThuoc, p -> true);
        tblThuoc.setItems(dsThuocFilter);
    }

    // --------------------------------------------------
    // Danh mục thuốc: Thuoc + LoThuoc + DonViQuyDoi
    // Tồn kho = SUM(LoThuoc.soLuongTon) theo maThuoc
    // Giá bán = từ DonViQuyDoi có tyLeQuyDoi = 1 (đơn vị cơ bản)
    // --------------------------------------------------
    private void loadDanhMucThuoc() {
        String sql =
            "SELECT t.maThuoc, t.tenThuoc, t.hoatChat, t.donViCoBan, " +
            "       ISNULL(SUM(lt.soLuongTon), 0) AS tonKho, " +
            "       ISNULL(MIN(dv.giaBan), 0)      AS giaBan " +
            "FROM Thuoc t " +
            "LEFT JOIN LoThuoc lt ON lt.maThuoc = t.maThuoc " +
            "LEFT JOIN DonViQuyDoi dv ON dv.maThuoc = t.maThuoc AND dv.tyLeQuyDoi = 1 " +
            "WHERE t.trangThai = 'DANG_BAN' " +
            "GROUP BY t.maThuoc, t.tenThuoc, t.hoatChat, t.donViCoBan " +
            "ORDER BY t.tenThuoc";

        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            ObservableList<ThuocModel> list = FXCollections.observableArrayList();
            while (rs.next()) {
                list.add(new ThuocModel(
                        rs.getString("maThuoc"),
                        rs.getString("tenThuoc"),
                        rs.getString("hoatChat"),
                        rs.getString("donViCoBan"),
                        rs.getInt("tonKho"),
                        rs.getDouble("giaBan")
                ));
            }
            Platform.runLater(() -> dsThuoc.setAll(list));

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() ->
                showAlert(Alert.AlertType.ERROR, "Lỗi tải danh mục",
                        "Không tải được danh sách thuốc!\n" + e.getMessage()));
        }
    }

    // --------------------------------------------------
    // Hết hạn: LoThuoc.hanSuDung trong vòng 30 ngày tới
    // --------------------------------------------------
    private void loadCanhBaoHetHan() {
        String sql =
            "SELECT t.tenThuoc, lt.hanSuDung, " +
            "       DATEDIFF(day, GETDATE(), lt.hanSuDung) AS soNgay " +
            "FROM LoThuoc lt JOIN Thuoc t ON t.maThuoc = lt.maThuoc " +
            "WHERE lt.hanSuDung BETWEEN GETDATE() AND DATEADD(day, 30, GETDATE()) " +
            "  AND lt.soLuongTon > 0 " +
            "ORDER BY lt.hanSuDung ASC";

        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            ObservableList<String> list = FXCollections.observableArrayList();
            while (rs.next())
                list.add("⚠ " + rs.getString("tenThuoc")
                        + " — còn " + rs.getInt("soNgay") + " ngày");
            if (list.isEmpty()) list.add("✅ Không có thuốc sắp hết hạn");
            Platform.runLater(() -> listHetHan.setItems(list));

        } catch (Exception e) { e.printStackTrace(); }
    }

    // --------------------------------------------------
    // Hết hàng: tổng tồn kho (quy về đơn vị cơ bản) <= 20
    // --------------------------------------------------
    private void loadCanhBaoHetHang() {
        String sql =
            "SELECT t.tenThuoc, t.donViCoBan, SUM(lt.soLuongTon) AS tongTon " +
            "FROM LoThuoc lt JOIN Thuoc t ON t.maThuoc = lt.maThuoc " +
            "GROUP BY t.maThuoc, t.tenThuoc, t.donViCoBan " +
            "HAVING SUM(lt.soLuongTon) <= 20 " +
            "ORDER BY tongTon ASC";

        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            ObservableList<String> list = FXCollections.observableArrayList();
            while (rs.next())
                list.add("📦 " + rs.getString("tenThuoc")
                        + " — còn " + rs.getInt("tongTon")
                        + " " + rs.getString("donViCoBan"));
            if (list.isEmpty()) list.add("✅ Tồn kho đầy đủ");
            Platform.runLater(() -> listHetHang.setItems(list));

        } catch (Exception e) { e.printStackTrace(); }
    }

    // --------------------------------------------------
    // 10 hóa đơn gần nhất, tính tổng từ ChiTietHoaDon
    // --------------------------------------------------
    private void loadGiaoDichGanNhat() {
        String sql =
            "SELECT TOP 10 h.maHoaDon, h.ngayLap, " +
            "       ISNULL(kh.hoTen, N'Khách lẻ') AS tenKhach, " +
            "       ISNULL(SUM(ct.soLuong * ct.donGia), 0) AS tongTien " +
            "FROM HoaDon h " +
            "LEFT JOIN KhachHang kh ON kh.maKhachHang = h.maKhachHang " +
            "LEFT JOIN ChiTietHoaDon ct ON ct.maHoaDon = h.maHoaDon " +
            "GROUP BY h.maHoaDon, h.ngayLap, kh.hoTen " +
            "ORDER BY h.ngayLap DESC";

        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            ObservableList<String> list = FXCollections.observableArrayList();
            NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
            while (rs.next())
                list.add(rs.getString("maHoaDon") + "  |  "
                        + rs.getString("tenKhach") + "  —  "
                        + fmt.format(rs.getDouble("tongTien")) + "đ");
            if (list.isEmpty()) list.add("Chưa có giao dịch nào");
            Platform.runLater(() -> listGiaoDich.setItems(list));

        } catch (Exception e) { e.printStackTrace(); }
    }

    // --------------------------------------------------
    // Thống kê hôm nay
    // --------------------------------------------------
    private void loadThongKeHomNay() {
        String sql =
            "SELECT COUNT(DISTINCT h.maHoaDon) AS soHD, " +
            "       ISNULL(SUM(ct.soLuong * ct.donGia), 0) AS doanhThu " +
            "FROM HoaDon h " +
            "LEFT JOIN ChiTietHoaDon ct ON ct.maHoaDon = h.maHoaDon " +
            "WHERE CAST(h.ngayLap AS DATE) = CAST(GETDATE() AS DATE)";

        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                int soHD  = rs.getInt("soHD");
                double dt = rs.getDouble("doanhThu");
                NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
                Platform.runLater(() -> {
                    lblSoHoaDon.setText(String.valueOf(soHD));
                    lblDoanhThu.setText(fmt.format(dt) + "đ");
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ===== FXML HANDLERS =====

    @FXML private void handleSearch() {
        String q = txtSearch.getText().toLowerCase().trim();
        dsThuocFilter.setPredicate(t -> {
            if (q.isEmpty()) return true;
            return t.getTenThuoc().toLowerCase().contains(q)
                    || t.getMaThuoc().toLowerCase().contains(q)
                    || t.getHoatChat().toLowerCase().contains(q);
        });
    }

    @FXML private void handleSelectThuoc(MouseEvent event) {
        if (event.getClickCount() == 2) {
            ThuocModel sel = tblThuoc.getSelectionModel().getSelectedItem();
            if (sel != null) {
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Chi tiết thuốc");
                a.setHeaderText(sel.getTenThuoc() + "  [" + sel.getMaThuoc() + "]");
                a.setContentText(
                    "Hoạt chất : " + sel.getHoatChat()       + "\n" +
                    "Đơn vị   : " + sel.getDonViCoBan()      + "\n" +
                    "Tồn kho  : " + sel.getTonKho()          + " " + sel.getDonViCoBan() + "\n" +
                    "Giá bán  : " + sel.getGiaBanFormatted()
                );
                a.showAndWait();
            }
        }
    }

    @FXML private void toggleBaoCao() {
        boolean vis = statsBox.isVisible();
        statsBox.setVisible(!vis);
        statsBox.setManaged(!vis);
        if (!vis) new Thread(this::loadThongKeHomNay).start();
    }

    @FXML private void showTrangChu() {}

    @FXML private void showBanHang() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("GUI_BanHang.fxml"));
            Stage s = new Stage();
            s.setTitle("Bán Thuốc — Long Nguyên Pharma");
            s.setScene(new Scene(root));
            s.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không mở được màn hình bán hàng:\n" + e.getMessage());
        }
    }

    @FXML private void handleDangXuat() {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION);
        c.setTitle("Đăng xuất");
        c.setHeaderText(null);
        c.setContentText("Bạn có chắc muốn đăng xuất không?");
        c.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                ((Stage) tblThuoc.getScene().getWindow()).close();
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("GUI_DangNhap.fxml"));
                    Stage login = new Stage();
                    login.setTitle("Đăng nhập — Long Nguyên Pharma");
                    login.setScene(new Scene(root));
                    login.setResizable(false);
                    login.show();
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(content);
        a.showAndWait();
    }

    // ===== MODEL =====
    public static class ThuocModel {
        private final String maThuoc, tenThuoc, hoatChat, donViCoBan;
        private final int    tonKho;
        private final double giaBan;

        public ThuocModel(String maThuoc, String tenThuoc, String hoatChat,
                          String donViCoBan, int tonKho, double giaBan) {
            this.maThuoc    = maThuoc;
            this.tenThuoc   = tenThuoc;
            this.hoatChat   = hoatChat   != null ? hoatChat   : "";
            this.donViCoBan = donViCoBan != null ? donViCoBan : "";
            this.tonKho     = tonKho;
            this.giaBan     = giaBan;
        }

        public String getMaThuoc()    { return maThuoc; }
        public String getTenThuoc()   { return tenThuoc; }
        public String getHoatChat()   { return hoatChat; }
        public String getDonViCoBan() { return donViCoBan; }
        public int    getTonKho()     { return tonKho; }
        public double getGiaBan()     { return giaBan; }

        public String getGiaBanFormatted() {
            if (giaBan == 0) return "Chưa có giá";
            return NumberFormat.getInstance(new Locale("vi", "VN")).format(giaBan) + "đ";
        }
    }
}
