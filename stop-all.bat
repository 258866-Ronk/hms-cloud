@echo off
chcp 65001 >nul
title HMS 一键停止
echo 正在停止全部微服务...
for %%t in (hms-eureka-server hms-config-server hms-gateway-server hms-user-service hms-department-service hms-schedule-service hms-register-service hms-record-service hms-hystrix-dashboard) do (
  taskkill /FI "WINDOWTITLE eq %%t" /F /T >nul 2>&1
)
echo 已发送停止指令，若仍有残留可执行：taskkill /F /IM java.exe
timeout /t 3 >nul
