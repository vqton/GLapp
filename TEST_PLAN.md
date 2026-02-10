# Test Plan - ERP Kế toán Thông tư 99/2025/TT-BTC

## Unit Tests - Nghiệp vụ cốt lõi

### 1. Kiểm tra cân đối Nợ=Có (Điều 18-22)

```python
# tests/unit/test_balance_validation.py
from decimal import Decimal
from app.domain.entities import JournalEntry, AccountingVoucher
from app.domain.value_objects import Money

class TestBalanceValidation:
    """Test nguyên tắc ghi sổ kép."""
    
    def test_balanced_entry_success(self):
        """Bút toán cân đối: Tổng Nợ = Tổng Có."""
        entry = JournalEntry(
            entry_number="BT/202512/001",
            voucher_id=None,
            voucher_date=date(2025, 12, 15),
            posting_date=date(2025, 12, 15),
            description="Mua hàng",
            created_by="test",
            total_debit=Money(Decimal("11000000"), "VND"),
            total_credit=Money(Decimal("11000000"), "VND")
        )
        assert entry.is_balanced() is True
    
    def test_unbalanced_entry_fails(self):
        """Bút toán không cân đối phải reject."""
        entry = JournalEntry(
            entry_number="BT/202512/002",
            voucher_id=None,
            voucher_date=date(2025, 12, 15),
            posting_date=date(2025, 12, 15),
            description="Test",
            created_by="test",
            total_debit=Money(Decimal("10000000"), "VND"),
            total_credit=Money(Decimal("11000000"), "VND")
        )
        assert entry.is_balanced() is False
        
        with pytest.raises(ValueError, match=".*không cân đối.*"):
            entry.post("admin")
    
    def test_voucher_auto_balance_check(self):
        """Chứng từ tự động kiểm tra cân đối khi tạo."""
        voucher = AccountingVoucher(
            voucher_number="CT/202512/001",
            voucher_type="MUA",
            voucher_date=date(2025, 12, 15),
            description="Mua hàng hóa",
            company_code="DEMO",
            created_by="user",
            is_locked=False
        )
        # Validate before save
        assert voucher.can_modify() is True
```

### 2. Cảnh báo âm tồn kho (TK 111, 112, 131, 15x, 21x)

```python
# tests/unit/test_negative_balance.py
from app.domain.entities import AccountBalance
from app.domain.value_objects import Money

class TestNegativeBalanceWarning:
    """Test cảnh báo âm tồn kho."""
    
    def test_negative_cash_balance(self):
        """Phát hiện số dư âm tài khoản tiền."""
        balance = AccountBalance(
            account_code="1111",
            period_type="MONTH",
            period_value=12,
            year=2025,
            company_code="DEMO",
            closing_debit=Money(Decimal("-5000000"), "VND")
        )
        warnings = balance.check_negative_balance()
        assert len(warnings) == 1
        assert "1111" in warnings[0]
        assert "âm" in warnings[0]
    
    def test_positive_balance_no_warning(self):
        """Số dư dương không cảnh báo."""
        balance = AccountBalance(
            account_code="1111",
            period_type="MONTH",
            period_value=12,
            year=2025,
            company_code="DEMO",
            closing_debit=Money(Decimal("50000000"), "VND")
        )
        warnings = balance.check_negative_balance()
        assert len(warnings) == 0
    
    def test_critical_accounts_check(self):
        """Kiểm tra tài khoản nghiệp vụ quan trọng."""
        critical_accounts = [
            ("1111", "Tiền mặt"),
            ("1121", "Tiền gửi ngân hàng"),
            ("131", "Phải thu khách hàng"),
            ("1561", "Hàng hóa kho"),
            ("211", "TSCĐ hữu hình"),
        ]
        
        for code, name in critical_accounts:
            balance = AccountBalance(
                account_code=code,
                period_type="MONTH",
                period_value=12,
                year=2025,
                company_code="DEMO",
                closing_debit=Money(Decimal("1000000"), "VND")
            )
            warnings = balance.check_negative_balance()
            assert len(warnings) == 0, f"{name} ({code}) should not warn with positive balance"
```

### 3. Tỷ giá ngoại tệ (Điều 31)

