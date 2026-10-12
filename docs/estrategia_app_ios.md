# Estrategia y Hoja de Ruta: Desarrollo y Despliegue de la App iOS (Sin Mac Física)
**Proyecto:** Estoy Ok  
**Estado:** Documento de referencia y arquitectura futura  
**Objetivo:** Guía práctica para portar la aplicación a iOS (iPhone), compilar sin hardware Apple físico y publicar en el App Store.

---

## 1. Contexto y Diagnóstico Inicial

* **Estado actual del proyecto:**
  * **Backend:** Laravel 12 (PHP 8.4) con PostgreSQL 16 + PostGIS, Redis y contrato OpenAPI/Swagger (`docs/`) 100% estabilizado.
  * **App Móvil:** Desarrollada en **Android Nativo (Kotlin con Jetpack Compose)** en `android-native/`, publicada formalmente en Google Play Store para más de 17.700 modelos de dispositivos.
* **El desafío:** No se dispone de una computadora Mac física local para correr Xcode (el entorno oficial y obligatorio de Apple para compilar y firmar apps de iOS).

---

## 2. Cómo Compilar y Publicar para iOS sin Comprar una Mac

Apple exige de forma ineludible que los paquetes de instalación (`.ipa`) sean compilados mediante **Xcode** en el sistema operativo **macOS**. Hoy no es necesario comprar una MacBook de $2.000 USD; la industria resuelve esto mediante infraestructura en la nube:

### 2.1 Las 3 Mejores Opciones de Hardware en la Nube

