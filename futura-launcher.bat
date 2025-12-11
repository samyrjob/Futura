@echo off
REM futura-launcher.bat - Place this in your Java Swing project folder

cd /d "C:\Users\notre\OneDrive - University of Chester\isometric project\Isometric2d"

REM %1 contains the full URL: futura://open?user=xxx&gender=yyy
java -cp "bin;lib/*" main.Main %1

REM Keep window open if there's an error (useful for debugging)
if %ERRORLEVEL% neq 0 pause