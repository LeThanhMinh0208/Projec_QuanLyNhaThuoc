package service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;
import entity.PhieuKiemKe;
import gui.dialogs.Dialog_ChiTietPhieuKiemKeController.ChiTiet;
import javafx.collections.ObservableList;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.awt.Desktop;
import java.io.File;

public class Print_PhieuKiemKe {

    public static boolean inPhieu(PhieuKiemKe pk, ObservableList<ChiTiet> listChiTiet, String path) {
        Document document = new Document(PageSize.A4, 40, 40, 50, 50); 
        try {
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();

            // =======================================================
            // 1. CÀI ĐẶT FONT TIẾNG VIỆT
            // =======================================================
            BaseFont bf = BaseFont.createFont("C:\\Windows\\Fonts\\arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font fontShopName = new Font(bf, 18, Font.BOLD, BaseColor.BLACK);
            Font fontTitle = new Font(bf, 16, Font.BOLD, BaseColor.BLACK);
            Font fontNormal = new Font(bf, 11, Font.NORMAL, BaseColor.BLACK);
            Font fontBold = new Font(bf, 11, Font.BOLD, BaseColor.BLACK);
            Font fontItalic = new Font(bf, 10, Font.ITALIC, BaseColor.DARK_GRAY);
            
            BaseColor headerBlue = new BaseColor(43, 139, 198); 
            Font fontTableHeader = new Font(bf, 11, Font.BOLD, BaseColor.WHITE);

            // =======================================================
            // 2. HEADER THƯƠNG HIỆU
            // =======================================================
            Paragraph shopName = new Paragraph("NHÀ THUỐC LONG NGUYÊN", fontShopName);
            shopName.setAlignment(Element.ALIGN_CENTER);
            document.add(shopName);

            Paragraph address = new Paragraph("Đ/c: 12 Nguyễn Văn Bảo, P.4, Gò Vấp, TP.HCM\nHotline: 0123.456.789", fontItalic);
            address.setAlignment(Element.ALIGN_CENTER);
            document.add(address);
            
            document.add(new Paragraph(" "));

            DottedLineSeparator separator = new DottedLineSeparator();
            separator.setPercentage(100);
            separator.setLineColor(BaseColor.GRAY);
            document.add(new Chunk(separator));

            // =======================================================
            // 3. TIÊU ĐỀ PHIẾU
            // =======================================================
            Paragraph title = new Paragraph("PHIẾU KIỂM KÊ KHO", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(15f);
            title.setSpacingAfter(20f);
            document.add(title);

            // =======================================================
            // 4. THÔNG TIN CHUNG
            // =======================================================
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{1.5f, 4f});
            infoTable.setSpacingAfter(15f);

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd/MM/yyyy");

            addInfoRow(infoTable, "Mã phiếu:", pk.getMaPhieuKiemKe(), fontBold);
            addInfoRow(infoTable, "Ngày lập:", sdf.format(pk.getNgayTao()), fontNormal);
            addInfoRow(infoTable, "Người lập:", pk.getNhanVienTao() != null ? pk.getNhanVienTao().getHoTen() : "---", fontNormal);
            
            String nguoiDuyet = pk.getNhanVienDuyet() != null ? pk.getNhanVienDuyet().getHoTen() : "Chưa duyệt";
            String ngayDuyet = pk.getNgayDuyet() != null ? sdf.format(pk.getNgayDuyet()) : "---";
            
            addInfoRow(infoTable, "Người duyệt:", nguoiDuyet, fontNormal);
            addInfoRow(infoTable, "Ngày duyệt:", ngayDuyet, fontNormal);

            document.add(infoTable);

            // =======================================================
            // 5. BẢNG CHI TIẾT KIỂM KÊ (ĐÃ TÁCH CỘT)
            // =======================================================
            PdfPTable table = new PdfPTable(7); // Tăng lên 7 cột
            table.setWidthPercentage(100);
            // Chia lại tỷ lệ: STT, Mã Lô, Tên, Thực Tế, Lệch, Lý do, Ghi chú
            table.setWidths(new float[]{0.8f, 1.2f, 3.0f, 1.0f, 1.0f, 1.5f, 1.5f}); 

            String[] headers = {"STT", "Mã lô", "Tên thuốc", "Thực tế", "Độ lệch", "Lý do", "Ghi chú"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fontTableHeader));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(headerBlue);
                cell.setPaddingBottom(8f); cell.setPaddingTop(5f);
                cell.setBorderColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }

            int stt = 1;
            int matHangLech = 0;
            for (ChiTiet ct : listChiTiet) {
                table.addCell(createDataCell(String.valueOf(stt++), fontNormal, Element.ALIGN_CENTER));
                table.addCell(createDataCell(ct.getMaLo(), fontNormal, Element.ALIGN_CENTER));
                table.addCell(createDataCell(ct.getTen(), fontNormal, Element.ALIGN_LEFT));
                table.addCell(createDataCell(String.valueOf(ct.getKiemTra()), fontBold, Element.ALIGN_CENTER));
                
                String lechStr = String.valueOf(ct.getLech());
                if (ct.getLech() > 0) { lechStr = "+" + ct.getLech(); matHangLech++; } 
                else if (ct.getLech() < 0) { matHangLech++; }
                
                table.addCell(createDataCell(lechStr, fontBold, Element.ALIGN_CENTER));
                
                // Tách riêng 2 cột ra đây
                table.addCell(createDataCell(ct.getLyDo() != null ? ct.getLyDo() : "", fontNormal, Element.ALIGN_LEFT));
                table.addCell(createDataCell(ct.getGhiChu() != null ? ct.getGhiChu() : "", fontNormal, Element.ALIGN_LEFT));
            }
            document.add(table);

            // =======================================================
            // 6. THỐNG KÊ TỔNG
            // =======================================================
            document.add(new Paragraph(" "));
            Paragraph pTong = new Paragraph("Tổng số mặt hàng: " + listChiTiet.size() + "  |  Số mặt hàng lệch: " + matHangLech, fontBold);
            pTong.setAlignment(Element.ALIGN_RIGHT);
            document.add(pTong);
            
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // =======================================================
            // 7. CHỮ KÝ
            // =======================================================
            PdfPTable tableChuKy = new PdfPTable(2);
            tableChuKy.setWidthPercentage(100);
            
            PdfPCell cellNguoiLap = new PdfPCell(new Phrase("Người lập phiếu", fontBold));
            cellNguoiLap.setHorizontalAlignment(Element.ALIGN_CENTER); cellNguoiLap.setBorder(Rectangle.NO_BORDER);
            
            PdfPCell cellNguoiDuyet = new PdfPCell(new Phrase("Quản lý duyệt", fontBold));
            cellNguoiDuyet.setHorizontalAlignment(Element.ALIGN_CENTER); cellNguoiDuyet.setBorder(Rectangle.NO_BORDER);
            
            tableChuKy.addCell(cellNguoiLap); tableChuKy.addCell(cellNguoiDuyet);

            PdfPCell cellGhiChu1 = new PdfPCell(new Phrase("(Ký và ghi rõ họ tên)", fontItalic));
            cellGhiChu1.setHorizontalAlignment(Element.ALIGN_CENTER); cellGhiChu1.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellGhiChu2 = new PdfPCell(new Phrase("(Ký và ghi rõ họ tên)", fontItalic));
            cellGhiChu2.setHorizontalAlignment(Element.ALIGN_CENTER); cellGhiChu2.setBorder(Rectangle.NO_BORDER);

            tableChuKy.addCell(cellGhiChu1); tableChuKy.addCell(cellGhiChu2);

            document.add(tableChuKy);
            document.close();
            
            // Tự động mở file PDF sau khi tạo xong
            File pdfFile = new File(path);
            if (pdfFile.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdfFile);
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Các hàm Helper giữ nguyên
    private static void addInfoRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, font)); c1.setBorder(Rectangle.NO_BORDER); c1.setPaddingBottom(6f);
        PdfPCell c2 = new PdfPCell(new Phrase(value, font)); c2.setBorder(Rectangle.NO_BORDER); c2.setPaddingBottom(6f);
        table.addCell(c1); table.addCell(c2);
    }

    private static PdfPCell createDataCell(String text, Font font, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setHorizontalAlignment(align); c.setBorderColor(BaseColor.LIGHT_GRAY);
        c.setPaddingTop(6f); c.setPaddingBottom(8f);
        return c;
    }
}