# Domain Layer - Hệ Thống ERP Kế Toán Việt Nam

## Tổng Quan

Domain layer là lõi của hệ thống, chứa tất cả logic nghiệp vụ theo **Domain-Driven Design (DDD)** và **TT 99/2025/TT-BTC**.

**Nguyên tắc:**
- ✅ Pure Java POJOs - không phụ thuộc Spring/JPA
- ✅ Đóng gói logic nghiệp vụ
- ✅ Bảo vệ invariant (bất biến) của aggregate
- ✅ Dễ test, dễ hiểu, dễ bảo trì

---

## Cấu Trúc Thư Mục

```
domain/
├── model/
│   ├── entity/                          # Entities (Pure Java)
│   │   ├── ChungTu.java                 # Chứng từ kế toán
│   │   ├── ButToan.java                 # Bút toán (chi tiết)
│   │   ├── TonKho.java                  # Tồn kho
│   │   ├── DonHang.java                 # Đơn hàng bán
│   │   ├── DonHangChiTiet.java          # Chi tiết đơn hàng
│   │   ├── HoaDon.java                  # Hóa đơn
│   │   ├── HopDongDichVu.java           # Hợp đồng dịch vụ
│   │   ├── KhachHang.java               # Khách hàng
│   │   ├── NhaCungCap.java              # Nhà cung cấp
│   │   ├── TaiKhoan.java                # Tài khoản kế toán (Chart of Accounts)
│   │   └── SanPham.java                 # Sản phẩm (nếu cần)
│   │
│   ├── valueobject/                     # Value Objects (Immutable)
│   │   ├── TienTe.java                  # Tiền tệ (VND, USD, EUR...)
│   │   ├── Tien.java                    # Tiền (với phép toán, so sánh)
│   │   ├── SoLuong.java                 # Số lượng (với đơn vị)
│   │   ├── GiaVon.java                  # Giá vốn (FIFO, LIFO, Trung bình)
│   │   ├── TrangThaiChungTu.java        # Enum: Trạng thái chứng từ
│   │   ├── TrangThaiDonHang.java        # Enum: Trạng thái đơn hàng
│   │   └── PhuongThucTinhGia.java       # Enum: Phương thức tính giá
│   │
│   └── aggregate/                       # Aggregate Roots
│       ├── ChungTuAggregate.java        # Root: Chứng từ + Bút toán
│       ├── DonHangAggregate.java        # Root: Đơn hàng + Chi tiết
│       └── HopDongAggregate.java        # Root: Hợp đồng + Milestone
│
├── repository/                          # Repository Interfaces (No Spring!)
│   ├── ChungTuRepository.java
│   ├── DonHangRepository.java
│   ├── TonKhoRepository.java
│   ├── TaiKhoanRepository.java
│   ├── KhachHangRepository.java
│   └── NhaCungCapRepository.java
│
├── service/                             # Domain Services (Pure Logic)
│   ├── GiaVonService.java               # FIFO, LIFO, Trung bình
│   ├── DoanhThuDichVuService.java       # Công nhân doanh thu dịch vụ
│   ├── DuPhongNoService.java            # Dự phòng nợ phải thu
│   └── TyGiaService.java                # Tỷ giá ngoại tệ
│
└── event/                               # Domain Events
    ├── DomainEvent.java                 # Base class
    ├── ChungTuCreatedEvent.java
    ├── ChungTuLockedEvent.java
    └── KhoUpdatedEvent.java
```

---

## 1. Value Objects (Giá Trị)

Value Objects là **bất biến (immutable)** - không thay đổi sau khi tạo.

### TienTe (Tiền Tệ)
```java
TienTe vnd = TienTe.vnd();      // Đồng Việt Nam
TienTe usd = TienTe.usd();      // Đô la Mỹ
boolean isVND = vnd.isVND();
```

