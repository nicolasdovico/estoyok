# Matriz de Funcionalidades Exclusivas Premium PRO - Estoy Ok

Este documento detalla el análisis comparativo entre el **Plan Gratuito (FREE)** y el **Plan Premium PRO**, junto con los ganchos comerciales y la propuesta de valor para convertir usuarios en suscriptores pagos.

---

## 👑 Matriz Comparativa de Funcionalidades (Free vs Premium PRO)

| Funcionalidad / Beneficio | Plan Gratuito (FREE) 🛡️ | Plan Premium PRO 👑 (7 Días Gratis, luego $4.99/mes) | Gancho Comercial / Valor Percibido |
| :--- | :--- | :--- | :--- |
| **Alertas de Emergencia SOS e Inactividad** | Notificaciones Push y Emails estándar | **WhatsApp Automatizado + SMS Nativo Directo** a Contactos SOS | *"Nadie mira el email en una urgencia. El WhatsApp/SMS llega al instante."* |
| **Telemetría Vehicular en el Mapa** | Resumen general y métricas sumadas (sin mapa detallado) | **Trazado de Ruta en Mapa + Marcadores de Excesos, Frenadas Bruscas, Aceleración y Uso de Celular** | *"Conoce exactamente por dónde manejó y si usó el celular al volante."* |
| **Historial de Rutas y Ubicaciones** | **Últimas 24 Horas** | **Últimos 30 Días** con reproductor interactivo de recorridos | *"No te quedes con la duda de dónde estuvieron la semana pasada."* |
| **Detección Automática de Accidentes (Crash)** | Desactivado | **Detección de impactos (4.5G o más) por acelerómetro con Sirena y Alerta SOS Automática** | *"Si sufren un accidente en ruta y quedan inconscientes, la app avisa sola."* |
| **S.O.S. Silencioso con Audio Ambiental** | Notificación Push básica a la familia | **S.O.S. con 15s de Grabación Ambiente de Fondo + Rastro GPS prioritario (5s)** | *"En momentos de peligro o robo, escucha en vivo lo que ocurre en el lugar."* |
| **Zonas Seguras (Geocercas)** | Hasta 2 Zonas Seguras | **Zonas Seguras Ilimitadas** con Avisos de Entrada y Salida | *"Monitorea colegio, club, casa de abuelos y trabajo sin límites."* |
| **Radar Móvil de Proximidad** | Desactivado | **Proximidad Relativa Dinámica entre Miembros** (ej: 50m, 100m) | *"Ideal para viajes o shopping: te avisa si alguien se aleja del grupo."* |
| **Alertas de Batería Baja y Sensores** | Básico | **Alertas prioritarias de Batería Baja (15% o menos), GPS Desactivado y Modo Avión** | *"Enterate antes de que se queden sin batería o apaguen el GPS."* |
| **Frecuencia de Rastreo GPS** | Estándar (15s a 30s) | **Alta Frecuencia Adaptativa (5s en vehículo, 30s caminando)** | *"Seguimiento fluido y en tiempo real sin saltos en el mapa."* |

---

## 🎁 Estrategia de Conversión: Prueba Gratis de 7 Días (7-Day Free Trial)

Para reducir la fricción en la compra y maximizar las conversiones, el sistema implementa la mecánica de **Prueba Gratis sin Riesgo**:

1. **Acceso Total Inmediato ($0.00 hoy):** El usuario activa el plan PRO sin pagar nada el primer día. Desbloquea al instante los mensajes de WhatsApp, telemetría vehicular, historial de 30 días y detección de accidentes.
2. **Notificación Preventiva de Transparencia (Día 5):** Se le envía una notificación Push e Email avisando que su prueba termina en 2 días y que comenzará la suscripción mensual ($4.99/mes), permitiéndole cancelar sin costo si lo desea.
3. **Débito Automático Recurrente (Día 8):** Finalizados los 7 días, se efectúa el primer cobro mensual de forma automática a través de Stripe, Google Play Billing, Mercado Pago o PayPal.

---

## 💻 Enforzamiento Técnico en el Backend (Laravel API)

Las restricciones para cuentas gratuitas están aplicadas a nivel de código en el servidor:

* **Historial de 24h vs 30d:** `app/Http/Controllers/Api/HistoryController.php` (valida `$isPremium` antes de devolver posiciones mayores a 24h).
* **Ocultamiento de Coordenadas de Telemetría Vehicular:** `app/Http/Controllers/Api/DriveController.php` (para usuarios Free, remueve los puntos de ruta `route_points` y las coordenadas GPS de los eventos de velocidad/frenadas del JSON).
* **Envío de WhatsApp y SMS:** `app/Jobs/SendInactivityAlerts.php` y `app/Jobs/SendCrashAlertJob.php` (verifican `$user->hasPremiumAccess()` antes de invocar Twilio SDK).
