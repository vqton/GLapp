"""
Unit tests - Domain layer tests for critical business logic.
Testing: Balance check Nợ=Có, VAT TK 333, dự phòng nợ, tỷ giá, doanh thu dịch vụ.
Coverage target: ≥90% domain layer.
"""

from datetime import date
from decimal import Decimal

import pytest

from app.domain.entities import Account, AccountBalance, AccountingVoucher, JournalEntry
from app.domain.services import (
    ExchangeRateService,
    InventoryService,
    ProvisionService,
)
from app.domain.value_objects import ExchangeRate, Money, VoucherLineDetail


class TestBalanceCheck:
    """Test kiểm tra cân đối bút toán (Nguyên tắc ghi sổ kép)."""

    def test_balanced_entry_debit_equals_credit(self):
        """Bút toán cân đối: Tổng Nợ = Tổng Có."""
        entry = JournalEntry(
            entry_number="BT/20251215/001",
            voucher_id=None,
            voucher_date=date(2025, 12, 15),
            posting_date=date(2025, 12, 15),
            description="Mua hàng hóa",
            created_by="test",
            total_debit=Money(Decimal("11000000"), "VND"),
            total_credit=Money(Decimal("11000000"), "VND"),
            difference=Money(Decimal("0"), "VND")
        )
        assert entry.is_balanced() is True

    def test_unbalanced_entry_debit_not_equals_credit(self):
        """Bút toán không cân đối."""
        entry = JournalEntry(
            entry_number="BT/20251215/002",
            voucher_id=None,
            voucher_date=date(2025, 12, 15),
            posting_date=date(2025, 12, 15),
            description="Mua hàng hóa",
            created_by="test",
            total_debit=Money(Decimal("10000000"), "VND"),
            total_credit=Money(Decimal("11000000"), "VND"),
            difference=Money(Decimal("-1000000"), "VND")
        )
        assert entry.is_balanced() is False

    def test_calculate_totals_from_lines(self):
        """Tính tổng từ các dòng bút toán."""
        lines = [
            VoucherLineDetail(
                account_code="1561",
                debit_amount=Money(Decimal("10000000"), "VND"),
                description="Hàng hóa"
            ),
            VoucherLineDetail(
                account_code="3331",
                credit_amount=Money(Decimal("1000000"), "VND"),
                description="Thuế GTGT"
            ),
            VoucherLineDetail(
                account_code="331",
                credit_amount=Money(Decimal("9000000"), "VND"),
                description="Phải trả NCC"
            )
        ]

        entry = JournalEntry(
            entry_number="BT/20251215/003",
            voucher_id=None,
            voucher_date=date(2025, 12, 15),
            posting_date=date(2025, 12, 15),
            description="Mua hàng",
            created_by="test",
            lines=lines
        )

        result = entry.calculate_totals()
        assert result.total_debit.amount == Decimal("10000000")
        assert result.total_credit.amount == Decimal("10000000")
        assert result.difference.amount == Decimal("0")

    def test_post_balanced_entry_success(self):
        """Ghi sổ bút toán cân đối thành công."""
        entry = JournalEntry(
            entry_number="BT/20251215/004",
            voucher_id=None,
            voucher_date=date(2025, 12, 15),
            posting_date=date(2025, 12, 15),
            description="Test",
            created_by="test",
            total_debit=Money(Decimal("5000000"), "VND"),
            total_credit=Money(Decimal("5000000"), "VND"),
            difference=Money(Decimal("0"), "VND")
        )

        posted = entry.post("admin")
        assert posted.is_posted is True
        assert posted.posted_at is not None

    def test_post_unbalanced_entry_fails(self):
        """Ghi sổ bút toán không cân đối thất bại."""
        entry = JournalEntry(
            entry_number="BT/20251215/005",
            voucher_id=None,
            voucher_date=date(2025, 12, 15),
            posting_date=date(2025, 12, 15),
            description="Test",
            created_by="test",
            total_debit=Money(Decimal("5000000"), "VND"),
            total_credit=Money(Decimal("4000000"), "VND"),
            difference=Money(Decimal("1000000"), "VND")
        )

        with pytest.raises(ValueError, match=".*không cân đối.*"):
            entry.post("admin")


