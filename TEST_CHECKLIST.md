# TEST CHECKLIST - QLVT

## Giai đoạn 1 - Nền tảng, phân quyền, QR, FEFO

### Phân quyền

- Đăng nhập `admin`: truy cập được quản trị người dùng, khoa/phòng, audit log, vật tư, kho, nhập/xuất, báo cáo.
- Đăng nhập `thukho`: truy cập được vật tư, kho, vị trí, tồn kho, nhập kho, xuất kho, lô/HSD, kiểm kê, điều chỉnh tồn, chuyển kho, thu hồi, hủy vật tư.
- Đăng nhập `nhanvien`: tạo/xem yêu cầu cấp phát và tồn tại khoa; không thấy menu quản trị người dùng/audit/kho nội bộ không thuộc quyền.
- Đăng nhập `ketoan`: xem báo cáo, lịch sử giá, cảnh báo giá; không sửa trực tiếp tồn kho.
- Truy cập URL trái quyền phải trả về trang lỗi 403 dễ hiểu, không lộ stacktrace.

### QR

- Mở `/qr/public/material/{code}` khi chưa đăng nhập: chỉ thấy mã, tên, loại, đơn vị tính, ghi chú an toàn, trạng thái cơ bản.
- QR public không hiển thị tồn kho, vị trí kho, nhà cung cấp, giá nhập, lô, hạn dùng chi tiết.
- Mở `/qr/internal/material/{code}` khi chưa đăng nhập: chuyển về đăng nhập hoặc bị chặn.
- Mở QR nội bộ bằng `admin`, `thukho`, `lanhdao`: thấy tồn theo lô/vị trí và tổng tồn tính từ lô hợp lệ.

### Tồn kho và FEFO

- Nhập kho một lô mới: số lượng lô tăng, tồn vật tư được đồng bộ lại từ lô.
- Xuất kho: hệ thống lấy lô còn hạn, trạng thái `AVAILABLE`, hạn dùng gần nhất trước.
- Không xuất lô hết hạn, cách ly, thu hồi hoặc đã hủy.
- Không xuất vượt tồn khả dụng.
- Không cho số lượng âm ở tồn kho, tồn lô, tồn tại khoa.
- Xóa/đảo phiếu nhập hoặc phiếu xuất: lô, tồn vị trí và tồn vật tư được đồng bộ lại.
- Nhận trả từ khoa: tồn khoa giảm, lô kho tăng, tồn vật tư được đồng bộ lại.

### Dữ liệu mẫu và cấu hình

- Có ít nhất 50 vật tư mẫu.
- Có ít nhất 10 nhà cung cấp mẫu.
- Có ít nhất 50 lô hàng mẫu.
- Có dữ liệu tồn thấp, sắp hết hạn, thu hồi/hủy/cách ly để kiểm tra dashboard/cảnh báo.
- `application.properties` không chứa mật khẩu SQL Server thật; dùng `DB_PASSWORD`.
- README hiển thị tiếng Việt đúng và hướng dẫn cấu hình bằng biến môi trường.

### UI Foundation

- Sidebar/menu hiển thị theo role.
- Bảng có trạng thái, action rõ ràng, không lộ nút ngoài quyền.
- Form nhập/xuất/yêu cầu có validation thông báo rõ.
- Trang lỗi/403 dùng tiếng Việt dễ hiểu.
- QR public/internal hiển thị rõ, không dùng Bootstrap mặc định quá thô.

## Giai đoạn 2 - Nghiệp vụ kho bệnh viện

### Kho, vị trí, lô

- Mở `/warehouses`: xem danh sách kho, thêm/sửa/xóa mềm kho theo quyền.
- Mở `/locations`: xem vị trí theo kho, thêm/sửa vị trí và gán lô vào vị trí khi nhập/chuyển.
- Mở chi tiết vật tư/lô: thấy tồn theo lô, kho, vị trí và trạng thái lô.
- Lô `QUARANTINED`, `RECALLED`, `DESTROYED` hoặc hết hạn không xuất được.

