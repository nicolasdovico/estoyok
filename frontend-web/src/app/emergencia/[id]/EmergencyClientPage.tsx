'use client';

import { useState, useEffect } from 'react';
import dynamic from 'next/dynamic';

const EmergencyMap = dynamic(() => import('@/components/EmergencyMap'), { 
  ssr: false,
  loading: () => <div className="h-full w-full bg-gray-100 animate-pulse flex items-center justify-center">Cargando mapa...</div>
});

interface EmergencyData {
  user_name: string;
  status: string;
  type: string;
  last_check_in_at: string;
  location: {
    latitude: number;
    longitude: number;
    updated_at: string;
  } | null;
}

export default function EmergencyClientPage({ id }: { id: string }) {
  const [data, setData] = useState<EmergencyData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/emergency-alerts/${id}`);
        if (!res.ok) throw new Error();
        const json = await res.json();
        setData(json);
      } catch (err) {
        setError(true);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [id]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-red-600"></div>
      </div>
    );
  }

  if (error || !data) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 p-4">
        <div className="max-w-md w-full bg-white rounded-2xl shadow-xl p-8 text-center border-t-4 border-red-500">
          <h1 className="text-2xl font-bold text-gray-900 mb-4">Alerta No Encontrada</h1>
          <p className="text-gray-600 mb-6">El enlace de emergencia puede haber expirado o es inválido.</p>
        </div>
      </div>
    );
  }

  // Formateo de fechas en el cliente para respetar la zona horaria del navegador
  const formatDateTime = (dateStr: string) => {
    try {
      const date = new Date(dateStr);
      return date.toLocaleString();
    } catch (e) {
      return 'Fecha no disponible';
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <header className={`${data.status === 'resolved' ? 'bg-emerald-600' : 'bg-red-600'} text-white p-4 shadow-lg sticky top-0 z-50`}>
        <div className="max-w-4xl mx-auto flex items-center justify-between">
          <div>
            <h1 className="text-xl font-bold">
              {data.status === 'resolved' ? 'ESTADO ACTUALIZADO' : 'ALERTA DE SEGURIDAD'}
            </h1>
            <p className="text-sm opacity-90">
              {data.status === 'resolved' ? 'El usuario se encuentra a salvo' : 'Protocolo de Emergencia Activado'}
            </p>
          </div>
          <div className={`bg-white ${data.status === 'resolved' ? 'text-emerald-600' : 'text-red-600'} px-3 py-1 rounded-full text-xs font-bold ${data.status === 'resolved' ? '' : 'animate-pulse'}`}>
            {data.status === 'resolved' ? 'RESUELTA' : 'VIVO'}
          </div>
        </div>
      </header>

      <main className="flex-1 max-w-4xl w-full mx-auto p-4 md:p-6 space-y-6">
        {data.status === 'resolved' && (
          <div className="bg-emerald-50 border border-emerald-100 p-6 rounded-2xl flex items-start gap-4 shadow-sm animate-fade-in">
            <div className="p-3 bg-emerald-500 text-white rounded-xl text-xl">
              🛡️
            </div>
            <div>
              <h3 className="text-emerald-950 font-black text-lg">¡Buenas noticias!</h3>
              <p className="text-emerald-700 text-sm mt-1 leading-relaxed">
                El usuario ya se ha reportado y se encuentra bien. Esta alerta ha sido resuelta.
              </p>
            </div>
          </div>
        )}

        <section className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <div>
              <h2 className="text-3xl font-extrabold text-gray-900">{data.user_name}</h2>
              <p className="text-gray-500 mt-1">Estado: <span className={`font-semibold uppercase ${data.status === 'resolved' ? 'text-emerald-600' : 'text-red-600'}`}>{data.status === 'resolved' ? 'Resuelta' : data.status === 'active' ? 'Activa' : data.status}</span></p>
            </div>
          </div>
          <hr className="my-6 border-gray-100" />
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 text-sm">
            <div className="bg-gray-50 p-4 rounded-xl">
              <p className="text-gray-500 mb-1">Último Reporte Voluntario</p>
              <p className="text-lg font-bold text-gray-800">{formatDateTime(data.last_check_in_at)}</p>
            </div>
            <div className="bg-gray-50 p-4 rounded-xl">
              <p className="text-gray-500 mb-1">Última Ubicación GPS</p>
              <p className="text-lg font-bold text-gray-800">{data.location ? formatDateTime(data.location.updated_at) : 'No disponible'}</p>
            </div>
          </div>
        </section>

        <section className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden flex flex-col">
          <div className="p-4 border-b border-gray-100 flex items-center justify-between">
            <h3 className="font-bold text-gray-800 flex items-center gap-2">📍 Mapa de Rastreo</h3>
          </div>
          <div className="h-[400px] w-full relative">
            {data.status === 'resolved' ? (
              <div className="absolute inset-0 bg-gray-50 flex flex-col items-center justify-center p-8 text-center">
                <div className="text-5xl mb-4">🔒</div>
                <h4 className="font-extrabold text-gray-900 text-lg">Ubicación Privada</h4>
                <p className="text-xs text-gray-500 mt-2 max-w-md leading-relaxed">
                  El mapa de rastreo se ha desactivado por razones de privacidad debido a que el usuario ya se reportó y la alerta de emergencia ha sido resuelta.
                </p>
              </div>
            ) : data.location ? (
              <EmergencyMap center={[data.location.latitude, data.location.longitude]} />
            ) : (
              <div className="absolute inset-0 bg-gray-100 flex items-center justify-center text-gray-500">Ubicación no disponible</div>
            )}
          </div>
        </section>
      </main>
    </div>
  );
}
