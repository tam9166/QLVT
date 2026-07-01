# QLVT - Quản lý vật tư y tế bệnh viện

Ứng dụng quản lý vật tư y tế nội bộ cho bệnh viện, tập trung vào tồn kho theo lô, cấp phát FEFO, cảnh báo hạn dùng, tồn tại khoa, điều kiện bảo quản, lịch sử giá nhập, đề nghị mua, chứng từ đính kèm, dashboard vận hành, chatbot nội bộ, thông báo và nhật ký thao tác.

## Công Nghệ

- Java 17
- Spring Boot 3.3.5
- Spring Security
- Spring Data JPA / Hibernate
- SQL Server
- Thymeleaf
- Bootstrap 5
- Maven Wrapper

## Yêu Cầu Môi Trường

- JDK 17. Project đặt `<java.version>17</java.version>` trong `pom.xml`.
- SQL Server đang chạy, bật SQL Server Authentication và TCP/IP.
- Database `QLVT`.
- Tài khoản SQL Server `tam` có quyền truy cập database `QLVT`.

Nếu VS Code đang chạy bằng JDK/JRE 21, cài JDK 17 và chọn lại Java runtime trong VS Code bằng lệnh `Java: Configure Java Runtime`. Nếu máy chỉ có JDK 21 thì cần cài thêm JDK 17 trước khi trỏ VS Code sang runtime 17.

## Cấu Hình SQL Server Local

Ứng dụng dùng profile `dev` mặc định qua `spring.profiles.active=${SPRING_PROFILES_ACTIVE:dev}`. Cấu hình local nằm trong `src/main/resources/application-dev.properties`:

```properties
spring.datasource.url=${DB_URL:jdbc:sqlserver://localhost:1433;databaseName=QLVT;encrypt=true;trustServerCertificate=true}
spring.datasource.username=${DB_USERNAME:tam}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
spring.jpa.database-platform=org.hibernate.dialect.SQLServerDialect
spring.jpa.hibernate.ddl-auto=${JPA_DDL_AUTO:update}
spring.jpa.show-sql=${JPA_SHOW_SQL:true}
```

Nếu máy dùng instance khác, đặt `DB_URL`, ví dụ:

```powershell
$env:DB_URL="jdbc:sqlserver://localhost\SQLEXPRESS;databaseName=QLVT;encrypt=true;trustServerCertificate=true"
```

Tạo hoặc sửa login local:

```powershell
sqlcmd -S localhost -E -i database/fix-login-user.sql -v QLVT_SQL_PASSWORD="your-strong-password"
```

Sau đó chạy các script schema/seed theo đúng thứ tự nếu cần dựng lại dữ liệu:

```text
database/01_create_schema.sql
database/02_seed_master_data.sql
database/03_seed_demo_data.sql
```

`01_create_schema.sql` có thể chạy lại từ đầu vì script sẽ drop FK/bảng ứng dụng rồi tạo lại schema. `02_seed_master_data.sql` nạp role, người dùng, khoa, kho, vị trí, nhà cung cấp và danh mục vật tư. `03_seed_demo_data.sql` nạp dữ liệu nghiệp vụ demo như lô, tồn kho, yêu cầu cấp phát, nhập/xuất, kiểm kê, chuyển kho, thu hồi, hủy, audit log và thông báo.

Ứng dụng đọc cấu hình chung từ `src/main/resources/application.properties`, cấu hình dev từ `src/main/resources/application-dev.properties`, và có thể ghi đè bằng biến môi trường. Với profile `dev`, `DB_PASSWORD` là bắt buộc để tránh app đăng nhập SQL Server bằng mật khẩu rỗng.

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `SPRING_PROFILES_ACTIVE`
- `JPA_SHOW_SQL`

