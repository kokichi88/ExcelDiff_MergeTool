#!/bin/bash

PATH_2_JARFILE="ExcelDiff.jar"
java -jar ${PATH_2_JARFILE} file1=$1 file2=$2