### Tien (Tiền)
```java
Tien tien1 = new Tien(100000, TienTe.vnd());
Tien tien2 = Tien.ofVND(50000);

// Phép toán (trả về object mới)
Tien tong = tien1.add(tien2);
Tien hieu = tien1.subtract(tien2);
Tien nhan = tien1.multiply(2.5);

// So sánh
if (tien1.isPositive()) { ... }
if (tien1.isZero()) { ... }
if (tien1.compareTo(tien2) > 0) { ... }
```

### SoLuong (Số Lượng)
```java
SoLuong sl1 = SoLuong.ofCai(10);        // 10 cái
SoLuong sl2 = SoLuong.ofKg(5.5);        // 5.5 kg

SoLuong tong = sl1.add(sl2);            // Chỉ cộng được nếu cùng đơn vị!
```

### GiaVon (Giá Vốn)
```java
// FIFO: Hàng nhập trước xuất trước
GiaVon fifo = GiaVon.fifoVND(BigDecimal.valueOf(10000));

// LIFO: Hàng nhập sau xuất trước
GiaVon lifo = new GiaVon(gia, GiaVon.LIFO);

// Trung bình gia quyền
GiaVon tb = GiaVon.trungBinh(gia);
```

### TrangThaiChungTu & TrangThaiDonHang (Enums)
```java
TrangThaiChungTu status = TrangThaiChungTu.DRAFT;
if (status.canEdit()) { ... }      // Chỉ DRAFT được sửa
if (status.canPost()) { ... }      // Chỉ DRAFT được ghi sổ
if (status.isLocked()) { ... }     // Đã khóa?
```

---

## 2. Entities (Thực Thể)

Entities có **identity** (định danh duy nhất).

### ChungTu (Chứng Từ)
**Aggregate Root** - quản lý bút toán.

```java
// Tạo mới
ChungTu ct = new ChungTu(
    "CT001",                          // mã
    "HĐ",                             // loại
    LocalDateTime.now(),              // ngày
    "1010",                           // TK nợ (Tiền)
    "4011",                           // TK có (Doanh thu)
    Tien.ofVND(1000000),             // số tiền
    "Bán hàng"                        // nội dung
);

// Business methods
ct.ghiSo("user1");                    // DRAFT → POSTED
ct.khoa("user2");                     // POSTED → LOCKED (không sửa lại được)
ct.huy("user3");                      // → CANCELLED

// Kiểm tra trạng thái
ct.isValid();                         // Không hủy, không draft?
ct.isDaGhiSo();                       // Đã ghi sổ?
ct.isDaKhoa();                        // Đã khóa?
```

### ButToan (Bút Toán)
Chi tiết của chứng từ: nợ TK 1010, có TK 4011.

```java
ButToan bt = new ButToan("1010", "4011", Tien.ofVND(1000000), "Bán hàng");
bt.isNo();                            // true
```

### TonKho (Tồn Kho)
Quản lý số lượng & giá vốn hàng tồn kho.

```java
TonKho kho = new TonKho(
    "SP001",
    "Áo phông",
    SoLuong.ofCai(100),               // Số lượng đầu kỳ
    GiaVon.fifoVND(BigDecimal.valueOf(50000))
);

// Nhập hàng
kho.nhapHang(SoLuong.ofCai(50), 
             GiaVon.fifoVND(BigDecimal.valueOf(55000)), 
             "user1");

// Xuất (bán) hàng
kho.xuatHang(SoLuong.ofCai(80),
             GiaVon.fifoVND(BigDecimal.valueOf(52000)),
             "user2");

// Tính tổng giá trị tồn kho cuối kỳ
Tien tongGiaTri = kho.getTongGiaTriTonKho();
```

### DonHang (Đơn Hàng)
Quản lý vòng đời đơn hàng: DRAFT → CONFIRMED → SHIPPING → DELIVERED → PAID.

