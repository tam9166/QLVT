# UI GUIDELINE - QLVT

## Định hướng

Giao diện ưu tiên cảm giác hệ thống vận hành bệnh viện: rõ ràng, dễ quét nhanh, ít trang trí, tập trung vào quyết định nghiệp vụ.

## Màu sắc

- Màu chính: xanh y tế `#0f766e`.
- Màu phụ: xanh navy `#0f4c81`, xanh hành động `#2563eb`.
- Cảnh báo: đỏ cho lỗi/rủi ro, vàng cho cần theo dõi, xanh lá cho ổn định.
- Nền: xám sạch, dùng shadow nhẹ để tách lớp thay vì viền dày.

## Spacing và layout

- 4px: khoảng cách rất nhỏ giữa icon và text phụ.
- 8px: khoảng cách trong chip/badge.
- 12px: khoảng cách label/input hoặc các item trong danh sách.
- 16px: padding panel nhỏ.
- 24px: khoảng cách giữa section.
- 32px: vùng hero hoặc block lớn.

## Typography

- Page title: 28-32px, weight 700-800.
- Section title: 20-24px, weight 600-800.
- Card title: 16-18px, weight 600-800.
- Body/table: 14-16px.
- Badge: 12-13px, weight 700-800.

## Component pattern

- Dashboard dùng metric card, bảng cảnh báo và thanh xếp hạng.
- Bảng có mã dạng `code-pill`, badge trạng thái và empty state tiếng Việt.
- Form chia theo nhóm, dùng label rõ và nút chính/phụ phân cấp.
- Nút nguy hiểm như hủy, xóa, thu hồi phải dùng màu cảnh báo và có xác nhận ở luồng nghiệp vụ.
- Sidebar hiển thị theo quyền, icon nhất quán và active state rõ.

## Microcopy

- Dùng câu tiếng Việt tự nhiên.
- Tránh chữ chung chung như `Error`, `Submit`, `Data not found`.
- Dùng: `Có lỗi xảy ra, vui lòng kiểm tra lại thông tin.`, `Lưu thay đổi`, `Chưa có dữ liệu phù hợp.`

## Trang trọng yếu

- Dashboard: số liệu ra quyết định, cảnh báo kho lạnh, lô rủi ro, đề xuất mua.
- Vật tư/lô: trạng thái, tồn, kho/vị trí, hạn dùng, QR public/internal.
- Yêu cầu cấp phát: trạng thái, người duyệt, hành động tiếp theo.
- Kiểm kê/điều chỉnh: chênh lệch, phê duyệt, không sửa tồn trực tiếp.
- Báo cáo: có bộ lọc, dữ liệu xuất, bảng rõ và tổng hợp dễ đọc.
