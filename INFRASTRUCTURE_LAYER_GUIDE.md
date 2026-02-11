# INFRASTRUCTURE LAYER - Hướng Dẫn Cài Đặt (TT 99/2025/TT-BTC)

## 📋 Tổng Quan

Infrastructure layer sẽ implement các repository interfaces từ domain layer sử dụng Spring Data JPA, cấu hình database, và các tính năng persistence.

**Folder Structure:**
```
infrastructure/
├── config/                          # Configuration classes
│   ├── JpaConfiguration.java        # JPA/Hibernate config
│   ├── DatabaseConfiguration.java   # DataSource, transaction manager
│   └── SecurityConfiguration.java   # Spring Security (future)
├── persistence/
│   ├── jpa/                        # JPA entity annotations
│   │   └── ChungTuEntity.java (extends domain ChungTu + JPA)
│   ├── repository/                 # Spring Data repository implementations
│   │   ├── ChungTuJpaRepository.java
│   │   ├── DonHangJpaRepository.java
│   │   ├── TonKhoJpaRepository.java
│   │   ├── HopDongDichVuJpaRepository.java
│   │   ├── DuPhongNoJpaRepository.java
│   │   ├── TaiKhoanJpaRepository.java
│   │   ├── KhachHangJpaRepository.java
│   │   └── NhaCungCapJpaRepository.java
│   ├── auditing/                   # Audit trail implementation
│   │   └── AuditTrailJpaRepository.java
│   └── converter/                  # Value object converters
│       ├── TienConverter.java
│       ├── SoLuongConverter.java
│       └── GiaVonConverter.java
├── migration/
│   └── db/
│       ├── V001__CreateSchema.sql  # Initial schema
│       ├── V002__AddChungTuTable.sql
│       ├── V003__AddDonHangTable.sql
│       ├── V004__AddTonKhoTable.sql
│       ├── V005__AddAuditTrailTable.sql
│       └── ...
├── event/
│   └── DomainEventPublisher.java    # Publish domain events
└── README.md
```

## 🔧 Implementation Steps

### Step 1: Add JPA Annotations to Domain Models

Add JPA annotations to domain entity classes WITHOUT changing domain logic:

```java
// src/main/java/com/tonvq/accountingerp/domain/model/ChungTu.java

@Entity
@Table(name = "chungtus", indexes = {
    @Index(name = "idx_ma_chungtus", columnList = "ma_chungtus"),
    @Index(name = "idx_trangThai", columnList = "trangThai"),
    @Index(name = "idx_ngayChungTus", columnList = "ngayChungTus")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChungTu implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "ma_chungtus", unique = true, nullable = false, length = 50)
    private String maChungTu;
    
    @Column(name = "loai_chungtus", nullable = false, length = 50)
    private String loaiChungTu;
    
    @Column(name = "ngayChungTus", nullable = false)
    private LocalDate ngayChungTu;
    
    @Column(name = "nd_chungtus", length = 500)
    private String ndChungTu;
    
    @Column(name = "soTien", precision = 18, scale = 2)
    private BigDecimal soTien;
    
    @Column(name = "donViTinh", length = 10)
    private String donViTinh;
    
    @Column(name = "trangThai", nullable = false, length = 20)
    private String trangThai;
    
    @Column(name = "createdBy")
    private Long createdBy;
    
    @Column(name = "createdAt")
    private LocalDateTime createdAt;
    
    @Column(name = "updatedBy")
    private Long updatedBy;
    
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;
    
    @Column(name = "approvedBy")
    private Long approvedBy;
    
    @Column(name = "approvedAt")
    private LocalDateTime approvedAt;
    
    @Column(name = "postedBy")
    private Long postedBy;
    
    @Column(name = "postedAt")
    private LocalDateTime postedAt;
    
    @Column(name = "lockedBy")
    private Long lockedBy;
    
    @Column(name = "lockedAt")
    private LocalDateTime lockedAt;
    
    @Column(name = "ghiChu", length = 500)
    private String ghiChu;
    
    // One-to-Many: ChungTu → ButToan
    @OneToMany(mappedBy = "chungTu", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ButToan> butToanList = new ArrayList<>();
    
    // Domain logic methods unchanged
    public void duyetChungTu(Long userId) { ... }
    public void ghiSo(Long userId) { ... }
    public void khoa(Long userId) { ... }
    // ... rest of methods
}
```

**Note:** Add JPA annotations carefully - do NOT modify domain logic methods.

### Step 2: Create Spring Data JPA Repositories

Implement domain repository interfaces using Spring Data:

```java
// src/main/java/com/tonvq/accountingerp/infrastructure/persistence/repository/ChungTuJpaRepository.java

@Repository
public interface ChungTuJpaRepository extends JpaRepository<ChungTu, Long>, 
                                             ChungTuRepository {
    
    @Query("SELECT c FROM ChungTu c WHERE c.maChungTu = :maChungTu")
    Optional<ChungTu> findByMaChungTu(@Param("maChungTu") String maChungTu);
    
    @Query("SELECT c FROM ChungTu c WHERE c.trangThai = :trangThai ORDER BY c.createdAt DESC")
    List<ChungTu> findByTrangThai(@Param("trangThai") String trangThai);
    
    @Query("SELECT c FROM ChungTu c WHERE c.ngayChungTu BETWEEN :startDate AND :endDate ORDER BY c.ngayChungTu DESC")
    List<ChungTu> findByNgayChungTuBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);
    
    @Query("SELECT c FROM ChungTu c WHERE c.loaiChungTu = :loaiChungTu ORDER BY c.createdAt DESC")
    List<ChungTu> findByLoaiChungTu(@Param("loaiChungTu") String loaiChungTu);
    
    @Query("SELECT c FROM ChungTu c WHERE c.createdBy = :createdBy ORDER BY c.createdAt DESC")
    List<ChungTu> findByCreatedBy(@Param("createdBy") Long createdBy);
    
    @Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING(ma_chungtus, 4) AS NUMERIC)), 0) + 1 FROM chungtus WHERE ma_chungtus LIKE :prefix", nativeQuery = true)
    Long getNextMaChungTu(@Param("prefix") String prefix);
}
```

### Step 3: Database Schema Creation (Flyway/Liquibase)

Create migration files for database schema:

**V001__Initial_Schema.sql:**
```sql
CREATE TABLE chungtus (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ma_chungtus VARCHAR(50) UNIQUE NOT NULL,
    loai_chungtus VARCHAR(50) NOT NULL,
    ngayChungTus DATE NOT NULL,
    nd_chungtus VARCHAR(500),
    soTien DECIMAL(18, 2) NOT NULL,
    donViTinh VARCHAR(10) DEFAULT 'VND',
    trangThai VARCHAR(20) NOT NULL,
    createdBy BIGINT,
    createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedBy BIGINT,
    updatedAt DATETIME,
    approvedBy BIGINT,
    approvedAt DATETIME,
    postedBy BIGINT,
    postedAt DATETIME,
    lockedBy BIGINT,
    lockedAt DATETIME,
    ghiChu VARCHAR(500),
    INDEX idx_ma_chungtus (ma_chungtus),
    INDEX idx_trangThai (trangThai),
    INDEX idx_ngayChungTus (ngayChungTus),
    INDEX idx_createdBy (createdBy)
);

CREATE TABLE buttoan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    chungtus_id BIGINT NOT NULL,
    tk_no VARCHAR(50) NOT NULL,
    tk_co VARCHAR(50) NOT NULL,
    soTien DECIMAL(18, 2) NOT NULL,
    ghiChu VARCHAR(500),
    FOREIGN KEY (chungtus_id) REFERENCES chungtus(id) ON DELETE CASCADE,
    INDEX idx_chungtus_id (chungtus_id),
    INDEX idx_tk_no (tk_no),
    INDEX idx_tk_co (tk_co)
);

CREATE TABLE audit_trail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    entityType VARCHAR(100) NOT NULL,
    entityId BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    userId VARCHAR(100) NOT NULL,
    changedAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    oldValue LONGTEXT,
    newValue LONGTEXT,
    changeReason VARCHAR(500),
    ipAddress VARCHAR(50),
    INDEX idx_entityType_id (entityType, entityId),
    INDEX idx_userId (userId),
    INDEX idx_changedAt (changedAt)
);
```

### Step 4: JPA Configuration

```java
// src/main/java/com/tonvq/accountingerp/infrastructure/config/JpaConfiguration.java

@Configuration
@EnableJpaRepositories(
    basePackages = "com.tonvq.accountingerp.infrastructure.persistence.repository",
    repositoryBaseClass = SimpleJpaRepository.class
)
@EnableJpaAuditing
public class JpaConfiguration {
    
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.tonvq.accountingerp.domain.model");
        
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQL13Dialect");
        properties.put("hibernate.hbm2ddl.auto", "validate");
        properties.put("hibernate.jdbc.batch_size", "20");
        properties.put("hibernate.order_inserts", "true");
        properties.put("hibernate.order_updates", "true");
        em.setJpaPropertyMap(properties);
        
        return em;
    }
    
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
```

### Step 5: Application Configuration

Update **application-dev.yml:**
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:test
    driver-class-name: org.h2.Driver
    username: sa
    password:
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true

---
spring:
  profiles: prod
  datasource:
    url: jdbc:postgresql://localhost:5432/accountingerp
    driver-class-name: org.postgresql.Driver
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD}
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQL13Dialect
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
```

### Step 6: Value Object Converters

For value objects like Tien, SoLuong, GiaVon:

```java
// src/main/java/com/tonvq/accountingerp/infrastructure/persistence/converter/TienConverter.java

