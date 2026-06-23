@echo off
REM ============================================================
REM  MossLuckyTuccar - Yerel kutuphane kurulumu
REM ============================================================
REM  ShopGUI+ API jar'i genel Maven depolarindan inmez.
REM  Bu script onu projedeki lib/ klasorunden yerel Maven
REM  deposuna (.m2) kurar. Internet GEREKTIRMEZ.
REM
REM  Projeyi ilk kez derlemeden once BIR KEZ calistirmaniz yeterli:
REM      install-libs.bat
REM  Ardindan normal sekilde derleyin:
REM      mvn clean package
REM ============================================================

cd /d "%~dp0"

echo ShopGUI+ API yerel Maven deposuna kuruluyor...
call mvn install:install-file ^
  -Dfile="lib/ShopGUIPlus-1.113.0.jar" ^
  -DgroupId=net.brcdev ^
  -DartifactId=shopgui ^
  -Dversion=1.113.0 ^
  -Dpackaging=jar

echo.
echo Bitti. Artik "mvn clean package" calistirabilirsiniz.
pause
