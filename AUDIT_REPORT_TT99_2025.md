# 📋 BÁO CÁO KIỂM TOÁN TT 99/2025/TT-BTC

**Dự Án:** AccountingERP System  
**Phiên Bản:** 1.0.0  
**Ngày Audit:** 2025-02-11  
**Cán Bộ Audit:** Senior Tax Inspector (20+ năm kinh nghiệm)  
**Mục Đích:** Đánh giá mức độ tuân thủ TT 99/2025/TT-BTC (effective 01/01/2026)

---

## I. ĐIỂM TUÂN THỦ TỔNG THỂ

**COMPLIANCE SCORE: 34/100 (Chưa Đạt)**

**Tóm Tắt:**
- ✅ Đạt: 3/10 (30%)
- 🟡 Đạt Một Phần: 2/10 (20%)
- ❌ Chưa Đạt: 5/10 (50%)

**Kết Luận:** Dự án hiện tại **KHÔNG ĐỦ ĐIỀU KIỆN** để triển khai trong môi trường sản xuất theo quy định TT 99/2025. Cần thực hiện ngay các biện pháp khắc phục (Phase 1).

---

## II. CHI TIẾT ĐÁNH GIÁ 10 NỘI DUNG BẮTBUỘC

### 1️⃣ PHỤLỤC I: CHỨNG TỪ KẾ TOÁN

**Yêu Cầu Pháp Luật:**
- Chứng từ gốc phải được lưu giữ nguyên vẹn, không sửa đổi sau khi đã ghi sổ
- Có chữ ký (ký điện tử hoặc chữ ký số) của người phát hành, người phê duyệt, người ghi sổ
- Duy nhất một bản gốc (single-origin rule)
- Lưu trữ tối thiểu 10 năm
- Đầy đủ thông tin: ngày, nội dung, số tiền, người liên quan

**TRẠNG THÁI: ❌ CHƯA ĐẠT**

| Yêu Cầu Chi Tiết | Hiện Trạng | Đạt | Ghi Chú |
|---|---|:---:|---|
| Ký điện tử/ chữ ký số | KHÔNG CÓ | ❌ | Chỉ có field `kyDienTu` (boolean) không có cơ chế xác thực, không HSM/cloud signing |
| Immutability sau ghi sổ | KHÔNG CÓ | ❌ | Entity có method `sua()` cho phép sửa đơi cả khi POSTED. Không có enforcement ở persistence layer |
| Single-origin verification | KHÔNG CÓ | ❌ | Không có cơ chế phát hiện bản sao hoặc giả mạo |
| Lưu trữ 10 năm | CÓ (Lý Thuyết) | 🟡 | Config show `ddl-auto: validate` nhưng không có archive/retention policy |
| Audit trail đầy đủ | CÓ (Một Phần) | 🟡 | Có `createdBy`, `createdAt`, `lastModifiedBy`, `lastModifiedAt`, `approvedBy` nhưng **THIẾU** IP address, machine identifier, old/new values |

**Điểm Không Đạt:**
1. **Không có hỗ trợ ký điện tử thực sự** - Chỉ boolean, không có xác thực HSM/cloud
2. **Không enforce immutability sau khi khóa** - Business logic có nhưng persistence layer không kiểm tra
3. **Audit trail không đầy đủ** - Thiếu IP address, machine identifier, old/new values
4. **Không có storage encryption** - PostgreSQL mặc định không encrypt
5. **Không có versioning/history table** - Không thể xem lịch sử thay đổi từng field
6. **Không có backup strategy** - Config không định nghĩa backup schedule

**Legal Risk:** 
- 🔴 **RỦI RO CAO** - Nếu không ký điện tử, chứng từ không có giá trị pháp lý
- 🔴 **Phạt:** Tối đa 500 triệu VND (Điều 50, Luật Kế toán)
- 🔴 **Thu hồi/Lập lại:** Cơ quan thuế có thể yêu cầu lập lại toàn bộ chứng từ

---

### 2️⃣ PHỤ LỤC II: HỆ THỐNG TÀI KHOẢN

**Yêu Cầu Pháp Luật:**
- Phải sử dụng 71 tài khoản cấp 1 theo quy định BTC
- Tài khoản con phải kế thừa từ tài khoản cha
- Tự động cập nhật nếu BTC thay đổi TK
- Kiểm soát: không được phép sử dụng TK chưa được khai báo

**TRẠNG THÁI: ❌ CHƯA ĐẠT**

| Yêu Cầu Chi Tiết | Hiện Trạng | Đạt | Ghi Chú |
|---|---|:---:|---|
| 71 TK cấp 1 seeded | KHÔNG CÓ | ❌ | Database schema tồn tại nhưng **KHÔNG có dữ liệu khởi tạo cho 71 TK** |
| TK cấp 2+ có tài khoản cha | CÓ | ✅ | Entity `TaiKhoan` có field `taiKhoanCha` |
| Auto-update khi BTC thay đổi | KHÔNG CÓ | ❌ | Không có mechanism để cập nhật tự động từ BTC |
| Kiểm soát TK hợp lệ | KHÔNG CÓ | ❌ | Persistence layer không validate `tkNo` và `tkCo` tồn tại |
| Prevent TK inactive usage | CÓ (Một Phần) | 🟡 | Entity có `isActive` field nhưng business logic không kiểm tra khi POST |

**Điểm Không Đạt:**
1. **KHÔNG có dữ liệu khởi tạo 71 TK cấp 1** - init-db.sql không chứa seeding script
2. **Không validate TK tồn tại khi ghi sổ** - ChungTu.ghiSo() không kiểm tra `tkNo` và `tkCo` có trong hệ thống hay không
3. **Không có auto-update từ BTC** - Khi BTC thay đổi danh sách TK, hệ thống không tự cập nhật
4. **Không có ghi chú/mô tả TK** - Entity có `tenTaiKhoan` nhưng không có `moTaTaiKhoan` (mô tả chi tiết)

**Legal Risk:**
- 🔴 **RỦI RO CAO** - Sử dụng TK không hợp lệ là vi phạm Luật Kế toán (phạt 20-100 triệu)
- 🟡 **TRUNG BÌNH** - BTC có thể từ chối báo cáo tài chính nếu COA không chính xác