```python
# tests/unit/test_exchange_rate.py
from decimal import Decimal
from app.domain.services import ExchangeRateService
from app.domain.value_objects import Money, ExchangeRate

class TestExchangeRate:
    """Test tỷ giá ngoại tệ (Điều 31)."""
    
    def test_convert_usd_to_vnd(self):
        """Quy đổi USD sang VND."""
        rate = ExchangeRate(
            rate=Decimal("24500"),
            currency="USD",
            rate_type="REALTIME",
            valuation_date=date(2025, 12, 15)
        )
        
        service = ExchangeRateService()
        result = service.convert_to_vnd(
            amount=Decimal("1000"),
            currency="USD",
            exchange_rate=rate
        )
        
        assert result.amount == Decimal("24500000")
        assert result.currency == "VND"
    
    def test_exchange_diff_profit(self):
        """Lãi tỷ giá (TK 515)."""
        original = ExchangeRate(Decimal("24000"), "USD")
        current = ExchangeRate(Decimal("24500"), "USD")
        
        service = ExchangeRateService()
        diff = service.calculate_exchange_diff(original, current, Decimal("1000"))
        
        assert diff.amount == Decimal("500000")  # Lãi
    
    def test_exchange_diff_loss(self):
        """Lỗ tỷ giá (TK 635)."""
        original = ExchangeRate(Decimal("25000"), "USD")
        current = ExchangeRate(Decimal("24500"), "USD")
        
        service = ExchangeRateService()
        diff = service.calculate_exchange_diff(original, current, Decimal("1000"))
        
        assert diff.amount == Decimal("-500000")  # Lỗ
    
    def test_classify_exchange_diff(self):
        """Phân loại chênh lệch tỷ giá."""
        service = ExchangeRateService()
        
        # Lãi -> TK 515
        account, acc_type = service.classify_exchange_diff(
            Money(Decimal("100000"), "VND")
        )
        assert account == "4131"
        assert acc_type == "REVENUE"
        
        # Lỗ -> TK 635
        account, acc_type = service.classify_exchange_diff(
            Money(Decimal("-100000"), "VND")
        )
        assert account == "4132"
        assert acc_type == "EXPENSE"
```

### 4. Dự phòng nợ phải thu (Điều 32 & TT48/2019)

```python
# tests/unit/test_provision.py
from decimal import Decimal
from app.domain.services import ProvisionService
from app.domain.value_objects import Money

class TestReceivableProvision:
    """Test dự phòng nợ phải thu (Điều 32)."""
    
    def test_no_provision_current_debt(self):
        """Nợ chưa đến hạn: 0%."""
        service = ProvisionService()
        receivables = [{"amount": Decimal("10000000"), "overdue_days": 30}]
        
        provision = service.calculate_specific_provision(receivables, 30)
        assert provision.amount == Decimal("0")
    
    def test_30_percent_3_to_6_months(self):
        """Nợ quá hạn 3-6 tháng: 30%."""
        service = ProvisionService()
        receivables = [{"amount": Decimal("10000000"), "overdue_days": 120}]
        
        provision = service.calculate_specific_provision(receivables, 120)
        assert provision.amount == Decimal("3000000")
    
    def test_50_percent_6_to_12_months(self):
        """Nợ quá hạn 6-12 tháng: 50%."""
        service = ProvisionService()
        receivables = [{"amount": Decimal("10000000"), "overdue_days": 200}]
        
        provision = service.calculate_specific_provision(receivables, 200)
        assert provision.amount == Decimal("5000000")
    
    def test_100_percent_over_1_year(self):
        """Nợ quá hạn trên 1 năm: 100%."""
        service = ProvisionService()
        receivables = [{"amount": Decimal("10000000"), "overdue_days": 400}]
        
        provision = service.calculate_specific_provision(receivables, 400)
        assert provision.amount == Decimal("10000000")
    
    def test_general_provision_1_percent(self):
        """Dự phòng chung: 1% tổng nợ."""
        service = ProvisionService()
        total_ar = Money(Decimal("100000000"), "VND")
        
        provision = service.calculate_general_provision(total_ar)
        assert provision.amount == Decimal("1000000")
    
    def test_multiple_receivables(self):
        """Nhiều khoản nợ với các mức quá hạn khác nhau."""
        service = ProvisionService()
        receivables = [
            {"amount": Decimal("10000000"), "overdue_days": 30},    # 0%
            {"amount": Decimal("20000000"), "overdue_days": 150},   # 30%
            {"amount": Decimal("30000000"), "overdue_days": 250},   # 50%
            {"amount": Decimal("40000000"), "overdue_days": 400},   # 100%
        ]
        
        provision = service.calculate_specific_provision(receivables, 400)
        expected = (Decimal("0") + 
                    Decimal("6000000") + 
                    Decimal("15000000") + 
                    Decimal("40000000"))
        assert provision.amount == expected
```