Ví dụ cấu hình local trên PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
$env:DB_URL="jdbc:sqlserver://localhost:1433;databaseName=QLVT;encrypt=true;trustServerCertificate=true"
$env:DB_USERNAME="tam"
$env:DB_PASSWORD="your-strong-password"
```

Checklist trước khi chạy:

- SQL Server service đang chạy.
- SQL Server Authentication đang bật.
- TCP/IP đang bật, port `1433` đang listen hoặc `DB_URL` trỏ đúng instance như `localhost\SQLEXPRESS`.
- `DB_ID('QLVT')` trả về giá trị khác `NULL`.
- Login `tam` tồn tại, không bị disabled, password đúng.
- User/login `tam` có quyền `db_owner` hoặc quyền tương đương trên `QLVT`.
- Active profile là `dev`.

Kiểm tra nhanh bằng `sqlcmd`:

```powershell
sqlcmd -S localhost -U tam -P $env:DB_PASSWORD -d QLVT -Q "SELECT DB_NAME(), SUSER_SNAME(), USER_NAME()"
```

## Chạy Project

Cách chạy ổn định nhất trên PowerShell là đặt biến môi trường ngay trong terminal đang dùng:

```powershell
cd E:\QLVT
$env:SPRING_PROFILES_ACTIVE="dev"
$env:DB_USERNAME="tam"
$env:DB_PASSWORD="your-strong-password"
.\mvnw.cmd clean spring-boot:run
```

Hoặc dùng script kiểm tra SQL Server trước khi chạy:

```powershell
cd E:\QLVT
$env:DB_PASSWORD="your-strong-password"
.\scripts\run-dev.ps1
```

Nếu chỉ muốn kiểm tra cấu hình SQL Server:

```powershell
cd E:\QLVT
$env:DB_PASSWORD="your-strong-password"
.\scripts\verify-sqlserver.ps1
```

Nếu đã set User Environment bằng `[Environment]::SetEnvironmentVariable(...)`, hãy đóng mở lại VS Code để Java Debugger nhận biến mới. Terminal hoặc VS Code đang mở từ trước có thể vẫn chạy với `DB_PASSWORD` rỗng và gây lỗi `Login failed for user 'tam'`.

Hoặc build jar:

```powershell
cd E:\QLVT
.\mvnw.cmd -DskipTests package
java -jar target\qlvt-1.0.0.jar
```

Mở `http://localhost:8080/login`.

