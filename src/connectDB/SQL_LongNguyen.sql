-- ========================================================
-- TẠO DATABASE: NHÀ THUỐC LONG NGUYÊN
-- ========================================================
CREATE DATABASE QuanLyNhaThuoc_LongNguyen;
GO

USE QuanLyNhaThuoc_LongNguyen;
GO

-- ========================================================
-- 1. TẠO CÁC BẢNG ĐỘC LẬP (KHÔNG CÓ KHÓA NGOẠI)
-- ========================================================

-- Bảng Nhà Cung Cấp
CREATE TABLE NhaCungCap (
    maNhaCungCap VARCHAR(20) PRIMARY KEY,
    tenNhaCungCap NVARCHAR(100) NOT NULL,
    sdt VARCHAR(15),
    diaChi NVARCHAR(255),
    congNo DECIMAL(18,2) DEFAULT 0
);

-- Bảng Khách Hàng
CREATE TABLE KhachHang (
    maKhachHang VARCHAR(20) PRIMARY KEY,
    hoTen NVARCHAR(100) NOT NULL,
    sdt VARCHAR(15),
    diaChi NVARCHAR(255),
    diemTichLuy INT DEFAULT 0
);

-- Bảng Nhân Viên
CREATE TABLE NhanVien (
    maNhanVien VARCHAR(20) PRIMARY KEY,
    tenDangNhap VARCHAR(50) UNIQUE NOT NULL,
    matKhau VARCHAR(255) NOT NULL,
    hoTen NVARCHAR(100) NOT NULL,
    chucVu NVARCHAR(50),
    caLamViec NVARCHAR(50),
    sdt VARCHAR(15)
);

-- Bảng Danh Mục Thuốc
CREATE TABLE DanhMucThuoc (
    maDanhMuc VARCHAR(20) PRIMARY KEY,
    tenDanhMuc NVARCHAR(100) NOT NULL,
    moTa NVARCHAR(255)
);

-- ========================================================
-- 2. TẠO BẢNG THUỐC & CHI TIẾT SẢN PHẨM (CÓ KHÓA NGOẠI)
-- ========================================================

-- Bảng Thuốc (Header)
CREATE TABLE Thuoc (
    maThuoc VARCHAR(20) PRIMARY KEY,
    maDanhMuc VARCHAR(20) NOT NULL,
    tenThuoc NVARCHAR(150) NOT NULL,
    hoatChat NVARCHAR(255),
    hamLuong NVARCHAR(50),
    hangSanXuat NVARCHAR(100),
    nuocSanXuat NVARCHAR(50),
    congDungTrieuChung NVARCHAR(MAX),
    donViCoBan NVARCHAR(20) NOT NULL, -- Vd: Viên, Gói...
    hinhAnh VARCHAR(255),
    canKeDon BIT DEFAULT 0, -- 0: Không cần, 1: Cần kê đơn
    trangThai VARCHAR(20) DEFAULT 'DANG_BAN', 
    
    FOREIGN KEY (maDanhMuc) REFERENCES DanhMucThuoc(maDanhMuc),
    -- Ràng buộc check cho Enum TrangThaiThuoc
    CONSTRAINT CHK_TrangThai CHECK (trangThai IN ('DANG_BAN', 'NGUNG_BAN', 'HET_HANG'))
);

-- Bảng Đơn Vị Quy Đổi (Detail Giá & Đóng gói)
CREATE TABLE DonViQuyDoi (
    maQuyDoi VARCHAR(20) PRIMARY KEY,
    maThuoc VARCHAR(20) NOT NULL,
    tenDonVi NVARCHAR(50) NOT NULL, -- Vd: Hộp, Vỉ...
    tyLeQuyDoi INT NOT NULL, -- Vd: 100, 10, 1...
    giaBan DECIMAL(18,2) NOT NULL,

    FOREIGN KEY (maThuoc) REFERENCES Thuoc(maThuoc) ON DELETE CASCADE
);

-- Bảng Lô Thuốc (Quản lý tồn kho FEFO)
CREATE TABLE LoThuoc (
    maLoThuoc VARCHAR(20) PRIMARY KEY,
    maThuoc VARCHAR(20) NOT NULL,
    ngaySanXuat DATE,
    hanSuDung DATE NOT NULL,
    soLuongTon INT DEFAULT 0, -- Lưu theo Đơn vị cơ bản (Viên)
    giaNhap DECIMAL(18,2),

    FOREIGN KEY (maThuoc) REFERENCES Thuoc(maThuoc) ON DELETE CASCADE
);

-- ========================================================
-- 3. TẠO BẢNG NGHIỆP VỤ: NHẬP HÀNG
-- ========================================================

-- Bảng Phiếu Nhập (Header)
CREATE TABLE PhieuNhap (
    maPhieuNhap VARCHAR(20) PRIMARY KEY,
    maNhaCungCap VARCHAR(20) NOT NULL,
    maNhanVien VARCHAR(20) NOT NULL,
    ngayNhap DATETIME DEFAULT GETDATE(),
    
    FOREIGN KEY (maNhaCungCap) REFERENCES NhaCungCap(maNhaCungCap),
    FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien)
);

