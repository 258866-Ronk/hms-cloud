@echo off
title HMS Cloud - One Click Start
setlocal

rem ============================================================
rem  Hospital Registration System (hms-cloud) - self-contained starter
rem  Rules for reliability:
rem   - pure ASCII only, never use chcp
rem   - NO call :subroutines (they caused cmd to skip lines on Win11)
rem   - straight-line main flow with goto loops only
rem ============================================================

set "PROJ=%~dp0"
if not "%PROJ:~-1%"=="\" set "PROJ=%PROJ%\"
set "PROJF=%PROJ:\=/%"
cd /d "%PROJ%"
if not exist logs mkdir logs

echo ============================================================
echo   Hospital Registration System - One Click Start
echo   Project: %PROJ%
echo ============================================================
echo.

rem ---------- 1. Locate JDK ----------
set "JAVA_EXE="
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
if not defined JAVA_EXE if exist "C:\Java\jdk1.8.0_211\bin\java.exe" set "JAVA_EXE=C:\Java\jdk1.8.0_211\bin\java.exe"
if not defined JAVA_EXE if exist "C:\Program Files\Java\jdk-8\bin\java.exe" set "JAVA_EXE=C:\Program Files\Java\jdk-8\bin\java.exe"
if not defined JAVA_EXE for /f "delims=" %%i in ('where java 2^>nul') do if not defined JAVA_EXE set "JAVA_EXE=%%i"
if not defined JAVA_EXE (
  echo [ERROR] Java 8 not found. Install JDK 8 and set JAVA_HOME, then retry.
  pause
  exit /b 1
)
echo [OK] JDK: %JAVA_EXE%

rem ---------- 2. Locate Maven ----------
set "MVN_EXE="
if exist "%PROJ%mvnw.cmd" set "MVN_EXE=%PROJ%mvnw.cmd"
if not defined MVN_EXE for /f "delims=" %%i in ('where mvn.cmd 2^>nul') do if not defined MVN_EXE set "MVN_EXE=%%i"
if not defined MVN_EXE for /f "delims=" %%i in ('where mvn 2^>nul') do if not defined MVN_EXE set "MVN_EXE=%%i"
if defined MVN_EXE (echo [OK] Maven: %MVN_EXE%) else (echo [WARN] Maven not found - auto-build disabled.)

rem ---------- 3. Ensure config-repo is a git repo ----------
if exist "%PROJ%config-repo\.git" (
  echo [OK] config-repo git repo found
) else (
  echo [*] Initializing config-repo as local git repo...
  pushd "%PROJ%config-repo"
  git init -q 2>nul
  git symbolic-ref HEAD refs/heads/master 2>nul
  git add -A 2>nul
  git -c user.email=hms@local -c user.name=hms commit -q -m init 2>nul
  popd
  echo [OK] config-repo initialized
)

rem ---------- 4. MySQL ----------
echo [*] Checking MySQL service ...
set "MYSQL_SVC="
for /f "tokens=2" %%s in ('sc query state^= all ^| findstr /i "SERVICE_NAME" ^| findstr /i "MySQL MariaDB"') do set "MYSQL_SVC=%%s"
if defined MYSQL_SVC (
  sc query "%MYSQL_SVC%" | findstr "RUNNING" >nul
  if errorlevel 1 (
    echo   [*] Starting MySQL %MYSQL_SVC% ...
    net start "%MYSQL_SVC%" >nul 2>&1
  )
)
netstat -ano | findstr "LISTENING" | findstr ":3306 " >nul
if errorlevel 1 (
  echo   [WARN] MySQL port 3306 not listening. If services fail, start MySQL manually.
) else (
  echo   [OK] MySQL is running
)

rem ---------- 5. Build jars if missing ----------
set "NEED_BUILD=0"
for %%m in (hms-eureka-server hms-config-server hms-gateway-server hms-user-service hms-department-service hms-schedule-service hms-register-service hms-record-service hms-hystrix-dashboard) do (
  if not exist "%PROJ%%%m\target\*.jar" set "NEED_BUILD=1"
)
if "%NEED_BUILD%"=="1" (
  if defined MVN_EXE (
    echo [!] Some service jars are missing. Building with Maven, please wait...
    call "%MVN_EXE%" -q clean package -DskipTests
    if errorlevel 1 (
      echo [ERROR] Maven build failed. Run manually: mvn clean package -DskipTests
      pause
      exit /b 1
    )
    echo [OK] Build finished
  ) else (
    echo [ERROR] Jars missing and Maven not found. Run: mvn clean package -DskipTests
    pause
    exit /b 1
  )
) else (
  echo [OK] All service jars are ready
)

