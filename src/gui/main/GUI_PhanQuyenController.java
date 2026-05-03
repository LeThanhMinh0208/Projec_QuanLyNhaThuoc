package gui.main;

import dao.DAO_NhanVien;
import dao.DAO_NhatKyHoatDong;
import dao.DAO_PhanQuyen;
import entity.NhanVien;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GUI_PhanQuyenController {

    @FXML private TextField txtTimKiem;
    @FXML private TableView<NhanVien> tableNhanVien;
    @FXML private TableColumn<NhanVien, String> colMa, colHoTen, colChucVu;
    @FXML private TreeView<String> treeQuyen;
    @FXML private Label lblNhanVienDangChon;
    @FXML private Label lblMaNV, lblChucVu;

    private DAO_NhanVien daoNhanVien = new DAO_NhanVien();
    private DAO_PhanQuyen daoPhanQuyen = new DAO_PhanQuyen();
    private ObservableList<NhanVien> dsNhanVien = FXCollections.observableArrayList();
    private NhanVien nvDangChon = null;

    // Cấu trúc cây quyền
    private static final LinkedHashMap<String, QuyenNode> CAY_QUYEN = new LinkedHashMap<>();

    static {
        CAY_QUYEN.put("QLBH", new QuyenNode("Quản Lý Bán Hàng", "🛒", new String[][]{
            {"QLBH.LAP_HOA_DON", "Lập Hóa Đơn"},
            {"QLBH.DANH_SACH_HOA_DON", "Danh Sách Hóa Đơn"},
            {"QLBH.XU_LY_DOI_TRA", "Xử Lý Đổi Trả"}
        }));
        CAY_QUYEN.put("QLBG", new QuyenNode("Quản Lý Bảng Giá", "💲", new String[][]{
            {"QLBG.DANH_SACH_BANG_GIA", "Danh Sách Bảng Giá"},
            {"QLBG.TAO_BANG_GIA_MOI", "Tạo Bảng Giá Mới"}
        }));
        CAY_QUYEN.put("QLDT", new QuyenNode("Quản Lý Đơn Thuốc", "📋", new String[][]{
            {"QLDT.DANH_MUC_DON_THUOC", "Danh Mục Đơn Thuốc"}
        }));
        CAY_QUYEN.put("QLT", new QuyenNode("Quản Lý Thuốc", "💊", new String[][]{
            {"QLT.DANH_MUC_THUOC", "Danh Mục Thuốc"},
            {"QLT.DON_VI_QUY_DOI", "Quản Lý Đơn Vị Quy Đổi"},
            {"QLT.LO_THUOC", "Quản Lý Lô Thuốc"}
        }));
        CAY_QUYEN.put("QLK", new QuyenNode("Quản Lý Kho", "📦", new String[][]{
            {"QLK.DANH_MUC_KHO", "Danh Mục Kho"},
            {"QLK.DON_DAT_HANG", "Quản Lý Đơn Đặt Hàng"},
            {"QLK.NHAP_KHO", "Nhập Kho"},
            {"QLK.XUAT_KHO", "Xuất Kho"}
        }));
        CAY_QUYEN.put("QLKH", new QuyenNode("Quản Lý Khách Hàng", "👥", new String[][]{
            {"QLKH.DANH_MUC_KHACH_HANG", "Danh Mục Khách Hàng"},
            {"QLKH.LICH_SU_GIAO_DICH", "Lịch Sử Giao Dịch"}
        }));
        CAY_QUYEN.put("QLNCC", new QuyenNode("Quản Lý Nhà Cung Cấp", "🏭", new String[][]{
            {"QLNCC.DANH_MUC_NHA_CUNG_CAP", "Danh Mục Nhà Cung Cấp"},
            {"QLNCC.CONG_NO", "Quản Lý Công Nợ"}
        }));
        CAY_QUYEN.put("QLND", new QuyenNode("Quản Lý Người Dùng", "👤", new String[][]{
            {"QLND.DANH_MUC_NGUOI_DUNG", "Danh Mục Người Dùng"},
            {"QLND.PHAN_QUYEN", "Phân Quyền Người Dùng"},
            {"QLND.NHAT_KY", "Nhật Ký Hoạt Động"}
        }));
        CAY_QUYEN.put("BCTK", new QuyenNode("Báo Cáo Thống Kê", "📊", new String[][]{
            {"BCTK.DOANH_THU", "Thống Kê Doanh Thu"},
            {"BCTK.HANG_HOA", "Thống Kê Hàng Hóa"},
            {"BCTK.TON_KHO", "Thống Kê Tồn Kho"}
        }));
    }

    @FXML
    public void initialize() {
        setupTable();
        loadNhanVien();
        setupSearch();
        buildTree(new ArrayList<>());

        // Khi click chọn nhân viên -> load quyền
        tableNhanVien.getSelectionModel().selectedItemProperty().addListener((obs, old, nv) -> {
            if (nv != null) {
                nvDangChon = nv;
                lblNhanVienDangChon.setText(nv.getHoTen());
                lblMaNV.setText(nv.getMaNhanVien());
                lblChucVu.setText(nv.getChucVu() != null ? nv.getChucVu() : "Nhân Viên");
                List<String> dsQuyen = daoPhanQuyen.getQuyenByNhanVien(nv.getMaNhanVien());
                buildTree(dsQuyen);
            }
        });
    }

    private void setupTable() {
        colMa.setCellValueFactory(new PropertyValueFactory<>("maNhanVien"));
        colHoTen.setCellValueFactory(new PropertyValueFactory<>("hoTen"));
        colChucVu.setCellValueFactory(new PropertyValueFactory<>("chucVu"));
    }

    private void loadNhanVien() {
        dsNhanVien.clear();
        dsNhanVien.addAll(daoNhanVien.getChiNhanVien());
        tableNhanVien.setItems(dsNhanVien);
    }

    private void setupSearch() {
        FilteredList<NhanVien> filtered = new FilteredList<>(dsNhanVien, p -> true);
        txtTimKiem.textProperty().addListener((obs, old, val) -> {
            filtered.setPredicate(nv -> {
                if (val == null || val.isEmpty()) return true;
                String key = val.toLowerCase();
                return nv.getHoTen().toLowerCase().contains(key) ||
                       nv.getMaNhanVien().toLowerCase().contains(key);
            });
        });
        SortedList<NhanVien> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tableNhanVien.comparatorProperty());
        tableNhanVien.setItems(sorted);
    }

    /**
     * Build cây quyền bằng CheckBoxTreeItem + CheckBoxTreeCell có sẵn.
     * CheckBoxTreeItem(independent=false) tự propagate cha↔con.
     */
    private void buildTree(List<String> dsQuyenHienCo) {
        CheckBoxTreeItem<String> root = new CheckBoxTreeItem<>("Tất Cả Quyền");
        root.setExpanded(true);

        for (Map.Entry<String, QuyenNode> entry : CAY_QUYEN.entrySet()) {
            QuyenNode node = entry.getValue();

            CheckBoxTreeItem<String> parentItem = new CheckBoxTreeItem<>(node.icon + "  " + node.tenHienThi);
            parentItem.setExpanded(true);

            for (String[] child : node.children) {
                String tenCon = child[1];
                CheckBoxTreeItem<String> childItem = new CheckBoxTreeItem<>("      " + tenCon);
                parentItem.getChildren().add(childItem);
            }
            root.getChildren().add(parentItem);
        }

        // Đánh dấu chọn (tick) sau khi cây đã được nối đầy đủ để tự động cascade
        int chaIndex = 0;
        for (Map.Entry<String, QuyenNode> entry : CAY_QUYEN.entrySet()) {
            QuyenNode node = entry.getValue();
            CheckBoxTreeItem<String> parentItem = (CheckBoxTreeItem<String>) root.getChildren().get(chaIndex);
            for (int i = 0; i < node.children.length; i++) {
                String maCon = node.children[i][0];
                if (dsQuyenHienCo.contains(maCon)) {
                    CheckBoxTreeItem<String> childItem = (CheckBoxTreeItem<String>) parentItem.getChildren().get(i);
                    childItem.setSelected(true);
                }
            }
            chaIndex++;
        }

        treeQuyen.setRoot(root);
        treeQuyen.setShowRoot(false);
        // Dùng CheckBoxTreeCell có sẵn → KHÔNG có zombie listener bug
        treeQuyen.setCellFactory(CheckBoxTreeCell.forTreeView());
    }

    /**
     * Lưu quyền đã chọn
     */
    @FXML
    void handleLuuQuyen() {
        if (nvDangChon == null) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng chọn nhân viên trước!").show();
            return;
        }

        List<String> dsQuyenMoi = new ArrayList<>();
        List<String> dsTenQuyenMoi = new ArrayList<>();
        CheckBoxTreeItem<String> root = (CheckBoxTreeItem<String>) treeQuyen.getRoot();

        int chaIndex = 0;
        for (Map.Entry<String, QuyenNode> entry : CAY_QUYEN.entrySet()) {
            String maCha = entry.getKey();
            QuyenNode node = entry.getValue();
            CheckBoxTreeItem<String> parentItem = (CheckBoxTreeItem<String>) root.getChildren().get(chaIndex);

            boolean tatCaConDuocChon = true;
            boolean coItNhatMotConDuocChon = false;

            for (int i = 0; i < node.children.length; i++) {
                CheckBoxTreeItem<String> childItem = (CheckBoxTreeItem<String>) parentItem.getChildren().get(i);
                if (childItem.isSelected()) {
                    dsQuyenMoi.add(node.children[i][0]);
                    dsTenQuyenMoi.add(node.children[i][1]);
                    coItNhatMotConDuocChon = true;
                } else {
                    tatCaConDuocChon = false;
                }
            }

            if (tatCaConDuocChon && coItNhatMotConDuocChon) {
                dsQuyenMoi.add(maCha);
            }

            chaIndex++;
        }

        if (daoPhanQuyen.capNhatQuyen(nvDangChon.getMaNhanVien(), dsQuyenMoi)) {
            String dsQuyenStr = String.join(", ", dsTenQuyenMoi);
            String moTa = "Cập nhật phân quyền cho nhân viên " + nvDangChon.getHoTen() + ".\nDanh sách quyền (" + dsTenQuyenMoi.size() + "): " + dsQuyenStr;
            DAO_NhatKyHoatDong.ghiLog("CAP_NHAT_QUYEN", "Nhân Viên", nvDangChon.getMaNhanVien(), moTa);
            new Alert(Alert.AlertType.INFORMATION, "Lưu phân quyền thành công!").show();
        } else {
            new Alert(Alert.AlertType.ERROR, "Lưu phân quyền thất bại!").show();
        }
    }

    @FXML
    void handleChonTatCa() {
        CheckBoxTreeItem<String> root = (CheckBoxTreeItem<String>) treeQuyen.getRoot();
        if (root == null) return;
        for (TreeItem<String> parent : root.getChildren()) {
            if (parent instanceof CheckBoxTreeItem) {
                ((CheckBoxTreeItem<String>) parent).setSelected(true);
            }
        }
    }

    @FXML
    void handleBoChonTatCa() {
        CheckBoxTreeItem<String> root = (CheckBoxTreeItem<String>) treeQuyen.getRoot();
        if (root == null) return;
        for (TreeItem<String> parent : root.getChildren()) {
            if (parent instanceof CheckBoxTreeItem) {
                ((CheckBoxTreeItem<String>) parent).setSelected(false);
            }
        }
    }

    /**
     * Helper class lưu cấu trúc cây quyền
     */
    private static class QuyenNode {
        String tenHienThi;
        String icon;
        String[][] children;

        QuyenNode(String tenHienThi, String icon, String[][] children) {
            this.tenHienThi = tenHienThi;
            this.icon = icon;
            this.children = children;
        }
    }
}