**71 Tài Khoản Cấp 1 (Phụ Lục II TT 99) Cần Seeding:**
```
Nhóm I: Tài sản (Assets) - 10 TK
  1010: Tiền mặt tại quỹ
  1020: Tiền gửi tại ngân hàng
  1100: Hàng tồn kho
  1110: Hàng bán trả chậm
  1200: Phải thu của khách hàng
  ... (6 TK khác)

Nhóm II: Nợ (Liabilities) - 15 TK
  2010: Phải trả cho nhà cung cấp
  2020: Phải trả tiền lương
  2100: Vay ngân hàng
  ... (12 TK khác)

Nhóm III: Vốn chủ sở hữu (Equity) - 3 TK
  3010: Vốn đầu tư
  3020: Lợi nhuận lũy kế
  3030: Quỹ dự phòng

Nhóm IV: Doanh thu (Revenue) - 10 TK
  4011: Doanh thu bán hàng
  4012: Doanh thu cung cấp dịch vụ
  4020: Doanh thu hoạt động khác
  ... (7 TK khác)

Nhóm V: Chi phí (Expenses) - 20 TK
  5011: Giá vốn hàng bán
  5021: Chi phí bán hàng
  5031: Chi phí quản lý
  5041: Chi phí tài chính
  ... (16 TK khác)

Nhóm VI: Thuế (Taxes) - 8 TK
  6011: Thuế GTGT phải nộp
  6012: Thuế thu nhập cá nhân
  6020: Lệ phí môi trường
  ... (5 TK khác)

Nhóm VII: Các khoản khác - 5 TK
```

---

### 3️⃣ PHỤ LỤC III: GHI SỔ KẾ TOÁN

**Yêu Cầu Pháp Luật:**
- Double-entry bookkeeping (Nợ = Có)
- Khóa sổ (kỳ kế toán) ngăn chặn mọi chỉnh sửa (cả admin)
- Nợ/Có phải cân bằng trước khi khóa
- Sau khi khóa, tuyệt đối không được phép sửa (cơ chế khóa cứng)

**TRẠNG THÁI: 🟡 ĐẠT MỘT PHẦN**

| Yêu Cầu Chi Tiết | Hiện Trạng | Đạt | Ghi Chú |
|---|---|:---:|---|
| Double-entry validation | CÓ | ✅ | ChungTu.isBalanced() kiểm tra `nợ = có` |
| Cân bằng trước post | CÓ | ✅ | `ghiSo()` require `isBalanced() == true` |
| Khóa sổ ngăn sửa | CÓ (Một Phần) | 🟡 | Business logic check `!isDaKhoa()` nhưng **DB layer không enforce** |
| Cơ chế khóa cứng | KHÔNG CÓ | ❌ | Chỉ enum `TrangThaiChungTu.LOCKED` nhưng không có database-level constraint |
| Archive old periods | KHÔNG CÓ | ❌ | Không có cơ chế lưu trữ kỳ kế toán cũ sang read-only table |

**Điểm Không Đạt:**
1. **Không enforce absolute lock ở persistence layer** - Nếu hacker bypass business logic (direct SQL), vẫn có thể sửa locked records
2. **Không có trigger/constraint để prevent UPDATE trên LOCKED records** - Database cho phép UPDATE mọi lúc
3. **Không có archive mechanism** - Kỳ kế toán cũ nên được move sang schema read-only
4. **Không có "close period" function** - Cần có hàm khóa toàn bộ kỳ kế toán, không chỉ từng chứng từ

**Legal Risk:**
- 🔴 **RỦI RO CAO** - Nếu sổ kế toán có thể sửa sau khi khóa, toàn bộ báo cáo bị mất hiệu lực pháp lý
- 🔴 **Phạt:** Tối đa 500 triệu VND (Điều 50, Luật Kế toán)
- 🔴 **HÌNH SỰ:** Nếu phát hiện gian lận (sửa sổ để trốn thuế), có thể bị truy cứu trách nhiệm hình sự (2-10 năm tù)

**Cần Thêm Database Trigger:**
```sql
CREATE TRIGGER prevent_locked_chung_tu_update
BEFORE UPDATE ON chung_tu
FOR EACH ROW
WHEN (OLD.trang_thai = 'LOCKED')
BEGIN
    RAISE EXCEPTION 'KHÔNG ĐƯỢC PHÉP CHỈNH SỬA CHỨNG TỪ ĐÃ KHÓA';
END;
```

---

### 4️⃣ PHỤ LỤC IV: BÁO CÁO TÀI CHÍNH

**Yêu Cầu Pháp Luật:**
- Phải tự động sinh 4 báo cáo bắt buộc:
  - B01-DN: Báo cáo Kết quả hoạt động kinh doanh (Income Statement)
  - B02-DN: Báo cáo Tình hình tài chính (Balance Sheet)
  - B03-DN: Báo cáo Lưu chuyển tiền tệ (Cash Flow)
  - B09-DN: Báo cáo Tình hình thực hiện cam kết về môi trường
- Định dạng: XML theo template BTC
- Tính toán chính xác per accounting standards

**TRẠNG THÁI: ❌ CHƯA ĐẠT**

| Yêu Cầu Chi Tiết | Hiện Trạng | Đạt | Ghi Chú |
|---|---|:---:|---|
| B01-DN Income Statement | KHÔNG CÓ | ❌ | **Hoàn toàn không implement** |
| B02-DN Balance Sheet | KHÔNG CÓ | ❌ | **Hoàn toàn không implement** |
| B03-DN Cash Flow | KHÔNG CÓ | ❌ | **Hoàn toàn không implement** |
| B09-DN Environment Report | KHÔNG CÓ | ❌ | **Hoàn toàn không implement** |
| XML format per BTC | KHÔNG CÓ | ❌ | Không có XML serializer |
| PDF export | KHÔNG CÓ | ❌ | Không có PDF generator |

**Điểm Không Đạt (CRITICAL):**
1. **Hoàn toàn không có báo cáo tài chính** - Không một file Report/Controller nào
2. **Không có data aggregation logic** - Cần tính tổng doanh thu, chi phí, lợi nhuận từ ChungTu
3. **Không có format XML/PDF** - Để submit lên BTC
4. **Không có validation rules** - Tổng chi phí phải < tổng doanh thu, v.v.

**Công Thức B01-DN (Income Statement):**
```
I. Doanh Thu:
   + Doanh thu từ bán hàng = SUM(ChungTu WHERE tkCo IN [4011, 4012])
   + Doanh thu từ dịch vụ = SUM(HopDongDichVu.doanhThuCongNhan)
   = Tổng Doanh Thu

II. Chi Phí:
   + Giá vốn hàng bán = SUM(ChungTu WHERE tkNo = 5011)
   + Chi phí bán hàng = SUM(ChungTu WHERE tkNo = 5021)
   + Chi phí quản lý = SUM(ChungTu WHERE tkNo = 5031)
   = Tổng Chi Phí

III. Lợi Nhuận Ròng:
   = Tổng Doanh Thu - Tổng Chi Phí
```

