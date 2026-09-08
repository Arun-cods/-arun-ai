@echo off
title Push Arun AI to GitHub
cd /d "C:\Users\asus\OneDrive\Desktop\arun-ai"
set "PATH=%LOCALAPPDATA%\Programs\Git\cmd;%PATH%"
echo ========================================================
echo       Pushing Arun AI to GitHub (Arun-cods/-arun-ai)
echo ========================================================
echo.
git push -u origin main
echo.
echo ========================================================
echo Finished! Press any key to close.
echo ========================================================
pause