"""
API Routers - FastAPI endpoints for accounting operations.
"""

from datetime import date, datetime
from decimal import Decimal
from uuid import UUID

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.application.dto.accounting_dto import (
    BalanceCheckResultDTO,
    SigningRequestDTO,
    VoucherCreateDTO,
    VoucherResponseDTO,
)
from app.infrastructure.database import get_db

router = APIRouter(prefix="/api/v1", tags=["Chứng từ kế toán"])


@router.post("/vouchers", response_model=VoucherResponseDTO, status_code=status.HTTP_201_CREATED)
def create_voucher(
    dto: VoucherCreateDTO,
    db: Session = Depends(get_db),
    current_user: str = "admin"
):
    """
    Tạo chứng từ kế toán mới (Điều 8-9, Phụ lục I - TT99/2025).
    
    - Mỗi chứng từ chỉ phát sinh một lần
    - Tự động kiểm tra cân đối Nợ = Có
    - Mã chứng từ không trùng lặp
    """
    from app.infrastructure.database.models import AccountingVoucher as VoucherModel
    from app.infrastructure.database.models import JournalEntry, JournalEntryLine

    # Tạo mã chứng từ (format: PK/YYYYMMDD/XXX)
    voucher_date_str = dto.voucher_date.strftime("%Y%m%d")

    # Kiểm tra trùng mã
    existing = db.query(VoucherModel).filter(
        VoucherModel.voucher_number.like(f"%{voucher_date_str}%")
    ).count()
    voucher_number = f"CT/{voucher_date_str}/{existing + 1:03d}"

    # Validate Nợ = Có
    total_debit = sum(
        (line.debit_amount or Decimal("0")) for line in dto.lines
    )
    total_credit = sum(
        (line.credit_amount or Decimal("0")) for line in dto.lines
    )

    if total_debit != total_credit:
        raise HTTPException(
            status_code=400,
            detail=f"Bút toán không cân đối: Tổng Nợ = {total_debit}, Tổng Có = {total_credit}"
        )

    # Tạo chứng từ
    voucher = VoucherModel(
        voucher_number=voucher_number,
        voucher_type=dto.voucher_type,
        voucher_date=dto.voucher_date,
        posting_date=dto.posting_date or dto.voucher_date,
        description=dto.description,
        description_detail=dto.description_detail,
        document_ref=dto.document_ref,
        document_date=dto.document_date,
        branch_id=dto.branch_id,
        company_id=UUID("00000000-0000-0000-0000-000000000001"),  # Demo company
        created_by=current_user,
        is_locked=False,
        lock_status="OPEN"
    )
    db.add(voucher)
    db.flush()

    # Tạo bút toán
    entry = JournalEntry(
        voucher_id=voucher.id,
        entry_number=f"BT/{voucher_date_str}/{existing + 1:03d}",
        voucher_date=dto.voucher_date,
        posting_date=dto.posting_date or dto.voucher_date,
        description=dto.description,
        description_detail=dto.description_detail,
        total_debit=total_debit,
        total_credit=total_credit,
        difference=total_debit - total_credit,
        created_by=current_user
    )
    db.add(entry)
    db.flush()

    # Tạo dòng bút toán
    for idx, line in enumerate(dto.lines, start=1):
        db_line = JournalEntryLine(
            journal_entry_id=entry.id,
            account_id=UUID("00000000-0000-0000-0000-000000000001"),  # Demo account
            account_code=line.account_code,
            line_number=idx,
            debit_amount=line.debit_amount,
            credit_amount=line.credit_amount,
            counterpart_account=line.counterpart_account,
            description=line.description,
            quantity=line.quantity,
            unit_price=line.unit_price,
            exchange_rate=line.exchange_rate,
            foreign_amount=line.foreign_amount,
            tax_code=line.tax_code,
            tax_rate=line.tax_rate,
            object_code=line.object_code,
            contract_code=line.contract_code
        )
        db.add(db_line)

    db.commit()
    db.refresh(voucher)

    return VoucherResponseDTO.model_validate(voucher)


@router.get("/vouchers/{voucher_id}", response_model=VoucherResponseDTO)
def get_voucher(voucher_id: UUID, db: Session = Depends(get_db)):
    """Lấy thông tin chứng từ theo ID."""
    from app.infrastructure.database.models import AccountingVoucher as VoucherModel

    voucher = db.query(VoucherModel).filter(VoucherModel.id == voucher_id).first()
    if not voucher:
        raise HTTPException(status_code=404, detail="Không tìm thấy chứng từ")
    return VoucherResponseDTO.model_validate(voucher)


