# Guía de Pruebas Móviles con Túnel ngrok Personal

Esta guía detalla los pasos para exponer el servidor de desarrollo local de Expo (Metro Bundler) a internet utilizando tu propio túnel de **ngrok**. Esto es indispensable para realizar pruebas de rastreo de ubicación, geofencing y notificaciones en un dispositivo físico real conectado a la red de datos móviles (4G/5G) cuando el tester se desplaza fuera del rango de la red Wi-Fi local.

> [!NOTE]
> **¿Por qué no usar `npx expo start --tunnel`?**
> El comando `--tunnel` nativo de Expo utiliza una cuenta compartida global que frecuentemente alcanza el límite de 5000 conexiones simultáneas de ngrok (`ERR_NGROK_108`), produciendo un fallo silencioso con el error `TypeError: Cannot read properties of undefined (reading 'body')`. Usar tu propia cuenta soluciona este problema de manera definitiva.

---

## Requisitos Previos

1. Tener instalado **ngrok** (versión 3.x) en el sistema.
2. Contar con una cuenta gratuita en [ngrok.com](https://dashboard.ngrok.com/).
3. Configurar tu authtoken personal en la terminal:
   ```bash
   ngrok config add-authtoken <TU_AUTHTOKEN_PERSONAL>
   ```

---

## Configuración y Flujo de Trabajo

### Paso 1: Reclamar tu Dominio Fijo (Opcional - Recomendado)
Para evitar que la URL de pruebas cambie cada vez que reinicies el túnel, puedes reclamar un dominio gratuito en ngrok:
1. Entra a tu panel en [dashboard.ngrok.com](https://dashboard.ngrok.com/).
2. Ve a **Cloud Edge** > **Domains** y crea/copia tu dominio estático (por ejemplo: `mi-proyecto.ngrok-free.app`).

---

### Paso 2: Iniciar el Túnel de ngrok
Abre una terminal y levanta el túnel en el puerto `8081` (puerto por defecto del Metro Bundler de Expo):

* **Si usas dominio estático:**
  ```bash
  ngrok http 8081 --domain=mi-proyecto.ngrok-free.app
  ```
* **Si usas dominio dinámico (URL aleatoria):**
  ```bash
  ngrok http 8081
  ```

> [!IMPORTANT]
> **No cierres esta terminal.** El túnel permanecerá activo únicamente mientras este proceso siga en ejecución.

---

### Paso 3: Iniciar el Servidor de Expo (Metro)
En tu terminal principal de desarrollo (dentro de la carpeta `mobile`), inicia el servidor de desarrollo indicándole la URL de tu túnel mediante la variable de entorno `EXPO_PACKAGER_PROXY_URL`:

```bash
EXPO_PACKAGER_PROXY_URL=https://mi-proyecto.ngrok-free.app npx expo start
```
*(Si usas dominio dinámico, reemplaza la URL por la que te muestre la terminal de ngrok en la sección "Forwarding")*

---

### Paso 4: Conectar el Dispositivo Móvil
Para el teléfono móvil físico que va a realizar la prueba en la calle:

1. **Configura el dispositivo:** Apaga el Wi-Fi del celular y enciende los datos móviles (4G/5G). Asegúrate de que el GPS esté encendido.
2. **Abrir la App:** Inicia la aplicación **Expo Go**.
3. **Conectarse:**
   * **Método Manual (Recomendado):** En la app Expo Go, selecciona la opción para ingresar una dirección URL manualmente y escribe el esquema usando `exp://` en lugar de `https://`:
     ```text
     exp://mi-proyecto.ngrok-free.app
     ```
   * **Escaneo QR:** Escanea el código QR que se visualiza en la consola de Expo. Este QR apuntará automáticamente a tu dirección de ngrok externa.

---

## Solución de Problemas Comunes

* **"Network connection lost" al salir a la calle:**
  Verifica que tu terminal con `ngrok` no se haya cerrado y que el notebook no haya entrado en modo de suspensión o suspensión de red.
* **El backend local no responde:**
  Si la aplicación móvil intenta comunicarse con un backend corriendo en tu máquina local (`localhost:8000`), el celular no podrá acceder a él directamente por estar fuera de la red local. 
  * Para resolverlo, asegúrate de que la app móvil apunte al backend desplegado en Railway, o en su defecto, abre un segundo túnel de ngrok hacia el puerto de tu backend (ej. `ngrok http 8000`) y actualiza la URL del backend en la configuración de la app móvil.
