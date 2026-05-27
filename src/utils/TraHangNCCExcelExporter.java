package utils;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class TraHangNCCExcelExporter {

    public static class LoRow {
        public final String tenThuoc;
        public final String donVi;
        public final String soLo;
        public final String viTriKho;
        public final int soLuongTon;

        public LoRow(String tenThuoc, String donVi, String soLo, String viTriKho, int soLuongTon) {
            this.tenThuoc   = tenThuoc  != null ? tenThuoc  : "";
            this.donVi      = donVi     != null ? donVi     : "";
            this.soLo       = soLo      != null ? soLo      : "";
            this.viTriKho   = viTriKho  != null ? viTriKho  : "";
            this.soLuongTon = soLuongTon;
        }
    }

    private static final class CV {
        final String v; final int s;
        CV(String v, int s) { this.v = v; this.s = s; }
    }

    public static void xuatFileMau(String tenNCC, String lyDo, List<LoRow> dsLo, File file) throws Exception {
        try (OutputStream os = Files.newOutputStream(file.toPath());
             ZipOutputStream zos = new ZipOutputStream(os)) {
            write(zos, "[Content_Types].xml",          contentTypes());
            write(zos, "_rels/.rels",                   rootRels());
            write(zos, "xl/workbook.xml",               workbook());
            write(zos, "xl/_rels/workbook.xml.rels",    workbookRels());
            write(zos, "xl/styles.xml",                 styles());
            write(zos, "xl/worksheets/sheet1.xml",      sheet(tenNCC, lyDo, dsLo));
        }
    }

    private static void write(ZipOutputStream zos, String name, String content) throws Exception {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private static String sheet(String tenNCC, String lyDo, List<LoRow> dsLo) {
        StringBuilder sb = new StringBuilder();
        List<String> merges = new ArrayList<>();

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">\n");
        sb.append("<sheetFormatPr defaultRowHeight=\"22\"/>\n");
        sb.append("<cols>");
        sb.append(colDef(1, 1, 40)); // A: Tên Thuốc
        sb.append(colDef(2, 2, 22)); // B: Số Lô
        sb.append(colDef(3, 3, 12)); // C: Đơn Vị
        sb.append(colDef(4, 4, 18)); // D: Vị Trí Kho
        sb.append(colDef(5, 5, 12)); // E: SL Tồn
        sb.append(colDef(6, 6, 15)); // F: SL Trả (yellow input)
        sb.append("</cols>\n");
        sb.append("<sheetData>\n");

        int r = 1;

        // Row 1: Title
        merges.add("A1:F1");
        sb.append(row(r++, new CV[]{ new CV("PHIẾU TRẢ HÀNG NHÀ CUNG CẤP - NHÀ THUỐC LONG NGUYÊN", 0) }));

        // Row 2: Instructions
        merges.add("A2:F2");
        sb.append(row(r++, new CV[]{ new CV(
            "Điền Số Lượng Trả cho từng lô thuốc. Để trống sẽ trả toàn bộ số lượng tồn. Không thay đổi các cột khác.", 1) }));

        // Row 3: Nhà cung cấp
        merges.add("B3:F3");
        sb.append(row(r++, new CV[]{
            new CV("Nhà Cung Cấp:", 3),
            new CV(tenNCC != null ? tenNCC : "", 4)
        }));

        // Row 4: Lý do
        merges.add("B4:F4");
        sb.append(row(r++, new CV[]{
            new CV("Lý Do:", 3),
            new CV(lyDo != null ? lyDo : "", 7)
        }));

        // Row 5: Table headers
        sb.append(row(r++, new CV[]{
            new CV("TÊN THUỐC",   2),
            new CV("SỐ LÔ",      2),
            new CV("ĐƠN VỊ",     2),
            new CV("VỊ TRÍ KHO", 2),
            new CV("SL TỒN",     2),
            new CV("SL TRẢ",     2)
        }));

        // Data rows
        if (dsLo != null) {
            for (LoRow lo : dsLo) {
                String tenKho = "KHO_BAN_HANG".equals(lo.viTriKho) ? "Kho bán hàng" : "Kho dự trữ";
                sb.append(row(r++, new CV[]{
                    new CV(lo.tenThuoc,                    5),
                    new CV(lo.soLo,                        5),
                    new CV(lo.donVi,                       5),
                    new CV(tenKho,                         5),
                    new CV(String.valueOf(lo.soLuongTon),  6),
                    new CV("",                             7) // yellow input, user fills SL trả
                }));
            }
        }

        sb.append("</sheetData>\n");
        sb.append("<mergeCells count=\"").append(merges.size()).append("\">\n");
        for (String m : merges) sb.append("<mergeCell ref=\"").append(m).append("\"/>\n");
        sb.append("</mergeCells>\n");
        sb.append("<pageMargins left=\"0.5\" right=\"0.5\" top=\"0.5\" bottom=\"0.5\" header=\"0.3\" footer=\"0.3\"/>\n");
        sb.append("</worksheet>");
        return sb.toString();
    }

    private static String row(int rn, CV[] cells) {
        StringBuilder r = new StringBuilder();
        r.append("<row r=\"").append(rn).append("\">");
        for (int i = 0; i < cells.length; i++) {
            if (cells[i] == null) continue;
            String ref = colName(i + 1) + rn;
            // quotePrefix for yellow input cells (style 7) to prevent date/number auto-conversion
            String qp = (cells[i].s == 7) ? " quotePrefix=\"1\"" : "";
            r.append("<c r=\"").append(ref).append("\" s=\"").append(cells[i].s)
             .append("\"").append(qp)
             .append(" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
             .append(esc(cells[i].v)).append("</t></is></c>");
        }
        r.append("</row>\n");
        return r.toString();
    }

    private static String colName(int idx) {
        StringBuilder sb = new StringBuilder();
        while (idx > 0) { int rem = (idx - 1) % 26; sb.insert(0, (char)('A' + rem)); idx = (idx - 1) / 26; }
        return sb.toString();
    }

    private static String colDef(int min, int max, double w) {
        return "<col min=\"" + min + "\" max=\"" + max + "\" width=\"" + w + "\" customWidth=\"1\"/>";
    }

    private static String esc(String v) {
        return v.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
                .replace("\"","&quot;").replace("'","&apos;");
    }

    private static String contentTypes() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">" +
            "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>" +
            "<Default Extension=\"xml\" ContentType=\"application/xml\"/>" +
            "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>" +
            "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>" +
            "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>" +
            "</Types>";
    }

    private static String rootRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
            "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>" +
            "</Relationships>";
    }

    private static String workbook() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"" +
            " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">" +
            "<sheets><sheet name=\"TraHangNCC\" sheetId=\"1\" r:id=\"rId1\"/></sheets>" +
            "</workbook>";
    }

    private static String workbookRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
            "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>" +
            "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>" +
            "</Relationships>";
    }

    private static String styles() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">" +
            "<fonts count=\"5\">" +
            "<font><sz val=\"11\"/><color rgb=\"FF000000\"/><name val=\"Calibri\"/><family val=\"2\"/></font>" +
            "<font><b/><sz val=\"16\"/><color rgb=\"FF0EA5E9\"/><name val=\"Calibri\"/><family val=\"2\"/></font>" +
            "<font><i/><sz val=\"10\"/><color rgb=\"FF6B7280\"/><name val=\"Calibri\"/><family val=\"2\"/></font>" +
            "<font><b/><sz val=\"11\"/><color rgb=\"FFFFFFFF\"/><name val=\"Calibri\"/><family val=\"2\"/></font>" +
            "<font><b/><sz val=\"10\"/><color rgb=\"FF1F2937\"/><name val=\"Calibri\"/><family val=\"2\"/></font>" +
            "</fonts>" +
            "<fills count=\"5\">" +
            "<fill><patternFill patternType=\"none\"/></fill>" +
            "<fill><patternFill patternType=\"gray125\"/></fill>" +
            "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFF8FAFC\"/><bgColor indexed=\"64\"/></patternFill></fill>" +
            "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF1D4ED8\"/><bgColor indexed=\"64\"/></patternFill></fill>" +
            "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFFEF9C3\"/><bgColor indexed=\"64\"/></patternFill></fill>" +
            "</fills>" +
            "<borders count=\"2\">" +
            "<border><left/><right/><top/><bottom/><diagonal/></border>" +
            "<border>" +
            "<left style=\"thin\"><color rgb=\"FFD1D5DB\"/></left>" +
            "<right style=\"thin\"><color rgb=\"FFD1D5DB\"/></right>" +
            "<top style=\"thin\"><color rgb=\"FFD1D5DB\"/></top>" +
            "<bottom style=\"thin\"><color rgb=\"FFD1D5DB\"/></bottom>" +
            "<diagonal/>" +
            "</border>" +
            "</borders>" +
            "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>" +
            "<cellXfs count=\"8\">" +
            // 0: Title
            "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" +
            // 1: Instructions
            "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"center\" vertical=\"center\" wrapText=\"1\"/></xf>" +
            // 2: Table header (dark blue bg, white bold, centered)
            "<xf numFmtId=\"0\" fontId=\"3\" fillId=\"3\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" +
            // 3: Label cell (light gray bg, dark bold)
            "<xf numFmtId=\"0\" fontId=\"4\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>" +
            // 4: Info value cell (light gray bg, normal)
            "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>" +
            // 5: Data cell (no fill, left)
            "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>" +
            // 6: Number cell (no fill, right)
            "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"right\" vertical=\"center\"/></xf>" +
            // 7: Yellow input cell (yellow fill, dark bold, center, numFmtId=49 text format)
            "<xf numFmtId=\"49\" fontId=\"4\" fillId=\"4\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyNumberFormat=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" +
            "</cellXfs>" +
            "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>" +
            "<dxfs count=\"0\"/>" +
            "<tableStyles count=\"0\" defaultTableStyle=\"TableStyleMedium2\" defaultPivotStyle=\"PivotStyleLight16\"/>" +
            "</styleSheet>";
    }
}
