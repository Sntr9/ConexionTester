@echo off
REM Compila y ejecuta ConexionTester en Windows
if not exist bin mkdir bin
javac -encoding UTF-8 -d bin src\com\iudigital\conexiontester\ConexionTester.java
if errorlevel 1 (
  echo Error de compilacion. Verifique que el JDK este instalado.
  pause
  exit /b 1
)
java -cp "bin;lib/*" com.iudigital.conexiontester.ConexionTester
