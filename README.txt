MultasPlugin - Instrucciones de compilación

Requisitos:
 - JDK 17 instalado
 - Gradle (o usa el Gradle Wrapper si lo añades)
 - Conexión a Internet para descargar dependencias (Paper API, Vault API)

Compilar (Linux / macOS):
 1. Abre una terminal en la carpeta del proyecto.
 2. Ejecuta: gradle shadowJar
 3. El JAR se generará en: build/libs/MultasPlugin-1.0.0.jar

Compilar (Windows PowerShell):
 1. Abre PowerShell en la carpeta del proyecto.
 2. Ejecuta: gradle shadowJar
 3. El JAR estará en build\libs\MultasPlugin-1.0.0.jar

Notas:
 - El proyecto pone Paper API y Vault como 'compileOnly'. No se recomienda incluir Paper en el jar del plugin.
 - Asegúrate de instalar Vault y un plugin de economía en tu servidor (por ejemplo, EssentialsX + its economy module) si quieres usar la función de pago.
 - Si quieres que incluya el Gradle Wrapper (./gradlew), indícalo y lo añado; actualmente no está incluido por limitaciones del entorno.
