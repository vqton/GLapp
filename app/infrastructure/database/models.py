"""
Infrastructure - SQLModel database models and configurations.
"""

from datetime import date, datetime
from decimal import Decimal
from typing import TYPE_CHECKING
from uuid import UUID, uuid4

from sqlmodel import Field, Relationship, SQLModel

if TYPE_CHECKING:
    pass


class Company(SQLModel, table=True):
    """Doanh nghiệp."""

    id: UUID = Field(default_factory=uuid4, primary_key=True)
    tax_code: str = Field(unique=True, index=True)
    name: str
    address: str | None = None
    phone: str | None = None
    email: str | None = None
    representative_name: str | None = None
    accounting_software_code: str | None = None
    fiscal_year_start: int = 1  # Tháng bắt đầu năm tài chính
    is_active: bool = True
    created_at: datetime = Field(default_factory=datetime.utcnow)
    updated_at: datetime = Field(default_factory=datetime.utcnow)

    branches: list["Branch"] = Relationship(back_populates="company")
    accounts: list["Account"] = Relationship(back_populates="company")
    fiscal_periods: list["FiscalPeriod"] = Relationship(back_populates="company")
    vouchers: list["AccountingVoucher"] = Relationship(back_populates="company")


class Branch(SQLModel, table=True):
    """Chi nhánh/Đơn vị cơ sở."""

    id: UUID = Field(default_factory=uuid4, primary_key=True)
    company_id: UUID = Field(foreign_key="company.id")
    code: str
    name: str
    address: str | None = None
    tax_code: str | None = None
    is_active: bool = True
    created_at: datetime = Field(default_factory=datetime.utcnow)
    updated_at: datetime = Field(default_factory=datetime.utcnow)

    company: "Company" = Relationship(back_populates="branches")


class Account(SQLModel, table=True):
    """Tài khoản kế toán (Phụ lục II - TT99/2025)."""

    id: UUID = Field(default_factory=uuid4, primary_key=True)
    company_id: UUID = Field(foreign_key="company.id")
    code: str = Field(index=True)
    name: str
    account_type: str
    parent_code: str | None = Field(default=None, index=True)
    is_detail: bool = False
    is_active: bool = True
    is_system: bool = False
    opening_balance_debit: Decimal | None = None
    opening_balance_credit: Decimal | None = None
    current_balance: Decimal | None = None
    balance_direction: str = "DEBIT"
    currency: str = "VND"
    detail_objects: str | None = None  # JSON array: ["KH", "NCC", "HH"]
    created_at: datetime = Field(default_factory=datetime.utcnow)
    updated_at: datetime = Field(default_factory=datetime.utcnow)
    version: int = 1

    company: "Company" = Relationship(back_populates="accounts")
    journal_lines: list["JournalEntryLine"] = Relationship(back_populates="account")


class FiscalPeriod(SQLModel, table=True):
    """Kỳ kế toán."""

    id: UUID = Field(default_factory=uuid4, primary_key=True)
    company_id: UUID = Field(foreign_key="company.id")
    period_type: str  # MONTH, QUARTER, YEAR
    year: int
    period_value: int  # 1-12 (tháng), 1-4 (quý)
    start_date: date
    end_date: date
    is_locked: bool = False
    lock_type: str | None = None
    locked_at: datetime | None = None
    locked_by: str | None = None
    created_at: datetime = Field(default_factory=datetime.utcnow)
    updated_at: datetime = Field(default_factory=datetime.utcnow)

    company: "Company" = Relationship(back_populates="fiscal_periods")


