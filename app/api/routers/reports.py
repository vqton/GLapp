"""
API Routers - Financial reports endpoints (Phụ lục IV - TT99/2025).
"""

from datetime import date, datetime
from decimal import Decimal
from uuid import UUID

from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.application.dto.accounting_dto import (
    AuditLogResponseDTO,
    FinancialStatementDTO,
    TrialBalanceDTO,
)
from app.infrastructure.database import get_db

router = APIRouter(prefix="/api/v1/reports", tags=["Báo cáo tài chính"])


@router.get("/trial-balance", response_model=TrialBalanceDTO)
def get_trial_balance(
    year: int = Query(..., description="Năm tài chính"),
    period_type: str = Query("MONTH", description="MONTH, QUARTER, YEAR"),
    period_value: int = Query(..., description="Tháng (1-12) hoặc Quý (1-4)"),
    db: Session = Depends(get_db)
):
    """
    Lấy Bảng cân đối thử (Sổ cái/Sổ chi tiết).
    
    Phụ lục III - TT99/2025: Kiểm tra cân đối Nợ = Có
    """
    from sqlalchemy import func

    from app.infrastructure.database.models import Account, JournalEntryLine

    if period_type == "MONTH":
        period_filter = JournalEntryLine.id == JournalEntryLine.id  # Demo filter
    elif period_type == "QUARTER":
        period_filter = JournalEntryLine.id == JournalEntryLine.id
    else:
        period_filter = JournalEntryLine.id == JournalEntryLine.id

    results = db.query(
        Account.code,
        Account.name,
        Account.account_type,
        func.coalesce(func.sum(JournalEntryLine.debit_amount), Decimal("0")).label("total_debit"),
        func.coalesce(func.sum(JournalEntryLine.credit_amount), Decimal("0")).label("total_credit")
    ).outerjoin(
        JournalEntryLine, Account.code == JournalEntryLine.account_code
    ).filter(
        Account.company_id == UUID("00000000-0000-0000-0000-000000000001")
    ).group_by(
        Account.code, Account.name, Account.account_type
    ).all()

    accounts = []
    total_debit = Decimal("0")
    total_credit = Decimal("0")

    for r in results:
        balance = r.total_debit - r.total_credit
        if balance != 0:
            total_debit += balance if balance > 0 else Decimal("0")
            total_credit += abs(balance) if balance < 0 else Decimal("0")

        accounts.append({
            "code": r.code,
            "name": r.name,
            "account_type": r.account_type,
            "debit": r.total_debit,
            "credit": r.total_credit,
            "balance": balance
        })

    return TrialBalanceDTO(
        period_type=period_type,
        period_value=period_value,
        year=year,
        accounts=accounts,
        total_debit=total_debit,
        total_credit=total_credit,
        difference=total_debit - total_credit
    )


@router.get("/balance-sheet", response_model=FinancialStatementDTO)
def get_balance_sheet(
    report_date: date = Query(..., description="Ngày lập báo cáo"),
    db: Session = Depends(get_db)
):
    """
    Lấy Báo cáo tình hình tài chính (Phụ lục IV - TT99/2025).
    
    Thay thế Bảng cân đối kế toán theo TT200.
    """
    from sqlalchemy import func

    from app.infrastructure.database.models import JournalEntryLine

    company_tax_code = "0101234567"  # Demo
    company_name = "Công ty TNHH Demo"

    def get_account_balance(account_codes: list[str]) -> Decimal:
        result = db.query(
            func.coalesce(func.sum(JournalEntryLine.debit_amount), Decimal("0")),
            func.coalesce(func.sum(JournalEntryLine.credit_amount), Decimal("0"))
        ).filter(
            JournalEntryLine.account_code.in_(account_codes)
        ).first()
        debit = result[0] or Decimal("0")
        credit = result[1] or Decimal("0")
        return debit - credit

    values = {
        "Mã số 100": "A. TÀI SẢN",
        "Mã số 110": "I. Tiền và các khoản tương đương tiền",
        "Mã số 111": get_account_balance(["1111", "1112"]),
        "Mã số 112": get_account_balance(["1121", "1122"]),
        "Mã số 120": "II. Các khoản đầu tư tài chính",
        "Mã số 130": "III. Các khoản phải thu",
        "Mã số 131": get_account_balance(["131"]),
        "Mã số 150": "IV. Hàng tồn kho",
        "Mã số 151": get_account_balance(["151", "152", "153", "156"]),
        "Mã số 200": "B. NỢ PHẢI TRẢ",
        "Mã số 210": "I. Nợ ngắn hạn",
        "Mã số 311": get_account_balance(["311"]),
        "Mã số 331": get_account_balance(["331"]),
        "Mã số 300": "C. VỐN CHỦ SỞ HỮU",
        "Mã số 410": "I. Vốn chủ sở hữu",
        "Mã số 411": get_account_balance(["411"]),
        "Mã số 421": get_account_balance(["421"]),
    }

    return FinancialStatementDTO(
        report_type="BALANCE_SHEET",
        report_period=report_date.strftime("%Y-%m-%d"),
        company_name=company_name,
        tax_code=company_tax_code,
        values=values
    )


