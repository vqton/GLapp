# AccountingERP - Hệ Thống Quản Lý Kế Toán Doanh Nghiệp

## 📋 Giới Thiệu

**AccountingERP** là một hệ thống quản lý kế toán doanh nghiệp (Enterprise Resource Planning) được xây dựng theo kiến trúc **Domain-Driven Design (DDD)**, sử dụng:

- **Java 21** (OpenJDK)
- **Spring Boot 3.3+**
- **PostgreSQL 16+** (production), **H2** (development/test)
- **Thymeleaf 3.1+** (Template Engine)
- **Bootstrap 5** + **Chart.js** (Frontend)
- **Spring Security + JWT** (Authentication)

Tuân thủ: **Thông Tư 99/2025/TT-BTC** về kế toán

---

## 📁 Cấu Trúc Dự Án (DDD Architecture)

```
AccountingERP/
│
├── pom.xml                                          # Maven configuration
├── README.md
│
└── src/main/java/com/tonvq/accountingerp/
    │
    ├── domain/                                       # Domain Layer (Pure Business Logic)
    │   ├── model/
    │   │   └── ChungTu.java                         # Entity: Chứng Từ (Voucher)
    │   ├── repository/
    │   │   └── ChungTuRepository.java               # Domain Repository Interface
    │   ├── service/                                 # Domain Services
    │   │   └── (các service miền nếu cần)
    │   └── event/                                   # Domain Events
    │       └── (các events miền nếu cần)
    │
    ├── application/                                 # Application Layer (Use Cases)
    │   ├── dto/
    │   │   ├── ChungTuCreateDTO.java               # Create DTO
    │   │   └── ChungTuResponseDTO.java             # Response DTO
    │   ├── service/
    │   │   └── ChungTuApplicationService.java      # Application Service
    │   └── mapper/
    │       └── ChungTuMapper.java                  # DTO Mapper
    │
    ├── infrastructure/                              # Infrastructure Layer (Adapters)
    │   ├── persistence/
    │   │   └── JpaChungTuRepository.java           # JPA Repository Implementation
    │   ├── web/
    │   │   └── controller/
    │   │       ├── HomeController.java
    │   │       └── ChungTuController.java          # REST Controller
    │   └── config/
    │       ├── SecurityConfig.java                 # Spring Security Config
    │       └── (các config khác)
    │
    ├── shared/                                      # Shared Layer (Common Utilities)
    │   ├── exception/
    │   │   ├── BusinessException.java
    │   │   └── ResourceNotFoundException.java
    │   └── util/
    │       └── (các utility)
    │
    └── AccountingERPApplication.java                # Main Application Class

└── src/main/resources/
    ├── application.yml                              # Default config
    ├── application-dev.yml                          # Development config
    ├── application-prod.yml                         # Production config
    ├── templates/
    │   ├── index.html
    │   └── dashboard.html
    └── static/
        ├── css/
        └── js/

└── src/test/java/com/tonvq/accountingerp/
    └── (Unit tests, Integration tests)
```

---

## 🔧 Yêu Cầu Hệ Thống

- **OpenJDK 21** trở lên
- **Maven 3.9+**
- **PostgreSQL 16+** (cho production)
- **Git** (tùy chọn)

---

## 📦 Cài Đặt & Chạy Dự Án

### 1️⃣ Cài Đặt OpenJDK 21 trên Windows

#### Cách A: Tải trực tiếp từ Oracle

```powershell
# Download từ https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html
# Hoặc sử dụng Choco (nếu có Chocolatey)
choco install openjdk21
```

#### Cách B: Sử dụng SDKMAN (Recommended)

```powershell
# Cài SDKMAN trên Windows (sử dụng Git Bash hoặc WSL)
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Cài Java 21
sdk install java 21-open
sdk default java 21-open
```

#### Kiểm tra phiên bản:

```powershell
java -version
# Output: openjdk version "21" ...
```

### 2️⃣ Cài Đặt Maven

```powershell
# Nếu sử dụng Choco
choco install maven

# Hoặc tải từ https://maven.apache.org/download.cgi
# Giải nén và thêm vào PATH
```

Kiểm tra:

```powershell
mvn -v
# Output: Apache Maven 3.9.x
```

### 3️⃣ Cài Đặt PostgreSQL 16

```powershell
# Sử dụng Choco
choco install postgresql

# Hoặc tải từ https://www.postgresql.org/download/windows/
```

**Tạo database:**

```powershell
# Mở PostgreSQL command line
psql -U postgres

# Tạo user và database
CREATE ROLE accounting WITH PASSWORD 'your_password' LOGIN;
CREATE DATABASE accounting_erp OWNER accounting;

# Hoặc tải AccountingERP project và chạy script tự động
```

### 4️⃣ Clone hoặc Tạo Project

**Cách A: Sử dụng lệnh Maven Archetype**

```powershell
mvn archetype:generate `
  -DgroupId=com.tonvq `
  -DartifactId=accounting-erp `
  -DarchetypeArtifactId=maven-archetype-quickstart `
  -DinteractiveMode=false
```

**Cách B: Sử dụng Spring Initializr (Web)**

Truy cập https://start.spring.io/ và chọn:
- Project: Maven
- Language: Java
- Spring Boot: 3.3.6
- Project metadata:
  - Group: com.tonvq
  - Artifact: accounting-erp
  - Name: AccountingERP
  - Package name: com.tonvq.accountingerp
- Dependencies: Web, Data JPA, Security, Thymeleaf, PostgreSQL, Validation, Lombok

