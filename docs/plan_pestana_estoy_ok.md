# Plan de Trabajo: Pestaña "Estoy ok" (Monitoreo Pasivo y Bienestar)

Este documento especifica los 4 módulos a desarrollar e integrar en la pestaña **"Estoy ok"** de la aplicación nativa (Android / Kotlin Jetpack Compose), asegurando atomicidad en cada entrega.

---

## 📌 Módulos a Implementar

### 1. ⏱️ Temporizador y Cuenta Regresiva Reactiva en Tiempo Real
- **Objetivo:** Mostrar un cronómetro dinámico en tiempo real (*"Expira en 05 h 23 min 12 s"*) que se actualice cada segundo.
- **Componentes:**
  - `LaunchedEffect` con bucle de 1 segundo en `PanelViewModel` / Composable.
  - Indicador / Barra de progreso circular o lineal con gradiente dinámico de color (Verde $\rightarrow$ Amarillo en 75% $\rightarrow$ Rojo al expirar).
- **Archivos a modificar:**
  - `android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelScreen.kt`
  - `android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelViewModel.kt`

---

### 2. 👨‍👩‍👧‍👦 Sección "Tranquilidad del Núcleo" (Bienestar de los Familiares)
- **Objetivo:** Permitir visualizar el estado de check-in pasivo de los miembros del grupo sin necesidad de abrir el mapa.
- **Componentes:**
  - Tarjeta o lista de familiares con su último reporte de bienestar (*"Protegido hasta las 20:00"*, *"Reporte Vencido hace 15m"*).
  - Botón de **"Recordar 🔔"** para disparar un ping/notificación al familiar si su reporte está vencido.
- **Archivos a modificar:**
  - `android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelScreen.kt`
  - `android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelViewModel.kt`

---

### 3. ⚙️ Tarjeta de Resumen y Acceso Rápido a Configuración de Protección
- **Objetivo:** Mostrar una vista sintética de los parámetros de seguridad configurados con un botón de acceso directo a Ajustes.
- **Componentes:**
  - Tarjeta con resumen de: Intervalo activo, Contactos asignados, Modo Sueño y Auto-Check-in en Wi-Fi.
  - Botón "Configurar ⚙️" con navegación a la pantalla de Ajustes.
- **Archivos a modificar:**
  - `android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelScreen.kt`

---

### 4. ✨ Feedback Animado y Celebración de Reporte Exitoso
- **Objetivo:** Ofrecer una respuesta táctil y visual satisfactoria al presionar el botón "Estoy OK".
- **Componentes:**
  - Diálogo / Cartel modal de felicitación animado (*"¡Genial! Tu familia sabe que estás bien y estás protegido hasta..."*).
  - Animación de confirmación en el botón.
- **Archivos a modificar:**
  - `android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelScreen.kt`

---

## 📝 Convención de Commits y Documentación
Cada uno de los 4 puntos anteriores se entregará en un **commit atómico individual** siguiendo la convención Conventional Commits, actualizando `docs/progress.txt` y `docs/status.md` tras la validación de compilación.