-- Bảng Chi Tiết Phiếu Nhập (Detail)
CREATE TABLE ChiTietPhieuNhap (
    maPhieuNhap VARCHAR(20) NOT NULL,
    maQuyDoi VARCHAR(20) NOT NULL,
    maLoThuoc VARCHAR(20) NOT NULL,
    soLuong INT NOT NULL,
    donGiaNhap DECIMAL(18,2) NOT NULL,

    -- Khóa chính kép để 1 phiếu có thể nhập nhiều thuốc khác nhau
    PRIMARY KEY (maPhieuNhap, maQuyDoi, maLoThuoc),
    FOREIGN KEY (maPhieuNhap) REFERENCES PhieuNhap(maPhieuNhap) ON DELETE CASCADE,
    FOREIGN KEY (maQuyDoi) REFERENCES DonViQuyDoi(maQuyDoi),
    FOREIGN KEY (maLoThuoc) REFERENCES LoThuoc(maLoThuoc)
);

-- ========================================================
-- 4. TẠO BẢNG NGHIỆP VỤ: BÁN HÀNG
-- ========================================================

-- Bảng Hóa Đơn (Header)
CREATE TABLE HoaDon (
    maHoaDon VARCHAR(20) PRIMARY KEY,
    maKhachHang VARCHAR(20), -- Cho phép NULL nếu khách vãng lai không tích điểm
    maNhanVien VARCHAR(20) NOT NULL,
    ngayLap DATETIME DEFAULT GETDATE(),
    thueVAT DECIMAL(5,2) DEFAULT 0, -- Vd: 8.00 cho 8%
    hinhThucThanhToan VARCHAR(20) DEFAULT 'TIEN_MAT',
    ghiChu NVARCHAR(255),

    FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang),
    FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
    -- Ràng buộc check cho Enum HinhThucThanhToan
    CONSTRAINT CHK_ThanhToan CHECK (hinhThucThanhToan IN ('TIEN_MAT', 'CHUYEN_KHOAN', 'THE'))
);

-- Bảng Đơn Thuốc (Nếu mua theo đơn Bác sĩ - Quan hệ 1-1 với Hóa Đơn)
CREATE TABLE DonThuoc (
    maDonThuoc VARCHAR(20) PRIMARY KEY,
    maHoaDon VARCHAR(20) UNIQUE NOT NULL, -- Đảm bảo 1 HĐ chỉ có max 1 Đơn thuốc
    tenBacSi NVARCHAR(100),
    chanDoan NVARCHAR(255),
    hinhAnhDon VARCHAR(255),
    thongTinBenhNhan NVARCHAR(255),

    FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon) ON DELETE CASCADE
);

-- Bảng Chi Tiết Hóa Đơn (Detail)
CREATE TABLE ChiTietHoaDon (
    maHoaDon VARCHAR(20) NOT NULL,
    maQuyDoi VARCHAR(20) NOT NULL,
    maLoThuoc VARCHAR(20) NOT NULL,
    soLuong INT NOT NULL,
    donGia DECIMAL(18,2) NOT NULL, -- Chốt giá tại thời điểm bán

    PRIMARY KEY (maHoaDon, maQuyDoi, maLoThuoc),
    FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon) ON DELETE CASCADE,
    FOREIGN KEY (maQuyDoi) REFERENCES DonViQuyDoi(maQuyDoi),
    FOREIGN KEY (maLoThuoc) REFERENCES LoThuoc(maLoThuoc)
);

-- ========================================================
-- 5. TẠO BẢNG NGHIỆP VỤ: ĐỔI TRẢ
-- ========================================================

-- Bảng Phiếu Đổi Trả (Header)
CREATE TABLE PhieuDoiTra (
    maPhieuDoiTra VARCHAR(20) PRIMARY KEY,
    maHoaDon VARCHAR(20) NOT NULL, -- Đổi trả dựa trên bill nào
    maNhanVien VARCHAR(20) NOT NULL,
    ngayDoiTra DATETIME DEFAULT GETDATE(),
    lyDo NVARCHAR(255),
    hinhThucXuLy VARCHAR(20) NOT NULL,
    phiPhat DECIMAL(18,2) DEFAULT 0,

    FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon),
    FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
    -- Ràng buộc check cho Enum HinhThucDoiTra
    CONSTRAINT CHK_HinhThucXuLy CHECK (hinhThucXuLy IN ('HOAN_TIEN', 'DOI_SAN_PHAM'))
);

-- Bảng Chi Tiết Đổi Trả (Detail)
CREATE TABLE ChiTietDoiTra (
    maPhieuDoiTra VARCHAR(20) NOT NULL,
    maQuyDoi VARCHAR(20) NOT NULL,
    maLoThuoc VARCHAR(20) NOT NULL, -- Biết trả lại vào Lô nào
    soLuong INT NOT NULL,
    tinhTrang NVARCHAR(100), -- Vd: Còn nguyên seal, Rách hộp...

    PRIMARY KEY (maPhieuDoiTra, maQuyDoi, maLoThuoc),
    FOREIGN KEY (maPhieuDoiTra) REFERENCES PhieuDoiTra(maPhieuDoiTra) ON DELETE CASCADE,
    FOREIGN KEY (maQuyDoi) REFERENCES DonViQuyDoi(maQuyDoi),
    FOREIGN KEY (maLoThuoc) REFERENCES LoThuoc(maLoThuoc)
);
GO
PRINT N'Đã tạo xong Database QuanLyNhaThuoc_LongNguyen xịn xò con bò cho sếp!';