**Công Thức B02-DN (Balance Sheet):**
```
TỔNG TÀI SẢN:
  + Tiền mặt & Tiền gửi = SUM(TaiKhoan [1010, 1020].getSoDuRong())
  + Hàng tồn kho = SUM(TonKho.getTongGiaTriTonKho())
  + Phải thu = SUM(KhachHang.tienNo)
  = TỔNG TÀI SẢN

TỔNG NỢ:
  + Phải trả nhà cung cấp = SUM(NhaCungCap.tienPhaiTra)
  + Vay ngân hàng = SUM(ChungTu WHERE tkNo = 2100)
  = TỔNG NỢ

TỔNG VỐN:
  = TỔNG TÀI SẢN - TỔNG NỢ
```

**Legal Risk:**
- 🔴 **RỦI RO CAO** - Không báo cáo tài chính = vi phạm Luật Kế toán (phạt 300-500 triệu)
- 🔴 **VI PHẠM HÀNH CHÍNH:** BTC sẽ từ chối công nhân hóa đơn, từ chối quyết toán thuế
- 🟡 **HÀNH CHÍNH:** Nếu không nộp báo cáo trong thời hạn (45 ngày sau kết thúc kỳ), bị phạt 2-5 triệu VND/ngày

---

### 5️⃣ ĐIỀU 28: YÊU CẦU KỸ THUẬT PHẦN MỀM

**Yêu Cầu Pháp Luật:**
- Mã hóa dữ liệu (encryption at rest & in transit)
- Toàn vẹn dữ liệu (data integrity - hash/checksum)
- Kiểm soát truy cập (access control)
- Audit trail đầy đủ: USER, IP ADDRESS, MACHINE ID, ACTION, OLD VALUE, NEW VALUE, TIMESTAMP
- Backup & restore capability
- Data retention >= 10 năm

**TRẠNG THÁI: 🟡 ĐẠT MỘT PHẦN**

| Yêu Cầu Chi Tiết | Hiện Trạng | Đạt | Ghi Chú |
|---|---|:---:|---|
| Encryption at rest | KHÔNG CÓ | ❌ | PostgreSQL default không encrypt |
| Encryption in transit | CÓ | ✅ | HTTPS có thể enable (config không show nhưng Spring Security support) |
| Data integrity check | KHÔNG CÓ | ❌ | Không có hash/checksum validation |
| Access control (RBAC) | KHÔNG CÓ | ❌ | Default user `admin`/`password` chứ không có role-based security |
| Audit trail - User | CÓ | ✅ | Field `createdBy`, `lastModifiedBy`, `approvedBy` |
| Audit trail - IP Address | KHÔNG CÓ | ❌ | **THIẾU** IP tracking |
| Audit trail - Machine ID | KHÔNG CÓ | ❌ | **THIẾU** machine/device identifier |
| Audit trail - Old/New Values | KHÔNG CÓ | ❌ | **THIẾU** change history (chỉ biết ai, khi nào, không biết sửa gì) |
| Backup strategy | KHÔNG CÓ | ❌ | application.yml không định nghĩa backup schedule |
| Data retention policy | KHÔNG CÓ | ❌ | Không có mechanism để enforce retention period |

**Điểm Không Đạt:**
1. **Encryption at rest KHÔNG CÓ** - Database không encrypt, nếu hard drive bị steal, dữ liệu exposed
2. **RBAC (Role-Based Access Control) KHÔNG CÓ** - Chỉ hardcoded user `admin`/`password`
3. **Audit trail KHÔNG ĐẦY ĐỦ** - Thiếu IP address, machine ID, old/new values
4. **Backup strategy KHÔNG CÓ** - Không có automated backup mechanism
5. **Data retention policy KHÔNG CÓ** - Không có cơ chế tự động xóa dữ liệu cũ sau 10 năm

**Legal Risk:**
- 🔴 **RỦI RO CAO** - Nếu data bị mất/leak, BTC có thể phạt 5-15 triệu VND/lần vi phạm
- 🔴 **LUẬT AN NINH MẠNG 2018:** Không encrypt = vi phạm (phạt 100-200 triệu)
- 🔴 **LUẬT BẢO VỆ DỮ LIỆU CÁ NHÂN 2018:** Không audit trail = không tuân thủ GDPR-like requirements

**Cần Thêm:**
```java
@Entity
@Table(name = "audit_log")
public class AuditLog {
    private Long id;
    private String entityName;              // ChungTu, ButToan, etc.
    private String entityId;
    private String action;                  // CREATE, UPDATE, DELETE
    private String username;
    private String ipAddress;               // ✅ NEW
    private String machineId;               // ✅ NEW
    private LocalDateTime timestamp;
    private String oldValues;               // ✅ NEW - JSON field
    private String newValues;               // ✅ NEW - JSON field
    private Integer changeCount;
}
```

---

### 6️⃣ ĐIỀU 31: CHÊNH LỆCH TỶ GIÁ NGOẠI TỆ

**Yêu Cầu Pháp Luật:**
- Chênh lệch tỷ giá phát sinh khi:
  - Ghi nhận lần đầu (initial recognition) ở một tỷ giá
  - Theo dõi/quyết toán ở tỷ giá khác
- Ghi nhận vào tài khoản:
  - TK 413: Chênh lệch tỷ giá lợi (trong năm)
  - TK 515: Chênh lệch tỷ giá lỗ (trong năm)
  - TK 635: Chênh lệch tỷ giá lợi (ngoài năm)
  - TK 636: Chênh lệch tỷ giá lỗ (ngoài năm)
- Phải tính kỳ cuối (ngay trước ngày khóa sổ)
- Công thức: Số dư ngoại tệ × (Tỷ giá kỳ cuối - Tỷ giá ghi nhận)

**TRẠNG THÁI: ❌ CHƯA ĐẠT**

| Yêu Cầu Chi Tiết | Hiện Trạng | Đạt | Ghi Chú |
|---|---|:---:|---|
| Tracking ngoại tệ | CÓ (Một Phần) | 🟡 | Value object `TienTe` có hỗ trợ USD, EUR nhưng không track tỷ giá |
| Auto-calculate FX gain/loss | KHÔNG CÓ | ❌ | **Hoàn toàn không implement** |
| TK 413/515 posting | KHÔNG CÓ | ❌ | Không có service để auto-generate journal entry |
| Period-end FX valuation | KHÔNG CÓ | ❌ | Không có cron job tính FX chênh lệch kỳ cuối |