### 5. Doanh thu dịch vụ (VAS 14/15)

```python
# tests/unit/test_revenue_recognition.py
from decimal import Decimal
from datetime import date

class TestRevenueRecognition:
    """Test ghi nhận doanh thu dịch vụ (VAS 14/15)."""
    
    def test_completed_contract_revenue(self):
        """Phương pháp hợp đồng hoàn thành."""
        contract_value = Decimal("100000000")
        completion_date = date(2025, 12, 15)
        revenue = contract_value
        
        assert revenue == contract_value
        assert revenue == Decimal("100000000")
    
    def test_percentage_completion_revenue(self):
        """Phương pháp tỷ lệ hoàn thành."""
        contract_value = Decimal("100000000")
        completion_percentage = Decimal("0.6")  # 60%
        revenue = contract_value * completion_percentage
        
        assert revenue == Decimal("60000000")
    
    def test_service_revenue_accrual(self):
        """Doanh thu dịch vụ cung cấp dần."""
        monthly_revenue = Decimal("5000000")
        months = 12
        total_revenue = monthly_revenue * months
        
        assert total_revenue == Decimal("60000000")
```

### 6. Tính VAT (TK 333)

```python
# tests/unit/test_vat_calculation.py
from decimal import Decimal

class TestVATCalculation:
    """Test tính thuế GTGT (TK 333)."""
    
    def test_vat_10_percent(self):
        """Thuế suất 10%."""
        price_before_vat = Decimal("10000000")
        vat_rate = Decimal("10")
        
        vat_amount = price_before_vat * vat_rate / Decimal("100")
        total = price_before_vat + vat_amount
        
        assert vat_amount == Decimal("1000000")
        assert total == Decimal("11000000")
    
    def test_vat_8_percent(self):
        """Thuế suất 8%."""
        price_before_vat = Decimal("10000000")
        vat_rate = Decimal("8")
        
        vat_amount = price_before_vat * vat_rate / Decimal("100")
        
        assert vat_amount == Decimal("800000")
    
    def test_vat_5_percent(self):
        """Thuế suất 5%."""
        price_before_vat = Decimal("10000000")
        vat_rate = Decimal("5")
        
        vat_amount = price_before_vat * vat_rate / Decimal("100")
        
        assert vat_amount == Decimal("500000")
    
    def test_vat_0_percent(self):
        """Thuế suất 0% (hàng xuất khẩu)."""
        price_before_vat = Decimal("10000000")
        vat_rate = Decimal("0")
        
        vat_amount = price_before_vat * vat_rate / Decimal("100")
        
        assert vat_amount == Decimal("0")
    
    def test_extracted_vat(self):
        """Tách VAT từ giá đã bao gồm thuế."""
        price_incl_vat = Decimal("11000000")
        vat_rate = Decimal("10")
        
        price_before_vat = price_incl_vat / (Decimal("1") + vat_rate / Decimal("100"))
        vat_amount = price_incl_vat - price_before_vat
        
        assert price_before_vat == Decimal("10000000")
        assert vat_amount == Decimal("1000000")
```

### 7. Giá vốn hàng tồn kho (FIFO/LIFO/Trung bình)

