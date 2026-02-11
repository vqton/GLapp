@echo off
REM Script cài đặt PostgreSQL database cho AccountingERP trên Windows

set DB_USER=accounting
set DB_NAME=accounting_erp
set DB_PASSWORD=postgres
set DB_HOST=localhost
set PGPASSWORD=%DB_PASSWORD%

echo ==========================================
echo Initializing AccountingERP PostgreSQL DB
echo ==========================================

REM Kiểm tra PostgreSQL
where psql >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [!] PostgreSQL khong duoc cai dat!
    echo Tai tai: https://www.postgresql.org/download/windows/
    pause
    exit /b 1
)

echo [OK] PostgreSQL tim thay

REM Tạo user và database
echo [*] Tao database '%DB_NAME%'...
psql -U postgres -h %DB_HOST% << EOF
CREATE USER IF NOT EXISTS %DB_USER% WITH PASSWORD '%DB_PASSWORD%';
CREATE DATABASE IF NOT EXISTS %DB_NAME% OWNER %DB_USER%;
GRANT CONNECT ON DATABASE %DB_NAME% TO %DB_USER%;
GRANT USAGE ON SCHEMA public TO %DB_USER%;
GRANT CREATE ON SCHEMA public TO %DB_USER%;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO %DB_USER%;
EOF

if %ERRORLEVEL% EQU 0 (
    echo [OK] Database tao thanh cong
) else (
    echo [!] Loi khi tao database
    pause
    exit /b 1
)

REM Chạy script khởi tạo
echo [*] Chay script khoi tao schema...
psql -U %DB_USER% -h %DB_HOST% -d %DB_NAME% < init-db.sql

if %ERRORLEVEL% EQU 0 (
    echo [OK] Schema tao thanh cong
) else (
    echo [!] Loi khi tao schema
    pause
    exit /b 1
)

echo.
echo ==========================================
echo [OK] Cai dat hoan tat!
echo ==========================================
echo Connection String:
echo jdbc:postgresql://localhost:5432/accounting_erp
echo User: %DB_USER%
echo Password: %DB_PASSWORD%
echo.
pause
