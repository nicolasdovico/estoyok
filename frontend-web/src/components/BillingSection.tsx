'use client';

import { useState } from 'react';

interface UserData {
  id: number;
  name: string;
  email: string;
  is_premium: boolean;
  checkin_interval_hours: number;
  last_check_in_at: string | null;
  quiet_hours_enabled?: boolean;
  quiet_hours_start?: string | null;
  quiet_hours_end?: string | null;
  allow_sms_whatsapp_checkin?: boolean;
  escalation_enabled?: boolean;
  escalation_interval_minutes?: number;
  share_contact_responses?: boolean;
  low_battery_alerts_enabled?: boolean;
  proximity_alerts_enabled?: boolean;
  wifi_checkin_enabled?: boolean;
  safe_wifi_ssid?: string | null;
  sensor_checkin_enabled?: boolean;
}

interface BillingSectionProps {
  userData: UserData | null;
  setUserData: React.Dispatch<React.SetStateAction<UserData | null>>;
  showToast: (message: string, type: 'success' | 'error') => void;
}

export default function BillingSection({
  userData,
  showToast,
}: BillingSectionProps) {
  const [selectedProvider, setSelectedProvider] = useState<'stripe' | 'mercadopago' | 'paypal'>('stripe');
  const [loading, setLoading] = useState(false);

  const handleCheckout = async (provider: 'stripe' | 'mercadopago' | 'paypal') => {
    setLoading(true);
    const token = localStorage.getItem('auth_token');
    if (!token) {
      showToast('Sesión no válida. Inicia sesión nuevamente.', 'error');
      setLoading(false);
      return;
    }

    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/subscriptions/checkout`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
          'Accept': 'application/json',
        },
        body: JSON.stringify({ provider, plan: 'premium' }),
      });

      if (response.ok) {
        const data = await response.json();
        if (data.checkout_url) {
          showToast(`Redirigiendo de forma segura a ${provider === 'stripe' ? 'Stripe Checkout' : provider === 'mercadopago' ? 'Mercado Pago' : 'PayPal'}...`, 'success');
          // Redirigir al checkout hosted
          window.location.href = data.checkout_url;
        } else {
          showToast('No se recibió la URL de pago.', 'error');
        }
      } else {
        const errData = await response.json();
        showToast(errData.message || 'Error al generar checkout.', 'error');
      }
    } catch {
      showToast('Error de red al procesar el checkout.', 'error');
    } finally {
      setLoading(false);
    }
  };

  if (userData?.is_premium) {
    return (
      <div className="bg-white p-8 rounded-3xl border border-yellow-100 shadow-sm space-y-6">
        <div className="flex items-center gap-4">
          <div className="w-14 h-14 bg-yellow-100 rounded-2xl flex items-center justify-center text-3xl shadow-inner animate-pulse">
            ⭐
          </div>
          <div>
            <span className="text-[10px] bg-yellow-100 text-yellow-800 font-extrabold uppercase px-2.5 py-1 rounded-full tracking-wider">
              Miembro PRO
            </span>
            <h2 className="text-xl font-black text-gray-900 mt-1.5 font-sans">Suscripción Activa</h2>
          </div>
        </div>

        <div className="p-4 bg-yellow-50/50 rounded-2xl border border-yellow-100/50 space-y-2">
          <p className="text-sm font-medium text-gray-700 font-sans">
            ¡Gracias por confiar en **Estoy Ok** para proteger a tu familia!
          </p>
          <p className="text-xs text-gray-500 font-sans leading-relaxed">
            Tu cuenta cuenta actualmente con acceso ilimitado a todas las herramientas avanzadas: historial de 30 días, radares de proximidad dinámicos, alertas de velocidad, detección de colisiones por acelerómetro y S.O.S. de máxima prioridad.
          </p>
        </div>

        <div className="pt-4 border-t border-gray-100 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
          <div>
            <h3 className="text-xs font-bold text-gray-400 uppercase tracking-widest font-sans">Facturación</h3>
            <p className="text-sm font-bold text-gray-800 mt-1 font-sans">Débito automático mensual activo</p>
          </div>
          <button
            onClick={() => showToast('El portal de autogestión de facturación se habilitará en el Issue 5.', 'success')}
            className="px-5 py-2.5 bg-gray-900 hover:bg-gray-800 text-white rounded-xl text-xs font-bold transition-all shadow-sm cursor-pointer"
          >
            ⚙️ Administrar Suscripción
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      {/* Comparación de Planes */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Plan Gratis */}
        <div className="bg-white p-6 rounded-3xl border border-gray-150 shadow-sm relative overflow-hidden flex flex-col justify-between">
          <div className="space-y-4">
            <div>
              <span className="text-[9px] bg-gray-100 text-gray-600 font-extrabold uppercase px-2.5 py-1 rounded-full tracking-wider">
                Plan Básico
              </span>
              <h3 className="text-lg font-black text-gray-900 mt-2 font-sans">Básico (Free)</h3>
            </div>
            <div className="flex items-baseline gap-1 text-gray-900 font-sans">
              <span className="text-3xl font-black">$0</span>
              <span className="text-xs text-gray-500 font-medium">/ mes</span>
            </div>
            <ul className="space-y-3 pt-2">
              <li className="flex items-start gap-2.5 text-xs text-gray-600 font-medium">
                <span className="text-emerald-500 font-bold">✓</span>
                <span>Check-in manual una vez al día</span>
              </li>
              <li className="flex items-start gap-2.5 text-xs text-gray-600 font-medium">
                <span className="text-emerald-500 font-bold">✓</span>
                <span>Hasta 3 contactos de emergencia</span>
              </li>
              <li className="flex items-start gap-2.5 text-xs text-gray-600 font-medium">
                <span className="text-emerald-500 font-bold">✓</span>
                <span>Historial de ubicación de las últimas 24 horas</span>
              </li>
              <li className="flex items-start gap-2.5 text-xs text-gray-600 font-medium">
                <span className="text-emerald-500 font-bold">✓</span>
                <span>Alertas preventivas básicas (Email / Push)</span>
              </li>
            </ul>
          </div>
          <div className="pt-6 border-t border-gray-100 mt-6">
            <span className="block w-full py-2.5 bg-gray-100 text-gray-500 rounded-xl text-xs font-bold text-center">
              Plan Activo
            </span>
          </div>
        </div>

        {/* Plan Premium PRO */}
        <div className="bg-gradient-to-br from-neutral-900 via-neutral-950 to-neutral-900 p-6 rounded-3xl border border-yellow-500/20 shadow-xl relative overflow-hidden flex flex-col justify-between text-white">
          <div className="absolute top-0 right-0 w-32 h-32 bg-yellow-500/10 rounded-full blur-3xl pointer-events-none"></div>
          
          <div className="space-y-5">
            <div className="flex items-center justify-between">
              <div>
                <span className="text-[9px] bg-yellow-500/20 text-yellow-400 font-extrabold uppercase px-2.5 py-1 rounded-full tracking-wider">
                  Plan PRO
                </span>
                <h3 className="text-lg font-black text-white mt-2 font-sans">Premium PRO</h3>
              </div>
              <span className="text-xl animate-pulse">⭐</span>
            </div>
            
            <div className="flex items-baseline gap-1 text-white font-sans border-b border-neutral-850 pb-4">
              <span className="text-4xl font-black">$4.99</span>
              <span className="text-xs text-gray-400 font-medium">/ mes (débito automático)</span>
            </div>
            
            <ul className="space-y-4 pt-2">
              <li className="flex items-start gap-3 text-xs">
                <span className="text-yellow-400 font-bold text-sm leading-none">✓</span>
                <div className="space-y-0.5">
                  <p className="font-bold text-neutral-100">Auto-Check-in Pasivo Inteligente</p>
                  <p className="text-[10px] text-neutral-400 font-medium leading-relaxed">Olvídate del check-in manual. Confirma tu seguridad automáticamente al llegar a casa (Wi-Fi seguro) o con tus pasos diarios (podómetro).</p>
                </div>
              </li>
              <li className="flex items-start gap-3 text-xs">
                <span className="text-yellow-400 font-bold text-sm leading-none">✓</span>
                <div className="space-y-0.5">
                  <p className="font-bold text-neutral-100">Detección de Accidentes (Fuerza G)</p>
                  <p className="text-[10px] text-neutral-400 font-medium leading-relaxed">El sensor del acelerómetro detecta impactos severos. En caso de colisión, se activa una sirena acústica local y se alerta automáticamente a tu red con tu ubicación en tiempo real.</p>
                </div>
              </li>
              <li className="flex items-start gap-3 text-xs">
                <span className="text-yellow-400 font-bold text-sm leading-none">✓</span>
                <div className="space-y-0.5">
                  <p className="font-bold text-neutral-100">S.O.S. Silencioso y Grabación Ambiental</p>
                  <p className="text-[10px] text-neutral-400 font-medium leading-relaxed">Dispara un protocolo discreto de crisis: acelera el tracking GPS a 5 segundos y graba 15s de audio ambiente, disponible para tus contactos de emergencia.</p>
                </div>
              </li>
              <li className="flex items-start gap-3 text-xs">
                <span className="text-yellow-400 font-bold text-sm leading-none">✓</span>
                <div className="space-y-0.5">
                  <p className="font-bold text-neutral-100">Alertas Críticas vía WhatsApp & SMS</p>
                  <p className="text-[10px] text-neutral-400 font-medium leading-relaxed">Mensajería ilimitada y redundante vía WhatsApp y SMS. Tus contactos se enteran de inactividad o emergencias incluso si no tienen internet ni la app instalada.</p>
                </div>
              </li>
              <li className="flex items-start gap-3 text-xs">
                <span className="text-yellow-400 font-bold text-sm leading-none">✓</span>
                <div className="space-y-0.5">
                  <p className="font-bold text-neutral-100">Radares de Proximidad Relativos</p>
                  <p className="text-[10px] text-neutral-400 font-medium leading-relaxed">Establece distancias de seguridad dinámica con tus hijos. Ambos teléfonos vibrarán de forma persistente y mostrarán alertas si se alejan más de lo configurado.</p>
                </div>
              </li>
              <li className="flex items-start gap-3 text-xs">
                <span className="text-yellow-400 font-bold text-sm leading-none">✓</span>
                <div className="space-y-0.5">
                  <p className="font-bold text-neutral-100">Historial Completo de 30 Días</p>
                  <p className="text-[10px] text-neutral-400 font-medium leading-relaxed">Consulta y reproduce la trayectoria exacta de cualquier miembro de tu núcleo familiar del último mes con velocímetro detallado.</p>
                </div>
              </li>
              <li className="flex items-start gap-3 text-xs">
                <span className="text-yellow-400 font-bold text-sm leading-none">✓</span>
                <div className="space-y-0.5">
                  <p className="font-bold text-neutral-100">Monitoreo de Sensores y Batería</p>
                  <p className="text-[10px] text-neutral-400 font-medium leading-relaxed">Recibe avisos de batería baja (&lt;15%) de tus seres queridos y entérate si apagaron el GPS o activaron el Modo Avión.</p>
                </div>
              </li>
              <li className="flex items-start gap-3 text-xs">
                <span className="text-yellow-400 font-bold text-sm leading-none">✓</span>
                <div className="space-y-0.5">
                  <p className="font-bold text-neutral-100">Alertas Escalonadas Secuenciales</p>
                  <p className="text-[10px] text-neutral-400 font-medium leading-relaxed">Evita alarmar a todos a la vez. Configura el orden de contactos y el tiempo de retraso para alertas consecutivas.</p>
                </div>
              </li>
            </ul>
          </div>

          <div className="pt-6 border-t border-neutral-800 mt-6 text-center">
            <span className="inline-block text-[10px] text-yellow-400 font-extrabold uppercase tracking-wide">
              ⭐ Seguridad familiar de máxima fidelidad
            </span>
          </div>
        </div>
      </div>

      {/* Pasarelas de Pago */}
      <div className="bg-white p-6 rounded-3xl border border-gray-100 shadow-sm space-y-6">
        <div>
          <h3 className="text-sm font-black text-gray-900 font-sans">Método de Pago Preferido</h3>
          <p className="text-[11px] text-gray-500 font-medium font-sans">Selecciona la forma de pago para el débito automático mensual.</p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
          {/* Opción Stripe (Tarjeta) */}
          <button
            type="button"
            onClick={() => setSelectedProvider('stripe')}
            className={`p-4 rounded-2xl border-2 text-left transition-all cursor-pointer ${
              selectedProvider === 'stripe'
                ? 'border-indigo-600 bg-indigo-50/20'
                : 'border-gray-100 hover:border-gray-200 bg-white'
            }`}
          >
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-gray-800 font-sans">Tarjeta de Crédito</span>
              <span className="text-lg">💳</span>
            </div>
            <p className="text-[10px] text-gray-400 mt-1 font-medium font-sans">Checkout integrado Stripe</p>
          </button>

          {/* Opción Mercado Pago */}
          <button
            type="button"
            onClick={() => setSelectedProvider('mercadopago')}
            className={`p-4 rounded-2xl border-2 text-left transition-all cursor-pointer ${
              selectedProvider === 'mercadopago'
                ? 'border-blue-600 bg-blue-50/20'
                : 'border-gray-100 hover:border-gray-200 bg-white'
            }`}
          >
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-gray-800 font-sans">Mercado Pago</span>
              <span className="text-lg">💙</span>
            </div>
            <p className="text-[10px] text-gray-400 mt-1 font-medium font-sans">Suscripción local en Pesos (ARS)</p>
          </button>

          {/* Opción PayPal */}
          <button
            type="button"
            onClick={() => setSelectedProvider('paypal')}
            className={`p-4 rounded-2xl border-2 text-left transition-all cursor-pointer ${
              selectedProvider === 'paypal'
                ? 'border-yellow-600 bg-yellow-50/20'
                : 'border-gray-100 hover:border-gray-200 bg-white'
            }`}
          >
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-gray-800 font-sans">PayPal</span>
              <span className="text-lg">💛</span>
            </div>
            <p className="text-[10px] text-gray-400 mt-1 font-medium font-sans">Suscripción internacional en USD</p>
          </button>
        </div>

        {/* Panel Activo según Selección */}
        <div className="p-5 bg-gray-50 rounded-2xl border border-gray-150">
          {selectedProvider === 'stripe' ? (
            <div className="space-y-4">
              <div className="flex items-center gap-2">
                <span className="text-xs font-bold text-gray-800 font-sans">Suscripción Directa con Tarjeta</span>
                <span className="text-[10px] bg-indigo-100 text-indigo-800 px-2 py-0.5 rounded font-extrabold uppercase font-sans">PRO Elements</span>
              </div>
              <p className="text-xs text-gray-500 font-medium font-sans leading-relaxed">
                En el próximo issue (Issue 2) integraremos Stripe Elements seguro. Por ahora, utilizaremos el redireccionamiento seguro a Stripe Checkout para procesar la suscripción.
              </p>
              <button
                type="button"
                disabled={loading}
                onClick={() => handleCheckout('stripe')}
                className="w-full py-3 bg-indigo-600 hover:bg-indigo-750 disabled:bg-gray-300 text-white rounded-xl text-xs font-bold transition-all flex items-center justify-center gap-2 shadow-sm cursor-pointer font-sans"
              >
                {loading ? 'Procesando...' : '🔒 Suscribirse con Tarjeta vía Stripe Checkout'}
              </button>
            </div>
          ) : selectedProvider === 'mercadopago' ? (
            <div className="space-y-4">
              <div className="flex items-center gap-2">
                <span className="text-xs font-bold text-gray-800 font-sans">Suscripción Mensual vía Mercado Pago</span>
                <span className="text-[10px] bg-blue-100 text-blue-800 px-2 py-0.5 rounded font-extrabold uppercase font-sans">Débito ARS</span>
              </div>
              <p className="text-xs text-gray-500 font-medium font-sans leading-relaxed">
                Al presionar el botón serás redirigido de forma segura a Mercado Pago para confirmar el débito automático mensual desde tu saldo virtual o tarjeta guardada.
              </p>
              <button
                type="button"
                disabled={loading}
                onClick={() => handleCheckout('mercadopago')}
                className="w-full py-3 bg-blue-600 hover:bg-blue-700 disabled:bg-gray-300 text-white rounded-xl text-xs font-bold transition-all flex items-center justify-center gap-2 shadow-sm cursor-pointer font-sans"
              >
                {loading ? 'Generando enlace...' : '🔗 Continuar en Mercado Pago'}
              </button>
            </div>
          ) : (
            <div className="space-y-4">
              <div className="flex items-center gap-2">
                <span className="text-xs font-bold text-gray-800 font-sans">Suscripción Mensual vía PayPal</span>
                <span className="text-[10px] bg-yellow-100 text-yellow-800 px-2 py-0.5 rounded font-extrabold uppercase font-sans">Débito USD</span>
              </div>
              <p className="text-xs text-gray-500 font-medium font-sans leading-relaxed">
                Al presionar el botón se abrirá la pasarela segura de PayPal para autorizar el cargo automático mensual recurrente.
              </p>
              <button
                type="button"
                disabled={loading}
                onClick={() => handleCheckout('paypal')}
                className="w-full py-3 bg-yellow-650 hover:bg-yellow-700 disabled:bg-gray-300 text-white rounded-xl text-xs font-bold transition-all flex items-center justify-center gap-2 shadow-sm cursor-pointer font-sans"
              >
                {loading ? 'Generando enlace...' : '🔗 Continuar en PayPal'}
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
