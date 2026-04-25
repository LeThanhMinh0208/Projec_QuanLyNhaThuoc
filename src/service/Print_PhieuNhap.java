package service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;
import entity.PhieuNhap;
import gui.dialogs.Dialog_ChiTietPhieuNhapController.ChiTietUI;
import javafx.collections.ObservableList;

import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class Print_PhieuNhap {

    public static boolean inPhieu(PhieuNhap pn, ObservableList<ChiTietUI> listChiTiet, String path) {
        // Căn lề A4 chuẩn như Hóa Đơn
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
            
            // Màu sắc đồng bộ Hóa Đơn
            BaseColor headerBlue = new BaseColor(43, 139, 198); 
            Font fontTableHeader = new Font(bf, 11, Font.BOLD, BaseColor.WHITE);
            Font fontTotalRed = new Font(bf, 14, Font.BOLD, new BaseColor(220, 38, 38)); 

            // =======================================================
            // 2. HEADER THƯƠNG HIỆU
            // =======================================================
            Paragraph shopName = new Paragraph("NHÀ THUỐC LONG NGUYÊN", fontShopName);
            shopName.setAlignment(Element.ALIGN_CENTER);
            document.add(shopName);

            Paragraph address = new Paragraph("Đ/c: 12 Nguyễn Văn Bảo, P.Hạnh Thông, TP.HCM\nHotline: 0123.456.789", fontItalic);
            address.setAlignment(Element.ALIGN_CENTER);
            document.add(address);
            
            document.add(new Paragraph(" ")); // Dòng trống

            // Đường nét đứt (Dotted Line)
            DottedLineSeparator separator = new DottedLineSeparator();
            separator.setPercentage(100);
            separator.setLineColor(BaseColor.GRAY);
            document.add(new Chunk(separator));

            // =======================================================
            // 3. TIÊU ĐỀ PHIẾU
            // =======================================================
            Paragraph title = new Paragraph("PHIẾU NHẬP KHO THUỐC", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(15f);
            title.setSpacingAfter(20f);
            document.add(title);

            // =======================================================
            // 4. THÔNG TIN CHUNG (Dùng Lưới không viền để gióng hàng)
            // =======================================================
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{1.5f, 4f});
            infoTable.setSpacingAfter(15f);

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd/MM/yyyy");
            DecimalFormat df = new DecimalFormat("#,###");

            addInfoRow(infoTable, "Mã phiếu:", pn.getMaPhieuNhap(), fontNormal);
            addInfoRow(infoTable, "Ngày lập:", sdf.format(pn.getNgayNhap()), fontNormal);
            addInfoRow(infoTable, "Nhà cung cấp:", pn.getNhaCungCap().getTenNhaCungCap(), fontNormal);
            addInfoRow(infoTable, "Người lập:", pn.getNhanVien().getHoTen(), fontNormal);

            document.add(infoTable);

            // =======================================================
            // 5. BẢNG CHI TIẾT HÀNG NHẬP
            // =======================================================
            PdfPTable table = new PdfPTable(7); 
            table.setWidthPercentage(100);
            // Căn chỉnh độ rộng các cột cho cân đối
            table.setWidths(new float[]{0.8f, 3.5f, 1f, 1f, 1.5f, 1.5f, 2f}); 

            String[] headers = {"STT", "Tên thuốc", "ĐVT", "SL", "Mã lô", "Giá nhập", "Thành tiền"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fontTableHeader));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(headerBlue);
                cell.setPaddingBottom(8f);
                cell.setPaddingTop(5f);
                cell.setBorderColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }

            int stt = 1;
            for (ChiTietUI ct : listChiTiet) {
                table.addCell(createDataCell(String.valueOf(stt++), fontNormal, Element.ALIGN_CENTER));
                table.addCell(createDataCell(ct.getTenThuoc(), fontNormal, Element.ALIGN_LEFT));
                table.addCell(createDataCell(ct.getDonVi(), fontNormal, Element.ALIGN_CENTER));
                table.addCell(createDataCell(String.valueOf(ct.getSoLuong()), fontNormal, Element.ALIGN_CENTER));
                table.addCell(createDataCell(ct.getMaLo(), fontNormal, Element.ALIGN_CENTER));
                table.addCell(createDataCell(df.format(ct.getGiaNhap()), fontNormal, Element.ALIGN_RIGHT));
                table.addCell(createDataCell(df.format(ct.getGiaNhap() * ct.getSoLuong()), fontNormal, Element.ALIGN_RIGHT));
            }
            document.add(table);

            // =======================================================
            // 6. TỔNG CỘNG
            // =======================================================
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(100);
            totalTable.setSpacingBefore(10f);
            totalTable.setWidths(new float[]{7f, 3f});

            PdfPCell cellLabel = new PdfPCell(new Phrase("TỔNG CỘNG:", fontTotalRed));
            cellLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellLabel.setBorder(Rectangle.TOP);
            cellLabel.setBorderColor(BaseColor.LIGHT_GRAY);
            cellLabel.setPaddingTop(10f);

            PdfPCell cellValue = new PdfPCell(new Phrase(df.format(pn.getTongTien()) + " đ", fontTotalRed));
            cellValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellValue.setBorder(Rectangle.TOP);
            cellValue.setBorderColor(BaseColor.LIGHT_GRAY);
            cellValue.setPaddingTop(10f);

            totalTable.addCell(cellLabel);
            totalTable.addCell(cellValue);
            document.add(totalTable);

            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // =======================================================
            // 7. CHỮ KÝ
            // =======================================================
            PdfPTable tableChuKy = new PdfPTable(2);
            tableChuKy.setWidthPercentage(100);
            
            PdfPCell cellNguoiLap = new PdfPCell(new Phrase("Người lập phiếu", fontBold));
            cellNguoiLap.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellNguoiLap.setBorder(Rectangle.NO_BORDER);
            
            PdfPCell cellNguoiGiao = new PdfPCell(new Phrase("Người giao hàng", fontBold));
            cellNguoiGiao.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellNguoiGiao.setBorder(Rectangle.NO_BORDER);
            
            tableChuKy.addCell(cellNguoiLap);
            tableChuKy.addCell(cellNguoiGiao);

            PdfPCell cellGhiChu1 = new PdfPCell(new Phrase("(Ký và ghi rõ họ tên)", fontItalic));
            cellGhiChu1.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellGhiChu1.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellGhiChu2 = new PdfPCell(new Phrase("(Ký và ghi rõ họ tên)", fontItalic));
            cellGhiChu2.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellGhiChu2.setBorder(Rectangle.NO_BORDER);

            tableChuKy.addCell(cellGhiChu1);
            tableChuKy.addCell(cellGhiChu2);

            document.add(tableChuKy);

            document.close();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // =======================================================
    // HÀM HỖ TRỢ VẼ BẢNG (HELPER METHODS)
    // =======================================================
    private static void addInfoRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, font));
        c1.setBorder(Rectangle.NO_BORDER);
        c1.setPaddingBottom(6f);
        
        PdfPCell c2 = new PdfPCell(new Phrase(value, font));
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setPaddingBottom(6f);
        
        table.addCell(c1);
        table.addCell(c2);
    }

    private static PdfPCell createDataCell(String text, Font font, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setHorizontalAlignment(align);
        c.setBorderColor(BaseColor.LIGHT_GRAY);
        c.setPaddingTop(6f);
        c.setPaddingBottom(8f);
        return c;
    }
}