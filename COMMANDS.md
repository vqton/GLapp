# AccountingERP - Command Cheatsheet

## 🔨 BUILD & COMPILE

```powershell
# Clean build (remove old artifacts)
mvn clean

# Compile source code
mvn compile

# Build project + run tests
mvn clean install

# Build project + skip tests
mvn clean install -DskipTests

# Build specific module (if multi-module)
mvn clean install -pl module-name

# Build with debug output
mvn clean install -X
```

---

## 🚀 RUN APPLICATION

```powershell
# Development Mode (H2 Database)
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Production Mode (PostgreSQL)
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"

# Custom port
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=9000"

# With custom JVM arguments
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx512m"

# Package as JAR
mvn clean package

# Run from JAR file
java -jar target/accounting-erp-1.0.0.jar --spring.profiles.active=dev

# Run from JAR with custom properties
java -Dspring.profiles.active=prod -Dserver.port=8081 -jar target/accounting-erp-1.0.0.jar
```

---

## 🧪 TESTING

```powershell
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ChungTuRepositoryTest

# Run specific test method
mvn test -Dtest=ChungTuRepositoryTest#testFindByMaChungTu

# Skip tests during build
mvn install -DskipTests

# Run with coverage (requires maven-jacoco-plugin)
mvn clean test jacoco:report
```

---

## 📦 DEPENDENCY MANAGEMENT

```powershell
# Show dependency tree
mvn dependency:tree

# Show dependency tree for specific scope
mvn dependency:tree -Dscope=compile

# Check for dependency updates
mvn versions:display-dependency-updates

# Update all dependencies to latest versions
mvn versions:use-latest-versions

# Analyze unused dependencies
mvn dependency:analyze

# Download all dependencies
mvn dependency:resolve
```

---

## 🗄️ DATABASE SETUP

### Windows
```powershell
# Initialize database (run from project root)
.\scripts\init-db.bat

# Or manual PostgreSQL setup:
psql -U postgres
```

```sql
CREATE USER accounting WITH PASSWORD 'postgres';
CREATE DATABASE accounting_erp OWNER accounting;
GRANT ALL PRIVILEGES ON DATABASE accounting_erp TO accounting;
```

### Linux/Mac
```bash
bash scripts/init-db.sh
```

---

## 🐳 DOCKER COMMANDS

```bash
# Build Docker image
docker build -t accounting-erp:1.0.0 .

# Run application in Docker
docker run -p 8080:8080 accounting-erp:1.0.0

# Start services with Docker Compose
docker-compose up -d

# Stop services
docker-compose down

# View logs
docker-compose logs -f accounting_erp

# Restart services
docker-compose restart

# Remove volumes (database data)
docker-compose down -v

# Check container status
docker-compose ps

# Access PostgreSQL inside Docker
docker exec -it accounting_erp_postgres psql -U accounting -d accounting_erp
```

---

## 🧹 CLEANUP

```powershell
# Delete compiled artifacts
mvn clean

# Delete specific target directory
Remove-Item -Recurse target/

# Delete local Maven cache
Remove-Item -Recurse $env:USERPROFILE\.m2\repository\

# Clean IDE cache
Remove-Item -Recurse .idea/
Remove-Item *.iml -Force

# Remove build artifacts
mvn clean

# Full reset (careful!)
mvn clean && Remove-Item -Recurse target/ -Force
```

---

## 📊 VERIFY SETUP

```powershell
# Check Java version
java -version

# Check Maven version
mvn -v

# Check Spring Boot version
mvn help:describe -Dplugin=org.springframework.boot:spring-boot-maven-plugin

# List all installed Maven plugins
mvn plugin:active-project

# Verify project structure
mvn project-info-reports:project-summary
```

---

## 🔌 ACCESS APPLICATION

```
🌐 Web Browser:
  Homepage:        http://localhost:8080
  Dashboard:       http://localhost:8080/dashboard
  Swagger/API:     http://localhost:8080/swagger-ui.html
  H2 Console:      http://localhost:8080/h2-console (dev mode only)

🗄️ Database:
  Host:            localhost
  Port:            5432
  Database:        accounting_erp
  Username:        accounting
  Password:        postgres (change in production!)
```

---

## 🔧 IDE INTEGRATION (VS Code)

```powershell
# Open project in VS Code
code .

# Install necessary extensions:
# - Extension Pack for Java
# - Spring Boot Extension Pack
# - Maven for Java
# - REST Client (for testing APIs)
```

---

## 📝 COMMON ISSUES & FIXES

### Port 8080 already in use
```powershell
# Find process using port 8080
netstat -ano | findstr :8080

# Kill the process (replace PID)
taskkill /PID <PID> /F

# Or use different port
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

### PostgreSQL connection failed
```powershell
# Check if PostgreSQL service is running
Get-Service postgresql-x64-16

# Start service if stopped
Start-Service postgresql-x64-16

# Verify connection
psql -U postgres -h localhost
```

### Maven build fails
```powershell
# Clear Maven cache and rebuild
mvn clean install -DskipTests -U

# Update Maven plugins
mvn clean install -U

# Show verbose output
mvn clean install -X
```

### Java version mismatch
```powershell
# Verify Java 21 is set
java -version

# Set JAVA_HOME if needed
$env:JAVA_HOME = "C:\Java\openjdk21"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
```

---

## 🎯 DEVELOPMENT WORKFLOW

### Daily Development
```powershell
# 1. Update code
# 2. Build project
mvn clean install

# 3. Run application
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# 4. Test APIs
curl http://localhost:8080/swagger-ui.html

# 5. Commit changes
git add .
git commit -m "feat: add new feature"
git push origin develop
```

### Before Deployment
```powershell
# 1. Run all tests
mvn clean test

# 2. Check dependencies for vulnerabilities
mvn org.owasp:dependency-check-maven:check

# 3. Build production package
mvn clean package -DskipTests

# 4. Build Docker image
docker build -t accounting-erp:latest .

# 5. Push to registry (optional)
docker push your-registry/accounting-erp:latest
```

---

## 📚 USEFUL LINKS

- Maven: https://maven.apache.org/
- Spring Boot: https://spring.io/projects/spring-boot
- Java 21: https://jdk.java.net/21/
- PostgreSQL: https://www.postgresql.org/
- Docker: https://www.docker.com/
- Thymeleaf: https://www.thymeleaf.org/

---

**Last updated: 2025-02-11**
