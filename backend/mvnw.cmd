@REM Maven Wrapper batch script for Windows
@echo off
setlocal

set MAVEN_PROJECTBASEDIR=%~dp0
set WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"

@REM Download wrapper jar if not present
if not exist "%WRAPPER_JAR%" (
    echo Downloading maven-wrapper.jar...
    powershell -Command "Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile '%WRAPPER_JAR%'"
)

@REM Find java
if not defined JAVA_HOME (
    set JAVA_HOME=%JAVA_HOME:"=%
    for /f "delims=" %%i in ('where java') do (
        set JAVA_CMD=%%i
        goto :found_java
    )
    echo ERROR: JAVA_HOME is not set and no 'java' command could be found.
    exit /b 1
    :found_java
)

set MAVEN_OPTS=%MAVEN_OPTS% -Xmx64m -Xms64m

@REM Execute Maven
"%JAVA_HOME%\bin\java" %MAVEN_OPTS% -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*

endlocal
