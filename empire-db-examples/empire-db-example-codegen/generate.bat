@echo off
REM Licensed to the Apache Software Foundation (ASF) under one
REM or more contributor license agreements.  See the NOTICE file
REM distributed with this work for additional information
REM regarding copyright ownership.  The ASF licenses this file
REM to you under the Apache License, Version 2.0 (the
REM "License"); you may not use this file except in compliance
REM with the License.  You may obtain a copy of the License at
REM
REM   http://www.apache.org/licenses/LICENSE-2.0
REM
REM Unless required by applicable law or agreed to in writing,
REM software distributed under the License is distributed on an
REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
REM KIND, either express or implied.  See the License for the
REM specific language governing permissions and limitations
REM under the License.
IF [%1%] == [] GOTO INVALID_PARAMS
IF [%2%] == [] GOTO INVALID_PARAMS
rem base setting
set repo=%2%
set empire-db-version=3.0.0-SNAPSHOT
set jdbc-jar-path=org\hsqldb\hsqldb\2.5.2\hsqldb-2.5.2.jar
IF [%3%] == [] GOTO CLASSPATH
set jdbc-jar-path=%3%
rem Assemble classpath
:CLASSPATH
set classpath=%repo%\org\apache\empire-db\empire-db\%empire-db-version%\empire-db-%empire-db-version%.jar
set classpath=%classpath%;%repo%\org\apache\empire-db\empire-db-codegen\%empire-db-version%\empire-db-codegen-%empire-db-version%.jar
set classpath=%classpath%;%repo%\org\slf4j\slf4j-api\1.7.36\slf4j-api-1.7.36.jar
set classpath=%classpath%;%repo%\org\slf4j\slf4j-reload4j\1.7.36\slf4j-reload4j-1.7.36.jar
set classpath=%classpath%;%repo%\ch\qos\reload4j\reload4j\1.2.19\reload4j-1.2.19.jar
set classpath=%classpath%;%repo%\commons-beanutils\commons-beanutils\1.9.4\commons-beanutils-1.9.4.jar
set classpath=%classpath%;%repo%\commons-logging\commons-logging\1.2\commons-logging-1.2.jar
set classpath=%classpath%;%repo%\org\apache\velocity\velocity\1.7\velocity-1.7.jar
set classpath=%classpath%;%repo%\commons-collections\commons-collections\3.2.2\commons-collections-3.2.2.jar
set classpath=%classpath%;%repo%\commons-lang\commons-lang\2.4\commons-lang-2.4.jar
rem The JDBC class libaray
set classpath=%classpath%;%repo%\%jdbc-jar-path%
echo Calling code generator
echo Config-File: %1%
echo JDBC-jar: %jdbc-jar-path%
echo Empire-db-version: %empire-db-version%
echo Maven-Repo-Path: %repo%
echo classpath: %classpath%
java -cp %classpath% org.apache.empire.db.codegen.CodeGenerator %1
goto done
:INVALID_PARAMS
echo. 
echo Please add your Config-File and the path to your Maven repository
echo. 
echo    generate XML_CONFIG_FILE MAVEN_REPO_PATH [PATH_TO_JDBC_JAR]
echo.
:DONE
pause