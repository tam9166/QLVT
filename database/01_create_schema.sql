IF DB_ID(N'qlvt') IS NULL CREATE DATABASE [qlvt];
GO
USE [qlvt];
GO
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO
-- Restartable cleanup: drop foreign keys first, then tables.
DECLARE @sql nvarchar(max) = N'';
SELECT @sql += N'ALTER TABLE ' + QUOTENAME(SCHEMA_NAME(parent.schema_id)) + N'.' + QUOTENAME(parent.name) + N' DROP CONSTRAINT ' + QUOTENAME(fk.name) + N';' + CHAR(13) FROM sys.foreign_keys fk JOIN sys.tables parent ON parent.object_id = fk.parent_object_id WHERE parent.is_ms_shipped = 0;
EXEC sp_executesql @sql;
GO
DROP TABLE IF EXISTS [dbo].[warehouses];
DROP TABLE IF EXISTS [dbo].[users];
DROP TABLE IF EXISTS [dbo].[user_roles];
DROP TABLE IF EXISTS [dbo].[temperature_logs];
DROP TABLE IF EXISTS [dbo].[suppliers];
DROP TABLE IF EXISTS [dbo].[storage_locations];
DROP TABLE IF EXISTS [dbo].[storage_conditions];
DROP TABLE IF EXISTS [dbo].[stock_transfers];
DROP TABLE IF EXISTS [dbo].[stock_transfer_lines];
DROP TABLE IF EXISTS [dbo].[stock_reservations];
DROP TABLE IF EXISTS [dbo].[stock_movements];
DROP TABLE IF EXISTS [dbo].[stock_balances];
DROP TABLE IF EXISTS [dbo].[stock_adjustments];
DROP TABLE IF EXISTS [dbo].[stock_adjustment_lines];
DROP TABLE IF EXISTS [dbo].[roles];
DROP TABLE IF EXISTS [dbo].[request_approval_logs];
DROP TABLE IF EXISTS [dbo].[receipts];
DROP TABLE IF EXISTS [dbo].[receipt_lines];
DROP TABLE IF EXISTS [dbo].[recall_orders];
DROP TABLE IF EXISTS [dbo].[recall_order_lines];
DROP TABLE IF EXISTS [dbo].[recall_department_responses];
DROP TABLE IF EXISTS [dbo].[purchase_requests];
DROP TABLE IF EXISTS [dbo].[purchase_request_lines];
DROP TABLE IF EXISTS [dbo].[purchase_orders];
DROP TABLE IF EXISTS [dbo].[purchase_order_lines];
DROP TABLE IF EXISTS [dbo].[price_alerts];
DROP TABLE IF EXISTS [dbo].[notifications];
DROP TABLE IF EXISTS [dbo].[materials];
DROP TABLE IF EXISTS [dbo].[material_requests];
DROP TABLE IF EXISTS [dbo].[material_request_lines];
DROP TABLE IF EXISTS [dbo].[material_price_histories];
DROP TABLE IF EXISTS [dbo].[material_batches];
DROP TABLE IF EXISTS [dbo].[issue_slips];
DROP TABLE IF EXISTS [dbo].[issue_slip_lines];
DROP TABLE IF EXISTS [dbo].[issue_batch_allocations];
DROP TABLE IF EXISTS [dbo].[inventory_counts];
DROP TABLE IF EXISTS [dbo].[inventory_count_lines];
DROP TABLE IF EXISTS [dbo].[destruction_slips];
DROP TABLE IF EXISTS [dbo].[destruction_slip_lines];
DROP TABLE IF EXISTS [dbo].[departments];
DROP TABLE IF EXISTS [dbo].[department_stocks];
DROP TABLE IF EXISTS [dbo].[department_stock_movements];
DROP TABLE IF EXISTS [dbo].[department_returns];
DROP TABLE IF EXISTS [dbo].[department_return_lines];
DROP TABLE IF EXISTS [dbo].[chat_sessions];
DROP TABLE IF EXISTS [dbo].[chat_messages];
DROP TABLE IF EXISTS [dbo].[audit_logs];
DROP TABLE IF EXISTS [dbo].[attachments];
GO
CREATE TABLE [dbo].[attachments] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [content_type] VARCHAR(120) NOT NULL,
    [deleted] BIT NOT NULL,
    [file_size] BIGINT NOT NULL,
    [note] NVARCHAR(1000) NULL,
    [original_file_name] NVARCHAR(260) NULL,
    [reference_id] BIGINT NOT NULL,
    [reference_type] VARCHAR(40) NOT NULL,
    [storage_path] NVARCHAR(1000) NULL,
    [stored_file_name] VARCHAR(120) NOT NULL,
    [uploaded_at] DATETIME2(6) NULL,
    [uploaded_by] VARCHAR(255) NULL,
    CONSTRAINT [PK__attachme__3213E83F3BDC63E7] PRIMARY KEY ([id]),
    CONSTRAINT [CK__attachmen__refer__41B8C09B] CHECK ([reference_type]='MATERIAL' OR [reference_type]='PURCHASE_ORDER' OR [reference_type]='SUPPLIER' OR [reference_type]='DEPARTMENT_RETURN' OR [reference_type]='RECALL_ORDER' OR [reference_type]='DESTRUCTION_SLIP' OR [reference_type]='STOCK_ADJUSTMENT' OR [reference_type]='MATERIAL_REQUEST' OR [reference_type]='ISSUE_SLIP' OR [reference_type]='RECEIPT')
);
GO
CREATE TABLE [dbo].[audit_logs] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [action] VARCHAR(255) NULL,
    [created_at] DATETIME2(6) NULL,
    [detail] NVARCHAR(1500) NULL,
    [target_code] VARCHAR(255) NULL,
    [target_type] VARCHAR(255) NULL,
    [username] VARCHAR(255) NULL,
    [actor_username] VARCHAR(255) NULL,
    [entity_id] VARCHAR(255) NULL,
    [entity_name] VARCHAR(255) NULL,
    [ip_address] VARCHAR(255) NULL,
    [new_value] NVARCHAR(1500) NULL,
    [old_value] NVARCHAR(1500) NULL,
    CONSTRAINT [PK__audit_lo__3213E83F4294749C] PRIMARY KEY ([id])
);
GO
CREATE TABLE [dbo].[chat_messages] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [created_at] DATETIME2(6) NULL,
    [intent] VARCHAR(255) NULL,
    [message] NVARCHAR(2000) NULL,
    [response] NVARCHAR(4000) NULL,
    [sender_type] VARCHAR(20) NOT NULL,
    [session_id] BIGINT NOT NULL,
    CONSTRAINT [PK__chat_mes__3213E83FF03C3E1F] PRIMARY KEY ([id]),
    CONSTRAINT [CK__chat_mess__inten__2EA5EC27] CHECK ([intent]='UNKNOWN' OR [intent]='HELP' OR [intent]='CHECK_DEPARTMENT_EXPIRING_MATERIALS' OR [intent]='CHECK_RECEIVED_HISTORY' OR [intent]='HELP_CREATE_REQUEST' OR [intent]='CREATE_REQUEST_DRAFT' OR [intent]='CHECK_REQUEST_STATUS' OR [intent]='SUGGEST_ALTERNATIVE' OR [intent]='CHECK_SUPPLIER' OR [intent]='CHECK_BATCH' OR [intent]='CHECK_EXPIRY' OR [intent]='CHECK_LOCATION' OR [intent]='CHECK_STOCK' OR [intent]='SEARCH_MATERIAL')
);
GO
CREATE TABLE [dbo].[chat_sessions] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [created_at] DATETIME2(6) NULL,
    [title] NVARCHAR(220) NULL,
    [updated_at] DATETIME2(6) NULL,
    [username] VARCHAR(80) NOT NULL,
    CONSTRAINT [PK__chat_ses__3213E83FCFA94D17] PRIMARY KEY ([id])
);
GO
CREATE TABLE [dbo].[department_return_lines] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [note] NVARCHAR(500) NULL,
    [quantity] INT NOT NULL,
    [batch_id] BIGINT NOT NULL,
    [department_return_id] BIGINT NOT NULL,
    [department_stock_id] BIGINT NOT NULL,
    [location_id] BIGINT NOT NULL,
    [material_id] BIGINT NOT NULL,
    CONSTRAINT [PK__departme__3213E83F30DC4D92] PRIMARY KEY ([id])
);
GO
CREATE TABLE [dbo].[department_returns] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [created_at] DATETIME2(6) NULL,
    [created_by] VARCHAR(255) NULL,
    [department] NVARCHAR(120) NULL,
    [reason] NVARCHAR(1000) NULL,
    [received_at] DATETIME2(6) NULL,
    [received_by] VARCHAR(255) NULL,
    [return_code] VARCHAR(60) NOT NULL,
    [status] VARCHAR(40) NOT NULL,
    [updated_at] DATETIME2(6) NULL,
    [warehouse_id] BIGINT NOT NULL,
    CONSTRAINT [PK__departme__3213E83F65C8D6BD] PRIMARY KEY ([id]),
    CONSTRAINT [UK2nobk8dkv3a9tj6t8a5ddbtr7] UNIQUE ([return_code]),
    CONSTRAINT [CK__departmen__statu__467D75B8] CHECK ([status]='CANCELLED' OR [status]='REJECTED' OR [status]='RECEIVED_BY_WAREHOUSE' OR [status]='SUBMITTED' OR [status]='DRAFT')
);
GO
CREATE TABLE [dbo].[department_stock_movements] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [after_quantity] INT NOT NULL,
    [before_quantity] INT NOT NULL,
    [created_at] DATETIME2(6) NULL,
    [created_by] VARCHAR(255) NULL,
    [department] NVARCHAR(120) NULL,
    [movement_type] VARCHAR(40) NOT NULL,
    [note] NVARCHAR(1000) NULL,
    [quantity] INT NOT NULL,
    [reference_id] BIGINT NULL,
    [reference_type] VARCHAR(255) NULL,
    [batch_id] BIGINT NOT NULL,
    [material_id] BIGINT NOT NULL,
    CONSTRAINT [PK__departme__3213E83F205DCCE1] PRIMARY KEY ([id]),
    CONSTRAINT [CK__departmen__movem__4959E263] CHECK ([movement_type]='ADJUSTMENT' OR [movement_type]='RECALL_RETURN' OR [movement_type]='RETURN_TO_WAREHOUSE' OR [movement_type]='EXPIRED_AT_DEPARTMENT' OR [movement_type]='LOST' OR [movement_type]='DAMAGE' OR [movement_type]='USE' OR [movement_type]='RECEIVE_FROM_WAREHOUSE')
);
GO
CREATE TABLE [dbo].[department_stocks] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [department] NVARCHAR(120) NULL,
    [last_received_at] DATETIME2(6) NULL,
    [quantity_damaged] INT NOT NULL,
    [quantity_lost] INT NOT NULL,
    [quantity_on_hand] INT NOT NULL,
    [quantity_returned] INT NOT NULL,
    [quantity_used] INT NOT NULL,
    [updated_at] DATETIME2(6) NULL,
    [version] BIGINT NULL,
    [batch_id] BIGINT NOT NULL,
    [material_id] BIGINT NOT NULL,
    CONSTRAINT [PK__departme__3213E83FF9D1F713] PRIMARY KEY ([id]),
    CONSTRAINT [uk_department_stock] UNIQUE ([department],[material_id],[batch_id])
);
GO
CREATE TABLE [dbo].[departments] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [active] BIT NOT NULL,
    [code] VARCHAR(40) NOT NULL,
    [created_at] DATETIME2(6) NULL,
    [deleted] BIT NOT NULL,
    [description] NVARCHAR(500) NULL,
    [name] NVARCHAR(160) NULL,
    [updated_at] DATETIME2(6) NULL,
    CONSTRAINT [PK__departme__3213E83F2BF53EE8] PRIMARY KEY ([id]),
    CONSTRAINT [idx_departments_code] UNIQUE ([code])
);
GO
CREATE TABLE [dbo].[destruction_slip_lines] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [note] NVARCHAR(500) NULL,
    [quantity] INT NOT NULL,
    [reason] VARCHAR(255) NULL,
    [batch_id] BIGINT NOT NULL,
    [destruction_slip_id] BIGINT NOT NULL,
    [location_id] BIGINT NOT NULL,
    [material_id] BIGINT NOT NULL,
    [warehouse_id] BIGINT NOT NULL,
    CONSTRAINT [PK__destruct__3213E83F21B231D7] PRIMARY KEY ([id]),
    CONSTRAINT [CK__destructi__reaso__625A9A57] CHECK ([reason]='OTHER' OR [reason]='INVENTORY_LOSS' OR [reason]='QUALITY_FAILED' OR [reason]='LOST_STERILITY' OR [reason]='BROKEN_PACKAGE' OR [reason]='DAMAGED' OR [reason]='EXPIRED')
);
GO
CREATE TABLE [dbo].[destruction_slips] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [approved_at] DATETIME2(6) NULL,
    [approved_by] VARCHAR(255) NULL,
    [created_at] DATETIME2(6) NULL,
    [created_by] VARCHAR(255) NULL,
    [destroyed_at] DATETIME2(6) NULL,
    [destruction_code] VARCHAR(60) NOT NULL,
    [note] NVARCHAR(1000) NULL,
    [reason] NVARCHAR(1000) NULL,
    [rejected_at] DATETIME2(6) NULL,
    [rejected_by] VARCHAR(255) NULL,
    [rejected_reason] NVARCHAR(1000) NULL,
    [status] VARCHAR(255) NULL,
    [updated_at] DATETIME2(6) NULL,
    [accountant_approved_at] DATETIME2(6) NULL,
    [accountant_approved_by] VARCHAR(255) NULL,
    [manager_approved_at] DATETIME2(6) NULL,
    [manager_approved_by] VARCHAR(255) NULL,
    CONSTRAINT [PK__destruct__3213E83F719ED5F7] PRIMARY KEY ([id]),
    CONSTRAINT [UKdimeacyvsdq17fuu1oy5hm7qu] UNIQUE ([destruction_code]),
    CONSTRAINT [CK__destructi__statu__65370702] CHECK ([status]='CANCELLED' OR [status]='DESTROYED' OR [status]='REJECTED' OR [status]='APPROVED' OR [status]='SUBMITTED' OR [status]='DRAFT')
);
GO
CREATE TABLE [dbo].[inventory_count_lines] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [actual_quantity] INT NULL,
    [difference_quantity] INT NOT NULL,
    [note] NVARCHAR(500) NULL,
    [system_quantity] INT NOT NULL,
    [batch_id] BIGINT NOT NULL,
    [inventory_count_id] BIGINT NOT NULL,
    [location_id] BIGINT NOT NULL,
    [material_id] BIGINT NOT NULL,
    CONSTRAINT [PK__inventor__3213E83FDEF0C7F3] PRIMARY KEY ([id])
);
GO
CREATE TABLE [dbo].[inventory_counts] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [completed_at] DATETIME2(6) NULL,
    [completed_by] VARCHAR(255) NULL,
    [count_code] VARCHAR(60) NOT NULL,
    [created_at] DATETIME2(6) NULL,
    [note] NVARCHAR(1000) NULL,
    [started_at] DATETIME2(6) NULL,
    [started_by] VARCHAR(255) NULL,
    [status] VARCHAR(255) NULL,
    [updated_at] DATETIME2(6) NULL,
    [warehouse_id] BIGINT NOT NULL,
    CONSTRAINT [PK__inventor__3213E83F8D7697F0] PRIMARY KEY ([id]),
    CONSTRAINT [UK166nb05hcpnq7w4yqku3v00ge] UNIQUE ([count_code]),
    CONSTRAINT [CK__inventory__statu__69FBBC1F] CHECK ([status]='CANCELLED' OR [status]='COMPLETED' OR [status]='COUNTING' OR [status]='DRAFT')
);
GO
CREATE TABLE [dbo].[issue_batch_allocations] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [quantity] INT NOT NULL,
    [batch_id] BIGINT NOT NULL,
    [issue_slip_line_id] BIGINT NOT NULL,
    [location_id] BIGINT NOT NULL,
    [material_id] BIGINT NOT NULL,
    [warehouse_id] BIGINT NOT NULL,
    CONSTRAINT [PK__issue_ba__3213E83F319A4E57] PRIMARY KEY ([id])
);
GO
CREATE TABLE [dbo].[issue_slip_lines] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [approved_quantity] INT NOT NULL,
    [issued_quantity] INT NOT NULL,
    [note] NVARCHAR(500) NULL,
    [requested_quantity] INT NOT NULL,
    [issue_slip_id] BIGINT NOT NULL,
    [material_id] BIGINT NOT NULL,
    CONSTRAINT [PK__issue_sl__3213E83FB9BDBF42] PRIMARY KEY ([id])
);
GO
CREATE TABLE [dbo].[issue_slips] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [created_at] DATETIME2(6) NULL,
    [created_by] VARCHAR(255) NULL,
    [department] NVARCHAR(160) NULL,
    [issue_code] VARCHAR(60) NOT NULL,
    [issued_at] DATETIME2(6) NULL,
    [issued_by] VARCHAR(255) NULL,
    [note] NVARCHAR(1000) NULL,
    [received_at] DATETIME2(6) NULL,
    [received_by] NVARCHAR(120) NULL,
    [status] VARCHAR(255) NULL,
    [updated_at] DATETIME2(6) NULL,
    [material_request_id] BIGINT NOT NULL,
    [warehouse_id] BIGINT NULL,
    CONSTRAINT [PK__issue_sl__3213E83F2EA0FC0D] PRIMARY KEY ([id]),
    CONSTRAINT [UKpoihgqk6cuw0uo2usq5tea813] UNIQUE ([issue_code]),
    CONSTRAINT [CK__issue_sli__statu__2CF2ADDF] CHECK ([status]='CANCELLED' OR [status]='RECEIVED' OR [status]='ISSUED' OR [status]='PREPARING' OR [status]='DRAFT')
);
GO
CREATE TABLE [dbo].[material_batches] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [batch_number] VARCHAR(80) NOT NULL,
    [expiry_date] DATE NULL,
    [manufacture_date] DATE NULL,
    [quantity] INT NOT NULL,
    [receipt_date] DATE NULL,
    [status] VARCHAR(30) NOT NULL,
    [location_id] BIGINT NULL,
    [material_id] BIGINT NOT NULL,
    [supplier_id] BIGINT NULL,
    [warehouse_id] BIGINT NOT NULL,
    [created_at] DATETIME2(6) NULL,
    [initial_quantity] INT NULL,
    [updated_at] DATETIME2(6) NULL,
    [version] BIGINT NULL,
    CONSTRAINT [PK__material__3213E83FF386CF38] PRIMARY KEY ([id]),
    CONSTRAINT [CK__material___statu__4BAC3F29] CHECK ([status]='DESTROYED' OR [status]='RECALLED' OR [status]='EXPIRED' OR [status]='QUARANTINED' OR [status]='AVAILABLE')
);
GO
CREATE TABLE [dbo].[material_price_histories] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [created_at] DATETIME2(6) NULL,
    [created_by] VARCHAR(255) NULL,
    [note] NVARCHAR(1000) NULL,
    [quantity] INT NOT NULL,
    [received_date] DATE NULL,
    [total_amount] NUMERIC(18,2) NOT NULL,
    [unit_price] NUMERIC(18,2) NOT NULL,
    [material_id] BIGINT NOT NULL,
    [purchase_order_id] BIGINT NULL,
    [receipt_id] BIGINT NOT NULL,
    [supplier_id] BIGINT NULL,
    CONSTRAINT [PK__material__3213E83F41CA43E4] PRIMARY KEY ([id])
);
GO
CREATE TABLE [dbo].[material_request_lines] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [approved_quantity] INT NOT NULL,
    [reason] VARCHAR(255) NULL,
    [requested_quantity] INT NOT NULL,
    [material_id] BIGINT NOT NULL,
    [request_id] BIGINT NOT NULL,
    [issued_quantity] INT NULL,
    [note] NVARCHAR(500) NULL,
    [status] VARCHAR(255) NULL,
    CONSTRAINT [PK__material__3213E83F82B3B2D5] PRIMARY KEY ([id])
);
GO
CREATE TABLE [dbo].[material_requests] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [code] VARCHAR(60) NOT NULL,
    [created_at] DATETIME2(6) NULL,
    [department] NVARCHAR(160) NULL,
    [note] NVARCHAR(1000) NULL,
    [priority] NVARCHAR(80) NULL,
    [requester] NVARCHAR(120) NULL,
    [status] VARCHAR(40) NOT NULL,
    [department_approved_at] DATETIME2(6) NULL,
    [department_approved_by] NVARCHAR(120) NULL,
    [received_at] DATETIME2(6) NULL,
    [received_by] NVARCHAR(120) NULL,
    [rejected_reason] NVARCHAR(1000) NULL,
    [submitted_at] DATETIME2(6) NULL,
    [updated_at] DATETIME2(6) NULL,
    [warehouse_approved_at] DATETIME2(6) NULL,
    [warehouse_approved_by] NVARCHAR(120) NULL,
    CONSTRAINT [PK__material__3213E83FB57D1982] PRIMARY KEY ([id]),
    CONSTRAINT [UKcofg5ceptl5b11mkr2petj2mt] UNIQUE ([code]),
    CONSTRAINT [CK__material___statu__5070F446] CHECK ([status]='CANCELLED' OR [status]='RECEIVED' OR [status]='ISSUED' OR [status]='WAREHOUSE_REJECTED' OR [status]='WAREHOUSE_APPROVED' OR [status]='DEPARTMENT_REJECTED' OR [status]='DEPARTMENT_APPROVED' OR [status]='SUBMITTED' OR [status]='DRAFT')
);
GO
CREATE TABLE [dbo].[materials] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [actual_quantity] INT NOT NULL,
    [alias_text] NVARCHAR(500) NULL,
    [category] NVARCHAR(120) NULL,
    [code] VARCHAR(50) NOT NULL,
    [created_at] DATETIME2(6) NULL,
    [deleted] BIT NOT NULL,
    [estimated_unit_price] NUMERIC(38,2) NULL,
    [max_stock] INT NOT NULL,
    [min_stock] INT NOT NULL,
    [name] NVARCHAR(220) NULL,
    [package_spec] NVARCHAR(160) NULL,
    [pending_issue_quantity] INT NOT NULL,
    [reserved_quantity] INT NOT NULL,
    [special_control] BIT NOT NULL,
    [status] VARCHAR(255) NULL,
    [storage_condition] NVARCHAR(300) NULL,
    [unit] NVARCHAR(60) NULL,
    [updated_at] DATETIME2(6) NULL,
    [version] BIGINT NULL,
    CONSTRAINT [PK__material__3213E83FBE40EF96] PRIMARY KEY ([id]),
    CONSTRAINT [idx_materials_code] UNIQUE ([code]),
    CONSTRAINT [CK__materials__actua__534D60F1] CHECK ([actual_quantity]>=(0)),
    CONSTRAINT [CK__materials__max_s__5441852A] CHECK ([max_stock]>=(0)),
    CONSTRAINT [CK__materials__min_s__5535A963] CHECK ([min_stock]>=(0)),
    CONSTRAINT [CK__materials__pendi__5629CD9C] CHECK ([pending_issue_quantity]>=(0)),
    CONSTRAINT [CK__materials__reser__571DF1D5] CHECK ([reserved_quantity]>=(0))
);
GO
CREATE TABLE [dbo].[notifications] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [content] NVARCHAR(1000) NULL,
    [created_at] DATETIME2(6) NULL,
    [link] VARCHAR(255) NULL,
    [read_status] BIT NOT NULL,
    [receiver] VARCHAR(255) NULL,
    [title] NVARCHAR(220) NULL,
    [type] VARCHAR(255) NULL,
    CONSTRAINT [PK__notifica__3213E83FF988D708] PRIMARY KEY ([id])
);
GO
CREATE TABLE [dbo].[price_alerts] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [alert_level] VARCHAR(30) NOT NULL,
    [created_at] DATETIME2(6) NULL,
    [difference_percent] NUMERIC(8,2) NULL,
    [message] NVARCHAR(1000) NULL,
    [new_price] NUMERIC(18,2) NULL,
    [old_price] NUMERIC(18,2) NULL,
    [resolved] BIT NOT NULL,
    [resolved_at] DATETIME2(6) NULL,
    [material_id] BIGINT NOT NULL,
    [receipt_id] BIGINT NULL,
    [supplier_id] BIGINT NULL,
    CONSTRAINT [PK__price_al__3213E83F704B4837] PRIMARY KEY ([id]),
    CONSTRAINT [CK__price_ale__alert__5006DFF2] CHECK ([alert_level]='CRITICAL' OR [alert_level]='WARNING' OR [alert_level]='NORMAL')
);
GO
CREATE TABLE [dbo].[purchase_order_lines] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [note] NVARCHAR(500) NULL,
    [ordered_quantity] INT NOT NULL,
    [received_quantity] INT NOT NULL,
    [unit_price] NUMERIC(38,2) NULL,
    [material_id] BIGINT NOT NULL,
    [purchase_order_id] BIGINT NOT NULL,
    CONSTRAINT [PK__purchase__3213E83FD9E6326E] PRIMARY KEY ([id])
);
GO
CREATE TABLE [dbo].[purchase_orders] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [created_at] DATETIME2(6) NULL,
    [created_by] VARCHAR(255) NULL,
    [expected_delivery_date] DATE NULL,
    [note] NVARCHAR(1000) NULL,
    [order_code] VARCHAR(60) NOT NULL,
    [order_date] DATE NULL,
    [received_at] DATETIME2(6) NULL,
    [status] VARCHAR(255) NULL,
    [supplier_id] BIGINT NULL,
    CONSTRAINT [PK__purchase__3213E83FA35F5487] PRIMARY KEY ([id]),
    CONSTRAINT [UKf62gjxy3rx0v1vx1kmasgy7bh] UNIQUE ([order_code]),
    CONSTRAINT [CK__purchase___statu__6EC0713C] CHECK ([status]='CANCELLED' OR [status]='RECEIVED' OR [status]='PARTIALLY_RECEIVED' OR [status]='SENT' OR [status]='DRAFT')
);
GO
CREATE TABLE [dbo].[purchase_request_lines] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [note] NVARCHAR(500) NULL,
    [requested_quantity] INT NOT NULL,
    [suggested_quantity] INT NOT NULL,
    [material_id] BIGINT NOT NULL,
    [purchase_request_id] BIGINT NOT NULL,
    CONSTRAINT [PK__purchase__3213E83F17A8D752] PRIMARY KEY ([id])
);
GO
CREATE TABLE [dbo].[purchase_requests] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [approved_at] DATETIME2(6) NULL,
    [approved_by] VARCHAR(255) NULL,
    [created_at] DATETIME2(6) NULL,
    [created_by] VARCHAR(255) NULL,
    [reason] NVARCHAR(1000) NULL,
    [request_code] VARCHAR(60) NOT NULL,
    [status] VARCHAR(255) NULL,
    [updated_at] DATETIME2(6) NULL,
    CONSTRAINT [PK__purchase__3213E83FDC03D84C] PRIMARY KEY ([id]),
    CONSTRAINT [UKcw09djyspe3dvtx66u7bd4hh4] UNIQUE ([request_code]),
    CONSTRAINT [CK__purchase___statu__73852659] CHECK ([status]='CANCELLED' OR [status]='REJECTED' OR [status]='APPROVED' OR [status]='SUBMITTED' OR [status]='DRAFT')
);
GO
CREATE TABLE [dbo].[recall_department_responses] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [department] NVARCHAR(160) NULL,
    [note] NVARCHAR(1000) NULL,
    [remaining_quantity] INT NOT NULL,
    [responded_at] DATETIME2(6) NULL,
    [responded_by] VARCHAR(255) NULL,
    [returned_quantity] INT NOT NULL,
    [used_quantity] INT NOT NULL,
    [recall_order_id] BIGINT NOT NULL,
    CONSTRAINT [PK__recall_d__3213E83F85F4AD02] PRIMARY KEY ([id])
);
GO
CREATE TABLE [dbo].[recall_order_lines] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [department] NVARCHAR(160) NULL,
    [issued_quantity] INT NOT NULL,
    [note] NVARCHAR(500) NULL,
    [remaining_quantity] INT NOT NULL,
    [returned_quantity] INT NOT NULL,
    [status] VARCHAR(255) NULL,
    [recall_order_id] BIGINT NOT NULL,
    CONSTRAINT [PK__recall_o__3213E83FA4D0B8C8] PRIMARY KEY ([id])
);
GO
CREATE TABLE [dbo].[recall_orders] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [approved_by] VARCHAR(255) NULL,
    [created_at] DATETIME2(6) NULL,
    [created_by] VARCHAR(255) NULL,
    [note] NVARCHAR(1000) NULL,
    [reason] NVARCHAR(1000) NULL,
    [recall_code] VARCHAR(60) NOT NULL,
    [status] VARCHAR(255) NULL,
    [updated_at] DATETIME2(6) NULL,
    [batch_id] BIGINT NOT NULL,
    [material_id] BIGINT NOT NULL,
    CONSTRAINT [PK__recall_o__3213E83FBF73CBB5] PRIMARY KEY ([id]),
    CONSTRAINT [UKg9tw5y65yqgjsyihggpyquwa9] UNIQUE ([recall_code]),
    CONSTRAINT [CK__recall_or__statu__7A3223E8] CHECK ([status]='CANCELLED' OR [status]='COMPLETED' OR [status]='ACTIVE' OR [status]='DRAFT')
);
GO
CREATE TABLE [dbo].[receipt_lines] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [batch_number] VARCHAR(80) NOT NULL,
    [expiry_date] DATE NULL,
    [manufacturing_date] DATE NULL,
    [note] NVARCHAR(500) NULL,
    [quantity] INT NOT NULL,
    [unit_price] NUMERIC(38,2) NULL,
    [location_id] BIGINT NOT NULL,
    [material_id] BIGINT NOT NULL,
    [receipt_id] BIGINT NOT NULL,
    CONSTRAINT [PK__receipt___3213E83FD924CCAD] PRIMARY KEY ([id])
);
GO
CREATE TABLE [dbo].[receipts] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [confirmed_at] DATETIME2(6) NULL,
    [confirmed_by] VARCHAR(255) NULL,
    [created_at] DATETIME2(6) NULL,
    [created_by] VARCHAR(255) NULL,
    [note] NVARCHAR(1000) NULL,
    [receipt_code] VARCHAR(60) NOT NULL,
    [receipt_date] DATE NULL,
    [status] VARCHAR(255) NULL,
    [updated_at] DATETIME2(6) NULL,
    [supplier_id] BIGINT NULL,
    [warehouse_id] BIGINT NOT NULL,
    CONSTRAINT [PK__receipts__3213E83FD5C7AC32] PRIMARY KEY ([id]),
    CONSTRAINT [UKt1da02pofk1uv20ldpwr8yhwl] UNIQUE ([receipt_code]),
    CONSTRAINT [CK__receipts__status__31B762FC] CHECK ([status]='CANCELLED' OR [status]='CONFIRMED' OR [status]='DRAFT')
);
GO
CREATE TABLE [dbo].[request_approval_logs] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [action] VARCHAR(255) NULL,
    [actor] VARCHAR(255) NULL,
    [created_at] DATETIME2(6) NULL,
    [note] NVARCHAR(1000) NULL,
    [material_request_id] BIGINT NOT NULL,
    CONSTRAINT [PK__request___3213E83FB3625E10] PRIMARY KEY ([id])
);
GO
CREATE TABLE [dbo].[stock_adjustment_lines] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [actual_quantity] INT NOT NULL,
    [adjustment_quantity] INT NOT NULL,
    [note] NVARCHAR(500) NULL,
    [system_quantity] INT NOT NULL,
    [batch_id] BIGINT NOT NULL,
    [location_id] BIGINT NOT NULL,
    [material_id] BIGINT NOT NULL,
    [stock_adjustment_id] BIGINT NOT NULL,
    CONSTRAINT [PK__stock_ad__3213E83F0160BAE8] PRIMARY KEY ([id])
);
GO
CREATE TABLE [dbo].[stock_adjustments] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [adjustment_code] VARCHAR(60) NOT NULL,
    [approved_at] DATETIME2(6) NULL,
    [approved_by] VARCHAR(255) NULL,
    [created_at] DATETIME2(6) NULL,
    [created_by] VARCHAR(255) NULL,
    [reason] NVARCHAR(1000) NULL,
    [rejected_at] DATETIME2(6) NULL,
    [rejected_by] VARCHAR(255) NULL,
    [rejected_reason] NVARCHAR(1000) NULL,
    [status] VARCHAR(255) NULL,
    [updated_at] DATETIME2(6) NULL,
    [inventory_count_id] BIGINT NULL,
    [warehouse_id] BIGINT NOT NULL,
    [accountant_approved_at] DATETIME2(6) NULL,
    [accountant_approved_by] VARCHAR(255) NULL,
    [manager_approved_at] DATETIME2(6) NULL,
    [manager_approved_by] VARCHAR(255) NULL,
    CONSTRAINT [PK__stock_ad__3213E83FC3298F38] PRIMARY KEY ([id]),
    CONSTRAINT [UKm2ll7vl26fxglcc6uuap364xf] UNIQUE ([adjustment_code]),
    CONSTRAINT [CK__stock_adj__statu__7EF6D905] CHECK ([status]='CANCELLED' OR [status]='REJECTED' OR [status]='APPROVED' OR [status]='SUBMITTED' OR [status]='DRAFT')
);
GO
CREATE TABLE [dbo].[stock_balances] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [actual_quantity] INT NOT NULL,
    [created_at] DATETIME2(6) NULL,
    [pending_issue_quantity] INT NOT NULL,
    [reserved_quantity] INT NOT NULL,
    [updated_at] DATETIME2(6) NULL,
    [batch_id] BIGINT NOT NULL,
    [location_id] BIGINT NOT NULL,
    [material_id] BIGINT NOT NULL,
    [warehouse_id] BIGINT NOT NULL,
    [version] BIGINT NULL,
    CONSTRAINT [PK__stock_ba__3213E83FD6839338] PRIMARY KEY ([id]),
    CONSTRAINT [uk_stock_balance_scope] UNIQUE ([material_id],[batch_id],[warehouse_id],[location_id])
);
GO
CREATE TABLE [dbo].[stock_movements] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [after_quantity] INT NOT NULL,
    [before_quantity] INT NOT NULL,
    [created_at] DATETIME2(6) NULL,
    [created_by] VARCHAR(255) NULL,
    [movement_type] VARCHAR(30) NOT NULL,
    [note] VARCHAR(255) NULL,
    [quantity] INT NOT NULL,
    [reference_code] VARCHAR(255) NULL,
    [reference_type] VARCHAR(255) NULL,
    [batch_id] BIGINT NULL,
    [material_id] BIGINT NOT NULL,
    [warehouse_id] BIGINT NULL,
    [location_id] BIGINT NULL,
    CONSTRAINT [PK__stock_mo__3213E83FF3EB999E] PRIMARY KEY ([id]),
    CONSTRAINT [CK__stock_mov__movem__5BE2A6F2] CHECK ([movement_type]='RECALL' OR [movement_type]='DESTROY' OR [movement_type]='ADJUSTMENT_OUT' OR [movement_type]='ADJUSTMENT_IN' OR [movement_type]='RELEASE' OR [movement_type]='RESERVE' OR [movement_type]='OUT' OR [movement_type]='IN')
);
GO
CREATE TABLE [dbo].[stock_reservations] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [created_at] DATETIME2(6) NULL,
    [issued_at] DATETIME2(6) NULL,
    [released_at] DATETIME2(6) NULL,
    [reserved_quantity] INT NOT NULL,
    [status] VARCHAR(30) NOT NULL,
    [batch_id] BIGINT NOT NULL,
    [location_id] BIGINT NOT NULL,
    [material_id] BIGINT NOT NULL,
    [material_request_id] BIGINT NOT NULL,
    [material_request_line_id] BIGINT NOT NULL,
    [stock_balance_id] BIGINT NOT NULL,
    [warehouse_id] BIGINT NOT NULL,
    [version] BIGINT NULL,
    CONSTRAINT [PK__stock_re__3213E83F83CFB35E] PRIMARY KEY ([id]),
    CONSTRAINT [CK__stock_res__statu__3864608B] CHECK ([status]='CANCELLED' OR [status]='ISSUED' OR [status]='RELEASED' OR [status]='ACTIVE')
);
GO
CREATE TABLE [dbo].[stock_transfer_lines] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [note] NVARCHAR(500) NULL,
    [quantity] INT NOT NULL,
    [batch_id] BIGINT NOT NULL,
    [from_location_id] BIGINT NOT NULL,
    [material_id] BIGINT NOT NULL,
    [stock_transfer_id] BIGINT NOT NULL,
    [to_location_id] BIGINT NOT NULL,
    CONSTRAINT [PK__stock_tr__3213E83FC800188C] PRIMARY KEY ([id])
);
GO
CREATE TABLE [dbo].[stock_transfers] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [approved_at] DATETIME2(6) NULL,
    [approved_by] VARCHAR(255) NULL,
    [created_at] DATETIME2(6) NULL,
    [created_by] VARCHAR(255) NULL,
    [note] NVARCHAR(1000) NULL,
    [reason] NVARCHAR(1000) NULL,
    [received_at] DATETIME2(6) NULL,
    [received_by] VARCHAR(255) NULL,
    [status] VARCHAR(255) NULL,
    [transfer_code] VARCHAR(60) NOT NULL,
    [transferred_at] DATETIME2(6) NULL,
    [transferred_by] VARCHAR(255) NULL,
    [updated_at] DATETIME2(6) NULL,
    [from_warehouse_id] BIGINT NOT NULL,
    [to_warehouse_id] BIGINT NOT NULL,
    CONSTRAINT [PK__stock_tr__3213E83F051CE290] PRIMARY KEY ([id]),
    CONSTRAINT [UKel3lrfowfromgwcvxlve9xvow] UNIQUE ([transfer_code]),
    CONSTRAINT [CK__stock_tra__statu__03BB8E22] CHECK ([status]='CANCELLED' OR [status]='REJECTED' OR [status]='RECEIVED' OR [status]='TRANSFERRED' OR [status]='APPROVED' OR [status]='SUBMITTED' OR [status]='DRAFT')
);
GO
CREATE TABLE [dbo].[storage_conditions] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [material_id] BIGINT NOT NULL,
    [min_temperature] DECIMAL(10,2) NULL,
    [max_temperature] DECIMAL(10,2) NULL,
    [min_humidity] DECIMAL(10,2) NULL,
    [max_humidity] DECIMAL(10,2) NULL,
    [light_sensitive] BIT NOT NULL CONSTRAINT [DF_storage_conditions_light_sensitive] DEFAULT ((0)),
    [cold_chain_required] BIT NOT NULL CONSTRAINT [DF_storage_conditions_cold_chain_required] DEFAULT ((0)),
    [note] NVARCHAR(700) NULL,
    [created_at] DATETIME2(7) NULL,
    [updated_at] DATETIME2(7) NULL,
    CONSTRAINT [PK__storage___3213E83F99027296] PRIMARY KEY ([id])
);
GO
CREATE TABLE [dbo].[storage_locations] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [active] BIT NOT NULL,
    [code] VARCHAR(50) NOT NULL,
    [location_type] VARCHAR(255) NULL,
    [name] NVARCHAR(150) NULL,
    [parent_id] BIGINT NULL,
    [warehouse_id] BIGINT NOT NULL,
    [created_at] DATETIME2(6) NULL,
    [description] NVARCHAR(500) NULL,
    [updated_at] DATETIME2(6) NULL,
    [deleted] BIT NOT NULL CONSTRAINT [DF_storage_locations_deleted] DEFAULT ((0)),
    CONSTRAINT [PK__storage___3213E83FB44085C6] PRIMARY KEY ([id]),
    CONSTRAINT [idx_locations_code] UNIQUE ([code]),
    CONSTRAINT [idx_locations_warehouse_code] UNIQUE ([warehouse_id],[code])
);
GO
CREATE TABLE [dbo].[suppliers] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [active] BIT NOT NULL,
    [address] NVARCHAR(300) NULL,
    [code] VARCHAR(40) NOT NULL,
    [email] VARCHAR(255) NULL,
    [name] NVARCHAR(180) NULL,
    [phone] VARCHAR(255) NULL,
    CONSTRAINT [PK__supplier__3213E83F3C065E47] PRIMARY KEY ([id]),
    CONSTRAINT [idx_suppliers_code] UNIQUE ([code])
);
GO
CREATE TABLE [dbo].[temperature_logs] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [warehouse_id] BIGINT NOT NULL,
    [recorded_at] DATETIME2(7) NULL,
    [temperature] DECIMAL(10,2) NULL,
    [humidity] DECIMAL(10,2) NULL,
    [recorded_by] NVARCHAR(120) NULL,
    [status] VARCHAR(30) NULL,
    [note] NVARCHAR(700) NULL,
    CONSTRAINT [PK__temperat__3213E83FF7C9041B] PRIMARY KEY ([id])
);
GO
CREATE TABLE [dbo].[users] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [department] NVARCHAR(120) NULL,
    [enabled] BIT NOT NULL,
    [full_name] NVARCHAR(150) NULL,
    [password] VARCHAR(255) NOT NULL,
    [role] VARCHAR(40) NOT NULL,
    [username] VARCHAR(80) NOT NULL,
    [created_at] DATETIME2(6) NULL,
    [email] VARCHAR(160) NULL,
    [phone] VARCHAR(30) NULL,
    [updated_at] DATETIME2(6) NULL,
    [locked] BIT NOT NULL CONSTRAINT [DF_users_locked] DEFAULT ((0)),
    [deleted] BIT NOT NULL CONSTRAINT [DF_users_deleted] DEFAULT ((0)),
    [must_change_password] BIT NOT NULL DEFAULT 0,
    CONSTRAINT [PK__users__3213E83F964FE602] PRIMARY KEY ([id]),
    CONSTRAINT [idx_users_username] UNIQUE ([username]),
    CONSTRAINT [CK__users__role__628FA481] CHECK ([role]='MANAGER' OR [role]='ACCOUNTANT' OR [role]='PROCUREMENT' OR [role]='WAREHOUSE_STAFF' OR [role]='DEPARTMENT_HEAD' OR [role]='DEPARTMENT_STAFF' OR [role]='ADMIN')
);
GO
CREATE TABLE [dbo].[warehouses] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [active] BIT NOT NULL,
    [address] NVARCHAR(300) NULL,
    [code] VARCHAR(40) NOT NULL,
    [name] NVARCHAR(150) NULL,
    [type] VARCHAR(255) NULL,
    [created_at] DATETIME2(6) NULL,
    [description] NVARCHAR(500) NULL,
    [updated_at] DATETIME2(6) NULL,
    [deleted] BIT NOT NULL CONSTRAINT [DF_warehouses_deleted] DEFAULT ((0)),
    CONSTRAINT [PK__warehous__3213E83FC079EA81] PRIMARY KEY ([id]),
    CONSTRAINT [idx_warehouses_code] UNIQUE ([code])
);
GO
CREATE TABLE [dbo].[roles] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [code] VARCHAR(40) NOT NULL,
    [name] NVARCHAR(120) NOT NULL,
    CONSTRAINT [PK_roles] PRIMARY KEY ([id]),
    CONSTRAINT [UQ_roles_code] UNIQUE ([code])
);
GO
CREATE TABLE [dbo].[user_roles] (
    [user_id] BIGINT NOT NULL,
    [role_id] BIGINT NOT NULL,
    CONSTRAINT [PK_user_roles] PRIMARY KEY ([user_id], [role_id])
);
GO
ALTER TABLE [dbo].[chat_messages] ADD CONSTRAINT [FK3cpkdtwdxndrjhrx3gt9q5ux9] FOREIGN KEY ([session_id]) REFERENCES [dbo].[chat_sessions] ([id]);
GO
ALTER TABLE [dbo].[department_return_lines] ADD CONSTRAINT [FK5q4nbmo4igia5qvwpm493cjav] FOREIGN KEY ([location_id]) REFERENCES [dbo].[storage_locations] ([id]);
GO
ALTER TABLE [dbo].[department_return_lines] ADD CONSTRAINT [FKgh1jm19y99bcplssmt3u25cf9] FOREIGN KEY ([batch_id]) REFERENCES [dbo].[material_batches] ([id]);
GO
ALTER TABLE [dbo].[department_return_lines] ADD CONSTRAINT [FKhd5r3xx6pj8ejm2eaklf5twr0] FOREIGN KEY ([material_id]) REFERENCES [dbo].[materials] ([id]);
GO
ALTER TABLE [dbo].[department_return_lines] ADD CONSTRAINT [FKi1hp09do8v5fichchrlqabwhv] FOREIGN KEY ([department_stock_id]) REFERENCES [dbo].[department_stocks] ([id]);
GO
ALTER TABLE [dbo].[department_return_lines] ADD CONSTRAINT [FKj89xao2glixkrj58uukf32b4p] FOREIGN KEY ([department_return_id]) REFERENCES [dbo].[department_returns] ([id]);
GO
ALTER TABLE [dbo].[department_returns] ADD CONSTRAINT [FKnds27ojgbp6j2xn2s1yu4i41f] FOREIGN KEY ([warehouse_id]) REFERENCES [dbo].[warehouses] ([id]);
GO
ALTER TABLE [dbo].[department_stock_movements] ADD CONSTRAINT [FK5grvod990k1ucyul1faq482g4] FOREIGN KEY ([material_id]) REFERENCES [dbo].[materials] ([id]);
GO
ALTER TABLE [dbo].[department_stock_movements] ADD CONSTRAINT [FKl8jr6m0kj178tb0l2q8kik3qc] FOREIGN KEY ([batch_id]) REFERENCES [dbo].[material_batches] ([id]);
GO
ALTER TABLE [dbo].[department_stocks] ADD CONSTRAINT [FKiphkesulkfllhbfkmib91fdtr] FOREIGN KEY ([batch_id]) REFERENCES [dbo].[material_batches] ([id]);
GO
ALTER TABLE [dbo].[department_stocks] ADD CONSTRAINT [FKnf51f4kv53wjj4sec9sw0ilc3] FOREIGN KEY ([material_id]) REFERENCES [dbo].[materials] ([id]);
GO
ALTER TABLE [dbo].[destruction_slip_lines] ADD CONSTRAINT [FK6d71mkhe1wm2e7jesclt0rttb] FOREIGN KEY ([batch_id]) REFERENCES [dbo].[material_batches] ([id]);
GO
ALTER TABLE [dbo].[destruction_slip_lines] ADD CONSTRAINT [FK9r8hyv9r6pfj510njm7apocwa] FOREIGN KEY ([warehouse_id]) REFERENCES [dbo].[warehouses] ([id]);
GO
ALTER TABLE [dbo].[destruction_slip_lines] ADD CONSTRAINT [FKanqq2ut9f8rxor04qbwn3ibjm] FOREIGN KEY ([material_id]) REFERENCES [dbo].[materials] ([id]);
GO
ALTER TABLE [dbo].[destruction_slip_lines] ADD CONSTRAINT [FKin71nigefpvyyd3fhiuwqpdda] FOREIGN KEY ([destruction_slip_id]) REFERENCES [dbo].[destruction_slips] ([id]);
GO
ALTER TABLE [dbo].[destruction_slip_lines] ADD CONSTRAINT [FKtaw1yfsxfampuox1md3187y54] FOREIGN KEY ([location_id]) REFERENCES [dbo].[storage_locations] ([id]);
GO
ALTER TABLE [dbo].[inventory_count_lines] ADD CONSTRAINT [FK29x57x63b17t5hms34n7dv27s] FOREIGN KEY ([batch_id]) REFERENCES [dbo].[material_batches] ([id]);
GO
ALTER TABLE [dbo].[inventory_count_lines] ADD CONSTRAINT [FKhwrk5v2ffem3mfp2g9rk1npjg] FOREIGN KEY ([location_id]) REFERENCES [dbo].[storage_locations] ([id]);
GO
ALTER TABLE [dbo].[inventory_count_lines] ADD CONSTRAINT [FKmeh9t6q4oqokce8p650ai0vcj] FOREIGN KEY ([inventory_count_id]) REFERENCES [dbo].[inventory_counts] ([id]);
GO
ALTER TABLE [dbo].[inventory_count_lines] ADD CONSTRAINT [FKrg7pisxaw0bqe65macdrcarns] FOREIGN KEY ([material_id]) REFERENCES [dbo].[materials] ([id]);
GO
ALTER TABLE [dbo].[inventory_counts] ADD CONSTRAINT [FK70km01tdhwal706tmn6y6x94e] FOREIGN KEY ([warehouse_id]) REFERENCES [dbo].[warehouses] ([id]);
GO
ALTER TABLE [dbo].[issue_batch_allocations] ADD CONSTRAINT [FK35l06upx76dyaugft8680feel] FOREIGN KEY ([warehouse_id]) REFERENCES [dbo].[warehouses] ([id]);
GO
ALTER TABLE [dbo].[issue_batch_allocations] ADD CONSTRAINT [FKa44lc0wrbyqbdb4gs92v3d2va] FOREIGN KEY ([material_id]) REFERENCES [dbo].[materials] ([id]);
GO
ALTER TABLE [dbo].[issue_batch_allocations] ADD CONSTRAINT [FKey8kbxvhv9qkd36vf29yh20nr] FOREIGN KEY ([issue_slip_line_id]) REFERENCES [dbo].[issue_slip_lines] ([id]);
GO
ALTER TABLE [dbo].[issue_batch_allocations] ADD CONSTRAINT [FKmblkn4geksiidh0vsmhwwd3g5] FOREIGN KEY ([batch_id]) REFERENCES [dbo].[material_batches] ([id]);
GO
ALTER TABLE [dbo].[issue_batch_allocations] ADD CONSTRAINT [FKp8eg1qb7g3eakdqoio9sqwu6f] FOREIGN KEY ([location_id]) REFERENCES [dbo].[storage_locations] ([id]);
GO
ALTER TABLE [dbo].[issue_slip_lines] ADD CONSTRAINT [FK73vavjpl6lob6u4ioimv0ivh4] FOREIGN KEY ([material_id]) REFERENCES [dbo].[materials] ([id]);
GO
ALTER TABLE [dbo].[issue_slip_lines] ADD CONSTRAINT [FKi3qgnmwe870wtyevw7osib4jt] FOREIGN KEY ([issue_slip_id]) REFERENCES [dbo].[issue_slips] ([id]);
GO
ALTER TABLE [dbo].[issue_slips] ADD CONSTRAINT [FKh0dx0ixf3g2vnuue7tdneq7o8] FOREIGN KEY ([warehouse_id]) REFERENCES [dbo].[warehouses] ([id]);
GO
ALTER TABLE [dbo].[issue_slips] ADD CONSTRAINT [FKhsug21qge7rdxv7jarqyp8m2m] FOREIGN KEY ([material_request_id]) REFERENCES [dbo].[material_requests] ([id]);
GO
ALTER TABLE [dbo].[material_batches] ADD CONSTRAINT [FK34crtl0ua1umfybtqnyigm5xy] FOREIGN KEY ([supplier_id]) REFERENCES [dbo].[suppliers] ([id]);
GO
ALTER TABLE [dbo].[material_batches] ADD CONSTRAINT [FKg3ey9hucohhes9srcv9h702k6] FOREIGN KEY ([material_id]) REFERENCES [dbo].[materials] ([id]);
GO
ALTER TABLE [dbo].[material_batches] ADD CONSTRAINT [FKryv1li16eqvpcy67h33x1irvm] FOREIGN KEY ([location_id]) REFERENCES [dbo].[storage_locations] ([id]);
GO
ALTER TABLE [dbo].[material_batches] ADD CONSTRAINT [FKsgtun6ooip4fx7u8be4l04bcs] FOREIGN KEY ([warehouse_id]) REFERENCES [dbo].[warehouses] ([id]);
GO
ALTER TABLE [dbo].[material_price_histories] ADD CONSTRAINT [FK5vfbtciwq41cs4gdq8eho26fr] FOREIGN KEY ([receipt_id]) REFERENCES [dbo].[receipts] ([id]);
GO
ALTER TABLE [dbo].[material_price_histories] ADD CONSTRAINT [FKfqes3enuvetosfp1jfyi9dhm1] FOREIGN KEY ([material_id]) REFERENCES [dbo].[materials] ([id]);
GO
ALTER TABLE [dbo].[material_price_histories] ADD CONSTRAINT [FKikwdkw1vibkb0e54wvb8le18r] FOREIGN KEY ([supplier_id]) REFERENCES [dbo].[suppliers] ([id]);
GO
ALTER TABLE [dbo].[material_price_histories] ADD CONSTRAINT [FKrbkeo95fuapvl0lb5250wgun3] FOREIGN KEY ([purchase_order_id]) REFERENCES [dbo].[purchase_orders] ([id]);
GO
ALTER TABLE [dbo].[material_request_lines] ADD CONSTRAINT [FK1dco8yujfe7w7pbxe2i2wmwgh] FOREIGN KEY ([material_id]) REFERENCES [dbo].[materials] ([id]);
GO
ALTER TABLE [dbo].[material_request_lines] ADD CONSTRAINT [FKc65bv4rmm6ldn5th17blijjek] FOREIGN KEY ([request_id]) REFERENCES [dbo].[material_requests] ([id]);
GO
ALTER TABLE [dbo].[price_alerts] ADD CONSTRAINT [FK4h3rafevms2c35cj3egwc3ul0] FOREIGN KEY ([receipt_id]) REFERENCES [dbo].[receipts] ([id]);
GO
ALTER TABLE [dbo].[price_alerts] ADD CONSTRAINT [FK9i1los5lw8vhhv956p1qwxl0f] FOREIGN KEY ([material_id]) REFERENCES [dbo].[materials] ([id]);
GO
ALTER TABLE [dbo].[price_alerts] ADD CONSTRAINT [FKolm1m58wkhutphxqpsxj51o4b] FOREIGN KEY ([supplier_id]) REFERENCES [dbo].[suppliers] ([id]);
GO
ALTER TABLE [dbo].[purchase_order_lines] ADD CONSTRAINT [FKeu2ib4961jw2981gttsut6lwq] FOREIGN KEY ([material_id]) REFERENCES [dbo].[materials] ([id]);
GO
ALTER TABLE [dbo].[purchase_order_lines] ADD CONSTRAINT [FKlm5ieywqw1p1l4oxnkup8j2m6] FOREIGN KEY ([purchase_order_id]) REFERENCES [dbo].[purchase_orders] ([id]);
GO
ALTER TABLE [dbo].[purchase_orders] ADD CONSTRAINT [FKrpdasmb8y8xs5tiy4369xpinq] FOREIGN KEY ([supplier_id]) REFERENCES [dbo].[suppliers] ([id]);
GO
ALTER TABLE [dbo].[purchase_request_lines] ADD CONSTRAINT [FKcaam25bpexyvoexso64cd5vvw] FOREIGN KEY ([material_id]) REFERENCES [dbo].[materials] ([id]);
GO
ALTER TABLE [dbo].[purchase_request_lines] ADD CONSTRAINT [FKheosdp8jo8etr4nal5y5y7pjt] FOREIGN KEY ([purchase_request_id]) REFERENCES [dbo].[purchase_requests] ([id]);
GO
ALTER TABLE [dbo].[recall_department_responses] ADD CONSTRAINT [FK9t785bq87tgnyhuvb91fcme6i] FOREIGN KEY ([recall_order_id]) REFERENCES [dbo].[recall_orders] ([id]);
GO
ALTER TABLE [dbo].[recall_order_lines] ADD CONSTRAINT [FKbwo68d0v96cglraease4a18hy] FOREIGN KEY ([recall_order_id]) REFERENCES [dbo].[recall_orders] ([id]);
GO
ALTER TABLE [dbo].[recall_orders] ADD CONSTRAINT [FK7frtk6vogdrq3ivmq0umrr339] FOREIGN KEY ([material_id]) REFERENCES [dbo].[materials] ([id]);
GO
ALTER TABLE [dbo].[recall_orders] ADD CONSTRAINT [FK90y6134ct64s9flqoxtdu0sle] FOREIGN KEY ([batch_id]) REFERENCES [dbo].[material_batches] ([id]);
GO
ALTER TABLE [dbo].[receipt_lines] ADD CONSTRAINT [FK72205pbvxcrayl5ect6pa7lix] FOREIGN KEY ([receipt_id]) REFERENCES [dbo].[receipts] ([id]);
GO
ALTER TABLE [dbo].[receipt_lines] ADD CONSTRAINT [FK9vbohn2uq80kqvjsaup1cduv0] FOREIGN KEY ([location_id]) REFERENCES [dbo].[storage_locations] ([id]);
GO
ALTER TABLE [dbo].[receipt_lines] ADD CONSTRAINT [FKiepf5g2g49eujpalthgii9dvo] FOREIGN KEY ([material_id]) REFERENCES [dbo].[materials] ([id]);
GO
ALTER TABLE [dbo].[receipts] ADD CONSTRAINT [FK4ksphcrrl2epyxvdqwo0gat9d] FOREIGN KEY ([supplier_id]) REFERENCES [dbo].[suppliers] ([id]);
GO
ALTER TABLE [dbo].[receipts] ADD CONSTRAINT [FKtpm8me8cng0vjgj5s7ojcd0n0] FOREIGN KEY ([warehouse_id]) REFERENCES [dbo].[warehouses] ([id]);
GO
ALTER TABLE [dbo].[request_approval_logs] ADD CONSTRAINT [FK32clvf0lj4wbu8dq2uk6pyuag] FOREIGN KEY ([material_request_id]) REFERENCES [dbo].[material_requests] ([id]);
GO
ALTER TABLE [dbo].[stock_adjustment_lines] ADD CONSTRAINT [FK9ihlm4590p5m78kss9byvrsc0] FOREIGN KEY ([batch_id]) REFERENCES [dbo].[material_batches] ([id]);
GO
ALTER TABLE [dbo].[stock_adjustment_lines] ADD CONSTRAINT [FKrgdui28907drrkq6l2fwsve7w] FOREIGN KEY ([stock_adjustment_id]) REFERENCES [dbo].[stock_adjustments] ([id]);
GO
ALTER TABLE [dbo].[stock_adjustment_lines] ADD CONSTRAINT [FKrostmohtjqcdbxc2rxt203pib] FOREIGN KEY ([location_id]) REFERENCES [dbo].[storage_locations] ([id]);
GO
ALTER TABLE [dbo].[stock_adjustment_lines] ADD CONSTRAINT [FKsb8i3h8vtjhm1e1gyl05oeguq] FOREIGN KEY ([material_id]) REFERENCES [dbo].[materials] ([id]);
GO
ALTER TABLE [dbo].[stock_adjustments] ADD CONSTRAINT [FK6w01sbrsf3prbhrd0yo54ceeq] FOREIGN KEY ([inventory_count_id]) REFERENCES [dbo].[inventory_counts] ([id]);
GO
ALTER TABLE [dbo].[stock_adjustments] ADD CONSTRAINT [FKrx0rcvfnre9vgtayn08ax20f5] FOREIGN KEY ([warehouse_id]) REFERENCES [dbo].[warehouses] ([id]);
GO
ALTER TABLE [dbo].[stock_balances] ADD CONSTRAINT [FK82pbvvgg7391j8pn1qrlcec59] FOREIGN KEY ([batch_id]) REFERENCES [dbo].[material_batches] ([id]);
GO
ALTER TABLE [dbo].[stock_balances] ADD CONSTRAINT [FKhyi31eekmc71q5st7waxr5ug6] FOREIGN KEY ([location_id]) REFERENCES [dbo].[storage_locations] ([id]);
GO
ALTER TABLE [dbo].[stock_balances] ADD CONSTRAINT [FKnunr6oip0sfbr0v72cu5i03ip] FOREIGN KEY ([warehouse_id]) REFERENCES [dbo].[warehouses] ([id]);
GO
ALTER TABLE [dbo].[stock_balances] ADD CONSTRAINT [FKqmw3m32px8rb3jiifxwvaoeif] FOREIGN KEY ([material_id]) REFERENCES [dbo].[materials] ([id]);
GO
ALTER TABLE [dbo].[stock_movements] ADD CONSTRAINT [FKa0ppxirl86hw6c927xssogk3n] FOREIGN KEY ([location_id]) REFERENCES [dbo].[storage_locations] ([id]);
GO
ALTER TABLE [dbo].[stock_movements] ADD CONSTRAINT [FKiparp4rp4rsfsxb9y02oyxauh] FOREIGN KEY ([warehouse_id]) REFERENCES [dbo].[warehouses] ([id]);
GO
ALTER TABLE [dbo].[stock_movements] ADD CONSTRAINT [FKkgvfix1r4nrcxvn6n7f714pif] FOREIGN KEY ([material_id]) REFERENCES [dbo].[materials] ([id]);
GO
ALTER TABLE [dbo].[stock_movements] ADD CONSTRAINT [FKpm0pmc7clh583gvybnoe07966] FOREIGN KEY ([batch_id]) REFERENCES [dbo].[material_batches] ([id]);
GO
ALTER TABLE [dbo].[stock_reservations] ADD CONSTRAINT [FK1ye28nghyxce3wqmit5wk8ab3] FOREIGN KEY ([batch_id]) REFERENCES [dbo].[material_batches] ([id]);
GO
ALTER TABLE [dbo].[stock_reservations] ADD CONSTRAINT [FK61ri052f1yua0uymndfmexn4] FOREIGN KEY ([material_request_line_id]) REFERENCES [dbo].[material_request_lines] ([id]);
GO
ALTER TABLE [dbo].[stock_reservations] ADD CONSTRAINT [FKadmgnd316fdte197amsw2xx3i] FOREIGN KEY ([location_id]) REFERENCES [dbo].[storage_locations] ([id]);
GO
ALTER TABLE [dbo].[stock_reservations] ADD CONSTRAINT [FKd4shjpsv3n0xxwu5l30v4uytp] FOREIGN KEY ([material_request_id]) REFERENCES [dbo].[material_requests] ([id]);
GO
ALTER TABLE [dbo].[stock_reservations] ADD CONSTRAINT [FKlipghe3urcynub0ag28ey7vnq] FOREIGN KEY ([stock_balance_id]) REFERENCES [dbo].[stock_balances] ([id]);
GO
ALTER TABLE [dbo].[stock_reservations] ADD CONSTRAINT [FKlkqj3yfw6ndjv8lqfm7fjn9ij] FOREIGN KEY ([material_id]) REFERENCES [dbo].[materials] ([id]);
GO
ALTER TABLE [dbo].[stock_reservations] ADD CONSTRAINT [FKnojhmaduwmnxqk9fr736vs6do] FOREIGN KEY ([warehouse_id]) REFERENCES [dbo].[warehouses] ([id]);
GO
ALTER TABLE [dbo].[stock_transfer_lines] ADD CONSTRAINT [FKbu8q5c7bkmlf22woiug239t96] FOREIGN KEY ([batch_id]) REFERENCES [dbo].[material_batches] ([id]);
GO
ALTER TABLE [dbo].[stock_transfer_lines] ADD CONSTRAINT [FKju7dkdfofh2q8t01mj3hq586b] FOREIGN KEY ([material_id]) REFERENCES [dbo].[materials] ([id]);
GO
ALTER TABLE [dbo].[stock_transfer_lines] ADD CONSTRAINT [FKq5rwtp2putv9y3gwmywwm8l7l] FOREIGN KEY ([from_location_id]) REFERENCES [dbo].[storage_locations] ([id]);
GO
ALTER TABLE [dbo].[stock_transfer_lines] ADD CONSTRAINT [FKqgt9on5wiuohw46f00o0iylvu] FOREIGN KEY ([to_location_id]) REFERENCES [dbo].[storage_locations] ([id]);
GO
ALTER TABLE [dbo].[stock_transfer_lines] ADD CONSTRAINT [FKqj7ihemc7rqm9fulth6ecuj5o] FOREIGN KEY ([stock_transfer_id]) REFERENCES [dbo].[stock_transfers] ([id]);
GO
ALTER TABLE [dbo].[stock_transfers] ADD CONSTRAINT [FK1a7ck2ceraf7yvx8oyalg6qcl] FOREIGN KEY ([from_warehouse_id]) REFERENCES [dbo].[warehouses] ([id]);
GO
ALTER TABLE [dbo].[stock_transfers] ADD CONSTRAINT [FKtg575pm9p5bbyj3ki1ssffcpc] FOREIGN KEY ([to_warehouse_id]) REFERENCES [dbo].[warehouses] ([id]);
GO
ALTER TABLE [dbo].[storage_locations] ADD CONSTRAINT [FK94deej4le4g06iglvdj1oxobn] FOREIGN KEY ([warehouse_id]) REFERENCES [dbo].[warehouses] ([id]);
GO
ALTER TABLE [dbo].[storage_locations] ADD CONSTRAINT [FKdr4pthi47mt0b2cxprhqdxpd4] FOREIGN KEY ([parent_id]) REFERENCES [dbo].[storage_locations] ([id]);
GO
ALTER TABLE [dbo].[user_roles] ADD CONSTRAINT [FK_user_roles_users] FOREIGN KEY ([user_id]) REFERENCES [dbo].[users] ([id]);
GO
ALTER TABLE [dbo].[user_roles] ADD CONSTRAINT [FK_user_roles_roles] FOREIGN KEY ([role_id]) REFERENCES [dbo].[roles] ([id]);
GO
CREATE INDEX [idx_audit_created] ON [dbo].[audit_logs] ([created_at] ASC);
GO
CREATE INDEX [idx_batches_expiry] ON [dbo].[material_batches] ([expiry_date] ASC);
GO
CREATE INDEX [idx_batches_number] ON [dbo].[material_batches] ([batch_number] ASC);
GO
CREATE INDEX [idx_requests_status] ON [dbo].[material_requests] ([status] ASC);
GO
CREATE INDEX [idx_materials_name] ON [dbo].[materials] ([name] ASC);
GO
CREATE INDEX [idx_movements_created] ON [dbo].[stock_movements] ([created_at] ASC);
GO
CREATE UNIQUE INDEX [uk_storage_condition_material] ON [dbo].[storage_conditions] ([material_id] ASC);
GO
CREATE INDEX [idx_temperature_logs_recorded] ON [dbo].[temperature_logs] ([recorded_at] ASC);
GO
CREATE INDEX [idx_temperature_logs_warehouse] ON [dbo].[temperature_logs] ([warehouse_id] ASC);
GO
CREATE INDEX [IX_user_roles_role_id] ON [dbo].[user_roles] ([role_id]);
GO