## Build Và Test

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd clean package
```

Project có Maven Wrapper chuẩn trong `.mvn/wrapper`. Launcher ưu tiên Maven local nếu đã có sẵn để tránh lỗi truststore Java trên môi trường Windows hiện tại; trên máy chưa cài Maven, `mvnw` sẽ dùng wrapper để tải Maven 3.9.15 từ Maven Central.

Nếu Maven báo `PKIX path building failed` khi tải Surefire từ Maven Central, đây là lỗi chứng chỉ môi trường Java/Maven. Có thể kiểm tra compile/package tạm thời bằng:

```powershell
.\mvnw.cmd -DskipTests package
```

## Tài Khoản Mẫu

Các tài khoản demo chỉ dùng cho môi trường dev/demo. Đặt mật khẩu demo bằng `APP_DEMO_DEFAULT_PASSWORD`; không commit mật khẩu thật vào source.

- `admin`: quản trị hệ thống
- `thukho`: thủ kho
- `truongkhoa`: trưởng khoa Cấp cứu
- `nhanvien`: nhân viên khoa Cấp cứu
- `nhanvien2`: nhân viên khoa Nội tổng hợp
- `ketoan`: kế toán vật tư
- `lanhdao`: lãnh đạo bệnh viện

## Role Và Quyền Chính

- `ADMIN`: toàn quyền hệ thống, quản trị người dùng, khoa/phòng, audit log.
- `WAREHOUSE_STAFF`: nhập kho, xuất kho, quản lý lô, tồn kho, kiểm kê, điều chỉnh, thu hồi, hủy vật tư.
- `DEPARTMENT_STAFF`: tạo yêu cầu cấp phát, xem/tác nghiệp tồn tại khoa theo phân quyền.
- `DEPARTMENT_HEAD`: duyệt yêu cầu cấp phát ở cấp khoa.
- `ACCOUNTANT`: xem báo cáo, lịch sử giá, cảnh báo giá, nghiệp vụ mua hàng/kế toán liên quan.
- `PROCUREMENT`: đề nghị mua, đơn mua, lịch sử/cảnh báo giá.
- `MANAGER`: dashboard, duyệt nghiệp vụ kho, kiểm kê, báo cáo.

## Module Chính

- Người dùng, vai trò, khoa/phòng, kho, vị trí lưu trữ.
- Vật tư, lô, hạn dùng, tồn theo kho/vị trí/lô.
- Cảnh báo tồn thấp, hết hàng, sắp hết hạn.
- Điều kiện bảo quản vật tư, ghi nhận nhiệt độ/độ ẩm kho, cảnh báo chuỗi lạnh.
- Nhập kho, xác nhận nhập, lịch sử giá nhập.
- Yêu cầu cấp vật tư, duyệt khoa, duyệt kho, giữ hàng.
- Xuất kho FEFO, batch allocation, khoa xác nhận nhận.
- Tồn tại khoa, báo sử dụng, báo hỏng/mất/hết hạn.
- Kiểm kê, điều chỉnh tồn, chuyển kho, thu hồi, hủy vật tư.
- Đề nghị mua, đơn mua, cảnh báo giá, lịch sử giá nhập.
- Dashboard thông minh, báo cáo, chatbot nội bộ, thông báo, audit log.

## Luồng Nghiệp Vụ Chính

1. Khoa/phòng tạo yêu cầu cấp phát, trưởng khoa duyệt, kho duyệt và xuất theo FEFO.
2. Thủ kho nhập hàng theo lô, gán kho/vị trí, hệ thống đồng bộ tồn tổng từ lô hợp lệ.
3. Kiểm kê ghi nhận chênh lệch, tạo phiếu điều chỉnh, quản lý/kế toán duyệt theo ngưỡng giá trị.
4. Lô sắp hết hạn được ưu tiên xuất; lô hết hạn, cách ly, thu hồi hoặc hủy bị chặn cấp phát.
5. Vật tư tồn thấp hoặc có tốc độ dùng cao được đưa vào đề xuất mua hàng trên dashboard.
6. Kho ghi nhận nhiệt độ/độ ẩm; cảnh báo bảo quản xuất hiện trên dashboard và module bảo quản.

## Quy Tắc Tồn Kho

- Tồn tổng của vật tư được đồng bộ từ các lô còn hiệu lực.
- Lô hết hạn, cách ly, thu hồi hoặc hủy không được cấp phát.
- Xuất kho dùng FEFO: ưu tiên lô có hạn dùng gần nhất trước.
- Không cho số lượng âm và không cho xuất vượt tồn khả dụng.
- Điều chỉnh tồn phải đi qua phiếu điều chỉnh hoặc kiểm kê.

## QR Vật Tư

- QR công khai chỉ hiển thị mã, tên, loại, đơn vị tính, ghi chú an toàn và trạng thái cơ bản.
- QR nội bộ yêu cầu đăng nhập và quyền kho/quản lý/admin để xem tồn, lô, hạn dùng, vị trí và trạng thái.

## Ghi Chú Vận Hành

- Khi dữ liệu cũ bị lệch giữa vật tư và lô, runner khởi động sẽ đồng bộ lại `materials.actual_quantity` từ các lô hợp lệ.
- Không commit cấu hình local chứa mật khẩu thật.

## Tài Liệu Bàn Giao

- `BUSINESS_FLOW.md`: mô tả luồng nghiệp vụ và điểm kiểm soát.
- `UI_GUIDELINE.md`: chuẩn giao diện, spacing, typography và trạng thái.
- `TEST_CHECKLIST.md`: checklist kiểm thử thủ công theo giai đoạn.
- `CHANGELOG.md`: thay đổi đã hoàn thiện trong đợt nâng cấp.
