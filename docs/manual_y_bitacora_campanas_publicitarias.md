# Manual Operativo y Bitácora de Campañas Publicitarias (Meta Ads)

**Proyecto:** Estoy Ok: Localizador Familiar  
**Propósito:** Documento maestro de consulta obligatoria antes de configurar, lanzar, auditar o escalar cualquier campaña publicitaria paga. Registra las reglas técnicas y estratégicas aprendidas con dinero real para **no repetir errores de configuración ni de conversión**.

---

## 🛡️ PARTE 1: Las 6 Reglas de Oro Pre-Lanzamiento (Checklist Innegociable)

Antes de apretar el botón **"Publicar"** en cualquier campaña nueva, verificar uno por uno estos 6 puntos:

### 1. Ubicaciones: 100% Instagram (PROHIBIDO mezclar con Facebook)
* **Qué hacer:** En el Conjunto de Anuncios $\rightarrow$ **Ubicaciones manuales** $\rightarrow$ **Desmarcar por completo `Facebook`, `Messenger` y `Audience Network`**. Dejar tildado únicamente **`Instagram`** (`Instagram Reels` y opcionalmente `Instagram Stories`).
* **Por qué (Lección aprendida el 20/10/2026):** Si se dejan Facebook Reels e Instagram Reels juntos en el mismo Conjunto de Anuncios, el algoritmo de Meta detecta que el costo por impresión (CPM) en Facebook Argentina es más barato y **desvía el 96% del presupuesto a Facebook**, dejando a Instagram sin entrega. El público de Facebook genera reacciones ("Me gusta") pero no instala aplicaciones.

### 2. Sistema Operativo: Exclusivo "Solo dispositivos Android"
* **Qué hacer:** En Ubicaciones $\rightarrow$ *Mostrar más opciones* $\rightarrow$ *Dispositivos móviles y sistemas operativos específicos* $\rightarrow$ Seleccionar **"Solo dispositivos Android"** (Android 8.0+).
* **Por qué:** Mientras Estoy Ok esté publicado únicamente en Google Play Store, mostrar el anuncio en iOS (iPhone) desperdicia hasta el 40% del presupuesto en usuarios que no pueden instalar la app.

