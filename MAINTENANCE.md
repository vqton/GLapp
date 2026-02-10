# Maintenance Guide - ERP Kế toán Thông tư 99/2025/TT-BTC

## 1. Cập nhật Phụ lục khi có thay đổi

### Cấu trúc Config

```python
# app/config/tt99_config.py
from dataclasses import dataclass
from typing import List, Dict
from decimal import Decimal

@dataclass
class AccountTemplate:
    code: str
    name: str
    account_type: str
    parent_code: str | None
    is_detail: bool
    balance_direction: str  # DEBIT hoặc CREDIT

@dataclass
class ReportTemplate:
    report_type: str  # BALANCE_SHEET, INCOME, CASH_FLOW
    line_code: str
    line_name: str
    formula: str
    account_codes: List[str] | None

# Config cho Phụ lục II - Hệ thống tài khoản
CHART_OF_ACCOUNTS: List[AccountTemplate] = [
    AccountTemplate("111", "Tiền mặt", "ASSET", None, True, "DEBIT"),
    AccountTemplate("1111", "Tiền Việt Nam", "ASSET", "111", True, "DEBIT"),
    AccountTemplate("1112", "Ngoại tệ", "ASSET", "111", True, "DEBIT"),
    # ... thêm các tài khoản khác
]

# Config cho Phụ lục IV - Mẫu Báo cáo tài chính
FINANCIAL_REPORTS: Dict[str, List[ReportTemplate]] = {
    "BALANCE_SHEET": [
        ReportTemplate("BALANCE_SHEET", "Mã số 100", "A. TÀI SẢN", "SUM", ["111", "112", "121"]),
        ReportTemplate("BALANCE_SHEET", "Mã số 110", "I. Tiền", "SUM", ["111", "112"]),
        ReportTemplate("BALANCE_SHEET", "Mã số 111", "1. Tiền mặt", "SUM", ["1111"]),
        # ... thêm các dòng khác
    ]
}
```

### API cập nhật config

```python
# app/api/routers/config.py
from fastapi import APIRouter, Depends

router = APIRouter(prefix="/api/v1/config", tags=["Config"])

@router.get("/accounts")
def get_chart_of_accounts():
    """Lấy hệ thống tài khoản theo Phụ lục II."""
    return {
        "version": "TT99_2025",
        "effective_date": "2026-01-01",
        "accounts": CHART_OF_ACCOUNTS
    }

@router.get("/reports/templates")
def get_report_templates():
    """Lấy mẫu báo cáo theo Phụ lục IV."""
    return {
        "version": "TT99_2025",
        "effective_date": "2026-01-01",
        "reports": FINANCIAL_REPORTS
    }

@router.post("/accounts/update")
def update_chart_of_accounts(accounts: List[AccountTemplate]):
    """
    Cập nhật tài khoản khi có thay đổi từ Bộ Tài chính.
    Chỉ admin mới được thực hiện.
    """
    # Validate và lưu vào database
    # Ghi audit log
    pass
```

## 2. Backup Scripts

### Windows PowerShell - Daily Backup

