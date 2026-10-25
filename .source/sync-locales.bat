@echo off
setlocal

:: Get the directory where this batch file is located
set "ScriptDir=%~dp0"
set "ScriptDir=%ScriptDir:~0,-1%"

:: Get project root
set "ProjectRoot=%ScriptDir%\.."

:: Resolve to absolute path
for %%i in ("%ProjectRoot%") do set "ProjectRoot=%%~fi"

set "LangDir=%ProjectRoot%\src\main\resources\assets\toxicologistsdelight\lang"
set "SourceFile=%LangDir%\en_us.json"

echo Project Root: %ProjectRoot%
echo Lang Directory: %LangDir%
echo.

where attranslate >nul 2>nul
if errorlevel 1 (
    echo ERROR: attranslate not found on PATH. Install it with: npm install --global attranslate
    pause
    exit /b 1
)

if not exist "%SourceFile%" (
    echo ERROR: Source file not found at %SourceFile%
    pause
    exit /b 1
)

:: Space-separated list of target languages
set "TargetList=ru_ru.json rpr.json"
set "FailCount=0"

for %%t in (%TargetList%) do (
    echo Syncing %%t with en_us.json ...

    attranslate --srcFile="%SourceFile%" --srcLng=en --targetFile="%LangDir%\%%t" --targetLng=en --format=json --service=sync-without-translate

    if errorlevel 1 (
        echo WARNING: Failed to sync %%t
        set /a FailCount+=1
    ) else (
        echo Done: %%t
    )
    echo.
)

if %FailCount% GTR 0 (
    echo Finished with %FailCount% failure^(s^) - review the warnings above.
) else (
    echo All targets synced successfully.
)

pause
exit /b %FailCount%