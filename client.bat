    @echo off
    title MAIL SYSTEM LAUNCHER

    cd /d C:\Users\LENOVO\MailServer

    echo ========================================
    echo             MAIL SYSTEM
    echo ========================================
    echo.

    echo [1/2] Starting Mail Server...
    start "MAIL SERVER" cmd /k mvn exec:java "-Dexec.mainClass=com.mailserver.server.ServerGUI"

    echo Waiting for server to start...
    timeout /t 2 /nobreak >nul

    echo [2/2] Starting Mail Client...
    start "MAIL CLIENT" cmd /k mvn exec:java "-Dexec.mainClass=com.mailserver.client.MailClientGUI"

    echo.
    echo Both windows launched.
    pause