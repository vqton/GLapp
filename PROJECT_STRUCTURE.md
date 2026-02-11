# AccountingERP - Project Structure Overview

Cấu trúc dự án theo **Domain-Driven Design (DDD)**

```
AccountingERP/
│
├── 📁 src/main/java/com/tonvq/accountingerp/
│   │
│   ├── 📁 domain/                           ★ DOMAIN LAYER (Pure Business Logic)
│   │   ├── model/                           # Entities, Aggregates, Value Objects
│   │   │   └── ChungTu.java                # Entity: Chứng Từ
│   │   ├── repository/                      # Domain Repository Interfaces
│   │   │   └── ChungTuRepository.java
│   │   ├── service/                         # Domain Services (Business Rules)
│   │   │   └── (ChungTuDomainService.java)
│   │   └── event/                           # Domain Events
│   │       └── (ChungTuCreatedEvent.java)
│   │
│   ├── 📁 application/                      ★ APPLICATION LAYER (Use Cases)
│   │   ├── dto/                             # Data Transfer Objects
│   │   │   ├── ChungTuCreateDTO.java
│   │   │   └── ChungTuResponseDTO.java
│   │   ├── service/                         # Application Services (Orchestrators)
│   │   │   └── ChungTuApplicationService.java
│   │   └── mapper/                          # Entity ↔ DTO Mappers
│   │       └── ChungTuMapper.java
│   │
│   ├── 📁 infrastructure/                   ★ INFRASTRUCTURE LAYER (Adapters)
│   │   ├── persistence/                     # Repository Implementations
│   │   │   ├── JpaChungTuRepository.java   # Spring Data JPA
│   │   │   └── ChungTuRepositoryAdapter.java
│   │   ├── web/
│   │   │   ├── controller/                  # REST/Web Controllers
│   │   │   │   ├── HomeController.java
│   │   │   │   └── ChungTuController.java
│   │   │   └── (filter/, interceptor/, etc.)
│   │   └── config/                          # Spring Configurations
│   │       ├── SecurityConfig.java
│   │       ├── JpaConfig.java
│   │       └── WebConfig.java
│   │
│   ├── 📁 shared/                           ★ SHARED LAYER (Common Utilities)
│   │   ├── exception/                       # Custom Exceptions
│   │   │   ├── BusinessException.java
│   │   │   └── ResourceNotFoundException.java
│   │   ├── util/                            # Utility Classes
│   │   │   ├── DateUtils.java
│   │   │   └── ValidationUtils.java
│   │   └── (constant/, enum/, etc.)
│   │
│   └── AccountingERPApplication.java        ★ Main Entry Point

│
├── 📁 src/main/resources/
│   ├── application.yml                      # Default configuration
│   ├── application-dev.yml                  # Development config (H2)
│   ├── application-prod.yml                 # Production config (PostgreSQL)
│   ├── templates/                           # Thymeleaf Templates
│   │   ├── index.html                       # Homepage
│   │   ├── dashboard.html                   # Dashboard
│   │   └── (fragments/, layouts/, etc.)
│   └── static/                              # Static resources
│       ├── css/
│       │   └── (custom styles)
│       ├── js/
│       │   └── (custom scripts)
│       └── (images/, fonts/, etc.)
│
├── 📁 src/test/java/com/tonvq/accountingerp/
│   ├── domain/                              # Domain tests
│   ├── application/                         # Application tests
│   └── infrastructure/                      # Integration tests
│
├── 📁 scripts/
│   ├── init-db.sql                          # PostgreSQL init script
│   ├── init-db.sh                           # Linux/Mac setup
│   └── init-db.bat                          # Windows setup
│
├── pom.xml                                  ★ Maven Configuration
├── Dockerfile                               # Docker image definition
├── docker-compose.yml                       # Docker Compose for local dev
├── .editorconfig                            # Editor configuration
├── .gitignore                               # Git ignore rules
├── README.md                                # Project documentation
├── INSTALL.md                               # Installation guide
└── PROJECT_STRUCTURE.md                     # This file
```

---

## 🎯 DDD Layers Explanation

### 1️⃣ **Domain Layer** (`domain/`)
- ✅ Tương đối độc lập với framework
- ✅ Chứa business logic thuần túy
- ❌ Không import Spring annotations (ngoại trừ @Transient nếu cần)
- **Các thành phần:**
  - `model/`: Entities, Aggregates, Value Objects
  - `repository/`: Chỉ interface, không implementation
  - `service/`: Domain Services (khi logic không thuộc entity nào)
  - `event/`: Domain events (sự kiện miền)