@Converter(autoApply = true)
public class TienConverter implements AttributeConverter<Tien, BigDecimal> {
    
    @Override
    public BigDecimal convertToDatabaseColumn(Tien attribute) {
        return attribute == null ? null : attribute.getAmount();
    }
    
    @Override
    public Tien convertToEntityAttribute(BigDecimal dbData) {
        return dbData == null ? null : new Tien(dbData, TienTe.VND);
    }
}
```

### Step 7: Audit Trail Implementation

```java
// src/main/java/com/tonvq/accountingerp/infrastructure/persistence/auditing/AuditTrail.java

@Entity
@Table(name = "audit_trail", indexes = {
    @Index(name = "idx_entity", columnList = "entityType, entityId"),
    @Index(name = "idx_user", columnList = "userId"),
    @Index(name = "idx_changed_date", columnList = "changedAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditTrail {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String entityType;
    
    @Column(nullable = false)
    private Long entityId;
    
    @Column(nullable = false)
    private String action;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private LocalDateTime changedAt;
    
    @Column(columnDefinition = "LONGTEXT")
    private String oldValue;
    
    @Column(columnDefinition = "LONGTEXT")
    private String newValue;
    
    @Column(length = 500)
    private String changeReason;
    
    @Column(length = 50)
    private String ipAddress;
}
```

### Step 8: JPA Event Listeners (for audit trail)

```java
// src/main/java/com/tonvq/accountingerp/infrastructure/persistence/auditing/AuditingEntityListener.java

@Component
public class AuditingEntityListener {
    
    @Autowired
    private AuditTrailRepository auditTrailRepository;
    
    @PostPersist
    public void postPersist(Object entity) {
        // Log creation
        logAuditTrail(entity, "CREATE", null, convertToJson(entity));
    }
    
    @PostUpdate
    public void postUpdate(Object entity) {
        // Log update
        logAuditTrail(entity, "UPDATE", null, convertToJson(entity));
    }
    
    @PostRemove
    public void postRemove(Object entity) {
        // Log deletion
        logAuditTrail(entity, "DELETE", convertToJson(entity), null);
    }
    
    private void logAuditTrail(Object entity, String action, String oldValue, String newValue) {
        // Implementation
    }
    
    private String convertToJson(Object entity) {
        // JSON conversion
        return "";
    }
}
```

## 📦 Dependencies to Add

Add to **pom.xml:**

```xml
<!-- Spring Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Hibernate -->
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-core</artifactId>
</dependency>

<!-- PostgreSQL Driver -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- H2 (Development/Testing) -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Flyway (Database Migration) -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>

<!-- Jackson (JSON Conversion) -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```

## 🎯 Implementation Checklist

- [ ] Add JPA annotations to all domain entities (9 entities)
- [ ] Create Spring Data JPA repository interfaces (8 repositories)
- [ ] Create database migration files (V001-V010+)
- [ ] Implement JPA configuration class
- [ ] Implement audit trail entity and listener
- [ ] Create value object converters (Tien, SoLuong, GiaVon)
- [ ] Configure database (H2 for dev, PostgreSQL for prod)
- [ ] Test repository operations
- [ ] Create integration tests
- [ ] Document persistence layer

## 📋 Expected Tables

- `chungtus` - Chứng từ
- `buttoan` - Bút toán chi tiết
- `donhangs` - Đơn hàng
- `donhang_chitiets` - Chi tiết đơn hàng
- `tonkhos` - Tồn kho
- `hopdong_dichvus` - Hợp đồng dịch vụ
- `duphong_nos` - Dự phòng nợ
- `taikhoans` - Tài khoản kế toán
- `khachhangs` - Khách hàng
- `nhacungcaps` - Nhà cung cấp
- `hoaddons` - Hóa đơn
- `audit_trail` - Audit trail log

## ⚠️ Important Notes

1. **JPA Annotations = Infrastructure Concern**
   - Add annotations only in infrastructure layer or as separate JPA entity wrappers
   - Keep domain models pure Java (no Spring/JPA dependencies)

2. **Spring Data = Repository Implementation**
   - Repository interfaces defined in domain layer
   - Implementations in infrastructure layer
   - No direct entity use in application layer (use DTOs)

3. **Transaction Management**
   - `@Transactional` remains in application services
   - Infrastructure handles underlying JPA transactions

4. **Audit Trail**
   - JPA listeners log all changes automatically
   - Captured in audit_trail table
   - User/IP address from SecurityContext

5. **Testing**
   - Use H2 in-memory DB for unit tests
   - Use testcontainers for integration tests with PostgreSQL
   - Mock repositories in application service tests

## 📚 References

- Spring Data JPA: https://spring.io/projects/spring-data-jpa
- Hibernate: https://hibernate.org/
- Flyway: https://flywaydb.org/
- TT 99/2025/TT-BTC Article requirements

---

**Next: Infrastructure Layer Implementation (4-6 weeks estimated)**