rem ---------- 6. Clean slate: stop old instances on our ports ----------
echo [*] Stopping any old service instances ...
for %%p in (8761 8888 8500 8071 8072 8073 8074 8075 8070) do (
  for /f "tokens=5" %%a in ('netstat -ano ^| findstr "LISTENING" ^| findstr ":%%p "') do taskkill /PID %%a /F >nul 2>&1
)
ping -n 3 127.0.0.1 >nul

rem ---------- 7. Resolve jar paths ----------
set "MISSING="
set "JAR_EUREKA="
for /f "delims=" %%j in ('dir /b "%PROJ%hms-eureka-server\target\hms-eureka-server-*.jar" 2^>nul') do set "JAR_EUREKA=%PROJ%hms-eureka-server\target\%%j"
if not defined JAR_EUREKA set "MISSING=%MISSING% hms-eureka-server"
set "JAR_CONFIG="
for /f "delims=" %%j in ('dir /b "%PROJ%hms-config-server\target\hms-config-server-*.jar" 2^>nul') do set "JAR_CONFIG=%PROJ%hms-config-server\target\%%j"
if not defined JAR_CONFIG set "MISSING=%MISSING% hms-config-server"
set "JAR_GATEWAY="
for /f "delims=" %%j in ('dir /b "%PROJ%hms-gateway-server\target\hms-gateway-server-*.jar" 2^>nul') do set "JAR_GATEWAY=%PROJ%hms-gateway-server\target\%%j"
if not defined JAR_GATEWAY set "MISSING=%MISSING% hms-gateway-server"
set "JAR_USER="
for /f "delims=" %%j in ('dir /b "%PROJ%hms-user-service\target\hms-user-service-*.jar" 2^>nul') do set "JAR_USER=%PROJ%hms-user-service\target\%%j"
if not defined JAR_USER set "MISSING=%MISSING% hms-user-service"
set "JAR_DEPT="
for /f "delims=" %%j in ('dir /b "%PROJ%hms-department-service\target\hms-department-service-*.jar" 2^>nul') do set "JAR_DEPT=%PROJ%hms-department-service\target\%%j"
if not defined JAR_DEPT set "MISSING=%MISSING% hms-department-service"
set "JAR_SCHED="
for /f "delims=" %%j in ('dir /b "%PROJ%hms-schedule-service\target\hms-schedule-service-*.jar" 2^>nul') do set "JAR_SCHED=%PROJ%hms-schedule-service\target\%%j"
if not defined JAR_SCHED set "MISSING=%MISSING% hms-schedule-service"
set "JAR_REG="
for /f "delims=" %%j in ('dir /b "%PROJ%hms-register-service\target\hms-register-service-*.jar" 2^>nul') do set "JAR_REG=%PROJ%hms-register-service\target\%%j"
if not defined JAR_REG set "MISSING=%MISSING% hms-register-service"
set "JAR_REC="
for /f "delims=" %%j in ('dir /b "%PROJ%hms-record-service\target\hms-record-service-*.jar" 2^>nul') do set "JAR_REC=%PROJ%hms-record-service\target\%%j"
if not defined JAR_REC set "MISSING=%MISSING% hms-record-service"
set "JAR_DASH="
for /f "delims=" %%j in ('dir /b "%PROJ%hms-hystrix-dashboard\target\hms-hystrix-dashboard-*.jar" 2^>nul') do set "JAR_DASH=%PROJ%hms-hystrix-dashboard\target\%%j"
if not defined JAR_DASH set "MISSING=%MISSING% hms-hystrix-dashboard"
if defined MISSING (
  echo [ERROR] jars not found:%MISSING%
  echo Run: mvn clean package -DskipTests   then retry.
  pause
  exit /b 1
)