**Ví dụ:**
```java
// ChungTu.java - Pure domain logic, no Spring dependency
public class ChungTu {
    public void duyetChungTu(Long userId) {
        if (!"DRAFT".equals(this.trangThai)) {
            throw new IllegalStateException("Không thể duyệt");
        }
        this.trangThai = "APPROVED";
    }
}
```

### 2️⃣ **Application Layer** (`application/`)
- ✅ Điều phối use cases
- ✅ Chuyển đổi giữa external (DTOs) và domain (Entities)
- **Các thành phần:**
  - `dto/`: Request/Response objects (không phụ thuộc vào domain)
  - `service/`: Application services (orchestrate domain services)
  - `mapper/`: Convert entity ↔ DTO

**Ví dụ:**
```java
// ChungTuApplicationService.java
@Service
public class ChungTuApplicationService {
    public ChungTuResponseDTO duyetChungTu(Long id, Long userId) {
        ChungTu chungTu = repository.findById(id);
        chungTu.duyetChungTu(userId);  // Domain logic
        repository.save(chungTu);      // Persist
        return mapper.toDTO(chungTu);  // Return DTO
    }
}
```

### 3️⃣ **Infrastructure Layer** (`infrastructure/`)
- ✅ Implementations của domain interfaces
- ✅ Framework-specific code (Spring, JPA, etc.)
- **Các thành phần:**
  - `persistence/`: JPA repositories
  - `web/`: Controllers, REST endpoints
  - `config/`: Spring configurations

**Ví dụ:**
```java
// JpaChungTuRepository.java - JPA implementation
@Repository
public interface JpaChungTuRepository extends JpaRepository<ChungTu, Long> {
    Optional<ChungTu> findByMaChungTu(String maChungTu);
}

// ChungTuRepositoryAdapter.java - Adapter to domain interface
@Component
public class ChungTuRepositoryAdapter implements ChungTuRepository {
    public Optional<ChungTu> findById(Long id) {
        return jpaRepository.findById(id);
    }
}
```

### 4️⃣ **Shared Layer** (`shared/`)
- ✅ Common utilities, exceptions
- ✅ Được sử dụng bởi tất cả layers

---

## 📊 Data Flow Example

**User requests to approve a voucher:**

```
HTTP Request
    ↓
ChungTuController (Infrastructure)
    ↓ HTTP → DTO
ChungTuApplicationService (Application)
    ↓ DTO → Entity + orchestrate
ChungTu domain logic (Domain)
    ↓ Business rule validation
ChungTuRepository (Domain interface)
    ↓ Implemented by
ChungTuRepositoryAdapter (Infrastructure)
    ↓ JPA call
JpaChungTuRepository (Spring Data JPA)
    ↓ Database call
PostgreSQL/H2 (Persistence)
    ↓
Response (DTO) → HTTP Response
```

---

## 🔄 Dependency Direction (DDD)

```
Presentation/Web (Controllers)
        ↓ depends on
Application Services
        ↓ depends on
Domain Layer (Entities, Business Logic)
        ↓ depends on
Infrastructure (Repository implementations)
```

**Nguyên tắc:** Domain không phụ thuộc vào bất kỳ layer khác

---

## 📦 Adding New Module Example

Giả sử thêm module "Tài Khoản" (Accounts):

```
1. Create Domain:
   domain/model/TaiKhoan.java
   domain/repository/TaiKhoanRepository.java
   domain/service/TaiKhoanDomainService.java

2. Create Application:
   application/dto/TaiKhoanCreateDTO.java
   application/dto/TaiKhoanResponseDTO.java
   application/service/TaiKhoanApplicationService.java
   application/mapper/TaiKhoanMapper.java

3. Create Infrastructure:
   infrastructure/persistence/JpaTaiKhoanRepository.java
   infrastructure/persistence/TaiKhoanRepositoryAdapter.java
   infrastructure/web/controller/TaiKhoanController.java

4. Create Tests:
   src/test/... corresponding test classes
```

---

## 🚀 Best Practices

✅ **Do:**
- Keep domain layer free from framework dependencies
- Use repositories to abstract persistence
- DTOs for external communication
- Test domain logic without Spring context

❌ **Don't:**
- Mix business logic with Spring annotations
- Use entities for API responses (use DTOs)
- Access database directly from domain
- Create circular dependencies

---

## 📚 References

- [Domain-Driven Design - Eric Evans](https://www.domainlanguage.com/ddd/)
- [Implementing Domain-Driven Design - Vaughn Vernon](https://vaughnvernon.com/)
- [Spring Boot Architecture](https://spring.io/guides/tutorials/rest/)

---

**Happy coding with DDD! 🎯**
