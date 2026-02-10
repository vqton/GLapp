"""
API DTOs - Data Transfer Objects for API requests/responses.
"""

from datetime import date, datetime
from decimal import Decimal
from uuid import UUID

from pydantic import BaseModel, ConfigDict, Field


class VoucherLineCreateDTO(BaseModel):
    """DTO - Tạo dòng chứng từ."""
    account_code: str = Field(..., description="Mã tài khoản")
    debit_amount: Decimal | None = Field(None, ge=0, description="Số tiền Nợ")
    credit_amount: Decimal | None = Field(None, ge=0, description="Số tiền Có")
    counterpart_account: str | None = Field(None, description="Tài khoản đối ứng")
    description: str | None = Field(None, description="Diễn giải")
    quantity: Decimal | None = Field(None, ge=0, description="Số lượng")
    unit_price: Decimal | None = Field(None, ge=0, description="Đơn giá")
    exchange_rate: Decimal | None = Field(None, ge=0, description="Tỷ giá")
    foreign_amount: Decimal | None = Field(None, description="Số tiền ngoại tệ")
    tax_code: str | None = Field(None, description="Mã thuế GTGT")
    tax_rate: Decimal | None = Field(None, ge=0, le=100, description="Thuế suất %")
    object_code: str | None = Field(None, description="Mã đối tượng (KH, NCC)")
    contract_code: str | None = Field(None, description="Mã hợp đồng")


class VoucherCreateDTO(BaseModel):
    """DTO - Tạo chứng từ kế toán."""
    voucher_type: str = Field(..., description="Loại chứng từ")
    voucher_date: date = Field(..., description="Ngày chứng từ")
    posting_date: date | None = Field(None, description="Ngày ghi sổ")
    description: str = Field(..., max_length=500, description="Nội dung kinh tế")
    description_detail: str | None = Field(None, description="Diễn giải chi tiết")
    document_ref: str | None = Field(None, description="Số hóa đơn/Hợp đồng")
    document_date: date | None = Field(None, description="Ngày hóa đơn")
    branch_id: UUID | None = Field(None, description="Mã chi nhánh")
    lines: list[VoucherLineCreateDTO] = Field(..., min_length=1, description="Các dòng chứng từ")

    model_config = ConfigDict(json_schema_extra={
        "example": {
            "voucher_type": "MUA",
            "voucher_date": "2025-12-15",
            "posting_date": "2025-12-15",
            "description": "Mua hàng hóa Công ty ABC",
            "description_detail": "Mua 100 sản phẩm XYZ",
            "document_ref": "HD001234",
            "document_date": "2025-12-15",
            "lines": [
                {
                    "account_code": "1561",
                    "debit_amount": 10000000,
                    "credit_amount": None,
                    "description": "Nhập kho hàng hóa",
                    "quantity": 100,
                    "unit_price": 100000,
                    "object_code": "NCC001",
                    "tax_code": "GTGT",
                    "tax_rate": 10
                },
                {
                    "account_code": "3331",
                    "debit_amount": None,
                    "credit_amount": 1000000,
                    "description": "Thuế GTGT đầu ra",
                    "tax_code": "GTGT",
                    "tax_rate": 10
                }
            ]
        }
    })


class VoucherResponseDTO(BaseModel):
    """DTO - Phản hồi chứng từ."""
    id: UUID
    voucher_number: str
    voucher_type: str
    voucher_date: date
    posting_date: date | None
    description: str
    description_detail: str | None
    document_ref: str | None
    document_date: date | None
    is_signed: bool
    signed_at: datetime | None
    signer_id: str | None
    is_locked: bool
    lock_status: str
    created_by: str
    created_at: datetime
    updated_at: datetime

    model_config = ConfigDict(from_attributes=True)


class JournalEntryResponseDTO(BaseModel):
    """DTO - Phản hồi bút toán."""
    id: UUID
    entry_number: str
    voucher_id: UUID
    voucher_date: date
    posting_date: date
    description: str
    description_detail: str | None
    total_debit: Decimal | None
    total_credit: Decimal | None
    difference: Decimal | None
    is_posted: bool
    posted_at: datetime | None
    is_locked: bool
    lock_status: str

    model_config = ConfigDict(from_attributes=True)


class AccountResponseDTO(BaseModel):
    """DTO - Phản hồi tài khoản."""
    id: UUID
    code: str
    name: str
    account_type: str
    parent_code: str | None
    is_detail: bool
    is_active: bool
    current_balance: Decimal | None
    balance_direction: str
    currency: str

    model_config = ConfigDict(from_attributes=True)


class BalanceCheckResultDTO(BaseModel):
    """DTO - Kết quả kiểm tra cân đối."""
    is_balanced: bool
    voucher_number: str
    total_debit: Decimal
    total_credit: Decimal
    difference: Decimal
    errors: list[str] = []


class TrialBalanceDTO(BaseModel):
    """DTO - Bảng cân đối thử."""
    period_type: str
    period_value: int
    year: int
    accounts: list[dict]
    total_debit: Decimal
    total_credit: Decimal
    difference: Decimal


class FinancialStatementDTO(BaseModel):
    """DTO - Báo cáo tài chính (Phụ lục IV - TT99/2025)."""
    report_type: str  # BALANCE_SHEET, INCOME, CASH_FLOW
    report_period: str
    company_name: str
    tax_code: str
    currency: str = "VND"
    values: dict  # Các chỉ tiêu theo mẫu TT99


class SigningRequestDTO(BaseModel):
    """DTO - Yêu cầu ký số."""
    voucher_id: UUID
    signer_id: str
    signature_provider: str = Field(default="USB", description="USB hoặc CLOUD")


class LockPeriodRequestDTO(BaseModel):
    """DTO - Khóa kỳ kế toán."""
    period_type: str  # MONTH, QUARTER, YEAR
    year: int
    period_value: int


class AuditLogResponseDTO(BaseModel):
    """DTO - Audit log."""
    id: UUID
    user_id: str
    action: str
    entity_type: str
    entity_id: UUID
    old_value: str | None
    new_value: str | None
    ip_address: str | None
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)


class ProvisionCalculationDTO(BaseModel):
    """DTO - Tính dự phòng nợ phải thu."""
    calculation_date: date
    customer_id: UUID | None
    customer_code: str | None
    original_amount: Decimal
    overdue_days: int
    provision_rate: Decimal
    provision_amount: Decimal
    provision_type: str  # CỤ_THỂ, CHUNG


class InventoryReconciliationDTO(BaseModel):
    """DTO - Đối chiếu kiểm kê."""
    product_code: str
    product_name: str
    unit: str
    book_quantity: Decimal
    actual_quantity: Decimal
    difference: Decimal
    unit_cost: Decimal
    difference_amount: Decimal
    account_code: str  # 1381 (thiếu) hoặc 3381 (thừa)