class AccountingVoucher(SQLModel, table=True):
    """Chứng từ kế toán (Điều 8-9, Phụ lục I - TT99/2025)."""

    id: UUID = Field(default_factory=uuid4, primary_key=True)
    company_id: UUID = Field(foreign_key="company.id")
    voucher_number: str = Field(unique=True, index=True)
    voucher_type: str
    voucher_date: date = Field(index=True)
    posting_date: date | None = None
    description: str
    description_detail: str | None = None
    document_ref: str | None = None  # Số hóa đơn, hợp đồng
    document_date: date | None = None

    is_signed: bool = False
    signed_at: datetime | None = None
    signature_data: str | None = None
    signer_id: str | None = None

    is_locked: bool = False
    locked_at: datetime | None = None
    lock_status: str = "OPEN"

    branch_id: UUID | None = Field(default=None, foreign_key="branch.id")
    created_by: str
    created_at: datetime = Field(default_factory=datetime.utcnow)
    updated_at: datetime = Field(default_factory=datetime.utcnow)
    version: int = 1

    company: "Company" = Relationship(back_populates="vouchers")
    journal_entries: list["JournalEntry"] = Relationship(back_populates="voucher")


class JournalEntry(SQLModel, table=True):
    """Bút toán kế toán (Phụ lục III - TT99/2025)."""

    id: UUID = Field(default_factory=uuid4, primary_key=True)
    voucher_id: UUID = Field(foreign_key="accountingvoucher.id")
    entry_number: str = Field(unique=True, index=True)
    voucher_date: date = Field(index=True)
    posting_date: date
    description: str
    description_detail: str | None = None
    total_debit: Decimal | None = None
    total_credit: Decimal | None = None
    difference: Decimal | None = None
    is_posted: bool = False
    posted_at: datetime | None = None
    is_locked: bool = False
    lock_status: str = "OPEN"
    created_by: str
    created_at: datetime = Field(default_factory=datetime.utcnow)
    updated_at: datetime = Field(default_factory=datetime.utcnow)
    version: int = 1

    voucher: "AccountingVoucher" = Relationship(back_populates="journal_entries")
    lines: list["JournalEntryLine"] = Relationship(back_populates="journal_entry")


class JournalEntryLine(SQLModel, table=True):
    """Dòng bút toán kế toán."""

    id: UUID = Field(default_factory=uuid4, primary_key=True)
    journal_entry_id: UUID = Field(foreign_key="journalentry.id")
    account_id: UUID = Field(foreign_key="account.id")
    line_number: int

    account_code: str = Field(index=True)
    debit_amount: Decimal | None = None
    credit_amount: Decimal | None = None
    counterpart_account: str | None = None
    description: str | None = None

    quantity: Decimal | None = None
    unit_price: Decimal | None = None
    exchange_rate: Decimal | None = None
    foreign_amount: Decimal | None = None

    tax_code: str | None = None
    tax_rate: Decimal | None = None

    object_code: str | None = None
    object_type: str | None = None  # KH, NCC, HH, HD
    contract_code: str | None = None

    created_at: datetime = Field(default_factory=datetime.utcnow)

    journal_entry: "JournalEntry" = Relationship(back_populates="lines")
    account: "Account" = Relationship(back_populates="journal_lines")


class AccountBalance(SQLModel, table=True):
    """Số dư tài khoản theo kỳ."""

    id: UUID = Field(default_factory=uuid4, primary_key=True)
    company_id: UUID = Field(foreign_key="company.id")
    account_id: UUID = Field(foreign_key="account.id")
    account_code: str = Field(index=True)
    period_type: str
    year: int
    period_value: int
    start_date: date
    end_date: date

    opening_debit: Decimal | None = None
    opening_credit: Decimal | None = None
    period_debit: Decimal | None = None
    period_credit: Decimal | None = None
    closing_debit: Decimal | None = None
    closing_credit: Decimal | None = None

    created_at: datetime = Field(default_factory=datetime.utcnow)
    updated_at: datetime = Field(default_factory=datetime.utcnow)


