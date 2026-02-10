# AGENTS.md - VN Accounting ERP Development Guide

## Project Overview
ERP Kế toán theo Thông tư 99/2025/TT-BTC dành cho doanh nghiệp thương mại và dịch vụ (SME <100 nhân viên).

## Tech Stack
- **Framework**: FastAPI + SQLModel (SQLAlchemy ORM)
- **Database**: SQLite (dev), PostgreSQL (production)
- **Migration**: Alembic
- **Testing**: pytest + hypothesis (property-based testing)
- **Type Checking**: mypy
- **Linting**: ruff

## Build/Lint/Test Commands

### Virtual Environment
```bash
# Create virtual environment
python -m venv .venv
source .venv/bin/activate  # Linux/Mac
.venv\Scripts\activate     # Windows

# Install dependencies
pip install -e ".[dev]"
```

### Running Tests
```bash
# Run all tests
pytest

# Run single test file
pytest tests/unit/test_accounting_domain.py

# Run single test
pytest tests/unit/test_accounting_domain.py::TestBalanceCheck::test_balanced_entry_debit_equals_credit

# Run with coverage
pytest --cov=app --cov-report=html

# Run with hypothesis (property-based)
pytest --hypothesis

# Run specific test categories
pytest tests/unit/              # Unit tests only
pytest tests/integration/       # Integration tests
```

### Linting & Type Checking
```bash
# Lint with ruff
ruff check app/ tests/

# Auto-fix
ruff check --fix app/ tests/

# Type checking
mypy app/

# Format code
ruff format app/ tests/
```

### Database Operations
```bash
# Initialize database
python -m app.infrastructure.database

# Create migration
alembic revision -m "initial_migration"

# Apply migrations
alembic upgrade head

# Seed default accounts
python -m app.infrastructure.database seed_default_accounts <company_id>
```

### Running the Application
```bash
# Development
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000

# Production
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

## Code Style Guidelines

### Naming Conventions
- **Classes**: PascalCase (e.g., `AccountingVoucher`, `JournalEntry`)
- **Variables/Functions/Methods**: snake_case (e.g., `calculate_totals`, `total_debit`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `DEFAULT_CURRENCY = "VND"`)
- **Private methods**: prefix with `_` (e.g., `_validate_balance`)
- **Internal modules**: prefix with `_` (e.g., `_internal_utils`)

### Import Order
```python
# Standard library
from datetime import date, datetime
from decimal import Decimal
from typing import Optional

# Third party
from fastapi import APIRouter, Depends
from pydantic import BaseModel, Field

# Local application
from app.domain.entities import Account, JournalEntry
from app.application.dto import AccountingDTO
from app.infrastructure.database import get_db
```

### Type Hints (Required)
```python
# Always use type hints for function signatures
def calculate_vat(amount: Decimal, rate: Decimal) -> Decimal:
    return amount * rate / 100

# Use Optional for nullable types
def get_account(code: str) -> Optional[Account]:
    ...

# Use Union for multiple types
def parse_value(value: Union[str, int, Decimal]) -> Decimal:
    ...

# Use Protocol/ABC for interfaces
class IAccountRepository(ABC):
    @abstractmethod
    def get_by_code(self, code: str) -> Optional[Account]:
        ...
```

### Error Handling
```python
# Use custom exceptions for domain errors
class AccountingError(Exception):
    """Base exception for accounting errors."""
    pass

class VoucherNotBalancedError(AccountingError):
    def __init__(self, debit: Decimal, credit: Decimal):
        super().__init__(f"Voucher not balanced: Debit={debit}, Credit={credit}")

# Raise with context
def validate_balance(entry: JournalEntry) -> None:
    if not entry.is_balanced():
        raise VoucherNotBalancedError(entry.total_debit, entry.total_credit)
```

### Domain-Driven Design
- **Domain Layer** (`app/domain/`): Pure Python, business logic only, no dependencies on frameworks
- **Application Layer** (`app/application/`): Use cases, DTOs, services
- **Infrastructure Layer** (`app/infrastructure/`): Database, external services, file I/O
- **API Layer** (`app/api/`): FastAPI routers, endpoints

### Docstrings (Google Style)
```python
def calculate_specific_provision(
    receivables: list[dict],
    overdue_days: int
) -> Money:
    """Calculate specific provision for overdue receivables.
    
    Args:
        receivables: List of receivable items with 'amount' and 'overdue_days'
        overdue_days: Total overdue days for calculation
    
    Returns:
        Money: Total provision amount
    
    Raises:
        ValueError: If receivables list is empty
    """
```

### File Structure
```
app/
├── domain/          # Pure business logic
│   ├── entities.py  # Domain entities
│   ├── services.py  # Domain services
│   └── value_objects.py
├── application/      # Use cases
│   ├── dto/         # Data transfer objects
│   └── usecases/
├── infrastructure/  # External concerns
│   └── database/    # SQLModel models
├── api/             # FastAPI
│   └── routers/
└── core/            # Config, exceptions
```

### Database Models (SQLModel)
```python
class Account(SQLModel, table=True):
    id: UUID = Field(default_factory=uuid4, primary_key=True)
    code: str = Field(index=True)
    name: str
    account_type: str
    current_balance: Decimal | None = None
    
    # Relationships
    journal_lines: list["JournalEntryLine"] = Relationship(back_populates="account")
```

### Business Rules Compliance (TT99/2025)
- All vouchers must balance (Debit = Credit)
- Vouchers are immutable after signing
- Period locking prevents modifications to closed periods
- Audit trail required for all changes (user_id, action, old/new value)
- Chart of accounts follows Phụ lục II (71 TK cấp 1)
- Financial reports follow Phụ lục IV templates

### Critical Business Logic Tests
Required ≥90% coverage for:
- Balance validation (Nợ = Có)
- VAT calculation (TK 333)
- Receivable provisions (Điều 32)
- Exchange rate differences (Điều 31)
- Revenue recognition for services
- Inventory cost methods (FIFO/LIFO/Weighted Avg)

### API Response Patterns
```python
# Success response
{
    "status": "success",
    "data": {...},
    "message": "Operation completed"
}

# Error response
{
    "status": "error",
    "detail": "Error description",
    "code": "ERROR_CODE"
}
```

### Configuration
Environment variables in `.env`:
```
DATABASE_TYPE=sqlite
DATABASE_PATH=./data/accounting.db
API_HOST=0.0.0.0
API_PORT=8000
```
