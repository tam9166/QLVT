-- Run with sqlcmd variable, for example:
-- sqlcmd -S localhost -i database/fix-login-user.sql -v QLVT_SQL_PASSWORD="your-strong-password"

USE master;
GO

IF NOT EXISTS (SELECT * FROM sys.sql_logins WHERE name = 'tam')
BEGIN
    CREATE LOGIN tam WITH PASSWORD = '$(QLVT_SQL_PASSWORD)';
END
ELSE
BEGIN
    ALTER LOGIN tam WITH PASSWORD = '$(QLVT_SQL_PASSWORD)';
    ALTER LOGIN tam ENABLE;
END
GO

IF DB_ID('QLVT') IS NULL
BEGIN
    CREATE DATABASE QLVT;
END
GO

USE QLVT;
GO

IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'tam')
   AND NOT EXISTS (
       SELECT 1
       FROM sys.database_principals
       WHERE name = 'dbo'
         AND sid = SUSER_SID('tam')
   )
BEGIN
    CREATE USER tam FOR LOGIN tam;
END
GO

IF EXISTS (SELECT * FROM sys.database_principals WHERE name = 'tam')
BEGIN
    ALTER ROLE db_owner ADD MEMBER tam;
END
GO