```java
DonHang dh = new DonHang("DH001", "KH001", "Hà Nội");

// Thêm chi tiết
DonHangChiTiet chiTiet = new DonHangChiTiet(
    "SP001", "Áo phông",
    SoLuong.ofCai(10),
    Tien.ofVND(100000)
);
dh.addChiTiet(chiTiet);

// Trạng thái
dh.xacNhan("user1");                  // DRAFT → CONFIRMED
dh.batchGiao("user2");                // CONFIRMED → SHIPPING
dh.thucHienGiao("user3");             // SHIPPING → DELIVERED

// Thanh toán
dh.ghiNhanThanhToan(Tien.ofVND(500000), "user4");
// Nếu thanh toán hết → PAID, isCompleted = true
```

### HoaDon (Hóa Đơn)
Hóa đơn bán hàng (có thể từ DonHang hoặc độc lập).

```java
HoaDon hd = new HoaDon("HD001", "KH001", "Công ty ABC", "Hà Nội");
hd.phatHanh("user1");                 // DRAFT → ISSUED
hd.ghiNhanThanhToan(Tien.ofVND(500000), "user2");
```

### HopDongDichVu (Hợp Đồng Dịch Vụ)
Hợp đồng dài hạn, công nhân doanh thu dần theo milestone.

```java
HopDongDichVu hd = new HopDongDichVu(
    "HD001",
    "KH001",
    "Công ty ABC",
    LocalDateTime.now(),
    LocalDateTime.now().plusMonths(12),
    Tien.ofVND(120000000)
);

hd.kichHoat("user1");

// Cập nhật tiến độ
hd.updateTienDo(1, 25.0, "user2");    // Milestone 1: 25% hoàn thành

// Ghi nhận doanh thu công nhân
hd.ghiNhanDoanhThuCongNhan(Tien.ofVND(30000000), "user3");

hd.hoanThanh("user4");                // Hoàn thành hợp đồng
```

### KhachHang & NhaCungCap
Đối tác của công ty.

```java
KhachHang kh = new KhachHang("KH001", "Công ty ABC", "Tổ chức", "Hà Nội");
kh.updateTienNo(500000);              // Khách hàng nợ 500k
kh.thanhToanNo(200000);               // Thanh toán 200k

NhaCungCap ncc = new NhaCungCap("NCC001", "Nhà máy XYZ", "Công ty", "TP HCM");
ncc.updateTienPhaiTra(1000000);
ncc.thanhToanNo(300000);
```

### TaiKhoan (Chart of Accounts)
Tài khoản kế toán theo TT 99/2025.

```java
TaiKhoan tk = new TaiKhoan("1010", "Tiền mặt", "Cash", "T.Sản", "Nợ");
tk.congNo(100000);                    // Cộng nợ
tk.congCo(50000);                     // Cộng có
double soDuRong = tk.getSoDuRong();   // Nợ - Có
```

---

## 3. Aggregate Roots (Gốc Aggregate)

Aggregates bảo vệ **invariant** (bất biến) của tập hợp entities.

### ChungTuAggregate
```java
ChungTu ct = new ChungTu(...);
ChungTuAggregate agg = new ChungTuAggregate(ct);

// Thêm bút toán (bảo vệ invariant: phải DRAFT)
ButToan bt = new ButToan("1010", "4011", Tien.ofVND(1000000), "");
agg.addButToan(bt);

// Ghi sổ (yêu cầu: cân bằng + ≥1 bút toán)
if (agg.isBalanced() && agg.getButToanCount() > 0) {
    agg.ghiSo("user1");
}

// Khóa (yêu cầu: đã ghi sổ)
agg.khoa("user2");

// Tính VAT
Tien vat = agg.calculateVAT(10.0);    // 10% VAT
```

