"""
Main FastAPI application - ERP Kế toán theo Thông tư 99/2025/TT-BTC.
"""

from collections.abc import AsyncGenerator
from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from app.api.routers import reports, vouchers
from app.infrastructure.database import init_db


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncGenerator:
    """Application lifespan - startup and shutdown events."""
    init_db()
    yield


app = FastAPI(
    title="VN Accounting ERP API",
    description="""
## ERP Kế toán theo Thông tư 99/2025/TT-BTC

### Tính năng chính:
- **Chứng từ kế toán**: Tạo, lưu trữ, ký số điện tử (Phụ lục I)
- **Hệ thống tài khoản**: 71 TK cấp 1, 101 TK cấp 2 (Phụ lục II)
- **Ghi sổ kép**: Kiểm tra cân đối Nợ = Có (Phụ lục III)
- **Báo cáo tài chính**: Báo cáo tình hình Tài chính, Kết quả Kinh doanh, Lưu chuyển tiền tệ (Phụ lục IV)
- **Dự phòng nợ phải thu** (Điều 32)
- **Tỷ giá ngoại tệ** (Điều 31)
- **Quản lý kho** FIFO/LIFO/Trung bình

### Nguyên tắc:
- Mỗi chứng từ chỉ phát sinh một lần
- Sau khóa sổ không cho phép chỉnh sửa dữ liệu cũ
- Audit trail đầy đủ mọi thay đổi
    """,
    version="0.1.0",
    lifespan=lifespan
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(vouchers.router)
app.include_router(reports.router)


@app.get("/")
def root():
    return {
        "name": "VN Accounting ERP API",
        "version": "0.1.0",
        "regulation": "Thông tư 99/2025/TT-BTC",
        "docs": "/docs"
    }


@app.get("/health")
def health_check():
    """Health check endpoint."""
    return {"status": "healthy", "database": "connected"}


@app.exception_handler(ValueError)
async def value_error_handler(request: Request, exc: ValueError):
    """Handle validation errors."""
    return JSONResponse(
        status_code=400,
        content={"detail": str(exc)}
    )


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
