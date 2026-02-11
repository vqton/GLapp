# 🏢 AccountingERP - ERP Modules Development Roadmap

## 📋 Overview

AccountingERP là một hệ thống quản lý kế toán doanh nghiệp (ERP) hoàn chỉnh. Project hiện tại bao gồm module cơ sở "Chứng Từ" (Vouchers). Dưới đây là các module cần phát triển tiếp theo.

---

## 📊 AVAILABLE MODULES

### Module 1: ✅ **Chứng Từ (Vouchers)** - BASE MODULE

**Status:** ✅ Implemented  
**Description:** Quản lý các chứng từ kế toán gốc  
**Key Features:**
- Tạo, sửa, xóa chứng từ
- Phân loại (Hóa đơn, Phiếu chi, Phiếu thu, Biên bản, etc.)
- Trạng thái workflow (DRAFT → APPROVED/REJECTED)
- Audit logging

**Files:**
- `domain/model/ChungTu.java`
- `application/dto/ChungTuCreateDTO.java`
- `infrastructure/persistence/JpaChungTuRepository.java`

**Next:** Implement controller, service, templates

---

### Module 2: 📂 **Kho (Inventory Management)**

**Status:** 🚧 To be implemented  
**Description:** Quản lý tồn kho và các phiếu nhập/xuất  
**Key Features:**
- Quản lý sản phẩm, hàng hóa
- Theo dõi tồn kho theo kho
- Phiếu nhập kho (Receipt)
- Phiếu xuất kho (Dispatch)
- Kiểm kho (Inventory Check)
- Báo cáo tồn kho

**Entities to Create:**
```
- SanPham (Product)
- Kho (Warehouse)
- ToKho (Stock)
- PhieuNhapKho (Receipt)
- PhieuXuatKho (Dispatch)
- KiemKho (Inventory Check)
```

**Files Structure:**
```
domain/
├── model/
│   ├── SanPham.java
│   ├── Kho.java
│   ├── ToKho.java
│   ├── PhieuNhapKho.java
│   └── PhieuXuatKho.java
└── repository/
    ├── SanPhamRepository.java
    ├── KhoRepository.java
    └── ToKhoRepository.java

application/
├── dto/
│   ├── SanPhamDTO.java
│   ├── PhieuNhapKhoDTO.java
│   └── PhieuXuatKhoDTO.java
└── service/
    ├── InventoryService.java
    └── WarehouseService.java

infrastructure/
├── persistence/
│   ├── JpaSanPhamRepository.java
│   ├── JpaKhoRepository.java
│   └── JpaToKhoRepository.java
└── web/
    └── controller/
        ├── InventoryController.java
        └── WarehouseController.java
```

---

### Module 3: 💳 **Bán Hàng (Sales Management)**

**Status:** 🚧 To be implemented  
**Description:** Quản lý đơn hàng và hóa đơn bán  
**Key Features:**
- Quản lý khách hàng (KhachHang)
- Tạo đơn hàng (DonHang)
- Tạo hóa đơn bán (HoaDonBan)
- Theo dõi thanh toán
- Báo cáo bán hàng

**Entities:**
```
- KhachHang (Customer)
- DonHang (Sales Order)
- ChiTietDonHang (Order Line)
- HoaDonBan (Sales Invoice)
- ThanhToan (Payment)
```

**Workflow:**
```
Quote → Order → Invoice → Payment → Delivery
```

---

### Module 4: 💰 **Mua Hàng (Purchasing Management)**

**Status:** 🚧 To be implemented  
**Description:** Quản lý đơn hàng mua và hóa đơn nhập  
**Key Features:**
- Quản lý nhà cung cấp (NhaCungCap)
- Tạo đơn mua hàng (DonMuaHang)
- Tạo hóa đơn mua (HoaDonMua)
- Theo dõi thanh toán
- Báo cáo mua hàng

**Entities:**
```
- NhaCungCap (Vendor)
- DonMuaHang (Purchase Order)
- ChiTietDonMua (PO Line)
- HoaDonMua (Purchase Invoice)
```

---

### Module 5: 👥 **Nhân Sự (Human Resources)**

**Status:** 🚧 To be implemented  
**Description:** Quản lý nhân sự và lương  
**Key Features:**
- Quản lý nhân viên (NhanVien)
- Chứng chỉ và kỹ năng
- Quản lý lương
- Tính lương
- Bảng lương
- Báo cáo nhân sự

