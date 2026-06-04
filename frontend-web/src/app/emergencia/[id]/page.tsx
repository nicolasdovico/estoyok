import dynamic from 'next/dynamic';

const EmergencyMap = dynamic(() => import('@/components/EmergencyMap'), {
  ssr: false,
  loading: () => <div className="h-full w-full bg-gray-100 flex items-center justify-center">Cargando mapa...</div>
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

async function getEmergencyData(id: string): Promise<EmergencyData | null> {
  const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://estoyok-backend/api';
  
  try {
    const res = await fetch(`${apiUrl}/emergency-alerts/${id}`, {
      cache: 'no-store'
    });
    
    if (!res.ok) return null;
    return res.json();
  } catch (error) {
    console.error("Error fetching emergency data:", error);
    return null;
  }
}

export default async function EmergencyPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  const data = await getEmergencyData(id);

  if (!data) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 p-4">
        <div className="max-w-md w-full bg-white rounded-2xl shadow-xl p-8 text-center border-t-4 border-red-500">
          <h1 className="text-2xl font-bold text-gray-900 mb-4">Alerta No Encontrada</h1>
          <p className="text-gray-600 mb-6">
            El enlace de emergencia puede haber expirado o es inválido. 
            Por favor, intente contactar al familiar directamente.
          </p>
          <div className="w-16 h-16 bg-red-100 text-red-600 rounded-full flex items-center justify-center mx-auto mb-6">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-8 h-8">
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.34c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126ZM12 15.75h.007v.008H12v-.008Z" />
            </svg>
          </div>
        </div>
      </div>
    );
  }

  const lastCheckIn = new Date(data.last_check_in_at).toLocaleString('es-AR');
  const lastLocationTime = data.location ? new Date(data.location.updated_at).toLocaleString('es-AR') : 'No disponible';

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      {/* Header Crítico */}
      <header className="bg-red-600 text-white p-4 shadow-lg sticky top-0 z-50">
        <div className="max-w-4xl mx-auto flex items-center justify-between">
          <div>
            <h1 className="text-xl font-bold">ALERTA DE SEGURIDAD</h1>
            <p className="text-sm opacity-90">Protocolo de Emergencia Activado</p>
          </div>
          <div className="bg-white text-red-600 px-3 py-1 rounded-full text-xs font-bold animate-pulse">
            VIVO
          </div>
        </div>
      </header>

      <main className="flex-1 max-w-4xl w-full mx-auto p-4 md:p-6 space-y-6">
        {/* Card de Información Principal */}
        <section className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <div>
              <h2 className="text-3xl font-extrabold text-gray-900">{data.user_name}</h2>
              <p className="text-gray-500 mt-1">
                Estado: <span className="font-semibold text-red-600 uppercase">{data.status}</span>
              </p>
            </div>
            <div className="flex gap-2">
              <a 
                href={`tel:${data.user_name}`} // En un caso real tendríamos el teléfono del usuario aquí
                className="bg-red-600 hover:bg-red-700 text-white px-6 py-3 rounded-xl font-bold transition-colors flex items-center gap-2 justify-center"
              >
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" className="w-5 h-5">
                  <path fillRule="evenodd" d="M1.5 4.5a3 3 0 013-3h1.372c.86 0 1.61.586 1.819 1.42l1.105 4.423a1.875 1.875 0 01-.694 1.915l-2.26 1.81a11.214 11.214 0 006.985 6.985l1.81-2.26a1.875 1.875 0 011.915-.694l4.423 1.105c.834.209 1.42.959 1.42 1.82V19.5a3 3 0 01-3 3h-2.25C8.552 22.5 1.5 15.448 1.5 6.75V4.5z" clipRule="evenodd" />
                </svg>
                Llamar
              </a>
            </div>
          </div>

          <hr className="my-6 border-gray-100" />

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 text-sm">
            <div className="bg-gray-50 p-4 rounded-xl">
              <p className="text-gray-500 mb-1">Último Reporte Voluntario</p>
              <p className="text-lg font-bold text-gray-800">{lastCheckIn}</p>
            </div>
            <div className="bg-gray-50 p-4 rounded-xl">
              <p className="text-gray-500 mb-1">Última Ubicación GPS</p>
              <p className="text-lg font-bold text-gray-800">{lastLocationTime}</p>
            </div>
          </div>
        </section>

        {/* Mapa */}
        <section className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden flex-1 min-h-[400px] flex flex-col">
          <div className="p-4 border-b border-gray-100 flex items-center justify-between">
            <h3 className="font-bold text-gray-800 flex items-center gap-2">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" className="w-5 h-5 text-red-600">
                <path fillRule="evenodd" d="m11.54 22.351.07.04.028.016a.76.76 0 0 0 .723 0l.028-.015.071-.041a16.975 16.975 0 0 0 1.144-.742 19.58 19.58 0 0 0 2.683-2.282c1.944-1.99 3.963-4.98 3.963-8.827a8.25 8.25 0 0 0-16.5 0c0 3.846 2.02 6.837 3.963 8.827a19.58 19.58 0 0 0 2.682 2.282 16.975 16.975 0 0 0 1.145.742ZM12 13.5a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z" clipRule="evenodd" />
              </svg>
              Mapa de Rastreo
            </h3>
            {data.location && (
              <a 
                href={`https://www.google.com/maps/search/?api=1&query=${data.location.latitude},${data.location.longitude}`}
                target="_blank"
                rel="noopener noreferrer"
                className="text-xs text-blue-600 font-bold hover:underline"
              >
                Abrir en Google Maps
              </a>
            )}
          </div>
          <div className="flex-1 relative">
            {data.location && typeof data.location.latitude === 'number' && typeof data.location.longitude === 'number' ? (
              <EmergencyMap center={[data.location.latitude, data.location.longitude]} />
            ) : (
              <div className="absolute inset-0 bg-gray-100 flex flex-col items-center justify-center text-center p-6">
                <p className="text-gray-500 font-medium">Ubicación no disponible en este momento</p>
                <p className="text-xs text-gray-400 mt-2">El dispositivo del usuario podría estar apagado o sin señal.</p>
              </div>
            )}
          </div>
        </section>

        {/* Footer Informativo */}
        <footer className="text-center pb-8">
          <p className="text-xs text-gray-400">
            Powered by <strong>Estoy Ok</strong> - Plataforma de Seguridad Familiar
          </p>
        </footer>
      </main>
    </div>
  );
}