```powershell
# scripts/backup.ps1
# Script backup tự động hàng ngày
# Lưu trữ ≥10 năm theo quy định

param(
    [string]$BackupDir = "D:\ERP\Backups",
    [string]$DataDir = "C:\ERP\data",
    [int]$RetentionDays = 3650  # 10 năm
)

$ErrorActionPreference = "Stop"

# Tạo thư mục backup nếu chưa có
if (-not (Test-Path $BackupDir)) {
    New-Item -ItemType Directory -Path $BackupDir -Force
}

# Tạo thư mục theo ngày
$Date = Get-Date -Format "yyyyMMdd"
$Timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$BackupPath = Join-Path $BackupDir $Date

if (-not (Test-Path $BackupPath)) {
    New-Item -ItemType Directory -Path $BackupPath -Force
}

# Thông tin backup
$BackupInfo = @{
    BackupDate = $Date
    BackupTime = $Timestamp
    DatabaseType = "SQLite"
    SourcePath = $DataDir
}

try {
    # 1. Backup SQLite database
    $DbFile = Join-Path $DataDir "accounting.db"
    if (Test-Path $DbFile) {
        $DbBackup = Join-Path $BackupPath "accounting_$Timestamp.db"
        Copy-Item -Path $DbFile -Destination $DbBackup
        $BackupInfo.DatabaseFile = $DbBackup
        Write-Host "[OK] Database backed up: $DbBackup"
    }
    
    # 2. Backup configuration
    $ConfigFiles = @(".env", "pyproject.toml")
    foreach ($configFile in $ConfigFiles) {
        $ConfigPath = Join-Path "C:\ERP" $configFile
        if (Test-Path $ConfigPath) {
            $ConfigBackup = Join-Path $BackupPath $configFile
            Copy-Item -Path $ConfigPath -Destination $ConfigBackup
        }
    }
    
    # 3. Export audit logs to JSON
    $AuditExport = Join-Path $BackupPath "audit_logs_$Timestamp.json"
    $AuditExport | Out-File
    # Trong thực tế: gọi API export audit logs
    
    # 4. Tạo manifest
    $ManifestFile = Join-Path $BackupPath "manifest.json"
    $BackupInfo | ConvertTo-Json -Depth 3 | Out-File -FilePath $ManifestFile
    
    # 5. Nén backup (optional)
    $ZipFile = Join-Path $BackupDir "ERP_Backup_$Date.zip"
    Compress-Archive -Path "$BackupPath\*" -DestinationPath $ZipFile -Force
    
    # 6. Cleanup old backups
    $CutoffDate = (Get-Date).AddDays(-$RetentionDays)
    Get-ChildItem -Path $BackupDir -Directory | Where-Object {
        $_.CreationTime -lt $CutoffDate
    } | Remove-Item -Recurse -Force
    
    Write-Host "[SUCCESS] Backup completed at $BackupPath"
    
    # Ghi log
    $LogFile = Join-Path $BackupDir "backup.log"
    "[$Timestamp] Backup completed successfully" | Out-File -FilePath $LogFile -Append
    
} catch {
    Write-Error "[ERROR] Backup failed: $_"
    
    $LogFile = Join-Path $BackupDir "backup.log"
    "[$Timestamp] Backup FAILED: $_" | Out-File -FilePath $LogFile -Append
    
    # Gửi thông báo (email, Slack, etc.)
    # Send-MailMessage ...
    
    exit 1
}
```

### Task Scheduler Setup

```powershell
# Tạo Scheduled Task cho backup hàng ngày

# Run as Administrator
$action = New-ScheduledTaskAction -Execute "powershell.exe" `
    -Argument "-ExecutionPolicy Bypass -File C:\ERP\scripts\backup.ps1"

$trigger = New-ScheduledTaskTrigger -Daily -At "02:00"  # 2 AM

$settings = New-ScheduledTaskSettingsSet `
    -StartWhenAvailable `
    -DontStopIfOnBatteries `
    -WakeToRun

Register-ScheduledTask `
    -TaskName "ERP_Daily_Backup" `
    -Action $action `
    -Trigger $trigger `
    -Settings $settings `
    -RunLevel Highest `
    -Description "Daily backup cho ERP Kế toán"
```

### PostgreSQL Backup (Production)

```powershell
# backup_postgres.ps1
param(
    [string]$BackupDir = "D:\ERP\Backups",
    [string]$PGHost = "localhost",
    [string]$PGPort = "5432",
    [string]$PGDatabase = "vn_accounting",
    [string]$PGUser = "erp_backup",
    [int]$RetentionDays = 3650
)

$Date = Get-Date -Format "yyyyMMdd"
$Timestamp = Get-Date -Format "yyyyMMdd_HHmmss"

$BackupFile = Join-Path $BackupDir "postgres_$Date\vn_accounting_$Timestamp.dump"

# Tạo thư mục
if (-not (Test-Path (Join-Path $BackupDir $Date))) {
    New-Item -ItemType Directory -Path (Join-Path $BackupDir $Date) -Force
}

try {
    # Dump PostgreSQL
    & pg_dump -h $PGHost -p $PGPort -U $PGUser -F c -b -f $BackupFile $PGDatabase
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] PostgreSQL backup completed: $BackupFile"
    } else {
        throw "pg_dump failed with exit code $LASTEXITCODE"
    }
    
    # Cleanup old backups
    $CutoffDate = (Get-Date).AddDays(-$RetentionDays)
    Get-ChildItem -Path $BackupDir -Directory | Where-Object {
        $_.Name -match "^\d{8}$" -and $_.CreationTime -lt $CutoffDate
    } | Remove-Item -Recurse -Force
    
} catch {
    Write-Error "[ERROR] PostgreSQL backup failed: $_"
    exit 1
}
```

### Restore Script

