@echo off
title Mail Server System
color 0A

echo ==========================================
echo        MAIL SERVER SYSTEM
echo ==========================================
echo.
echo [1] Starting Mail Server...
start "MAIL SERVER" cmd /k "mvn exec:java -Dexec.mainClass=com.mailserver.server.MailServer"

timeout /t 2 /nobreak >nul

echo [2] Starting Server GUI...
start "SERVER GUI" cmd /k "mvn exec:java -Dexec.mainClass=com.mailserver.server.ServerGUI"

timeout /t 2 /nobreak >nul

echo [3] Starting Mail Client GUI...
start "MAIL CLIENT GUI" cmd /k "mvn exec:java -Dexec.mainClass=com.mailserver.client.MailClientGUI"

timeout /t 2 /nobreak >nul

echo [4] Starting Mail Client GUI...
start "MAIL CLIENT GUI" cmd /k "mvn exec:java -Dexec.mainClass=com.mailserver.server.SMTPServer"


timeout /t 2 /nobreak >nul

echo [5] Starting Mail Client GUI...
start "MAIL CLIENT GUI" cmd /k "mvn exec:java -Dexec.mainClass=com.mailserver.server.POP3Server"

echo.
echo ==========================================
echo All components have been started!
echo ==========================================
echo.
pause