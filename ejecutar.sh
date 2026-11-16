#!/bin/sh
# Compila y ejecuta ConexionTester en Linux / macOS
cd "$(dirname "$0")" || exit 1
mkdir -p bin
javac -encoding UTF-8 -d bin src/com/iudigital/conexiontester/ConexionTester.java || exit 1
java -cp "bin:lib/*" com.iudigital.conexiontester.ConexionTester
