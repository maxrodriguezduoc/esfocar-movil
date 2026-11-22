@echo off
title ESFOCAR - Detener microservicios
echo Deteniendo los microservicios (puertos 8081, 8082 y 8083)...
for %%P in (8081 8082 8083) do (
    for /f "tokens=5" %%I in ('netstat -ano ^| findstr /r /c:":%%P .*LISTENING"') do (
        taskkill /PID %%I /F >nul 2>&1 && echo   Puerto %%P detenido
    )
)
echo Listo.
timeout /t 3 >nul
