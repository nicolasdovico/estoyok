# Plan de Rediseño Visual (Life360 Style) - Estoy Ok

Este plan detalla los cambios propuestos para alinear la estética y el diseño de la aplicación nativa en Kotlin con la estructura observada en la captura de pantalla de Life360 ([modelolife360.jpg](file:///home/usuario/aplicaciones/estoyok/docs/modelolife360.jpg)), utilizando un color de identidad distintivo y adaptándolo al sistema de diseño oscuro premium actual de Estoy Ok.

---

## 🎨 Propuesta de Color de Marca: **Teal Turquesa Eléctrico (Electric Teal)**

Para diferenciarnos de **Life360** (Violeta) y de **GeoZilla/otras** (Rojos/Naranjas tradicionales), proponemos adoptar el **Teal Turquesa Eléctrico** como el color insignia de **Estoy Ok**.

### Muestra de Color Propuesta:
![Electric Teal Swatch](/home/usuario/aplicaciones/estoyok/docs/electric_teal_swatch.jpg)

> [!TIP]
> **¿Por qué este color?**
> 1. **Psicología:** El turquesa/teal transmite tranquilidad, balance, protección y "estado OK" (seguro/salvo) sin ser el verde común de semáforo.
> 2. **Identidad del Sitio:** Combina de forma impecable con el esquema esmeralda/teal actual de la web de Estoy Ok, manteniendo consistencia de marca.
> 3. **Legibilidad:** En pantallas oscuras (Dark Mode), el teal eléctrico resalta con una nitidez excelente y luce sumamente premium.
> 4. **Especificaciones de Color:**
>    * **Color Primario (Electric Teal):** `#00F0C0` (Vibrante) o `#0D9488` (Base).
>    * **Contraste/Secundario (Teal Oscuro):** `#115E59`.
>    * **Fondo de Acento (Acento Soft):** `#00F0C0` con opacidad del 10% al 15% para tarjetas y selecciones.

---

## 📋 Plan de Acción para el Rediseño UI

### Paso 1: Configurar la Paleta en el Sistema de Temas (`Color.kt` & `Theme.kt`)
* Actualizar [Color.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/core/theme/Color.kt) para centralizar la nueva gama de color:
  ```kotlin
  val PrimaryTeal = Color(0xFF00F0C0)        // Eléctrico y vibrante para marcas/píldoras
  val PrimaryTealDark = Color(0xFF0D9488)    // Tono base
  val TealSurface = Color(0xFF115E59)        // Fondo de acentos
  ```
* Sincronizar [Theme.kt](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/core/theme/Theme.kt) para mapear `primary`, `secondary` y `onPrimary` a estos nuevos tokens.

### Paso 2: Rediseñar los Botones Flotantes del Mapa (`MapaScreen.kt`)
Siguiendo la disposición de Life360, transformaremos los elementos de control sobre el mapa en botones circulares limpios de fondo sólido (`DarkSurfaceVariant` o similar en nuestro modo oscuro) con sombras discretas:
1. **Botón de Ajustes (Arriba Izquierda):** Un botón circular pequeño con el icono de engranaje (violeta en Life360, teal eléctrico en nuestro caso).
2. **Píldora del Selector de Núcleo (Arriba Centro):** Píldora redondeada con fondo oscuro translúcido, bordes suaves y flecha desplegable (`ArrowDropDown`).
3. **Botones de Mensajería y Correo (Arriba Derecha):** Botones circulares apilados verticalmente para notificaciones o alertas rápidas.
4. **Botones Rápidos de Acción (Sobre el BottomSheet):**
   * **Estoy OK / Registro:** Botón estilo píldora con fondo de acento teal (con bordes redondos y sombreado).
   * **S.O.S.:** Botón estilo píldora de emergencia al lado del de registro.
   * **Botones Auxiliares (Derecha):** Botones circulares flotantes para compartir ubicación (`Share`) y capas del mapa (`Layers`).

### Paso 3: Estilizar el Historial y el Panel Deslizable (BottomSheet)
* **Carrusel de Categorías Rápidas:** Diseñar un carrusel de tarjetas redondeadas de acento (como los iconos de Personas, Mascota, Llaves, Maletín en la captura) para alternar vistas rápidas del núcleo.
* **Separadores y Timeline:** Usar líneas punteadas y marcas sutiles con nuestro color de marca teal para estructurar la línea de tiempo de viajes y permanencias de forma prolija.

### Paso 4: Unificar la Barra de Navegación Inferior (Bottom Navigation)
* Configurar los iconos activos (Mapa, Vehículo, Estoy ok, Premium) para que se iluminen con el color de marca teal, aplicando una píldora de fondo con opacidad suave de acento para la pestaña activa (similar al estilo Google M3 y el selector de Life360).

---

## 🔒 Próximos Pasos

Por favor, revisa el color propuesto y los detalles de diseño. **Una vez que me des tu visto bueno (OK), procederé a implementar los cambios** en los archivos correspondientes.
