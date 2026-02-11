# 📁 Application Layer - Directory Structure

## Complete File Tree

```
src/main/java/com/tonvq/accountingerp/application/
│
├── 📂 dto/                          (30 DTO classes)
│   ├── AuditTrailResponse.java
│   ├── BaoCaoTaiChinhRequest.java
│   ├── BaoCaoTaiChinhResponse.java
│   ├── ButToanCreateRequest.java
│   ├── ButToanResponse.java
│   ├── ChungTuApproveRequest.java
│   ├── ChungTuCreateRequest.java
│   ├── ChungTuLockRequest.java
│   ├── ChungTuPostRequest.java
│   ├── ChungTuResponse.java
│   ├── DonHangChiTietCreateRequest.java
│   ├── DonHangChiTietResponse.java
│   ├── DonHangConfirmRequest.java
│   ├── DonHangCreateRequest.java
│   ├── DonHangPaymentRequest.java
│   ├── DonHangResponse.java
│   ├── DonHangShipRequest.java
│   ├── DuPhongNoCalculateRequest.java
│   ├── DuPhongNoResponse.java
│   ├── HoaDonCreateRequest.java
│   ├── HoaDonPublishRequest.java
│   ├── HoaDonResponse.java
│   ├── HopDongDichVuCreateRequest.java
│   ├── HopDongDichVuProgressRequest.java
│   ├── HopDongDichVuResponse.java
│   ├── NhapHangRequest.java
│   ├── TaiKhoanCreateRequest.java
│   ├── TaiKhoanResponse.java
│   ├── TinhGiaVonRequest.java
│   ├── TonKhoCreateRequest.java
│   ├── TonKhoResponse.java
│   ├── TyGiaCalculateRequest.java
│   ├── TyGiaResponse.java
│   └── XuatHangRequest.java
│
├── 📂 exception/                    (3 exception classes)
│   ├── BusinessException.java
│   ├── DataAccessException.java
│   └── ResourceNotFoundException.java
│
├── 📂 mapper/                       (4 mapper classes)
│   ├── ChungTuMapper.java
│   ├── DonHangMapper.java
│   ├── HopDongDichVuMapper.java
│   └── TonKhoMapper.java
│
├── 📂 service/                      (8 application services)
│   ├── AuditTrailApplicationService.java
│   ├── BaoCaoTaiChinhApplicationService.java
│   ├── ChungTuApplicationService.java
│   ├── DonHangApplicationService.java
│   ├── DuPhongNoApplicationService.java
│   ├── HopDongDichVuApplicationService.java
│   ├── TonKhoApplicationService.java
│   └── TyGiaApplicationService.java
│
├── 📄 README.md                     (Comprehensive guide)
│
└── [Other files from domain/infrastructure/...]
```

## DTOs by Domain

### ChungTu (Chứng Từ / Document) - 7 DTOs
```
ChungTuCreateRequest     → Create new document (DRAFT)
ChungTuApproveRequest    → Approve workflow (DRAFT → APPROVED)
ChungTuPostRequest       → Post/Ghi sổ (APPROVED → POSTED)
ChungTuLockRequest       → Lock (POSTED → LOCKED)
ChungTuResponse          → Full document response
ButToanCreateRequest     → Journal entry detail
ButToanResponse          → Journal entry response
```

### DonHang (Đơn Hàng / Order) - 7 DTOs
```
DonHangCreateRequest           → Create new order (DRAFT)
DonHangConfirmRequest          → Confirm (DRAFT → CONFIRMED)
DonHangShipRequest             → Ship (CONFIRMED → DELIVERED)
DonHangPaymentRequest          → Record payment → PAID
DonHangResponse                → Order details
DonHangChiTietCreateRequest    → Line item
DonHangChiTietResponse         → Line item details
```

### TonKho (Tồn Kho / Inventory) - 5 DTOs
```
TonKhoCreateRequest      → Create inventory
TonKhoResponse           → Inventory details
NhapHangRequest          → Import stock
XuatHangRequest          → Export stock
TinhGiaVonRequest        → Calculate cost (FIFO/LIFO/AVG)
```

