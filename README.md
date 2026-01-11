# Arda Shared Kernel
*Library dùng chung cho toàn bộ Microservices của Arda.*

### Các module chính:
- **Multitenant:** Chứa `TenantContext` và `RoutingDataSource`.
- **Database:** Cấu hình Hibernate tự động nhận diện Oracle/Postgres.
- **Exception:** Cấu hình Global Exception Handler.

### Cách sử dụng:
Thêm dependency vào các service khác:
```xml
<dependency>
    <groupId>vn.io.arda</groupId>
    <artifactId>arda-shared-kernel</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>