@ECHO OFF
@ECHO Comparing two files:
@ECHO 1: %1
@ECHO 2: %2

SET JAVA_HOME = "C:\Program Files\Java\jre1.8.0_121\bin\java.exe"
SET PATH_2_JAR = "ExcelDiff.jar"

SET "FILE1=%1"
SET "FILE2=%2"

%JAVA_HOME% -jar %PATH_2_JAR% file1=%FILE1% file2=%FILE2%