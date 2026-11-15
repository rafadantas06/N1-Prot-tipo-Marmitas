#!/bin/sh
# Executa o teste automatizado do cenário (sem interação).
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java") || exit 1
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp out teste.TesteCenarioPrincipal
