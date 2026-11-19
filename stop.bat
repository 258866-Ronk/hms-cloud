@echo off
title HMS Cloud Stop
setlocal
rem NOTE: keep this file pure ASCII, do NOT use chcp here.
echo ============================================================
echo   Stopping all hms-cloud microservices - by port
echo ============================================================
echo.
for %%p in (8761 8888 8500 8071 8072 8073 8074 8075 8070) do (
  for /f "tokens=5" %%a in ('netstat -ano ^| findstr "LISTENING" ^| findstr ":%p% "') do (
    echo   [STOP] port %%p -^> PID %%a
    taskkill /PID %%a /F >nul 2>&1
  )
)
echo.
echo Stop signals sent. Kill leftover java.exe manually if needed.
echo.
pause