### HopDongDichVu (Hợp Đồng Dịch Vụ / Service Contract) - 3 DTOs
```
HopDongDichVuCreateRequest       → Create contract (DRAFT)
HopDongDichVuProgressRequest     → Update progress → IN_PROGRESS
HopDongDichVuResponse            → Contract details
```

### HoaDon (Hóa Đơn / Invoice) - 3 DTOs
```
HoaDonCreateRequest      → Create invoice
HoaDonPublishRequest     → Publish (DRAFT → ISSUED)
HoaDonResponse           → Invoice details
```

### DuPhongNo (Dự Phòng Nợ / Allowance) - 2 DTOs
```
DuPhongNoCalculateRequest    → Calculate allowance
DuPhongNoResponse            → Allowance details
```

### Tài Chính (Financial) - 4 DTOs
```
BaoCaoTaiChinhRequest        → Report request (B01, B02, B03, B09)
BaoCaoTaiChinhResponse       → Report data
TaiKhoanCreateRequest        → Create account
TaiKhoanResponse             → Account details
```

### Khác (Other) - 2 DTOs
```
TyGiaCalculateRequest        → FX rate calculation
TyGiaResponse                → FX difference
AuditTrailResponse           → Audit log entry
```

## Services by Responsibility

### 1. ChungTuApplicationService (280+ lines)
```
Lifecycle Management:
  ├─ createChungTu()        DRAFT
  ├─ approveChungTu()       → APPROVED
  ├─ postChungTu()          → POSTED
  ├─ lockChungTu()          → LOCKED
  └─ cancelChungTu()        → CANCELLED

Queries:
  ├─ getChungTuById()
  ├─ getChungTuByMa()
  └─ getChungTuByTrangThai()
```

### 2. DonHangApplicationService (320+ lines)
```
Lifecycle Management:
  ├─ createDonHang()       DRAFT
  ├─ confirmDonHang()      → CONFIRMED
  ├─ shipDonHang()         → DELIVERED
  └─ recordPayment()       → PAID

Financial:
  └─ calculateVAT()

Queries:
  ├─ getDonHangById()
  ├─ getDonHangByTrangThai()
  └─ getUnpaidDonHang()
```

### 3. TonKhoApplicationService (280+ lines)
```
Inventory Operations:
  ├─ createTonKho()
  ├─ importStock()
  ├─ exportStock()
  └─ calculateCost()  [FIFO, LIFO, TRUNG_BINH]

Queries:
  ├─ getTonKhoByMaSanPham()
  └─ getOutOfStockProducts()
```

### 4. HopDongDichVuApplicationService (250+ lines)
```
Lifecycle Management (VAS 14/15):
  ├─ createHopDong()       DRAFT
  ├─ activateHopDong()     → ACTIVE
  ├─ updateProgress()      → IN_PROGRESS
  ├─ recognizeRevenue()    [per VAS 14/15]
  └─ completeHopDong()     → COMPLETED

Queries:
  ├─ getHopDongById()
  └─ getHopDongByTrangThai()
```

### 5. DuPhongNoApplicationService (240+ lines)
```
Calculation Methods (TT 48/2019 - Article 32):
  ├─ calculateDuPhongByHistory()    [By historical %]
  ├─ calculateDuPhongByAging()      [By age: 1%, 5%, 10%, 50%]
  └─ calculateDuPhongBySpecific()   [Specific %]

Adjustment:
  └─ adjustAllowance()
```

### 6. BaoCaoTaiChinhApplicationService (200+ lines)
```
Reports (TT 99 Phụ lục IV):
  ├─ generateB01()    Income Statement
  ├─ generateB02()    Balance Sheet
  ├─ generateB03()    Cash Flow Statement
  └─ generateB09()    Inventory Statement
```

