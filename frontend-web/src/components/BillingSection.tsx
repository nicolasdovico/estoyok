'use client';

import { useState } from 'react';
import { loadStripe } from '@stripe/stripe-js';
import { Elements, useStripe, useElements, CardNumberElement, CardExpiryElement, CardCvcElement } from '@stripe/react-stripe-js';

const stripePromise = loadStripe(process.env.NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY || 'pk_test_TYooMQauvdEDq54NiTphI7jx');

import { UserData } from './Dashboard';

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
  const [mockupType, setMockupType] = useState<'inactivity' | 'sos' | 'accident'>('inactivity');

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
    <div className="space-y-10">
      {/* Encabezado Principal de Alta Conversión */}
      <div className="text-center max-w-2xl mx-auto space-y-3">
        <span className="text-[10px] font-black tracking-widest uppercase bg-indigo-50 text-indigo-600 px-3 py-1 rounded-full border border-indigo-100 font-sans">
          Membresía Premium PRO
        </span>
        <h2 className="text-2xl md:text-3xl font-black text-gray-900 tracking-tight leading-none font-sans">
          Seguridad Familiar en Piloto Automático
        </h2>
        <p className="text-xs md:text-sm text-gray-500 font-medium max-w-lg mx-auto leading-relaxed font-sans">
          La máxima tecnología de protección pasiva y activa, diseñada para que tu familia esté a salvo sin necesidad de que recuerden hacer check-ins manuales.
        </p>
      </div>

      {/* Grid: Comparación de Planes & Simulador */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        
        {/* Lado Izquierdo: Beneficios Seductores (8 cols) */}
        <div className="lg:col-span-7 space-y-6">
          
          {/* Card Premium PRO Destacada con Glow */}
          <div className="relative bg-gradient-to-br from-neutral-900 via-neutral-950 to-neutral-900 p-6 md:p-8 rounded-3xl border border-yellow-500/30 shadow-2xl overflow-hidden text-white">
            <div className="absolute top-0 right-0 w-48 h-48 bg-yellow-500/5 rounded-full blur-3xl pointer-events-none"></div>
            
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-neutral-800 pb-5">
              <div>
                <div className="flex items-center gap-2">
                  <span className="text-[9px] bg-yellow-500/20 text-yellow-400 font-extrabold uppercase px-2.5 py-1 rounded-full tracking-wider font-sans">
                    Plan PRO recomendado
                  </span>
                  <span className="text-xs">⭐</span>
                </div>
                <h3 className="text-xl font-black text-white mt-2 font-sans">Premium PRO</h3>
                <p className="text-[10px] text-gray-400 mt-1 font-sans">Suscripción mensual familiar recurrente</p>
              </div>
              <div className="flex items-baseline gap-1 text-white font-sans bg-neutral-850 px-4 py-2.5 rounded-2xl border border-neutral-800">
                <span className="text-3xl font-black text-yellow-400">$4.99</span>
                <span className="text-[10px] text-gray-400 font-medium">/ mes</span>
              </div>
            </div>

            {/* Listado de Beneficios Clave */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-5 pt-6">
              
              <div className="space-y-1">
                <div className="flex items-center gap-2">
                  <span className="text-yellow-400 text-sm">💬</span>
                  <h4 className="text-xs font-bold text-neutral-100 font-sans">WhatsApp & SMS Ilimitados</h4>
                </div>
                <p className="text-[10px] text-neutral-400 leading-relaxed font-sans pl-6">
                  Tus familiares reciben alertas instantáneas por SMS y WhatsApp si algo ocurre, sin necesitar internet ni tener la app instalada.
                </p>
              </div>

              <div className="space-y-1">
                <div className="flex items-center gap-2">
                  <span className="text-yellow-400 text-sm">🛡️</span>
                  <h4 className="text-xs font-bold text-neutral-100 font-sans">Auto-Check-in Pasivo</h4>
                </div>
                <p className="text-[10px] text-neutral-400 leading-relaxed font-sans pl-6">
                  Se reporta de forma inteligente al llegar a casa (Wi-Fi seguro) o a través de sensores corporales (pasos del día).
                </p>
              </div>

              <div className="space-y-1">
                <div className="flex items-center gap-2">
                  <span className="text-yellow-400 text-sm">🚨</span>
                  <h4 className="text-xs font-bold text-neutral-100 font-sans">SOS Silencioso & Audio</h4>
                </div>
                <p className="text-[10px] text-neutral-400 leading-relaxed font-sans pl-6">
                  Ante peligro, activa el SOS silencioso: transmite ubicación cada 5s y graba 15s de audio ambiente de inmediato.
                </p>
              </div>

              <div className="space-y-1">
                <div className="flex items-center gap-2">
                  <span className="text-yellow-400 text-sm">🚗</span>
                  <h4 className="text-xs font-bold text-neutral-100 font-sans">Detección de Accidentes</h4>
                </div>
                <p className="text-[10px] text-neutral-400 leading-relaxed font-sans pl-6">
                  Mide fuerzas G por acelerómetro y envía alertas de ayuda instantáneas si detecta colisiones de tráfico.
                </p>
              </div>

              <div className="space-y-1">
                <div className="flex items-center gap-2">
                  <span className="text-yellow-400 text-sm">📍</span>
                  <h4 className="text-xs font-bold text-neutral-100 font-sans">Historial de 30 Días</h4>
                </div>
                <p className="text-[10px] text-neutral-400 leading-relaxed font-sans pl-6">
                  Acceso completo a la reproducción detallada de rutas, sensores y velocidad de tu núcleo familiar del último mes.
                </p>
              </div>

              <div className="space-y-1">
                <div className="flex items-center gap-2">
                  <span className="text-yellow-400 text-sm">🛰️</span>
                  <h4 className="text-xs font-bold text-neutral-100 font-sans">Radares de Proximidad</h4>
                </div>
                <p className="text-[10px] text-neutral-400 leading-relaxed font-sans pl-6">
                  Crea perímetros dinámicos entre dispositivos. Vibra y suena si tus hijos se alejan más de la distancia establecida.
                </p>
              </div>

            </div>

            {/* Microcopy de valor y ahorro */}
            <div className="mt-8 pt-5 border-t border-neutral-800 flex items-center justify-between text-[10px] text-neutral-400">
              <span className="flex items-center gap-1.5 text-yellow-400 font-bold">
                🛡️ Garantía de devolución de 30 días
              </span>
              <span>☕ Menos de $0.17 USD al día</span>
            </div>
          </div>

        </div>

        {/* Lado Derecho: WhatsApp Mockup Simulador Interactivo (5 cols) */}
        <div className="lg:col-span-5 space-y-6">
          <div className="bg-white p-6 rounded-3xl border border-gray-150 shadow-sm space-y-4">
            <div>
              <h3 className="text-xs font-black text-gray-900 font-sans">Simula la protección en tu móvil</h3>
              <p className="text-[10px] text-gray-500 font-medium font-sans">Observa qué mensaje real recibe tu red de emergencia vía WhatsApp.</p>
            </div>

            {/* Botones selectores del Mockup */}
            <div className="grid grid-cols-3 gap-1.5">
              <button
                type="button"
                onClick={() => setMockupType('inactivity')}
                className={`py-2 px-1 text-[9px] font-black rounded-lg border transition-all cursor-pointer ${
                  mockupType === 'inactivity'
                    ? 'bg-emerald-50 border-emerald-500 text-emerald-800'
                    : 'bg-gray-50 border-gray-150 text-gray-600 hover:bg-gray-100'
                }`}
              >
                Inactividad
              </button>
              <button
                type="button"
                onClick={() => setMockupType('sos')}
                className={`py-2 px-1 text-[9px] font-black rounded-lg border transition-all cursor-pointer ${
                  mockupType === 'sos'
                    ? 'bg-red-50 border-red-500 text-red-800'
                    : 'bg-gray-50 border-gray-150 text-gray-600 hover:bg-gray-100'
                }`}
              >
                S.O.S. Silencioso
              </button>
              <button
                type="button"
                onClick={() => setMockupType('accident')}
                className={`py-2 px-1 text-[9px] font-black rounded-lg border transition-all cursor-pointer ${
                  mockupType === 'accident'
                    ? 'bg-amber-50 border-amber-500 text-amber-800'
                    : 'bg-gray-50 border-gray-150 text-gray-600 hover:bg-gray-100'
                }`}
              >
                Accidente 🚗
              </button>
            </div>

            {/* Estructura del Celular WhatsApp */}
            <div className="border-[6px] border-neutral-800 rounded-[2.5rem] p-2.5 bg-neutral-900 shadow-xl relative overflow-hidden max-w-[280px] mx-auto">
              {/* Notch */}
              <div className="absolute top-2.5 left-1/2 -translate-x-1/2 w-20 h-4 bg-neutral-800 rounded-full z-20 flex items-center justify-center">
                <div className="w-1.5 h-1.5 bg-neutral-900 rounded-full"></div>
              </div>

              {/* Pantalla del Teléfono */}
              <div className="bg-[#efeae2] rounded-[2rem] overflow-hidden flex flex-col h-[400px] text-gray-900 relative">
                
                {/* Header WhatsApp */}
                <div className="bg-[#075e54] text-white px-3 pt-5 pb-2.5 flex items-center gap-2 text-[10px] font-sans shadow-md">
                  <span className="text-xs">←</span>
                  <div className="w-6 h-6 bg-emerald-600 rounded-full flex items-center justify-center font-bold text-[10px] text-white">
                    OK
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="font-bold truncate flex items-center gap-0.5">
                      Estoy Ok ✔️
                    </p>
                    <p className="text-[7px] text-emerald-200">En línea (Cuenta Oficial)</p>
                  </div>
                </div>

                {/* Área de Chats */}
                <div className="flex-1 p-3 overflow-y-auto space-y-2 flex flex-col justify-start">
                  
                  {/* Bubble */}
                  <div className="bg-white p-3 rounded-2xl shadow-xs max-w-[90%] text-[9px] leading-relaxed relative border-l-4 border-emerald-500 font-sans self-start">
                    <p className="font-extrabold text-emerald-600 text-[8px] uppercase tracking-wider mb-1">Estoy Ok - Alerta Oficial</p>
                    
                    {mockupType === 'inactivity' && (
                      <div className="space-y-1.5 text-gray-700">
                        <p>⚠️ <strong>AVISO DE INACTIVIDAD</strong></p>
                        <p><strong>{userData?.name || 'Juan Pérez'}</strong> no ha reportado bienestar en la app en las últimas <strong>24 horas</strong>.</p>
                        <p className="p-1 bg-gray-50 rounded border border-gray-100 font-mono text-[8px]">
                          📍 Última posición conocida:<br />
                          Lat: -34.6037, Lng: -58.3816
                        </p>
                        <p className="text-blue-600 font-semibold underline mt-1">
                          👉 ver-mapa-en-vivo-estoyok.com
                        </p>
                        <p className="text-[7px] text-gray-400 flex justify-between items-center mt-1">
                          <span>🔋 Batería: 14% | GPS: Activo</span>
                          <span>14:32 ✔️✔️</span>
                        </p>
                      </div>
                    )}

                    {mockupType === 'sos' && (
                      <div className="space-y-1.5 text-gray-700">
                        <p className="text-red-600">🚨 <strong>EMERGENCIA: S.O.S. SILENCIOSO</strong></p>
                        <p><strong>{userData?.name || 'Juan Pérez'}</strong> activó una alerta de máxima prioridad.</p>
                        <p className="font-semibold bg-red-50 text-red-800 p-1 rounded text-[8px] border border-red-100">
                          🔊 Grabación de Audio Ambiental de 15 segundos disponible en el mapa de emergencia.
                        </p>
                        <p className="text-blue-600 font-semibold underline mt-1">
                          👉 ver-mapa-en-vivo-estoyok.com
                        </p>
                        <p className="text-[7px] text-gray-400 flex justify-between items-center mt-1">
                          <span>🔋 Batería: 82% | Tracking: 5s</span>
                          <span>14:34 ✔️✔️</span>
                        </p>
                      </div>
                    )}

                    {mockupType === 'accident' && (
                      <div className="space-y-1.5 text-gray-700">
                        <p className="text-amber-600">🚗 <strong>ALERTA DE ACCIDENTE VEHICULAR</strong></p>
                        <p>El sensor detectó una desaceleración fuerte (colisión) en el móvil de <strong>{userData?.name || 'Juan Pérez'}</strong>.</p>
                        <p className="p-1 bg-amber-50 text-amber-800 rounded border border-amber-100 text-[8px]">
                          ⚡ Impacto G-Force: 4.2G<br />
                          🚗 Velocidad antes del impacto: 82 km/h
                        </p>
                        <p className="text-blue-600 font-semibold underline mt-1">
                          👉 ver-mapa-en-vivo-estoyok.com
                        </p>
                        <p className="text-[7px] text-gray-400 flex justify-between items-center mt-1">
                          <span>🔋 Batería: 76% | GPS: Activado</span>
                          <span>14:35 ✔️✔️</span>
                        </p>
                      </div>
                    )}
                  </div>

                </div>

                {/* Teclado simulado */}
                <div className="bg-white p-2 flex items-center justify-between gap-1 border-t border-gray-200">
                  <div className="flex-1 bg-gray-100 rounded-full px-3 py-1.5 text-[8px] text-gray-400">
                    Solo lectura...
                  </div>
                  <div className="w-5 h-5 bg-emerald-500 rounded-full flex items-center justify-center text-white text-[8px] font-bold">
                    🎤
                  </div>
                </div>
              </div>
            </div>

            <p className="text-[9px] text-gray-400 text-center leading-relaxed font-sans">
              *Los mensajes se envían de forma automática por SMS o WhatsApp directo mediante nuestra API integrada.*
            </p>
          </div>
        </div>

      </div>

      {/* Tabla Comparativa Premium Exclusiva a Pantalla Completa */}
      <div className="bg-white p-6 md:p-8 rounded-3xl border border-gray-150 shadow-sm space-y-6">
        <div className="space-y-1.5">
          <h3 className="text-sm font-black text-gray-900 font-sans flex items-center gap-2">
            📊 Comparativa Detallada de Características
          </h3>
          <p className="text-[11px] text-gray-500 font-medium font-sans">
            Analiza por qué la membresía Premium PRO es la opción ideal para mantener protegidos a tus seres queridos las 24 horas del día.
          </p>
        </div>

        <div className="overflow-x-auto rounded-2xl border border-gray-100">
          <table className="w-full text-left text-xs font-sans border-collapse">
            <thead>
              <tr className="bg-gray-50 text-gray-500 border-b border-gray-150">
                <th className="py-3 px-4 font-bold text-[10px] uppercase tracking-wider">Módulo / Funcionalidad</th>
                <th className="py-3 px-4 text-center font-bold text-[10px] uppercase tracking-wider w-1/3">Básico (Free)</th>
                <th className="py-3 px-4 text-center font-bold text-[10px] uppercase tracking-wider w-1/3 bg-indigo-50/30 text-indigo-900 border-x border-gray-150">
                  ⚡ Premium PRO (Recomendado)
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 text-gray-600">
              {/* Categoría: Canales de Alerta */}
              <tr className="bg-gray-50/50">
                <td colSpan={3} className="py-2 px-4 font-extrabold text-[9px] text-gray-400 tracking-widest uppercase">
                  💬 CANALES DE ALERTA & EMERGENCIA
                </td>
              </tr>
              <tr className="hover:bg-neutral-50/50 transition-colors">
                <td className="py-3 px-4">
                  <p className="font-bold text-gray-800">Alertas críticas a contactos</p>
                  <p className="text-[10px] text-gray-400 font-medium">Cómo se enteran tus contactos si necesitas auxilio.</p>
                </td>
                <td className="py-3 px-4 text-center">
                  <span className="inline-block text-[10px] bg-gray-100 text-gray-500 px-2 py-0.5 rounded-full font-bold">
                    Solo Push / Email
                  </span>
                </td>
                <td className="py-3 px-4 text-center bg-indigo-50/15 border-x border-gray-100">
                  <span className="inline-block text-[10px] bg-emerald-100 text-emerald-800 px-2.5 py-0.5 rounded-full font-black">
                    WhatsApp & SMS Ilimitados
                  </span>
                </td>
              </tr>
              <tr className="hover:bg-neutral-50/50 transition-colors">
                <td className="py-3 px-4">
                  <p className="font-bold text-gray-800">Costo de mensajería</p>
                  <p className="text-[10px] text-gray-400 font-medium">Valor de los envíos de alerta en emergencias.</p>
                </td>
                <td className="py-3 px-4 text-center">
                  <span className="inline-block text-[10px] bg-gray-100 text-gray-500 px-2 py-0.5 rounded-full font-bold">
                    Sin costo (No disponible)
                  </span>
                </td>
                <td className="py-3 px-4 text-center bg-indigo-50/15 border-x border-gray-100">
                  <span className="inline-block text-[10px] bg-emerald-100 text-emerald-800 px-2.5 py-0.5 rounded-full font-black">
                    100% Cubierto por Estoy Ok
                  </span>
                </td>
              </tr>

              {/* Categoría: Bienestar Familiar */}
              <tr className="bg-gray-50/50">
                <td colSpan={3} className="py-2 px-4 font-extrabold text-[9px] text-gray-400 tracking-widest uppercase">
                  🛡️ BIENESTAR DIARIO PASIVO
                </td>
              </tr>
              <tr className="hover:bg-neutral-50/50 transition-colors">
                <td className="py-3 px-4">
                  <p className="font-bold text-gray-800">Detección de Bienestar</p>
                  <p className="text-[10px] text-gray-400 font-medium">Método para registrar que te encuentras a salvo.</p>
                </td>
                <td className="py-3 px-4 text-center">
                  <span className="inline-block text-[10px] bg-gray-100 text-gray-500 px-2 py-0.5 rounded-full font-bold">
                    Manual (1 vez al día)
                  </span>
                </td>
                <td className="py-3 px-4 text-center bg-indigo-50/15 border-x border-gray-100">
                  <span className="inline-block text-[10px] bg-emerald-100 text-emerald-800 px-2.5 py-0.5 rounded-full font-black">
                    Pasivo Automático (Wi-Fi + Pasos)
                  </span>
                </td>
              </tr>
              <tr className="hover:bg-neutral-50/50 transition-colors">
                <td className="py-3 px-4">
                  <p className="font-bold text-gray-800">Intervalo de Reporte</p>
                  <p className="text-[10px] text-gray-400 font-medium">Cada cuánto tiempo debes dar señales de bienestar.</p>
                </td>
                <td className="py-3 px-4 text-center">
                  <span className="inline-block text-[10px] bg-gray-100 text-gray-500 px-2 py-0.5 rounded-full font-bold">
                    Fijo cada 24 hs
                  </span>
                </td>
                <td className="py-3 px-4 text-center bg-indigo-50/15 border-x border-gray-100">
                  <span className="inline-block text-[10px] bg-emerald-100 text-emerald-800 px-2.5 py-0.5 rounded-full font-black">
                    Configurable (1h a 48h)
                  </span>
                </td>
              </tr>
              <tr className="hover:bg-neutral-50/50 transition-colors">
                <td className="py-3 px-4">
                  <p className="font-bold text-gray-800">Modo Sueño y Escalado Secuencial</p>
                  <p className="text-[10px] text-gray-400 font-medium">Pausa de recordatorios nocturnos y orden de contactos.</p>
                </td>
                <td className="py-3 px-4 text-center">
                  <span className="inline-block text-[10px] bg-gray-100 text-gray-500 px-2 py-0.5 rounded-full font-bold">
                    Básico (Silenciado simple)
                  </span>
                </td>
                <td className="py-3 px-4 text-center bg-indigo-50/15 border-x border-gray-100">
                  <span className="inline-block text-[10px] bg-emerald-100 text-emerald-800 px-2.5 py-0.5 rounded-full font-black">
                    Personalizado con Delay Inteligente
                  </span>
                </td>
              </tr>

              {/* Categoría: Ubicación y Ruta */}
              <tr className="bg-gray-50/50">
                <td colSpan={3} className="py-2 px-4 font-extrabold text-[9px] text-gray-400 tracking-widest uppercase">
                  📍 GEOLOCALIZACIÓN Y RASTREO ACTIVO
                </td>
              </tr>
              <tr className="hover:bg-neutral-50/50 transition-colors">
                <td className="py-3 px-4">
                  <p className="font-bold text-gray-800">Historial de Ubicación</p>
                  <p className="text-[10px] text-gray-400 font-medium">Acceso a las trayectorias de tus familiares.</p>
                </td>
                <td className="py-3 px-4 text-center">
                  <span className="inline-block text-[10px] bg-gray-100 text-gray-500 px-2 py-0.5 rounded-full font-bold">
                    Últimas 24 horas
                  </span>
                </td>
                <td className="py-3 px-4 text-center bg-indigo-50/15 border-x border-gray-100">
                  <span className="inline-block text-[10px] bg-emerald-100 text-emerald-800 px-2.5 py-0.5 rounded-full font-black">
                    30 días completos con velocidad
                  </span>
                </td>
              </tr>
              <tr className="hover:bg-neutral-50/50 transition-colors">
                <td className="py-3 px-4">
                  <p className="font-bold text-gray-800">Zonas Seguras del Núcleo</p>
                  <p className="text-[10px] text-gray-400 font-medium">Perímetros que notifican automáticamente ingreso/salida.</p>
                </td>
                <td className="py-3 px-4 text-center">
                  <span className="inline-block text-[10px] bg-gray-100 text-gray-500 px-2 py-0.5 rounded-full font-bold">
                    Máximo 1 zona activa
                  </span>
                </td>
                <td className="py-3 px-4 text-center bg-indigo-50/15 border-x border-gray-100">
                  <span className="inline-block text-[10px] bg-emerald-100 text-emerald-800 px-2.5 py-0.5 rounded-full font-black">
                    Zonas Seguras Ilimitadas
                  </span>
                </td>
              </tr>
              <tr className="hover:bg-neutral-50/50 transition-colors">
                <td className="py-3 px-4">
                  <p className="font-bold text-gray-800">Radares de Proximidad Móviles</p>
                  <p className="text-[10px] text-gray-400 font-medium">Alertas de distancia relativa en vivo con tus hijos.</p>
                </td>
                <td className="py-3 px-4 text-center">
                  <span className="inline-block text-[10px] bg-gray-100 text-gray-400 px-2 py-0.5 rounded-full font-medium">
                    No disponible
                  </span>
                </td>
                <td className="py-3 px-4 text-center bg-indigo-50/15 border-x border-gray-100">
                  <span className="inline-block text-[10px] bg-emerald-100 text-emerald-800 px-2.5 py-0.5 rounded-full font-black">
                    Vibración & Sirena dinámicas
                  </span>
                </td>
              </tr>

              {/* Categoría: Seguridad Activa y Sensores */}
              <tr className="bg-gray-50/50">
                <td colSpan={3} className="py-2 px-4 font-extrabold text-[9px] text-gray-400 tracking-widest uppercase">
                  🚗 VEHÍCULOS, CHOQUES Y MONITOREO DE SENSORES
                </td>
              </tr>
              <tr className="hover:bg-neutral-50/50 transition-colors">
                <td className="py-3 px-4">
                  <p className="font-bold text-gray-800">Detección de Accidentes (G-Force)</p>
                  <p className="text-[10px] text-gray-400 font-medium">Reacción y alertas automáticas ante impactos vehiculares.</p>
                </td>
                <td className="py-3 px-4 text-center">
                  <span className="inline-block text-[10px] bg-gray-100 text-gray-450 px-2 py-0.5 rounded-full font-medium">
                    No disponible
                  </span>
                </td>
                <td className="py-3 px-4 text-center bg-indigo-50/15 border-x border-gray-100">
                  <span className="inline-block text-[10px] bg-emerald-100 text-emerald-800 px-2.5 py-0.5 rounded-full font-black">
                    Auxilio automático e inmediato
                  </span>
                </td>
              </tr>
              <tr className="hover:bg-neutral-50/50 transition-colors">
                <td className="py-3 px-4">
                  <p className="font-bold text-gray-800">Monitoreo de conducción y velocidad</p>
                  <p className="text-[10px] text-gray-400 font-medium">Registro de trayectos activos y alertas de excesos.</p>
                </td>
                <td className="py-3 px-4 text-center">
                  <span className="inline-block text-[10px] bg-gray-100 text-gray-450 px-2 py-0.5 rounded-full font-medium">
                    No disponible
                  </span>
                </td>
                <td className="py-3 px-4 text-center bg-indigo-50/15 border-x border-gray-100">
                  <span className="inline-block text-[10px] bg-emerald-100 text-emerald-800 px-2.5 py-0.5 rounded-full font-black">
                    Historial de Conducción PRO
                  </span>
                </td>
              </tr>
              <tr className="hover:bg-neutral-50/50 transition-colors">
                <td className="py-3 px-4">
                  <p className="font-bold text-gray-800">Monitoreo de Sensores y Batería</p>
                  <p className="text-[10px] text-gray-400 font-medium">Avisos si un familiar apaga su GPS, Modo Avión o le queda &lt;15% de batería.</p>
                </td>
                <td className="py-3 px-4 text-center">
                  <span className="inline-block text-[10px] bg-gray-100 text-gray-450 px-2 py-0.5 rounded-full font-medium">
                    No disponible
                  </span>
                </td>
                <td className="py-3 px-4 text-center bg-indigo-50/15 border-x border-gray-100">
                  <span className="inline-block text-[10px] bg-emerald-100 text-emerald-800 px-2.5 py-0.5 rounded-full font-black">
                    Sincronización en vivo de sensores
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      {/* Pasarelas de Pago */}
      <div className="bg-white p-6 md:p-8 rounded-3xl border border-gray-150 shadow-sm space-y-6">
        <div>
          <h3 className="text-sm font-black text-gray-900 font-sans">1. Elige tu Método de Pago Preferido</h3>
          <p className="text-[11px] text-gray-500 font-medium font-sans">La membresía se debitará automáticamente cada mes. Puedes cancelar cuando quieras sin permanencia.</p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
          {/* Opción Stripe (Tarjeta) */}
          <button
            type="button"
            onClick={() => setSelectedProvider('stripe')}
            className={`p-4 rounded-2xl border-2 text-left transition-all cursor-pointer ${
              selectedProvider === 'stripe'
                ? 'border-indigo-600 bg-indigo-50/20 shadow-sm'
                : 'border-gray-100 hover:border-gray-200 bg-white'
            }`}
          >
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-gray-800 font-sans">Tarjeta de Crédito</span>
              <span className="text-lg">💳</span>
            </div>
            <p className="text-[9px] text-gray-400 mt-1.5 font-medium font-sans leading-relaxed">Conexión internacional y pasarela segura integrada.</p>
          </button>

          {/* Opción Mercado Pago */}
          <button
            type="button"
            onClick={() => setSelectedProvider('mercadopago')}
            className={`p-4 rounded-2xl border-2 text-left transition-all cursor-pointer ${
              selectedProvider === 'mercadopago'
                ? 'border-blue-600 bg-blue-50/20 shadow-sm'
                : 'border-gray-100 hover:border-gray-200 bg-white'
            }`}
          >
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-gray-800 font-sans">Mercado Pago</span>
              <span className="text-lg">💙</span>
            </div>
            <p className="text-[9px] text-gray-400 mt-1.5 font-medium font-sans leading-relaxed">Suscripción mensual en pesos (ARS) para Argentina.</p>
          </button>

          {/* Opción PayPal */}
          <button
            type="button"
            onClick={() => setSelectedProvider('paypal')}
            className={`p-4 rounded-2xl border-2 text-left transition-all cursor-pointer ${
              selectedProvider === 'paypal'
                ? 'border-yellow-600 bg-yellow-50/20 shadow-sm'
                : 'border-gray-100 hover:border-gray-200 bg-white'
            }`}
          >
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-gray-800 font-sans">PayPal</span>
              <span className="text-lg">💛</span>
            </div>
            <p className="text-[9px] text-gray-400 mt-1.5 font-medium font-sans leading-relaxed">Débito automático en dólares (USD) para el resto del mundo.</p>
          </button>
        </div>

        {/* Panel Activo según Selección */}
        <div className="p-5 bg-gray-50 rounded-2xl border border-gray-150">
          {selectedProvider === 'stripe' ? (
            <Elements stripe={stripePromise}>
              <StripeCheckoutForm userData={userData} showToast={showToast} />
            </Elements>
          ) : selectedProvider === 'mercadopago' ? (
            <div className="space-y-4">
              <div className="flex items-center gap-2">
                <span className="text-xs font-bold text-gray-800 font-sans">Suscripción Mensual vía Mercado Pago</span>
                <span className="text-[10px] bg-blue-100 text-blue-800 px-2 py-0.5 rounded font-extrabold uppercase font-sans">Débito ARS</span>
              </div>
              <p className="text-[11px] text-gray-500 font-medium font-sans leading-relaxed">
                Al continuar serás redirigido a la pasarela segura de Mercado Pago para suscribirte a nuestro plan mensual mediante tarjeta de crédito, débito o saldo virtual.
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
              <p className="text-[11px] text-gray-500 font-medium font-sans leading-relaxed">
                Serás redirigido a la pasarela de PayPal para programar tu cargo automático mensual recurrente.
              </p>
              <button
                type="button"
                disabled={loading}
                onClick={() => handleCheckout('paypal')}
                className="w-full py-3 bg-yellow-600 hover:bg-yellow-750 disabled:bg-gray-300 text-white rounded-xl text-xs font-bold transition-all flex items-center justify-center gap-2 shadow-sm cursor-pointer font-sans"
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

interface StripeCheckoutFormProps {
  userData: UserData | null;
  showToast: (message: string, type: 'success' | 'error') => void;
}

function StripeCheckoutForm({ userData, showToast }: StripeCheckoutFormProps) {
  const stripe = useStripe();
  const elements = useElements();
  const [loading, setLoading] = useState(false);
  const [cardBrand, setCardBrand] = useState<string>('unknown');
  const [errors, setErrors] = useState<{
    number: string | null;
    expiry: string | null;
    cvc: string | null;
  }>({
    number: null,
    expiry: null,
    cvc: null,
  });

  const handleCardChange = (field: 'number' | 'expiry' | 'cvc', event: { error?: { message: string } | null; brand?: string }) => {
    setErrors(prev => ({
      ...prev,
      [field]: event.error ? event.error.message : null,
    }));
    if (field === 'number') {
      setCardBrand(event.brand || 'unknown');
    }
  };

  const getBrandBadge = (brand: string) => {
    switch (brand) {
      case 'visa':
        return <span className="text-[9px] bg-blue-100 text-blue-800 font-extrabold uppercase px-2 py-0.5 rounded tracking-wider">Visa</span>;
      case 'mastercard':
        return <span className="text-[9px] bg-red-100 text-red-800 font-extrabold uppercase px-2 py-0.5 rounded tracking-wider">Mastercard</span>;
      case 'amex':
        return <span className="text-[9px] bg-emerald-100 text-emerald-800 font-extrabold uppercase px-2 py-0.5 rounded tracking-wider">Amex</span>;
      case 'discover':
        return <span className="text-[9px] bg-orange-100 text-orange-850 font-extrabold uppercase px-2 py-0.5 rounded tracking-wider">Discover</span>;
      case 'diners':
        return <span className="text-[9px] bg-indigo-100 text-indigo-850 font-extrabold uppercase px-2 py-0.5 rounded tracking-wider">Diners</span>;
      case 'jcb':
        return <span className="text-[9px] bg-violet-100 text-violet-850 font-extrabold uppercase px-2 py-0.5 rounded tracking-wider">JCB</span>;
      case 'unionpay':
        return <span className="text-[9px] bg-rose-100 text-rose-850 font-extrabold uppercase px-2 py-0.5 rounded tracking-wider">UnionPay</span>;
      default:
        return <span className="text-[9px] bg-gray-250 text-gray-500 px-2 py-0.5 rounded font-medium uppercase">Tarjeta</span>;
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!stripe || !elements) {
      showToast('Stripe aún no se ha cargado. Reintenta en unos instantes.', 'error');
      return;
    }

    const numberElement = elements.getElement(CardNumberElement);
    if (!numberElement) return;

    if (errors.number || errors.expiry || errors.cvc) {
      showToast('Por favor, corrige los errores de tarjeta antes de continuar.', 'error');
      return;
    }

    setLoading(true);

    try {
      const { error, paymentMethod } = await stripe.createPaymentMethod({
        type: 'card',
        card: numberElement,
        billing_details: {
          name: userData?.name || 'Cliente Estoy Ok',
          email: userData?.email || '',
        },
      });

      if (error) {
        showToast(error.message || 'Error al validar la tarjeta.', 'error');
        setLoading(false);
        return;
      }

      showToast('🔒 Tarjeta tokenizada con éxito.', 'success');
      console.log('Stripe Payment Method token:', paymentMethod.id);

      // Simular proceso del backend (se conectará al endpoint en el Issue 4)
      await new Promise(resolve => setTimeout(resolve, 2000));
      showToast('Suscripción mensual activada de forma segura (Simulado).', 'success');
      alert(`Token generado: ${paymentMethod.id}\n(Este token se transmitirá a Laravel en el Issue 4 para activar el cobro mensual automático).`);

    } catch (err) {
      showToast('Error al procesar el método de pago.', 'error');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const ELEMENT_OPTIONS = {
    style: {
      base: {
        fontSize: '14px',
        color: '#1f2937',
        fontFamily: 'Inter, system-ui, sans-serif',
        '::placeholder': {
          color: '#9ca3af',
        },
      },
      invalid: {
        color: '#ef4444',
        iconColor: '#ef4444',
      },
    },
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="flex items-center justify-between border-b border-gray-150 pb-2">
        <div className="flex items-center gap-2">
          <span className="text-xs font-bold text-gray-800 font-sans">Pago Directo con Tarjeta</span>
          <span className="text-[9px] bg-indigo-150 text-indigo-800 px-2 py-0.5 rounded font-extrabold uppercase font-sans">Elements</span>
        </div>
        {getBrandBadge(cardBrand)}
      </div>

      <p className="text-[11px] text-gray-500 font-medium font-sans leading-relaxed">
        Ingresa los datos de tu tarjeta de crédito o débito. La carga se realiza directamente en los servidores de Stripe bajo la norma <strong>PCI-DSS Compliance</strong>.
      </p>

      {/* Grid de Inputs Seguros */}
      <div className="space-y-3">
        {/* Número de Tarjeta */}
        <div className="border border-gray-200 rounded-xl px-4 py-3 bg-white focus-within:border-indigo-500 focus-within:ring-1 focus-within:ring-indigo-500 transition-all shadow-xs relative">
          <label className="text-[10px] font-bold text-gray-400 uppercase block mb-1">Número de Tarjeta</label>
          <div className="h-5">
            <CardNumberElement 
              options={ELEMENT_OPTIONS} 
              onChange={(e) => handleCardChange('number', e)}
            />
          </div>
          {errors.number && (
            <p className="text-[9px] text-red-500 font-bold mt-1">{errors.number}</p>
          )}
        </div>

        {/* Expiración y CVC en una sola fila */}
        <div className="grid grid-cols-2 gap-3">
          {/* Fecha Expiración */}
          <div className="border border-gray-200 rounded-xl px-4 py-3 bg-white focus-within:border-indigo-500 focus-within:ring-1 focus-within:ring-indigo-500 transition-all shadow-xs">
            <label className="text-[10px] font-bold text-gray-400 uppercase block mb-1">Vencimiento</label>
            <div className="h-5">
              <CardExpiryElement 
                options={ELEMENT_OPTIONS} 
                onChange={(e) => handleCardChange('expiry', e)}
              />
            </div>
            {errors.expiry && (
              <p className="text-[9px] text-red-500 font-bold mt-1">{errors.expiry}</p>
            )}
          </div>

          {/* CVC */}
          <div className="border border-gray-200 rounded-xl px-4 py-3 bg-white focus-within:border-indigo-500 focus-within:ring-1 focus-within:ring-indigo-500 transition-all shadow-xs">
            <label className="text-[10px] font-bold text-gray-400 uppercase block mb-1">CVC / CVV</label>
            <div className="h-5">
              <CardCvcElement 
                options={ELEMENT_OPTIONS} 
                onChange={(e) => handleCardChange('cvc', e)}
              />
            </div>
            {errors.cvc && (
              <p className="text-[9px] text-red-500 font-bold mt-1">{errors.cvc}</p>
            )}
          </div>
        </div>
      </div>

      <button
        type="submit"
        disabled={loading || !stripe}
        className="w-full py-3 bg-indigo-600 hover:bg-indigo-750 disabled:bg-gray-300 text-white rounded-xl text-xs font-bold transition-all flex items-center justify-center gap-2 shadow-sm cursor-pointer font-sans"
      >
        {loading ? 'Procesando Tarjeta...' : '🔒 Suscribirse con Tarjeta Segura (Elements)'}
      </button>

      <div className="text-center text-[9px] text-gray-400 font-medium">
        🛡️ Tus datos bancarios se transmiten de forma encriptada AES-256 directamente a Stripe.
      </div>
    </form>
  );
}