**Entities:**
```
- NhanVien (Employee)
- ChungChi (Certificate)
- KyNang (Skill)
- TinhLuong (Salary Calculation)
- BangLuong (Payroll)
```

---

### Module 6: 📊 **Tài Khoản (Chart of Accounts)**

**Status:** 🚧 To be implemented  
**Description:** Quản lý bảng tài khoản kế toán  
**Key Features:**
- Bảng tài khoản (TaiKhoan)
- Phân loại tài khoản
- Cấp độ tài khoản (Level 1, 2, 3, etc.)
- Tài khoản mẹ-con
- Tài khoản chi tiết

**Entities:**
```
- TaiKhoan (Account)
- LoaiTaiKhoan (Account Type)
- TaiKhoanCapDo (Account Level)
```

**Account Types:**
```
- Tài sản (Assets)
- Nợ (Liabilities)
- Vốn (Equity)
- Doanh thu (Revenue)
- Chi phí (Expenses)
```

---

### Module 7: 💸 **Sổ Cái (General Ledger)**

**Status:** 🚧 To be implemented  
**Description:** Ghi sổ cái tổng hợp và chi tiết  
**Key Features:**
- Ghi nhập sổ cái
- Nút ghi (Journal Entry)
- Hạch toán kép (Double Entry)
- Sổ cái tổng hợp
- Sổ cái chi tiết
- Đối chiếu sổ cái

**Entities:**
```
- SoCai (Journal Entry Header)
- ChiTietSoCai (Journal Entry Detail)
- DoiChieuSoCai (Reconciliation)
```

---

### Module 8: 📈 **Báo Cáo Tài Chính (Financial Reports)**

**Status:** 🚧 To be implemented  
**Description:** Tạo báo cáo tài chính theo chuẩn  
**Key Features:**
- Bảng cân đối kế toán (Balance Sheet)
- Báo cáo kết quả kinh doanh (Income Statement)
- Báo cáo lưu chuyển tiền (Cash Flow)
- Báo cáo thay đổi vốn (Equity Change)
- Báo cáo bổ sung (Supplementary)

**Report Types:**
```
- Cân đối kế toán (Thực hiện TT 99/2025/TT-BTC)
- Kết quả kinh doanh
- Lưu chuyển tiền mặt
- Thay đổi vốn
```

---

### Module 9: 🏛️ **Tài Sản Cố Định (Fixed Assets)**

**Status:** 🚧 To be implemented  
**Description:** Quản lý tài sản cố định và khấu hao  
**Key Features:**
- Quản lý tài sản (TaiSan)
- Khấu hao (Depreciation)
- Tính khấu hao theo kỳ
- Thanh lý tài sản
- Báo cáo tài sản

**Entities:**
```
- TaiSan (Fixed Asset)
- LoaiKhauHao (Depreciation Method)
- ChiTietKhauHao (Depreciation Detail)
- ThanhLyTaiSan (Asset Disposal)
```

---

### Module 10: 🏦 **Quản Lý Tiền (Cash Management)**

**Status:** 🚧 To be implemented  
**Description:** Quản lý tiền mặt, ngân hàng, thẻ tín dụng  
**Key Features:**
- Quản lý tài khoản ngân hàng
- Quản lý tiền mặt
- Ghi nhận giao dịch
- Đối chiếu ngân hàng
- Báo cáo tiền

**Entities:**
```
- TaiKhoanNganHang (Bank Account)
- GiaoDichNganHang (Bank Transaction)
- DoiChieuNganHang (Bank Reconciliation)
```

---

### Module 11: 📋 **Thuế (Tax Management)**

**Status:** 🚧 To be implemented  
**Description:** Quản lý thuế và quyết toán thuế  
**Key Features:**
- Cấu hình thuế
- Tính thuế GTGT (VAT)
- Tính thuế thu nhập doanh nghiệp (CIT)
- Quyết toán thuế
- Báo cáo thuế

**Entities:**
```
- LoaiThue (Tax Type)
- CauHinhThue (Tax Configuration)
- TinhToanThue (Tax Calculation)
- QuyetToanThue (Tax Settlement)
```

---

## 🔄 DEVELOPMENT WORKFLOW FOR NEW MODULE

### Step 1: Create Domain Entities
```
1. Create entity class in domain/model/
2. Add business methods
3. Add validation logic
4. Add equals() and hashCode()
```

### Step 2: Create Repository Interface
```
1. Create interface in domain/repository/
2. Define query methods
3. Define aggregate operations
```

