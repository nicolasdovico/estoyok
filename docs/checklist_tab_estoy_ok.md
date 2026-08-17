# Checklist de Verificación y Pruebas QA — Pestaña "Estoy OK" (Bienestar)

Este documento es una guía exhaustiva e interactiva para auditar y probar el 100% de las funcionalidades implementadas en la pestaña **"Estoy OK" (Mi Bienestar)** y sus flujos asociados (Gestión de Contactos SOS, Ajustes de Seguridad, Automatizaciones pasivas, SOS silencioso, recordatorios a familiares y backend jobs).

---

## 📌 Guía de Entorno y Preparación para Pruebas

> [!TIP]
> **Modo Rápido de Pruebas en Entorno Local:**  
> Cuando el backend está en `APP_ENV=local`, los comandos de verificación de inactividad (`checkins:verify-inactivity`) y recordatorios (`checkins:send-reminders`) evalúan los intervalos de horas configurados como **MINUTOS** (ej. un intervalo de 24 horas se evalúa como 24 minutos, y la ventana preventiva es de 2 minutos). Esto permite validar el ciclo completo en minutos sin esperar días.

* **Comandos útiles de terminal:**
  * **Verificar Inactividad:** `docker compose exec backend php artisan checkins:verify-inactivity`
  * **Enviar Recordatorios Preventivos:** `docker compose exec backend php artisan checkins:send-reminders`
  * **Verificar Colas de Trabajo:** `docker compose exec backend php artisan queue:work --once`
  * **Revisar logs en vivo:** `docker compose logs -f backend` o `adb logcat -s EstoyOk TrackingService`

---

## 1. Onboarding y Descargo de Responsabilidad Obligatorio

Esta ventana modal bloqueante se presenta únicamente la primera vez que un usuario nuevo ingresa a la aplicación.