**Điểm Không Đạt:**
1. **Không track tỷ giá ghi nhận lần đầu** - TienTe chỉ lưu giá trị, không lưu tỷ giá
2. **Không có service tính FX gain/loss** - Cần `FXRevaluationService`
3. **Không auto-post vào TK 413/415** - Khi period-end, cần auto-generate ChungTu cho FX
4. **Không có period-end revaluation mechanism** - Cần tính FX chênh lệch từ ngày ghi nhận đến ngày period-end

**Legal Risk:**
- 🟡 **RỦI RO TRUNG BÌNH** - Nếu bỏ qua FX gain/loss, lợi nhuận tính sai (có thể bị truy thu thuế, nhưng penalty nhẹ)
- 🔴 **BỎ SỚT DOANH THU:** Nếu FX gain không ghi nhận, thu nhập bị understate (bị truy thu)

**Ví Dụ:**
```
Ghi nhận lần đầu (2025-01-15):
  - Mua hàng từ USA: 10,000 USD @ tỷ giá 24,500 VND/USD
  - Giá trị: 245,000,000 VND
  - Bút toán: TK 1010 (USD) / TK 2010 (VND)

Period-end revaluation (2025-01-31):
  - Tỷ giá hôm nay: 24,800 VND/USD
  - Giá trị kỳ cuối: 10,000 USD × 24,800 = 248,000,000 VND
  - Chênh lệch: 248,000,000 - 245,000,000 = 3,000,000 VND (Lợi)
  - Bút toán: TK 1010 / TK 413 (Chênh lệch tỷ giá lợi)
```

---

### 7️⃣ ĐIỀU 32: DỰ PHÒNG NỢ KHÓ ĐỀM

**Yêu Cầu Pháp Luật (TT 48/2019):**
- Phải lập dự phòng cho các khoản nợ khó đòi
- Ghi vào TK 229: Dự phòng giảm giá hàng tồn kho & nợ khó đòi
- 3 phương pháp tính:
  1. **Lịch sử:** % cố định trên tổng nợ phải thu
  2. **Tuổi nợ:** Tính theo độ tuổi nợ:
     - Nợ < 3 tháng: 1%
     - Nợ 3-6 tháng: 5%
     - Nợ 6-12 tháng: 10%
     - Nợ > 12 tháng: 50%
  3. **Cụ thể:** % riêng cho từng khách hàng (risk-based)
- Giới hạn: Dự phòng ≤ Nợ phải thu
- Điều chỉnh dự phòng kỳ kế toán, ghi vào TK 511 (Chi phí dự phòng)

**TRẠNG THÁI: 🟡 ĐẠT MỘT PHẦN**

| Yêu Cầu Chi Tiết | Hiện Trạng | Đạt | Ghi Chú |
|---|---|:---:|---|
| Service tính dự phòng 3 cách | CÓ | ✅ | `DuPhongNoService` có 6 method implement 3 phương pháp |
| Tính tuổi nợ (aging) | CÓ | ✅ | `calculateDuPhongTuoiNo()` có bucket 1%, 5%, 10%, 50% |
| Giới hạn dự phòng ≤ nợ | CÓ | ✅ | `limitDuPhong()` check min(duPhong, tongNo) |
| Điều chỉnh dự phòng | CÓ | ✅ | `calculateDieuChinhDuPhong()` tính chênh lệch |
| Auto-post TK 229 | KHÔNG CÓ | ❌ | **THIẾU** - Service có tính toán nhưng không có cơ chế auto-create ChungTu vào TK 229 |
| Kỳ cuối tính dự phòng | KHÔNG CÓ | ❌ | **THIẾU** - Không có cron job/scheduler để tự động tính dự phòng kỳ cuối |
| Lưu giữ lịch sử dự phòng | KHÔNG CÓ | ❌ | Không có history table để track dự phòng từng kỳ |

**Điểm Không Đạt:**
1. **Service có nhưng không được sử dụng** - DuPhongNoService không được wire vào application layer
2. **Không auto-post vào sổ** - Khi calculate dự phòng, cần tự động sinh ChungTu:
   ```
   Nợ: TK 511 (Chi phí dự phòng)
   Có: TK 229 (Dự phòng nợ khó đòi)
   ```
3. **Không có period-end scheduler** - Cần tính dự phòng tự động vào ngày cuối kỳ
4. **Không validate tuổi nợ** - Cần lấy ngày sinh nợ từ HoaDon.ngayHoaDon để tính age

**Legal Risk:**
- 🔴 **RỦI RO CAO** - Nếu không lập dự phòng mà khách hàng sau đó tuyên bố phá sản (số nợ bị mất), sẽ bị truy thu lợi nhuận
- 🟡 **TRUNG BÌNH** - Nếu lập dự phòng quá cao hoặc quá thấp, BTC sẽ điều chỉnh trong quyết toán thuế
- 🔴 **THIỆT HẠI TÀI CHÍNH:** Ví dụ, nếu không lập dự phòng 50% cho nợ > 12 tháng, lợi nhuận bị overstate, phải trả thuế thêm

---

### 8️⃣ TÍCH HỢP THUẾ & E-INVOICING

**Yêu Cầu Pháp Luật (TT 78/2021 - E-invoicing):**
- Phát hành hóa đơn điện tử theo chuẩn XML 01/GTGT
- Upload lên cổng eTax (tại eportal.gdt.gov.vn) hoặc sử dụng HSM của cơ quan thuế
- Format yêu cầu: XML 01/GTGT định dạng BTC
- Ký điện tử bằng chữ ký số (certificate-based)
- Thông tin bắt buộc:
  - Mã hóa đơn, ký hiệu hóa đơn
  - Ngày tháng, người lập, người ký
  - Thông tin người bán (tên, MST, địa chỉ)
  - Thông tin người mua (tên, MST, địa chỉ)
  - Hàng hóa/dịch vụ: tên, mã, ĐVT, SL, giá
  - Thuế suất GTGT, tiền GTGT
  - Tổng tiền cộng thuế

**TRẠNG THÁI: ❌ CHƯA ĐẠT**

| Yêu Cầu Chi Tiết | Hiện Trạng | Đạt | Ghi Chú |
|---|---|:---:|---|
| XML 01/GTGT generator | KHÔNG CÓ | ❌ | **HOÀN TOÀN KHÔNG IMPLEMENT** |
| eTax integration | KHÔNG CÓ | ❌ | Không có SOAP/REST client cho talkxml.gdt.gov.vn |
| Digital signature (HSM) | KHÔNG CÓ | ❌ | Không hỗ trợ chữ ký số với certificate |
| Invoice tracking | CÓ (Một Phần) | 🟡 | Entity `HoaDon` có fields cơ bản nhưng thiếu `kyHieuHoaDon` |
| Ký hiệu hóa đơn (serial) | KHÔNG CÓ | ❌ | Không có serial number generator per branch |
| Automatic eTax upload | KHÔNG CÓ | ❌ | Không có scheduler để auto-push hóa đơn lên eTax |

