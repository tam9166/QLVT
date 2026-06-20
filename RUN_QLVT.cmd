@echo off
setlocal
cd /d "%~dp0"

if not exist "application-local.properties" if "%DB_PASSWORD%"=="" (
    echo [QLVT] Chua co cau hinh local cho SQL Server.
    echo [QLVT] Tao file application-local.properties tu application-local.properties.example
    echo [QLVT] hoac set bien moi truong DB_PASSWORD truoc khi chay.
    exit /b 1
)

call .\mvnw.cmd -Dmaven.test.skip=true spring-boot:run
endlocal
