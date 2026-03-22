package gui.main;

import dao.*;
import entity.*;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GUI_NhapKhoController implements Initializable {
    @FXML private ComboBox<DonNhapHang> cmbDonDatHang;
    @FXML private TextField txtNhaCungCap, txtNguoiGiao;
    @FXML private Label lblTongTienThanhToan;
    @FXML private TableView<ChiTietDonNhapHang> tableChiTietNhap;
    @FXML private TableColumn<ChiTietDonNhapHang, String> colTenThuoc, colDonVi, colMaLo, colNgaySX, colHanDung;
    @FXML private TableColumn<ChiTietDonNhapHang, Integer> colSLDat, colSLNhan;
    @FXML private TableColumn<ChiTietDonNhapHang, Double> colDonGia, colThanhTien;
    @FXML private TextArea txtGhiChu;

    private DAO_DonNhapHang daoDonNhap = new DAO_DonNhapHang();
    private DAO_LoThuoc daoLoThuoc = new DAO_LoThuoc();
    private ObservableList<ChiTietDonNhapHang> listChiTiet = FXCollections.observableArrayList();
    

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableEditable();
        loadComboBoxDonHang();
    }
    
    public void setDonDatHang(DonNhapHang don) {
        if (don != null) {
            cmbDonDatHang.setValue(don);
            txtNhaCungCap.setText(don.getNhaCungCap().getTenNhaCungCap());
            
            // Nạp ghi chú cũ vào, làm trống ô Người giao để sếp nhập mới
            txtGhiChu.setText(don.getGhiChu() != null ? don.getGhiChu() : "");
            if (txtNguoiGiao != null) txtNguoiGiao.clear();

            List<ChiTietDonNhapHang> ds = daoDonNhap.getChiTietByMaDon(don.getMaDonNhap());
            listChiTiet.setAll(ds);
            tableChiTietNhap.setItems(listChiTiet);
            tinhTongTien();
        }
    }

    private void loadComboBoxDonHang() {
        cmbDonDatHang.setItems(FXCollections.observableArrayList(daoDonNhap.getDonHangChoNhap()));
        cmbDonDatHang.setOnAction(e -> setDonDatHang(cmbDonDatHang.getValue()));
    }

    private void setupTableEditable() {
        colTenThuoc.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getThuoc().getTenThuoc()));
        colDonVi.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDonViQuyDoi().getTenDonVi()));
        colSLDat.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("soLuongDat"));

        // 1. CỘT SỐ LƯỢNG NHẬN
        colSLNhan.setCellFactory(param -> new TableCell<ChiTietDonNhapHang, Integer>() {
            private final TextField txt = new TextField();
            {
                // Khi đang gõ -> Tính lại tổng tiền bên dưới
                txt.textProperty().addListener((obs, oldV, newV) -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        try {
                            getTableRow().getItem().setSoLuongDaNhan(newV.isEmpty() ? 0 : Integer.parseInt(newV.replace(",", "")));
                            tinhTongTien(); 
                        } catch (Exception e) {}
                    }
                });
                
                // Khi bấm Enter -> Refresh bảng để cột Thành Tiền nhảy số
                txt.setOnAction(e -> getTableView().refresh());
                
                // Khi click chuột ra chỗ khác -> Refresh bảng để cột Thành Tiền nhảy số
                txt.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                    if (!isFocused) getTableView().refresh();
                });
            }
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    txt.setText(String.valueOf(getTableRow().getItem().getSoLuongDaNhan()));
                    setGraphic(txt);
                }
            }
        });

        // 2. CỘT ĐƠN GIÁ (Giá Nhập Thực Tế)
        colDonGia.setCellFactory(param -> new TableCell<ChiTietDonNhapHang, Double>() {
            private final TextField txt = new TextField();
            {
                txt.textProperty().addListener((obs, oldV, newV) -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        try {
                            getTableRow().getItem().setDonGiaDuKien(newV.isEmpty() ? 0 : Double.parseDouble(newV.replace(",", "")));
                            tinhTongTien();
                        } catch (Exception e) {}
                    }
                });
                txt.setOnAction(e -> getTableView().refresh());
                
                // Mất focus (click ra ngoài) cũng refresh lại Thành Tiền
                txt.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                    if (!isFocused) getTableView().refresh();
                });
            }
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    txt.setText(String.format("%.0f", getTableRow().getItem().getDonGiaDuKien()));
                    setGraphic(txt);
                }
            }
        });

        // 3. CÁC CỘT TEXT (Mã Lô, Ngày SX, Hạn Dùng)
        setupTextColumn(colMaLo, "maLo");
        setupTextColumn(colNgaySX, "ngaySanXuatTemp");
        setupTextColumn(colHanDung, "hanSuDung");

        // 4. CỘT THÀNH TIỀN (Lấy SL Nhận * Đơn Giá)
        colThanhTien.setCellValueFactory(cell -> {
            ChiTietDonNhapHang ct = cell.getValue();
            // Nhân Số Lượng Nhận với Đơn Giá
            double thanhTien = ct.getSoLuongDaNhan() * ct.getDonGiaDuKien();
            return new javafx.beans.property.SimpleDoubleProperty(thanhTien).asObject();
        });
        
        colThanhTien.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : String.format("%,.0f đ", item));
            }
        });
    }

    private void setupTextColumn(TableColumn<ChiTietDonNhapHang, String> col, String prop) {
        col.setCellFactory(param -> new TableCell<>() {
            private final TextField txt = new TextField();
            {
                txt.textProperty().addListener((o, oldV, newV) -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        ChiTietDonNhapHang item = getTableRow().getItem();
                        if (prop.equals("maLo")) item.setMaLo(newV);
                        else if (prop.equals("ngaySanXuatTemp")) item.setNgaySanXuatTemp(newV);
                        else if (prop.equals("hanSuDung")) item.setHanSuDung(newV);
                    }
                });
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    ChiTietDonNhapHang ct = getTableRow().getItem();
                    String v = prop.equals("maLo") ? ct.getMaLo() : (prop.equals("ngaySanXuatTemp") ? ct.getNgaySanXuatTemp() : ct.getHanSuDung());
                    txt.setText(v != null ? v : "");
                    setGraphic(txt);
                }
            }
        });
    }

    private void tinhTongTien() {
        double tong = listChiTiet.stream().mapToDouble(c -> c.getSoLuongDaNhan() * c.getDonGiaDuKien()).sum();
        lblTongTienThanhToan.setText(String.format("%,.0f VNĐ", tong));
    }

    @FXML
    void handleXacNhanNhapKho(ActionEvent event) {
        DonNhapHang don = cmbDonDatHang.getValue();
        if (don == null) return;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("d/M/yyyy");
        try {
            double tongTienThucTe = 0;
            for (ChiTietDonNhapHang ct : listChiTiet) {
                if (ct.getSoLuongDaNhan() <= 0) continue;
                ct.setDonNhapHang(don);

                // Lưu lô thuốc
                LoThuoc lo = new LoThuoc();
                lo.setMaLoThuoc(ct.getMaLo());
                lo.setThuoc(ct.getThuoc());
                lo.setSoLuongTon(ct.getSoLuongDaNhan() * ct.getDonViQuyDoi().getTyLeQuyDoi());
                lo.setGiaNhap(ct.getDonGiaDuKien() / ct.getDonViQuyDoi().getTyLeQuyDoi()); // Cập nhật giá theo lô
                lo.setNgaySanXuat(java.sql.Date.valueOf(LocalDate.parse(ct.getNgaySanXuatTemp(), dtf)));
                lo.setHanSuDung(java.sql.Date.valueOf(LocalDate.parse(ct.getHanSuDung(), dtf)));
                lo.setViTriKho("KHO_DU_TRU");

                if (daoLoThuoc.themLoThuoc(lo)) {
                    daoDonNhap.capNhatThongTinThucNhap(ct);
                    // ĐÃ XÓA LỆNH CẬP NHẬT GIÁ VÀO BẢNG THUỐC Ở ĐÂY
                    tongTienThucTe += (ct.getSoLuongDaNhan() * ct.getDonGiaDuKien());
                }
            }
            
            // Chốt hóa đơn
            String tenNguoiGiao = (txtNguoiGiao != null && !txtNguoiGiao.getText().isEmpty()) ? txtNguoiGiao.getText() : "Không có";
            String ghiChuHienTai = (txtGhiChu != null && !txtGhiChu.getText().isEmpty()) ? txtGhiChu.getText() : "Không có";
            String ghiChuMoi = "Người giao: " + tenNguoiGiao + " | Ghi chú: " + ghiChuHienTai;

            // Gọi hàm mới cập nhật cả Tiền và Ghi Chú
            if (daoDonNhap.capNhatTrangThaiTienVaGhiChu(don.getMaDonNhap(), tongTienThucTe, ghiChuMoi)) {
                new Alert(Alert.AlertType.INFORMATION, "Nhập kho thành công!").showAndWait();
                utils.SceneUtils.switchPage("/gui/main/GUI_DanhMucPhieuNhap.fxml");
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Lỗi định dạng ngày dd/MM/yyyy hoặc trống Mã lô!").show();
        }
    }

    @FXML void handleChuyenTrangDanhSach(ActionEvent event) { utils.SceneUtils.switchPage("/gui/main/GUI_DanhMucPhieuNhap.fxml"); }
    @FXML void handleChuyenTrangNhapKho(ActionEvent event) { utils.SceneUtils.switchPage("/gui/main/GUI_NhapKho.fxml"); }
    @FXML void handleHuyBo(ActionEvent event) {
        cmbDonDatHang.getSelectionModel().clearSelection();
        txtNhaCungCap.clear();
        if(txtNguoiGiao != null) txtNguoiGiao.clear();
        if(txtGhiChu != null) txtGhiChu.clear();
        listChiTiet.clear();
        lblTongTienThanhToan.setText("0 VNĐ");
        tableChiTietNhap.refresh();
    }
}