class AuditLog(SQLModel, table=True):
    """Audit trail cho mọi thay đổi (bắt buộc theo TT99/2025)."""

    id: UUID = Field(default_factory=uuid4, primary_key=True)
    company_id: UUID | None = Field(default=None, foreign_key="company.id")
    user_id: str = Field(index=True)
    action: str = Field(index=True)  # CREATE, UPDATE, DELETE, SIGN, LOCK, UNLOCK

    entity_type: str  # Voucher, JournalEntry, Account, ...
    entity_id: UUID

    old_value: str | None = None  # JSON
    new_value: str | None = None  # JSON

    ip_address: str | None = None
    user_agent: str | None = None

    created_at: datetime = Field(default_factory=datetime.utcnow, index=True)


class Customer(SQLModel, table=True):
    """Khách hàng."""

    id: UUID = Field(default_factory=uuid4, primary_key=True)
    company_id: UUID = Field(foreign_key="company.id")
    customer_code: str = Field(unique=True, index=True)
    customer_name: str
    customer_type: str = "BUSINESS"  # BUSINESS, INDIVIDUAL
    tax_code: str | None = None
    address: str | None = None
    phone: str | None = None
    email: str | None = None
    channel: str = "ONSITE"  # ONSITE, ONLINE
    online_platform: str | None = None  # Shopee, Lazada, Tiki
    credit_limit: Decimal = Decimal("0")
    credit_term_days: int = 0
    is_active: bool = True
    created_at: datetime = Field(default_factory=datetime.utcnow)
    updated_at: datetime = Field(default_factory=datetime.utcnow)


class Supplier(SQLModel, table=True):
    """Nhà cung cấp."""

    id: UUID = Field(default_factory=uuid4, primary_key=True)
    company_id: UUID = Field(foreign_key="company.id")
    supplier_code: str = Field(unique=True, index=True)
    supplier_name: str
    supplier_type: str = "BUSINESS"  # BUSINESS, INDIVIDUAL
    tax_code: str | None = None
    address: str | None = None
    phone: str | None = None
    email: str | None = None
    account_code: str | None = None  # TK 331 mặc định
    credit_limit: Decimal = Decimal("0")
    credit_term_days: int = 0
    is_active: bool = True
    created_at: datetime = Field(default_factory=datetime.utcnow)
    updated_at: datetime = Field(default_factory=datetime.utcnow)


class Product(SQLModel, table=True):
    """Sản phẩm/Hàng hóa."""

    id: UUID = Field(default_factory=uuid4, primary_key=True)
    company_id: UUID = Field(foreign_key="company.id")
    product_code: str = Field(unique=True, index=True)
    product_name: str
    product_type: str = "GOODS"  # GOODS, SERVICE
    unit: str = "chiếc"
    unit_price: Decimal = Decimal("0")
    purchase_price: Decimal = Decimal("0")
    vat_rate: Decimal = Decimal("10")
    inventory_account: str = "1561"
    cost_account: str = "632"
    stock_account: str = "1561"
    category: str | None = None
    brand: str | None = None
    origin: str | None = None
    barcode: str | None = None
    min_stock: int = 0
    max_stock: int = 0
    lead_time_days: int = 0
    is_active: bool = True
    created_at: datetime = Field(default_factory=datetime.utcnow)
    updated_at: datetime = Field(default_factory=datetime.utcnow)


class Contract(SQLModel, table=True):
    """Hợp đồng dịch vụ (VAS 14/15)."""

    id: UUID = Field(default_factory=uuid4, primary_key=True)
    company_id: UUID = Field(foreign_key="company.id")
    contract_code: str = Field(unique=True, index=True)
    contract_type: str = "CONSULTING"  # CONSULTING, SERVICE, MAINTENANCE
    customer_code: str | None = None
    customer_name: str | None = None
    start_date: date
    end_date: date
    total_value: Decimal = Decimal("0")
    vat_rate: Decimal = Decimal("10")
    revenue_recognition: str = "PERCENTAGE"  # PERCENTAGE, MILESTONE, COMPLETED
    progress_payment: str = "MONTHLY"  # MONTHLY, QUARTERLY, MILESTONE
    service_description: str | None = None
    account_code: str = "5112"
    status: str = "ACTIVE"
    is_active: bool = True
    created_at: datetime = Field(default_factory=datetime.utcnow)
    updated_at: datetime = Field(default_factory=datetime.utcnow)


