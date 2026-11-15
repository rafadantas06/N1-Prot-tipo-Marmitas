@echo off
chcp 65001 >nul
if not exist out mkdir out
dir /s /b src\*.java > fontes.txt
javac -encoding UTF-8 -d out @fontes.txt
if errorlevel 1 exit /b 1
java -Dfile.encoding=UTF-8 -cp out teste.TesteCenarioPrincipal
