IF OBJECT_ID(N'users', N'U') IS NOT NULL AND COL_LENGTH(N'users', N'visible_password') IS NULL
    ALTER TABLE users ADD visible_password nvarchar(120) NULL;

IF OBJECT_ID(N'users', N'U') IS NOT NULL AND COL_LENGTH(N'users', N'locked') IS NULL
    ALTER TABLE users ADD locked bit NOT NULL CONSTRAINT DF_users_locked DEFAULT 0;

IF OBJECT_ID(N'users', N'U') IS NOT NULL AND COL_LENGTH(N'users', N'deleted') IS NULL
    ALTER TABLE users ADD deleted bit NOT NULL CONSTRAINT DF_users_deleted DEFAULT 0;

IF OBJECT_ID(N'users', N'U') IS NOT NULL AND COL_LENGTH(N'users', N'created_at') IS NULL
    ALTER TABLE users ADD created_at datetime2 NULL;

IF OBJECT_ID(N'users', N'U') IS NOT NULL AND COL_LENGTH(N'users', N'updated_at') IS NULL
    ALTER TABLE users ADD updated_at datetime2 NULL;

IF OBJECT_ID(N'users', N'U') IS NOT NULL AND COL_LENGTH(N'users', N'email') IS NULL
    ALTER TABLE users ADD email varchar(160) NULL;

IF OBJECT_ID(N'users', N'U') IS NOT NULL AND COL_LENGTH(N'users', N'phone') IS NULL
    ALTER TABLE users ADD phone varchar(30) NULL;

IF OBJECT_ID(N'users', N'U') IS NOT NULL AND COL_LENGTH(N'users', N'visible_password') IS NOT NULL
    UPDATE users SET visible_password = '123456' WHERE visible_password IS NULL OR visible_password = '';