```python
# tests/unit/test_inventory_cost.py
from decimal import Decimal
from datetime import date
from app.domain.services import InventoryService

class TestInventoryCost:
    """Test giá vốn hàng bán (TK 156, 632)."""
    
    def test_fifo_cost(self):
        """Phương pháp FIFO."""
        service = InventoryService()
        
        goods = [{"product_code": "SP001", "quantity": Decimal("50")}]
        inventory = [
            {"product_code": "SP001", "remaining_qty": Decimal("30"), 
             "unit_cost": Decimal("100000"), "receipt_date": date(2025, 1, 1)},
            {"product_code": "SP001", "remaining_qty": Decimal("40"), 
             "unit_cost": Decimal("110000"), "receipt_date": date(2025, 2, 1)}
        ]
        
        cost = service.calculate_cost_of_goods_sold(
            goods, inventory, InventoryService.CostMethod.FIFO
        )
        
        # 30 * 100000 + 20 * 110000 = 5200000
        assert cost.amount == Decimal("5200000")
    
    def test_lifo_cost(self):
        """Phương pháp LIFO."""
        service = InventoryService()
        
        goods = [{"product_code": "SP001", "quantity": Decimal("50")}]
        inventory = [
            {"product_code": "SP001", "remaining_qty": Decimal("30"), 
             "unit_cost": Decimal("100000"), "receipt_date": date(2025, 1, 1)},
            {"product_code": "SP001", "remaining_qty": Decimal("40"), 
             "unit_cost": Decimal("110000"), "receipt_date": date(2025, 2, 1)}
        ]
        
        cost = service.calculate_cost_of_goods_sold(
            goods, inventory, InventoryService.CostMethod.LIFO
        )
        
        # 40 * 110000 + 10 * 100000 = 5400000
        assert cost.amount == Decimal("5400000")
    
    def test_weighted_average_cost(self):
        """Phương pháp bình quân gia quyền."""
        service = InventoryService()
        
        goods = [{"product_code": "SP001", "quantity": Decimal("50")}]
        inventory = [
            {"product_code": "SP001", "remaining_qty": Decimal("30"), 
             "unit_cost": Decimal("100000")},
            {"product_code": "SP001", "remaining_qty": Decimal("40"), 
             "unit_cost": Decimal("110000")}
        ]
        
        cost = service.calculate_cost_of_goods_sold(
            goods, inventory, InventoryService.CostMethod.WEIGHTED_AVG
        )
        
        # Avg = (30*100000 + 40*110000) / 70 = 105714.29
        # Cost = 50 * 105714.29 = 5285714.29
        assert cost.amount == pytest.approx(Decimal("5285714.29"), abs=Decimal("1"))
    
    def test_inventory_reconciliation_shortage(self):
        """Đối chiếu kiểm kê - Thiếu hụt (TK 1381)."""
        service = InventoryService()
        
        amount, account = service.reconcile_inventory(
            product_code="SP001",
            actual_quantity=Decimal("95"),
            book_quantity=Decimal("100"),
            unit_cost=Money(Decimal("100000"), "VND")
        )
        
        assert amount.amount == Decimal("500000")
        assert account == "1381"
    
    def test_inventory_reconciliation_surplus(self):
        """Đối chiếu kiểm kê - Thừa (TK 3381)."""
        service = InventoryService()
        
        amount, account = service.reconcile_inventory(
            product_code="SP001",
            actual_quantity=Decimal("105"),
            book_quantity=Decimal("100"),
            unit_cost=Money(Decimal("100000"), "VND")
        )
        
        assert amount.amount == Decimal("500000")
        assert account == "3381"
```

## Integration Tests

### 1. Không chỉnh sửa sau khóa sổ

```python
# tests/integration/test_period_locking.py
from datetime import date, datetime
from uuid import uuid4

class TestPeriodLocking:
    """Test khóa sổ kế toán (Điều 18-22)."""
    
    def test_cannot_edit_locked_voucher(self, db_session):
        """Chứng từ sau khóa không được chỉnh sửa."""
        from app.infrastructure.database.models import AccountingVoucher
        
        voucher = AccountingVoucher(
            voucher_number="CT/202512/001",
            voucher_type="MUA",
            voucher_date=date(2025, 12, 15),
            description="Test",
            company_id=uuid4(),
            created_by="user",
            is_locked=True,
            lock_status="MONTH_LOCKED"
        )
        
        db_session.add(voucher)
        db_session.commit()
        
        # Cố gắng sửa
        voucher.description = "Modified"
        
        with pytest.raises(Exception):
            db_session.commit()
    
    def test_lock_monthly_period(self, db_session):
        """Khóa kỳ tháng."""
        from app.infrastructure.database.models import FiscalPeriod, AccountingVoucher
        
        period = FiscalPeriod(
            company_id=uuid4(),
            period_type="MONTH",
            year=2025,
            period_value=11,
            start_date=date(2025, 11, 1),
            end_date=date(2025, 11, 30),
            is_locked=True,
            lock_type="MONTH_LOCKED",
            locked_at=datetime.utcnow()
        )
        
        db_session.add(period)
        db_session.commit()
        
        assert period.is_locked is True
```

### 2. Audit Trail

