@echo off
setlocal
cd /d "%~dp0"
if not exist bin mkdir bin

javac -encoding UTF-8 -d bin -sourcepath src -cp "lib\jlayer-1.0.1.jar" src\*.java
if errorlevel 1 exit /b 1

java -cp "bin;src;lib\jlayer-1.0.1.jar" ByteBound
