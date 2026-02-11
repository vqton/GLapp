#!/bin/bash

# Script cài đặt PostgreSQL database cho AccountingERP trên Linux/Mac

DB_USER="accounting"
DB_NAME="accounting_erp"
DB_PASSWORD="postgres"
DB_HOST="localhost"

echo "=========================================="
echo "Initializing AccountingERP PostgreSQL DB"
echo "=========================================="

# Kiểm tra PostgreSQL đã cài chưa
if ! command -v psql &> /dev/null; then
    echo "❌ PostgreSQL không được cài đặt!"
    echo "Cài đặt: brew install postgresql (Mac) hoặc apt install postgresql (Linux)"
    exit 1
fi

echo "✓ PostgreSQL tìm thấy"

# Tạo database
echo "📁 Tạo database '$DB_NAME'..."
psql -U postgres -h $DB_HOST << EOF
CREATE USER IF NOT EXISTS $DB_USER WITH PASSWORD '$DB_PASSWORD';
CREATE DATABASE IF NOT EXISTS $DB_NAME OWNER $DB_USER;
GRANT CONNECT ON DATABASE $DB_NAME TO $DB_USER;
GRANT USAGE ON SCHEMA public TO $DB_USER;
GRANT CREATE ON SCHEMA public TO $DB_USER;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO $DB_USER;
EOF

if [ $? -eq 0 ]; then
    echo "✓ Database tạo thành công"
else
    echo "❌ Lỗi khi tạo database"
    exit 1
fi

# Chạy script khởi tạo
echo "📝 Chạy script khởi tạo schema..."
psql -U $DB_USER -h $DB_HOST -d $DB_NAME << EOF
$(cat init-db.sql)
EOF

if [ $? -eq 0 ]; then
    echo "✓ Schema tạo thành công"
else
    echo "❌ Lỗi khi tạo schema"
    exit 1
fi

echo ""
echo "=========================================="
echo "✅ Cài đặt hoàn tất!"
echo "=========================================="
echo "Connection String:"
echo "jdbc:postgresql://localhost:5432/accounting_erp"
echo "User: $DB_USER"
echo "Password: $DB_PASSWORD"