@router.get("/income-statement", response_model=FinancialStatementDTO)
def get_income_statement(
    from_date: date = Query(..., description="Từ ngày"),
    to_date: date = Query(..., description="Đến ngày"),
    db: Session = Depends(get_db)
):
    """
    Lấy Báo cáo kết quả kinh doanh (Phụ lục IV - TT99/2025).
    """
    from sqlalchemy import func

    from app.infrastructure.database.models import JournalEntryLine

    company_tax_code = "0101234567"
    company_name = "Công ty TNHH Demo"

    def get_revenue_expense(account_codes: list[str]) -> Decimal:
        result = db.query(
            func.coalesce(func.sum(JournalEntryLine.credit_amount), Decimal("0")),
            func.coalesce(func.sum(JournalEntryLine.debit_amount), Decimal("0"))
        ).filter(
            JournalEntryLine.account_code.in_(account_codes)
        ).first()
        credit = result[0] or Decimal("0")
        debit = result[1] or Decimal("0")
        return credit - debit

    values = {
        "Mã số 01": "1. Doanh thu bán hàng và cung cấp dịch vụ",
        "Mã số 02": "2. Các khoản giảm trừ doanh thu",
        "Mã số 10": "DOANH THU THUẦN",
        "Mã số 11": get_revenue_expense(["511"]),
        "Mã số 20": "2. Giá vốn hàng bán",
        "Mã số 21": get_revenue_expense(["632"]),
        "Mã số 50": "LỢI NHUẬN GỘP",
        "Mã số 60": "3. Doanh thu hoạt động tài chính",
        "Mã số 61": get_revenue_expense(["515"]),
        "Mã số 70": "4. Chi phí tài chính",
        "Mã số 71": get_revenue_expense(["635"]),
        "Mã số 80": "5. Chi phí quản lý doanh nghiệp",
        "Mã số 81": get_revenue_expense(["641", "642"]),
        "Mã số 90": "TỔNG LỢI NHUẬN KẾ TOÁN TRƯỚC THUẾ",
    }

    return FinancialStatementDTO(
        report_type="INCOME_STATEMENT",
        report_period=f"{from_date} to {to_date}",
        company_name=company_name,
        tax_code=company_tax_code,
        values=values
    )


@router.get("/cash-flow", response_model=FinancialStatementDTO)
def get_cash_flow_statement(
    from_date: date = Query(..., description="Từ ngày"),
    to_date: date = Query(..., description="Đến ngày"),
    method: str = Query("DIRECT", description="DIRECT hoặc INDIRECT"),
    db: Session = Depends(get_db)
):
    """
    Lấy Báo cáo lưu chuyển tiền tệ (Phụ lục IV - TT99/2025).
    """
    company_tax_code = "0101234567"
    company_name = "Công ty TNHH Demo"

    values = {
        "Mã số 20": "I. Lưu chuyển tiền từ hoạt động kinh doanh",
        "Mã số 21": "1. Tiền thu từ bán hàng, cung cấp dịch vụ",
        "Mã số 22": "2. Tiền chi trả cho người cung cấp hàng hóa",
        "Mã số 23": "3. Tiền chi trả cho người lao động",
        "Mã số 24": "4. Tiền chi trả lãi vay",
        "Mã số 30": "Lưu chuyển tiền thuần từ hoạt động kinh doanh",
        "Mã số 40": "II. Lưu chuyển tiền từ hoạt động đầu tư",
        "Mã số 50": "III. Lưu chuyển tiền từ hoạt động tài chính",
        "Mã số 60": "Lưu chuyển tiền thuần trong kỳ",
        "Mã số 70": "Tiền đầu kỳ",
        "Mã số 71": "Ảnh hưởng của thay đổi tỷ giá hối đoái",
        "Mã số 80": "Tiền cuối kỳ",
    }

    return FinancialStatementDTO(
        report_type="CASH_FLOW",
        report_period=f"{from_date} to {to_date}",
        company_name=company_name,
        tax_code=company_tax_code,
        values=values
    )


@router.get("/audit-logs", response_model=list[AuditLogResponseDTO])
def get_audit_logs(
    entity_type: str | None = None,
    entity_id: UUID | None = None,
    user_id: str | None = None,
    action: str | None = None,
    start_date: datetime | None = None,
    end_date: datetime | None = None,
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db)
):
    """Lấy Audit trail (bắt buộc theo TT99/2025)."""
    from app.infrastructure.database.models import AuditLog

    query = db.query(AuditLog)

    if entity_type:
        query = query.filter(AuditLog.entity_type == entity_type)
    if entity_id:
        query = query.filter(AuditLog.entity_id == entity_id)
    if user_id:
        query = query.filter(AuditLog.user_id == user_id)
    if action:
        query = query.filter(AuditLog.action == action)
    if start_date:
        query = query.filter(AuditLog.created_at >= start_date)
    if end_date:
        query = query.filter(AuditLog.created_at <= end_date)

    logs = query.order_by(AuditLog.created_at.desc()).offset(skip).limit(limit).all()
    return [AuditLogResponseDTO.model_validate(log) for log in logs]
