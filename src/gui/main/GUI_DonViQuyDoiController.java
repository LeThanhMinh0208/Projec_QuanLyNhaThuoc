package gui.main;

import java.util.ArrayList;
import java.util.List;

import dao.DAO_DonViQuyDoi;
import dao.DAO_Thuoc;
import entity.DonViQuyDoi;
import entity.Thuoc;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utils.AlertUtils;

public class GUI_DonViQuyDoiController {

    @FXML private TextField txtTimKiem;
    @FXML private TableView<DonViQuyDoiRow> tableQuyDoi;
    @FXML private TableColumn<DonViQuyDoiRow, Integer> colStt;
    @FXML private TableColumn<DonViQuyDoiRow, String> colMaThuoc;
    @FXML private TableColumn<DonViQuyDoiRow, String> colTenThuoc;
    @FXML private TableColumn<DonViQuyDoiRow, String> colDonViBac1;
    @FXML private TableColumn<DonViQuyDoiRow, String> colDonViBac2;
    @FXML private TableColumn<DonViQuyDoiRow, String> colDonViBac3;
    @FXML private TableColumn<DonViQuyDoiRow, Void> colThaoTac;

    private final DAO_DonViQuyDoi daoDonViQuyDoi = new DAO_DonViQuyDoi();
    private final DAO_Thuoc daoThuoc = new DAO_Thuoc();
    private final ObservableList<DonViQuyDoiRow> masterData = FXCollections.observableArrayList();
    private FilteredList<DonViQuyDoiRow> filteredData;

    @FXML
    public void initialize() {
        setupTable();
        loadData();
        setupSearch();
    }

    public void setThuoc(Thuoc thuoc) {
        if (thuoc == null) {
            return;
        }
        if (txtTimKiem != null) {
            txtTimKiem.setText(thuoc.getMaThuoc());
        }
    }

    private void setupTable() {
        colStt.setCellValueFactory(new PropertyValueFactory<>("stt"));
        colMaThuoc.setCellValueFactory(new PropertyValueFactory<>("maThuoc"));
        colTenThuoc.setCellValueFactory(new PropertyValueFactory<>("tenThuoc"));
        colDonViBac1.setCellValueFactory(new PropertyValueFactory<>("donViBac1"));
        colDonViBac2.setCellValueFactory(new PropertyValueFactory<>("donViBac2"));
        colDonViBac3.setCellValueFactory(new PropertyValueFactory<>("donViBac3"));

        colThaoTac.setCellFactory(column -> new TableCell<>() {
            private final Button btnSua = new Button("Sửa");
            private final Button btnXoa = new Button("Xóa");
            private final HBox actions = new HBox(10, btnSua, btnXoa); // Tăng khoảng cách lên 10

            {
                // Gán class CSS thay vì setStyle cứng
                btnSua.getStyleClass().add("btn-edit-pill");
                btnXoa.getStyleClass().add("btn-delete-pill");
                actions.setAlignment(Pos.CENTER);

                btnSua.setOnAction(e -> {
                    DonViQuyDoiRow row = getTableView().getItems().get(getIndex());
                    moPopupSua(row);
                });

                btnXoa.setOnAction(e -> {
                    DonViQuyDoiRow row = getTableView().getItems().get(getIndex());
                    xoaDonViMoRong(row);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(actions);
                }
            }
        });
    }

    private void setupSearch() {
        filteredData = new FilteredList<>(masterData, item -> true);
        txtTimKiem.textProperty().addListener((obs, oldValue, newValue) -> {
            String keyword = newValue == null ? "" : newValue.trim().toLowerCase();
            filteredData.setPredicate(item -> {
                if (keyword.isEmpty()) {
                    return true;
                }
                return item.getMaThuoc().toLowerCase().contains(keyword)
                        || item.getTenThuoc().toLowerCase().contains(keyword)
                        || item.getDonViBac1().toLowerCase().contains(keyword)
                        || item.getDonViBac2().toLowerCase().contains(keyword)
                        || item.getDonViBac3().toLowerCase().contains(keyword);
            });
        });

        SortedList<DonViQuyDoiRow> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableQuyDoi.comparatorProperty());
        tableQuyDoi.setItems(sortedData);
    }