```powershell
# restore.ps1
param(
    [string]$BackupDate,  # Format: yyyyMMdd
    [string]$BackupDir = "D:\ERP\Backups",
    [string]$DataDir = "C:\ERP\data",
    [switch]$Force
)

$BackupPath = Join-Path $BackupDir $BackupDate

if (-not (Test-Path $BackupPath)) {
    Write-Error "Backup not found: $BackupPath"
    exit 1
}

Write-Host "Starting restore from: $BackupPath"
Write-Host "Target data directory: $DataDir"

if (-not $Force) {
    $confirm = Read-Host "Continue? (y/n)"
    if ($confirm -ne "y") {
        exit 0
    }
}

try {
    # 1. Stop ERP service
    C:\"Program Files"\nssm\nssm.exe stop ERPService
    Start-Sleep -Seconds 5
    
    # 2. Backup current data
    $CurrentBackup = Join-Path $BackupDir "pre_restore_$(Get-Date -Format 'yyyyMMdd_HHmmss')"
    if (Test-Path $DataDir) {
        Copy-Item -Path $DataDir -Destination $CurrentBackup -Recurse
        Write-Host "Current data backed up to: $CurrentBackup"
    }
    
    # 3. Restore database
    $DbBackup = Get-ChildItem -Path $BackupPath -Filter "*.db" | Select-Object -First 1
    if ($DbBackup) {
        $TargetDb = Join-Path $DataDir "accounting.db"
        Copy-Item -Path $DbBackup.FullName -Destination $TargetDb -Force
        Write-Host "Database restored: $TargetDb"
    }
    
    # 4. Restore config
    $ConfigFiles = @(".env", "pyproject.toml")
    foreach ($configFile in $ConfigFiles) {
        $ConfigBackup = Join-Path $BackupPath $configFile
        if (Test-Path $ConfigBackup) {
            Copy-Item -Path $ConfigBackup -Destination "C:\ERP\$configFile" -Force
        }
    }
    
    # 5. Start ERP service
    C:\"Program Files"\nssm\nssm.exe start ERPService
    
    Write-Host "[SUCCESS] Restore completed"
    
} catch {
    Write-Error "[ERROR] Restore failed: $_"
    exit 1
}
```

## 3. API Versioning

```python
# app/api/__init__.py
from fastapi import APIRouter

api_v1 = APIRouter(prefix="/api/v1", tags=["API v1"])

# Import routers
from app.api.routers import auth, vouchers, reports

api_v1.include_router(auth.router)
api_v1.include_router(vouchers.router)
api_v1.include_router(reports.router)

# app/main.py
from fastapi import FastAPI

app = FastAPI(
    title="VN Accounting ERP API",
    version="1.0.0",  # API version
    deprecated=False
)

from app.api import api_v1
app.include_router(api_v1)

# Version endpoint
@app.get("/api/version")
def get_api_version():
    return {
        "api_version": "1.0.0",
        "api_name": "VN Accounting ERP",
        "regulation": "Thông tư 99/2025/TT-BTC",
        "deprecated": False
    }
```

## 4. Smoke Tests

```powershell
# scripts/smoke_test.ps1
param(
    [string]$BaseUrl = "http://localhost:8000",
    [int]$Timeout = 30
)

$ErrorActionPreference = "Continue"
$TestsPassed = 0
$TestsFailed = 0

function Test-Endpoint {
    param([string]$Name, [string]$Url, [string]$ExpectedStatus = "200")
    
    try {
        $response = Invoke-RestMethod -Uri $Url -TimeoutSec $Timeout
        Write-Host "[PASS] $Name" -ForegroundColor Green
        $script:TestsPassed++
        return $true
    } catch {
        Write-Host "[FAIL] $Name: $_" -ForegroundColor Red
        $script:TestsFailed++
        return $false
    }
}

Write-Host "=== ERP Smoke Tests ===" -ForegroundColor Cyan
Write-Host ""

# 1. Health check
Test-Endpoint "Health Check" "$BaseUrl/health"

# 2. API root
Test-Endpoint "API Root" "$BaseUrl/"

# 3. Auth endpoints
Test-Endpoint "Login Page" "$BaseUrl/docs"

# 4. Database connectivity
$health = Invoke-RestMethod -Uri "$BaseUrl/health"
if ($health.database -eq "connected") {
    Write-Host "[PASS] Database Connected" -ForegroundColor Green
    $TestsPassed++
} else {
    Write-Host "[FAIL] Database Not Connected" -ForegroundColor Red
    $TestsFailed++
}

# 5. Quick voucher test (requires auth)
# ...

Write-Host ""
Write-Host "=== Results ===" -ForegroundColor Cyan
Write-Host "Passed: $TestsPassed" -ForegroundColor Green
Write-Host "Failed: $TestsFailed" -ForegroundColor $(if ($TestsFailed -gt 0) { "Red" } else { "Green" })

if ($TestsFailed -gt 0) {
    exit 1
}
```

