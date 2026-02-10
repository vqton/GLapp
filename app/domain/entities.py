"""
Domain Entities - Core business entities theo DDD.
Áp dụng nghiệp vụ kế toán theo Thông tư 99/2025/TT-BTC.
"""

import uuid
from dataclasses import dataclass, field, replace
from datetime import date, datetime
from decimal import Decimal

from .value_objects import (
    AccountCode,
    AccountType,
    Money,
    VoucherLineDetail,
    VoucherType,
)


@dataclass
class Account:
    """
    Entity - Tài khoản kế toán (Phụ lục II - TT99/2025).
    Áp dụng hệ thống 71 TK cấp 1, 101 TK cấp 2,...
    """
    code: AccountCode
    name: str
    account_type: AccountType
    company_code: str
    id: uuid.UUID = field(default_factory=uuid.uuid4)
    parent_code: AccountCode | None = None
    is_detail: bool = False
    is_active: bool = True
    is_system: bool = False
    opening_balance_debit: Money | None = None
    opening_balance_credit: Money | None = None
    current_balance: Money | None = None
    balance_direction: str = "DEBIT"
    currency: str = "VND"
    detail_objects: list[str] = field(default_factory=list)
    created_at: datetime = field(default_factory=datetime.utcnow)
    updated_at: datetime = field(default_factory=datetime.utcnow)
    version: int = 1

    def is_balanced(self) -> bool:
        if self.opening_balance_debit and self.opening_balance_credit:
            return False
        return True

    def post_balance(self, debit: Money, credit: Money) -> "Account":
        new_balance = self.current_balance or Money(Decimal("0"), self.currency)
        if self.balance_direction == "DEBIT":
            new_balance = Money(new_balance.amount + debit.amount - credit.amount, self.currency)
        else:
            new_balance = Money(new_balance.amount - debit.amount + credit.amount, self.currency)
        return replace(self, current_balance=new_balance, version=self.version + 1)


@dataclass
class AccountingVoucher:
    """
    Entity - Chứng từ kế toán (Điều 8-9, Phụ lục I - TT99/2025).
    Mỗi chứng từ chỉ phát sinh một lần, không sửa đổi nội dung cốt lõi.
    """
    voucher_number: str
    voucher_type: VoucherType
    voucher_date: date
    description: str
    company_code: str
    created_by: str
    id: uuid.UUID = field(default_factory=uuid.uuid4)
    posting_date: date | None = None
    description_detail: str | None = None
    document_ref: str | None = None
    document_date: date | None = None
    is_signed: bool = False
    signed_at: datetime | None = None
    signature_data: str | None = None
    signer_id: str | None = None
    is_locked: bool = False
    locked_at: datetime | None = None
    lock_status: str = "OPEN"
    branch_code: str | None = None
    journal_entry_ids: list[uuid.UUID] = field(default_factory=list)
    created_at: datetime = field(default_factory=datetime.utcnow)
    updated_at: datetime = field(default_factory=datetime.utcnow)
    version: int = 1

    def sign(self, signer_id: str, signature: str) -> "AccountingVoucher":
        if self.is_signed:
            raise ValueError("Chứng từ đã được ký trước đó")
        return replace(
            self,
            is_signed=True,
            signed_at=datetime.utcnow(),
            signer_id=signer_id,
            signature_data=signature,
            version=self.version + 1
        )

    def lock(self, lock_type: str) -> "AccountingVoucher":
        return replace(
            self,
            is_locked=True,
            locked_at=datetime.utcnow(),
            lock_status=lock_type,
            version=self.version + 1
        )

    def can_modify(self) -> bool:
        return not self.is_locked


@dataclass
class JournalEntry:
    """
    Entity - Bút toán kế toán (Phụ lục III - TT99/2025).
    Ghi sổ kép: Tổng Nợ = Tổng Có.
    """
    entry_number: str
    voucher_id: uuid.UUID
    voucher_date: date
    posting_date: date
    description: str
    created_by: str
    id: uuid.UUID = field(default_factory=uuid.uuid4)
    description_detail: str | None = None
    lines: list[VoucherLineDetail] = field(default_factory=list)
    total_debit: Money | None = None
    total_credit: Money | None = None
    difference: Money | None = None
    is_posted: bool = False
    posted_at: datetime | None = None
    is_locked: bool = False
    lock_status: str = "OPEN"
    created_at: datetime = field(default_factory=datetime.utcnow)
    version: int = 1

    def is_balanced(self) -> bool:
        if self.total_debit is None or self.total_credit is None:
            return False
        return self.total_debit.amount == self.total_credit.amount

    def calculate_totals(self) -> "JournalEntry":
        total_debit = Money(Decimal("0"), "VND")
        total_credit = Money(Decimal("0"), "VND")
        for line in self.lines:
            if line.debit_amount:
                total_debit += line.debit_amount
            if line.credit_amount:
                total_credit += line.credit_amount
        return replace(
            self,
            total_debit=total_debit,
            total_credit=total_credit,
            difference=Money(total_debit.amount - total_credit.amount, "VND")
        )

    def post(self, posted_by: str) -> "JournalEntry":
        if not self.is_balanced():
            raise ValueError("Bút toán không cân đối: Tổng Nợ != Tổng Có")
        if self.is_posted:
            raise ValueError("Bút toán đã được ghi sổ trước đó")
        return replace(
            self,
            is_posted=True,
            posted_at=datetime.utcnow(),
            version=self.version + 1
        )

    def lock(self, lock_type: str) -> "JournalEntry":
        return replace(
            self,
            is_locked=True,
            lock_status=lock_type,
            version=self.version + 1
        )


@dataclass
class AccountBalance:
    """
    Value Object - Số dư tài khoản theo kỳ.
    """
    account_code: AccountCode
    period_type: str
    period_value: int
    company_code: str
    opening_debit: Money | None = None
    opening_credit: Money | None = None
    period_debit: Money | None = None
    period_credit: Money | None = None
    closing_debit: Money | None = None
    closing_credit: Money | None = None

    def check_negative_balance(self) -> list[str]:
        warnings = []
        if self.closing_debit and self.closing_debit.amount < 0:
            warnings.append(f"TK {self.account_code}: Số dư cuối kỳ âm")
        return warnings
