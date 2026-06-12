IF DB_ID(N'qlvt') IS NULL
BEGIN
    CREATE DATABASE qlvt;
END
GO

USE qlvt;
GO

-- Spring Boot se tu tao/cap nhat cac bang bang Hibernate ddl-auto=update.
-- File nay chi tao database moi cho du an QLVT.
