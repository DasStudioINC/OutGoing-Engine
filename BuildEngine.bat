@echo off
echo ====================================================
echo STEP 1: Cleaning and Rebuilding Fresh JAR Artifact...
echo ====================================================
cd "C:\Personal Projects\OutGoing Engine"

:: 1. Build your engine jar first
call mvn clean package || echo Skipping internal compiler step...

:: 2. FIX: Copy RichTextFX and other dependencies directly into your jpackage input folder!
echo Copying external dependencies into the artifact bundle folder...
call mvn dependency:copy-dependencies -DoutputDirectory="C:\Personal Projects\OutGoing Engine\out\artifacts\OutGoingEngine_jar"

echo.
echo ====================================================
echo STEP 2: Running jpackage to wrap into Native .exe...
echo ====================================================
:: Now when jpackage runs, it will grab your Engine jar AND all the library jars copied above!
jpackage --type app-image --name "OutGoing Engine" --input "C:\Personal Projects\OutGoing Engine\out\artifacts\OutGoingEngine_jar" --main-jar OutGoingEngine.jar --main-class com.yurpha.outgoingengine.Main --dest dist --module-path lib/javafx-jmods --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.media,jdk.compiler,jdk.zipfs

echo.
echo ====================================================
echo STEP 3: Automatically syncing compilers and assets...
echo ====================================================
:: This copies your tools, javac.exe, and icon templates directly into the production layout
xcopy /E /I /Y "C:\Personal Projects\OutGoing Engine\lib" "C:\Personal Projects\OutGoing Engine\dist\OutGoing Engine\lib"

echo.
echo ====================================================
echo STEP 4: Creating Projects folder and syncing DemoGame...
echo ====================================================
:: 1. Force the creation of the Projects folder inside dist if it doesn't exist
if not exist "C:\Personal Projects\OutGoing Engine\dist\OutGoing Engine\Projects" (
    mkdir "C:\Personal Projects\OutGoing Engine\dist\OutGoing Engine\Projects"
)

:: 2. Mirror the whole DemoGame directory (overwriting files with your latest changes)
echo Mirroring current DemoGame files into the production folder...
xcopy /E /I /Y "C:\Personal Projects\OutGoing Engine\Projects\DemoGame" "C:\Personal Projects\OutGoing Engine\dist\OutGoing Engine\Projects\DemoGame"

echo.
echo ====================================================
echo BUILD COMPLETE! Engine and DemoGame are perfectly synced.
echo ====================================================
pause