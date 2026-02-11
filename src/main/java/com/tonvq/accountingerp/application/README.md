# Application Layer - AccountingERP

## Tổng Quan (Overview)

Application layer cung cấp các dịch vụ ứng dụng (Application Services) điều phối logic domain theo các use case cốt lõi của TT 99/2025/TT-BTC.

Cấu trúc:
```
application/
├── dto/                 # Data Transfer Objects (Request/Response)
├── mapper/              # Entity ↔ DTO converters
├── service/             # Application Services
├── exception/           # Custom exceptions
└── README.md           # This file
```

## DTOs (Data Transfer Objects)

### ChungTu (Voucher/Document)
- **ChungTuCreateRequest** - Tạo chứng từ
- **ChungTuResponse** - Trả về chứng từ
- **ChungTuApproveRequest** - Duyệt chứng từ
- **ChungTuPostRequest** - Ghi sổ
- **ChungTuLockRequest** - Khóa chứng từ
- **ButToanCreateRequest** - Tạo bút toán
- **ButToanResponse** - Trả về bút toán

### DonHang (Order)
- **DonHangCreateRequest** - Tạo đơn hàng
- **DonHangResponse** - Trả về đơn hàng
- **DonHangConfirmRequest** - Xác nhận đơn
- **DonHangShipRequest** - Giao hàng
- **DonHangPaymentRequest** - Ghi nhận thanh toán
- **DonHangChiTietCreateRequest** - Chi tiết dòng hàng
- **DonHangChiTietResponse** - Trả về chi tiết

### TonKho (Inventory)
- **TonKhoCreateRequest** - Tạo tồn kho
- **TonKhoResponse** - Trả về tồn kho
- **NhapHangRequest** - Nhập hàng
- **XuatHangRequest** - Xuất hàng
- **TinhGiaVonRequest** - Tính giá vốn

### HopDongDichVu (Service Contract)
- **HopDongDichVuCreateRequest** - Tạo hợp đồng
- **HopDongDichVuResponse** - Trả về hợp đồng
- **HopDongDichVuProgressRequest** - Cập nhật tiến độ

### Khác
- **DuPhongNoCalculateRequest** - Tính dự phòng nợ
- **DuPhongNoResponse** - Trả về dự phòng nợ
- **HoaDonCreateRequest/Response** - Hóa đơn
- **TaiKhoanCreateRequest/Response** - Tài khoản
- **BaoCaoTaiChinhRequest/Response** - Báo cáo tài chính
- **TyGiaCalculateRequest/Response** - Tỷ giá
- **AuditTrailResponse** - Audit trail log

## Application Services

### 1. ChungTuApplicationService
Điều phối logic chứng từ:
- `createChungTu()` - Tạo chứng từ (DRAFT)
- `approveChungTu()` - Duyệt → APPROVED
- `postChungTu()` - Ghi sổ → POSTED
- `lockChungTu()` - Khóa → LOCKED
- `cancelChungTu()` - Hủy → CANCELLED
- `getChungTuById()`, `getChungTuByTrangThai()`

Lifecycle: **DRAFT → APPROVED → POSTED → LOCKED** (hoặc CANCELLED)

### 2. DonHangApplicationService
Điều phối logic đơn hàng:
- `createDonHang()` - Tạo đơn (DRAFT)
- `confirmDonHang()` - Xác nhận → CONFIRMED
- `shipDonHang()` - Giao hàng → DELIVERED
- `recordPayment()` - Ghi nhận thanh toán → PAID
- `calculateVAT()` - Tính VAT
- Query methods

Lifecycle: **DRAFT → CONFIRMED → SHIPPING → DELIVERED → PAID**

### 3. TonKhoApplicationService
Quản lý tồn kho:
- `createTonKho()` - Tạo sản phẩm
- `importStock()` - Nhập hàng (tăng số lượng)
- `exportStock()` - Xuất hàng (giảm số lượng)
- `calculateCost()` - Tính giá vốn (FIFO/LIFO/TRUNG_BINH)
- Queries

### 4. HopDongDichVuApplicationService
Quản lý hợp đồng dịch vụ (per VAS 14/15):
- `createHopDong()` - Tạo hợp đồng (DRAFT)
- `activateHopDong()` - Kích hoạt → ACTIVE
- `updateProgress()` - Cập nhật tiến độ → IN_PROGRESS
- `recognizeRevenue()` - Công nhận doanh thu
- `completeHopDong()` - Hoàn thành → COMPLETED

Lifecycle: **DRAFT → ACTIVE → IN_PROGRESS → COMPLETED**

