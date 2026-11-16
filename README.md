# ConexionTester – Probador de conexión JDBC

Actividad Evaluativa 3 (Unidad 3) – Programación Orientada a Objetos II – IU Digital.

Aplicación de escritorio en Java Swing que recibe la URL JDBC, el usuario y la contraseña de una base de datos y prueba la conexión con `DriverManager.getConnection()`. El resultado se muestra en verde si la conexión es exitosa y en rojo si falla, junto con el mensaje técnico del error.

## Estructura

```
ConexionTester/
├── src/com/iudigital/conexiontester/ConexionTester.java
├── lib/sqlite-jdbc-3.46.1.3.jar   (driver JDBC de SQLite)
├── ejecutar.bat                    (Windows)
└── ejecutar.sh                     (Linux / macOS)
```

## Requisitos

- JDK 17 o superior (probado con OpenJDK 21).
- No usa frameworks externos. El único `.jar` adicional es el driver JDBC de SQLite, que Java no trae incluido.

## Compilar y ejecutar

Windows:

```
ejecutar.bat
```

Linux / macOS:

```
sh ejecutar.sh
```

Manual:

```
javac -encoding UTF-8 -d bin src/com/iudigital/conexiontester/ConexionTester.java
java -cp "bin;lib/*" com.iudigital.conexiontester.ConexionTester      (Windows)
java -cp "bin:lib/*" com.iudigital.conexiontester.ConexionTester      (Linux / macOS)
```

En NetBeans, Eclipse o IntelliJ basta con crear un proyecto Java, copiar la carpeta `src` y agregar `lib/sqlite-jdbc-3.46.1.3.jar` a las librerías del proyecto.

## Casos de prueba

| URL | Resultado esperado |
|---|---|
| `jdbc:sqlite:inventario.db` | Conexión exitosa (verde) |
| `jdbc:sqlite:/carpeta/que/no/existe/inventario.db` | Fallo de conexión – `SQLException` (rojo) |
| `jdbc:mysql://localhost:3306/inventario` | Fallo del driver – `ClassNotFoundException` (rojo) |
| `jdbc:basedatos:inventario` | Fallo de conexión – "No suitable driver" (rojo) |

Autor: Santiago López Restrepo