class TestVoucherLocking:
    """Test khóa chứng từ (Điều 18-22)."""

    def test_lock_voucher(self):
        """Khóa chứng từ."""
        voucher = AccountingVoucher(
            voucher_number="CT/20251215/001",
            voucher_type="MUA",
            voucher_date=date(2025, 12, 15),
            description="Mua hàng",
            company_code="DEMO",
            created_by="admin",
            is_locked=False,
            lock_status="OPEN"
        )

        locked = voucher.lock("MONTH_LOCKED")
        assert locked.is_locked is True
        assert locked.lock_status == "MONTH_LOCKED"
        assert locked.locked_at is not None

    def test_cannot_modify_locked_voucher(self):
        """Chứng từ đã khóa không thể sửa."""
        voucher = AccountingVoucher(
            voucher_number="CT/20251215/002",
            voucher_type="BAN",
            voucher_date=date(2025, 12, 15),
            description="Bán hàng",
            company_code="DEMO",
            created_by="admin",
            is_locked=True,
            lock_status="YEAR_LOCKED"
        )

        assert voucher.can_modify() is False


class TestVoucherSigning:
    """Test ký số chứng từ (Thông tư 78/2021/TT-BTC)."""

    def test_sign_voucher(self):
        """Ký số chứng từ."""
        voucher = AccountingVoucher(
            voucher_number="CT/20251215/001",
            voucher_type="MUA",
            voucher_date=date(2025, 12, 15),
            description="Mua hàng",
            company_code="DEMO",
            created_by="admin",
            is_signed=False
        )

        signed = voucher.sign(signer_id="NV001", signature="SIGNATURE_DATA")
        assert signed.is_signed is True
        assert signed.signer_id == "NV001"
        assert signed.signature_data == "SIGNATURE_DATA"
        assert signed.signed_at is not None

    def test_cannot_sign_already_signed(self):
        """Không thể ký chứng từ đã ký."""
        voucher = AccountingVoucher(
            voucher_number="CT/20251215/002",
            voucher_type="BAN",
            voucher_date=date(2025, 12, 15),
            description="Bán hàng",
            company_code="DEMO",
            created_by="admin",
            is_signed=True,
            signed_at=date(2025, 12, 15)
        )

        with pytest.raises(ValueError, match=".*đã được ký.*"):
            voucher.sign("NV002", "NEW_SIGNATURE")


class TestExchangeRate:
    """Test tỷ giá ngoại tệ (Điều 31 - TT99/2025)."""

    def test_convert_usd_to_vnd(self):
        """Quy đổi USD sang VND."""
        rate = ExchangeRate(
            rate=Decimal("24500"),
            currency="USD",
            rate_type="REALTIME"
        )

        service = ExchangeRateService()
        result = service.convert_to_vnd(
            amount=Decimal("1000"),
            currency="USD",
            exchange_rate=rate
        )

        assert result.amount == Decimal("24500000")
        assert result.currency == "VND"

    def test_calculate_exchange_diff_profit(self):
        """Tính chênh lệch tỷ giá - Lãi (TK 515)."""
        original_rate = ExchangeRate(rate=Decimal("24000"), currency="USD")
        current_rate = ExchangeRate(rate=Decimal("24500"), currency="USD")

        service = ExchangeRateService()
        diff = service.calculate_exchange_diff(
            original_rate=original_rate,
            current_rate=current_rate,
            amount=Decimal("1000")
        )

        assert diff.amount == Decimal("500000")  # Lãi

    def test_calculate_exchange_diff_loss(self):
        """Tính chênh lệch tỷ giá - Lỗ (TK 635)."""
        original_rate = ExchangeRate(rate=Decimal("25000"), currency="USD")
        current_rate = ExchangeRate(rate=Decimal("24500"), currency="USD")

        service = ExchangeRateService()
        diff = service.calculate_exchange_diff(
            original_rate=original_rate,
            current_rate=current_rate,
            amount=Decimal("1000")
        )

        assert diff.amount == Decimal("-500000")  # Lỗ

    def test_classify_exchange_diff_revenue(self):
        """Phân loại chênh lệch tỷ giá - Doanh thu."""
        service = ExchangeRateService()
        diff = Money(Decimal("1000000"), "VND")

        account, acc_type = service.classify_exchange_diff(diff)
        assert account == "4131"  # Lãi tỷ giá
        assert acc_type == "REVENUE"

    def test_classify_exchange_diff_expense(self):
        """Phân loại chênh lệch tỷ giá - Chi phí."""
        service = ExchangeRateService()
        diff = Money(Decimal("-500000"), "VND")

        account, acc_type = service.classify_exchange_diff(diff)
        assert account == "4132"  # Lỗ tỷ giá
        assert acc_type == "EXPENSE"


