"""
Database initialization and session management.
"""

import os
from collections.abc import Generator
from pathlib import Path

from sqlalchemy import create_engine
from sqlalchemy.orm import Session, declarative_base, sessionmaker

from app.infrastructure.database.models import (
    Account,
    AccountBalance,
    AccountingVoucher,
    AuditLog,
    Branch,
    Company,
    Contract,
    Customer,
    Employee,
    ExchangeRateHistory,
    FiscalPeriod,
    JournalEntry,
    JournalEntryLine,
    Product,
    ProvisionCalculation,
    Supplier,
    TaxConfig,
    get_engine_url,
)

DATABASE_URL = get_engine_url(os.getenv("DATABASE_TYPE", "sqlite"))

if "sqlite" in DATABASE_URL:
    engine = create_engine(DATABASE_URL, echo=False, connect_args={"check_same_thread": False})
else:
    engine = create_engine(DATABASE_URL, echo=False)

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()


def get_db() -> Generator[Session, None, None]:
    """Dependency - Get database session."""
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def init_db() -> None:
    """Initialize database - create all tables."""
    from app.infrastructure.database.models import (
        Account,
        AccountBalance,
        AccountingVoucher,
        AuditLog,
        Branch,
        Company,
        Contract,
        Customer,
        Employee,
        ExchangeRateHistory,
        FiscalPeriod,
        JournalEntry,
        JournalEntryLine,
        Product,
        ProvisionCalculation,
        Supplier,
        TaxConfig,
    )
    from sqlmodel import SQLModel

    SQLModel.metadata.create_all(bind=engine)


