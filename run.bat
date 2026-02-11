@echo off
REM ============================================================
REM AccountingERP - Quick Commands Batch File (Windows)
REM ============================================================
REM Usage: Run this file and select options
REM ============================================================

setlocal enabledelayedexpansion

:MENU
cls
echo.
echo ============================================================
echo    AccountingERP - Quick Commands
echo ============================================================
echo.
echo 1. Build Project (mvn clean install)
echo 2. Build without tests (mvn clean install -DskipTests)
echo 3. Run in Dev Mode (H2 Database)
echo 4. Run in Prod Mode (PostgreSQL)
echo 5. Run on different port (e.g., 9000)
echo 6. Clean project (mvn clean)
echo 7. Run tests
echo 8. Show Maven info
echo 9. Exit
echo.
set /p choice="Select option (1-9): "

if "%choice%"=="1" goto BUILD
if "%choice%"=="2" goto BUILD_SKIP
if "%choice%"=="3" goto RUN_DEV
if "%choice%"=="4" goto RUN_PROD
if "%choice%"=="5" goto RUN_CUSTOM_PORT
if "%choice%"=="6" goto CLEAN
if "%choice%"=="7" goto TEST
if "%choice%"=="8" goto INFO
if "%choice%"=="9" goto EXIT

echo Invalid choice!
pause
goto MENU

:BUILD
echo.
echo Building project with tests...
mvn clean install
pause
goto MENU

:BUILD_SKIP
echo.
echo Building project (skipping tests)...
mvn clean install -DskipTests
pause
goto MENU

:RUN_DEV
echo.
echo Starting application in Dev Mode (H2 Database)...
echo Access: http://localhost:8080
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
goto MENU

:RUN_PROD
echo.
echo Starting application in Prod Mode (PostgreSQL)...
echo Make sure PostgreSQL is running!
echo Access: http://localhost:8080
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
goto MENU

:RUN_CUSTOM_PORT
echo.
set /p port="Enter port number (default 8080): "
if "!port!"=="" set port=8080
echo Starting on port !port!...
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=!port! --spring.profiles.active=dev"
goto MENU

:CLEAN
echo.
echo Cleaning project...
mvn clean
pause
goto MENU

:TEST
echo.
echo Running tests...
mvn test
pause
goto MENU

:INFO
echo.
echo Java version:
java -version
echo.
echo Maven version:
mvn -v
echo.
pause
goto MENU

:EXIT
echo Goodbye!
exit /b 0