    private void loadData() {
        masterData.clear();

        // 1. Lấy tất cả thuốc (1 query)
        List<Thuoc> dsThuoc = daoThuoc.getAllThuocTatCa();

        // 2. Lấy tất cả đơn vị quy đổi (1 query)
        List<DonViQuyDoi> allUnits = daoDonViQuyDoi.getAllDonViQuyDoi();

        // 3. Nhóm đơn vị quy đổi theo maThuoc bằng Map để tra cứu nhanh O(1)
        java.util.Map<String, ArrayList<DonViQuyDoi>> unitMap = new java.util.HashMap<>();
        for (DonViQuyDoi dv : allUnits) {
            unitMap.computeIfAbsent(dv.getMaThuoc(), k -> new ArrayList<>()).add(dv);
        }

        // 4. Sắp xếp danh sách thuốc
        dsThuoc.sort((a, b) -> {
            String maA = a == null ? "" : a.getMaThuoc();
            String maB = b == null ? "" : b.getMaThuoc();
            return maB.compareTo(maA);
        });

        // 5. Build dữ liệu cho table
        int stt = 1;
        for (Thuoc thuoc : dsThuoc) {
            ArrayList<DonViQuyDoi> donVi = unitMap.getOrDefault(thuoc.getMaThuoc(), new ArrayList<>());

            // Đảm bảo list donVi được sắp xếp theo tyLeQuyDoi ASC (đã sort ở SQL nhưng check lại cho chắc)
            donVi.sort(java.util.Comparator.comparingInt(DonViQuyDoi::getTyLeQuyDoi));

            String bac1 = formatBac1(thuoc, donVi);
            String bac2 = "--";
            String bac3 = "--";

            ArrayList<DonViQuyDoi> donViMoRong = new ArrayList<>();
            for (DonViQuyDoi dv : donVi) {
                if (dv.getTyLeQuyDoi() > 1) {
                    donViMoRong.add(dv);
                }
            }
            if (!donViMoRong.isEmpty()) {
                bac2 = formatDonVi(donViMoRong.get(0));
            }
            if (donViMoRong.size() > 1) {
                bac3 = formatDonVi(donViMoRong.get(1));
            }

            masterData.add(new DonViQuyDoiRow(stt++, thuoc, donVi, bac1, bac2, bac3));
        }
    }

    private void moPopupSua(DonViQuyDoiRow row) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_SuaDonViQuyDoi.fxml"));
            Parent root = loader.load();

            gui.dialogs.Dialog_SuaDonViQuyDoiController controller = loader.getController();
            controller.setAddMode(false);
            controller.setDataForEdit(row.getThuoc(), row.getDanhSachDonVi());
            controller.setOnSaved(this::loadData);

            Stage stage = new Stage();
            stage.setTitle("Sửa Quy Đổi Đơn Vị Thuốc");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception ex) {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở form sửa quy đổi.");
        }
    }

    private void xoaDonViMoRong(DonViQuyDoiRow row) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Xóa toàn bộ đơn vị quy đổi thêm của thuốc " + row.getTenThuoc() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText(null);

        if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
            return;
        }

        boolean ok = daoDonViQuyDoi.deleteDonViMoRongByMaThuoc(row.getMaThuoc());
        if (ok) {
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa đơn vị mở rộng của thuốc.");
            loadData();
        } else {
            String message = daoDonViQuyDoi.getLastError() == null
                    ? "Không thể xóa đơn vị mở rộng."
                    : daoDonViQuyDoi.getLastError();
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", message);
        }
    }

    private String formatBac1(Thuoc thuoc, List<DonViQuyDoi> dsDonVi) {
        for (DonViQuyDoi dv : dsDonVi) {
            if (dv.getTyLeQuyDoi() == 1) {
                return formatDonVi(dv);
            }
        }
        return (thuoc.getDonViCoBan() == null || thuoc.getDonViCoBan().isBlank())
                ? "--"
                : thuoc.getDonViCoBan() + " (1)";
    }

    private String formatDonVi(DonViQuyDoi dv) {
        return dv.getTenDonVi() + " (" + dv.getTyLeQuyDoi() + ")";
    }

    public static class DonViQuyDoiRow {
        private final int stt;
        private final Thuoc thuoc;
        private final ArrayList<DonViQuyDoi> danhSachDonVi;
        private final String donViBac1;
        private final String donViBac2;
        private final String donViBac3;

        public DonViQuyDoiRow(int stt, Thuoc thuoc, ArrayList<DonViQuyDoi> danhSachDonVi,
                              String donViBac1, String donViBac2, String donViBac3) {
            this.stt = stt;
            this.thuoc = thuoc;
            this.danhSachDonVi = new ArrayList<>(danhSachDonVi);
            this.donViBac1 = donViBac1;
            this.donViBac2 = donViBac2;
            this.donViBac3 = donViBac3;
        }

        public int getStt() {
            return stt;
        }

        public Thuoc getThuoc() {
            return thuoc;
        }

        public String getMaThuoc() {
            return thuoc.getMaThuoc();
        }

        public String getTenThuoc() {
            return thuoc.getTenThuoc();
        }

        public ArrayList<DonViQuyDoi> getDanhSachDonVi() {
            return new ArrayList<>(danhSachDonVi);
        }

        public String getDonViBac1() {
            return donViBac1;
        }

        public String getDonViBac2() {
            return donViBac2;
        }

        public String getDonViBac3() {
            return donViBac3;
        }
    }
}