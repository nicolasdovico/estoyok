'use client';

import { useState, useEffect } from 'react';
import dynamic from 'next/dynamic';

const EmergencyMap = dynamic(() => import('@/components/EmergencyMap'), { 
  ssr: false,
  loading: () => <div className="h-full w-full bg-gray-100 animate-pulse flex items-center justify-center">Cargando mapa...</div>
});

interface ResponseData {
  id: number;
  contact_name: string;
  status: string;
  created_at: string;
}

interface EmergencyData {
  user_name: string;
  status: string;
  type: string;
  last_check_in_at: string;
  share_contact_responses: boolean;
  responses: ResponseData[];
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
  const [contactName, setContactName] = useState(() => (typeof window !== 'undefined' ? localStorage.getItem('contact_name') || '' : ''));
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [submitSuccess, setSubmitSuccess] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/emergency-alerts/${id}`);
        if (!res.ok) throw new Error();
        const json = await res.json();
        setData(json);
      } catch {
        setError(true);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [id]);

  useEffect(() => {
    if (!data || data.status === 'resolved') return;

    const interval = setInterval(async () => {
      try {
        const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/emergency-alerts/${id}`);
        if (res.ok) {
          const json = await res.json();
          setData(json);
        }
      } catch (err) {
        console.error('Error polling emergency status:', err);
      }
    }, 10000);

    return () => clearInterval(interval);
  }, [id, data]);

  const handleRespond = async (status: 'read' | 'acknowledged' | 'on_my_way') => {
    if (!contactName.trim()) return;
    setIsSubmitting(true);
    setSubmitError(null);
    setSubmitSuccess(false);

    try {
      const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/emergency-alerts/${id}/respond`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          contact_name: contactName,
          status,
        }),
      });

      if (!res.ok) {
        throw new Error('No se pudo registrar la respuesta.');
      }

      if (typeof window !== 'undefined') {
        localStorage.setItem('contact_name', contactName);
      }
      setSubmitSuccess(true);
      setTimeout(() => setSubmitSuccess(false), 4000);

      const updatedRes = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/emergency-alerts/${id}`);
      if (updatedRes.ok) {
        const json = await updatedRes.json();
        setData(json);
      }
    } catch (err) {
      const errMsg = err instanceof Error ? err.message : 'Error de conexión';
      setSubmitError(errMsg);
    } finally {
      setIsSubmitting(false);
    }
  };

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
    } catch {
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

        {data.status !== 'resolved' && (
          <section className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 space-y-6">
            <div>
              <h3 className="text-lg font-black text-gray-900 flex items-center gap-2">🤝 Coordinar Ayuda</h3>
              <p className="text-xs text-gray-500 mt-1">
                Notifica al núcleo familiar y a otros contactos tu estado o si te estás haciendo cargo.
              </p>
            </div>

            <div className="space-y-4">
              <div>
                <label className="block text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-1.5 ml-1">Tu Nombre</label>
                <input
                  type="text"
                  placeholder="Ej: Juan Pérez"
                  value={contactName}
                  onChange={(e) => {
                    setContactName(e.target.value);
                    if (typeof window !== 'undefined') {
                      localStorage.setItem('contact_name', e.target.value);
                    }
                  }}
                  disabled={isSubmitting}
                  className="w-full bg-gray-50 text-gray-800 text-sm font-semibold p-3.5 rounded-xl border border-transparent focus:border-red-100 focus:bg-white focus:outline-none transition-all"
                />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                <button
                  onClick={() => handleRespond('read')}
                  disabled={isSubmitting || !contactName.trim()}
                  className={`
                    px-4 py-3.5 rounded-xl text-xs font-bold transition-all cursor-pointer flex items-center justify-center gap-2
                    ${!contactName.trim() 
                      ? 'bg-gray-100 text-gray-400 cursor-not-allowed' 
                      : 'bg-blue-50 text-blue-700 hover:bg-blue-100'}
                  `}
                >
                  👁️ Marcar como Leído
                </button>
                <button
                  onClick={() => handleRespond('acknowledged')}
                  disabled={isSubmitting || !contactName.trim()}
                  className={`
                    px-4 py-3.5 rounded-xl text-xs font-bold transition-all cursor-pointer flex items-center justify-center gap-2
                    ${!contactName.trim() 
                      ? 'bg-gray-100 text-gray-400 cursor-not-allowed' 
                      : 'bg-indigo-50 text-indigo-700 hover:bg-indigo-100'}
                  `}
                >
                  💬 Recibido / Enterado
                </button>
                <button
                  onClick={() => handleRespond('on_my_way')}
                  disabled={isSubmitting || !contactName.trim()}
                  className={`
                    px-4 py-3.5 rounded-xl text-xs font-bold transition-all cursor-pointer flex items-center justify-center gap-2
                    ${!contactName.trim() 
                      ? 'bg-gray-100 text-gray-400 cursor-not-allowed' 
                      : 'bg-emerald-50 text-emerald-800 hover:bg-emerald-100 shadow-sm'}
                  `}
                >
                  🚗 Voy en camino
                </button>
              </div>

              {submitSuccess && (
                <p className="text-xs font-bold text-emerald-800 bg-emerald-50 border border-emerald-100 p-3 rounded-xl text-center">
                  ¡Respuesta registrada con éxito!
                </p>
              )}

              {submitError && (
                <p className="text-xs font-bold text-red-800 bg-red-50 border border-red-100 p-3 rounded-xl text-center">
                  {submitError}
                </p>
              )}
            </div>

            <hr className="border-gray-100" />

            {data.share_contact_responses ? (
              data.responses && data.responses.length > 0 ? (
                <div className="space-y-3">
                  <h4 className="text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-1.5 ml-1">Respuestas registradas</h4>
                  <div className="space-y-2 max-h-60 overflow-y-auto pr-1">
                    {data.responses.map((resp) => (
                      <div key={resp.id} className="flex items-center justify-between p-3 bg-gray-50 rounded-xl border border-gray-100">
                        <div className="flex items-center gap-3">
                          <div className="w-8 h-8 rounded-full flex items-center justify-center text-sm bg-white border border-gray-200 shadow-sm">
                            {resp.status === 'on_my_way' ? '🚗' : resp.status === 'acknowledged' ? '👍' : '👁️'}
                          </div>
                          <div>
                            <p className="text-sm font-bold text-gray-800">{resp.contact_name}</p>
                            <p className="text-[11px] text-gray-505">
                              {resp.status === 'on_my_way' ? 'Voy en camino' : resp.status === 'acknowledged' ? 'Recibido / Enterado' : 'Visto / Leído'}
                            </p>
                          </div>
                        </div>
                        <span className="text-[10px] font-semibold text-gray-400">
                          {formatDateTime(resp.created_at)}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              ) : (
                <p className="text-[11px] text-gray-400 italic text-center">
                  Aún no hay respuestas de otros contactos. Sé el primero en avisar.
                </p>
              )
            ) : (
              <div className="p-3.5 bg-gray-50 rounded-xl border border-gray-100 text-center">
                <p className="text-[11px] text-gray-400 italic">
                  🔒 Por configuración de privacidad, no se muestran las respuestas de otros contactos.
                </p>
              </div>
            )}
          </section>
        )}

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