### 3. Plataforma de Creación: Siempre desde PC en Meta Ads Manager
* **Qué hacer:** Configurar todo desde [adsmanager.facebook.com](https://adsmanager.facebook.com) en computadora.
* **Por qué:** El botón *"Promocionar"* de la app móvil de Instagram no permite filtrar solo Android ni aislar ubicaciones con precisión, además de aplicar recargos si se gestiona desde iOS.

### 4. URL de Destino: Puente Nativo `https://estoyok24.com/descargar` (Sin redirección web forzada en Android)
* **Qué hacer:** Utilizar siempre `https://estoyok24.com/descargar` con el botón CTA **"Descargar"** o **"Instalar"**.
* **Regla técnica estricta en el código de `/descargar`:**
  * En dispositivos Android (`isAndroid`), la página debe disparar **únicamente el protocolo nativo** `intent://details?id=com.estoyok.app#Intent;scheme=market;package=com.android.vending;S.browser_fallback_url=...;end` y el botón verde en pantalla también debe apuntar a `intent://...`.
  * **PROHIBIDO colocar un `setTimeout(..., 400)` hacia `https://play.google.com/...` en Android:** Si se redirige a `https://play.google.com` por web después de unos milisegundos, el navegador interno (WebView) de Instagram/Facebook carga la página web de Google Play en lugar de abrir la app nativa de Play Store. En ese navegador interno el usuario **no tiene su sesión de Google iniciada**, Google le pide escribir su correo y contraseña de Gmail, y el 100% de los usuarios abandona la descarga.
  * Solo las computadoras de escritorio/notebooks (`!isAndroid`) deben redirigirse a la URL web `https://play.google.com/store/apps/details?id=com.estoyok.app`.

### 5. Estructura de Anuncios y Prueba A/B
* **Qué hacer:** Usar publicaciones existentes del feed de `@estoyok24` ("Usar publicación existente") para que todos los likes y comentarios queden acumulados como prueba social orgánica en el perfil de Instagram.
* **Presupuesto:** Campaña con presupuesto Advantage+ (CBO) dejando competir a los 3 ángulos de guión (Familia/Padres, Vivir Solo/Bienestar, Urgencia/SOS).

### 6. Auditoría a las 48-72 hs: Cruzar Meta Ads con Base de Datos Real
* **Nunca evaluar una campaña solo por "Me gusta" o por "Clics en el enlace" de Meta:** Siempre cruzar los clics de Meta con la cantidad real de usuarios nuevos registrados en el backend de producción (`api.estoyok24.com`).

---

## 📓 PARTE 2: Bitácora Histórica de Campañas y Aprendizajes

### 📅 Hito 1: Lanzamiento Inicial de Campaña — Sábado 17 de Octubre de 2026
* **Objetivo configurado:** Tráfico (Maximizar número de clics en el enlace).
* **Segmentación:** Argentina, 28 a 54 años, Solo dispositivos Android.
* **Anuncios activos (3 Reels en formato Talking Head A-Roll + B-Roll de la app):**
  1. `Reel 1 - Padres Auto` (*"Basta del 'Avisame cuando llegues'"* — Zonas Seguras y batería en vivo).
  2. `Reel 2 - Vivir Solo (Bienestar Diario)` (*"¿Y si vivís solo o te pasa algo?"* — Botón Estoy OK, Wi-Fi de casa y alerta WhatsApp).
  3. `Reel 3 - Urgencia (SOS Audio y Choques)` (*"La función que ojalá nunca uses"* — Botón SOS con audio de 15s y choque por Fuerza G).
* **Resolución del bloqueo de URL en Meta Ads:**
  * Meta Ads rechazaba/bloqueaba el enlace directo `https://play.google.com/store/apps/details?id=com.estoyok.app` en campañas con objetivo Tráfico.
  * Se creó la ruta puente [`frontend-web/src/app/descargar/page.tsx`](file:///home/usuario/aplicaciones/estoyok/frontend-web/src/app/descargar/page.tsx) (`https://estoyok24.com/descargar`) y su espejo en [`backend/routes/web.php`](file:///home/usuario/aplicaciones/estoyok/backend/routes/web.php) con etiquetas OpenGraph HTTP 200 para el bot de Meta (`facebookexternalhit`) y redirección por script para usuarios reales.

---

### 📅 Hito 2: Auditoría de las 72 Horas y Corrección de Errores — Martes 20 de Octubre de 2026

Al cumplirse 72 horas del lanzamiento, se realizó la auditoría completa porque se observaban reacciones en Facebook, nula interacción en Instagram y 0 registros nuevos en la base de datos de producción.

#### 1. Resultados Cuantitativos de los Anuncios en las Primeras 72 hs:

| Anuncio | Clics en el enlace | Costo por Clic (CPC) | Monto Gastado | Impresiones | CTR (% de clics) |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **🥇 Reel 1 - Padres Auto** | **218** | **$30,28 ARS** | $6.600,70 (65%) | 7.331 | **2,97%** |
| **🥈 Reel 2 - Vivir Solo (Bienestar Diario)** | **93** | **$26,82 ARS** | $2.493,83 (25%) | 2.360 | **3,94%** 🔥 |
| **🥉 Reel 3 - Urgencia (SOS Audio y Choques)** | **34** | **$30,38 ARS** | $1.032,90 (10%) | 1.095 | **3,10%** |
| **TOTALES (72 hs)** | **345 clics** | **$29,35 ARS** | **$10.127,43** | **10.786** | **3,20%** |

* **Conclusión sobre la creatividad (los 3 videos):**
  * **Rendimiento sobresaliente de atracción:** El CPC promedio ($29,35 ARS) fue **3 veces más económico** que el benchmark proyectado ($60 a $90 ARS), y el CTR promedio (**3,20%**) duplicó la media saludable del sector (1.5% - 2.0%).
  * **Destacado:** El **Reel 2 (Vivir Solo)** logró la mayor eficiencia relativa con un **CTR de 3,94%** y el clic más barato (**$26,82 ARS**), mientras que el **Reel 1 (Padres Auto)** traccionó el mayor volumen bruto (218 clics).

#### 2. Diagnóstico de los 2 Errores Críticos Detectados (Por qué 345 clics dieron 0 registros):

* **❌ Error #1: Fuga del 96% del presupuesto hacia Facebook**
  * **Hallazgo:** Al aplicar en Meta Ads Manager el filtro **Desglose $\rightarrow$ Por entrega $\rightarrow$ Plataforma**, se descubrió que de los **$10.127 ARS** gastados, **$9.700 ARS (96%) se habían ido a Facebook** y solo **~$400 ARS (4%) a Instagram**.
  * **Causa:** Teníamos tildados *Facebook Reels* e *Instagram Reels* juntos en el mismo conjunto de anuncios. Como el CPM en Facebook es mucho más barato, el algoritmo volcó casi todo el dinero a Facebook.
  * **Corrección aplicada hoy (20/10/2026):** Se editaron las ubicaciones manuales en Meta Ads Manager desmarcando Facebook, Messenger y Audience Network, dejando **100% exclusivo Instagram**.

* **❌ Error #2: El `setTimeout` de 400 ms en `/descargar` abría Google Play Web adentro del navegador de Meta pidiendo contraseña de Gmail**
  * **Hallazgo:** En la primera versión de [`frontend-web/src/app/descargar/page.tsx`](file:///home/usuario/aplicaciones/estoyok/frontend-web/src/app/descargar/page.tsx), el script ejecutaba:
    ```javascript
    window.location.replace(ANDROID_INTENT_URL);
    setTimeout(function() {
      window.location.replace("https://play.google.com/store/apps/details?id=com.estoyok.app");
    }, 400);
    ```
  * **Causa:** Cuando el usuario hacía clic en el anuncio en su celular Android, se abría el navegador interno de Facebook/Instagram. Apenas 0,4 segundos (`400ms`) después de invocar el `intent://`, el `setTimeout` se disparaba encima y forzaba al navegador interno de Meta a cargar la página web `https://play.google.com/...`. Además, el botón verde manual de la pantalla también apuntaba a `https://play.google.com/...`. Al quedar dentro del navegador web interno de Meta (donde el usuario no está logueado en Google), tocar *"Instalar"* le pedía ingresar su correo y contraseña de Gmail, provocando el abandono del 100% de los 345 clics.
  * **Corrección aplicada hoy (20/10/2026):**
    1. Se eliminó por completo el `setTimeout` de 400 ms en [`frontend-web/src/app/descargar/page.tsx`](file:///home/usuario/aplicaciones/estoyok/frontend-web/src/app/descargar/page.tsx) y [`backend/routes/web.php`](file:///home/usuario/aplicaciones/estoyok/backend/routes/web.php) (el propio protocolo `intent://` de Android ya gestiona su fallback mediante `S.browser_fallback_url`).
    2. Se apuntó el botón verde principal (`"📲 Abrir en Google Play Store (Gratis)"`) directamente al `ANDROID_INTENT_URL` (`intent://...`), garantizando que tanto la redirección automática como un toque manual en el botón abran **la app nativa de Google Play Store** del teléfono (donde el usuario ya está logueado y descarga con 1 solo toque).
    3. Se conservó en la rama `else` (`!isAndroid`) la redirección web normal para quienes hagan clic desde una computadora o notebook.

* **❌ Error #3: Nombre y Biografía Confusos en el Perfil de Instagram (`@estoyok24`)**
  * **Hallazgo:** El campo **Nombre** del perfil tenía el nombre personal (`"nicolas Dovico"`) y la **Presentación (Bio)** decía *"Cuenta para seguimiento activo y pasivo de usuarios"*, lo cual hacía que quienes visitaban el perfil creyeran que era una cuenta personal o una herramienta para medir seguidores de Instagram.
  * **Corrección aplicada el 20/10/2026:**
    - **Nombre:** `Estoy Ok | Seguridad y Localizador Familiar`
    - **Presentación (Bio):** Reemplazada por propuesta de valor clara (cuidado familiar en tiempo real, mapa GPS con avisos de llegada/salida, botón diario "Estoy OK" y SOS por WhatsApp + llamado a la acción de descarga gratuita).
    - **Enlace de Bio:** `https://estoyok24.com/descargar`.

---

### 📅 Hito 3: Auditoría de 48 Horas Post-Corrección de Instagram — Jueves 22 de Octubre de 2026

Al cumplirse 48 horas de haber forzado la exclusividad de Instagram y corregido la redirección nativa en `/descargar`, se analizaron las estadísticas oficiales de Meta Ads Manager (período 21 y 22 de octubre) y su impacto en la conversión real:

#### 1. Resultados Cuantitativos en Instagram Reels (21 y 22 de Octubre):

| Métrica / Elemento | Valor Registrado (48 hs) | Interpretación / Estado |
| :--- | :---: | :--- |
| **Gasto Total en el Período** | **$6.397,30 ARS** | Cumplimiento del presupuesto diario de $4.000 ARS. |
| **Gasto Exclusivo en Instagram** | **$6.126,94 ARS (96%)** | **Éxito total:** Se erradicó la fuga de presupuesto hacia Facebook ($267 residual). |
| **Impresiones en Instagram** | **1.323 impresiones** | Entrega fluida dentro del ecosistema de Instagram Reels. |
| **Clics en el enlace (Instagram)** | **93 clics** | Usuarios interesados que tocaron el CTA para descargar. |
| **Costo por Clic Promedio (CPC)** | **$65,88 ARS** | Muy saludable y por debajo del límite de $90 ARS. |

#### 2. Comportamiento y Desempeño por Reel en Instagram:
* **🥇 Reel 2 - Vivir Solo (Bienestar Diario):** Continúa siendo el creativo preferido del algoritmo de Meta y de la audiencia. Acaparó el **71% del presupuesto ($4.564,17 ARS)** y generó **69 clics** con un CPC de **$66,15 ARS**. Demuestra que el ángulo de "vivir solo / personas mayores / confirmación de bienestar" conecta con altísima empatía.
* **🥈 Reel 3 - Urgencia (SOS y Choques):** Tuvo el clic más económico de la cuenta (**$44,17 ARS**), logrando **16 clics** con apenas $706,65 ARS de gasto.
* **🥉 Reel 1 - Padres Auto:** Generó **16 clics** a **$70,41 ARS** con $1.126,48 ARS asignados.

#### 3. Hallazgos sobre Interacción Orgánica vs Comportamiento de Descarga:
* **Causa de nula interacción en el perfil de Instagram (0 comentarios/likes en la cuenta):** 
  En Instagram Reels con anuncios de conversión/utilidad, el usuario no abre el perfil de la cuenta ni comenta el video: el comportamiento natural es presionar directamente el botón de llamado a la acción (*"Descargar"* / *"Instalar"*) y salir de Instagram hacia la tienda. Los comentarios orgánicos acumulados requieren un volumen de impresiones significativamente superior (>15.000 a 20.000 vistas).
* **Confirmación de Descarga en Google Play Store:** Se constató en Google Play Console la primera descarga confirmada correspondiente al 20/10.

#### 4. Diagnóstico del Cuello de Botella (Tasa de Conversión en la Ficha de Play Store - CVR):
* Con 93 clics en el enlace desde Instagram, el paso de clics a descargas efectivas (~1% a 2%) revela que el cuello de botella actual no está en los anuncios (que traccionan clics muy baratos y calificados), sino en la **fase de decisión final dentro de la ficha de Google Play Store**.
* **Factores psicológicos de fricción identificados:**
  1. *Falta de Prueba Social Visible:* Al ser una app recién lanzada, el indicador de descargas iniciales y pocas valoraciones genera dudas en el usuario frío que llega desde un anuncio de video.
  2. *Latencia de Reporte de Play Console:* Google Play Console consolida los informes de adquisición con un retraso estándar de 24 a 48 horas, por lo que las descargas de los días 21 y 22 aún están en proceso de consolidación.
  3. *Coherencia Visual Reel $\rightarrow$ Ficha:* El Reel más exitoso es el Reel 2 (Vivir Solo / Botón Estoy OK diario). Las primeras 2 capturas de la ficha deben reflejar inmediatamente esa propuesta para convalidar lo prometido en el video.

#### 5. Plan de Acción y Recomendaciones Estratégicas:
1. **Mantener la Campaña Activa sin Reiniciar Aprendizaje:** La entrega en Instagram está estabilizada, barata y optimizada. Pausarla o editarla ahora interrumpiría el aprendizaje de Meta.
2. **Campaña de Reseñas Iniciales (Prueba Social Prioritaria):** Impulsar de forma inmediata entre 10 y 15 reseñas de 5 estrellas con comentarios de valor en Google Play Store (familiares, amigos y testers). Una calificación de 4.8+ con testimonios reales derriba la desconfianza del usuario que llega desde Instagram Reels.
3. **Optimización Visual de Capturas ASO en Play Store:** Asegurar que la captura del Botón "Estoy OK" y el aviso automático por WhatsApp ocupen las posiciones #1 y #2 en la ficha de la tienda.
4. **Monitoreo Continuo:** Cruzar los datos de adquisición de Google Play Console de los días 21 y 22 una vez que el reporte esté consolidado por Google.

---

### 📅 Hito 4: Auditoría del 9º Día y Consolidación de Instagram Reels — Lunes 26 de Octubre de 2026

Al acumularse 9 días de campaña (período total 17 al 26 de octubre de 2026), se auditaron las capturas oficiales de Meta Ads Manager (`desglose2610.png` y `metricas.png`) y se cruzaron con la telemetría de producción en PostgreSQL/FCM.

#### 1. Resultados Acumulados por Plataforma (17 al 26 de Octubre):

| Plataforma | Clics en el enlace | CPC Promedio | Monto Gastado | Impresiones |
| :--- | :---: | :---: | :---: | :---: |
| **Instagram** | **356** | **$63,46 ARS** | **$22.591,50 (63,0%)** | 5.646 |
| **Facebook** (Histórico inicial previo al 20/10) | 446 | $29,13 ARS | $12.989,89 (36,2%) | 14.136 |
| **Audience Network** | 5 | $39,80 ARS | $199,01 (0,5%) | 46 |
| **WhatsApp** | 1 | $78,64 ARS | $78,64 (0,2%) | 159 |
| **TOTAL GENERAL** | **808 clics** | **$44,38 ARS** | **$35.859,04** | **19.987** |

* **Consolidación en Instagram:** Del 22 al 26 de octubre se invirtieron ~$16.465 ARS casi de forma pura en Instagram Reels. La fuga de presupuesto hacia Facebook quedó 100% erradicada tras la corrección de ubicaciones manuales.

#### 2. Desempeño Acumulado por Creativo / Anuncio:

| Anuncio | Clics en el enlace | Costo por Clic (CPC) | Monto Gastado | Impresiones | CTR (% de clics) |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **🥇 Reel 2 - Vivir Solo (Bienestar Diario)** | **408** | **$53,25 ARS** | **$21.727,62 (60,6%)** | 7.445 | **5,48%** 🔥 |
| **🥈 Reel 1 - Padres Auto** | **336** | **$34,61 ARS** | **$11.628,45 (32,4%)** | 10.717 | **3,14%** |
| **🥉 Reel 3 - Urgencia (SOS Audio y Choques)** | **64** | **$39,11 ARS** | **$2.502,97 (7,0%)** | 1.825 | **3,51%** |
| **TOTALES** | **808 clics** | **$44,38 ARS** | **$35.859,04** | **19.987** | **4,04%** |

* **Hallazgo Clave sobre la Audiencia:**
  * El **Reel 2 (Vivir Solo / Bienestar)** es la pieza reina de la campaña: acumula un **CTR sobresaliente del 5,48%** (más del doble del benchmark de la industria) y acapara más del 60% del presupuesto de forma orgánica según el algoritmo de Meta.
  * El **Reel 1 (Padres Auto)** conserva el clic más barato (**$34,61 ARS**), garantizando volumen bruto de visitas a bajo costo.
  * El CTR global de la cuenta se ubica en **4,04%**, confirmando que los videos conectan con alta efectividad con el público argentino.

#### 3. Cruce con Base de Datos de Producción y Dispositivos:
* **Registros de usuarios nuevos:** Se pasó de 18 a 21 usuarios reales verificados (+3 usuarios con cuenta activa):
  * User #42: Ernesto (`jito51cer@gmail.com`) con token FCM Android activo.
  * User #43: Alfredo (`alfredoramosv@gmail.com`) verificado el 25/10 con token FCM Android activo.
  * User #44: Adriana Virginia Almada (`avalmada@gmail.com`) con token FCM Android activo.
* **Dispositivos registrados:** Subió de 63 a **68 dispositivos** (+5 nuevos dispositivos Android instalados).
* **Confirmación Técnica:** El flujo nativo del Android Intent en `/descargar` funciona sin fricción; los usuarios instalan desde Google Play Store y crean sus credenciales y tokens FCM de forma normal.
* **Costo por Instalación Efectiva (CPI):** Se estima en ~$3.200 ARS por instalación activa en este período de arranque.

#### 4. Directivas Operativas Vigentes:
1. **Mantener Anuncios Activos:** No pausar ningún Reel; los 3 anuncios conviven con métricas muy saludables y complementarias.
2. **Presupuesto Estable:** Continuar en $4.000 ARS diarios para consolidar la entrega en Instagram sin reiniciar la fase de aprendizaje.
3. **Foco en Conversión en Tienda (ASO y Reseñas):** Fortalecer la prueba social (reseñas de 5 estrellas con testimonios) para reducir la desconfianza del usuario frío y elevar el ratio de visita a instalación.

---

### 📅 Hito 5: Auditoría del 17º Día, Análisis del Embudo Completo (Google Play) y Reajuste Estratégico — Martes 3 de Noviembre de 2026

Al completarse 17 días desde el lanzamiento de la campaña (16 de octubre al 3 de noviembre de 2026), se auditaron las capturas oficiales de Meta Ads Manager (`campana03.png` y `desglose03.png`), cruzándolas con las métricas oficiales de adquisición en Google Play Console (**Visitantes de la ficha** e **Instalaciones registradas**).

#### 1. Resultados Acumulados Generales (16 de Octubre al 3 de Noviembre):
* **Inversión Total Acumulada:** **$65.732,22 ARS** (+$29.873 ARS invertidos en la última semana).
* **Impresiones Totales:** **30.597 impresiones**.
* **Alcance:** **24.504 personas únicas**.
* **Clics en el enlace (Resultados Meta):** **1.290 clics salientes** (+482 clics desde la última auditoría).
* **Costo por Clic Promedio (CPC global):** **$50,96 ARS** (excelente nivel de costo de tráfico en Argentina).
* **Visitantes Reales a la Ficha en Google Play Store:** **286 visitantes**.
* **Instalaciones Reales Registradas en Google Play Console:** **8 instalaciones**.

---

#### 2. Desempeño Acumulado por Creativo / Anuncio:

| Anuncio | Clics en el enlace | Costo por Clic (CPC) | Monto Gastado | Impresiones | Alcance | % del Presupuesto |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| **🥇 Reel 2 - Vivir Solo (Bienestar Diario)** | **804** | $56,04 ARS | **$45.056,43** | 15.750 | 13.309 | **68,5%** |
| **🥈 Reel 1 - Padres Auto** | **409** | **$41,80 ARS** | **$17.094,75** | 12.605 | 10.574 | **26,0%** |
| **🥉 Reel 3 - Urgencia (SOS y Choques)** | **77** | $46,51 ARS | **$3.581,04** | 2.242 | 1.926 | **5,5%** |
| **TOTALES** | **1.290** | **$50,96 ARS** | **$65.732,22** | **30.597** | **24.504** | **100%** |

* **Conclusión de Creativos:**
  * El **Reel 2 (Vivir Solo)** se consolida como el creativo rey indiscutido del algoritmo de Meta, absorbiendo casi el 70% del presupuesto con 804 clics generados.
  * El **Reel 1 (Padres Auto)** conserva el CPC más bajo ($41,80 ARS) y funciona como soporte de volumen bruto.
  * El **Reel 3 (SOS y Choques)** fue prácticamente apagado por el algoritmo ante la preferencia de los otros dos temas.

---

#### 3. Auditoría de Entrega por Plataforma:
* **En el Reel 2 (el principal):**
  * **Instagram:** **$32.275,49 ARS (71,6%)** $\rightarrow$ 449 clics a **$71,88 ARS**.
  * **Facebook:** **$12.474,12 ARS (27,7%)** *(histórico de los primeros días antes del corte)* $\rightarrow$ 348 clics a $35,85 ARS.
  * **Audience Network / WhatsApp / Otros:** Marginal ($300 ARS).
* **Confirmación Técnica:** Se confirma que desde el 20 de octubre el 100% del gasto nuevo se concentró en Instagram Reels, logrando un CPC real en Instagram de $71,88 ARS.

---

#### 4. Mapeo y Diagnóstico Preciso del Embudo de Conversión:

```
[1.290 Clics en Meta Ads] (100%)
         │
         ▼  (22,17% de llegada a la tienda)
[ 286 Visitantes en Ficha Google Play ]
         │
         ▼  (2,80% de tasa de conversión CVR)
[   8 Instalaciones Reales Confirmadas ]
```

* **Métricas Unitarias Derivadas:**
  * **Costo por Clic Saliente (Meta):** $50,96 ARS
  * **Costo por Visitante Calificado de Tienda (CPV):** **$229,83 ARS** ($65.732 / 286 visitantes)
  * **Costo por Instalación Efectiva (CPI):** **$8.216,52 ARS** ($65.732 / 8 instalaciones)

* **Diagnóstico de los Dos Tramos:**
  1. **Tramo 1 (Meta Ads $\rightarrow$ Play Store: 22,2% de llegada):** De cada 5 personas que hicieron clic en Instagram, solo 1 esperó y abrió efectivamente la ficha de Google Play. El resto abandonó por curiosidad superficial, lentitud de conexión o desestimiento ante el cambio de app. Este es el comportamiento típico del **Objetivo Tráfico** de Meta (premia a usuarios que hacen clics compulsivos pero sin intención de compra/descarga).
  2. **Tramo 2 (Ficha de Play Store $\rightarrow$ Instalación: 2,80% CVR):** Casi un 3% de los visitantes que leyeron la ficha decidieron instalar Estoy Ok. Este porcentaje se encuentra dentro del rango de mercado para apps utilitarias nuevas sin marca establecida (2% a 5%), confirmando que la ficha y las capturas ASO cumplen su función básica.

---

#### 5. Plan de Acción y Próximos Pasos (Hoja de Ruta Recomendada):

1. **Pausar o Reducir al Mínimo la Campaña Actual de Tráfico:**
   * La campaña ya cumplió su ciclo de validación de creativos (el Reel 2 es un éxito probado).
   * Mantener el objetivo Tráfico con un CPI de ~$8.200 ARS resulta financieramente ineficiente para adquisición escalable. Se recomienda pausarla o dejarla en presupuesto testimonial para frenar el desgaste de dinero.
2. **Priorizar la Activación con la Versión 17 (1.1.0):**
   * Aprovechar que la v17 recién lanzada cuenta con el **Checklist Interactivo de Primeros Pasos** (`SecurityChecklistCard.kt`) y estados vacíos formativos para maximizar que los usuarios ya instalados (y los que lleguen orgánicamente) configuren su primer check-in diario, contacto y zona segura.
3. **Evolución de Pauta Publicitaria (Siguiente Fase):**
   * **Opción A (Recomendada):** Configurar una campaña con **Objetivo "Promoción de la aplicación" (App Installs)** directamente vinculada a Google Play Store, para que el algoritmo de Meta optimice la entrega hacia personas con historial de instalar apps y no solo hacer clic.
   * **Opción B (Comunidad / Lead):** Probar campañas hacia WhatsApp o Landing web enfocadas en resolver el dolor de familias con personas mayores que viven solas.
4. **Fomentar Reseñas y Prueba Social en Google Play:**
   * Continuar sumando reseñas de 5 estrellas de confianza para que la tasa de conversión en la ficha (CVR) suba del 2,8% actual hacia un 6% - 8%.

---

### 📅 Hito 6: Transición a Campaña de "Promoción de la App" (App Installs) — Martes 3 de Noviembre de 2026 (Tarde)

Tras concluir la auditoría del Hito 5, se ejecutó el cambio estructural en la estrategia publicitaria de Meta Ads:

1. **Pausa Definitiva de Campaña de Tráfico:**
   * Apagada la campaña histórica de Tráfico (16 oct - 3 nov) para detener el gasto ineficiente por clics vacíos.
2. **Registro Oficial de la App en Meta for Developers:**
   * Se creó la app oficial de Estoy Ok en Meta Developers con **App ID `1105219368949683`**.
   * Se configuró la plataforma Android vinculando el package `com.estoyok.app` y la plataforma Web (`https://estoyok24.com`), asociando las políticas de privacidad y eliminación de datos en `https://estoyok24.com/politica-de-privacidad`.
   * Se cambió el estado de la app en Developers de *Desarrollo* a **En vivo (Active)**.
3. **Lanzamiento de Nueva Campaña de "Promoción de la App" (App Installs Nativo):**
   * **Objetivo de Campaña:** *Promoción de la aplicación (App Promotion)*.
   * **Objetivo de Rendimiento:** *Maximizar el número de instalaciones de la app*.
   * **Vinculación Oficial:** Seleccionada la app mediante su App ID `1105219368949683` vinculada a Google Play, resolviendo los errores `#1487678` y `#1815437` de `promoted_object.application_id`.
   * **Presupuesto:** Advantage+ CBO a nivel Campaña en **$4.000 ARS diarios**.
   * **Segmentación:** Argentina, 28 a 54 años, Solo dispositivos Android (8.0+).
   * **Creativos Activos (Dúo Competitivo A/B):**
     1. **Reel 2 (Vivir Solo / Bienestar Diario):** Creativo líder con llamado a la acción "Instalar" y copy enfocado en tranquilidad personal y reporte diario.
     2. **Reel 1 (Padres Auto):** Creativo complementario enfocado en padres de familia, eliminación del estrés del "¿Llegaste?" y alertas automáticas de zonas seguras.

---

### 📅 Hito 7: Auditoría de 48h de Campaña "App Installs", Corrección de Ubicaciones y Desplome de CPI — Jueves 5 de Noviembre de 2026

A las 48 horas de activa la nueva campaña con objetivo *Promoción de la App*, se analizó la captura oficial de Meta Ads Manager (`desglose05.png`) y se cruzó con la base de datos de producción:

#### 1. Rendimiento Cuantitativo (3 al 5 de Noviembre):
* **Inversión Total en el período:** **$9.313,51 ARS**.
* **Impresiones Totales:** **2.579 impresiones**.
* **Alcance:** **1.944 personas**.
* **Distribución de Presupuesto por Creativo:**
  * **Reel 2 (Vivir Solo):** **$8.105,81 ARS (87,0%)** con 2.161 impresiones. Se consolida una vez más como el anuncio favorito de la audiencia y del algoritmo.
  * **Reel 1 (Padres Auto):** **$272,53 ARS (2,9%)** con 85 impresiones.
  * **Residual Anuncio previo:** $91,04 ARS.

#### 2. Diagnóstico de Fuga Detectada y Corrección Inmediata:
* **Fuga de Ubicaciones Advantage+:** Al crearse la nueva campaña, Meta volvió a setear por defecto *Ubicaciones Advantage+ (automáticas)*.
* Del gasto del Reel 2 ($8.105,81 ARS):
  * **Instagram:** $3.186,77 ARS (39,3%)
  * **Facebook:** $2.729,12 ARS (33,7%)
  * **Audience Network:** $2.189,92 ARS (27,0%)
* **Acción Inmediata (05/11/2026):** Se editó el Conjunto de Anuncios, pasando a **Ubicaciones Manuales** y desmarcando Facebook y Audience Network para dejar **100% exclusivo Instagram**.

#### 3. Impacto en Producción y Desplome del Costo por Instalación (CPI):
* **Registros de Usuarios Reales:** Pasaron de **21 a 32 usuarios (+11 nuevos usuarios verificados)** con token FCM nativo (ej. Agustín Rodríguez #45, Liz Lonné #46, Tomas Delvechio #47, Mary Lucas #48, Susana #50, Stella Garcia #51, Osvaldo Rueda #52, Cristina Zarate #53, Anna AM #54, Gabriela Morelli #55).
* **Dispositivos Android Únicos:** Pasaron de **68 a 82 dispositivos (+14 instalaciones activas)**.
* **Usuarios con Check-in Activo:** Múltiples usuarios nuevos (Tomas Delvechio #47, Osvaldo Rueda #52, Cristina Zarate #53) ya están realizando check-ins manuales diarios.
* **Cálculo de CPI Real:**
  * **Campaña 1 (Tráfico):** $8.216,52 ARS por instalación.
  * **Campaña 2 (Promoción App):** **~$665 a $846 ARS por instalación/usuario**.
  * **Resultado:** Reducción de más de **9x en el costo de adquisición**, incluso con el 60% del presupuesto fugado en Facebook/Audience Network. Con el 100% enfocado en Instagram, la eficiencia crecerá sustancialmente.

---

### 📅 Hito 8: Auditoría de los 6 Días de "App Installs", Conversión en Play Store al 9,43% y Diagnóstico de Resultados Vacíos — Lunes 9 de Noviembre de 2026

Al cumplirse 6 días de la campaña con objetivo *Promoción de la App*, se realizó el cruce entre la captura oficial de Meta Ads Manager ([`docs/desglose09.png`](file:///home/nicolas/aplicaciones/estoyok/docs/desglose09.png)), el informe de Google Play Console ([`docs/analisisficha_play09.png`](file:///home/nicolas/aplicaciones/estoyok/docs/analisisficha_play09.png)) y la telemetría en tiempo real de la base de datos de producción:

#### 1. Rendimiento Cuantitativo Acumulado (3 al 9 de Noviembre):
* **Inversión Total Acumulada:** **$24.623,91 ARS**.
* **Impresiones Totales:** **7.180 impresiones**.
* **Alcance:** **5.819 personas**.
* **Desglose de Gasto por Creativo y Plataforma:**
  * **Reel 1 (Padres Auto):** **$14.875,00 ARS (60,4%)** | 4.490 impresiones | 3.819 personas.
    * *Instagram:* $13.525,64 ARS (90,9% del gasto de este anuncio).
    * *Audience Network:* $999,75 ARS (6,7%).
    * *Facebook:* $349,56 ARS (2,3%).
  * **Reel 2 (Vivir Solo):** **$8.813,74 ARS (35,8%)** | 2.357 impresiones | 1.695 personas.
    * *Instagram:* $3.846,95 ARS (43,6%).
    * *Facebook (previo al 05/11):* $2.768,00 ARS.
    * *Audience Network (previo al 05/11):* $2.198,79 ARS.
* **Diagnóstico de Distribución:** El Reel 1 absorbió el 90% del presupuesto durante el fin de semana del 5 al 9 de nov debido a que el algoritmo CBO de Meta, al no tener medición de instalaciones in-app, optimizó por impresiones y clics más baratos en Instagram.

#### 2. Validación de Embudo en Google Play Store:
* **Impresiones de Ficha:** 469 visitas en la ficha de Play Store en los últimos 7 días (+66,31% de crecimiento).
* **Adquisiciones en Dispositivos:** **32 descargas registradas en Google Play Store**.
* **Tasa de Conversión de la Ficha (CVR):** **9,43%** (vs 2,80% en la campaña de Tráfico previa). Esto valida que el ASO y los nuevos screenshots con el botón "Estoy OK" multiplicaron por más de 3x la efectividad en tienda.
* **Costo por Descarga Real (CPI Tienda):**
  $$\text{CPI} = \frac{\$24.623,91}{32} = \mathbf{\$769,50 \text{ ARS}}$$
* **Costo por Usuario Registrado (CAC Base de Datos):**
  * La base de datos subió de 21 a **38 usuarios verificados (+17 altas reales)**.
  $$\text{CAC} = \frac{\$24.623,91}{17} = \mathbf{\$1.448,46 \text{ ARS}}$$

#### 3. Hallazgo Técnico y Solución: Columna "Resultados" Vacía en Meta:
* **Causa Raíz:** Meta Ads no recibía los eventos de descarga porque Google Play no los notifica externamente. Al no existir un SDK dentro de la app que leyera el `InstallReferrer`, Meta mantenía en 0 la columna de Resultados y el algoritmo operaba a ciegas.
* **Solución Implementada (Versión 18 - 1.1.1):**
  * Integrado el SDK oficial `com.facebook.android:facebook-core:18.0.2` y `com.android.installreferrer:installreferrer:2.2`.
  * Configurados App ID `1105219368949683` y Client Token `6ff163df453e5b9752fae6918bece5c5` con auto-log de instalaciones de forma no bloqueante en `EstoyOkApplication.kt`.
  * Generado y firmado el bundle `app-release.aab` (v18 - 1.1.1) y enviado a revisión en Google Play Console el 09/11/2026.
  * Desplegado en GitHub y Railway en ramas `dev` y `main`.

---

## 🔍 PARTE 3: Protocolo Paso a Paso para Auditar la Campaña en Próximas Sesiones

Cada vez que revisemos la campaña (a las 24h, 48h o fin de semana), seguir este orden:

### Paso 1: Revisar en Meta Ads Manager
1. Verificar el **Desglose por Plataforma**:
   * Botón **Desglose** (arriba a la derecha de la tabla) $\rightarrow$ **Por entrega** $\rightarrow$ **Plataforma**.
   * Confirmar que el 100% del gasto nuevo esté entrando en **Instagram**.
   * Para quitar el desglose: volver a tocar el botón (que ahora dirá *"Plataforma"*) y elegir **"Borrar todos los desgloses"**.
2. Mirar las 4 métricas clave por cada Reel:
   * **Resultados (Clics en el enlace)**
   * **Costo por resultado (CPC)** $\rightarrow$ Meta saludable: `< $60 ARS` (actual: `~$29 ARS`).
   * **Monto gastado** $\rightarrow$ Ver a qué Reel le asigna más presupuesto el algoritmo.
   * **CTR (Clics / Impresiones)** $\rightarrow$ Meta saludable: `> 1.8%` (actual: `~3.20%`).

### Paso 2: Cruzar con Usuarios Reales en Producción (Vía Agente IA)
Pedirle al agente en el chat: *"Chequeá cuántos usuarios y dispositivos nuevos se registraron en producción"*.  
El agente consultará `https://api.estoyok24.com/api/maintenance/diagnose-push` comparando contra la línea base del **20/10/2026 (17:15 hs)**:
* **Línea base al 20/10/2026 (momento de aplicar las correcciones):**
  * `total_users`: **18 usuarios** (último ID registrado: `#41`).
  * `total_devices`: **63 dispositivos**.

### Paso 3: Criterio de Toma de Decisiones (Cuándo apagar o escalar)
* **Cuándo apagar un Reel:** Si tras acumular más de 1.500 impresiones en Instagram un Reel tiene un CPC más del doble de caro que los otros dos y no genera descargas.
* **Cuándo escalar presupuesto:** Cuando se verifiquen registros reales entrando en la base de datos de producción a un Costo por Instalación (CPI) rentable, aumentar el presupuesto diario de forma gradual (**+15% a +20% cada 48 hs**) para no reiniciar la fase de aprendizaje de Instagram.
