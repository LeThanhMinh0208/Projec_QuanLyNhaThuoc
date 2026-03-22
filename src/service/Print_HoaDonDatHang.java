package service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import entity.ChiTietDonNhapHang;
import entity.DonNhapHang;
import java.io.File;
import java.io.FileOutputStream;
import java.awt.Desktop;
import java.util.List;

public class Print_HoaDonDatHang {
    public static void inHoaDon(DonNhapHang don, List<ChiTietDonNhapHang> listCT) {
        Document document = new Document(PageSize.A4.rotate());
        try {
            File dir = new File("hoa_don_da_in");
            if (!dir.exists()) dir.mkdirs();
            String path = "hoa_don_da_in/PhieuNhap_" + don.getMaDonNhap() + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();
            
            BaseFont bf = BaseFont.createFont("C:\\Windows\\Fonts\\tahoma.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font fontTitle = new Font(bf, 18, Font.BOLD, BaseColor.BLUE);
            Font fontBold = new Font(bf, 10, Font.BOLD);
            Font fontNormal = new Font(bf, 10, Font.NORMAL);
            
            Paragraph title = new Paragraph(don.getTrangThai().equals("DA_NHAP_KHO") ? "PHIẾU NHẬP KHO THUỐC" : "ĐƠN ĐẶT HÀNG THUỐC", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("Mã phiếu: " + don.getMaDonNhap(), fontBold));
            document.add(new Paragraph("Nhà cung cấp: " + don.getNhaCungCap().getTenNhaCungCap(), fontNormal));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(8); // Tăng lên 8 cột
            table.setWidthPercentage(100);
            String[] headers = {"Tên thuốc", "Đơn vị", "SL Đặt", "SL Nhận", "Mã Lô", "Giá Nhập", "Hạn Dùng", "Thành tiền"};
            for (String h : headers) table.addCell(new Phrase(h, fontBold));

            for (ChiTietDonNhapHang ct : listCT) {
                table.addCell(new Phrase(ct.getThuoc().getTenThuoc(), fontNormal));
                table.addCell(new Phrase(ct.getDonViQuyDoi().getTenDonVi(), fontNormal));
                table.addCell(new Phrase(String.valueOf(ct.getSoLuongDat()), fontNormal));
                table.addCell(new Phrase(String.valueOf(ct.getSoLuongDaNhan()), fontNormal));
                table.addCell(new Phrase(ct.getMaLo() != null ? ct.getMaLo() : "---", fontNormal));
                table.addCell(new Phrase(String.format("%,.0f", ct.getDonGiaDuKien()), fontNormal));
                table.addCell(new Phrase(ct.getHanSuDung() != null ? ct.getHanSuDung() : "---", fontNormal));
                table.addCell(new Phrase(String.format("%,.0f", (ct.getSoLuongDaNhan() > 0 ? ct.getSoLuongDaNhan() : ct.getSoLuongDat()) * ct.getDonGiaDuKien()), fontNormal));
            }
            document.add(table);
            document.close();
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(new File(path));
        } catch (Exception e) { e.printStackTrace(); }
    }
}