### 7. TyGiaApplicationService (140+ lines)
```
Exchange Rate (TT 99 Article 31):
  └─ calculateExchangeRateDifference()  [TK 413/515/635]
```

### 8. AuditTrailApplicationService (120+ lines)
```
Logging:
  ├─ logCreation()
  ├─ logUpdate()
  ├─ logDeletion()
  └─ logAction()

Queries:
  ├─ getAuditTrail()
  ├─ getAuditTrailByDateRange()
  └─ getAuditTrailByUser()
```

## Mappers

### ChungTuMapper
```
toEntity()       ChungTuCreateRequest → ChungTu
toResponse()     ChungTu → ChungTuResponse
toResponseList() List<ChungTu> → List<ChungTuResponse>
```

### DonHangMapper
```
toEntity()       DonHangCreateRequest → DonHang
toResponse()     DonHang → DonHangResponse
toResponseList() List<DonHang> → List<DonHangResponse>
```

### TonKhoMapper
```
toEntity()       TonKhoCreateRequest → TonKho
toResponse()     TonKho → TonKhoResponse
toResponseList() List<TonKho> → List<TonKhoResponse>
```

### HopDongDichVuMapper
```
toEntity()       HopDongDichVuCreateRequest → HopDongDichVu
toResponse()     HopDongDichVu → HopDongDichVuResponse
toResponseList() List<HopDongDichVu> → List<HopDongDichVuResponse>
```

## Exception Hierarchy

```
RuntimeException
├── BusinessException
│   ├── Invalid state transitions
│   ├── Business rule violations
│   ├── Constraint violations
│   └── Data validation errors
│
├── DataAccessException
│   ├── Database operation failures
│   ├── Connection issues
│   └── Persistence errors
│
└── ResourceNotFoundException
    ├── Entity not found
    ├── Record deleted
    └── Invalid reference
```

## Code Statistics

| Category | Count | Lines |
|----------|-------|-------|
| DTOs | 30 | ~900 |
| Services | 8 | ~1,830 |
| Mappers | 4 | ~350 |
| Exceptions | 3 | ~80 |
| Documentation | 1 | ~500 |
| **Total** | **46** | **~3,660** |

## Integration Points

```
Application Layer
    ↓
    ├─ Uses Domain Repositories
    │   ├─ ChungTuRepository
    │   ├─ DonHangRepository
    │   ├─ TonKhoRepository
    │   ├─ HopDongDichVuRepository
    │   ├─ DuPhongNoRepository
    │   ├─ TaiKhoanRepository
    │   ├─ KhachHangRepository
    │   └─ BaoCaoTaiChinhRepository
    │
    └─ Uses Domain Services
        ├─ GiaVonService
        ├─ DoanhThuDichVuService
        └─ DuPhongNoService
        
    ↑
    ├─ Consumed by Controllers (Web Layer)
    └─ Consumed by CLI/Batch Jobs
```

## Testing Coverage

```
Application Layer Tests
├── Unit Tests (Service Logic)
│   ├── Create operations
│   ├── State transitions
│   ├── Validation logic
│   ├── Error handling
│   └── Null safety
│
├── Integration Tests (with Mocked Repos)
│   ├── Complete workflows
│   ├── Business rule enforcement
│   ├── Payment reconciliation
│   └── Inventory calculations
│
└── DTO Tests
    ├── Serialization/Deserialization
    ├── Builder patterns
    └── Null checks
```

## Performance Considerations

- Read-only queries use `@Transactional(readOnly = true)`
- Batch operations in service methods
- Lazy loading handled in infrastructure layer
- DTO conversion optimized with mappers

## Security Considerations

- Null checks on all inputs
- Validation before business operations
- User tracking (createdBy, updatedBy, etc.)
- Audit trail for all modifications
- Exception messages don't leak sensitive data

---

**Last Updated:** 2026-02-11
**Framework:** Spring Boot 3.3.6 + Java 21
**Architecture:** Domain-Driven Design (DDD)
**Compliance:** TT 99/2025/TT-BTC
