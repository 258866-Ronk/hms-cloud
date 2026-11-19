@echo off
chcp 65001 >nul
title HMS 一键启动
set JAVA_HOME=C:\Java\jdk1.8.0_211
set PATH=%JAVA_HOME%\bin;%PATH%
cd /d %~dp0

if not exist logs mkdir logs

echo ==========================================================
echo   微服务架构医院挂号与问诊系统 —— 一键启动
echo ==========================================================
echo.
echo [0/9] 如未初始化数据库请先执行 docs\sql\init.sql
echo.

echo [1/9] 启动 Eureka 注册中心            http://localhost:8761
start "hms-eureka-server" /MIN cmd /c ""%JAVA_HOME%\bin\java.exe" -Xmx320m -jar hms-eureka-server\target\hms-eureka-server-1.0.0.jar > logs\eureka.log 2>&1"
timeout /t 22 >nul

echo [2/9] 启动 Config 配置中心            http://localhost:8888
start "hms-config-server" /MIN cmd /c ""%JAVA_HOME%\bin\java.exe" -Xmx256m -jar hms-config-server\target\hms-config-server-1.0.0.jar > logs\config.log 2>&1"
timeout /t 15 >nul

echo [3/9] 启动 Gateway 网关 + 前端页面     http://localhost:8500
start "hms-gateway-server" /MIN cmd /c ""%JAVA_HOME%\bin\java.exe" -Xmx320m -jar hms-gateway-server\target\hms-gateway-server-1.0.0.jar > logs\gateway.log 2>&1"
timeout /t 15 >nul

echo [4/9] 启动 用户/认证服务   8071
start "hms-user-service" /MIN cmd /c ""%JAVA_HOME%\bin\java.exe" -Xmx320m -jar hms-user-service\target\hms-user-service-1.0.0.jar > logs\user.log 2>&1"
timeout /t 10 >nul

echo [5/9] 启动 科室服务        8072
start "hms-department-service" /MIN cmd /c ""%JAVA_HOME%\bin\java.exe" -Xmx320m -jar hms-department-service\target\hms-department-service-1.0.0.jar > logs\department.log 2>&1"
timeout /t 10 >nul

echo [6/9] 启动 排班服务        8073
start "hms-schedule-service" /MIN cmd /c ""%JAVA_HOME%\bin\java.exe" -Xmx320m -jar hms-schedule-service\target\hms-schedule-service-1.0.0.jar > logs\schedule.log 2>&1"
timeout /t 10 >nul

echo [7/9] 启动 挂号服务        8074
start "hms-register-service" /MIN cmd /c ""%JAVA_HOME%\bin\java.exe" -Xmx320m -jar hms-register-service\target\hms-register-service-1.0.0.jar > logs\register.log 2>&1"
timeout /t 12 >nul

echo [8/9] 启动 病历服务        8075
start "hms-record-service" /MIN cmd /c ""%JAVA_HOME%\bin\java.exe" -Xmx320m -jar hms-record-service\target\hms-record-service-1.0.0.jar > logs\record.log 2>&1"
timeout /t 10 >nul

echo [9/9] 启动 Hystrix 监控面板 8070
start "hms-hystrix-dashboard" /MIN cmd /c ""%JAVA_HOME%\bin\java.exe" -Xmx256m -jar hms-hystrix-dashboard\target\hms-hystrix-dashboard-1.0.0.jar > logs\dashboard.log 2>&1"

timeout /t 20 >nul
echo.
echo ==========================================================
echo   全部服务启动完成！
echo   前端入口：   http://localhost:8500/
echo   注册中心：   http://localhost:8761/
echo   熔断监控：   http://localhost:8070/hystrix
echo               监控地址填 http://localhost:8074/actuator/hystrix.stream
echo   演示账号：   patient1 / D001 / admin   密码：123456
echo ==========================================================
echo.
start http://localhost:8500/
