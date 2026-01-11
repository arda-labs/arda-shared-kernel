# Hướng dẫn sử dụng arda-shared-kernel

## Giới thiệu

`arda-shared-kernel` là thư viện chia sẻ cho các dịch vụ trong hệ thống Arda Platform, cung cấp các tính năng:

- **Multi-tenancy**: Hỗ trợ đa khách hàng với dynamic database routing
- **TenantContext**: Quản lý tenant ID trong thread-local
- **RoutingDataSource**: Tự động định tuyến kết nối database theo tenant
- **Base Entities**: BaseEntity, AuditableEntity với soft delete và auditing
- **Exception Handling**: Global exception handlers và custom exceptions
- **Rate Limiting**: AOP-based rate limiting với Redis/in-memory
- **Security**: JWT validation, tenant access control
- **Utilities**: JsonUtils, DateTimeUtils, ValidationUtils

## 1. Cách lấy GitHub Personal Access Token (PAT)

### Bước 1: Truy cập GitHub Settings

1. Đăng nhập vào GitHub
2. Click vào avatar góc phải trên → **Settings**
3. Scroll xuống dưới cùng sidebar trái → **Developer settings**
4. Click **Personal access tokens** → **Tokens (classic)**

### Bước 2: Tạo Token mới

1. Click **Generate new token** → **Generate new token (classic)**
2. Đặt tên cho token: ví dụ `Maven GitHub Packages`
3. Chọn quyền (scopes):
   - ✅ `read:packages` - Đọc packages từ GitHub
   - ✅ `write:packages` - Ghi packages lên GitHub (nếu cần publish)
   - ✅ `repo` - Truy cập private repositories (nếu repo là private)

4. Click **Generate token**
5. **⚠️ QUAN TRỌNG**: Copy token ngay lập tức và lưu lại. Token chỉ hiển thị một lần duy nhất!

### Bước 3: Lưu Token an toàn

Lưu token vào file an toàn hoặc password manager. Không commit token vào Git!

---

## 2. Cấu hình Maven settings.xml

### Vị trí file settings.xml

- **Windows**: `C:\Users\<YourUsername>\.m2\settings.xml`
- **Linux/Mac**: `~/.m2/settings.xml`

Nếu file chưa tồn tại, tạo mới file này.

### Nội dung settings.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              https://maven.apache.org/xsd/settings-1.0.0.xsd">

  <servers>
    <!-- Server configuration for GitHub Packages -->
    <server>
      <id>github</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_PERSONAL_ACCESS_TOKEN</password>
    </server>
  </servers>

  <profiles>
    <profile>
      <id>github</id>
      <repositories>
        <repository>
          <id>github</id>
          <name>GitHub Packages</name>
          <url>https://maven.pkg.github.com/arda-labs/arda-shared-kernel</url>
          <releases>
            <enabled>true</enabled>
          </releases>
          <snapshots>
            <enabled>true</enabled>
          </snapshots>
        </repository>
      </repositories>
    </profile>
  </profiles>

  <activeProfiles>
    <activeProfile>github</activeProfile>
  </activeProfiles>

</settings>
```

### Thay thế thông tin

- `YOUR_GITHUB_USERNAME`: Tên GitHub của bạn (ví dụ: `hoangnv`)
- `YOUR_PERSONAL_ACCESS_TOKEN`: Token vừa tạo ở bước 1

**⚠️ Bảo mật**: File `settings.xml` chứa thông tin nhạy cảm. Đảm bảo file có quyền chỉ bạn đọc được (chmod 600 trên Linux/Mac).

---

## 3. Sử dụng thư viện trong các dự án khác

### 3.1. Thêm dependency vào pom.xml

Trong các dự án như `arda-crm-service`, `arda-bpm-engine`, `arda-iam-service`, thêm dependency:

```xml
<dependencies>
    <!-- Arda Shared Kernel -->
    <dependency>
        <groupId>vn.io.arda</groupId>
        <artifactId>arda-shared-kernel</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </dependency>

    <!-- Các dependency khác... -->
