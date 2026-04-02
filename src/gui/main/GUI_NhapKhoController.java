package gui.main;

import dao.DAO_DonDatHang;
import dao.DAO_PhieuNhap;
import entity.ChiTietDonDatHang;
import entity.DonDatHang;
import entity.PhieuNhap;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import utils.AlertUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.TableCell;
public class GUI_NhapKhoController {

    @FXML private ToggleGroup tabGroup;
    @FXML private ToggleButton tabTaoPhieu, tabDanhSach;
    @FXML private HBox viewTaoPhieu; 
    @FXML private VBox viewDanhSach;
    
    // ==========================================
    // UI TAB 1: TẠO PHIẾU NHẬP
    // ==========================================
    @FXML private ComboBox<DonDatHang> cbMaDon;
    @FXML private TextField txtNhaCungCap, txtNguoiGiao;
    @FXML private DatePicker dpNgayNhap;
    @FXML private TextArea txtGhiChu;
    @FXML private Label lblTongTien;
    
    @FXML private TableView<ChiTietDonDatHang> tableNhapKho;
    @FXML private TableColumn<ChiTietDonDatHang, String> colTenThuoc, colDonVi, colMaLo, colNgaySX, colHanDung;
    @FXML private TableColumn<ChiTietDonDatHang, Integer> colSLDat, colSLNhan;
    @FXML private TableColumn<ChiTietDonDatHang, Double> colGiaNhap;

    // ==========================================
    // UI TAB 2: DANH MỤC PHIẾU NHẬP
    // ==========================================
    @FXML private TextField txtTimKiemPhieuNhap;
    @FXML private TableView<PhieuNhap> tablePhieuNhap;
    @FXML private TableColumn<PhieuNhap, Void> colXemChiTietPN;
    @FXML private TableColumn<PhieuNhap, String> colMaPhieuNhap, colNhaCungCapPN, colNhanVienPN;
    @FXML private TableColumn<PhieuNhap, java.sql.Timestamp> colNgayNhapPN; // Đã đổi sang Timestamp
    @FXML private TableColumn<PhieuNhap, Double> colTongTienPN;

    // ==========================================
    // BIẾN TOÀN CỤC
    // ==========================================
    private DAO_DonDatHang daoDon = new DAO_DonDatHang();
    private DAO_PhieuNhap daoPhieuNhap = new DAO_PhieuNhap();
    private DonDatHang donHienTai;
    private List<ChiTietDonDatHang> listChiTietHienTai;
    
    private DecimalFormat df = new DecimalFormat("#,### VNĐ");
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm"); // Hiển thị cả giờ phút cho chuyên nghiệp

