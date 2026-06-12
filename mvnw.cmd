@ECHO OFF
SETLOCAL

SET "BASEDIR=%~dp0"
IF "%BASEDIR:~-1%"=="\" SET "BASEDIR=%BASEDIR:~0,-1%"
SET "WRAPPER_JAR=%BASEDIR%\.mvn\wrapper\maven-wrapper.jar"

IF DEFINED JAVA_HOME (
  SET "JAVACMD=%JAVA_HOME%\bin\java.exe"
) ELSE (
  SET "JAVACMD=java"
)

IF DEFINED MAVEN_HOME (
  SET "WRAPPER_MVN=%MAVEN_HOME%\bin\mvn.cmd"
  IF EXIST "%WRAPPER_MVN%" GOTO run
)

IF DEFINED M2_HOME (
  SET "WRAPPER_MVN=%M2_HOME%\bin\mvn.cmd"
  IF EXIST "%WRAPPER_MVN%" GOTO run
)

SET "WRAPPER_MVN=C:\Program Files\NetBeans-23\netbeans\java\maven\bin\mvn.cmd"
IF EXIST "%WRAPPER_MVN%" GOTO run

WHERE mvn >NUL 2>NUL
IF %ERRORLEVEL% EQU 0 (
  SET "WRAPPER_MVN=mvn"
  GOTO run
)

IF EXIST "%WRAPPER_JAR%" (
  "%JAVACMD%" "-Dmaven.multiModuleProjectDirectory=%BASEDIR%" -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
  EXIT /B %ERRORLEVEL%
)

ECHO Maven was not found and Maven Wrapper could not run. Check JAVA_HOME or network/certificate access to the configured Maven distribution. 1>&2
EXIT /B 1

:run
CALL "%WRAPPER_MVN%" %*
EXIT /B %ERRORLEVEL%
