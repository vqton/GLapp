"""
Security Core - RBAC, Audit Trail (Theo Thông tư 99/2025/TT-BTC)
"""

import os
import hashlib
import hmac
from datetime import datetime, date, timedelta
from enum import Enum
from typing import Optional
from uuid import UUID, uuid4
from dataclasses import dataclass


class UserRole(str, Enum):
    ADMIN = "ADMIN"
    ACCOUNTING_MANAGER = "ACC_MGR"
    ACCOUNTANT = "ACCOUNTANT"
    VIEWER = "VIEWER"
    AUDITOR = "AUDITOR"


class Permission(str, Enum):
    VOUCHER_CREATE = "VOUCHER_CREATE"
    VOUCHER_EDIT = "VOUCHER_EDIT"
    VOUCHER_DELETE = "VOUCHER_DELETE"
    VOUCHER_SIGN = "VOUCHER_SIGN"
    VOUCHER_LOCK = "VOUCHER_LOCK"
    JOURNAL_POST = "JOURNAL_POST"
    JOURNAL_REVERSE = "JOURNAL_REVERSE"
    REPORT_VIEW = "REPORT_VIEW"
    REPORT_EXPORT = "REPORT_EXPORT"
    TAX_DECLARE = "TAX_DECLARE"
    TAX_SIGN = "TAX_SIGN"


ROLE_PERMISSIONS: dict[UserRole, list[Permission]] = {
    UserRole.ADMIN: list(Permission),
    UserRole.ACCOUNTING_MANAGER: [
        Permission.VOUCHER_CREATE,
        Permission.VOUCHER_EDIT,
        Permission.VOUCHER_SIGN,
        Permission.VOUCHER_LOCK,
        Permission.JOURNAL_POST,
        Permission.JOURNAL_REVERSE,
        Permission.REPORT_VIEW,
        Permission.REPORT_EXPORT,
        Permission.TAX_DECLARE,
        Permission.TAX_SIGN,
    ],
    UserRole.ACCOUNTANT: [
        Permission.VOUCHER_CREATE,
        Permission.VOUCHER_EDIT,
        Permission.JOURNAL_POST,
        Permission.REPORT_VIEW,
    ],
    UserRole.VIEWER: [Permission.REPORT_VIEW],
    UserRole.AUDITOR: [Permission.REPORT_VIEW, Permission.REPORT_EXPORT],
}


@dataclass
class AuditLog:
    id: UUID = field(default_factory=uuid4)
    company_id: UUID
    user_id: UUID
    user_ip: str
    user_agent: str
    entity_type: str
    entity_id: UUID
    action: str
    old_value: Optional[str] = None
    new_value: Optional[str] = None
    description: Optional[str] = None
    reference_id: Optional[UUID] = None
    created_at: datetime = field(default_factory=datetime.utcnow)


class AuditAction(str, Enum):
    CREATE = "CREATE"
    UPDATE = "UPDATE"
    DELETE = "DELETE"
    SIGN = "SIGN"
    LOCK = "LOCK"
    UNLOCK = "UNLOCK"
    POST = "POST"
    REVERSE = "REVERSE"
    LOGIN = "LOGIN"
    LOGOUT = "LOGOUT"


def hash_password(password: str, salt: Optional[bytes] = None) -> tuple[str, bytes]:
    if salt is None:
        salt = os.urandom(16)
    pw_hash = hashlib.pbkdf2_hmac("sha256", password.encode(), salt, 100000)
    return pw_hash.hex(), salt


class RBACService:
    def has_permission(self, role: UserRole, permission: Permission) -> bool:
        return permission in ROLE_PERMISSIONS.get(role, [])

    def get_user_permissions(self, role: UserRole) -> list[Permission]:
        return ROLE_PERMISSIONS.get(role, [])

    def can_sign_voucher(self, role: UserRole) -> bool:
        return self.has_permission(role, Permission.VOUCHER_SIGN)

    def can_lock_period(self, role: UserRole) -> bool:
        return role in [UserRole.ADMIN, UserRole.ACCOUNTING_MANAGER]

    def can_declare_tax(self, role: UserRole) -> bool:
        return self.has_permission(role, Permission.TAX_DECLARE)
