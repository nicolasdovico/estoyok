# Plan Maestro de Contenido: Instagram Stories e Historias Destacadas (@estoyok24)

> **Documento Estratégico y Operativo de Producción Audiovisual**  
> **Fecha de Actualización:** 29 de Septiembre de 2026  
> **Cuenta Oficial:** [@estoyok24](https://instagram.com/estoyok24)  
> **Herramientas de Producción:** Google Flow (Avatar AI) + CapCut / Premiere + Python (Generador de Assets)  
> **Estado:** Aprobado para Ejecución.

---

## 1. Metodología de Trabajo: Formato "Pantalla Dividida (50/50)"

Para no abrumar al usuario con decenas de historias cortas ni complicar la edición con pantallas verdes o recortes de fondo, utilizaremos la arquitectura de **Pantalla Dividida (Split Screen 50/50)** en resolución vertical oficial **1080 x 1920 px (9:16)**.

```
┌──────────────────────────────────────────────┐
│                                              │
│             MITAD SUPERIOR (50%)             │  ← Video del Personaje de Google Flow
│            [ Personaje Hablando ]            │    hablando a cámara (tomas de ≤ 10 seg).
│                                              │    Fondo original, sin recortar.
├──────────────────────────────────────────────┤
│                                              │
│             MITAD INFERIOR (50%)             │  ← Captura Real de la App Estoy Ok
│            [ Interfaz Real Estoy Ok ]        │    en marco de smartphone estilizado
│                                              │    con flechas y elementos de foco.
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

## 2. Inventario de Capturas de Pantalla (Existentes vs Faltantes)

Para que el script genere las imágenes de la mitad inferior, necesitamos el catálogo completo de capturas reales:

### ✅ Capturas YA Disponibles en el Proyecto:
1. `docs/imagen06.jpg` → Pantalla de Inicio / Login con botón de Google.
2. `docs/imagen01.jpg` → Mapa interactivo en tiempo real con miembros del núcleo y batería.
3. `docs/imagen02.jpg` → Pestaña "Estoy OK" con el botón verde gigante y temporizador.
4. `docs/imagen04.jpg` → Historial de reportes y auto check-in ("Vía Wi-Fi Seguro").
5. `docs/zoom.jpg` → Mapa con radio perimetral celeste de Zona Segura.
6. `docs/rastreo.jpg` → Trazado de ruta de viaje en el mapa con línea turquesa.
7. `docs/imagen03.jpg` → Pantalla de Protección Vehicular con Score de manejo (70 pts).
8. `docs/imagen05.jpg` → Desglose de viajes y eventos de velocidad en auto.
9. `docs/crash_alert_screen.jpg` → Pantalla roja de impacto detectado (Fuerza G 4.80G) con cuenta regresiva.
10. `docs/crash_alert_message_whatsapp.jpg` → Captura del mensaje real recibido en WhatsApp con enlace de rescate.

---

### 📸 Capturas FALTANTES (A tomar desde tu celular):
Para que la explicación sea 100% precisa, tomá estas 4 capturas en tu app y guardalas en la carpeta `docs/`:

| Archivo a Guardar | Pantalla a Capturar | Qué debe mostrar |
|---|---|---|
| `docs/captura_crear_nucleo.jpg` | Pestaña Familia / Núcleo | La tarjeta donde figura tu **Código de 6 dígitos** para invitar a familiares. |
| `docs/captura_notif_zona.jpg` | Notificación del Sistema | La notificación emergente de Android que dice *"Nicolás llegó a Casa"* o *"Salió de Escuela"*. |
| `docs/captura_modal_contactos.jpg` | Pestaña Estoy OK | El modal abierto de **Contactos SOS** donde se ve el campo para cargar el WhatsApp del contacto. |
| `docs/captura_rescate_web.jpg` | Navegador Web | La pantalla pública de emergencia (`/emergencia/[uuid]`) con el botón **"Voy en camino"**. |

---

## 3. Guiones Palabra por Palabra con Referencias Exactas (≤ 10 Segundos por Toma)

Cada toma está cronometrada para un ritmo natural de voz (**18 a 23 palabras = 8 a 9.5 segundos**) e incluye la **referencia exacta de navegación** (pestaña, botón o menú) para que el usuario nunca tenga que adivinar dónde está cada función.

---

### 📁 Destacada 1: 🚀 Empezá Acá (Paso a Paso)

#### 🎬 Historia 1: Tu cuenta y tus opciones de acceso (Video final de 19 seg = Toma 1 + Toma 2)
* **Toma 1 (Google Flow - 9 seg / 20 palabras):**
  > *"¿Querés cuidar a tu familia sin invadir su privacidad? Te muestro cómo crear tu cuenta en menos de un minuto."*
  * *Visual Inferior:* `imagen06.jpg` (Pantalla de inicio y bienvenida).
* **Toma 2 (Google Flow - 9 seg / 22 palabras):**
  > *"En la pantalla inicial, tocá 'Continuar con Google' para entrar directo, o 'Registrate aquí' para hacerlo con tu correo y contraseña."*
  * *Visual Inferior:* `imagen06.jpg` (Flechas señalando el botón de Google y el enlace inferior de registro).

#### 🎬 Historia 2: Invitar a la Familia (Video final de 19 seg = Toma 3 + Toma 4)
* **Toma 3 (Google Flow - 9 seg / 23 palabras):**
  > *"Arriba en el mapa, tocá 'Familia' y elegí 'Crear Núcleo'. La app te da este código de seis dígitos para compartirles."*
  * *Visual Inferior:* `captura_crear_nucleo.jpg` (Flecha señalando botón Familia arriba y código de 6 dígitos).
* **Toma 4 (Google Flow - 10 seg / 23 palabras):**
  > *"Ellos tocan 'Unirse al Núcleo', ingresan tu código y listo: ya se ven en el mapa en vivo con su batería."*
  * *Visual Inferior:* `imagen01.jpg` (Mapa con pines familiares y niveles de batería).

---

### 📁 Destacada 2: 🟢 El Botón "Estoy OK"

#### 🎬 Historia 1: El Check-in Diario (Video final de 19 seg = Toma 1 + Toma 2)
* **Toma 1 (Google Flow - 9 seg / 22 palabras):**
  > *"En la barra inferior, tocá la pestaña central 'Estoy OK'. Este botón verde es el diferencial exclusivo de nuestra aplicación."*
  * *Visual Inferior:* `imagen02.jpg` (Flecha señalando la pestaña central y el botón verde).
* **Toma 2 (Google Flow - 9 seg / 21 palabras):**
  > *"Tocalo una vez al día para reportarte. Arriba ves el reloj regresivo: mientras corra, tu familia sabe que estás seguro."*
  * *Visual Inferior:* `imagen02.jpg` (Flecha señalando la tarjeta "Protegido y a Salvo" con el temporizador).

#### 🎬 Historia 2: Alerta por WhatsApp & Wi-Fi (Video final de 19 seg = Toma 3 + Toma 4)
* **Toma 3 (Google Flow - 9 seg / 22 palabras):**
  > *"¿Y si te olvidás? Si el reloj llega a cero, el sistema avisa automáticamente a tus contactos por WhatsApp con tu ubicación."*
  * *Visual Inferior:* `crash_alert_message_whatsapp.jpg` (Mensaje de WhatsApp recibido con enlace a mapa de rescate).
* **Toma 4 (Google Flow - 10 seg / 23 palabras):**
  > *"O mejor: entrá a Ajustes, vinculá el Wi-Fi de tu casa y la app confirmará tu bienestar sola cada vez que llegues."*
  * *Visual Inferior:* `imagen04.jpg` (Flecha señalando el historial con reporte "Vía Wi-Fi Seguro").

---

### 📁 Destacada 3: 📍 Zonas Seguras

#### 🎬 Historia 1: Crear Zonas (Video final de 18 seg = Toma 1 + Toma 2)
* **Toma 1 (Google Flow - 9 seg / 21 palabras):**
  > *"Chau al 'avisame cuando llegues'. En la pestaña Mapa, tocá el selector de Zonas Seguras para agregar Casa, Escuela o Trabajo."*
  * *Visual Inferior:* `zoom.jpg` (Flecha señalando el selector de Zonas Seguras arriba en el mapa).
* **Toma 2 (Google Flow - 9 seg / 21 palabras):**
  > *"Marcás el lugar exacto en el mapa, ajustás el radio del círculo y la app empezará a monitorear en segundo plano."*
  * *Visual Inferior:* `zoom.jpg` (Círculo perimetral celeste en el mapa).

#### 🎬 Historia 2: Avisos y Rutas (Video final de 19 seg = Toma 3 + Toma 4)
* **Toma 3 (Google Flow - 9 seg / 21 palabras):**
  > *"Cuando un familiar entra o sale del perímetro, recibís una notificación automática al celular sin que tengan que escribirte."*
  * *Visual Inferior:* `captura_notif_zona.jpg` (Notificación push emergente en el teléfono).
* **Toma 4 (Google Flow - 10 seg / 23 palabras):**
  > *"Y tocando sobre cualquier familiar en el mapa, podés ver el trazado de calles por donde se movieron durante el día."*
  * *Visual Inferior:* `rastreo.jpg` (Línea turquesa de recorrido en el mapa).

---

### 📁 Destacada 4: 🚨 Contactos SOS & WhatsApp

#### 🎬 Historia 1: Cargar Contactos (Video final de 18 seg = Toma 1 + Toma 2)
* **Toma 1 (Google Flow - 9 seg / 22 palabras):**
  > *"En la pestaña 'Estoy OK', tocá 'Contactos SOS'. Tus contactos de emergencia no necesitan tener la app instalada para ayudarte."*
  * *Visual Inferior:* `imagen02.jpg` (Flecha señalando el botón "Contactos SOS").
* **Toma 2 (Google Flow - 9 seg / 20 palabras):**
  > *"Presioná 'Agregar Contacto', cargá el celular de tus padres o amigos y van a recibir todas las alertas por WhatsApp."*
  * *Visual Inferior:* `captura_modal_contactos.jpg` (Formulario abierto de nuevo contacto).

#### 🎬 Historia 2: Botón de Pánico y Rescate (Video final de 19 seg = Toma 3 + Toma 4)
* **Toma 3 (Google Flow - 10 seg / 22 palabras):**
  > *"Ante un peligro, tocá el botón rojo 'SOS' arriba a la derecha. Les llegará tu mapa en vivo con quince segundos de audio."*
  * *Visual Inferior:* `imagen02.jpg` (Flecha señalando el botón rojo SOS en la cabecera).
* **Toma 4 (Google Flow - 9 seg / 22 palabras):**
  > *"En ese mensaje, tus contactos tocan 'Voy en camino' y toda la familia se entera al instante de que la ayuda comenzó."*
  * *Visual Inferior:* `captura_rescate_web.jpg` (Botón "Voy en camino" en la web de emergencia).

---

### 📁 Destacada 5: 🚗 En el Auto (Seguridad Vial)

#### 🎬 Historia 1: Puntuación de Manejo (Video final de 19 seg = Toma 1 + Toma 2)
* **Toma 1 (Google Flow - 9 seg / 22 palabras):**
  > *"En la barra inferior, tocá la pestaña 'Vehículo'. La app incluye un módulo completo para cuidar a tu familia cuando maneja."*
  * *Visual Inferior:* `imagen03.jpg` (Flecha señalando la pestaña "Vehículo" abajo).
* **Toma 2 (Google Flow - 10 seg / 23 palabras):**
  > *"Acá ves el puntaje semanal de cada conductor y detectás excesos de velocidad, frenadas bruscas o uso del celular al volante."*
  * *Visual Inferior:* `imagen03.jpg` (Flechas señalando el Score circular de 70 pts y las métricas).

#### 🎬 Historia 2: Detección de Choques (Video final de 19 seg = Toma 3 + Toma 4)
* **Toma 3 (Google Flow - 9 seg / 22 palabras):**
  > *"Si los sensores detectan un choque por Fuerza G, en pantalla aparece esta alerta roja de quince segundos para cancelar si estás bien."*
  * *Visual Inferior:* `crash_alert_screen.jpg` (Pantalla roja con Fuerza G 4.80G y botón de cancelar).
* **Toma 4 (Google Flow - 10 seg / 23 palabras):**
  > *"Si no cancelás a tiempo, el sistema avisa automáticamente a tu familia por WhatsApp que hubo un impacto con tu ubicación."*
  * *Visual Inferior:* `crash_alert_message_whatsapp.jpg` (Mensaje de WhatsApp de impacto vehicular).

---

### 📁 Destacada 6: 🔋 Batería & Privacidad

#### 🎬 Historia 1: Batería Inteligente (Video final de 19 seg = Toma 1 + Toma 2)
* **Toma 1 (Google Flow - 9 seg / 21 palabras):**
  > *"¿Te preocupa que el GPS te gaste la batería? Estoy Ok fue desarrollada con algoritmos inteligentes de muy bajo consumo."*
  * *Visual Inferior:* `imagen01.jpg` (Detalle de nivel de batería en el mapa).
* **Toma 2 (Google Flow - 9 seg / 21 palabras):**
  > *"Cuando estás quieto en un lugar, el GPS se duerme por completo. Solo se despierta si detecta movimiento real o una emergencia."*
  * *Visual Inferior:* Imagen de sensor en reposo.

#### 🎬 Historia 2: Privacidad Garantizada (Video final de 19 seg = Toma 3 + Toma 4)
* **Toma 3 (Google Flow - 9 seg / 22 palabras):**
  > *"El permiso de 'Ubicación todo el tiempo' es solo para que las alertas de llegada y el SOS funcionen con el celular bloqueado."*
  * *Visual Inferior:* Diálogo de Prominent Disclosure de Ubicación.
* **Toma 4 (Google Flow - 9 seg / 20 palabras):**
  > *"Tus datos están 100% cifrados. No vendemos información a terceros ni mostramos anuncios. Tu privacidad familiar es sagrada."*
  * *Visual Inferior:* Icono de candado / Privacidad.

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
