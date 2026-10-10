# 🚀 Nuevas Funcionalidades y Backlog de Innovación - Estoy Ok

Este documento centraliza las especificaciones técnicas, casos de uso y arquitectura de nuevas funcionalidades planificadas para **Estoy Ok**. Su objetivo es registrar ideas innovadoras, evaluar su viabilidad técnica y definir el paso a paso para su posterior implementación en la aplicación móvil y el backend.

---

## 1. 🚨 SOS de Bolsillo (Disparador Silencioso con Botón de Encendido)

* **Estado:** Especificado y Validado Técnicamente (Listo para Implementar).
* **Plataforma:** Android Nativo (Kotlin / Jetpack Compose).
* **Componentes Involucrados:** `TrackingService.kt`, `SosRepository.kt`, `AudioRecorder.kt`, `SessionManager.kt`, `AjustesScreen.kt`.

### 1.1 Objetivo y Caso de Uso Real
En una situación de riesgo inminente (un asalto en la vía pública, una persecución, violencia intrafamiliar o una descompensación médica aguda), **es inviable y peligroso tener que sacar el teléfono, desbloquearlo con huella/PIN, abrir la aplicación y buscar la solapa "Estoy OK"**.

El **SOS de Bolsillo** permite que el usuario dispare una alerta de auxilio completa con la mano dentro del bolsillo de la campera o el pantalón, **sin encender la pantalla, sin emitir ningún sonido y sin alertar al agresor**.

---

### 1.2 Viabilidad Técnica y Cumplimiento con Google Play Store

> [!NOTE]
> **100% Viable y Seguro:** Esta funcionalidad está completamente confirmada y no pone en riesgo la publicación en Google Play Console.

* **El problema de otras soluciones del mercado:** Muchas aplicaciones intentan interceptar botones físicos utilizando permisos de Accesibilidad (`AccessibilityService`). Google Play revisa exhaustivamente estos permisos y suele rechazar las aplicaciones si no están estrictamente destinadas a personas con discapacidades motrices.
* **Nuestra solución técnica aprobada:** 
  - Estoy Ok ya cuenta con un servicio en primer plano persistente de geolocalización aprobado en producción (`TrackingService.kt` con `FOREGROUND_SERVICE_LOCATION`).
  - Cuando el usuario presiona el botón físico de encendido (*Power button*), el sistema operativo Android emite dos emisiones globales:
    - `Intent.ACTION_SCREEN_OFF`: cuando la pantalla se apaga.
    - `Intent.ACTION_SCREEN_ON`: cuando la pantalla se enciende.
  - Aunque estas emisiones no pueden registrarse en el archivo estático `AndroidManifest.xml` desde Android 8.0 (por restricciones de ahorro de energía), **Android permite registrarlas dinámicamente en código en tiempo de ejecución (`registerReceiver`) dentro de un Foreground Service activo**.
  - No requiere ningún permiso nuevo ni peligroso. Utiliza únicamente los permisos ya homologados y concedidos en la app (`RECORD_AUDIO`, `ACCESS_FINE_LOCATION`, `VIBRATE`).

---

### 1.3 Algoritmo de Detección Antifalsos Positivos (Ventana Deslizante)

Para evitar que la alerta se dispare si el usuario simplemente mira la hora o guarda el celular:

1. **Ventana de Tiempo Estricta:** Se define una ventana temporal de **2.5 a 3.0 segundos**.
2. **Umbral de Eventos:** Se requiere una cadencia rápida de **4 cambios de estado de pantalla consecutivos** (lo que equivale a presionar el botón 3 o 4 veces seguidas rápidamente: *Prender $\rightarrow$ Apagar $\rightarrow$ Prender $\rightarrow$ Apagar*).
3. **Mecanismo de Cola Deslizante (`ArrayDeque<Long>`):**
   - Cada pulsación registra la marca de tiempo `System.currentTimeMillis()`.
   - Se eliminan todas las marcas con una antigüedad mayor a 2500 ms.
   - Si la cantidad de marcas válidas alcanza 4, se dispara el protocolo de emergencia y se limpia la cola para evitar dobles disparos.
