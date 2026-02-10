"""
Domain Services - Business logic that operates on multiple entities.
Áp dụng nghiệp vụ kế toán theo Thông tư 99/2025/TT-BTC.
"""

from abc import ABC, abstractmethod
from datetime import date
from decimal import Decimal
from enum import Enum
from typing import Optional

from .entities import Account, AccountingVoucher, JournalEntry
from .value_objects import AccountCode, ExchangeRate, Money, VoucherLineDetail


class IAccountRepository(ABC):

    @abstractmethod
    def get_by_code(self, code: AccountCode) -> Account | None:
        ...

    @abstractmethod
    def save(self, account: Account) -> Account:
        ...

    @abstractmethod
    def get_by_pattern(self, pattern: str) -> list[Account]:
        ...

    @abstractmethod
    def list_by_type(self, account_type: str) -> list[Account]:
        ...


class IJournalEntryRepository(ABC):

    @abstractmethod
    def save(self, entry: JournalEntry) -> JournalEntry:
        ...

    @abstractmethod
    def get_by_voucher(self, voucher_id: str) -> list[JournalEntry]:
        ...

    @abstractmethod
    def get_by_period(self, start_date: date, end_date: date) -> list[JournalEntry]:
        ...

    @abstractmethod
    def get_by_account(self, account_code: AccountCode) -> list[JournalEntry]:
        ...


class AccountingBalanceService:
    """
    Service - Kiểm tra cân đối bút toán (Nguyên tắc ghi sổ kép).
    Điều 18-22, Phụ lục III - TT99/2025.
    """

    def __init__(
        self,
        account_repo: IAccountRepository,
        journal_repo: IJournalEntryRepository
    ):
        self.account_repo = account_repo
        self.journal_repo = journal_repo

    def validate_voucher_balance(self, voucher: AccountingVoucher) -> tuple[bool, list[str]]:
        """
        Kiểm tra chứng từ có cân đối không.
        Yêu cầu: Tổng Nợ = Tổng Có.
        """
        entries = self.journal_repo.get_by_voucher(str(voucher.id))
        errors = []

        for entry in entries:
            if not entry.is_balanced():
                errors.append(
                    f"Bút toán {entry.entry_number}: Tổng Nợ ({entry.total_debit}) "
                    f"!= Tổng Có ({entry.total_credit})"
                )

        return len(errors) == 0, errors

    def check_negative_balances(self, company_code: str, period: date) -> list[str]:
        """
        Kiểm tra âm tồn kho các tài khoản.
        Cảnh báo cho TK 111, 112, 131, 138, 15x, 21x, 31x.
        """
        warnings = []

        critical_accounts = [
            "111", "112",
            "131", "138",
            "151", "152", "156", "157",
            "211", "213",
            "311", "331",
        ]

        for code in critical_accounts:
            account = self.account_repo.get_by_code(AccountCode(code))
            if account and account.current_balance:
                if account.current_balance.amount < 0:
                    warnings.append(
                        f"Cảnh báo: TK {code} ({account.name}) có số dư âm "
                        f"{account.current_balance.amount:,.0f}"
                    )

        return warnings


class VoucherPostingService:
    """
    Service - Ghi sổ chứng từ kế toán.
    Điều 18-22 - Ghi sổ kép kịp thời.
    """

    def __init__(self, journal_repo: IJournalEntryRepository):
        self.journal_repo = journal_repo

    def auto_suggest_entries(self, voucher: AccountingVoucher) -> list[VoucherLineDetail]:
        """
        Tự động gợi ý bút toán kép (Nợ = Có).
        """
        suggestions: list[VoucherLineDetail] = []
        return suggestions

    def _get_default_accounts(self) -> dict[str, AccountCode]:
        """Lấy tài khoản mặc định theo Phụ lục II."""
        return {
            "cash": AccountCode("1111"),
            "bank": AccountCode("1121"),
            "ar_trade": AccountCode("131"),
            "ap_trade": AccountCode("331"),
            "inventory": AccountCode("1561"),
            "revenue_sale": AccountCode("5111"),
            "cost_sale": AccountCode("632"),
            "vat_payable": AccountCode("3331"),
            "vat_receivable": AccountCode("1331"),
        }


