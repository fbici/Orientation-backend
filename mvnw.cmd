@REM
@REM Copyright 2007-2024 the original author or authors.
@REM
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM      https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM

@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.3.2
@REM
@REM Optional ENV vars:
@REM   MVNW_REPOURL - repo url base for downloading maven distribution
@REM   MVNW_USERNAME/MVNW_PASSWORD - user and password for downloading maven
@REM   MVNW_VERBOSE - true: enable verbose log; debug: trace the mvnw script;
@REM                  others: silence the output
@REM ----------------------------------------------------------------------------

@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET __MVNW_CMD__=
@SET __MVNW_ERROR__=
@SET __MVNW_PSMODULEP_SAVE=%PSModulePath%
@SET PSModulePath=
@FOR /F "usebackq tokens=1* delims==" %%A IN ("%~dp0\.mvn\wrapper\maven-wrapper.properties") DO @(
    IF "%%~A"=="wrapperUrl" SET "__MVNW_CMD__=%%~B"
    IF "%%~A"=="distributionUrl" SET "WRAPPER_JAR=%~dp0\.mvn\wrapper\maven-wrapper.jar"
)
@IF "%__MVNW_CMD__%"=="" SET "__MVNW_CMD__=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"
@IF "%WRAPPER_JAR%"=="" SET "WRAPPER_JAR=%~dp0\.mvn\wrapper\maven-wrapper.jar"
@IF NOT EXIST "%WRAPPER_JAR%" (
    @ECHO Downloading Maven Wrapper...
    @POWERShell -Command "&{"^
		"$webclient = new-object System.Net.WebClient;"^
		"[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $webclient.DownloadFile('%__MVNW_CMD__', '%WRAPPER_JAR%')"^
		"}"
    @IF %ERRORLEVEL% NEQ 0 (
        @ECHO Failed to download Maven Wrapper
        @EXIT /B 1
    )
)
@IF EXIST "%~dp0\.mvn\wrapper\maven-wrapper.bat" (
    @CALL "%~dp0\.mvn\wrapper\maven-wrapper.bat" %*
) ELSE (
    @ECHO Maven Wrapper not found. Please run 'mvn wrapper:wrapper' first.
    @EXIT /B 1
)
