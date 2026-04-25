package service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;
import entity.ChiTietDonDatHang;
import entity.DonDatHang;

import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class Print_PhieuDatHang {

    public static boolean inHoaDon(DonDatHang don, List<ChiTietDonDatHang> listChiTiet, String filePath) {
        // Căn lề A4
        Document document = new Document(PageSize.A4, 40, 40, 50, 50);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // 1. Cài đặt Font Tiếng Việt (Arial)
            BaseFont bf = BaseFont.createFont("C:\\Windows\\Fonts\\arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font fontShopName = new Font(bf, 18, Font.BOLD, BaseColor.BLACK);
            Font fontTitle = new Font(bf, 16, Font.BOLD, BaseColor.BLACK);
            Font fontNormal = new Font(bf, 11, Font.NORMAL, BaseColor.BLACK);
            Font fontItalic = new Font(bf, 10, Font.ITALIC, BaseColor.DARK_GRAY);
            BaseColor headerBlue = new BaseColor(43, 139, 198); 
            Font fontTableHeader = new Font(bf, 11, Font.BOLD, BaseColor.WHITE);
            Font fontTotalRed = new Font(bf, 14, Font.BOLD, new BaseColor(220, 38, 38)); 

            // 2. HEADER
            Paragraph shopName = new Paragraph("NHÀ THUỐC LONG NGUYÊN", fontShopName);
            shopName.setAlignment(Element.ALIGN_CENTER);
            document.add(shopName);

            Paragraph address = new Paragraph("Đ/c: 12 Nguyễn Văn Bảo, P.4, Gò Vấp, TP.HCM", fontItalic);
            address.setAlignment(Element.ALIGN_CENTER);
            document.add(address);
            
            document.add(new Paragraph(" ")); // Dòng trống

            // Đường nét đứt
            DottedLineSeparator separator = new DottedLineSeparator();
            separator.setPercentage(100);
            separator.setLineColor(BaseColor.GRAY);
            document.add(new Chunk(separator));

            // 3. TIÊU ĐỀ
            Paragraph title = new Paragraph("PHIẾU ĐẶT HÀNG", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(15f);
            title.setSpacingAfter(20f);
            document.add(title);

            // 4. THÔNG TIN CHUNG
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{1.5f, 4f});
            infoTable.setSpacingAfter(15f);

            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm - dd/MM/yyyy");
            SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");

            addInfoRow(infoTable, "Mã phiếu đặt:", don.getMaDonDatHang(), fontNormal);
            addInfoRow(infoTable, "Ngày lập:", sdfTime.format(don.getNgayDat()), fontNormal);
            addInfoRow(infoTable, "Nhà cung cấp:", don.getNhaCungCap().getTenNhaCungCap(), fontNormal);
            addInfoRow(infoTable, "Người lập:", don.getNhanVien().getHoTen(), fontNormal);
            
            String ngayGiao = (don.getNgayGiaoDuKien() != null) ? sdfDate.format(don.getNgayGiaoDuKien()) : "Chưa xác định";
            addInfoRow(infoTable, "Ngày dự kiến:", ngayGiao, fontNormal);

            document.add(infoTable);

            // 5. BẢNG CHI TIẾT
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.8f, 3.5f, 1.2f, 1.8f, 1f, 2f});

            String[] headers = {"STT", "Tên thuốc", "ĐVT", "Đơn giá", "SL", "Thành tiền"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fontTableHeader));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(headerBlue);
                cell.setPaddingBottom(8f);
                cell.setBorderColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }

            DecimalFormat df = new DecimalFormat("#,###");
            int stt = 1;
            double tongTien = 0;

            for (ChiTietDonDatHang ct : listChiTiet) {
                table.addCell(createDataCell(String.valueOf(stt++), fontNormal, Element.ALIGN_CENTER));
                table.addCell(createDataCell(ct.getThuoc().getTenThuoc(), fontNormal, Element.ALIGN_LEFT));
                table.addCell(createDataCell(ct.getDonViQuyDoi().getTenDonVi(), fontNormal, Element.ALIGN_CENTER));
                table.addCell(createDataCell(df.format(ct.getDonGiaDuKien()), fontNormal, Element.ALIGN_RIGHT));
                table.addCell(createDataCell(String.valueOf(ct.getSoLuongDat()), fontNormal, Element.ALIGN_CENTER));
                
                double tt = ct.getSoLuongDat() * ct.getDonGiaDuKien();
                tongTien += tt;
                table.addCell(createDataCell(df.format(tt), fontNormal, Element.ALIGN_RIGHT));
            }
            document.add(table);

            // 6. TỔNG CỘNG
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(100);
            totalTable.setSpacingBefore(10f);
            totalTable.setWidths(new float[]{7f, 3f});

            PdfPCell cellLabel = new PdfPCell(new Phrase("TỔNG CỘNG:", fontTotalRed));
            cellLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellLabel.setBorder(Rectangle.TOP);
            cellLabel.setPaddingTop(10f);

            PdfPCell cellValue = new PdfPCell(new Phrase(df.format(tongTien) + " đ", fontTotalRed));
            cellValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellValue.setBorder(Rectangle.TOP);
            cellValue.setPaddingTop(10f);

            totalTable.addCell(cellLabel);
            totalTable.addCell(cellValue);
            document.add(totalTable);

            document.close();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void addInfoRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, font));
        c1.setBorder(Rectangle.NO_BORDER);
        PdfPCell c2 = new PdfPCell(new Phrase(value, font));
        c2.setBorder(Rectangle.NO_BORDER);
        table.addCell(c1);
        table.addCell(c2);
    }

    private static PdfPCell createDataCell(String text, Font font, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setHorizontalAlignment(align);
        c.setBorderColor(BaseColor.LIGHT_GRAY);
        c.setPaddingTop(5f);
        c.setPaddingBottom(7f);
        return c;
    }
}