## 5. Log Rotation

```python
# scripts/log_rotation.py
import os
import shutil
from datetime import datetime, timedelta
import glob

def rotate_logs(
    log_dir: str = "C:\\ERP\\logs",
    max_age_days: int = 90,
    max_total_size_mb: int = 500
):
    """Xoay log files."""
    
    cutoff_date = datetime.now() - timedelta(days=max_age_days)
    
    # Xoá log cũ
    for log_file in glob.glob(os.path.join(log_dir, "*.log")):
        mtime = datetime.fromtimestamp(os.path.getmtime(log_file))
        if mtime < cutoff_date:
            os.remove(log_file)
            print(f"Deleted: {log_file}")
    
    # Kiểm tra kích thước
    total_size = sum(
        os.path.getsize(f) 
        for f in glob.glob(os.path.join(log_dir, "*.log"))
    )
    
    if total_size > max_total_size_mb * 1024 * 1024:
        # Nén các log cũ nhất
        log_files = sorted(
            glob.glob(os.path.join(log_dir, "*.log")),
            key=os.path.getmtime
        )[:-5]  # Giữ lại 5 file gần nhất
        
        for log_file in log_files:
            shutil.make_archive(
                log_file.replace(".log", ""),
                'gzip',
                log_dir,
                os.path.basename(log_file)
            )
            os.remove(log_file)

if __name__ == "__main__":
    rotate_logs()
```

## 6. Monitoring Checklist Hàng ngày

```powershell
# daily_check.ps1
Write-Host "=== Daily ERP Health Check ===" -ForegroundColor Cyan

# 1. Service status
$serviceStatus = C:\"Program Files"\nssm\nssm.exe status ERPService
if ($serviceStatus -eq "SERVICE_RUNNING") {
    Write-Host "[OK] ERP Service is running" -ForegroundColor Green
} else {
    Write-Host "[WARN] ERP Service is not running: $serviceStatus" -ForegroundColor Yellow
}

# 2. Disk space
$disk = Get-WmiObject -Class Win32_LogicalDisk -Filter "DeviceID='C:'"
$freeSpaceGB = [math]::Round($disk.FreeSpace / 1GB, 2)
if ($freeSpaceGB -lt 10) {
    Write-Host "[WARN] Low disk space: ${freeSpaceGB}GB" -ForegroundColor Yellow
} else {
    Write-Host "[OK] Disk space: ${freeSpaceGB}GB free" -ForegroundColor Green
}

# 3. Backup status
$lastBackup = Get-ChildItem -Path "D:\ERP\Backups" -Directory | Sort-Object Name -Descending | Select-Object -First 1
if ($lastBackup) {
    $backupDate = [datetime]::ParseExact($lastBackup.Name, 'yyyyMMdd', $null)
    $daysSince = (Get-Date).Subtract($backupDate).Days
    if ($daysSince -le 1) {
        Write-Host "[OK] Backup completed today" -ForegroundColor Green
    } else {
        Write-Host "[WARN] Last backup was $daysSince days ago" -ForegroundColor Yellow
    }
} else {
    Write-Host "[ERROR] No backup found" -ForegroundColor Red
}

# 4. API health
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8000/health" -TimeoutSec 5
    Write-Host "[OK] API responding" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] API not responding: $_" -ForegroundColor Red
}

# 5. Database size
$dbSize = Get-Item "C:\ERP\data\accounting.db" -ErrorAction SilentlyContinue
if ($dbSize) {
    $sizeMB = [math]::Round($dbSize.Length / 1MB, 2)
    Write-Host "[INFO] Database size: ${sizeMB}MB" -ForegroundColor Cyan
}
```

## 7. Emergency Procedures

```powershell
# emergency_stop.ps1
Write-Host "[EMERGENCY] Stopping ERP Services..." -ForegroundColor Red

# 1. Dừng service
C:\"Program Files"\nssm\nssm.exe stop ERPService

# 2. Ngắn kết nối mới
# (Trong thực tế: cấu hình IIS để hiển thị trang bảo trì)

# 3. Backup khẩn cấp
$EmergencyBackup = Join-Path "D:\ERP\Backups" "emergency_$(Get-Date -Format 'yyyyMMdd_HHmmss')"
Copy-Item -Path "C:\ERP\data" -Destination $EmergencyBackup -Recurse -Force

Write-Host "[EMERGENCY] Backup created: $EmergencyBackup" -ForegroundColor Yellow
Write-Host "[EMERGENCY] Services stopped. Contact admin." -ForegroundColor Red
```
