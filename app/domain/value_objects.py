"""
Domain Layer - Pure Python business logic following DDD.
Áp dụng nghiệp vụ kế toán theo Thông tư 99/2025/TT-BTC.
"""

from dataclasses import dataclass
from datetime import date
from decimal import Decimal
from enum import Enum
from typing import NewType

AccountCode = NewType("AccountCode", str)
VoucherNumber = NewType("VoucherNumber", str)


class AccountType(str, Enum):
    """Phân loại tài khoản theo Phụ lục II - TT99/2025."""
    ASSET = "ASSET"          # Tài sản (1xx)
    LIABILITY = "LIABILITY"  # Nợ phải trả (2xx)
    EQUITY = "EQUITY"        # Vốn chủ sở hữu (3xx)
    REVENUE = "REVENUE"      # Doanh thu (4xx)
    EXPENSE = "EXPENSE"      # Chi phí (5xx)
    DIRECT_COST = "DIRECT_COST"    # Giá vốn hàng bán (6xx)
    OTHER_REVENUE = "OTHER_REVENUE" # Thu nhập khác (7xx)
    OTHER_EXPENSE = "OTHER_EXPENSE" # Chi phí khác (8xx)


class VoucherType(str, Enum):
    """Loại chứng từ kế toán theo Phụ lục I - TT99/2025."""
    THU = "THU"      # Chứng từ thu tiền
    CHI = "CHI"      # Chứng từ chi tiền
    NKC = "NKC"      # Nhập khẩu hàng hóa
    XK = "XK"        # Xuất khẩu hàng hóa
    MUA = "MUA"      # Mua hàng hóa, dịch vụ
    BAN = "BAN"      # Bán hàng hóa, dịch vụ
    KPH = "KPH"      # Kiểm kê phát hiện thiếu/hụt
    KPD = "KPD"      # Kiểm kê phát hiện thừa
    DIEU_CHINH = "DIEU_CHINH"  # Điều chỉnh
    KHAC = "KHAC"    # Chứng từ khác


class LockStatus(str, Enum):
    """Trạng thái khóa sổ kế toán (Điều 18-22, Phụ lục III)."""
    OPEN = "OPEN"        # Mở - cho phép chỉnh sửa
    MONTH_LOCKED = "MONTH_LOCKED"    # Khóa tháng
    QUARTER_LOCKED = "QUARTER_LOCKED"  # Khóa quý
    YEAR_LOCKED = "YEAR_LOCKED"        # Khóa năm
    FINALIZED = "FINALIZED"            # Đã quyết toán


@dataclass(frozen=True, slots=True)
class Money:
    """Value Object - Số tiền theo đơn vị tiền tệ."""
    amount: Decimal
    currency: str = "VND"

    def __post_init__(self) -> None:
        pass  # Allow negative amounts for accounting purposes

    def __add__(self, other: "Money") -> "Money":
        if self.currency != other.currency:
            raise ValueError("Không thể cộng các đơn vị tiền tệ khác nhau")
        return Money(amount=self.amount + other.amount, currency=self.currency)

    def __sub__(self, other: "Money") -> "Money":
        if self.currency != other.currency:
            raise ValueError("Không thể trừ các đơn vị tiền tệ khác nhau")
        return Money(amount=self.amount - other.amount, currency=self.currency)


@dataclass(frozen=True, slots=True)
class ExchangeRate:
    """Value Object - Tỷ giá hối đoái (Điều 31 - TT99/2025)."""
    rate: Decimal
    currency: str
    rate_type: str = "REALTIME"  # REALTIME, AVERAGE
    valuation_date: date | None = None

    def to_vnd(self, amount: Decimal) -> Money:
        """Quy đổi số tiền sang VND."""
        return Money(amount=amount * self.rate, currency="VND")


@dataclass(frozen=True, slots=True)
class VoucherLineDetail:
    """Chi tiết dòng chứng từ - line item."""
    account_code: AccountCode
    debit_amount: Money | None = None
    credit_amount: Money | None = None
    counterpart_account: AccountCode | None = None  # Tài khoản đối ứng
    description: str = ""
    quantity: Decimal | None = None
    unit_price: Money | None = None
    exchange_rate: ExchangeRate | None = None
    foreign_amount: Money | None = None  # Số tiền ngoại tệ
    tax_code: str | None = None  # Mã thuế GTGT
    tax_rate: Decimal | None = None  # Thuế suất (0%, 5%, 8%, 10%)
    object_code: str | None = None  # Mã đối tượng (KH, NCC, HH)
    contract_code: str | None = None  # Mã hợp đồng


@dataclass(frozen=True, slots=True)
class AccountCodePattern:
    """Pattern matching cho mã tài khoản (Phụ lục II)."""
    pattern: str  # e.g., "111", "156*", "3331_"
    description: str
    is_detail: bool  # TK chi tiết hay TK tổng hợp