def seed_default_accounts(company_id: str) -> None:
    """Seed default chart of accounts (Phụ lục II - TT99/2025)."""
    from app.infrastructure.database.models import Account, Company

    default_accounts = [
        ("111", "Tiền mặt", "ASSET", None, True),
        ("1111", "Tiền Việt Nam", "ASSET", "111", True),
        ("1112", "Ngoại tệ", "ASSET", "111", True),
        ("112", "Tiền gửi Ngân hàng", "ASSET", None, True),
        ("1121", "Tiền Việt Nam", "ASSET", "112", True),
        ("1122", "Ngoại tệ", "ASSET", "112", True),
        ("131", "Phải thu khách hàng", "ASSET", None, True),
        ("138", "Phải thu khác", "ASSET", None, True),
        ("1381", "Tài sản thiếu chờ xử lý", "ASSET", "138", True),
        ("1388", "Phải thu khác", "ASSET", "138", True),
        ("151", "Hàng mua đang đi đường", "ASSET", None, True),
        ("152", "Nguyên liệu, vật liệu", "ASSET", None, True),
        ("153", "Công cụ, dụng cụ", "ASSET", None, True),
        ("156", "Hàng hóa", "ASSET", None, True),
        ("1561", "Hàng hóa kho", "ASSET", "156", True),
        ("1562", "Hàng hóa bất động sản", "ASSET", "156", True),
        ("1567", "Hàng hóa cho thuê", "ASSET", "156", True),
        ("157", "Hàng gửi đi bán", "ASSET", "156", True),
        ("211", "Tài sản cố định hữu hình", "ASSET", None, True),
        ("213", "Tài sản cố định vô hình", "ASSET", None, True),
        ("214", "Hao mòn TSCĐ", "ASSET", None, True),
        ("311", "Vay và nợ thuê tài chính", "LIABILITY", None, True),
        ("331", "Phải trả người bán", "LIABILITY", None, True),
        ("333", "Thuế và các khoản phải nộp Nhà nước", "LIABILITY", None, True),
        ("3331", "Thuế GTGT phải nộp", "LIABILITY", "333", True),
        ("3332", "Thuế tiêu thụ đặc biệt", "LIABILITY", "333", True),
        ("3333", "Thuế xuất, nhập khẩu", "LIABILITY", "333", True),
        ("3334", "Thuế thu nhập doanh nghiệp", "LIABILITY", "333", True),
        ("3335", "Thuế thu nhập hoãn lại phải trả", "LIABILITY", "333", True),
        ("3339", "Thuế và các khoản khác", "LIABILITY", "333", True),
        ("334", "Phải trả người lao động", "LIABILITY", None, True),
        ("335", "Chi phí phải trả", "LIABILITY", None, True),
        ("338", "Phải trả, phải nộp khác", "LIABILITY", None, True),
        ("3381", "Tài sản thừa chờ xử lý", "LIABILITY", "338", True),
        ("411", "Vốn đầu tư của chủ sở hữu", "EQUITY", None, True),
        ("4111", "Vốn góp của chủ sở hữu", "EQUITY", "411", True),
        ("4118", "Vốn khác của chủ sở hữu", "EQUITY", "411", True),
        ("421", "Lợi nhuận sau thuế chưa phân phối", "EQUITY", None, True),
        ("4211", "Lợi nhuận sau thuế chưa phân phối", "EQUITY", "421", True),
        ("511", "Doanh thu bán hàng và cung cấp dịch vụ", "REVENUE", None, True),
        ("5111", "Doanh thu bán hàng hóa", "REVENUE", "511", True),
        ("5112", "Doanh thu cung cấp dịch vụ", "REVENUE", "511", True),
        ("515", "Doanh thu hoạt động tài chính", "REVENUE", None, True),
        ("5151", "Lãi tiền gửi, cho vay", "REVENUE", "515", True),
        ("5152", "Lãi chênh lệch tỷ giá", "REVENUE", "515", True),
        ("521", "Chiết khấu thương mại", "EXPENSE", None, True),
        ("5211", "Chiết khấu hàng bán", "EXPENSE", "521", True),
        ("531", "Hàng bán bị trả lại", "EXPENSE", None, True),
        ("5311", "Hàng bán bị trả lại", "EXPENSE", "531", True),
        ("532", "Giảm giá hàng bán", "EXPENSE", None, True),
        ("5321", "Giảm giá hàng bán", "EXPENSE", "532", True),
        ("611", "Mua hàng", "DIRECT_COST", None, True),
        ("6111", "Mua hàng hóa", "DIRECT_COST", "611", True),
        ("621", "Chi phí nguyên vật liệu trực tiếp", "DIRECT_COST", None, True),
        ("622", "Chi phí nhân công trực tiếp", "DIRECT_COST", None, True),
        ("623", "Chi phí sản xuất chung", "DIRECT_COST", None, True),
        ("627", "Chi phí quản lý doanh nghiệp", "DIRECT_COST", None, True),
        ("631", "Giá thành sản phẩm dở dang", "DIRECT_COST", None, True),
        ("632", "Giá vốn hàng bán", "DIRECT_COST", None, True),
        ("635", "Chi phí tài chính", "EXPENSE", None, True),
        ("6351", "Lãi tiền vay", "EXPENSE", "635", True),
        ("6352", "Lỗ chênh lệch tỷ giá", "EXPENSE", "635", True),
        ("641", "Chi phí bán hàng", "EXPENSE", None, True),
        ("6411", "Chi phí nhân viên bán hàng", "EXPENSE", "641", True),
        ("6412", "Chi phí vật liệu, bao bì", "EXPENSE", "641", True),
        ("642", "Chi phí quản lý doanh nghiệp", "EXPENSE", None, True),
        ("6421", "Chi phí nhân viên quản lý", "EXPENSE", "642", True),
        ("711", "Thu nhập khác", "OTHER_REVENUE", None, True),
        ("7111", "Thu nhập từ thanh lý, nhượng bán TSCĐ", "OTHER_REVENUE", "711", True),
        ("811", "Chi phí khác", "OTHER_EXPENSE", None, True),
        ("8111", "Chi phí thanh lý, nhượng bán TSCĐ", "OTHER_EXPENSE", "811", True),
        ("821", "Chi phí thuế TNDN hoãn lại", "OTHER_EXPENSE", None, True),
        ("8211", "Chi phí thuế TNDN hoãn lại", "OTHER_EXPENSE", "821", True),
        ("911", "Xác định lợi nhuận thuần", "OTHER_EXPENSE", None, True),
        ("9111", "Kết chuyển doanh thu", "OTHER_EXPENSE", "911", True),
        ("9112", "Kết chuyển chi phí", "OTHER_EXPENSE", "911", True),
    ]

    db = SessionLocal()
    try:
        for code, name, acc_type, parent, is_detail in default_accounts:
            account = Account(
                company_id=company_id,
                code=code,
                name=name,
                account_type=acc_type,
                parent_code=parent,
                is_detail=is_detail,
                balance_direction="DEBIT"
                if acc_type in ["ASSET", "DIRECT_COST", "EXPENSE", "OTHER_EXPENSE"]
                else "CREDIT",
                is_system=True,
            )
            db.add(account)
        db.commit()
    finally:
        db.close()


if __name__ == "__main__":
    Path("./data").mkdir(exist_ok=True)
    init_db()
    print("Database initialized successfully!")
