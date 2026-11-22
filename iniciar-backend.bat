@echo off
setlocal
title ESFOCAR - Iniciar microservicios
cd /d "%~dp0backend"

echo ==========================================================
echo   ESFOCAR Estudio - Iniciando los 3 microservicios
echo ==========================================================
echo.

rem Spring Boot necesita Java 17 o superior. Si existe el JDK de Android Studio, se usa ese.
if exist "%ProgramFiles%\Android\Android Studio\jbr\bin\java.exe" set "JAVA_HOME=%ProgramFiles%\Android\Android Studio\jbr"
if defined JAVA_HOME (
    set "JAVA=%JAVA_HOME%\bin\java.exe"
) else (
    set "JAVA=java"
)
echo Java utilizado: %JAVA%
echo.

echo [1/2] Compilando los microservicios (la primera vez tarda un poco)...
call "%~dp0backend\gradlew.bat" bootJar -q
if errorlevel 1 (
    echo.
    echo ERROR: no se pudieron compilar los microservicios.
    echo Revisa que Android Studio este instalado o que JAVA_HOME apunte a Java 17 o superior.
    pause
    exit /b 1
)

echo [2/2] Iniciando cada microservicio en su propia ventana...
start "ms-contenidos (puerto 8083)" /d "%~dp0backend\ms-contenidos" "%JAVA%" -jar build\libs\ms-contenidos.jar
start "ms-apuntes (puerto 8081)" /d "%~dp0backend\ms-apuntes" "%JAVA%" -jar build\libs\ms-apuntes.jar
start "ms-resultados (puerto 8082)" /d "%~dp0backend\ms-resultados" "%JAVA%" -jar build\libs\ms-resultados.jar

echo.
echo Listo. Se abrieron 3 ventanas, una por microservicio.
echo Espera a que cada una muestre "Started Ms...Application" (unos 10 segundos).
echo.
echo   ms-contenidos  http://localhost:8083/api/contenidos/version
echo   ms-apuntes     http://localhost:8081/api/apuntes?alias=prueba
echo   ms-resultados  http://localhost:8082/api/resultados/resumen?alias=prueba
echo.
echo Para detenerlos: cierra esas ventanas o ejecuta detener-backend.bat
echo.
pause
