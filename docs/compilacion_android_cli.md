# Guía de Compilación de Android desde Línea de Comandos (CLI)

Este documento detalla cómo compilar, construir e instalar la aplicación nativa Android de **Estoy Ok** directamente desde la terminal, sin necesidad de abrir o utilizar el entorno gráfico de Android Studio.

---

## 📋 Requisitos del Sistema

Para poder compilar y comunicarte con tu dispositivo móvil desde la consola, el sistema requiere:

1. **Java Development Kit (JDK):** Versión 17 o 21 (actualmente configurado con OpenJDK 21).
2. **Android SDK:** Ubicado en la ruta local definida en el archivo [local.properties](file:///home/usuario/aplicaciones/estoyok/android-native/local.properties) (por defecto en `/home/usuario/Android/Sdk`).
3. **Android Command Line Tools (`adb`):** Herramienta para transferir y depurar aplicaciones en dispositivos conectados.

---

## 🚀 Comandos Principales (Gradle Wrapper)

Todos los comandos de compilación deben ejecutarse desde la carpeta del proyecto nativo: `android-native/`.

### 1. Limpiar el Proyecto
Borra los archivos de compilaciones previas, temporales y cachés para asegurar una compilación 100% limpia.
```bash
./gradlew clean
```

### 2. Compilar Código Kotlin (Verificación de Tipos)
Verifica que todo el código fuente Kotlin compile correctamente sin generar un archivo instalable (ideal para chequeos rápidos de sintaxis).
```bash
./gradlew :app:compileDebugKotlin
```

### 3. Construir APK de Desarrollo (Debug)
Genera el paquete instalable `.apk` con firmas de depuración. Es el comando ideal para probar en teléfonos físicos de test.
```bash
./gradlew assembleDebug
```
* **Ubicación del APK resultante:**
  `android-native/app/build/outputs/apk/debug/app-debug.apk`

### 4. Construir App Bundle de Producción (Release)
Construye el archivo `.aab` optimizado para ser subido a la tienda oficial de Google Play Store.
```bash
./gradlew bundleRelease
```
* **Ubicación del App Bundle resultante:**
  `android-native/app/build/outputs/bundle/release/app-release.aab`

---

## 📱 Instalación y Depuración Directa en Dispositivos

Puedes instalar la aplicación compilada en tu dispositivo físico directamente por cable USB o Wi-Fi usando la herramienta `adb` (Android Debug Bridge) de Google.

### Paso 1: Activar Depuración USB en el Teléfono
1. Ve a **Ajustes > Acerca del teléfono** en tu dispositivo.
2. Pulsa 7 veces seguidas sobre **Número de compilación** para activar las *Opciones de Desarrollador*.
3. Regresa al menú principal de Ajustes, entra en **Opciones de Desarrollador** y activa **Depuración por USB**.

### Paso 2: Verificar la Conexión en la PC
Conecta el teléfono a tu computadora mediante un cable USB. Ejecuta en la terminal:
```bash
adb devices
```
*Deberías ver una salida indicando que el dispositivo está conectado (ej. `ad34ef01 device`). Si aparece `unauthorized`, acepta la huella de depuración en la pantalla del celular.*

### Paso 3: Compilar e Instalar en un Solo Comando
Para compilar la app, generar el APK de pruebas, subirlo e instalarlo directamente en tu teléfono móvil conectado:
```bash
./gradlew installDebug
```
Una vez que el comando finalice con `BUILD SUCCESSFUL`, la aplicación **Estoy Ok** estará instalada y lista para abrir en tu celular.

---

## 🛠️ Consejos y Resolución de Problemas

### Permisos de Ejecución del Script `gradlew`
Si la consola arroja un error de permiso denegado (`Permission denied`) al ejecutar `./gradlew`, otorga los permisos necesarios con:
```bash
chmod +x gradlew
```

### Monitoreo de Logs en Vivo
Si quieres ver los logs que produce la app nativa en tiempo real mientras la usas en tu teléfono (filtros por tag o errores):
```bash
adb logcat *:E
```
*(Filtra únicamente errores críticos. Puedes cambiar `*:E` por `EstoyOk:D` si quieres ver logs de debug propios).*

### ADB Reversible para Desarrollo Local
Para permitir que la app instalada en tu teléfono USB pueda hacer peticiones HTTP al servidor Laravel local de tu computadora (`http://127.0.0.1:8000`), ejecuta:
```bash
adb reverse tcp:8000 tcp:8000
```

### Error de Jlink / JDK Incompleto (jlink executable does not exist)
Si al compilar obtienes un error como:
`java.lang.IllegalArgumentException: jlink executable /usr/lib/jvm/java-21-openjdk-amd64/bin/jlink does not exist`

Esto ocurre porque en tu sistema operativo solo tienes el JRE (Java Runtime Environment) básico y te falta el JDK completo de compilación. Tienes dos formas de solucionarlo:

**Opción A: Instalar el JDK completo del sistema (Recomendado)**
```bash
sudo apt update && sudo apt install -y openjdk-21-jdk
```

**Opción B: Usar el JDK de Android Studio (Si lo instalaste vía Snap)**
Antepone la variable de entorno `JAVA_HOME` apuntando al JDK interno de Android Studio antes de tus comandos de Gradle:
```bash
JAVA_HOME=/snap/android-studio/current/jbr ./gradlew installDebug
```
*(Puedes definir `export JAVA_HOME=/snap/android-studio/current/jbr` en tu archivo `.bashrc` o `.zshrc` para que sea persistente en todas tus terminales).*

