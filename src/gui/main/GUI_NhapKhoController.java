package gui.main;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import utils.AlertUtils;

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
    @FXML private TableColumn<ChiTietDonDatHang, String> colGiaNhap;

    // ==========================================
    // UI TAB 2: DANH MỤC PHIẾU NHẬP
    // ==========================================
    @FXML private TextField txtTimKiemPhieuNhap;
    @FXML private TableView<PhieuNhap> tablePhieuNhap;
    @FXML private TableColumn<PhieuNhap, Void> colXemChiTietPN;
    @FXML private TableColumn<PhieuNhap, String> colMaPhieuNhap, colNhaCungCapPN, colNhanVienPN;
    @FXML private TableColumn<PhieuNhap, java.sql.Timestamp> colNgayNhapPN;
    @FXML private TableColumn<PhieuNhap, Double> colTongTienPN;

    private DAO_DonDatHang daoDon = new DAO_DonDatHang();
    private DAO_PhieuNhap daoPhieuNhap = new DAO_PhieuNhap();
    private DonDatHang donHienTai;
    private List<ChiTietDonDatHang> listChiTietHienTai;

    private DecimalFormat df = new DecimalFormat("#,### VNĐ");
    private DecimalFormat dfInput = new DecimalFormat("#,###");
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML public void initialize() {
        setupTabs();
        setupTableTaoPhieu();
        setupTableDanhSachPhieuNhap();

        loadDonChoNhap();

        // Khóa ngày nhập kho (mặc định hôm nay)
        if(dpNgayNhap != null) {
            dpNgayNhap.setValue(LocalDate.now());
            dpNgayNhap.setDisable(true);
            dpNgayNhap.setStyle("-fx-opacity: 1; -fx-background-color: #f1f5f9;");
            dpNgayNhap.setConverter(new StringConverter<LocalDate>() {
                @Override public String toString(LocalDate date) { return (date != null) ? dateFormatter.format(date) : ""; }
                @Override public LocalDate fromString(String string) { return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null; }
            });
        }

        cbMaDon.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
				hienThiChiTietDon(newV);
			}
        });
    }

    private void loadDonChoNhap() {
        List<DonDatHang> dsDon = daoDon.getAllDonDatHang().stream()
                .filter(DonDatHang::isChoPhepNhapKho)
                .filter(don -> {
                    String ttDB = don.getTrangThai();
                    if (ttDB == null) {
						return true;
					}
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
        colTenThuoc.setPrefWidth(220);
        colDonVi.setPrefWidth(70);
        colSLDat.setPrefWidth(70);
        colSLNhan.setPrefWidth(80);
        colMaLo.setPrefWidth(120);
        colNgaySX.setPrefWidth(140);
        colHanDung.setPrefWidth(140);
        colGiaNhap.setPrefWidth(140);

        colTenThuoc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getThuoc().getTenThuoc()));
        colDonVi.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDonViQuyDoi().getTenDonVi()));
        colSLDat.setCellValueFactory(new PropertyValueFactory<>("soLuongDat"));
        colSLDat.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        // Ô SỐ LƯỢNG NHẬN
        colSLNhan.setCellValueFactory(new PropertyValueFactory<>("soLuongDaNhan"));
        colSLNhan.setCellFactory(column -> new TableCell<ChiTietDonDatHang, Integer>() {
            private final TextField textField = new TextField();
            {
                textField.getStyleClass().add("table-input-active");
                textField.setAlignment(Pos.CENTER);
                textField.textProperty().addListener((obs, oldV, newV) -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        try {
                            int val = newV.isEmpty() ? 0 : Integer.parseInt(newV);
                            getTableRow().getItem().setSoLuongDaNhan(val);
                            tinhToanTongTienHienThi();
                        } catch (NumberFormatException e) { textField.setText(oldV); }
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

        // Ô MÃ LÔ
        colMaLo.setCellValueFactory(new PropertyValueFactory<>("maLo"));
        colMaLo.setCellFactory(column -> new TableCell<ChiTietDonDatHang, String>() {
            private final TextField textField = new TextField();
            {
                textField.getStyleClass().add("table-input-active");
                textField.setPromptText("Nhập mã...");
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

        // 🚨 Ô NGÀY SX (SỔ LỊCH + ĐIỀU KIỆN TRƯỚC HÔM NAY) 🚨
        colNgaySX.setCellValueFactory(new PropertyValueFactory<>("ngaySanXuatTemp"));
        colNgaySX.setCellFactory(column -> new TableCell<ChiTietDonDatHang, String>() {
            private final DatePicker datePicker = new DatePicker();
            {
                datePicker.getStyleClass().add("table-date-active");
                datePicker.setPrefWidth(130);

                // Format dd/MM/yyyy
                datePicker.setConverter(new StringConverter<LocalDate>() {
                    @Override public String toString(LocalDate date) { return (date != null) ? dateFormatter.format(date) : ""; }
                    @Override public LocalDate fromString(String string) { return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null; }
                });

                datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
                    if (getTableRow() != null && getTableRow().getItem() != null && newDate != null) {
                        getTableRow().getItem().setNgaySanXuatTemp(newDate.toString()); // Lưu DB dạng yyyy-MM-dd
                    }
                });

                // CHẶN NGÀY: Chỉ cho chọn ngày trước hôm nay
                datePicker.setDayCellFactory(picker -> new DateCell() {
                    @Override
					public void updateItem(LocalDate date, boolean empty) {
                        super.updateItem(date, empty);
                        // Disable nếu ngày đó lớn hơn hoặc bằng hôm nay (nghĩa là chỉ nhận quá khứ)
                        setDisable(empty || date.compareTo(LocalDate.now()) >= 0);
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
                    if (dateStr != null && !dateStr.isEmpty() && dateStr.contains("-")) {
                        try { datePicker.setValue(LocalDate.parse(dateStr)); } catch(Exception e) {}
                    } else {
                        datePicker.setValue(null);
                    }
                    setGraphic(datePicker);
                }
            }
        });

        // 🚨 Ô HSD (SỔ LỊCH + ĐIỀU KIỆN SAU NSX) 🚨
        colHanDung.setCellValueFactory(new PropertyValueFactory<>("hanSuDung"));
        colHanDung.setCellFactory(column -> new TableCell<ChiTietDonDatHang, String>() {
            private final DatePicker datePicker = new DatePicker();
            {
                datePicker.getStyleClass().add("table-date-active");
                datePicker.setPrefWidth(130);

                datePicker.setConverter(new StringConverter<LocalDate>() {
                    @Override public String toString(LocalDate date) { return (date != null) ? dateFormatter.format(date) : ""; }
                    @Override public LocalDate fromString(String string) { return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null; }
                });

                datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
                    if (getTableRow() != null && getTableRow().getItem() != null && newDate != null) {
                        getTableRow().getItem().setHanSuDung(newDate.toString());
                    }
                });

                // MẸO UX: Khi vừa click mở lịch HSD, kiểm tra xem NSX là ngày nào để khóa những ngày trước đó
                datePicker.setOnShowing(event -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        String nsxStr = getTableRow().getItem().getNgaySanXuatTemp();
                        if (nsxStr != null && !nsxStr.isEmpty()) {
                            LocalDate nsx = LocalDate.parse(nsxStr);
                            datePicker.setDayCellFactory(picker -> new DateCell() {
                                @Override
								public void updateItem(LocalDate date, boolean empty) {
                                    super.updateItem(date, empty);
                                    // Disable nếu ngày HSD nhỏ hơn hoặc bằng NSX
                                    setDisable(empty || date.compareTo(nsx) <= 0);
                                }
                            });
                        }
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
                    if (dateStr != null && !dateStr.isEmpty() && dateStr.contains("-")) {
                        try { datePicker.setValue(LocalDate.parse(dateStr)); } catch(Exception e) {}
                    } else {
                        datePicker.setValue(null);
                    }
                    setGraphic(datePicker);
                }
            }
        });

        // Ô GIÁ NHẬP
        colGiaNhap.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getDonGiaDuKien())));
        colGiaNhap.setCellFactory(column -> new TableCell<ChiTietDonDatHang, String>() {
            private final TextField textField = new TextField();
            {
                textField.getStyleClass().add("table-input-active");
                textField.setAlignment(Pos.CENTER_RIGHT);
                textField.textProperty().addListener((obs, oldV, newV) -> {
                    if (getTableRow() != null && getTableRow().getItem() != null && newV != null && !newV.isEmpty()) {
                        String cleanStr = newV.replaceAll("[^\\d]", "");
                        try {
                            if (!cleanStr.isEmpty()) {
                                long val = Long.parseLong(cleanStr);
                                String formatted = dfInput.format(val).replace(',', '.');
                                if (!newV.equals(formatted)) {
                                    textField.setText(formatted);
                                }
                                getTableRow().getItem().setDonGiaDuKien(val);
                                tinhToanTongTienHienThi();
                            }
                        } catch (NumberFormatException e) {
                            textField.setText(oldV);
                        }
                    }
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    double val = getTableRow().getItem().getDonGiaDuKien();
                    textField.setText(dfInput.format(val).replace(',', '.'));
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
        if(lblTongTien != null) {
			lblTongTien.setText(df.format(tong));
		}
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

                // 🚨 KIỂM TRA ĐIỀU KIỆN NGÀY THÁNG LÚC LƯU 🚨
                try {
                    if (ct.getNgaySanXuatTemp() == null || ct.getHanSuDung() == null) {
						throw new Exception();
					}

                    LocalDate nsx = LocalDate.parse(ct.getNgaySanXuatTemp());
                    LocalDate hsd = LocalDate.parse(ct.getHanSuDung());
                    LocalDate today = LocalDate.now();

                    if (!nsx.isBefore(today)) {
                        AlertUtils.showAlert(Alert.AlertType.WARNING, "Lỗi Ngày", "Ngày sản xuất của lô " + ct.getMaLo() + " phải TRƯỚC ngày hôm nay!");
                        return;
                    }
                    if (!hsd.isAfter(nsx)) {
                        AlertUtils.showAlert(Alert.AlertType.WARNING, "Lỗi Ngày", "Hạn sử dụng của lô " + ct.getMaLo() + " phải SAU ngày sản xuất!");
                        return;
                    }
                } catch (Exception e) {
                    AlertUtils.showAlert(Alert.AlertType.WARNING, "Thiếu dữ liệu", "Vui lòng chọn đầy đủ Ngày SX và HSD cho lô " + ct.getMaLo());
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
            } else {
				return;
			}
        }

        PhieuNhap pn = new PhieuNhap();
        pn.setDonDatHang(donHienTai);
        pn.setNhaCungCap(donHienTai.getNhaCungCap());

        entity.NhanVien nv = new entity.NhanVien();
        nv.setMaNhanVien("NV001");
        pn.setNhanVien(nv);

        boolean tc = daoPhieuNhap.luuPhieuNhapVaCapNhatDon(pn, listChiTietHienTai, donHienTai, soNgayHen);

        if (tc) {
            double tongTienNhap = 0;
            for (ChiTietDonDatHang ct : listChiTietHienTai) {
                tongTienNhap += ct.getSoLuongDaNhan() * ct.getDonGiaDuKien();
            }

            dao.DAO_NhaCungCap daoNCC = new dao.DAO_NhaCungCap();
            daoNCC.congCongNoNhaCungCap(donHienTai.getNhaCungCap().getMaNhaCungCap(), tongTienNhap);

            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Thành công",
                "Đã lưu phiếu nhập kho!\nĐã cộng dồn " + df.format(tongTienNhap) + " vào công nợ của NCC.");

            loadDonChoNhap();
            handleHuyNhapKho(null);

            if(txtTimKiemPhieuNhap != null) {
				txtTimKiemPhieuNhap.clear();
			}
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
        if(lblTongTien != null) {
			lblTongTien.setText("0 VNĐ");
		}
        donHienTai = null;
    }

    private void setupTabs() {
        tabGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) { oldT.setSelected(true); return; }
            if (viewTaoPhieu != null && viewDanhSach != null) {
                boolean isTao = (newT == tabTaoPhieu);
                viewTaoPhieu.setVisible(isTao);
                viewTaoPhieu.setManaged(isTao);
                viewDanhSach.setVisible(!isTao);
                viewDanhSach.setManaged(!isTao);

                if (!isTao) {
                    loadDanhSachPhieuNhap();
                }
            }
        });
    }

    private void setupTableDanhSachPhieuNhap() {
        if (tablePhieuNhap == null) {
			return;
		}

        colMaPhieuNhap.setCellValueFactory(new PropertyValueFactory<>("maPhieuNhap"));
        colMaPhieuNhap.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        colNhaCungCapPN.setCellValueFactory(c -> {
            if(c.getValue().getNhaCungCap() != null) {
				return new SimpleStringProperty(c.getValue().getNhaCungCap().getTenNhaCungCap());
			}
            return new SimpleStringProperty("N/A");
        });

        colNhanVienPN.setCellValueFactory(c -> {
            if(c.getValue().getNhanVien() != null) {
				return new SimpleStringProperty(c.getValue().getNhanVien().getHoTen());
			}
            return new SimpleStringProperty("N/A");
        });

        colNgayNhapPN.setCellValueFactory(new PropertyValueFactory<>("ngayNhap"));
        colNgayNhapPN.setStyle("-fx-alignment: CENTER;");
        colNgayNhapPN.setCellFactory(column -> new TableCell<PhieuNhap, java.sql.Timestamp>() {
            @Override
			protected void updateItem(java.sql.Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : sdf.format(item));
            }
        });

        colTongTienPN.setCellValueFactory(new PropertyValueFactory<>("tongTien"));
        colTongTienPN.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold; -fx-text-fill: #be123c;");
        colTongTienPN.setCellFactory(column -> new TableCell<PhieuNhap, Double>() {
            @Override
			protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : df.format(item));
            }
        });

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
            @Override
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

        if(txtTimKiemPhieuNhap != null) {
            txtTimKiemPhieuNhap.textProperty().addListener((obs, oldText, newText) -> {
                loadDanhSachPhieuNhap(newText);
            });
        }
    }

    private void loadDanhSachPhieuNhap() {
        loadDanhSachPhieuNhap("");
    }

    private void loadDanhSachPhieuNhap(String tuKhoa) {
        if (daoPhieuNhap == null || tablePhieuNhap == null) {
			return;
		}
        List<PhieuNhap> ds = daoPhieuNhap.getAllPhieuNhap(tuKhoa);
        tablePhieuNhap.setItems(FXCollections.observableArrayList(ds));
    }

    private void moDialogChiTietPhieuNhap(PhieuNhap phieuNhap) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_ChiTietPhieuNhap.fxml"));
            javafx.scene.Parent root = loader.load();

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