# Catálogo de Eventos y Notificaciones Push

Este documento detalla todas las notificaciones push configuradas en el backend de **Estoy Ok** y cómo probar cada una de ellas desde la terminal de comandos interactiva de Laravel (Artisan Tinker).

Las notificaciones utilizan el servicio unificado [PushNotificationService](file:///home/usuario/aplicaciones/estoyok/backend/app/Services/PushNotificationService.php) que rutea de forma híbrida hacia **Expo Push Tokens** o **Google FCM Tokens**.

---

## 📋 Resumen de Eventos

| # | Evento | Payload `type` | Destinatario | Activador / Condición |
|---|---|---|---|---|
| 1 | **Recordatorio de Check-In** | `checkin_reminder` | El usuario inactivo | Comando cron preventivo |
| 2 | **Alerta de Inactividad** | `inactivity_alert` | El usuario inactivo | Fin de la ventana "Estoy OK" |
| 3 | **SOS Crítico / Silencioso** | `silent_sos` | Miembros del círculo | Botón SOS presionado |
| 4 | **Accidente Vehicular** | `crash_alert` | Miembros del círculo | Telemetría G-Force alta |
| 5 | **Alerta de Perímetro** | `geofence_alert` | Miembros del círculo | Entrada/Salida de zona segura |
| 6 | **Activación del Radar** | `dynamic_geofence_activated` | Usuario objetivo | Inicio de radar dinámico |
| 7 | **Desactivación del Radar** | `dynamic_geofence_deactivated` | Otro participante | Fin de radar dinámico |
| 8 | **Infracción de Radar** | `dynamic_geofence_breached` | Iniciador del radar | Objetivo se aleja |
| 9 | **Radar Restablecido** | `dynamic_geofence_restored` | Iniciador del radar | Objetivo regresa |
| 10 | **Batería Baja** | `low_battery_alert` | Miembros del círculo | Batería < 15% |
| 11 | **Exceso de Velocidad** | `speeding_alert` | Administrador del círculo | Velocidad > Límite del círculo |

---

## 🛠️ Comandos de Prueba (Artisan Tinker)

Para iniciar las pruebas individuales, entra a la consola interactiva de Laravel dentro del contenedor de backend:
```bash
docker compose exec backend php artisan tinker
```

### 1. Recordatorio de Check-In (`checkin_reminder`)
* **Código Fuente:** [SendCheckInReminders.php](file:///home/usuario/aplicaciones/estoyok/backend/app/Console/Commands/SendCheckInReminders.php#L89)
* **Texto:** *"¿Estás por ahí? Recuerda confirmar tu bienestar para evitar avisar a tus contactos."*
* **Comando Tinker:**
  ```php
  $user = App\Models\User::whereNotNull('expo_push_token')->first();
  app(App\Services\PushNotificationService::class)->sendPush(
      $user->expo_push_token,
      '¿Estás por ahí?',
      'Recuerda confirmar tu bienestar para evitar avisar a tus contactos.',
      ['type' => 'checkin_reminder']
  );
  ```

### 2. Alerta de Inactividad (`inactivity_alert`)
* **Código Fuente:** [SendInactivityAlerts.php](file:///home/usuario/aplicaciones/estoyok/backend/app/Jobs/SendInactivityAlerts.php#L114)
* **Texto:** *"¡Alerta de Seguridad! No has realizado tu check-in diario. Tus contactos de emergencia han sido notificados."*
* **Comando Tinker:**
  ```php
  $user = App\Models\User::whereNotNull('expo_push_token')->first();
  // Dispara el Job simulando la inactividad del usuario (contactIndex = 0 envía la push)
  App\Jobs\SendInactivityAlerts::dispatch($user, 1, 0);
  ```

### 3. SOS Crítico / Silencioso (`silent_sos`)
* **Código Fuente:** [EmergencyAlertController.php](file:///home/usuario/aplicaciones/estoyok/backend/app/Http/Controllers/EmergencyAlertController.php#L211)
* **Texto:** *"🚨 ¡SOS CRÍTICO! {nombre} ha activado un SOS silencioso. Revisa su ubicación inmediatamente."*
* **Comando Tinker:**
  ```php
  $user = App\Models\User::whereNotNull('expo_push_token')->first();
  $alert = App\Models\EmergencyAlert::create([
      'user_id' => $user->id,
      'type' => 'silent_sos',
      'status' => 'active',
      'expires_at' => now()->addHours(1)
  ]);
  $user->load('circles.users');
  foreach ($user->circles as $circle) {
      foreach ($circle->users as $member) {
          if ($member->id !== $user->id && $member->expo_push_token) {
              app(App\Services\PushNotificationService::class)->sendPush(
                  $member->expo_push_token,
                  '🚨 ¡SOS CRÍTICO!',
                  "{$user->name} ha activado un SOS silencioso. Revisa su ubicación inmediatamente.",
                  [
                      'type' => 'silent_sos',
                      'alert_id' => (string) $alert->id,
                      'user_id' => (string) $user->id,
                      'user_name' => $user->name,
                  ],
                  true
              );
          }
      }
  }
  ```

### 4. Accidente Vehicular (`crash_alert`)
* **Código Fuente:** [SendCrashAlertJob.php](file:///home/usuario/aplicaciones/estoyok/backend/app/Jobs/SendCrashAlertJob.php#L58)
* **Texto:** *"Se ha detectado un fuerte impacto vehicular de {G_force} G en el dispositivo de {nombre}."*
* **Comando Tinker:**
  ```php
  $user = App\Models\User::whereNotNull('expo_push_token')->first();
  $alert = App\Models\EmergencyAlert::create([
      'user_id' => $user->id,
      'type' => 'crash_alert',
      'status' => 'active',
      'expires_at' => now()->addHours(1)
  ]);
  $crashEvent = App\Models\CrashEvent::create([
      'user_id' => $user->id,
      'g_force' => 5.2,
      'occurred_at' => now()
  ]);
  App\Jobs\SendCrashAlertJob::dispatch($crashEvent, $alert);
  ```

### 5. Alerta de Perímetro (`geofence_alert`)
* **Código Fuente:** [ProcessGeofencing.php](file:///home/usuario/aplicaciones/estoyok/backend/app/Jobs/ProcessGeofencing.php#L92)
* **Texto:** *"{nombre} ha ingresado a: {nombre_geocerca}"* (o *"salido de"*)
* **Comando Tinker:**
  ```php
  $user = App\Models\User::first();
  $geofence = App\Models\Geofence::first(); // Requiere al menos una zona segura en DB
  if ($geofence) {
      App\Jobs\ProcessGeofencing::dispatch($user, $geofence, 'ingresado a');
  } else {
      echo "Crea una zona segura (Geofence) antes de probar.";
  }
  ```

### 6. Activación del Radar de Proximidad (`dynamic_geofence_activated`)
* **Código Fuente:** [DynamicGeofenceController.php](file:///home/usuario/aplicaciones/estoyok/backend/app/Http/Controllers/Api/DynamicGeofenceController.php#L102)
* **Texto:** *"{nombre} ha iniciado un radar de proximidad contigo."*
* **Comando Tinker:**
  ```php
  $initiator = App\Models\User::first();
  $target = App\Models\User::where('id', '!=', $initiator->id)->whereNotNull('expo_push_token')->first();
  $geofence = App\Models\DynamicGeofence::create([
      'initiator_id' => $initiator->id,
      'target_id' => $target->id,
      'safe_radius_meters' => 100,
      'is_active' => true
  ]);
  app(App\Services\PushNotificationService::class)->sendPush(
      $target->expo_push_token,
      '📡 Radar de Proximidad Activado',
      "{$initiator->name} ha iniciado un radar de proximidad contigo.",
      [
          'type' => 'dynamic_geofence_activated',
          'geofence_id' => (string) $geofence->id,
          'initiator_name' => $initiator->name,
      ],
      true
  );
  ```

### 7. Desactivación del Radar de Proximidad (`dynamic_geofence_deactivated`)
* **Código Fuente:** [DynamicGeofenceController.php](file:///home/usuario/aplicaciones/estoyok/backend/app/Http/Controllers/Api/DynamicGeofenceController.php#L147)
* **Texto:** *"El radar de proximidad ha sido desactivado por {nombre}."*
* **Comando Tinker:**
  ```php
  $user = App\Models\User::first();
  $target = App\Models\User::where('id', '!=', $user->id)->whereNotNull('expo_push_token')->first();
  $geofence = App\Models\DynamicGeofence::create([
      'initiator_id' => $user->id,
      'target_id' => $target->id,
      'safe_radius_meters' => 100,
      'is_active' => false
  ]);
  app(App\Services\PushNotificationService::class)->sendPush(
      $target->expo_push_token,
      '📡 Radar de Proximidad Finalizado',
      "El radar de proximidad ha sido desactivado por {$user->name}.",
      [
          'type' => 'dynamic_geofence_deactivated',
          'geofence_id' => (string) $geofence->id,
      ],
      true
  );
  ```

### 8. Infracción de Radar (`dynamic_geofence_breached`)
* **Código Fuente:** [ProcessDynamicGeofencing.php](file:///home/usuario/aplicaciones/estoyok/backend/app/Jobs/ProcessDynamicGeofencing.php#L82)
* **Texto:** *"¡Atención! {nombre_objetivo} se ha alejado demasiado ({distancia}m)"*
* **Comando Tinker:**
  ```php
  $initiator = App\Models\User::whereNotNull('expo_push_token')->first();
  $target = App\Models\User::where('id', '!=', $initiator->id)->first();
  $geofence = App\Models\DynamicGeofence::create([
      'initiator_id' => $initiator->id,
      'target_id' => $target->id,
      'safe_radius_meters' => 20,
      'is_active' => true
  ]);
  // Simulamos una distancia de 150m (excede los 20m)
  App\Jobs\ProcessDynamicGeofencing::dispatch($geofence, 150.0);
  ```

### 9. Radar de Proximidad Restablecido (`dynamic_geofence_restored`)
* **Código Fuente:** [ProcessDynamicGeofencing.php](file:///home/usuario/aplicaciones/estoyok/backend/app/Jobs/ProcessDynamicGeofencing.php#L104)
* **Texto:** *"{nombre_objetivo} ha regresado a la zona de seguridad ({distancia}m)"*
* **Comando Tinker:**
  ```php
  $initiator = App\Models\User::whereNotNull('expo_push_token')->first();
  $target = App\Models\User::where('id', '!=', $initiator->id)->first();
  $geofence = App\Models\DynamicGeofence::create([
      'initiator_id' => $initiator->id,
      'target_id' => $target->id,
      'safe_radius_meters' => 100,
      'is_active' => true
  ]);
  // Marcamos la infracción previa en caché para que el disparador de restauración actúe
  $cacheKey = "dynamic_geofence_alert_{$geofence->id}";
  cache()->put($cacheKey, true, now()->addMinutes(5));
  // Simulamos una distancia de 30m (dentro del rango seguro de 100m)
  App\Jobs\ProcessDynamicGeofencing::dispatch($geofence, 30.0);
  ```

### 10. Batería Baja (`low_battery_alert`)
* **Código Fuente:** [SendBatteryAlertJob.php](file:///home/usuario/aplicaciones/estoyok/backend/app/Jobs/SendBatteryAlertJob.php#L53)
* **Texto:** *"La batería de {nombre} está baja ({porcentaje}%)."*
* **Comando Tinker:**
  ```php
  $user = App\Models\User::first();
  // Simular envío de reporte de batería baja al 12% (0.12)
  App\Jobs\SendBatteryAlertJob::dispatch($user, 0.12);
  ```

### 11. Exceso de Velocidad (`speeding_alert`)
* **Código Fuente:** [SendSpeedingAlertJob.php](file:///home/usuario/aplicaciones/estoyok/backend/app/Jobs/SendSpeedingAlertJob.php#L43)
* **Texto:** *"{nombre} está viajando en coche a {velocidad} km/h, superando el límite de {limite} km/h."*
* **Comando Tinker:**
  ```php
  $owner = App\Models\User::whereNotNull('expo_push_token')->first();
  $user = App\Models\User::where('id', '!=', $owner->id)->first();
  // Simular velocidad de 130 km/h en un círculo/núcleo con límite de 90 km/h
  App\Jobs\SendSpeedingAlertJob::dispatch($owner, $user, 130.0, 90);
  ```