**Cách C: Copy toàn bộ project từ file này**

Giải nén project từ file đã cung cấp.

### 5️⃣ Build Project

```powershell
cd AccountingERP
mvn clean install
```

**Nếu gặp lỗi proxy:**

```powershell
mvn clean install -DskipTests
```

### 6️⃣ Chạy Ứng Dụng

**Chế độ Development (H2 database):**

```powershell
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

**Chế độ Production (PostgreSQL):**

```powershell
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

**Hoặc chạy jar file:**

```powershell
mvn clean package
java -jar target/accounting-erp-1.0.0.jar --spring.profiles.active=dev
```

### 7️⃣ Truy Cập Ứng Dụng

```
🏠 Trang chủ:        http://localhost:8080
📊 Dashboard:        http://localhost:8080/dashboard
📖 Swagger/API Docs: http://localhost:8080/swagger-ui.html
💾 H2 Console:       http://localhost:8080/h2-console (dev mode)
```

---

## 📝 Cấu Trúc Main Classes

### AccountingERPApplication.java
```java
@SpringBootApplication
@OpenAPIDefinition(...)
public class AccountingERPApplication {
    public static void main(String[] args) {
        SpringApplication.run(AccountingERPApplication.class, args);
    }
}
```

---

## 🏗️ Kiến Trúc DDD - Các Lớp

### 1. **Domain Layer** (Pure Business Logic)
- Không phụ thuộc vào framework
- Chứa Entities, Value Objects, Aggregates
- Repository Interfaces (chỉ interface, không implementation)
- Domain Services
- Domain Events

**Ví dụ: ChungTu.java (Entity)**
- Nghiệp vụ: duyệt chứng từ, từ chối chứng từ, kiểm tra hợp lệ

### 2. **Application Layer** (Use Cases)
- Điều phối giữa Domain và Infrastructure
- Application Services (orchestrate business use cases)
- DTOs (Data Transfer Objects)
- Mappers (convert entity ↔ DTO)

### 3. **Infrastructure Layer** (Adapters)
- JPA Repository Implementation
- REST Controllers
- Spring Config
- External service adapters

### 4. **Shared Layer** (Common)
- Exceptions
- Utilities
- Constants

---

## 🔐 Security & Authentication

### Spring Security Configuration
```java
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### JWT Token (nếu cần)
- Dependency: jjwt-api, jjwt-impl, jjwt-jackson
- Implement: JwtTokenProvider, JwtAuthenticationFilter

---

## 📊 Database Schema

### Chứng Từ (vouchers) Table
```sql
CREATE TABLE chung_tu (
    id BIGINT PRIMARY KEY,
    ma_chung_tu VARCHAR(50) UNIQUE NOT NULL,
    loai_chung_tu VARCHAR(50) NOT NULL,
    ngay_chung_tu DATE NOT NULL,
    nd_chung_tu TEXT,
    so_tien DECIMAL(18, 2),
    trang_thai VARCHAR(20) -- DRAFT, APPROVED, REJECTED
    created_by BIGINT,
    created_at TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP
);
```

---

## 🚀 Tiếp Tục Phát Triển

### Thêm Module Mới

1. **Tạo Entity trong `domain/model`**
2. **Tạo Repository Interface trong `domain/repository`**
3. **Tạo JPA Repository trong `infrastructure/persistence`**
4. **Tạo DTOs trong `application/dto`**
5. **Tạo Mapper trong `application/mapper`**
6. **Tạo Application Service trong `application/service`**
7. **Tạo Controller trong `infrastructure/web/controller`**
8. **Tạo Template HTML trong `resources/templates`**

### Các Module ERP Khác
- **Bán Hàng (Sales)** - Quản lý đơn hàng, hóa đơn
- **Kho (Inventory)** - Quản lý hàng tồn kho, nhập xuất
- **Nhân Sự (HR)** - Quản lý nhân viên, lương
- **Tài Sản (Fixed Assets)** - Quản lý tài sản, khấu hao
- **Báo Cáo Tài Chính (Financial Reports)** - Báo cáo BCTC

---

## 📚 Dependencies Chính

| Dependency | Version | Mục Đích |
|-----------|---------|---------|
| spring-boot-starter-web | 3.3.6 | Web framework, REST API |
| spring-boot-starter-data-jpa | 3.3.6 | ORM, Database access |
| spring-boot-starter-security | 3.3.6 | Authentication, Authorization |
| spring-boot-starter-thymeleaf | 3.3.6 | Server-side templating |
| postgresql | Latest | Database driver |
| jjwt-api | 0.12.5 | JWT token handling |
| springdoc-openapi | 2.3.0 | Swagger/OpenAPI docs |
| lombok | Latest | Reduce boilerplate |

---

## 🧪 Testing

```powershell
# Chạy tất cả test
mvn test

# Chạy test cụ thể
mvn test -Dtest=ChungTuRepositoryTest
```

---

## 📖 Tài Liệu & Tham Khảo

- [Spring Boot 3.3 Documentation](https://spring.io/projects/spring-boot)
- [Domain-Driven Design - Eric Evans](https://www.domainlanguage.com/ddd/)
- [Thymeleaf Guide](https://www.thymeleaf.org/doc/tutorials/3.1/usingthymeleaf.html)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Thông Tư 99/2025/TT-BTC](https://example.com) (Link chính thức)

---

## 📞 Hỗ Trợ

Liên hệ: info@tonvq.com

---

**Happy Coding! 🚀**