- [ ] **1.1 Despliegue Bloqueante por Falta de Aceptación**
  * **Código:** [`PanelScreen.kt`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelScreen.kt#L126-L128) y [`PanelViewModel.kt`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelViewModel.kt#L452-L460)
  * **Instrucciones de Prueba:**
    1. Registra un usuario nuevo o en base de datos ejecuta `UPDATE users SET disclaimer_accepted_at = NULL WHERE id = <tu_id>;`.
    2. Abre la app e inicia sesión.
    3. Accede a la pestaña "Estoy OK".
  * **Resultado Esperado:** Se despliega el diálogo modal *"Condiciones y Protección"* con los 6 puntos normativos (Conectividad, Permisos de fondo, No sustitución del 911, Check-in/Modo sueño, Privacidad consensual y No venta de datos). El modal no se puede cerrar tocando afuera.

- [ ] **1.2 Persistencia de Aceptación Legal**
  * **Código:** [`SettingsController.php@acceptDisclaimer`](file:///home/usuario/aplicaciones/estoyok/backend/app/Http/Controllers/Api/SettingsController.php#L501-L515)
  * **Instrucciones de Prueba:**
    1. En el modal anterior, haz clic en el botón verde *"Entendido y Aceptar Condiciones 🟢"*.
    2. Cierra la app y vuelve a abrirla (o haz Pull-to-Refresh).
  * **Resultado Esperado:** El diálogo modal desaparece inmediatamente. El backend registra el timestamp en `users.disclaimer_accepted_at` y el modal nunca más vuelve a aparecer ni parpadear.

---

## 2. Cabecera (Header) y SOS Silencioso de Emergencia

- [x] **2.1 Visualización de Identidad de Usuario**
  * **Código:** [`PanelScreen.kt`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelScreen.kt#L166-L177)
  * **Instrucciones de Prueba:**
    1. Observa la esquina superior izquierda de la pantalla.
  * **Resultado Esperado:** Muestra el título *"Mi Bienestar"* y debajo el nombre completo del usuario autenticado obtenido de la API.

- [x] **2.2 Botón de Actualización Manual ("Actualizar")**
  * **Código:** [`PanelScreen.kt`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelScreen.kt#L193-L200) y [`PanelViewModel.kt@refreshDashboard`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelViewModel.kt#L109-L119)
  * **Instrucciones de Prueba:**
    1. Presiona el botón *"Actualizar"* en el encabezado.
  * **Resultado Esperado:** Se recargan en paralelo los datos de perfil, historial de check-ins, miembros del núcleo y contactos de emergencia sin congelar la interfaz.

- [x] **2.3 Botón SOS — Prevención de Falso Toque (Click Corto)**
  * **Código:** [`PanelScreen.kt`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelScreen.kt#L206-L229)
  * **Instrucciones de Prueba:**
    1. Realiza un toque simple y rápido sobre el botón rojo *"SOS"*.
  * **Resultado Esperado:** La app no dispara la emergencia y muestra un mensaje Toast: *"Mantén presionado por 3 segundos para activar SOS"*.

- [x] **2.4 Botón SOS — Activación Crítica Silenciosa (Long Press de 3s)**
  * **Código:** [`PanelViewModel.kt@triggerSos`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelViewModel.kt#L314-L362), [`EmergencyAlertController.php@storeSos`](file:///home/usuario/aplicaciones/estoyok/backend/app/Http/Controllers/EmergencyAlertController.php#L184-L242) y [`AudioRecorder.kt`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/core/util/AudioRecorder.kt)
  * **Instrucciones de Prueba:**
    1. Mantén presionado el botón *"SOS"* firmemente durante 3 segundos.
    2. Observa el estado del botón y los logs del dispositivo.
  * **Resultado Esperado:**
    * El botón cambia temporalmente su texto a *"SOS... 🚨"* con opacidad media.
    * Muestra el Toast *"¡SOS Silencioso Enviado!"*.
    * El backend crea una alerta en `emergency_alerts` de tipo `silent_sos` activa.
    * Se acelera la frecuencia de GPS del `TrackingService` a 5 segundos (`EXTRA_EMERGENCY`).
    * Se inicia la grabación de audio ambiental de 15 segundos en segundo plano (`AudioRecorder`) y al finalizar se sube automáticamente a `POST /api/emergency-alerts/{id}/audio`.
    * Los miembros del núcleo reciben notificación Push inmediata (`🚨 ¡SOS CRÍTICO!`).
    * Los contactos de emergencia reciben WhatsApp con el link público al mapa de crisis (`https://estoyok24.com/emergencia/{id}`).

- [x] **2.5 Acceso Rápido a Ajustes (Icono de Engranaje ⚙️)**
  * **Código:** [`PanelScreen.kt`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelScreen.kt#L179-L185)
  * **Instrucciones de Prueba:**
    1. Toca el icono de engranaje (⚙️) en la esquina superior derecha.
  * **Resultado Esperado:** Navega de forma directa y fluida a la pantalla de Ajustes de Seguridad (`Screen.Ajustes.route`).

---

## 3. Banner de Estado de Bienestar (StatusBanner)

- [x] **3.1 Estado "Sin Reportes" (Usuario Inicial)**
  * **Código:** [`PanelScreen.kt@StatusBanner`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelScreen.kt#L417-L422)
  * **Instrucciones de Prueba:**
    1. Con un usuario que no tenga check-ins registrados (`last_check_in_at = NULL`).
  * **Resultado Esperado:** La tarjeta se muestra en tono gris/neutro con el emoji `ℹ️`, título *"Sin Reportes"* y descripción explicativa. El contador indica `--h --m --s (Pendiente)` y la barra de progreso en 0%.

- [x] **3.2 Estado "Protegido y a Salvo" con Cuenta Regresiva Activa**
  * **Código:** [`PanelScreen.kt@StatusBanner`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelScreen.kt#L423-L428) y [`PanelViewModel.kt@calculateStatus`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelViewModel.kt#L396-L450)
  * **Instrucciones de Prueba:**
    1. Realiza un check-in manual.
    2. Observa la tarjeta superior y el segundero.
  * **Resultado Esperado:** 
    * El banner cambia a verde esmeralda con emoji `🛡️` y título *"Protegido y a Salvo"*.
    * Muestra la fecha/hora exacta límite de reporte (*"Debes reportarte antes de: dd/MM/yyyy HH:mm:ss"*).
    * El reloj digital (`%02dh %02dm %02ds`) descuenta en vivo segundo a segundo sin trabas ni parpadeos.
    * La barra de progreso refleja el porcentaje de tiempo restante (Verde > 25%, Naranja entre 10% y 25%, Rojo < 10%).

- [x] **3.3 Estado "Reporte Vencido" (Alerta Crítica)**
  * **Código:** [`PanelScreen.kt@StatusBanner`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelScreen.kt#L429-L434)
  * **Instrucciones de Prueba:**
    1. En base de datos o mediante API actualiza `last_check_in_at` a una fecha anterior al intervalo (ej. hace 48 horas).
    2. Haz clic en "Actualizar".
  * **Resultado Esperado:** El banner se colorea en rojo con emoji `⚠️`, título *"Reporte Vencido"*, texto de advertencia urgente y el reloj marcando `00h 00m 00s (Expirado)`.

---

## 4. Tarjeta Resumen de Protección (ProtectionSummaryCard)

- [x] **4.1 Visualización de Métricas Clave**
  * **Código:** [`PanelScreen.kt@ProtectionSummaryCard`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelScreen.kt#L956-L1120)
  * **Instrucciones de Prueba:**
    1. Revisa los 3 bloques de datos en la tarjeta de resumen.
  * **Resultado Esperado:**
    * **Intervalo:** Refleja las horas configuradas (ej. `24h Activo`).
    * **Contactos SOS:** Muestra la píldora verde interactiva con el conteo (ej. `1 Contacto ✏️` o `3 Contactos ✏️`).
    * **Auto-Checkin:** Muestra `Wi-Fi Activo 📶` si la opción está encendida en ajustes, o `Manual 🟢` si está apagada.

- [x] **4.2 Botón "Configurar ⚙️" de la Tarjeta**
  * **Código:** [`PanelScreen.kt`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelScreen.kt#L996-L1007)
  * **Instrucciones de Prueba:**
    1. Toca el botón *"Configurar ⚙️"* situado a la derecha del título de la tarjeta.
  * **Resultado Esperado:** Redirige a la pantalla de Ajustes.

- [x] **4.3 Apertura Rápida del Modal de Contactos desde la Tarjeta**
  * **Código:** [`PanelScreen.kt`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelScreen.kt#L1037-L1064)
  * **Instrucciones de Prueba:**
    1. Toca directamente la pastilla `[X Contactos ✏️]`.
  * **Resultado Esperado:** Se abre directamente el diálogo modal *"Contactos de Alerta SOS"* sin abandonar la pestaña actual.

---

## 5. Modal de Gestión de Contactos SOS (ManageContactsModal)

- [x] **5.1 Lista de Miembros del Círculo Automáticos**
  * **Código:** [`PanelScreen.kt@ManageContactsModal`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelScreen.kt#L1267-L1297)
  * **Instrucciones de Prueba:**
    1. Abre el modal de contactos teniendo un núcleo con otros integrantes.
  * **Resultado Esperado:** La sección superior lista a los familiares del círculo con el badge `🛡️ Núcleo`, nombre y correo, indicando que están protegidos automáticamente.

- [x] **5.2 Lista de Contactos Externos y Numeración de Prioridad**
  * **Código:** [`PanelScreen.kt@ManageContactsModal`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelScreen.kt#L1315-L1353)
  * **Instrucciones de Prueba:**
    1. Revisa los contactos externos cargados.
  * **Resultado Esperado:** Cada contacto muestra su badge de orden `#1`, `#2`, etc., nombre, teléfono, email opcional y parentesco (ej. "Madre", "Hermano").

- [x] **5.3 Reordenamiento de Prioridad Secuencial (Flechas ⬆️ / ⬇️)**
  * **Código:** [`PanelViewModel.kt@moveContactUp / moveContactDown`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelViewModel.kt#L197-L222) y [`EmergencyContactController.php@reorder`](file:///home/usuario/aplicaciones/estoyok/backend/app/Http/Controllers/Api/EmergencyContactController.php#L152-L200)
  * **Instrucciones de Prueba:**
    1. Teniendo 2 o más contactos, presiona la flecha ⬇️ en el contacto `#1` o ⬆️ en el contacto `#2`.
  * **Resultado Esperado:** La lista se reordena en la interfaz de inmediato y el backend persiste el nuevo orden en `emergency_contacts.priority` (`POST /api/emergency-contacts/reorder`). La flecha ⬆️ está deshabilitada en el primer contacto y la ⬇️ en el último.

- [x] **5.4 Alta de Nuevo Contacto Externo**
  * **Código:** [`PanelViewModel.kt@addContact`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelViewModel.kt#L147-L170) y [`EmergencyContactController.php@store`](file:///home/usuario/aplicaciones/estoyok/backend/app/Http/Controllers/Api/EmergencyContactController.php#L27-L62)
  * **Instrucciones de Prueba:**
    1. En el formulario inferior del modal, ingresa Nombre ("Papá"), Teléfono ("+5491122334455"), Email opcional y Parentesco ("Padre").
    2. Presiona *"Guardar Contacto"*.
  * **Resultado Esperado:** Valida campos requeridos, formatea el teléfono a formato internacional E.164 (`+`), lo crea en backend (`POST /api/emergency-contacts`), limpia los campos y refresca la lista con el nuevo contacto al final.

- [x] **5.5 Edición de Contacto Existente**
  * **Código:** [`PanelScreen.kt`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelScreen.kt#L1379-L1395) y [`PanelViewModel.kt@updateContact`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelViewModel.kt#L172-L195)
  * **Instrucciones de Prueba:**
    1. Presiona el icono de lápiz (✏️) en un contacto.
    2. Modifica el nombre o teléfono en el formulario que se autocompletó.
    3. Presiona *"Actualizar Contacto ✏️"*. (O prueba presionar *"Cancelar Edición"*).
  * **Resultado Esperado:** El contacto cambia a estado resaltado durante la edición. Al guardar, ejecuta `PUT /api/emergency-contacts/{id}` y actualiza la tarjeta con los nuevos datos.

- [x] **5.6 Eliminación con Diálogo de Confirmación**
  * **Código:** [`PanelScreen.kt`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelScreen.kt#L1194-L1233) y [`EmergencyContactController.php@destroy`](file:///home/usuario/aplicaciones/estoyok/backend/app/Http/Controllers/Api/EmergencyContactController.php#L129-L150)
  * **Instrucciones de Prueba:**
    1. Presiona el icono de papelera roja (🗑️) junto a un contacto.
    2. En el diálogo emergente, presiona *"Eliminar"*.
  * **Resultado Esperado:** Muestra primero el diálogo modal de confirmación con el nombre del contacto. Al confirmar, remueve el registro con `DELETE /api/emergency-contacts/{id}` de forma instantánea.

---

## 6. Botón Principal "Estoy OK" y Flujo de Check-in Manual

- [ ] **6.1 Animación y Feedback de Registro**
  * **Código:** [`PanelScreen.kt@CheckInButton`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelScreen.kt#L557-L606) y [`PanelViewModel.kt@performCheckIn`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelViewModel.kt#L289-L308)
  * **Instrucciones de Prueba:**
    1. Presiona el botón circular verde *"Estoy OK"*.
  * **Resultado Esperado:**
    * El botón muestra un spinner de carga circular blanco (`CircularProgressIndicator`) mientras realiza la petición.
    * El backend recibe `POST /api/check-in` con `{ "source": "manual" }`.
    * Se actualiza `users.last_check_in_at = NOW()`.
    * Si el usuario tenía alertas activas en `emergency_alerts`, se resuelven a `resolved`.
    * Se limpia el candado de caché de Redis `auto_checkin_wifi_{user_id}`.

- [ ] **6.2 Diálogo Modal de Éxito (CheckInSuccessDialog)**
  * **Código:** [`PanelScreen.kt@CheckInSuccessDialog`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelScreen.kt#L1122-L1174)
  * **Instrucciones de Prueba:**
    1. Tras pulsar el botón "Estoy OK".
  * **Resultado Esperado:** Aparece la ventana modal con emoji `🛡️✨`, título *"¡Reporte Registrado!"* y mensaje confirmando que la familia y contactos han recibido la tranquilidad. Al presionar *"Entendido 👍"*, se cierra y el temporizador se encuentra reiniciado.

---

## 7. Sección "Tranquilidad del Núcleo" (Monitoreo Familiar)

- [ ] **7.1 Estado Vacío (Sin Otros Miembros)**
  * **Código:** [`PanelScreen.kt`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelScreen.kt#L345-L363)
  * **Instrucciones de Prueba:**
    1. Inicia sesión con un usuario que no pertenezca a ningún núcleo o esté solo en su círculo.
  * **Resultado Esperado:** Se muestra una tarjeta con el texto: *"Sin otros miembros en tu círculo. Invita a tu familia para ver su estado de bienestar aquí."*.

- [ ] **7.2 Tarjeta de Miembro Familiar y Vencimiento Relativo**
  * **Código:** [`PanelScreen.kt@CircleMemberWellbeingCard`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelScreen.kt#L727-L953)
  * **Instrucciones de Prueba:**
    1. Agrega a otro usuario al núcleo y observa su tarjeta en la lista "Tranquilidad del Núcleo".
  * **Resultado Esperado:**
    * **Avatar circular:** Con las dos iniciales del familiar en mayúsculas (ej. "AN" para Analía).
    * **Estado:** Muestra `🟢 Reportado OK (hace Xm)` con borde verde si está al día, o `⚠️ Reporte Vencido (hace Xh)` con borde naranja si expiró.
    * **Subtexto dinámico de vencimiento:** Si está al día, calcula si vence hoy (`Vence hoy a las 18:30 hs`) o mañana (`Vence mañana a las 09:15 hs`).

- [ ] **7.3 Botón "Recordar 🔔" (Push Notification con Rate-Limit)**
  * **Código:** [`PanelViewModel.kt@sendReminderPing`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelViewModel.kt#L237-L256) y [`CircleController.php@remindMember`](file:///home/usuario/aplicaciones/estoyok/backend/app/Http/Controllers/Api/CircleController.php#L265-L300)
  * **Instrucciones de Prueba:**
    1. Presiona el botón *"Recordar 🔔"* en la tarjeta de un familiar.
    2. Vuelve a presionarlo de inmediato.
  * **Resultado Esperado:**
    * Primer toque: Muestra Toast *"🔔 Recordatorio enviado a [Nombre]"*. El familiar recibe una notificación Push con el mensaje *"🔔 Recordatorio de Bienestar: [Nombre] te solicita confirmar tu reporte de bienestar ('Estoy OK')"*.
    * Segundo toque inmediato: El backend aplica rate-limit y retorna mensaje informativo para evitar spam (mínimo 5 minutos de espera entre recordatorios).

---

## 8. Sección "Historial de Reportes"

- [ ] **8.1 Estado Vacío de Historial**
  * **Código:** [`PanelScreen.kt`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelScreen.kt#L383-L399)
  * **Instrucciones de Prueba:**
    1. Con una cuenta recién creada sin check-ins.
  * **Resultado Esperado:** Muestra el recuadro centrado *"Aún no tienes reportes guardados."*.

- [ ] **8.2 Distinción Visual por Origen (`source`)**
  * **Código:** [`PanelScreen.kt@CheckInItemRow`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/PanelScreen.kt#L608-L725)
  * **Instrucciones de Prueba:**
    1. Genera reportes desde distintos orígenes (manual, por Wi-Fi, sensor o WhatsApp).
  * **Resultado Esperado:** Cada fila del historial muestra su icono representativo, texto claro de origen y fecha/hora formateada (ej. `14 Ago, 05:20 PM`):
    * `manual` ➔ 👆 **Vía Manual**
    * `wifi` ➔ 📶 **Vía Wi-Fi Seguro**
    * `movement` ➔ 🚶 **Vía Sensor**
    * `sms` ➔ 💬 **Vía SMS**
    * `whatsapp` ➔ 🟢 **Vía WhatsApp**

---

## 9. Pantalla de Ajustes de Seguridad (`AjustesScreen.kt`)

- [ ] **9.1 Selector de Intervalo de Reporte (Chips 12h, 24h, 48h)**
  * **Código:** [`AjustesScreen.kt`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/AjustesScreen.kt#L271-L298) y [`SettingsController.php@updateCheckinInterval`](file:///home/usuario/aplicaciones/estoyok/backend/app/Http/Controllers/Api/SettingsController.php#L13-L44)
  * **Instrucciones de Prueba:**
    1. En Ajustes, selecciona el chip de "12 Horas" o "48 Horas".
    2. Regresa a la pestaña "Estoy OK".
  * **Resultado Esperado:** Guarda en backend `PUT /api/settings/checkin-interval`, muestra Toast de éxito y la tarjeta de protección y el banner recalculan inmediatamente el nuevo tiempo límite en base a esas horas.

- [ ] **9.2 Modo Sueño (Horas Silenciosas con Cruce de Medianoche)**
  * **Código:** [`AjustesScreen.kt`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/AjustesScreen.kt#L300-L373) y [`SettingsController.php@updateQuietHours`](file:///home/usuario/aplicaciones/estoyok/backend/app/Http/Controllers/Api/SettingsController.php#L46-L115)
  * **Instrucciones de Prueba:**
    1. Activa el toggle de *"Habilitar Modo Sueño"*.
    2. Ingresa Hora Inicio (ej. `22:00`) y Hora Fin (ej. `08:00`).
    3. Presiona *"Guardar Horas"*.
  * **Resultado Esperado:** Persiste en base de datos (`quiet_hours_enabled = true`, `quiet_hours_start = "22:00"`, `quiet_hours_end = "08:00"` y la zona horaria del dispositivo). Durante esta franja, los comandos de recordatorio e inactividad se suspenden automáticamente sin molestar al usuario.

- [ ] **9.3 Toggle de Reporte por WhatsApp**
  * **Código:** [`AjustesScreen.kt`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/AjustesScreen.kt#L375-L404) y [`SettingsController.php@updateSmsWhatsappCheckin`](file:///home/usuario/aplicaciones/estoyok/backend/app/Http/Controllers/Api/SettingsController.php#L116-L157)
  * **Instrucciones de Prueba:**
    1. Activa el interruptor *"Reporte por WhatsApp"*.
  * **Resultado Esperado:** Se actualiza `allow_sms_whatsapp_checkin = true` en la base de datos y muestra confirmación mediante Toast.

- [ ] **9.4 Auto Check-in Pasivo por Wi-Fi Seguro y Botón "📶 Usar Wi-Fi Actual"**
  * **Código:** [`AjustesScreen.kt`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/AjustesScreen.kt#L437-L545) y [`LocationController.php@handlePassiveWifiAutoCheckin`](file:///home/usuario/aplicaciones/estoyok/backend/app/Http/Controllers/Api/LocationController.php#L307-L352)
  * **Instrucciones de Prueba:**
    1. Conéctate a tu red Wi-Fi hogareña.
    2. En Ajustes, activa *"Auto-reportarse por Wi-Fi Seguro"*.
    3. Presiona el botón *"📶 Usar Wi-Fi Actual"*.
  * **Resultado Esperado:** 
    * Solicita permisos de ubicación si hiciera falta.
    * Detecta el SSID conectado y lo completa automáticamente en el campo de texto.
    * Guarda en backend `wifi_checkin_enabled = true` y `safe_wifi_ssid = "TuSSID"`.
    * En segundo plano, al enviar reportes periódicos de ubicación (latido cada 15 min), si coincide la red, ejecuta el check-in automático pasivo (`source: 'wifi'`) y envía una notificación push al usuario confirmándole la protección.

- [ ] **9.5 Auto Check-in Pasivo por Actividad Física (Podómetro)**
  * **Código:** [`AjustesScreen.kt`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/AjustesScreen.kt#L549-L575)
  * **Instrucciones de Prueba:**
    1. Activa *"Auto-reportarse por Actividad Física"*.
  * **Resultado Esperado:** Guarda `sensor_checkin_enabled = true` en backend.

- [ ] **9.6 Toggle de Compartir Ubicación (Rastreo en Segundo Plano)**
  * **Código:** [`AjustesScreen.kt`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/AjustesScreen.kt#L578-L651) y [`TrackingService.kt`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/services/TrackingService.kt)
  * **Instrucciones de Prueba:**
    1. Activa o desactiva el switch *"Compartir Ubicación (Rastreo)"*.
  * **Resultado Esperado:** Si faltan permisos de segundo plano, despliega el diálogo de advertencia prominente ("Prominent Disclosure") solicitando *"Permitir todo el tiempo"*. Al concederse, inicia el Foreground Service con su notificación persistente y comienza el envío adaptativo de telemetría.

- [ ] **9.7 Modal de Condiciones de Servicio**
  * **Código:** [`AjustesScreen.kt`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/AjustesScreen.kt#L653-L671)
  * **Instrucciones de Prueba:**
    1. Presiona el botón *"Condiciones del Servicio 📋"*.
  * **Resultado Esperado:** Despliega el modal de términos operativos de 6 puntos con botón de cierre.

- [ ] **9.8 Cierre de Sesión (Logout)**
  * **Código:** [`AjustesScreen.kt`](file:///home/usuario/aplicaciones/estoyok/android-native/app/src/main/java/com/estoyok/app/features/wellbeing/presentation/AjustesScreen.kt#L673-L697) y [`AuthController.php@logout`](file:///home/usuario/aplicaciones/estoyok/backend/app/Http/Controllers/Api/AuthController.php)
  * **Instrucciones de Prueba:**
    1. Presiona el botón rojo *"Cerrar Sesión"*.
  * **Resultado Esperado:** Revoca el token Sanctum en el servidor, limpia el `expo_push_token` del usuario para no recibir alertas de otra cuenta, limpia el DataStore y redirige a la pantalla de Login.

---

## 10. Procesos Backend Automatizados y Webhooks

- [ ] **10.1 Comando de Recordatorios Preventivos (`checkins:send-reminders`)**
  * **Código:** [`SendCheckInReminders.php`](file:///home/usuario/aplicaciones/estoyok/backend/app/Console/Commands/SendCheckInReminders.php)
  * **Instrucciones de Prueba:**
    1. Ubica a un usuario que se encuentre dentro de la ventana previa al vencimiento.
    2. Ejecuta: `docker compose exec backend php artisan checkins:send-reminders`.
  * **Resultado Esperado:** El usuario recibe una notificación Push (*"¿Estás por ahí? Recuerda confirmar tu bienestar..."*) y un correo electrónico recordatorio. Si está en Modo Sueño, el envío se omite y se registra en logs.

- [ ] **10.2 Comando de Detección de Inactividad y Escalamiento (`checkins:verify-inactivity`)**
  * **Código:** [`VerifyInactivity.php`](file:///home/usuario/aplicaciones/estoyok/backend/app/Console/Commands/VerifyInactivity.php) y [`SendInactivityAlerts.php`](file:///home/usuario/aplicaciones/estoyok/backend/app/Jobs/SendInactivityAlerts.php)
  * **Instrucciones de Prueba:**
    1. Con un usuario cuyo último reporte esté vencido (`last_check_in_at < NOW() - intervalo`).
    2. Ejecuta: `docker compose exec backend php artisan checkins:verify-inactivity`.
    3. Ejecuta el worker: `docker compose exec backend php artisan queue:work --once`.
  * **Resultado Esperado:**
    * Crea una alerta en `emergency_alerts` (`type = 'inactivity'`).
    * Envía Push de alerta crítica al propio usuario y a todos los integrantes de sus Núcleos familiares.
    * Si tiene contactos de emergencia, despacha el correo electrónico y mensaje de WhatsApp oficial (vía Evolution API) con el enlace público `https://estoyok24.com/emergencia/{uuid}`.
    * Si el escalamiento está habilitado, encola al contacto siguiente con delay de $X$ minutos.

- [ ] **10.3 Auto Check-in por Webhook de WhatsApp (Evolution API)**
  * **Código:** [`WebhookController.php@evolutionMessage`](file:///home/usuario/aplicaciones/estoyok/backend/app/Http/Controllers/Api/WebhookController.php#L64-L141)
  * **Instrucciones de Prueba:**
    1. Habiendo recibido una alerta o mensaje de WhatsApp de la pasarela de Estoy Ok.
    2. Responde con el texto `"OK"` o `"Estoy OK"` o `"1"` desde tu WhatsApp personal hacia el número del bot (`+5492323610697`).
  * **Resultado Esperado:**
    * El webhook `POST /api/webhooks/evolution/message` identifica al usuario por su número de teléfono.
    * Registra el check-in con `source = 'whatsapp'`.
    * Resuelve cualquier alerta activa.
    * El bot responde por WhatsApp: *"Bienestar verificado con éxito en Estoy Ok. ¡Gracias!"*.
    * Al abrir la app, el banner pasa a estar en verde y en el historial figura *"Vía WhatsApp 🟢"*.

---

## 📋 Resumen de Control de Pruebas

| Módulo | Total de Pruebas | Aprobadas | Observaciones / Fallas |
| :--- | :---: | :---: | :--- |
| **1. Onboarding y Disclaimer** | 2 | [ ] | |
| **2. Cabecera y SOS Silencioso** | 5 | [ ] | |
| **3. Banner de Estado (Temporizador)** | 3 | [ ] | |
| **4. Tarjeta Resumen de Protección** | 3 | [ ] | |
| **5. Modal de Contactos SOS** | 6 | [ ] | |
| **6. Botón Principal "Estoy OK"** | 2 | [ ] | |
| **7. Tranquilidad del Núcleo** | 3 | [ ] | |
| **8. Historial de Reportes** | 2 | [ ] | |
| **9. Ajustes de Seguridad** | 8 | [ ] | |
| **10. Procesos Backend y Webhooks** | 3 | [ ] | |
| **TOTAL** | **37** | **[ / 37 ]** | |