@router.get("/vouchers", response_model=list[VoucherResponseDTO])
def list_vouchers(
    start_date: date | None = None,
    end_date: date | None = None,
    voucher_type: str | None = None,
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db)
):
    """Danh sách chứng từ theo điều kiện lọc."""
    from app.infrastructure.database.models import AccountingVoucher as VoucherModel

    query = db.query(VoucherModel)

    if start_date:
        query = query.filter(VoucherModel.voucher_date >= start_date)
    if end_date:
        query = query.filter(VoucherModel.voucher_date <= end_date)
    if voucher_type:
        query = query.filter(VoucherModel.voucher_type == voucher_type)

    vouchers = query.order_by(VoucherModel.voucher_date.desc()).offset(skip).limit(limit).all()
    return [VoucherResponseDTO.model_validate(v) for v in vouchers]


@router.post("/vouchers/{voucher_id}/sign")
def sign_voucher(
    voucher_id: UUID,
    dto: SigningRequestDTO,
    db: Session = Depends(get_db),
    current_user: str = "admin"
):
    """
    Ký số chứng từ (Thông tư 78/2021/TT-BTC).
    
    - USB Token hoặc Cloud Signing
    - Mỗi chứng từ chỉ ký một lần
    """
    from app.infrastructure.database.models import AccountingVoucher as VoucherModel
    from app.infrastructure.database.models import AuditLog

    voucher = db.query(VoucherModel).filter(VoucherModel.id == voucher_id).first()
    if not voucher:
        raise HTTPException(status_code=404, detail="Không tìm thấy chứng từ")

    if voucher.is_signed:
        raise HTTPException(status_code=400, detail="Chứng từ đã được ký trước đó")

    # Ký số (demo - trong thực tế gọi API USB token hoặc cloud)
    voucher.is_signed = True
    voucher.signed_at = datetime.utcnow()
    voucher.signer_id = dto.signer_id
    voucher.signature_data = f"SIGNED_{datetime.utcnow().timestamp()}"
    voucher.version += 1

    # Audit log
    audit = AuditLog(
        user_id=current_user,
        action="SIGN",
        entity_type="AccountingVoucher",
        entity_id=voucher_id,
        old_value=None,
        new_value=voucher.signature_data
    )
    db.add(audit)
    db.commit()

    return {"status": "signed", "voucher_number": voucher.voucher_number}


@router.post("/vouchers/{voucher_id}/lock")
def lock_voucher(
    voucher_id: UUID,
    db: Session = Depends(get_db),
    current_user: str = "admin"
):
    """Khóa chứng từ (không cho phép chỉnh sửa sau khóa)."""
    from app.infrastructure.database.models import AccountingVoucher as VoucherModel
    from app.infrastructure.database.models import AuditLog

    voucher = db.query(VoucherModel).filter(VoucherModel.id == voucher_id).first()
    if not voucher:
        raise HTTPException(status_code=404, detail="Không tìm thấy chứng từ")

    if voucher.is_locked:
        raise HTTPException(status_code=400, detail="Chứng từ đã bị khóa trước đó")

    voucher.is_locked = True
    voucher.locked_at = datetime.utcnow()
    voucher.lock_status = "MANUAL"
    voucher.version += 1

    audit = AuditLog(
        user_id=current_user,
        action="LOCK",
        entity_type="AccountingVoucher",
        entity_id=voucher_id
    )
    db.add(audit)
    db.commit()

    return {"status": "locked", "voucher_number": voucher.voucher_number}


@router.get("/vouchers/{voucher_id}/balance-check", response_model=BalanceCheckResultDTO)
def check_voucher_balance(voucher_id: UUID, db: Session = Depends(get_db)):
    """
    Kiểm tra cân đối chứng từ.
    
    Nguyên tắc: Tổng Nợ = Tổng Có
    """
    from app.infrastructure.database.models import AccountingVoucher as VoucherModel
    from app.infrastructure.database.models import JournalEntry

    voucher = db.query(VoucherModel).filter(VoucherModel.id == voucher_id).first()
    if not voucher:
        raise HTTPException(status_code=404, detail="Không tìm thấy chứng từ")

    entry = db.query(JournalEntry).filter(JournalEntry.voucher_id == voucher_id).first()
    if not entry:
        raise HTTPException(status_code=404, detail="Không tìm thấy bút toán")

    is_balanced = (entry.total_debit or Decimal("0")) == (entry.total_credit or Decimal("0"))

    return BalanceCheckResultDTO(
        is_balanced=is_balanced,
        voucher_number=voucher.voucher_number,
        total_debit=entry.total_debit or Decimal("0"),
        total_credit=entry.total_credit or Decimal("0"),
        difference=entry.difference or Decimal("0"),
        errors=[] if is_balanced else ["Tổng Nợ != Tổng Có"]
    )