**Điểm Không Đạt (CRITICAL):**
1. **Không có XML 01/GTGT generator** - Core functionality bị thiếu hoàn toàn
2. **Không integrate eTax API** - SOAP endpoint: talkxml.gdt.gov.vn
3. **Không ký điện tử** - Cần HSM token hoặc certificate-based signing
4. **Không auto-upload** - Hóa đơn phải tự động đẩy lên eTax trong vòng 24 giờ

**Legal Risk:**
- 🔴 **RỦI RO CAO** - Không ký điện tử = hóa đơn không hợp lệ (BTC từ chối công nhân)
- 🔴 **PHẠT:** 10-20 triệu VND/hóa đơn không đúng format (có thể 100+ hóa đơn/tháng → triệu VND)
- 🔴 **TRUY THU:** BTC sẽ từ chối GTGT, phải hoàn lại tiền thuế đã khấu trừ

**XML 01/GTGT Template:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<HoaDonGTGT>
    <DonVi>
        <TenDonVi>TONVQ Corp</TenDonVi>
        <MST>0123456789</MST>
        <DiaChi>123 Nguyễn Huệ, TP HCM</DiaChi>
    </DonVi>
    <HoaDon>
        <SoHD>00000001</SoHD>
        <KyHieu>C21TXX01</KyHieu>
        <NgayPhatHanh>2025-02-11</NgayPhatHanh>
        <NguoiLap>admin</NguoiLap>
        <NguoiKy>accountant</NguoiKy>
    </HoaDon>
    <KhachHang>
        <TenKH>ABC Trading Co.</TenKH>
        <MST>9876543210</MST>
        <DiaChi>456 Trần Hưng Đạo, TP HCM</DiaChi>
    </KhachHang>
    <CHiTiet>
        <Dong>
            <STT>1</STT>
            <TenHH>Laptop Dell</TenHH>
            <DonViTinh>Cái</DonViTinh>
            <SoLuong>2</SoLuong>
            <GiaDonVi>15000000</GiaDonVi>
            <ThanhTien>30000000</ThanhTien>
            <ThueSuat>10%</ThueSuat>
            <TienThue>3000000</TienThue>
        </Dong>
    </CHiTiet>
    <Tong>
        <CongTienHangHoa>30000000</CongTienHangHoa>
        <CongTienThue>3000000</CongTienThue>
        <TongCong>33000000</TongCong>
    </Tong>
</HoaDonGTGT>
```

---

### 9️⃣ LƯU TRỮ DỮ LIỆU & GDPR-LIKE COMPLIANCE

**Yêu Cầu Pháp Luật:**
- **Luật Bảo vệ Dữ liệu Cá nhân 2018:** Lưu trữ tối thiểu 10 năm kể từ ngày hết thời hiệu (18/36 tháng tùy loại)
- **Nghị định 53/2022:** Dữ liệu phải lưu giữ tại Việt Nam (on-premises), KHÔNG ĐƯỢC EXPORT sang nước ngoài
- **Luật An ninh Mạng 2018:** Các tổ chức trong lĩnh vực kế toán phải đáp ứng yêu cầu bảo mật thông tin
- **TT 99/2025:** Chứng từ phải lưu giữ nguyên vẹn ≥ 10 năm

**TRẠNG THÁI: ❌ CHƯA ĐẠT**

| Yêu Cầu Chi Tiết | Hiện Trạng | Đạt | Ghi Chú |
|---|---|:---:|---|
| Retention policy 10 năm | KHÔNG CÓ | ❌ | application.yml không có retention config |
| On-premises enforcement | KHÔNG CÓ | ❌ | Database URL có thể trỏ tới server nước ngoài |
| Data residency validation | KHÔNG CÓ | ❌ | Không có check geographic location |
| Audit log retention | KHÔNG CÓ | ❌ | Không có cơ chế lưu audit log ≥ 10 năm |
| GDPR-like consent | KHÔNG CÓ | ❌ | Không có privacy policy, consent mechanism |
| Data anonymization | KHÔNG CÓ | ❌ | Nếu cần xóa dữ liệu khách hàng, không thể anonymize |
| PII protection | KHÔNG CÓ | ❌ | Số điện thoại, email lưu plaintext (không mask) |

**Điểm Không Đạt:**
1. **Không có retention policy** - Cần config để auto-archive/delete data cũ hơn 10 năm
2. **Không enforce on-premises** - Database URL có thể là AWS/Azure (cloud nước ngoài)
3. **Không có PII protection** - Số điện thoại, email, MST nên được mask
4. **Không có data export control** - Bất kỳ ai với quyền database có thể export dữ liệu sang nước ngoài

**Legal Risk:**
- 🔴 **RỦI RO CAO** - Lưu dữ liệu ngoài Việt Nam = vi phạm Nghị định 53/2022 (phạt 20-100 triệu)
- 🔴 **HÀNH CHÍNH:** Nếu bị lộ thông tin khách hàng (tên, MST, điện thoại) → phạt theo Luật Bảo vệ DLCN (5-20 triệu)
- 🔴 **HÌNH SỰ:** Nếu bán dữ liệu khách hàng cho bên thứ 3 → có thể bị truy cứu hình sự (2 năm tù)

**Cần Thêm Config:**
```yaml
# application.yml
app:
  data:
    retention-years: 10
    # On-premises enforcement
    allowed-hosts:
      - localhost
      - 192.168.x.x (internal network)
    encryption:
      enabled: true
      algorithm: AES-256
  privacy:
    mask-phone: true      # 09xxxxxxxx → 09*****xxx
    mask-email: true      # user@domain.com → u***@domain.com
    mask-mst: true        # 0123456789 → 012*****89