### Step 3: Create DTOs
```
1. Create CreateDTO in application/dto/
2. Create ResponseDTO in application/dto/
3. Add validation annotations
```

### Step 4: Create Repository Adapter
```
1. Create JPA repository in infrastructure/persistence/
2. Create adapter implementation
3. Implement all domain repository methods
```

### Step 5: Create Application Service
```
1. Create service in application/service/
2. Implement use cases
3. Use domain logic (call domain methods)
4. Handle transactions
```

### Step 6: Create Controller
```
1. Create controller in infrastructure/web/controller/
2. Create REST endpoints
3. Call application service
4. Return DTOs
```

### Step 7: Create Templates
```
1. Create list.html
2. Create detail.html
3. Create form.html
4. Add Bootstrap styling
```

### Step 8: Create Tests
```
1. Create domain unit tests
2. Create application service tests
3. Create controller integration tests
```

---

## 📝 EXAMPLE: Adding "Tài Khoản" (Chart of Accounts) Module

### File Structure
```
AccountingERP/
├── domain/
│   ├── model/
│   │   └── TaiKhoan.java
│   └── repository/
│       └── TaiKhoanRepository.java
│
├── application/
│   ├── dto/
│   │   ├── TaiKhoanCreateDTO.java
│   │   └── TaiKhoanResponseDTO.java
│   └── service/
│       └── TaiKhoanApplicationService.java
│
├── infrastructure/
│   ├── persistence/
│   │   ├── JpaTaiKhoanRepository.java
│   │   └── TaiKhoanRepositoryAdapter.java
│   └── web/
│       └── controller/
│           └── TaiKhoanController.java
│
└── src/main/resources/
    └── templates/
        ├── tai-khoan/
        │   ├── list.html
        │   ├── detail.html
        │   └── form.html
        └── fragments/
            └── tai-khoan-fragments.html
```

### Commands to Create Structure
```powershell
# Create directories
mkdir domain/model
mkdir domain/repository
mkdir application/dto
mkdir application/service
mkdir infrastructure/persistence
mkdir infrastructure/web/controller
mkdir src/main/resources/templates/tai-khoan

# Create Java files
type nul > domain/model/TaiKhoan.java
type nul > domain/repository/TaiKhoanRepository.java
# ... etc
```

---

## 🎯 PRIORITY ROADMAP

### Phase 1: Core Accounting (Months 1-2)
- ✅ Chứng Từ (Done)
- 📂 Kho (Inventory)
- 📊 Tài Khoản (Chart of Accounts)
- 💸 Sổ Cái (General Ledger)

### Phase 2: Sales & Purchasing (Months 2-3)
- 💳 Bán Hàng (Sales)
- 💰 Mua Hàng (Purchasing)
- 🏦 Quản Lý Tiền (Cash Management)

### Phase 3: Advanced Features (Months 4-5)
- 👥 Nhân Sự (HR)
- 📈 Báo Cáo Tài Chính (Financial Reports)
- 🏛️ Tài Sản Cố Định (Fixed Assets)
- 📋 Thuế (Tax)

---

## ✅ CHECKLIST FOR EACH MODULE

- [ ] Domain entities created
- [ ] Repository interfaces defined
- [ ] DTOs created
- [ ] Repository implementations done
- [ ] Application services implemented
- [ ] REST controllers created
- [ ] HTML templates built
- [ ] Unit tests written
- [ ] Integration tests written
- [ ] Documentation updated
- [ ] Database migration created
- [ ] Tested in dev mode
- [ ] Tested in production mode
- [ ] Code reviewed
- [ ] Merged to main branch

---

## 🚀 GETTING STARTED WITH A NEW MODULE

**Example: Adding "Tài Khoản" Module**

1. Read this file
2. Choose module "Tài Khoản"
3. Follow "Development Workflow"
4. Create domain/model/TaiKhoan.java
5. Create repository interface
6. Create DTOs
7. Create repository adapter
8. Create application service
9. Create controller
10. Test thoroughly

---

## 📚 REFERENCES

- Domain-Driven Design: https://www.domainlanguage.com/ddd/
- Spring Data JPA: https://spring.io/projects/spring-data-jpa
- Vietnamese Accounting Standards: TT 99/2025/TT-BTC

---

**Last Updated:** 2025-02-11  
**Status:** Planning Phase  
**Next Review:** After Phase 1 completion