    @FXML public void initialize() {
        setupTabs();
        setupTableTaoPhieu();
        setupTableDanhSachPhieuNhap(); // Khởi tạo bảng danh mục phiếu nhập
        
        loadDonChoNhap(); 
        
        if(dpNgayNhap != null) dpNgayNhap.setValue(LocalDate.now());

        cbMaDon.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) hienThiChiTietDon(newV);
        });
    }

    // ========================================================
    // CÁC HÀM XỬ LÝ TAB 1: TẠO PHIẾU
    // ========================================================
    private void loadDonChoNhap() {
        List<DonDatHang> dsDon = daoDon.getAllDonDatHang().stream()
                
                .filter(DonDatHang::isChoPhepNhapKho)
              
                .filter(don -> {
                    String ttDB = don.getTrangThai();
                    if (ttDB == null) return true;
                    
                    if (ttDB.equals("GIAO_DU") || ttDB.equals("DONG_DON_THIEU") || ttDB.equals("DA_HUY") || ttDB.equals("GIAO_MOT_PHAN")) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
                
        cbMaDon.setItems(FXCollections.observableArrayList(dsDon));
    }

    private void hienThiChiTietDon(DonDatHang don) {
        this.donHienTai = don;
        txtNhaCungCap.setText(don.getNhaCungCap().getTenNhaCungCap());
        txtNguoiGiao.clear();
        txtGhiChu.clear();

        listChiTietHienTai = daoDon.getChiTietByMaDon(don.getMaDonDatHang());
        tableNhapKho.setItems(FXCollections.observableArrayList(listChiTietHienTai));
        tinhToanTongTienHienThi();
    }

    public void chuyenTuDonDatHang(DonDatHang don) {
        tabTaoPhieu.setSelected(true);
        loadDonChoNhap(); 
        for (DonDatHang d : cbMaDon.getItems()) {
            if (d.getMaDonDatHang().equals(don.getMaDonDatHang())) {
                cbMaDon.getSelectionModel().select(d);
                break;
            }
        }
    }

    private void setupTableTaoPhieu() {
        colTenThuoc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getThuoc().getTenThuoc()));
        colDonVi.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDonViQuyDoi().getTenDonVi()));
        
        colSLDat.setCellValueFactory(new PropertyValueFactory<>("soLuongDat"));
        colSLDat.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        // 1. Ô SỐ LƯỢNG NHẬN (Gõ thẳng, lưu liền, tự tính tiền)
        colSLNhan.setCellValueFactory(new PropertyValueFactory<>("soLuongDaNhan"));
        colSLNhan.setCellFactory(column -> new TableCell<ChiTietDonDatHang, Integer>() {
            private final TextField textField = new TextField();
            {
                textField.getStyleClass().add("table-input-active");
                textField.textProperty().addListener((obs, oldV, newV) -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        try {
                            int val = newV.isEmpty() ? 0 : Integer.parseInt(newV);
                            getTableRow().getItem().setSoLuongDaNhan(val);
                            tinhToanTongTienHienThi(); // Gõ số nhảy tiền ngay lập tức!
                        } catch (NumberFormatException e) {
                            // Bỏ qua nếu gõ chữ
                        }
                    }
                });
            }
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    textField.setText(String.valueOf(getTableRow().getItem().getSoLuongDaNhan()));
                    setGraphic(textField);
                }
            }
        });

        // 2. Ô MÃ LÔ (Gõ thẳng)
        colMaLo.setCellValueFactory(new PropertyValueFactory<>("maLo"));
        colMaLo.setCellFactory(column -> new TableCell<ChiTietDonDatHang, String>() {
            private final TextField textField = new TextField();
            {
                textField.getStyleClass().add("table-input-active");
                textField.setPromptText("Nhập lô...");
                textField.textProperty().addListener((obs, oldV, newV) -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        getTableRow().getItem().setMaLo(newV);
                    }
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    textField.setText(getTableRow().getItem().getMaLo());
                    setGraphic(textField);
                }
            }
        });

        // 3. Ô NGÀY SẢN XUẤT (Sổ Lịch)
        colNgaySX.setCellValueFactory(new PropertyValueFactory<>("ngaySanXuatTemp"));
        colNgaySX.setCellFactory(column -> new TableCell<ChiTietDonDatHang, String>() {
            private final DatePicker datePicker = new DatePicker();
            {
                datePicker.getStyleClass().add("table-date-active");
                datePicker.setPrefWidth(120);
                datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
                    if (getTableRow() != null && getTableRow().getItem() != null && newDate != null) {
                        // Lưu dạng yyyy-MM-dd để nhét vào Database SQL chuẩn xác
                        getTableRow().getItem().setNgaySanXuatTemp(newDate.toString());
                    }
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    String dateStr = getTableRow().getItem().getNgaySanXuatTemp();
                    if (dateStr != null && !dateStr.isEmpty()) {
                        try { datePicker.setValue(LocalDate.parse(dateStr)); } catch(Exception e) {}
                    } else {
                        datePicker.setValue(null);
                    }
                    setGraphic(datePicker);
                }
            }
        });

        // 4. Ô HẠN SỬ DỤNG (Sổ Lịch)
        colHanDung.setCellValueFactory(new PropertyValueFactory<>("hanSuDung"));
        colHanDung.setCellFactory(column -> new TableCell<ChiTietDonDatHang, String>() {
            private final DatePicker datePicker = new DatePicker();
            {
                datePicker.getStyleClass().add("table-date-active");
                datePicker.setPrefWidth(120);
                datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
                    if (getTableRow() != null && getTableRow().getItem() != null && newDate != null) {
                        getTableRow().getItem().setHanSuDung(newDate.toString());
                    }
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    String dateStr = getTableRow().getItem().getHanSuDung();
                    if (dateStr != null && !dateStr.isEmpty()) {
                        try { datePicker.setValue(LocalDate.parse(dateStr)); } catch(Exception e) {}
                    } else {
                        datePicker.setValue(null);
                    }
                    setGraphic(datePicker);
                }
            }
        });

        // 5. Ô GIÁ NHẬP (Gõ thẳng, tính tiền ngay)
        colGiaNhap.setCellValueFactory(new PropertyValueFactory<>("donGiaDuKien"));
        colGiaNhap.setCellFactory(column -> new TableCell<ChiTietDonDatHang, Double>() {
            private final TextField textField = new TextField();
            {
                textField.getStyleClass().add("table-input-active");
                textField.textProperty().addListener((obs, oldV, newV) -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        try {
                            String cleanStr = newV.replaceAll("[,\\s]", ""); // Xóa dấu phẩy nếu có
                            double val = cleanStr.isEmpty() ? 0 : Double.parseDouble(cleanStr);
                            getTableRow().getItem().setDonGiaDuKien(val);
                            tinhToanTongTienHienThi();
                        } catch (NumberFormatException e) {}
                    }
                });
            }
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Double val = getTableRow().getItem().getDonGiaDuKien();
                    textField.setText(val != null ? String.format("%.0f", val) : "0");
                    setGraphic(textField);
                }
            }
        });
    }

    private void tinhToanTongTienHienThi() {
        double tong = 0;
        if (listChiTietHienTai != null) {
            for (ChiTietDonDatHang ct : listChiTietHienTai) {
                tong += ct.getSoLuongDaNhan() * ct.getDonGiaDuKien();
            }
        }
        if(lblTongTien != null) lblTongTien.setText(df.format(tong));
    }

    @FXML void handleLuuPhieuNhap(ActionEvent event) {
        tableNhapKho.requestFocus(); 

        if (donHienTai == null || listChiTietHienTai == null || listChiTietHienTai.isEmpty()) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn đơn hàng để nhập kho!");
            return;
        }

        if (txtNguoiGiao.getText().trim().isEmpty()) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập người giao hàng!");
            txtNguoiGiao.requestFocus();
            return;
        }

        boolean isGiaoThieu = false;
        for (ChiTietDonDatHang ct : listChiTietHienTai) {
            if (ct.getSoLuongDaNhan() > 0) {
                if (ct.getMaLo() == null || ct.getMaLo().trim().isEmpty()) {
                    AlertUtils.showAlert(Alert.AlertType.WARNING, "Thiếu dữ liệu", "Vui lòng nhập Mã Lô cho thuốc: " + ct.getThuoc().getTenThuoc());
                    return; 
                }
                if (daoPhieuNhap.kiemTraMaLoTonTai(ct.getMaLo().trim())) {
                    AlertUtils.showAlert(Alert.AlertType.ERROR, "Trùng Mã Lô", 
                        "Mã lô [" + ct.getMaLo().trim() + "] của thuốc " + ct.getThuoc().getTenThuoc() + 
                        " ĐÃ TỒN TẠI!\n\nVui lòng nhập mã lô khác để quản lý đợt nhập này.");
                    return; 
                }
            }
            if (ct.getSoLuongDaNhan() < ct.getSoLuongDat()) {
                isGiaoThieu = true;
            }
        }

        int soNgayHen = 0;
        if (isGiaoThieu) {
            TextInputDialog dialog = new TextInputDialog("3");
            dialog.setTitle("Phát hiện giao thiếu");
            dialog.setHeaderText("Hệ thống sẽ tách đơn cho phần còn thiếu.");
            dialog.setContentText("Hẹn mấy ngày nữa giao bù? (Nhập 0 nếu hủy phần thiếu):");
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                try {
                    soNgayHen = Integer.parseInt(result.get());
                } catch (NumberFormatException e) {
                    AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập số ngày hợp lệ!");
                    return;
                }
            } else return;
        }

        PhieuNhap pn = new PhieuNhap();
        pn.setDonDatHang(donHienTai);
        pn.setNhaCungCap(donHienTai.getNhaCungCap());
        
        entity.NhanVien nv = new entity.NhanVien();
        nv.setMaNhanVien("NV001"); // TODO: Lấy user đang đăng nhập
        pn.setNhanVien(nv);