class Employee(SQLModel, table=True):
    """Nhân viên."""

    id: UUID = Field(default_factory=uuid4, primary_key=True)
    company_id: UUID = Field(foreign_key="company.id")
    employee_code: str = Field(unique=True, index=True)
    employee_name: str
    department: str | None = None
    position: str | None = None
    hire_date: date = date(2026, 1, 1)
    salary: Decimal = Decimal("0")
    allowance: Decimal = Decimal("0")
    tax_deduction: Decimal = Decimal("11000000")
    insurance_rate: Decimal = Decimal("0.08")
    tax_rate: Decimal = Decimal("0.1")
    payment_method: str = "BANK"  # CASH, BANK
    bank_account: str | None = None
    id_card: str | None = None
    address: str | None = None
    phone: str | None = None
    email: str | None = None
    note: str | None = None
    is_active: bool = True
    created_at: datetime = Field(default_factory=datetime.utcnow)
    updated_at: datetime = Field(default_factory=datetime.utcnow)


class TaxConfig(SQLModel, table=True):
    """Cấu hình thuế."""

    id: UUID = Field(default_factory=uuid4, primary_key=True)
    company_id: UUID = Field(foreign_key="company.id")
    tax_type: str  # VAT, CIT, PIT
    tax_code: str
    tax_name: str
    rate: Decimal
    applies_to_account: str | None = None
    is_active: bool = True
    created_at: datetime = Field(default_factory=datetime.utcnow)
    updated_at: datetime = Field(default_factory=datetime.utcnow)


class ExchangeRateHistory(SQLModel, table=True):
    """Lịch sử tỷ giá ngoại tệ (Điều 31 - TT99/2025)."""

    id: UUID = Field(default_factory=uuid4, primary_key=True)
    currency: str = Field(index=True)
    valuation_date: date = Field(index=True)
    rate: Decimal
    rate_type: str = "REALTIME"  # REALTIME, AVERAGE, FIXING
    source: str | None = None  # VCB, BIDV, ...
    created_at: datetime = Field(default_factory=datetime.utcnow)


class ProvisionCalculation(SQLModel, table=True):
    """Tính dự phòng nợ phải thu (Điều 32 - TT99/2025)."""

    id: UUID = Field(default_factory=uuid4, primary_key=True)
    company_id: UUID = Field(foreign_key="company.id")
    calculation_date: date
    customer_id: UUID | None = None
    customer_code: str | None = None
    receivable_type: str | None = None  # Ngắn hạn, Dài hạn
    original_amount: Decimal
    overdue_days: int
    provision_rate: Decimal
    provision_amount: Decimal
    provision_type: str  # CỤ_THỂ, CHUNG
    fiscal_period: str
    created_at: datetime = Field(default_factory=datetime.utcnow)


def get_engine_url(database_type: str = "sqlite") -> str:
    """Lấy database URL từ environment."""
    import os

    db_type = database_type or os.getenv("DATABASE_TYPE", "sqlite")

    if db_type == "sqlite":
        db_path = os.getenv("DATABASE_PATH", "./data/accounting.db")
        return f"sqlite:///{db_path}"
    elif db_type == "postgresql":
        host = os.getenv("DB_HOST", "localhost")
        port = os.getenv("DB_PORT", "5432")
        dbname = os.getenv("DB_NAME", "accounting")
        user = os.getenv("DB_USER", "postgres")
        password = os.getenv("DB_PASSWORD", "postgres")
        return f"postgresql://{user}:{password}@{host}:{port}/{dbname}"
    else:
        raise ValueError(f"Unsupported database type: {db_type}")
