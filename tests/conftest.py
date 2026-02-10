"""
Pytest configuration and fixtures.
"""

from datetime import date
from decimal import Decimal
from uuid import UUID, uuid4

import pytest

from app.domain.entities import Account, AccountingVoucher, JournalEntry
from app.domain.value_objects import Money


@pytest.fixture
def sample_company_id() -> UUID:
    return UUID("00000000-0000-0000-0000-000000000001")


@pytest.fixture
def sample_voucher() -> AccountingVoucher:
    return AccountingVoucher(
        id=uuid4(),
        voucher_number="CT/20251215/001",
        voucher_type="MUA",
        voucher_date=date(2025, 12, 15),
        posting_date=date(2025, 12, 15),
        description="Mua hàng hóa test",
        company_code="DEMO",
        created_by="test_user",
        is_locked=False,
        lock_status="OPEN"
    )


@pytest.fixture
def balanced_entry() -> JournalEntry:
    return JournalEntry(
        id=uuid4(),
        entry_number="BT/20251215/001",
        voucher_id=uuid4(),
        voucher_date=date(2025, 12, 15),
        posting_date=date(2025, 12, 15),
        description="Test entry",
        created_by="test",
        total_debit=Money(Decimal("10000000"), "VND"),
        total_credit=Money(Decimal("10000000"), "VND"),
        difference=Money(Decimal("0"), "VND")
    )


@pytest.fixture
def cash_account() -> Account:
    return Account(
        id=uuid4(),
        code="1111",
        name="Tiền mặt",
        account_type="ASSET",
        company_code="DEMO",
        current_balance=Money(Decimal("50000000"), "VND"),
        balance_direction="DEBIT"
    )


@pytest.fixture
def payable_account() -> Account:
    return Account(
        id=uuid4(),
        code="331",
        name="Phải trả người bán",
        account_type="LIABILITY",
        company_code="DEMO",
        current_balance=Money(Decimal("20000000"), "VND"),
        balance_direction="CREDIT"
    )