</dependencies>
```

### 3.2. Thêm repository vào pom.xml (nếu chưa có profile)

Nếu bạn chưa cấu hình `settings.xml` hoặc muốn repository riêng cho project:

```xml
<repositories>
    <repository>
        <id>github</id>
        <name>GitHub Packages</name>
        <url>https://maven.pkg.github.com/arda-labs/arda-shared-kernel</url>
    </repository>
</repositories>
```

### 3.3. Build project

```bash
# Clean và build lại project
./mvnw clean install

# Hoặc trên Windows
mvnw.cmd clean install
```

Maven sẽ tự động tải `arda-shared-kernel` từ GitHub Packages xuống local repository.

---

## 4. Cấu hình Multi-tenancy trong Service

### 4.1. Thêm properties vào application.yml

```yaml
arda:
  shared:
    multi-tenancy:
      enabled: true
      central-platform-url: http://localhost:8000  # URL của arda-central-platform
      tenant-identifier-header: X-Tenant-ID

    datasource:
      cache:
        enabled: true
        max-size: 100
        expire-after-minutes: 30

    migration:
      auto-migrate: true  # Tự động chạy Liquibase migration cho tất cả tenant
```

### 4.2. Tạo DataSource Configuration

```java
package vn.io.arda.crm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.io.arda.shared.multitenant.datasource.RoutingDataSource;
import vn.io.arda.shared.multitenant.service.TenantMetadataService;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource(TenantMetadataService tenantMetadataService) {
        return new RoutingDataSource(tenantMetadataService);
    }
}
```

### 4.3. Sử dụng Base Entities

```java
package vn.io.arda.crm.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import vn.io.arda.shared.persistence.entity.AuditableEntity;

@Data
@Entity
@Table(name = "customers")
@EqualsAndHashCode(callSuper = true)
public class Customer extends AuditableEntity {

    private String name;
    private String email;
    private String phone;

    // Tự động có: id, createdAt, updatedAt, version, deleted, createdBy, updatedBy
}
```

### 4.4. Sử dụng Repository với Soft Delete

```java
package vn.io.arda.crm.repository;

import org.springframework.stereotype.Repository;
import vn.io.arda.crm.domain.Customer;
import vn.io.arda.shared.persistence.repository.BaseRepository;

import java.util.UUID;

@Repository
public interface CustomerRepository extends BaseRepository<Customer, UUID> {

    // BaseRepository đã có sẵn:
    // - findAllActive() - Tìm tất cả records chưa xóa
    // - findActiveById(id) - Tìm theo ID nếu chưa xóa
    // - softDelete(entity) - Xóa mềm (set deleted = true)
    // - restore(entity) - Khôi phục (set deleted = false)

    List<Customer> findByEmailContaining(String email);
}
```

### 4.5. Sử dụng Rate Limiting

```java
package vn.io.arda.crm.controller;

import org.springframework.web.bind.annotation.*;
import vn.io.arda.shared.ratelimit.annotation.RateLimit;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @GetMapping
    @RateLimit(key = "customer:list", limit = 100, windowSeconds = 60)
    public List<Customer> getAllCustomers() {
        // Giới hạn 100 requests mỗi 60 giây cho endpoint này
        return customerService.findAll();
    }

    @PostMapping
    @RateLimit(key = "customer:create", limit = 20, windowSeconds = 60)
    public Customer createCustomer(@RequestBody CustomerRequest request) {
        // Giới hạn 20 requests mỗi 60 giây
        return customerService.create(request);
    }
}
```

### 4.6. Sử dụng Utilities

```java
package vn.io.arda.crm.service;

import vn.io.arda.shared.util.JsonUtils;
import vn.io.arda.shared.util.DateTimeUtils;
import vn.io.arda.shared.util.ValidationUtils;

public class CustomerService {

