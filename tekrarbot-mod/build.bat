@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo === Takip Modu derleme ===
set NEED_RESTART=0

rem --- Java 25 kontrolu ---
java -version 2>&1 | findstr /C:"\"25" >nul
if errorlevel 1 (
  echo Java 25 bulunamadi, kuruluyor...
  winget install -e --id EclipseAdoptium.Temurin.25.JDK --accept-package-agreements --accept-source-agreements
  set NEED_RESTART=1
)

rem --- Gradle kontrolu ---
where gradle >nul 2>nul
if errorlevel 1 (
  echo Gradle bulunamadi, kuruluyor...
  winget install -e --id Gradle.Gradle --accept-package-agreements --accept-source-agreements
  set NEED_RESTART=1
)

if "%NEED_RESTART%"=="1" (
  echo.
  echo Kurulumlar tamamlandi. Bu pencereyi kapat ve build.bat dosyasini TEKRAR calistir.
  pause
  exit /b 0
)

echo Derleniyor, ilk seferde birkac dakika surebilir...
call gradle build --no-daemon
if errorlevel 1 (
  echo.
  echo HATA: Derleme basarisiz. Yukaridaki hata mesajinin son satirlarini kopyalayip bana gonder.
  pause
  exit /b 1
)

if not exist HAZIR mkdir HAZIR
copy /y build\libs\tekrarbot-1.0.0.jar HAZIR\ >nul
echo.
echo TAMAM! HAZIR klasorundeki tekrarbot-1.0.0.jar dosyasini .minecraft\mods klasorune at.
echo Fabric API jar'ini (fabric-api-0.165.0+26.3) ayrica indirip ayni klasore koymayi unutma.
pause