rem ---------- 8. Start Eureka and wait for 8761 ----------
echo.
echo [1/3] Starting Eureka on port 8761 ...
start "hms-eureka-server" /MIN cmd /c ""%JAVA_EXE%" -Xmx320m -jar "%JAR_EUREKA%" > "%PROJ%logs\hms-eureka-server.log" 2>&1"
set /a CNT=0
:W1
netstat -ano | findstr "LISTENING" | findstr ":8761 " >nul
if not errorlevel 1 goto U1
ping -n 3 127.0.0.1 >nul
set /a CNT+=1
if %CNT% GEQ 30 goto T1
goto W1
:U1
echo   [OK] eureka is up
goto S2
:T1
echo   [WARN] eureka not ready after ~90s, continuing anyway - check logs\hms-eureka-server.log
:S2

rem ---------- 9. Start Config Server and wait for 8888 ----------
echo [2/3] Starting Config Server on port 8888 ...
start "hms-config-server" /MIN cmd /c ""%JAVA_EXE%" -Xmx256m "-Dspring.cloud.config.server.git.uri=file:///%PROJF%config-repo" -jar "%JAR_CONFIG%" > "%PROJ%logs\hms-config-server.log" 2>&1"
set /a CNT=0
:W2
netstat -ano | findstr "LISTENING" | findstr ":8888 " >nul
if not errorlevel 1 goto U2
ping -n 3 127.0.0.1 >nul
set /a CNT+=1
if %CNT% GEQ 20 goto T2
goto W2
:U2
echo   [OK] config-server is up
goto S3
:T2
echo   [WARN] config-server not ready after ~60s, continuing anyway - check logs\hms-config-server.log
:S3

rem ---------- 10. Start Gateway + 6 business services ----------
echo [3/3] Starting Gateway and all business services ...
start "hms-gateway-server" /MIN cmd /c ""%JAVA_EXE%" -Xmx320m -jar "%JAR_GATEWAY%" > "%PROJ%logs\hms-gateway-server.log" 2>&1"
start "hms-user-service" /MIN cmd /c ""%JAVA_EXE%" -Xmx320m -jar "%JAR_USER%" > "%PROJ%logs\hms-user-service.log" 2>&1"
start "hms-department-service" /MIN cmd /c ""%JAVA_EXE%" -Xmx256m -jar "%JAR_DEPT%" > "%PROJ%logs\hms-department-service.log" 2>&1"
start "hms-schedule-service" /MIN cmd /c ""%JAVA_EXE%" -Xmx256m -jar "%JAR_SCHED%" > "%PROJ%logs\hms-schedule-service.log" 2>&1"
start "hms-register-service" /MIN cmd /c ""%JAVA_EXE%" -Xmx320m -jar "%JAR_REG%" > "%PROJ%logs\hms-register-service.log" 2>&1"
start "hms-record-service" /MIN cmd /c ""%JAVA_EXE%" -Xmx256m -jar "%JAR_REC%" > "%PROJ%logs\hms-record-service.log" 2>&1"
start "hms-hystrix-dashboard" /MIN cmd /c ""%JAVA_EXE%" -Xmx256m -jar "%JAR_DASH%" > "%PROJ%logs\hms-hystrix-dashboard.log" 2>&1"

echo   [*] 7 services launching, waiting for Gateway port 8500 ...
set /a CNT=0
:W3
netstat -ano | findstr "LISTENING" | findstr ":8500 " >nul
if not errorlevel 1 goto U3
ping -n 3 127.0.0.1 >nul
set /a CNT+=1
if %CNT% GEQ 40 goto T3
goto W3
:U3
echo   [OK] gateway is up
goto S4
:T3
echo   [WARN] gateway not ready after ~120s - check logs\hms-gateway-server.log
:S4

rem ---------- 11. Summary ----------
echo.
echo   Give services ~30s more to register with Eureka on first load.
echo ============================================================
echo   STARTUP SEQUENCE FINISHED
echo   Web UI:    http://localhost:8500/
echo   Eureka:    http://localhost:8761/
echo   Hystrix:   http://localhost:8070/hystrix
echo   Demo:      patient1 / D001-D008 / admin   password: 123456
echo   Stop all:  stop.bat next to this file
echo   Logs:      logs\ in this folder
echo ============================================================
echo.
if exist "%PROJ%logs\hms-gateway-server.log" if "%CNT%"=="0" echo   Tip: all ports came up on first check.
start "" http://localhost:8500/
echo Press any key to close this window - services keep running...
pause >nul
exit /b 0