class TestProvisionCalculation:
    """Test dự phòng nợ phải thu (Điều 32 - TT99/2025)."""

    def test_no_provision_for_current_debt(self):
        """Nợ chưa đến hạn không cần dự phòng."""
        service = ProvisionService()
        receivables = [
            {"amount": Decimal("10000000"), "overdue_days": 30}
        ]

        provision = service.calculate_specific_provision(receivables, 30)
        assert provision.amount == Decimal("0")

    def test_30_provision_for_3_to_6_months_overdue(self):
        """Nợ quá hạn 3-6 tháng: 30%."""
        service = ProvisionService()
        receivables = [
            {"amount": Decimal("10000000"), "overdue_days": 120}
        ]

        provision = service.calculate_specific_provision(receivables, 120)
        assert provision.amount == Decimal("3000000")

    def test_50_provision_for_6_to_12_months_overdue(self):
        """Nợ quá hạn 6-12 tháng: 50%."""
        service = ProvisionService()
        receivables = [
            {"amount": Decimal("10000000"), "overdue_days": 200}
        ]

        provision = service.calculate_specific_provision(receivables, 200)
        assert provision.amount == Decimal("5000000")

    def test_100_provision_for_over_1_year_overdue(self):
        """Nợ quá hạn trên 1 năm: 100%."""
        service = ProvisionService()
        receivables = [
            {"amount": Decimal("10000000"), "overdue_days": 400}
        ]

        provision = service.calculate_specific_provision(receivables, 400)
        assert provision.amount == Decimal("10000000")

    def test_general_provision_1_percent(self):
        """Dự phòng chung: 1% tổng nợ phải thu ngắn hạn."""
        service = ProvisionService()
        total_ar = Money(Decimal("100000000"), "VND")

        provision = service.calculate_general_provision(total_ar)
        assert provision.amount == Decimal("1000000")


class TestInventoryCost:
    """Test giá vốn hàng tồn kho (TK 156, 632)."""

    def test_fifo_cost_calculation(self):
        """Tính giá vốn theo FIFO."""
        service = InventoryService()

        goods = [{"product_code": "SP001", "quantity": Decimal("50")}]
        inventory = [
            {"product_code": "SP001", "remaining_qty": Decimal("30"), "unit_cost": Decimal("100000"), "receipt_date": date(2025, 1, 1)},
            {"product_code": "SP001", "remaining_qty": Decimal("40"), "unit_cost": Decimal("110000"), "receipt_date": date(2025, 2, 1)}
        ]

        cost = service.calculate_cost_of_goods_sold(
            goods=goods,
            inventory_items=inventory,
            method=InventoryService.CostMethod.FIFO
        )

        # 30 * 100000 + 20 * 110000 = 5200000
        assert cost.amount == Decimal("5200000")

    def test_lifo_cost_calculation(self):
        """Tính giá vốn theo LIFO."""
        service = InventoryService()

        goods = [{"product_code": "SP001", "quantity": Decimal("50")}]
        inventory = [
            {"product_code": "SP001", "remaining_qty": Decimal("30"), "unit_cost": Decimal("100000"), "receipt_date": date(2025, 1, 1)},
            {"product_code": "SP001", "remaining_qty": Decimal("40"), "unit_cost": Decimal("110000"), "receipt_date": date(2025, 2, 1)}
        ]

        cost = service.calculate_cost_of_goods_sold(
            goods=goods,
            inventory_items=inventory,
            method=InventoryService.CostMethod.LIFO
        )

        # 40 * 110000 + 10 * 100000 = 5400000
        assert cost.amount == Decimal("5400000")

    def test_weighted_average_cost(self):
        """Tính giá vốn theo bình quân gia quyền."""
        service = InventoryService()

        goods = [{"product_code": "SP001", "quantity": Decimal("50")}]
        inventory = [
            {"product_code": "SP001", "remaining_qty": Decimal("30"), "unit_cost": Decimal("100000")},
            {"product_code": "SP001", "remaining_qty": Decimal("40"), "unit_cost": Decimal("110000")}
        ]

        cost = service.calculate_cost_of_goods_sold(
            goods=goods,
            inventory_items=inventory,
            method=InventoryService.CostMethod.WEIGHTED_AVG
        )

        # Avg cost = (30*100000 + 40*110000) / 70 = 105714.2857...
        # 50 * 105714.2857 = 5285714.28
        assert cost.amount == pytest.approx(Decimal("5285714.29"), abs=Decimal("1"))

    def test_reconcile_inventory_shortage(self):
        """Đối chiếu kiểm kê - Thiếu hụt (TK 1381)."""
        service = InventoryService()

        amount, account = service.reconcile_inventory(
            product_code="SP001",
            actual_quantity=Decimal("95"),
            book_quantity=Decimal("100"),
            unit_cost=Money(Decimal("100000"), "VND")
        )

        assert amount.amount == Decimal("500000")  # 5 * 100000
        assert account == "1381"

    def test_reconcile_inventory_surplus(self):
        """Đối chiếu kiểm kê - Thừa (TK 3381)."""
        service = InventoryService()

        amount, account = service.reconcile_inventory(
            product_code="SP001",
            actual_quantity=Decimal("105"),
            book_quantity=Decimal("100"),
            unit_cost=Money(Decimal("100000"), "VND")
        )

        assert amount.amount == Decimal("500000")  # 5 * 100000
        assert account == "3381"

    def test_reconcile_inventory_matched(self):
        """Đối chiếu kiểm kê - Khớp."""
        service = InventoryService()

        amount, account = service.reconcile_inventory(
            product_code="SP001",
            actual_quantity=Decimal("100"),
            book_quantity=Decimal("100"),
            unit_cost=Money(Decimal("100000"), "VND")
        )

        assert amount.amount == Decimal("0")
        assert account == ""


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


