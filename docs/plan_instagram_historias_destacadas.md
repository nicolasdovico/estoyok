# Plan Maestro de Contenido: Instagram Stories e Historias Destacadas (@estoyok24)

> **Documento Estratégico y Operativo de Producción Audiovisual**  
> **Fecha de Actualización:** 29 de Septiembre de 2026  
> **Cuenta Oficial:** [@estoyok24](https://instagram.com/estoyok24)  
> **Herramientas de Producción:** Google Flow (Avatar AI) + CapCut / Premiere + Python (Generador de Assets)  
> **Estado:** Aprobado para Ejecución.

---

## 1. Metodología de Trabajo: Formato "FaceCam / Burbuja Flotante (PiP)"

Tras validar en edición que el formato dividido 50/50 reducía el tamaño de la interfaz de la app haciéndola ilegible y sobrecargando la atención en el presentador, se evolucionó al formato estándar de la industria tech: **FaceCam / Burbuja Flotante**.

La captura de la app se muestra centrada a **escala nativa 1:1 (720 x 1600 px)** en el lienzo vertical oficial **1080 x 1920 px (9:16)**, manteniendo total legibilidad en textos, botones y flechas de foco. El video del avatar de Google Flow se sitúa en una **burbuja circular flotante** en la esquina superior.

```
┌──────────────────────────────────────────────┐
│  [  Burbuja  ]                               │  ← Video del Avatar 1:1 recortado en
│  [  Avatar   ]                               │    círculo suave (30% de escala)
│                                              │
│         ┌──────────────────────────┐         │
│         │   MARCO SMARTPHONE       │         │  ← Captura Real de Estoy Ok a escala
│         │   720 x 1600 px          │         │    nativa 1:1, nítida, con botones
│         │   (Centrado y Completo)  │         │    gigantes y flechas indicadoras.
│         │                          │         │
│         │   [ Botón Estoy OK ]     │         │
│         └──────────────────────────┘         │
│                                              │
└──────────────────────────────────────────────┘
```

### 1.1 Regla de Oro: Fusión de Clips en el Editor
* Google Flow genera clips de un **máximo de 10 segundos** (~20 a 24 palabras por toma).
* En el editor (CapCut / Premiere), se unen **2 tomas continuas** de Google Flow para exportar **1 historia de 20 segundos fluidos**.
* **Resultado en Instagram:** Cada carpeta destacada tendrá **únicamente 2 o 3 historias en total** (máximo 3 puntos arriba). El usuario aprende toda la función en menos de 45 segundos sin cansarse.

### 1.2 Proporciones y Dimensiones del Video en Google Flow
En Instagram Stories el lienzo total mide **1080 x 1920 px**, por lo que la mitad superior asignada al personaje mide **1080 x 960 px**:

* **Opción Recomendada en Google Flow: Cuadrado (1:1 o 1080 x 1080 px) ⭐**
  * Al medir `1080 x 1080`, encaja de forma casi idéntica en la mitad superior de `1080 x 960` sin cortar nada del personaje (encuadre plano medio desde el pecho hacia arriba).
* **Opción Alternativa: Horizontal (16:9 o 1920 x 1080 px)**
  * Al insertarse en el lienzo vertical ocupa `1080 x 608 px`. Deja un margen superior limpio ideal para colocar títulos de cabecera o el logo de Estoy Ok.
* **Si Google Flow solo permite exportar en Vertical (9:16 o 1080 x 1920 px):**
  * Sirve perfectamente. En el editor de video (CapCut/Premiere) se importa el clip vertical, se achica (escala) y se arrastra a la mitad superior, o se utiliza la herramienta de recorte (*Crop*) para encuadrar al personaje de la cintura para arriba.

---

## 2. Catálogo Oficial de Piezas Visuales Listas para Usar (`docs/instagram_stories_assets/`)

Se desarrollaron las **13 plantillas definitivas en resolución oficial 1080 x 1920 px (9:16)**. Cada archivo tiene la mitad superior limpia (lista para el avatar de Google Flow sin títulos invasivos) y la mitad inferior con el smartphone flotante completo y las flechas de foco.

Para facilitar la ubicación en tu editor de video (Kdenlive, CapCut, Premiere), cada pieza cuenta con su **nombre correlativo directo (`imagen01.png` a `imagen13.png`)** y su versión descriptiva:

| # Correlativo | Archivo Descriptivo en Assets | Carpeta Destacada | Video / Historia Asociada | Qué muestra en el celular |
|:---:|---|---|---|---|
| [`imagen01.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen01.png) | [`imagen01_destacada1_h1_login.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen01_destacada1_h1_login.png) | 🚀 Empezá Acá | **Historia 01** (Tomas 1 y 2) | Flechas señalando **"Continuar con Google"** y **"Regístrate aquí"**. |
| [`imagen02.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen02.png) | [`imagen02_destacada1_h2_nucleo.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen02_destacada1_h2_nucleo.png) | 🚀 Empezá Acá | **Historia 02** (Toma 3: 0 a 9s) | Foco y flecha al botón para copiar tu **Código Familiar de 10 caracteres**. |
| [`imagen03.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen03.png) | [`imagen03_destacada1_h3_mapa.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen03_destacada1_h3_mapa.png) | 🚀 Empezá Acá | **Historia 02** (Toma 4: 9 a 19s) | Mapa en tiempo real con miembros del núcleo y batería en vivo. |
| [`imagen04.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen04.png) | [`imagen04_destacada2_h1_estoy_ok.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen04_destacada2_h1_estoy_ok.png) | 🟢 El Botón Estoy OK | **Historia 03** (Tomas 5 y 6) | Foco circular y flecha apuntando al **Botón verde de Bienestar**. |
| [`imagen05.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen05.png) | [`imagen05_destacada2_h2_alerta_whatsapp.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen05_destacada2_h2_alerta_whatsapp.png) | 🟢 El Botón Estoy OK | **Historia 04** (Tomas 7 y 8) | Captura del **mensaje real de WhatsApp** recibido con mapa de rescate. |
| [`imagen06.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen06.png) | [`imagen06_destacada3_h1_zonas_crear.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen06_destacada3_h1_zonas_crear.png) | 📍 Zonas Seguras | **Historia 05** (Tomas 9 y 10) | Delimitación de Zona Segura y radio perimetral celeste en el mapa. |
| [`imagen07.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen07.png) | [`imagen07_destacada3_h2_zonas_notif.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen07_destacada3_h2_zonas_notif.png) | 📍 Zonas Seguras | **Historia 06** (Tomas 11 y 12) | **Notificación push real de Android:** *"Alerta de Perímetro: Llegada a Casa"*. |
| [`imagen08.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen08.png) | [`imagen08_destacada4_h1_contactos_sos.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen08_destacada4_h1_contactos_sos.png) | 🚨 Contactos SOS | **Historia 07** (Tomas 13 y 14) | Modal abierto para cargar el WhatsApp de tus contactos de emergencia. |
| [`imagen09.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen09.png) | [`imagen09_destacada4_h2_rescate_web.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen09_destacada4_h2_rescate_web.png) | 🚨 Contactos SOS | **Historia 08** (Tomas 15 y 16) | Pantalla pública de emergencia con el botón **"Voy en camino"**. |
| [`imagen10.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen10.png) | [`imagen10_destacada5_h1_conduccion.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen10_destacada5_h1_conduccion.png) | 🚗 Seguridad Vial | **Historia 09** (Tomas 17 y 18) | Pestaña Vehículo con el **Score semanal de manejo (70 pts)**. |
| [`imagen11.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen11.png) | [`imagen11_destacada5_h2_choques.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen11_destacada5_h2_choques.png) | 🚗 Seguridad Vial | **Historia 10** (Tomas 19 y 20) | Pantalla roja de impacto detectado (Fuerza G 4.80G) con cuenta regresiva. |
| [`imagen12.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen12.png) | [`imagen12_destacada6_h1_bateria.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen12_destacada6_h1_bateria.png) | 🔋 Batería & Privacidad | **Historia 11** (Tomas 21 y 22) | Panel familiar con batería en vivo (`⚡ 77%`) y badge de estadía en reposo (`En Casa 3h 49m`). |
| [`imagen13.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen13.png) | [`imagen13_destacada6_h2_privacidad.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen13_destacada6_h2_privacidad.png) | 🔋 Batería & Privacidad | **Historia 12** (Tomas 23 y 24) | Modal de Condiciones con foco en permiso todo el tiempo y garantía de no venta de datos. |

---

## 3. Guiones Palabra por Palabra con Referencias Exactas (≤ 10 Segundos por Toma)

Cada toma está cronometrada para un ritmo natural de voz (**18 a 23 palabras = 8 a 9.5 segundos**) e incluye la **referencia exacta de navegación** y el **archivo visual exacto** que debés colocar en la pista V1 de tu editor de video.

---

### 📁 Destacada 1: 🚀 Empezá Acá (Paso a Paso)

#### 🎬 Historia 01: Tu cuenta y tus opciones de acceso (Video final de 19 seg = Toma 1 + Toma 2)
> 💡 **Pista V1 (Fondo):** Usar [`imagen01.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen01.png) (`imagen01_destacada1_h1_login.png`) durante todo el video (0 a 19 seg).
* **Toma 1 (Google Flow - 9 seg / 20 palabras):**
  > *"¿Querés cuidar a tu familia sin invadir su privacidad? Te muestro cómo crear tu cuenta en menos de un minuto."*
  * *Visual Inferior:* [`imagen01.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen01.png) (`imagen01_destacada1_h1_login.png`).
* **Toma 2 (Google Flow - 9 seg / 22 palabras):**
  > *"En la pantalla inicial, tocá 'Continuar con Google' para entrar directo, o 'Registrate aquí' para hacerlo con tu correo y contraseña."*
  * *Visual Inferior:* [`imagen01.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen01.png) (`imagen01_destacada1_h1_login.png`) con flechas señalando Google y Registro.

#### 🎬 Historia 02: Invitar a la Familia (Video final de 19 seg = Toma 3 + Toma 4)
> ⚠️ **Nota de Edición en Pista V1:** Esta historia utiliza **2 imágenes consecutivas**:
> * En los primeros **9 segundos (Toma 3)**: colocás [`imagen02.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen02.png) (`imagen02_destacada1_h2_nucleo.png`).
> * A partir del **segundo 9 hasta el final (Toma 4)**: cambiás a [`imagen03.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen03.png) (`imagen03_destacada1_h3_mapa.png`).
* **Toma 3 (Google Flow - 9 seg / 22 palabras):**
  > *"Arriba en el mapa, tocá 'Seleccionar Núcleo' y elegí 'Crear / Unirse'. La app te da este código de diez caracteres para compartirles."*
  * *Visual Inferior (0 a 9s):* [`imagen02.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen02.png) (`imagen02_destacada1_h2_nucleo.png`) con foco y flecha al código familiar de 10 dígitos.
* **Toma 4 (Google Flow - 10 seg / 23 palabras):**
  > *"Ellos tocan 'Unirse al Núcleo', ingresan tu código y listo: ya se ven en el mapa en vivo."*
  * *Visual Inferior (9 a 19s):* [`imagen03.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen03.png) (`imagen03_destacada1_h3_mapa.png`) mostrando el mapa con los pines familiares y niveles de batería.

---

### 📁 Destacada 2: 🟢 El Botón "Estoy OK"

#### 🎬 Historia 03: El Check-in Diario (Video final de 19 seg = Toma 5 + Toma 6)
> 💡 **Pista V1 (Fondo):** Usar [`imagen04.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen04.png) (`imagen04_destacada2_h1_estoy_ok.png`) durante todo el video (0 a 19 seg).
* **Toma 5 (Google Flow - 9 seg / 22 palabras):**
  > *"En la barra inferior, tocá la pestaña central 'Estoy OK'. Este botón verde es el diferencial exclusivo de nuestra aplicación."*
  * *Visual Inferior:* [`imagen04.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen04.png) (`imagen04_destacada2_h1_estoy_ok.png`) con badge flotante "Tocá 1 vez al día" y flecha indicadora al botón verde central.
* **Toma 6 (Google Flow - 9 seg / 21 palabras):**
  > *"Tocalo una vez al día para reportarte. Arriba ves el reloj regresivo: mientras corra, tu familia sabe que estás seguro."*
  * *Visual Inferior:* [`imagen04.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen04.png) (`imagen04_destacada2_h1_estoy_ok.png`) mostrando tarjeta "Protegido y a Salvo".

#### 🎬 Historia 04: Alerta por WhatsApp & Wi-Fi (Video final de 19 seg = Toma 7 + Toma 8)
> 💡 **Pista V1 (Fondo):** 
> - **0 a 9 seg:** Usar [`imagen05.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen05.png) (`imagen05_destacada2_h2_alerta_whatsapp.png`) mostrando el aviso de inactividad / bienestar en WhatsApp.
> - **9 a 19 seg:** Usar [`imagen05b.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen05b.png) mostrando la configuración de Wi-Fi Seguro en la pantalla de Ajustes.
* **Toma 7 (Google Flow - 9 seg / 22 palabras):**
  > *"¿Y si te olvidás? Si el reloj llega a cero, el sistema avisa automáticamente a tus contactos por WhatsApp con tu ubicación."*
  * *Visual Inferior (0 a 9s):* [`imagen05.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen05.png) (`imagen05_destacada2_h2_alerta_whatsapp.png`) con el chat de WhatsApp recibiendo el aviso de bienestar no confirmado y enlace de rescate.
* **Toma 8 (Google Flow - 10 seg / 23 palabras):**
  > *"O mejor: entrá a Ajustes, vinculá el Wi-Fi de tu casa y la app confirmará tu bienestar sola cada vez que llegues."*
  * *Visual Inferior (9 a 19s):* [`imagen05b.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen05b.png) (`imagen05b_destacada2_h2_wifi_ajustes.png`) mostrando la sección "Automatizaciones y Sensores" en Ajustes con el switch y nombre de red Wi-Fi.

---

### 📁 Destacada 3: 📍 Zonas Seguras

#### 🎬 Historia 05: Crear Zonas (Video final de 19 seg = Toma 9 + Toma 10)
> 💡 **Pista V1 (Fondo):** Usar [`imagen06.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen06.png) (`imagen06_destacada3_h1_zonas_crear.png`) durante todo el video (0 a 19 seg).
* **Toma 9 (Google Flow - 9 seg / 20 palabras):**
  > *"Chau al 'avisame cuando llegues'. Mantené presionado cualquier punto del mapa para fijar lugares clave como Casa, Colegio o Trabajo."*
  * *Visual Inferior:* [`imagen06.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen06.png) (`imagen06_destacada3_h1_zonas_crear.png`) con el mapa y la Zona Segura delimitada.
* **Toma 10 (Google Flow - 10 seg / 22 palabras):**
  > *"Elegís el radio del círculo y la app monitorea en segundo plano la llegada y salida de cada miembro del núcleo."*
  * *Visual Inferior:* [`imagen06.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen06.png) (`imagen06_destacada3_h1_zonas_crear.png`) con el círculo perimetral celeste en el mapa.

#### 🎬 Historia 06: Avisos y Rutas (Video final de 19 seg = Toma 11 + Toma 12)
> 💡 **Pista V1 (Fondo):** Usar [`imagen07.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen07.png) (`imagen07_destacada3_h2_zonas_notif.png`) durante todo el video (0 a 19 seg).
* **Toma 11 (Google Flow - 9 seg / 21 palabras):**
  > *"Cuando un familiar entra o sale del perímetro, recibís una notificación automática al celular sin que tengan que escribirte."*
  * *Visual Inferior:* [`imagen07.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen07.png) (`imagen07_destacada3_h2_zonas_notif.png`) con la notificación push emergente en Android.
* **Toma 12 (Google Flow - 10 seg / 23 palabras):**
  > *"Y tocando sobre cualquier familiar en el mapa, podés ver el trazado de calles por donde se movió durante el día."*
  * *Visual Inferior:* [`imagen07.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen07.png) (`imagen07_destacada3_h2_zonas_notif.png`).

---

### 📁 Destacada 4: 🚨 Contactos SOS & WhatsApp

#### 🎬 Historia 07: Cargar Contactos (Video final de 18 seg = Toma 13 + Toma 14)
> 💡 **Pista V1 (Fondo):** Usar [`imagen08.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen08.png) (`imagen08_destacada4_h1_contactos_sos.png`) durante todo el video (0 a 18 seg).
* **Toma 13 (Google Flow - 9 seg / 22 palabras):**
  > *"En la pestaña 'Estoy OK', tocá 'Contactos SOS'. Tus contactos de emergencia no necesitan tener la app instalada para ayudarte."*
  * *Visual Inferior:* [`imagen08.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen08.png) (`imagen08_destacada4_h1_contactos_sos.png`) con el modal abierto de contactos.
* **Toma 14 (Google Flow - 9 seg / 20 palabras):**
  > *"Presioná 'Agregar Nuevo Contacto Externo', cargá el celular de tus padres o amigos y van a recibir todas las alertas por WhatsApp."*
  * *Visual Inferior:* [`imagen08.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen08.png) (`imagen08_destacada4_h1_contactos_sos.png`) con el formulario de nuevo contacto.

#### 🎬 Historia 08: Botón de Pánico y Rescate (Video final de 19 seg = Toma 15 + Toma 16)
> 💡 **Pista V1 (Fondo):** Usar [`imagen09.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen09.png) (`imagen09_destacada4_h2_rescate_web.png`) durante todo el video (0 a 19 seg).
* **Toma 15 (Google Flow - 10 seg / 22 palabras):**
  > *"Ante un peligro, tocá el botón rojo 'SOS'. A tus contactos SOS, les llegará tu mapa en vivo con quince segundos de audio."*
  * *Visual Inferior:* [`imagen09.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen09.png) (`imagen09_destacada4_h2_rescate_web.png`).
* **Toma 16 (Google Flow - 9 seg / 22 palabras):**
  > *"En ese mensaje, tus contactos tocan 'Voy en camino' y toda la familia se entera al instante de que la ayuda comenzó."*
  * *Visual Inferior:* [`imagen09.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen09.png) (`imagen09_destacada4_h2_rescate_web.png`) con el botón "Voy en camino" en la web de emergencia.

---

### 📁 Destacada 5: 🚗 En el Auto (Seguridad Vial)

#### 🎬 Historia 09: Puntuación de Manejo (Video final de 19 seg = Toma 17 + Toma 18)
> 💡 **Pista V1 (Fondo):** Usar [`imagen10.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen10.png) (`imagen10_destacada5_h1_conduccion.png`) durante todo el video (0 a 19 seg).
* **Toma 17 (Google Flow - 9 seg / 22 palabras):**
  > *"En la barra inferior, tocá la pestaña 'Vehículo'. La app incluye un módulo completo para cuidar a tu familia cuando maneja."*
  * *Visual Inferior:* [`imagen10.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen10.png) (`imagen10_destacada5_h1_conduccion.png`) señalando la pestaña Vehículo.
* **Toma 18 (Google Flow - 10 seg / 23 palabras):**
  > *"Acá ves el puntaje semanal de cada conductor y detectás excesos de velocidad, frenadas bruscas o uso del celular al volante."*
  * *Visual Inferior:* [`imagen10.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen10.png) (`imagen10_destacada5_h1_conduccion.png`) con el Score de 70 pts y métricas.

#### 🎬 Historia 10: Detección de Choques (Video final de 19 seg = Toma 19 + Toma 20)
> 💡 **Pista V1 (Fondo):** Usar [`imagen11.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen11.png) (`imagen11_destacada5_h2_choques.png`) durante todo el video (0 a 19 seg).
* **Toma 19 (Google Flow - 9 seg / 22 palabras):**
  > *"Si los sensores detectan un choque por Fuerza G, en pantalla aparece esta alerta roja de quince segundos para cancelar si estás bien."*
  * *Visual Inferior:* [`imagen11.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen11.png) (`imagen11_destacada5_h2_choques.png`) con la pantalla roja de impacto y cuenta regresiva.
* **Toma 20 (Google Flow - 10 seg / 23 palabras):**
  > *"Si no cancelás a tiempo, el sistema avisa automáticamente a tu familia por WhatsApp que hubo un impacto con tu ubicación."*
  * *Visual Inferior:* [`imagen11.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen11.png) (`imagen11_destacada5_h2_choques.png`).

---

### 📁 Destacada 6: 🔋 Batería & Privacidad

#### 🎬 Historia 11: Batería Inteligente (Video final de 19 seg = Toma 21 + Toma 22)
> 💡 **Pista V1 (Fondo):** Usar [`imagen12.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen12.png) (`imagen12_destacada6_h1_bateria.png`) durante todo el video (0 a 19 seg).
* **Toma 21 (Google Flow - 9 seg / 21 palabras):**
  > *"¿Te preocupa que el GPS te gaste la batería? Estoy Ok fue desarrollada con algoritmos inteligentes de muy bajo consumo."*
  * *Visual Inferior:* [`imagen12.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen12.png) (`imagen12_destacada6_h1_bateria.png`) con foco y flecha verde apuntando al indicador de batería en vivo (`⚡ 77%`) en el panel de familiares.
* **Toma 22 (Google Flow - 9 seg / 21 palabras):**
  > *"Cuando estás quieto en un lugar, el GPS se duerme por completo. Solo se despierta si detecta movimiento real o una emergencia."*
  * *Visual Inferior:* [`imagen12.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen12.png) (`imagen12_destacada6_h1_bateria.png`) con foco celeste y flecha hacia la estadía en el mapa (*"Inmóvil = GPS en Reposo"*).

#### 🎬 Historia 12: Privacidad Garantizada (Video final de 19 seg = Toma 23 + Toma 24)
> 💡 **Pista V1 (Fondo):** Usar [`imagen13.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen13.png) (`imagen13_destacada6_h2_privacidad.png`) durante todo el video (0 a 19 seg).
* **Toma 23 (Google Flow - 9 seg / 22 palabras):**
  > *"El permiso de 'Ubicación todo el tiempo' es solo para que las alertas de llegada y el SOS funcionen con el celular bloqueado."*
  * *Visual Inferior:* [`imagen13.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen13.png) (`imagen13_destacada6_h2_privacidad.png`) con foco celeste y flecha hacia el Punto 2 (*"🛡️ Alertas con Celular Bloqueado"*).
* **Toma 24 (Google Flow - 9 seg / 20 palabras):**
  > *"Tus datos están 100% cifrados. No vendemos información a terceros ni mostramos anuncios. Tu privacidad familiar es sagrada."*
  * *Visual Inferior:* [`imagen13.png`](file:///home/usuario/aplicaciones/estoyok/docs/instagram_stories_assets/imagen13.png) (`imagen13_destacada6_h2_privacidad.png`) con foco verde y flecha hacia el Punto 6 (*"🔒 100% Cifrado • Sin Publicidad"*).

---

## 4. Guía de Uso del Script Generador (`docs/generate_instagram_stories.py`)

Desarrollaremos un script en Python que toma las capturas de la carpeta `docs/` y las procesa para la mitad inferior:

### 4.1 Qué hace el script automáticamente:
1. **Recorte y Escala 1080 x 960 px:** Ajusta la captura de la app para que encaje de forma exacta en la mitad inferior de la historia vertical.
2. **Marco de Smartphone Estilizado:** Le añade bisel metálico, bordes redondeados y reflejos oscuros (`#0B132B`).
3. **Puntos de Foco y Flechas de Indicación:** Añade flechas o círculos de foco en color Esmeralda (`#10B981`) señalando el botón exacto del que habla el avatar.
4. **Portadas Circulares para Destacadas:** Genera las 6 imágenes cuadradas (`1080 x 1080 px`) con los iconos (`🚀`, `🟢`, `📍`, `🚨`, `🚗`, `🔋`) listas para configurar las portadas en Instagram.

### 4.2 Cómo se ejecuta el script en la terminal:
```bash
# Ejecutar el generador desde la raíz del proyecto:
python3 docs/generate_instagram_stories.py
```
* Las imágenes procesadas se guardarán automáticamente en la carpeta `docs/instagram_stories/` listas para importar en tu editor de video.

---

## 5. Pasos Siguientes para Vos:
1. **Sacar las 4 capturas faltantes** en tu celular y guardarlas en `docs/`.
2. **Generar los clips de video en Google Flow** copiando y pegando los textos de cada toma.
3. Ejecutar el script generador para tener las imágenes de la mitad inferior listas para unir en CapCut.



### 📋 Plantilla Maestra para Google Flow (Copiar y Pegar)

    Animate the person in the attached reference image as a live-action presenter speaking directly to 
  camera. 
    - Character consistency: Maintain identical facial features, age, hairstyle, skin tone, clothing and
  appearance as the attached image.
    - Framing & Camera: Medium close-up (from chest up), centered in frame, looking straight into the
  camera lens with a warm, friendly, empathetic and trustworthy expression. 
    - Environment & Background: A warm, modern and cozy tech-oriented space (contemporary smart office or
  modern apartment living room). Sleek background with soft interior lighting, dark slate and navy
    accents, and subtle blurred emerald green and electric teal ambient lighting highlights. Shallow depth
    of field with beautiful soft cinematic bokeh.
    - Animation & Motion: Natural micro-expressions, subtle head nods, natural blinking, relaxed shoulder
  posture and smooth lip synchronization matching the spoken audio.
    - Audio & Voice: Clear voice in native Argentinian Spanish with an authentic Rioplatense accent
  (Buenos Aires cadence, warm, friendly and conversational tone, natural Argentinian voseo: 'vos / mirá /
    tocá').
    - Aspect ratio: 1:1 (Square 1080x1080).
    - Dialogue:
    "[PEGAR AQUÍ EL TEXTO DE LA TOMA]"

---

## 6. Guía de Montaje en Kdenlive: Efecto Burbuja Flotante (FaceCam)

### 6.1 Disposición de Pistas en la Línea de Tiempo
* **Pista V1 (Abajo):** Imagen de fondo generada (ej. `imagen01.png` / `imagen01_destacada1_h1_login.png` en `docs/instagram_stories_assets/`).
* **Pista V2 (Arriba):** Video cuadrado 1:1 del avatar de Google Flow.

---

### 6.2 Paso a Paso de Efectos en el Video (Pista V2)

#### Paso 1: Encuadrar solo la Cara en un Círculo Perfecto (Efecto: Forma alfa)
> 💡 **¿Por qué quedaba un óvalo/elipse estirada?**  
> Porque el lienzo vertical mide `1080 x 1920 px` (proporción 9:16). Si ponés el mismo número en X y en Y (ej. 500 y 500), el alto es casi el doble que el ancho y se deforma en huevo. Además, al poner `Posición Y: 500` queda centrado en el pecho.  
> Para que sea un **círculo redondo perfecto enfocado solo en el rostro**, aplicamos la **regla del 56%** (`Tamaño Y = Tamaño X × 0.56`) y subimos la posición Y hacia la cabeza:

1. Seleccioná el video en la pista **V2**.
2. En la pestaña de Efectos buscá **"Forma alfa"** (*Alpha shapes*) y arrastralo al video.
3. Configurá estos valores exactos:
   * **Forma:** `Elipse` *(así se llama la herramienta en Kdenlive)*.
   * **Posición X:** `500` *(centrado horizontalmente en la cara)*.
   * **Posición Y:** `350` *(apunta al rostro/ojos, excluyendo el cuerpo)*.
   * **Tamaño X:** `400` *(ancho que abraza la cabeza completa con buen aire)*.
   * **Tamaño Y:** `224` *(el 56% de 400: genera un **círculo redondo perfecto** sin deformarse en óvalo)*.
   * **Transición / Suavizado:** `3%` a `5%` *(borde suave y profesional)*.

*(En este momento vas a ver en el monitor su cabeza completa en un círculo grande perfecto).*

#### Paso 2: Achicar la Burbuja Proporcionalmente y Ubicarla en la Esquina (Efecto: Transformar)
1. En la pestaña de Efectos buscá **"Transformar"** (*Transform*) y arrastralo al video.
2. **Muy importante:** En la lista de efectos del clip, **"Transformar" debe quedar SIEMPRE DEBAJO de "Forma alfa"**.
3. En el efecto **Transformar**, configurá:
   * **Tamaño / Escala:** **`30%`** a **`35%`** *(¡acá es donde se achica proporcionalmente a tamaño avatar para el rincón!)*.
   * **Posición:** Arrastrá la burbuja con el mouse en el monitor hacia la **esquina superior derecha** (o poné `X: 720`, `Y: 80`).

---

### 6.3 Verificación del Orden en la Pila de Efectos
Para ver la lista de efectos aplicados al clip:
1. Hacé clic sobre el clip en la línea de tiempo.
2. Abrí el panel **"Pila de efectos"** (*Effect Stack*). Si no está visible, activalo desde el menú superior: **Ver $\rightarrow$ Pila de efectos**.
3. Verificá que el orden de lectura (de arriba hacia abajo) sea:
   ```text
   ┌──────────────────────────────────────┐
   │ [👁]  Forma alfa                     │  <-- 1° ARRIBA (Corta el círculo)
   ├──────────────────────────────────────┤
   │ [👁]  Transformar                    │  <-- 2° ABAJO (Escala al 30% y ubica)
   └──────────────────────────────────────┘
   ```
   *(Si quedaron invertidos, arrastrá la barra de Transformar hacia abajo o usá las flechas ▲ / ▼ en la cabecera).*

---

### 6.4 Replicar en todos los videos en 1 segundo
Una vez que te gustó cómo quedó ese primer clip:
1. Clic derecho sobre el clip configurado $\rightarrow$ **Copiar** (`Ctrl + C`).
2. Clic derecho sobre el siguiente video $\rightarrow$ **Pegar efectos** (*Paste Effects*).
3. ¡Listo! Se le aplicará la misma burbuja, tamaño y posición al instante.