### DonHangAggregate
```java
DonHang dh = new DonHang(...);
DonHangAggregate agg = new DonHangAggregate(dh);

// Thêm chi tiết (bảo vệ: chỉ DRAFT được thêm)
DonHangChiTiet ct = new DonHangChiTiet(...);
agg.addChiTiet(ct);

// Xác nhận (yêu cầu: ≥1 chi tiết)
if (agg.canConfirm()) {
    agg.xacNhan("user1");
}

// Giao
agg.giao("user2");
agg.thucHienGiao("user3");

// Thanh toán
agg.ghiNhanThanhToan(Tien.ofVND(500000), "user4");

// Tổng tiền
Tien tongCong = agg.getTongCong();
Tien tienConNo = agg.getTienConNo();
```

---

## 4. Domain Services (Dịch Vụ Miền)

Các phép tính phức tạp không thuộc entity nào.

### GiaVonService
Tính giá vốn hàng bán theo FIFO, LIFO, Trung bình.

```java
// FIFO: Hàng cũ xuất trước
GiaVon fifo = GiaVonService.calculateFIFO(
    soLuongDau, giaVonDau,
    List.of(  // Nhập trong kỳ
        new AbstractMap.SimpleEntry<>(sl1, gv1),
        new AbstractMap.SimpleEntry<>(sl2, gv2)
    ),
    soLuongXuat
);

// LIFO: Hàng mới xuất trước
GiaVon lifo = GiaVonService.calculateLIFO(
    soLuongDau, giaVonDau, luotNhap, soLuongXuat
);

// Trung bình gia quyền
GiaVon tb = GiaVonService.calculateTrungBinh(
    soLuongDau, giaVonDau, luotNhap
);
```

### DoanhThuDichVuService
Công nhân doanh thu dịch vụ.

```java
// Theo milestone
Tien doanhthu1 = DoanhThuDichVuService.calculateDoanhThuMilestone(
    tongGiaTriHopDong, 25.0  // 25% hoàn thành
);

// Theo % hoàn thành (Cost-to-Cost)
Tien doanhthu2 = DoanhThuDichVuService.calculateDoanhThuCongNhanDan(
    tongGiaTriHopDong,
    chiPhiThucTe,
    chiPhiDuKien
);

// Hoàn thành toàn bộ
Tien doanhThuHT = DoanhThuDichVuService.calculateDoanhThuHoanThanh(
    tongGiaTriHopDong,
    isCompleted
);

// Kiểm tra lỗ
boolean isLoss = DoanhThuDichVuService.isLossContract(
    tongGiaTriHopDong, chiPhiDuKien
);
```

### DuPhongNoService
Tính dự phòng nợ phải thu (Điều 32, TK 229).

```java
// Phương pháp lịch sử: % cố định
Tien duPhong1 = DuPhongNoService.calculateDuPhongLichSu(
    tongNoPhaiThu, 2.0  // 2%
);

// Phương pháp tuổi nợ
Tien duPhong2 = DuPhongNoService.calculateDuPhongTuoiNo(
    noTruoc3Thang,      // 1%
    no3Den6Thang,       // 5%
    no6Den12Thang,      // 10%
    noTren12Thang       // 50%
);

// Dự phòng cụ thể từng khách hàng
Tien duPhong3 = DuPhongNoService.calculateDuPhongCuThe(
    tongNoKhachHang, 10.0  // 10% rủi ro
);

// Hạn chế (không vượt quá nợ)
Tien duPhongHanChe = DuPhongNoService.limitDuPhong(duPhongTinh, tongNo);
```

---

## 5. Repository Interfaces (Giao Diện Lưu Trữ)

Pure **interfaces** - không có annotation Spring/JPA trong domain.

Implementation sẽ ở tầng **Infrastructure** (dùng Spring Data JPA).

```java
// Chỉ interface trong domain
public interface ChungTuRepository {
    ChungTu save(ChungTu chungTu);
    Optional<ChungTu> findById(String id);
    Optional<ChungTu> findByMaChungTu(String maChungTu);
    List<ChungTu> findByTrangThai(TrangThaiChungTu trangThai);
    List<ChungTu> findByNgayChungTuBetween(LocalDateTime start, LocalDateTime end);
    void delete(ChungTu chungTu);
    String getNextMaChungTu(String loaiChungTu);  // Sinh mã tự động
}
```

