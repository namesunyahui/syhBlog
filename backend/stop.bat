@echo off
chcp 65001 > nul
echo ========================================
echo 正在停止 Syh Blog 后端服务...
echo ========================================

for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080') do (
    echo 正在停止进程 %%a...
    taskkill /F /PID %%a 2>nul
)

echo 后端服务已停止
pause
