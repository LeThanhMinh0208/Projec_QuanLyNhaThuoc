package utils;

import entity.ChiTietBangGia;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Đọc file Excel (.xlsx) để lấy danh sách giá bán mà không cần thư viện ngoài (Apache POI).
 * Hỗ trợ đọc cả chuỗi dạng inline (inlineStr) và shared strings (s).
 */
public class BangGiaExcelImporter {

    public static List<ChiTietBangGia> docFileExcel(String filePath, List<ChiTietBangGia> chiTietHienTai) throws Exception {
        List<ChiTietBangGia> result = new ArrayList<>();
        Map<Integer, String> sharedStrings = new HashMap<>();

        try (ZipFile zipFile = new ZipFile(filePath)) {
            // 1. Đọc sharedStrings.xml (nếu có, do Excel tự sinh khi lưu)
            ZipEntry sharedStringsEntry = zipFile.getEntry("xl/sharedStrings.xml");
            if (sharedStringsEntry != null) {
                try (InputStream is = zipFile.getInputStream(sharedStringsEntry)) {
                    sharedStrings = parseSharedStrings(is);
                }
            }

            // 2. Đọc sheet1.xml
            ZipEntry sheetEntry = zipFile.getEntry("xl/worksheets/sheet1.xml");
            if (sheetEntry == null) {
                throw new Exception("File Excel không đúng định dạng (không tìm thấy sheet1.xml).");
            }

            try (InputStream is = zipFile.getInputStream(sheetEntry)) {
                parseSheet(is, sharedStrings, chiTietHienTai, result);
            }
        }

        return result;
    }

    private static Map<Integer, String> parseSharedStrings(InputStream is) throws Exception {
        Map<Integer, String> sharedStrings = new HashMap<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(is);

        NodeList siNodes = doc.getElementsByTagName("si");
        for (int i = 0; i < siNodes.getLength(); i++) {
            Element si = (Element) siNodes.item(i);
            NodeList tNodes = si.getElementsByTagName("t");
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < tNodes.getLength(); j++) {
                sb.append(tNodes.item(j).getTextContent());
            }
            sharedStrings.put(i, sb.toString());
        }
        return sharedStrings;
    }

    private static void parseSheet(InputStream is, Map<Integer, String> sharedStrings, 
                                   List<ChiTietBangGia> chiTietHienTai, List<ChiTietBangGia> result) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(is);

        NodeList rowNodes = doc.getElementsByTagName("row");
        boolean isHeaderFound = false;

        // Map để tìm nhanh ChiTietBangGia theo Tên Thuốc + Đơn Vị
        Map<String, ChiTietBangGia> mapChiTiet = new HashMap<>();
        if (chiTietHienTai != null) {
            for (ChiTietBangGia ct : chiTietHienTai) {
                String key = (ct.getTenThuoc() + "_" + ct.getTenDonVi()).toLowerCase();
                mapChiTiet.put(key, ct);
            }
        }

        for (int i = 0; i < rowNodes.getLength(); i++) {
            Element row = (Element) rowNodes.item(i);
            NodeList cNodes = row.getElementsByTagName("c");

            List<String> cellValues = new ArrayList<>();
            // Khởi tạo 4 cột rỗng
            for (int j = 0; j < 4; j++) cellValues.add("");

            for (int j = 0; j < cNodes.getLength(); j++) {
                Element c = (Element) cNodes.item(j);
                String r = c.getAttribute("r"); // VD: A1, B2, C3
                int colIndex = getColumnIndex(r);
                if (colIndex >= 4) continue; // Chỉ quan tâm tối đa 4 cột đầu (STT, Tên, ĐV, Giá)

                String cellType = c.getAttribute("t");
                String value = "";

                // Lấy giá trị thô
                NodeList vNodes = c.getElementsByTagName("v");
                if (vNodes.getLength() > 0) {
                    value = vNodes.item(0).getTextContent();
                } else {
                    // Xử lý inlineStr
                    NodeList isNodes = c.getElementsByTagName("is");
                    if (isNodes.getLength() > 0) {
                        NodeList tNodes = ((Element)isNodes.item(0)).getElementsByTagName("t");
                        if (tNodes.getLength() > 0) {
                            value = tNodes.item(0).getTextContent();
                        }
                    }
                }

                // Xử lý shared string
                if ("s".equals(cellType) && !value.isEmpty()) {
                    try {
                        int ssi = Integer.parseInt(value);
                        value = sharedStrings.getOrDefault(ssi, "");
                    } catch (NumberFormatException ignored) {}
                }

                cellValues.set(colIndex, value.trim());
            }

            // Bỏ qua các dòng tiêu đề chung (kiểm tra dòng có chữ "Tên Thuốc")
            if (!isHeaderFound) {
                if (cellValues.contains("Tên Thuốc") && cellValues.contains("Đơn Vị")) {
                    isHeaderFound = true;
                }
                continue;
            }

            // Đã qua header, bắt đầu xử lý data
            String tenThuoc = "";
            String donVi = "";
            String giaStr = "";

            // File mẫu có 3 cột (A: Tên, B: ĐV, C: Giá), file xuất data có 4 cột (B: Tên, C: ĐV, D: Giá)
            if (cellValues.get(0).isEmpty() || !cellValues.get(0).matches("\\d+")) {
                // Khả năng là file mẫu 3 cột
                tenThuoc = cellValues.get(0);
                donVi = cellValues.get(1);
                giaStr = cellValues.get(2);
            } else {
                // Khả năng là file xuất 4 cột có STT
                tenThuoc = cellValues.get(1);
                donVi = cellValues.get(2);
                giaStr = cellValues.get(3);
            }

            if (tenThuoc.isEmpty() || donVi.isEmpty()) continue;

            String key = (tenThuoc + "_" + donVi).toLowerCase();
            ChiTietBangGia ct = mapChiTiet.get(key);
            
            if (ct != null && !giaStr.isEmpty()) {
                try {
                    giaStr = giaStr.replace(",", "").replace(".", ""); // Bỏ dấu phân cách hàng nghìn
                    BigDecimal donGiaBan = new BigDecimal(giaStr);
                    
                    // Tạo bản sao để tránh sửa trực tiếp vào list ban đầu nếu chưa xác nhận lưu
                    ChiTietBangGia updatedCt = new ChiTietBangGia(
                            ct.getMaBangGia(), ct.getMaQuyDoi(), donGiaBan,
                            ct.getMaThuoc(), ct.getTenThuoc(), ct.getTenDonVi()
                    );
                    result.add(updatedCt);
                } catch (NumberFormatException ignored) {
                    // Bỏ qua giá không hợp lệ
                }
            }
        }
    }

    private static int getColumnIndex(String r) {
        if (r == null || r.isEmpty()) return 0;
        String letters = r.replaceAll("[0-9]", "");
        int index = 0;
        for (int i = 0; i < letters.length(); i++) {
            index = index * 26 + (letters.charAt(i) - 'A' + 1);
        }
        return index - 1; // 0-based
    }
}
