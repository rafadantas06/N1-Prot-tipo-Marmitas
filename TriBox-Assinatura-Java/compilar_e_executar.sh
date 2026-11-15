#!/bin/sh
# Compila e executa o programa (requer JDK 17 ou superior).
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java") || exit 1
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp out fronteira.Main