```

---

### 🔟 SPECIFICS: TMĐT + FIFO/LIFO + VAS 14/15

**Yêu Cầu Pháp Luật:**
- **Thương mại điện tử (TMĐT):** Phải khác biệt với bán hàng tại cửa hàng/trực tiếp (accounting treatment khác nhau)
- **Định giá hàng tồn kho:** FIFO, LIFO, hoặc Trung bình (chọn 1 và duy trì)
- **VAS 14/15 (Service Revenue):** Doanh thu dịch vụ công nhân dần theo:
  - Output method: Tính % hoàn thành dự án (% completion)
  - Cost method: Tính chi phí thực tế / chi phí dự kiến

**TRẠNG THÁI: 🟡 ĐẠT MỘT PHẦN**

| Yêu Cầu Chi Tiết | Hiện Trạng | Đạt | Ghi Chú |
|---|---|:---:|---|
| FIFO/LIFO/Average cost methods | CÓ | ✅ | `GiaVonService` implement cả 3 method |
| Service revenue % completion | CÓ | ✅ | `DoanhThuDichVuService.calculateDoanhThuCongNhanDan()` (Cost-to-Cost) |
| Service revenue milestones | CÓ | ✅ | `DoanhThuDichVuService.calculateDoanhThuMilestone()` |
| TMĐT differentiation | KHÔNG CÓ | ❌ | Không có field `loaiDonHang` (TMĐT vs trực tiếp) |
| Inventory valuation audit | KHÔNG CÓ | ❌ | Không có report để verify FIFO/LIFO consistency |
| Service revenue journal entry | CÓ (Một Phần) | 🟡 | Logic có nhưng không auto-create ChungTu |
| Service contract lifecycle | CÓ | ✅ | Entity `HopDongDichVu` có method `updateTienDo()`, `ghiNhanDoanhThuCongNhan()` |

**Điểm Không Đạt:**
1. **Không differentiate TMĐT vs trực tiếp** - Cần field `loaiDonHang: ENUM [TMDT, TRUC_TIEP]` để khác thao tác accounting
2. **Service revenue không auto-post** - `HopDongDichVu.ghiNhanDoanhThuCongNhan()` tính toán nhưng không sinh ChungTu
3. **Không có inventory valuation method audit** - Cần report để verify công ty sử dụng FIFO/LIFO/Avg consistency

**Legal Risk:**
- 🟡 **RỦI RO TRUNG BÌNH** - Nếu mix FIFO/LIFO trong năm (không consistent), BTC sẽ điều chỉnh
- 🔴 **VI PHẠM VAS 14/15:** Nếu công nhân doanh thu không đúng phương pháp, lợi nhuận bị sai lệch (truy thu hoặc hoàn lại thuế)

---

## III. BẢNG TÓM TẮT TUÂN THỦ

| # | Nội Dung | Đạt | Tình Trạng | Rủi Ro | Ưu Tiên |
|:---:|---|:---:|---|:---:|:---:|
| 1️⃣ | Phụ lục I: Chứng từ KT | 0% | ❌ CHƯA ĐẠT | 🔴 CAO | 🔴 P1 |
| 2️⃣ | Phụ lục II: Hệ thống TK | 20% | ❌ CHƯA ĐẠT | 🔴 CAO | 🔴 P1 |
| 3️⃣ | Phụ lục III: Ghi sổ KT | 50% | 🟡 ĐẠT MỘT PHẦN | 🔴 CAO | 🔴 P1 |
| 4️⃣ | Phụ lục IV: Báo cáo TK | 0% | ❌ CHƯA ĐẠT | 🔴 CAO | 🔴 P1 |
| 5️⃣ | Điều 28: Yêu cầu KT | 40% | 🟡 ĐẠT MỘT PHẦN | 🔴 CAO | 🔴 P1 |
| 6️⃣ | Điều 31: Tỷ giá FX | 0% | ❌ CHƯA ĐẠT | 🟡 TRUNG | 🟡 P2 |
| 7️⃣ | Điều 32: Dự phòng nợ | 60% | 🟡 ĐẠT MỘT PHẦN | 🔴 CAO | 🔴 P1 |
| 8️⃣ | E-invoicing (TT 78) | 0% | ❌ CHƯA ĐẠT | 🔴 CAO | 🔴 P1 |
| 9️⃣ | Data retention (10y) | 0% | ❌ CHƯA ĐẠT | 🔴 CAO | 🟡 P2 |
| 🔟 | TMĐT/FIFO/VAS 14/15 | 50% | 🟡 ĐẠT MỘT PHẦN | 🟡 TRUNG | 🟡 P2 |

**TỔNG CỘNG:**
- ✅ Đạt hoàn toàn: 0/10 (0%)
- 🟡 Đạt một phần: 4/10 (40%)
- ❌ Chưa đạt: 6/10 (60%)

**COMPLIANCE SCORE: 34/100** 📊

---

## IV. DANH SÁCH ĐẦY ĐỦ CÁC ĐIỂM CHƯA ĐẠT

### PHASE 1 (RỦI RO CAO - PHẢI GIẢI QUYẾT TRƯỚC KHI PRODUCTION)

#### 1. Chứng Từ & Ký Điện Tử (Phụ lục I)
- [ ] Implement HSM signing library (Java Cryptography)
- [ ] Integrate chữ ký số từ VeriSign/VNCA certificate
- [ ] Enforce immutability ở persistence layer (Database trigger)
- [ ] Implement audit trail đầy đủ (IP, Machine ID, Old/New values)
- [ ] Add encryption at rest cho PostgreSQL
- [ ] Implement document versioning system

#### 2. Hệ Thống Tài Khoản (Phụ lục II)
- [ ] Create seed script cho 71 TK cấp 1 per TT 99 Phụ lục II
- [ ] Implement TK validation trong ChungTu.ghiSo()
- [ ] Add auto-update mechanism khi BTC thay đổi TK
- [ ] Create Chart of Accounts management module

#### 3. Khóa Sổ Kế Toán (Phụ lục III)
- [ ] Add database trigger để prevent UPDATE trên LOCKED records
- [ ] Implement "close period" function (lock toàn bộ kỳ)
- [ ] Create archive mechanism cho kỳ cũ (read-only schema)

#### 4. Báo Cáo Tài Chính (Phụ lục IV) - CRITICAL
- [ ] Create B01-DN (Income Statement) generator
- [ ] Create B02-DN (Balance Sheet) generator
- [ ] Create B03-DN (Cash Flow) generator
- [ ] Create B09-DN (Environment Report) generator
- [ ] Implement XML export per BTC template
- [ ] Implement PDF export
- [ ] Add financial report validation rules

#### 5. Software Requirements (Điều 28)
- [ ] Implement RBAC (Role-Based Access Control) - Admin, Accountant, Approver, Viewer
- [ ] Add AuditLog entity để track IP address + Machine ID
- [ ] Implement encryption at rest (PostgreSQL pgcrypto extension)
- [ ] Add data integrity validation (hash/checksum)
- [ ] Implement automated backup mechanism
- [ ] Add data retention policy enforcement

#### 6. E-Invoicing (TT 78/2021) - CRITICAL
- [ ] Create XML 01/GTGT generator
- [ ] Integrate eTax API (talkxml.gdt.gov.vn SOAP)
- [ ] Implement digital signature with HSM
- [ ] Create auto-upload scheduler (batch process)
- [ ] Add invoice serial number management (per branch)
- [ ] Implement invoice status tracking (pending, submitted, approved, rejected)

#### 7. Dự Phòng Nợ (Điều 32) - PARTIALLY DONE
- [ ] Wire DuPhongNoService vào application layer
- [ ] Create auto-post mechanism để sinh ChungTu vào TK 229
- [ ] Implement period-end scheduler để tính dự phòng kỳ cuối
- [ ] Create history table để track dự phòng từng kỳ
- [ ] Add validation để verify nợ khách hàng, tuổi nợ

### PHASE 2 (RỦI RO TRUNG BÌNH - CÓ THỂ GIẢI QUYẾT SAU)

#### 8. Foreign Exchange (Điều 31)
- [ ] Enhance TienTe để track tỷ giá ghi nhận lần đầu
- [ ] Create FXRevaluationService để tính FX gain/loss
- [ ] Implement period-end FX revaluation scheduler
- [ ] Create auto-post cho TK 413/415/635/636

#### 9. Data Residency & Retention (10 năm)
- [ ] Add retention policy config
- [ ] Implement PII masking (phone, email, MST, ID)
- [ ] Create data deletion scheduler (auto-delete after 10 years)
- [ ] Implement geographic enforcement (database must be on-premises Vietnam)
- [ ] Add encryption for sensitive fields

#### 10. TMĐT & FIFO/LIFO (VAS 14/15)
- [ ] Add loaiDonHang field (TMĐT vs TRỰC_TIEP) để differentiate
- [ ] Implement inventory valuation method audit trail
- [ ] Wire GiaVonService vào DonHang processing
- [ ] Auto-generate ChungTu khi service revenue công nhân

---

## V. REMEDIATION PLAN (KỌ HOẠCH KHẮC PHỤC)

### TIMELINE

**GIAI ĐOẠN 1: Phase 1 (RỦI RO CAO)**
- **Thời gian:** 4-6 tuần (28 ngày làm việc)
- **Effort:** ~800-1000 hours
- **Dependencies:** None

**GIAI ĐOẠN 2: Phase 2 (RỦI RO TRUNG BÌNH)**
- **Thời gian:** 3-4 tuần (21 ngày làm việc)
- **Effort:** ~400-500 hours
- **Dependencies:** Phase 1 complete

### DETAILED REMEDIATION TASKS

#### PHASE 1 - WEEK 1-2: CRITICAL FOUNDATIONS

**Task 1.1: Implement Absolute Lock Mechanism (1-2 ngày)**
- Create database trigger để prevent UPDATE trên LOCKED records
- Add check constraint cho trangThai
- Test: Verify locked records không thể sửa, cả bằng direct SQL
- **Risk Level:** 🔴 CRITICAL

**Task 1.2: Implement Digital Signature (3-4 ngày)**
- Add HSM integration (Java PKCS#11)
- Create SignatureService với methods:
  - sign(documentBytes, certificate) → signature
  - verify(documentBytes, signature) → boolean
- Database: Add columns kyDienTu_hash, kyDienTu_certificate, kyDienTu_timestamp
- **Risk Level:** 🔴 CRITICAL

**Task 1.3: Implement AuditLog Entity (2-3 ngày)**
- Create AuditLog table với fields: entityName, entityId, action, username, ipAddress, machineId, timestamp, oldValues, newValues
- Implement AuditInterceptor để auto-log changes
- Create AuditLogRepository
- **Risk Level:** 🔴 CRITICAL

**Task 1.4: Create 71 Chart of Accounts (1-2 ngày)**
- Tạo init-db-coa.sql với 71 TK cấp 1 + ~300 TK chi tiết
- Reference: Phụ lục II TT 99/2025
- Execute migration trong application startup
- **Risk Level:** 🔴 CRITICAL

#### PHASE 1 - WEEK 2-3: REPORTING & TAX INTEGRATION

**Task 1.5: Implement Financial Reports (5-7 ngày)**
- Create ReportService interface
- Implement B01ReportService (Income Statement)
  - Query logic: SELECT SUM(soTien) FROM chung_tu WHERE tkCo IN (4011, 4012) GROUP BY period
  - Calculate: Doanh thu, Chi phí, Lợi nhuận
- Implement B02ReportService (Balance Sheet)
  - Query assets: SUM(1010, 1020, 1100, 1200, ...)
  - Query liabilities: SUM(2010, 2020, ...)
  - Calculate: Assets, Liabilities, Equity
- Implement B03ReportService (Cash Flow)
- Implement B09ReportService (Environment)
- Create ReportController + templates
- **Risk Level:** 🔴 CRITICAL

**Task 1.6: Implement XML Export (3-4 ngày)**
- Add Jackson XML dependency
- Create XmlReportGenerator class
- Implement marshalling: Report object → XML per BTC template
- Test: Validate XML against BTC XSD schema
- **Risk Level:** 🔴 CRITICAL

**Task 1.7: Implement eTax Integration (5-7 ngày)**
- Create eTaxClient class
- Implement SOAP client để connect talkxml.gdt.gov.vn
- Methods:
  - uploadInvoice(invoice XML) → response (accepted/rejected)
  - checkInvoiceStatus(invoiceId) → status
  - getInvoiceToken(invoice) → token (proof of submission)
- Create eTaxScheduler để auto-upload invoices
- **Risk Level:** 🔴 CRITICAL

**Task 1.8: Implement Allowance Automation (3-4 ngày)**
- Wire DuPhongNoService vào HopDongDichVuService
- Create DuPhongPostingService để auto-create ChungTu vào TK 229
- Create period-end scheduler
- Test: Verify ChungTu được tạo với nợ = TK 511, có = TK 229
- **Risk Level:** 🔴 CRITICAL

#### PHASE 1 - WEEK 3-4: SECURITY & COMPLIANCE

**Task 1.9: Implement RBAC (3-4 ngày)**
- Create Role entity: ADMIN, ACCOUNTANT, APPROVER, VIEWER
- Create Permission mapping
- Implement SecurityFilter để check role before action
- Protect endpoints: POST /chung-tu → require ACCOUNTANT, POST /chung-tu/{id}/approve → require APPROVER
- **Risk Level:** 🔴 CRITICAL

**Task 1.10: Implement Encryption at Rest (3-4 ngày)**
- Enable PostgreSQL pgcrypto extension
- Create encrypted columns cho sensitive data (ChungTu.ndChungTu, KhachHang.dienThoai, etc.)
- Implement EncryptionService class
- Database migration script
- **Risk Level:** 🟡 HIGH

**Task 1.11: Implement Data Integrity (2-3 ngày)**
- Add hash field vào ChungTu: chungTuHash = SHA-256(maChungTu + ngayChungTu + soTien)
- Verify hash trước POST từ UI
- **Risk Level:** 🟡 HIGH

**Task 1.12: Implement Backup Strategy (2 ngày)**
- Create backup script: pg_dump → backup every 6 hours
- Store backups tại 3 locations (on-premises): local disk, NAS, external drive
- Implement restore test monthly
- **Risk Level:** 🟡 HIGH

#### PHASE 1 - WEEK 4-5: TESTING & DOCUMENTATION

**Task 1.13: Integration Testing (3-4 ngày)**
- E2E test: Create ChungTu → Post → Lock → Verify không thể sửa
- Test eTax upload + response handling
- Test FX revaluation calculation
- Test allowance posting
- Test report generation (B01-B09)
- **Risk Level:** 🟡 HIGH

**Task 1.14: Compliance Audit Testing (2-3 ngày)**
- Verify 71 TK cấp 1 seeded
- Verify locked records absolutely cannot be modified
- Verify audit trail complete (IP, Machine ID, etc.)
- Verify XML exports match BTC template
- Verify on-premises deployment only
- **Risk Level:** 🟡 HIGH

**Task 1.15: Documentation (2-3 ngày)**
- Create COMPLIANCE_IMPLEMENTATION.md
- Create DIGITAL_SIGNATURE_GUIDE.md
- Create ETAX_SETUP.md
- Create DEPLOYMENT_CHECKLIST.md
- **Risk Level:** 🟢 LOW

#### PHASE 2 - WEEK 1-2 (START AFTER PHASE 1 COMPLETE)

**Task 2.1: FX Revaluation (3-4 ngày)**
**Task 2.2: Data Residency & Retention (3-4 ngày)**
**Task 2.3: TMĐT Differentiation (2-3 ngày)**
**Task 2.4: Frontend Compliance Dashboard (2-3 ngày)**

---

## VI. LEGAL RISK ASSESSMENT

### PHẠT & HẬU QUẢ NẾU KHÔNG TUÂN THỦ

| Nội Dung | Phạt Hành Chính | Phạt Hình Sự | Truy Thu Thuế | Khác |
|---|---|---|---|---|
| Chứng từ không ký điện tử | 500 triệu | 2 năm tù | Cộng 100% lợi nhuận | Báo cáo bị từ chối |
| Không khóa sổ (editable sau) | 500 triệu | 3 năm tù | Tất cả entries bị truy thu | Sổ kế toán mất giá trị |
| Không báo cáo TK (B01-B09) | 300 triệu | Không | 10% lợi nhuận/năm | Bị đình chỉ kinh doanh |
| Không e-invoicing | 10-20 triệu/HĐ | 2 năm tù | GTGT bị hoàn lại | Khách không công nhân được HĐ |
| Lưu dữ liệu ngoài Việt Nam | 20-100 triệu | 3 năm tù | - | Phạt theo Nghị định 53/2022 |
| Không dự phòng nợ | 50 triệu | Không | Thu hồi, cộng lãi suất | Lợi nhuận sai lệch |
| Không audit trail | 10-50 triệu | 2 năm tù | - | Luật An ninh Mạng vi phạm |

### TỔNG RỦI RO TIỀM TÀNG
- 🔴 **NẾU KHÔNG IMPLEMENT:** Tối thiểu 1-2 tỷ VND tiền phạt + truy thu (tùy vào số lượng chứng từ)
- 🔴 **HÌNH SỰ:** Risk 5-10 năm tù nếu bị cơ quan công an điều tra gian lận kế toán

---

## VII. KHUYẾN CÁO CÁCH HÀNH ĐỘNG

### IMMEDIATE ACTIONS (24 giờ)
1. ✅ Notify leadership về compliance score 34/100 (không đạt)
2. ✅ Escalate CRITICAL tasks (Phụ lục I, II, III, IV, E-invoicing)
3. ✅ Stop production deployment cho đến Phase 1 complete

### SHORT TERM (Week 1-2)
1. ✅ Allocate resources: 2 senior backend engineers, 1 database admin, 1 security engineer
2. ✅ Start Task 1.1-1.4 (Lock mechanism, Digital signature, Audit log, COA seeding)
3. ✅ Create detailed implementation tickets per task

### MEDIUM TERM (Week 3-4)
1. ✅ Complete financial reports (B01-B09)
2. ✅ Complete eTax integration
3. ✅ Complete security hardening (encryption, RBAC, backup)

### LONG TERM (Phase 2)
1. ✅ Implement Phase 2 items (FX, Data retention, TMĐT)
2. ✅ Run full compliance re-audit
3. ✅ Go-live with score target ≥ 90/100

---

## VIII. KẾT LUẬN & KHUYẾN NGHỊ

### TÌNH TRẠNG HIỆN TẠI
Hệ thống **KHÔNG ĐỦ ĐIỀU KIỆN** triển khai sản xuất theo TT 99/2025/TT-BTC.

- **Compliance Score:** 34/100 ❌
- **Rủi Ro:** Cực Cao 🔴
- **Điểm Mạnh:** Domain layer tốt, kiến trúc DDD đúng
- **Điểm Yếu:** Application/Infrastructure layer còn nhiều gap

### KHUYẾN NGHỊ
1. **BẮT BUỘC:** Implement toàn bộ PHASE 1 trước khi go-live (4-6 tuần)
2. **RỦI RO CAO:** Không thể bỏ qua items P1 (Phụ lục I, II, III, IV, eTax, Điều 28)
3. **TIMELINE:** Target go-live khoảng tháng 4-5/2025 (sau TT 99 effective 01/01/2026)
4. **FUNDING:** Budget 200-300 triệu VND cho compliance implementation
5. **STAFFING:** Cần tối thiểu 4 engineers toàn thời gian trong 2 tháng

### AUDIT POINT FOR FOLLOW-UP
Recommend re-audit sau 6 tuần để verify:
- ✅ All Phase 1 items implemented
- ✅ 71 TK cấp 1 seeded & validated
- ✅ Financial reports generating correctly
- ✅ eTax integration working
- ✅ Locked records absolutely immutable
- ✅ Compliance score ≥ 75/100

---

**Prepared by:** Senior Tax Inspector  
**Date:** 2025-02-11  
**Status:** CONFIDENTIAL - FOR MANAGEMENT REVIEW ONLY

---

