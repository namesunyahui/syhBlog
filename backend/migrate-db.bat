@echo off
chcp 65001 > nul
echo ========================================
echo 数据库迁移 - 添加用户字段
echo ========================================
echo.

set PGPASSWORD=qwer1234

echo 正在连接 PostgreSQL 数据库...
echo 数据库: syh_blog
echo 用户: syh
echo.

psql -h localhost -p 5432 -U syh -d syh_blog -f "src\main\resources\migration_add_user_fields.sql"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo 数据库迁移成功！
    echo ========================================
) else (
    echo.
    echo ========================================
    echo 数据库迁移失败！
    echo 请检查 PostgreSQL 是否正在运行
    echo ========================================
)

set PGPASSWORD=

pause
