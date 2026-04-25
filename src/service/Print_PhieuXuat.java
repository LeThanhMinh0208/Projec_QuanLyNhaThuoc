package service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;
import java.io.FileOutputStream;
import java.util.List;

public class Print_PhieuXuat {

    /**
     * Hàm in dùng chung cho cả Chuyển Kho, Xuất Hủy, và Trả NCC.
     */
    public static boolean inPhieu(
            String tieuDe, String maPhieu, String ngayLap, String nguoiLap,
            String labelDoiTac, String tenDoiTac, String ghiChu,
            double tongTien, List<String[]> dsThuoc, String path) {
        
        Document document = new Document(PageSize.A4, 40, 40, 50, 50);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();

            // 1. CÀI ĐẶT FONT
            BaseFont bf = BaseFont.createFont("C:\\Windows\\Fonts\\arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font fontShopName = new Font(bf, 18, Font.BOLD, BaseColor.BLACK);
            Font fontTitle = new Font(bf, 16, Font.BOLD, BaseColor.BLACK);
            Font fontNormal = new Font(bf, 11, Font.NORMAL, BaseColor.BLACK);
            Font fontBold = new Font(bf, 11, Font.BOLD, BaseColor.BLACK);
            Font fontItalic = new Font(bf, 10, Font.ITALIC, BaseColor.DARK_GRAY);
            
            BaseColor headerBlue = new BaseColor(43, 139, 198);
            Font fontTableHeader = new Font(bf, 11, Font.BOLD, BaseColor.WHITE);
            Font fontTotalRed = new Font(bf, 14, Font.BOLD, new BaseColor(220, 38, 38));

            // 2. HEADER NHÀ THUỐC
            Paragraph shopName = new Paragraph("NHÀ THUỐC LONG NGUYÊN", fontShopName);
            shopName.setAlignment(Element.ALIGN_CENTER);
            document.add(shopName);

            Paragraph address = new Paragraph("Đ/c: 12 Nguyễn Văn Bảo, P.4, Gò Vấp, TP.HCM", fontItalic);
            address.setAlignment(Element.ALIGN_CENTER);
            document.add(address);
            
            document.add(new Paragraph(" "));
            DottedLineSeparator separator = new DottedLineSeparator();
            separator.setPercentage(100);
            separator.setLineColor(BaseColor.GRAY);
            document.add(new Chunk(separator));

            // 3. TIÊU ĐỀ PHIẾU TÙY CHỈNH
            Paragraph title = new Paragraph(tieuDe, fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(15f);
            title.setSpacingAfter(20f);
            document.add(title);

            // 4. THÔNG TIN CHUNG
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{1.5f, 4f});
            infoTable.setSpacingAfter(15f);

            addInfoRow(infoTable, "Mã phiếu:", maPhieu, fontNormal);
            addInfoRow(infoTable, "Ngày lập:", ngayLap, fontNormal);
            addInfoRow(infoTable, labelDoiTac, tenDoiTac, fontNormal);
            addInfoRow(infoTable, "Người lập:", nguoiLap, fontNormal);
            addInfoRow(infoTable, "Lý do/Ghi chú:", ghiChu != null && !ghiChu.isEmpty() ? ghiChu : "---", fontNormal);

            document.add(infoTable);

            // 5. BẢNG THUỐC
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.8f, 3.5f, 1.5f, 1.2f, 1.5f, 2f});

            String[] headers = {"STT", "Tên thuốc", "Số Lô", "SL", "Đơn giá", "Thành tiền"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fontTableHeader));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(headerBlue);
                cell.setPaddingBottom(8f);
                cell.setPaddingTop(5f);
                cell.setBorderColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }

            for (String[] row : dsThuoc) {
                table.addCell(createDataCell(row[0], fontNormal, Element.ALIGN_CENTER)); // STT
                table.addCell(createDataCell(row[1], fontNormal, Element.ALIGN_LEFT));   // Tên
                table.addCell(createDataCell(row[2], fontNormal, Element.ALIGN_CENTER)); // Lô
                table.addCell(createDataCell(row[3], fontNormal, Element.ALIGN_CENTER)); // SL
                table.addCell(createDataCell(row[4], fontNormal, Element.ALIGN_RIGHT));  // Giá
                table.addCell(createDataCell(row[5], fontNormal, Element.ALIGN_RIGHT));  // Tiền
            }
            document.add(table);

            // 6. TỔNG CỘNG
            if (tongTien > 0) {
                java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
                PdfPTable totalTable = new PdfPTable(2);
                totalTable.setWidthPercentage(100);
                totalTable.setSpacingBefore(10f);
                totalTable.setWidths(new float[]{7f, 3f});

                PdfPCell cellLabel = new PdfPCell(new Phrase("TỔNG GIÁ TRỊ:", fontTotalRed));
                cellLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cellLabel.setBorder(Rectangle.TOP);
                cellLabel.setBorderColor(BaseColor.LIGHT_GRAY);
                cellLabel.setPaddingTop(10f);

                PdfPCell cellValue = new PdfPCell(new Phrase(df.format(tongTien) + " đ", fontTotalRed));
                cellValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cellValue.setBorder(Rectangle.TOP);
                cellValue.setBorderColor(BaseColor.LIGHT_GRAY);
                cellValue.setPaddingTop(10f);

                totalTable.addCell(cellLabel);
                totalTable.addCell(cellValue);
                document.add(totalTable);
            }

            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // 7. CHỮ KÝ
            PdfPTable tableChuKy = new PdfPTable(2);
            tableChuKy.setWidthPercentage(100);
            
            PdfPCell cellNguoiLap = new PdfPCell(new Phrase("Người lập phiếu", fontBold));
            cellNguoiLap.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellNguoiLap.setBorder(Rectangle.NO_BORDER);
            
            PdfPCell cellNguoiGiao = new PdfPCell(new Phrase("Đại diện nhận / Phụ trách", fontBold));
            cellNguoiGiao.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellNguoiGiao.setBorder(Rectangle.NO_BORDER);
            
            tableChuKy.addCell(cellNguoiLap);
            tableChuKy.addCell(cellNguoiGiao);

            document.add(tableChuKy);
            document.close();
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void addInfoRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, font)); c1.setBorder(Rectangle.NO_BORDER); c1.setPaddingBottom(6f);
        PdfPCell c2 = new PdfPCell(new Phrase(value, font)); c2.setBorder(Rectangle.NO_BORDER); c2.setPaddingBottom(6f);
        table.addCell(c1); table.addCell(c2);
    }
    private static PdfPCell createDataCell(String text, Font font, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text, font)); c.setHorizontalAlignment(align); c.setBorderColor(BaseColor.LIGHT_GRAY); c.setPaddingTop(6f); c.setPaddingBottom(8f); return c;
    }
}