---

## 6. Domain Events (Sự Kiện Miền)

Phát hành khi có sự kiện quan trọng.

```java
// Khi tạo chứng từ
ChungTuCreatedEvent event1 = new ChungTuCreatedEvent(
    chungTuId, maChungTu, loai, ngay, createdBy
);

// Khi khóa chứng từ
ChungTuLockedEvent event2 = new ChungTuLockedEvent(
    chungTuId, maChungTu, previousStatus, lockedBy
);

// Khi cập nhật kho
KhoUpdatedEvent event3 = new KhoUpdatedEvent(
    khoId, maSanPham, "NHAP", soLuong, gia, updatedBy
);

// Event listeners (ở Application layer) sẽ xử lý:
// - Gửi notification
// - Cập nhật read model (CQRS)
// - Ghi log audit
```

---

## 7. Business Rules & Validation (Luật Kinh Doanh)

### TT 99/2025/TT-BTC Compliance

| Rule | Implementation |
|------|-----------------|
| Chứng từ phải có mã duy nhất | Mã + Loại sinh tự động |
| Nợ = Có (cân bằng) | `ChungTu.isBalanced()`, check trong aggregate |
| Không sửa sau khóa | `TrangThaiChungTu.canEdit()` check |
| Ký số (tuỳ chọn) | `ChungTu.kyDienTu`, `chungChiKyDienTu` |
| Audit trail | `createdBy`, `lastModifiedBy`, timestamp |
| Tồn kho ≥ 0 | `SoLuong` validate >= 0 |
| Giá vốn theo FIFO/LIFO | `GiaVonService` tính toán |
| Dự phòng nợ (TK 229) | `DuPhongNoService.calculateDuPhong*` |
| Doanh thu dịch vụ | `DoanhThuDichVuService` công nhận theo milestone |

### Exception Handling

```java
// Tất cả violation đều throw exception
throw new IllegalArgumentException("Mã không được rỗng");
throw new IllegalStateException("Không thể sửa ở trạng thái này");
throw new BusinessException("Vi phạm luật: ...");
```

---

## 8. Testing

Domain layer dễ test vì pure Java.

```java
@Test
public void testChungTuGhiSo() {
    ChungTu ct = new ChungTu(...);
    ct.ghiSo("user1");
    
    assertEquals(TrangThaiChungTu.POSTED, ct.getTrangThai());
    assertNotNull(ct.getNgayGhiSo());
}

@Test
public void testTienAdd() {
    Tien t1 = Tien.ofVND(100000);
    Tien t2 = Tien.ofVND(50000);
    Tien sum = t1.add(t2);
    
    assertEquals(150000, sum.getAmount());
}

@Test
public void testGiaVonFIFO() {
    GiaVon fifo = GiaVonService.calculateFIFO(...);
    assertNotNull(fifo);
    assertTrue(fifo.isFIFO());
}
```

---

## 9. Package Organization

```
com.tonvq.accountingerp.domain
├── model
│   ├── entity
│   ├── valueobject
│   └── aggregate
├── repository
├── service
├── event
└── exception          # BusinessException, InvalidAggregateException, etc.
```

---

## 10. Tham Chiếu

- **TT 99/2025/TT-BTC**: Tiêu chuẩn kế toán Việt Nam (hiệu lực 01/01/2026)
- **DDD Pattern**: Domain-Driven Design
- **VAS 14/15**: Service Revenue Recognition (Doanh thu dịch vụ)
- **FIFO/LIFO**: Phương pháp định giá hàng tồn kho

---

**Ngày cập nhật:** 2024
**Phiên bản:** 1.0
**Tác giả:** Ton VQ
