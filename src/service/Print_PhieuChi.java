package service;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import entity.PhieuChi;

public class Print_PhieuChi {
    private static DecimalFormat df = new DecimalFormat("#,###");
    private static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public static void inPhieu(PhieuChi pc) {
        // Đường dẫn lưu file PDF tạm
        String path = "reports/PhieuChi_" + pc.getMaPhieuChi() + ".pdf";

        try {
            // Tạo thư mục nếu chưa có
            File directory = new File("reports");
            if (!directory.exists()) {
				directory.mkdirs();
			}

            Document document = new Document(PageSize.A5.rotate()); // In khổ A5 ngang cho tiết kiệm giấy
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();

            // Cấu hình Font tiếng Việt (Sếp kiểm tra đường dẫn font này trong project nhé)
            BaseFont bf = BaseFont.createFont("src/resources/fonts/vuArial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font fontHeader = new Font(bf, 16, Font.BOLD);
            Font fontTitle = new Font(bf, 13, Font.BOLD);
            Font fontNormal = new Font(bf, 11, Font.NORMAL);
            Font fontItalic = new Font(bf, 10, Font.ITALIC);

            // 1. Thông tin nhà thuốc
            Paragraph nhaThuoc = new Paragraph("NHÀ THUỐC LONG NGUYÊN", fontHeader);
            nhaThuoc.setAlignment(Element.ALIGN_LEFT);
            document.add(nhaThuoc);

            document.add(new Paragraph("Địa chỉ: Quận Gò Vấp, TP. Hồ Chí Minh", fontItalic));
            document.add(new Paragraph("Điện thoại: 0987.xxx.xxx", fontItalic));
            document.add(new Chunk(new LineSeparator()));

            // 2. Tiêu đề phiếu
            Paragraph title = new Paragraph("\nPHIẾU CHI TIỀN", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph maPhieu = new Paragraph("Mã phiếu: " + pc.getMaPhieuChi(), fontItalic);
            maPhieu.setAlignment(Element.ALIGN_CENTER);
            document.add(maPhieu);
            document.add(new Paragraph("\n"));

            // 3. Nội dung chi tiết
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.2f, 3f});

            addRow(table, "Nhà cung cấp:", pc.getNhaCungCap().getTenNhaCungCap(), fontNormal);
            addRow(table, "Lý do chi:", (pc.getGhiChu() == null || pc.getGhiChu().isEmpty()) ? "Thanh toán công nợ" : pc.getGhiChu(), fontNormal);
            addRow(table, "Hình thức:", dichHinhThuc(pc.getHinhThucChi()), fontNormal);
            addRow(table, "Số tiền chi:", df.format(pc.getTongTienChi()) + " VNĐ", fontTitle);

            // Dòng quan trọng: Đọc tiền bằng chữ
            addRow(table, "Bằng chữ:", docSoThanhChu((long) pc.getTongTienChi()), fontItalic);

            document.add(table);

            // 4. Chữ ký
            document.add(new Paragraph("\n"));
            PdfPTable tableKy = new PdfPTable(2);
            tableKy.setWidthPercentage(100);

            PdfPCell cell1 = new PdfPCell(new Paragraph("Người lập phiếu\n(Ký và ghi rõ họ tên)", fontNormal));
            PdfPCell cell2 = new PdfPCell(new Paragraph("Người nhận tiền\n(Ký và ghi rõ họ tên)", fontNormal));

            cell1.setBorder(Rectangle.NO_BORDER); cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell2.setBorder(Rectangle.NO_BORDER); cell2.setHorizontalAlignment(Element.ALIGN_CENTER);

            tableKy.addCell(cell1);
            tableKy.addCell(cell2);
            document.add(tableKy);

            document.add(new Paragraph("\n\n\n"));
            Paragraph ngayKy = new Paragraph("Ngày in phiếu: " + sdf.format(new java.util.Date()), fontItalic);
            ngayKy.setAlignment(Element.ALIGN_RIGHT);
            document.add(ngayKy);

            document.close();

            // Mở file PDF sau khi in xong
            Desktop.getDesktop().open(new File(path));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell cellLabel = new PdfPCell(new Paragraph(label, font));
        cellLabel.setBorder(Rectangle.NO_BORDER);
        cellLabel.setPaddingBottom(8);

        PdfPCell cellValue = new PdfPCell(new Paragraph(value, font));
        cellValue.setBorder(Rectangle.NO_BORDER);
        cellValue.setPaddingBottom(8);

        table.addCell(cellLabel);
        table.addCell(cellValue);
    }

    private static String dichHinhThuc(String ht) {
        if ("CHUYEN_KHOAN".equals(ht)) {
			return "Chuyển Khoản";
		}
        if ("THE".equals(ht)) {
			return "Thẻ";
		}
        return "Tiền Mặt";
    }

    // Hàm phụ trợ đọc số thành chữ (Sếp có thể dùng thư viện hoặc hàm tự viết)
    public static String docSoThanhChu(long amount) {
        if (amount == 0) {
			return "Không đồng";
		}
        // Phần này sếp có thể copy hàm đọc số tiền Việt Nam trên mạng vào nhé
        // Để demo em ghi tạm là:
        return "(Một số tiền bằng chữ tương ứng) đồng chẵn.";
    }
}