boolean tc = daoPhieuNhap.luuPhieuNhapVaCapNhatDon(pn, listChiTietHienTai, donHienTai, soNgayHen);
        
        if (tc) {
            // =========================================================
            // BẮT ĐẦU LOGIC MỚI: CỘNG DỒN CÔNG NỢ CHO NHÀ CUNG CẤP
            // =========================================================
            double tongTienNhap = 0;
            for (ChiTietDonDatHang ct : listChiTietHienTai) {
                tongTienNhap += ct.getSoLuongDaNhan() * ct.getDonGiaDuKien();
            }
            
            // Gọi hàm DAO vừa tạo để cộng tiền
            dao.DAO_NhaCungCap daoNCC = new dao.DAO_NhaCungCap();
            boolean updateCongNo = daoNCC.congCongNoNhaCungCap(donHienTai.getNhaCungCap().getMaNhaCungCap(), tongTienNhap);
            
            if (updateCongNo) {
                AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Thành công", 
                    "Đã lưu phiếu nhập kho!\nĐã cộng dồn " + df.format(tongTienNhap) + " vào công nợ của NCC.");
            } else {
                AlertUtils.showAlert(Alert.AlertType.WARNING, "Cảnh báo", 
                    "Đã lưu phiếu nhập nhưng có lỗi xảy ra khi cập nhật công nợ NCC!");
            }
            // =========================================================

            loadDonChoNhap(); 
            handleHuyNhapKho(null);
            
            // Xóa bộ lọc tìm kiếm và tải lại dữ liệu mới nhất cho Tab Danh mục
            if(txtTimKiemPhieuNhap != null) txtTimKiemPhieuNhap.clear();
            loadDanhSachPhieuNhap(); 
        } else {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi Server", "Không thể lưu dữ liệu phiếu nhập, vui lòng thử lại!");
        }
    }

    @FXML void handleHuyNhapKho(ActionEvent event) {
        cbMaDon.getSelectionModel().clearSelection();
        txtNhaCungCap.clear();
        txtNguoiGiao.clear();
        txtGhiChu.clear();
        tableNhapKho.getItems().clear();
        if(lblTongTien != null) lblTongTien.setText("0 VNĐ");
        donHienTai = null;
    }

    // ========================================================
    // CÁC HÀM XỬ LÝ TAB 2: DANH MỤC PHIẾU NHẬP
    // ========================================================
    private void setupTabs() {
        tabGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) { oldT.setSelected(true); return; }
            if (viewTaoPhieu != null && viewDanhSach != null) {
                boolean isTao = (newT == tabTaoPhieu);
                viewTaoPhieu.setVisible(isTao);
                viewTaoPhieu.setManaged(isTao);
                viewDanhSach.setVisible(!isTao);
                viewDanhSach.setManaged(!isTao);
                
                // Khi người dùng bấm sang Tab Danh Sách thì tự động tải dữ liệu từ DB
                if (!isTao) {
                    loadDanhSachPhieuNhap();
                }
            }
        });
    }

    private void setupTableDanhSachPhieuNhap() {
        if (tablePhieuNhap == null) return;

        colMaPhieuNhap.setCellValueFactory(new PropertyValueFactory<>("maPhieuNhap"));
        colMaPhieuNhap.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        // Lấy Tên Nhà cung cấp
        colNhaCungCapPN.setCellValueFactory(c -> {
            if(c.getValue().getNhaCungCap() != null) 
                return new SimpleStringProperty(c.getValue().getNhaCungCap().getTenNhaCungCap());
            return new SimpleStringProperty("N/A");
        });
        
        // Lấy Họ Tên Nhân viên (khớp với entity NhanVien)
        colNhanVienPN.setCellValueFactory(c -> {
            if(c.getValue().getNhanVien() != null) 
                return new SimpleStringProperty(c.getValue().getNhanVien().getHoTen());
            return new SimpleStringProperty("N/A");
        });

        // Định dạng Timestamp
        colNgayNhapPN.setCellValueFactory(new PropertyValueFactory<>("ngayNhap"));
        colNgayNhapPN.setStyle("-fx-alignment: CENTER;");
        colNgayNhapPN.setCellFactory(column -> new TableCell<PhieuNhap, java.sql.Timestamp>() {
            protected void updateItem(java.sql.Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : sdf.format(item));
            }
        });

        // Định dạng Tiền tệ
        colTongTienPN.setCellValueFactory(new PropertyValueFactory<>("tongTien")); 
        colTongTienPN.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold; -fx-text-fill: #be123c;");
        colTongTienPN.setCellFactory(column -> new TableCell<PhieuNhap, Double>() {
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : df.format(item));
            }
        });

        // Cột Nút XEM CHI TIẾT
        colXemChiTietPN.setCellFactory(param -> new TableCell<PhieuNhap, Void>() {
            private final Button btnXem = new Button("Xem");
            {
                btnXem.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0284c7; -fx-background-radius: 20; -fx-font-weight: bold; -fx-cursor: hand;");
                btnXem.setPrefWidth(60);
                btnXem.setOnAction(e -> {
                    PhieuNhap pn = getTableRow().getItem();
                    if (pn != null) {
                        moDialogChiTietPhieuNhap(pn);
                    }
                });
            }
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(btnXem);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        
        // Sự kiện gõ tìm kiếm (Tìm theo mã phiếu hoặc Tên NCC)
        if(txtTimKiemPhieuNhap != null) {
            txtTimKiemPhieuNhap.textProperty().addListener((obs, oldText, newText) -> {
                loadDanhSachPhieuNhap(newText);
            });
        }
    }

    private void loadDanhSachPhieuNhap() {
        loadDanhSachPhieuNhap(""); // Mặc định tải tất cả
    }
    
    private void loadDanhSachPhieuNhap(String tuKhoa) {
        if (daoPhieuNhap == null || tablePhieuNhap == null) return;
        
        // Kéo dữ liệu từ DAO đổ lên bảng
        List<PhieuNhap> ds = daoPhieuNhap.getAllPhieuNhap(tuKhoa); 
        tablePhieuNhap.setItems(FXCollections.observableArrayList(ds));
    }

    private void moDialogChiTietPhieuNhap(PhieuNhap phieuNhap) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_ChiTietPhieuNhap.fxml"));
            javafx.scene.Parent root = loader.load();

            // Truyền dữ liệu phiếu qua Dialog
            gui.dialogs.Dialog_ChiTietPhieuNhapController controller = loader.getController();
            controller.setPhieuNhap(phieuNhap); 

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Chi tiết Phiếu Nhập - " + phieuNhap.getMaPhieuNhap());
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL); 
            stage.setResizable(false);
            stage.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở giao diện chi tiết phiếu nhập.");
        }
    }
}