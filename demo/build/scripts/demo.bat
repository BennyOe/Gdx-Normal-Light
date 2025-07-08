@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem
@rem SPDX-License-Identifier: Apache-2.0
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  demo startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and DEMO_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\demo-1.0.1-pooling-SNAPSHOT.jar;%APP_HOME%\lib\core-1.0.1-pooling-SNAPSHOT.jar;%APP_HOME%\lib\ktx-app-1.13.1-rc1.jar;%APP_HOME%\lib\ktx-assets-1.13.1-rc1.jar;%APP_HOME%\lib\ktx-math-1.13.1-rc1.jar;%APP_HOME%\lib\ktx-async-1.13.1-rc1.jar;%APP_HOME%\lib\kotlinx-coroutines-core-jvm-1.10.1.jar;%APP_HOME%\lib\kotlin-stdlib-2.1.10.jar;%APP_HOME%\lib\gdx-backend-lwjgl3-1.13.1.jar;%APP_HOME%\lib\annotations-23.0.0.jar;%APP_HOME%\lib\box2dlights-1.5.jar;%APP_HOME%\lib\gdx-box2d-1.6.2.jar;%APP_HOME%\lib\gdx-1.13.1.jar;%APP_HOME%\lib\lwjgl-glfw-3.3.3.jar;%APP_HOME%\lib\lwjgl-glfw-3.3.3-natives-linux.jar;%APP_HOME%\lib\lwjgl-glfw-3.3.3-natives-linux-arm32.jar;%APP_HOME%\lib\lwjgl-glfw-3.3.3-natives-linux-arm64.jar;%APP_HOME%\lib\lwjgl-glfw-3.3.3-natives-macos.jar;%APP_HOME%\lib\lwjgl-glfw-3.3.3-natives-macos-arm64.jar;%APP_HOME%\lib\lwjgl-glfw-3.3.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-glfw-3.3.3-natives-windows-x86.jar;%APP_HOME%\lib\lwjgl-jemalloc-3.3.3.jar;%APP_HOME%\lib\lwjgl-jemalloc-3.3.3-natives-linux.jar;%APP_HOME%\lib\lwjgl-jemalloc-3.3.3-natives-linux-arm32.jar;%APP_HOME%\lib\lwjgl-jemalloc-3.3.3-natives-linux-arm64.jar;%APP_HOME%\lib\lwjgl-jemalloc-3.3.3-natives-macos.jar;%APP_HOME%\lib\lwjgl-jemalloc-3.3.3-natives-macos-arm64.jar;%APP_HOME%\lib\lwjgl-jemalloc-3.3.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-jemalloc-3.3.3-natives-windows-x86.jar;%APP_HOME%\lib\lwjgl-openal-3.3.3.jar;%APP_HOME%\lib\lwjgl-openal-3.3.3-natives-linux.jar;%APP_HOME%\lib\lwjgl-openal-3.3.3-natives-linux-arm32.jar;%APP_HOME%\lib\lwjgl-openal-3.3.3-natives-linux-arm64.jar;%APP_HOME%\lib\lwjgl-openal-3.3.3-natives-macos.jar;%APP_HOME%\lib\lwjgl-openal-3.3.3-natives-macos-arm64.jar;%APP_HOME%\lib\lwjgl-openal-3.3.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-openal-3.3.3-natives-windows-x86.jar;%APP_HOME%\lib\lwjgl-opengl-3.3.3.jar;%APP_HOME%\lib\lwjgl-opengl-3.3.3-natives-linux.jar;%APP_HOME%\lib\lwjgl-opengl-3.3.3-natives-linux-arm32.jar;%APP_HOME%\lib\lwjgl-opengl-3.3.3-natives-linux-arm64.jar;%APP_HOME%\lib\lwjgl-opengl-3.3.3-natives-macos.jar;%APP_HOME%\lib\lwjgl-opengl-3.3.3-natives-macos-arm64.jar;%APP_HOME%\lib\lwjgl-opengl-3.3.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-opengl-3.3.3-natives-windows-x86.jar;%APP_HOME%\lib\lwjgl-stb-3.3.3.jar;%APP_HOME%\lib\lwjgl-stb-3.3.3-natives-linux.jar;%APP_HOME%\lib\lwjgl-stb-3.3.3-natives-linux-arm32.jar;%APP_HOME%\lib\lwjgl-stb-3.3.3-natives-linux-arm64.jar;%APP_HOME%\lib\lwjgl-stb-3.3.3-natives-macos.jar;%APP_HOME%\lib\lwjgl-stb-3.3.3-natives-macos-arm64.jar;%APP_HOME%\lib\lwjgl-stb-3.3.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-stb-3.3.3-natives-windows-x86.jar;%APP_HOME%\lib\lwjgl-3.3.3.jar;%APP_HOME%\lib\lwjgl-3.3.3-natives-linux.jar;%APP_HOME%\lib\lwjgl-3.3.3-natives-linux-arm32.jar;%APP_HOME%\lib\lwjgl-3.3.3-natives-linux-arm64.jar;%APP_HOME%\lib\lwjgl-3.3.3-natives-macos.jar;%APP_HOME%\lib\lwjgl-3.3.3-natives-macos-arm64.jar;%APP_HOME%\lib\lwjgl-3.3.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-3.3.3-natives-windows-x86.jar;%APP_HOME%\lib\jlayer-1.0.1-gdx.jar;%APP_HOME%\lib\jorbis-0.0.17.jar;%APP_HOME%\lib\gdx-jnigen-loader-2.5.2.jar


@rem Execute demo
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %DEMO_OPTS%  -classpath "%CLASSPATH%" com.github.bennyOe.gdxNormalLight.demo.GgdxNormalMapExampleKt %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable DEMO_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%DEMO_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
