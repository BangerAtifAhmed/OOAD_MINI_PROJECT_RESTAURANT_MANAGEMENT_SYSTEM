@echo off
echo === FoodieFlow Build ===

:: Create output directory
if not exist out mkdir out

:: Compile all Java sources
echo Compiling...
javac -cp "lib\*" -sourcepath src -d out ^
  src\Main.java ^
  src\model\*.java ^
  src\model\decorator\*.java ^
  src\model\state\*.java ^
  src\model\payment\*.java ^
  src\dao\*.java ^
  src\singleton\*.java ^
  src\controller\*.java ^
  src\view\*.java

if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo Compilation successful!
echo.
echo Running FoodieFlow...
java -cp "out;lib\*" Main

pause
