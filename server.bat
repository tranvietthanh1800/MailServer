@echo off

title MAIL SERVER

cd /d C:\Users\LENOVO\MailServer

echo ========================================
echo          MAIL SERVER SYSTEM
echo ========================================
echo.

mvn exec:java "-Dexec.mainClass=com.mailserver.Main"

pause