class ExchangeRateService:
    """
    Service - Quản lý tỷ giá ngoại tệ (Điều 31 - TT99/2025).
    """

    def convert_to_vnd(
        self,
        amount: Decimal,
        currency: str,
        exchange_rate: ExchangeRate
    ) -> Money:
        vnd_amount = amount * exchange_rate.rate
        return Money(amount=vnd_amount, currency="VND")

    def calculate_exchange_diff(
        self,
        original_rate: ExchangeRate,
        current_rate: ExchangeRate,
        amount: Decimal
    ) -> Money:
        original_vnd = amount * original_rate.rate
        current_vnd = amount * current_rate.rate
        diff = current_vnd - original_vnd
        return Money(amount=diff, currency="VND")

    def classify_exchange_diff(self, diff: Money) -> tuple[str, str]:
        if diff.amount > 0:
            return ("4131", "REVENUE")
        else:
            return ("4132", "EXPENSE")


class ProvisionService:
    """
    Service - Tính dự phòng nợ phải thu (Điều 32 - TT99/2025).
    Theo Thông tư 48/2019/TT-BTC (TK 229).
    """

    def calculate_specific_provision(
        self,
        receivables: list[dict],
        overdue_days: int
    ) -> Money:
        """
        Tính dự phòng cụ thể theo thời gian quá hạn.
        """
        provision_rates = {
            (0, 90): Decimal("0.00"),
            (91, 180): Decimal("0.30"),
            (181, 365): Decimal("0.50"),
            (366, 99999): Decimal("1.00"),
        }

        total_provision = Money(Decimal("0"), "VND")

        for item in receivables:
            amount = item.get("amount", Decimal("0"))
            overdue = item.get("overdue_days", 0)

            rate = Decimal("0")
            for (days_min, days_max), r in provision_rates.items():
                if days_min <= overdue <= days_max:
                    rate = r
                    break

            provision_amount = amount * rate
            total_provision += Money(amount=provision_amount, currency="VND")

        return total_provision

    def calculate_general_provision(self, total_ar: Money) -> Money:
        rate = Decimal("0.01")
        return Money(amount=total_ar.amount * rate, currency="VND")


class InventoryService:
    """
    Service - Quản lý hàng tồn kho (TK 156).
    Áp dụng FIFO/LIFO/Trung bình gia quyền.
    """

    class CostMethod(Enum):
        FIFO = "FIFO"
        LIFO = "LIFO"
        WEIGHTED_AVG = "WEIGHTED_AVG"

    def calculate_cost_of_goods_sold(
        self,
        goods: list[dict],
        inventory_items: list[dict],
        method: Optional["InventoryService.CostMethod"] = None
    ) -> Money:
        if method is None:
            method = self.CostMethod.FIFO

        total_cost = Money(Decimal("0"), "VND")

        for item in goods:
            quantity = item.get("quantity", Decimal("0"))
            product_code = item.get("product_code")

            available = [
                inv for inv in inventory_items
                if inv["product_code"] == product_code
                and inv["remaining_qty"] > 0
            ]

            if method == self.CostMethod.FIFO:
                available.sort(key=lambda x: x["receipt_date"])
            elif method == self.CostMethod.LIFO:
                available.sort(key=lambda x: x["receipt_date"], reverse=True)
            elif method == self.CostMethod.WEIGHTED_AVG:
                return self._calculate_weighted_avg(goods, inventory_items)

            remaining_qty = quantity
            for lot in available:
                take_qty = min(remaining_qty, lot["remaining_qty"])
                total_cost += Money(amount=take_qty * lot["unit_cost"], currency="VND")
                remaining_qty -= take_qty

                if remaining_qty <= 0:
                    break

        return total_cost

    def _calculate_weighted_avg(
        self,
        goods: list[dict],
        inventory_items: list[dict]
    ) -> Money:
        total_cost = Money(Decimal("0"), "VND")
        total_qty = Decimal("0")
        total_value = Decimal("0")

        for inv in inventory_items:
            qty = inv.get("remaining_qty", Decimal("0"))
            cost = inv.get("unit_cost", Decimal("0"))
            total_qty += qty
            total_value += qty * cost

        avg_cost = total_value / total_qty if total_qty > 0 else Decimal("0")

        for item in goods:
            qty = item.get("quantity", Decimal("0"))
            total_cost += Money(amount=qty * avg_cost, currency="VND")

        return total_cost

    def reconcile_inventory(
        self,
        product_code: str,
        actual_quantity: Decimal,
        book_quantity: Decimal,
        unit_cost: Money
    ) -> tuple[Money, str]:
        difference = actual_quantity - book_quantity

        if difference < 0:
            amount = abs(difference) * unit_cost.amount
            return Money(amount=amount, currency="VND"), "1381"
        elif difference > 0:
            amount = difference * unit_cost.amount
            return Money(amount=amount, currency="VND"), "3381"
        else:
            return Money(Decimal("0"), "VND"), ""
