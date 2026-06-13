# CHANGELOG

## 2026-06-12 - Hoàn thiện theo 3 giai đoạn

### Giai đoạn 1

- Đồng bộ role, menu và security path cho các luồng quản trị, kho, khoa, kế toán và quản lý.
- Tách QR public/internal: public không lộ tồn kho, vị trí, nhà cung cấp, giá, lô và hạn dùng nội bộ.
- Thêm `InventorySyncService` để đồng bộ tồn tổng vật tư từ lô còn hiệu lực.
- Chuẩn hóa FEFO, bỏ qua lô hết hạn/cách ly/thu hồi/hủy.
- Bỏ mật khẩu SQL Server thật khỏi cấu hình mặc định, chuyển sang `DB_PASSWORD`.
- Sửa tiếng Việt ở layout, error page, access denied, QR và README.
- Cập nhật màu UI sang xanh y tế/navy và thêm checklist test giai đoạn 1.

### Giai đoạn 2

- Bổ sung quản lý điều kiện bảo quản vật tư.
- Bổ sung ghi nhận nhiệt độ/độ ẩm kho và đánh giá `NORMAL`, `WARNING`, `RISK`.
- Thêm bảng migration runtime cho `storage_conditions` và `temperature_logs`.
- Thêm dữ liệu mẫu điều kiện bảo quản và nhiệt độ kho.
- Gắn menu và phân quyền `/storage-monitoring`.
- Đưa cảnh báo bảo quản lên dashboard.

### Giai đoạn 3

- Nâng dashboard thành màn hình ra quyết định: giá trị tồn, tồn thấp, hạn dùng 30/90 ngày, giá trị hàng sắp hết hạn, lô khóa/rủi ro.
- Thêm đề xuất mua hàng trên dashboard dựa trên tồn khả dụng, tồn tối thiểu và mức dùng trung bình 30 ngày.
- Thêm top vật tư xuất nhiều trong 30 ngày từ `stock_movements`.
- Cập nhật tài liệu bàn giao: `README.md`, `BUSINESS_FLOW.md`, `UI_GUIDELINE.md`, `TEST_CHECKLIST.md`.
- Bổ sung CSS cho alert row, rank bar, empty state và badge trạng thái đúng màu.

## Kiểm chứng đã chạy

- `.\mvnw.cmd -DskipTests compile`: thành công sau thay đổi dashboard.
- `.\mvnw.cmd -DskipTests package`: thành công, tạo `target\qlvt-1.0.0.jar`.
- Smoke run jar với `DB_PASSWORD=123456`: log có `Started QlvtApplication`.
- HTTP smoke đăng nhập `admin/123456`: `/dashboard`, `/materials`, `/requests`, `/storage-monitoring`, `/reports`, `/reports/advanced`, `/qr/internal/material/VT001` đều trả 200.
- Port 8080 đã được giải phóng sau smoke run.

## Cần kiểm chứng lại khi bàn giao

- Kiểm thử thủ công các URL chính theo `TEST_CHECKLIST.md`.
- `.\mvnw.cmd test` đang bị chặn bởi lỗi chứng chỉ môi trường khi Maven tải `org.apache.maven.surefire:surefire-junit-platform:3.2.5` từ Maven Central (`PKIX path building failed`).

## 2026-06-13 - Tiếp tục giai đoạn 2

- Sửa chi tiết thu hồi để nạp sẵn `RecallOrder.lines`, tránh `LazyInitializationException` khi `spring.jpa.open-in-view=false`.
- Bổ sung lịch sử phản hồi khoa trên trang chi tiết thu hồi.
- Khi khoa trả hàng theo lệnh thu hồi, hệ thống trừ `department_stocks`, ghi `department_stock_movements` loại `RECALL_RETURN`, đồng thời tăng lại tồn kho/lô và ghi `stock_movements`.
- Chặn phản hồi thu hồi vượt số đã cấp khi có nhiều lần trả hàng.
- Thêm alias `/requests/my` và `/requests/my-department` cho người dùng khoa xem phiếu của phạm vi mình.
- Sửa layout để trang lỗi không bị 500 kép khi `currentPath` không có trong model.
- Smoke trên database dựng lại từ `database/01_create_schema.sql`, `02_seed_master_data.sql`, `03_seed_demo_data.sql`: các trang giai đoạn 2 và role mẫu đều trả 200.

## 2026-06-13 - Cải thiện giai đoạn 3

- Sửa dashboard để top vật tư xuất nhiều và đề xuất mua hàng tính số lượng xuất theo trị tuyệt đối của biến động `OUT`, tránh mất dữ liệu khi phiếu xuất ghi số âm.
- Tách số đếm lô hết hạn 30 ngày và lô khóa/rủi ro khỏi danh sách hiển thị giới hạn 10 dòng, giúp metric dashboard phản ánh toàn bộ dữ liệu.
- Chỉnh rule chatbot để `vật tư ... còn không` ưu tiên kiểm tra tồn kho, không bị lệch sang vị trí vì từ khóa ngắn `tu`.
- Chỉnh ngữ cảnh chatbot để không tự gắn vật tư gần nhất chỉ vì câu có chữ `còn/không`.
- Smoke trên database dựng lại từ SQL: `/dashboard`, `/reports`, `/reports/advanced`, CSV tồn kho, CSV biến động và chatbot các câu checklist đều hoạt động.
