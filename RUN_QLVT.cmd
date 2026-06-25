@echo off
setlocal
cd /d "%~dp0"

if "%DB_PASSWORD%"=="" (
  echo Missing DB_PASSWORD.
  echo Example: set DB_PASSWORD=123456
  pause
  exit /b 1
)

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\run-dev.ps1"
set EXIT_CODE=%ERRORLEVEL%
pause
exit /b %EXIT_CODE%
