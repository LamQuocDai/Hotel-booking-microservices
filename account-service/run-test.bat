@echo off
echo Starting Account Service with Test Profile...
echo Using H2 in-memory database (no MySQL/Redis required)
echo.

set SPRING_PROFILES_ACTIVE=test
set SERVER_PORT=3002

echo Profile: %SPRING_PROFILES_ACTIVE%
echo Port: %SERVER_PORT%
echo.

REM Try to find Java
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Java not found in PATH
    echo Please install Java 17+ or add it to your PATH
    pause
    exit /b 1
)

echo Java found. Starting application...
echo.

REM Run the main class directly
java -cp "target/classes;target/dependency/*" ^
     -Dspring.profiles.active=test ^
     -Dserver.port=3002 ^
     com.hotelbooking.account.AccountServiceApplication

pause
