# Deployment Guide - ERP Kế toán Thông tư 99/2025/TT-BTC
# Triển khai trực tiếp trên Windows (không Docker)

## Yêu cầu hệ thống

### Phần cứng tối thiểu
| Thành phần | Tối thiểu | Khuyến nghị |
|------------|-----------|-------------|
| CPU | 2 cores | 4+ cores |
| RAM | 4 GB | 8-16 GB |
| Disk | 50 GB SSD | 100+ GB SSD |
| OS | Windows 10 Pro | Windows Server 2019+ |

### Phần mềm yêu cầu
- Python 3.10+ (tải tại python.org)
- PostgreSQL 14+ (production) hoặc SQLite (dev)
- Git for Windows
- Visual C++ Build Tools (cho PyJWT, cryptography)

## Bước 1: Chuẩn bị môi trường

```powershell
# Mở PowerShell với quyền Administrator

# 1. Kiểm tra Python
python --version
# Output: Python 3.10.x

# 2. Tạo thư mục cài đặt
New-Item -ItemType Directory -Path "C:\ERP" -Force
cd C:\ERP

# 3. Clone hoặc copy source code vào thư mục
```

## Bước 2: Cài đặt Python Virtual Environment

```powershell
# Tạo virtual environment
python -m venv venv

# Activate venv
.\venv\Scripts\Activate.ps1

# Nâng cấp pip
python -m pip install --upgrade pip

# Cài đặt dependencies
pip install -e ".[dev]"

# Cài đặt Gunicorn (cho production)
pip install gunicorn
```

## Bước 3: Cấu hình môi trường

```powershell
# Tạo file .env
notepad .env

# Nội dung .env:
DATABASE_TYPE=sqlite
DATABASE_PATH=C:\ERP\data\accounting.db
API_HOST=0.0.0.0
API_PORT=8000
JWT_SECRET=your-super-secret-key-change-in-production
ENCRYPTION_KEY=your-32-byte-key-here
LOG_LEVEL=INFO

# Cho PostgreSQL (production):
# DATABASE_TYPE=postgresql
# DB_HOST=localhost
# DB_PORT=5432
# DB_NAME=vn_accounting
# DB_USER=erp_user
# DB_PASSWORD=your-password
```

## Bước 4: Khởi tạo Database

```powershell
# Tạo thư mục data
New-Item -ItemType Directory -Path "C:\ERP\data" -Force

# Initialize database và seed dữ liệu mặc định
python -m app.infrastructure.database

# Hoặc với PostgreSQL:
# createdb -U postgres vn_accounting
# python -m app.infrastructure.database
```

## Bước 5: Triển khai với Gunicorn (Khuyến nghị Production)

```powershell
# Tạo Windows Service script
notepad C:\ERP\erp_service.py

# Nội dung:
$nssm = "C:\Program Files\nssm\nssm-2.24\win64\nssm.exe"

# Hoặc dùng Gunicorn directly:
gunicorn -w 4 -k uvicorn.workers.UvicornWorker --bind 0.0.0.0:8000 app.main:app --daemon
```

### Cấu hình Windows Service (NSSM)

```powershell
# Tải NSSM từ https://nssm.cc/download
# Giải nén vào C:\Program Files\nssm

# Cài đặt service
C:\Program Files\nssm\nssm.exe install ERPService `
    "C:\ERP\venv\Scripts\python.exe" `
    "-m", "gunicorn", "-w", "4", "-k", "uvicorn.workers.UvicornWorker", "--bind", "0.0.0.0:8000", "app.main:app"

# Thiết lập working directory
C:\Program Files\nssm\nssm.exe set ERPService AppDirectory "C:\ERP"

# Thiết lập log
C:\Program Files\nssm\nssm.exe set ERPService AppStdout "C:\ERP\logs\stdout.log"
C:\Program Files\nssm\nssm.exe set ERPService AppStderr "C:\ERP\logs\stderr.log"
C:\Program Files\nssm\nssm.exe set ERPService AppStdoutCreationDisposition 4
C:\Program Files\nssm\nssm.exe set ERPService AppStderrCreationDisposition 4

# Tạo thư mục logs
New-Item -ItemType Directory -Path "C:\ERP\logs" -Force

# Khởi động service
C:\Program Files\nssm\nssm.exe start ERPService

# Kiểm tra trạng thái
C:\Program Files\nssm\nssm.exe status ERPService
```

## Bước 6: Cấu hình IIS (Optional - Reverse Proxy)

```powershell
# Cài đặt IIS với URL Rewrite Module
# Download: https://www.iis.net/downloads/microsoft/url-rewrite

# Tạo web.config trong C:\ERP
notepad C:\ERP\web.config

# Nội dung:
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <system.webServer>
        <rewrite>
            <rules>
                <rule name="ReverseProxy" stopProcessing="true">
                    <match url="(.*)" />
                    <action type="Rewrite" url="http://localhost:8000/{R:1}" />
                </rule>
            </rules>
        </rewrite>
        <proxy enabled="true" />
    </system.webServer>
</configuration>

# Application Pool: No Managed Code
# Bindings: Port 80 (HTTP) hoặc 443 (HTTPS)
```

## Bước 7: Firewall Configuration

```powershell
# Mở port 8000 cho API
New-NetFirewallRule -DisplayName "ERP API" -Direction Inbound -Protocol TCP `
    -LocalPort 8000 -Action Allow

# Hoặc qua GUI:
# Windows Defender Firewall > Advanced Settings > Inbound Rules > New Rule
```

## Bước 8: Xác minh cài đặt

```powershell
# Kiểm tra API đang chạy
Invoke-WebRequest -Uri http://localhost:8000/ | Select-Object StatusCode

# Kiểm tra health endpoint
Invoke-WebRequest -Uri http://localhost:8000/health | Select-Object StatusCode

# Output mong đợi: 200
```

## Cấu trúc thư mục sau cài đặt

```
C:\ERP\
├── venv\                    # Python virtual environment
├── data\
│   ├── accounting.db        # SQLite database
│   └── backups\            # Backup files
├── logs\                   # Application logs
├── app\                    # Source code
├── tests\                  # Unit tests
├── scripts\                # Utility scripts
├── .env                   # Environment configuration
├── pyproject.toml         # Project config
└── README.md
```

## Troubleshooting

```powershell
# Kiểm tra logs
Get-Content C:\ERP\logs\stderr.log -Tail 50

# Restart service
C:\Program Files\nssm\nssm.exe restart ERPService

# Kiểm tra port đang listening
netstat -ano | findstr :8000

# Kiểm tra Python packages
pip list | findstr -i vn-accounting
```

## Production Checklist

- [ ] Thay đổi JWT_SECRET (32+ characters)
- [ ] Cấu hình HTTPS/SSL certificate
- [ ] Thiết lập PostgreSQL thay vì SQLite
- [ ] Cấu hình automated backup (xem script bên dưới)
- [ ] Thiết lập log rotation
- [ ] Cấu hình monitoring (Prometheus/Grafana)
- [ ] Backup encryption key lưu trữ an toàn
