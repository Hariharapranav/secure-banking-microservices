@echo off
title Secure Banking System - Stop All Services
color 0C

echo ============================================================
echo    Secure Banking System - Stopping All Services
echo ============================================================
echo.

echo Stopping all Java Spring Boot processes...

:: Kill all Java processes running Spring Boot
for /f "tokens=1" %%p in ('wmic process where "commandline like '%%spring-boot%%' and name='java.exe'" get processid 2^>nul ^| findstr /r "[0-9]"') do (
    echo Stopping process: %%p
    taskkill /PID %%p /F >nul 2>nul
)

:: Also kill by window titles
taskkill /FI "WINDOWTITLE eq Auth Service*" /F >nul 2>nul
taskkill /FI "WINDOWTITLE eq User Service*" /F >nul 2>nul
taskkill /FI "WINDOWTITLE eq Account Service*" /F >nul 2>nul
taskkill /FI "WINDOWTITLE eq Transaction Service*" /F >nul 2>nul
taskkill /FI "WINDOWTITLE eq Notification Service*" /F >nul 2>nul
taskkill /FI "WINDOWTITLE eq API Gateway*" /F >nul 2>nul

echo.
echo All services stopped!
echo.
pause
