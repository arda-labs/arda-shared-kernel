# Arda Shared Kernel

**Thư viện Core** cho hệ thống Arda Platform - Hỗ trợ Multi-tenancy, Dynamic DB Routing, Security, Caching và Event Bus.

## 📋 Tổng quan

`arda-shared-kernel` là thư viện Java Spring Boot 4.0.1 (Java 21) cung cấp các tính năng nền tảng cho toàn bộ microservices trong hệ thống Arda:

- ✅ **Multi-tenancy**: Database-per-Tenant với dynamic routing
- ✅ **Security**: Keycloak JWT validation tích hợp sẵn
- ✅ **Exception Handling**: Global exception handler đồng nhất
- ✅ **Base Entities**: AuditableEntity, SoftDeletableEntity với auto-audit
- ✅ **Caching**: Redis caching với tenant-aware keys
- ✅ **Event Bus**: Spring Events để publish/subscribe domain events
- ✅ **Rate Limiting**: API rate limiting per tenant/user (annotation-based)

## 🚀 Quick Start

### 1. Build và Install Library

```bash
cd arda-shared-kernel
mvnw.cmd clean install
```

Library sẽ được cài đặt vào `.m2/repository/vn/io/arda/arda-shared-kernel/0.0.1-SNAPSHOT/`

### 2. Thêm Dependency vào Service

Trong `pom.xml` của service (ví dụ: `arda-iam-service`):

```xml
<dependency>
    <groupId>vn.io.arda</groupId>
    <artifactId>arda-shared-kernel</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>