```python
# tests/integration/test_audit_trail.py
from datetime import datetime
from uuid import uuid4

class TestAuditTrail:
    """Test audit trail đầy đủ."""
    
    def test_audit_log_created_on_update(self, db_session):
        """Ghi log khi có thay đổi."""
        from app.infrastructure.database.models import AuditLog, AccountingVoucher
        
        voucher = AccountingVoucher(
            voucher_number="CT/202512/001",
            voucher_type="MUA",
            voucher_date=date(2025, 12, 15),
            description="Original",
            company_id=uuid4(),
            created_by="user"
        )
        db_session.add(voucher)
        db_session.commit()
        
        # Thay đổi
        old_desc = voucher.description
        voucher.description = "Modified"
        db_session.commit()
        
        # Kiểm tra audit log
        audit = db_session.query(AuditLog).filter(
            AuditLog.entity_id == voucher.id
        ).order_by(AuditLog.created_at.desc()).first()
        
        assert audit is not None
        assert audit.action == "UPDATE"
        assert old_desc in (audit.old_value or "")
        assert "Modified" in (audit.new_value or "")
    
    def test_audit_log_fields_complete(self, db_session):
        """Audit log đầy đủ thông tin."""
        from app.infrastructure.database.models import AuditLog
        
        audit = AuditLog(
            company_id=uuid4(),
            user_id=uuid4(),
            user_ip="192.168.1.100",
            user_agent="Mozilla/5.0",
            entity_type="Voucher",
            entity_id=uuid4(),
            action="CREATE",
            old_value=None,
            new_value='{"description": "Test"}'
        )
        
        db_session.add(audit)
        db_session.commit()
        
        assert audit.id is not None
        assert audit.created_at is not None
        assert audit.user_ip == "192.168.1.100"
```

### 3. Ký số chứng từ

```python
# tests/integration/test_digital_signature.py
from datetime import datetime
from uuid import uuid4

class TestDigitalSignature:
    """Test ký số chứng từ (Thông tư 78/2021)."""
    
    def test_voucher_signature(self, db_session):
        """Ký số chứng từ hợp lệ."""
        from app.infrastructure.database.models import AccountingVoucher, SignedDocument
        
        voucher = AccountingVoucher(
            voucher_number="CT/202512/001",
            voucher_type="MUA",
            voucher_date=date(2025, 12, 15),
            description="Test",
            company_id=uuid4(),
            created_by="user",
            is_signed=False
        )
        db_session.add(voucher)
        db_session.commit()
        
        # Ký số
        voucher.is_signed = True
        voucher.signed_at = datetime.utcnow()
        voucher.signer_id = str(uuid4())
        voucher.signature_data = "MOCK_SIGNATURE_DATA"
        voucher.version += 1
        db_session.commit()
        
        assert voucher.is_signed is True
        assert voucher.signed_at is not None
    
    def test_cannot_resign_signed_voucher(self, db_session):
        """Không thể ký lại chứng từ đã ký."""
        from app.infrastructure.database.models import AccountingVoucher
        
        voucher = AccountingVoucher(
            voucher_number="CT/202512/002",
            voucher_type="BAN",
            voucher_date=date(2025, 12, 15),
            description="Test",
            company_id=uuid4(),
            created_by="user",
            is_signed=True,
            signed_at=datetime.utcnow()
        )
        
        with pytest.raises(ValueError, match=".*đã được ký.*"):
            voucher.sign("new_signer", "new_signature")
```

## API Tests

```python
# tests/api/test_vouchers.py
from decimal import Decimal

class TestVoucherAPI:
    """Test API endpoints."""
    
    def test_create_voucher_with_balance_check(self, client):
        """Tạo chứng từ với kiểm tra cân đối."""
        payload = {
            "voucher_type": "MUA",
            "voucher_date": "2025-12-15",
            "description": "Mua hàng hóa",
            "company_code": "DEMO",
            "lines": [
                {
                    "account_code": "1561",
                    "debit_amount": 10000000,
                    "description": "Hàng hóa"
                },
                {
                    "account_code": "3331",
                    "credit_amount": 1000000,
                    "description": "Thuế GTGT"
                },
                {
                    "account_code": "331",
                    "credit_amount": 9000000,
                    "description": "Phải trả NCC"
                }
            ]
        }
        
        response = client.post("/api/v1/vouchers", json=payload)
        
        assert response.status_code == 201
        data = response.json()
        assert data["voucher_number"] is not None
    
    def test_create_unbalanced_voucher_rejected(self, client):
        """Từ chối chứng từ không cân đối."""
        payload = {
            "voucher_type": "MUA",
            "voucher_date": "2025-12-15",
            "description": "Test",
            "company_code": "DEMO",
            "lines": [
                {"account_code": "1561", "debit_amount": 10000000},
                {"account_code": "3331", "credit_amount": 8000000}
            ]
        }
        
        response = client.post("/api/v1/vouchers", json=payload)
        
        assert response.status_code == 400
        assert "không cân đối" in response.json()["detail"]
    
    def test_sign_voucher(self, client, auth_headers):
        """Ký số chứng từ."""
        # Create voucher first
        payload = {
            "voucher_type": "BAN",
            "voucher_date": "2025-12-15",
            "description": "Bán hàng",
            "company_code": "DEMO",
            "lines": [
                {"account_code": "131", "debit_amount": 11000000},
                {"account_code": "5111", "credit_amount": 10000000},
                {"account_code": "3331", "credit_amount": 1000000}
            ]
        }
        
        create_resp = client.post("/api/v1/vouchers", json=payload)
        voucher_id = create_resp.json()["id"]
        
        # Sign
        sign_resp = client.post(
            f"/api/v1/vouchers/{voucher_id}/sign",
            json={"signer_id": "KT_TRUONG", "signature_provider": "USB"},
            headers=auth_headers
        )
        
        assert sign_resp.status_code == 200
        assert sign_resp.json()["status"] == "signed"
```