### 5. DuPhongNoApplicationService
Tính dự phòng nợ (per TT 48/2019 - Article 32):
- `calculateDuPhongByHistory()` - Phương pháp lịch sử %
- `calculateDuPhongByAging()` - Phương pháp tuổi nợ (1%/5%/10%/50%)
- `calculateDuPhongBySpecific()` - Phương pháp cụ thể %
- `adjustAllowance()` - Điều chỉnh dự phòng

### 6. BaoCaoTaiChinhApplicationService
Tạo báo cáo tài chính (B01-B09):
- `generateB01()` - Income Statement
- `generateB02()` - Balance Sheet
- `generateB03()` - Cash Flow Statement
- `generateB09()` - Inventory Statement

### 7. TyGiaApplicationService
Tính chênh lệch tỷ giá (per Article 31):
- `calculateExchangeRateDifference()` - Tính chênh lệch, ghi TK 413/515/635

### 8. AuditTrailApplicationService
Ghi log mọi thay đổi:
- `logCreation()`, `logUpdate()`, `logDeletion()` - Ghi các hành động
- `logAction()` - Ghi workflow (APPROVE, POST, LOCK)
- Query audit trail

## Mappers

Chuyển đổi giữa domain entity và DTO:
- **ChungTuMapper** - ChungTuCreateRequest ↔ ChungTu
- **DonHangMapper** - DonHangCreateRequest ↔ DonHang
- **TonKhoMapper** - TonKhoCreateRequest ↔ TonKho
- **HopDongDichVuMapper** - HopDongDichVuCreateRequest ↔ HopDongDichVu

## Exception Handling

Custom exceptions:
- **BusinessException** - Lỗi business logic (validation, state, etc.)
- **DataAccessException** - Lỗi database/persistence
- **ResourceNotFoundException** - Resource không tìm thấy

## Key Principles

1. **No Framework Dependencies** - Application layer chỉ gọi repository interfaces (định nghĩa trong domain)
2. **Transactional Boundaries** - `@Transactional` ở service level
3. **Null Safety** - `Objects.requireNonNull()` checks
4. **Logging** - SLF4j (@Slf4j) cho audit trail tracking
5. **Domain Logic Protection** - Validation qua domain entities/services
6. **Read-Only Queries** - `@Transactional(readOnly = true)` cho queries

## Lifecycle Examples

### ChungTu Workflow
```
1. createChungTu() → ChungTu(DRAFT)
2. addButToan() → Add detailed journal entries
3. approveChungTu() → ChungTu(APPROVED)
4. postChungTu() → ChungTu(POSTED) - cân bằng nợ=có
5. lockChungTu() → ChungTu(LOCKED) - không sửa được
```

### DonHang Workflow
```
1. createDonHang() → DonHang(DRAFT)
2. confirmDonHang() → DonHang(CONFIRMED)
3. shipDonHang() → DonHang(DELIVERED)
4. recordPayment() → DonHang(PAID) - khi thanh toán đủ
```

### HopDongDichVu Workflow (VAS 14/15)
```
1. createHopDong() → HopDongDichVu(DRAFT)
2. activateHopDong() → HopDongDichVu(ACTIVE)
3. updateProgress() → HopDongDichVu(IN_PROGRESS)
4. recognizeRevenue() → Công nhận doanh thu per VAS
5. completeHopDong() → HopDongDichVu(COMPLETED)
```

## Integration Points

### Domain Layer
Application layer phụ thuộc vào domain layer:
- Domain models (entities, value objects)
- Domain services (GiaVonService, DuPhongNoService, DoanhThuDichVuService)
- Repository interfaces

### Infrastructure Layer (Next)
Application layer sẽ được implement bởi infrastructure:
- JPA/Hibernate persistence
- Spring Data repository implementations
- Database migrations
- Security/authentication

## Testing

Example unit tests:
```java
@Test
public void testCreateChungTu() {
    ChungTuCreateRequest req = new ChungTuCreateRequest(...);
    ChungTuResponse resp = service.createChungTu(req);
    assertNotNull(resp.getId());
    assertEquals("DRAFT", resp.getTrangThai());
}

@Test
public void testApproveChungTu() {
    // Arrange
    ChungTu entity = repository.save(...);
    
    // Act
    ChungTuResponse resp = service.approveChungTu(...);
    
    // Assert
    assertEquals("APPROVED", resp.getTrangThai());
}
```

## Compliance Notes

Per TT 99/2025/TT-BTC:
- Chứng từ phải được ghi sổ đầy đủ (Phụ lục I)
- Tồn kho phải được tính giá vốn đúng (FIFO/LIFO - Phụ lục II)
- Doanh thu dịch vụ per VAS 14/15 (milestone/% completion)
- Dự phòng nợ per TT 48/2019 (Article 32)
- Tỷ giá per Article 31, TK 413/515/635
- Tất cả thay đổi phải audit trail (user, timestamp, old/new)

## Author
Ton VQ

## Updated
2026-02-11
