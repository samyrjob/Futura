@echo off

jpackage ^
 --name "Futura_curls" ^
 --app-version "1.0" ^
 --input "out\production\Isometric2d\artifacts\Isometric2d_jar" ^
 --main-jar "Isometric2d.jar" ^
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
