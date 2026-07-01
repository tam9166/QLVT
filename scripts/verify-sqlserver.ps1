param(
    [string]$Server = $(if ($env:QLVT_SQL_SERVER) { $env:QLVT_SQL_SERVER } else { "localhost" }),
    [string]$Database = $(if ($env:QLVT_SQL_DATABASE) { $env:QLVT_SQL_DATABASE } else { "QLVT" }),
    [string]$Username = $(if ($env:DB_USERNAME) { $env:DB_USERNAME } else { "tam" }),
    [string]$Password = $env:DB_PASSWORD
)

$ErrorActionPreference = "Stop"

function Fail($Message) {
    Write-Error $Message
    exit 1
}

if (-not (Get-Command sqlcmd -ErrorAction SilentlyContinue)) {
    Fail "Khong tim thay sqlcmd. Hay cai Microsoft SQL Server Command Line Utilities hoac them sqlcmd vao PATH."
}

if ([string]::IsNullOrWhiteSpace($Password)) {
    Fail "Thieu DB_PASSWORD. Dat bien moi truong truoc khi chay: `$env:DB_PASSWORD='mat-khau-sql-server'"
}

Write-Host "Kiem tra SQL Server cho QLVT..."
Write-Host "Server   : $Server"
Write-Host "Database : $Database"
Write-Host "Username : $Username"

$query = @"
SET NOCOUNT ON;
SELECT
    DB_NAME() AS db_name,
    SUSER_SNAME() AS login_name,
    USER_NAME() AS user_name,
    CONVERT(nvarchar(128), SERVERPROPERTY('InstanceName')) AS instance_name,
    CONVERT(int, SERVERPROPERTY('IsIntegratedSecurityOnly')) AS windows_auth_only;
"@

try {
    sqlcmd -S $Server -U $Username -P $Password -d $Database -b -Q $query
    if ($LASTEXITCODE -ne 0) {
        throw "sqlcmd exited with code $LASTEXITCODE"
    }
} catch {
    Fail @"
Khong ket noi duoc SQL Server bang cau hinh hien tai.
Kiem tra lai:
- SQL Server dang chay va TCP/IP da bat.
- DB_URL/Server dung instance va port.
- Database $Database da ton tai.
- Login $Username ton tai, khong bi disable va password dung.
- User $Username co quyen tren database $Database.

Loi goc: $($_.Exception.Message)
"@
}

Write-Host "Ket noi SQL Server thanh cong."
