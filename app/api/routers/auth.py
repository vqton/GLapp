"""
Authentication & Security API Endpoints.
"""

from datetime import datetime, timedelta
from typing import Optional
from uuid import UUID

from fastapi import APIRouter, Depends, HTTPException, status, Request
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from pydantic import BaseModel

from app.core.security import (
    User,
    UserRole,
    AuditLog,
    AuditAction,
    RBACService,
    hash_password,
    verify_password,
)
from app.infrastructure.database import get_db

router = APIRouter(prefix="/api/v1/auth", tags=["Authentication"])

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/api/v1/auth/login")
rbac_service = RBACService()


class TokenResponse(BaseModel):
    access_token: str
    token_type: str
    expires_in: int


class UserCreateRequest(BaseModel):
    username: str
    password: str
    email: str
    full_name: str
    role: str


class UserResponse(BaseModel):
    id: UUID
    username: str
    email: str
    full_name: str
    role: str
    is_active: bool
    created_at: datetime


@router.post("/login", response_model=TokenResponse)
async def login(
    request: Request, form_data: OAuth2PasswordRequestForm = Depends(), db=Depends(get_db)
):
    """
    Đăng nhập - Hỗ trợ username/password và Windows Auth.
    """
    from app.infrastructure.database.models import User as DBUser, AuditLog as DBAuditLog

    user = db.query(DBUser).filter(DBUser.username == form_data.username).first()

    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid credentials",
            headers={"WWW-Authenticate": "Bearer"},
        )

    if user.locked_until and user.locked_until > datetime.utcnow():
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Account locked")

    pw_hash, salt = hash_password(form_data.password)
    if not verify_password(form_data.password, user.password_hash, salt):
        user.failed_login_attempts += 1
        if user.failed_login_attempts >= 5:
            user.locked_until = datetime.utcnow() + timedelta(hours=1)
        db.commit()

        audit = DBAuditLog(
            company_id=user.company_id,
            user_id=user.id,
            user_ip=request.client.host,
            user_agent=request.headers.get("user-agent", ""),
            entity_type="User",
            entity_id=user.id,
            action="LOGIN_FAILED",
            description=f"Failed login attempt: {user.failed_login_attempts}",
        )
        db.add(audit)
        db.commit()

        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid credentials",
            headers={"WWW-Authenticate": "Bearer"},
        )

    user.last_login = datetime.utcnow()
    user.failed_login_attempts = 0
    db.commit()

    from app.core.security import generate_jwt_token

    token = generate_jwt_token(user, os.getenv("JWT_SECRET", "demo_secret"))

    audit = DBAuditLog(
        company_id=user.company_id,
        user_id=user.id,
        user_ip=request.client.host,
        user_agent=request.headers.get("user-agent", ""),
        entity_type="User",
        entity_id=user.id,
        action="LOGIN",
    )
    db.add(audit)
    db.commit()

    return TokenResponse(access_token=token, token_type="Bearer", expires_in=8 * 3600)


@router.post("/refresh")
async def refresh_token(token: str = Depends(oauth2_scheme), db=Depends(get_db)):
    """Làm mới JWT token."""
    from app.core.security import decode_jwt_token, generate_jwt_token
    from app.infrastructure.database.models import User as DBUser

    payload = decode_jwt_token(token, os.getenv("JWT_SECRET", "demo_secret"))
    if not payload:
        raise HTTPException(status_code=401, detail="Invalid token")

    user = db.query(DBUser).filter(DBUser.id == UUID(payload["sub"])).first()
    if not user or not user.is_active:
        raise HTTPException(status_code=401, detail="User not found or inactive")

    new_token = generate_jwt_token(user, os.getenv("JWT_SECRET", "demo_secret"))
    return {"access_token": new_token, "token_type": "Bearer"}


@router.get("/me", response_model=UserResponse)
async def get_current_user(token: str = Depends(oauth2_scheme), db=Depends(get_db)):
    """Lấy thông tin user hiện tại."""
    from app.core.security import decode_jwt_token
    from app.infrastructure.database.models import User as DBUser

    payload = decode_jwt_token(token, os.getenv("JWT_SECRET", "demo_secret"))
    if not payload:
        raise HTTPException(status_code=401, detail="Invalid token")

    user = db.query(DBUser).filter(DBUser.id == UUID(payload["sub"])).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    return UserResponse(
        id=user.id,
        username=user.username,
        email=user.email,
        full_name=user.full_name,
        role=user.role,
        is_active=user.is_active,
        created_at=user.created_at,
    )


@router.post("/change-password")
async def change_password(
    old_password: str,
    new_password: str,
    token: str = Depends(oauth2_scheme),
    request: Request = None,
    db=Depends(get_db),
):
    """Đổi mật khẩu."""
    from app.core.security import decode_jwt_token, hash_password, verify_password
    from app.infrastructure.database.models import User as DBUser, AuditLog as DBAuditLog

    payload = decode_jwt_token(token, os.getenv("JWT_SECRET", "demo_secret"))
    if not payload:
        raise HTTPException(status_code=401, detail="Invalid token")

    user = db.query(DBUser).filter(DBUser.id == UUID(payload["sub"])).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    if not verify_password(old_password, user.password_hash, os.urandom(16)):
        raise HTTPException(status_code=400, detail="Invalid old password")

    new_hash, salt = hash_password(new_password)
    user.password_hash = new_hash
    db.commit()

    audit = DBAuditLog(
        company_id=user.company_id,
        user_id=user.id,
        user_ip=request.client.host if request else "",
        user_agent="",
        entity_type="User",
        entity_id=user.id,
        action="PASSWORD_CHANGE",
    )
    db.add(audit)
    db.commit()

    return {"message": "Password changed successfully"}


@router.get("/permissions")
async def get_my_permissions(token: str = Depends(oauth2_scheme)):
    """Lấy danh sách quyền của user hiện tại."""
    from app.core.security import decode_jwt_token

    payload = decode_jwt_token(token, os.getenv("JWT_SECRET", "demo_secret"))
    if not payload:
        raise HTTPException(status_code=401, detail="Invalid token")

    role = UserRole(payload["role"])
    permissions = rbac_service.get_user_permissions(role)

    return {
        "role": role.value,
        "permissions": [p.value for p in permissions],
        "can_sign": rbac_service.can_sign_voucher(role),
        "can_lock_period": rbac_service.can_lock_period(role),
        "can_declare_tax": rbac_service.can_declare_tax(role),
    }
