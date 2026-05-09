package utils;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ThongKeDoanhThuExcelExporter {

    private static final DateTimeFormatter SHOW_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final java.text.DecimalFormat DF_CURRENCY = new java.text.DecimalFormat("#,##0");

    private static final class CellValue {
        final String value;
        final int style;

        CellValue(String value, int style) {
            this.value = value;
            this.style = style;
        }
    }

    public static String xuatExcel(
            LocalDate tuNgay,
            LocalDate denNgay,
            String loaiBan,
            String hinhThuc,
            double tongDoanhThu,
            int tongDon,
            double giaTrungBinh,
            int soKhachHang,
            List<Map<String, Object>> doanhThuTheoNgay,
            List<Map<String, Object>> doanhThuTheoNhom,
            List<Map<String, Object>> topKhachHang,
            List<Map<String, Object>> productDead,
            List<Map<String, Object>> thongKeHinhThuc) throws Exception {

        Path dir = Paths.get("exports/thongke");
        Files.createDirectories(dir);

        String timeStr = LocalDateTime.now().format(FILE_TIME);
        String fileName = "ThongKeDoanhThu_" + timeStr + ".xlsx";
        Path filePath = dir.resolve(fileName);

        try (OutputStream os = Files.newOutputStream(filePath);
             ZipOutputStream zos = new ZipOutputStream(os)) {

            writeEntry(zos, "[Content_Types].xml", buildContentTypesXml());
            writeEntry(zos, "_rels/.rels", buildRootRelsXml());
            writeEntry(zos, "xl/workbook.xml", buildWorkbookXml());
            writeEntry(zos, "xl/_rels/workbook.xml.rels", buildWorkbookRelsXml());
            writeEntry(zos, "xl/styles.xml", buildStylesXml());
            writeEntry(zos, "xl/worksheets/sheet1.xml", buildSheetXml(
                    tuNgay, denNgay, loaiBan, hinhThuc,
                    tongDoanhThu, tongDon, giaTrungBinh, soKhachHang,
                    doanhThuTheoNgay, doanhThuTheoNhom, topKhachHang, productDead, thongKeHinhThuc));
        }

        return filePath.toString();
    }

    private static void writeEntry(ZipOutputStream zos, String name, String content) throws Exception {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private static String buildContentTypesXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">" +
                "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>" +
                "<Default Extension=\"xml\" ContentType=\"application/xml\"/>" +
                "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>" +
                "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>" +
                "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>" +
                "</Types>";
    }

    private static String buildRootRelsXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
                "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>" +
                "</Relationships>";
    }

    private static String buildWorkbookXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">" +
                "<sheets><sheet name=\"ThongKeDoanhThu\" sheetId=\"1\" r:id=\"rId1\"/></sheets>" +
                "</workbook>";
    }

    private static String buildWorkbookRelsXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
                "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>" +
                "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>" +
                "</Relationships>";
    }

    private static String buildStylesXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">" +
                "<fonts count=\"5\">" +
                "<font><sz val=\"11\"/><color rgb=\"FF000000\"/><name val=\"Calibri\"/><family val=\"2\"/></font>" +
                "<font><b/><sz val=\"16\"/><color rgb=\"FF0EA5E9\"/><name val=\"Calibri\"/><family val=\"2\"/></font>" +
                "<font><i/><sz val=\"10\"/><color rgb=\"FF6B7280\"/><name val=\"Calibri\"/><family val=\"2\"/></font>" +
                "<font><b/><sz val=\"10\"/><color rgb=\"FFFFFFFF\"/><name val=\"Calibri\"/><family val=\"2\"/></font>" +
                "<font><b/><sz val=\"10\"/><color rgb=\"FF1F2937\"/><name val=\"Calibri\"/><family val=\"2\"/></font>" +
                "</fonts>" +
                "<fills count=\"5\">" +
                "<fill><patternFill patternType=\"none\"/></fill>" +
                "<fill><patternFill patternType=\"gray125\"/></fill>" +
                "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFF8FAFC\"/><bgColor indexed=\"64\"/></patternFill></fill>" +
                "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFB91C1C\"/><bgColor indexed=\"64\"/></patternFill></fill>" +
                "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF1D4ED8\"/><bgColor indexed=\"64\"/></patternFill></fill>" +
                "</fills>" +
                "<borders count=\"2\">" +
                "<border><left/><right/><top/><bottom/><diagonal/></border>" +
                "<border><left style=\"thin\"><color rgb=\"FFD1D5DB\"/></left><right style=\"thin\"><color rgb=\"FFD1D5DB\"/></right><top style=\"thin\"><color rgb=\"FFD1D5DB\"/></top><bottom style=\"thin\"><color rgb=\"FFD1D5DB\"/></bottom><diagonal/></border>" +
                "</borders>" +
                "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>" +
                "<cellXfs count=\"9\">" +
                "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" +
                "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" +
                "<xf numFmtId=\"0\" fontId=\"3\" fillId=\"4\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" +
                "<xf numFmtId=\"0\" fontId=\"3\" fillId=\"3\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" +
                "<xf numFmtId=\"0\" fontId=\"4\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>" +
                "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>" +
                "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>" +
                "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>" +
                "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"right\" vertical=\"center\"/></xf>" +
                "</cellXfs>" +
                "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>" +
                "<dxfs count=\"0\"/>" +
                "<tableStyles count=\"0\" defaultTableStyle=\"TableStyleMedium2\" defaultPivotStyle=\"PivotStyleLight16\"/>" +
                "</styleSheet>";
    }

    private static String buildSheetXml(
            LocalDate tuNgay,
            LocalDate denNgay,
            String loaiBan,
            String hinhThuc,
            double tongDoanhThu,
            int tongDon,
            double giaTrungBinh,
            int soKhachHang,
            List<Map<String, Object>> doanhThuTheoNgay,
            List<Map<String, Object>> doanhThuTheoNhom,
            List<Map<String, Object>> topKhachHang,
            List<Map<String, Object>> productDead,
            List<Map<String, Object>> thongKeHinhThuc) {

        StringBuilder sb = new StringBuilder();
        List<String> merges = new ArrayList<>();

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">\n");
        sb.append("<sheetFormatPr defaultRowHeight=\"20\"/>\n");
        sb.append("<cols>");
        sb.append(colXml(1, 1, 18));
        sb.append(colXml(2, 2, 35));
        sb.append(colXml(3, 3, 16));
        sb.append(colXml(4, 4, 18));
        sb.append(colXml(5, 5, 18));
        sb.append(colXml(6, 6, 18));
        sb.append(colXml(7, 7, 18));
        sb.append("</cols>\n");
        sb.append("<sheetData>\n");

        int row = 1;
        row = addMergedRow(sb, merges, row, 1, "BÁO CÁO THỐNG KÊ DOANH THU");
        row = addMergedRow(sb, merges, row, 2, "Nhà Thuốc Long Nguyên");
        row = addMergedRow(sb, merges, row, 2, "Từ ngày " + tuNgay.format(SHOW_DATE) + " đến " + denNgay.format(SHOW_DATE));
        row++;

        row = addMergedRow(sb, merges, row, 3, "THÔNG TIN BÁO CÁO");
        row = addTableHeaderRow(sb, row, new String[]{"Chỉ tiêu", "Giá trị"});
        row = addTwoColRow(sb, row, "Thời gian lập báo cáo", LocalDate.now().format(SHOW_DATE), 5, 6);
        row = addTwoColRow(sb, row, "Từ ngày", tuNgay.format(SHOW_DATE), 5, 6);
        row = addTwoColRow(sb, row, "Đến ngày", denNgay.format(SHOW_DATE), 5, 6);
        row = addTwoColRow(sb, row, "Loại bán", mapLoaiBanDisplay(loaiBan), 5, 6);
        row = addTwoColRow(sb, row, "Hình thức thanh toán", mapHinhThucDisplay(hinhThuc), 5, 6);
        row++;

        row = addMergedRow(sb, merges, row, 3, "TỔNG HỢP THỐNG KÊ CHÍNH");
        row = addTableHeaderRow(sb, row, new String[]{"Chỉ tiêu", "Giá trị"});
        row = addTwoColRow(sb, row, "Tổng doanh thu", formatMoney(tongDoanhThu) + " ₫", 5, 6);
        row = addTwoColRow(sb, row, "Tổng số đơn", tongDon + " đơn", 5, 6);
        row = addTwoColRow(sb, row, "Giá trị trung bình/đơn", formatMoney(giaTrungBinh) + " ₫", 5, 6);
        row = addTwoColRow(sb, row, "Số khách hàng", soKhachHang + " khách", 5, 6);
        row++;

        row = addMergedRow(sb, merges, row, 3, "DOANH THU THEO NHÓM THUỐC");
        row = addTableHeaderRow(sb, row, new String[]{"Nhóm thuốc", "Doanh thu", "Tỷ lệ"});
        if (doanhThuTheoNhom != null && !doanhThuTheoNhom.isEmpty()) {
            for (Map<String, Object> item : doanhThuTheoNhom) {
                String nhomThuoc = String.valueOf(item.get("nhomThuoc"));
                double doanhThu = toDouble(item.get("doanhThu"));
                String tyLe = formatPercent(totalPercent(doanhThu, tongDoanhThu));
                row = addThreeColRow(sb, row, nhomThuoc, formatMoney(doanhThu) + " ₫", tyLe);
            }
        } else {
            row = addThreeColRow(sb, row, "Không có dữ liệu", "", "");
        }
        row++;

        row = addMergedRow(sb, merges, row, 3, "DOANH THU THEO NGÀY");
        row = addTableHeaderRow(sb, row, new String[]{"Ngày", "Doanh thu"});
        if (doanhThuTheoNgay != null && !doanhThuTheoNgay.isEmpty()) {
            for (Map<String, Object> item : doanhThuTheoNgay) {
                String ngay = String.valueOf(item.get("ngay"));
                String doanhThu = formatMoney(toDouble(item.get("doanhThu"))) + " ₫";
                row = addTwoColRow(sb, row, ngay, doanhThu, 7, 8);
            }
        } else {
            row = addTwoColRow(sb, row, "Không có dữ liệu", "", 7, 8);
        }
        row++;

        row = addMergedRow(sb, merges, row, 3, "THỐNG KÊ THEO HÌNH THỨC THANH TOÁN");
        row = addTableHeaderRow(sb, row, new String[]{"Hình thức thanh toán", "Số đơn"});
        if (thongKeHinhThuc != null && !thongKeHinhThuc.isEmpty()) {
            for (Map<String, Object> item : thongKeHinhThuc) {
                String hinhThucTT = String.valueOf(item.get("hinhThuc"));
                String soDon = String.valueOf(item.get("soDon"));
                row = addTwoColRow(sb, row, mapHinhThucDisplay(hinhThucTT), soDon, 7, 8);
            }
        } else {
            row = addTwoColRow(sb, row, "Không có dữ liệu", "", 7, 8);
        }
        row++;

        row = addMergedRow(sb, merges, row, 3, "TOP 5 KHÁCH HÀNG MUA NHIỀU NHẤT");
        row = addTableHeaderRow(sb, row, new String[]{"STT", "Tên khách hàng", "Số đơn", "Tổng doanh thu"});
        if (topKhachHang != null && !topKhachHang.isEmpty()) {
            for (Map<String, Object> item : topKhachHang) {
                String stt = String.valueOf(item.get("stt"));
                String tenKH = String.valueOf(item.get("tenKhachHang"));
                String soDon = String.valueOf(item.get("soDon"));
                String doanhThu = formatMoney(toDouble(item.get("doanhThu"))) + " ₫";
                row = addFourColRow(sb, row, stt, tenKH, soDon, doanhThu);
            }
        } else {
            row = addFourColRow(sb, row, "", "Không có dữ liệu", "", "");
        }
        row++;

        row = addMergedRow(sb, merges, row, 3, "SẢN PHẨM KHÔNG CHUYỂN ĐỘNG (CẢNH BÁO)");
        row = addTableHeaderRow(sb, row, new String[]{"STT", "Tên sản phẩm", "Nhóm", "Số ngày không bán", "Tồn kho"});
        if (productDead != null && !productDead.isEmpty()) {
            for (Map<String, Object> item : productDead) {
                row = addFiveColRow(sb,
                        row,
                        String.valueOf(item.get("stt")),
                        String.valueOf(item.get("tenThuoc")),
                        String.valueOf(item.get("nhomThuoc")),
                        String.valueOf(item.get("soNgayKhongBan")),
                        String.valueOf(item.get("tonKho")));
            }
        } else {
            row = addFiveColRow(sb, row, "", "Không có dữ liệu", "", "", "");
        }

        row++;
        row = addMergedRow(sb, merges, row, 2, "Báo cáo được tạo tự động từ hệ thống thống kê doanh thu.");

        sb.append("</sheetData>\n");
        if (!merges.isEmpty()) {
            sb.append("<mergeCells count=\"").append(merges.size()).append("\">\n");
            for (String merge : merges) {
                sb.append("<mergeCell ref=\"").append(merge).append("\"/>\n");
            }
            sb.append("</mergeCells>\n");
        }
        sb.append("<pageMargins left=\"0.5\" right=\"0.5\" top=\"0.5\" bottom=\"0.5\" header=\"0.3\" footer=\"0.3\"/>\n");
        sb.append("</worksheet>");
        return sb.toString();
    }

    private static String colXml(int min, int max, double width) {
        return "<col min=\"" + min + "\" max=\"" + max + "\" width=\"" + width + "\" customWidth=\"1\"/>";
    }

    private static int addMergedRow(StringBuilder sb, List<String> merges, int rowNum, int style, String text) {
        String ref = "A" + rowNum + ":G" + rowNum;
        merges.add(ref);
        sb.append(rowXml(rowNum, new CellValue[]{new CellValue(text, style)}));
        return rowNum + 1;
    }

    private static int addTableHeaderRow(StringBuilder sb, int rowNum, String[] headers) {
        CellValue[] cells = new CellValue[headers.length];
        for (int i = 0; i < headers.length; i++) {
            cells[i] = new CellValue(headers[i], 4);
        }
        sb.append(rowXml(rowNum, cells));
        return rowNum + 1;
    }

    private static int addTwoColRow(StringBuilder sb, int rowNum, String left, String right, int leftStyle, int rightStyle) {
        sb.append(rowXml(rowNum, new CellValue[]{new CellValue(left, leftStyle), new CellValue(right, rightStyle)}));
        return rowNum + 1;
    }

    private static int addFourColRow(StringBuilder sb, int rowNum, String c1, String c2, String c3, String c4) {
        sb.append(rowXml(rowNum, new CellValue[]{
                new CellValue(c1, 7),
                new CellValue(c2, 7),
                new CellValue(c3, 7),
                new CellValue(c4, 8)
        }));
        return rowNum + 1;
    }

    private static int addThreeColRow(StringBuilder sb, int rowNum, String c1, String c2, String c3) {
        sb.append(rowXml(rowNum, new CellValue[]{
                new CellValue(c1, 7),
                new CellValue(c2, 8),
                new CellValue(c3, 5)
        }));
        return rowNum + 1;
    }

    private static int addFiveColRow(StringBuilder sb, int rowNum, String c1, String c2, String c3, String c4, String c5) {
        sb.append(rowXml(rowNum, new CellValue[]{
                new CellValue(c1, 7),
                new CellValue(c2, 7),
                new CellValue(c3, 7),
                new CellValue(c4, 8),
                new CellValue(c5, 8)
        }));
        return rowNum + 1;
    }

    private static String rowXml(int rowNum, CellValue[] cells) {
        StringBuilder row = new StringBuilder();
        row.append("<row r=\"").append(rowNum).append("\">");
        for (int i = 0; i < cells.length; i++) {
            CellValue cell = cells[i];
            if (cell == null || cell.value == null) {
                continue;
            }
            String ref = colName(i + 1) + rowNum;
            row.append("<c r=\"").append(ref).append("\" s=\"").append(cell.style).append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
                    .append(escapeXml(cell.value))
                    .append("</t></is></c>");
        }
        row.append("</row>\n");
        return row.toString();
    }

    private static String colName(int index) {
        StringBuilder sb = new StringBuilder();
        while (index > 0) {
            int rem = (index - 1) % 26;
            sb.insert(0, (char) ('A' + rem));
            index = (index - 1) / 26;
        }
        return sb.toString();
    }

    private static String escapeXml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private static double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value == null) {
            return 0;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private static String formatMoney(double value) {
        return DF_CURRENCY.format(value);
    }

    private static double totalPercent(double amount, double total) {
        if (total <= 0) {
            return 0;
        }
        return (amount / total) * 100.0;
    }

    private static String formatPercent(double value) {
        return String.format("%.1f%%", value);
    }

    private static String mapLoaiBanDisplay(String loaiBan) {
        if (loaiBan == null || loaiBan.isEmpty()) {
            return "Tất cả";
        }
        if ("BAN_THEO_DON".equalsIgnoreCase(loaiBan)) {
            return "Bán theo đơn";
        }
        if ("BAN_LE".equalsIgnoreCase(loaiBan)) {
            return "Bán lẻ";
        }
        return loaiBan;
    }

    private static String mapHinhThucDisplay(String hinhThuc) {
        if (hinhThuc == null || hinhThuc.isEmpty()) {
            return "Tất cả";
        }
        if ("TIEN_MAT".equalsIgnoreCase(hinhThuc)) {
            return "Tiền mặt";
        }
        if ("CHUYEN_KHOAN".equalsIgnoreCase(hinhThuc)) {
            return "Chuyển khoản";
        }
        if ("THE".equalsIgnoreCase(hinhThuc)) {
            return "Thẻ";
        }
        return hinhThuc;
    }
}