| Opción | Servicio | Cómo Funciona | Costo Estimado | Para qué sirve mejor |
|---|---|---|---|---|
| **A (Recomendada)** | **[MacInCloud](https://www.macincloud.com)** | Alquilás una Mac física en un datacenter. Te conectás por Escritorio Remoto (VNC / Remmina) desde tu Linux actual y ves una Mac en pantalla con Xcode listo. | ~$1 USD / hora (prepago) o ~$20 a $30 USD / mes | Desarrollar, probar en el simulador de iPhone, gestionar certificados y subir a App Store Connect con interfaz visual. |
| **B (Automatizada)** | **[Codemagic](https://codemagic.io)** o **Bitrise** | CI/CD en la nube para móviles. Conectás tu repositorio de GitHub y servidores Mac M1/M2 compilan, firman y mandan la app a TestFlight con 1 clic. | Plan gratuito disponible (500 min/mes), luego pago por uso. | Despliegue continuo automático sin tocar una Mac manualmente. |
| **C (Developer)** | **GitHub Actions (macOS)** | Usás workflows de GitHub con la directiva `runs-on: macos-latest` y herramientas como *Fastlane* para compilar y firmar. | Minutos incluidos en planes de GitHub. | Automatización técnica pura para repositorios abiertos o privados con minutos libres. |

---

## 3. Comparativa de Código: SwiftUI Nativo vs. Kotlin Multiplatform (KMP)

¿Cómo llevamos el código actual de Kotlin a iOS? En la industria existen dos grandes caminos:

### 3.1 Opción 1: SwiftUI Nativo asistido por IA (Antigravity) – *La más recomendada para Estoy Ok*
* **Cómo funciona:** Antigravity toma el código Kotlin de `android-native/` y el contrato Swagger, y escribe el proyecto nativo de iOS en **Swift y SwiftUI**.
* **Por qué es la mejor para Estoy Ok:**
  * Estoy Ok depende fuertemente de **sensores críticos del sistema operativo**: rastreo GPS en segundo plano (`CoreLocation`), detección de choques (`CoreMotion`), grabación de audio de emergencia (`AVFoundation`) y suscripciones (`StoreKit`).
  * Los revisores del App Store de Apple son sumamente estrictos con las apps de ubicación de fondo. Las apps en **SwiftUI puro** se aprueban con mucha mayor facilidad, no sufren incompatibilidades con nuevas versiones de iOS y tienen el menor consumo de batería posible en iPhones.
  * **La ventaja con IA:** Históricamente, la única traba para hacer SwiftUI era tener que pagar un desarrollador de iOS aparte. Con Antigravity, la escritura del código Swift es automática y a costo cero.

### 3.2 Opción 2: Kotlin Multiplatform (KMP)
* **Cómo funciona:** Se reorganiza el proyecto para que el código en Kotlin actual viva en una carpeta compartida (`commonMain`). El compilador oficial de Kotlin (*Kotlin/Native*) lo compila directamente a binarios para iPhone.
* **Ventajas:** Compartís la lógica de negocio (modelos, llamadas de red, cálculo de distancias Haversine) en un solo lugar.
* **Desventajas para Estoy Ok:** Para el GPS de fondo persistente, las notificaciones push de APNs y las compras de Apple StoreKit, de todos modos hay que escribir código nativo en Swift mediante puentes (*expect/actual*).

---

## 4. El Rol de Antigravity en la Creación de la App iOS

Cuando se decida iniciar el port a iOS, el reparto de tareas será el siguiente:

1. **Antigravity (El Ingeniero de Software):**
   * Lee la arquitectura actual de Android (`android-native/`) y el backend Laravel.
   * Crea el proyecto de Xcode con arquitectura MVVM limpia en SwiftUI.
   * Traduce las pantallas de Jetpack Compose a vistas reactivas en SwiftUI.
   * Conecta las llamadas de red usando `URLSession` / `async-await` consumiendo los mismos endpoints de Laravel.
   * Implementa el servicio de fondo con `CoreLocation` (`startMonitoringSignificantLocationChanges` y `allowsBackgroundLocationUpdates`).
   * Configura las compras de suscripciones mediante Apple **StoreKit 2**.
2. **La Mac en la Nube (La Fábrica de Empaquetado):**
   * Abre el proyecto generado por Antigravity.
   * Ejecuta la compilación oficial de Xcode.
   * Firma digitalmente con tu cuenta de Apple Developer.
   * Sube el `.ipa` a **TestFlight** para pruebas en celulares reales y posterior envío a revisión pública en el App Store.

---

## 5. Requisitos Obligatorios de Apple (Ineludibles)

Independientemente de la tecnología elegida y de compilar en la nube, Apple exige para cualquier app pública:

1. **Cuenta de Desarrollador (Apple Developer Program):**
   * Costo: **$99 USD al año**.
   * Te permite crear certificados de firma, identificadores de Bundle (`com.estoyok.app`), notificaciones APNs y acceso a **App Store Connect**.
2. **Acceso a TestFlight:**
   * Es la plataforma oficial de Apple (incluida en los $99/año) para distribuir la app de forma privada a hasta 10.000 evaluadores antes del lanzamiento abierto.
3. **Revisión Humana de Políticas de Privacidad:**
   * Al igual que en Google Play, Apple exige un enlace público a la Política de Privacidad (ya creada y online en `https://estoyok24.com/politica-de-privacidad`) y la justificación clara de por qué se usa ubicación en segundo plano.

---

## 6. Perspectiva de la Industria: Cómo Trabajan los Grandes

* **Empresas como Netflix, Uber, Shopify o Mercado Libre:**  
  **Nunca** compilan apps para producción desde la laptop personal de un programador. Utilizan exactamente esta misma infraestructura: granjas masivas de Macs en la nube (AWS EC2 Mac, MacStadium, Bitrise) donde los pipelines de CI/CD automatizan la compilación y el envío al App Store.
* **Estrategia para el mercado de América Latina (LATAM):**  
  En Argentina y la región, Android abarca entre el **85% y el 90% de los usuarios**. Es una práctica estándar de las mejores startups tecnológicas (como Ualá o PedidosYa) validar el producto, probar el modelo de negocio y conseguir los primeros clientes de pago en Android primero, y luego financiar la expansión a iOS con los propios ingresos generados.

---

## 7. Hoja de Ruta para Cuando se Decida Retomar

Cuando sea el momento de lanzar Estoy Ok en iOS, los pasos a seguir serán:

- [ ] **Paso 1:** Registrar la cuenta en [developer.apple.com](https://developer.apple.com) ($99 USD/año).
- [ ] **Paso 2:** Alquilar una instancia básica en **MacInCloud** o configurar **Codemagic**.
- [ ] **Paso 3:** Indicarle a Antigravity: *"Crear el cliente iOS en SwiftUI replicando la arquitectura de android-native"*.
- [ ] **Paso 4:** Probar el build en TestFlight en un iPhone físico.
- [ ] **Paso 5:** Enviar a revisión en App Store Connect y habilitar la distribución para los 177 países.
