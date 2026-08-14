@echo off
cd /d "%~dp0"
if exist ".venv\Scripts\python.exe" (
  ".venv\Scripts\python.exe" -m uvicorn app.main:app --reload --port 8000
) else (
  py -3 -m uvicorn app.main:app --reload --port 8000
)
