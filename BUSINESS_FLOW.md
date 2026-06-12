# BUSINESS FLOW - QLVT

## Nguyên tắc nghiệp vụ

- Không sửa trực tiếp tồn tổng vật tư khi vật tư đã quản lý theo lô.
- Tồn tổng được đồng bộ từ các lô `AVAILABLE`, còn hạn và còn số lượng.
- Xuất kho dùng FEFO, bỏ qua lô hết hạn, cách ly, thu hồi hoặc đã hủy.
- Chênh lệch tồn phải đi qua kiểm kê hoặc phiếu điều chỉnh.
- Thao tác nghiệp vụ quan trọng phải có audit log hoặc stock movement.

## Luồng nhập kho

1. Thủ kho tạo phiếu nhập.
2. Nhập nhiều dòng vật tư, lô, hạn dùng, kho và vị trí.
3. Xác nhận nhập.
4. Hệ thống tạo/cập nhật lô, tồn vị trí, stock movement và đồng bộ tồn vật tư.
5. Nếu giá nhập biến động, hệ thống lưu lịch sử giá và cảnh báo giá.

## Luồng cấp phát FEFO

1. Nhân viên khoa tạo yêu cầu cấp phát.
2. Trưởng khoa duyệt cấp khoa.
3. Thủ kho/quản lý duyệt cấp kho.
4. Hệ thống giữ hàng và tạo phiếu xuất.
5. Khi xuất, hệ thống phân bổ theo FEFO và lưu từng lô đã xuất.
6. Khoa xác nhận nhận hàng, tồn tại khoa tăng theo lô nhận.

## Luồng kiểm kê và điều chỉnh

1. Thủ kho tạo đợt kiểm kê theo kho.
2. Hệ thống lấy tồn theo vật tư, lô, vị trí.
3. Người kiểm kê nhập tồn thực tế.
4. Hệ thống tính chênh lệch.
5. Từ chênh lệch tạo phiếu điều chỉnh.
6. Quản lý hoặc kế toán duyệt theo phân quyền.
7. Khi áp dụng, hệ thống cập nhật lô, tồn vị trí và tồn vật tư.

## Luồng xử lý lô rủi ro

1. Lô sắp hết hạn được cảnh báo và ưu tiên dùng.
2. Lô lỗi chất lượng có thể đưa vào thu hồi hoặc hủy.
3. Lô cách ly, thu hồi, hủy hoặc hết hạn không được xuất.
4. Dashboard hiển thị các lô khóa/rủi ro để kho xử lý trước.

## Luồng bảo quản

1. Cấu hình điều kiện bảo quản theo vật tư.
2. Ghi nhận nhiệt độ/độ ẩm theo kho.
3. Hệ thống đánh giá `NORMAL`, `WARNING`, `RISK`.
4. Cảnh báo `WARNING` và `RISK` xuất hiện trên dashboard.
5. Kho lạnh vượt ngưỡng cần kiểm tra và cân nhắc cách ly lô nhạy cảm.

## Luồng mua hàng

1. Dashboard tính tồn hiện tại và mức dùng trung bình 30 ngày.
2. Vật tư dưới tồn tối thiểu hoặc ước tính còn dưới 14 ngày được gợi ý mua.
3. Nhân sự mua hàng tạo đề nghị mua.
4. Đề nghị mua được duyệt và chuyển thành đơn mua.
5. Khi hàng về, phiếu nhập có thể liên kết lại luồng mua hàng.
