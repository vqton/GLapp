# 🚀 HƯỚNG DẪN CÀI ĐẶT VÀ CHẠY ACCOUNTING ERP TRÊN WINDOWS

## 📋 Nội Dung

1. [Yêu Cầu Hệ Thống](#yêu-cầu-hệ-thống)
2. [Cài Đặt OpenJDK 21](#cài-đặt-openjdk-21)
3. [Cài Đặt Maven](#cài-đặt-maven)
4. [Cài Đặt PostgreSQL](#cài-đặt-postgresql)
5. [Chạy Project](#chạy-project)
6. [Troubleshooting](#troubleshooting)

---

## 📌 Yêu Cầu Hệ Thống

- Windows 10 trở lên
- OpenJDK 21 hoặc Oracle JDK 21
- Maven 3.9+
- PostgreSQL 16+ (nếu sử dụng production mode)
- ~2GB RAM tối thiểu
- ~500MB disk space

---

## 🔧 Cài Đặt OpenJDK 21

### Cách 1: Tải trực tiếp (Recommended)

1. Truy cập: **https://jdk.java.net/21/**
2. Tải phiên bản Windows (x64): `openjdk-21_windows-x64_bin.zip`
3. Giải nén vào: `C:\Java\openjdk21\`

**Cấu hình biến môi trường:**

```powershell
# Mở PowerShell as Administrator
# Đặt biến JAVA_HOME
[System.Environment]::SetEnvironmentVariable('JAVA_HOME', 'C:\Java\openjdk21', 'Machine')

# Thêm bin vào PATH
$currentPath = [System.Environment]::GetEnvironmentVariable('Path', 'Machine')
[System.Environment]::SetEnvironmentVariable('Path', "$currentPath;C:\Java\openjdk21\bin", 'Machine')
```

**Kiểm tra:**

```powershell
java -version
# Output:
# openjdk version "21" ...
# OpenJDK Runtime Environment
```

### Cách 2: Sử dụng Chocolatey

```powershell
# Cài Chocolatey nếu chưa có (chạy PowerShell as Administrator)
Set-ExecutionPolicy Bypass -Scope Process -Force
[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# Cài OpenJDK 21
choco install openjdk21

# Kiểm tra
java -version
```

### Cách 3: Sử dụng SDKMAN (WSL/Git Bash)

Nếu bạn sử dụng **WSL (Windows Subsystem for Linux)** hoặc **Git Bash:**

```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 21-open
sdk default java 21-open
```

---

## 📦 Cài Đặt Maven

### Cách 1: Tải trực tiếp

1. Truy cập: **https://maven.apache.org/download.cgi**
2. Tải phiên bản nhị phân: `apache-maven-3.9.x-bin.zip`
3. Giải nén vào: `C:\Maven\apache-maven-3.9.x\`

**Cấu hình biến môi trường:**

```powershell
# PowerShell as Administrator
[System.Environment]::SetEnvironmentVariable('MAVEN_HOME', 'C:\Maven\apache-maven-3.9.x', 'Machine')

$currentPath = [System.Environment]::GetEnvironmentVariable('Path', 'Machine')
[System.Environment]::SetEnvironmentVariable('Path', "$currentPath;C:\Maven\apache-maven-3.9.x\bin", 'Machine')
```

**Kiểm tra:**

```powershell
mvn -v
# Output:
# Apache Maven 3.9.x
# Maven home: C:\Maven\apache-maven-3.9.x
```

### Cách 2: Sử dụng Chocolatey

```powershell
choco install maven
mvn -v
```

---

## 🗄️ Cài Đặt PostgreSQL 16

### Cách 1: Tải trực tiếp

1. Truy cập: **https://www.postgresql.org/download/windows/**
2. Tải bộ cài: `postgresql-16.x-1-windows-x64.exe`
3. Chạy installer, ghi nhớ **password superuser**

**Thiết lập database:**

```powershell
# Mở Command Prompt hoặc PowerShell
cd "C:\Program Files\PostgreSQL\16\bin"

# Đăng nhập PostgreSQL
psql -U postgres

# Trong PostgreSQL CLI:
```

```sql
-- Tạo user mới
CREATE USER accounting WITH PASSWORD 'your_secure_password';

-- Tạo database
CREATE DATABASE accounting_erp OWNER accounting;

-- Cấp quyền
GRANT CONNECT ON DATABASE accounting_erp TO accounting;
GRANT USAGE ON SCHEMA public TO accounting;
GRANT CREATE ON SCHEMA public TO accounting;

-- Kiểm tra
\l                           -- Liệt kê databases
\du                          -- Liệt kê users
\q                           -- Thoát
```

### Cách 2: Sử dụng Chocolatey

```powershell
choco install postgresql

# Theo dõi thông báo cài đặt để lấy thông tin kết nối
```

---

## 🚀 Chạy Project AccountingERP

### Bước 1: Tải/Clone Project

**Cách A: Nếu có Git**

```powershell
cd e:\glApp
git clone https://github.com/your-repo/AccountingERP.git
cd AccountingERP
```

**Cách B: Giải nén file ZIP**

```powershell
# Giải nén AccountingERP.zip vào e:\glApp\AccountingERP
cd e:\glApp\AccountingERP
```

### Bước 2: Build Project

```powershell
mvn clean install -DskipTests
```

**Nếu gặp lỗi proxy:**

```powershell
mvn clean install -DskipTests -X
# -X: debug mode để xem chi tiết lỗi
```

### Bước 3: Chạy ứng dụng

#### Development Mode (H2 Database)

```powershell
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

**Output:**

```
Started AccountingERPApplication in x.xxx seconds
INFO: Embedded H2 started
INFO: Server listening on port 8080
```

#### Production Mode (PostgreSQL)

**Trước tiên, cập nhật application-prod.yml:**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/accounting_erp
    username: accounting
    password: your_secure_password
```

**Sau đó chạy:**

```powershell
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

### Bước 4: Truy cập Ứng Dụng

Mở trình duyệt và truy cập:

| Chức Năng | URL |
|----------|-----|
| 🏠 Trang Chủ | http://localhost:8080 |
| 📊 Dashboard | http://localhost:8080/dashboard |
| 📖 API Docs | http://localhost:8080/swagger-ui.html |
| 💾 H2 Console | http://localhost:8080/h2-console *(dev mode)* |

---

## 🛠️ Troubleshooting

### ❌ Lỗi: "Java command not found"

**Giải pháp:**

```powershell
# Kiểm tra JAVA_HOME
echo $env:JAVA_HOME

# Nếu rỗng, đặt lại:
[System.Environment]::SetEnvironmentVariable('JAVA_HOME', 'C:\Java\openjdk21', 'Machine')

# Khởi động lại PowerShell/Command Prompt
```

### ❌ Lỗi: "mvn command not found"

**Giải pháp:**

```powershell
# Kiểm tra MAVEN_HOME
echo $env:MAVEN_HOME

# Kiểm tra PATH
echo $env:Path

# Khởi động lại PowerShell
```

### ❌ Lỗi: "PostgreSQL connection refused"

**Giải pháp:**

```powershell
# Kiểm tra PostgreSQL service
Get-Service | grep -i postgres

# Nếu chưa chạy, khởi động:
Start-Service postgresql-x64-16

# Kiểm tra cổng
netstat -ano | findstr :5432
```

### ❌ Lỗi: "port 8080 already in use"

**Giải pháp:**

```powershell
# Tìm process sử dụng port 8080
netstat -ano | findstr :8080

# Lấy PID (số cuối) và kill process
taskkill /PID <PID> /F

# Hoặc sử dụng cổng khác
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

### ❌ Lỗi: "Database 'accounting_erp' does not exist"

**Giải pháp:**

```powershell
# Tạo lại database
psql -U postgres

# Trong PostgreSQL CLI:
```

```sql
CREATE DATABASE accounting_erp OWNER accounting;
\c accounting_erp
-- Hibernate sẽ tự tạo bảng (nếu ddl-auto: create-drop)
```

### ❌ Lỗi: Maven dependencies download failed

**Giải pháp:**

```powershell
# Xóa cache Maven
rmdir C:\Users\<Username>\.m2\repository -Recurse -Force

# Build lại
mvn clean install

# Hoặc skip tests
mvn clean install -DskipTests
```

---

## 📝 Cấu Hình Tùy Chỉnh

### Thay đổi cổng mặc định

**Cách 1: Dòng lệnh**

```powershell
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=9000"
```

**Cách 2: File application.yml**

```yaml
server:
  port: 9000
```

### Thay đổi database connection

**File application-prod.yml**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://your-host:5432/your-db
    username: your-user
    password: your-password
```

### Bật debug logging

**File application.yml**

```yaml
logging:
  level:
    com.tonvq.accountingerp: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql: TRACE
```

---

## ✅ Các Bước Xác Minh Thành Công

Sau khi khởi động thành công, kiểm tra:

```powershell
# 1. Kiểm tra ứng dụng chạy
curl http://localhost:8080

# 2. Kiểm tra API
curl http://localhost:8080/swagger-ui.html

# 3. Kiểm tra database (nếu dùng PostgreSQL)
psql -U accounting -d accounting_erp -c "SELECT * FROM information_schema.tables;"
```

---

## 🎯 Tiếp Theo

Sau khi setup thành công:

1. **Tìm hiểu cấu trúc DDD** trong `src/main/java/com/tonvq/accountingerp/`
2. **Tạo entity mới** theo hướng dẫn DDD
3. **Implement business logic** trong domain layer
4. **Tạo REST API** trong infrastructure layer
5. **Viết unit tests** trong `src/test/`

---

## 📚 Tài Liệu Tham Khảo

- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [OpenJDK 21](https://jdk.java.net/21/)
- [Maven Guide](https://maven.apache.org/guides/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

---

**Chúc bạn thành công! 🚀**
