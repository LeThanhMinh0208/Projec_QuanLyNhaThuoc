package utils;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DonDatHangExcelExporter {

    public static class DrugRow {
        public final String tenThuoc;
        public final String tenDonVi;
        public DrugRow(String tenThuoc, String tenDonVi) {
            this.tenThuoc = tenThuoc != null ? tenThuoc : "";
            this.tenDonVi = tenDonVi != null ? tenDonVi : "";
        }
    }

    private static final class CV {
        final String v;
        final int s;
        CV(String v, int s) { this.v = v; this.s = s; }
    }

    public static void xuatFileMau(List<DrugRow> dsThuoc, File file) throws Exception {
        try (OutputStream os = Files.newOutputStream(file.toPath());
             ZipOutputStream zos = new ZipOutputStream(os)) {
            write(zos, "[Content_Types].xml", contentTypes());
            write(zos, "_rels/.rels", rootRels());
            write(zos, "xl/workbook.xml", workbook());
            write(zos, "xl/_rels/workbook.xml.rels", workbookRels());
            write(zos, "xl/styles.xml", styles());
            write(zos, "xl/worksheets/sheet1.xml", sheet(dsThuoc));
        }
    }

    private static void write(ZipOutputStream zos, String name, String content) throws Exception {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private static String sheet(List<DrugRow> dsThuoc) {
        StringBuilder sb = new StringBuilder();
        List<String> merges = new ArrayList<>();

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">\n");
        sb.append("<sheetFormatPr defaultRowHeight=\"22\"/>\n");
        sb.append("<cols>");
        sb.append(colDef(1, 1, 45));
        sb.append(colDef(2, 2, 18));
        sb.append(colDef(3, 3, 15));
        sb.append("</cols>\n");
        sb.append("<sheetData>\n");

        int r = 1;

        // Row 1: Title
        merges.add("A1:C1");
        sb.append(row(r++, new CV[]{ new CV("FILE MẪU ĐẶT HÀNG - Nhà Thuốc Long Nguyên", 0) }));

        // Row 2: Instructions
        merges.add("A2:C2");
        sb.append(row(r++, new CV[]{ new CV(
            "Hướng dẫn: Điền Nhà Cung Cấp vào ô B3, Ghi Chú vào ô B4. " +
            "Điền Số Lượng cần đặt (để trống hoặc 0 = bỏ qua). " +
            "Không thay đổi cột Tên Thuốc và Đơn Vị.", 2) }));

        // Row 3: NCC — A3=label, B3:C3 merged = input (yellow)
        merges.add("B3:C3");
        sb.append(row(r++, new CV[]{ new CV("Nhà Cung Cấp:", 5), new CV("", 9) }));

        // Row 4: Ghi Chú — A4=label, B4:C4 merged = input (yellow)
        merges.add("B4:C4");
        sb.append(row(r++, new CV[]{ new CV("Ghi Chú:", 5), new CV("", 9) }));

        // Row 5: empty spacer
        r++;

        // Row 6: Table headers
        sb.append(row(r++, new CV[]{ new CV("Tên Thuốc", 3), new CV("Đơn Vị", 3), new CV("Số Lượng", 3) }));

        // Data rows (row 7+)
        if (dsThuoc != null) {
            for (DrugRow dr : dsThuoc) {
                sb.append(row(r++, new CV[]{ new CV(dr.tenThuoc, 7), new CV(dr.tenDonVi, 6), new CV("", 8) }));
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
            r.append("<c r=\"").append(ref).append("\" s=\"").append(cells[i].s)
             .append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
             .append(esc(cells[i].v)).append("</t></is></c>");
        }
        r.append("</row>\n");
        return r.toString();
    }

    private static String colName(int idx) {
        StringBuilder sb = new StringBuilder();
        while (idx > 0) {
            int rem = (idx - 1) % 26;
            sb.insert(0, (char)('A' + rem));
            idx = (idx - 1) / 26;
        }
        return sb.toString();
    }

    private static String colDef(int min, int max, double w) {
        return "<col min=\"" + min + "\" max=\"" + max + "\" width=\"" + w + "\" customWidth=\"1\"/>";
    }

    private static String esc(String v) {
        return v.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
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
            "<sheets><sheet name=\"DonDatHang\" sheetId=\"1\" r:id=\"rId1\"/></sheets>" +
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
            "<font><b/><sz val=\"10\"/><color rgb=\"FFFFFFFF\"/><name val=\"Calibri\"/><family val=\"2\"/></font>" +
            "<font><b/><sz val=\"10\"/><color rgb=\"FF1F2937\"/><name val=\"Calibri\"/><family val=\"2\"/></font>" +
            "</fonts>" +
            "<fills count=\"6\">" +
            "<fill><patternFill patternType=\"none\"/></fill>" +
            "<fill><patternFill patternType=\"gray125\"/></fill>" +
            "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFF8FAFC\"/><bgColor indexed=\"64\"/></patternFill></fill>" +
            "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFB91C1C\"/><bgColor indexed=\"64\"/></patternFill></fill>" +
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
            "<cellXfs count=\"10\">" +
            "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" +
            "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" +
            "<xf numFmtId=\"0\" fontId=\"3\" fillId=\"4\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"center\" vertical=\"center\" wrapText=\"1\"/></xf>" +
            "<xf numFmtId=\"0\" fontId=\"3\" fillId=\"3\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" +
            "<xf numFmtId=\"0\" fontId=\"4\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>" +
            "<xf numFmtId=\"0\" fontId=\"4\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>" +
            "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>" +
            "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>" +
            "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"right\" vertical=\"center\"/></xf>" +
            "<xf numFmtId=\"0\" fontId=\"4\" fillId=\"5\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>" +
            "</cellXfs>" +
            "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>" +
            "<dxfs count=\"0\"/>" +
            "<tableStyles count=\"0\" defaultTableStyle=\"TableStyleMedium2\" defaultPivotStyle=\"PivotStyleLight16\"/>" +
            "</styleSheet>";
    }
}
