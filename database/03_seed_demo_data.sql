USE [qlvt];
GO
-- Demo workflow data: batches, requests, receipts/issues, inventory, transfers, recalls, reports and dashboard samples.
PRINT N'Seeding audit_logs';
SET IDENTITY_INSERT [dbo].[audit_logs] ON;
INSERT INTO [dbo].[audit_logs] ([id], [action], [created_at], [detail], [target_code], [target_type], [username], [actor_username], [entity_id], [entity_name], [ip_address], [new_value], [old_value]) VALUES (1, N'CREATE_REQUEST', '2026-06-04T23:02:36.4911260', N'Tạo yêu cầu cấp vật tư', NULL, NULL, NULL, N'admin', N'YC-20260604230236', N'MATERIAL_REQUEST', NULL, NULL, NULL);
INSERT INTO [dbo].[audit_logs] ([id], [action], [created_at], [detail], [target_code], [target_type], [username], [actor_username], [entity_id], [entity_name], [ip_address], [new_value], [old_value]) VALUES (2, N'UPDATE_USER', '2026-06-04T23:27:50.6592400', N'Lưu tài khoản ketoan', NULL, NULL, NULL, N'admin', N'ketoan', N'AppUser', NULL, N'ketoan|ACCOUNTANT', N'ketoan|ACCOUNTANT');
INSERT INTO [dbo].[audit_logs] ([id], [action], [created_at], [detail], [target_code], [target_type], [username], [actor_username], [entity_id], [entity_name], [ip_address], [new_value], [old_value]) VALUES (3, N'CREATE_RECEIPT', '2026-06-05T01:43:22.9404930', N'Tạo phiếu nhập kho', NULL, NULL, NULL, N'admin', N'PN-20260605014322', N'RECEIPT', NULL, NULL, NULL);
INSERT INTO [dbo].[audit_logs] ([id], [action], [created_at], [detail], [target_code], [target_type], [username], [actor_username], [entity_id], [entity_name], [ip_address], [new_value], [old_value]) VALUES (4, N'CREATE_RECEIPT', '2026-06-05T06:00:57.0870540', N'Tạo phiếu nhập kho', NULL, NULL, NULL, N'admin', N'PN-1', N'RECEIPT', NULL, NULL, NULL);
INSERT INTO [dbo].[audit_logs] ([id], [action], [created_at], [detail], [target_code], [target_type], [username], [actor_username], [entity_id], [entity_name], [ip_address], [new_value], [old_value]) VALUES (5, N'CREATE_RECEIPT', '2026-06-05T06:01:29.6815680', N'Tạo phiếu nhập kho', NULL, NULL, NULL, N'admin', N'PN-2', N'RECEIPT', NULL, NULL, NULL);
INSERT INTO [dbo].[audit_logs] ([id], [action], [created_at], [detail], [target_code], [target_type], [username], [actor_username], [entity_id], [entity_name], [ip_address], [new_value], [old_value]) VALUES (6, N'CREATE_RECEIPT', '2026-06-05T06:02:06.2062990', N'Tạo phiếu nhập kho', NULL, NULL, NULL, N'admin', N'PN-3', N'RECEIPT', NULL, NULL, NULL);
INSERT INTO [dbo].[audit_logs] ([id], [action], [created_at], [detail], [target_code], [target_type], [username], [actor_username], [entity_id], [entity_name], [ip_address], [new_value], [old_value]) VALUES (7, N'CREATE_RECEIPT', '2026-06-05T06:02:06.2115190', N'Tạo phiếu nhập kho', NULL, NULL, NULL, N'admin', N'PN-4', N'RECEIPT', NULL, NULL, NULL);
INSERT INTO [dbo].[audit_logs] ([id], [action], [created_at], [detail], [target_code], [target_type], [username], [actor_username], [entity_id], [entity_name], [ip_address], [new_value], [old_value]) VALUES (8, N'CREATE_RECEIPT', '2026-06-05T06:10:27.1475050', N'Tạo phiếu nhập kho', NULL, NULL, NULL, N'admin', N'PN-5', N'RECEIPT', NULL, NULL, NULL);
INSERT INTO [dbo].[audit_logs] ([id], [action], [created_at], [detail], [target_code], [target_type], [username], [actor_username], [entity_id], [entity_name], [ip_address], [new_value], [old_value]) VALUES (9, N'CREATE_RECEIPT', '2026-06-05T06:11:30.2968290', N'Tạo phiếu nhập kho', NULL, NULL, NULL, N'admin', N'PN-6', N'RECEIPT', NULL, NULL, NULL);
INSERT INTO [dbo].[audit_logs] ([id], [action], [created_at], [detail], [target_code], [target_type], [username], [actor_username], [entity_id], [entity_name], [ip_address], [new_value], [old_value]) VALUES (10, N'CREATE_RECEIPT', '2026-06-05T06:11:48.9343510', N'Tạo phiếu nhập kho', NULL, NULL, NULL, N'admin', N'PN-7', N'RECEIPT', NULL, NULL, NULL);
INSERT INTO [dbo].[audit_logs] ([id], [action], [created_at], [detail], [target_code], [target_type], [username], [actor_username], [entity_id], [entity_name], [ip_address], [new_value], [old_value]) VALUES (11, N'CREATE_RECEIPT', '2026-06-05T06:12:26.3095730', N'Tạo phiếu nhập kho', NULL, NULL, NULL, N'admin', N'PN-8', N'RECEIPT', NULL, NULL, NULL);
INSERT INTO [dbo].[audit_logs] ([id], [action], [created_at], [detail], [target_code], [target_type], [username], [actor_username], [entity_id], [entity_name], [ip_address], [new_value], [old_value]) VALUES (12, N'DEPARTMENT_APPROVE', '2026-06-05T06:15:44.1711720', N'Trưởng khoa duyệt yêu cầu', NULL, NULL, NULL, N'admin', N'YC-20260604230236', N'MATERIAL_REQUEST', NULL, NULL, NULL);
INSERT INTO [dbo].[audit_logs] ([id], [action], [created_at], [detail], [target_code], [target_type], [username], [actor_username], [entity_id], [entity_name], [ip_address], [new_value], [old_value]) VALUES (14, N'CREATE_RECEIPT', '2026-06-05T07:58:34.9962070', N'Tạo phiếu nhập kho', NULL, NULL, NULL, N'admin', N'PN-9', N'RECEIPT', NULL, NULL, NULL);
INSERT INTO [dbo].[audit_logs] ([id], [action], [created_at], [detail], [target_code], [target_type], [username], [actor_username], [entity_id], [entity_name], [ip_address], [new_value], [old_value]) VALUES (15, N'DELETE_RECEIPT', '2026-06-05T07:58:35.1048130', N'Xóa phiếu nhập kho', NULL, NULL, NULL, N'admin', N'PN-9', N'RECEIPT', NULL, NULL, NULL);
INSERT INTO [dbo].[audit_logs] ([id], [action], [created_at], [detail], [target_code], [target_type], [username], [actor_username], [entity_id], [entity_name], [ip_address], [new_value], [old_value]) VALUES (16, N'DELETE_RECEIPT', '2026-06-05T08:18:34.3217620', N'Xóa phiếu nhập kho', NULL, NULL, NULL, N'admin', N'PN-8', N'RECEIPT', NULL, NULL, NULL);
INSERT INTO [dbo].[audit_logs] ([id], [action], [created_at], [detail], [target_code], [target_type], [username], [actor_username], [entity_id], [entity_name], [ip_address], [new_value], [old_value]) VALUES (17, N'DELETE_RECEIPT', '2026-06-05T10:36:09.1599110', N'Xóa phiếu nhập kho', NULL, NULL, NULL, N'admin', N'PN-7', N'RECEIPT', NULL, NULL, NULL);
INSERT INTO [dbo].[audit_logs] ([id], [action], [created_at], [detail], [target_code], [target_type], [username], [actor_username], [entity_id], [entity_name], [ip_address], [new_value], [old_value]) VALUES (18, N'DELETE_RECEIPT', '2026-06-05T10:36:12.2517670', N'Xóa phiếu nhập kho', NULL, NULL, NULL, N'admin', N'PN-6', N'RECEIPT', NULL, NULL, NULL);
INSERT INTO [dbo].[audit_logs] ([id], [action], [created_at], [detail], [target_code], [target_type], [username], [actor_username], [entity_id], [entity_name], [ip_address], [new_value], [old_value]) VALUES (19, N'DELETE_RECEIPT', '2026-06-05T10:36:14.9087020', N'Xóa phiếu nhập kho', NULL, NULL, NULL, N'admin', N'PN-5', N'RECEIPT', NULL, NULL, NULL);
INSERT INTO [dbo].[audit_logs] ([id], [action], [created_at], [detail], [target_code], [target_type], [username], [actor_username], [entity_id], [entity_name], [ip_address], [new_value], [old_value]) VALUES (20, N'DELETE_RECEIPT', '2026-06-05T10:36:16.9089750', N'Xóa phiếu nhập kho', NULL, NULL, NULL, N'admin', N'PN-4', N'RECEIPT', NULL, NULL, NULL);
INSERT INTO [dbo].[audit_logs] ([id], [action], [created_at], [detail], [target_code], [target_type], [username], [actor_username], [entity_id], [entity_name], [ip_address], [new_value], [old_value]) VALUES (21, N'CREATE_RECEIPT', '2026-06-05T10:36:50.8637830', N'Tạo phiếu nhập kho', NULL, NULL, NULL, N'admin', N'PN-4', N'RECEIPT', NULL, NULL, NULL);
INSERT INTO [dbo].[audit_logs] ([id], [action], [created_at], [detail], [target_code], [target_type], [username], [actor_username], [entity_id], [entity_name], [ip_address], [new_value], [old_value]) VALUES (22, N'CREATE_PRICE_HISTORY', '2026-06-05T10:37:09.6580930', N'Ghi lịch sử giá nhập vật tư', NULL, NULL, NULL, N'admin', N'CB001', N'MATERIAL', NULL, NULL, NULL);
INSERT INTO [dbo].[audit_logs] ([id], [action], [created_at], [detail], [target_code], [target_type], [username], [actor_username], [entity_id], [entity_name], [ip_address], [new_value], [old_value]) VALUES (23, N'CONFIRM_RECEIPT', '2026-06-05T10:37:09.6628800', N'Xác nhận nhập kho', NULL, NULL, NULL, N'admin', N'PN-4', N'RECEIPT', NULL, NULL, NULL);
INSERT INTO [dbo].[audit_logs] ([id], [action], [created_at], [detail], [target_code], [target_type], [username], [actor_username], [entity_id], [entity_name], [ip_address], [new_value], [old_value]) VALUES (24, N'DELETE_RECEIPT', '2026-06-05T10:37:44.5828430', N'Xóa phiếu nhập kho', NULL, NULL, NULL, N'admin', N'PN-3', N'RECEIPT', NULL, NULL, NULL);
INSERT INTO [dbo].[audit_logs] ([id], [action], [created_at], [detail], [target_code], [target_type], [username], [actor_username], [entity_id], [entity_name], [ip_address], [new_value], [old_value]) VALUES (25, N'CREATE_REQUEST', '2026-06-05T10:39:37.8188590', N'Tạo yêu cầu cấp vật tư', NULL, NULL, NULL, N'admin', N'YC-20260605103937', N'MATERIAL_REQUEST', NULL, NULL, NULL);
INSERT INTO [dbo].[audit_logs] ([id], [action], [created_at], [detail], [target_code], [target_type], [username], [actor_username], [entity_id], [entity_name], [ip_address], [new_value], [old_value]) VALUES (26, N'DEPARTMENT_APPROVE', '2026-06-05T10:41:58.7833890', N'Trưởng khoa duyệt yêu cầu', NULL, NULL, NULL, N'admin', N'YC-20260605103937', N'MATERIAL_REQUEST', NULL, NULL, NULL);
INSERT INTO [dbo].[audit_logs] ([id], [action], [created_at], [detail], [target_code], [target_type], [username], [actor_username], [entity_id], [entity_name], [ip_address], [new_value], [old_value]) VALUES (28, N'CREATE_REQUEST', '2026-06-05T10:43:37.0717520', N'Tạo yêu cầu cấp vật tư', NULL, NULL, NULL, N'admin', N'YC-20260605104337', N'MATERIAL_REQUEST', NULL, NULL, NULL);
SET IDENTITY_INSERT [dbo].[audit_logs] OFF;
GO
PRINT N'Seeding chat_sessions';
SET IDENTITY_INSERT [dbo].[chat_sessions] ON;
INSERT INTO [dbo].[chat_sessions] ([id], [created_at], [title], [updated_at], [username]) VALUES (1, '2026-05-31T22:38:39.7806640', N'Tra cứu QLVT', '2026-06-08T18:20:57.2992880', N'admin');
SET IDENTITY_INSERT [dbo].[chat_sessions] OFF;
GO
PRINT N'Seeding destruction_slips';
SET IDENTITY_INSERT [dbo].[destruction_slips] ON;
INSERT INTO [dbo].[destruction_slips] ([id], [approved_at], [approved_by], [created_at], [created_by], [destroyed_at], [destruction_code], [note], [reason], [rejected_at], [rejected_by], [rejected_reason], [status], [updated_at], [accountant_approved_at], [accountant_approved_by], [manager_approved_at], [manager_approved_by]) VALUES (1, NULL, NULL, '2026-06-04T19:59:50.0820150', N'thukho', NULL, N'HUY-DEMO-001', NULL, N'Hủy vật tư lỗi bao bì trong quá trình kiểm tra', NULL, NULL, NULL, N'SUBMITTED', '2026-06-05T00:59:50.0820150', NULL, NULL, NULL, NULL);
SET IDENTITY_INSERT [dbo].[destruction_slips] OFF;
GO
PRINT N'Seeding material_requests';
SET IDENTITY_INSERT [dbo].[material_requests] ON;
INSERT INTO [dbo].[material_requests] ([id], [code], [created_at], [department], [note], [priority], [requester], [status], [department_approved_at], [department_approved_by], [received_at], [received_by], [rejected_reason], [submitted_at], [updated_at], [warehouse_approved_at], [warehouse_approved_by]) VALUES (1, N'YC-20260604230236', '2026-06-04T23:02:36.3906220', N'Khoa Cấp cứu', N'', N'BINH_THUONG', N'admin', N'DEPARTMENT_APPROVED', '2026-06-05T06:15:44.1655710', N'admin', NULL, NULL, NULL, '2026-06-04T23:02:36.3982890', '2026-06-05T06:15:44.1655710', NULL, NULL);
INSERT INTO [dbo].[material_requests] ([id], [code], [created_at], [department], [note], [priority], [requester], [status], [department_approved_at], [department_approved_by], [received_at], [received_by], [rejected_reason], [submitted_at], [updated_at], [warehouse_approved_at], [warehouse_approved_by]) VALUES (2, N'YC-DEMO-001', '2026-06-04T20:26:50.6203110', N'Khoa Cấp cứu', N'Cấp bổ sung vật tư trực cấp cứu cuối tuần', N'Ưu tiên vừa', N'admin', N'SUBMITTED', NULL, NULL, NULL, NULL, NULL, '2026-06-04T20:31:50.6203110', '2026-06-04T23:26:50.6203110', NULL, NULL);
INSERT INTO [dbo].[material_requests] ([id], [code], [created_at], [department], [note], [priority], [requester], [status], [department_approved_at], [department_approved_by], [received_at], [received_by], [rejected_reason], [submitted_at], [updated_at], [warehouse_approved_at], [warehouse_approved_by]) VALUES (3, N'YC-DEMO-002', '2026-06-03T23:26:50.6682070', N'Khoa Nội tổng hợp', N'Trưởng khoa đã duyệt, chờ kho xử lý', N'Ưu tiên vừa', N'nhanvien2', N'DEPARTMENT_APPROVED', '2026-06-04T03:26:50.6682070', N'truongkhoa', NULL, NULL, NULL, '2026-06-03T23:31:50.6682070', '2026-06-04T23:26:50.6682070', NULL, NULL);
INSERT INTO [dbo].[material_requests] ([id], [code], [created_at], [department], [note], [priority], [requester], [status], [department_approved_at], [department_approved_by], [received_at], [received_by], [rejected_reason], [submitted_at], [updated_at], [warehouse_approved_at], [warehouse_approved_by]) VALUES (4, N'YC-20260605103937', '2026-06-05T10:39:37.7779550', N'Khoa Cấp cứu', N'', N'KHAN_CAP', N'admin', N'DEPARTMENT_APPROVED', '2026-06-05T10:41:58.7734760', N'admin', NULL, NULL, NULL, '2026-06-05T10:39:37.7779550', '2026-06-05T10:41:58.7734760', NULL, NULL);
INSERT INTO [dbo].[material_requests] ([id], [code], [created_at], [department], [note], [priority], [requester], [status], [department_approved_at], [department_approved_by], [received_at], [received_by], [rejected_reason], [submitted_at], [updated_at], [warehouse_approved_at], [warehouse_approved_by]) VALUES (5, N'YC-20260605104337', '2026-06-05T10:43:37.0601690', N'Khoa Cấp cứu', N'', N'KHAN_CAP', N'admin', N'SUBMITTED', NULL, NULL, NULL, NULL, NULL, '2026-06-05T10:43:37.0638610', '2026-06-05T10:43:37.0601690', NULL, NULL);
SET IDENTITY_INSERT [dbo].[material_requests] OFF;
GO
PRINT N'Seeding notifications';
SET IDENTITY_INSERT [dbo].[notifications] ON;
INSERT INTO [dbo].[notifications] ([id], [content], [created_at], [link], [read_status], [receiver], [title], [type]) VALUES (1, N'Mot so vat tu dang gan muc ton toi thieu, can theo doi bo sung.', '2026-05-31T17:21:48.2303690', N'/materials', 0, N'WAREHOUSE_STAFF', N'Canh bao ton kho', N'LOW_STOCK');
INSERT INTO [dbo].[notifications] ([id], [content], [created_at], [link], [read_status], [receiver], [title], [type]) VALUES (2, N'Có yêu cầu cấp vật tư mới cần kho xử lý.', '2026-06-04T23:26:50.8426670', N'/requests', 1, N'WAREHOUSE_STAFF', N'Yêu cầu mới cần xử lý', N'REQUEST');
INSERT INTO [dbo].[notifications] ([id], [content], [created_at], [link], [read_status], [receiver], [title], [type]) VALUES (3, N'Có phiếu trả vật tư từ khoa cần kiểm tra.', '2026-06-04T23:26:50.8426670', N'/department-returns', 0, N'WAREHOUSE_STAFF', N'Có phiếu trả từ khoa', N'DEPARTMENT_RETURN');
INSERT INTO [dbo].[notifications] ([id], [content], [created_at], [link], [read_status], [receiver], [title], [type]) VALUES (4, N'Phiếu kiểm kê đã hoàn tất, vui lòng xem chênh lệch nếu có.', '2026-06-04T23:26:50.8426670', N'/inventory-counts', 0, N'MANAGER', N'Kiểm kê đã hoàn tất', N'INVENTORY_COUNT');
INSERT INTO [dbo].[notifications] ([id], [content], [created_at], [link], [read_status], [receiver], [title], [type]) VALUES (5, N'VT001 - Khẩu trang y tế 4 lớp, lô LO-VT001-01, HSD 15/07/2026, số lượng 180, mức Còn 31-60 ngày (còn 33 ngày).', '2026-06-05T01:36:04.3733620', N'/batches/1', 1, N'WAREHOUSE_STAFF', N'Cảnh báo hạn sử dụng', N'EXPIRY_EXPIRING_60');
INSERT INTO [dbo].[notifications] ([id], [content], [created_at], [link], [read_status], [receiver], [title], [type]) VALUES (6, N'VT002 - Găng tay y tế không bột size M, lô LO-VT002-01, HSD 04/08/2026, số lượng 75, mức Còn 31-60 ngày (còn 53 ngày).', '2026-06-05T01:36:04.3890070', N'/batches/2', 0, N'WAREHOUSE_STAFF', N'Cảnh báo hạn sử dụng', N'EXPIRY_EXPIRING_60');
INSERT INTO [dbo].[notifications] ([id], [content], [created_at], [link], [read_status], [receiver], [title], [type]) VALUES (7, N'VT003 - Bơm tiêm 5ml, lô LO-VT003-01, HSD 24/08/2026, số lượng 320, mức Còn 61-90 ngày (còn 73 ngày).', '2026-06-05T01:36:04.3890070', N'/batches/3', 0, N'WAREHOUSE_STAFF', N'Cảnh báo hạn sử dụng', N'EXPIRY_EXPIRING_90');
INSERT INTO [dbo].[notifications] ([id], [content], [created_at], [link], [read_status], [receiver], [title], [type]) VALUES (8, N'CB001 - Bộ dây truyền dịch trẻ em: Tồn thấp, tồn thực tế 8, khả dụng 8, tối thiểu 25.', '2026-06-05T01:36:04.3890070', N'/alerts#stock-alerts', 0, N'WAREHOUSE_STAFF', N'Cảnh báo tồn kho', N'STOCK_LOW_CB001');
INSERT INTO [dbo].[notifications] ([id], [content], [created_at], [link], [read_status], [receiver], [title], [type]) VALUES (9, N'CB001 - Bộ dây truyền dịch trẻ em, lô LO-4, HSD 26/06/2026, số lượng 100, mức Còn 1-30 ngày (còn 14 ngày).', '2026-06-08T14:44:01.1902080', N'/batches/56', 0, N'WAREHOUSE_STAFF', N'Cảnh báo hạn sử dụng', N'EXPIRY_EXPIRING_30');
INSERT INTO [dbo].[notifications] ([id], [content], [created_at], [link], [read_status], [receiver], [title], [type]) VALUES (10, N'VT001 - Khẩu trang y tế 4 lớp, lô LO-VT001-01, HSD 15/07/2026, số lượng 180, mức Còn 31-60 ngày (còn 33 ngày).', '2026-06-08T14:44:01.2405060', N'/batches/1', 0, N'WAREHOUSE_STAFF', N'Cảnh báo hạn sử dụng', N'EXPIRY_EXPIRING_60');
INSERT INTO [dbo].[notifications] ([id], [content], [created_at], [link], [read_status], [receiver], [title], [type]) VALUES (11, N'VT002 - Găng tay y tế không bột size M, lô LO-VT002-01, HSD 04/08/2026, số lượng 75, mức Còn 31-60 ngày (còn 53 ngày).', '2026-06-08T14:44:01.2482540', N'/batches/2', 0, N'WAREHOUSE_STAFF', N'Cảnh báo hạn sử dụng', N'EXPIRY_EXPIRING_60');
INSERT INTO [dbo].[notifications] ([id], [content], [created_at], [link], [read_status], [receiver], [title], [type]) VALUES (12, N'VT003 - Bơm tiêm 5ml, lô LO-VT003-01, HSD 24/08/2026, số lượng 320, mức Còn 61-90 ngày (còn 73 ngày).', '2026-06-08T14:44:01.2536360', N'/batches/3', 0, N'WAREHOUSE_STAFF', N'Cảnh báo hạn sử dụng', N'EXPIRY_EXPIRING_90');
INSERT INTO [dbo].[notifications] ([id], [content], [created_at], [link], [read_status], [receiver], [title], [type]) VALUES (13, N'CB001 - Bộ dây truyền dịch trẻ em: Tồn thấp, tồn thực tế 8, khả dụng 8, tối thiểu 25.', '2026-06-08T14:44:01.2805840', N'/alerts#stock-alerts', 0, N'WAREHOUSE_STAFF', N'Cảnh báo tồn kho', N'STOCK_LOW_CB001');
INSERT INTO [dbo].[notifications] ([id], [content], [created_at], [link], [read_status], [receiver], [title], [type]) VALUES (14, N'CB001 - Bộ dây truyền dịch trẻ em, lô LO-4, HSD 26/06/2026, số lượng 100, mức Còn 1-30 ngày (còn 14 ngày).', '2026-06-12T01:14:46.4763910', N'/batches/56', 0, N'WAREHOUSE_STAFF', N'Cảnh báo hạn sử dụng', N'EXPIRY_EXPIRING_30');
INSERT INTO [dbo].[notifications] ([id], [content], [created_at], [link], [read_status], [receiver], [title], [type]) VALUES (15, N'VT001 - Khẩu trang y tế 4 lớp, lô LO-VT001-01, HSD 15/07/2026, số lượng 180, mức Còn 31-60 ngày (còn 33 ngày).', '2026-06-12T01:14:46.4833340', N'/batches/1', 0, N'WAREHOUSE_STAFF', N'Cảnh báo hạn sử dụng', N'EXPIRY_EXPIRING_60');
INSERT INTO [dbo].[notifications] ([id], [content], [created_at], [link], [read_status], [receiver], [title], [type]) VALUES (16, N'VT002 - Găng tay y tế không bột size M, lô LO-VT002-01, HSD 04/08/2026, số lượng 75, mức Còn 31-60 ngày (còn 53 ngày).', '2026-06-12T01:14:46.4866030', N'/batches/2', 0, N'WAREHOUSE_STAFF', N'Cảnh báo hạn sử dụng', N'EXPIRY_EXPIRING_60');
INSERT INTO [dbo].[notifications] ([id], [content], [created_at], [link], [read_status], [receiver], [title], [type]) VALUES (17, N'VT003 - Bơm tiêm 5ml, lô LO-VT003-01, HSD 24/08/2026, số lượng 320, mức Còn 61-90 ngày (còn 73 ngày).', '2026-06-12T01:14:46.4886030', N'/batches/3', 0, N'WAREHOUSE_STAFF', N'Cảnh báo hạn sử dụng', N'EXPIRY_EXPIRING_90');
SET IDENTITY_INSERT [dbo].[notifications] OFF;
GO
PRINT N'Seeding purchase_requests';
SET IDENTITY_INSERT [dbo].[purchase_requests] ON;
INSERT INTO [dbo].[purchase_requests] ([id], [approved_at], [approved_by], [created_at], [created_by], [reason], [request_code], [status], [updated_at]) VALUES (1, NULL, NULL, '2026-06-04T20:59:50.0948930', N'muasam', N'Bổ sung vật tư tồn thấp và dịch truyền dự phòng', N'MS-DEMO-001', N'SUBMITTED', '2026-06-05T00:59:50.0948930');
SET IDENTITY_INSERT [dbo].[purchase_requests] OFF;
GO
PRINT N'Seeding storage_conditions';
SET IDENTITY_INSERT [dbo].[storage_conditions] ON;
INSERT INTO [dbo].[storage_conditions] ([id], [material_id], [min_temperature], [max_temperature], [min_humidity], [max_humidity], [light_sensitive], [cold_chain_required], [note], [created_at], [updated_at]) VALUES (1, 2, 15.00, 30.00, 35.00, 70.00, 1, 0, N'Tránh ánh nắng trực tiếp, không đặt sát nền kho.', '2026-06-12T12:19:19.7482043', '2026-06-12T12:19:19.7482043');
INSERT INTO [dbo].[storage_conditions] ([id], [material_id], [min_temperature], [max_temperature], [min_humidity], [max_humidity], [light_sensitive], [cold_chain_required], [note], [created_at], [updated_at]) VALUES (2, 6, 2.00, 8.00, 35.00, 65.00, 0, 1, N'Theo dõi nhiệt độ kho lạnh mỗi ca trực.', '2026-06-12T12:19:19.7482043', '2026-06-12T12:19:19.7482043');
SET IDENTITY_INSERT [dbo].[storage_conditions] OFF;
GO
PRINT N'Seeding temperature_logs';
SET IDENTITY_INSERT [dbo].[temperature_logs] ON;
INSERT INTO [dbo].[temperature_logs] ([id], [warehouse_id], [recorded_at], [temperature], [humidity], [recorded_by], [status], [note]) VALUES (1, 1, '2026-06-12T06:19:19.7643565', 24.50, 58.00, N'thukho', N'NORMAL', N'Nhiệt độ ổn định trong ca sáng');
INSERT INTO [dbo].[temperature_logs] ([id], [warehouse_id], [recorded_at], [temperature], [humidity], [recorded_by], [status], [note]) VALUES (2, 2, '2026-06-12T06:19:19.7643565', 27.20, 68.00, N'thukho', N'WARNING', N'Độ ẩm tăng, cần kiểm tra điều hòa kho');
INSERT INTO [dbo].[temperature_logs] ([id], [warehouse_id], [recorded_at], [temperature], [humidity], [recorded_by], [status], [note]) VALUES (3, 1, '2026-06-12T11:19:19.7643565', 31.00, 76.00, N'thukho', N'RISK', N'Vượt ngưỡng, cần kiểm tra và cân nhắc cách ly lô nhạy cảm');
SET IDENTITY_INSERT [dbo].[temperature_logs] OFF;
GO
PRINT N'Seeding chat_messages';
SET IDENTITY_INSERT [dbo].[chat_messages] ON;
INSERT INTO [dbo].[chat_messages] ([id], [created_at], [intent], [message], [response], [sender_type], [session_id]) VALUES (1, '2026-05-31T22:38:39.9041490', N'SEARCH_MATERIAL', N'Găng tay ở kho nào', N'Không tìm thấy vật tư phù hợp trong database.', N'USER', 1);
INSERT INTO [dbo].[chat_messages] ([id], [created_at], [intent], [message], [response], [sender_type], [session_id]) VALUES (2, '2026-05-31T22:38:39.9041490', N'SEARCH_MATERIAL', N'Không tìm thấy vật tư phù hợp trong database.', N'Không tìm thấy vật tư phù hợp trong database.', N'BOT', 1);
INSERT INTO [dbo].[chat_messages] ([id], [created_at], [intent], [message], [response], [sender_type], [session_id]) VALUES (3, '2026-06-01T14:05:16.2760490', N'CHECK_BATCH', N'có mấy lô sắp hết hạn sử dụng vậy', N'Không tìm thấy lô phù hợp trong database.', N'USER', 1);
INSERT INTO [dbo].[chat_messages] ([id], [created_at], [intent], [message], [response], [sender_type], [session_id]) VALUES (4, '2026-06-01T14:05:16.4259530', N'CHECK_BATCH', N'Không tìm thấy lô phù hợp trong database.', N'Không tìm thấy lô phù hợp trong database.', N'BOT', 1);
INSERT INTO [dbo].[chat_messages] ([id], [created_at], [intent], [message], [response], [sender_type], [session_id]) VALUES (5, '2026-06-08T18:19:21.4919100', N'HELP', N'xin chào', N'Chào bạn, mình đang sẵn sàng tra dữ liệu QLVT.
Bạn cứ hỏi tự nhiên, ví dụ: "VT001 còn bao nhiêu?", "vật tư này ở kho nào?", hoặc "các lô sắp hết hạn".
', N'USER', 1);
INSERT INTO [dbo].[chat_messages] ([id], [created_at], [intent], [message], [response], [sender_type], [session_id]) VALUES (6, '2026-06-08T18:19:21.5291740', N'HELP', N'Chào bạn, mình đang sẵn sàng tra dữ liệu QLVT.
Bạn cứ hỏi tự nhiên, ví dụ: "VT001 còn bao nhiêu?", "vật tư này ở kho nào?", hoặc "các lô sắp hết hạn".
', N'Chào bạn, mình đang sẵn sàng tra dữ liệu QLVT.
Bạn cứ hỏi tự nhiên, ví dụ: "VT001 còn bao nhiêu?", "vật tư này ở kho nào?", hoặc "các lô sắp hết hạn".
', N'BOT', 1);
INSERT INTO [dbo].[chat_messages] ([id], [created_at], [intent], [message], [response], [sender_type], [session_id]) VALUES (7, '2026-06-08T18:19:21.6439320', N'HELP', N'VT001 còn bao nhiêu', N'Chào bạn, mình đang sẵn sàng tra dữ liệu QLVT.
Bạn cứ hỏi tự nhiên, ví dụ: "VT001 còn bao nhiêu?", "vật tư này ở kho nào?", hoặc "các lô sắp hết hạn".
', N'USER', 1);
INSERT INTO [dbo].[chat_messages] ([id], [created_at], [intent], [message], [response], [sender_type], [session_id]) VALUES (8, '2026-06-08T18:19:21.6469770', N'HELP', N'Chào bạn, mình đang sẵn sàng tra dữ liệu QLVT.
Bạn cứ hỏi tự nhiên, ví dụ: "VT001 còn bao nhiêu?", "vật tư này ở kho nào?", hoặc "các lô sắp hết hạn".
', N'Chào bạn, mình đang sẵn sàng tra dữ liệu QLVT.
Bạn cứ hỏi tự nhiên, ví dụ: "VT001 còn bao nhiêu?", "vật tư này ở kho nào?", hoặc "các lô sắp hết hạn".
', N'BOT', 1);
INSERT INTO [dbo].[chat_messages] ([id], [created_at], [intent], [message], [response], [sender_type], [session_id]) VALUES (9, '2026-06-08T18:20:57.1913950', N'HELP', N'xin chào', N'Chào bạn, mình đang sẵn sàng tra dữ liệu QLVT.
Bạn cứ hỏi tự nhiên, ví dụ: "VT001 còn bao nhiêu?", "vật tư này ở kho nào?", hoặc "các lô sắp hết hạn".
', N'USER', 1);
INSERT INTO [dbo].[chat_messages] ([id], [created_at], [intent], [message], [response], [sender_type], [session_id]) VALUES (10, '2026-06-08T18:20:57.2099020', N'HELP', N'Chào bạn, mình đang sẵn sàng tra dữ liệu QLVT.
Bạn cứ hỏi tự nhiên, ví dụ: "VT001 còn bao nhiêu?", "vật tư này ở kho nào?", hoặc "các lô sắp hết hạn".
', N'Chào bạn, mình đang sẵn sàng tra dữ liệu QLVT.
Bạn cứ hỏi tự nhiên, ví dụ: "VT001 còn bao nhiêu?", "vật tư này ở kho nào?", hoặc "các lô sắp hết hạn".
', N'BOT', 1);
INSERT INTO [dbo].[chat_messages] ([id], [created_at], [intent], [message], [response], [sender_type], [session_id]) VALUES (11, '2026-06-08T18:20:57.2972580', N'CHECK_STOCK', N'VT001 còn bao nhiêu', N'Mình kiểm tra được như sau:
VT001 - Khẩu trang y tế 4 lớp
- Tổng còn có thể cấp: 240 Hộp

Theo từng kho/vị trí:
- Kho vật tư trung tâm / Kệ A1 / lô LO-VT001-01: còn 180 Hộp (thực tế 180, đang giữ 0)
- Kho vật tư trung tâm / Kệ A1 / lô DEMO-LO-VT001-01: còn 220 Hộp (thực tế 220, đang giữ 0)', N'USER', 1);
INSERT INTO [dbo].[chat_messages] ([id], [created_at], [intent], [message], [response], [sender_type], [session_id]) VALUES (12, '2026-06-08T18:20:57.2982850', N'CHECK_STOCK', N'Mình kiểm tra được như sau:
VT001 - Khẩu trang y tế 4 lớp
- Tổng còn có thể cấp: 240 Hộp

Theo từng kho/vị trí:
- Kho vật tư trung tâm / Kệ A1 / lô LO-VT001-01: còn 180 Hộp (thực tế 180, đang giữ 0)
- Kho vật tư trung tâm / Kệ A1 / lô DEMO-LO-VT001-01: còn 220 Hộp (thực tế 220, đang giữ 0)', N'Mình kiểm tra được như sau:
VT001 - Khẩu trang y tế 4 lớp
- Tổng còn có thể cấp: 240 Hộp

Theo từng kho/vị trí:
- Kho vật tư trung tâm / Kệ A1 / lô LO-VT001-01: còn 180 Hộp (thực tế 180, đang giữ 0)
- Kho vật tư trung tâm / Kệ A1 / lô DEMO-LO-VT001-01: còn 220 Hộp (thực tế 220, đang giữ 0)', N'BOT', 1);
SET IDENTITY_INSERT [dbo].[chat_messages] OFF;
GO
PRINT N'Seeding request_approval_logs';
SET IDENTITY_INSERT [dbo].[request_approval_logs] ON;
INSERT INTO [dbo].[request_approval_logs] ([id], [action], [actor], [created_at], [note], [material_request_id]) VALUES (1, N'DEPARTMENT_APPROVED', N'admin', '2026-06-05T06:15:44.1655710', N'Trưởng khoa duyệt yêu cầu', 1);
INSERT INTO [dbo].[request_approval_logs] ([id], [action], [actor], [created_at], [note], [material_request_id]) VALUES (3, N'DEPARTMENT_APPROVED', N'admin', '2026-06-05T10:41:58.7734760', N'Trưởng khoa duyệt yêu cầu', 4);
SET IDENTITY_INSERT [dbo].[request_approval_logs] OFF;
GO
PRINT N'Seeding material_request_lines';
SET IDENTITY_INSERT [dbo].[material_request_lines] ON;
INSERT INTO [dbo].[material_request_lines] ([id], [approved_quantity], [reason], [requested_quantity], [material_id], [request_id], [issued_quantity], [note], [status]) VALUES (1, 0, N'', 10, 8, 1, 0, NULL, NULL);
INSERT INTO [dbo].[material_request_lines] ([id], [approved_quantity], [reason], [requested_quantity], [material_id], [request_id], [issued_quantity], [note], [status]) VALUES (2, 0, N'Bổ sung từ trực cấp cứu', 12, 2, 2, 0, N'Dữ liệu mẫu', N'PENDING');
INSERT INTO [dbo].[material_request_lines] ([id], [approved_quantity], [reason], [requested_quantity], [material_id], [request_id], [issued_quantity], [note], [status]) VALUES (3, 0, N'Phát cho khu tiếp nhận', 8, 1, 2, 0, N'Dữ liệu mẫu', N'PENDING');
INSERT INTO [dbo].[material_request_lines] ([id], [approved_quantity], [reason], [requested_quantity], [material_id], [request_id], [issued_quantity], [note], [status]) VALUES (4, 20, N'Bổ sung xe tiêm truyền', 20, 6, 3, 0, N'Dữ liệu mẫu', N'APPROVED');
INSERT INTO [dbo].[material_request_lines] ([id], [approved_quantity], [reason], [requested_quantity], [material_id], [request_id], [issued_quantity], [note], [status]) VALUES (5, 0, N'', 10, 51, 4, 0, NULL, NULL);
INSERT INTO [dbo].[material_request_lines] ([id], [approved_quantity], [reason], [requested_quantity], [material_id], [request_id], [issued_quantity], [note], [status]) VALUES (6, 0, N'', 5, 2, 5, 0, NULL, NULL);
SET IDENTITY_INSERT [dbo].[material_request_lines] OFF;
GO
PRINT N'Seeding purchase_request_lines';
SET IDENTITY_INSERT [dbo].[purchase_request_lines] ON;
INSERT INTO [dbo].[purchase_request_lines] ([id], [note], [requested_quantity], [suggested_quantity], [material_id], [purchase_request_id]) VALUES (1, N'Đề xuất mua do tồn dưới mức tối thiểu', 80, 100, 51, 1);
INSERT INTO [dbo].[purchase_request_lines] ([id], [note], [requested_quantity], [suggested_quantity], [material_id], [purchase_request_id]) VALUES (2, N'Bổ sung tồn kho an toàn', 60, 80, 6, 1);
SET IDENTITY_INSERT [dbo].[purchase_request_lines] OFF;
GO
PRINT N'Seeding department_returns';
SET IDENTITY_INSERT [dbo].[department_returns] ON;
INSERT INTO [dbo].[department_returns] ([id], [created_at], [created_by], [department], [reason], [received_at], [received_by], [return_code], [status], [updated_at], [warehouse_id]) VALUES (1, '2026-06-04T22:26:50.7916420', N'nhanvien', N'Khoa Cấp cứu', N'Trả vật tư dư sau ca trực', NULL, NULL, N'TK-DEMO-001', N'SUBMITTED', '2026-06-04T22:26:50.7924990', 1);
SET IDENTITY_INSERT [dbo].[department_returns] OFF;
GO
PRINT N'Seeding inventory_counts';
SET IDENTITY_INSERT [dbo].[inventory_counts] ON;
INSERT INTO [dbo].[inventory_counts] ([id], [completed_at], [completed_by], [count_code], [created_at], [note], [started_at], [started_by], [status], [updated_at], [warehouse_id]) VALUES (1, '2026-06-04T05:26:50.8140590', N'thukho', N'KK-DEMO-001', '2026-06-04T23:26:50.8140590', N'Kiểm kê mẫu cuối ngày', '2026-06-03T23:26:50.8140590', N'thukho', N'COMPLETED', '2026-06-04T23:26:50.8140590', 1);
SET IDENTITY_INSERT [dbo].[inventory_counts] OFF;
GO
PRINT N'Seeding issue_slips';
SET IDENTITY_INSERT [dbo].[issue_slips] ON;
INSERT INTO [dbo].[issue_slips] ([id], [created_at], [created_by], [department], [issue_code], [issued_at], [issued_by], [note], [received_at], [received_by], [status], [updated_at], [material_request_id], [warehouse_id]) VALUES (1, '2026-06-04T23:26:50.7279780', N'thukho', N'Khoa Cấp cứu', N'PX-DEMO-001', '2026-06-04T21:26:50.7279780', N'thukho', N'Phiếu xuất mẫu theo yêu cầu YC-DEMO-001', NULL, NULL, N'ISSUED', '2026-06-04T23:26:50.7279780', 2, 1);
SET IDENTITY_INSERT [dbo].[issue_slips] OFF;
GO
PRINT N'Seeding receipts';
SET IDENTITY_INSERT [dbo].[receipts] ON;
INSERT INTO [dbo].[receipts] ([id], [confirmed_at], [confirmed_by], [created_at], [created_by], [note], [receipt_code], [receipt_date], [status], [updated_at], [supplier_id], [warehouse_id]) VALUES (1, '2026-06-03T00:26:50.6682070', N'thukho', '2026-06-04T23:26:50.6682070', N'thukho', N'Phiếu nhập mẫu từ nhà cung cấp', N'PN-DEMO-001', '2026-06-02T00:00:00.0000000', N'CONFIRMED', '2026-06-04T23:26:50.6682070', 1, 1);
INSERT INTO [dbo].[receipts] ([id], [confirmed_at], [confirmed_by], [created_at], [created_by], [note], [receipt_code], [receipt_date], [status], [updated_at], [supplier_id], [warehouse_id]) VALUES (2, NULL, NULL, '2026-06-05T01:43:22.8706160', N'admin', N'', N'PN-20260605014322', '2026-06-05T00:00:00.0000000', N'DRAFT', '2026-06-05T01:43:22.8706160', 1, 1);
INSERT INTO [dbo].[receipts] ([id], [confirmed_at], [confirmed_by], [created_at], [created_by], [note], [receipt_code], [receipt_date], [status], [updated_at], [supplier_id], [warehouse_id]) VALUES (3, NULL, NULL, '2026-06-05T06:00:57.0668380', N'admin', N'Test', N'PN-1', '2026-06-05T00:00:00.0000000', N'DRAFT', '2026-06-05T06:00:57.0668380', NULL, 1);
INSERT INTO [dbo].[receipts] ([id], [confirmed_at], [confirmed_by], [created_at], [created_by], [note], [receipt_code], [receipt_date], [status], [updated_at], [supplier_id], [warehouse_id]) VALUES (4, NULL, NULL, '2026-06-05T06:01:29.6815680', N'admin', N'Test', N'PN-2', '2026-06-05T00:00:00.0000000', N'DRAFT', '2026-06-05T06:01:29.6815680', NULL, 1);
INSERT INTO [dbo].[receipts] ([id], [confirmed_at], [confirmed_by], [created_at], [created_by], [note], [receipt_code], [receipt_date], [status], [updated_at], [supplier_id], [warehouse_id]) VALUES (12, '2026-06-05T10:37:09.6616380', N'admin', '2026-06-05T10:36:50.7283100', N'admin', N'', N'PN-4', '2026-06-05T00:00:00.0000000', N'CONFIRMED', '2026-06-05T10:37:09.6616380', NULL, 1);
SET IDENTITY_INSERT [dbo].[receipts] OFF;
GO
PRINT N'Seeding stock_transfers';
SET IDENTITY_INSERT [dbo].[stock_transfers] ON;
INSERT INTO [dbo].[stock_transfers] ([id], [approved_at], [approved_by], [created_at], [created_by], [note], [reason], [received_at], [received_by], [status], [transfer_code], [transferred_at], [transferred_by], [updated_at], [from_warehouse_id], [to_warehouse_id]) VALUES (1, NULL, NULL, '2026-06-04T16:59:50.0401720', N'thukho', NULL, N'Bổ sung vật tư cho kho cấp cứu', NULL, NULL, N'SUBMITTED', N'CK-DEMO-001', NULL, NULL, '2026-06-05T00:59:50.0401720', 1, 2);
SET IDENTITY_INSERT [dbo].[stock_transfers] OFF;
GO
PRINT N'Seeding stock_adjustments';
SET IDENTITY_INSERT [dbo].[stock_adjustments] ON;
INSERT INTO [dbo].[stock_adjustments] ([id], [adjustment_code], [approved_at], [approved_by], [created_at], [created_by], [reason], [rejected_at], [rejected_by], [rejected_reason], [status], [updated_at], [inventory_count_id], [warehouse_id], [accountant_approved_at], [accountant_approved_by], [manager_approved_at], [manager_approved_by]) VALUES (1, N'DC-DEMO-001', NULL, NULL, '2026-06-04T00:59:50.0161750', N'thukho', N'Điều chỉnh chênh lệch sau kiểm kê mẫu', NULL, NULL, NULL, N'SUBMITTED', '2026-06-05T00:59:50.0161750', NULL, 1, NULL, NULL, NULL, NULL);
SET IDENTITY_INSERT [dbo].[stock_adjustments] OFF;
GO
PRINT N'Seeding issue_slip_lines';
SET IDENTITY_INSERT [dbo].[issue_slip_lines] ON;
INSERT INTO [dbo].[issue_slip_lines] ([id], [approved_quantity], [issued_quantity], [note], [requested_quantity], [issue_slip_id], [material_id]) VALUES (1, 10, 10, N'Xuất theo FEFO từ lô demo', 10, 1, 2);
INSERT INTO [dbo].[issue_slip_lines] ([id], [approved_quantity], [issued_quantity], [note], [requested_quantity], [issue_slip_id], [material_id]) VALUES (2, 6, 6, N'Xuất theo FEFO từ lô demo', 6, 1, 1);
SET IDENTITY_INSERT [dbo].[issue_slip_lines] OFF;
GO
PRINT N'Seeding material_price_histories';
SET IDENTITY_INSERT [dbo].[material_price_histories] ON;
INSERT INTO [dbo].[material_price_histories] ([id], [created_at], [created_by], [note], [quantity], [received_date], [total_amount], [unit_price], [material_id], [purchase_order_id], [receipt_id], [supplier_id]) VALUES (1, '2026-06-05T00:59:50.1120130', N'thukho', N'Dữ liệu mẫu lịch sử giá nhập', 60, '2026-06-03T00:00:00.0000000', 3360000.00, 56000.00, 2, NULL, 1, 1);
INSERT INTO [dbo].[material_price_histories] ([id], [created_at], [created_by], [note], [quantity], [received_date], [total_amount], [unit_price], [material_id], [purchase_order_id], [receipt_id], [supplier_id]) VALUES (2, '2026-06-05T10:37:09.6274470', N'admin', N'', 100, '2026-06-05T00:00:00.0000000', 30000000.00, 300000.00, 51, NULL, 12, NULL);
SET IDENTITY_INSERT [dbo].[material_price_histories] OFF;
GO
PRINT N'Seeding price_alerts';
SET IDENTITY_INSERT [dbo].[price_alerts] ON;
INSERT INTO [dbo].[price_alerts] ([id], [alert_level], [created_at], [difference_percent], [message], [new_price], [old_price], [resolved], [resolved_at], [material_id], [receipt_id], [supplier_id]) VALUES (1, N'WARNING', '2026-06-05T00:59:50.1292940', 33.33, N'Giá nhập tăng 33,33% so với lần nhập trước', 56000.00, 42000.00, 0, NULL, 2, 1, 1);
SET IDENTITY_INSERT [dbo].[price_alerts] OFF;
GO
PRINT N'Seeding material_batches';
SET IDENTITY_INSERT [dbo].[material_batches] ON;
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (1, N'LO-VT001-01', '2026-07-15T00:00:00.0000000', '2026-04-01T00:00:00.0000000', 180, '2026-05-21T00:00:00.0000000', N'AVAILABLE', 1, 1, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (2, N'LO-VT002-01', '2026-08-04T00:00:00.0000000', '2026-03-31T00:00:00.0000000', 75, '2026-05-20T00:00:00.0000000', N'AVAILABLE', 1, 2, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (3, N'LO-VT003-01', '2026-08-24T00:00:00.0000000', '2026-03-30T00:00:00.0000000', 320, '2026-05-19T00:00:00.0000000', N'AVAILABLE', 1, 3, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (4, N'LO-VT004-01', '2026-09-13T00:00:00.0000000', '2026-03-29T00:00:00.0000000', 60, '2026-05-18T00:00:00.0000000', N'AVAILABLE', 1, 4, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (5, N'LO-VT005-01', '2026-10-03T00:00:00.0000000', '2026-03-28T00:00:00.0000000', 130, '2026-05-17T00:00:00.0000000', N'AVAILABLE', 1, 5, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (6, N'LO-VT006-01', '2026-10-23T00:00:00.0000000', '2026-03-27T00:00:00.0000000', 90, '2026-05-16T00:00:00.0000000', N'AVAILABLE', 1, 6, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (7, N'LO-VT007-01', '2026-11-12T00:00:00.0000000', '2026-03-26T00:00:00.0000000', 58, '2026-05-15T00:00:00.0000000', N'AVAILABLE', 1, 7, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (8, N'LO-VT008-01', '2026-12-02T00:00:00.0000000', '2026-03-25T00:00:00.0000000', 68, '2026-05-14T00:00:00.0000000', N'AVAILABLE', 1, 8, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (9, N'LO-VT009-01', '2026-12-22T00:00:00.0000000', '2026-03-24T00:00:00.0000000', 150, '2026-05-13T00:00:00.0000000', N'AVAILABLE', 1, 9, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (10, N'LO-VT010-01', '2027-01-11T00:00:00.0000000', '2026-03-23T00:00:00.0000000', 95, '2026-05-12T00:00:00.0000000', N'AVAILABLE', 1, 10, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (11, N'LO-VT011-01', '2027-01-31T00:00:00.0000000', '2026-03-22T00:00:00.0000000', 140, '2026-05-11T00:00:00.0000000', N'AVAILABLE', 1, 11, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (12, N'LO-VT012-01', '2027-02-20T00:00:00.0000000', '2026-03-21T00:00:00.0000000', 500, '2026-05-10T00:00:00.0000000', N'AVAILABLE', 1, 12, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (13, N'LO-VT013-01', '2027-03-12T00:00:00.0000000', '2026-03-20T00:00:00.0000000', 260, '2026-05-09T00:00:00.0000000', N'AVAILABLE', 1, 13, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (14, N'LO-VT014-01', '2027-04-01T00:00:00.0000000', '2026-03-19T00:00:00.0000000', 240, '2026-05-08T00:00:00.0000000', N'AVAILABLE', 1, 14, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (15, N'LO-VT015-01', '2027-04-21T00:00:00.0000000', '2026-03-18T00:00:00.0000000', 70, '2026-05-07T00:00:00.0000000', N'AVAILABLE', 1, 15, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (16, N'LO-VT016-01', '2027-05-11T00:00:00.0000000', '2026-03-17T00:00:00.0000000', 120, '2026-05-06T00:00:00.0000000', N'AVAILABLE', 1, 16, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (17, N'LO-VT017-01', '2027-05-31T00:00:00.0000000', '2026-03-16T00:00:00.0000000', 115, '2026-05-05T00:00:00.0000000', N'AVAILABLE', 1, 17, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (18, N'LO-VT018-01', '2027-06-20T00:00:00.0000000', '2026-03-15T00:00:00.0000000', 48, '2026-05-04T00:00:00.0000000', N'AVAILABLE', 1, 18, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (19, N'LO-VT019-01', '2027-07-10T00:00:00.0000000', '2026-03-14T00:00:00.0000000', 52, '2026-05-03T00:00:00.0000000', N'AVAILABLE', 1, 19, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (20, N'LO-VT020-01', '2027-07-30T00:00:00.0000000', '2026-03-13T00:00:00.0000000', 35, '2026-05-02T00:00:00.0000000', N'AVAILABLE', 1, 20, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (21, N'LO-VT021-01', '2027-08-19T00:00:00.0000000', '2026-03-12T00:00:00.0000000', 80, '2026-05-01T00:00:00.0000000', N'AVAILABLE', 1, 21, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (22, N'LO-VT022-01', '2027-09-08T00:00:00.0000000', '2026-03-11T00:00:00.0000000', 20, '2026-04-30T00:00:00.0000000', N'AVAILABLE', 1, 22, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (23, N'LO-VT023-01', '2027-09-28T00:00:00.0000000', '2026-03-10T00:00:00.0000000', 12, '2026-04-29T00:00:00.0000000', N'AVAILABLE', 1, 23, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (24, N'LO-VT024-01', '2027-10-18T00:00:00.0000000', '2026-03-09T00:00:00.0000000', 10, '2026-04-28T00:00:00.0000000', N'AVAILABLE', 1, 24, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (25, N'LO-VT025-01', '2027-11-07T00:00:00.0000000', '2026-03-08T00:00:00.0000000', 110, '2026-04-27T00:00:00.0000000', N'AVAILABLE', 1, 25, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (26, N'LO-VT026-01', '2027-11-27T00:00:00.0000000', '2026-03-07T00:00:00.0000000', 65, '2026-04-26T00:00:00.0000000', N'AVAILABLE', 1, 26, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (27, N'LO-VT027-01', '2027-12-17T00:00:00.0000000', '2026-03-06T00:00:00.0000000', 160, '2026-04-25T00:00:00.0000000', N'AVAILABLE', 1, 27, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (28, N'LO-VT028-01', '2028-01-06T00:00:00.0000000', '2026-03-05T00:00:00.0000000', 45, '2026-04-24T00:00:00.0000000', N'AVAILABLE', 1, 28, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (29, N'LO-VT029-01', '2028-01-26T00:00:00.0000000', '2026-03-04T00:00:00.0000000', 55, '2026-04-23T00:00:00.0000000', N'AVAILABLE', 1, 29, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (30, N'LO-VT030-01', '2028-02-15T00:00:00.0000000', '2026-03-03T00:00:00.0000000', 90, '2026-04-22T00:00:00.0000000', N'AVAILABLE', 1, 30, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (31, N'LO-VT031-01', '2028-03-06T00:00:00.0000000', '2026-03-02T00:00:00.0000000', 85, '2026-04-21T00:00:00.0000000', N'AVAILABLE', 1, 31, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (32, N'LO-VT032-01', '2028-03-26T00:00:00.0000000', '2026-03-01T00:00:00.0000000', 42, '2026-04-20T00:00:00.0000000', N'AVAILABLE', 1, 32, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (33, N'LO-VT033-01', '2028-04-15T00:00:00.0000000', '2026-02-28T00:00:00.0000000', 95, '2026-04-19T00:00:00.0000000', N'AVAILABLE', 1, 33, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (34, N'LO-VT034-01', '2028-05-05T00:00:00.0000000', '2026-02-27T00:00:00.0000000', 88, '2026-04-18T00:00:00.0000000', N'AVAILABLE', 1, 34, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (35, N'LO-VT035-01', '2028-05-25T00:00:00.0000000', '2026-02-26T00:00:00.0000000', 76, '2026-04-17T00:00:00.0000000', N'AVAILABLE', 1, 35, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (36, N'LO-VT036-01', '2028-06-14T00:00:00.0000000', '2026-02-25T00:00:00.0000000', 60, '2026-04-16T00:00:00.0000000', N'AVAILABLE', 1, 36, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (37, N'LO-VT037-01', '2028-07-04T00:00:00.0000000', '2026-02-24T00:00:00.0000000', 300, '2026-04-15T00:00:00.0000000', N'AVAILABLE', 1, 37, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (38, N'LO-VT038-01', '2028-07-24T00:00:00.0000000', '2026-02-23T00:00:00.0000000', 70, '2026-04-14T00:00:00.0000000', N'AVAILABLE', 1, 38, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (39, N'LO-VT039-01', '2028-08-13T00:00:00.0000000', '2026-02-22T00:00:00.0000000', 240, '2026-04-13T00:00:00.0000000', N'AVAILABLE', 1, 39, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (40, N'LO-VT040-01', '2028-09-02T00:00:00.0000000', '2026-02-21T00:00:00.0000000', 65, '2026-04-12T00:00:00.0000000', N'AVAILABLE', 1, 40, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (41, N'LO-VT041-01', '2028-09-22T00:00:00.0000000', '2026-02-20T00:00:00.0000000', 75, '2026-04-11T00:00:00.0000000', N'AVAILABLE', 1, 41, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (42, N'LO-VT042-01', '2028-10-12T00:00:00.0000000', '2026-02-19T00:00:00.0000000', 40, '2026-04-10T00:00:00.0000000', N'AVAILABLE', 1, 42, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (43, N'LO-VT043-01', '2028-11-01T00:00:00.0000000', '2026-02-18T00:00:00.0000000', 180, '2026-04-09T00:00:00.0000000', N'AVAILABLE', 1, 43, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (44, N'LO-VT044-01', '2028-11-21T00:00:00.0000000', '2026-02-17T00:00:00.0000000', 160, '2026-04-08T00:00:00.0000000', N'AVAILABLE', 1, 44, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (45, N'LO-VT045-01', '2028-12-11T00:00:00.0000000', '2026-02-16T00:00:00.0000000', 220, '2026-04-07T00:00:00.0000000', N'AVAILABLE', 1, 45, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (46, N'LO-VT046-01', '2028-12-31T00:00:00.0000000', '2026-02-15T00:00:00.0000000', 36, '2026-04-06T00:00:00.0000000', N'AVAILABLE', 1, 46, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (47, N'LO-VT047-01', '2029-01-20T00:00:00.0000000', '2026-02-14T00:00:00.0000000', 50, '2026-04-05T00:00:00.0000000', N'AVAILABLE', 1, 47, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (48, N'LO-VT048-01', '2029-02-09T00:00:00.0000000', '2026-02-13T00:00:00.0000000', 34, '2026-04-04T00:00:00.0000000', N'AVAILABLE', 1, 48, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (49, N'LO-VT049-01', '2029-03-01T00:00:00.0000000', '2026-02-12T00:00:00.0000000', 18, '2026-04-03T00:00:00.0000000', N'AVAILABLE', 1, 49, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (50, N'LO-VT050-01', '2029-03-21T00:00:00.0000000', '2026-02-11T00:00:00.0000000', 44, '2026-04-02T00:00:00.0000000', N'AVAILABLE', 1, 50, 1, 1, NULL, NULL, NULL, 0);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (51, N'DEMO-LO-VT002-01', '2027-04-12T00:00:00.0000000', '2026-04-12T00:00:00.0000000', 160, '2026-06-05T00:00:00.0000000', N'AVAILABLE', 1, 2, 1, 1, '2026-06-04T23:26:50.5234180', 160, '2026-06-04T23:26:50.5234180', 3);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (52, N'DEMO-LO-VT001-01', '2027-02-12T00:00:00.0000000', '2026-04-12T00:00:00.0000000', 220, '2026-06-05T00:00:00.0000000', N'AVAILABLE', 1, 1, 1, 1, '2026-06-04T23:26:50.5712580', 220, '2026-06-04T23:26:50.5712580', 3);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (53, N'DEMO-LO-VT006-01', '2026-12-12T00:00:00.0000000', '2026-04-12T00:00:00.0000000', 100, '2026-06-05T00:00:00.0000000', N'AVAILABLE', 1, 6, 1, 1, '2026-06-04T23:26:50.5892170', 100, '2026-06-04T23:26:50.5892170', 3);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (54, N'DEMO-LO-VT005-01', '2027-05-12T00:00:00.0000000', '2026-04-12T00:00:00.0000000', 140, '2026-06-05T00:00:00.0000000', N'AVAILABLE', 1, 5, 1, 1, '2026-06-04T23:26:50.5892170', 140, '2026-06-04T23:26:50.5892170', 3);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (55, N'DEMO-LO-CB001-01', '2027-03-12T00:00:00.0000000', '2026-04-12T00:00:00.0000000', 8, '2026-06-08T00:00:00.0000000', N'AVAILABLE', 1, 51, 1, 1, '2026-06-05T00:59:50.0061110', 8, '2026-06-05T00:59:50.0061110', 2);
INSERT INTO [dbo].[material_batches] ([id], [batch_number], [expiry_date], [manufacture_date], [quantity], [receipt_date], [status], [location_id], [material_id], [supplier_id], [warehouse_id], [created_at], [initial_quantity], [updated_at], [version]) VALUES (56, N'LO-4', '2026-06-26T00:00:00.0000000', '2026-06-05T00:00:00.0000000', 100, '2026-06-05T00:00:00.0000000', N'AVAILABLE', 1, 51, NULL, 1, '2026-06-05T10:37:09.5866110', 100, '2026-06-05T10:37:09.5866110', 0);
SET IDENTITY_INSERT [dbo].[material_batches] OFF;
GO
PRINT N'Seeding receipt_lines';
SET IDENTITY_INSERT [dbo].[receipt_lines] ON;
INSERT INTO [dbo].[receipt_lines] ([id], [batch_number], [expiry_date], [manufacturing_date], [note], [quantity], [unit_price], [location_id], [material_id], [receipt_id]) VALUES (1, N'DEMO-NHAP-VT002', '2027-06-04T00:00:00.0000000', '2026-04-04T00:00:00.0000000', N'Dòng nhập mẫu', 60, 42000.00, 1, 2, 1);
INSERT INTO [dbo].[receipt_lines] ([id], [batch_number], [expiry_date], [manufacturing_date], [note], [quantity], [unit_price], [location_id], [material_id], [receipt_id]) VALUES (2, N'DEMO-NHAP-VT001', '2027-06-04T00:00:00.0000000', '2026-04-04T00:00:00.0000000', N'Dòng nhập mẫu', 80, 65000.00, 1, 1, 1);
INSERT INTO [dbo].[receipt_lines] ([id], [batch_number], [expiry_date], [manufacturing_date], [note], [quantity], [unit_price], [location_id], [material_id], [receipt_id]) VALUES (3, N'DEMO-NHAP-VT006', '2027-06-04T00:00:00.0000000', '2026-04-04T00:00:00.0000000', N'Dòng nhập mẫu', 40, 18000.00, 1, 6, 1);
INSERT INTO [dbo].[receipt_lines] ([id], [batch_number], [expiry_date], [manufacturing_date], [note], [quantity], [unit_price], [location_id], [material_id], [receipt_id]) VALUES (4, N'001', '2027-11-05T00:00:00.0000000', '2026-06-05T00:00:00.0000000', N'', 200, 500.00, 1, 51, 2);
INSERT INTO [dbo].[receipt_lines] ([id], [batch_number], [expiry_date], [manufacturing_date], [note], [quantity], [unit_price], [location_id], [material_id], [receipt_id]) VALUES (5, N'TEST-ERR-1565620794', NULL, NULL, N'Test', 1, 0.00, 1, 51, 3);
INSERT INTO [dbo].[receipt_lines] ([id], [batch_number], [expiry_date], [manufacturing_date], [note], [quantity], [unit_price], [location_id], [material_id], [receipt_id]) VALUES (6, N'TEST-STATUS-1153977184', NULL, NULL, N'Test', 1, 0.00, 1, 51, 4);
INSERT INTO [dbo].[receipt_lines] ([id], [batch_number], [expiry_date], [manufacturing_date], [note], [quantity], [unit_price], [location_id], [material_id], [receipt_id]) VALUES (14, N'LO-4', '2026-06-26T00:00:00.0000000', '2026-06-05T00:00:00.0000000', N'', 100, 300000.00, 1, 51, 12);
SET IDENTITY_INSERT [dbo].[receipt_lines] OFF;
GO
PRINT N'Seeding department_stocks';
SET IDENTITY_INSERT [dbo].[department_stocks] ON;
INSERT INTO [dbo].[department_stocks] ([id], [department], [last_received_at], [quantity_damaged], [quantity_lost], [quantity_on_hand], [quantity_returned], [quantity_used], [updated_at], [version], [batch_id], [material_id]) VALUES (1, N'Khoa Cấp cứu', '2026-06-12T10:21:19.9612010', 0, 0, 10, 0, 3, '2026-06-12T12:21:19.9612010', 38, 51, 2);
INSERT INTO [dbo].[department_stocks] ([id], [department], [last_received_at], [quantity_damaged], [quantity_lost], [quantity_on_hand], [quantity_returned], [quantity_used], [updated_at], [version], [batch_id], [material_id]) VALUES (2, N'Khoa Cấp cứu', '2026-06-12T10:21:19.9768870', 0, 0, 6, 0, 2, '2026-06-12T12:21:19.9768870', 38, 52, 1);
INSERT INTO [dbo].[department_stocks] ([id], [department], [last_received_at], [quantity_damaged], [quantity_lost], [quantity_on_hand], [quantity_returned], [quantity_used], [updated_at], [version], [batch_id], [material_id]) VALUES (3, N'Khoa Nội tổng hợp', '2026-06-12T10:21:19.9768870', 0, 0, 18, 0, 5, '2026-06-12T12:21:19.9768870', 38, 53, 6);
SET IDENTITY_INSERT [dbo].[department_stocks] OFF;
GO
PRINT N'Seeding destruction_slip_lines';
SET IDENTITY_INSERT [dbo].[destruction_slip_lines] ON;
INSERT INTO [dbo].[destruction_slip_lines] ([id], [note], [quantity], [reason], [batch_id], [destruction_slip_id], [location_id], [material_id], [warehouse_id]) VALUES (1, N'Dữ liệu mẫu hủy vật tư', 2, N'DAMAGED', 55, 1, 1, 51, 1);
SET IDENTITY_INSERT [dbo].[destruction_slip_lines] OFF;
GO
PRINT N'Seeding inventory_count_lines';
SET IDENTITY_INSERT [dbo].[inventory_count_lines] ON;
INSERT INTO [dbo].[inventory_count_lines] ([id], [actual_quantity], [difference_quantity], [note], [system_quantity], [batch_id], [inventory_count_id], [location_id], [material_id]) VALUES (1, 158, -2, N'Lệch do đã cấp cho khoa', 160, 51, 1, 1, 2);
INSERT INTO [dbo].[inventory_count_lines] ([id], [actual_quantity], [difference_quantity], [note], [system_quantity], [batch_id], [inventory_count_id], [location_id], [material_id]) VALUES (2, 220, 0, N'Khớp hệ thống', 220, 52, 1, 1, 1);
INSERT INTO [dbo].[inventory_count_lines] ([id], [actual_quantity], [difference_quantity], [note], [system_quantity], [batch_id], [inventory_count_id], [location_id], [material_id]) VALUES (3, 101, 1, N'Thừa 1 chai sau đối chiếu', 100, 53, 1, 1, 6);
INSERT INTO [dbo].[inventory_count_lines] ([id], [actual_quantity], [difference_quantity], [note], [system_quantity], [batch_id], [inventory_count_id], [location_id], [material_id]) VALUES (4, 140, 0, N'Khớp hệ thống', 140, 54, 1, 1, 5);
SET IDENTITY_INSERT [dbo].[inventory_count_lines] OFF;
GO
PRINT N'Seeding issue_batch_allocations';
SET IDENTITY_INSERT [dbo].[issue_batch_allocations] ON;
INSERT INTO [dbo].[issue_batch_allocations] ([id], [quantity], [batch_id], [issue_slip_line_id], [location_id], [material_id], [warehouse_id]) VALUES (1, 10, 51, 1, 1, 2, 1);
INSERT INTO [dbo].[issue_batch_allocations] ([id], [quantity], [batch_id], [issue_slip_line_id], [location_id], [material_id], [warehouse_id]) VALUES (2, 6, 52, 2, 1, 1, 1);
SET IDENTITY_INSERT [dbo].[issue_batch_allocations] OFF;
GO
PRINT N'Seeding recall_orders';
SET IDENTITY_INSERT [dbo].[recall_orders] ON;
INSERT INTO [dbo].[recall_orders] ([id], [approved_by], [created_at], [created_by], [note], [reason], [recall_code], [status], [updated_at], [batch_id], [material_id]) VALUES (1, N'lanhdao', '2026-06-04T18:59:50.0666360', N'thukho', NULL, N'Theo dõi chất lượng lô sau phản ánh từ khoa', N'TH-DEMO-001', N'ACTIVE', '2026-06-05T00:59:50.0666360', 53, 6);
SET IDENTITY_INSERT [dbo].[recall_orders] OFF;
GO
PRINT N'Seeding stock_adjustment_lines';
SET IDENTITY_INSERT [dbo].[stock_adjustment_lines] ON;
INSERT INTO [dbo].[stock_adjustment_lines] ([id], [actual_quantity], [adjustment_quantity], [note], [system_quantity], [batch_id], [location_id], [material_id], [stock_adjustment_id]) VALUES (1, 158, -2, N'Dữ liệu mẫu điều chỉnh tồn', 160, 51, 1, 2, 1);
SET IDENTITY_INSERT [dbo].[stock_adjustment_lines] OFF;
GO
PRINT N'Seeding stock_balances';
SET IDENTITY_INSERT [dbo].[stock_balances] ON;
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (1, 180, '2026-05-31T19:39:32.1646270', 0, 0, '2026-05-31T19:39:32.1646270', 1, 1, 1, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (2, 75, '2026-05-31T19:39:32.1983400', 0, 0, '2026-05-31T19:39:32.1983400', 2, 1, 2, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (3, 320, '2026-05-31T19:39:32.1983400', 0, 0, '2026-05-31T19:39:32.1983400', 3, 1, 3, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (4, 60, '2026-05-31T19:39:32.1983400', 0, 0, '2026-05-31T19:39:32.1983400', 4, 1, 4, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (5, 130, '2026-05-31T19:39:32.2119490', 0, 0, '2026-05-31T19:39:32.2119490', 5, 1, 5, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (6, 90, '2026-05-31T19:39:32.2138950', 0, 0, '2026-05-31T19:39:32.2138950', 6, 1, 6, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (7, 58, '2026-05-31T19:39:32.2138950', 0, 0, '2026-05-31T19:39:32.2138950', 7, 1, 7, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (8, 68, '2026-05-31T19:39:32.2138950', 0, 0, '2026-05-31T19:39:32.2138950', 8, 1, 8, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (9, 150, '2026-05-31T19:39:32.2138950', 0, 0, '2026-05-31T19:39:32.2138950', 9, 1, 9, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (10, 95, '2026-05-31T19:39:32.2138950', 0, 0, '2026-05-31T19:39:32.2138950', 10, 1, 10, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (11, 140, '2026-05-31T19:39:32.2290300', 0, 0, '2026-05-31T19:39:32.2290300', 11, 1, 11, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (12, 500, '2026-05-31T19:39:32.2297630', 0, 0, '2026-05-31T19:39:32.2297630', 12, 1, 12, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (13, 260, '2026-05-31T19:39:32.2297630', 0, 0, '2026-05-31T19:39:32.2297630', 13, 1, 13, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (14, 240, '2026-05-31T19:39:32.2297630', 0, 0, '2026-05-31T19:39:32.2297630', 14, 1, 14, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (15, 70, '2026-05-31T19:39:32.2297630', 0, 0, '2026-05-31T19:39:32.2297630', 15, 1, 15, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (16, 120, '2026-05-31T19:39:32.2297630', 0, 0, '2026-05-31T19:39:32.2297630', 16, 1, 16, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (17, 115, '2026-05-31T19:39:32.2457400', 0, 0, '2026-05-31T19:39:32.2457400', 17, 1, 17, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (18, 48, '2026-05-31T19:39:32.2475150', 0, 0, '2026-05-31T19:39:32.2475150', 18, 1, 18, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (19, 52, '2026-05-31T19:39:32.2475150', 0, 0, '2026-05-31T19:39:32.2475150', 19, 1, 19, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (20, 35, '2026-05-31T19:39:32.2475150', 0, 0, '2026-05-31T19:39:32.2475150', 20, 1, 20, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (21, 80, '2026-05-31T19:39:32.2475150', 0, 0, '2026-05-31T19:39:32.2475150', 21, 1, 21, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (22, 20, '2026-05-31T19:39:32.2475150', 0, 0, '2026-05-31T19:39:32.2475150', 22, 1, 22, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (23, 12, '2026-05-31T19:39:32.2627130', 0, 0, '2026-05-31T19:39:32.2627130', 23, 1, 23, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (24, 10, '2026-05-31T19:39:32.2654190', 0, 0, '2026-05-31T19:39:32.2654190', 24, 1, 24, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (25, 110, '2026-05-31T19:39:32.2654190', 0, 0, '2026-05-31T19:39:32.2654190', 25, 1, 25, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (26, 65, '2026-05-31T19:39:32.2654190', 0, 0, '2026-05-31T19:39:32.2654190', 26, 1, 26, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (27, 160, '2026-05-31T19:39:32.2731900', 0, 0, '2026-05-31T19:39:32.2731900', 27, 1, 27, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (28, 45, '2026-05-31T19:39:32.2731900', 0, 0, '2026-05-31T19:39:32.2731900', 28, 1, 28, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (29, 55, '2026-05-31T19:39:32.2731900', 0, 0, '2026-05-31T19:39:32.2731900', 29, 1, 29, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (30, 90, '2026-05-31T19:39:32.2813570', 0, 0, '2026-05-31T19:39:32.2813570', 30, 1, 30, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (31, 85, '2026-05-31T19:39:32.2851350', 0, 0, '2026-05-31T19:39:32.2851350', 31, 1, 31, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (32, 42, '2026-05-31T19:39:32.2851350', 0, 0, '2026-05-31T19:39:32.2851350', 32, 1, 32, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (33, 95, '2026-05-31T19:39:32.2851350', 0, 0, '2026-05-31T19:39:32.2851350', 33, 1, 33, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (34, 88, '2026-05-31T19:39:32.2851350', 0, 0, '2026-05-31T19:39:32.2851350', 34, 1, 34, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (35, 76, '2026-05-31T19:39:32.2851350', 0, 0, '2026-05-31T19:39:32.2851350', 35, 1, 35, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (36, 60, '2026-05-31T19:39:32.2975430', 0, 0, '2026-05-31T19:39:32.2975430', 36, 1, 36, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (37, 300, '2026-05-31T19:39:32.3013170', 0, 0, '2026-05-31T19:39:32.3013170', 37, 1, 37, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (38, 70, '2026-05-31T19:39:32.3013170', 0, 0, '2026-05-31T19:39:32.3013170', 38, 1, 38, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (39, 240, '2026-05-31T19:39:32.3013170', 0, 0, '2026-05-31T19:39:32.3013170', 39, 1, 39, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (40, 65, '2026-05-31T19:39:32.3013170', 0, 0, '2026-05-31T19:39:32.3013170', 40, 1, 40, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (41, 75, '2026-05-31T19:39:32.3013170', 0, 0, '2026-05-31T19:39:32.3013170', 41, 1, 41, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (42, 40, '2026-05-31T19:39:32.3133460', 0, 0, '2026-05-31T19:39:32.3133460', 42, 1, 42, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (43, 180, '2026-05-31T19:39:32.3133460', 0, 0, '2026-05-31T19:39:32.3133460', 43, 1, 43, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (44, 160, '2026-05-31T19:39:32.3133460', 0, 0, '2026-05-31T19:39:32.3133460', 44, 1, 44, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (45, 220, '2026-05-31T19:39:32.3133460', 0, 0, '2026-05-31T19:39:32.3133460', 45, 1, 45, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (46, 36, '2026-05-31T19:39:32.3133460', 0, 0, '2026-05-31T19:39:32.3133460', 46, 1, 46, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (47, 50, '2026-05-31T19:39:32.3133460', 0, 0, '2026-05-31T19:39:32.3133460', 47, 1, 47, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (48, 34, '2026-05-31T19:39:32.3304810', 0, 0, '2026-05-31T19:39:32.3304810', 48, 1, 48, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (49, 18, '2026-05-31T19:39:32.3304810', 0, 0, '2026-05-31T19:39:32.3304810', 49, 1, 49, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (50, 44, '2026-05-31T19:39:32.3304810', 0, 0, '2026-05-31T19:39:32.3304810', 50, 1, 50, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (51, 160, '2026-06-04T23:26:51.2552460', 0, 0, '2026-06-04T23:26:51.2552460', 51, 1, 2, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (52, 220, '2026-06-04T23:26:51.2721110', 0, 0, '2026-06-04T23:26:51.2721110', 52, 1, 1, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (53, 100, '2026-06-04T23:26:51.2831350', 0, 0, '2026-06-04T23:26:51.2831350', 53, 1, 6, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (54, 140, '2026-06-04T23:26:51.2931960', 0, 0, '2026-06-04T23:26:51.2931960', 54, 1, 5, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (55, 8, '2026-06-05T01:12:21.7134750', 0, 0, '2026-06-05T01:12:21.7134750', 55, 1, 51, 1, 0);
INSERT INTO [dbo].[stock_balances] ([id], [actual_quantity], [created_at], [pending_issue_quantity], [reserved_quantity], [updated_at], [batch_id], [location_id], [material_id], [warehouse_id], [version]) VALUES (56, 100, '2026-06-05T10:37:09.6060060', 0, 0, '2026-06-05T10:37:09.6060060', 56, 1, 51, 1, 0);
SET IDENTITY_INSERT [dbo].[stock_balances] OFF;
GO
PRINT N'Seeding stock_movements';
SET IDENTITY_INSERT [dbo].[stock_movements] ON;
INSERT INTO [dbo].[stock_movements] ([id], [after_quantity], [before_quantity], [created_at], [created_by], [movement_type], [note], [quantity], [reference_code], [reference_type], [batch_id], [material_id], [warehouse_id], [location_id]) VALUES (1, 108, 8, '2026-06-05T10:37:09.6168940', N'admin', N'IN', NULL, 100, N'PN-4', N'RECEIPT', 56, 51, 1, 1);
SET IDENTITY_INSERT [dbo].[stock_movements] OFF;
GO
PRINT N'Seeding stock_transfer_lines';
SET IDENTITY_INSERT [dbo].[stock_transfer_lines] ON;
INSERT INTO [dbo].[stock_transfer_lines] ([id], [note], [quantity], [batch_id], [from_location_id], [material_id], [stock_transfer_id], [to_location_id]) VALUES (1, N'Dữ liệu mẫu chuyển kho nội bộ', 15, 52, 1, 1, 1, 3);
SET IDENTITY_INSERT [dbo].[stock_transfer_lines] OFF;
GO
PRINT N'Seeding department_return_lines';
SET IDENTITY_INSERT [dbo].[department_return_lines] ON;
INSERT INTO [dbo].[department_return_lines] ([id], [note], [quantity], [batch_id], [department_return_id], [department_stock_id], [location_id], [material_id]) VALUES (1, N'Dữ liệu mẫu trả khoa', 2, 51, 1, 1, 1, 2);
SET IDENTITY_INSERT [dbo].[department_return_lines] OFF;
GO
PRINT N'Seeding recall_order_lines';
SET IDENTITY_INSERT [dbo].[recall_order_lines] ON;
INSERT INTO [dbo].[recall_order_lines] ([id], [department], [issued_quantity], [note], [remaining_quantity], [returned_quantity], [status], [recall_order_id]) VALUES (1, N'Khoa Nội tổng hợp', 20, N'Dữ liệu mẫu thu hồi theo lô', 12, 3, N'Đang thu hồi', 1);
SET IDENTITY_INSERT [dbo].[recall_order_lines] OFF;
GO
-- Supplemental demo coverage to keep an empty rebuild useful for acceptance testing.
DECLARE @warehouseId bigint = (SELECT TOP 1 [id] FROM [dbo].[warehouses] ORDER BY [id]);
DECLARE @toWarehouseId bigint = (SELECT TOP 1 [id] FROM [dbo].[warehouses] WHERE [id] <> @warehouseId ORDER BY [id]);
DECLARE @materialId bigint = (SELECT TOP 1 [id] FROM [dbo].[materials] ORDER BY [id]);
DECLARE @batchId bigint = (SELECT TOP 1 [id] FROM [dbo].[material_batches] WHERE [material_id] = @materialId ORDER BY [id]);
DECLARE @locationId bigint = (SELECT TOP 1 [id] FROM [dbo].[storage_locations] WHERE [warehouse_id] = @warehouseId ORDER BY [id]);
DECLARE @toLocationId bigint = (SELECT TOP 1 [id] FROM [dbo].[storage_locations] WHERE [warehouse_id] = ISNULL(@toWarehouseId, @warehouseId) ORDER BY [id]);
IF @toWarehouseId IS NULL SET @toWarehouseId = @warehouseId;
IF @batchId IS NULL SELECT TOP 1 @batchId = [id], @materialId = [material_id] FROM [dbo].[material_batches] ORDER BY [id];
IF @locationId IS NULL SELECT TOP 1 @locationId = [id] FROM [dbo].[storage_locations] ORDER BY [id];
IF @toLocationId IS NULL SET @toLocationId = @locationId;

DECLARE @i int = 1;
WHILE (SELECT COUNT(*) FROM [dbo].[material_requests]) < 10
BEGIN
    DECLARE @requestCode varchar(40) = CONCAT('MR-AUTO-', FORMAT(@i, '000'));
    IF NOT EXISTS (SELECT 1 FROM [dbo].[material_requests] WHERE [code] = @requestCode)
    BEGIN
        INSERT INTO [dbo].[material_requests] ([code], [created_at], [department], [note], [priority], [requester], [status], [submitted_at], [updated_at])
        VALUES (@requestCode, DATEADD(day, -@i, SYSDATETIME()), N'Khoa Cấp cứu', N'Dữ liệu demo nghiệm thu giai đoạn 1', N'NORMAL', N'nhanvien', 'SUBMITTED', DATEADD(day, -@i, SYSDATETIME()), SYSDATETIME());
        INSERT INTO [dbo].[material_request_lines] ([approved_quantity], [reason], [requested_quantity], [material_id], [request_id], [issued_quantity], [note], [status])
        VALUES (0, 'ROUTINE', 5 + @i, @materialId, SCOPE_IDENTITY(), 0, N'Dòng yêu cầu demo', 'PENDING');
    END
    SET @i += 1;
END

SET @i = 1;
WHILE (SELECT COUNT(*) FROM [dbo].[stock_movements]) < 20
BEGIN
    INSERT INTO [dbo].[stock_movements] ([after_quantity], [before_quantity], [created_at], [created_by], [movement_type], [note], [quantity], [reference_code], [reference_type], [batch_id], [material_id], [warehouse_id], [location_id])
    VALUES (100 + @i, 100, DATEADD(hour, -@i, SYSDATETIME()), 'system', CASE WHEN @i % 2 = 0 THEN 'IN' ELSE 'OUT' END, 'Demo stock movement', @i, CONCAT('AUTO-MOV-', FORMAT(@i, '000')), 'DEMO', @batchId, @materialId, @warehouseId, @locationId);
    SET @i += 1;
END

SET @i = 1;
WHILE (SELECT COUNT(*) FROM [dbo].[inventory_counts]) < 5
BEGIN
    DECLARE @countCode varchar(40) = CONCAT('KK-AUTO-', FORMAT(@i, '000'));
    INSERT INTO [dbo].[inventory_counts] ([completed_at], [completed_by], [count_code], [created_at], [note], [started_at], [started_by], [status], [updated_at], [warehouse_id])
    VALUES (SYSDATETIME(), 'thukho', @countCode, DATEADD(day, -@i, SYSDATETIME()), N'Kiểm kê demo', DATEADD(day, -@i, SYSDATETIME()), 'thukho', 'COMPLETED', SYSDATETIME(), @warehouseId);
    INSERT INTO [dbo].[inventory_count_lines] ([actual_quantity], [difference_quantity], [note], [system_quantity], [batch_id], [inventory_count_id], [location_id], [material_id])
    VALUES (100 + @i, @i, N'Dòng kiểm kê demo', 100, @batchId, SCOPE_IDENTITY(), @locationId, @materialId);
    SET @i += 1;
END

SET @i = 1;
WHILE ((SELECT COUNT(*) FROM [dbo].[stock_transfers]) + (SELECT COUNT(*) FROM [dbo].[recall_orders]) + (SELECT COUNT(*) FROM [dbo].[destruction_slips])) < 5
BEGIN
    DECLARE @transferCode varchar(40) = CONCAT('CK-AUTO-', FORMAT(@i, '000'));
    INSERT INTO [dbo].[stock_transfers] ([created_at], [created_by], [note], [reason], [status], [transfer_code], [updated_at], [from_warehouse_id], [to_warehouse_id])
    VALUES (SYSDATETIME(), 'thukho', N'Chuyển kho demo', N'Cân bằng tồn kho', 'SUBMITTED', @transferCode, SYSDATETIME(), @warehouseId, @toWarehouseId);
    INSERT INTO [dbo].[stock_transfer_lines] ([note], [quantity], [batch_id], [from_location_id], [material_id], [stock_transfer_id], [to_location_id])
    VALUES (N'Dòng chuyển kho demo', 1 + @i, @batchId, @locationId, @materialId, SCOPE_IDENTITY(), @toLocationId);
    SET @i += 1;
END
GO
