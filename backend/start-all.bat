@echo off
title Secure Banking System - Service Launcher
color 0A

echo ============================================================
echo    Secure Banking System - Starting All Services
echo ============================================================
echo.

:: Check if Maven is available
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Maven is not installed or not in PATH!
    echo Please install Maven and add it to your system PATH.
    pause
    exit /b 1
)

echo [1/6] Starting Auth Service (port 8081)...
start "Auth Service :8081" cmd /k "cd /d %~dp0auth-service && mvn spring-boot:run"
timeout /t 15 /nobreak >nul

echo [2/6] Starting User Service (port 8082)...
start "User Service :8082" cmd /k "cd /d %~dp0user-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

echo [3/6] Starting Account Service (port 8083)...
start "Account Service :8083" cmd /k "cd /d %~dp0account-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

echo [4/6] Starting Transaction Service (port 8084)...
start "Transaction Service :8084" cmd /k "cd /d %~dp0transaction-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

echo [5/6] Starting Notification Service (port 8085)...
start "Notification Service :8085" cmd /k "cd /d %~dp0notification-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

echo [6/6] Starting API Gateway (port 8080)...
start "API Gateway :8080" cmd /k "cd /d %~dp0api-gateway && mvn spring-boot:run"

echo.
echo ============================================================
echo    All services are starting!
echo ============================================================
echo.
echo    API Gateway:           http://localhost:8080
echo    Auth Service:          http://localhost:8081
echo    User Service:          http://localhost:8082
echo    Account Service:       http://localhost:8083
echo    Transaction Service:   http://localhost:8084
echo    Notification Service:  http://localhost:8085
echo.
echo    Swagger UI (Auth):     http://localhost:8081/swagger-ui.html
echo.
echo    Wait ~30-60 seconds for all services to fully start.
echo    Each service runs in its own window.
echo ============================================================
echo.
pause
