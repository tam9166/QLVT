IF DB_ID(N'QLVT') IS NULL
BEGIN
    CREATE DATABASE [QLVT];
END
GO

USE [QLVT];
GO

-- Spring Boot dev profile validates this schema with spring.jpa.hibernate.ddl-auto=validate.
-- Run these scripts in order for a clean database:
--   database/01_create_schema.sql
--   database/02_seed_master_data.sql
--   database/03_seed_demo_data.sql
--
-- In SSMS, open each file and execute it against SQL Server.
-- In sqlcmd mode you may also run the ordered files manually with :r commands.
