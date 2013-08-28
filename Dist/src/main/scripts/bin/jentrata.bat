@echo off

rem Jentrata startup Windows script

if "%OS%" == "Windows_NT" setlocal

rem Guess JENTRATA_HOME if not defined
set "CURRENT_DIR=%cd%"
if not "%JENTRATA_HOME%" == "" goto gotHome
set "JENTRATA_HOME=%CURRENT_DIR%"
if exist "%JENTRATA_HOME%\bin\jentrata.bat" goto okHome
cd ..
set "JENTRATA_HOME=%cd%"
cd "%CURRENT_DIR%"
:gotHome

if exist "%JENTRATA_HOME%\bin\jentrata.bat" goto okHome
echo The JENTRATA_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end
:okHome

rem Copy JENTRATA_BASE from JENTRATA_HOME if not defined
if not "%JENTRATA_BASE%" == "" goto gotBase
set "JENTRATA_BASE=%JENTRATA_HOME%"
:gotBase

rem Get standard environment variables
if not exist "%JENTRATA_BASE%\bin\setenv.bat" goto checkSetenvHome
call "%JENTRATA_BASE%\bin\setenv.bat"
goto setenvDone
:checkSetenvHome
if exist "%JENTRATA_HOME%\bin\setenv.bat" call "%JENTRATA_HOME%\bin\setenv.bat"
:setenvDone

rem Make sure prerequisite environment variables are set

rem In debug mode we need a real JDK (JAVA_HOME)
if ""%1"" == ""debug"" goto needJavaHome

rem Otherwise either JRE or JDK are fine
if not "%JRE_HOME%" == "" goto gotJreHome
if not "%JAVA_HOME%" == "" goto gotJavaHome
echo Neither the JAVA_HOME nor the JRE_HOME environment variable is defined
echo At least one of these environment variable is needed to run this program
goto exit

:needJavaHome
rem Check if we have a usable JDK
if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if not exist "%JAVA_HOME%\bin\javaw.exe" goto noJavaHome
if not exist "%JAVA_HOME%\bin\jdb.exe" goto noJavaHome
if not exist "%JAVA_HOME%\bin\javac.exe" goto noJavaHome
set "JRE_HOME=%JAVA_HOME%"
goto okJava

:noJavaHome
echo The JAVA_HOME environment variable is not defined correctly.
echo It is needed to run this program in debug mode.
echo NB: JAVA_HOME should point to a JDK not a JRE.
goto exit

:gotJavaHome
rem No JRE given, use JAVA_HOME as JRE_HOME
set "JRE_HOME=%JAVA_HOME%"

:gotJreHome
rem Check if we have a usable JRE
if not exist "%JRE_HOME%\bin\java.exe" goto noJreHome
if not exist "%JRE_HOME%\bin\javaw.exe" goto noJreHome
goto okJava

:noJreHome
rem Needed at least a JRE
echo The JRE_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto exit

:okJava

rem Check if we have a TOMCAT_HOME
if "%TOMCAT_HOME%" == "" goto noTomcatHome
goto gotTomcatHome

:noTomcatHome
echo The TOMCAT_HOME environment variable is not defined correctly.
goto exit

:gotTomcatHome

rem check if jentrata is deployed
if exist "%TOMCAT_HOME%\webapps\corvus" goto jentrataDeployed
echo Deploying Jentrata to "%TOMCAT_HOME%\webapps"
mkdir "%TOMCAT_HOME%\webapps\corvus"
xcopy "%JENTRATA_HOME%\webapps\corvus" "%TOMCAT_HOME%\webapps/corvus" /S /E /Y /Q

:jentrataDeployed

set "EXECUTABLE=%TOMCAT_HOME%\bin\catalina.bat"

rem Check that target executable exists
if exist "%EXECUTABLE%" goto tomcatOk
echo Cannot find "%EXECUTABLE%"
echo This file is needed to run this program
goto end
:tomcatOk

rem check JAVA_OPTS and set corvus okHome
if "%JAVA_OPTS%" == "" goto noJavaOpts
set JAVA_OPTS="%JAVA_OPTS% -Dcorvus.home=%JENTRATA_HOME%"
goto okExec
:noJavaOpts
set JAVA_OPTS="-Dcorvus.home=%JENTRATA_HOME%"

:okExec

cd "%TOMCAT_HOME%\bin"

rem Get remaining unshifted command line arguments and save them in the
set CMD_LINE_ARGS=
:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs
:doneSetArgs

:execTomcat
call "%EXECUTABLE%" %CMD_LINE_ARGS%

goto end

:exit
exit /b 1

:end
exit /b 0
