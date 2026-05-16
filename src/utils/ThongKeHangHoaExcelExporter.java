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

public class ThongKeHangHoaExcelExporter {

    private static final DateTimeFormatter SHOW_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FILE_TIME  = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final java.text.DecimalFormat DF   = new java.text.DecimalFormat("#,##0");

    // -- Inner cell holder --
    private static final class CV {
        final String value;
        final int    style;
        CV(String v, int s) { value = v; style = s; }
    }

    // ============================================================
    // MAIN EXPORT
    // ============================================================
    public static String xuatExcel(
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

        String fileName = "ThongKeHangHoa_" + LocalDateTime.now().format(FILE_TIME) + ".xlsx";
        Path   filePath = dir.resolve(fileName);

        try (OutputStream os  = Files.newOutputStream(filePath);
             ZipOutputStream zos = new ZipOutputStream(os)) {

            writeEntry(zos, "[Content_Types].xml", buildContentTypes());
            writeEntry(zos, "_rels/.rels",          buildRootRels());
            writeEntry(zos, "xl/workbook.xml",      buildWorkbook());
            writeEntry(zos, "xl/_rels/workbook.xml.rels", buildWorkbookRels());
            writeEntry(zos, "xl/styles.xml",        buildStyles());
            writeEntry(zos, "xl/worksheets/sheet1.xml",
                    buildSheet(tuNgay, denNgay, danhMuc,
                            tongQuan, topBanChay, doanhThuTheoDanhMuc,
                            xuHuongTheoNgay, chamBan));
        }
        return filePath.toString();
    }

    // ============================================================
    // ZIP HELPER
    // ============================================================
    private static void writeEntry(ZipOutputStream zos, String name, String content)
            throws Exception {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    // ============================================================
    // XLSX SKELETON
    // ============================================================
    private static String buildContentTypes() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
               "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">" +
               "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>" +
               "<Default Extension=\"xml\"  ContentType=\"application/xml\"/>" +
               "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>" +
               "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>" +
               "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>" +
               "</Types>";
    }

