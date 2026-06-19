'use client';

import { useState, useEffect } from 'react';

const INTERVAL_OPTIONS = [
  { label: '6 Horas', value: 6 },
  { label: '12 Horas', value: 12 },
  { label: '24 Horas', value: 24 },
  { label: '48 Horas', value: 48 },
  { label: '1 Semana', value: 168 },
];

interface SecuritySettingsProps {
  initialInterval: number;
  initialQuietHoursEnabled: boolean;
  initialQuietHoursStart: string;
  initialQuietHoursEnd: string;
  initialAllowSmsWhatsappCheckin: boolean;
  initialEscalationEnabled: boolean;
  initialEscalationIntervalMinutes: number;
  initialShareContactResponses: boolean;
  initialWifiCheckinEnabled: boolean;
  initialSafeWifiSsid: string;
  initialSensorCheckinEnabled: boolean;
}

export default function SecuritySettings({
  initialInterval,
  initialQuietHoursEnabled,
  initialQuietHoursStart,
  initialQuietHoursEnd,
  initialAllowSmsWhatsappCheckin,
  initialEscalationEnabled,
  initialEscalationIntervalMinutes,
  initialShareContactResponses,
  initialWifiCheckinEnabled,
  initialSafeWifiSsid,
  initialSensorCheckinEnabled,
}: SecuritySettingsProps) {
  const [interval, setIntervalValue] = useState(initialInterval);
  const [quietHoursEnabled, setQuietHoursEnabled] = useState(initialQuietHoursEnabled);
  const [quietHoursStart, setQuietHoursStart] = useState(initialQuietHoursStart ? initialQuietHoursStart.substring(0, 5) : '23:00');
  const [quietHoursEnd, setQuietHoursEnd] = useState(initialQuietHoursEnd ? initialQuietHoursEnd.substring(0, 5) : '07:00');
  const [allowSmsWhatsappCheckin, setAllowSmsWhatsappCheckin] = useState(initialAllowSmsWhatsappCheckin);
  const [escalationEnabled, setEscalationEnabled] = useState(initialEscalationEnabled);
  const [escalationIntervalMinutes, setEscalationIntervalMinutes] = useState(initialEscalationIntervalMinutes);
  const [shareContactResponses, setShareContactResponses] = useState(initialShareContactResponses);
  const [wifiCheckinEnabled, setWifiCheckinEnabled] = useState(initialWifiCheckinEnabled);
  const [safeWifiSsid, setSafeWifiSsid] = useState(initialSafeWifiSsid);
  const [sensorCheckinEnabled, setSensorCheckinEnabled] = useState(initialSensorCheckinEnabled);
  const [isUpdating, setIsUpdating] = useState(false);
  const [message, setMessage] = useState('');
  const [isInputFocused, setIsInputFocused] = useState(false);

  const handleFocus = (e: React.FocusEvent<HTMLInputElement>) => {
    setIsInputFocused(true);
    const target = e.target;
    setTimeout(() => {
      const rect = target.getBoundingClientRect();
      const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
      window.scrollTo({
        top: rect.top + scrollTop - 120,
        behavior: 'smooth'
      });
    }, 300);
  };

  const handleBlur = () => {
    setTimeout(() => {
      setIsInputFocused(false);
    }, 100);
  };

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setIntervalValue(initialInterval);
  }, [initialInterval]);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setQuietHoursEnabled(initialQuietHoursEnabled);
  }, [initialQuietHoursEnabled]);

  useEffect(() => {
    if (initialQuietHoursStart) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setQuietHoursStart(initialQuietHoursStart.substring(0, 5));
    }
  }, [initialQuietHoursStart]);

  useEffect(() => {
    if (initialQuietHoursEnd) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setQuietHoursEnd(initialQuietHoursEnd.substring(0, 5));
    }
  }, [initialQuietHoursEnd]);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setAllowSmsWhatsappCheckin(initialAllowSmsWhatsappCheckin);
  }, [initialAllowSmsWhatsappCheckin]);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setEscalationEnabled(initialEscalationEnabled);
  }, [initialEscalationEnabled]);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setEscalationIntervalMinutes(initialEscalationIntervalMinutes);
  }, [initialEscalationIntervalMinutes]);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setShareContactResponses(initialShareContactResponses);
  }, [initialShareContactResponses]);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setWifiCheckinEnabled(initialWifiCheckinEnabled);
  }, [initialWifiCheckinEnabled]);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setSafeWifiSsid(initialSafeWifiSsid);
  }, [initialSafeWifiSsid]);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setSensorCheckinEnabled(initialSensorCheckinEnabled);
  }, [initialSensorCheckinEnabled]);

  const handleUpdate = async (value: number) => {
    setIsUpdating(true);
    setMessage('');
    const token = localStorage.getItem('auth_token');
    
    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/settings/checkin-interval`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ checkin_interval_hours: value }),
      });

      if (response.ok) {
        setIntervalValue(value);
        setMessage('Configuración de intervalo actualizada');
        setTimeout(() => setMessage(''), 3000);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setIsUpdating(false);
    }
  };

  const handleUpdateQuietHours = async (enabled: boolean, start: string, end: string) => {
    setIsUpdating(true);
    setMessage('');
    const token = localStorage.getItem('auth_token');
    const timezone = Intl.DateTimeFormat().resolvedOptions().timeZone;

    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/settings/quiet-hours`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          quiet_hours_enabled: enabled,
          quiet_hours_start: start,
          quiet_hours_end: end,
          timezone: timezone
        }),
      });

      if (response.ok) {
        const data = await response.json();
        setQuietHoursEnabled(data.quiet_hours_enabled);
        setQuietHoursStart(data.quiet_hours_start);
        setQuietHoursEnd(data.quiet_hours_end);
        setMessage('Configuración de Modo Sueño actualizada');
        setTimeout(() => setMessage(''), 3000);
      } else {
        const errorData = await response.json();
        console.error('Validation errors:', errorData);
        setMessage('Error al actualizar Modo Sueño: ' + JSON.stringify(errorData.errors || errorData.message));
      }
    } catch (err) {
      console.error(err);
      setMessage('Error de conexión');
    } finally {
      setIsUpdating(false);
    }
  };

  const handleUpdateSmsWhatsappCheckin = async (enabled: boolean) => {
    setIsUpdating(true);
    setMessage('');
    const token = localStorage.getItem('auth_token');

    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/settings/sms-whatsapp-checkin`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          allow_sms_whatsapp_checkin: enabled,
        }),
      });

      if (response.ok) {
        setAllowSmsWhatsappCheckin(enabled);
        setMessage('Configuración de check-in por SMS/WhatsApp actualizada');
        setTimeout(() => setMessage(''), 3000);
      } else {
        const errorData = await response.json();
        console.error('Validation errors:', errorData);
        setMessage('Error al actualizar check-in: ' + JSON.stringify(errorData.errors || errorData.message));
      }
    } catch (err) {
      console.error(err);
      setMessage('Error de conexión');
    } finally {
      setIsUpdating(false);
    }
  };

  const handleUpdateEscalation = async (enabled: boolean, minutes: number) => {
    setIsUpdating(true);
    setMessage('');
    const token = localStorage.getItem('auth_token');

    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/settings/escalation`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          escalation_enabled: enabled,
          escalation_interval_minutes: minutes,
        }),
      });

      if (response.ok) {
        setEscalationEnabled(enabled);
        setEscalationIntervalMinutes(minutes);
        setMessage('Configuración de Alertas Escalonadas actualizada');
        setTimeout(() => setMessage(''), 3000);
      } else {
        const errorData = await response.json();
        console.error('Validation errors:', errorData);
        setMessage('Error al actualizar: ' + JSON.stringify(errorData.errors || errorData.message));
      }
    } catch (err) {
      console.error(err);
      setMessage('Error de conexión');
    } finally {
      setIsUpdating(false);
    }
  };

  const handleUpdatePrivacy = async (enabled: boolean) => {
    setIsUpdating(true);
    setMessage('');
    const token = localStorage.getItem('auth_token');

    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/settings/privacy`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          share_contact_responses: enabled,
        }),
      });

      if (response.ok) {
        setShareContactResponses(enabled);
        setMessage('Configuración de privacidad de respuestas actualizada');
        setTimeout(() => setMessage(''), 3000);
      } else {
        const errorData = await response.json();
        console.error('Validation errors:', errorData);
        setMessage('Error al actualizar privacidad: ' + JSON.stringify(errorData.errors || errorData.message));
      }
    } catch (err) {
      console.error(err);
      setMessage('Error de conexión');
    } finally {
      setIsUpdating(false);
    }
  };

  const handleUpdateAutomation = async (wifiEnabled: boolean, ssid: string, sensorEnabled: boolean) => {
    setIsUpdating(true);
    setMessage('');
    const token = localStorage.getItem('auth_token');

    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/settings/automation`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          wifi_checkin_enabled: wifiEnabled,
          safe_wifi_ssid: ssid || null,
          sensor_checkin_enabled: sensorEnabled,
        }),
      });

      if (response.ok) {
        setWifiCheckinEnabled(wifiEnabled);
        setSafeWifiSsid(ssid);
        setSensorCheckinEnabled(sensorEnabled);
        setMessage('Configuración de auto-check-in actualizada');
        setTimeout(() => setMessage(''), 3000);
      } else {
        const errorData = await response.json();
        console.error('Validation errors:', errorData);
        setMessage('Error al actualizar auto-check-in: ' + JSON.stringify(errorData.errors || errorData.message));
      }
    } catch (err) {
      console.error(err);
      setMessage('Error de conexión');
    } finally {
      setIsUpdating(false);
    }
  };

  return (
    <div className="bg-white rounded-3xl shadow-sm border border-gray-100 p-8 flex flex-col justify-between">
      <div>
        <h3 className="text-xl font-black text-gray-900 mb-2">Configuración de Seguridad</h3>
        <p className="text-xs text-gray-500 mb-6">Define los parámetros de protección pasiva del sistema.</p>
        
        <div className="space-y-4">
          <label className="flex items-center text-xs font-bold text-gray-500 uppercase tracking-widest ml-1">
            <span>Intervalo de &quot;Estoy Ok&quot;</span>
            <div className="relative group inline-block ml-1.5 align-middle cursor-help normal-case font-normal">
              <span className="text-[10px] text-gray-400 hover:text-gray-600 bg-gray-100 hover:bg-gray-200 w-3.5 h-3.5 rounded-full inline-flex items-center justify-center font-black">?</span>
              <div className="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 w-56 p-3 bg-gray-900 text-[10px] text-white rounded-xl shadow-xl opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 z-50 font-medium leading-relaxed normal-case">
                Frecuencia con la que debes presionar el botón de bienestar. Si pasa este plazo sin reportarte, alertaremos a tus contactos.
                <div className="absolute top-full left-1/2 -translate-x-1/2 w-2 h-2 bg-gray-900 rotate-45 -mt-1"></div>
              </div>
            </div>
          </label>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
            {INTERVAL_OPTIONS.map((option) => (
              <button
                key={option.value}
                onClick={() => handleUpdate(option.value)}
                disabled={isUpdating}
                className={`
                  px-5 py-4 rounded-2xl text-sm font-bold transition-all cursor-pointer
                  ${interval === option.value 
                    ? 'bg-red-600 text-white shadow-lg shadow-red-100' 
                    : 'bg-gray-50 text-gray-600 hover:bg-gray-100'}
                `}
              >
                {option.label}
                {interval === option.value && <span className="ml-2">✓</span>}
              </button>
            ))}
          </div>
        </div>

        <hr className="my-6 border-gray-100" />

        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <label className="flex items-center text-xs font-bold text-gray-500 uppercase tracking-widest ml-1">
                <span>Modo Sueño (Horas Silenciosas)</span>
                <div className="relative group inline-block ml-1.5 align-middle cursor-help normal-case font-normal">
                  <span className="text-[10px] text-gray-400 hover:text-gray-600 bg-gray-100 hover:bg-gray-200 w-3.5 h-3.5 rounded-full inline-flex items-center justify-center font-black">?</span>
                  <div className="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 w-56 p-3 bg-gray-900 text-[10px] text-white rounded-xl shadow-xl opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 z-50 font-medium leading-relaxed normal-case">
                    Evita el envío de alarmas y recordatorios durante el horario definido (ej. mientras duermes) para no generar falsas alertas.
                    <div className="absolute top-full left-1/2 -translate-x-1/2 w-2 h-2 bg-gray-900 rotate-45 -mt-1"></div>
                  </div>
                </div>
              </label>
              <p className="text-[11px] text-gray-400 mt-1 ml-1 leading-relaxed">
                Pausa las alertas mientras duermes o realizas otras actividades.
              </p>
            </div>
            <button
              onClick={() => handleUpdateQuietHours(!quietHoursEnabled, quietHoursStart, quietHoursEnd)}
              disabled={isUpdating}
              className={`
                w-12 h-6 rounded-full p-0.5 transition-all duration-200 cursor-pointer flex items-center flex-shrink-0
                ${quietHoursEnabled ? 'bg-red-600 justify-end' : 'bg-gray-200 justify-start'}
              `}
            >
              <div className="w-5 h-5 rounded-full bg-white shadow-sm" />
            </button>
          </div>

          {quietHoursEnabled && (
            <div className="grid grid-cols-2 gap-4 pt-2">
              <div>
                <label className="block text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-1.5 ml-1">Hora Inicio</label>
                <input
                  type="time"
                  value={quietHoursStart}
                  onChange={(e) => {
                    setQuietHoursStart(e.target.value);
                    handleUpdateQuietHours(quietHoursEnabled, e.target.value, quietHoursEnd);
                  }}
                  onFocus={handleFocus}
                  onBlur={handleBlur}
                  disabled={isUpdating}
                  style={{ scrollMarginBottom: '250px', scrollMarginTop: '150px' }}
                  className="w-full bg-gray-50 text-gray-800 text-sm font-bold p-3.5 rounded-2xl border border-transparent focus:border-red-100 focus:bg-white focus:outline-none transition-all"
                />
              </div>
              <div>
                <label className="block text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-1.5 ml-1">Hora Fin</label>
                <input
                  type="time"
                  value={quietHoursEnd}
                  onChange={(e) => {
                    setQuietHoursEnd(e.target.value);
                    handleUpdateQuietHours(quietHoursEnabled, quietHoursStart, e.target.value);
                  }}
                  onFocus={handleFocus}
                  onBlur={handleBlur}
                  disabled={isUpdating}
                  style={{ scrollMarginBottom: '250px', scrollMarginTop: '150px' }}
                  className="w-full bg-gray-50 text-gray-800 text-sm font-bold p-3.5 rounded-2xl border border-transparent focus:border-red-100 focus:bg-white focus:outline-none transition-all"
                />
              </div>
            </div>
          )}
        </div>

        <hr className="my-6 border-gray-100" />

        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <label className="flex items-center text-xs font-bold text-gray-500 uppercase tracking-widest ml-1">
                <span>Check-in por SMS / WhatsApp</span>
                <div className="relative group inline-block ml-1.5 align-middle cursor-help normal-case font-normal">
                  <span className="text-[10px] text-gray-400 hover:text-gray-600 bg-gray-100 hover:bg-gray-200 w-3.5 h-3.5 rounded-full inline-flex items-center justify-center font-black">?</span>
                  <div className="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 w-56 p-3 bg-gray-900 text-[10px] text-white rounded-xl shadow-xl opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 z-50 font-medium leading-relaxed normal-case">
                    Permite contestar el mensaje automático con palabras como &quot;OK&quot; o &quot;1&quot; para hacer tu check-in sin abrir la web o la app móvil.
                    <div className="absolute top-full left-1/2 -translate-x-1/2 w-2 h-2 bg-gray-900 rotate-45 -mt-1"></div>
                  </div>
                </div>
              </label>
              <p className="text-[11px] text-gray-400 mt-1 ml-1 leading-relaxed">
                Permite responder &quot;OK&quot;, &quot;1&quot; o &quot;BIEN&quot; directamente al mensaje preventivo para confirmar tu bienestar.
              </p>
            </div>
            <button
              onClick={() => handleUpdateSmsWhatsappCheckin(!allowSmsWhatsappCheckin)}
              disabled={isUpdating}
              className={`
                w-12 h-6 rounded-full p-0.5 transition-all duration-200 cursor-pointer flex items-center flex-shrink-0
                ${allowSmsWhatsappCheckin ? 'bg-red-600 justify-end' : 'bg-gray-200 justify-start'}
              `}
            >
              <div className="w-5 h-5 rounded-full bg-white shadow-sm" />
            </button>
          </div>
        </div>

        <hr className="my-6 border-gray-100" />

        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <label className="flex items-center text-xs font-bold text-gray-500 uppercase tracking-widest ml-1">
                <span>Notificación Escalonada</span>
                <div className="relative group inline-block ml-1.5 align-middle cursor-help normal-case font-normal">
                  <span className="text-[10px] text-gray-400 hover:text-gray-600 bg-gray-100 hover:bg-gray-200 w-3.5 h-3.5 rounded-full inline-flex items-center justify-center font-black">?</span>
                  <div className="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 w-56 p-3 bg-gray-900 text-[10px] text-white rounded-xl shadow-xl opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 z-50 font-medium leading-relaxed normal-case">
                    Alertará a tus contactos uno por uno con un margen de tiempo en lugar de a todos a la vez, permitiendo resolver alertas antes de asustar a todo el grupo.
                    <div className="absolute top-full left-1/2 -translate-x-1/2 w-2 h-2 bg-gray-900 rotate-45 -mt-1"></div>
                  </div>
                </div>
              </label>
              <p className="text-[11px] text-gray-400 mt-1 ml-1 leading-relaxed">
                Notifica secuencialmente a tus contactos con un intervalo de retraso en lugar de alertar a todos a la vez.
              </p>
            </div>
            <button
              onClick={() => handleUpdateEscalation(!escalationEnabled, escalationIntervalMinutes)}
              disabled={isUpdating}
              className={`
                w-12 h-6 rounded-full p-0.5 transition-all duration-200 cursor-pointer flex items-center flex-shrink-0
                ${escalationEnabled ? 'bg-red-600 justify-end' : 'bg-gray-200 justify-start'}
              `}
            >
              <div className="w-5 h-5 rounded-full bg-white shadow-sm" />
            </button>
          </div>

          {escalationEnabled && (
            <div className="pt-2">
              <label className="block text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-1.5 ml-1">Intervalo de escalado (minutos)</label>
              <input
                type="number"
                min="1"
                max="1440"
                value={escalationIntervalMinutes}
                onChange={(e) => {
                  const val = parseInt(e.target.value, 10) || 15;
                  setEscalationIntervalMinutes(val);
                  handleUpdateEscalation(escalationEnabled, val);
                }}
                onFocus={handleFocus}
                onBlur={handleBlur}
                disabled={isUpdating}
                style={{ scrollMarginBottom: '250px', scrollMarginTop: '150px' }}
                className="w-full bg-gray-50 text-gray-800 text-sm font-bold p-3.5 rounded-2xl border border-transparent focus:border-red-100 focus:bg-white focus:outline-none transition-all"
              />
            </div>
          )}
        </div>

        <hr className="my-6 border-gray-100" />

        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <label className="block text-xs font-bold text-gray-500 uppercase tracking-widest ml-1">Compartir respuestas de apoyo</label>
              <p className="text-[11px] text-gray-400 mt-1 ml-1 leading-relaxed">
                Permite que otros contactos de emergencia vean quiénes ya han leído o van en camino en la pantalla pública.
              </p>
            </div>
            <button
              onClick={() => handleUpdatePrivacy(!shareContactResponses)}
              disabled={isUpdating}
              className={`
                w-12 h-6 rounded-full p-0.5 transition-all duration-200 cursor-pointer flex items-center flex-shrink-0
                ${shareContactResponses ? 'bg-red-600 justify-end' : 'bg-gray-200 justify-start'}
              `}
            >
              <div className="w-5 h-5 rounded-full bg-white shadow-sm" />
            </button>
          </div>
        </div>

        <hr className="my-6 border-gray-100" />

        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <label className="flex items-center text-xs font-bold text-gray-500 uppercase tracking-widest ml-1">
                <span>Auto-check-in por Wi-Fi</span>
                <div className="relative group inline-block ml-1.5 align-middle cursor-help normal-case font-normal">
                  <span className="text-[10px] text-gray-400 hover:text-gray-600 bg-gray-100 hover:bg-gray-200 w-3.5 h-3.5 rounded-full inline-flex items-center justify-center font-black">?</span>
                  <div className="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 w-56 p-3 bg-gray-900 text-[10px] text-white rounded-xl shadow-xl opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 z-50 font-medium leading-relaxed normal-case">
                    El celular hará check-in automático y silencioso apenas se conecte a la red Wi-Fi segura configurada (ej. el Wi-Fi de tu hogar).
                    <div className="absolute top-full left-1/2 -translate-x-1/2 w-2 h-2 bg-gray-900 rotate-45 -mt-1"></div>
                  </div>
                </div>
              </label>
              <p className="text-[11px] text-gray-400 mt-1 ml-1 leading-relaxed">
                Confirma tu bienestar automáticamente cuando te conectas al Wi-Fi de tu casa.
              </p>
            </div>
            <button
              onClick={() => handleUpdateAutomation(!wifiCheckinEnabled, safeWifiSsid, sensorCheckinEnabled)}
              disabled={isUpdating}
              className={`
                w-12 h-6 rounded-full p-0.5 transition-all duration-200 cursor-pointer flex items-center flex-shrink-0
                ${wifiCheckinEnabled ? 'bg-red-600 justify-end' : 'bg-gray-200 justify-start'}
              `}
            >
              <div className="w-5 h-5 rounded-full bg-white shadow-sm" />
            </button>
          </div>

          {wifiCheckinEnabled && (
            <div className="pt-2">
              <label className="block text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-1.5 ml-1">SSID de Wi-Fi Seguro</label>
              <input
                type="text"
                placeholder="Ej: MiWifiDeCasa"
                value={safeWifiSsid}
                onChange={(e) => {
                  setSafeWifiSsid(e.target.value);
                }}
                onBlur={() => {
                  handleBlur();
                  handleUpdateAutomation(wifiCheckinEnabled, safeWifiSsid, sensorCheckinEnabled);
                }}
                onFocus={handleFocus}
                disabled={isUpdating}
                style={{ scrollMarginBottom: '250px', scrollMarginTop: '150px' }}
                className="w-full bg-gray-50 text-gray-800 text-sm font-bold p-3.5 rounded-2xl border border-transparent focus:border-red-100 focus:bg-white focus:outline-none transition-all"
              />
            </div>
          )}
        </div>

        <hr className="my-6 border-gray-100" />

        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <label className="flex items-center text-xs font-bold text-gray-500 uppercase tracking-widest ml-1">
                <span>Auto-check-in por Actividad Física</span>
                <div className="relative group inline-block ml-1.5 align-middle cursor-help normal-case font-normal">
                  <span className="text-[10px] text-gray-400 hover:text-gray-600 bg-gray-100 hover:bg-gray-200 w-3.5 h-3.5 rounded-full inline-flex items-center justify-center font-black">?</span>
                  <div className="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 w-56 p-3 bg-gray-900 text-[10px] text-white rounded-xl shadow-xl opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 z-50 font-medium leading-relaxed normal-case">
                    La app móvil monitoreará tus pasos; si detecta más de 100 pasos en una hora, confirmará tu bienestar de forma automática.
                    <div className="absolute top-full left-1/2 -translate-x-1/2 w-2 h-2 bg-gray-900 rotate-45 -mt-1"></div>
                  </div>
                </div>
              </label>
              <p className="text-[11px] text-gray-400 mt-1 ml-1 leading-relaxed">
                Confirma tu bienestar automáticamente cuando detectamos más de 100 pasos en tu dispositivo.
              </p>
            </div>
            <button
              onClick={() => handleUpdateAutomation(wifiCheckinEnabled, safeWifiSsid, !sensorCheckinEnabled)}
              disabled={isUpdating}
              className={`
                w-12 h-6 rounded-full p-0.5 transition-all duration-200 cursor-pointer flex items-center flex-shrink-0
                ${sensorCheckinEnabled ? 'bg-red-600 justify-end' : 'bg-gray-200 justify-start'}
              `}
            >
              <div className="w-5 h-5 rounded-full bg-white shadow-sm" />
            </button>
          </div>
        </div>
      </div>

      <div>
        {message && (
          <p className="mt-6 text-xs font-bold text-emerald-800 bg-emerald-50 border border-emerald-100 p-3.5 rounded-2xl text-center">
            {message}
          </p>
        )}

        <div className="mt-6 p-4 bg-blue-50/50 rounded-2xl border border-blue-100/50">
          <p className="text-[11px] text-blue-800 leading-relaxed">
            <strong>Importante:</strong> Si pasan más de {interval} horas sin que confirmes tu bienestar (y no estás en tu Modo Sueño), notificaremos automáticamente a tus contactos con tu última ubicación conocida.
          </p>
        </div>
      </div>
      {isInputFocused && <div className="h-[350px] md:hidden" />}
    </div>
  );
}