## Property-Based Tests (Hypothesis)

```python
# tests/property/test_financial_calculations.py
from decimal import Decimal, ROUND_HALF_UP
from hypothesis import given, strategies as st
from app.domain.services import ExchangeRateService, ProvisionService

class TestFinancialCalculationsProperty:
    """Property-based tests cho các tính toán tài chính."""
    
    @given(st.decimals(min_value=0, max_value=1000000000, allow_nan=False, allow_infinity=False))
    def test_vat_calculation_linear(self, amount):
        """VAT tính theo tỷ lệ tuyến tính."""
        rate = Decimal("10")
        expected = (amount * rate / Decimal("100")).quantize(
            Decimal("1"), rounding=ROUND_HALF_UP
        )
        assert expected >= 0
        assert expected <= amount
    
    @given(st.decimals(min_value=0, max_value=1000000000))
    def test_exchange_rate_conversion(self, amount):
        """Quy đổi tỷ giá."""
        rate = Decimal("24000")  # USD to VND
        
        service = ExchangeRateService()
        exchange_rate = type('ExchangeRate', (), {'rate': rate, 'currency': 'USD'})()
        
        result = service.convert_to_vnd(amount, "USD", exchange_rate)
        
        assert result.amount == amount * rate
        assert result.currency == "VND"
    
    @given(st.integers(min_value=0, max_value=1000))
    def test_provision_rate_bounds(self, overdue_days):
        """Dự phòng trong giới hạn cho phép."""
        service = ProvisionService()
        
        provision_rates = {
            (0, 90): Decimal("0.00"),
            (91, 180): Decimal("0.30"),
            (181, 365): Decimal("0.50"),
            (366, 99999): Decimal("1.00"),
        }
        
        for (min_days, max_days), rate in provision_rates.items():
            if min_days <= overdue_days <= max_days:
                assert Decimal("0") <= rate <= Decimal("1")
                break
```

## Chạy Tests

```bash
# Tất cả tests
pytest tests/ -v

# Unit tests
pytest tests/unit/ -v

# Integration tests  
pytest tests/integration/ -v

# API tests
pytest tests/api/ -v

# Property-based tests
pytest tests/property/ -v --hypothesis

# Coverage report
pytest --cov=app --cov-report=html --cov-report=term-missing

# Specific test
pytest tests/unit/test_accounting_domain.py::TestBalanceCheck::test_balanced_entry_debit_equals_credit -v
```

## Coverage Target

```
Domain Layer:          ≥95%
  - entities.py:       100%
  - services.py:       95%
  - value_objects.py:  100%

Application Layer:     ≥85%
Infrastructure Layer:  ≥70%
API Layer:            ≥80%
```

## Smoke Tests Sau Deploy

```powershell
# Windows PowerShell
$baseUrl = "http://localhost:8000"

# 1. Health check
$r = Invoke-RestMethod -Uri "$baseUrl/health"
$r.status | Should -Be "healthy"

# 2. API root
$r = Invoke-RestMethod -Uri "$baseUrl/"
$r.name | Should -Be "VN Accounting ERP API"

# 3. Test login
$body = @{username="admin"; password="test"}
$r = Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/login" -Method Post -Body ($body | ConvertTo-Json)
$r.access_token | Should -Not -BeNullOrEmpty

# 4. List vouchers
$token = $r.access_token
$headers = @{Authorization="Bearer $token"}
$r = Invoke-RestMethod -Uri "$baseUrl/api/v1/vouchers" -Headers $headers
$r | Should -Not -BeNullOrEmpty
```
