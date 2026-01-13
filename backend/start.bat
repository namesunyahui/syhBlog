@echo off
chcp 65001 > nul
echo ========================================
echo 正在启动 Syh Blog 后端服务...
echo ========================================

cd /d "%~dp0"
mvn spring-boot:run

pause
