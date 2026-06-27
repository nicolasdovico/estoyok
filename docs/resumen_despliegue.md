# Bitácora de Despliegue en Railway: "Estoy Ok"
**Fecha:** 26-27 de Junio, 2026

Este documento detalla todas las acciones, correcciones y configuraciones realizadas durante la sesión de despliegue de la plataforma en **Railway.com**, sirviendo como contexto inmediato para reanudar el trabajo.

---

## 🛠️ Servicios Desplegados
Se encuentran activos y en línea en Railway los siguientes servicios:
1. **PostgreSQL + PostGIS** (Base de datos espacial).
2. **Redis** (Caché y Colas).
3. **backend-api** (Laravel API - Servidor en puerto `8080` detrás del proxy).
4. **backend-worker** (Laravel Queue Worker corriendo `php artisan queue:work`).
5. **backend-scheduler** (Laravel Scheduler corriendo `php artisan schedule:work`).
6. **frontend-web** (Next.js Frontend).

---

## 🐞 Problemas Detectados y Soluciones Aplicadas

### 1. Error de Compilación TypeScript en el Frontend Web (`next build`)
* **Síntoma:** El deploy del frontend fallaba en Railway con un error indicando que los tipos `UserData` en `Dashboard.tsx` y `BillingSection.tsx` eran incompatibles.
* **Causa:** Existían dos definiciones independientes de la interfaz `UserData` en ambos archivos. La definición en el Dashboard contenía campos adicionales indispensables (`circles` y `current_location`).
* **Solución:**
  * Exportamos la interfaz `UserData` desde [Dashboard.tsx](file:///home/usuario/aplicaciones/estoyok/frontend-web/src/components/Dashboard.tsx).
  * La importamos en [BillingSection.tsx](file:///home/usuario/aplicaciones/estoyok/frontend-web/src/components/BillingSection.tsx), eliminando la interfaz duplicada local.
  * Agregamos tipado explícito `(e: any)` en los callbacks de cambio de tarjeta de Stripe para evitar errores con la regla estricta `noImplicitAny`.

### 2. Estilos Rotos en el Panel de Filament (`/admin/login`)
* **Síntoma:** El panel administrativo del backend se cargaba sin estilos CSS en producción.
* **Causa:** Railway realiza *SSL Offloading* (tráfico externo es HTTPS, pero interno es HTTP). Laravel, al no reconocer el proxy, generaba las URLs de los assets con `http://`, lo que provocaba un bloqueo por **Contenido Mixto** en el navegador.
* **Solución:**
  * Modificamos [bootstrap/app.php](file:///home/usuario/aplicaciones/estoyok/backend/bootstrap/app.php) para configurar los Proxies de Confianza en Laravel 11/12:
    ```php
    ->withMiddleware(function (Middleware $middleware): void {
        $middleware->trustProxies(at: '*');
    })
    ```
  * Confirmamos la importancia de que la variable `APP_URL` comience con `https://` en Railway.

### 3. Error 403 Forbidden al Iniciar Sesión en Filament
* **Síntoma:** Al loguearse en `/admin/login`, se devolvía un error HTTP 403.
* **Causa:** Filament v3 restringe el acceso al panel `/admin` en entornos de producción (`APP_ENV=production`) a menos que el modelo de usuario implemente explícitamente las reglas de acceso.
* **Solución:**
  * Modificamos [User.php](file:///home/usuario/aplicaciones/estoyok/backend/app/Models/User.php) implementando la interfaz `FilamentUser` y el método `canAccessPanel`:
    ```php
    public function canAccessPanel(Panel $panel): bool
    {
        return str_ends_with($this->email, '@estoyok.com');
    }
    ```
  * Esto restringe el acceso de usuarios externos y permite que la cuenta del seeder (`admin@estoyok.com` / `admin123`) ingrese normalmente.

### 4. Error de Registro de Usuarios (Class "Redis" not found)
* **Síntoma:** Al registrar un usuario, la API devolvía un "Server error" (HTTP 500). Los logs mostraban la ausencia de la clase PHP `Redis`.
* **Causa:** El backend encolaba la tarea usando Redis, pero la extensión nativa `phpredis` no estaba presente en la imagen compilada por Nixpacks en Railway.
* **Solución:**
  * Modificamos el archivo [composer.json](file:///home/usuario/aplicaciones/estoyok/backend/composer.json) agregando la dependencia `"ext-redis": "*"` en el bloque `"require"`.
  * Esto fuerza a Nixpacks a compilar e instalar la extensión nativa de Redis para PHP durante el proceso de build.

### 5. Timeout en el Envío de Correos con Resend (Puertos SMTP Bloqueados)
* **Síntoma:** El flujo de envío del OTP fallaba con un error de timeout al intentar conectarse al servidor SMTP de Resend en los puertos 587 y 465.
* **Causa:** Railway tiene bloqueados los puertos de correo tradicionales por seguridad para prevenir spam.
* **Solución:**
  * Cambiamos la configuración del correo al puerto de escape alternativo **`2587`** con encriptación **`tls`** en las variables de entorno de Railway. Este puerto no está restringido y conecta con éxito a `smtp.resend.com`.
  * Habilitamos de forma temporal el canal de logs `LOG_CHANNEL=stderr` para diagnosticar errores de Laravel directamente en la consola de Railway.

---

## 📲 Pruebas del Frontend Mobile (Expo Go)
Para probar la aplicación móvil conectada a los servicios de producción en Railway:
1. **Configuración en el código:** Cambiamos la variable de conexión `BASE_URL` en [api.ts](file:///home/usuario/aplicaciones/estoyok/mobile/src/services/api.ts) de la IP local a la URL de producción:
   `const BASE_URL = 'https://backend-api-production-aec1.up.railway.app/api';`
2. **Pruebas en exteriores (Geofencing):** Para probar fuera del rango del WiFi de casa (usando datos móviles) y poder reiniciar/recargar la app de Expo Go, se debe iniciar Metro en modo túnel:
   ```bash
   npx expo start --tunnel
   ```

---

## 📧 Estrategia de Testing Multicuenta en Resend (Plan Gratuito)
Dado que el plan gratuito sin dominio verificado restringe los destinatarios únicamente a tu cuenta de registro, se definieron tres alternativas:
1. **Alias de Gmail (`+`):** Registrar cuentas en la app usando `tuusuario+papa@gmail.com`, `tuusuario+mama@gmail.com`. Se registran como usuarios distintos en Laravel pero todos los OTPs llegan al mismo buzón de correo.
2. **Test Addresses:** Añadir correos de colaboradores externos desde el panel de Resend en la pestaña **Audience** enviándoles una invitación previa.
3. **Dominio Propio:** Configurar registros DNS para producción ilimitada.

---

## 📌 Pendientes para la Próxima Sesión
* [ ] Restaurar la variable de entorno **`QUEUE_CONNECTION=redis`** en `backend-api` (actualmente en `sync` para depuración) para regresar el procesamiento de correos al segundo plano a través del Worker.
* [ ] Verificar el inicio del servicio `backend-worker` y comprobar que lea las tareas de Redis una vez devuelto a modo de colas.
* [ ] Configuración final de pasarelas de pago y webhooks en producción.
