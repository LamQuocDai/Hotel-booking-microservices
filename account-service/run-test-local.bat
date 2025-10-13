@echo off
echo Starting Account Service in TEST mode (no database/redis required)...
echo.
echo This will use:
echo - H2 in-memory database
echo - No Redis connection
echo - Swagger UI enabled at http://localhost:3002/swagger-ui.html
echo - Health check at http://localhost:3002/health
echo.

REM Try different ways to run the application
if exist mvnw.cmd (
    echo Using Maven wrapper...
    mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=test
) else if exist mvn.cmd (
    echo Using system Maven...
    mvn spring-boot:run -Dspring-boot.run.profiles=test
) else (
    echo Maven not found. Please run from IntelliJ IDEA with VM options:
    echo -Dspring.profiles.active=test
    pause
)
