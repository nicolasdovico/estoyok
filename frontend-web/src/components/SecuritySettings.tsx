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
}

export default function SecuritySettings({
  initialInterval,
  initialQuietHoursEnabled,
  initialQuietHoursStart,
  initialQuietHoursEnd,
  initialAllowSmsWhatsappCheckin,
  initialEscalationEnabled,
  initialEscalationIntervalMinutes,
}: SecuritySettingsProps) {
  const [interval, setIntervalValue] = useState(initialInterval);
  const [quietHoursEnabled, setQuietHoursEnabled] = useState(initialQuietHoursEnabled);
  const [quietHoursStart, setQuietHoursStart] = useState(initialQuietHoursStart ? initialQuietHoursStart.substring(0, 5) : '23:00');
  const [quietHoursEnd, setQuietHoursEnd] = useState(initialQuietHoursEnd ? initialQuietHoursEnd.substring(0, 5) : '07:00');
  const [allowSmsWhatsappCheckin, setAllowSmsWhatsappCheckin] = useState(initialAllowSmsWhatsappCheckin);
  const [escalationEnabled, setEscalationEnabled] = useState(initialEscalationEnabled);
  const [escalationIntervalMinutes, setEscalationIntervalMinutes] = useState(initialEscalationIntervalMinutes);
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

  return (
    <div className="bg-white rounded-3xl shadow-sm border border-gray-100 p-8 flex flex-col justify-between">
      <div>
        <h3 className="text-xl font-black text-gray-900 mb-2">Configuración de Seguridad</h3>
        <p className="text-xs text-gray-500 mb-6">Define los parámetros de protección pasiva del sistema.</p>
        
        <div className="space-y-4">
          <label className="block text-xs font-bold text-gray-500 uppercase tracking-widest ml-1">Intervalo de &quot;Estoy Ok&quot;</label>
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
              <label className="block text-xs font-bold text-gray-500 uppercase tracking-widest ml-1">Modo Sueño (Horas Silenciosas)</label>
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
              <label className="block text-xs font-bold text-gray-500 uppercase tracking-widest ml-1">Check-in por SMS / WhatsApp</label>
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
              <label className="block text-xs font-bold text-gray-500 uppercase tracking-widest ml-1">Notificación Escalonada</label>
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
