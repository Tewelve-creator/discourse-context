@echo off
setlocal enabledelayedexpansion

set "ROOT=%~dp0"
set "DEPS=%ROOT%.deps"
set "CLASSES=%ROOT%target\classes"
set "JACKSON_VER=2.17.2"
set "BASE=https://repo1.maven.org/maven2/com/fasterxml/jackson"

if not exist "%DEPS%" mkdir "%DEPS%"
if not exist "%CLASSES%" mkdir "%CLASSES%"

call :download "%BASE%/core/jackson-core/%JACKSON_VER%/jackson-core-%JACKSON_VER%.jar" "%DEPS%\jackson-core-%JACKSON_VER%.jar"
call :download "%BASE%/core/jackson-databind/%JACKSON_VER%/jackson-databind-%JACKSON_VER%.jar" "%DEPS%\jackson-databind-%JACKSON_VER%.jar"
call :download "%BASE%/core/jackson-annotations/%JACKSON_VER%/jackson-annotations-%JACKSON_VER%.jar" "%DEPS%\jackson-annotations-%JACKSON_VER%.jar"

echo Compiling...
dir /s /b "%ROOT%src\main\java\*.java" > "%ROOT%sources.txt"
javac -encoding UTF-8 -cp "%DEPS%\*" -d "%CLASSES%" @sources.txt
if errorlevel 1 exit /b 1

echo Copying resources...
xcopy /s /y /i "%ROOT%src\main\resources\*" "%CLASSES%\" >nul

echo Starting LinguaPulse Web...
start "" "http://localhost:8090"
java -cp "%CLASSES%;%DEPS%\*" org.linguapulse.LinguaPulseWebApp
goto :eof

:download
if exist "%~2" goto :eof
echo Downloading %~1 ...
powershell -NoProfile -Command "Invoke-WebRequest -Uri '%~1' -OutFile '%~2'"
goto :eof
