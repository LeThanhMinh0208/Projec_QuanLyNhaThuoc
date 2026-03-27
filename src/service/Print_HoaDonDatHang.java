package service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import entity.ChiTietDonDatHang;
import entity.DonDatHang;
import java.io.File;
import java.io.FileOutputStream;
import java.awt.Desktop;
import java.util.List;

public class Print_HoaDonDatHang {
    
    public static void inHoaDon(DonDatHang don, List<ChiTietDonDatHang> listCT) {
        Document document = new Document(PageSize.A4.rotate());
        String path = "";
        try {
            // 1. Tạo thư mục chứa file PDF nếu chưa có
            File dir = new File("hoa_don_da_in");
            if (!dir.exists()) dir.mkdirs();
            
            // 2. Đổi tên file xuất ra (Fix: getMaDonDatHang thay vì getMaDonNhap)
            path = "hoa_don_da_in/DonDatHang_" + don.getMaDonDatHang() + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();
            
            // 3. Cài đặt font tiếng Việt (Khắc phục lỗi ô vuông)
            BaseFont bf = BaseFont.createFont("C:\\Windows\\Fonts\\tahoma.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font fontTitle = new Font(bf, 18, Font.BOLD, BaseColor.BLUE);
            Font fontBold = new Font(bf, 10, Font.BOLD);
            Font fontNormal = new Font(bf, 10, Font.NORMAL);
            
            // 4. Logic tiêu đề thông minh: Đã nhập kho thì in là Phiếu Nhập, chưa thì in là Đơn Đặt
            String tieuDe = don.getTrangThaiNhap().equals("Đã Nhập Kho") ? "PHIẾU NHẬP KHO THUỐC" : "ĐƠN ĐẶT HÀNG THUỐC";
            Paragraph title = new Paragraph(tieuDe, fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            
            document.add(new Paragraph("Mã phiếu: " + don.getMaDonDatHang(), fontBold));
            document.add(new Paragraph("Nhà cung cấp: " + don.getNhaCungCap().getTenNhaCungCap(), fontNormal));
            document.add(new Paragraph("Trạng thái: " + don.getTrangThaiHang(), fontNormal));
            document.add(new Paragraph(" ")); // Dòng trống cách dòng

            // 5. Bảng chi tiết thuốc (8 cột)
            PdfPTable table = new PdfPTable(8); 
            table.setWidthPercentage(100);
            
            // Đặt kích thước độ rộng tương đối cho các cột cho đẹp
            table.setWidths(new float[]{3f, 1f, 1f, 1f, 1.5f, 1.5f, 1.5f, 2f});

            String[] headers = {"Tên thuốc", "Đơn vị", "SL Đặt", "SL Nhận", "Mã Lô", "Giá Nhập", "Hạn Dùng", "Thành tiền"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fontBold));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }

            // 6. Đổ dữ liệu vào bảng
            for (ChiTietDonDatHang ct : listCT) {
                table.addCell(new Phrase(ct.getThuoc().getTenThuoc(), fontNormal));
                
                PdfPCell cellDonVi = new PdfPCell(new Phrase(ct.getDonViQuyDoi().getTenDonVi(), fontNormal));
                cellDonVi.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cellDonVi);
                
                PdfPCell cellSLDat = new PdfPCell(new Phrase(String.valueOf(ct.getSoLuongDat()), fontNormal));
                cellSLDat.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cellSLDat);
                
                PdfPCell cellSLNhan = new PdfPCell(new Phrase(String.valueOf(ct.getSoLuongDaNhan()), fontNormal));
                cellSLNhan.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cellSLNhan);
                
                table.addCell(new Phrase(ct.getMaLo() != null ? ct.getMaLo() : "---", fontNormal));
                
                PdfPCell cellGia = new PdfPCell(new Phrase(String.format("%,.0f", ct.getDonGiaDuKien()), fontNormal));
                cellGia.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cellGia);
                
                table.addCell(new Phrase(ct.getHanSuDung() != null ? ct.getHanSuDung() : "---", fontNormal));
                
                // Logic tính tiền: Nếu chưa nhận (hoặc nhận 0) thì tính theo SL đặt, nếu đã nhận thì tính theo SL nhận
                double slTinhTien = ct.getSoLuongDaNhan() > 0 ? ct.getSoLuongDaNhan() : ct.getSoLuongDat();
                PdfPCell cellThanhTien = new PdfPCell(new Phrase(String.format("%,.0f", slTinhTien * ct.getDonGiaDuKien()), fontNormal));
                cellThanhTien.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cellThanhTien);
            }
            
            document.add(table);
            document.close();
            
            // 7. Mở file PDF ngay sau khi tạo xong
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(path));
            }
            
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }
}