    private static String buildRootRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
               "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
               "<Relationship Id=\"rId1\" " +
               "  Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" " +
               "  Target=\"xl/workbook.xml\"/>" +
               "</Relationships>";
    }

    private static String buildWorkbook() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
               "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" " +
               "  xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">" +
               "<sheets><sheet name=\"ThongKeHangHoa\" sheetId=\"1\" r:id=\"rId1\"/></sheets>" +
               "</workbook>";
    }

    private static String buildWorkbookRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
               "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
               "<Relationship Id=\"rId1\" " +
               "  Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" " +
               "  Target=\"worksheets/sheet1.xml\"/>" +
               "<Relationship Id=\"rId2\" " +
               "  Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" " +
               "  Target=\"styles.xml\"/>" +
               "</Relationships>";
    }

    private static String buildStyles() {
        // style index:
        // 0 = Title (blue bold large, center)
        // 1 = Subtitle (italic gray, center)
        // 2 = Blue header (white bold on blue bg, center)
        // 3 = Red header  (white bold on red  bg, center)
        // 4 = Label cell  (bold dark, light bg, left)
        // 5 = Normal left (border)
        // 6 = Normal right (border)
        // 7 = Normal center (border)
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
               "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">" +
               "<fonts count=\"6\">" +
               "  <font><sz val=\"11\"/><name val=\"Calibri\"/></font>" +                              // 0 normal
               "  <font><b/><sz val=\"18\"/><color rgb=\"FF2980B9\"/><name val=\"Calibri\"/></font>" + // 1 title
               "  <font><i/><sz val=\"10\"/><color rgb=\"FF6B7280\"/><name val=\"Calibri\"/></font>" + // 2 italic gray
               "  <font><b/><sz val=\"10\"/><color rgb=\"FFFFFFFF\"/><name val=\"Calibri\"/></font>" + // 3 white bold (header)
               "  <font><b/><sz val=\"10\"/><color rgb=\"FF1F2937\"/><name val=\"Calibri\"/></font>" + // 4 dark bold (label)
               "  <font><sz val=\"10\"/><name val=\"Calibri\"/></font>" +                              // 5 small normal
               "</fonts>" +
               "<fills count=\"5\">" +
               "  <fill><patternFill patternType=\"none\"/></fill>" +
               "  <fill><patternFill patternType=\"gray125\"/></fill>" +
               "  <fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFF0F9FF\"/></patternFill></fill>" + // light blue row
               "  <fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF2980B9\"/></patternFill></fill>" + // blue header
               "  <fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFC0392B\"/></patternFill></fill>" + // red  header
               "</fills>" +
               "<borders count=\"2\">" +
               "  <border><left/><right/><top/><bottom/><diagonal/></border>" +
               "  <border>" +
               "    <left style=\"thin\"><color rgb=\"FFD1D5DB\"/></left>" +
               "    <right style=\"thin\"><color rgb=\"FFD1D5DB\"/></right>" +
               "    <top style=\"thin\"><color rgb=\"FFD1D5DB\"/></top>" +
               "    <bottom style=\"thin\"><color rgb=\"FFD1D5DB\"/></bottom>" +
               "    <diagonal/>" +
               "  </border>" +
               "</borders>" +
               "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>" +
               "<cellXfs count=\"8\">" +
               "  <xf fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" + // 0 Title
               "  <xf fontId=\"2\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"center\"/></xf>" +                    // 1 Subtitle
               "  <xf fontId=\"3\" fillId=\"3\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" + // 2 Blue header
               "  <xf fontId=\"3\" fillId=\"4\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" + // 3 Red header
               "  <xf fontId=\"4\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>" +  // 4 Label
               "  <xf fontId=\"5\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>" +                  // 5 Normal left
               "  <xf fontId=\"5\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"right\" vertical=\"center\"/></xf>" +                 // 6 Normal right
               "  <xf fontId=\"5\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" +                // 7 Normal center
               "</cellXfs>" +
               "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>" +
               "</styleSheet>";
    }

    // ============================================================
    // SHEET CONTENT
    // ============================================================
    private static String buildSheet(
            LocalDate tuNgay, LocalDate denNgay, String danhMuc,
            Map<String, Object> tongQuan,
            List<Map<String, Object>> topBanChay,
            List<Map<String, Object>> doanhThuTheoDanhMuc,
            List<Map<String, Object>> xuHuongTheoNgay,
            List<Map<String, Object>> chamBan) {

        StringBuilder sb     = new StringBuilder();
        List<String>  merges = new ArrayList<>();

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
        sb.append("<sheetFormatPr defaultRowHeight=\"20\"/>");
        // Column widths: A=14, B=36, C=20, D=20, E=20, F=20, G=20
        sb.append("<cols>");
        sb.append(col(1, 1, 14));
        sb.append(col(2, 2, 36));
        sb.append(col(3, 3, 22));
        sb.append(col(4, 4, 18));
        sb.append(col(5, 5, 20));
        sb.append(col(6, 6, 18));
        sb.append(col(7, 7, 18));
        sb.append("</cols>");
        sb.append("<sheetData>");

        int row = 1;

        // ── HEADER ──
        row = addMergedRow(sb, merges, row, 0, "BÁO CÁO THỐNG KÊ HÀNG HÓA");
        row = addMergedRow(sb, merges, row, 1, "Nhà Thuốc Long Nguyên");
        row = addMergedRow(sb, merges, row, 1,
                "Từ ngày " + tuNgay.format(SHOW_DATE) + " đến " + denNgay.format(SHOW_DATE));
        row = addMergedRow(sb, merges, row, 1,
                "Danh mục: " + (danhMuc == null || danhMuc.isEmpty() ? "Tất cả" : danhMuc));
        row++;

        // ── THÔNG TIN BÁO CÁO ──
        row = addMergedRow(sb, merges, row, 2, "THÔNG TIN BÁO CÁO");
        row = addHeaderRow(sb, row, "Chỉ tiêu", "Giá trị");
        row = addTwoCol(sb, row, "Ngày lập báo cáo", LocalDate.now().format(SHOW_DATE));
        row = addTwoCol(sb, row, "Từ ngày",           tuNgay.format(SHOW_DATE));
        row = addTwoCol(sb, row, "Đến ngày",          denNgay.format(SHOW_DATE));
        row = addTwoCol(sb, row, "Danh mục lọc",
                (danhMuc == null || danhMuc.isEmpty()) ? "Tất cả" : danhMuc);
        row++;

        // ── TỔNG QUAN ──
        row = addMergedRow(sb, merges, row, 2, "TỔNG HỢP CHỈ TIÊU CHÍNH");
        row = addHeaderRow(sb, row, "Chỉ tiêu", "Giá trị");
        row = addTwoCol(sb, row, "Tổng mặt hàng đã bán",
                str(tongQuan.get("tongMatHang")) + " mặt hàng");
        row = addTwoCol(sb, row, "Tổng số lượng bán ra",
                DF.format(toDouble(tongQuan.get("tongSoLuongBan"))) + " đơn vị");
        row = addTwoCol(sb, row, "Tổng doanh thu",
                DF.format(toDouble(tongQuan.get("tongDoanhThu"))) + " ₫");
        row = addTwoCol(sb, row, "Tổng số đơn hàng",
                str(tongQuan.get("tongDon")) + " đơn");
        row++;

        // ── TOP BÁN CHẠY ──
        row = addMergedRow(sb, merges, row, 2, "TOP THUỐC BÁN CHẠY NHẤT");
        row = addRow(sb, row,
                cv("STT",        2), cv("Tên thuốc",   2),
                cv("Danh mục",   2), cv("SL Bán",      2), cv("Doanh thu", 2));
        if (empty(topBanChay)) {
            row = addRow(sb, row, cv("", 5), cv("Không có dữ liệu", 5));
        } else {
            for (Map<String, Object> item : topBanChay) {
                row = addRow(sb, row,
                        cv(str(item.get("stt")),        7),
                        cv(str(item.get("tenThuoc")),   5),
                        cv(str(item.get("tenDanhMuc")), 5),
                        cv(DF.format(toDouble(item.get("soLuongBan"))), 6),
                        cv(DF.format(toDouble(item.get("doanhThu"))) + " ₫", 6));
            }
        }
        row++;

        // ── DOANH THU THEO NHÓM ──
        double tongDT = (doanhThuTheoDanhMuc == null) ? 0
                : doanhThuTheoDanhMuc.stream().mapToDouble(r -> toDouble(r.get("doanhThu"))).sum();

        row = addMergedRow(sb, merges, row, 2, "DOANH THU THEO NHÓM THUỐC");
        row = addRow(sb, row,
                cv("Nhóm thuốc", 2), cv("SL Bán", 2),
                cv("Doanh thu",  2), cv("Tỷ lệ",  2));
        if (empty(doanhThuTheoDanhMuc)) {
            row = addRow(sb, row, cv("Không có dữ liệu", 5));
        } else {
            for (Map<String, Object> item : doanhThuTheoDanhMuc) {
                double dt   = toDouble(item.get("doanhThu"));
                String tyLe = tongDT > 0
                        ? String.format("%.1f%%", (dt / tongDT) * 100) : "0.0%";
                row = addRow(sb, row,
                        cv(str(item.get("tenDanhMuc")),                    5),
                        cv(DF.format(toDouble(item.get("soLuongBan"))),    6),
                        cv(DF.format(dt) + " ₫",                           6),
                        cv(tyLe,                                            7));
            }
        }
        row++;

        // ── XU HƯỚNG THEO NGÀY ──
        row = addMergedRow(sb, merges, row, 2, "XU HƯỚNG BÁN HÀNG THEO NGÀY");
        row = addRow(sb, row, cv("Ngày", 2), cv("Số lượng bán", 2), cv("Doanh thu", 2));
        if (empty(xuHuongTheoNgay)) {
            row = addRow(sb, row, cv("Không có dữ liệu", 5));
        } else {
            for (Map<String, Object> item : xuHuongTheoNgay) {
                Object ngayObj = item.get("ngay");
                String ngayStr = (ngayObj instanceof LocalDate)
                        ? ((LocalDate) ngayObj).format(SHOW_DATE) : str(ngayObj);
                row = addRow(sb, row,
                        cv(ngayStr,                                          7),
                        cv(DF.format(toDouble(item.get("soLuongBan"))),      6),
                        cv(DF.format(toDouble(item.get("doanhThu"))) + " ₫", 6));
            }
        }
        row++;

        // ── THUỐC CHẬM BÁN ──
        row = addMergedRow(sb, merges, row, 3, "THUỐC CHẬM BÁN (CÒN TỒN KHO)");
        row = addRow(sb, row,
                cv("STT",        3), cv("Tên thuốc",   3),
                cv("Danh mục",   3), cv("SL Bán",      3), cv("Tồn kho",   3));
        if (empty(chamBan)) {
            row = addRow(sb, row, cv("", 5), cv("Không có dữ liệu", 5));
        } else {
            for (Map<String, Object> item : chamBan) {
                row = addRow(sb, row,
                        cv(str(item.get("stt")),        7),
                        cv(str(item.get("tenThuoc")),   5),
                        cv(str(item.get("tenDanhMuc")), 5),
                        cv(DF.format(toDouble(item.get("soLuongBan"))), 6),
                        cv(DF.format(toDouble(item.get("tonKho"))),     6));
            }
        }
        row++;

        // Footer
        row = addMergedRow(sb, merges, row, 1,
                "Báo cáo được tạo tự động từ hệ thống thống kê hàng hóa.");

        sb.append("</sheetData>");

        if (!merges.isEmpty()) {
            sb.append("<mergeCells count=\"").append(merges.size()).append("\">");
            for (String m : merges) sb.append("<mergeCell ref=\"").append(m).append("\"/>");
            sb.append("</mergeCells>");
        }

        sb.append("<pageMargins left=\"0.5\" right=\"0.5\" top=\"0.5\" bottom=\"0.5\" header=\"0.3\" footer=\"0.3\"/>");
        sb.append("</worksheet>");
        return sb.toString();
    }

    // ============================================================
    // ROW / CELL BUILDERS
    // ============================================================
    private static int addMergedRow(StringBuilder sb, List<String> merges,
            int rowNum, int style, String text) {
        merges.add("A" + rowNum + ":G" + rowNum);
        return addRow(sb, rowNum, cv(text, style));
    }

    private static int addHeaderRow(StringBuilder sb, int rowNum,
            String... values) {
        CV[] cells = new CV[values.length];
        for (int i = 0; i < values.length; i++) cells[i] = cv(values[i], 2);
        return addRow(sb, rowNum, cells);
    }

    private static int addTwoCol(StringBuilder sb, int rowNum,
            String label, String value) {
        return addRow(sb, rowNum, cv(label, 4), cv(value, 5));
    }

    private static int addRow(StringBuilder sb, int rowNum, CV... cells) {
        sb.append("<row r=\"").append(rowNum).append("\">");
        for (int i = 0; i < cells.length; i++) {
            CV c = cells[i];
            if (c == null || c.value == null) continue;
            String ref = colName(i + 1) + rowNum;
            sb.append("<c r=\"").append(ref)
              .append("\" s=\"").append(c.style)
              .append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
              .append(escapeXml(c.value))
              .append("</t></is></c>");
        }
        sb.append("</row>");
        return rowNum + 1;
    }

    private static CV cv(String v, int s) { return new CV(v, s); }

    private static String col(int min, int max, double width) {
        return "<col min=\"" + min + "\" max=\"" + max
                + "\" width=\"" + width + "\" customWidth=\"1\"/>";
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

    private static String escapeXml(String v) {
        if (v == null) return "";
        return v.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    // ============================================================
    // UTILITY
    // ============================================================
    private static double toDouble(Object value) {
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value == null) return 0;
        try { return Double.parseDouble(String.valueOf(value)); }
        catch (NumberFormatException e) { return 0; }
    }

    private static String str(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static boolean empty(List<?> list) {
        return list == null || list.isEmpty();
    }
}