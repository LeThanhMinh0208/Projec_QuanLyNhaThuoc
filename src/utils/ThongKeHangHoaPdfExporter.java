package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;

public class ThongKeHangHoaPdfExporter {

    private static final DecimalFormat dfCurrency = new DecimalFormat("#,##0");

    // ============================================================
    // FONT HELPER
    // ============================================================
    private static Font getFont(float size, int style) {
        return getFont(size, style, null);
    }

    private static Font getFont(float size, int style, BaseColor color) {
        try {
            String[] fontPaths = {
                    "C:/Windows/Fonts/arial.ttf",
                    "C:/Windows/Fonts/times.ttf",
                    "/usr/share/fonts/truetype/freefont/FreeSerif.ttf"
            };
            for (String path : fontPaths) {
                if (new File(path).exists()) {
                    BaseFont bf = BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                    Font f = new Font(bf, size, style);
                    if (color != null) f.setColor(color);
                    return f;
                }
            }
        } catch (Exception ignored) {}
        Font f = new Font(Font.FontFamily.HELVETICA, size, style);
        if (color != null) f.setColor(color);
        return f;
    }

    // ============================================================
    // PAGE NUMBER EVENT
    // ============================================================
    static class PageNumberEvent extends PdfPageEventHelper {
        Font fPage = getFont(8, Font.ITALIC, BaseColor.GRAY);

