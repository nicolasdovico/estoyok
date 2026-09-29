# Plan de Implementación: Onboarding / Walkthrough Dinámico (Bienvenida a Nuevos Usuarios)

> **Documento de Diseño y Planificación Técnica**  
> **Fecha:** 29 de Septiembre de 2026  
> **Objetivo:** Guiar al nuevo usuario en su primer inicio de sesión para que entienda el valor diferencial de Estoy Ok (Núcleos, Botón "Estoy OK" y Zonas Seguras), maximizando la retención del Día 1.  
> **Estado:** Propuesta en revisión (Sin cambios en código hasta aprobación).

---

## 1. Experiencia de Usuario (UX) y Contenido de las Pantallas

El Onboarding constará de un carrusel dinámico y deslizante (**HorizontalPager**) de **3 pantallas** con tema oscuro premium (`#0B132B`), tipografía *Outfit*, indicadores de puntos animados y botón de acción principal.

```
┌─────────────────────────────────────────────────────────────┐
│  [Omitir]                                                   │
│                                                             │
│                    [ ILUSTRACIÓN VECTORIAL ]                │
│                                                             │
│              TÍTULO PRINCIPAL (Outfit Bold 26sp)            │
│          Subtítulo descriptivo y legible (16sp)             │
│                                                             │
│                       ● ○ ○                                 │
│                                                             │
│              [ BOTÓN: Siguiente / Comenzar ]                │
└─────────────────────────────────────────────────────────────┘
```

### Pantalla 1: Tu Núcleo Familiar en Vivo
* **Iconografía / Gráfico:** Círculo familiar conectado con mapa interactivo y pines de colores.
* **Badge / Tag superior:** `🟢 LOCALIZACIÓN EN TIEMPO REAL`
* **Título:** *Tu Familia Conectada en Todo Momento*
* **Descripción:**  
  *Creá tu Núcleo Familiar con un simple código de 6 dígitos. Podrás ver dónde están tus seres queridos en un mapa de alta precisión con deslizamiento continuo y en tiempo real.*
* **Botón de acción:** *Siguiente $\rightarrow$*

---

### Pantalla 2: El Botón "Estoy OK" (Tu Diferencial de Protección Pasiva)
* **Iconografía / Gráfico:** El icónico botón verde esmeralda "Estoy OK" con temporizador de protección y aviso a WhatsApp.
* **Badge / Tag superior:** `🛡️ CUIDADO DIARIO AUTOMÁTICO`
* **Título:** *Confirmá tu Bienestar en un Solo Toque*
* **Descripción:**  
  *Presioná el botón al iniciar tu día para confirmar que estás bien. Si no lo hacés en tu horario habitual, la app le enviará una alerta automática inmediata a tus contactos de emergencia por WhatsApp.*
* **Botón de acción:** *Siguiente $\rightarrow$*

---

### Pantalla 3: Zonas Seguras y Avisos Automáticos (Opción 1 Elegida)
* **Iconografía / Gráfico:** Perímetros de Zonas Seguras (Hogar, Colegio, Trabajo) con campana de notificación de llegada.
* **Badge / Tag superior:** `📍 LLEGADAS Y SALIDAS`
* **Título:** *Avisos de Casa, Escuela y Trabajo*
* **Descripción:**  
  *Configurá lugares frecuentes y recibí notificaciones instantáneas automáticas cuando tus hijos o familiares lleguen o salgan de su destino, sin necesidad de que te escriban 'ya llegué'.*
* **Botón de acción:** *«¡Comenzar a usar Estoy Ok! 🚀»*

---

## 2. Arquitectura Técnica y Flujo de Datos (Android Jetpack Compose)

### 2.1 Persistencia del Estado (`SessionManager.kt`)
* Guardar el estado de finalización del Onboarding en Preferences DataStore:
  ```kotlin
  private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
  
  val onboardingCompletedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
      preferences[ONBOARDING_COMPLETED] ?: false
  }
  
  suspend fun saveOnboardingCompleted(completed: Boolean = true)
  ```

### 2.2 Control de Estado en `AuthViewModel.kt`
* Exponer `val isOnboardingCompleted: StateFlow<Boolean?>` para que la navegación reaccione al instante sin parpadeos ni condiciones de carrera.
* Método `fun completeOnboarding()` que persiste el valor y transiciona la UI.

### 2.3 Secuencia Estricta de Navegación en `NavGraph.kt`
Garantizar que no haya colisiones de diálogos ni peticiones prematuras de permisos del sistema:

```
[ Registro / Login Exitoso ]
         ↓
[ ¿Aceptó Términos y Condiciones? ]
   ├── NO  → Mostrar DisclaimerMandatoryDialog
   └── SÍ  ↓
[ ¿Completó Onboarding? ]
   ├── NO  → Mostrar OnboardingScreen (Carrusel 3 Pantallas)
   └── SÍ  ↓
[ Pantalla Principal: Screen.Mapa ]
   ├── Diálogo Prominent Disclosure de Ubicación (Aceptar y continuar)
   └── Solicitud de Permisos en Tiempo de Ejecución de Android
```

> **Beneficio clave:** El usuario no se siente bombardeado con solicitudes de permisos del sistema operativo mientras está leyendo la bienvenida. El flujo es ordenado, pedagógico y transparente.

### 2.4 Acceso Opcional desde Ajustes (`AjustesScreen.kt`)
* Añadir una opción en la tarjeta de soporte/información: *"Ver tutorial de bienvenida"*, permitiendo reabrir el carrusel en cualquier momento si el usuario desea refrescar cómo funciona la app.

---

## 3. Plan de Ejecución Paso a Paso

1. **Paso 1: Capa de Datos y Persistencia Local**
   * Modificar `SessionManager.kt` para agregar `ONBOARDING_COMPLETED`.
   * Conectar en `AuthViewModel.kt`.

2. **Paso 2: Componentes UI de Presentación (Jetpack Compose)**
   * Crear `OnboardingScreen.kt` en `features/auth/presentation/onboarding/`.
   * Implementar `HorizontalPager` con animaciones de transición, indicadores de página (dots) y tarjetas con estilo oscuro Slate Navy (`#0B132B`).
   * Diseñar los 3 gráficos vectoriales/composables temáticos con iconos oficiales de Compose.

3. **Paso 3: Integración en la Navegación Global**
   * Integrar en `NavGraph.kt` respetando la secuencia Disclaimer $\rightarrow$ Onboarding $\rightarrow$ Mapa.

4. **Paso 4: Botón de Re-visualización en Ajustes**
   * Agregar la fila de acceso en `AjustesScreen.kt` para poder volver a verlo cuando se desee.

5. **Paso 5: Pruebas, Compilación y No Regresión**
   * Compilar el proyecto en modo `debug` con Gradle (`./gradlew assembleDebug`).
   * Ejecutar suite de pruebas de backend (`docker compose exec backend php artisan test`).
   * Actualizar [`docs/status.md`](file:///home/usuario/aplicaciones/estoyok/docs/status.md) y [`docs/progress.txt`](file:///home/usuario/aplicaciones/estoyok/docs/progress.txt).