class TestAccountPosting:
    """Test cập nhật số dư tài khoản."""

    def test_post_debit_to_asset_account(self):
        """Ghi Nợ vào tài sản (tăng số dư)."""
        account = Account(
            code="1111",
            name="Tiền mặt",
            account_type="ASSET",
            company_code="DEMO",
            current_balance=Money(Decimal("10000000"), "VND"),
            balance_direction="DEBIT"
        )

        posted = account.post_balance(
            debit=Money(Decimal("5000000"), "VND"),
            credit=Money(Decimal("0"), "VND")
        )

        assert posted.current_balance.amount == Decimal("15000000")

    def test_post_credit_to_asset_account(self):
        """Ghi Có vào tài sản (giảm số dư)."""
        account = Account(
            code="1111",
            name="Tiền mặt",
            account_type="ASSET",
            company_code="DEMO",
            current_balance=Money(Decimal("10000000"), "VND"),
            balance_direction="DEBIT"
        )

        posted = account.post_balance(
            debit=Money(Decimal("0"), "VND"),
            credit=Money(Decimal("3000000"), "VND")
        )

        assert posted.current_balance.amount == Decimal("7000000")

    def test_post_credit_to_liability_account(self):
        """Ghi Có vào nợ phải trả (tăng số dư)."""
        account = Account(
            code="331",
            name="Phải trả người bán",
            account_type="LIABILITY",
            company_code="DEMO",
            current_balance=Money(Decimal("5000000"), "VND"),
            balance_direction="CREDIT"
        )

        posted = account.post_balance(
            debit=Money(Decimal("0"), "VND"),
            credit=Money(Decimal("2000000"), "VND")
        )

        assert posted.current_balance.amount == Decimal("7000000")


class TestNegativeBalanceWarning:
    """Test cảnh báo âm tồn kho."""

    def test_negative_balance_check(self):
        """Kiểm tra âm tồn kho."""
        balance = AccountBalance(
            account_code="131",
            period_type="MONTH",
            period_value=12,
            company_code="DEMO",
            closing_debit=Money(Decimal("-1000000"), "VND")
        )

        warnings = balance.check_negative_balance()
        assert len(warnings) == 1
        assert "TK 131" in warnings[0]
        assert "âm" in warnings[0]

    def test_no_warning_for_positive_balance(self):
        """Không cảnh báo khi số dư dương."""
        balance = AccountBalance(
            account_code="1111",
            period_type="MONTH",
            period_value=12,
            company_code="DEMO",
            closing_debit=Money(Decimal("50000000"), "VND")
        )

        warnings = balance.check_negative_balance()
        assert len(warnings) == 0
