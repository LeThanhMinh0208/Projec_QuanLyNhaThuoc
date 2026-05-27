package utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class KiemKeChonLoExcelImporter {

    /**
     * Đọc file Excel mẫu chọn lô kiểm kê.
     * Trả về danh sách maLoThuoc (uppercase) của những lô được đánh dấu ở cột CHỌN (D).
     */
    public static List<String> docFileExcel(String filePath) throws Exception {
        List<String> result = new ArrayList<>();
        Map<Integer, String> sharedStrings = new HashMap<>();

        try (ZipFile zipFile = new ZipFile(filePath)) {
            ZipEntry ssEntry = zipFile.getEntry("xl/sharedStrings.xml");
            if (ssEntry != null) {
                try (InputStream is = zipFile.getInputStream(ssEntry)) {
                    sharedStrings = parseSharedStrings(is);
                }
            }
            ZipEntry sheetEntry = zipFile.getEntry("xl/worksheets/sheet1.xml");
            if (sheetEntry == null) throw new Exception("File Excel không đúng định dạng (thiếu sheet1.xml).");
            try (InputStream is = zipFile.getInputStream(sheetEntry)) {
                parseSheet(is, sharedStrings, result);
            }
        }
        return result;
    }

    private static Map<Integer, String> parseSharedStrings(InputStream is) throws Exception {
        Map<Integer, String> ss = new HashMap<>();
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
        NodeList siNodes = doc.getElementsByTagName("si");
        for (int i = 0; i < siNodes.getLength(); i++) {
            Element si = (Element) siNodes.item(i);
            NodeList tNodes = si.getElementsByTagName("t");
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < tNodes.getLength(); j++) sb.append(tNodes.item(j).getTextContent());
            ss.put(i, sb.toString());
        }
        return ss;
    }

    private static void parseSheet(InputStream is, Map<Integer, String> ss, List<String> result) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
        NodeList rowNodes = doc.getElementsByTagName("row");
        boolean headerFound = false;

        for (int i = 0; i < rowNodes.getLength(); i++) {
            Element rowEl = (Element) rowNodes.item(i);

            // Đọc 4 cột A-D (index 0-3)
            String[] vals = {"", "", "", ""};
            NodeList cNodes = rowEl.getElementsByTagName("c");
            for (int j = 0; j < cNodes.getLength(); j++) {
                Element c = (Element) cNodes.item(j);
                int colIdx = getColIdx(c.getAttribute("r"));
                if (colIdx >= 4) continue;
                vals[colIdx] = getCellValue(c, ss);
            }

            // Tìm dòng header: col A = "TÊN THUỐC"
            if (!headerFound) {
                String a = vals[0].trim();
                if (a.equalsIgnoreCase("TÊN THUỐC") || a.equalsIgnoreCase("TEN THUOC")) {
                    headerFound = true;
                }
                continue;
            }

            // Dòng dữ liệu: B(index1)=maLoThuoc, D(index3)=tick
            String maLo = vals[1].trim();
            if (maLo.isEmpty()) continue;

            String tick = vals[3].trim();
            if (!tick.isEmpty()) {
                result.add(maLo.toUpperCase());
            }
        }
    }

    private static String getCellValue(Element c, Map<Integer, String> ss) {
        NodeList isNodes = c.getElementsByTagName("is");
        if (isNodes.getLength() > 0) {
            NodeList tNodes = ((Element) isNodes.item(0)).getElementsByTagName("t");
            if (tNodes.getLength() > 0) return tNodes.item(0).getTextContent();
        }
        NodeList vNodes = c.getElementsByTagName("v");
        if (vNodes.getLength() > 0) {
            String v = vNodes.item(0).getTextContent();
            if ("s".equals(c.getAttribute("t"))) {
                try { return ss.getOrDefault(Integer.parseInt(v), ""); }
                catch (NumberFormatException e) { /* fall through */ }
            }
            return v;
        }
        return "";
    }

    private static int getColIdx(String r) {
        if (r == null || r.isEmpty()) return 0;
        String letters = r.replaceAll("[0-9]", "");
        int idx = 0;
        for (int i = 0; i < letters.length(); i++) idx = idx * 26 + (letters.charAt(i) - 'A' + 1);
        return idx - 1;
    }
}