### Yêu cầu cấp phát

- `nhanvien` tạo phiếu yêu cầu nhiều dòng vật tư.
- `truongkhoa` duyệt hoặc từ chối cấp khoa.
- `thukho` duyệt cấp kho, giữ hàng và tạo phiếu xuất.
- Xuất từ yêu cầu phải có phân bổ lô và không vượt tồn khả dụng.
- Khoa xác nhận nhận hàng, tồn tại khoa tăng đúng lô nhận.

### Kiểm kê, điều chỉnh, chuyển kho

- Tạo đợt kiểm kê theo kho, hệ thống lấy tồn hiện tại.
- Nhập số lượng thực tế, hệ thống tính chênh lệch.
- Tạo phiếu điều chỉnh từ kiểm kê có chênh lệch.
- Phiếu điều chỉnh cần duyệt trước khi áp dụng; sau khi áp dụng, tồn lô và tồn vật tư đồng bộ.
- Chuyển kho giảm tồn vị trí nguồn và tăng tồn vị trí đích, có audit log.

### Thu hồi, hủy và bảo quản

- Tạo lệnh thu hồi theo vật tư/lô, ghi nhận phản hồi khoa.
- Tạo phiếu hủy, duyệt và hủy lô theo quyền.
- Mở `/storage-monitoring`, cấu hình nhiệt độ/độ ẩm cho vật tư.
- Ghi nhận nhiệt độ/độ ẩm kho; trạng thái `WARNING` hoặc `RISK` xuất hiện trên dashboard.
- Thao tác bảo quản ghi audit log.

## Giai đoạn 3 - Dashboard, chatbot, báo cáo, bàn giao

### Dashboard thông minh

- Dashboard hiển thị giá trị tồn kho, tồn thấp, lô hết hạn 30/90 ngày, lô bị khóa và cảnh báo bảo quản.
- Khối đề xuất mua hàng xuất hiện khi vật tư dưới tồn tối thiểu hoặc ước tính còn dưới 14 ngày.
- Top vật tư xuất nhiều trong 30 ngày hiển thị theo dữ liệu `stock_movements`.
- Dashboard không lỗi khi chưa có dữ liệu xuất kho, nhiệt độ hoặc đề xuất mua.

### Chatbot nội bộ

- Hỏi `VT001 còn bao nhiêu` trả lời tồn và vị trí nếu có dữ liệu.
- Hỏi `VT001 nằm ở đâu` trả lời kho/vị trí/lô.
- Hỏi `lô nào sắp hết hạn` hoặc mã lô trả lời hạn dùng và cảnh báo.
- Hỏi `phiếu của tôi đến đâu rồi` trả lời trạng thái phiếu theo user/khoa.
- Chatbot không trả lời bịa khi không tìm thấy vật tư; phải gợi ý nhập mã hoặc tên rõ hơn.

### Báo cáo và dữ liệu

- `/reports` mở được, xuất CSV tồn kho và biến động kho.
- `/reports/advanced` hiển thị tồn theo lô, lô sắp hết hạn, kiểm kê, hủy/thu hồi và đơn hàng trễ.
- Dữ liệu mẫu có kho, vị trí, lô gần hết hạn, lô rủi ro, cảnh báo giá, đề nghị mua và nhiệt độ kho.
- README, BUSINESS_FLOW, UI_GUIDELINE, CHANGELOG cập nhật đúng tiếng Việt.

### Kiểm chứng cuối

- `.\mvnw.cmd -DskipTests compile` thành công.
- `.\mvnw.cmd -DskipTests package` thành công.
- Khởi động jar với `DB_PASSWORD` đúng và thấy log `Started QlvtApplication`.
- Truy cập `/login`, đăng nhập `admin`, mở dashboard, vật tư, nhập kho, yêu cầu, bảo quản, báo cáo không lỗi 500.
