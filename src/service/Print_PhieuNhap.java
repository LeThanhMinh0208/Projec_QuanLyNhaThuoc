package service;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import entity.PhieuNhap;
import gui.dialogs.Dialog_ChiTietPhieuNhapController.ChiTietUI;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert.AlertType;
import utils.AlertUtils;

public class Print_PhieuNhap {

    public static void inPhieu(PhieuNhap pn, ObservableList<ChiTietUI> listChiTiet) {
        Document document = new Document(PageSize.A4);
        try {
            // 1. ĐỊNH DẠNG ĐƯỜNG DẪN LƯU (Lưu trực tiếp vào thư mục có sẵn)
            String path = "hoa_don_da_in/PhieuNhap_" + pn.getMaPhieuNhap() + ".pdf";
            
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();

            // =======================================================
            // CÀI ĐẶT FONT TIẾNG VIỆT
            // =======================================================
            Font fontTieuDe = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLUE);
            Font fontThuong = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
            Font fontDam = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
            
            // TIÊU ĐỀ PHIẾU
            Paragraph tieuDe = new Paragraph("PHIẾU NHẬP KHO THUỐC", fontTieuDe);
            tieuDe.setAlignment(Element.ALIGN_CENTER);
            document.add(tieuDe);
            document.add(new Paragraph(" ")); 

            // THÔNG TIN CHUNG
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            DecimalFormat df = new DecimalFormat("#,### VNĐ");

            document.add(new Paragraph("Mã phiếu: " + pn.getMaPhieuNhap(), fontDam));
            document.add(new Paragraph("Nhà cung cấp: " + pn.getNhaCungCap().getTenNhaCungCap(), fontThuong));
            document.add(new Paragraph("Người lập phiếu: " + pn.getNhanVien().getHoTen(), fontThuong));
            document.add(new Paragraph("Ngày nhập: " + sdf.format(pn.getNgayNhap()), fontThuong));
            document.add(new Paragraph(" ")); 

            // =======================================================
            // BẢNG CHI TIẾT HÀNG NHẬP
            // =======================================================
            PdfPTable table = new PdfPTable(7); 
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 3, 1.5f, 1.5f, 1.5f, 2, 2}); 

            String[] headers = {"STT", "Tên Thuốc", "Đơn Vị", "Số Lượng Nhận", "Mã Lô", "Giá Nhập", "Thành Tiền"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fontDam));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setPadding(5);
                table.addCell(cell);
            }

            int stt = 1;
            for (ChiTietUI ct : listChiTiet) {
                table.addCell(new Phrase(String.valueOf(stt++), fontThuong));
                table.addCell(new Phrase(ct.getTenThuoc(), fontThuong));
                table.addCell(new Phrase(ct.getDonVi(), fontThuong));
                
                PdfPCell cellSL = new PdfPCell(new Phrase(String.valueOf(ct.getSoLuong()), fontThuong));
                cellSL.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cellSL);
                
                table.addCell(new Phrase(ct.getMaLo(), fontThuong));
                
                PdfPCell cellGia = new PdfPCell(new Phrase(df.format(ct.getGiaNhap()), fontThuong));
                cellGia.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cellGia);
                
                PdfPCell cellTien = new PdfPCell(new Phrase(ct.getThanhTienStr(), fontThuong));
                cellTien.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cellTien);
            }
            document.add(table);
            document.add(new Paragraph(" "));

            // TỔNG TIỀN & CHỮ KÝ
            Paragraph tongTien = new Paragraph("TỔNG TIỀN THANH TOÁN: " + df.format(pn.getTongTien()), fontDam);
            tongTien.setAlignment(Element.ALIGN_RIGHT);
            document.add(tongTien);
            document.add(new Paragraph(" "));

            PdfPTable tableChuKy = new PdfPTable(2);
            tableChuKy.setWidthPercentage(100);
            
            PdfPCell cellNguoiLap = new PdfPCell(new Phrase("Người lập phiếu\n(Ký và ghi rõ họ tên)", fontDam));
            cellNguoiLap.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellNguoiLap.setBorder(Rectangle.NO_BORDER);
            
            PdfPCell cellNguoiGiao = new PdfPCell(new Phrase("Người giao hàng\n(Ký và ghi rõ họ tên)", fontDam));
            cellNguoiGiao.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellNguoiGiao.setBorder(Rectangle.NO_BORDER);
            
            tableChuKy.addCell(cellNguoiLap);
            tableChuKy.addCell(cellNguoiGiao);
            document.add(tableChuKy);

            document.close();

            // TỰ ĐỘNG MỞ FILE SAU KHI XUẤT
            File file = new File(path);
            if (file.exists()) {
                java.awt.Desktop.getDesktop().open(file);
            }

            AlertUtils.showAlert(AlertType.INFORMATION, "Thành công", "Đã lưu phiếu nhập tại: " + path);

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showAlert(AlertType.ERROR, "Lỗi", "Không thể in phiếu nhập: " + e.getMessage());
        }
    }
}