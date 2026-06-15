'use client';

import { useState } from 'react';

const INTERVAL_OPTIONS = [
  { label: '6 Horas', value: 6 },
  { label: '12 Horas', value: 12 },
  { label: '24 Horas', value: 24 },
  { label: '48 Horas', value: 48 },
  { label: '1 Semana', value: 168 },
];

export default function SecuritySettings({ initialInterval }: { initialInterval: number }) {
  const [interval, setIntervalValue] = useState(initialInterval);
  const [isUpdating, setIsUpdating] = useState(false);
  const [message, setMessage] = useState('');

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
        setMessage('Configuración actualizada correctamente');
        setTimeout(() => setMessage(''), 3000);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setIsUpdating(false);
    }
  };

  return (
    <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
      <h3 className="text-lg font-bold text-gray-900 mb-2">Configuración de Seguridad</h3>
      <p className="text-sm text-gray-500 mb-6">Define cada cuánto tiempo debes confirmar que estás bien.</p>
      
      <div className="space-y-4">
        <label className="block text-xs font-bold text-gray-500 uppercase tracking-widest ml-1">Intervalo de &quot;Estoy Ok&quot;</label>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
          {INTERVAL_OPTIONS.map((option) => (
            <button
              key={option.value}
              onClick={() => handleUpdate(option.value)}
              disabled={isUpdating}
              className={`
                px-5 py-4 rounded-2xl text-sm font-bold transition-all
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

      {message && (
        <p className="mt-4 text-sm font-bold text-green-600 bg-green-50 p-3 rounded-xl text-center animate-pulse">
          {message}
        </p>
      )}

      <div className="mt-8 p-4 bg-blue-50 rounded-2xl border border-blue-100">
        <p className="text-xs text-blue-800 leading-relaxed">
          <strong>Importante:</strong> Si pasan más de {interval} horas sin que presiones el botón &quot;Estoy Ok&quot;, el sistema notificará automáticamente a tus contactos de emergencia con tu última ubicación.
        </p>
      </div>
    </div>
  );
}