    public void processCustomer(String jsonData) {
        // JSON parsing
        Customer customer = JsonUtils.fromJson(jsonData, Customer.class);

        // Email validation
        if (!ValidationUtils.isValidEmail(customer.getEmail())) {
            throw new IllegalArgumentException("Invalid email");
        }

        // Date calculations
        Instant startOfDay = DateTimeUtils.startOfDay(Instant.now());
        int age = DateTimeUtils.calculateAge(customer.getBirthDate());

        // Save...
    }
}
```

---

## 5. Kiểm tra kết nối GitHub Packages

### Kiểm tra phiên bản có sẵn

Xem các phiên bản đã publish tại:
```
https://github.com/orgs/arda-labs/packages?repo_name=arda-shared-kernel
```

Hoặc dùng Maven:

```bash
# Tìm kiếm artifact
./mvnw dependency:get -Dartifact=vn.io.arda:arda-shared-kernel:0.0.1-SNAPSHOT

# Xem dependency tree
./mvnw dependency:tree
```

---

## 6. Troubleshooting

### Lỗi: "Could not find artifact vn.io.arda:arda-shared-kernel"

**Nguyên nhân**: Maven không thể kết nối đến GitHub Packages

**Giải pháp**:
1. Kiểm tra `settings.xml` đã cấu hình đúng server `<id>github</id>`
2. Kiểm tra PAT còn hiệu lực và có quyền `read:packages`
3. Kiểm tra username và token trong `<server>` không có lỗi chính tả
4. Thử build lại với `-U` flag: `./mvnw clean install -U`

### Lỗi: "401 Unauthorized"

**Nguyên nhân**: Token không có quyền hoặc sai username

**Giải pháp**:
1. Tạo lại PAT với đầy đủ quyền `read:packages`, `write:packages`, `repo`
2. Đảm bảo username trong settings.xml là GitHub username, không phải email
3. Copy lại token chính xác, không có khoảng trắng thừa

### Lỗi: TenantContext không tìm thấy tenant

**Nguyên nhân**: Request không có header `X-Tenant-ID` hoặc filter chưa được cấu hình

**Giải pháp**:
1. Đảm bảo APISIX Gateway đã cấu hình inject header `X-Tenant-ID`
2. Kiểm tra `TenantFilter` đã được đăng ký trong Spring Security chain
3. Test với Postman/curl: thêm header `-H "X-Tenant-ID: tenant-001"`

### Lỗi: Liquibase migration failed

**Nguyên nhân**: Changelog file không tồn tại hoặc database connection sai

**Giải pháp**:
1. Tạo file `db/changelog/db.changelog-master.yaml` trong resources
2. Kiểm tra tenant database connection string trong Central Platform
3. Tắt auto-migrate tạm thời: `arda.shared.migration.auto-migrate=false`

---

## 7. Liên hệ & Hỗ trợ

- **Repository**: https://github.com/arda-labs/arda-shared-kernel
- **Issues**: https://github.com/arda-labs/arda-shared-kernel/issues
- **Documentation**: https://arda.io.vn/docs

## 8. Changelog

### Version 0.0.1-SNAPSHOT (2026-01-12)

**Core Features:**
- Multi-tenancy với RoutingDataSource và TenantContext
- Base entities (BaseEntity, AuditableEntity) với soft delete và auditing
- Global exception handling và custom exceptions
- Rate limiting với AOP và Redis/in-memory strategies
- JWT validation và tenant security
- Liquibase multi-tenant migration runner
- Utilities: JsonUtils, DateTimeUtils, ValidationUtils

**Dependencies:**
- Spring Boot 3.5.9
- Java 21
- Hibernate 6.x với MultiTenantConnectionProvider
- Liquibase, HikariCP, Caffeine cache
- PostgreSQL và Oracle JDBC drivers
- Jackson, JJWT, Lombok

**GitHub Actions:**
- Automatic build và publish to GitHub Packages on push to main
- Java 21 (Temurin distribution)
- Maven verify và deploy