4. **Coexistencia con el 911 Nativo de Android:** En Android 12+, el sistema operativo cuenta con su propia función de llamada al 911 configurada en **5 toques**. Con **4 toques**, Estoy Ok activa el auxilio familiar silencioso antes de invocar los servicios públicos del sistema.

---

### 1.4 Respuesta Háptica 100% Discreta (En el Bolsillo)

Para que el usuario tenga la total certeza de que la alerta se envió sin tener que mirar el celular:
* **Pantalla y altavoz:** Permanecen en silencio y sin destellos.
* **Vibración háptica distintiva:** El teléfono ejecuta una vibración codificada discreta (por ejemplo, 2 pulsos cortos de 250 ms: `vibrate(longArrayOf(0, 250, 150, 250), -1)`).
* El usuario siente la confirmación física en su pierna o mano y sabe que sus contactos ya están siendo alertados.

---

### 1.5 Pipeline de Emergencia Ejecutado en Segundo Plano

Al confirmarse la secuencia en `TrackingService`:
1. **Llamada a la API (`POST /api/tracking/sos`):** Dispara `sosRepository.triggerSos()`, creando el incidente en el backend.
2. **Aceleración Crítica de GPS:** El servicio eleva inmediatamente la frecuencia del GPS a **máxima precisión cada 5 segundos** (`TrackingService.ACTION_UPDATE_INTERVAL` a 5000L).
3. **Grabación de Audio Ambiental Silenciosa:** Se inicializa `AudioRecorder(context)` y se graban **15 segundos de audio** del micrófono en segundo plano.
4. **Carga a la Nube:** Al finalizar los 15 segundos, se envía el archivo de audio al servidor mediante `sosRepository.uploadAudio(alertId, file)`.
5. **Difusión Automática de Rescate:** El backend despacha las alertas de WhatsApp con el mapa interactivo en vivo a los contactos de emergencia vinculados.

---

### 1.6 Interfaz de Usuario y Configuración (Ajustes)

En `AjustesScreen.kt`:
* Se incorpora una nueva tarjeta dentro de la sección de seguridad:
  * **Título:** *"SOS de Bolsillo (Botón de Encendido)"*
  * **Descripción:** *"Presioná el botón de encendido 4 veces seguidas rápidamente para enviar un SOS silencioso con el teléfono bloqueado o guardado en el bolsillo."*
  * **Interruptor (Switch):** Activado / Desactivado.
* **Persistencia:** Guardado en `DataStore` a través de `SessionManager.kt` (`isPowerButtonSosEnabled: Flow<Boolean>`).
* **Estrategia Comercial:** Puede configurarse como funcionalidad estándar de alta protección o como beneficio exclusivo del **Plan PRO** para potenciar las conversiones.

---

### 1.7 Pasos Técnicos para su Implementación

1. **`SessionManager.kt`:** Añadir clave `POWER_BUTTON_SOS_ENABLED` con valor por defecto `true`.
2. **`TrackingService.kt`:**
   - Crear e inyectar `SosRepository`.
   - Declarar e instanciar un `BroadcastReceiver` dinámico para `Intent.ACTION_SCREEN_ON` y `Intent.ACTION_SCREEN_OFF`.
   - Registrar el receiver en `onCreate()` y desregistrarlo en `onDestroy()`.
   - Implementar el método `handleScreenToggleEvent()` con la cola deslizante de 2500 ms y la vibración háptica.
3. **`AjustesScreen.kt` & `SettingsViewModel.kt`:** Exponer el toggle visual para que el usuario pueda activar o desactivar la detección.

---

## 2. 📝 Banco de Ideas y Próximas Funcionalidades

*(Espacio reservado para agregar nuevas propuestas y mejoras a evaluar).*

### [Idea 2.1] ...
* **Descripción:**
* **Caso de Uso:**
* **Viabilidad Técnica:**
