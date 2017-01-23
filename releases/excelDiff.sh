#!/bin/bash
JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk1.8.0_73.jdk/Contents/Home"
PATH_2_JARFILE="ExcelDiff.jar"
${JAVA_HOME}/bin/java -jar ${PATH_2_JARFILE} file1="$1" file2="$2"