        @Override
        public void onEndPage(PdfWriter writer, Document doc) {
            PdfContentByte cb = writer.getDirectContent();
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase("Trang " + writer.getPageNumber(), fPage),
                    doc.right(), doc.bottom() - 10, 0);
        }
    }

    // ============================================================
    // MAIN EXPORT METHOD
    // ============================================================
    public static String xuatPDF(
            LocalDate tuNgay,
            LocalDate denNgay,
            String danhMuc,
            Map<String, Object> tongQuan,
            List<Map<String, Object>> topBanChay,
            List<Map<String, Object>> doanhThuTheoDanhMuc,
            List<Map<String, Object>> xuHuongTheoNgay,
            List<Map<String, Object>> chamBan) throws Exception {

        Path dir = Paths.get("exports/thongke");
        Files.createDirectories(dir);

        DateTimeFormatter dtfFile = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        DateTimeFormatter dtfShow = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String timeStr  = java.time.LocalDateTime.now().format(dtfFile);
        String fileName = "ThongKeHangHoa_" + timeStr + ".pdf";
        String filePath = dir.resolve(fileName).toString();

        Document doc = new Document(PageSize.A4, 40, 40, 50, 40);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(filePath));
        writer.setPageEvent(new PageNumberEvent());
        doc.open();

        // -- FONTS --
        Font fTitleXl   = getFont(20, Font.BOLD);
        Font fTitle     = getFont(14, Font.BOLD);
        Font fSubTitle  = getFont(11, Font.BOLD);
        Font fSub       = getFont(9,  Font.ITALIC, BaseColor.DARK_GRAY);
        Font fHead      = getFont(10, Font.BOLD,   BaseColor.WHITE);
        Font fNormal    = getFont(10, Font.NORMAL);
        Font fBold      = getFont(10, Font.BOLD);

        // ── HEADER ──
        Paragraph shopName = new Paragraph("NHÀ THUỐC LONG NGUYÊN", fTitleXl);
        shopName.setAlignment(Element.ALIGN_CENTER);
        doc.add(shopName);

        Paragraph shopAddr = new Paragraph(
                "Đ/c: 12 Nguyễn Văn Bảo, P.Hạnh Thông, TP.HCM  |  Hotline: 0123.456.789", fSub);
        shopAddr.setAlignment(Element.ALIGN_CENTER);
        shopAddr.setSpacingAfter(12);
        doc.add(shopAddr);

        DottedLineSeparator separator = new DottedLineSeparator();
        separator.setGap(3);
        doc.add(new Chunk(separator));

        // ── TIÊU ĐỀ ──
        Paragraph title = new Paragraph("BÁO CÁO THỐNG KÊ HÀNG HÓA", fTitle);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(15);
        title.setSpacingAfter(15);
        doc.add(title);

        // ── THỜI GIAN / FILTER ──
        String dateRange = String.format("Từ ngày %s đến %s",
                tuNgay.format(dtfShow), denNgay.format(dtfShow));
        Paragraph dateP = new Paragraph(dateRange, fBold);
        dateP.setAlignment(Element.ALIGN_CENTER);
        dateP.setSpacingAfter(6);
        doc.add(dateP);

        String filterInfo = "Danh mục: " + (danhMuc == null || danhMuc.isEmpty() ? "Tất cả" : danhMuc);
        Paragraph filterP = new Paragraph(filterInfo, fSub);
        filterP.setAlignment(Element.ALIGN_CENTER);
        filterP.setSpacingAfter(15);
        doc.add(filterP);

        // ── KPI TỔNG QUAN ──
        Paragraph kpiTitle = new Paragraph("TỔNG HỢP CHỈ TIÊU CHÍNH", fSubTitle);
        kpiTitle.setSpacingBefore(10);
        kpiTitle.setSpacingAfter(10);
        doc.add(kpiTitle);

        PdfPTable kpiTable = new PdfPTable(2);
        kpiTable.setWidthPercentage(100);
        kpiTable.setSpacingAfter(20);

        addKPIRow(kpiTable, "Tổng mặt hàng đã bán",
                str(tongQuan.get("tongMatHang")) + " mặt hàng", fNormal, fBold);
        addKPIRow(kpiTable, "Tổng số lượng bán ra",
                dfCurrency.format(toDouble(tongQuan.get("tongSoLuongBan"))) + " đơn vị", fNormal, fBold);
        addKPIRow(kpiTable, "Tổng doanh thu",
                dfCurrency.format(toDouble(tongQuan.get("tongDoanhThu"))) + " ₫", fNormal, fBold);
        addKPIRow(kpiTable, "Tổng số đơn hàng",
                str(tongQuan.get("tongDon")) + " đơn", fNormal, fBold);
        doc.add(kpiTable);

        // ── TOP THUỐC BÁN CHẠY ──
        if (topBanChay != null && !topBanChay.isEmpty()) {
            Paragraph secTitle = new Paragraph("TOP THUỐC BÁN CHẠY NHẤT", fSubTitle);
            secTitle.setSpacingBefore(10);
            secTitle.setSpacingAfter(10);
            doc.add(secTitle);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.8f, 3.5f, 2f, 1.5f, 2.5f});
            table.setHeaderRows(1);
            table.setSpacingAfter(18);

            BaseColor headerBg = new BaseColor(41, 128, 185);
            String[] headers = {"STT", "Tên thuốc", "Danh mục", "SL Bán", "Doanh thu"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fHead));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setBackgroundColor(headerBg);
                cell.setPadding(6);
                table.addCell(cell);
            }

            int idx = 0;
            for (Map<String, Object> row : topBanChay) {
                BaseColor rowBg = (idx++ % 2 == 0)
                        ? new BaseColor(245, 249, 255) : BaseColor.WHITE;
                addTableCell(table, str(row.get("stt")),         Element.ALIGN_CENTER, fNormal, rowBg);
                addTableCell(table, str(row.get("tenThuoc")),    Element.ALIGN_LEFT,   fNormal, rowBg);
                addTableCell(table, str(row.get("tenDanhMuc")), Element.ALIGN_LEFT,   fNormal, rowBg);
                addTableCell(table, dfCurrency.format(toDouble(row.get("soLuongBan"))),
                        Element.ALIGN_RIGHT, fNormal, rowBg);
                addTableCell(table, dfCurrency.format(toDouble(row.get("doanhThu"))) + " ₫",
                        Element.ALIGN_RIGHT, fNormal, rowBg);
            }
            doc.add(table);
        }

        // ── DOANH THU THEO DANH MỤC ──
        if (doanhThuTheoDanhMuc != null && !doanhThuTheoDanhMuc.isEmpty()) {
            double tongDT = doanhThuTheoDanhMuc.stream()
                    .mapToDouble(r -> toDouble(r.get("doanhThu"))).sum();

            Paragraph secTitle = new Paragraph("DOANH THU THEO NHÓM THUỐC", fSubTitle);
            secTitle.setSpacingBefore(15);
            secTitle.setSpacingAfter(10);
            doc.add(secTitle);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3.5f, 1.8f, 2.5f, 1.5f});
            table.setHeaderRows(1);
            table.setSpacingAfter(18);

            BaseColor headerBg = new BaseColor(41, 128, 185);
            String[] headers = {"Nhóm thuốc", "SL Bán", "Doanh thu", "Tỷ lệ"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fHead));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setBackgroundColor(headerBg);
                cell.setPadding(6);
                table.addCell(cell);
            }

            int idx = 0;
            for (Map<String, Object> row : doanhThuTheoDanhMuc) {
                double dt   = toDouble(row.get("doanhThu"));
                String tyLe = tongDT > 0
                        ? String.format("%.1f%%", (dt / tongDT) * 100) : "0.0%";
                BaseColor rowBg = (idx++ % 2 == 0)
                        ? new BaseColor(245, 249, 255) : BaseColor.WHITE;
                addTableCell(table, str(row.get("tenDanhMuc")),   Element.ALIGN_LEFT,   fNormal, rowBg);
                addTableCell(table, dfCurrency.format(toDouble(row.get("soLuongBan"))),
                        Element.ALIGN_RIGHT, fNormal, rowBg);
                addTableCell(table, dfCurrency.format(dt) + " ₫", Element.ALIGN_RIGHT,  fNormal, rowBg);
                addTableCell(table, tyLe,                          Element.ALIGN_CENTER, fNormal, rowBg);
            }
            doc.add(table);
        }

        // ── XU HƯỚNG BÁN THEO NGÀY ──
        if (xuHuongTheoNgay != null && !xuHuongTheoNgay.isEmpty()) {
            Paragraph secTitle = new Paragraph("XU HƯỚNG BÁN HÀNG THEO NGÀY", fSubTitle);
            secTitle.setSpacingBefore(15);
            secTitle.setSpacingAfter(10);
            doc.add(secTitle);

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2.5f, 2f, 3f});
            table.setHeaderRows(1);
            table.setSpacingAfter(18);

            BaseColor headerBg = new BaseColor(41, 128, 185);
            String[] headers = {"Ngày", "Số lượng bán", "Doanh thu"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fHead));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setBackgroundColor(headerBg);
                cell.setPadding(6);
                table.addCell(cell);
            }

            int idx = 0;
            for (Map<String, Object> row : xuHuongTheoNgay) {
                BaseColor rowBg = (idx++ % 2 == 0)
                        ? new BaseColor(245, 249, 255) : BaseColor.WHITE;
                Object ngayObj = row.get("ngay");
                String ngayStr = (ngayObj instanceof LocalDate)
                        ? ((LocalDate) ngayObj).format(dtfShow) : str(ngayObj);
                addTableCell(table, ngayStr,                                               Element.ALIGN_CENTER, fNormal, rowBg);
                addTableCell(table, dfCurrency.format(toDouble(row.get("soLuongBan"))),
                        Element.ALIGN_RIGHT, fNormal, rowBg);
                addTableCell(table, dfCurrency.format(toDouble(row.get("doanhThu"))) + " ₫",
                        Element.ALIGN_RIGHT, fNormal, rowBg);
            }
            doc.add(table);
        }

        // ── TOP THUỐC CHẬM BÁN ──
        if (chamBan != null && !chamBan.isEmpty()) {
            Paragraph secTitle = new Paragraph("THUỐC CHẬM BÁN (CÒN TỒN KHO)", fSubTitle);
            secTitle.setSpacingBefore(15);
            secTitle.setSpacingAfter(10);
            doc.add(secTitle);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.8f, 3.5f, 2f, 1.5f, 1.5f});
            table.setHeaderRows(1);
            table.setSpacingAfter(18);

            BaseColor headerBg = new BaseColor(192, 57, 43);
            String[] headers = {"STT", "Tên thuốc", "Danh mục", "SL Bán", "Tồn kho"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fHead));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setBackgroundColor(headerBg);
                cell.setPadding(6);
                table.addCell(cell);
            }

            int idx = 0;
            for (Map<String, Object> row : chamBan) {
                BaseColor rowBg = (idx++ % 2 == 0)
                        ? new BaseColor(255, 248, 245) : BaseColor.WHITE;
                addTableCell(table, str(row.get("stt")),         Element.ALIGN_CENTER, fNormal, rowBg);
                addTableCell(table, str(row.get("tenThuoc")),    Element.ALIGN_LEFT,   fNormal, rowBg);
                addTableCell(table, str(row.get("tenDanhMuc")), Element.ALIGN_LEFT,   fNormal, rowBg);
                addTableCell(table, dfCurrency.format(toDouble(row.get("soLuongBan"))),
                        Element.ALIGN_RIGHT, fNormal, rowBg);
                addTableCell(table, dfCurrency.format(toDouble(row.get("tonKho"))),
                        Element.ALIGN_RIGHT, fNormal, rowBg);
            }
            doc.add(table);
        }

        // ── FOOTER ──
        doc.add(Chunk.NEWLINE);
        doc.add(new Chunk(separator));
        Paragraph footer = new Paragraph(
                "Báo cáo được tạo tự động từ hệ thống. Cảm ơn quý khách!", fSub);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(12);
        doc.add(footer);

        doc.close();
        return filePath;
    }

    // ============================================================
    // HELPERS
    // ============================================================
    private static void addKPIRow(PdfPTable t, String label, String value,
            Font fLabel, Font fValue) {
        PdfPCell lbl = new PdfPCell(new Phrase(label, fLabel));
        lbl.setBackgroundColor(new BaseColor(245, 248, 250));
        lbl.setBorder(Rectangle.NO_BORDER);
        lbl.setHorizontalAlignment(Element.ALIGN_LEFT);
        lbl.setPadding(8);

        PdfPCell val = new PdfPCell(new Phrase(value, fValue));
        val.setBorder(Rectangle.NO_BORDER);
        val.setHorizontalAlignment(Element.ALIGN_LEFT);
        val.setPadding(8);

        t.addCell(lbl);
        t.addCell(val);
    }

    private static void addTableCell(PdfPTable t, String text, int align,
            Font f, BaseColor bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", f));
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(bg);
        cell.setBorderColor(new BaseColor(220, 220, 220));
        cell.setBorderWidth(0.5f);
        cell.setPadding(5);
        cell.setPaddingBottom(7);
        t.addCell(cell);
    }

    private static double toDouble(Object value) {
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value == null) return 0;
        try { return Double.parseDouble(String.valueOf(value)); }
        catch (NumberFormatException e) { return 0; }
    }

    private static String str(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}