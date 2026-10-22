@echo off
setlocal
set "DIR=%~dp0"
if exist "%DIR%apache-maven-3.9.6\bin\mvn.cmd" (
    "%DIR%apache-maven-3.9.6\bin\mvn.cmd" %*
) else (
    mvn %*
)
