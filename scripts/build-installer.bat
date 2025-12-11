@echo off

jpackage ^
 --name "Futura_curly" ^
 --app-version "1.0" ^
 --input "" ^
 --main-jar "MyApp.jar" ^
 --main-class "main.Main" ^
 --dest "installer_output" ^
 --vendor "Ankillous" ^
 --icon "src\res\futura.ico" ^
 --type msi ^
 --win-shortcut ^
 --win-menu ^
 --win-dir-chooser ^
 --add-modules java.desktop

call register_url.bat
